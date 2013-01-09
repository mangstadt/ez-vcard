package ezvcard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ezvcard.io.HCardPage;
import ezvcard.io.HCardReader;
import ezvcard.io.IParser;
import ezvcard.io.VCardReader;
import ezvcard.io.VCardWriter;
import ezvcard.io.XCardDocument;
import ezvcard.io.XCardReader;
import ezvcard.types.VCardType;
import ezvcard.util.IOUtils;
import freemarker.template.TemplateException;

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
 * <p>
 * Contains helper methods for parsing/writing vCards. The methods wrap the
 * following classes:
 * </p>
 * 
 * 
 * <table border="1">
 * <tr>
 * <th></th>
 * <th>Reading</th>
 * <th>Writing</th>
 * </tr>
 * <tr>
 * <th>Plain text</th>
 * <td>{@link VCardReader}</td>
 * <td>{@link VCardWriter}</td>
 * </tr>
 * <tr>
 * <th>XML</th>
 * <td>{@link XCardReader}</td>
 * <td>{@link XCardDocument}</td>
 * </tr>
 * <tr>
 * <th>HTML</th>
 * <td>{@link HCardReader}</td>
 * <td>{@link HCardPage}</td>
 * </tr>
 * </table>
 * @author Michael Angstadt
 */
public class Ezvcard {
	/**
	 * The version of the library.
	 */
	public static final String VERSION;

	/**
	 * The project webpage.
	 */
	public static final String URL;

