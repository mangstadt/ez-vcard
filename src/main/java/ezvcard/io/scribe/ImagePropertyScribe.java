package ezvcard.io.scribe;

import java.util.List;

import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.parameter.ImageType;
import ezvcard.property.ImageProperty;
import ezvcard.util.DataUri;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 */

/**
 * Marshals properties that contain images.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class ImagePropertyScribe<T extends ImageProperty> extends BinaryPropertyScribe<T, ImageType> {
	public ImagePropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}

	@Override
	protected ImageType _mediaTypeFromTypeParameter(String type) {
		return ImageType.get(type, null, null);
	}

	@Override
	protected ImageType _mediaTypeFromMediaTypeParameter(String mediaType) {
		return ImageType.get(null, mediaType, null);
	}

	@Override
	protected ImageType _mediaTypeFromFileExtension(String extension) {
		return ImageType.find(null, null, extension);
	}

	@Override
	protected T _parseHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if (!"img".equals(elementName)) {
			return super._parseHtml(element, warnings);
		}

		String src = element.absUrl("src");
		if (src.length() == 0) {
			throw new CannotParseException(13);
		}

		try {
			DataUri uri = DataUri.parse(src);
			ImageType mediaType = _mediaTypeFromMediaTypeParameter(uri.getContentType());
			return _newInstance(uri.getData(), mediaType);
		} catch (IllegalArgumentException e) {
			//not a data URI
			String extension = getFileExtension(src);
			ImageType mediaType = (extension == null) ? null : _mediaTypeFromFileExtension(extension);
			return _newInstance(src, mediaType);
		}
	}
}
