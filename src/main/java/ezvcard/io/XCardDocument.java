package ezvcard.io;

import static ezvcard.util.VCardStringUtils.NEWLINE;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.ProdIdType;
import ezvcard.types.RawType;
import ezvcard.types.TypeList;
import ezvcard.types.VCardType;
import ezvcard.types.XmlType;
import ezvcard.util.IOUtils;
import ezvcard.util.ListMultimap;
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

//@formatter:off
/**
* <p>
* Represents an XML document that contains vCard objects ("xCard" standard).
* This class can be used to read and write xCard documents.
* </p>
* <p>
* <b>Examples:</b>
* 
* <pre>
* String xml =
* "&lt;vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\"&gt;" +
*   "&lt;vcard&gt;" +
*     "&lt;fn&gt;" +
*       "&lt;text&gt;John Doe&lt;/text&gt;" +
*     "&lt;/fn&gt;" +
*     "&lt;n&gt;" +
*       "&lt;surname&gt;Doe&lt;/surname&gt;" +
*        "&lt;given&gt;Johnathan&lt;/given&gt;" +
*        "&lt;additional&gt;Jonny&lt;/additional&gt;" +
*        "&lt;additional&gt;John&lt;/additional&gt;" +
*        "&lt;prefix&gt;Mr.&lt;/prefix&gt;" +
*        "&lt;suffix /&gt;" +
*      "&lt;/n&gt;" +
*    "&lt;/vcard&gt;" +
* "&lt;/vcards&gt;";
*     
* //parsing an existing xCard document
* XCardDocument xcard = new XCardDocument(xml);
* List&lt;VCard&gt; vcards = xcard.parseAll();
* 
* //creating an empty xCard document
* XCardDocument xcard = new XCardDocument();
* 
* //VCard objects can be added at any time
* VCard vcard = ...
* xcard.add(vcard);
* 
* //retrieving the raw XML DOM
* Document document = xcard.getDocument();
* 
* //call one of the "write()" methods to output the xCard document
* File file = new File("johndoe.xml");
* xcard.write(file);
* </pre>
* 
* </p>
* @author Michael Angstadt
* @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
*/
//@formatter:on
public class XCardDocument {
	private static final VCardVersion version4 = VCardVersion.V4_0; //xCard only supports 4.0
	private static final XCardNamespaceContext nsContext = new XCardNamespaceContext(version4, "v");

	/**
	 * Defines the names of the XML elements that are used to hold each
	 * parameter's value.
	 */
	private final Map<String, VCardDataType> parameterDataTypes = new HashMap<String, VCardDataType>();
	{
		registerParameterDataType(VCardSubTypes.ALTID, VCardDataType.TEXT);
		registerParameterDataType(VCardSubTypes.CALSCALE, VCardDataType.TEXT);
		registerParameterDataType(VCardSubTypes.GEO, VCardDataType.URI);
		registerParameterDataType(VCardSubTypes.LABEL, VCardDataType.TEXT);
		registerParameterDataType(VCardSubTypes.LANGUAGE, VCardDataType.LANGUAGE_TAG);
		registerParameterDataType(VCardSubTypes.MEDIATYPE, VCardDataType.TEXT);
		registerParameterDataType(VCardSubTypes.PID, VCardDataType.TEXT);
		registerParameterDataType(VCardSubTypes.PREF, VCardDataType.INTEGER);
		registerParameterDataType(VCardSubTypes.SORT_AS, VCardDataType.TEXT);
		registerParameterDataType(VCardSubTypes.TYPE, VCardDataType.TEXT);
		registerParameterDataType(VCardSubTypes.TZ, VCardDataType.URI);
	}

	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private boolean addProdId = true;
	private boolean versionStrict = true;
	private final List<List<String>> parseWarnings = new ArrayList<List<String>>();
	private final Map<QName, Class<? extends VCardType>> extendedTypeClasses = new HashMap<QName, Class<? extends VCardType>>();
	private Document document;
	private Element root;

