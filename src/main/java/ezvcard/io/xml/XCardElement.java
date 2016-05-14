package ezvcard.io.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
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
 * Wraps xCard functionality around an XML {@link Element} object.
 * @author Michael Angstadt
 */
public class XCardElement {
	private final Document document;
	private final Element element;
	private final VCardVersion version;
	private final String namespace;

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
	 * Gets the first value with one of the given data types.
	 * @param dataTypes the data type(s) to look for (null signifies the
	 * "unknown" data type)
	 * @return the value or null if not found
	 */
	public String first(VCardDataType... dataTypes) {
		String names[] = new String[dataTypes.length];
		for (int i = 0; i < dataTypes.length; i++) {
			VCardDataType dataType = dataTypes[i];
			names[i] = toLocalName(dataType);
		}
		return first(names);
	}

	/**
	 * Gets the value of the first child element with one of the given names.
	 * @param names the possible names of the element
	 * @return the element's text or null if not found
	 */
	public String first(String... names) {
		List<String> localNamesList = Arrays.asList(names);
		for (Element child : children()) {
			if (localNamesList.contains(child.getLocalName()) && namespace.equals(child.getNamespaceURI())) {
				return child.getTextContent();
			}
		}
		return null;
	}

	/**
	 * Gets all the values of a given data type.
	 * @param dataType the data type to look for
	 * @return the values
	 */
	public List<String> all(VCardDataType dataType) {
		String dataTypeStr = toLocalName(dataType);
		return all(dataTypeStr);
	}

	/**
	 * Gets the value of all non-empty child elements that have the given name.
	 * @param localName the element name
	 * @return the values of the child elements
	 */
	public List<String> all(String localName) {
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
	 * Adds a value.
	 * @param dataType the data type or null for the "unknown" data type
	 * @param value the value
	 * @return the created element
	 */
	public Element append(VCardDataType dataType, String value) {
		String dataTypeStr = toLocalName(dataType);
		return append(dataTypeStr, value);
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
		return child;
	}

	/**
	 * Adds multiple child elements, each with the same name.
	 * @param name the name for all the child elements
	 * @param values the values of each child element
	 * @return the created elements
	 */
	public List<Element> append(String name, Collection<String> values) {
		if (values.isEmpty()) {
			Element element = append(name, (String) null);
			return Arrays.asList(element);
		}

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
		return XmlUtils.toElementList(element.getChildNodes());
	}

	/**
	 * Finds the first child element that has the xCard namespace and returns
	 * its data type and value. If no such element is found, the parent
	 * {@link XCardElement}'s text content, along with a null data type, is
	 * returned.
	 * @return the value and data type
	 */
	public XCardValue firstValue() {
		String elementNamespace = version.getXmlNamespace();
		for (Element child : children()) {
			String childNamespace = child.getNamespaceURI();
			if (elementNamespace.equals(childNamespace)) {
				VCardDataType dataType = toDataType(child.getLocalName());
				String value = child.getTextContent();
				return new XCardValue(dataType, value);
			}
		}

		return new XCardValue(null, element.getTextContent());
	}

	/**
	 * Gets the appropriate XML local name of a {@link VCardDataType} object.
	 * @param dataType the data type or null for "unknown"
	 * @return the local name (e.g. "text")
	 */
	private static String toLocalName(VCardDataType dataType) {
		return (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
	}

	/**
	 * Converts an XML local name to the appropriate {@link VCardDataType}
	 * object.
	 * @param localName the local name (e.g. "text")
	 * @return the data type or null for "unknown"
	 */
	private static VCardDataType toDataType(String localName) {
		return "unknown".equals(localName) ? null : VCardDataType.get(localName);
	}

	/**
	 * Represents the data type and value of a child element under an
	 * {@link XCardElement}.
	 */
	public static class XCardValue {
		private final VCardDataType dataType;
		private final String value;

		/**
		 * @param dataType the data type or null if "unknown"
		 * @param value the value
		 */
		public XCardValue(VCardDataType dataType, String value) {
			this.dataType = dataType;
			this.value = value;
		}

		/**
		 * Gets the data type
		 * @return the data type or null if "unknown"
		 */
		public VCardDataType getDataType() {
			return dataType;
		}

		/**
		 * Get the value.
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
	}
}
