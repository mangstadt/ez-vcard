package ezvcard.io.xml;

import static ezvcard.io.xml.XCardQNames.GROUP;
import static ezvcard.io.xml.XCardQNames.PARAMETERS;
import static ezvcard.io.xml.XCardQNames.VCARD;
import static ezvcard.io.xml.XCardQNames.VCARDS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.ParseWarnings;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
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
 * <p>
 * Reads xCards (XML-encoded vCards) in a streaming fashion.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * File file = new File("vcards.xml");
 * final List&lt;VCard&gt; vcards = new ArrayList&lt;VCard&gt;();
 * XCardReader xcardReader = new XCardReader(file);
 * xcardReader.read(new XCardReadListener(){
 *   public void vcardRead(VCard vcard, List&lt;String&gt; warnings) throws StopReadingException{
 *     vcards.add(vcard);
 *     //throw a "StopReadingException" to stop parsing early
 *   }
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCardReader {
	private final VCardVersion targetVersion = VCardVersion.V4_0;
	private final String NS = targetVersion.getXmlNamespace();

	private final Source source;
	private final ParseWarnings warnings = new ParseWarnings();
	private ScribeIndex index = new ScribeIndex();
	private XCardListener listener;

	/**
	 * Creates an xCard reader.
	 * @param str the string to read the xCards from
	 */
	public XCardReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * Creates an xCard reader.
	 * @param in the input stream to read the xCards from
	 */
	public XCardReader(InputStream in) {
		this(new StreamSource(in));
	}

	/**
	 * Creates an xCard reader.
	 * @param file the file to read the xCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public XCardReader(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}

	/**
	 * Creates an xCard reader.
	 * @param reader the reader to read from
	 */
	public XCardReader(Reader reader) {
		this(new StreamSource(reader));
	}

	/**
	 * Creates an xCard reader.
	 * @param node the DOM node to read from
	 */
	public XCardReader(Node node) {
		this(new DOMSource(node));
	}

	/**
	 * Creates an xCard reader.
	 * @param source the source to read from
	 */
	public XCardReader(Source source) {
		this.source = source;
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
	public void registerScribe(VCardPropertyScribe<? extends VCardProperty> scribe) {
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
	 * Starts parsing the XML document. This method blocks until the entire
	 * input stream or DOM is consumed, or until a {@link StopReadingException}
	 * is thrown from the given {@link XCardListener}.
	 * @param listener used for retrieving the parsed vCards
	 * @throws TransformerException if there's a problem reading from the input
	 * stream or a problem parsing the XML
	 */
	public void read(XCardListener listener) throws TransformerException {
		this.listener = listener;

		//create the transformer
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			//no complex configurations
			throw new RuntimeException(e);
		} catch (TransformerFactoryConfigurationError e) {
			//no complex configurations
			throw new RuntimeException(e);
		}

		//prevent error messages from being printed to stderr
		transformer.setErrorListener(new ErrorListener() {
			public void error(TransformerException e) {
				//empty
			}

			public void fatalError(TransformerException e) {
				//empty
			}

			public void warning(TransformerException e) {
				//empty
			}
		});

		//start parsing
		ContentHandlerImpl handler = new ContentHandlerImpl();
		SAXResult result = new SAXResult(handler);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof StopReadingException) {
				//ignore this exception because it signals that the user canceled the parsing operation
			} else {
				throw e;
			}
		}
	}

	private class ContentHandlerImpl extends DefaultHandler {
		private final Document DOC = XmlUtils.createDocument();

		private boolean inVCards, inParameters;
		private String group, paramName, paramDataType;
		private StringBuilder characterBuffer = new StringBuilder();
		private Element propertyElement, parent;

		private VCard vcard;
		private VCardParameters parameters;

		@Override
		public void characters(char[] buffer, int start, int length) throws SAXException {
			characterBuffer.append(buffer, start, length);
		}

		@Override
		public void endElement(String namespace, String localName, String qName) throws SAXException {
			QName qname = new QName(namespace, localName);
			String textContent = characterBuffer.toString();
			characterBuffer.setLength(0);

			if (paramDataType != null && NS.equals(namespace) && localName.equals(paramDataType)) {
				parameters.put(paramName, textContent);
				paramDataType = null;
				return;
			}

			if (paramName != null && NS.equals(namespace) && localName.equals(paramName)) {
				paramName = null;
				return;
			}

			if (inParameters) {
				if (PARAMETERS.equals(qname)) {
					inParameters = false;
				}
				return;
			}

			if (propertyElement != null && namespace.equals(propertyElement.getNamespaceURI()) && localName.equals(propertyElement.getLocalName())) {
				propertyElement.appendChild(DOC.createTextNode(textContent));

				String propertyName = localName;
				VCardProperty property;
				VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(qname);
				try {
					Result<? extends VCardProperty> result = scribe.parseXml(propertyElement, parameters);
					property = result.getProperty();
					property.setGroup(group);
					vcard.addProperty(property);
					for (String warning : result.getWarnings()) {
						warnings.add(null, propertyName, warning);
					}
				} catch (SkipMeException e) {
					warnings.add(null, propertyName, 22, e.getMessage());
				} catch (CannotParseException e) {
					String xml = XmlUtils.toString(propertyElement);
					warnings.add(null, propertyName, 33, xml, e.getMessage());

					scribe = index.getPropertyScribe(Xml.class);
					Result<? extends VCardProperty> result = scribe.parseXml(propertyElement, parameters);
					property = result.getProperty();
					property.setGroup(group);
					vcard.addProperty(property);
				} catch (EmbeddedVCardException e) {
					warnings.add(null, propertyName, 34);
				}

				propertyElement = null;
				return;
			}

			if (group != null && GROUP.equals(qname)) {
				group = null;
				return;
			}

			if (vcard != null && VCARD.equals(qname)) {
				listener.vcardRead(vcard, warnings.copy());
				warnings.clear();
				vcard = null;
				return;
			}

			if (inVCards && VCARDS.equals(qname)) {
				inVCards = false;
				return;
			}

			if (parent == null) {
				return;
			}

			if (textContent.length() > 0) {
				parent.appendChild(DOC.createTextNode(textContent));
			}
			parent = (Element) parent.getParentNode();
		}

		@Override
		public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
			QName qname = new QName(namespace, localName);
			String textContent = characterBuffer.toString();
			characterBuffer.setLength(0);

			if (!inVCards) {
				if (VCARDS.equals(qname)) {
					inVCards = true;
				}
				return;
			}

			if (vcard == null) {
				if (VCARD.equals(qname)) {
					vcard = new VCard();
					vcard.setVersion(targetVersion);
				}
				return;
			}

			if (group == null && GROUP.equals(qname)) {
				group = attributes.getValue("name");
				return;
			}

			if (propertyElement == null) {
				propertyElement = createElement(namespace, localName, attributes);
				parameters = new VCardParameters();
				parent = propertyElement;
				return;
			}

			if (!inParameters && PARAMETERS.equals(qname)) {
				inParameters = true;
				return;
			}

			if (inParameters) {
				if (paramName == null) {
					paramName = localName;
				} else if (paramDataType == null) {
					paramDataType = localName;
				}
				return;
			}

			if (textContent.length() > 0) {
				parent.appendChild(DOC.createTextNode(textContent));
			}
			Element element = createElement(namespace, localName, attributes);
			parent.appendChild(element);
			parent = element;
		}

		private Element createElement(String namespace, String localName, Attributes attributes) {
			Element element = DOC.createElementNS(namespace, localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getLocalName(i);
				String value = attributes.getValue(i);
				element.setAttribute(name, value);
			}
			return element;
		}
	}
}