	static {
		InputStream in = null;
		try {
			in = Ezvcard.class.getResourceAsStream("/ez-vcard-info.properties");
			Properties props = new Properties();
			props.load(in);
			VERSION = props.getProperty("version");
			URL = props.getProperty("url");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * <p>
	 * Parses plain text vCards.
	 * </p>
	 * <p>
	 * Use {@link VCardReader} for more control over the parsing.
	 * </p>
	 * @param str the vCard string
	 * @return chainer object for completing the parse operation
	 * @see VCardReader
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static TextStringParserChain parse(String str) {
		return new TextStringParserChain(str);
	}

	/**
	 * <p>
	 * Parses plain text vCards.
	 * </p>
	 * <p>
	 * Use {@link VCardReader} for more control over the parsing.
	 * </p>
	 * @param file the vCard file
	 * @return chainer object for completing the parse operation
	 * @throws FileNotFoundException if the file does not exist or cannot be
	 * accessed
	 * @see VCardReader
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static TextParserChain parse(File file) throws FileNotFoundException {
		return parse(new FileReader(file));
	}

	/**
	 * <p>
	 * Parses plain text vCards.
	 * </p>
	 * <p>
	 * Use {@link VCardReader} for more control over the parsing.
	 * </p>
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 * @see VCardReader
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static TextParserChain parse(InputStream in) {
		return parse(new InputStreamReader(in));
	}

	/**
	 * <p>
	 * Parses plain text vCards.
	 * </p>
	 * <p>
	 * Use {@link VCardReader} for more control over the parsing.
	 * </p>
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 * @see VCardReader
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static TextParserChain parse(Reader reader) {
		return new TextParserChain(reader);
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard).
	 * </p>
	 * <p>
	 * Use {@link XCardReader} for more control over the parsing.
	 * </p>
	 * @param xml the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static XmlStringParserChain parseXml(String xml) {
		return new XmlStringParserChain(xml);
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard).
	 * </p>
	 * <p>
	 * Use {@link XCardReader} for more control over the parsing.
	 * </p>
	 * @param file the XML file
	 * @return chainer object for completing the parse operation
	 * @throws FileNotFoundException if the file does not exist or cannot be
	 * accessed
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static XmlParserChain parseXml(File file) throws FileNotFoundException {
		return parseXml(new FileReader(file));
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard).
	 * </p>
	 * <p>
	 * Use {@link XCardReader} for more control over the parsing.
	 * </p>
	 * @param in the input stream to the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static XmlParserChain parseXml(InputStream in) {
		return parseXml(new InputStreamReader(in));
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard).
	 * </p>
	 * <p>
	 * Use {@link XCardReader} for more control over the parsing.
	 * </p>
	 * @param reader the reader to the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static XmlParserChain parseXml(Reader reader) {
		return new XmlParserChain(reader);
	}

	/**
	 * <p>
	 * Parses HTML-encoded vCards (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardReader} for more control over the parsing.
	 * </p>
	 * @param html the HTML page
	 * @return chainer object for completing the parse operation
	 * @see HCardReader
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static HtmlStringParserChain parseHtml(String html) {
		return new HtmlStringParserChain(html);
	}

	/**
	 * <p>
	 * Parses HTML-encoded vCards (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardReader} for more control over the parsing.
	 * </p>
	 * @param file the HTML file
	 * @return chainer object for completing the parse operation
	 * @throws FileNotFoundException if the file does not exist or cannot be
	 * accessed
	 * @see HCardReader
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static HtmlParserChain parseHtml(File file) throws FileNotFoundException {
		return parseHtml(new FileReader(file));
	}

	/**
	 * <p>
	 * Parses HTML-encoded vCards (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardReader} for more control over the parsing.
	 * </p>
	 * @param in the input stream to the HTML page
	 * @return chainer object for completing the parse operation
	 * @see HCardReader
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static HtmlParserChain parseHtml(InputStream in) {
		return parseHtml(new InputStreamReader(in));
	}

	/**
	 * <p>
	 * Parses HTML-encoded vCards (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardReader} for more control over the parsing.
	 * </p>
	 * @param reader the reader to the HTML page
	 * @return chainer object for completing the parse operation
	 * @see HCardReader
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static HtmlParserChain parseHtml(Reader reader) {
		return new HtmlParserChain(reader);
	}

	/**
	 * <p>
	 * Parses HTML-encoded vCards (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardReader} for more control over the parsing.
	 * </p>
	 * @param url the URL of the webpage
	 * @return chainer object for completing the parse operation
	 * @see HCardReader
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static HtmlParserChain parseHtml(URL url) {
		return new HtmlParserChain(url);
	}

	/**
	 * <p>
	 * Marshals one or more vCards their text representation.
	 * </p>
	 * 
	 * <p>
	 * Use {@link VCardWriter} for more control over how the vCard is written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see VCardWriter
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static TextWriterChain write(VCard... vcards) {
		return write(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards their text representation.
	 * </p>
	 * 
	 * <p>
	 * Use {@link VCardWriter} for more control over how the vCard is written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see VCardWriter
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static TextWriterChain write(Collection<VCard> vcards) {
		return new TextWriterChain(vcards);
	}

	/**
	 * <p>
	 * Marshals one or more vCards their XML representation (xCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link XCardDocument} for more control over how the vCard is written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see XCardDocument
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static XmlWriterChain writeXml(VCard... vcards) {
		return writeXml(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards their XML representation (xCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link XCardDocument} for more control over how the vCard is written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see XCardDocument
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static XmlWriterChain writeXml(Collection<VCard> vcards) {
		return new XmlWriterChain(vcards);
	}

	/**
	 * <p>
	 * Marshals one or more vCards their HTML representation (hCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link HCardPage} for more control over how the vCard is written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see HCardPage
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static HtmlWriterChain writeHtml(VCard... vcards) {
		return writeHtml(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards their HTML representation (hCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link HCardPage} for more control over how the vCard is written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see HCardPage
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static HtmlWriterChain writeHtml(Collection<VCard> vcards) {
		return new HtmlWriterChain(vcards);
	}

	public static abstract class ParserChain {
		final List<Class<? extends VCardType>> extendedTypes = new ArrayList<Class<? extends VCardType>>();
		List<List<String>> warnings;

		/**
		 * Reads the first vCard from the stream.
		 * @return the vCard or null if there are no vCards
		 * @throws IOException if there's an I/O problem
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public VCard first() throws IOException, SAXException {
			IParser parser = ready();
			VCard vcard = parser.readNext();
			if (warnings != null) {
				warnings.add(parser.getWarnings());
			}
			return vcard;
		}

		/**
		 * Reads all vCards from the stream.
		 * @return the parsed vCards
		 * @throws IOException if there's an I/O problem
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public List<VCard> all() throws IOException, SAXException {
			IParser parser = ready();
			List<VCard> vcards = new ArrayList<VCard>();
			VCard vcard;
			while ((vcard = parser.readNext()) != null) {
				if (warnings != null) {
					warnings.add(parser.getWarnings());
				}
				vcards.add(vcard);
			}
			return vcards;
		}

		private IParser ready() throws IOException, SAXException {
			IParser parser = init();
			for (Class<? extends VCardType> extendedType : extendedTypes) {
				parser.registerExtendedType(extendedType);
			}
			return parser;
		}

		/**
		 * Creates the parser.
		 * @return the parser object
		 */
		abstract IParser init() throws IOException, SAXException;
	}

	/**
	 * Convenience chainer class for parsing plain text vCards.
	 */
	public static class TextParserChain extends ParserChain {
		private Reader reader;

		private TextParserChain(Reader reader) {
			this.reader = reader;
		}

		/**
		 * Registers an extended type class.
		 * @param typeClass the extended type class
		 * @return this
		 */
		public TextParserChain register(Class<? extends VCardType> typeClass) {
			extendedTypes.add(typeClass);
			return this;
		}

		/**
		 * Provides a list object that any unmarshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each unmarshalled vCard. Each element of the list is the
		 * list of warnings for one of the unmarshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public TextParserChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		IParser init() {
			return new VCardReader(reader);
		}

		@Override
		public VCard first() throws IOException {
			try {
				return super.first();
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}

		@Override
		public List<VCard> all() throws IOException {
			try {
				return super.all();
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}
	}

	/**
	 * Convenience chainer class for parsing plain text vCards.
	 */
	public static class TextStringParserChain extends ParserChain {
		private String text;

		private TextStringParserChain(String text) {
			this.text = text;
		}

		/**
		 * Registers an extended type class.
		 * @param typeClass the extended type class
		 * @return this
		 */
		public TextStringParserChain register(Class<? extends VCardType> typeClass) {
			extendedTypes.add(typeClass);
			return this;
		}

		/**
		 * Provides a list object that any unmarshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each unmarshalled vCard. Each element of the list is the
		 * list of warnings for one of the unmarshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public TextStringParserChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		IParser init() {
			return new VCardReader(text);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (SAXException e) {
				//not parsing XML
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (SAXException e) {
				//not parsing XML
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	/**
	 * Convenience chainer class for parsing XML vCards.
	 */
	public static class XmlParserChain extends ParserChain {
		private Reader reader;

		private XmlParserChain(Reader reader) {
			this.reader = reader;
		}

		/**
		 * Registers an extended type class.
		 * @param typeClass the extended type class
		 * @return this
		 */
		public XmlParserChain register(Class<? extends VCardType> typeClass) {
			extendedTypes.add(typeClass);
			return this;
		}

		/**
		 * Provides a list object that any unmarshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each unmarshalled vCard. Each element of the list is the
		 * list of warnings for one of the unmarshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public XmlParserChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		IParser init() throws IOException, SAXException {
			return new XCardReader(reader);
		}

		@Override
		public VCard first() throws SAXException, IOException {
			return super.first();
		}

		@Override
		public List<VCard> all() throws SAXException, IOException {
			return super.all();
		}
	}

	/**
	 * Convenience chainer class for parsing XML vCards.
	 */
	public static class XmlStringParserChain extends ParserChain {
		private String xml;

		private XmlStringParserChain(String xml) {
			this.xml = xml;
		}

		/**
		 * Registers an extended type class.
		 * @param typeClass the extended type class
		 * @return this
		 */
		public XmlStringParserChain register(Class<? extends VCardType> typeClass) {
			extendedTypes.add(typeClass);
			return this;
		}

		/**
		 * Provides a list object that any unmarshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each unmarshalled vCard. Each element of the list is the
		 * list of warnings for one of the unmarshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public XmlStringParserChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		IParser init() throws IOException, SAXException {
			return new XCardReader(xml);
		}

		@Override
		public VCard first() throws SAXException {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<VCard> all() throws SAXException {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	/**
	 * Convenience chainer class for parsing HTML vCards.
	 */
	public static class HtmlParserChain extends ParserChain {
		private Reader reader;
		private URL url;
		private String pageUrl;

		private HtmlParserChain(Reader reader) {
			this.reader = reader;
		}

		private HtmlParserChain(URL url) {
			this.url = url;
		}

		/**
		 * Registers an extended type class.
		 * @param typeClass the extended type class
		 * @return this
		 */
		public HtmlParserChain register(Class<? extends VCardType> typeClass) {
			extendedTypes.add(typeClass);
			return this;
		}

		/**
		 * Provides a list object that any unmarshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each unmarshalled vCard. Each element of the list is the
		 * list of warnings for one of the unmarshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public HtmlParserChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		/**
		 * Sets the original URL of the webpage. This is used to resolve
		 * relative links and to set the SOURCE property on the vCard. Setting
		 * this property has no effect if reading from a {@link URL}.
		 * @param pageUrl the webpage URL
		 * @return this
		 */
		public HtmlParserChain pageUrl(String pageUrl) {
			this.pageUrl = pageUrl;
			return this;
		}

		@Override
		IParser init() throws IOException {
			return (url == null) ? new HCardReader(reader, pageUrl) : new HCardReader(url);
		}

		@Override
		public VCard first() throws IOException {
			try {
				return super.first();
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}

		@Override
		public List<VCard> all() throws IOException {
			try {
				return super.all();
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}
	}

	/**
	 * Convenience chainer class for parsing HTML vCards.
	 */
	public static class HtmlStringParserChain extends ParserChain {
		private String html;
		private URL url;
		private String pageUrl;

		private HtmlStringParserChain(String html) {
			this.html = html;
		}

		private HtmlStringParserChain(URL url) {
			this.url = url;
		}

		/**
		 * Registers an extended type class.
		 * @param typeClass the extended type class
		 * @return this
		 */
		public HtmlStringParserChain register(Class<? extends VCardType> typeClass) {
			extendedTypes.add(typeClass);
			return this;
		}

		/**
		 * Provides a list object that any unmarshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each unmarshalled vCard. Each element of the list is the
		 * list of warnings for one of the unmarshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public HtmlStringParserChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		/**
		 * Sets the original URL of the webpage. This is used to resolve
		 * relative links and to set the SOURCE property on the vCard. Setting
		 * this property has no effect if reading from a {@link URL}.
		 * @param pageUrl the webpage URL
		 * @return this
		 */
		public HtmlStringParserChain pageUrl(String pageUrl) {
			this.pageUrl = pageUrl;
			return this;
		}

		@Override
		IParser init() throws IOException {
			return (url == null) ? new HCardReader(html, pageUrl) : new HCardReader(url);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}
	}

	public static abstract class WriterChain {
		final Collection<VCard> vcards;

		private WriterChain(Collection<VCard> vcards) {
			this.vcards = vcards;
		}
	}

	/**
	 * Convenience chainer class for writing plain text vCards
	 */
	public static class TextWriterChain extends WriterChain {
		private VCardVersion version;
		private boolean prodId = true;
		private List<List<String>> warnings;

		private TextWriterChain(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * <p>
		 * Sets the version that all the vCards will be marshalled to. The
		 * version that is attached to each individual {@link VCard} object will
		 * be ignored.
		 * </p>
		 * <p>
		 * If no version is passed into this method, the writer will look at the
		 * version attached to each individual {@link VCard} object and marshal
		 * it to that version. And if a {@link VCard} object has no version
		 * attached to it, then it will be marshalled to version 3.0.
		 * </p>
		 * @param version the version to marshal the vCards to
		 * @return this
		 */
		public TextWriterChain version(VCardVersion version) {
			this.version = version;
			return this;
		}

		/**
		 * Sets whether or not to add a PRODID type to each vCard, saying that
		 * the vCard was generated by this library. For 2.1 vCards, the extended
		 * type X-PRODID is used, since PRODID is not supported by that version.
		 * @param prodId true to add PRODID (default), false not to
		 * @return this
		 */
		public TextWriterChain prodId(boolean prodId) {
			this.prodId = prodId;
			return this;
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of each marshalled vCard. Each element of the list is the
		 * list of warnings for one of the marshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public TextWriterChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		/**
		 * Writes the vCards to a string.
		 * @return the vCard string
		 */
		public String go() {
			StringWriter sw = new StringWriter();
			try {
				go(sw);
			} catch (IOException e) {
				//writing to a string
			}
			return sw.toString();
		}

		/**
		 * Writes the vCards to an output stream.
		 * @param out the output stream to write to
		 * @throws IOException if there's a problem writing to the output stream
		 */
		public void go(OutputStream out) throws IOException {
			go(new OutputStreamWriter(out));
		}

		/**
		 * Writes the vCards to a file.
		 * @param file the file to write to
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException {
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				go(writer);
			} finally {
				IOUtils.closeQuietly(writer);
			}
		}

		/**
		 * Writes the vCards to a writer.
		 * @param writer the writer to write to
		 * @throws IOException if there's a problem writing to the writer
		 */
		public void go(Writer writer) throws IOException {
			VCardWriter vcardWriter = new VCardWriter(writer);
			if (version != null) {
				vcardWriter.setTargetVersion(version);
			}
			vcardWriter.setAddProdId(prodId);

			for (VCard vcard : vcards) {
				if (version == null) {
					VCardVersion vcardVersion = vcard.getVersion();
					vcardWriter.setTargetVersion((vcardVersion == null) ? VCardVersion.V3_0 : vcardVersion);
				}
				vcardWriter.write(vcard);
				if (warnings != null) {
					warnings.add(vcardWriter.getWarnings());
				}
			}
		}
	}

	/**
	 * Convenience chainer class for writing XML vCards (xCard).
	 */
	public static class XmlWriterChain extends WriterChain {
		private boolean prodId = true;
		private int indent = -1;
		private List<List<String>> warnings;

		private XmlWriterChain(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * Sets whether or not to add a PRODID type to each vCard, saying that
		 * the vCard was generated by this library.
		 * @param prodId true to add PRODID (default), false not to
		 * @return this
		 */
		public XmlWriterChain prodId(boolean prodId) {
			this.prodId = prodId;
			return this;
		}

		/**
		 * Sets the number of indent spaces to use for pretty-printing. If not
		 * set, then the XML will not be pretty-printed.
		 * @param indent the number of spaces in the indent string
		 * @return this
		 */
		public XmlWriterChain indent(int indent) {
			this.indent = indent;
			return this;
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of each marshalled vCard. Each element of the list is the
		 * list of warnings for one of the marshalled vCards. Therefore, the
		 * size of this list will be equal to the number of parsed vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public XmlWriterChain warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		/**
		 * Writes the xCards to a string.
		 * @return the XML document
		 */
		public String go() {
			StringWriter sw = new StringWriter();
			try {
				go(sw);
			} catch (TransformerException e) {
				//writing to a string
			}
			return sw.toString();
		}

		/**
		 * Writes the xCards to an output stream.
		 * @param out the output stream to write to
		 * @throws TransformerException if there's a problem writing to the
		 * output stream
		 */
		public void go(OutputStream out) throws TransformerException {
			go(new OutputStreamWriter(out));
		}

		/**
		 * Writes the xCards to a file.
		 * @param file the file to write to
		 * @throws IOException if the file can't be opened
		 * @throws TransformerException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException, TransformerException {
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				go(writer);
			} finally {
				IOUtils.closeQuietly(writer);
			}
		}

		/**
		 * Writes the xCards to a writer.
		 * @param writer the writer to write to
		 * @throws TransformerException if there's a problem writing to the
		 * writer
		 */
		public void go(Writer writer) throws TransformerException {
			XCardDocument doc = createXCardDocument();
			doc.write(writer, indent);
		}

		/**
		 * Generates an XML document object model (DOM) containing the xCards.
		 * @return the DOM
		 */
		public Document dom() {
			XCardDocument doc = createXCardDocument();
			return doc.getDocument();
		}

		private XCardDocument createXCardDocument() {
			XCardDocument doc = new XCardDocument();
			doc.setAddProdId(prodId);

			for (VCard vcard : vcards) {
				doc.addVCard(vcard);
				if (warnings != null) {
					warnings.add(doc.getWarnings());
				}
			}

			return doc;
		}
	}

	/**
	 * Convenience chainer class for writing HTML vCards (hCard).
	 */
	public static class HtmlWriterChain extends WriterChain {
		private HtmlWriterChain(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * Writes the hCards to a string.
		 * @return the HTML page
		 * @throws TemplateException if there's a problem with the freemarker
		 * template
		 */
		public String go() throws TemplateException {
			StringWriter sw = new StringWriter();
			try {
				go(sw);
			} catch (IOException e) {
				//writing string
			}
			return sw.toString();
		}

		/**
		 * Writes the hCards to an output stream.
		 * @param out the output stream to write to
		 * @throws IOException if there's a problem writing to the output stream
		 * @throws TemplateException if there's a problem with the freemarker
		 * template
		 */
		public void go(OutputStream out) throws IOException, TemplateException {
			go(new OutputStreamWriter(out));
		}

		/**
		 * Writes the hCards to a file.
		 * @param file the file to write to
		 * @throws IOException if there's a problem writing to the file
		 * @throws TemplateException if there's a problem with the freemarker
		 * template
		 */
		public void go(File file) throws IOException, TemplateException {
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				go(writer);
			} finally {
				IOUtils.closeQuietly(writer);
			}
		}

		/**
		 * Writes the hCards to a writer.
		 * @param writer the writer to write to
		 * @throws IOException if there's a problem writing to the writer
		 * @throws TemplateException if there's a problem with the freemarker
		 * template
		 */
		public void go(Writer writer) throws IOException, TemplateException {
			HCardPage page = new HCardPage();
			for (VCard vcard : vcards) {
				page.addVCard(vcard);
			}
			page.write(writer);
		}
	}

	private Ezvcard() {
		//hide
	}
}
