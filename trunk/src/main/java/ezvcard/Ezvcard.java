package ezvcard;

import java.io.File;
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

import com.fasterxml.jackson.core.JsonParseException;

import ezvcard.io.StreamReader;
import ezvcard.io.html.HCardPage;
import ezvcard.io.html.HCardParser;
import ezvcard.io.json.JCardParseException;
import ezvcard.io.json.JCardReader;
import ezvcard.io.json.JCardWriter;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import ezvcard.io.xml.XCardDocument;
import ezvcard.io.xml.XCardDocument.XCardDocumentStreamWriter;
import ezvcard.io.xml.XCardReader;
import ezvcard.io.xml.XCardWriter;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;

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
 * Contains chaining factory methods for parsing/writing vCards. They are
 * convenience methods that make use of the following classes:
 * </p>
 * 
 * 
 * <table class="simpleTable">
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
 * <td>{@link XCardDocument}, {@link XCardReader}</td>
 * <td>{@link XCardDocument}, {@link XCardWriter}</td>
 * </tr>
 * <tr>
 * <th>HTML</th>
 * <td>{@link HCardParser}</td>
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
	 * @see VCardReader
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public static ParserChainTextReader parse(File file) {
		return new ParserChainTextReader(file);
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
	 * Parses XML-encoded vCards (xCard) from a string.
	 * </p>
	 * <p>
	 * Use {@link XCardDocument} or {@link XCardReader} for more control over
	 * the parsing.
	 * </p>
	 * @param xml the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardDocument
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static ParserChainXmlString parseXml(String xml) {
		return new ParserChainXmlString(xml);
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard) from a file.
	 * </p>
	 * <p>
	 * Use {@link XCardDocument} or {@link XCardReader} for more control over
	 * the parsing.
	 * </p>
	 * @param file the XML file
	 * @return chainer object for completing the parse operation
	 * @see XCardDocument
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static ParserChainXmlReader parseXml(File file) {
		return new ParserChainXmlReader(file);
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard) from an input stream.
	 * </p>
	 * <p>
	 * Use {@link XCardDocument} or {@link XCardReader} for more control over
	 * the parsing.
	 * </p>
	 * @param in the input stream to the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardDocument
	 * @see XCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static ParserChainXmlReader parseXml(InputStream in) {
		return new ParserChainXmlReader(in);
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard) from a reader.
	 * </p>
	 * <p>
	 * Note that use of this method is discouraged. It ignores the character
	 * encoding that is defined within the XML document itself, and should only
	 * be used if the encoding is undefined or if the encoding needs to be
	 * ignored for whatever reason. The {@link #parseXml(InputStream)} method
	 * should be used instead, since it takes the XML document's character
	 * encoding into account when parsing.
	 * </p>
	 * <p>
	 * Use {@link XCardDocument} or {@link XCardReader} for more control over
	 * the parsing.
	 * </p>
	 * @param reader the reader to the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardDocument
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
	 * Use {@link XCardDocument} or {@link XCardReader} for more control over
	 * the parsing.
	 * </p>
	 * @param document the XML document
	 * @return chainer object for completing the parse operation
	 * @see XCardDocument
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
	 * Use {@link HCardParser} for more control over the parsing.
	 * </p>
	 * @param html the HTML page
	 * @return chainer object for completing the parse operation
	 * @see HCardParser
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
	 * Use {@link HCardParser} for more control over the parsing.
	 * </p>
	 * @param file the HTML file
	 * @return chainer object for completing the parse operation
	 * @see HCardParser
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static ParserChainHtmlReader parseHtml(File file) {
		return new ParserChainHtmlReader(file);
	}

	/**
	 * <p>
	 * Parses HTML-encoded vCards (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardParser} for more control over the parsing.
	 * </p>
	 * @param in the input stream to the HTML page
	 * @return chainer object for completing the parse operation
	 * @see HCardParser
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
	 * Use {@link HCardParser} for more control over the parsing.
	 * </p>
	 * @param reader the reader to the HTML page
	 * @return chainer object for completing the parse operation
	 * @see HCardParser
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
	 * Use {@link HCardParser} for more control over the parsing.
	 * </p>
	 * @param url the URL of the webpage
	 * @return chainer object for completing the parse operation
	 * @see HCardParser
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
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
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
	 * @see JCardReader
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public static ParserChainJsonReader parseJson(File file) {
		return new ParserChainJsonReader(file);
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
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public static ParserChainJsonReader parseJson(InputStream in) {
		return new ParserChainJsonReader(in);
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
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public static ParserChainJsonReader parseJson(Reader reader) {
		return new ParserChainJsonReader(reader);
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their traditional, plain-text
	 * representation.
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
	public static WriterChainText write(VCard... vcards) {
		return write(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their traditional, plain-text
	 * representation.
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
	public static WriterChainText write(Collection<VCard> vcards) {
		return new WriterChainText(vcards);
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their XML representation (xCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link XCardDocument} or {@link XCardWriter} for more control over
	 * how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see XCardDocument
	 * @see XCardWriter
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static WriterChainXml writeXml(VCard... vcards) {
		return writeXml(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their XML representation (xCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link XCardDocument} or {@link XCardWriter} for more control over
	 * how the vCards are written.
	 * </p>
	 * @param vcards the vCard to marshal
	 * @return chainer object for completing the write operation
	 * @see XCardDocument
	 * @see XCardWriter
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public static WriterChainXml writeXml(Collection<VCard> vcards) {
		return new WriterChainXml(vcards);
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
	 * Marshals one or more vCards to their JSON representation (jCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link JCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see JCardWriter
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public static WriterChainJson writeJson(VCard... vcards) {
		return writeJson(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their JSON representation (jCard).
	 * </p>
	 * 
	 * <p>
	 * Use {@link JCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see JCardWriter
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public static WriterChainJson writeJson(Collection<VCard> vcards) {
		return new WriterChainJson(vcards);
	}

	static abstract class ParserChain<T> {
		final ScribeIndex index = new ScribeIndex();
		List<List<String>> warnings;

		@SuppressWarnings("unchecked")
		final T this_ = (T) this;

		/**
		 * Registers a property scribe.
		 * @param scribe the scribe
		 * @return this
		 */
		public T register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			index.register(scribe);
			return this_;
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
		public T warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this_;
		}

		/**
		 * Reads the first vCard from the stream.
		 * @return the vCard or null if there are no vCards
		 * @throws IOException if there's an I/O problem
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public abstract VCard first() throws IOException, SAXException;

		/**
		 * Reads all vCards from the stream.
		 * @return the parsed vCards
		 * @throws IOException if there's an I/O problem
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public abstract List<VCard> all() throws IOException, SAXException;
	}

	static abstract class ParserChainText<T> extends ParserChain<T> {
		boolean caretDecoding = true;
		final boolean closeWhenDone;

		private ParserChainText(boolean closeWhenDone) {
			this.closeWhenDone = closeWhenDone;
		}

		/**
		 * Sets whether the reader will decode characters in parameter values
		 * that use circumflex accent encoding (enabled by default).
		 * 
		 * @param enable true to use circumflex accent decoding, false not to
		 * @return this
		 * @see VCardReader#setCaretDecodingEnabled(boolean)
		 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
		 */
		public T caretDecoding(boolean enable) {
			caretDecoding = enable;
			return this_;
		}

		@Override
		public VCard first() throws IOException {
			VCardReader parser = constructReader();

			try {
				VCard vcard = parser.readNext();
				if (warnings != null) {
					warnings.add(parser.getWarnings());
				}
				return vcard;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		@Override
		public List<VCard> all() throws IOException {
			VCardReader parser = constructReader();

			try {
				List<VCard> vcards = new ArrayList<VCard>();
				VCard vcard;
				while ((vcard = parser.readNext()) != null) {
					if (warnings != null) {
						warnings.add(parser.getWarnings());
					}
					vcards.add(vcard);
				}
				return vcards;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		private VCardReader constructReader() throws IOException {
			VCardReader parser = _constructReader();
			parser.setScribeIndex(index);
			parser.setCaretDecodingEnabled(caretDecoding);
			return parser;
		}

		abstract VCardReader _constructReader() throws IOException;
	}

	/**
	 * Chainer class for parsing plain text vCards.
	 * @see Ezvcard#parse(InputStream)
	 * @see Ezvcard#parse(File)
	 * @see Ezvcard#parse(Reader)
	 */
	public static class ParserChainTextReader extends ParserChainText<ParserChainTextReader> {
		private final Reader reader;
		private final File file;

		private ParserChainTextReader(Reader reader) {
			super(false);
			this.reader = reader;
			this.file = null;
		}

		private ParserChainTextReader(File file) {
			super(true);
			this.reader = null;
			this.file = file;
		}

		@Override
		public ParserChainTextReader register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
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
		@SuppressWarnings("resource")
		VCardReader _constructReader() throws IOException {
			return (reader != null) ? new VCardReader(reader) : new VCardReader(file);
		}
	}

	/**
	 * Chainer class for parsing plain text vCards.
	 * @see Ezvcard#parse(String)
	 */
	public static class ParserChainTextString extends ParserChainText<ParserChainTextString> {
		private final String text;

		private ParserChainTextString(String text) {
			super(false);
			this.text = text;
		}

		@Override
		public ParserChainTextString register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
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
		VCardReader _constructReader() {
			return new VCardReader(text);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}
	}

	static abstract class ParserChainXml<T> extends ParserChain<T> {
		@Override
		public VCard first() throws IOException, SAXException {
			StreamReader reader = constructStreamReader();
			VCard vcard = reader.readNext();
			if (warnings != null) {
				warnings.add(reader.getWarnings());
			}
			return vcard;
		}

		@Override
		public List<VCard> all() throws IOException, SAXException {
			List<VCard> vcards = new ArrayList<VCard>();
			StreamReader reader = constructStreamReader();
			VCard vcard = null;
			while ((vcard = reader.readNext()) != null) {
				vcards.add(vcard);
				if (warnings != null) {
					warnings.add(reader.getWarnings());
				}
			}
			return vcards;
		}

		private StreamReader constructStreamReader() throws SAXException, IOException {
			XCardDocument parser = _constructDocument();
			StreamReader reader = parser.reader();
			reader.setScribeIndex(index);
			return reader;
		}

		abstract XCardDocument _constructDocument() throws IOException, SAXException;
	}

	/**
	 * Chainer class for parsing XML vCards.
	 * @see Ezvcard#parseXml(InputStream)
	 * @see Ezvcard#parseXml(File)
	 * @see Ezvcard#parseXml(Reader)
	 */
	public static class ParserChainXmlReader extends ParserChainXml<ParserChainXmlReader> {
		private final InputStream in;
		private final File file;
		private final Reader reader;

		private ParserChainXmlReader(InputStream in) {
			this.in = in;
			this.reader = null;
			this.file = null;
		}

		private ParserChainXmlReader(File file) {
			this.in = null;
			this.reader = null;
			this.file = file;
		}

		private ParserChainXmlReader(Reader reader) {
			this.in = null;
			this.reader = reader;
			this.file = null;
		}

		@Override
		public ParserChainXmlReader register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
		}

		@Override
		public ParserChainXmlReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCardDocument _constructDocument() throws IOException, SAXException {
			if (in != null) {
				return new XCardDocument(in);
			}
			if (file != null) {
				return new XCardDocument(file);
			}
			return new XCardDocument(reader);
		}
	}

	/**
	 * Chainer class for parsing XML vCards.
	 * @see Ezvcard#parseXml(String)
	 */
	public static class ParserChainXmlString extends ParserChainXml<ParserChainXmlString> {
		private final String xml;

		private ParserChainXmlString(String xml) {
			this.xml = xml;
		}

		@Override
		public ParserChainXmlString register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
		}

		@Override
		public ParserChainXmlString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCardDocument _constructDocument() throws SAXException {
			return new XCardDocument(xml);
		}

		@Override
		public VCard first() throws SAXException {
			try {
				return super.first();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<VCard> all() throws SAXException {
			try {
				return super.all();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Chainer class for parsing XML vCards.
	 * @see Ezvcard#parseXml(Document)
	 */
	public static class ParserChainXmlDom extends ParserChainXml<ParserChainXmlDom> {
		private final Document document;

		private ParserChainXmlDom(Document document) {
			this.document = document;
		}

		@Override
		public ParserChainXmlDom register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
		}

		@Override
		public ParserChainXmlDom warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCardDocument _constructDocument() {
			return new XCardDocument(document);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//should never be thrown because we're reading from a DOM
				throw new RuntimeException(e);
			} catch (SAXException e) {
				//should never be thrown because we're reading from a DOM
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//should never be thrown because we're reading from a DOM
				throw new RuntimeException(e);
			} catch (SAXException e) {
				//should never be thrown because we're reading from a DOM
				throw new RuntimeException(e);
			}
		}
	}

	static abstract class ParserChainHtml<T> extends ParserChain<T> {
		String pageUrl;

		/**
		 * Sets the original URL of the webpage. This is used to resolve
		 * relative links and to set the SOURCE property on the vCard. Setting
		 * this property has no effect if reading from a {@link URL}.
		 * @param pageUrl the webpage URL
		 * @return this
		 */
		public T pageUrl(String pageUrl) {
			this.pageUrl = pageUrl;
			return this_;
		}

		@Override
		public VCard first() throws IOException {
			HCardParser parser = constructReader();

			VCard vcard = parser.readNext();
			if (warnings != null) {
				warnings.add(parser.getWarnings());
			}
			return vcard;
		}

		@Override
		public List<VCard> all() throws IOException {
			HCardParser parser = constructReader();
			List<VCard> vcards = new ArrayList<VCard>();
			VCard vcard = null;
			while ((vcard = parser.readNext()) != null) {
				vcards.add(vcard);
				if (warnings != null) {
					warnings.add(parser.getWarnings());
				}
			}
			return vcards;
		}

		private HCardParser constructReader() throws IOException {
			HCardParser parser = _constructReader();
			parser.setScribeIndex(index);
			return parser;
		}

		abstract HCardParser _constructReader() throws IOException;
	}

	/**
	 * Chainer class for parsing HTML vCards.
	 * @see Ezvcard#parseHtml(InputStream)
	 * @see Ezvcard#parseHtml(File)
	 * @see Ezvcard#parseHtml(Reader)
	 */
	public static class ParserChainHtmlReader extends ParserChainHtml<ParserChainHtmlReader> {
		private final Reader reader;
		private final File file;
		private final URL url;

		private ParserChainHtmlReader(Reader reader) {
			this.reader = reader;
			this.file = null;
			this.url = null;
		}

		private ParserChainHtmlReader(File file) {
			this.reader = null;
			this.file = file;
			this.url = null;
		}

		private ParserChainHtmlReader(URL url) {
			this.reader = null;
			this.file = null;
			this.url = url;
		}

		@Override
		public ParserChainHtmlReader register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
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
		HCardParser _constructReader() throws IOException {
			if (reader != null) {
				return new HCardParser(reader, pageUrl);
			}

			if (file != null) {
				//Jsoup (presumably) closes the FileReader it creates
				return new HCardParser(file, pageUrl);
			}

			return new HCardParser(url);
		}
	}

	/**
	 * Chainer class for parsing HTML vCards.
	 * @see Ezvcard#parseHtml(String)
	 */
	public static class ParserChainHtmlString extends ParserChainHtml<ParserChainHtmlString> {
		private final String html;

		private ParserChainHtmlString(String html) {
			this.html = html;
		}

		@Override
		public ParserChainHtmlString register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
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
		HCardParser _constructReader() {
			return new HCardParser(html, pageUrl);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}
	}

	static abstract class ParserChainJson<T> extends ParserChain<T> {
		final boolean closeWhenDone;

		private ParserChainJson(boolean closeWhenDone) {
			this.closeWhenDone = closeWhenDone;
		}

		/**
		 * @throws JCardParseException if the jCard syntax is incorrect (the
		 * JSON syntax may be valid, but it is not in the correct jCard format).
		 * @throws JsonParseException if the JSON syntax is incorrect
		 */
		@Override
		public VCard first() throws IOException {
			JCardReader parser = constructReader();

			try {
				VCard vcard = parser.readNext();
				if (warnings != null) {
					warnings.add(parser.getWarnings());
				}
				return vcard;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		/**
		 * @throws JCardParseException if the jCard syntax is incorrect (the
		 * JSON syntax may be valid, but it is not in the correct jCard format).
		 * @throws JsonParseException if the JSON syntax is incorrect
		 */
		@Override
		public List<VCard> all() throws IOException {
			JCardReader parser = constructReader();

			try {
				List<VCard> vcards = new ArrayList<VCard>();
				VCard vcard;
				while ((vcard = parser.readNext()) != null) {
					if (warnings != null) {
						warnings.add(parser.getWarnings());
					}
					vcards.add(vcard);
				}
				return vcards;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		private JCardReader constructReader() throws IOException {
			JCardReader parser = _constructReader();
			parser.setScribeIndex(index);
			return parser;
		}

		abstract JCardReader _constructReader() throws IOException;
	}

	/**
	 * Chainer class for parsing JSON-encoded vCards (jCard).
	 * @see Ezvcard#parseJson(InputStream)
	 * @see Ezvcard#parseJson(File)
	 * @see Ezvcard#parseJson(Reader)
	 */
	public static class ParserChainJsonReader extends ParserChainJson<ParserChainJsonReader> {
		private final InputStream in;
		private final File file;
		private final Reader reader;

		private ParserChainJsonReader(InputStream in) {
			super(false);
			this.in = in;
			this.reader = null;
			this.file = null;
		}

		private ParserChainJsonReader(File file) {
			super(true);
			this.in = null;
			this.reader = null;
			this.file = file;
		}

		private ParserChainJsonReader(Reader reader) {
			super(false);
			this.in = null;
			this.reader = reader;
			this.file = null;
		}

		@Override
		public ParserChainJsonReader register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
		}

		@Override
		public ParserChainJsonReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		JCardReader _constructReader() throws IOException {
			if (in != null) {
				return new JCardReader(in);
			}
			if (file != null) {
				return new JCardReader(file);
			}
			return new JCardReader(reader);
		}
	}

	/**
	 * Chainer class for parsing JSON-encoded vCards (jCard).
	 * @see Ezvcard#parseJson(String)
	 */
	public static class ParserChainJsonString extends ParserChainJson<ParserChainJsonString> {
		private final String json;

		private ParserChainJsonString(String json) {
			super(false);
			this.json = json;
		}

		@Override
		public ParserChainJsonString register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			return super.register(scribe);
		}

		@Override
		public ParserChainJsonString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		JCardReader _constructReader() {
			return new JCardReader(json);
		}

		@Override
		public VCard first() {
			try {
				return super.first();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<VCard> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//should never be thrown because we're reading from a string
				throw new RuntimeException(e);
			}
		}
	}

	static abstract class WriterChain<T> {
		final Collection<VCard> vcards;

		@SuppressWarnings("unchecked")
		final T this_ = (T) this;

		WriterChain(Collection<VCard> vcards) {
			this.vcards = vcards;
		}
	}

	/**
	 * Chainer class for writing plain text vCards
	 * @see Ezvcard#write(Collection)
	 * @see Ezvcard#write(VCard...)
	 */
	public static class WriterChainText extends WriterChain<WriterChainText> {
		VCardVersion version;
		boolean prodId = true;
		boolean versionStrict = true;
		boolean caretEncoding = false;
		final ScribeIndex index = new ScribeIndex();

		private WriterChainText(Collection<VCard> vcards) {
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
		public WriterChainText version(VCardVersion version) {
			this.version = version;
			return this_;
		}

		/**
		 * Sets whether or not to add a PRODID property to each vCard, saying
		 * that the vCard was generated by this library. For 2.1 vCards, the
		 * extended property X-PRODID is used, since PRODID is not supported by
		 * that version.
		 * @param include true to add PRODID (default), false not to
		 * @return this
		 */
		public WriterChainText prodId(boolean include) {
			this.prodId = include;
			return this_;
		}

		/**
		 * Sets whether the writer will use circumflex accent encoding for vCard
		 * 3.0 and 4.0 parameter values (disabled by default).
		 * @param enable true to use circumflex accent encoding, false not to
		 * @return this
		 * @see VCardWriter#setCaretEncodingEnabled(boolean)
		 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
		 */
		public WriterChainText caretEncoding(boolean enable) {
			this.caretEncoding = enable;
			return this_;
		}

		/**
		 * Sets whether properties that do not support the target version will
		 * be excluded from the written vCard.
		 * @param versionStrict true to exclude properties that do not support
		 * the target version, false to include them anyway (defaults to true)
		 * @return this
		 */
		public WriterChainText versionStrict(boolean versionStrict) {
			this.versionStrict = versionStrict;
			return this_;
		}

		/**
		 * Registers a property scribe.
		 * @param scribe the scribe to register
		 * @return this
		 */
		public WriterChainText register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			index.register(scribe);
			return this_;
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
			VCardWriter vcardWriter = new VCardWriter(out, version);
			go(vcardWriter);
		}

		/**
		 * Writes the vCards to a file. If the file exists, it will be
		 * overwritten.
		 * @param file the file to write to
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException {
			go(file, false);
		}

		/**
		 * Writes the vCards to a file.
		 * @param file the file to write to
		 * @param append true to append onto the end of the file, false to
		 * overwrite it
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file, boolean append) throws IOException {
			VCardWriter vcardWriter = new VCardWriter(file, append, version);
			try {
				go(vcardWriter);
			} finally {
				IOUtils.closeQuietly(vcardWriter);
			}
		}

		/**
		 * Writes the vCards to a writer.
		 * @param writer the writer to write to
		 * @throws IOException if there's a problem writing to the writer
		 */
		public void go(Writer writer) throws IOException {
			VCardWriter vcardWriter = new VCardWriter(writer, version);
			go(vcardWriter);
		}

		private void go(VCardWriter vcardWriter) throws IOException {
			vcardWriter.setAddProdId(prodId);
			vcardWriter.setCaretEncodingEnabled(caretEncoding);
			vcardWriter.setVersionStrict(versionStrict);
			vcardWriter.setScribeIndex(index);

			for (VCard vcard : vcards) {
				if (version == null) {
					VCardVersion vcardVersion = vcard.getVersion();
					if (vcardVersion == null) {
						vcardVersion = VCardVersion.V3_0;
					}
					vcardWriter.setTargetVersion(vcardVersion);
				}
				vcardWriter.write(vcard);
				vcardWriter.flush();
			}
		}
	}

	/**
	 * Chainer class for writing XML vCards (xCard).
	 * @see Ezvcard#writeXml(Collection)
	 * @see Ezvcard#writeXml(VCard...)
	 */
	public static class WriterChainXml extends WriterChain<WriterChainXml> {
		boolean prodId = true;
		boolean versionStrict = true;
		int indent = -1;
		final ScribeIndex index = new ScribeIndex();

		private WriterChainXml(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * Sets whether or not to add a PRODID property to each vCard, saying
		 * that the vCard was generated by this library.
		 * @param include true to add PRODID (default), false not to
		 * @return this
		 */
		public WriterChainXml prodId(boolean include) {
			this.prodId = include;
			return this_;
		}

		/**
		 * Sets the number of indent spaces to use for pretty-printing. If not
		 * set, then the XML will not be pretty-printed.
		 * @param indent the number of spaces in the indent string
		 * @return this
		 */
		public WriterChainXml indent(int indent) {
			this.indent = indent;
			return this_;
		}

		/**
		 * Sets whether properties that do not support xCard (vCard version 4.0)
		 * will be excluded from the written vCard.
		 * @param versionStrict true to exclude properties that do not support
		 * xCard, false to include them anyway (defaults to true)
		 * @return this
		 */
		public WriterChainXml versionStrict(boolean versionStrict) {
			this.versionStrict = versionStrict;
			return this_;
		}

		/**
		 * Registers a property scribe.
		 * @param scribe the scribe to register
		 * @return this
		 */
		public WriterChainXml register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			index.register(scribe);
			return this_;
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
			XCardDocument doc = createXCardDocument();
			doc.write(out, indent);
		}

		/**
		 * Writes the xCards to a file.
		 * @param file the file to write to
		 * @throws IOException if the file can't be opened
		 * @throws TransformerException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException, TransformerException {
			XCardDocument doc = createXCardDocument();
			doc.write(file, indent);
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
			XCardDocumentStreamWriter writer = doc.writer();
			writer.setAddProdId(prodId);
			writer.setVersionStrict(versionStrict);
			writer.setScribeIndex(index);

			for (VCard vcard : vcards) {
				writer.write(vcard);
			}

			return doc;
		}
	}

	/**
	 * Chainer class for writing HTML vCards (hCard).
	 * @see Ezvcard#writeHtml(Collection)
	 * @see Ezvcard#writeHtml(VCard...)
	 */
	public static class WriterChainHtml extends WriterChain<WriterChainHtml> {
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
				page.add(vcard);
			}
			page.write(writer);
		}
	}

	/**
	 * Chainer class for writing JSON-encoded vCards (jCard).
	 * @see Ezvcard#writeJson(Collection)
	 * @see Ezvcard#writeJson(VCard...)
	 */
	public static class WriterChainJson extends WriterChain<WriterChainJson> {
		boolean prodId = true;
		boolean versionStrict = true;
		boolean indent = false;
		final ScribeIndex index = new ScribeIndex();

		private WriterChainJson(Collection<VCard> vcards) {
			super(vcards);
		}

		/**
		 * Sets whether or not to add a PRODID property to each vCard, saying
		 * that the vCard was generated by this library.
		 * @param include true to add PRODID (default), false not to
		 * @return this
		 */
		public WriterChainJson prodId(boolean include) {
			this.prodId = include;
			return this_;
		}

		/**
		 * Sets whether or not to pretty-print the JSON.
		 * @param indent true to pretty-print it, false not to (defaults to
		 * false)
		 * @return this
		 */
		public WriterChainJson indent(boolean indent) {
			this.indent = indent;
			return this_;
		}

		/**
		 * Sets whether properties that do not support jCard (vCard version 4.0)
		 * will be excluded from the written vCard.
		 * @param versionStrict true to exclude properties that do not support
		 * jCard, false to include them anyway (defaults to true)
		 * @return this
		 */
		public WriterChainJson versionStrict(boolean versionStrict) {
			this.versionStrict = versionStrict;
			return this_;
		}

		/**
		 * Registers a property scribe.
		 * @param scribe the scribe to register
		 * @return this
		 */
		public WriterChainJson register(VCardPropertyScribe<? extends VCardProperty> scribe) {
			index.register(scribe);
			return this_;
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
			go(new JCardWriter(out, vcards.size() > 1));
		}

		/**
		 * Writes the jCards to a file.
		 * @param file the file to write to
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException {
			JCardWriter writer = new JCardWriter(file, vcards.size() > 1);
			try {
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
			go(new JCardWriter(writer, vcards.size() > 1));
		}

		private void go(JCardWriter writer) throws IOException {
			writer.setAddProdId(prodId);
			writer.setIndent(indent);
			writer.setVersionStrict(versionStrict);
			writer.setScribeIndex(index);
			try {
				for (VCard vcard : vcards) {
					writer.write(vcard);
					writer.flush();
				}
			} finally {
				writer.closeJsonStream();
			}
		}
	}

	private Ezvcard() {
		//hide
	}
}
