package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HtmlUtils;
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
public class RawTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final String propertyValue = "value;value";
	private final RawTypeImpl type = new RawTypeImpl(propertyValue);

	@After
	public void after() {
		warnings.clear();
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = type.marshalText(version, compatibilityMode);
		assertEquals(propertyValue, actual);
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		xe.append("unknown", propertyValue);
		Document expected = xe.document();
		xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();
		type.marshalXml(xe.element(), version, compatibilityMode);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = type.marshalJson(version);

		assertJCardValue(null, propertyValue, value);
	}

	@Test
	public void unmarshalText() {
		VCardVersion version = VCardVersion.V2_1;
		RawTypeImpl type = new RawTypeImpl();
		type.unmarshalText(subTypes, propertyValue, version, warnings, compatibilityMode);

		assertEquals(propertyValue, type.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		RawTypeImpl type = new RawTypeImpl();
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, propertyValue);
		xe.append(VCardDataType.TEXT, "another value");
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertEquals(propertyValue, type.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_non_xcard_child_elements() {
		VCardVersion version = VCardVersion.V4_0;
		RawTypeImpl type = new RawTypeImpl();
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		Element child1 = xe.document().createElementNS("http://example.com", "one");
		child1.setTextContent("1");
		xe.element().appendChild(child1);
		Element child2 = xe.document().createElementNS("http://example.com", "two");
		child2.setTextContent("2");
		xe.element().appendChild(child2);
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertEquals("1", type.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_no_child_elements() {
		VCardVersion version = VCardVersion.V4_0;
		RawTypeImpl type = new RawTypeImpl();
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		xe.element().setTextContent(propertyValue);
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertEquals(propertyValue, type.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_no_text_content() {
		VCardVersion version = VCardVersion.V4_0;
		RawTypeImpl type = new RawTypeImpl();
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertEquals("", type.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + propertyValue + "</div>");

		RawTypeImpl type = new RawTypeImpl();
		type.unmarshalHtml(element, warnings);

		assertEquals(propertyValue, type.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.TEXT, propertyValue);

		RawTypeImpl type = new RawTypeImpl();
		type.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(propertyValue, type.getValue());
		assertWarnings(0, warnings);
	}

	private class RawTypeImpl extends RawType {
		public static final String NAME = "RAW";

		public RawTypeImpl() {
			super(NAME);
		}

		public RawTypeImpl(String value) {
			super(NAME, value);
		}
	}
}
