package ezvcard.util;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
public class XmlUtilsTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	//@formatter:off
	private final String xml =
		"<root>" +
			"   \n" +
			"<child1 />" +
			"some text" +
			"<n:child2 xmlns:n=\"http://example.com\" />" +
			"<!-- comment -->" +
			"More text" +
		"</root>";
	//@formatter:on

	@Test
	public void createDocument() throws Exception {
		Document document = XmlUtils.createDocument();
		assertNotNull(document);
	}

	@Test
	public void toDocument() throws Exception {
		Document document = XmlUtils.toDocument(xml);

		Element root = (Element) document.getFirstChild();
		assertEquals("root", root.getLocalName());
		assertNull(root.getNamespaceURI());

		NodeList nodes = root.getChildNodes();
		assertEquals(5, nodes.getLength());

		int i = 0;
		Text text = (Text) nodes.item(i++);
		assertEquals("   \n", text.getTextContent());

		Element element = (Element) nodes.item(i++);
		assertEquals("child1", element.getLocalName());
		assertNull(element.getNamespaceURI());

		text = (Text) nodes.item(i++);
		assertEquals("some text", text.getTextContent());

		element = (Element) nodes.item(i++);
		assertEquals("child2", element.getLocalName());
		assertEquals("http://example.com", element.getNamespaceURI());

		text = (Text) nodes.item(i++);
		assertEquals("More text", text.getTextContent());
	}

	@Test
	public void toDocument_utf8() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<root><child>\u1e08hild</child></root>";
		//@formatter:on

		File file = tempFolder.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write(xml);
		writer.close();

		Document document = XmlUtils.toDocument(new FileInputStream(file));
		Element root = (Element) document.getFirstChild();
		NodeList nodes = root.getChildNodes();
		Element child = (Element) nodes.item(0);
		assertEquals("\u1e08hild", child.getTextContent());
	}

	@Test(expected = SAXException.class)
	public void toDocument_invalid_xml() throws Exception {
		String xml = "not-xml";
		XmlUtils.toDocument(xml);
	}

	@Test
	public void toString_() throws Exception {
		Document expected = XmlUtils.toDocument(xml);
		String string = XmlUtils.toString(expected);
		Document actual = XmlUtils.toDocument(string);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void toString_output_properties() throws Exception {
		Document expected = XmlUtils.toDocument(xml);
		Map<String, String> outputProperties = new HashMap<String, String>();
		outputProperties.put(OutputKeys.STANDALONE, "no");
		String string = XmlUtils.toString(expected, outputProperties);
		Document actual = XmlUtils.toDocument(string);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void toElementList() throws Exception {
		Document document = XmlUtils.toDocument(xml);
		List<Element> elements = XmlUtils.toElementList(document.getFirstChild().getChildNodes());
		assertEquals(2, elements.size());
		assertEquals("child1", elements.get(0).getLocalName());
		assertEquals("child2", elements.get(1).getLocalName());
		document = XmlUtils.toDocument(xml);

		document = XmlUtils.toDocument("<root />");
		elements = XmlUtils.toElementList(document.getFirstChild().getChildNodes());
		assertTrue(elements.isEmpty());
	}

	@Test
	public void toElementList_no_children() throws Exception {
		String xml = "<root/>";
		Document document = XmlUtils.toDocument(xml);

		List<Element> elements = XmlUtils.toElementList(document.getFirstChild().getChildNodes());
		assertTrue(elements.isEmpty());
	}

	@Test
	public void getFirstChildElement() throws Exception {
		Document document = XmlUtils.toDocument(xml);
		Element root = (Element) document.getFirstChild();
		Element element = XmlUtils.getFirstChildElement(root);
		assertEquals("child1", element.getLocalName());
	}

	@Test
	public void getFirstChildElement_no_children() throws Exception {
		Document document = XmlUtils.toDocument(xml);
		Element root = (Element) document.getFirstChild();
		Element child1 = XmlUtils.getFirstChildElement(root);
		assertNull(XmlUtils.getFirstChildElement(child1));
	}
}
