package ezvcard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.w3c.dom.Document;

import ezvcard.io.chain.ChainingHtmlParser;
import ezvcard.io.chain.ChainingHtmlStringParser;
import ezvcard.io.chain.ChainingHtmlWriter;
import ezvcard.io.chain.ChainingJsonParser;
import ezvcard.io.chain.ChainingJsonStringParser;
import ezvcard.io.chain.ChainingJsonWriter;
import ezvcard.io.chain.ChainingTextParser;
import ezvcard.io.chain.ChainingTextStringParser;
import ezvcard.io.chain.ChainingTextWriter;
import ezvcard.io.chain.ChainingXmlMemoryParser;
import ezvcard.io.chain.ChainingXmlParser;
import ezvcard.io.chain.ChainingXmlWriter;
import ezvcard.io.html.HCardPage;
import ezvcard.io.html.HCardParser;
import ezvcard.io.json.JCardReader;
import ezvcard.io.json.JCardWriter;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import ezvcard.io.xml.XCardDocument;
import ezvcard.io.xml.XCardReader;
import ezvcard.io.xml.XCardWriter;
import ezvcard.util.IOUtils;

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
 * Contains chaining factory methods for parsing/writing vCards. They are
 * convenience methods that make use of the following classes:
 * </p>
 * <table class="simpleTable">
 * <caption>Classes used by this class</caption>
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
public final class Ezvcard {
	/**
	 * The version of the library.
	 */
	public static final String VERSION;

	/**
	 * The Maven group ID.
	 */
	public static final String GROUP_ID;

	/**
	 * The Maven artifact ID.
	 */
	public static final String ARTIFACT_ID;

	/**
	 * The project webpage.
	 */
	public static final String URL;

	static {
		InputStream in = null;
		Properties props = new Properties();
		try {
			in = Ezvcard.class.getResourceAsStream("ez-vcard.properties");
			props.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}

		VERSION = props.getProperty("version");
		GROUP_ID = props.getProperty("groupId");
		ARTIFACT_ID = props.getProperty("artifactId");
		URL = props.getProperty("url");
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
	public static ChainingTextStringParser parse(String str) {
		return new ChainingTextStringParser(str);
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
	public static ChainingTextParser<ChainingTextParser<?>> parse(File file) {
		return new ChainingTextParser<ChainingTextParser<?>>(file);
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
	public static ChainingTextParser<ChainingTextParser<?>> parse(InputStream in) {
		return new ChainingTextParser<ChainingTextParser<?>>(in);
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
	public static ChainingTextParser<ChainingTextParser<?>> parse(Reader reader) {
		return new ChainingTextParser<ChainingTextParser<?>>(reader);
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
	public static ChainingXmlMemoryParser parseXml(String xml) {
		return new ChainingXmlMemoryParser(xml);
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
	public static ChainingXmlParser<ChainingXmlParser<?>> parseXml(File file) {
		return new ChainingXmlParser<ChainingXmlParser<?>>(file);
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
	public static ChainingXmlParser<ChainingXmlParser<?>> parseXml(InputStream in) {
		return new ChainingXmlParser<ChainingXmlParser<?>>(in);
	}

	/**
	 * <p>
	 * Parses XML-encoded vCards (xCard) from a reader.
	 * </p>
	 * <p>
	 * Note that use of this method is discouraged. It ignores the character
	 * encoding that is defined within the XML document itself, and should only
	 * be used if the encoding is undefined or if the encoding needs to be
	 * ignored for some reason. The {@link #parseXml(InputStream)} method should
	 * be used instead, since it takes the XML document's character encoding
	 * into account when parsing.
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
	public static ChainingXmlParser<ChainingXmlParser<?>> parseXml(Reader reader) {
		return new ChainingXmlParser<ChainingXmlParser<?>>(reader);
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
	public static ChainingXmlMemoryParser parseXml(Document document) {
		return new ChainingXmlMemoryParser(document);
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
	public static ChainingHtmlStringParser parseHtml(String html) {
		return new ChainingHtmlStringParser(html);
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
	public static ChainingHtmlParser<ChainingHtmlParser<?>> parseHtml(File file) {
		return new ChainingHtmlParser<ChainingHtmlParser<?>>(file);
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
	public static ChainingHtmlParser<ChainingHtmlParser<?>> parseHtml(InputStream in) {
		return new ChainingHtmlParser<ChainingHtmlParser<?>>(in);
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
	public static ChainingHtmlParser<ChainingHtmlParser<?>> parseHtml(Reader reader) {
		return new ChainingHtmlParser<ChainingHtmlParser<?>>(reader);
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
	public static ChainingHtmlParser<ChainingHtmlParser<?>> parseHtml(URL url) {
		return new ChainingHtmlParser<ChainingHtmlParser<?>>(url);
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
	public static ChainingJsonStringParser parseJson(String json) {
		return new ChainingJsonStringParser(json);
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
	public static ChainingJsonParser<ChainingJsonParser<?>> parseJson(File file) {
		return new ChainingJsonParser<ChainingJsonParser<?>>(file);
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
	public static ChainingJsonParser<ChainingJsonParser<?>> parseJson(InputStream in) {
		return new ChainingJsonParser<ChainingJsonParser<?>>(in);
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
	public static ChainingJsonParser<ChainingJsonParser<?>> parseJson(Reader reader) {
		return new ChainingJsonParser<ChainingJsonParser<?>>(reader);
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their traditional, plain-text
	 * representation.
	 * </p>
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
	public static ChainingTextWriter write(VCard... vcards) {
		return write(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their traditional, plain-text
	 * representation.
	 * </p>
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
	public static ChainingTextWriter write(Collection<VCard> vcards) {
		return new ChainingTextWriter(vcards);
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their XML representation (xCard).
	 * </p>
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
	public static ChainingXmlWriter writeXml(VCard... vcards) {
		return writeXml(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their XML representation (xCard).
	 * </p>
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
	public static ChainingXmlWriter writeXml(Collection<VCard> vcards) {
		return new ChainingXmlWriter(vcards);
	}

	/**
	 * <p>
	 * Marshals one or more vCards their HTML representation (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardPage} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see HCardPage
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static ChainingHtmlWriter writeHtml(VCard... vcards) {
		return writeHtml(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards their HTML representation (hCard).
	 * </p>
	 * <p>
	 * Use {@link HCardPage} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCard(s) to marshal
	 * @return chainer object for completing the write operation
	 * @see HCardPage
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public static ChainingHtmlWriter writeHtml(Collection<VCard> vcards) {
		return new ChainingHtmlWriter(vcards);
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their JSON representation (jCard).
	 * </p>
	 * <p>
	 * Use {@link JCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see JCardWriter
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public static ChainingJsonWriter writeJson(VCard... vcards) {
		return writeJson(Arrays.asList(vcards));
	}

	/**
	 * <p>
	 * Marshals one or more vCards to their JSON representation (jCard).
	 * </p>
	 * <p>
	 * Use {@link JCardWriter} for more control over how the vCards are written.
	 * </p>
	 * @param vcards the vCards to marshal
	 * @return chainer object for completing the write operation
	 * @see JCardWriter
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public static ChainingJsonWriter writeJson(Collection<VCard> vcards) {
		return new ChainingJsonWriter(vcards);
	}

	private Ezvcard() {
		//hide
	}
}
