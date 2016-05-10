package ezvcard.io.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.xml.XCardElement.XCardValue;
import ezvcard.util.XmlUtils;

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
public class XCardElementTest {
	@Test
	public void first() {
		XCardElement xcardElement = build("<prop><one>1</one><two>2</two></prop>");
		assertEquals("2", xcardElement.first("two"));
	}

	@Test
	public void first_data_type() {
		XCardElement xcardElement = build("<prop><one>1</one><text>2</text></prop>");
		assertEquals("2", xcardElement.first(VCardDataType.TEXT));
	}

	@Test
	public void first_unknown() {
		XCardElement xcardElement = build("<prop><one>1</one><unknown>2</unknown></prop>");
		assertEquals("2", xcardElement.first((VCardDataType) null));
	}

	@Test
	public void first_xcard_namespace_with_prefix() {
		XCardElement xcardElement = build("<v:prop><v:one>1</v:one><v:two>2</v:two></v:prop>", "v");
		assertEquals("2", xcardElement.first("two"));
	}

	@Test
	public void first_empty() {
		XCardElement xcardElement = build("<prop><one>1</one><two></two></prop>");
		assertEquals("", xcardElement.first("two"));
	}

	@Test
	public void first_none() {
		XCardElement xcardElement = build("<prop><one>1</one><two>2</two></prop>");
		assertNull(xcardElement.first("three"));
	}

	@Test
	public void first_multiple_names() {
		XCardElement xcardElement = build("<prop><one>1</one><two>2</two><three>3</three></prop>");
		assertEquals("2", xcardElement.first("two", "three"));
		assertEquals("2", xcardElement.first("three", "two"));
	}

	@Test
	public void first_only_xcard_namespace() {
		XCardElement xcardElement = build("<prop><n:four xmlns:n=\"http://example.com\"></n:four></prop>");
		assertNull(xcardElement.first("four"));
	}

	@Test
	public void all() {
		XCardElement xcardElement = build("<prop><one>1</one><two>2</two><two /><three>3</three><two>2-2</two></prop>");
		assertEquals(Arrays.asList("2", "2-2"), xcardElement.all("two"));
	}

	@Test
	public void all_data_type() {
		XCardElement xcardElement = build("<prop><one>1</one><text>2</text><two /><three>3</three><text>2-2</text></prop>");
		assertEquals(Arrays.asList("2", "2-2"), xcardElement.all(VCardDataType.TEXT));
	}

	@Test
	public void all_unknown() {
		XCardElement xcardElement = build("<prop><one>1</one><unknown>2</unknown><two /><three>3</three><unknown>2-2</unknown></prop>");
		assertEquals(Arrays.asList("2", "2-2"), xcardElement.all((VCardDataType) null));
	}

	@Test
	public void all_none() {
		XCardElement xcardElement = build("<prop><one>1</one><two>2</two></prop>");
		assertTrue(xcardElement.all("three").isEmpty());
	}

	@Test
	public void firstValue() {
		XCardElement xcardElement = build("<prop><text>one</text></prop>");
		XCardValue child = xcardElement.firstValue();
		assertEquals(VCardDataType.TEXT, child.getDataType());
		assertEquals("one", child.getValue());
	}

	@Test
	public void firstValue_unknown() {
		XCardElement xcardElement = build("<prop><unknown>one</unknown></prop>");
		XCardValue child = xcardElement.firstValue();
		assertNull(child.getDataType());
		assertEquals("one", child.getValue());
	}

	@Test
	public void firstValue_namespace() {
		XCardElement xcardElement = build("<prop><n:foo xmlns:n=\"http://example.com\">one</n:foo><text>two</text></prop>");
		XCardValue child = xcardElement.firstValue();
		assertEquals(VCardDataType.TEXT, child.getDataType());
		assertEquals("two", child.getValue());
	}

	@Test
	public void firstValue_no_xcard_children() {
		XCardElement xcardElement = build("<prop><n:foo xmlns:n=\"http://example.com\">one</n:foo><n:bar xmlns:n=\"http://example.com\">two</n:bar></prop>");
		XCardValue child = xcardElement.firstValue();
		assertNull(child.getDataType());
		assertEquals("onetwo", child.getValue());
	}

	@Test
	public void append() {
		XCardElement xcardElement = build("<prop><one>1</one></prop>");
		Element appendedElement = xcardElement.append("two", "2");
		assertEquals("two", appendedElement.getLocalName());
		assertEquals(VCardVersion.V4_0.getXmlNamespace(), appendedElement.getNamespaceURI());
		assertEquals("2", appendedElement.getTextContent());

		Iterator<Element> it = XmlUtils.toElementList(xcardElement.element().getChildNodes()).iterator();

		Element element = it.next();
		assertEquals("one", element.getLocalName());
		assertEquals(VCardVersion.V4_0.getXmlNamespace(), element.getNamespaceURI());
		assertEquals("1", element.getTextContent());

		element = it.next();
		assertEquals(appendedElement, element);

		assertFalse(it.hasNext());
	}

	@Test
	public void append_multiple() {
		XCardElement xcardElement = build("<prop />");
		List<Element> elements = xcardElement.append("number", Arrays.asList("1", "2", "3"));
		Iterator<Element> it = elements.iterator();

		Element element = it.next();
		assertEquals("number", element.getLocalName());
		assertEquals(VCardVersion.V4_0.getXmlNamespace(), element.getNamespaceURI());
		assertEquals("1", element.getTextContent());

		element = it.next();
		assertEquals("number", element.getLocalName());
		assertEquals(VCardVersion.V4_0.getXmlNamespace(), element.getNamespaceURI());
		assertEquals("2", element.getTextContent());

		element = it.next();
		assertEquals("number", element.getLocalName());
		assertEquals(VCardVersion.V4_0.getXmlNamespace(), element.getNamespaceURI());
		assertEquals("3", element.getTextContent());

		assertFalse(it.hasNext());

		assertEquals(XmlUtils.toElementList(xcardElement.element().getChildNodes()), elements);
	}

	private static XCardElement build(String innerXml) {
		return build(innerXml, null);
	}

	private static XCardElement build(String innerXml, String prefix) {
		//@formatter:off
		String xml =
		"<%sroot xmlns%s=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			innerXml +
		"</%sroot>";
		//@formatter:on
		if (prefix == null) {
			xml = String.format(xml, "", "", "", "", "");
		} else {
			xml = String.format(xml, prefix + ":", ":" + prefix, prefix + ":");
		}

		Document document;
		try {
			document = XmlUtils.toDocument(xml);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
		Element element = XmlUtils.getFirstChildElement(document.getDocumentElement());
		return new XCardElement(element);
	}
}
