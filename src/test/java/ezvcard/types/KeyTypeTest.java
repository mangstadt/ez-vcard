package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.KeyTypeParameter;
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
public class KeyTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCard vcard = new VCard();
	final VCardSubTypes subTypes = new VCardSubTypes();

	final String text = "abc123";
	final String url = "http://example.com";
	final KeyType withText = new KeyType();
	{
		withText.setText(text, KeyTypeParameter.PGP);
		withText.setType("work"); //4.0 TYPE parameter
	}
	final KeyType withUrl = new KeyType();
	{
		withUrl.setUrl(url, KeyTypeParameter.PGP);
	}
	KeyType key;

	@Before
	public void before() {
		warnings.clear();
		subTypes.clear();
		key = new KeyType();
	}

	@Test
	public void marshalSubTypes_text_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = withText.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(2, subTypes.size());
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals(KeyTypeParameter.PGP.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_text_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = withText.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(2, subTypes.size());
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals(KeyTypeParameter.PGP.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalSubTypes_text_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = withText.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(3, subTypes.size());
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals("work", subTypes.getType());
		assertEquals(KeyTypeParameter.PGP.getMediaType(), subTypes.getMediaType());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_text_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = withText.marshalText(version, warnings, compatibilityMode);

		assertEquals(text, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_text_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = withText.marshalText(version, warnings, compatibilityMode);

		assertEquals(text, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_text_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = withText.marshalText(version, warnings, compatibilityMode);

		assertEquals(text, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_url_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = withUrl.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertWarnings(1, warnings);
	}

	@Test
	public void marshalText_url_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = withUrl.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertWarnings(1, warnings);
	}

	@Test
	public void marshalText_url_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = withUrl.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(KeyType.NAME.toLowerCase());
		xe.text(text);
		Document expectedDoc = xe.document();
		xe = new XCardElement(KeyType.NAME.toLowerCase());
		Document actualDoc = xe.document();
		Element element = xe.element();
		withText.marshalXml(element, version, warnings, compatibilityMode);

		assertXMLEqual(expectedDoc, actualDoc);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = withText.marshalJson(version, warnings);

		assertJCardValue(JCardDataType.TEXT, text, value);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_text_without_value_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setType(KeyTypeParameter.PGP.getValue());
		key.unmarshalText(subTypes, text, version, warnings, compatibilityMode);

		assertEquals(text, key.getText());
		assertNull(key.getUrl());
		assertNull(key.getData());
		assertEquals(KeyTypeParameter.PGP, key.getContentType());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_text_without_value_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		subTypes.setType(KeyTypeParameter.PGP.getValue());
		key.unmarshalText(subTypes, text, version, warnings, compatibilityMode);

		assertEquals(text, key.getText());
		assertNull(key.getUrl());
		assertNull(key.getData());
		assertEquals(KeyTypeParameter.PGP, key.getContentType());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_text_without_value_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setMediaType(KeyTypeParameter.PGP.getMediaType());
		key.unmarshalText(subTypes, text, version, warnings, compatibilityMode);

		assertEquals(text, key.getText());
		assertNull(key.getUrl());
		assertNull(key.getData());
		assertEquals(KeyTypeParameter.PGP, key.getContentType());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(KeyType.NAME.toLowerCase());
		xe.text(text);
		subTypes.setMediaType(KeyTypeParameter.PGP.getMediaType());
		key.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(text, key.getText());
		assertNull(key.getUrl());
		assertNull(key.getData());
		assertEquals(KeyTypeParameter.PGP, key.getContentType());
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(KeyType.NAME.toLowerCase());
		key.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(JCardDataType.TEXT, text);
		subTypes.setMediaType(KeyTypeParameter.PGP.getMediaType());
		key.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(text, key.getText());
		assertNull(key.getUrl());
		assertNull(key.getData());
		assertEquals(KeyTypeParameter.PGP, key.getContentType());
		assertWarnings(0, warnings);
	}
}
