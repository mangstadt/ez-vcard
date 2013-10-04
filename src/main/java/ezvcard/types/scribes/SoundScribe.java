package ezvcard.types.scribes;

import java.util.List;

import ezvcard.io.CannotParseException;
import ezvcard.parameters.SoundTypeParameter;
import ezvcard.types.SoundType;
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
 */

/**
 * Marshals {@link SoundType} properties.
 * @author Michael Angstadt
 */
public class SoundScribe extends BinaryPropertyScribe<SoundType, SoundTypeParameter> {
	public SoundScribe() {
		super(SoundType.class, "SOUND");
	}

	@Override
	protected SoundTypeParameter _buildTypeObj(String type) {
		return SoundTypeParameter.get(type, null, null);
	}

	@Override
	protected SoundTypeParameter _buildMediaTypeObj(String mediaType) {
		return SoundTypeParameter.get(null, mediaType, null);
	}

	@Override
	protected SoundType _newInstance(String uri, SoundTypeParameter contentType) {
		return new SoundType(uri, contentType);
	}

	@Override
	protected SoundType _newInstance(byte[] data, SoundTypeParameter contentType) {
		return new SoundType(data, contentType);
	}

	@Override
	protected SoundType _parseHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if (!"audio".equals(elementName) && !"source".equals(elementName)) {
			return super._parseHtml(element, warnings);
		}

		if ("audio".equals(elementName)) {
			//parse its child "<source>" element
			org.jsoup.nodes.Element source = element.getElement().getElementsByTag("source").first();
			if (source == null) {
				throw new CannotParseException("No <source> tag found beneath <audio> tag.");
			}

			element = new HCardElement(source);
		}

		String src = element.absUrl("src");
		if (src.length() == 0) {
			throw new CannotParseException("<source> tag does not have a \"src\" attribute.");
		}

		String type = element.attr("type");
		SoundTypeParameter mediaType = (type.length() == 0) ? null : _buildMediaTypeObj(type);

		try {
			DataUri uri = new DataUri(src);
			mediaType = _buildMediaTypeObj(uri.getContentType());
			return new SoundType(uri.getData(), mediaType);
		} catch (IllegalArgumentException e) {
			//not a data URI
			//TODO create buildTypeObjFromExtension() method
			return new SoundType(src, null);
		}
	}
}
