package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.json.JsonValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.io.xml.XCardElement.XCardValue;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.RawProperty;

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
 * Marshals {@link RawProperty} properties.
 * @author Michael Angstadt
 */
/*
 * Note concerning escaping and unescaping special characters:
 * 
 * Values are not escaped and unescaped for the following reason: If the
 * extended property's value is a list or structured list, then the escaping
 * must be preserved or else escaped special characters will be lost.
 * 
 * This is an inconvenience, considering the fact that most extended properties
 * contain simple text values. But it is necessary in order to prevent data
 * loss.
 */
public class RawPropertyScribe extends VCardPropertyScribe<RawProperty> {
	public RawPropertyScribe(String propertyName) {
		super(RawProperty.class, propertyName);
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return null;
	}

	@Override
	protected VCardDataType _dataType(RawProperty property, VCardVersion version) {
		return property.getDataType();
	}

	@Override
	protected String _writeText(RawProperty property, WriteContext context) {
		String value = property.getValue();
		return (value == null) ? "" : value;
	}

	@Override
	protected RawProperty _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		RawProperty property = new RawProperty(propertyName, value);
		property.setDataType(dataType);
		return property;
	}

	@Override
	protected RawProperty _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		XCardValue firstValue = element.firstValue();
		VCardDataType dataType = firstValue.getDataType();
		String value = firstValue.getValue();

		RawProperty property = new RawProperty(propertyName, value);
		property.setDataType(dataType);
		return property;
	}

	@Override
	protected RawProperty _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		String valueStr = jcardValueToString(value);

		RawProperty property = new RawProperty(propertyName, valueStr);
		property.setDataType(dataType);
		return property;
	}

	@Override
	protected RawProperty _parseHtml(HCardElement element, List<String> warnings) {
		String value = element.value();

		return new RawProperty(propertyName, value);
	}

	private static String jcardValueToString(JCardValue value) {
		/*
		 * VCardPropertyScribe.jcardValueToString() cannot be used because it
		 * escapes single values.
		 */
		List<JsonValue> values = value.getValues();
		if (values.size() > 1) {
			List<String> multi = value.asMulti();
			if (!multi.isEmpty()) {
				return list(multi);
			}
		}

		if (!values.isEmpty() && values.get(0).getArray() != null) {
			List<List<String>> structured = value.asStructured();
			if (!structured.isEmpty()) {
				return structured(structured.toArray());
			}
		}

		return value.asSingle();
	}
}
