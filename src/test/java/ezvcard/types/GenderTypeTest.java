package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
public class GenderTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();
	final String gender = "M";
	final String text = "te;xt";
	final String escapedText = "te\\;xt";
	final GenderType genderType = GenderType.male();
	final GenderType genderTypeWithText = GenderType.male();
	{
		genderTypeWithText.setText(text);
	}
	GenderType t;

	@Before
	public void before() {
		t = new GenderType();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void isMale() {
		GenderType gender = new GenderType("M");
		assertTrue(gender.isMale());
		assertFalse(gender.isFemale());
		assertFalse(gender.isOther());
		assertFalse(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isFemale() {
		GenderType gender = new GenderType("F");
		assertFalse(gender.isMale());
		assertTrue(gender.isFemale());
		assertFalse(gender.isOther());
		assertFalse(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isOther() {
		GenderType gender = new GenderType("O");
		assertFalse(gender.isMale());
		assertFalse(gender.isFemale());
		assertTrue(gender.isOther());
		assertFalse(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isNone() {
		GenderType gender = new GenderType("N");
		assertFalse(gender.isMale());
		assertFalse(gender.isFemale());
		assertFalse(gender.isOther());
		assertTrue(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isUnknown() {
		GenderType gender = new GenderType("U");
		assertFalse(gender.isMale());
		assertFalse(gender.isFemale());
		assertFalse(gender.isOther());
		assertFalse(gender.isNone());
		assertTrue(gender.isUnknown());
	}

	@Test
	public void male() {
		GenderType gender = GenderType.male();
		assertEquals("M", gender.getGender());
	}

	@Test
	public void female() {
		GenderType gender = GenderType.female();
		assertEquals("F", gender.getGender());
	}

	@Test
	public void other() {
		GenderType gender = GenderType.other();
		assertEquals("O", gender.getGender());
	}

	@Test
	public void none() {
		GenderType gender = GenderType.none();
		assertEquals("N", gender.getGender());
	}

	@Test
	public void unknown() {
		GenderType gender = GenderType.unknown();
		assertEquals("U", gender.getGender());
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = genderType.marshalText(version, warnings, compatibilityMode);

		assertEquals(gender, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_with_text() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = genderTypeWithText.marshalText(version, warnings, compatibilityMode);

		assertEquals(gender + ";" + escapedText, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(BirthplaceType.NAME.toLowerCase());
		xe.append("sex", gender);
		Document expectedDoc = xe.document();
		xe = new XCardElement(BirthplaceType.NAME.toLowerCase());
		Document actualDoc = xe.document();
		genderType.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expectedDoc, actualDoc);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_with_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(BirthplaceType.NAME.toLowerCase());
		xe.append("sex", gender);
		xe.append("identity", text);
		Document expectedDoc = xe.document();
		xe = new XCardElement(BirthplaceType.NAME.toLowerCase());
		Document actualDoc = xe.document();
		genderTypeWithText.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expectedDoc, actualDoc);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = genderType.marshalJson(version, warnings);

		assertJCardValue(VCardDataType.TEXT, gender, value);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_with_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = genderTypeWithText.marshalJson(version, warnings);

		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue(gender),
				new JsonValue(text)
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText() {
		VCardVersion version = VCardVersion.V4_0;
		t.unmarshalText(subTypes, gender, version, warnings, compatibilityMode);

		assertEquals(gender, t.getGender());
		assertTrue(t.isMale());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_with_text() {
		VCardVersion version = VCardVersion.V4_0;
		t.unmarshalText(subTypes, gender + ";" + escapedText, version, warnings, compatibilityMode);

		assertEquals(gender, t.getGender());
		assertTrue(t.isMale());
		assertEquals(text, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(GenderType.NAME.toLowerCase());
		xe.append("sex", gender);

		Element element = xe.element();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertEquals(gender, t.getGender());
		assertTrue(t.isMale());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_with_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(GenderType.NAME.toLowerCase());
		xe.append("sex", gender);
		xe.append("identity", text);

		Element element = xe.element();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertEquals(gender, t.getGender());
		assertTrue(t.isMale());
		assertEquals(text, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.TEXT, gender);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(gender, t.getGender());
		assertTrue(t.isMale());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_gender_in_array() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(VCardDataType.TEXT, gender);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(gender, t.getGender());
		assertTrue(t.isMale());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_with_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(VCardDataType.TEXT, gender, text);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(gender, t.getGender());
		assertTrue(t.isMale());
		assertEquals(text, t.getText());
		assertWarnings(0, warnings);
	}
}
