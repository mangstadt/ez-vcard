package ezvcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import ezvcard.io.LuckyNumType;
import ezvcard.types.FormattedNameType;
import ezvcard.util.VCardBuilder;
import ezvcard.util.XCardBuilder;

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
public class EzvcardTest {
	@Test
	public void parse_first() throws Exception {
		VCardBuilder vb = new VCardBuilder(VCardVersion.V2_1);
		vb.prop("FN").value("John Doe");
		List<List<String>> warnings = new ArrayList<List<String>>();

		VCard vcard = Ezvcard.parse(vb.toString()).warnings(warnings).first();
		assertEquals(VCardVersion.V2_1, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, warnings.size());
	}

	@Test
	public void parse_all() throws Exception {
		VCardBuilder vb = new VCardBuilder(VCardVersion.V2_1);
		vb.prop("FN").value("John Doe");
		vb.begin(VCardVersion.V3_0);
		vb.prop("FN").value("Jane Doe");
		List<List<String>> warnings = new ArrayList<List<String>>();

		List<VCard> vcards = Ezvcard.parse(vb.toString()).warnings(warnings).all();
		Iterator<VCard> it = vcards.iterator();

		VCard vcard = it.next();
		assertEquals(VCardVersion.V2_1, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = it.next();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertEquals(2, warnings.size());

		assertFalse(it.hasNext());
	}

	@Test
	public void parse_register() throws Exception {
		VCardBuilder vb = new VCardBuilder(VCardVersion.V2_1);
		vb.prop("X-LUCKY-NUM").value("22");

		VCard vcard = Ezvcard.parse(vb.toString()).register(LuckyNumType.class).first();
		assertEquals(VCardVersion.V2_1, vcard.getVersion());
		List<LuckyNumType> ext = vcard.getExtendedType(LuckyNumType.class);
		assertEquals(1, ext.size());
		assertEquals(22, ext.get(0).luckyNum);
	}

	@Test
	public void parseXml_first() throws Exception {
		XCardBuilder xb = new XCardBuilder();
		xb.prop("fn", "<text>John Doe</text>");
		List<List<String>> warnings = new ArrayList<List<String>>();

		VCard vcard = Ezvcard.parseXml(xb.toString()).warnings(warnings).first();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, warnings.size());
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
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = it.next();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertEquals(2, warnings.size());

		assertFalse(it.hasNext());
	}

	@Test
	public void parseXml_register() throws Exception {
		XCardBuilder xb = new XCardBuilder();
		xb.prop("http://luckynum.com", "lucky-num", "<integer>22</integer>");

		VCard vcard = Ezvcard.parseXml(xb.toString()).register(LuckyNumType.class).first();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		List<LuckyNumType> ext = vcard.getExtendedType(LuckyNumType.class);
		assertEquals(1, ext.size());
		assertEquals(22, ext.get(0).luckyNum);
	}

