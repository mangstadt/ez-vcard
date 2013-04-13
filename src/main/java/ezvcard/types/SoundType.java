package ezvcard.types;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ezvcard.VCardSubTypes;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.SoundTypeParameter;
import ezvcard.util.DataUri;
import ezvcard.util.HCardElement;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * A sound to attach to the vCard, such as a pronunciation of the person's name.
 * 
 * <p>
 * <b>Adding a sound</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * //URL
 * SoundType sound = new SoundType("http://www.mywebsite.com/myname.ogg", SoundTypeParameter.OGG);
 * vcard.addSound(sound);
 * 
 * //binary data
 * byte data[] = ...
 * sound = new SoundType(data, SoundTypeParameter.OGG);
 * vcard.addSound(sound);
 * 
 * //if "SoundTypeParameter" does not have the pre-defined constant that you need, then create a new instance
 * //arg 1: the value of the 2.1/3.0 TYPE parameter
 * //arg 2: the value to use for the 4.0 MEDIATYPE parameter and for 4.0 data URIs
 * //arg 3: the file extension of the data type (optional)
 * SoundTypeParameter param = new SoundTypeParameter("wav", "audio/wav", "wav");
 * sound = new SoundType("http://www.mywebsite.com/myname.wav", SoundTypeParameter.WAV);
 * vcard.addSound(sound);
 * </pre>
 * 
 * <p>
 * <b>Getting the sounds</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = ...
 * 
 * int fileCount = 0;
 * for (SoundType sound : vcard.getSounds()){
 *   //the sound will have either a URL or a binary data
 *   if (sound.getData() == null){
 *     System.out.println("Sound URL: " + sound.getUrl());
 *   } else {
 *     SoundTypeParameter type = sound.getContentType();
 *     
 *     if (type == null) {
 *       //the vCard may not have any content type data associated with the sound
 *       System.out.println("Saving a sound file...");
 *     } else {
 *       System.out.println("Saving a \"" + type.getMediaType() + "\" file...");
 *     }
 *     
 *     String folder;
 *     if (type == SoundTypeParameter.OGG){ //it is safe to use "==" instead of "equals()"
 *       folder = "ogg-files";
 *     } else {
 *       folder = "sound-files";
 *     }
 *     
 *     byte data[] = sound.getData();
 *     String filename = "sound" + fileCount;
 *     if (type != null && type.getExtension() != null){
 *     	filename += "." + type.getExtension();
 *     }
 *     OutputStream out = new FileOutputStream(new File(folder, filename));
 *     out.write(data);
 *     out.close();
 *     fileCount++;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>SOUND</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class SoundType extends BinaryType<SoundTypeParameter> {
	public static final String NAME = "SOUND";

	public SoundType() {
		super(NAME);
	}

	/**
	 * @param url the URL to the sound file
	 * @param type the content type (e.g. OGG)
	 */
	public SoundType(String url, SoundTypeParameter type) {
		super(NAME, url, type);
	}

	/**
	 * @param data the binary data of the sound file
	 * @param type the content type (e.g. OGG)
	 */
	public SoundType(byte[] data, SoundTypeParameter type) {
		super(NAME, data, type);
	}

	/**
	 * @param in an input stream to the binary data (will be closed)
	 * @param type the content type (e.g. OGG)
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public SoundType(InputStream in, SoundTypeParameter type) throws IOException {
		super(NAME, in, type);
	}

	/**
	 * @param file the sound file
	 * @param type the content type (e.g. OGG)
	 * @throws IOException if there's a problem reading from the file
	 */
	public SoundType(File file, SoundTypeParameter type) throws IOException {
		super(NAME, file, type);
	}

	/**
	 * Gets the language.
	 * @return the language or null if not set
	 * @see VCardSubTypes#getLanguage
	 */
	public String getLanguage() {
		return subTypes.getLanguage();
	}

	/**
	 * Sets the language.
	 * @param language the language or null to remove
	 * @see VCardSubTypes#setLanguage
	 */
	public void setLanguage(String language) {
		subTypes.setLanguage(language);
	}

	@Override
	protected SoundTypeParameter buildTypeObj(String type) {
		SoundTypeParameter param = SoundTypeParameter.valueOf(type);
		if (param == null) {
			param = new SoundTypeParameter(type, "audio/" + type, null);
		}
		return param;
	}

	@Override
	protected SoundTypeParameter buildMediaTypeObj(String mediaType) {
		SoundTypeParameter p = SoundTypeParameter.findByMediaType(mediaType);
		if (p == null) {
			int slashPos = mediaType.indexOf('/');
			String type;
			if (slashPos == -1 || slashPos < mediaType.length() - 1) {
				type = "";
			} else {
				type = mediaType.substring(slashPos + 1);
			}
			p = new SoundTypeParameter(type, mediaType, null);
		}
		return p;
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if ("audio".equals(elementName)) {
			org.jsoup.nodes.Element sourceElement = element.getElement().getElementsByTag("source").first();
			if (sourceElement == null) {
				throw new SkipMeException("No <source> element found beneath <audio> element.");
			}
		}
		if ("source".equals(elementName)) {
			SoundTypeParameter mediaType = null;
			String type = element.attr("type");
			if (type.length() > 0) {
				mediaType = buildMediaTypeObj(type);
			}

			String src = element.absUrl("src");
			if (src.length() > 0) {
				try {
					DataUri uri = new DataUri(src);
					mediaType = buildMediaTypeObj(uri.getContentType());
					setData(uri.getData(), mediaType);
				} catch (IllegalArgumentException e) {
					//TODO create buildTypeObjFromExtension() method
					setUrl(src, null);
				}
			} else {
				throw new SkipMeException("<source> tag does not have a \"src\" attribute.");
			}
		} else {
			super.doUnmarshalHtml(element, warnings);
		}
	}
}
