package ezvcard.io.xml;

import static ezvcard.io.xml.XCardQNames.GROUP;
import static ezvcard.io.xml.XCardQNames.PARAMETERS;
import static ezvcard.io.xml.XCardQNames.VCARD;
import static ezvcard.io.xml.XCardQNames.VCARDS;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.namespace.QName;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamReader;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
import ezvcard.util.ClearableStringBuilder;
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
 * <p>
 * Reads xCards (XML-encoded vCards) in a streaming fashion.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * File file = new File("vcards.xml");
 * XCardReader reader = null;
 * try {
 *   reader = new XCardReader(file);
 *   VCard vcard;
 *   while ((vcard = reader.readNext()) != null){
 * 	   ...
 *   }
 * } finally {
 *   if (reader != null) reader.close();
 * }
 * </pre>
 * 
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCardReader extends StreamReader {
	private final VCardVersion version = VCardVersion.V4_0;
	private final String NS = version.getXmlNamespace();

	private final Source source;
	private final Closeable stream;

	private volatile VCard readVCard;
	private volatile TransformerException thrown;

	private final ReadThread thread = new ReadThread();
	private final Object lock = new Object();
	private final BlockingQueue<Object> readerBlock = new ArrayBlockingQueue<Object>(1);
	private final BlockingQueue<Object> threadBlock = new ArrayBlockingQueue<Object>(1);

	/**
	 * @param xml the XML to read from
	 */
	public XCardReader(String xml) {
		this(new StringReader(xml));
	}

	/**
	 * @param in the input stream to read from
	 */
	public XCardReader(InputStream in) {
		source = new StreamSource(in);
		stream = in;
	}

	/**
	 * @param file the file to read from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public XCardReader(File file) throws FileNotFoundException {
		this(new BufferedInputStream(new FileInputStream(file)));
	}

	/**
	 * @param reader the reader to read from
	 */
	public XCardReader(Reader reader) {
		source = new StreamSource(reader);
		stream = reader;
	}

	/**
	 * @param node the DOM node to read from
	 */
	public XCardReader(Node node) {
		source = new DOMSource(node);
		stream = null;
	}

	@Override
	protected VCard _readNext() throws IOException {
		readVCard = null;
		thrown = null;

		if (!thread.started) {
			thread.start();
		} else {
			if (thread.finished || thread.closed) {
				return null;
			}

			try {
				threadBlock.put(lock);
			} catch (InterruptedException e) {
				return null;
			}
		}

		//wait until thread reads xCard
		try {
			readerBlock.take();
		} catch (InterruptedException e) {
			return null;
		}

		if (thrown != null) {
			throw new IOException(thrown);
		}

		return readVCard;
	}

	private class ReadThread extends Thread {
		private final SAXResult result;
		private final Transformer transformer;
		private volatile boolean finished = false, started = false, closed = false;

		public ReadThread() {
			setName(getClass().getSimpleName());

			//create the transformer
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				XmlUtils.applyXXEProtection(factory);

				transformer = factory.newTransformer();
			} catch (TransformerConfigurationException e) {
				//shouldn't be thrown because it's a simple configuration
				throw new RuntimeException(e);
			}

			//prevent error messages from being printed to stderr
			transformer.setErrorListener(new NoOpErrorListener());

			result = new SAXResult(new ContentHandlerImpl());
		}

		@Override
		public void run() {
			started = true;

			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				if (!thread.closed) {
					thrown = e;
				}
			} finally {
				finished = true;
				try {
					readerBlock.put(lock);
				} catch (InterruptedException e) {
					//ignore
				}
			}
		}
	}

	private class ContentHandlerImpl extends DefaultHandler {
		private final Document DOC = XmlUtils.createDocument();
		private final XCardStructure structure = new XCardStructure();
		private final ClearableStringBuilder characterBuffer = new ClearableStringBuilder();

		private String group;
		private Element propertyElement, parent;
		private QName paramName;
		private VCardParameters parameters;

		@Override
		public void characters(char[] buffer, int start, int length) throws SAXException {
			/*
			 * Ignore all text nodes that are outside of a property element. All
			 * valid text nodes will be inside of property elements (parameter
			 * values and property values)
			 */
			if (propertyElement == null) {
				return;
			}

			characterBuffer.append(buffer, start, length);
		}

		@Override
		public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
			QName qname = new QName(namespace, localName);
			String textContent = characterBuffer.getAndClear();

			if (structure.isEmpty()) {
				//<vcards>
				if (VCARDS.equals(qname)) {
					structure.push(ElementType.vcards);
				}
				return;
			}

			ElementType parentType = structure.peek();
			ElementType typeToPush = null;

			if (parentType != null) {
				switch (parentType) {
				case vcards:
					//<vcard>
					if (VCARD.equals(qname)) {
						readVCard = new VCard();
						readVCard.setVersion(version);
						typeToPush = ElementType.vcard;
					}
					break;

				case vcard:
					//<group>
					if (GROUP.equals(qname)) {
						group = attributes.getValue("name");
						typeToPush = ElementType.group;
					} else {
						propertyElement = createElement(namespace, localName, attributes);
						parameters = new VCardParameters();
						parent = propertyElement;
						typeToPush = ElementType.property;
					}
					break;

				case group:
					propertyElement = createElement(namespace, localName, attributes);
					parameters = new VCardParameters();
					parent = propertyElement;
					typeToPush = ElementType.property;
					break;

				case property:
					//<parameters>
					if (PARAMETERS.equals(qname)) {
						typeToPush = ElementType.parameters;
					}
					break;

				case parameters:
					//inside of <parameters>
					if (NS.equals(namespace)) {
						paramName = qname;
						typeToPush = ElementType.parameter;
					}
					break;

				case parameter:
					if (NS.equals(namespace)) {
						typeToPush = ElementType.parameterValue;
					}
					break;

				case parameterValue:
					//should never have child elements
					break;
				}
			}

			//append to property element
			if (propertyElement != null && typeToPush != ElementType.property && typeToPush != ElementType.parameters && !structure.isUnderParameters()) {
				if (textContent.length() > 0) {
					parent.appendChild(DOC.createTextNode(textContent));
				}
				Element element = createElement(namespace, localName, attributes);
				parent.appendChild(element);
				parent = element;
			}

			structure.push(typeToPush);
		}

		@Override
		public void endElement(String namespace, String localName, String qName) throws SAXException {
			String textContent = characterBuffer.getAndClear();

			if (structure.isEmpty()) {
				//no <vcards> elements were read yet
				return;
			}

			ElementType type = structure.pop();
			if (type == null && (propertyElement == null || structure.isUnderParameters())) {
				//it's a non-xCard element
				return;
			}

			if (type != null) {
				switch (type) {
				case parameterValue:
					parameters.put(paramName.getLocalPart(), textContent);
					break;

				case parameter:
					//do nothing
					break;

				case parameters:
					//do nothing
					break;

				case property:
					propertyElement.appendChild(DOC.createTextNode(textContent));

					String propertyName = localName;
					VCardProperty property;
					QName propertyQName = new QName(propertyElement.getNamespaceURI(), propertyElement.getLocalName());
					VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(propertyQName);
					try {
						Result<? extends VCardProperty> result = scribe.parseXml(propertyElement, parameters);
						property = result.getProperty();
						property.setGroup(group);
						readVCard.addProperty(property);
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
						readVCard.addProperty(property);
					} catch (EmbeddedVCardException e) {
						warnings.add(null, propertyName, 34);
					}

					propertyElement = null;
					break;

				case group:
					group = null;
					break;

				case vcard:
					//wait for readNext() to be called again
					try {
						readerBlock.put(lock);
						threadBlock.take();
					} catch (InterruptedException e) {
						throw new SAXException(e);
					}
					break;

				case vcards:
					//do nothing
					break;
				}
			}

			//append element to property element
			if (propertyElement != null && type != ElementType.property && type != ElementType.parameters && !structure.isUnderParameters()) {
				if (textContent.length() > 0) {
					parent.appendChild(DOC.createTextNode(textContent));
				}
				parent = (Element) parent.getParentNode();
			}
		}

		private Element createElement(String namespace, String localName, Attributes attributes) {
			Element element = DOC.createElementNS(namespace, localName);
			applyAttributesTo(element, attributes);
			return element;
		}

		private void applyAttributesTo(Element element, Attributes attributes) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String qname = attributes.getQName(i);
				if (qname.startsWith("xmlns:")) {
					continue;
				}

				String name = attributes.getLocalName(i);
				String value = attributes.getValue(i);
				element.setAttribute(name, value);
			}
		}
	}

	private enum ElementType {
		//enum values are lower-case so they won't get confused with the "XCardQNames" variable names
		vcards, vcard, group, property, parameters, parameter, parameterValue;
	}

	/**
	 * <p>
	 * Keeps track of the structure of an xCard XML document.
	 * </p>
	 * 
	 * <p>
	 * Note that this class is here because you can't just do QName comparisons
	 * on a one-by-one basis. The location of an XML element within the XML
	 * document is important too. It's possible for two elements to have the
	 * same QName, but be treated differently depending on their location (e.g.
	 * a parameter named "parameters")
	 * </p>
	 */
	private static class XCardStructure {
		private final List<ElementType> stack = new ArrayList<ElementType>();

		/**
		 * Pops the top element type off the stack.
		 * @return the element type or null if the stack is empty
		 */
		public ElementType pop() {
			return isEmpty() ? null : stack.remove(stack.size() - 1);
		}

		/**
		 * Looks at the top element type.
		 * @return the top element type or null if the stack is empty
		 */
		public ElementType peek() {
			return isEmpty() ? null : stack.get(stack.size() - 1);
		}

		/**
		 * Adds an element type to the stack.
		 * @param type the type to add or null if the XML element is not an
		 * xCard element
		 */
		public void push(ElementType type) {
			stack.add(type);
		}

		/**
		 * Determines if the leaf node is under a {@code <parameters>} element.
		 * @return true if it is, false if not
		 */
		public boolean isUnderParameters() {
			//get the first non-null type
			ElementType nonNull = null;
			for (int i = stack.size() - 1; i >= 0; i--) {
				ElementType type = stack.get(i);
				if (type != null) {
					nonNull = type;
					break;
				}
			}

			//@formatter:off
			return
			nonNull == ElementType.parameters ||
			nonNull == ElementType.parameter ||
			nonNull == ElementType.parameterValue;
			//@formatter:on
		}

		/**
		 * Determines if the stack is empty
		 * @return true if the stack is empty, false if not
		 */
		public boolean isEmpty() {
			return stack.isEmpty();
		}
	}

	/**
	 * An implementation of {@link ErrorListener} that doesn't do anything.
	 */
	private static class NoOpErrorListener implements ErrorListener {
		public void error(TransformerException e) {
			//do nothing
		}

		public void fatalError(TransformerException e) {
			//do nothing
		}

		public void warning(TransformerException e) {
			//do nothing
		}
	}

	/**
	 * Closes the underlying input stream.
	 */
	public void close() throws IOException {
		if (thread.isAlive()) {
			thread.closed = true;
			thread.interrupt();
		}

		if (stream != null) {
			stream.close();
		}
	}
}
