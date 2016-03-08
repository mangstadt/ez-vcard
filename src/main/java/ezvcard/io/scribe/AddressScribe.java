package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;

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
 * Marshals {@link Address} properties.
 * @author Michael Angstadt
 */
public class AddressScribe extends VCardPropertyScribe<Address> {
	public AddressScribe() {
		super(Address.class, "ADR");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected void _prepareParameters(Address property, VCardParameters copy, VCardVersion version, VCard vcard) {
		handlePrefParam(property, copy, version, vcard);

		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			//remove the LABEL parameter
			//by the time this line of code is reached, VCardWriter will have created a LABEL property from this property's LABEL parameter
			copy.removeAll("LABEL");
		}
	}

	@Override
	protected String _writeText(Address property, VCardVersion version) {
		//@formatter:off
		return structured(
			property.getPoBoxes(),
			property.getExtendedAddresses(),
			property.getStreetAddresses(),
			property.getLocalities(),
			property.getRegions(),
			property.getPostalCodes(),
			property.getCountries()
		);
		//@formatter:on
	}

	@Override
	protected Address _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		StructuredIterator it = structured(value);
		return parseStructuredValue(it);
	}

	@Override
	protected void _writeXml(Address property, XCardElement parent) {
		parent.append("pobox", property.getPoBoxes()); //Note: The XML element must always be added, even if the value is null
		parent.append("ext", property.getExtendedAddresses());
		parent.append("street", property.getStreetAddresses());
		parent.append("locality", property.getLocalities());
		parent.append("region", property.getRegions());
		parent.append("code", property.getPostalCodes());
		parent.append("country", property.getCountries());
	}

	@Override
	protected Address _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		Address property = new Address();
		property.setPoBoxes(sanitizeXml(element, "pobox"));
		property.setExtendedAddresses(sanitizeXml(element, "ext"));
		property.setStreetAddresses(sanitizeXml(element, "street"));
		property.setLocalities(sanitizeXml(element, "locality"));
		property.setRegions(sanitizeXml(element, "region"));
		property.setPostalCodes(sanitizeXml(element, "code"));
		property.setCountries(sanitizeXml(element, "country"));
		return property;
	}

	private List<String> sanitizeXml(XCardElement element, String name) {
		return element.all(name);
	}

	@Override
	protected Address _parseHtml(HCardElement element, List<String> warnings) {
		Address property = new Address();
		property.setPoBoxes(element.allValues("post-office-box"));
		property.setExtendedAddresses(element.allValues("extended-address"));
		property.setStreetAddresses(element.allValues("street-address"));
		property.setLocalities(element.allValues("locality"));
		property.setRegions(element.allValues("region"));
		property.setPostalCodes(element.allValues("postal-code"));
		property.setCountries(element.allValues("country-name"));

		List<String> types = element.types();
		for (String type : types) {
			property.getParameters().addType(type);
		}

		return property;
	}

	@Override
	protected JCardValue _writeJson(Address property) {
		//@formatter:off
		return JCardValue.structured(
			property.getPoBoxes(),
			property.getExtendedAddresses(),
			property.getStreetAddresses(),
			property.getLocalities(),
			property.getRegions(),
			property.getPostalCodes(),
			property.getCountries()
		);
		//@formatter:on
	}

	@Override
	protected Address _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		StructuredIterator it = structured(value);
		return parseStructuredValue(it);
	}

	private Address parseStructuredValue(StructuredIterator it) {
		Address property = new Address();

		property.setPoBoxes(it.nextComponent());
		property.setExtendedAddresses(it.nextComponent());
		property.setStreetAddresses(it.nextComponent());
		property.setLocalities(it.nextComponent());
		property.setRegions(it.nextComponent());
		property.setPostalCodes(it.nextComponent());
		property.setCountries(it.nextComponent());

		return property;
	}
}