	/**
	 * Creates an xCard document.
	 */
	public XCardDocument() {
		document = XmlUtils.createDocument();
		root = createElement("vcards");
		document.appendChild(root);
	}

	/**
	 * Parses an xCard document from a string.
	 * @param xml the XML string to read the vCards from
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardDocument(String xml) throws SAXException {
		try {
			init(new StringReader(xml));
		} catch (IOException e) {
			//reading from string
		}
	}

	/**
	 * Parses an xCard document from an input stream.
	 * @param in the input stream to read the vCards from
	 * @throws IOException if there's a problem reading from the input stream
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardDocument(InputStream in) throws SAXException, IOException {
		this(new InputStreamReader(in));
	}

	/**
	 * Parses an xCard document from a file.
	 * @param file the file to read the vCards from
	 * @throws IOException if there's a problem reading from the file
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardDocument(File file) throws SAXException, IOException {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			init(reader);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	/**
	 * Parses an xCard document from a reader.
	 * @param reader the reader to read the vCards from
	 * @throws IOException if there's a problem reading from the reader
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardDocument(Reader reader) throws SAXException, IOException {
		init(reader);
	}

	/**
	 * Wraps an existing XML DOM object.
	 * @param document the XML DOM that contains the xCard document
	 */
	public XCardDocument(Document document) {
		init(document);
	}

	private void init(Reader reader) throws SAXException, IOException {
		init(XmlUtils.toDocument(reader));
	}

	private void init(Document document) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(nsContext);

		try {
			//find the <vcards> element
			String prefix = nsContext.getPrefix();
			root = (Element) xpath.evaluate("//" + prefix + ":vcards", document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			//never thrown, xpath expression is hard coded
		}
	}

	/**
	 * Gets the compatibility mode. Used for customizing the marshalling process
	 * to target a particular application.
	 * @return the compatibility mode
	 */
	@Deprecated
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Sets the compatibility mode. Used for customizing the marshalling process
	 * to target a particular application.
	 * @param compatibilityMode the compatibility mode
	 */
	@Deprecated
	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	/**
	 * Gets whether or not a "PRODID" type will be added to each vCard, saying
	 * that the vCard was generated by this library.
	 * @return true if it will be added, false if not (defaults to true)
	 */
	public boolean isAddProdId() {
		return addProdId;
	}

	/**
	 * Sets whether or not to add a "PRODID" type to each vCard, saying that the
	 * vCard was generated by this library.
	 * @param addProdId true to add this type, false not to (defaults to true)
	 */
	public void setAddProdId(boolean addProdId) {
		this.addProdId = addProdId;
	}

	/**
	 * Gets whether properties that do not support xCard (vCard version 4.0)
	 * will be excluded from the written vCard.
	 * @return true to exclude properties that do not support xCard, false to
	 * include them anyway (defaults to true)
	 */
	public boolean isVersionStrict() {
		return versionStrict;
	}

	/**
	 * Sets whether properties that do not support xCard (vCard version 4.0)
	 * will be excluded from the written vCard.
	 * @param versionStrict true to exclude properties that do not support
	 * xCard, false to include them anyway (defaults to true)
	 */
	public void setVersionStrict(boolean versionStrict) {
		this.versionStrict = versionStrict;
	}

	/**
	 * Gets the warnings from the last parse operation.
	 * @return the warnings (it is a "list of lists"--each parsed {@link VCard}
	 * object has its own warnings list)
	 * @see #parseAll
	 * @see #parseFirst
	 */
	public List<List<String>> getParseWarnings() {
		return parseWarnings;
	}

	/**
	 * Registers the data type of an experimental parameter. Experimental
	 * parameters use the "unknown" data type by default.
	 * @param parameterName the parameter name (e.g. "x-foo")
	 * @param dataType the data type or null to remove
	 */
	public void registerParameterDataType(String parameterName, VCardDataType dataType) {
		parameterName = parameterName.toLowerCase();
		if (dataType == null) {
			parameterDataTypes.remove(parameterName);
		} else {
			parameterDataTypes.put(parameterName, dataType);
		}
	}

