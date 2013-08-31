package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
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
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final VCard vcard = new VCard();

	private final String text = "Edna Smith";
	private final RelatedType textType = new RelatedType();
	{
		textType.setText(text);
	}

	private final String uri = "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af";
	private final RelatedType uriType = new RelatedType();
	{
		uriType.setUri(uri);
	}

	@After
	public void after() {
		warnings.clear();
	}

	@Test
	public void validate() {
		RelatedType empty = new RelatedType();
		assertWarnings(2, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(2, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		assertWarnings(1, textType.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, textType.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, textType.validate(VCardVersion.V4_0, vcard));

		assertWarnings(1, uriType.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, uriType.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, uriType.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void marshalSubTypes_text() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = textType.marshalSubTypes(version, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertEquals(VCardDataType.TEXT, subTypes.getValue());
	}

	@Test
	public void marshalSubTypes_uri() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = uriType.marshalSubTypes(version, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertEquals(VCardDataType.URI, subTypes.getValue());
	}

	@Test
	public void marshalSubTypes_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = t.marshalSubTypes(version, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
	}

	@Test
	public void marshalText_text() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = textType.marshalText(version, compatibilityMode);
		assertEquals(text, actual);
	}

	@Test
	public void marshalText_uri() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = uriType.marshalText(version, compatibilityMode);
		assertEquals(uri, actual);
	}

	@Test
	public void marshalText_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		String value = t.marshalText(version, compatibilityMode);

		assertEquals("", value);
	}

	@Test
	public void marshalXml_text() {
		assertMarshalXml(textType, "<text>" + text + "</text>");
	}

	@Test
	public void marshalXml_uri() {
		assertMarshalXml(uriType, "<uri>" + uri + "</uri>");
	}

	@Test
	public void marshalXml_no_value() {
		RelatedType t = new RelatedType();
		assertMarshalXml(t, "<uri/>");
	}

	@Test
	public void marshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = textType.marshalJson(version);

		assertJCardValue(VCardDataType.TEXT, text, value);
	}

	@Test
	public void marshalJson_uri() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = uriType.marshalJson(version);

		assertJCardValue(VCardDataType.URI, uri, value);
	}

	@Test
	public void marshalJson_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		JCardValue value = t.marshalJson(version);

		assertJCardValue(VCardDataType.URI, "", value);
	}

	@Test
	public void unmarshalText_text() {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setValue(VCardDataType.TEXT);
		t.unmarshalText(subTypes, text, version, warnings, compatibilityMode);
		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_uri() {
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setValue(VCardDataType.URI);
		t.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_no_value_parameter() {
		//treats it as a URI
		VCardVersion version = VCardVersion.V4_0;
		RelatedType t = new RelatedType();
		t.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;

		RelatedType t = new RelatedType();
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, text);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_uri() {
		VCardVersion version = VCardVersion.V4_0;

		RelatedType t = new RelatedType();
		XCardElement xe = new XCardElement(RelatedType.NAME.toLowerCase());
		xe.append(VCardDataType.URI, uri);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_unknown_tag() {
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

		JCardValue value = JCardValue.single(VCardDataType.TEXT, text);

		RelatedType t = new RelatedType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_uri() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.URI, uri);

		RelatedType t = new RelatedType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_unknown_datatype() {
		//treats it as a URI
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.LANGUAGE_TAG, uri);

		RelatedType t = new RelatedType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
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
