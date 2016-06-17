package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.TextListProperty;

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
 * Marshals properties that contain a list of values.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class ListPropertyScribe<T extends TextListProperty> extends VCardPropertyScribe<T> {
	public ListPropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		return list(property.getValues());
	}

	@Override
	protected T _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		List<String> values = list(value);
		return parse(values);
	}

	@Override
	protected void _writeXml(T property, XCardElement parent) {
		parent.append(VCardDataType.TEXT.getName().toLowerCase(), property.getValues());
	}

	@Override
	protected T _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		List<String> values = element.all(VCardDataType.TEXT);
		if (!values.isEmpty()) {
			return parse(values);
		}

		throw missingXmlElements(VCardDataType.TEXT);
	}

	@Override
	protected JCardValue _writeJson(T property) {
		List<String> values = property.getValues();
		if (values.isEmpty()) {
			return JCardValue.single("");
		}

		return JCardValue.multi(values);
	}

	@Override
	protected T _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		List<String> values = value.asMulti();
		return parse(values);
	}

	private T parse(List<String> values) {
		T property = _newInstance();
		property.getValues().addAll(values);
		return property;
	}

	protected abstract T _newInstance();
}
