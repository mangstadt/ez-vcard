package ezvcard.io.xml;

import static ezvcard.io.xml.XCardQNames.GROUP;
import static ezvcard.io.xml.XCardQNames.PARAMETERS;
import static ezvcard.io.xml.XCardQNames.VCARD;
import static ezvcard.io.xml.XCardQNames.VCARDS;
import static ezvcard.util.IOUtils.utf8Writer;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamReader;
import ezvcard.io.StreamWriter;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
import ezvcard.util.IOUtils;
import ezvcard.util.ListMultimap;
import ezvcard.util.XmlUtils;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
* List&lt;VCard&gt; vcards = xcard.getVCards();
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
	private final VCardVersion version4 = VCardVersion.V4_0; //xCard only supports 4.0
	private final Document document;
	private Element root;

	/**
	 * Creates an empty xCard document.
	 */
	public XCardDocument() {
		this(createXCardsRoot());
	}

	private static Document createXCardsRoot() {
		Document document = XmlUtils.createDocument();
		Element root = document.createElementNS(VCARDS.getNamespaceURI(), VCARDS.getLocalPart());
		document.appendChild(root);
		return document;
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
		this(readFile(file));
	}

	private static Document readFile(File file) throws SAXException, IOException {
		InputStream in = new FileInputStream(file);
		try {
			return XmlUtils.toDocument(in);
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
	 * {@link #XCardDocument(InputStream)} constructor is preferred, since it
	 * takes the XML document's character encoding into account when parsing.
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
		this.document = document;

		XCardNamespaceContext nsContext = new XCardNamespaceContext(version4, "v");
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(nsContext);

		try {
			//find the <vcards> element
			root = (Element) xpath.evaluate("//" + nsContext.getPrefix() + ":" + VCARDS.getLocalPart(), document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			//should never thrown because the xpath expression is hard coded
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a {@link StreamReader} object that reads vCards from this XML
	 * document.
	 * @return the reader
	 */
	public StreamReader reader() {
		return new XCardDocumentStreamReader();
	}

	/**
	 * Creates a {@link StreamWriter} object that adds vCards to this XML
	 * document.
	 * @return the writer
	 */
	public XCardDocumentStreamWriter writer() {
		return new XCardDocumentStreamWriter();
	}

	/**
	 * Gets the wrapped XML document.
	 * @return the XML document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Parses all of the vCards from this XML document. Modifications made to
	 * these {@link VCard} objects will NOT be applied to the XML document.
	 * @return the parsed vCards
	 */
	public List<VCard> getVCards() {
		try {
			return reader().readAll();
		} catch (IOException e) {
			//not thrown because we're reading from a DOM
			throw new RuntimeException(e);
		}
	}

	/**
	 * Adds a vCard to the XML document.
	 * @param vcard the vCard to add
	 * @throws IllegalArgumentException if no scribe has been registered for the
	 * property (only applies to custom property classes)
	 */
	public void add(VCard vcard) {
		writer().write(vcard);
	}

	/**
	 * Writes the XML document to a string.
	 * @return the XML string
	 */
	public String write() {
		return write(-1);
	}

	/**
	 * Writes the XML document to a string.
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @return the XML string
	 */
	public String write(int indent) {
		return write(indent, null);
	}

	/**
	 * Writes the XML document to a string.
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @return the XML string
	 */
	public String write(int indent, String xmlVersion) {
		return write(createOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the XML document to a string.
	 * @param outputProperties properties to assign to the JAXP
	 * transformer (see {@link Transformer#setOutputProperty})
	 * @return the XML string
	 */
	public String write(Map<String, String> outputProperties) {
		StringWriter sw = new StringWriter();
		try {
			write(sw, outputProperties);
		} catch (TransformerException e) {
			//should not be thrown because we're writing to a string
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

	/**
	 * Writes the XML document to an output stream.
	 * @param out the output stream (UTF-8 encoding will be used)
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out) throws TransformerException {
		write(out, -1);
	}

	/**
	 * Writes the XML document to an output stream.
	 * @param out the output stream (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, int indent) throws TransformerException {
		write(out, indent, null);
	}

	/**
	 * Writes the XML document to an output stream.
	 * @param out the output stream (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, int indent, String xmlVersion) throws TransformerException {
		write(out, createOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the XML document to an output stream.
	 * @param out the output stream (UTF-8 encoding will be used)
	 * @param outputProperties properties to assign to the JAXP
	 * transformer (see {@link Transformer#setOutputProperty})
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, Map<String, String> outputProperties) throws TransformerException {
		write(utf8Writer(out), outputProperties);
	}

	/**
	 * Writes the XML document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file) throws TransformerException, IOException {
		write(file, -1);
	}

	/**
	 * Writes the XML document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file, int indent) throws TransformerException, IOException {
		write(file, indent, null);
	}

	/**
	 * Writes the XML document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file, int indent, String xmlVersion) throws TransformerException, IOException {
		write(file, createOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the XML document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param outputProperties properties to assign to the JAXP
	 * transformer (see {@link Transformer#setOutputProperty})
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file, Map<String, String> outputProperties) throws TransformerException, IOException {
		Writer writer = utf8Writer(file);
		try {
			write(writer, outputProperties);
		} finally {
			writer.close();
		}
	}

	/**
	 * Writes the XML document to a writer.
	 * @param writer the writer
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer) throws TransformerException {
		write(writer, -1);
	}

	/**
	 * Writes the XML document to a writer.
	 * @param writer the writer
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, int indent) throws TransformerException {
		write(writer, indent, null);
	}

	/**
	 * Writes the XML document to a writer.
	 * @param writer the writer
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "-1" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, int indent, String xmlVersion) throws TransformerException {
		write(writer, createOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the XML document to a writer.
	 * @param writer the writer
	 * @param outputProperties properties to assign to the JAXP
	 * transformer (see {@link Transformer#setOutputProperty})
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, Map<String, String> outputProperties) throws TransformerException {
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			//should never be thrown because we're not doing anything fancy with the configuration
			throw new RuntimeException(e);
		} catch (TransformerFactoryConfigurationError e) {
			//should never be thrown because we're not doing anything fancy with the configuration
			throw new RuntimeException(e);
		}

		/*
		 * Using Transformer#setOutputProperties(Properties) doesn't work for
		 * some reason for setting the number of indentation spaces.
		 */
		for (Map.Entry<String, String> entry : outputProperties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			transformer.setOutputProperty(key, value);
		}

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
	}

	private Map<String, String> createOutputProperties(int indent, String xmlVersion) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(OutputKeys.METHOD, "xml");

		if (indent >= 0) {
			properties.put(OutputKeys.INDENT, "yes");
			properties.put("{http://xml.apache.org/xslt}indent-amount", indent + "");
		}

		if (xmlVersion != null) {
			properties.put(OutputKeys.VERSION, xmlVersion);
		}

		return properties;
	}

	private class XCardDocumentStreamReader extends StreamReader {
		private final Iterator<Element> vcardElements;
		{
			List<Element> list = (root == null) ? Collections.<Element> emptyList() : getChildElements(root, VCARD);
			vcardElements = list.iterator();
		}

		private VCard vcard;

		@Override
		public VCard readNext() {
			try {
				return super.readNext();
			} catch (IOException e) {
				//will not be thrown
				throw new RuntimeException(e);
			}
		}

		@Override
		protected VCard _readNext() throws IOException {
			if (!vcardElements.hasNext()) {
				return null;
			}

			vcard = new VCard();
			vcard.setVersion(version4);
			parseVCardElement(vcardElements.next());
			return vcard;
		}

		public void close() {
			//empty
		}

		private void parseVCardElement(Element vcardElement) {
			List<Element> children = XmlUtils.toElementList(vcardElement.getChildNodes());
			for (Element child : children) {
				if (XmlUtils.hasQName(child, GROUP)) {
					String group = child.getAttribute("name");
					if (group.length() == 0) {
						group = null;
					}
					List<Element> grandChildren = XmlUtils.toElementList(child.getChildNodes());
					for (Element grandChild : grandChildren) {
						parseAndAddElement(grandChild, group);
					}
					continue;
				}

				parseAndAddElement(child, null);
			}
		}

		/**
		 * Parses a property element from the XML document and adds the property
		 * to the vCard.
		 * @param element the element to parse
		 * @param group the group name or null if the property does not belong
		 * to a group
		 * @param vcard the vCard object
		 * @param warningsBuf the list to add the warnings to
		 */
		private void parseAndAddElement(Element element, String group) {
			VCardParameters parameters = parseParameters(element);

			VCardProperty property;
			String propertyName = element.getLocalName();
			String ns = element.getNamespaceURI();
			QName qname = new QName(ns, propertyName);
			VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(qname);
			try {
				Result<? extends VCardProperty> result = scribe.parseXml(element, parameters);

				property = result.getProperty();
				property.setGroup(group);

				for (String warning : result.getWarnings()) {
					warnings.add(null, propertyName, warning);
				}
			} catch (SkipMeException e) {
				warnings.add(null, propertyName, 22, e.getMessage());
				return;
			} catch (CannotParseException e) {
				String xml = XmlUtils.toString(element);
				warnings.add(null, propertyName, 33, xml, e.getMessage());

				scribe = index.getPropertyScribe(Xml.class);
				Result<? extends VCardProperty> result = scribe.parseXml(element, parameters);
				property = result.getProperty();
				property.setGroup(group);
			} catch (EmbeddedVCardException e) {
				warnings.add(null, propertyName, 34);
				return;
			}

			vcard.addProperty(property);
		}

		/**
		 * Parses the property parameters.
		 * @param element the property's XML element
		 * @return the parsed parameters
		 */
		private VCardParameters parseParameters(Element element) {
			VCardParameters parameters = new VCardParameters();

			List<Element> roots = XmlUtils.toElementList(element.getElementsByTagNameNS(PARAMETERS.getNamespaceURI(), PARAMETERS.getLocalPart()));
			for (Element root : roots) { // foreach "<parameters>" element (there should only be 1 though)
				List<Element> parameterElements = XmlUtils.toElementList(root.getChildNodes());
				for (Element parameterElement : parameterElements) {
					String name = parameterElement.getLocalName().toUpperCase();
					List<Element> valueElements = XmlUtils.toElementList(parameterElement.getChildNodes());
					for (Element valueElement : valueElements) {
						String value = valueElement.getTextContent();
						parameters.put(name, value);
					}
				}
			}

			return parameters;
		}

		private List<Element> getChildElements(Element parent, QName qname) {
			List<Element> elements = new ArrayList<Element>();
			for (Element child : XmlUtils.toElementList(parent.getChildNodes())) {
				if (XmlUtils.hasQName(child, qname)) {
					elements.add(child);
				}
			}
			return elements;
		}
	}

	public class XCardDocumentStreamWriter extends StreamWriter {
		/**
		 * Defines the names of the XML elements that are used to hold each
		 * parameter's value.
		 */
		private final Map<String, VCardDataType> parameterDataTypes = new HashMap<String, VCardDataType>();
		{
			registerParameterDataType(VCardParameters.ALTID, VCardDataType.TEXT);
			registerParameterDataType(VCardParameters.CALSCALE, VCardDataType.TEXT);
			registerParameterDataType(VCardParameters.GEO, VCardDataType.URI);
			registerParameterDataType(VCardParameters.LABEL, VCardDataType.TEXT);
			registerParameterDataType(VCardParameters.LANGUAGE, VCardDataType.LANGUAGE_TAG);
			registerParameterDataType(VCardParameters.MEDIATYPE, VCardDataType.TEXT);
			registerParameterDataType(VCardParameters.PID, VCardDataType.TEXT);
			registerParameterDataType(VCardParameters.PREF, VCardDataType.INTEGER);
			registerParameterDataType(VCardParameters.SORT_AS, VCardDataType.TEXT);
			registerParameterDataType(VCardParameters.TYPE, VCardDataType.TEXT);
			registerParameterDataType(VCardParameters.TZ, VCardDataType.URI);
		}

		@Override
		public void write(VCard vcard) {
			try {
				super.write(vcard);
			} catch (IOException e) {
				//won't be thrown because we're writing to a DOM
			}
		}

		@Override
		protected void _write(VCard vcard, List<VCardProperty> properties) throws IOException {
			ListMultimap<String, VCardProperty> propertiesByGroup = new ListMultimap<String, VCardProperty>(); //group the types by group name (null = no group name)
			for (VCardProperty property : properties) {
				propertiesByGroup.put(property.getGroup(), property);
			}

			//marshal each type object
			Element vcardElement = createElement(VCARD);
			for (Map.Entry<String, List<VCardProperty>> entry : propertiesByGroup) {
				String groupName = entry.getKey();
				Element parent;
				if (groupName != null) {
					Element groupElement = createElement(GROUP);
					groupElement.setAttribute("name", groupName);
					vcardElement.appendChild(groupElement);
					parent = groupElement;
				} else {
					parent = vcardElement;
				}

				for (VCardProperty property : entry.getValue()) {
					try {
						Element propertyElement = marshalProperty(property, vcard);
						parent.appendChild(propertyElement);
					} catch (SkipMeException e) {
						//skip property
					} catch (EmbeddedVCardException e) {
						//skip property
					}
				}
			}

			if (root == null) {
				root = createElement(VCARDS);
				Element documentRoot = XmlUtils.getRootElement(document);
				if (documentRoot == null) {
					document.appendChild(root);
				} else {
					documentRoot.appendChild(root);
				}
			}
			root.appendChild(vcardElement);
		}

		@Override
		protected VCardVersion getTargetVersion() {
			return VCardVersion.V4_0;
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

		public void close() {
			//empty
		}

		/**
		 * Marshals a type object to an XML element.
		 * @param property the property to marshal
		 * @param vcard the vcard the type belongs to
		 * @return the XML element
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Element marshalProperty(VCardProperty property, VCard vcard) {
			VCardPropertyScribe scribe = index.getPropertyScribe(property);

			Element propertyElement;
			if (property instanceof Xml) {
				Xml xml = (Xml) property;
				Document propertyDocument = xml.getValue();
				if (propertyDocument == null) {
					throw new SkipMeException();
				}
				propertyElement = XmlUtils.getRootElement(propertyDocument);
				propertyElement = (Element) document.importNode(propertyElement, true);
			} else {
				QName qname = scribe.getQName();
				propertyElement = createElement(qname);
				scribe.writeXml(property, propertyElement);
			}

			//marshal the parameters
			VCardParameters parameters = scribe.prepareParameters(property, version4, vcard);
			if (!parameters.isEmpty()) {
				Element parametersElement = marshalParameters(parameters);
				Node firstChild = propertyElement.getFirstChild();
				propertyElement.insertBefore(parametersElement, firstChild);
			}

			return propertyElement;
		}

		private Element marshalParameters(VCardParameters parameters) {
			Element parametersElement = createElement(PARAMETERS);

			for (Map.Entry<String, List<String>> parameter : parameters) {
				String parameterName = parameter.getKey().toLowerCase();
				Element parameterElement = createElement(parameterName);

				for (String parameterValue : parameter.getValue()) {
					VCardDataType dataType = parameterDataTypes.get(parameterName);
					String dataTypeElementName = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
					Element dataTypeElement = createElement(dataTypeElementName);
					dataTypeElement.setTextContent(parameterValue);
					parameterElement.appendChild(dataTypeElement);
				}

				parametersElement.appendChild(parameterElement);
			}

			return parametersElement;
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

		/**
		 * Creates a new XML element.
		 * @param qname the element name
		 * @return the new XML element
		 */
		private Element createElement(QName qname) {
			return createElement(qname.getLocalPart(), qname.getNamespaceURI());
		}
	}
}
