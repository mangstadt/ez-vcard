package ezvcard.types;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.JsonValue;
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
	final VCardVersion version = VCardVersion.V2_1;
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();

	final AddressType allFields = new AddressType();
	{
		allFields.setPoBox("P.O. Box 1234;");
		allFields.setExtendedAddress("Apt, 11");
		allFields.setStreetAddress("123 Main St");
		allFields.setLocality("Austin");
		allFields.setRegion("TX");
		allFields.setPostalCode("12345");
		allFields.setCountry("USA");
	}
	final AddressType someFields = new AddressType();
	{
		someFields.setPoBox("P.O. Box 1234;");
		someFields.setExtendedAddress(null);
		someFields.setStreetAddress(null);
		someFields.setLocality("Austin");
		someFields.setRegion("TX");
		someFields.setPostalCode("12345");
		someFields.setCountry(null);
	}
	final AddressType noFields = new AddressType();
	AddressType t;

	@Before
	public void before() {
		t = new AddressType();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void marshalSubTypes_label_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		t.setLabel("label");
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertNull(subTypes.first("LABEL"));
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_label_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		t.setLabel("label");
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertNull(subTypes.first("LABEL"));
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_label_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		t.setLabel("label");
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertEquals("label", subTypes.first("LABEL"));
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		t.addType(AddressTypeParameter.PREF);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertSetEquals(subTypes.getTypes(), AddressTypeParameter.PREF.getValue());
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		t.addType(AddressTypeParameter.PREF);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertSetEquals(subTypes.getTypes(), AddressTypeParameter.PREF.getValue());
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		t.addType(AddressTypeParameter.PREF);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());

		assertEquals(1, subTypes.size());
		assertIntEquals(1, subTypes.getPref());
		assertSetEquals(subTypes.getTypes());
		assertWarnings(0, warnings);
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void marshalSubTypes_pref_parameter_2_1() {
		VCardVersion version = VCardVersion.V2_1;

		VCard vcard = new VCard();
		AddressType t1 = new AddressType();
		t1.setPref(1);
		vcard.addAddress(t1);
		AddressType t2 = new AddressType();
		t2.setPref(2);
		vcard.addAddress(t2);

		VCardSubTypes subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertSetEquals(subTypes.getTypes(), AddressTypeParameter.PREF.getValue());

		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertNull(subTypes.getPref());
		assertSetEquals(subTypes.getTypes());
		assertWarnings(0, warnings);
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void marshalSubTypes_pref_parameter_3_0() {
		VCardVersion version = VCardVersion.V3_0;

		VCard vcard = new VCard();
		AddressType t1 = new AddressType();
		t1.setPref(1);
		vcard.addAddress(t1);
		AddressType t2 = new AddressType();
		t2.setPref(2);
		vcard.addAddress(t2);

		VCardSubTypes subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertSetEquals(subTypes.getTypes(), AddressTypeParameter.PREF.getValue());
		assertWarnings(0, warnings);

		warnings.clear();

		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertNull(subTypes.getPref());
		assertSetEquals(subTypes.getTypes());
		assertWarnings(0, warnings);
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void marshalSubTypes_pref_parameter_4_0() {
		VCardVersion version = VCardVersion.V4_0;

		VCard vcard = new VCard();
		AddressType t1 = new AddressType();
		t1.setPref(1);
		vcard.addAddress(t1);
		AddressType t2 = new AddressType();
		t2.setPref(2);
		vcard.addAddress(t2);

		version = VCardVersion.V4_0;
		VCardSubTypes subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertIntEquals(1, subTypes.getPref());
		assertSetEquals(subTypes.getTypes());
		assertWarnings(0, warnings);

		warnings.clear();

		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertIntEquals(2, subTypes.getPref());
		assertSetEquals(subTypes.getTypes());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_all_fields() {
		String expected = "P.O. Box 1234\\;;Apt\\, 11;123 Main St;Austin;TX;12345;USA";
		String actual = allFields.marshalText(version, warnings, compatibilityMode);

		assertEquals(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_some_fields() {
		String expected = "P.O. Box 1234\\;;;;Austin;TX;12345;";
		String actual = someFields.marshalText(version, warnings, compatibilityMode);

		assertEquals(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_no_fields() {
		String expected = ";;;;;;";
		String actual = noFields.marshalText(version, warnings, compatibilityMode);

		assertEquals(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_all_fields() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(AddressType.NAME.toLowerCase());
		xe.append("pobox", "P.O. Box 1234;");
		xe.append("ext", "Apt, 11");
		xe.append("street", "123 Main St");
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		xe.append("country", "USA");
		Document expected = xe.document();

		xe = new XCardElement(AddressType.NAME.toLowerCase());
		Document actual = xe.document();
		allFields.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_some_fields() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(AddressType.NAME.toLowerCase());
		xe.append("pobox", "P.O. Box 1234;");
		xe.append("ext", (String) null);
		xe.append("street", (String) null);
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		xe.append("country", (String) null);
		Document expected = xe.document();

		xe = new XCardElement(AddressType.NAME.toLowerCase());
		Document actual = xe.document();
		someFields.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_no_fields() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(AddressType.NAME.toLowerCase());
		xe.append("pobox", (String) null);
		xe.append("ext", (String) null);
		xe.append("street", (String) null);
		xe.append("locality", (String) null);
		xe.append("region", (String) null);
		xe.append("code", (String) null);
		xe.append("country", (String) null);
		Document expected = xe.document();

		xe = new XCardElement(AddressType.NAME.toLowerCase());
		Document actual = xe.document();
		noFields.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_all_fields() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = allFields.marshalJson(version, warnings);
		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue("P.O. Box 1234;"),
				new JsonValue("Apt, 11"),
				new JsonValue("123 Main St"),
				new JsonValue("Austin"),
				new JsonValue("TX"),
				new JsonValue("12345"),
				new JsonValue("USA")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_some_fields() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = someFields.marshalJson(version, warnings);
		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue("P.O. Box 1234;"),
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue("Austin"),
				new JsonValue("TX"),
				new JsonValue("12345"),
				new JsonValue("")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_no_fields() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = noFields.marshalJson(version, warnings);
		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue("")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_all_fields() {
		t.unmarshalText(subTypes, "P.O. Box 1234\\;;Apt\\, 11;123 Main St;Austin;TX;12345;USA", version, warnings, compatibilityMode);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt, 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_some_fields() {
		t.unmarshalText(subTypes, "P.O. Box 1234\\;;;;Austin;TX;12345;", version, warnings, compatibilityMode);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_missing_components() {
		t.unmarshalText(subTypes, "P.O. Box 1234\\;;Apt\\, 11;123 Main St;Austin;TX", version, warnings, compatibilityMode);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt, 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_no_fields() {
		t.unmarshalText(subTypes, ";;;;;;", version, warnings, compatibilityMode);

		assertNull(t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertNull(t.getLocality());
		assertNull(t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_empty_string() {
		t.unmarshalText(subTypes, "", version, warnings, compatibilityMode);

		assertNull(t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertNull(t.getLocality());
		assertNull(t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_all_fields() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(AddressType.NAME.toLowerCase());
		xe.append("pobox", "P.O. Box 1234;");
		xe.append("ext", "Apt, 11");
		xe.append("street", "123 Main St");
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		xe.append("country", "USA");
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt, 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_some_fields() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(AddressType.NAME.toLowerCase());
		xe.append("pobox", "P.O. Box 1234;");
		xe.append("locality", "Austin");
		xe.append("region", "TX");
		xe.append("code", "12345");
		xe.append("country", ""); //should convert empty strings to null
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_no_fields() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(AddressType.NAME.toLowerCase());
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertNull(t.getLocality());
		assertNull(t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_all_fields() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"post-office-box\">P.O. Box 1234;</span>" +
			"<span class=\"extended-address\">Apt, 11</span>" +
			"<span class=\"street-address\">123 Main St</span>" +
			"<span class=\"locality\">Austin</span>" +
			"<span class=\"region\">TX</span>" +
			"<span class=\"postal-code\">12345</span>" +
			"<span class=\"country-name\">USA</span>" +
		"</div>");
		//@formatter:on

		t.unmarshalHtml(element, warnings);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt, 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());
		assertSetEquals(t.getTypes());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_some_fields() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"post-office-box\">P.O. Box 1234;</span>" +
			"<span class=\"locality\">Austin</span>" +
			"<span class=\"region\">TX</span>" +
			"<span class=\"postal-code\">12345</span>" +
		"</div>");
		//@formatter:on

		t.unmarshalHtml(element, warnings);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertNull(t.getCountry());
		assertSetEquals(t.getTypes());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_no_fields() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
		"</div>");
		//@formatter:on

		t.unmarshalHtml(element, warnings);

		assertNull(t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertNull(t.getLocality());
		assertNull(t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertSetEquals(t.getTypes());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_with_types() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"type\">home</span>" +
			"<span class=\"type\">postal</span>" +
			"<span class=\"type\">other</span>" +
			"<span class=\"post-office-box\">P.O. Box 1234;</span>" +
			"<span class=\"extended-address\">Apt, 11</span>" +
			"<span class=\"street-address\">123 Main St</span>" +
			"<span class=\"locality\">Austin</span>" +
			"<span class=\"region\">TX</span>" +
			"<span class=\"postal-code\">12345</span>" +
			"<span class=\"country-name\">USA</span>" +
		"</div>");
		//@formatter:on

		t.unmarshalHtml(element, warnings);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt, 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());
		assertSetEquals(t.getTypes(), AddressTypeParameter.HOME, AddressTypeParameter.POSTAL, AddressTypeParameter.get("other"));
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_all_fields() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(JCardDataType.TEXT, "P.O. Box 1234;", "Apt, 11", "123 Main St", "Austin", "TX", "12345", "USA");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt, 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_some_fields() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(JCardDataType.TEXT, "P.O. Box 1234;", "", "", "Austin", "TX", "12345", "");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_missing_components() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(JCardDataType.TEXT, "P.O. Box 1234;", "Apt, 11", "123 Main St", "Austin", "TX");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt, 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_no_components() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(JCardDataType.TEXT);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertNull(t.getLocality());
		assertNull(t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_no_fields() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(JCardDataType.TEXT, "", null, "", "", "", "", "");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getPoBox());
		assertNull(t.getExtendedAddress());
		assertNull(t.getStreetAddress());
		assertNull(t.getLocality());
		assertNull(t.getRegion());
		assertNull(t.getPostalCode());
		assertNull(t.getCountry());
		assertWarnings(0, warnings);
	}
}
