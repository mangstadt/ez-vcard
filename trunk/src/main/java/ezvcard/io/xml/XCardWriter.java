package ezvcard.io.xml;

import static ezvcard.io.xml.XCardQNames.GROUP;
import static ezvcard.io.xml.XCardQNames.PARAMETERS;
import static ezvcard.io.xml.XCardQNames.VCARD;
import static ezvcard.io.xml.XCardQNames.VCARDS;
import static ezvcard.util.IOUtils.utf8Writer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamWriter;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
import ezvcard.util.ListMultimap;
import ezvcard.util.StringUtils;
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

/**
 * <p>
 * Writes xCards (XML-encoded vCards) in a streaming fashion.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * VCard vcard1 = ...
 * VCard vcard2 = ...
 * 
 * File file = new File("vcards.xml");
 * XCardWriter xcardWriter = new XCardWriter(file);
 * xcardWriter.write(vcard1);
 * xcardWriter.write(vcard2);
 * xcardWriter.close();
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCardWriter extends StreamWriter {
	//how to use SAX to write XML: http://stackoverflow.com/questions/4898590/generating-xml-using-sax-and-java
	private final VCardVersion targetVersion = VCardVersion.V4_0;
	private final Document DOC = XmlUtils.createDocument();

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

	private final Writer writer;
	private final TransformerHandler handler;
	private final String indent;
	private final boolean vcardsElementExists;
	private int level = 0;
	private boolean textNodeJustPrinted = false, started = false;

	/**
	 * Creates an xCard writer.
	 * @param out the output stream to write the xCards to
	 */
	public XCardWriter(OutputStream out) {
		this(utf8Writer(out));
	}

	/**
	 * Creates an xCard writer.
	 * @param out the output stream to write the xCards to
	 * @param indent the indentation string to use for pretty printing (e.g.
	 * "\t") or null not to pretty print
	 */
	public XCardWriter(OutputStream out, String indent) {
		this(utf8Writer(out), indent);
	}

	/**
	 * Creates an xCard writer.
	 * @param file the file to write the xCards to
	 * @throws IOException if there's a problem opening the file
	 */
	public XCardWriter(File file) throws IOException {
		this(utf8Writer(file));
	}

	/**
	 * Creates an xCard writer.
	 * @param file the file to write the xCards to
	 * @param indent the indentation string to use for pretty printing (e.g.
	 * "\t") or null not to pretty print
	 * @throws IOException if there's a problem opening the file
	 */
	public XCardWriter(File file, String indent) throws IOException {
		this(utf8Writer(file), indent);
	}

	/**
	 * Creates an xCard writer.
	 * @param writer the writer to write to
	 */
	public XCardWriter(Writer writer) {
		this(writer, null);
	}

	/**
	 * Creates an xCard writer.
	 * @param writer the writer to write to
	 * @param indent the indentation string to use for pretty printing (e.g.
	 * "\t") or null not to pretty print
	 */
	public XCardWriter(Writer writer, String indent) {
		this(writer, indent, null);
	}

	/**
	 * Creates an xCard writer.
	 * @param parent the DOM node to add child elements to
	 */
	public XCardWriter(Node parent) {
		this(null, null, parent);
	}

	private XCardWriter(Writer writer, String indent, Node parent) {
		this.writer = writer;
		this.indent = indent;

		if (parent instanceof Document) {
			Node root = parent.getFirstChild();
			if (root != null) {
				parent = root;
			}
		}
		this.vcardsElementExists = isVCardsElement(parent);

		try {
			SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
			handler = factory.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}

		Result result = (writer == null) ? new DOMResult(parent) : new StreamResult(writer);
		handler.setResult(result);
	}

	private boolean isVCardsElement(Node node) {
		if (node == null) {
			return false;
		}

		if (!(node instanceof Element)) {
			return false;
		}

		QName vcards = XCardQNames.VCARDS;
		return vcards.getNamespaceURI().equals(node.getNamespaceURI()) && vcards.getLocalPart().equals(node.getLocalName());
	}

	@Override
	protected void _write(VCard vcard, List<VCardProperty> properties) throws IOException {
		try {
			if (!started) {
				handler.startDocument();

				if (!vcardsElementExists) {
					//don't output a <vcards> element if the parent is a <vcards> element
					start(VCARDS);
					level++;
				}

				started = true;
			}

			ListMultimap<String, VCardProperty> propertiesByGroup = new ListMultimap<String, VCardProperty>(); //group the types by group name (null = no group name)
			for (VCardProperty property : properties) {
				propertiesByGroup.put(property.getGroup(), property);
			}

			start(VCARD);
			level++;

			for (Map.Entry<String, List<VCardProperty>> entry : propertiesByGroup) {
				String groupName = entry.getKey();
				if (groupName != null) {
					AttributesImpl attr = new AttributesImpl();
					attr.addAttribute(XCardQNames.NAMESPACE, "", "name", "", groupName);

					start(GROUP, attr);
					level++;
				}

				for (VCardProperty property : entry.getValue()) {
					write(property, vcard);
				}

				if (groupName != null) {
					level--;
					end(GROUP);
				}
			}

			level--;
			end(VCARD);
		} catch (SAXException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected VCardVersion getTargetVersion() {
		return targetVersion;
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
	 * Terminates the XML document and closes the output stream.
	 */
	public void close() throws IOException {
		try {
			if (!started) {
				handler.startDocument();

				if (!vcardsElementExists) {
					//don't output a <vcards> element if the parent is a <vcards> element
					start(VCARDS);
					level++;
				}
			}

			if (!vcardsElementExists) {
				level--;
				end(VCARDS);
			}
			handler.endDocument();
		} catch (SAXException e) {
			throw new IOException(e);
		}

		if (writer != null) {
			writer.close();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void write(VCardProperty property, VCard vcard) throws SAXException {
		VCardPropertyScribe scribe = index.getPropertyScribe(property);
		VCardParameters parameters = scribe.prepareParameters(property, targetVersion, vcard);

		//get the property element to write
		Element propertyElement;
		if (property instanceof Xml) {
			Xml xml = (Xml) property;
			Document value = xml.getValue();
			if (value == null) {
				return;
			}
			propertyElement = XmlUtils.getRootElement(value);
		} else {
			QName qname = scribe.getQName();
			propertyElement = DOC.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
			try {
				scribe.writeXml(property, propertyElement);
			} catch (SkipMeException e) {
				return;
			} catch (EmbeddedVCardException e) {
				return;
			}
		}

		start(propertyElement);
		level++;

		write(parameters);
		write(propertyElement);

		level--;
		end(propertyElement);
	}

	private void write(Element propertyElement) throws SAXException {
		NodeList children = propertyElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (child instanceof Element) {
				Element element = (Element) child;

				if (element.hasChildNodes()) {
					start(element);
					level++;

					write(element);

					level--;
					end(element);
				} else {
					//make childless elements appear as "<foo />" instead of "<foo></foo>"
					childless(element);
				}

				continue;
			}

			if (child instanceof Text) {
				Text text = (Text) child;
				text(text.getTextContent());
				continue;
			}
		}
	}

	private void write(VCardParameters parameters) throws SAXException {
		if (parameters.isEmpty()) {
			return;
		}

		start(PARAMETERS);
		level++;

		for (Map.Entry<String, List<String>> parameter : parameters) {
			String parameterName = parameter.getKey().toLowerCase();
			start(parameterName);
			level++;

			for (String parameterValue : parameter.getValue()) {
				VCardDataType dataType = parameterDataTypes.get(parameterName);
				String dataTypeElementName = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();

				start(dataTypeElementName);
				text(parameterValue);
				end(dataTypeElementName);
			}

			level--;
			end(parameterName);
		}

		level--;
		end(PARAMETERS);
	}

	private void indent() throws SAXException {
		if (indent == null) {
			return;
		}

		//"\n" is hard-coded here because if the Windows "\r\n" is used, it will encode the "\r" character for XML ("&#13;")
		String str = '\n' + StringUtils.repeat(indent, level);
		handler.ignorableWhitespace(str.toCharArray(), 0, str.length());
	}

	private void childless(Element element) throws SAXException {
		Attributes attributes = getElementAttributes(element);
		indent();
		handler.startElement(element.getNamespaceURI(), "", element.getLocalName(), attributes);
		handler.endElement(element.getNamespaceURI(), "", element.getLocalName());
	}

	private void start(Element element) throws SAXException {
		Attributes attributes = getElementAttributes(element);
		start(element.getNamespaceURI(), element.getLocalName(), attributes);
	}

	private void start(String element) throws SAXException {
		start(element, null);
	}

	private void start(QName qname) throws SAXException {
		start(qname, null);
	}

	private void start(QName qname, Attributes attributes) throws SAXException {
		start(qname.getNamespaceURI(), qname.getLocalPart(), attributes);
	}

	private void start(String element, Attributes attributes) throws SAXException {
		start(targetVersion.getXmlNamespace(), element, attributes);
	}

	private void start(String namespace, String element, Attributes attributes) throws SAXException {
		indent();
		handler.startElement(namespace, "", element, attributes);
	}

	private void end(Element element) throws SAXException {
		end(element.getNamespaceURI(), element.getLocalName());
	}

	private void end(String element) throws SAXException {
		end(targetVersion.getXmlNamespace(), element);
	}

	private void end(QName qname) throws SAXException {
		end(qname.getNamespaceURI(), qname.getLocalPart());
	}

	private void end(String namespace, String element) throws SAXException {
		if (!textNodeJustPrinted) {
			indent();
		}

		handler.endElement(namespace, "", element);
		textNodeJustPrinted = false;
	}

	private void text(String text) throws SAXException {
		handler.characters(text.toCharArray(), 0, text.length());
		textNodeJustPrinted = true;
	}

	private Attributes getElementAttributes(Element element) {
		AttributesImpl attributes = new AttributesImpl();
		NamedNodeMap attributeNodes = element.getAttributes();
		for (int i = 0; i < attributeNodes.getLength(); i++) {
			Node node = attributeNodes.item(i);
			attributes.addAttribute(node.getNamespaceURI(), "", node.getLocalName(), "", node.getNodeValue());
		}
		return attributes;
	}
}
