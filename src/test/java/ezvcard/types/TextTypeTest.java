package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.XmlUtils;

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
public class TextTypeTest {
	private static final String newline = System.getProperty("line.separator");

	@Test
	public void marshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		TextType t;
		String expected, actual;

		t = new TextType("NAME", "This is a test of the TextType.\nOne, two, three; and \\four\\.");
		expected = "This is a test of the TextType.\nOne\\, two\\, three\\; and \\\\four\\\\."; //newlines are escaped in the VCardWriter
		actual = t.marshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		TextType t;
		Document expected, actual;
		Element element;
		String expectedXml;

		t = new TextType("NAME", "This is a test of the TextType.\nOne, two, three; and \\four\\.");

		expectedXml = "<name xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		expectedXml += "<text>This is a test of the TextType.\nOne, two, three; and \\four\\.</text>";
		expectedXml += "</name>";
		expected = XmlUtils.toDocument(expectedXml);

		actual = XmlUtils.toDocument("<name xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\" />");
		element = XmlUtils.getRootElement(actual);
		t.marshalValue(element, version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		TextType t;
		String expected, actual;

		t = new TextType("NAME");
		t.unmarshalValue(subTypes, "This is a test of the TextType.\\nOne\\, two\\, three\\; and \\\\four\\\\.", version, warnings, compatibilityMode);
		expected = "This is a test of the TextType." + newline + "One, two, three; and \\four\\.";
		actual = t.getValue();
		assertEquals(expected, actual);
	}

	@Test
	public void unmarshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		TextType t;
		String expected, actual;
		String xml;
		Element element;

		xml = "<name xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		xml += "<text>This is a test of the TextType.\nOne, two, three; and \\four\\.</text>";
		xml += "</name>";
		element = XmlUtils.getRootElement(XmlUtils.toDocument(xml));
		t = new TextType("NAME");
		t.unmarshalValue(subTypes, element, version, warnings, compatibilityMode);
		expected = "This is a test of the TextType.\nOne, two, three; and \\four\\.";
		actual = t.getValue();
		assertEquals(expected, actual);
	}
}
