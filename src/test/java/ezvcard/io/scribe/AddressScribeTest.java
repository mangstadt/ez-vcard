package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.io.json.JCardValue;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.parameter.AddressType;
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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class AddressScribeTest {
	private final AddressScribe scribe = new AddressScribe();
	private final Sensei<Address> sensei = new Sensei<Address>(scribe);

	private final Address withAllFields = new Address();
	{
		withAllFields.setPoBox("P.O. Box 1234;");
		withAllFields.setExtendedAddress("Apt, 11");
		withAllFields.setStreetAddress("123 Main St");
		withAllFields.setLocality("Austin");
		withAllFields.setRegion("TX");
		withAllFields.setPostalCode("12345");
		withAllFields.setCountry("USA");
	}
	private final Address withMultipleValuesFields = new Address();
	{
		withMultipleValuesFields.setPoBox("P.O. Box 1234;");
		withMultipleValuesFields.getExtendedAddresses().addAll(Arrays.asList("Apt, 11", "P.O. Box 12"));
		withMultipleValuesFields.getStreetAddresses().addAll(Arrays.asList("123 Main St", "555 Main St"));
		withMultipleValuesFields.setLocality("Austin");
		withMultipleValuesFields.setRegion("TX");
		withMultipleValuesFields.setPostalCode("12345");
		withMultipleValuesFields.setCountry("USA");
	}
	private final Address withSomeFields = new Address();
	{
		withSomeFields.setPoBox("P.O. Box 1234;");
		withSomeFields.setExtendedAddress(null);
		withSomeFields.setStreetAddress(null);
		withSomeFields.setLocality("Austin");
		withSomeFields.setRegion("TX");
		withSomeFields.setPostalCode("12345");
		withSomeFields.setCountry(null);
	}
	private final Address empty = new Address();

	@Test
	public void getExtendedAddressFull() {
		String actual = withAllFields.getExtendedAddressFull();
		String expected = "Apt, 11";
		assertEquals(expected, actual);

		actual = withMultipleValuesFields.getExtendedAddressFull();
		expected = "Apt, 11,P.O. Box 12";
		assertEquals(expected, actual);

		assertNull(withSomeFields.getExtendedAddressFull());
	}

	@Test
	public void getStreetAddressFull() {
		String actual = withAllFields.getStreetAddressFull();
		String expected = "123 Main St";
		assertEquals(expected, actual);

		actual = withMultipleValuesFields.getStreetAddressFull();
		expected = "123 Main St,555 Main St";
		assertEquals(expected, actual);

		assertNull(withSomeFields.getStreetAddressFull());
	}

	@Test
	public void prepareParameters_label() {
		Address property = new Address();
		property.setLabel("label");

		//2.1 and 3.0 should remove it
		sensei.assertPrepareParams(property).versions(V2_1, V3_0).run();

		//4.0 should keep it
		sensei.assertPrepareParams(property).versions(V4_0).expected("LABEL", "label").run();
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void prepareParameters_type_pref() {
		Address property = new Address();
		property.getTypes().add(AddressType.PREF);

		//2.1 and 3.0 keep it
		sensei.assertPrepareParams(property).versions(V2_1, V3_0).expected("TYPE", "pref").run();

		//4.0 converts it to "PREF=1"
		sensei.assertPrepareParams(property).versions(V4_0).expected("PREF", "1").run();
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void prepareParameters_pref_parameter() {
		//TODO move this test to VCardPropertyScribeTest (except for the "label" part)
		VCard vcard = new VCard();

		Address adr2 = new Address();
		adr2.setPref(2);
		vcard.addAddress(adr2);

		Address adr1 = new Address();
		adr1.setPref(1);
		vcard.addAddress(adr1);

		Address adr3 = new Address();
		vcard.addAddress(adr3);

		//2.1 and 3.0 converts the lowest PREF parameter to "TYPE=pref", and removes all the rest
		sensei.assertPrepareParams(adr1).versions(V2_1, V3_0).vcard(vcard).expected("TYPE", "pref").run();
		sensei.assertPrepareParams(adr2).versions(V2_1, V3_0).vcard(vcard).run();
		sensei.assertPrepareParams(adr3).versions(V2_1, V3_0).vcard(vcard).run();

		//4.0 keeps it
		sensei.assertPrepareParams(adr1).versions(V4_0).vcard(vcard).expected("PREF", "1").run();
		sensei.assertPrepareParams(adr2).versions(V4_0).vcard(vcard).expected("PREF", "2").run();
		sensei.assertPrepareParams(adr3).versions(V4_0).vcard(vcard).run();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withAllFields).run("P.O. Box 1234\\;;Apt\\, 11;123 Main St;Austin;TX;12345;USA");
		sensei.assertWriteText(withMultipleValuesFields).run("P.O. Box 1234\\;;Apt\\, 11,P.O. Box 12;123 Main St,555 Main St;Austin;TX;12345;USA");
		sensei.assertWriteText(withSomeFields).run("P.O. Box 1234\\;;;;Austin;TX;12345");
		sensei.assertWriteText(withSomeFields).includeTrailingSemicolons(true).run("P.O. Box 1234\\;;;;Austin;TX;12345;");
		sensei.assertWriteText(empty).run("");
		sensei.assertWriteText(empty).includeTrailingSemicolons(true).run(";;;;;;");
	}

	@Test
	public void writeXml() {
		//@formatter:off
		sensei.assertWriteXml(withAllFields).run(
		"<pobox>P.O. Box 1234;</pobox>" +
		"<ext>Apt, 11</ext>" +
		"<street>123 Main St</street>" +
		"<locality>Austin</locality>"+
		"<region>TX</region>" +
		"<code>12345</code>" +
		"<country>USA</country>");
		
		sensei.assertWriteXml(withMultipleValuesFields).run(
		"<pobox>P.O. Box 1234;</pobox>" +
		"<ext>Apt, 11</ext>" +
		"<ext>P.O. Box 12</ext>" +
		"<street>123 Main St</street>" +
		"<street>555 Main St</street>" +
		"<locality>Austin</locality>"+
		"<region>TX</region>" +
		"<code>12345</code>" +
		"<country>USA</country>");

		sensei.assertWriteXml(withSomeFields).run(
		"<pobox>P.O. Box 1234;</pobox>" +
		"<ext/>" +
		"<street/>" +
		"<locality>Austin</locality>" +
		"<region>TX</region>" +
		"<code>12345</code>" +
		"<country/>");
		
		sensei.assertWriteXml(empty).run(
		"<pobox/>" +
		"<ext/>" +
		"<street/>" +
		"<locality/>" +
		"<region/>" +
		"<code/>" +
		"<country/>");
		//@formatter:on
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withAllFields).run(JCardValue.structured("P.O. Box 1234;", "Apt, 11", "123 Main St", "Austin", "TX", "12345", "USA"));
		sensei.assertWriteJson(withMultipleValuesFields).run(JCardValue.structured("P.O. Box 1234;", Arrays.asList("Apt, 11", "P.O. Box 12"), Arrays.asList("123 Main St", "555 Main St"), "Austin", "TX", "12345", "USA"));
		sensei.assertWriteJson(withSomeFields).run(JCardValue.structured("P.O. Box 1234;", "", "", "Austin", "TX", "12345", ""));
		sensei.assertWriteJson(empty).run(JCardValue.structured("", "", "", "", "", "", ""));
	}

	@Test
	public void parseText() {
		sensei.assertParseText("P.O. Box 1234\\;;Apt\\, 11;123 Main St;Austin;TX;12345;USA").run(withAllFields);
		sensei.assertParseText("P.O. Box 1234\\;;Apt\\, 11,P.O. Box 12;123 Main St,555 Main St;Austin;TX;12345;USA").run(withMultipleValuesFields);
		sensei.assertParseText("P.O. Box 1234\\;;;;Austin;TX;12345;").run(withSomeFields);
		sensei.assertParseText(";;;;;;").run(empty);
		sensei.assertParseText("").run(empty);
	}

	@Test
	public void parseXml() {
		//@formatter:off
		sensei.assertParseXml(
		"<pobox>P.O. Box 1234;</pobox>" +
		"<ext>Apt, 11</ext>" +
		"<street>123 Main St</street>" +
		"<locality>Austin</locality>" +
		"<region>TX</region>" +
		"<code>12345</code>" +
		"<country>USA</country>").run(withAllFields);
		
		sensei.assertParseXml(
		"<pobox>P.O. Box 1234;</pobox>" +
		"<ext>Apt, 11</ext>" +
		"<ext>P.O. Box 12</ext>" +
		"<street>123 Main St</street>" +
		"<street>555 Main St</street>" +
		"<locality>Austin</locality>" +
		"<region>TX</region>" +
		"<code>12345</code>" +
		"<country>USA</country>").run(withMultipleValuesFields);

		sensei.assertParseXml(
		"<pobox>P.O. Box 1234;</pobox>" +
		"<locality>Austin</locality>" +
		"<region>TX</region>" +
		"<code>12345</code>" +
		"<country></country>").run(withSomeFields);

		sensei.assertParseXml("").run(empty);
		//@formatter:on
	}

	@Test
	public void parseHtml() {
		//@formatter:off
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"post-office-box\">P.O. Box 1234;</span>" +
			"<span class=\"extended-address\">Apt, 11</span>" +
			"<span class=\"street-address\">123 Main St</span>" +
			"<span class=\"locality\">Austin</span>" +
			"<span class=\"region\">TX</span>" +
			"<span class=\"postal-code\">12345</span>" +
			"<span class=\"country-name\">USA</span>" +
		"</div>").run(withAllFields);
		
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"post-office-box\">P.O. Box 1234;</span>" +
			"<span class=\"extended-address\">Apt, 11</span>" +
			"<span class=\"extended-address\">P.O. Box 12</span>" +
			"<span class=\"street-address\">123 Main St</span>" +
			"<span class=\"street-address\">555 Main St</span>" +
			"<span class=\"locality\">Austin</span>" +
			"<span class=\"region\">TX</span>" +
			"<span class=\"postal-code\">12345</span>" +
			"<span class=\"country-name\">USA</span>" +
		"</div>").run(withMultipleValuesFields);

		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"post-office-box\">P.O. Box 1234;</span>" +
			"<span class=\"locality\">Austin</span>" +
			"<span class=\"region\">TX</span>" +
			"<span class=\"postal-code\">12345</span>" +
		"</div>").run(withSomeFields);

		sensei.assertParseHtml("<div></div>").run(empty);
		//@formatter:on
	}

	@Test
	public void parseHtml_type_parameters() {
		//@formatter:off
		sensei.assertParseHtml(
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
		"</div>").run(new Check<Address>(){
			public void check(Address property) {
				assertEquals("P.O. Box 1234;", property.getPoBox());
				assertEquals("Apt, 11", property.getExtendedAddress());
				assertEquals("123 Main St", property.getStreetAddress());
				assertEquals("Austin", property.getLocality());
				assertEquals("TX", property.getRegion());
				assertEquals("12345", property.getPostalCode());
				assertEquals("USA", property.getCountry());
				assertEquals(Arrays.asList(AddressType.HOME, AddressType.POSTAL, AddressType.get("other")), property.getTypes());
			}
		});
		//@formatter:on
	}

	@Test
	public void parseJson() {
		JCardValue value = JCardValue.structured("P.O. Box 1234;", "Apt, 11", "123 Main St", "Austin", "TX", "12345", "USA");
		sensei.assertParseJson(value).run(withAllFields);

		value = JCardValue.structured("P.O. Box 1234;", Arrays.asList("Apt, 11", "P.O. Box 12"), Arrays.asList("123 Main St", "555 Main St"), "Austin", "TX", "12345", "USA");
		sensei.assertParseJson(value).run(withMultipleValuesFields);

		value = JCardValue.structured("P.O. Box 1234;", "", "", "Austin", "TX", "12345", "");
		sensei.assertParseJson(value).run(withSomeFields);

		value = JCardValue.structured("", null, "", "", "", "", "");
		sensei.assertParseJson(value).run(empty);

		value = JCardValue.structured();
		sensei.assertParseJson(value).run(empty);

		value = JCardValue.structured("P.O. Box 1234;", "Apt, 11", "123 Main St", "Austin");
		sensei.assertParseJson(value).run(new Check<Address>() {
			public void check(Address property) {
				assertEquals("P.O. Box 1234;", property.getPoBox());
				assertEquals("Apt, 11", property.getExtendedAddress());
				assertEquals("123 Main St", property.getStreetAddress());
				assertEquals("Austin", property.getLocality());
				assertNull(property.getRegion());
				assertNull(property.getPostalCode());
				assertNull(property.getCountry());
			}
		});
	}
}
