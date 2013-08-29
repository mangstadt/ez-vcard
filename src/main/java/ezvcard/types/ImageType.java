package ezvcard.types;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ezvcard.io.SkipMeException;
import ezvcard.parameters.ImageTypeParameter;
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
 * Represents a vCard type that stores image data (for example,
 * {@link PhotoType}).
 * @author Michael Angstadt
 */
public class ImageType extends BinaryType<ImageTypeParameter> {
	/**
	 * Creates an image property.
	 * @param typeName the type name (e.g. "PHOTO")
	 */
	public ImageType(String typeName) {
		super(typeName);
	}

	/**
	 * Creates an image property.
	 * @param typeName the type name (e.g. "PHOTO")
	 * @param url the URL to the image
	 * @param type the content type (e.g. JPEG)
	 */
	public ImageType(String typeName, String url, ImageTypeParameter type) {
		super(typeName, url, type);
	}

	/**
	 * Creates an image property.
	 * @param typeName the type name (e.g. "PHOTO")
	 * @param data the binary data of the image
	 * @param type the content type (e.g. JPEG)
	 */
	public ImageType(String typeName, byte[] data, ImageTypeParameter type) {
		super(typeName, data, type);
	}

	/**
	 * Creates an image property.
	 * @param typeName the type name (e.g. "PHOTO")
	 * @param in an input stream to the binary data (will be closed)
	 * @param type the content type (e.g. JPEG)
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public ImageType(String typeName, InputStream in, ImageTypeParameter type) throws IOException {
		super(typeName, in, type);
	}

	/**
	 * Creates an image property.
	 * @param typeName the type name (e.g. "PHOTO")
	 * @param file the image file
	 * @param type the content type (e.g. JPEG)
	 * @throws IOException if there's a problem reading from the file
	 */
	public ImageType(String typeName, File file, ImageTypeParameter type) throws IOException {
		super(typeName, file, type);
	}

	@Override
	protected ImageTypeParameter buildTypeObj(String type) {
		return ImageTypeParameter.get(type, null, null);
	}

	@Override
	protected ImageTypeParameter buildMediaTypeObj(String mediaType) {
		return ImageTypeParameter.get(null, mediaType, null);
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if (!"img".equals(elementName)) {
			super.doUnmarshalHtml(element, warnings);
			return;
		}

		String src = element.absUrl("src");
		if (src.length() == 0) {
			throw new SkipMeException("<img> tag does not have a \"src\" attribute.");
		}

		try {
			DataUri uri = new DataUri(src);
			ImageTypeParameter mediaType = buildMediaTypeObj(uri.getContentType());
			setData(uri.getData(), mediaType);
		} catch (IllegalArgumentException e) {
			//not a data URI
			//TODO create buildTypeObjFromExtension() method
			setUrl(src, null);
		}
	}
}
