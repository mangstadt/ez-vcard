package ezvcard.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.RawType;
import ezvcard.types.TypeList;
import ezvcard.types.VCardType;
import ezvcard.types.XmlType;
import ezvcard.util.IOUtils;
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
 * Parses {@link VCard} objects from an XML document (xCard format).
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCardReader implements IParser {
	private static final VCardVersion version = VCardVersion.V4_0;
	public static final XCardNamespaceContext nsContext = new XCardNamespaceContext("v");

	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private List<String> warnings = new ArrayList<String>();
	private Map<QName, Class<? extends VCardType>> extendedTypeClasses = new HashMap<QName, Class<? extends VCardType>>();

	/**
	 * The <code>&lt;vcard&gt;</code> elements within the XML document.
	 */
	private Iterator<Element> vcardElements;

	/**
	 * Creates an xCard reader.
	 * @param xml the XML string to read the vCards from
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardReader(String xml) throws SAXException {
		try {
			init(new StringReader(xml));
		} catch (IOException e) {
			//reading from string
		}
	}

	/**
	 * Creates an xCard reader.
	 * @param in the input stream to read the vCards from
	 * @throws IOException if there's a problem reading from the input stream
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardReader(InputStream in) throws SAXException, IOException {
		this(new InputStreamReader(in));
	}

	/**
	 * Creates an xCard reader.
	 * @param file the file to read the vCards from
	 * @throws IOException if there's a problem reading from the file
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardReader(File file) throws SAXException, IOException {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			init(reader);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	/**
	 * Creates an xCard reader.
	 * @param reader the reader to read the vCards from
	 * @throws IOException if there's a problem reading from the reader
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardReader(Reader reader) throws SAXException, IOException {
		init(reader);
	}

	/**
	 * Creates an xCard reader.
	 * @param document the XML document to read the vCards from
	 */
	public XCardReader(Document document) {
		init(document);
	}

	private void init(Reader reader) throws SAXException, IOException {
		init(XmlUtils.toDocument(reader));
	}

	private void init(Document document) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(nsContext);

			String prefix = nsContext.prefix;
			NodeList nodeList = (NodeList) xpath.evaluate("//" + prefix + ":vcards/" + prefix + ":vcard", document, XPathConstants.NODESET);
			vcardElements = XmlUtils.toElementList(nodeList).iterator();
		} catch (XPathExpressionException e) {
			//never thrown, xpath expression is hard coded
		}
	}

	/**
	 * Gets the compatibility mode. Used for customizing the unmarshalling
	 * process based on the application that generated the vCard.
	 * @return the compatibility mode
	 */
	@Deprecated
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Sets the compatibility mode. Used for customizing the unmarshalling
	 * process based on the application that generated the vCard.
	 * @param compatibilityMode the compatibility mode
	 */
	@Deprecated
	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	//@Override
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.put(getQNameFromTypeClass(clazz), clazz);
	}

	//@Override
	public void unregisterExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.remove(getQNameFromTypeClass(clazz));
	}

	//@Override
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	//@Override
	public VCard readNext() {
		warnings.clear();

		if (!vcardElements.hasNext()) {
			return null;
		}

		VCard vcard = new VCard();
		vcard.setVersion(version);

		Element vcardElement = vcardElements.next();

		String ns = version.getXmlNamespace();
		List<Element> children = XmlUtils.toElementList(vcardElement.getChildNodes());
		List<String> warningsBuf = new ArrayList<String>();
		for (Element child : children) {
			if ("group".equals(child.getLocalName()) && ns.equals(child.getNamespaceURI())) {
				String group = child.getAttribute("name");
				if (group.length() == 0) {
					group = null;
				}
				List<Element> propElements = XmlUtils.toElementList(child.getChildNodes());
				for (Element propElement : propElements) {
					parseAndAddElement(propElement, group, version, vcard, warningsBuf);
				}
			} else {
				parseAndAddElement(child, null, version, vcard, warningsBuf);
			}
		}

		return vcard;
	}

	/**
	 * Parses a property element from the XML document and adds the property to
	 * the vCard.
	 * @param element the element to parse
	 * @param group the group name or null if the property does not belong to a
	 * group
	 * @param version the vCard version
	 * @param vcard the vCard object
	 * @param warningsBuf the list to add the warnings to
	 */
	private void parseAndAddElement(Element element, String group, VCardVersion version, VCard vcard, List<String> warningsBuf) {
		warningsBuf.clear();

		VCardSubTypes subTypes = parseSubTypes(element);
		VCardType type = createTypeObject(element.getLocalName(), element.getNamespaceURI());
		type.setGroup(group);
		try {
			try {
				type.unmarshalXml(subTypes, element, version, warningsBuf, compatibilityMode);
			} catch (UnsupportedOperationException e) {
				//type class does not support xCard
				warningsBuf.add("Property class \"" + type.getClass().getName() + "\" does not support xCard unmarshalling.  It will be unmarshalled as a " + XmlType.NAME + " property instead.");
				type = new XmlType();
				type.setGroup(group);
				type.unmarshalXml(subTypes, element, version, warningsBuf, compatibilityMode);
			}
			vcard.addProperty(type);
		} catch (SkipMeException e) {
			warningsBuf.add("Property has requested that it be skipped: " + e.getMessage());
		} catch (EmbeddedVCardException e) {
			warningsBuf.add("Property will not be unmarshalled because xCard does not supported embedded vCards.");
		} finally {
			for (String warning : warningsBuf) {
				addWarning(warning, type.getTypeName());
			}
		}
	}

	/**
	 * Parses the property parameters (aka "sub types").
	 * @param element the property's XML element
	 * @return the parsed parameters
	 */
	private VCardSubTypes parseSubTypes(Element element) {
		VCardSubTypes subTypes = new VCardSubTypes();

		List<Element> parametersElements = XmlUtils.toElementList(element.getElementsByTagNameNS(version.getXmlNamespace(), "parameters"));
		for (Element parametersElement : parametersElements) { // foreach "<parameters>" element (there should only be 1 though)
			List<Element> paramElements = XmlUtils.toElementList(parametersElement.getChildNodes());
			for (Element paramElement : paramElements) {
				String name = paramElement.getLocalName().toUpperCase();
				List<Element> valueElements = XmlUtils.toElementList(paramElement.getChildNodes());
				if (valueElements.isEmpty()) {
					String value = paramElement.getTextContent();
					subTypes.put(name, value);
				} else {
					for (Element valueElement : valueElements) {
						String value = valueElement.getTextContent();
						subTypes.put(name, value);
					}
				}
			}

			//remove the <parameters> element from the DOM
			element.removeChild(parametersElement);
		}

		return subTypes;
	}

	/**
	 * Creates the appropriate VCardType instance given the vCard property name.
	 * This method does not unmarshal the type, it just creates the type object.
	 * @param name the property name (e.g. "fn")
	 * @param ns the namespace of the element
	 * @return the type that was created
	 */
	private VCardType createTypeObject(String name, String ns) {
		name = name.toUpperCase();

		Class<? extends VCardType> clazz = TypeList.getTypeClass(name);
		if (clazz != null && VCardVersion.V4_0.getXmlNamespace().equals(ns)) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				//it is the responsibility of the ez-vcard developer to ensure that this exception is never thrown
				//all type classes defined in the ez-vcard library MUST have public, no-arg constructors
				throw new RuntimeException(e);
			}
		} else {
			Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(new QName(ns, name.toLowerCase()));
			if (extendedTypeClass != null) {
				try {
					return extendedTypeClass.newInstance();
				} catch (Exception e) {
					//this should never happen because the type class is checked to see if it has a public, no-arg constructor in the "registerExtendedType" method
					throw new RuntimeException("Extended type class \"" + extendedTypeClass.getName() + "\" MUST have a public, no-arg constructor.");
				}
			} else if (name.startsWith("X-")) {
				return new RawType(name);
			} else {
				//add as an XML property
				return new XmlType();
			}
		}
	}

	/**
	 * Gets the QName from a type class.
	 * @param clazz the type class
	 * @return the QName
	 */
	private QName getQNameFromTypeClass(Class<? extends VCardType> clazz) {
		try {
			VCardType type = clazz.newInstance();
			QName qname = type.getQName();
			if (qname == null) {
				qname = new QName(version.getXmlNamespace(), type.getTypeName().toLowerCase());
			}
			return qname;
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	/**
	 * Namespace context to use for xCard XPath expressions.
	 * @see XPath#setNamespaceContext(NamespaceContext)
	 */
	public static class XCardNamespaceContext implements NamespaceContext {
		private final String ns;
		private final String prefix;

		/**
		 * @param prefix the prefix to use
		 */
		public XCardNamespaceContext(String prefix) {
			ns = version.getXmlNamespace();
			this.prefix = prefix;
		}

		//@Override
		public String getNamespaceURI(String prefix) {
			if (prefix != null && prefix.equals(this.prefix)) {
				return ns;
			}
			return null;
		}

		//@Override
		public String getPrefix(String ns) {
			if (ns != null && ns.equals(this.ns)) {
				return prefix;
			}
			return null;
		}

		//@Override
		public Iterator<String> getPrefixes(String ns) {
			if (ns.equals(this.ns)) {
				return Arrays.asList(prefix).iterator();
			}
			return null;
		}
	}

	private void addWarning(String message, String propertyName) {
		warnings.add(propertyName + " property: " + message);
	}
}
