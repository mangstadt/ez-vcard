package ezvcard.io.scribe;

import java.util.List;

import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.parameter.SoundType;
import ezvcard.property.Sound;
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
 * Marshals {@link Sound} properties.
 * @author Michael Angstadt
 */
public class SoundScribe extends BinaryPropertyScribe<Sound, SoundType> {
	public SoundScribe() {
		super(Sound.class, "SOUND");
	}

	@Override
	protected SoundType _mediaTypeFromTypeParameter(String type) {
		return SoundType.get(type, null, null);
	}

	@Override
	protected SoundType _mediaTypeFromMediaTypeParameter(String mediaType) {
		return SoundType.get(null, mediaType, null);
	}

	@Override
	protected SoundType _mediaTypeFromFileExtension(String extension) {
		return SoundType.find(null, null, extension);
	}

	@Override
	protected Sound _newInstance(String uri, SoundType contentType) {
		return new Sound(uri, contentType);
	}

	@Override
	protected Sound _newInstance(byte[] data, SoundType contentType) {
		return new Sound(data, contentType);
	}

	@Override
	protected Sound _parseHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if (!"audio".equals(elementName) && !"source".equals(elementName)) {
			return super._parseHtml(element, warnings);
		}

		if ("audio".equals(elementName)) {
			//parse its child "<source>" element
			org.jsoup.nodes.Element source = element.getElement().getElementsByTag("source").first();
			if (source == null) {
				throw new CannotParseException(16);
			}

			element = new HCardElement(source);
		}

		String src = element.absUrl("src");
		if (src.length() == 0) {
			throw new CannotParseException(17);
		}

		String type = element.attr("type");
		SoundType mediaType = (type.length() == 0) ? null : _mediaTypeFromMediaTypeParameter(type);

		try {
			DataUri uri = DataUri.parse(src);
			mediaType = _mediaTypeFromMediaTypeParameter(uri.getContentType());
			return new Sound(uri.getData(), mediaType);
		} catch (IllegalArgumentException e) {
			//not a data URI
			if (mediaType == null) {
				String extension = getFileExtension(src);
				mediaType = (extension == null) ? null : _mediaTypeFromFileExtension(extension);
			}
			return new Sound(src, mediaType);
		}
	}
}
