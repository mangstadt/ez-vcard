package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HtmlUtils;
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
public class TextTypeTest {
	final String newline = System.getProperty("line.separator");
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();

	final TextType textType = new TextType("NAME", "This is a test of the TextType.\nOne, two, three; and \\four\\.");
	TextType t;

	@Before
	public void before() {
		t = new TextType("NAME");
		subTypes.clear();
		warnings.clear();
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "This is a test of the TextType.\nOne\\, two\\, three\\; and \\\\four\\\\."; //newlines are escaped in the VCardWriter
		String actual = textType.marshalText(version, warnings, compatibilityMode);

		assertEquals(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(textType.getTypeName().toLowerCase());
		xe.text("This is a test of the TextType.\nOne, two, three; and \\four\\.");
		Document expected = xe.document();
		xe = new XCardElement(textType.getTypeName().toLowerCase());
		Document actual = xe.document();
		textType.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = textType.marshalJson(version, warnings);
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{"This is a test of the TextType.\nOne, two, three; and \\four\\."})
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText() {
		VCardVersion version = VCardVersion.V2_1;
		t.unmarshalText(subTypes, "This is a test of the TextType.\\nOne\\, two\\, three\\; and \\\\four\\\\.", version, warnings, compatibilityMode);
		String expected = "This is a test of the TextType." + newline + "One, two, three; and \\four\\.";
		String actual = t.getValue();

		assertEquals(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("name");
		xe.text("This is a test of the TextType.\nOne, two, three; and \\four\\.");
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		String expected = "This is a test of the TextType.\nOne, two, three; and \\four\\.";
		String actual = t.getValue();

		assertEquals(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>This is a test of the TextType.<br/>One, two, three; and \\four\\.</div>");
		//@formatter:on

		t.unmarshalHtml(element, warnings);
		String expected = "This is a test of the TextType.\nOne, two, three; and \\four\\.";
		String actual = t.getValue();

		assertEquals(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue();
		value.addValues("This is a test of the TextType.\nOne, two, three; and \\four\\.");
		value.setDataType(JCardDataType.TEXT);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("This is a test of the TextType.\nOne, two, three; and \\four\\.", t.getValue());
		assertEquals(0, warnings.size());
	}
}
