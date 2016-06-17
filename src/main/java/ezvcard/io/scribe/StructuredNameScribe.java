package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.StructuredName;

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
 * Marshals {@link StructuredName} properties.
 * @author Michael Angstadt
 */
public class StructuredNameScribe extends VCardPropertyScribe<StructuredName> {
	public StructuredNameScribe() {
		super(StructuredName.class, "N");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(StructuredName property, WriteContext context) {
		//@formatter:off
		return structured(context.isIncludeTrailingSemicolons(), new Object[] {
			property.getFamily(),
			property.getGiven(),
			property.getAdditionalNames(),
			property.getPrefixes(),
			property.getSuffixes()
		});
		//@formatter:on
	}

	@Override
	protected StructuredName _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		StructuredName property = new StructuredName();
		StructuredIterator it = structured(value);

		property.setFamily(it.nextString());
		property.setGiven(it.nextString());
		property.getAdditionalNames().addAll(it.nextComponent());
		property.getPrefixes().addAll(it.nextComponent());
		property.getSuffixes().addAll(it.nextComponent());

		return property;
	}

	@Override
	protected void _writeXml(StructuredName property, XCardElement parent) {
		parent.append("surname", property.getFamily()); //the XML element still needs to be printed if value == null
		parent.append("given", property.getGiven());
		parent.append("additional", property.getAdditionalNames());
		parent.append("prefix", property.getPrefixes());
		parent.append("suffix", property.getSuffixes());
	}

	@Override
	protected StructuredName _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		StructuredName property = new StructuredName();

		property.setFamily(s(element.first("surname")));
		property.setGiven(s(element.first("given")));
		property.getAdditionalNames().addAll(element.all("additional"));
		property.getPrefixes().addAll(element.all("prefix"));
		property.getSuffixes().addAll(element.all("suffix"));

		return property;
	}

	private String s(String value) {
		return (value == null || value.length() == 0) ? null : value;
	}

	@Override
	protected StructuredName _parseHtml(HCardElement element, List<String> warnings) {
		StructuredName property = new StructuredName();

		property.setFamily(s(element.firstValue("family-name")));
		property.setGiven(s(element.firstValue("given-name")));
		property.getAdditionalNames().addAll(element.allValues("additional-name"));
		property.getPrefixes().addAll(element.allValues("honorific-prefix"));
		property.getSuffixes().addAll(element.allValues("honorific-suffix"));

		return property;
	}

	@Override
	protected JCardValue _writeJson(StructuredName property) {
		return JCardValue.structured(property.getFamily(), property.getGiven(), property.getAdditionalNames(), property.getPrefixes(), property.getSuffixes());
	}

	@Override
	protected StructuredName _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		StructuredName property = new StructuredName();
		StructuredIterator it = structured(value);

		property.setFamily(it.nextString());
		property.setGiven(it.nextString());
		property.getAdditionalNames().addAll(it.nextComponent());
		property.getPrefixes().addAll(it.nextComponent());
		property.getSuffixes().addAll(it.nextComponent());

		return property;
	}
}