	@Test
	public void parseHtml_first() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"vcard\">");
		sb.append("<div class=\"fn\">John Doe</div>");
		sb.append("</div>");
		List<List<String>> warnings = new ArrayList<List<String>>();

		VCard vcard = Ezvcard.parseHtml(sb.toString()).warnings(warnings).first();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, warnings.size());
	}

	@Test
	public void parseHtml_all() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<div class=\"vcard\">");
		sb.append("<div class=\"fn\">John Doe</div>");
		sb.append("</div>");
		sb.append("<div class=\"vcard\">");
		sb.append("<div class=\"fn\">Jane Doe</div>");
		sb.append("</div>");
		sb.append("</html>");
		List<List<String>> warnings = new ArrayList<List<String>>();

		List<VCard> vcards = Ezvcard.parseHtml(sb.toString()).warnings(warnings).all();
		Iterator<VCard> it = vcards.iterator();

		VCard vcard = it.next();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = it.next();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertEquals(2, warnings.size());

		assertFalse(it.hasNext());
	}

	@Test
	public void parseHtml_register() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"vcard\">");
		sb.append("<div class=\"x-lucky-num\">22</div>");
		sb.append("</div>");

		VCard vcard = Ezvcard.parseHtml(sb.toString()).register(LuckyNumType.class).first();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		List<LuckyNumType> ext = vcard.getExtendedType(LuckyNumType.class);
		assertEquals(1, ext.size());
		assertEquals(22, ext.get(0).luckyNum);
	}

	@Test
	public void parseHtml_pageUrl() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"vcard\">");
		sb.append("<a href=\"profile.html\" class=\"url fn\">John Doe</a>");
		sb.append("</div>");
		String html = sb.toString();

		//without
		VCard vcard = Ezvcard.parseHtml(html).first();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertTrue(vcard.getSources().isEmpty());
		assertEquals("profile.html", vcard.getUrls().get(0).getValue());

		//with
		vcard = Ezvcard.parseHtml(html).pageUrl("http://www.example.com/index.html").first();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals("http://www.example.com/index.html", vcard.getSources().get(0).getValue());
		assertEquals("http://www.example.com/profile.html", vcard.getUrls().get(0).getValue());
	}

	@Test
	public void write_one() throws Exception {
		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V2_1);
		vcard.setFormattedName(new FormattedNameType("John Doe"));
		List<List<String>> warnings = new ArrayList<List<String>>();

		String actual = Ezvcard.write(vcard).warnings(warnings).go();
		assertTrue(actual.contains("VERSION:2.1"));
		assertTrue(actual.contains("FN:John Doe"));
		assertEquals(1, warnings.size());
	}

	@Test
	public void write_multiple() throws Exception {
		VCard vcard1 = new VCard();
		vcard1.setVersion(VCardVersion.V2_1);
		vcard1.setFormattedName(new FormattedNameType("John Doe"));
		VCard vcard2 = new VCard();
		vcard2.setVersion(VCardVersion.V3_0);
		vcard2.setFormattedName(new FormattedNameType("Jane Doe"));
		VCard vcard3 = new VCard();
		vcard3.setVersion(VCardVersion.V4_0);
		vcard3.setFormattedName(new FormattedNameType("Janet Doe"));
		List<List<String>> warnings = new ArrayList<List<String>>();

		String actual = Ezvcard.write(vcard1, vcard2, vcard3).warnings(warnings).go();
		assertTrue(actual.matches("(?s)BEGIN:VCARD.*?VERSION:2\\.1.*?FN:John Doe.*?END:VCARD.*?BEGIN:VCARD.*?VERSION:3\\.0.*?FN:Jane Doe.*?END:VCARD.*?BEGIN:VCARD.*?VERSION:4\\.0.*?FN:Janet Doe.*?END:VCARD.*"));
		assertEquals(3, warnings.size());
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
	public void writeXml_one() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedNameType("John Doe"));
		List<List<String>> warnings = new ArrayList<List<String>>();

		String actual = Ezvcard.writeXml(vcard).warnings(warnings).go();
		assertTrue(actual.contains("<fn><text>John Doe</text></fn>"));
		assertEquals(1, warnings.size());
	}

	@Test
	public void writeXml_multiple() throws Exception {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName(new FormattedNameType("John Doe"));
		VCard vcard2 = new VCard();
		vcard2.setFormattedName(new FormattedNameType("Jane Doe"));
		VCard vcard3 = new VCard();
		vcard3.setFormattedName(new FormattedNameType("Janet Doe"));
		List<List<String>> warnings = new ArrayList<List<String>>();

		String actual = Ezvcard.writeXml(vcard1, vcard2, vcard3).warnings(warnings).go();
		assertTrue(actual.matches("(?s).*?<vcard>.*?<fn><text>John Doe</text></fn>.*?</vcard>.*?<vcard>.*?<fn><text>Jane Doe</text></fn>.*?</vcard>*?<vcard>.*?<fn><text>Janet Doe</text></fn>.*?</vcard>.*"));
		assertEquals(3, warnings.size());
	}

	@Test
	public void writeXml_prodId() throws Exception {
		VCard vcard = new VCard();

		String actual = Ezvcard.writeXml(vcard).go();
		assertTrue(actual.contains("<prodid>"));

		actual = Ezvcard.writeXml(vcard).prodId(true).go();
		assertTrue(actual.contains("<prodid>"));

		actual = Ezvcard.writeXml(vcard).prodId(false).go();
		assertFalse(actual.contains("<prodid>"));
	}

	@Test
	public void writeHtml_one() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedNameType("John Doe"));

		String actual = Ezvcard.writeHtml(vcard).go();
		Document document = Jsoup.parse(actual);
		assertEquals(1, document.select(".vcard").size());
		assertEquals(1, document.select(".vcard .fn").size());
	}

	@Test
	public void writeHtml_multiple() throws Exception {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName(new FormattedNameType("John Doe"));
		VCard vcard2 = new VCard();
		vcard2.setFormattedName(new FormattedNameType("Jane Doe"));
		VCard vcard3 = new VCard();
		vcard3.setFormattedName(new FormattedNameType("Janet Doe"));

		String actual = Ezvcard.writeHtml(vcard1, vcard2, vcard3).go();
		Document document = Jsoup.parse(actual);
		assertEquals(3, document.select(".vcard").size());
		assertEquals(3, document.select(".vcard .fn").size());
	}
}
