package ezvcard.io;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.RawType;
import ezvcard.types.TypeList;
import ezvcard.types.VCardType;
import ezvcard.types.XmlType;
import ezvcard.util.XCardUtils;

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
 * Unmarshals XML-encoded vCards into {@link VCard} objects.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCardReader {
	private static final NamespaceContext nsContext;
	static {
		nsContext = new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if (prefix.equals("v")) {
					return VCardVersion.V4_0.getXmlNamespace();
				}
				return null;
			}

			public String getPrefix(String ns) {
				if (ns.equals(VCardVersion.V4_0.getXmlNamespace())) {
					return "v";
				}
				return null;
			}

			public Iterator<String> getPrefixes(String ns) {
				if (ns.equals(VCardVersion.V4_0.getXmlNamespace())) {
					return Arrays.asList("v").iterator();
				}
				return null;
			}
		};
	}

	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private List<String> warnings = new ArrayList<String>();
	private Map<QName, Class<? extends VCardType>> extendedTypeClasses = new HashMap<QName, Class<? extends VCardType>>();
	private Document document;
	private Iterator<Element> vcardElements;

	/**
	 * @param reader the reader to read the vCards from
	 * @throws IOException if there's a problem reading from the input stream
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardReader(Reader reader) throws SAXException, IOException {
		try {
			//parse the XML document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setIgnoringComments(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(new InputSource(reader));

			//get the "<vcard>" elements
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(nsContext);
			vcardElements = XCardUtils.toElementList((NodeList) xpath.evaluate("/v:vcards/v:vcard", document, XPathConstants.NODESET)).iterator();
		} catch (XPathExpressionException e) {
			//never thrown, xpath expression is hard coded
		} catch (ParserConfigurationException e) {
			//never thrown because we're not doing anything fancy with the configuration
		}
	}

	/**
	 * Gets the compatibility mode.
	 * @return the compatibility mode
	 */
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Used for customizing the unmarshalling process based on the mail client
	 * that generated the vCard.
	 * @param compatibilityMode the compatibility mode
	 */
	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	/**
	 * Registers a extended type class. These types will be unmarshalled into
	 * instances of this class.
	 * @param clazz the extended type class. It MUST contain a public, no-arg
	 * constructor.
	 */
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		try {
			VCardType t = clazz.newInstance();
			String ns = t.getXmlNamespace();
			if (ns == null) {
				ns = VCardVersion.V4_0.getXmlNamespace();
			}
			String localName = t.getTypeName().toLowerCase();

			QName qname = new QName(ns, localName);
			extendedTypeClasses.put(qname, clazz);
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	/**
	 * Reads the next vCard.
	 * @return the next vCard or null if there are no more
	 * @throws VCardException if there's a problem parsing the vCard
	 */
	public VCard readNext() throws VCardException {
		warnings.clear();

		if (!vcardElements.hasNext()) {
			return null;
		}

		VCard vcard = new VCard();

		//only 4.0 supports xCards
		//TODO look at namespace for the version?
		VCardVersion version = VCardVersion.V4_0;
		vcard.setVersion(version);

		Element vcardElement = vcardElements.next();

		String ns = version.getXmlNamespace();
		List<Element> children = XCardUtils.toElementList(vcardElement.getChildNodes());
		for (Element child : children) {
			if ("group".equals(child.getLocalName()) && ns.equals(child.getNamespaceURI())) {
				String group = child.getAttribute("name");
				if (group.length() == 0) {
					group = null;
				}
				List<Element> propElements = XCardUtils.toElementList(child.getChildNodes());
				for (Element propElement : propElements) {
					parseAndAddElement(propElement, group, version, vcard);
				}
			} else {
				parseAndAddElement(child, null, version, vcard);
			}
		}

		return vcard;
	}

	/**
	 * Parses a property element from the XML document and adds the property to
	 * the vCard.
	 * @param element the element to parse
	 * @param group the group or null if the property does not belong to a group
	 * @param version the vCard version
	 * @param vcard the vCard object
	 * @throws VCardException
	 */
	private void parseAndAddElement(Element element, String group, VCardVersion version, VCard vcard) throws VCardException {
		VCardSubTypes subTypes = parseSubTypes(element);
		VCardType t = createTypeObject(element.getLocalName(), element.getNamespaceURI());
		if (t != null) {
			try {
				t.unmarshalValue(subTypes, element, version, warnings, compatibilityMode);
			} catch (UnsupportedOperationException e) {
				//type class does not support xCard

				warnings.add("Type class \"" + t.getClass().getName() + "\" does not support xCard.  It will be parsed as a XML property.");
				t = new XmlType();
				t.unmarshalValue(subTypes, element, version, warnings, compatibilityMode);
			}
			t.setGroup(group);
			addToVCard(t, vcard);
		}
	}

	/**
	 * Parses the property parameters (aka "sub types").
	 * @param element the property's XML element
	 * @return the parsed parameters
	 */
	private VCardSubTypes parseSubTypes(Element element) {
		VCardSubTypes subTypes = new VCardSubTypes();

		List<Element> parametersElements = XCardUtils.toElementList(element.getElementsByTagName("parameters"));
		for (Element parametersElement : parametersElements) { // foreach "<parameters>" element (there should only be 1 though)
			List<Element> paramElements = XCardUtils.toElementList(parametersElement.getChildNodes());
			for (Element paramElement : paramElements) {
				String name = paramElement.getLocalName().toUpperCase();
				List<Element> valueElements = XCardUtils.toElementList(paramElement.getChildNodes());
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
	 * @return the type that was created or null if a type object wasn't created
	 */
	private VCardType createTypeObject(String name, String ns) {
		name = name.toUpperCase();

		Class<? extends VCardType> clazz = TypeList.nameToTypeClass.get(name);
		if (clazz != null && VCardVersion.V4_0.getXmlNamespace().equals(ns)) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				//it is the responsibility of the EZ-vCard developer to ensure that this exception is never thrown
				//all type classes defined in the EZ-vCard library MUST have public, no-arg constructors
				return null;
			}
		} else {
			Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(new QName(ns, name.toLowerCase()));
			if (extendedTypeClass != null) {
				try {
					return extendedTypeClass.newInstance();
				} catch (Exception e) {
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
	 * Adds a type to the vCard.
	 * @param t the type object
	 * @param vcard the vCard
	 */
	private void addToVCard(VCardType t, VCard vcard) {
		Method method = TypeList.typeClassToAddMethod.get(t.getClass());
		if (method != null) {
			try {
				method.invoke(vcard, t);
			} catch (Exception e) {
				//this should NEVER be thrown because the method MUST be public
				throw new RuntimeException(e);
			}
		} else {
			vcard.addExtendedType(t);
		}
	}
}
