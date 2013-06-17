package ezvcard.types;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.TelUri;
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
public class TelephoneTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();
	final String number = "+1-555-555-1234";
	final String numberWithExt = "+1-555-555-1234 x101";
	final String uri = "tel:" + number;
	final String uriWithExt = uri + ";ext=101";
	final TelephoneType marshalObjText = new TelephoneType(number);
	final TelephoneType marshalObjUri = new TelephoneType(TelUri.global(number));
	{
		marshalObjUri.getUri().setExtension("101");
	}
	TelephoneType unmarshalObj;

	@Before
	public void before() {
		warnings.clear();
		unmarshalObj = new TelephoneType();
	}

	@Test
	public void marshalSubTypes_2_1_text() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = marshalObjText.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_2_1_uri() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = marshalObjUri.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_3_0_text() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = marshalObjText.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_3_0_uri() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = marshalObjUri.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_4_0_text() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = marshalObjText.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_4_0_uri() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = marshalObjUri.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		TelephoneType tel = new TelephoneType();
		tel.addType(TelephoneTypeParameter.PREF);
		VCardSubTypes subTypes = tel.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		TelephoneType tel = new TelephoneType();
		tel.addType(TelephoneTypeParameter.PREF);
		VCardSubTypes subTypes = tel.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		TelephoneType tel = new TelephoneType();
		tel.addType(TelephoneTypeParameter.PREF);
		VCardSubTypes subTypes = tel.marshalSubTypes(version, warnings, compatibilityMode, new VCard());

		assertEquals(1, subTypes.size());
		assertIntEquals(1, subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
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
		TelephoneType tel1 = new TelephoneType();
		tel1.setPref(1);
		vcard.addTelephoneNumber(tel1);
		TelephoneType tel2 = new TelephoneType();
		tel2.setPref(2);
		vcard.addTelephoneNumber(tel2);

		VCardSubTypes subTypes = tel1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));

		subTypes = tel2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertNull(subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
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
		TelephoneType tel1 = new TelephoneType();
		tel1.setPref(1);
		vcard.addTelephoneNumber(tel1);
		TelephoneType tel2 = new TelephoneType();
		tel2.setPref(2);
		vcard.addTelephoneNumber(tel2);

		VCardSubTypes subTypes = tel1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);

		warnings.clear();

		subTypes = tel2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertNull(subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
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
		TelephoneType tel1 = new TelephoneType();
		tel1.setPref(1);
		vcard.addTelephoneNumber(tel1);
		TelephoneType tel2 = new TelephoneType();
		tel2.setPref(2);
		vcard.addTelephoneNumber(tel2);

		version = VCardVersion.V4_0;
		VCardSubTypes subTypes = tel1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertIntEquals(1, subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);

		warnings.clear();

		subTypes = tel2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertIntEquals(2, subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_2_1_text() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = marshalObjText.marshalText(version, warnings, compatibilityMode);

		assertEquals(number, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_2_1_uri() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = marshalObjUri.marshalText(version, warnings, compatibilityMode);

		assertEquals(numberWithExt, actual);
		assertWarnings(1, warnings);
	}

	@Test
	public void marshalText_3_0_text() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = marshalObjText.marshalText(version, warnings, compatibilityMode);

		assertEquals(number, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_3_0_uri() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = marshalObjUri.marshalText(version, warnings, compatibilityMode);

		assertEquals(numberWithExt, actual);
		assertWarnings(1, warnings);
	}

	@Test
	public void marshalText_4_0_text() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = marshalObjText.marshalText(version, warnings, compatibilityMode);

		assertEquals(number, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_4_0_uri() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = marshalObjUri.marshalText(version, warnings, compatibilityMode);

		assertEquals(uriWithExt, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		xe.text(number);
		Document expectedDoc = xe.document();
		xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		Document actualDoc = xe.document();
		marshalObjText.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expectedDoc, actualDoc);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		xe.uri(uriWithExt);
		Document expectedDoc = xe.document();
		xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		Document actualDoc = xe.document();
		marshalObjUri.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expectedDoc, actualDoc);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = marshalObjText.marshalJson(version, warnings);
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ number })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_uri() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = marshalObjUri.marshalJson(version, warnings);
		assertEquals(JCardDataType.URI, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ uriWithExt })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_2_1_text() {
		VCardVersion version = VCardVersion.V2_1;
		unmarshalObj.unmarshalText(subTypes, number, version, warnings, compatibilityMode);

		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_2_1_uri() {
		VCardVersion version = VCardVersion.V2_1;
		unmarshalObj.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);

		assertEquals(uri, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_3_0_text() {
		VCardVersion version = VCardVersion.V3_0;
		unmarshalObj.unmarshalText(subTypes, number, version, warnings, compatibilityMode);

		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_3_0_uri() {
		VCardVersion version = VCardVersion.V3_0;
		unmarshalObj.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);

		assertEquals(uri, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_text() {
		VCardVersion version = VCardVersion.V4_0;
		unmarshalObj.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);

		assertEquals(uri, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_uri() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(ValueParameter.URI);
		unmarshalObj.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);

		assertNull(unmarshalObj.getText());
		assertEquals(number, unmarshalObj.getUri().getNumber());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_uri_invalid() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(ValueParameter.URI);
		unmarshalObj.unmarshalText(subTypes, number, version, warnings, compatibilityMode);

		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(1, warnings);
	}

	@Test
	public void unmarshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		xe.text(number);
		unmarshalObj.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		xe.uri(uri);
		unmarshalObj.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(unmarshalObj.getText());
		assertEquals(number, unmarshalObj.getUri().getNumber());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_uri_invalid() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		xe.uri(number);
		unmarshalObj.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(1, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TelephoneType.NAME.toLowerCase());
		unmarshalObj.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalHtml() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"type\">home</span>" +
			"<span class=\"type\">cell</span>" +
			"<span class=\"type\">foo</span>" +
			"<span class=\"value\">" + number + "</span>" +
		"</div>");
		//@formatter:on

		unmarshalObj.unmarshalHtml(element, warnings);

		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());

		assertEquals(3, unmarshalObj.getSubTypes().size());
		Set<TelephoneTypeParameter> types = unmarshalObj.getTypes();
		assertEquals(3, types.size());
		assertTrue(types.contains(TelephoneTypeParameter.HOME));
		assertTrue(types.contains(TelephoneTypeParameter.CELL));
		assertTrue(types.contains(new TelephoneTypeParameter("foo")));

		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_href() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<a href=\"" + uri + "\">Call me</a>");

		unmarshalObj.unmarshalHtml(element, warnings);

		assertEquals(0, unmarshalObj.getSubTypes().size());
		assertEquals(number, unmarshalObj.getUri().getNumber());
		assertNull(unmarshalObj.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_invalid_href_value() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<a href=\"foo\">" + number + "</a>");

		unmarshalObj.unmarshalHtml(element, warnings);

		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue();
		value.setDataType(JCardDataType.TEXT);
		value.addValues(number);

		unmarshalObj.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(0, unmarshalObj.getSubTypes().size());
		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_uri() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue();
		value.setDataType(JCardDataType.URI);
		value.addValues(uri);

		unmarshalObj.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(0, unmarshalObj.getSubTypes().size());
		assertNull(unmarshalObj.getText());
		assertEquals(number, unmarshalObj.getUri().getNumber());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_uri_invalid() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue();
		value.setDataType(JCardDataType.URI);
		value.addValues(number);

		unmarshalObj.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(0, unmarshalObj.getSubTypes().size());
		assertEquals(number, unmarshalObj.getText());
		assertNull(unmarshalObj.getUri());
		assertWarnings(1, warnings);
	}
}
