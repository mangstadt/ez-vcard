package ezvcard.io.scribe;

import java.util.List;

import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.parameter.ImageTypeParameter;
import ezvcard.property.ImageType;
import ezvcard.util.DataUri;

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
 */

/**
 * Marshals properties that contain images.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class ImagePropertyScribe<T extends ImageType> extends BinaryPropertyScribe<T, ImageTypeParameter> {
	public ImagePropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}

	@Override
	protected ImageTypeParameter _buildTypeObj(String type) {
		return ImageTypeParameter.get(type, null, null);
	}

	@Override
	protected ImageTypeParameter _buildMediaTypeObj(String mediaType) {
		return ImageTypeParameter.get(null, mediaType, null);
	}

	@Override
	protected T _parseHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if (!"img".equals(elementName)) {
			return super._parseHtml(element, warnings);
		}

		String src = element.absUrl("src");
		if (src.length() == 0) {
			throw new CannotParseException("<img> tag does not have a \"src\" attribute.");
		}

		try {
			DataUri uri = new DataUri(src);
			ImageTypeParameter mediaType = _buildMediaTypeObj(uri.getContentType());
			return _newInstance(uri.getData(), mediaType);
		} catch (IllegalArgumentException e) {
			//not a data URI
			//TODO create buildTypeObjFromExtension() method
			return _newInstance(src, null);
		}
	}
}