	/**
	 * Registers an extended type class.
	 * @param clazz the extended type class to register (MUST have a public,
	 * no-arg constructor)
	 * @throws RuntimeException if the class doesn't have a public, no-arg
	 * constructor
	 */
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.put(getQNameFromTypeClass(clazz), clazz);
	}

	/**
	 * Removes an extended type class that was previously registered.
	 * @param clazz the extended type class to remove
	 */
	public void unregisterExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.remove(getQNameFromTypeClass(clazz));
	}

	/**
	 * Gets the XML document that was generated.
	 * @return the XML document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Parses all the {@link VCard} objects from the xCard document.
	 * @return the vCard objects
	 */
	public List<VCard> parseAll() {
		parseWarnings.clear();

		if (root == null) {
			return Collections.emptyList();
		}

		List<VCard> vcards = new ArrayList<VCard>();
		for (Element vcardElement : getVCardElements()) {
			List<String> warnings = new ArrayList<String>();
			parseWarnings.add(warnings);
			VCard vcard = parseVCardElement(vcardElement, warnings);
			vcards.add(vcard);
		}

		return vcards;
	}

	/**
	 * Parses the first the {@link VCard} object from the xCard document.
	 * @return the vCard object
	 */
	public VCard parseFirst() {
		parseWarnings.clear();

		if (root == null) {
			return null;
		}

		List<Element> vcardElements = getVCardElements();
		if (vcardElements.isEmpty()) {
			return null;
		}

		List<String> warnings = new ArrayList<String>();
		parseWarnings.add(warnings);
		return parseVCardElement(vcardElements.get(0), warnings);
	}

	private VCard parseVCardElement(Element vcardElement, List<String> warnings) {
		VCard vcard = new VCard();
		vcard.setVersion(version4);

		String ns = version4.getXmlNamespace();
		List<Element> children = XmlUtils.toElementList(vcardElement.getChildNodes());
		for (Element child : children) {
			if ("group".equals(child.getLocalName()) && ns.equals(child.getNamespaceURI())) {
				String group = child.getAttribute("name");
				if (group.length() == 0) {
					group = null;
				}
				List<Element> propElements = XmlUtils.toElementList(child.getChildNodes());
				for (Element propElement : propElements) {
					parseAndAddElement(propElement, group, vcard, warnings);
				}
			} else {
				parseAndAddElement(child, null, vcard, warnings);
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
	 * @param vcard the vCard object
	 * @param warningsBuf the list to add the warnings to
	 */
	private void parseAndAddElement(Element element, String group, VCard vcard, List<String> warnings) {
		List<String> warningsBuf = new ArrayList<String>();

		VCardSubTypes subTypes = parseSubTypes(element);
		String propertyName = element.getLocalName();
		VCardType type = createTypeObject(propertyName, element.getNamespaceURI());
		type.setGroup(group);
		try {
			type.unmarshalXml(subTypes, element, version4, warningsBuf, compatibilityMode);
		} catch (SkipMeException e) {
			warningsBuf.add("Property has requested that it be skipped: " + e.getMessage());
			return;
		} catch (CannotParseException e) {
			String xml = XmlUtils.toString(element);
			warningsBuf.add("Property value could not be parsed.  It will be unmarshalled as a " + XmlType.NAME + " property instead." + NEWLINE + "  XML: " + xml + NEWLINE + "  Reason: " + e.getMessage());
			type = new XmlType();
			type.setGroup(group);
			type.unmarshalXml(subTypes, element, version4, warningsBuf, compatibilityMode);
		} catch (UnsupportedOperationException e) {
			warningsBuf.add("Property class \"" + type.getClass().getName() + "\" does not support xCard unmarshalling.  It will be unmarshalled as a " + XmlType.NAME + " property instead.");
			type = new XmlType();
			type.setGroup(group);
			type.unmarshalXml(subTypes, element, version4, warningsBuf, compatibilityMode);
		} catch (EmbeddedVCardException e) {
			warningsBuf.add("Property will not be unmarshalled because xCard does not supported embedded vCards.");
			return;
		} finally {
			for (String warning : warningsBuf) {
				addWarning(warning, type.getTypeName(), warnings);
			}
		}

		vcard.addType(type);
	}

	/**
	 * Parses the property parameters (aka "sub types").
	 * @param element the property's XML element
	 * @return the parsed parameters
	 */
	private VCardSubTypes parseSubTypes(Element element) {
		VCardSubTypes subTypes = new VCardSubTypes();

		List<Element> parametersElements = XmlUtils.toElementList(element.getElementsByTagNameNS(version4.getXmlNamespace(), "parameters"));
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
		//parse as a standard property
		Class<? extends VCardType> clazz = TypeList.getTypeClass(name);
		if (clazz != null && VCardVersion.V4_0.getXmlNamespace().equals(ns)) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				//should never be thrown
				//all type classes must have public, no-arg constructors
				throw new RuntimeException(e);
			}
		}

		//parse as a registered extended type class
		Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(new QName(ns, name.toLowerCase()));
		if (extendedTypeClass != null) {
			try {
				return extendedTypeClass.newInstance();
			} catch (Exception e) {
				//should never be thrown
				//the type class is checked to see if it has a public, no-arg constructor in the "registerExtendedType" method
				throw new RuntimeException("Extended type class \"" + extendedTypeClass.getName() + "\" MUST have a public, no-arg constructor.");
			}
		}

		//parse as a RawType
		if (name.toUpperCase().startsWith("X-")) {
			return new RawType(name);
		}

		//parse as an XML property
		return new XmlType();
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
				qname = new QName(version4.getXmlNamespace(), type.getTypeName().toLowerCase());
			}
			return qname;
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes the XML document to a string without pretty-printing it.
	 * @return the XML string
	 */
	public String write() {
		return write(-1);
	}

	/**
	 * Writes the XML document to a string and pretty-prints it.
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @return the XML string
	 */
	public String write(int indent) {
		StringWriter sw = new StringWriter();
		try {
			write(sw, indent);
		} catch (TransformerException e) {
			//writing to string
		}
		return sw.toString();
	}

	/**
	 * Writes the XML document to an output stream without pretty-printing it.
	 * @param out the output stream
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out) throws TransformerException {
		write(out, -1);
	}

	/**
	 * Writes the XML document to an output stream and pretty-prints it.
	 * @param out the output stream
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, int indent) throws TransformerException {
		write(new OutputStreamWriter(out), indent);
	}

	/**
	 * Writes the XML document to a file without pretty-printing it.
	 * @param file the file
	 * @throws TransformerException if there's a problem writing to the file
	 */
	public void write(File file) throws TransformerException, IOException {
		write(file, -1);
	}

	/**
	 * Writes the XML document to a file and pretty-prints it.
	 * @param file the file stream
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @throws TransformerException if there's a problem writing to the file
	 */
	public void write(File file, int indent) throws TransformerException, IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			write(writer, indent);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	/**
	 * Writes the XML document to a writer without pretty-printing it.
	 * @param writer the writer
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer) throws TransformerException {
		write(writer, -1);
	}

	/**
	 * Writes the XML document to a writer and pretty-prints it.
	 * @param writer the writer
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, int indent) throws TransformerException {
		Map<String, String> properties = new HashMap<String, String>();
		if (indent >= 0) {
			properties.put(OutputKeys.INDENT, "yes");
			properties.put("{http://xml.apache.org/xslt}indent-amount", indent + "");
		}
		XmlUtils.toWriter(document, writer, properties);
	}

	/**
	 * Adds a vCard to the XML document.
	 * @param vcard the vCard to add
	 */
	public void add(VCard vcard) {
		ListMultimap<String, VCardType> typesToAdd = new ListMultimap<String, VCardType>(); //group the types by group name (null = no group name)

		for (VCardType type : vcard) {
			if (addProdId && type instanceof ProdIdType) {
				//do not add the PRODID in the vCard if "addProdId" is true
				continue;
			}

			if (versionStrict && !type.isSupported(version4)) {
				//do not add the property to the vCard if it is not supported by the target version
				continue;
			}

			typesToAdd.put(type.getGroup(), type);
		}

		//add an extended type saying it was generated by this library
		if (addProdId) {
			EzvcardProdIdType prodId = new EzvcardProdIdType(version4);
			typesToAdd.put(prodId.getGroup(), prodId);
		}

		//marshal each type object
		Element vcardElement = createElement("vcard");
		for (String groupName : typesToAdd.keySet()) {
			Element parent;
			if (groupName != null) {
				Element groupElement = createElement("group");
				groupElement.setAttribute("name", groupName);
				vcardElement.appendChild(groupElement);
				parent = groupElement;
			} else {
				parent = vcardElement;
			}

			for (VCardType type : typesToAdd.get(groupName)) {
				try {
					Element typeElement = marshalType(type, vcard);
					parent.appendChild(typeElement);
				} catch (SkipMeException e) {
					//skip property
				} catch (EmbeddedVCardException e) {
					//skip property
				}
			}
		}
		root.appendChild(vcardElement);
	}

	/**
	 * Marshals a type object to an XML element.
	 * @param type the type object to marshal
	 * @param vcard the vcard the type belongs to
	 * @return the XML element or null not to add anything to the final XML
	 * document
	 */
	private Element marshalType(VCardType type, VCard vcard) {
		QName qname = type.getQName();
		String ns, localPart;
		if (qname == null) {
			localPart = type.getTypeName().toLowerCase();
			ns = version4.getXmlNamespace();
		} else {
			localPart = qname.getLocalPart();
			ns = qname.getNamespaceURI();
		}
		Element typeElement = createElement(localPart, ns);

		//marshal the sub types
		VCardSubTypes subTypes = type.marshalSubTypes(version4, compatibilityMode, vcard);
		subTypes.setValue(null); //don't include the VALUE parameter (modification of the "VCardSubTypes" object is safe because it's a copy)
		if (!subTypes.isEmpty()) {
			Element parametersElement = createElement("parameters");
			for (Map.Entry<String, List<String>> param : subTypes) {
				String paramName = param.getKey();
				Element parameterElement = createElement(paramName.toLowerCase());
				for (String paramValue : param.getValue()) {
					VCardDataType dataType = parameterDataTypes.get(paramName.toLowerCase());
					String valueElementName = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
					Element parameterValueElement = createElement(valueElementName);
					parameterValueElement.setTextContent(paramValue);
					parameterElement.appendChild(parameterValueElement);
				}
				parametersElement.appendChild(parameterElement);
			}
			typeElement.appendChild(parametersElement);
		}

		//marshal the value
		type.marshalXml(typeElement, version4, compatibilityMode);

		return typeElement;
	}

	private List<Element> getVCardElements() {
		return getChildElements(root, "vcard");
	}

	private List<Element> getChildElements(Element parent, String localName) {
		List<Element> elements = new ArrayList<Element>();
		for (Element child : XmlUtils.toElementList(parent.getChildNodes())) {
			if (localName.equals(child.getLocalName()) && version4.getXmlNamespace().equals(child.getNamespaceURI())) {
				elements.add(child);
			}
		}
		return elements;
	}

	/**
	 * Creates a new XML element.
	 * @param name the name of the XML element
	 * @return the new XML element
	 */
	private Element createElement(String name) {
		return createElement(name, version4.getXmlNamespace());
	}

	/**
	 * Creates a new XML element.
	 * @param name the name of the XML element
	 * @param ns the namespace of the XML element
	 * @return the new XML element
	 */
	private Element createElement(String name, String ns) {
		return document.createElementNS(ns, name);
	}

	private void addWarning(String message, String propertyName, List<String> warnings) {
		warnings.add(propertyName + " property: " + message);
	}
}
