package ezvcard.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.types.FormattedNameType;
import ezvcard.types.NoteType;
import ezvcard.types.PhotoType;

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
 * @author Michael Angstadt
 */
public class XCardMarshallerTest extends XMLTestCase {
	/**
	 * A basic test with one type.
	 */
	public void testBasicType() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		XCardMarshaller xcm = new XCardMarshaller();
		xcm.setAddGenerator(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<fn><text>John Doe</text></fn>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can marshal parameters.
	 */
	public void testParameters() throws Exception {
		VCard vcard = new VCard();
		NoteType note = new NoteType("This is a\nnote.");
		note.setLanguage("en");
		note.addPid(1, 1);
		note.addPid(2, 2);
		note.getSubTypes().put("X-CUSTOM", "xxx");
		vcard.addNote(note);

		XCardMarshaller xcm = new XCardMarshaller();
		xcm.setAddGenerator(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<note>");
					sb.append("<parameters>");
						sb.append("<language><language-tag>en</language-tag></language>");
						sb.append("<pid><text>1.1</text><text>2.2</text></pid>");
						sb.append("<x-custom><unknown>xxx</unknown></x-custom>");
					sb.append("</parameters>");
					sb.append("<text>This is a\\nnote.</text>");
				sb.append("</note>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can marshal groups.
	 */
	public void testGroup() throws Exception {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		NoteType note = new NoteType("This is a\nnote.");
		note.setGroup("group1");
		note.setLanguage("en");
		vcard.addNote(note);

		PhotoType photo = new PhotoType("http://example.com/image.jpg", ImageTypeParameter.JPEG);
		photo.setGroup("group1");
		vcard.addPhoto(photo);

		note = new NoteType("Bonjour.");
		note.setGroup("group2");
		vcard.addNote(note);

		XCardMarshaller xcm = new XCardMarshaller();
		xcm.setAddGenerator(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<fn><text>John Doe</text></fn>");
				sb.append("<group name=\"group1\">");
					sb.append("<photo><parameters><mediatype><text>image/jpeg</text></mediatype></parameters><text>http://example.com/image.jpg</text></photo>");
					sb.append("<note><parameters><language><language-tag>en</language-tag></language></parameters><text>This is a\\nnote.</text></note>");
				sb.append("</group>");
				sb.append("<group name=\"group2\">");
					sb.append("<note><text>Bonjour.</text></note>");
				sb.append("</group>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can add multiple vCards to the same document.
	 */
	public void testMultiple() throws Exception {
		VCard vcard1 = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard1.setFormattedName(fn);

		VCard vcard2 = new VCard();
		NoteType note = new NoteType("Hello world!");
		vcard2.addNote(note);

		XCardMarshaller xcm = new XCardMarshaller();
		xcm.setAddGenerator(false);
		xcm.addVCard(vcard1);
		xcm.addVCard(vcard2);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<fn><text>John Doe</text></fn>");
			sb.append("</vcard>");
			sb.append("<vcard>");
				sb.append("<note><text>Hello world!</text></note>");
				sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure the {@link XCardMarshaller#setAddGenerator} method works.
	 */
	public void testSetAddGenerator() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		XCardMarshaller xcm = new XCardMarshaller();
		xcm.setAddGenerator(true);
		xcm.addVCard(vcard);

		StringWriter sw = new StringWriter();
		xcm.write(sw);
		String xml = sw.toString();

		assertTrue(xml.matches(".*?<x-generator><text>.*?</text></x-generator>.*"));
	}

	/**
	 * Parses an XML string into a DOM.
	 * @param xml the XML string
	 * @return the parsed XML
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document toDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		fact.setNamespaceAware(true);
		DocumentBuilder db = fact.newDocumentBuilder();
		return db.parse(new ByteArrayInputStream(xml.getBytes()));
	}
}
