package ezvcard.types.scribes;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.RelatedType;
import ezvcard.util.JCardValue;
import ezvcard.util.XCardElement;

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
 * Marshals {@link RelatedType} properties.
 * @author Michael Angstadt
 */
public class RelatedScribe extends VCardPropertyScribe<RelatedType> {
	public RelatedScribe() {
		super(RelatedType.class, "RELATED");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.URI;
	}

	@Override
	protected VCardDataType _dataType(RelatedType property, VCardVersion version) {
		if (property.getUri() != null) {
			return VCardDataType.URI;
		}
		if (property.getText() != null) {
			return VCardDataType.TEXT;
		}
		return VCardDataType.URI;
	}

	@Override
	protected String _writeText(RelatedType property, VCardVersion version) {
		String uri = property.getUri();
		if (uri != null) {
			return uri;
		}

		String text = property.getText();
		if (text != null) {
			return escape(text);
		}

		return "";
	}

	@Override
	protected RelatedType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		value = unescape(value);

		RelatedType property = new RelatedType();
		if (dataType == VCardDataType.TEXT) {
			property.setText(value);
		} else {
			property.setUri(value);
		}
		return property;
	}

	@Override
	protected void _writeXml(RelatedType property, XCardElement parent) {
		String uri = property.getUri();
		if (uri != null) {
			parent.append(VCardDataType.URI, uri);
			return;
		}

		String text = property.getText();
		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}

		parent.append(VCardDataType.URI, "");
	}

	@Override
	protected RelatedType _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		String uri = element.first(VCardDataType.URI);
		if (uri != null) {
			RelatedType property = new RelatedType();
			property.setUri(uri);
			return property;
		}

		String text = element.first(VCardDataType.TEXT);
		if (text != null) {
			RelatedType property = new RelatedType();
			property.setText(text);
			return property;
		}

		throw missingXmlElements(VCardDataType.URI, VCardDataType.TEXT);
	}

	@Override
	protected JCardValue _writeJson(RelatedType property) {
		String uri = property.getUri();
		if (uri != null) {
			return JCardValue.single(VCardDataType.URI, uri);
		}

		String text = property.getText();
		if (text != null) {
			return JCardValue.single(VCardDataType.TEXT, text);
		}

		return JCardValue.single(VCardDataType.URI, "");
	}

	@Override
	protected RelatedType _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		String valueStr = value.asSingle();

		RelatedType property = new RelatedType();
		if (dataType == VCardDataType.TEXT) {
			property.setText(valueStr);
		} else {
			property.setUri(valueStr);
		}
		return property;
	}
}
