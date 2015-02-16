package ezvcard.io.scribe;

import java.util.List;

import ezvcard.Messages;
import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Telephone;
import ezvcard.util.TelUri;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Marshals {@link Telephone} properties.
 * @author Michael Angstadt
 */
public class TelephoneScribe extends VCardPropertyScribe<Telephone> {
	public TelephoneScribe() {
		super(Telephone.class, "TEL");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected VCardDataType _dataType(Telephone property, VCardVersion version) {
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
	protected void _prepareParameters(Telephone property, VCardParameters copy, VCardVersion version, VCard vcard) {
		handlePrefParam(property, copy, version, vcard);
	}

	@Override
	protected String _writeText(Telephone property, VCardVersion version) {
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
	protected Telephone _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value, dataType, warnings);
	}

	@Override
	protected void _writeXml(Telephone property, XCardElement parent) {
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
	protected Telephone _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String text = element.first(VCardDataType.TEXT);
		if (text != null) {
			return new Telephone(text);
		}

		String uri = element.first(VCardDataType.URI);
		if (uri != null) {
			try {
				return new Telephone(TelUri.parse(uri));
			} catch (IllegalArgumentException e) {
				warnings.add(Messages.INSTANCE.getParseMessage(18));
				return new Telephone(uri);
			}
		}

		throw missingXmlElements(VCardDataType.TEXT, VCardDataType.URI);
	}

	@Override
	protected Telephone _parseHtml(HCardElement element, List<String> warnings) {
		Telephone property;
		String href = element.attr("href");
		try {
			property = new Telephone(TelUri.parse(href));
		} catch (IllegalArgumentException e) {
			//not a tel URI
			property = new Telephone(element.value());
		}

		List<String> types = element.types();
		for (String type : types) {
			property.getParameters().addType(type);
		}

		return property;
	}

	@Override
	protected JCardValue _writeJson(Telephone property) {
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
	protected Telephone _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		String valueStr = value.asSingle();
		return parse(valueStr, dataType, warnings);
	}

	private Telephone parse(String value, VCardDataType dataType, List<String> warnings) {
		try {
			return new Telephone(TelUri.parse(value));
		} catch (IllegalArgumentException e) {
			if (dataType == VCardDataType.URI) {
				warnings.add(Messages.INSTANCE.getParseMessage(18));
			}
		}

		return new Telephone(value);
	}
}
