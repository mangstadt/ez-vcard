package ezvcard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardVersion;

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
 * Wraps xCard functionality around an XML {@link Element}.
 * @author Michael Angstadt
 */
public class XCardElement {
	private final Document document;
	private final Element element;
	private List<Element> children;
	private final VCardVersion version;
	private final String namespace;

	/**
	 * Creates a new XML element under its own XML document.
	 * @param propertyName the property name (e.g. "adr")
	 */
	public XCardElement(String propertyName) {
		this(propertyName, VCardVersion.V4_0);
	}

	/**
	 * Creates a new XML element under its own XML document.
	 * @param propertyName the property name (e.g. "adr")
	 * @param version the vCard version
	 */
	public XCardElement(String propertyName, VCardVersion version) {
		this.version = version;
		namespace = version.getXmlNamespace();
		document = XmlUtils.createDocument();
		element = document.createElementNS(namespace, propertyName);
		document.appendChild(element);
	}

	/**
	 * Wraps an existing XML element.
	 * @param element the XML element
	 */
	public XCardElement(Element element) {
		this(element, VCardVersion.V4_0);
	}

	/**
	 * Wraps an existing XML element.
	 * @param element the XML element
	 * @param version the vCard version
	 */
	public XCardElement(Element element, VCardVersion version) {
		this.document = element.getOwnerDocument();
		this.element = element;
		this.version = version;
		namespace = version.getXmlNamespace();
	}

	/**
	 * Gets the value of the first <code>&lt;text&gt;</code> child element.
	 * @return the text value or null if not found
	 */
	public String text() {
		return get("text");
	}

	/**
	 * Gets the value of the first <code>&lt;uri&gt;</code> child element.
	 * @return the URI or null if not found
	 */
	public String uri() {
		return get("uri");
	}

	/**
	 * Gets the value of the first <code>&lt;date-and-or-time&gt;</code> child
	 * element.
	 * @return the value or null if not found
	 */
	public String dateAndOrTime() {
		return get("date-and-or-time");
	}

	/**
	 * Gets the value of the first <code>&lt;timestamp&gt;</code> child element.
	 * @return the timestamp or null if not found
	 */
	public String timestamp() {
		return get("timestamp");
	}

	/**
	 * Gets the value of the first <code>&lt;utc-offset&gt;</code> child
	 * element.
	 * @return the UTC offset or null if not found
	 */
	public String utcOffset() {
		return get("utc-offset");
	}

	/**
	 * Gets the value of the first child element with one of the given names.
	 * @param names the possible names of the element
	 * @return the element's text or null if not found
	 */
	public String get(String... names) {
		List<String> localNamesList = Arrays.asList(names);
		for (Element child : children()) {
			if (localNamesList.contains(child.getLocalName()) && namespace.equals(child.getNamespaceURI())) {
				return child.getTextContent();
			}
		}
		return null;
	}

	/**
	 * Gets the value of all non-empty child elements that have the given name.
	 * @param localName the element name
	 * @return the values of the child elements
	 */
	public List<String> getAll(String localName) {
		List<String> childrenText = new ArrayList<String>();
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && namespace.equals(child.getNamespaceURI())) {
				String text = child.getTextContent();
				if (text.length() > 0) {
					childrenText.add(child.getTextContent());
				}
			}
		}
		return childrenText;
	}

	/**
	 * Adds a <code>&lt;text&gt;</code> child element.
	 * @param text the text
	 * @return the created element
	 */
	public Element text(String text) {
		return append("text", text);
	}

	/**
	 * Adds a <code>&lt;uri&gt;</code> child element.
	 * @param uri the URI
	 * @return the created element
	 */
	public Element uri(String uri) {
		return append("uri", uri);
	}

	/**
	 * Adds a <code>&lt;date-and-or-time&gt;</code> child element.
	 * @param dateAndOrTime the date/time
	 * @return the created element
	 */
	public Element dateAndOrTime(String dateAndOrTime) {
		return append("date-and-or-time", dateAndOrTime);
	}

	/**
	 * Adds a <code>&lt;timestamp&gt;</code> child element.
	 * @param timestamp the timestamp
	 * @return the created element
	 */
	public Element timestamp(String timestamp) {
		return append("timestamp", timestamp);
	}

	/**
	 * Adds a <code>&lt;utc-offset&gt;</code> child element.
	 * @param utcOffset the UTC offset
	 * @return the created element
	 */
	public Element utcOffset(String utcOffset) {
		return append("utc-offset", utcOffset);
	}

	/**
	 * Adds a child element.
	 * @param name the name of the child element
	 * @param value the value of the child element.
	 * @return the created element
	 */
	public Element append(String name, String value) {
		Element child = document.createElementNS(namespace, name);
		child.setTextContent(value);
		element.appendChild(child);

		if (children != null) {
			children.add(child);
		}

		return child;
	}

	/**
	 * Adds multiple child elements, each with the same name.
	 * @param name the name for all the child elements
	 * @param values the values of each child element
	 * @return the created elements
	 */
	public List<Element> append(String name, Collection<String> values) {
		List<Element> elements = new ArrayList<Element>(values.size());
		for (String value : values) {
			elements.add(append(name, value));
		}
		return elements;
	}

	/**
	 * Gets the owner document.
	 * @return the owner document
	 */
	public Document document() {
		return document;
	}

	/**
	 * Gets the wrapped XML element.
	 * @return the wrapped XML element
	 */
	public Element element() {
		return element;
	}

	/**
	 * Gets the vCard version.
	 * @return the vCard version
	 */
	public VCardVersion version() {
		return version;
	}

	/**
	 * Gets the child elements of the XML element.
	 * @return the child elements
	 */
	private List<Element> children() {
		if (children == null) {
			children = XmlUtils.toElementList(element.getChildNodes());
		}
		return children;
	}
}
