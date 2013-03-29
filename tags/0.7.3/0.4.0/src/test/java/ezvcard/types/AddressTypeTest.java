package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.util.XCardUtils;
/*
 Copyright (c) 2012, Michael Angstadt
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
		actual = t.marshalValue(version, warnings, compatibilityMode);
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
		actual = t.marshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//all nulls
		t = new AddressType();
		expected = ";;;;;;";
		actual = t.marshalValue(version, warnings, compatibilityMode);
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
		String expectedXml;

		//all fields present
		t = new AddressType();
		t.setPoBox("P.O. Box 1234");
		t.setExtendedAddress("Apt 11");
		t.setStreetAddress("123 Main St");
		t.setLocality("Austin");
		t.setRegion("TX");
		t.setPostalCode("12345");
		t.setCountry("USA");
		
		expectedXml = "<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		expectedXml += "<pobox>P.O. Box 1234</pobox>";
		expectedXml += "<ext>Apt 11</ext>";
		expectedXml += "<street>123 Main St</street>";
		expectedXml += "<locality>Austin</locality>";
		expectedXml += "<region>TX</region>";
		expectedXml += "<code>12345</code>";
		expectedXml += "<country>USA</country>";
		expectedXml += "</adr>";
		expected = XCardUtils.toDocument(expectedXml);
		
		actual = XCardUtils.toDocument("<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\" />");
		element = XCardUtils.getFirstElement(actual.getChildNodes());
		t.marshalValue(element, version, warnings, compatibilityMode);
		
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
		
		expectedXml = "<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		expectedXml += "<pobox>P.O. Box 1234</pobox>";
		expectedXml += "<locality>Austin</locality>";
		expectedXml += "<region>TX</region>";
		expectedXml += "<code>12345</code>";
		expectedXml += "</adr>";
		expected = XCardUtils.toDocument(expectedXml);
		
		actual = XCardUtils.toDocument("<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\" />");
		element = XCardUtils.getFirstElement(actual.getChildNodes());
		t.marshalValue(element, version, warnings, compatibilityMode);
		assertXMLEqual(expected, actual);

		//all nulls
		t = new AddressType();
		
		expectedXml = "<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		expectedXml += "</adr>";
		expected = XCardUtils.toDocument(expectedXml);
		
		actual = XCardUtils.toDocument("<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\" />");
		element = XCardUtils.getFirstElement(actual.getChildNodes());
		t.marshalValue(element, version, warnings, compatibilityMode);
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
		assertEquals("tz:America/New_York", subTypes.getFirst("TZ"));
	}

	@Test
	public void unmarshalTz() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.put("TZ", "tz:America/New_York");

		AddressType t = new AddressType();
		t.unmarshalValue(subTypes, ";;;", version, warnings, compatibilityMode);
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
		t.unmarshalValue(subTypes, "P.O. Box 1234\\;;Apt 11;123 Main St;Austin;TX;12345;USA", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some empty fields
		t = new AddressType();
		t.unmarshalValue(subTypes, "P.O. Box 1234\\;;;;Austin;TX;12345;USA", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals(null, t.getExtendedAddress());
		assertEquals(null, t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some fields missing at the end
		t = new AddressType();
		t.unmarshalValue(subTypes, "P.O. Box 1234\\;56;Apt 11;123 Main St;Austin;TX", version, warnings, compatibilityMode);
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
		String xml;
		Element element;

		//all fields present
		xml = "<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		xml += "<pobox>P.O. Box 1234</pobox>";
		xml += "<ext>Apt 11</ext>";
		xml += "<street>123 Main St</street>";
		xml += "<locality>Austin</locality>";
		xml += "<region>TX</region>";
		xml += "<code>12345</code>";
		xml += "<country>USA</country>";
		xml += "</adr>";
		element = XCardUtils.getFirstElement(XCardUtils.toDocument(xml).getChildNodes());
		t = new AddressType();
		t.unmarshalValue(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234", t.getPoBox());
		assertEquals("Apt 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some missing fields
		xml = "<adr xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		xml += "<pobox>P.O. Box 1234</pobox>";
		xml += "<locality>Austin</locality>";
		xml += "<region>TX</region>";
		xml += "<code>12345</code>";
		xml += "<country>USA</country>";
		xml += "</adr>";
		element = XCardUtils.getFirstElement(XCardUtils.toDocument(xml).getChildNodes());
		t = new AddressType();
		t.unmarshalValue(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234", t.getPoBox());
		assertEquals(null, t.getExtendedAddress());
		assertEquals(null, t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());
	}
}
