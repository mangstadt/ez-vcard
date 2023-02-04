package ezvcard.io.scribe;

import java.util.List;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.StructuredValueIterator;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.ParseContext;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Organization;

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
 * Marshals {@link Organization} properties.
 * @author Michael Angstadt
 */
public class OrganizationScribe extends VCardPropertyScribe<Organization> {
	public OrganizationScribe() {
		super(Organization.class, "ORG");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(Organization property, WriteContext context) {
		boolean escapeCommas = (context.getVersion() != VCardVersion.V2_1);
		return VObjectPropertyValues.writeSemiStructured(property.getValues(), escapeCommas, context.isIncludeTrailingSemicolons());
	}

	@Override
	protected Organization _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		Organization property = new Organization();

		List<String> values = VObjectPropertyValues.parseSemiStructured(value);
		property.getValues().addAll(values);

		return property;
	}

	@Override
	protected void _writeXml(Organization property, XCardElement parent) {
		parent.append(VCardDataType.TEXT.getName().toLowerCase(), property.getValues());
	}

	@Override
	protected Organization _parseXml(XCardElement element, VCardParameters parameters, ParseContext context) {
		List<String> values = element.all(VCardDataType.TEXT);
		if (!values.isEmpty()) {
			Organization property = new Organization();
			property.getValues().addAll(values);
			return property;
		}

		throw missingXmlElements(VCardDataType.TEXT);
	}

	@Override
	protected Organization _parseHtml(HCardElement element, ParseContext context) {
		Organization property = new Organization();

		String orgName = element.firstValue("organization-name");
		if (orgName != null) {
			property.getValues().add(orgName);
		}

		String orgUnit = element.firstValue("organization-unit");
		if (orgUnit != null) {
			property.getValues().add(orgUnit);
		}

		if (property.getValues().isEmpty()) {
			String value = element.value();
			if (value.length() > 0) {
				property.getValues().add(value);
			}
		}

		return property;
	}

	@Override
	protected JCardValue _writeJson(Organization property) {
		List<String> values = property.getValues();
		if (values.isEmpty()) {
			return JCardValue.single("");
		}

		if (values.size() == 1) {
			return JCardValue.single(values.get(0));
		}

		return JCardValue.structured(values.toArray(new Object[0]));
	}

	@Override
	protected Organization _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		Organization property = new Organization();

		StructuredValueIterator it = new StructuredValueIterator(value.asStructured());
		while (it.hasNext()) {
			String next = it.nextValue();
			if (next != null) {
				property.getValues().add(next);
			}
		}

		return property;
	}
}
