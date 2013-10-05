package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.property.OrganizationType;
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
 * Marshals {@link OrganizationType} properties.
 * @author Michael Angstadt
 */
public class OrganizationScribe extends VCardPropertyScribe<OrganizationType> {
	public OrganizationScribe() {
		super(OrganizationType.class, "ORG");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(OrganizationType property, VCardVersion version) {
		return structured(property.getValues());
	}

	@Override
	protected OrganizationType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		OrganizationType property = new OrganizationType();

		SemiStructuredIterator it = semistructured(value);
		while (it.hasNext()) {
			property.addValue(it.next());
		}

		return property;
	}

	@Override
	protected void _writeXml(OrganizationType property, XCardElement parent) {
		parent.append(VCardDataType.TEXT.getName().toLowerCase(), property.getValues());
	}

	@Override
	protected OrganizationType _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		List<String> values = element.all(VCardDataType.TEXT);
		if (!values.isEmpty()) {
			OrganizationType property = new OrganizationType();
			property.getValues().addAll(values);
			return property;
		}

		throw missingXmlElements(VCardDataType.TEXT);
	}

	@Override
	protected OrganizationType _parseHtml(HCardElement element, List<String> warnings) {
		OrganizationType property = new OrganizationType();

		String orgName = element.firstValue("organization-name");
		if (orgName != null) {
			property.addValue(orgName);
		}

		String orgUnit = element.firstValue("organization-unit");
		if (orgUnit != null) {
			property.addValue(orgUnit);
		}

		if (property.getValues().isEmpty()) {
			property.addValue(element.value());
		}

		return property;
	}

	@Override
	protected JCardValue _writeJson(OrganizationType property) {
		List<String> values = property.getValues();
		if (values.isEmpty()) {
			return JCardValue.single("");
		}

		if (values.size() == 1) {
			return JCardValue.single(values.get(0));
		}

		return JCardValue.structured(values);
	}

	@Override
	protected OrganizationType _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		OrganizationType property = new OrganizationType();

		StructuredIterator it = structured(value);
		while (it.hasNext()) {
			property.addValue(it.nextString());
		}

		return property;
	}
}
