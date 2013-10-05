package ezvcard.types.scribes;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.types.GenderType;

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
 * Marshals {@link GenderType} properties.
 * @author Michael Angstadt
 */
public class GenderScribe extends VCardPropertyScribe<GenderType> {
	public GenderScribe() {
		super(GenderType.class, "GENDER");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(GenderType property, VCardVersion version) {
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
	protected GenderType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		SemiStructuredIterator it = semistructured(value, 2);

		String sex = it.next();
		if (sex != null) {
			sex = sex.toUpperCase();
		}
		String text = it.next();

		GenderType property = new GenderType(sex);
		property.setText(text);
		return property;
	}

	@Override
	protected void _writeXml(GenderType property, XCardElement parent) {
		parent.append("sex", property.getGender());

		String text = property.getText();
		if (text != null) {
			parent.append("identity", text);
		}
	}

	@Override
	protected GenderType _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		String sex = element.first("sex");
		if (sex != null) {
			GenderType property = new GenderType(sex);
			property.setText(element.first("identity")); //optional field
			return property;
		}

		throw missingXmlElements("sex");
	}

	@Override
	protected JCardValue _writeJson(GenderType property) {
		String gender = property.getGender();
		String text = property.getText();

		if (text == null) {
			return JCardValue.single(gender);
		}
		return JCardValue.structured(gender, text);
	}

	@Override
	protected GenderType _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		StructuredIterator it = structured(value);

		String sex = it.nextString();
		if (sex != null) {
			sex = sex.toUpperCase();
		}
		String text = it.nextString();

		GenderType property = new GenderType(sex);
		property.setText(text);
		return property;
	}
}
