package ezvcard.types;

import java.util.List;

import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.KeyTypeParameter;
import ezvcard.parameters.MediaTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardUtils;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * A public key for encryption.
 * 
 * <p>
 * <b>Adding a key</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * //URL (vCard 4.0 only; KEYs cannot have URLs in vCard 2.1 and 3.0)
 * KeyType key = new KeyType("http://www.mywebsite.com/pubkey.pgp", KeyTypeParameter.PGP);
 * vcard.addKey(key);
 * 
 * //binary data
 * byte data[] = ...
 * key = new KeyType(data, KeyTypeParameter.PGP);
 * vcard.addKey(key);
 * 
 * //plain text value
 * key = new KeyType();
 * key.setText("...", KeyTypeParameter.PGP);
 * vcard.addKey(key);
 * 
 * //if "KeyTypeParameter" does not have the pre-defined constant that you need, then create a new instance
 * //arg 1: the value of the 2.1/3.0 TYPE parameter
 * //arg 2: the value to use for the 4.0 MEDIATYPE parameter and for 4.0 data URIs
 * //arg 3: the file extension of the data type (optional)
 * KeyTypeParameter param = new KeyTypeParameter("mykey", "application/my-key", "mkey");
 * key = new KeyType("http://www.mywebsite.com/pubkey.enc", param);
 * vcard.addKey(key);
 * </pre>
 * 
 * <p>
 * <b>Getting the keys</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = ...
 * 
 * int fileCount = 0;
 * for (KeyType key : vcard.getKeys()){
 *   //the key will have either a URL or a binary data
 *   //only 4.0 vCards are allowed to use URLs for keys
 *   if (key.getData() == null){
 *     System.out.println("Key URL: " + key.getUrl());
 *   } else {
 *     KeyTypeParameter type = key.getContentType();
 *     
 *     if (type == null) {
 *       //the vCard may not have any content type data associated with the key
 *       System.out.println("Saving a key file...");
 *     } else {
 *       System.out.println("Saving a \"" + type.getMediaType() + "\" file...");
 *     }
 *     
 *     String folder;
 *     if (type == KeyTypeParameter.PGP){ //it is safe to use "==" instead of "equals()"
 *       folder = "pgp-keys";
 *     } else {
 *       folder = "other-keys";
 *     }
 *     
 *     byte data[] = key.getData();
 *     String filename = "key" + fileCount;
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
 * vCard property name: KEY
 * </p>
 * <p>
 * vCard versions: 2.1, 3.0, 4.0
 * </p>
 * @author Michael Angstadt
 */
public class KeyType extends BinaryType<KeyTypeParameter> {
	public static final String NAME = "KEY";

	private String text;

	public KeyType() {
		super(NAME);
	}

	/**
	 * @param data the binary data
	 * @param type the type of key (e.g. PGP)
	 */
	public KeyType(byte data[], KeyTypeParameter type) {
		super(NAME, data, type);
	}

	/**
	 * @param url the URL to the key (vCard 4.0 only)
	 * @param type the type of key (e.g. PGP)
	 */
	public KeyType(String url, KeyTypeParameter type) {
		super(NAME, url, type);
	}

	/**
	 * Sets a plain text representation of the key.
	 * @param text the key in plain text
	 * @param type the key type
	 */
	public void setText(String text, KeyTypeParameter type) {
		this.text = text;
		data = null;
		url = null;
		setContentType(type);
	}

	/**
	 * Gets the plain text representation of the key.
	 * @return the key in plain text
	 */
	public String getText() {
		return text;
	}

	@Override
	protected KeyTypeParameter buildTypeObj(String type) {
		KeyTypeParameter param = KeyTypeParameter.valueOf(type);
		if (param == null) {
			param = new KeyTypeParameter(type, "application/" + type, null);
		}
		return param;
	}

	@Override
	protected KeyTypeParameter buildMediaTypeObj(String mediaType) {
		KeyTypeParameter p = KeyTypeParameter.findByMediaType(mediaType);
		if (p == null) {
			int slashPos = mediaType.indexOf('/');
			String type;
			if (slashPos == -1 || slashPos < mediaType.length() - 1) {
				type = "";
			} else {
				type = mediaType.substring(slashPos + 1);
			}
			p = new KeyTypeParameter(type, mediaType, null);
		}
		return p;
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		if (text != null) {
			MediaTypeParameter contentType = getContentType();
			if (contentType == null) {
				contentType = new MediaTypeParameter(null, null, null);
			}

			copy.setValue(ValueParameter.TEXT);
			copy.setEncoding(null);
			if (version == VCardVersion.V4_0) {
				//don't null out TYPE, it could be set to "home" or "work"
				copy.setMediaType(contentType.getMediaType());
			} else {
				copy.setType(contentType.getValue());
				copy.setMediaType(null);
			}
		}
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		if (text != null) {
			sb.append(VCardStringUtils.escapeText(text));
		} else {
			if ((version == VCardVersion.V2_1 || version == VCardVersion.V3_0) && getUrl() != null) {
				warnings.add("vCard version " + version + " specs do not allow URLs to be used in the " + NAME + " type.");
			}
			super.doMarshalValue(sb, version, warnings, compatibilityMode);
		}
	}

	@Override
	protected void doMarshalValue(Element parent, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		if (text != null) {
			XCardUtils.appendChild(parent, "text", text, version);
		} else {
			if ((version == VCardVersion.V2_1 || version == VCardVersion.V3_0) && getUrl() != null) {
				warnings.add("vCard version " + version + " specs do not allow URLs to be used in the " + NAME + " type.");
			}
			super.doMarshalValue(parent, version, warnings, compatibilityMode);
		}
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		String value = XCardUtils.getFirstChildText(element, "uri");
		if (value != null) {
			super.doUnmarshalValue(value, version, warnings, compatibilityMode);
		} else {
			value = XCardUtils.getFirstChildText(element, "text");
			if (value != null) {
				String mediaType = subTypes.getMediaType();
				KeyTypeParameter contentType = (mediaType != null) ? buildMediaTypeObj(mediaType) : null;
				setText(value, contentType);
			}
		}
	}

	@Override
	protected void cannotUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, KeyTypeParameter contentType) {
		//unmarshal it as a plain text key
		setText(VCardStringUtils.unescape(value), contentType);
	}
}
