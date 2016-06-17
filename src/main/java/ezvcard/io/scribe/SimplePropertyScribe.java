package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;

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
 * Marshals properties that have just a single value thats need no parsing or
 * writing logic and that always has the same data type.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class SimplePropertyScribe<T extends VCardProperty> extends VCardPropertyScribe<T> {
	protected final VCardDataType dataType;

	public SimplePropertyScribe(Class<T> clazz, String propertyName, VCardDataType dataType) {
		super(clazz, propertyName);
		this.dataType = dataType;
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return dataType;
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		String value = _writeValue(property);
		return (value == null) ? "" : escape(value);
	}

	@Override
	protected T _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		value = unescape(value);
		return _parseValue(value);
	}

	@Override
	protected void _writeXml(T property, XCardElement parent) {
		parent.append(dataType, _writeValue(property));
	}

	@Override
	protected T _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String value = element.first(dataType);
		if (value != null) {
			return _parseValue(value);
		}

		throw super.missingXmlElements(dataType);
	}

	@Override
	protected T _parseHtml(HCardElement element, List<String> warnings) {
		String value = element.value();
		return _parseValue(value);
	}

	@Override
	protected JCardValue _writeJson(T property) {
		String value = _writeValue(property);
		if (value == null) {
			value = "";
		}

		return JCardValue.single(value);
	}

	@Override
	protected T _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		String valueStr = value.asSingle();
		return _parseValue(valueStr);
	}

	/**
	 * Writes the property value to a string.
	 * @param property the property to write
	 * @return the property value
	 */
	protected abstract String _writeValue(T property);

	/**
	 * Parses the property from a string.
	 * @param value the property value
	 * @return the parsed property object
	 */
	protected abstract T _parseValue(String value);
}
