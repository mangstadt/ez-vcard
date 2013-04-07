package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
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
public class RelatedTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();

	final String text = "Edna Smith";
	final RelatedType textType = new RelatedType();
	{
		textType.setText(text);
	}

	final String uri = "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af";
	final RelatedType uriType = new RelatedType();
	{
		uriType.setUri(uri);
	}

	@After
	public void after() {
		warnings.clear();
	}

	@Test
	public void marshalSubTypes_text() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = textType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_uri() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = uriType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_no_value() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_text() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		String actual = textType.marshalText(version, warnings, compatibilityMode);
		assertEquals(text, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_uri() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		String actual = uriType.marshalText(version, warnings, compatibilityMode);
		assertEquals(uri, actual);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_no_value() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		t.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalXml_text() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.text(text);
		Document expectedDoc = xe.document();
		xe = new XCardElement(RelatedType.NAME.toLowerCase());
		Document actualDoc = xe.document();
		textType.marshalXml(xe.element(), version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalXml_uri() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.uri(uri);
		Document expectedDoc = xe.document();
		xe = new XCardElement(RelatedType.NAME.toLowerCase());
		Document actualDoc = xe.document();
		uriType.marshalXml(xe.element(), version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalXml_no_value() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.uri(uri);
		xe = new XCardElement(RelatedType.NAME.toLowerCase());
		RelatedType t = new RelatedType();
		t.marshalXml(xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void marshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = textType.marshalJson(version, new ArrayList<String>());
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ text })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalJson_uri() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = uriType.marshalJson(version, new ArrayList<String>());
		assertEquals(JCardDataType.URI, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ uri })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalJson_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		t.marshalJson(version, new ArrayList<String>());
	}

	@Test
	public void unmarshalText_text() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setValue(ValueParameter.TEXT);
		t.unmarshalText(subTypes, text, version, warnings, compatibilityMode);
		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_uri() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setValue(ValueParameter.URI);
		t.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_no_value_parameter() throws Exception {
		//treats it as a URI
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		t.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml_text() throws Exception {
		VCardVersion version = VCardVersion.V4_0;

		RelatedType t = new RelatedType();
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.text(text);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml_uri() throws Exception {
		VCardVersion version = VCardVersion.V4_0;

		RelatedType t = new RelatedType();
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.uri(uri);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_unknown_tag() throws Exception {
		//throws exception
		VCardVersion version = VCardVersion.V4_0;

		RelatedType t = new RelatedType();
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.append("foo", uri);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue();
		value.setDataType(JCardDataType.TEXT);
		value.addValues(text);

		RelatedType t = new RelatedType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalJson_uri() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue();
		value.setDataType(JCardDataType.URI);
		value.addValues(uri);

		RelatedType t = new RelatedType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalJson_unknown_datatype() {
		//treats it as a URI
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue();
		value.setDataType(JCardDataType.LANGUAGE_TAG);
		value.addValues(uri);

		RelatedType t = new RelatedType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertEquals(0, warnings.size());
	}

	@Test
	public void setUri() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUri(uri);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
	}

	@Test
	public void setUriEmail() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUriEmail("john.doe@example.com");

		assertNull(t.getText());
		assertEquals("mailto:john.doe@example.com", t.getUri());
	}

	@Test
	public void setUriIM() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUriIM("aim", "john.doe");

		assertNull(t.getText());
		assertEquals("aim:john.doe", t.getUri());
	}

	@Test
	public void setUriTelephone() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUriTelephone("555-555-5555");

		assertNull(t.getText());
		assertEquals("tel:555-555-5555", t.getUri());
	}

	@Test
	public void setText() {
		RelatedType t = new RelatedType();
		t.setUri(uri);
		t.setText(text);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
	}
}
