package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

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
public class UriTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCard vcard = new VCard();
	private final VCardSubTypes subTypes = new VCardSubTypes();

	private final String uri = "http://www.example.com";
	private final UriType withValue = new UriType("NAME", uri);
	private final UriType empty = new UriType("NAME");
	private UriType t;

	@Before
	public void before() {
		t = new UriType("NAME");
		subTypes.clear();
		warnings.clear();
	}

	@Test
	public void validate() {
		assertWarnings(1, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		assertWarnings(0, withValue.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(withValue.getTypeName().toLowerCase());
		xe.append(VCardDataType.URI, uri);
		Document expected = xe.document();

		xe = new XCardElement(withValue.getTypeName().toLowerCase());
		Document actual = xe.document();
		withValue.marshalXml(xe.element(), version, compatibilityMode);

		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalXml_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(empty.getTypeName().toLowerCase());
		xe.append(VCardDataType.URI, "");
		Document expected = xe.document();

		xe = new XCardElement(empty.getTypeName().toLowerCase());
		Document actual = xe.document();
		empty.marshalXml(xe.element(), version, compatibilityMode);

		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = withValue.marshalJson(version);

		assertJCardValue(VCardDataType.URI, uri, value);
	}

	@Test
	public void marshalJson_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = empty.marshalJson(version);

		assertJCardValue(VCardDataType.URI, null, value);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(withValue.getTypeName().toLowerCase());
		xe.append(VCardDataType.URI, uri);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(uri, t.getValue());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(withValue.getTypeName().toLowerCase());
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalJson() {
		//same as TextType.unmarshalJson()
	}
}
