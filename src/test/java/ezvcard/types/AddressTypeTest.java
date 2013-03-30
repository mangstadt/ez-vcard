package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.XCardElement;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class AddressTypeTest {
	@Test
	public void marshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		AddressType t;
		String expected, actual;

		//all fields present
		t = new AddressType();
		t.setPoBox("P.O. Box 1234;");
		t.setExtendedAddress("Apt, 11");
		t.setStreetAddress("123 Main St");
		t.setLocality("Austin");
		t.setRegion("TX");
		t.setPostalCode("12345");
		t.setCountry("USA");
		expected = "P.O. Box 1234\\;;Apt\\, 11;123 Main St;Austin;TX;12345;USA";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//some nulls
		t = new AddressType();
		t.setPoBox("P.O. Box 1234;");
		t.setExtendedAddress(null);
		t.setStreetAddress(null);
		t.setLocality("Austin");
		t.setRegion("TX");
		t.setPostalCode("12345");
		t.setCountry(null);
		expected = "P.O. Box 1234\\;;;;Austin;TX;12345;";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//all nulls
		t = new AddressType();
		expected = ";;;;;;";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		AddressType t;
		Document expected, actual;
		Element element;

		//all fields present
		t = new AddressType();
		t.setPoBox("P.O. Box 1234");
		t.setExtendedAddress("Apt 11");
		t.setStreetAddress("123 Main St");
		t.setLocality("Austin");
		t.setRegion("TX");
		t.setPostalCode("12345");
		t.setCountry("USA");

		XCardElement xe = new XCardElement("adr");
		xe.append("pobox", "P.O. Box 1234");
		xe.append("ext", "Apt 11");
		xe.append("street", "123 Main St");
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		xe.append("country", "USA");
		expected = xe.document();

		xe = new XCardElement("adr");
		actual = xe.document();
		element = xe.element();
		t.marshalXml(element, version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);

		//some nulls
		t = new AddressType();
		t.setPoBox("P.O. Box 1234");
		t.setExtendedAddress(null);
		t.setStreetAddress(null);
		t.setLocality("Austin");
		t.setRegion("TX");
		t.setPostalCode("12345");
		t.setCountry(null);

		xe = new XCardElement("adr");
		xe.append("pobox", "P.O. Box 1234");
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		expected = xe.document();

		xe = new XCardElement("adr");
		actual = xe.document();
		element = xe.element();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expected, actual);

		//all nulls
		t = new AddressType();

		xe = new XCardElement("adr");
		expected = xe.document();

		xe = new XCardElement("adr");
		actual = xe.document();
		element = xe.element();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalTz() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		AddressType t = new AddressType();
		t.setTimezone("America/New_York");
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals("tz:America/New_York", subTypes.first("TZ"));
	}

	@Test
	public void unmarshalTz() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.put("TZ", "tz:America/New_York");

		AddressType t = new AddressType();
		t.unmarshalText(subTypes, ";;;", version, warnings, compatibilityMode);
		assertEquals("America/New_York", t.getTimezone());
	}

	/**
	 * If a type contains a "TYPE=pref" parameter and it's being marshalled to
	 * 4.0, it should replace "TYPE=pref" with "PREF=1". <br>
	 * <br>
	 * Conversely, if types contain "PREF" parameters and they're being
	 * marshalled to 2.1/3.0, then it should find the type with the lowest PREF
	 * value and add "TYPE=pref" to it.
	 */
	@Test
	public void marshalPref() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		//ADR has "TYPE=pref"==========
		AddressType t = new AddressType();
		t.addType(AddressTypeParameter.PREF);

		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(AddressTypeParameter.PREF.getValue()));

		version = VCardVersion.V4_0;
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(1), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(AddressTypeParameter.PREF.getValue()));

		//ADR has PREF parameter=======
		VCard vcard = new VCard();
		AddressType t1 = new AddressType();
		t1.setPref(1);
		vcard.addAddress(t1);
		AddressType t2 = new AddressType();
		t2.setPref(2);
		vcard.addAddress(t2);

		version = VCardVersion.V2_1;
		subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(AddressTypeParameter.PREF.getValue()));
		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(AddressTypeParameter.PREF.getValue()));

		version = VCardVersion.V4_0;
		subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(1), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(AddressTypeParameter.PREF.getValue()));
		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(2), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(AddressTypeParameter.PREF.getValue()));
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		AddressType t;

		//all fields present
		t = new AddressType();
		t.unmarshalText(subTypes, "P.O. Box 1234\\;;Apt 11;123 Main St;Austin;TX;12345;USA", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some empty fields
		t = new AddressType();
		t.unmarshalText(subTypes, "P.O. Box 1234\\;;;;Austin;TX;12345;USA", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals(null, t.getExtendedAddress());
		assertEquals(null, t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some fields missing at the end
		t = new AddressType();
		t.unmarshalText(subTypes, "P.O. Box 1234\\;56;Apt 11;123 Main St;Austin;TX", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;56", t.getPoBox());
		assertEquals("Apt 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals(null, t.getPostalCode());
		assertEquals(null, t.getCountry());
	}

	@Test
	public void unmarshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		AddressType t;
		Element element;

		//all fields present
		XCardElement xe = new XCardElement("adr");
		xe.append("pobox", "P.O. Box 1234");
		xe.append("ext", "Apt 11");
		xe.append("street", "123 Main St");
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		xe.append("country", "USA");
		element = xe.element();
		t = new AddressType();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234", t.getPoBox());
		assertEquals("Apt 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some missing fields
		xe = new XCardElement("adr");
		xe.append("pobox", "P.O. Box 1234");
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		xe.append("country", "USA");
		element = xe.element();
		t = new AddressType();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234", t.getPoBox());
		assertEquals(null, t.getExtendedAddress());
		assertEquals(null, t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());
	}

	@Test
	public void unmarshalJson_all_fields() {
		JCardValue value = JCardValue.text();
		value.addValues("P.O. Box 1234", "Apt 11", "123 Main St", "Austin", "TX", "12345", "USA");

		AddressType adr = new AddressType();
		adr.unmarshalJson(new VCardSubTypes(), value, VCardVersion.V4_0, new ArrayList<String>());

		assertEquals("P.O. Box 1234", adr.getPoBox());
		assertEquals("Apt 11", adr.getExtendedAddress());
		assertEquals("123 Main St", adr.getStreetAddress());
		assertEquals("Austin", adr.getLocality());
		assertEquals("TX", adr.getRegion());
		assertEquals("12345", adr.getPostalCode());
		assertEquals("USA", adr.getCountry());
	}

	@Test
	public void unmarshalJson_missing_fields() {
		JCardValue value = JCardValue.text();
		value.addValues("P.O. Box 1234", "", "123 Main St", "", "TX", "12345", "USA");

		AddressType adr = new AddressType();
		adr.unmarshalJson(new VCardSubTypes(), value, VCardVersion.V4_0, new ArrayList<String>());

		assertEquals("P.O. Box 1234", adr.getPoBox());
		assertEquals(null, adr.getExtendedAddress());
		assertEquals("123 Main St", adr.getStreetAddress());
		assertEquals(null, adr.getLocality());
		assertEquals("TX", adr.getRegion());
		assertEquals("12345", adr.getPostalCode());
		assertEquals("USA", adr.getCountry());
	}

	@Test
	public void unmarshalJson_missing_fields_at_the_end() {
		JCardValue value = JCardValue.text();
		value.addValues("P.O. Box 1234", "Apt 11", "123 Main St", "Austin", "TX");

		AddressType adr = new AddressType();
		adr.unmarshalJson(new VCardSubTypes(), value, VCardVersion.V4_0, new ArrayList<String>());

		assertEquals("P.O. Box 1234", adr.getPoBox());
		assertEquals("Apt 11", adr.getExtendedAddress());
		assertEquals("123 Main St", adr.getStreetAddress());
		assertEquals("Austin", adr.getLocality());
		assertEquals("TX", adr.getRegion());
		assertEquals(null, adr.getPostalCode());
		assertEquals(null, adr.getCountry());
	}

	@Test
	public void marshalJson_all_fields() {
		//all fields present
		AddressType adr = new AddressType();
		adr.setPoBox("P.O. Box 1234");
		adr.setExtendedAddress("Apt 11");
		adr.setStreetAddress("123 Main St");
		adr.setLocality("Austin");
		adr.setRegion("TX");
		adr.setPostalCode("12345");
		adr.setCountry("USA");
		JCardValue value = adr.marshalJson(VCardVersion.V4_0, new ArrayList<String>());
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertTrue(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "P.O. Box 1234" }),
			Arrays.asList(new Object[]{ "Apt 11" }),
			Arrays.asList(new Object[]{ "123 Main St" }),
			Arrays.asList(new Object[]{ "Austin" }),
			Arrays.asList(new Object[]{ "TX" }),
			Arrays.asList(new Object[]{ "12345" }),
			Arrays.asList(new Object[]{ "USA" })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void marshalJson_missing_fields() {
		AddressType adr = new AddressType();
		adr.setPoBox("P.O. Box 1234");
		adr.setStreetAddress("123 Main St");
		adr.setRegion("TX");
		adr.setPostalCode("12345");
		JCardValue value = adr.marshalJson(VCardVersion.V4_0, new ArrayList<String>());
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertTrue(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "P.O. Box 1234" }),
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ "123 Main St" }),
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ "TX" }),
			Arrays.asList(new Object[]{ "12345" }),
			Arrays.asList(new Object[]{ null })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void marshalJson_all_missing() {
		AddressType adr = new AddressType();
		JCardValue value = adr.marshalJson(VCardVersion.V4_0, new ArrayList<String>());
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertTrue(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ null }),
			Arrays.asList(new Object[]{ null })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}
}
