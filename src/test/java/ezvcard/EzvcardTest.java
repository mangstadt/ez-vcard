package ezvcard;

import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.assertWarningsLists;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jsoup.Jsoup;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ezvcard.io.LuckyNumProperty;
import ezvcard.io.LuckyNumProperty.LuckyNumScribe;
import ezvcard.io.text.TargetApplication;
import ezvcard.io.xml.XCardNamespaceContext;
import ezvcard.parameter.ImageType;
import ezvcard.property.FormattedName;
import ezvcard.property.Photo;
import ezvcard.util.XCardBuilder;
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
 * @author Michael Angstadt
 */
public class EzvcardTest {
	private final XPath xpath = XPathFactory.newInstance().newXPath();
	{
		xpath.setNamespaceContext(new XCardNamespaceContext(VCardVersion.V4_0, "v"));
	}

	@Test
	public void parse_first() throws Exception {
		//@formatter:off
		String str = 
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		List<List<String>> warnings = new ArrayList<List<String>>();
		VCard vcard = Ezvcard.parse(str).warnings(warnings).first();
		assertVersion(VCardVersion.V2_1, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarningsLists(warnings, 0);
	}

	@Test
	public void parse_all() throws Exception {
		//@formatter:off
		String str = 
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"FN:John Doe\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"FN:Jane Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		List<List<String>> warnings = new ArrayList<List<String>>();
		List<VCard> vcards = Ezvcard.parse(str).warnings(warnings).all();
		Iterator<VCard> it = vcards.iterator();

		VCard vcard = it.next();
		assertVersion(VCardVersion.V2_1, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = it.next();
		assertVersion(VCardVersion.V3_0, vcard);
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertWarningsLists(warnings, 0, 0);

		assertFalse(it.hasNext());
	}

	@Test
	public void parse_register() throws Exception {
		//@formatter:off
		String str = 
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"X-LUCKY-NUM:22\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCard vcard = Ezvcard.parse(str).register(new LuckyNumScribe()).first();
		assertVersion(VCardVersion.V2_1, vcard);
		List<LuckyNumProperty> ext = vcard.getProperties(LuckyNumProperty.class);
		assertEquals(1, ext.size());
		assertEquals(22, ext.get(0).luckyNum);
	}

	@Test
	public void parse_caretDecoding() throws Exception {
		//@formatter:off
		String str = 
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"FN;X-TEST=George Herman ^'Babe^' Ruth:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		//defaults to true
		VCard vcard = Ezvcard.parse(str).first();
		assertEquals("George Herman \"Babe\" Ruth", vcard.getFormattedName().getParameter("X-TEST"));

		vcard = Ezvcard.parse(str).caretDecoding(true).first();
		assertEquals("George Herman \"Babe\" Ruth", vcard.getFormattedName().getParameter("X-TEST"));

		vcard = Ezvcard.parse(str).caretDecoding(false).first();
		assertEquals("George Herman ^'Babe^' Ruth", vcard.getFormattedName().getParameter("X-TEST"));
	}

	@Test
	public void parseXml_first() throws Exception {
		XCardBuilder xb = new XCardBuilder();
		xb.prop("fn", "<text>John Doe</text>");
		List<List<String>> warnings = new ArrayList<List<String>>();

		VCard vcard = Ezvcard.parseXml(xb.toString()).warnings(warnings).first();
		assertVersion(VCardVersion.V4_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarningsLists(warnings, 0);
	}

	@Test
	public void parseXml_all() throws Exception {
		XCardBuilder xb = new XCardBuilder();
		xb.prop("fn", "<text>John Doe</text>");
		xb.begin();
		xb.prop("fn", "<text>Jane Doe</text>");
		List<List<String>> warnings = new ArrayList<List<String>>();

		List<VCard> vcards = Ezvcard.parseXml(xb.toString()).warnings(warnings).all();
		Iterator<VCard> it = vcards.iterator();

		VCard vcard = it.next();
		assertVersion(VCardVersion.V4_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = it.next();
		assertVersion(VCardVersion.V4_0, vcard);
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertWarningsLists(warnings, 0, 0);

		assertFalse(it.hasNext());
	}

	@Test
	public void parseXml_register() throws Exception {
		XCardBuilder xb = new XCardBuilder();
		xb.prop("http://luckynum.com", "lucky-num", "<num>22</num>");

		VCard vcard = Ezvcard.parseXml(xb.toString()).register(new LuckyNumScribe()).first();
		assertVersion(VCardVersion.V4_0, vcard);
		List<LuckyNumProperty> ext = vcard.getProperties(LuckyNumProperty.class);
		assertEquals(1, ext.size());
		assertEquals(22, ext.get(0).luckyNum);
	}

	@Test
	public void parseHtml_first() throws Exception {
		//@formatter:off
		String html =
		"<div class=\"vcard\">" +
			"<div class=\"fn\">John Doe</div>" +
		"</div>";
		//@formatter:on

		List<List<String>> warnings = new ArrayList<List<String>>();

		VCard vcard = Ezvcard.parseHtml(html).warnings(warnings).first();
		assertVersion(VCardVersion.V3_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarningsLists(warnings, 0);
	}

	@Test
	public void parseHtml_all() throws Exception {
		//@formatter:off
		String html =
		"<html>" +
			"<div class=\"vcard\">" +
				"<div class=\"fn\">John Doe</div>" +
			"</div>" +
			"<div class=\"vcard\">" +
				"<div class=\"fn\">Jane Doe</div>" +
			"</div>" +
		"</html>";
		//@formatter:on
		List<List<String>> warnings = new ArrayList<List<String>>();

		List<VCard> vcards = Ezvcard.parseHtml(html).warnings(warnings).all();
		Iterator<VCard> it = vcards.iterator();

		VCard vcard = it.next();
		assertVersion(VCardVersion.V3_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = it.next();
		assertVersion(VCardVersion.V3_0, vcard);
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertWarningsLists(warnings, 0, 0);

		assertFalse(it.hasNext());
	}

	@Test
	public void parseHtml_register() throws Exception {
		//@formatter:off
		String html =
		"<div class=\"vcard\">" +
			"<div class=\"x-lucky-num\">22</div>" +
		"</div>";
		//@formatter:on

		VCard vcard = Ezvcard.parseHtml(html).register(new LuckyNumScribe()).first();
		assertVersion(VCardVersion.V3_0, vcard);
		List<LuckyNumProperty> ext = vcard.getProperties(LuckyNumProperty.class);
		assertEquals(1, ext.size());
		assertEquals(22, ext.get(0).luckyNum);
	}

	@Test
	public void parseHtml_pageUrl() throws Exception {
		//@formatter:off
		String html =
		"<div class=\"vcard\">" +
			"<a href=\"profile.html\" class=\"url fn\">John Doe</a>" +
		"</div>";
		//@formatter:on

		//without
		VCard vcard = Ezvcard.parseHtml(html).first();
		assertVersion(VCardVersion.V3_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertTrue(vcard.getSources().isEmpty());
		assertEquals("profile.html", vcard.getUrls().get(0).getValue());

		//with
		vcard = Ezvcard.parseHtml(html).pageUrl("http://www.example.com/index.html").first();
		assertVersion(VCardVersion.V3_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals("http://www.example.com/index.html", vcard.getSources().get(0).getValue());
		assertEquals("http://www.example.com/profile.html", vcard.getUrls().get(0).getValue());
	}

	@Test
	public void parseJson_first() {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		List<List<String>> warnings = new ArrayList<List<String>>();

		VCard vcard = Ezvcard.parseJson(json).warnings(warnings).first();
		assertVersion(VCardVersion.V4_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarningsLists(warnings, 0);
	}

	@Test
	public void parseJson_all() {
		//@formatter:off
		String json =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"Jane Doe\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on

		List<List<String>> warnings = new ArrayList<List<String>>();

		List<VCard> vcards = Ezvcard.parseJson(json).warnings(warnings).all();
		Iterator<VCard> it = vcards.iterator();

		VCard vcard = it.next();
		assertVersion(VCardVersion.V4_0, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = it.next();
		assertVersion(VCardVersion.V4_0, vcard);
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertWarningsLists(warnings, 0, 0);

		assertFalse(it.hasNext());
	}

	@Test
	public void parseJson_register() {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-lucky-num\", {}, \"text\", \"22\"]" +
		    "]" +
		  "]";
		//@formatter:on

		VCard vcard = Ezvcard.parseJson(json).register(new LuckyNumScribe()).first();
		assertVersion(VCardVersion.V4_0, vcard);
		List<LuckyNumProperty> ext = vcard.getProperties(LuckyNumProperty.class);
		assertEquals(1, ext.size());
		assertEquals(22, ext.get(0).luckyNum);
	}

	@Test
	public void write_one() throws Exception {
		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V2_1);
		vcard.setFormattedName(new FormattedName("John Doe"));

		String actual = Ezvcard.write(vcard).go();
		assertTrue(actual.contains("VERSION:2.1"));
		assertTrue(actual.contains("FN:John Doe"));
	}

	@Test
	public void write_multiple() throws Exception {
		VCard vcard1 = new VCard();
		vcard1.setVersion(VCardVersion.V2_1);
		vcard1.setFormattedName(new FormattedName("John Doe"));
		VCard vcard2 = new VCard();
		vcard2.setVersion(VCardVersion.V3_0);
		vcard2.setFormattedName(new FormattedName("Jane Doe"));
		VCard vcard3 = new VCard();
		vcard3.setVersion(VCardVersion.V4_0);
		vcard3.setFormattedName(new FormattedName("Janet Doe"));

		String actual = Ezvcard.write(vcard1, vcard2, vcard3).go();
		assertTrue(actual.matches("(?s)BEGIN:VCARD.*?VERSION:2\\.1.*?FN:John Doe.*?END:VCARD.*?BEGIN:VCARD.*?VERSION:3\\.0.*?FN:Jane Doe.*?END:VCARD.*?BEGIN:VCARD.*?VERSION:4\\.0.*?FN:Janet Doe.*?END:VCARD.*"));
	}

	@Test
	public void write_version() throws Exception {
		VCard vcard1 = new VCard();
		vcard1.setVersion(VCardVersion.V2_1);
		VCard vcard2 = new VCard();
		vcard2.setVersion(VCardVersion.V3_0);
		VCard vcard3 = new VCard();
		vcard3.setVersion(VCardVersion.V4_0);

		String actual = Ezvcard.write(vcard1, vcard2, vcard3).version(VCardVersion.V4_0).go();
		assertTrue(actual.matches("(?s)(.*?VERSION:4\\.0){3}.*"));
	}

	@Test
	public void write_prodId() throws Exception {
		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V3_0);

		String actual = Ezvcard.write(vcard).go();
		assertTrue(actual.contains("\r\nPRODID:"));

		actual = Ezvcard.write(vcard).prodId(true).go();
		assertTrue(actual.contains("\r\nPRODID:"));

		actual = Ezvcard.write(vcard).prodId(false).go();
		assertFalse(actual.contains("\r\nPRODID:"));
	}

	@Test
	public void write_caretEncoding() throws Exception {
		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V4_0);
		FormattedName fn = vcard.setFormattedName("test");
		fn.getParameters().put("X-TEST", "George Herman \"Babe\" Ruth");

		//default should be "false"
		try {
			Ezvcard.write(vcard).go();
			fail("IllegalArgumentException expected.");
		} catch (IllegalArgumentException e) {
			//expected
		}

		String actual = Ezvcard.write(vcard).caretEncoding(true).go();
		assertTrue(actual.contains("\r\nFN;X-TEST=George Herman ^'Babe^' Ruth:"));

		try {
			Ezvcard.write(vcard).caretEncoding(false).go();
			fail("IllegalArgumentException expected.");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	@Test
	public void write_versionStrict() throws Exception {
		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V4_0);
		vcard.setMailer("mailer"); //only supported by 2.1 and 3.0

		String actual = Ezvcard.write(vcard).go();
		assertFalse(actual.contains("\r\nMAILER:"));

		actual = Ezvcard.write(vcard).versionStrict(true).go();
		assertFalse(actual.contains("\r\nMAILER:"));

		actual = Ezvcard.write(vcard).versionStrict(false).go();
		assertTrue(actual.contains("\r\nMAILER:"));
	}

	@Test
	public void write_targetApplication() throws Exception {
		byte data[] = "data".getBytes();
		VCard vcard = new VCard();
		vcard.addPhoto(new Photo(data, ImageType.JPEG));

		//default value (null)
		{
			String actual = Ezvcard.write(vcard).prodId(false).version(VCardVersion.V2_1).go();

			//@formatter:off
			String expected =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"PHOTO;ENCODING=BASE64;JPEG:ZGF0YQ==\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}

		//with value
		{
			String actual = Ezvcard.write(vcard).prodId(false).version(VCardVersion.V2_1).targetApplication(TargetApplication.OUTLOOK).go();

			//@formatter:off
			String expected =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"PHOTO;ENCODING=BASE64;JPEG:ZGF0YQ==\r\n" +
				"\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			assertEquals(expected, actual);
		}
	}

	@Test
	public void writeXml_go() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		String xml = Ezvcard.writeXml(vcard).go();
		assertTrue(xml.contains("<fn><text>John Doe</text></fn>"));
	}

	@Test
	public void writeXml_dom() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedName("John Doe"));

		Document actual = Ezvcard.writeXml(vcard).prodId(false).dom();

		//@formatter:off
		String html =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(html);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeXml_one() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		Document dom = Ezvcard.writeXml(vcard).dom();
		String actual = (String) xpath.evaluate("/v:vcards/v:vcard/v:fn/v:text", dom, XPathConstants.STRING);
		assertEquals(vcard.getFormattedName().getValue(), actual);
	}

	@Test
	public void writeXml_multiple() throws Exception {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName("John Doe");
		VCard vcard2 = new VCard();
		vcard2.setFormattedName("Jane Doe");
		VCard vcard3 = new VCard();
		vcard3.setFormattedName("Janet Doe");

		Document dom = Ezvcard.writeXml(vcard1, vcard2, vcard3).dom();
		NodeList nl = (NodeList) xpath.evaluate("/v:vcards/v:vcard/v:fn/v:text", dom, XPathConstants.NODESET);

		assertEquals(vcard1.getFormattedName().getValue(), nl.item(0).getTextContent());
		assertEquals(vcard2.getFormattedName().getValue(), nl.item(1).getTextContent());
		assertEquals(vcard3.getFormattedName().getValue(), nl.item(2).getTextContent());
	}

	@Test
	public void writeXml_prodId() throws Exception {
		VCard vcard = new VCard();

		Document dom = Ezvcard.writeXml(vcard).dom();
		Double count = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", dom, XPathConstants.NUMBER);
		assertEquals(Double.valueOf(1), count);

		dom = Ezvcard.writeXml(vcard).prodId(true).dom();
		count = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", dom, XPathConstants.NUMBER);
		assertEquals(Double.valueOf(1), count);

		dom = Ezvcard.writeXml(vcard).prodId(false).dom();
		count = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", dom, XPathConstants.NUMBER);
		assertEquals(Double.valueOf(0), count);
	}

	@Test
	public void writeXml_versionStrict() throws Exception {
		VCard vcard = new VCard();
		vcard.setMailer("mailer"); //only supported by 2.1 and 3.0

		Document dom = Ezvcard.writeXml(vcard).dom();
		Double count = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:mailer)", dom, XPathConstants.NUMBER);
		assertEquals(Double.valueOf(0), count);

		dom = Ezvcard.writeXml(vcard).versionStrict(true).dom();
		count = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:mailer)", dom, XPathConstants.NUMBER);
		assertEquals(Double.valueOf(0), count);

		dom = Ezvcard.writeXml(vcard).versionStrict(false).dom();
		count = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:mailer)", dom, XPathConstants.NUMBER);
		assertEquals(Double.valueOf(1), count);
	}

	@Test
	public void writeXml_indent() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedName("John Doe"));

		String actual = Ezvcard.writeXml(vcard).indent(2).go();
		assertTrue(actual.contains("    <fn>" + NEWLINE + "      <text>John Doe</text>" + NEWLINE + "    </fn>"));
	}

	@Test
	public void writeHtml_one() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedName("John Doe"));

