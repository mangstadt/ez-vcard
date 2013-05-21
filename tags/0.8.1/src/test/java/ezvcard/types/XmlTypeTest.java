package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
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
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();

	@Before
	public void before() {
		subTypes.clear();
		warnings.clear();
	}

	@Test
	public void marshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		Document actual = XmlUtils.toDocument("<root></root>");
		Element root = XmlUtils.getRootElement(actual);
		XmlType xml = new XmlType("<a href=\"http://www.example.com\">some html</a>");
		xml.marshalXml(root, version, warnings, compatibilityMode);

		Document expected = XmlUtils.toDocument("<root><a href=\"http://www.example.com\">some html</a></root>");

		assertXMLEqual(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalXml_invalid_xml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		Document actual = XmlUtils.toDocument("<root></root>");
		Element root = XmlUtils.getRootElement(actual);

		XmlType xml = new XmlType("not valid XML");
		xml.marshalXml(root, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		String xml = "<a href=\"http://www.example.com\">some html</a>";
		Document document = XmlUtils.toDocument(xml);
		Element element = XmlUtils.getRootElement(document);
		XmlType t = new XmlType();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertEquals("<a href=\"http://www.example.com\">some html</a>", t.getValue());
		assertEquals(0, warnings.size());
	}
}
