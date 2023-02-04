package ezvcard.io.scribe;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.ParseContext;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.KeyType;
import ezvcard.parameter.MediaTypeParameter;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Key;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * Marshals {@link Key} properties.
 * @author Michael Angstadt
 */
public class KeyScribe extends BinaryPropertyScribe<Key, KeyType> {
	public KeyScribe() {
		super(Key.class, "KEY");
	}

	@Override
	protected VCardDataType _dataType(Key property, VCardVersion version) {
		/*
		 * Only include a "VALUE=TEXT" parameter if it's a 4.0 vCard.
		 */
		if (version == VCardVersion.V4_0 && property.getText() != null) {
			return VCardDataType.TEXT;
		}

		/*
		 * Always use the URI data type with URL/URIs for consistency (even
		 * though 2.1 technically only supports the "URL" data type).
		 */
		if (property.getUrl() != null) {
			return VCardDataType.URI;
		}

		return super._dataType(property, version);
	}

	@Override
	protected void _prepareParameters(Key property, VCardParameters copy, VCardVersion version, VCard vcard) {
		if (property.getText() != null) {
			MediaTypeParameter contentType = property.getContentType();
			if (contentType == null) {
				contentType = new MediaTypeParameter(null, null, null);
			}

			copy.setEncoding(null);

			switch (version) {
			case V2_1:
				copy.setType(contentType.getValue());
				copy.setMediaType(null);
				break;
			case V3_0:
				copy.setType(contentType.getValue());
				copy.setMediaType(null);
				break;
			case V4_0:
				copy.setMediaType(contentType.getMediaType());
				break;
			}

			return;
		}

		super._prepareParameters(property, copy, version, vcard);
	}

	@Override
	protected String _writeText(Key property, WriteContext context) {
		String text = property.getText();
		if (text != null) {
			return text;
		}

		return super._writeText(property, context);
	}

	@Override
	protected Key _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		value = VObjectPropertyValues.unescape(value);

		/*
		 * Parse as text if VALUE parameter is explicitly set to TEXT.
		 */
		if (dataType == VCardDataType.TEXT) {
			KeyType contentType = parseContentTypeFromValueAndParameters(value, parameters, context.getVersion());
			Key property = new Key();
			property.setText(value, contentType);
			return property;
		}

		return parse(value, dataType, parameters, context.getVersion());
	}

	@Override
	protected void _writeXml(Key property, XCardElement parent) {
		String text = property.getText();
		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}

		super._writeXml(property, parent);
	}

	@Override
	protected Key _parseXml(XCardElement element, VCardParameters parameters, ParseContext context) {
		String text = element.first(VCardDataType.TEXT);
		if (text != null) {
			KeyType contentType = parseContentTypeFromValueAndParameters(text, parameters, element.version());
			Key property = new Key();
			property.setText(text, contentType);
			return property;
		}

		String value = element.first(VCardDataType.URI);
		if (value != null) {
			return parse(value, VCardDataType.URI, parameters, element.version());
		}

		throw missingXmlElements(VCardDataType.URI, VCardDataType.TEXT);
	}

	@Override
	protected JCardValue _writeJson(Key property) {
		String text = property.getText();
		if (text != null) {
			return JCardValue.single(text);
		}

		return super._writeJson(property);
	}

	@Override
	protected Key _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		if (dataType == VCardDataType.TEXT) {
			String valueStr = value.asSingle();
			KeyType contentType = parseContentTypeFromValueAndParameters(valueStr, parameters, VCardVersion.V4_0);
			Key property = new Key();
			property.setText(valueStr, contentType);
			return property;
		}

		return super._parseJson(value, dataType, parameters, context);
	}

	@Override
	protected KeyType _mediaTypeFromTypeParameter(String type) {
		return KeyType.get(type, null, null);
	}

	@Override
	protected KeyType _mediaTypeFromMediaTypeParameter(String mediaType) {
		return KeyType.get(null, mediaType, null);
	}

	@Override
	protected KeyType _mediaTypeFromFileExtension(String extension) {
		return KeyType.find(null, null, extension);
	}

	@Override
	protected Key _newInstance(String uri, KeyType contentType) {
		return new Key(uri, contentType);
	}

	@Override
	protected Key _newInstance(byte[] data, KeyType contentType) {
		return new Key(data, contentType);
	}

	@Override
	protected Key cannotUnmarshalValue(String value, VCardVersion version, KeyType contentType) {
		switch (version) {
		case V2_1:
		case V3_0:
			/*
			 * It wasn't explicitly defined as a URI, and didn't have an
			 * ENCODING parameter, so treat it as text.
			 */
			Key key = new Key();
			key.setText(value, contentType);
			return key;
		case V4_0:
			/*
			 * It wasn't explicitly defined as a text value, and it could not be
			 * parsed as a data URI, so treat it as a generic URI.
			 */
			return new Key(value, contentType);
		}

		return null;
	}
}
