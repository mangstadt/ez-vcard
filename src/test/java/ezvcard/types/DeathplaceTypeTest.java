package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

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
public class DeathplaceTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final VCard vcard = new VCard();

	private final String text = "Mount, St. Helens";
	private final String textEscaped = "Mount\\, St. Helens";
	private final String uri = "geo:46.176502,-122.191658";
	private final DeathplaceType textType = new DeathplaceType();
	{
		textType.setText(text);
	}

	private final DeathplaceType uriType = new DeathplaceType();
	{
		uriType.setUri(uri);
	}
	private final DeathplaceType emptyType = new DeathplaceType();
	private DeathplaceType t;

	@Before
	public void before() {
		t = new DeathplaceType();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void validate() {
		assertWarnings(2, t.validate(VCardVersion.V2_1, vcard));
		assertWarnings(2, t.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, t.validate(VCardVersion.V4_0, vcard));

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

		assertEquals(0, subTypes.size());
	}

	@Test
	public void marshalSubTypes_uri() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = uriType.marshalSubTypes(version, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertEquals(VCardDataType.URI, subTypes.getValue());
	}

	@Test
	public void marshalText_text() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = textType.marshalText(version, compatibilityMode);

		assertEquals(textEscaped, actual);
	}

	@Test
	public void marshalText_uri() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = uriType.marshalText(version, compatibilityMode);

		assertEquals(uri, actual);
	}

	@Test
	public void marshalText_empty() {
		VCardVersion version = VCardVersion.V4_0;
		String value = emptyType.marshalText(version, compatibilityMode);

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
	public void marshalXml_empty() {
		assertMarshalXml(emptyType, "<text/>");
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
	public void marshalJson_empty() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = emptyType.marshalJson(version);

		assertJCardValue(VCardDataType.TEXT, "", value);
	}

	@Test
	public void unmarshalText_text_without_value_parameter() {
		VCardVersion version = VCardVersion.V4_0;
		t.unmarshalText(subTypes, textEscaped, version, warnings, compatibilityMode);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_text_with_value_parameter() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.TEXT);
		t.unmarshalText(subTypes, textEscaped, version, warnings, compatibilityMode);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_uri_without_value_parameter() {
		VCardVersion version = VCardVersion.V4_0;
		t.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);

		//parses as text
		assertEquals(uri, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_uri_with_value_parameter() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.URI);
		t.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DeathplaceType.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, text);
		Element element = xe.element();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DeathplaceType.NAME.toLowerCase());
		xe.append(VCardDataType.URI, uri);
		Element element = xe.element();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_both() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DeathplaceType.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, text);
		xe.append(VCardDataType.URI, uri);
		Element element = xe.element();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertEquals(text, t.getText()); //prefers the text
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_empty() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DeathplaceType.NAME.toLowerCase());
		Element element = xe.element();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.TEXT, text);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_uri() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.URI, uri);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_unknown_datatype() {
		//treats it as text
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.LANGUAGE_TAG, uri);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(uri, t.getText());
		assertNull(t.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void setUri() {
		assertNull(t.getUri());

		t.setText(text);
		t.setUri(uri);

		assertEquals(uri, t.getUri());
		assertNull(t.getText());
	}

	@Test
	public void setText() {
		assertNull(t.getText());

		t.setUri(uri);
		t.setText(text);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
	}
}
