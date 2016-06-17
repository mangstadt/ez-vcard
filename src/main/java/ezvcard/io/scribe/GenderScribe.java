package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Gender;

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
 * Marshals {@link Gender} properties.
 * @author Michael Angstadt
 */
public class GenderScribe extends VCardPropertyScribe<Gender> {
	public GenderScribe() {
		super(Gender.class, "GENDER");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(Gender property, WriteContext context) {
		String gender = property.getGender();
		String text = property.getText();

		if (text != null) {
			return structured(gender, text);
		}
		if (gender != null) {
			return structured(new Object[] { gender });
		}
		return "";
	}

	@Override
	protected Gender _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		SemiStructuredIterator it = semistructured(value, 2);

		String sex = it.next();
		if (sex != null) {
			if (sex.length() == 0) {
				sex = null;
			} else {
				sex = sex.toUpperCase();
			}
		}
		String text = it.next();

		Gender property = new Gender(sex);
		property.setText(text);
		return property;
	}

	@Override
	protected void _writeXml(Gender property, XCardElement parent) {
		parent.append("sex", property.getGender());

		String text = property.getText();
		if (text != null) {
			parent.append("identity", text);
		}
	}

	@Override
	protected Gender _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String sex = element.first("sex");
		if (sex != null) {
			Gender property = new Gender(sex);
			property.setText(element.first("identity")); //optional field
			return property;
		}

		throw missingXmlElements("sex");
	}

	@Override
	protected JCardValue _writeJson(Gender property) {
		String gender = property.getGender();
		String text = property.getText();

		if (text == null) {
			return JCardValue.single(gender);
		}
		return JCardValue.structured(gender, text);
	}

	@Override
	protected Gender _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		StructuredIterator it = structured(value);

		String sex = it.nextString();
		if (sex != null) {
			sex = sex.toUpperCase();
		}
		String text = it.nextString();

		Gender property = new Gender(sex);
		property.setText(text);
		return property;
	}
}
