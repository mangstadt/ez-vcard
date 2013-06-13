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
import ezvcard.io.JCardReader;
import ezvcard.io.JCardWriter;
import ezvcard.io.VCardReader;
import ezvcard.io.VCardWriter;
import ezvcard.io.XCardDocument;
import ezvcard.io.XCardReader;
import ezvcard.types.VCardType;
import ezvcard.util.IOUtils;

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
 * <tr>
 * <th>JSON</th>
 * <td>{@link JCardReader}</td>
 * <td>{@link JCardWriter}</td>
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
			in = Ezvcard.class.getResourceAsStream("/ez-vcard.properties");
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
	public static ParserChainTextString parse(String str) {
		return new ParserChainTextString(str);
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
	public static ParserChainTextReader parse(File file) throws FileNotFoundException {
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
	public static ParserChainTextReader parse(InputStream in) {
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
	public static ParserChainTextReader parse(Reader reader) {
		return new ParserChainTextReader(reader);
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
	public static ParserChainXmlString parseXml(String xml) {
		return new ParserChainXmlString(xml);
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
	public static ParserChainXmlReader parseXml(File file) throws FileNotFoundException {
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
	public static ParserChainXmlReader parseXml(InputStream in) {
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
	public static ParserChainXmlReader parseXml(Reader reader) {
		return new ParserChainXmlReader(reader);
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard).
	 * </p>
	 * <p>
	 * Use {@link XCardReader} for more control over the parsing.
	 * </p>
	 * @param document the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static ParserChainXmlDom parseXml(Document document) {
		return new ParserChainXmlDom(document);
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
	public static ParserChainHtmlString parseHtml(String html) {
		return new ParserChainHtmlString(html);
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
	public static ParserChainHtmlReader parseHtml(File file) throws FileNotFoundException {
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
	public static ParserChainHtmlReader parseHtml(InputStream in) {
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
	public static ParserChainHtmlReader parseHtml(Reader reader) {
		return new ParserChainHtmlReader(reader);
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
	public static ParserChainHtmlReader parseHtml(URL url) {
		return new ParserChainHtmlReader(url);
	}

	/**
	 * <p>
	 * Parses JSON-encoded vCards (jCard).
	 * </p>
	 * <p>
	 * Use {@link JCardReader} for more control over the parsing.
	 * </p>
	 * @param json the JSON string
	 * @return chainer object for completing the parse operation
	 * @see JCardReader
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
	 */
	public static ParserChainJsonString parseJson(String json) {
		return new ParserChainJsonString(json);
	}

	/**
	 * <p>
	 * Parses JSON-encoded vCards (jCard).
	 * </p>
	 * <p>
	 * Use {@link JCardReader} for more control over the parsing.
	 * </p>
	 * @param file the JSON file
	 * @return chainer object for completing the parse operation
	 * @throws FileNotFoundException if the file does not exist or cannot be
	 * accessed
	 * @see JCardReader
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
	 */
	public static ParserChainJsonReader parseJson(File file) throws FileNotFoundException {
		return parseJson(new FileReader(file));
	}

	/**
	 * <p>
	 * Parses JSON-encoded vCards (jCard).
	 * </p>
	 * <p>
	 * Use {@link JCardReader} for more control over the parsing.
	 * </p>
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 * @see JCardReader
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
	 */
	public static ParserChainJsonReader parseJson(InputStream in) {
		return parseJson(new InputStreamReader(in));
	}

	/**
	 * <p>
	 * Parses JSON-encoded vCards (jCard).
	 * </p>
	 * <p>
	 * Use {@link JCardReader} for more control over the parsing.
	 * </p>
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 * @see JCardReader
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
	 */
	public static ParserChainJsonReader parseJson(Reader reader) {
		return new ParserChainJsonReader(reader);
	}

	/**
	 * <p>
	 * Marshals a vCard to its traditional, plain-text representation.
	 * </p>
	 * 
	 * <p>
	 * Use {@link VCardWriter} for more control over how the vCard is written.
	 * </p>
	 * @param vcard the vCard to marshal
	 * @return chainer object for completing the write operation
	 * @see VCardWriter
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static WriterChainTextSingle write(VCard vcard) {
		return new WriterChainTextSingle(vcard);
	}

	/**
	 * <p>
	 * Marshals multiple vCards to their traditional, plain-text representation.
	 * </p>
	 * 
	 * <p>
	 * Use {@link VCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see VCardWriter
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static WriterChainTextMulti write(VCard... vcards) {
		return write(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals multiple vCards to their traditional, plain-text representation.
	 * </p>
	 * 
	 * <p>
	 * Use {@link VCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see VCardWriter
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static WriterChainTextMulti write(Collection<VCard> vcards) {
		return new WriterChainTextMulti(vcards);
	}

	/**
	 * <p>
	 * Marshals a vCard to its XML representation (xCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link XCardDocument} for more control over how the vCard is written.
	 * </p>
	 * @param vcard the vCard to marshal
	 * @return chainer object for completing the write operation
	 * @see XCardDocument
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static WriterChainXmlSingle writeXml(VCard vcard) {
		return new WriterChainXmlSingle(vcard);
	}

	/**
	 * <p>
	 * Marshals multiple vCards to their XML representation (xCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link XCardDocument} for more control over how the vCards are
	 * written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see XCardDocument
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static WriterChainXmlMulti writeXml(VCard... vcards) {
		return writeXml(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals multiple vCards to their XML representation (xCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link XCardDocument} for more control over how the vCards are
	 * written.
	 * </p>
	 * @param vcards the vCard to marshal
	 * @return chainer object for completing the write operation
	 * @see XCardDocument
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static WriterChainXmlMulti writeXml(Collection<VCard> vcards) {
		return new WriterChainXmlMulti(vcards);
	}

	/**
	 * <p>
	 * Marshals one or more vCards their HTML representation (hCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link HCardPage} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see HCardPage
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static WriterChainHtml writeHtml(VCard... vcards) {
		return writeHtml(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards their HTML representation (hCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link HCardPage} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see HCardPage
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static WriterChainHtml writeHtml(Collection<VCard> vcards) {
		return new WriterChainHtml(vcards);
	}

	/**
	 * <p>
	 * Marshals a vCard to its JSON representation (jCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link JCardWriter} for more control over how the vCard is written.
	 * </p>
	 * @param vcard the vCard to marshal
	 * @return chainer object for completing the write operation
	 * @see JCardWriter
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
	 */
	public static WriterChainJsonSingle writeJson(VCard vcard) {
		return new WriterChainJsonSingle(vcard);
	}

	/**
	 * <p>
	 * Marshals multiple vCards to their JSON representation (jCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link JCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see JCardWriter
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
	 */
	public static WriterChainJsonMulti writeJson(VCard... vcards) {
		return writeJson(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals multiple vCards to their JSON representation (jCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link JCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see JCardWriter
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
	 */
	public static WriterChainJsonMulti writeJson(Collection<VCard> vcards) {
		return new WriterChainJsonMulti(vcards);
	}

	static abstract class ParserChain<T, U extends IParser> {
		final List<Class<? extends VCardType>> extendedTypes = new ArrayList<Class<? extends VCardType>>();
		List<List<String>> warnings;

		/**
		 * Registers an extended type class.
		 * @param typeClass the extended type class
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T register(Class<? extends VCardType> typeClass) {
			extendedTypes.add(typeClass);
			return (T) this;
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
		@SuppressWarnings("unchecked")
		public T warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return (T) this;
		}

		/**
		 * Creates the parser.
		 * @return the parser object
		 */
		abstract U init() throws IOException, SAXException;

		U ready() throws IOException, SAXException {
			U parser = init();
			for (Class<? extends VCardType> extendedType : extendedTypes) {
				parser.registerExtendedType(extendedType);
			}
			return parser;
		}

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

	}

	static abstract class ParserChainText<T> extends ParserChain<T, VCardReader> {
		boolean caretDecoding = true;

		/**
		 * Sets whether the reader will decode characters in parameter values
		 * that use circumflex accent encoding (enabled by default).
		 * 
		 * @param enable true to use circumflex accent decoding, false not to
		 * @see VCardReader#setCaretDecodingEnabled(boolean)
		 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
		 */
		@SuppressWarnings("unchecked")
		public T caretDecoding(boolean enable) {
			caretDecoding = enable;
			return (T) this;
		}

		@Override
		VCardReader ready() throws IOException, SAXException {
			VCardReader parser = super.ready();
			parser.setCaretDecodingEnabled(caretDecoding);
			return parser;
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
	public static class ParserChainTextReader extends ParserChainText<ParserChainTextReader> {
		private Reader reader;

		private ParserChainTextReader(Reader reader) {
			this.reader = reader;
		}

		@Override
		public ParserChainTextReader register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainTextReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		public ParserChainTextReader caretDecoding(boolean enable) {
			return super.caretDecoding(enable);
		}

		@Override
		VCardReader init() {
			return new VCardReader(reader);
		}
	}

	/**
	 * Convenience chainer class for parsing plain text vCards.
	 */
	public static class ParserChainTextString extends ParserChainText<ParserChainTextString> {
		private String text;

		private ParserChainTextString(String text) {
			this.text = text;
		}

		@Override
		public ParserChainTextString register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainTextString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		public ParserChainTextString caretDecoding(boolean enable) {
			return super.caretDecoding(enable);
		}

		@Override
		VCardReader init() {
			return new VCardReader(text);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	static abstract class ParserChainXml<T> extends ParserChain<T, XCardReader> {
		//nothing
	}

	/**
	 * Convenience chainer class for parsing XML vCards.
	 */
	public static class ParserChainXmlReader extends ParserChainXml<ParserChainXmlReader> {
		private Reader reader;

		private ParserChainXmlReader(Reader reader) {
			this.reader = reader;
		}

		@Override
		public ParserChainXmlReader register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainXmlReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCardReader init() throws IOException, SAXException {
			return new XCardReader(reader);
		}
	}

	/**
	 * Convenience chainer class for parsing XML vCards.
	 */
	public static class ParserChainXmlString extends ParserChainXml<ParserChainXmlString> {
		private String xml;

		private ParserChainXmlString(String xml) {
			this.xml = xml;
		}

		@Override
		public ParserChainXmlString register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainXmlString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCardReader init() throws SAXException {
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
	 * Convenience chainer class for parsing XML vCards.
	 */
	public static class ParserChainXmlDom extends ParserChainXml<ParserChainXmlDom> {
		private Document document;

		private ParserChainXmlDom(Document document) {
			this.document = document;
		}

		@Override
		public ParserChainXmlDom register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainXmlDom warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCardReader init() {
			return new XCardReader(document);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from DOM
			} catch (SAXException e) {
				//reading from DOM
			}
			return null;
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from DOM
			} catch (SAXException e) {
				//reading from DOM
			}
			return null;
		}
	}

	static abstract class ParserChainHtml<T> extends ParserChain<T, HCardReader> {
		String pageUrl;

		/**
		 * Sets the original URL of the webpage. This is used to resolve
		 * relative links and to set the SOURCE property on the vCard. Setting
		 * this property has no effect if reading from a {@link URL}.
		 * @param pageUrl the webpage URL
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T pageUrl(String pageUrl) {
			this.pageUrl = pageUrl;
			return (T) this;
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
	public static class ParserChainHtmlReader extends ParserChainHtml<ParserChainHtmlReader> {
		private Reader reader;
		private URL url;

		private ParserChainHtmlReader(Reader reader) {
			this.reader = reader;
		}

		private ParserChainHtmlReader(URL url) {
			this.url = url;
		}

		@Override
		public ParserChainHtmlReader register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainHtmlReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		public ParserChainHtmlReader pageUrl(String pageUrl) {
			return super.pageUrl(pageUrl);
		}

		@Override
		HCardReader init() throws IOException {
			return (url == null) ? new HCardReader(reader, pageUrl) : new HCardReader(url);
		}
	}

	/**
	 * Convenience chainer class for parsing HTML vCards.
	 */
	public static class ParserChainHtmlString extends ParserChainHtml<ParserChainHtmlString> {
		private String html;

		private ParserChainHtmlString(String html) {
			this.html = html;
		}

		@Override
		HCardReader init() {
			return new HCardReader(html, pageUrl);
		}

		@Override
		public ParserChainHtmlString register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainHtmlString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		public ParserChainHtmlString pageUrl(String pageUrl) {
			return super.pageUrl(pageUrl);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	static abstract class ParserChainJson<T> extends ParserChain<T, JCardReader> {
		@Override
		JCardReader ready() throws IOException, SAXException {
			JCardReader parser = super.ready();
			return parser;
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
	 * Convenience chainer class for parsing JSON-encoded vCards (jCard).
	 */
	public static class ParserChainJsonReader extends ParserChainJson<ParserChainJsonReader> {
		private Reader reader;

		private ParserChainJsonReader(Reader reader) {
			this.reader = reader;
		}

		@Override
		public ParserChainJsonReader register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainJsonReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		JCardReader init() {
			return new JCardReader(reader);
		}
	}

	/**
	 * Convenience chainer class for parsing JSON-encoded vCards (jCard).
	 */
	public static class ParserChainJsonString extends ParserChainJson<ParserChainJsonString> {
		private String json;

		private ParserChainJsonString(String json) {
			this.json = json;
		}

		@Override
		public ParserChainJsonString register(Class<? extends VCardType> typeClass) {
			return super.register(typeClass);
		}

		@Override
		public ParserChainJsonString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		JCardReader init() {
			return new JCardReader(json);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	static abstract class WriterChain {
		final Collection<VCard> vcards;

		WriterChain(Collection<VCard> vcards) {
			this.vcards = vcards;
		}
	}

	static abstract class WriterChainText<T> extends WriterChain {
		VCardVersion version;
		boolean prodId = true;
		boolean caretEncoding = false;

		WriterChainText(Collection<VCard> vcards) {
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
		@SuppressWarnings("unchecked")
		public T version(VCardVersion version) {
			this.version = version;
			return (T) this;
		}

		/**
		 * Sets whether or not to add a PRODID type to each vCard, saying that
		 * the vCard was generated by this library. For 2.1 vCards, the extended
		 * type X-PRODID is used, since PRODID is not supported by that version.
		 * @param include true to add PRODID (default), false not to
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T prodId(boolean include) {
			this.prodId = include;
			return (T) this;
		}

		/**
		 * Sets whether the writer will use circumflex accent encoding for vCard
		 * 3.0 and 4.0 parameter values (disabled by default).
		 * 
		 * @param enable true to use circumflex accent encoding, false not to
		 * @see VCardWriter#setCaretEncodingEnabled(boolean)
		 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
		 */
		@SuppressWarnings("unchecked")
		public T caretEncoding(boolean enable) {
			this.caretEncoding = enable;
			return (T) this;
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
			vcardWriter.setCaretEncodingEnabled(caretEncoding);

			for (VCard vcard : vcards) {
				if (version == null) {
					VCardVersion vcardVersion = vcard.getVersion();
					vcardWriter.setTargetVersion((vcardVersion == null) ? VCardVersion.V3_0 : vcardVersion);
				}
				vcardWriter.write(vcard);
				addWarnings(vcardWriter.getWarnings());
			}
		}

		abstract void addWarnings(List<String> warnings);
	}

	/**
	 * Convenience chainer class for writing plain text vCards
	 */
	public static class WriterChainTextMulti extends WriterChainText<WriterChainTextMulti> {
		private List<List<String>> warnings;

		private WriterChainTextMulti(Collection<VCard> vcards) {
			super(vcards);
		}

		@Override
		public WriterChainTextMulti version(VCardVersion version) {
			return super.version(version);
		}

		@Override
		public WriterChainTextMulti prodId(boolean include) {
			return super.prodId(include);
		}

		@Override
		public WriterChainTextMulti caretEncoding(boolean enable) {
			return super.caretEncoding(enable);
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of each marshalled vCard. Each element of the list is the
		 * list of warnings for one of the marshalled vCards. Therefore, the
		 * size of this list will be equal to the number of written vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public WriterChainTextMulti warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.add(warnings);
			}
		}
	}

	/**
	 * Convenience chainer class for writing plain text vCards
	 */
	public static class WriterChainTextSingle extends WriterChainText<WriterChainTextSingle> {
		private List<String> warnings;

		private WriterChainTextSingle(VCard vcard) {
			super(Arrays.asList(vcard));
		}

		@Override
		public WriterChainTextSingle version(VCardVersion version) {
			return super.version(version);
		}

		@Override
		public WriterChainTextSingle prodId(boolean include) {
			return super.prodId(include);
		}

		@Override
		public WriterChainTextSingle caretEncoding(boolean enable) {
			return super.caretEncoding(enable);
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of the marshalled vCard.
		 * @return this
		 */
		public WriterChainTextSingle warnings(List<String> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.addAll(warnings);
			}
		}
	}

	static abstract class WriterChainXml<T> extends WriterChain {
		boolean prodId = true;
		int indent = -1;

		WriterChainXml(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * Sets whether or not to add a PRODID type to each vCard, saying that
		 * the vCard was generated by this library.
		 * @param include true to add PRODID (default), false not to
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T prodId(boolean include) {
			this.prodId = include;
			return (T) this;
		}

		/**
		 * Sets the number of indent spaces to use for pretty-printing. If not
		 * set, then the XML will not be pretty-printed.
		 * @param indent the number of spaces in the indent string
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T indent(int indent) {
			this.indent = indent;
			return (T) this;
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
				addWarnings(doc.getWarnings());
			}

			return doc;
		}

		abstract void addWarnings(List<String> warnings);
	}

	/**
	 * Convenience chainer class for writing XML vCards (xCard).
	 */
	public static class WriterChainXmlMulti extends WriterChainXml<WriterChainXmlMulti> {
		private List<List<String>> warnings;

		private WriterChainXmlMulti(Collection<VCard> vcards) {
			super(vcards);
		}

		@Override
		public WriterChainXmlMulti prodId(boolean include) {
			return super.prodId(include);
		}

		@Override
		public WriterChainXmlMulti indent(int indent) {
			return super.indent(indent);
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of each marshalled vCard. Each element of the list is the
		 * list of warnings for one of the marshalled vCards. Therefore, the
		 * size of this list will be equal to the number of written vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public WriterChainXmlMulti warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.add(warnings);
			}
		}
	}

	/**
	 * Convenience chainer class for writing XML vCards (xCard).
	 */
	public static class WriterChainXmlSingle extends WriterChainXml<WriterChainXmlSingle> {
		private List<String> warnings;

		private WriterChainXmlSingle(VCard vcard) {
			super(Arrays.asList(vcard));
		}

		@Override
		public WriterChainXmlSingle prodId(boolean include) {
			return super.prodId(include);
		}

		@Override
		public WriterChainXmlSingle indent(int indent) {
			return super.indent(indent);
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of each marshalled vCard.
		 * @return this
		 */
		public WriterChainXmlSingle warnings(List<String> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.addAll(warnings);
			}
		}
	}

	/**
	 * Convenience chainer class for writing HTML vCards (hCard).
	 */
	public static class WriterChainHtml extends WriterChain {
		private WriterChainHtml(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * Writes the hCards to a string.
		 * @return the HTML page
		 */
		public String go() {
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
		 */
		public void go(OutputStream out) throws IOException {
			go(new OutputStreamWriter(out));
		}

		/**
		 * Writes the hCards to a file.
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
		 * Writes the hCards to a writer.
		 * @param writer the writer to write to
		 * @throws IOException if there's a problem writing to the writer
		 */
		public void go(Writer writer) throws IOException {
			HCardPage page = new HCardPage();
			for (VCard vcard : vcards) {
				page.addVCard(vcard);
			}
			page.write(writer);
		}
	}

	static abstract class WriterChainJson<T> extends WriterChain {
		boolean prodId = true;
		boolean indent = false;

		WriterChainJson(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * Sets whether or not to add a PRODID type to each vCard, saying that
		 * the vCard was generated by this library.
		 * @param include true to add PRODID (default), false not to
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T prodId(boolean include) {
			this.prodId = include;
			return (T) this;
		}

		/**
		 * Sets whether or not to pretty-print the JSON.
		 * @param indent true to pretty-print it, false not to (defaults to
		 * false)
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T indent(boolean indent) {
			this.indent = indent;
			return (T) this;
		}

		/**
		 * Writes the jCards to a string.
		 * @return the JSON string
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
		 * Writes the jCards to an output stream.
		 * @param out the output stream to write to
		 * @throws IOException if there's a problem writing to the output stream
		 */
		public void go(OutputStream out) throws IOException {
			go(new OutputStreamWriter(out));
		}

		/**
		 * Writes the jCards to a file.
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
		 * Writes the jCards to a writer.
		 * @param writer the writer to write to
		 * @throws IOException if there's a problem writing to the writer
		 */
		public void go(Writer writer) throws IOException {
			JCardWriter jcardWriter = new JCardWriter(writer);
			jcardWriter.setAddProdId(prodId);
			jcardWriter.setIndent(indent);
			try {
				for (VCard vcard : vcards) {
					jcardWriter.write(vcard);
					addWarnings(jcardWriter.getWarnings());
				}
			} finally {
				jcardWriter.endJsonStream();
			}
		}

		abstract void addWarnings(List<String> warnings);
	}

	/**
	 * Convenience chainer class for writing JSON-encoded vCards (jCard).
	 */
	public static class WriterChainJsonMulti extends WriterChainJson<WriterChainJsonMulti> {
		private List<List<String>> warnings;

		private WriterChainJsonMulti(Collection<VCard> vcards) {
			super(vcards);
		}

		@Override
		public WriterChainJsonMulti prodId(boolean include) {
			return super.prodId(include);
		}

		@Override
		public WriterChainJsonMulti indent(boolean indent) {
			return super.indent(indent);
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of each marshalled vCard. Each element of the list is the
		 * list of warnings for one of the marshalled vCards. Therefore, the
		 * size of this list will be equal to the number of written vCards. If a
		 * vCard does not have any warnings, then its warning list will be
		 * empty.
		 * @return this
		 */
		public WriterChainJsonMulti warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.add(warnings);
			}
		}
	}

	/**
	 * Convenience chainer class for writing JSON-encoded vCards (jCard).
	 */
	public static class WriterChainJsonSingle extends WriterChainJson<WriterChainJsonSingle> {
		private List<String> warnings;

		private WriterChainJsonSingle(VCard vcard) {
			super(Arrays.asList(vcard));
		}

		@Override
		public WriterChainJsonSingle prodId(boolean include) {
			return super.prodId(include);
		}

		@Override
		public WriterChainJsonSingle indent(boolean indent) {
			return super.indent(indent);
		}

		/**
		 * Provides a list object that any marshal warnings will be put into.
		 * Warnings usually occur when there is a property in the VCard that is
		 * not supported by the version to which the vCard is being marshalled.
		 * @param warnings the list object that will be populated with the
		 * warnings of the marshalled vCard.
		 * @return this
		 */
		public WriterChainJsonSingle warnings(List<String> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.addAll(warnings);
			}
		}
	}

	private Ezvcard() {
		//hide
	}
}
