package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardSubTypes;
import ezvcard.property.TelephoneType;
import ezvcard.util.HCardElement;
import ezvcard.util.TelUri;

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
 * Marshals {@link TelephoneType} properties.
 * @author Michael Angstadt
 */
public class TelephoneScribe extends VCardPropertyScribe<TelephoneType> {
	public TelephoneScribe() {
		super(TelephoneType.class, "TEL");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected VCardDataType _dataType(TelephoneType property, VCardVersion version) {
		if (version == VCardVersion.V4_0) {
			if (property.getText() != null) {
				return VCardDataType.TEXT;
			}
			if (property.getUri() != null) {
				return VCardDataType.URI;
			}
		}

		return VCardDataType.TEXT;
	}

	@Override
	protected void _prepareParameters(TelephoneType property, VCardSubTypes copy, VCardVersion version, VCard vcard) {
		handlePrefParam(property, copy, version, vcard);
	}

	@Override
	protected String _writeText(TelephoneType property, VCardVersion version) {
		String text = property.getText();
		if (text != null) {
			return escape(text);
		}

		TelUri uri = property.getUri();
		if (uri != null) {
			if (version == VCardVersion.V4_0) {
				return uri.toString();
			}

			String ext = uri.getExtension();
			if (ext == null) {
				return escape(uri.getNumber());
			}
			return escape(uri.getNumber() + " x" + ext);
		}

		return "";
	}

	@Override
	protected TelephoneType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value, dataType, warnings);
	}

	@Override
	protected void _writeXml(TelephoneType property, XCardElement parent) {
		String text = property.getText();
		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}

		TelUri uri = property.getUri();
		if (uri != null) {
			parent.append(VCardDataType.URI, uri.toString());
			return;
		}

		parent.append(VCardDataType.TEXT, "");
	}

	@Override
	protected TelephoneType _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		String text = element.first(VCardDataType.TEXT);
		if (text != null) {
			return new TelephoneType(text);
		}

		String uri = element.first(VCardDataType.URI);
		if (uri != null) {
			try {
				return new TelephoneType(TelUri.parse(uri));
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
				return new TelephoneType(uri);
			}
		}

		throw missingXmlElements(VCardDataType.TEXT, VCardDataType.URI);
	}

	@Override
	protected TelephoneType _parseHtml(HCardElement element, List<String> warnings) {
		TelephoneType property;
		String href = element.attr("href");
		try {
			property = new TelephoneType(TelUri.parse(href));
		} catch (IllegalArgumentException e) {
			//not a tel URI
			property = new TelephoneType(element.value());
		}

		List<String> types = element.types();
		for (String type : types) {
			property.getSubTypes().addType(type);
		}

		return property;
	}

	@Override
	protected JCardValue _writeJson(TelephoneType property) {
		String text = property.getText();
		if (text != null) {
			return JCardValue.single(text);
		}

		TelUri uri = property.getUri();
		if (uri != null) {
			return JCardValue.single(uri.toString());
		}

		return JCardValue.single("");
	}

	@Override
	protected TelephoneType _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		String valueStr = value.asSingle();
		return parse(valueStr, dataType, warnings);
	}

	private TelephoneType parse(String value, VCardDataType dataType, List<String> warnings) {
		try {
			return new TelephoneType(TelUri.parse(value));
		} catch (IllegalArgumentException e) {
			if (dataType == VCardDataType.URI) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
			}
		}

		return new TelephoneType(value);
	}
}
