package ezvcard.io.xml;

import static ezvcard.util.IOUtils.utf8Writer;
import static ezvcard.util.VCardStringUtils.NEWLINE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
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

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.property.ProdIdType;
import ezvcard.property.VCardType;
import ezvcard.property.XmlType;
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
* <pre class="brush:java">
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

	private ScribeIndex index = new ScribeIndex();
	private boolean addProdId = true;
	private boolean versionStrict = true;
	private final List<List<String>> parseWarnings = new ArrayList<List<String>>();
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
		this(XmlUtils.toDocument(xml));
	}

	/**
	 * Parses an xCard document from an input stream.
	 * @param in the input stream to read the vCards from
	 * @throws IOException if there's a problem reading from the input stream
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardDocument(InputStream in) throws SAXException, IOException {
		this(XmlUtils.toDocument(in));
	}

	/**
	 * Parses an xCard document from a file.
	 * @param file the file to read the vCards from
	 * @throws IOException if there's a problem reading from the file
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardDocument(File file) throws SAXException, IOException {
		InputStream in = new FileInputStream(file);
		try {
			init(XmlUtils.toDocument(in));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * <p>
	 * Parses an xCard document from a reader.
	 * </p>
	 * <p>
	 * Note that use of this constructor is discouraged. It ignores the
	 * character encoding that is defined within the XML document itself, and
	 * should only be used if the encoding is undefined or if the encoding needs
	 * to be ignored for whatever reason. The
	 * {@link #XCardDocument(InputStream)} constructor should be used instead,
	 * since it takes the XML document's character encoding into account when
	 * parsing.
	 * </p>
	 * @param reader the reader to read the vCards from
	 * @throws IOException if there's a problem reading from the reader
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCardDocument(Reader reader) throws SAXException, IOException {
		this(XmlUtils.toDocument(reader));
	}

	/**
	 * Wraps an existing XML DOM object.
	 * @param document the XML DOM that contains the xCard document
	 */
	public XCardDocument(Document document) {
		init(document);
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
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(VCardPropertyScribe<? extends VCardType> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the scribe index.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
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
		VCardSubTypes parameters = parseSubTypes(element);

		VCardType property;
		String propertyName = element.getLocalName();
		String ns = element.getNamespaceURI();
		VCardPropertyScribe<? extends VCardType> scribe = index.getPropertyScribe(new QName(ns, propertyName));
		try {
			Result<? extends VCardType> result = scribe.parseXml(element, parameters);

			property = result.getProperty();
			property.setGroup(group);

			for (String warning : result.getWarnings()) {
				addWarning(warning, propertyName, warnings);
			}
		} catch (SkipMeException e) {
			addWarning("Property has requested that it be skipped: " + e.getMessage(), propertyName, warnings);
			return;
		} catch (CannotParseException e) {
			String xml = XmlUtils.toString(element);
			addWarning("Property value could not be parsed.  It will be unmarshalled as a " + XmlType.class.getSimpleName() + " property instead." + NEWLINE + "  XML: " + xml + NEWLINE + "  Reason: " + e.getMessage(), propertyName, warnings);

			scribe = index.getPropertyScribe(XmlType.class);
			Result<? extends VCardType> result = scribe.parseXml(element, parameters);
			property = result.getProperty();
			property.setGroup(group);
		} catch (UnsupportedOperationException e) {
			scribe = index.getPropertyScribe(XmlType.class);
			Result<? extends VCardType> result = scribe.parseXml(element, parameters);
			property = result.getProperty();
			property.setGroup(group);

			addWarning("Property class \"" + property.getClass().getName() + "\" does not support xCard unmarshalling.  It will be unmarshalled as a " + XmlType.class.getSimpleName() + " property instead.", propertyName, warnings);
		} catch (EmbeddedVCardException e) {
			addWarning("Property will not be unmarshalled because xCard does not supported embedded vCards.", propertyName, warnings);
			return;
		}

		vcard.addType(property);
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
		}

		return subTypes;
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
		write(utf8Writer(out), indent);
	}

	/**
	 * Writes the XML document to a file without pretty-printing it.
	 * @param file the file
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file) throws TransformerException, IOException {
		write(file, -1);
	}

	/**
	 * Writes the XML document to a file and pretty-prints it.
	 * @param file the file stream
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file, int indent) throws TransformerException, IOException {
		Writer writer = utf8Writer(file);
		try {
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
	 * @throws IllegalArgumentException if a scribe hasn't been registered for a
	 * custom property class (see: {@link #registerScribe})
	 */
	public void add(VCard vcard) {
		ListMultimap<String, VCardType> typesToAdd = new ListMultimap<String, VCardType>(); //group the types by group name (null = no group name)

		for (VCardType type : vcard) {
			if (addProdId && type instanceof ProdIdType) {
				//do not add the PRODID in the vCard if "addProdId" is true
				continue;
			}

			if (versionStrict && !type.getSupportedVersions().contains(version4)) {
				//do not add the property to the vCard if it is not supported by the target version
				continue;
			}

			//check for scribes before writing anything to the stream
			if (index.getPropertyScribe(type) == null) {
				throw new IllegalArgumentException("No scribe found for property class \"" + type.getClass().getName() + "\".");
			}

			typesToAdd.put(type.getGroup(), type);
		}

		//add an extended type saying it was generated by this library
		if (addProdId) {
			ProdIdType prodId = new ProdIdType("ez-vcard " + Ezvcard.VERSION);
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
	 * @return the XML element
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Element marshalType(VCardType type, VCard vcard) {
		VCardPropertyScribe scribe = index.getPropertyScribe(type);
		if (scribe == null) {
			throw new IllegalArgumentException("No marshaller found for property class \"" + type.getClass().getName() + "\".");
		}

		VCardSubTypes parameters = scribe.prepareParameters(type, version4, vcard);

		QName qname = scribe.getQName();
		Element typeElement = createElement(qname.getLocalPart(), qname.getNamespaceURI());

		//marshal the sub types
		if (!parameters.isEmpty()) {
			Element parametersElement = marshalSubTypes(parameters);
			typeElement.appendChild(parametersElement);
		}

		//marshal the value
		scribe.writeXml(type, typeElement);

		return typeElement;
	}

	private Element marshalSubTypes(VCardSubTypes parameters) {
		Element parametersElement = createElement("parameters");

		for (Map.Entry<String, List<String>> param : parameters) {
			String parameterName = param.getKey().toLowerCase();
			Element parameterElement = createElement(parameterName);

			for (String paramValue : param.getValue()) {
				VCardDataType dataType = parameterDataTypes.get(parameterName);
				String dataTypeElementName = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
				Element dataTypeElement = createElement(dataTypeElementName);
				dataTypeElement.setTextContent(paramValue);
				parameterElement.appendChild(dataTypeElement);
			}

			parametersElement.appendChild(parameterElement);
		}

		return parametersElement;
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
