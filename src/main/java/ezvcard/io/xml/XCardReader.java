package ezvcard.io.xml;

import static ezvcard.io.xml.XCardQNames.GROUP;
import static ezvcard.io.xml.XCardQNames.PARAMETERS;
import static ezvcard.io.xml.XCardQNames.VCARD;
import static ezvcard.io.xml.XCardQNames.VCARDS;

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
 * File file = new File(&quot;vcards.xml&quot;);
 * List&lt;VCard&gt; vcards = new ArrayList&lt;VCard&gt;();
 * XCardReader xcardReader = new XCardReader(file);
 * VCard vcard;
 * while ((vcard = xcardReader.readNext()) != null) {
 * 	vcards.add(vcard);
 * }
 * xcardReader.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCardReader implements Closeable {
	private final VCardVersion version = VCardVersion.V4_0;
	private final String NS = version.getXmlNamespace();

	private final Source source;
	private final Closeable stream;
	private volatile ScribeIndex index = new ScribeIndex();

	private volatile VCard readVCard;
	private final ParseWarnings warnings = new ParseWarnings();
	private volatile TransformerException thrown;

	private final ReadThread thread = new ReadThread();
	private final Object lock = new Object();
	private final BlockingQueue<Object> readerBlock = new ArrayBlockingQueue<Object>(1);
	private final BlockingQueue<Object> threadBlock = new ArrayBlockingQueue<Object>(1);

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
		source = new StreamSource(in);
		stream = in;
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
		source = new StreamSource(reader);
		stream = reader;
	}

	/**
	 * Creates an xCard reader.
	 * @param node the DOM node to read from
	 */
	public XCardReader(Node node) {
		source = new DOMSource(node);
		stream = null;
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
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return warnings.copy();
	}

	/**
	 * Reads the next vCard from the xCard stream.
	 * @return the next vCard or null if there are no more
	 * @throws TransformerException if there's a problem reading from the stream
	 */
	public VCard readNext() throws TransformerException {
		readVCard = null;
		warnings.clear();
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
			throw thrown;
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
				transformer = TransformerFactory.newInstance().newTransformer();
			} catch (TransformerConfigurationException e) {
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
		private final Hierarchy hierarchy = new Hierarchy();

		private String group;
		private StringBuilder characterBuffer = new StringBuilder();
		private Element propertyElement, parent;
		private QName propertyQName, paramName, paramDataType;

		private VCardParameters parameters;

		@Override
		public void characters(char[] buffer, int start, int length) throws SAXException {
			if (propertyElement == null) {
				return;
			}

			characterBuffer.append(buffer, start, length);
		}

		@Override
		public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
			QName qname = new QName(namespace, localName);
			String textContent = characterBuffer.toString();
			characterBuffer.setLength(0);

			if (hierarchy.eq()) {
				//<vcards>
				if (VCARDS.equals(qname)) {
					hierarchy.push(qname);
				}
				return;
			}

			if (hierarchy.eq(VCARDS)) {
				//<vcard>
				if (VCARD.equals(qname)) {
					readVCard = new VCard();
					readVCard.setVersion(version);
					hierarchy.push(qname);
				}
				return;
			}

			hierarchy.push(qname);

			//<group>
			if (hierarchy.eq(VCARDS, VCARD, GROUP)) {
				group = attributes.getValue("name");
				return;
			}

			//start property element
			if (propertyElement == null) {
				propertyElement = createElement(namespace, localName, attributes);
				propertyQName = qname;
				parameters = new VCardParameters();
				parent = propertyElement;
				return;
			}

			//<parameters>
			if ((group == null && hierarchy.eq(VCARDS, VCARD, propertyQName, PARAMETERS)) || (group != null && hierarchy.eq(VCARDS, VCARD, GROUP, propertyQName, PARAMETERS))) {
				return;
			}

			//inside of <parameters>
			if ((group == null && hierarchy.startsWith(VCARDS, VCARD, propertyQName, PARAMETERS)) || (group != null && hierarchy.startsWith(VCARDS, VCARD, GROUP, propertyQName, PARAMETERS))) {
				if (NS.equals(namespace)) {
					if (hierarchy.endsWith(paramName, qname)) {
						paramDataType = qname;
					} else {
						paramName = qname;
					}
				}

				return;
			}

			//append to property element
			if (textContent.length() > 0) {
				parent.appendChild(DOC.createTextNode(textContent));
			}
			Element element = createElement(namespace, localName, attributes);
			parent.appendChild(element);
			parent = element;
		}

		@Override
		public void endElement(String namespace, String localName, String qName) throws SAXException {
			String textContent = characterBuffer.toString();
			characterBuffer.setLength(0);

			if (hierarchy.eq()) {
				//no <vcards> elements were read yet
				return;
			}

			if (hierarchy.eq(VCARDS) && (!namespace.equals(VCARD.getNamespaceURI()) || !localName.equals(VCARD.getLocalPart()))) {
				//ignore any non-<vcard> elements under <vcards>
				return;
			}

			QName qname = hierarchy.pop();

			//if inside of <parameters>
			if (((group == null && hierarchy.startsWith(VCARDS, VCARD, propertyQName, PARAMETERS)) || (group != null && hierarchy.startsWith(VCARDS, VCARD, GROUP, propertyQName, PARAMETERS)))) {
				if (qname.equals(paramDataType)) {
					parameters.put(paramName.getLocalPart(), textContent);
					paramDataType = null;
					return;
				}

				if (qname.equals(paramName)) {
					paramName = null;
					return;
				}

				return;
			}

			//</parameters>
			if (((group == null && hierarchy.eq(VCARDS, VCARD, propertyQName)) || (group != null && hierarchy.eq(VCARDS, VCARD, GROUP, propertyQName))) && qname.equals(PARAMETERS)) {
				return;
			}

			//if the property element has ended
			if (((group == null && hierarchy.eq(VCARDS, VCARD)) || (group != null && hierarchy.eq(VCARDS, VCARD, GROUP))) && qname.equals(propertyQName)) {
				propertyElement.appendChild(DOC.createTextNode(textContent));

				String propertyName = localName;
				VCardProperty property;
				VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(qname);
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
				return;
			}

			//if inside of the property element
			if (propertyElement != null) {
				if (textContent.length() > 0) {
					parent.appendChild(DOC.createTextNode(textContent));
				}
				parent = (Element) parent.getParentNode();
				return;
			}

			//</group>
			if (hierarchy.eq(VCARDS, VCARD) && qname.equals(GROUP)) {
				group = null;
				return;
			}

			//</vcard>
			if (hierarchy.eq(VCARDS) && qname.equals(VCARD)) {
				//wait for readNext() to be called again
				try {
					readerBlock.put(lock);
					threadBlock.take();
				} catch (InterruptedException e) {
					throw new SAXException(e);
				}

				return;
			}
		}

		private Element createElement(String namespace, String localName, Attributes attributes) {
			Element element = DOC.createElementNS(namespace, localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				String qname = attributes.getQName(i);
				if (qname.startsWith("xmlns:")) {
					continue;
				}

				String name = attributes.getLocalName(i);
				String value = attributes.getValue(i);
				element.setAttribute(name, value);
			}
			return element;
		}
	}

	private class Hierarchy {
		private final List<QName> stack = new ArrayList<QName>();

		public boolean eq(QName... elements) {
			if (elements.length != stack.size()) {
				return false;
			}

			for (int i = elements.length - 1; i >= 0; i--) {
				//iterate backwards because it will result in less comparisons
				QName hier = stack.get(i);
				QName element = elements[i];
				if (!hier.equals(element)) {
					return false;
				}
			}
			return true;
		}

		public boolean startsWith(QName... elements) {
			if (elements.length > stack.size()) {
				return false;
			}

			for (int i = elements.length - 1; i >= 0; i--) {
				QName element = elements[i];
				QName hier = stack.get(i);
				if (!hier.equals(element)) {
					return false;
				}
			}
			return true;
		}

		public boolean endsWith(QName... elements) {
			if (elements.length > stack.size()) {
				return false;
			}

			for (int i = elements.length - 1; i >= 0; i--) {
				QName element = elements[i];
				QName hier = stack.get(stack.size() - (elements.length - i));
				if (!hier.equals(element)) {
					return false;
				}
			}
			return true;
		}

		public QName pop() {
			return stack.isEmpty() ? null : stack.remove(stack.size() - 1);
		}

		public void push(QName qname) {
			stack.add(qname);
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
