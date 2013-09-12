package ezvcard.types;

import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.XmlUtils;

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
public class XmlTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCard vcard = new VCard();
	private final VCardSubTypes subTypes = new VCardSubTypes();

	@Before
	public void before() {
		subTypes.clear();
		warnings.clear();
	}

	@Test
	public void validate() throws Throwable {
		XmlType empty = new XmlType();
		assertWarnings(2, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(2, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		XmlType withValue = new XmlType("<foo/>");
		assertWarnings(1, withValue.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, withValue.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V4_0, vcard));
	}

	@Test(expected = SAXException.class)
	public void invalid_xml() throws Throwable {
		new XmlType("not valid XML");
	}

	@Test
	public void unmarshalText() throws Throwable {
		VCardVersion version = VCardVersion.V4_0;
		XmlType t = new XmlType();
		t.unmarshalText(subTypes, "<a href=\"http://www.example.com\">some html</a>", version, warnings, compatibilityMode);

		assertXMLEqual(XmlUtils.toDocument("<a href=\"http://www.example.com\">some html</a>"), t.getDocument());
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_invalid() throws Throwable {
		VCardVersion version = VCardVersion.V4_0;
		XmlType t = new XmlType();
		t.unmarshalText(subTypes, "invalid", version, warnings, compatibilityMode);
	}

	@Test
	public void marshalText() throws Throwable {
		VCardVersion version = VCardVersion.V4_0;
		String expected = "<a href=\"http://www.example.com\">some html</a>";
		XmlType xml = new XmlType(expected);
		String actual = xml.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_null() throws Throwable {
		VCardVersion version = VCardVersion.V4_0;
		XmlType xml = new XmlType();
		String value = xml.marshalText(version, compatibilityMode);

		assertEquals("", value);
	}

	@Test
	public void marshalXml() throws Throwable {
		VCardVersion version = VCardVersion.V4_0;
		Document actual = XmlUtils.toDocument("<root></root>");
		Element root = XmlUtils.getRootElement(actual);
		XmlType xml = new XmlType("<a href=\"http://www.example.com\">some html</a>");
		xml.marshalXml(root, version, compatibilityMode);

		Document expected = XmlUtils.toDocument("<root><a href=\"http://www.example.com\">some html</a></root>");

		assertXMLEqual(expected, actual);
	}

	@Test
	public void unmarshalXml() throws Throwable {
		VCardVersion version = VCardVersion.V4_0;
		String xml = "<a href=\"http://www.example.com\">some html</a>";
		Document expected = XmlUtils.toDocument(xml);
		Element element = XmlUtils.getRootElement(expected);
		XmlType t = new XmlType();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertXMLEqual(expected, t.getDocument());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_remove_parameters() throws Throwable {
		//@formatter:off
		String inputXml =
		"<a href=\"http://www.example.com\">" +
			"<parameters xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
				"<one>1</one>" +
			"</parameters>" +
			"text" +
		"</a>";
		//@formatter:on
		VCardVersion version = VCardVersion.V4_0;

		Document inputDocument = XmlUtils.toDocument(inputXml);
		Element element = XmlUtils.getRootElement(inputDocument);
		XmlType t = new XmlType();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		String expectedXml = "<a href=\"http://www.example.com\">text</a>";
		Document expectedDocument = XmlUtils.toDocument(expectedXml);

		assertXMLEqual(expectedDocument, t.getDocument());
		assertWarnings(0, warnings);
	}

}
