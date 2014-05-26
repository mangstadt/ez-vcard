package ezvcard.io.xml;

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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.AbstractVCardWriter;
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
public class XCardDocument extends AbstractVCardWriter {
	private static final VCardVersion targetVersion = VCardVersion.V4_0; //xCard only supports 4.0

	private List<List<String>> parseWarnings;
	private Document document;
	private XCardWriter writer;

	/**
	 * Creates an empty xCard document.
	 */
	public XCardDocument() {
		this(defaultRootElement());
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
		this.document = document;

		XCardNamespaceContext nsContext = new XCardNamespaceContext(targetVersion, "v");
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(nsContext);

		//find the <vcards> element
		Element vcardsElement;
		try {
			String prefix = nsContext.getPrefix();
			vcardsElement = (Element) xpath.evaluate("//" + prefix + ":" + XCardQNames.VCARDS.getLocalPart(), document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			//never thrown because xpath expression is hard coded
			throw new RuntimeException(e);
		}

		Node parent = (vcardsElement == null) ? document : vcardsElement;
		writer = new XCardWriter(parent);
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
		writer.registerParameterDataType(parameterName, dataType);
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
		return parse(false);
	}

	/**
	 * Parses the first the {@link VCard} object from the xCard document.
	 * @return the vCard object
	 */
	public VCard parseFirst() {
		List<VCard> vcards = parse(true);
		return vcards.isEmpty() ? null : vcards.get(0);
	}

	private List<VCard> parse(boolean parseFirstOnly) {
		XCardReader reader = new XCardReader(document);
		reader.setScribeIndex(index);
		XCardListenerImpl listener = new XCardListenerImpl(parseFirstOnly);
		parseWarnings = listener.warnings;

		try {
			reader.read(listener);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}

		return listener.vcards;
	}

	/**
	 * Adds a vCard to the XML document.
	 * @param vcard the vCard to add
	 * @throws IllegalArgumentException if a scribe hasn't been registered for a
	 * custom property class (see: {@link #registerScribe})
	 */
	public void add(VCard vcard) {
		writer.setVersionStrict(versionStrict);
		writer.setAddProdId(addProdId);
		writer.setScribeIndex(index);

		try {
			writer.write(vcard);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes the XML document to a string without pretty-printing.
	 * @return the XML string
	 */
	public String write() {
		return write(-1);
	}

	/**
	 * Writes the XML document to a string with pretty-printing.
	 * @param indent the number of spaces to use for indentation
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
	 * Writes the XML document to an output stream without pretty-printing.
	 * @param out the output stream
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out) throws TransformerException {
		write(out, -1);
	}

	/**
	 * Writes the XML document to an output stream with pretty-printing.
	 * @param out the output stream
	 * @param indent the number of spaces to use for indentation
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, int indent) throws TransformerException {
		write(utf8Writer(out), indent);
	}

	/**
	 * Writes the XML document to a file without pretty-printing.
	 * @param file the file
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file) throws TransformerException, IOException {
		write(file, -1);
	}

	/**
	 * Writes the XML document to a file with pretty-printing.
	 * @param file the file stream
	 * @param indent the number of spaces to use for indentation
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
	 * Writes the XML document to a writer without pretty-printing.
	 * @param writer the writer
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer) throws TransformerException {
		write(writer, -1);
	}

	/**
	 * Writes the XML document to a writer with pretty-printing.
	 * @param writer the writer
	 * @param indent the number of spaces to use for indentation
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

	private static Document defaultRootElement() {
		Document document = XmlUtils.createDocument();
		QName vcards = XCardQNames.VCARDS;
		Element root = document.createElementNS(vcards.getNamespaceURI(), vcards.getLocalPart());
		document.appendChild(root);
		return document;
	}

	private static Document readFile(File file) throws SAXException, IOException {
		InputStream in = new FileInputStream(file);
		try {
			return XmlUtils.toDocument(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private static class XCardListenerImpl implements XCardListener {
		private final List<VCard> vcards = new ArrayList<VCard>();
		private final List<List<String>> warnings = new ArrayList<List<String>>();
		private final boolean parseFirstOnly;

		public XCardListenerImpl(boolean parseFirstOnly) {
			this.parseFirstOnly = parseFirstOnly;
		}

		public void vcardRead(VCard vcard, List<String> warnings) throws StopReadingException {
			this.vcards.add(vcard);
			this.warnings.add(warnings);

			if (parseFirstOnly) {
				throw new StopReadingException();
			}
		}
	}
}
