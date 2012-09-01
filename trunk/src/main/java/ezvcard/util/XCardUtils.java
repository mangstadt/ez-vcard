package ezvcard.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ezvcard.VCardVersion;

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
 * Contains utility methods for the xCard standard.
 * @author Michael Angstadt
 */
public class XCardUtils {
	/**
	 * Converts a {@link NodeList} object to a {@link List} object containing
	 * the {@link Element} objects that are inside the NodeList.
	 * @param nl the node list
	 * @return the list of elements
	 */
	public static List<Element> toElementList(NodeList nl) {
		List<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				elements.add((Element) node);
			}
		}
		return elements;
	}

	/**
	 * Gets the first {@link Element} in a {@link NodeList}.
	 * @param nl the node list
	 * @return the first element or null if there are no elements in the node
	 * list
	 */
	public static Element getFirstElement(NodeList nl) {
		return getFirstElement(nl, null);
	}

	/**
	 * Gets the first {@link Element} in a {@link NodeList} that has a certain
	 * name.
	 * @param nl the node list
	 * @param elementName the name of the element to look for
	 * @return the first element that has the name or null if not found
	 */
	public static Element getFirstElement(NodeList nl, String elementName) {
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && (elementName == null || elementName.equals(node.getNodeName()))) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * Given a list of child element names, this method returns the text content
	 * of the first child element it can find.
	 * @param parent the parent element
	 * @param childNames the names of the child elements to search for
	 * @return the text content of the first child element it finds or null if
	 * none were found
	 */
	public static String getFirstChildText(Element parent, String... childNames) {
		List<Element> children = toElementList(parent.getChildNodes());
		for (Element child : children) {
			String curName = child.getNodeName();
			for (String childName : childNames) {
				if (childName.equals(curName)) {
					return child.getTextContent();
				}
			}
		}
		return null;
	}

	/**
	 * Creates an XML element and appends it as a child to a parent XML element.
	 * @param parent the parent element
	 * @param childName the name of the new child element
	 * @param text the text content of the new child element
	 * @param version the vCard version
	 * @return the new child element
	 */
	public static Element appendChild(Element parent, String childName, String text, VCardVersion version) {
		return appendChild(parent, childName, text, getXCardNs(version));
	}

	/**
	 * Creates an XML element and appends it as a child to a parent XML element.
	 * @param parent the parent element
	 * @param childName the name of the new child element
	 * @param text the text content of the new child element
	 * @param ns the namespace of the new child element
	 * @return the new child element
	 */
	public static Element appendChild(Element parent, String childName, String text, String ns) {
		Element child = parent.getOwnerDocument().createElementNS(ns, childName);
		child.setTextContent(text);
		parent.appendChild(child);
		return child;
	}

	/**
	 * Gets the xCard namespace for a particular vCard version.
	 * @param version the vCard version
	 * @return the namespace
	 */
	public static String getXCardNs(VCardVersion version) {
		return "urn:ietf:params:xml:ns:vcard-" + version.getVersion();
	}

	/**
	 * Parses an XML string into a DOM.
	 * @param xml the XML string
	 * @return the parsed XML
	 * @throws SAXException if the string is not valid XML
	 */
	public static Document toDocument(String xml) throws SAXException {
		try {
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			DocumentBuilder db = fact.newDocumentBuilder();
			return db.parse(new ByteArrayInputStream(xml.getBytes()));
		} catch (ParserConfigurationException e) {
			//should never be thrown
			return null;
		} catch (IOException e) {
			//never thrown because it's reading from a string
			return null;
		}
	}
}