		String actual = Ezvcard.writeHtml(vcard).go();
		org.jsoup.nodes.Document document = Jsoup.parse(actual);
		assertEquals(1, document.select(".vcard").size());
		assertEquals(1, document.select(".vcard .fn").size());
	}

	@Test
	public void writeHtml_multiple() throws Exception {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName(new FormattedName("John Doe"));
		VCard vcard2 = new VCard();
		vcard2.setFormattedName(new FormattedName("Jane Doe"));
		VCard vcard3 = new VCard();
		vcard3.setFormattedName(new FormattedName("Janet Doe"));

		String actual = Ezvcard.writeHtml(vcard1, vcard2, vcard3).go();
		org.jsoup.nodes.Document document = Jsoup.parse(actual);
		assertEquals(3, document.select(".vcard").size());
		assertEquals(3, document.select(".vcard .fn").size());
	}

	@Test
	public void writeJson_one() {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		//@formatter:off
		String expected =
		"[\"vcard\"," +
		  "[" +
		    "[\"version\",{},\"text\",\"4.0\"]," +
		    "[\"fn\",{},\"text\",\"John Doe\"]" +
		  "]" +
		"]";
		//@formatter:on
		String actual = Ezvcard.writeJson(vcard).prodId(false).go();
		assertEquals(expected, actual);
	}

	@Test
	public void writeJson_multiple() {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName(new FormattedName("John Doe"));
		VCard vcard2 = new VCard();
		vcard2.setFormattedName(new FormattedName("Jane Doe"));
		VCard vcard3 = new VCard();
		vcard3.setFormattedName(new FormattedName("Janet Doe"));

		//@formatter:off
		String expected =
		"[" +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"John Doe\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"Jane Doe\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"Janet Doe\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		String actual = Ezvcard.writeJson(vcard1, vcard2, vcard3).prodId(false).go();
		assertEquals(expected, actual);
	}

	@Test
	public void writeJson_prodId() {
		VCard vcard = new VCard();

		String actual = Ezvcard.writeJson(vcard).go();
		assertTrue(actual.contains("[\"prodid\","));

		actual = Ezvcard.writeJson(vcard).prodId(true).go();
		assertTrue(actual.contains("[\"prodid\","));

		actual = Ezvcard.writeJson(vcard).prodId(false).go();
		assertFalse(actual.contains("[\"prodid\","));
	}

	@Test
	public void writeJson_versionStrict() {
		VCard vcard = new VCard();
		vcard.setMailer("mailer"); //only supported by 2.1 and 3.0

		String actual = Ezvcard.writeJson(vcard).go();
		assertFalse(actual.contains("[\"mailer\","));

		actual = Ezvcard.writeJson(vcard).versionStrict(true).go();
		assertFalse(actual.contains("[\"mailer\","));

		actual = Ezvcard.writeJson(vcard).versionStrict(false).go();
		assertTrue(actual.contains("[\"mailer\","));
	}

	@Test
	public void writeJson_indent() {
		VCard vcard = new VCard();

		//defaults to "false"
		String actual = Ezvcard.writeJson(vcard).go();
		assertTrue(actual.startsWith("[\"vcard\",[[\""));

		actual = Ezvcard.writeJson(vcard).prettyPrint(true).go();
		assertTrue(actual.startsWith("[" + NEWLINE + "  \"vcard\"," + NEWLINE + "  [" + NEWLINE + "    ["));

		actual = Ezvcard.writeJson(vcard).prettyPrint(false).go();
		assertTrue(actual.startsWith("[\"vcard\",[[\""));
	}

	@Test
	public void writeJson_does_not_close_stream() throws Exception {
		VCard vcard = new VCard();

		File file = new File("target", "temp.json");
		FileWriter writer = new FileWriter(file);
		try {
			Ezvcard.writeJson(vcard).go(writer);
			writer.write("test"); //an exception will be thrown if the writer is closed
		} finally {
			writer.close();
			file.delete();
		}
	}
}