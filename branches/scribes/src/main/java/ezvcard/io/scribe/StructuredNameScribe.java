package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardSubTypes;
import ezvcard.property.StructuredNameType;
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
 * Marshals {@link StructuredNameType} properties.
 * @author Michael Angstadt
 */
public class StructuredNameScribe extends VCardPropertyScribe<StructuredNameType> {
	public StructuredNameScribe() {
		super(StructuredNameType.class, "N");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(StructuredNameType property, VCardVersion version) {
		return structured(property.getFamily(), property.getGiven(), property.getAdditional(), property.getPrefixes(), property.getSuffixes());
	}

	@Override
	protected StructuredNameType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		StructuredNameType property = new StructuredNameType();
		StructuredIterator it = structured(value);

		property.setFamily(it.nextString());
		property.setGiven(it.nextString());
		property.getAdditional().addAll(it.nextComponent());
		property.getPrefixes().addAll(it.nextComponent());
		property.getSuffixes().addAll(it.nextComponent());

		return property;
	}

	@Override
	protected void _writeXml(StructuredNameType property, XCardElement parent) {
		parent.append("surname", property.getFamily()); //the XML element still needs to be printed if value == null
		parent.append("given", property.getGiven());
		parent.append("additional", property.getAdditional());
		parent.append("prefix", property.getPrefixes());
		parent.append("suffix", property.getSuffixes());
	}

	@Override
	protected StructuredNameType _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		StructuredNameType property = new StructuredNameType();

		property.setFamily(s(element.first("surname")));
		property.setGiven(s(element.first("given")));
		property.getAdditional().addAll(element.all("additional"));
		property.getPrefixes().addAll(element.all("prefix"));
		property.getSuffixes().addAll(element.all("suffix"));

		return property;
	}

	private String s(String value) {
		return (value == null || value.length() == 0) ? null : value;
	}

	@Override
	protected StructuredNameType _parseHtml(HCardElement element, List<String> warnings) {
		StructuredNameType property = new StructuredNameType();

		property.setFamily(s(element.firstValue("family-name")));
		property.setGiven(s(element.firstValue("given-name")));
		property.getAdditional().addAll(element.allValues("additional-name"));
		property.getPrefixes().addAll(element.allValues("honorific-prefix"));
		property.getSuffixes().addAll(element.allValues("honorific-suffix"));

		return property;
	}

	@Override
	protected JCardValue _writeJson(StructuredNameType property) {
		return JCardValue.structured(property.getFamily(), property.getGiven(), property.getAdditional(), property.getPrefixes(), property.getSuffixes());
	}

	@Override
	protected StructuredNameType _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		StructuredNameType property = new StructuredNameType();
		StructuredIterator it = structured(value);

		property.setFamily(it.nextString());
		property.setGiven(it.nextString());
		property.getAdditional().addAll(it.nextComponent());
		property.getPrefixes().addAll(it.nextComponent());
		property.getSuffixes().addAll(it.nextComponent());

		return property;
	}
}
