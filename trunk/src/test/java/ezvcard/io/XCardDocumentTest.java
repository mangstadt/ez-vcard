package ezvcard.io;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.types.FormattedNameType;
import ezvcard.types.KindType;
import ezvcard.types.MemberType;
import ezvcard.types.NoteType;
import ezvcard.types.PhotoType;
import ezvcard.types.ProdIdType;
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
 * @author Michael Angstadt
 */
public class XCardDocumentTest {
	/**
	 * A basic test with one type.
	 */
	@Test
	public void basicType() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">");
			sb.append("<vcard>");
				sb.append("<fn><text>John Doe</text></fn>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = XmlUtils.toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void check_supported_versions() throws Exception {
		//all properties support the version
		{
			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");

			XCardDocument doc = new XCardDocument();
			doc.addVCard(vcard);

			List<String> warnings = doc.getWarnings();
			assertTrue(warnings.isEmpty());
		}

		//one property does not support the version
		{
			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");
			vcard.setMailer("Thunderbird");

			XCardDocument doc = new XCardDocument();
			doc.addVCard(vcard);

			List<String> warnings = doc.getWarnings();
			assertEquals(1, warnings.size());

			//property not written to vCard
			VCard parsedVCard = Ezvcard.parseXml(doc.write()).first();
			assertNull(parsedVCard.getMailer());
		}
	}

	@Test
	public void required_properties() throws Exception {
		//without FN
		{
			VCard vcard = new VCard();
			XCardDocument doc = new XCardDocument();
			doc.addVCard(vcard);

			List<String> warnings = doc.getWarnings();
			assertEquals(1, warnings.size());
		}

		//with FN
		{
			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");

			XCardDocument doc = new XCardDocument();
			doc.addVCard(vcard);

			List<String> warnings = doc.getWarnings();
			assertTrue(warnings.isEmpty());
		}
	}

	/**
	 * Makes sure it can marshal parameters.
	 */
	@Test
	public void parameters() throws Exception {
		VCard vcard = new VCard();
		NoteType note = new NoteType("This is a\nnote.");
		note.setLanguage("en");
		note.addPid(1, 1);
		note.addPid(2, 2);
		note.getSubTypes().put("X-CUSTOM", "xxx");
		vcard.addNote(note);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">");
			sb.append("<vcard>");
				sb.append("<note>");
					sb.append("<parameters>");
						sb.append("<language><language-tag>en</language-tag></language>");
						sb.append("<pid><text>1.1</text><text>2.2</text></pid>");
						sb.append("<x-custom><unknown>xxx</unknown></x-custom>");
					sb.append("</parameters>");
					sb.append("<text>This is a\nnote.</text>");
				sb.append("</note>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = XmlUtils.toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can marshal groups.
	 */
	@Test
	public void group() throws Exception {
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

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">");
			sb.append("<vcard>");
				sb.append("<fn><text>John Doe</text></fn>");
				sb.append("<group name=\"group1\">");
					sb.append("<photo><parameters><mediatype><text>image/jpeg</text></mediatype></parameters><uri>http://example.com/image.jpg</uri></photo>");
					sb.append("<note><parameters><language><language-tag>en</language-tag></language></parameters><text>This is a\nnote.</text></note>");
				sb.append("</group>");
				sb.append("<group name=\"group2\">");
					sb.append("<note><text>Bonjour.</text></note>");
				sb.append("</group>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = XmlUtils.toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can add multiple vCards to the same document.
	 */
	@Test
	public void multiple() throws Exception {
		VCard vcard1 = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard1.setFormattedName(fn);

		VCard vcard2 = new VCard();
		NoteType note = new NoteType("Hello world!");
		vcard2.addNote(note);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard1);
		xcm.addVCard(vcard2);

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">");
			sb.append("<vcard>");
				sb.append("<fn><text>John Doe</text></fn>");
			sb.append("</vcard>");
			sb.append("<vcard>");
				sb.append("<note><text>Hello world!</text></note>");
				sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = XmlUtils.toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void setAddProdId() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		XCardDocument xcm = new XCardDocument();
		xcm.addVCard(vcard);
		String xml = xcm.write();
		assertTrue(xml.matches(".*?<prodid><text>.*?</text></prodid>.*"));

		xcm = new XCardDocument();
		xcm.setAddProdId(true);
		xcm.addVCard(vcard);
		xml = xcm.write();
		assertTrue(xml.matches(".*?<prodid><text>.*?</text></prodid>.*"));

		xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);
		xml = xcm.write();
		assertFalse(xml.matches(".*?<prodid><text>.*?</text></prodid>.*"));
	}

	@Test
	public void setAddProdId_overwrites_existing_prodId() throws Exception {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		ProdIdType prodId = new ProdIdType("Acme Co.");
		vcard.setProdId(prodId);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);
		String xml = xcm.write();
		assertTrue(xml.matches(".*?<prodid><text>Acme Co.</text></prodid>.*"));

		xcm = new XCardDocument();
		xcm.setAddProdId(true);
		xcm.addVCard(vcard);
		xml = xcm.write();
		assertFalse(xml.matches(".*?<prodid><text>Acme Co.</text></prodid>.*"));
	}

	/**
	 * If the type's marshal method throws a {@link SkipMeException}, then a
	 * warning should be added to the warnings list and the type object should
	 * NOT be marshalled.
	 */
	@Test
	public void skipMeException() throws Exception {
		VCard vcard = new VCard();

		//add FN property so a warning isn't generated (4.0 requires FN to be present)
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		LuckyNumType num = new LuckyNumType();
		num.luckyNum = 24;
		vcard.addExtendedType(num);

		//should be skipped
		num = new LuckyNumType();
		num.luckyNum = 13;
		vcard.addExtendedType(num);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		assertEquals(xcm.getWarnings().toString(), 1, xcm.getWarnings().size());

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" xmlns:a=\"http://luckynum.com\">");
			sb.append("<vcard>");
				sb.append("<fn><text>John Doe</text></fn>");
				sb.append("<a:lucky-num>24</a:lucky-num>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = XmlUtils.toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Tests how extended types are marshalled.
	 */
	@Test
	public void extendedTypes() throws Exception {
		VCard vcard = new VCard();

		//contains marshal methods and QName
		LuckyNumType num = new LuckyNumType();
		num.luckyNum = 24;
		vcard.addExtendedType(num);

		//contains marshal methods, but does not have a QName
		SalaryType salary = new SalaryType();
		salary.salary = 1000000;
		vcard.addExtendedType(salary);

		//does not contain marshal methods nor QName
		AgeType age = new AgeType();
		age.age = 22;
		vcard.addExtendedType(age);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		assertEquals(xcm.getWarnings().toString(), 1, xcm.getWarnings().size());

		Document actual = xcm.getDocument();

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">");
			sb.append("<vcard>");
				sb.append("<a:lucky-num xmlns:a=\"http://luckynum.com\">24</a:lucky-num>");
				sb.append("<x-salary>1000000</x-salary>");
				sb.append("<x-age><unknown>22</unknown></x-age>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		Document expected = XmlUtils.toDocument(sb.toString());
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void prettyPrint() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedNameType("John Doe"));

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		String actual = xcm.write(2);

		//@formatter:off
		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">").append(newline);
			sb.append("  <vcard>").append(newline);
				sb.append("    <fn>").append(newline);
					sb.append("      <text>John Doe</text>").append(newline);
				sb.append("    </fn>").append(newline);
			sb.append("  </vcard>").append(newline);
		sb.append("</vcards>");
		String expected = sb.toString();
		//@formatter:on

		//use "String.contains()" to ignore the XML declaration at the top
		assertTrue("Expected:" + newline + expected + newline + newline + "Actual:" + newline + actual, actual.contains(expected));
	}

	@Test
	public void kind_and_member_combination() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addMember(new MemberType("http://uri.com"));

		//correct KIND
		{
			vcard.setKind(KindType.group());

			XCardDocument doc = new XCardDocument();
			doc.addVCard(vcard);
			String xml = doc.write();

			List<String> warnings = doc.getWarnings();
			assertTrue(warnings.isEmpty());

			VCard parsedVCard = Ezvcard.parseXml(xml).first();
			assertEquals("group", parsedVCard.getKind().getValue());
			assertEquals(1, parsedVCard.getMembers().size());
			assertEquals("http://uri.com", parsedVCard.getMembers().get(0).getUri());
		}

		//wrong KIND
		{
			vcard.setKind(KindType.individual());

			XCardDocument doc = new XCardDocument();
			doc.addVCard(vcard);
			String xml = doc.write();

			List<String> warnings = doc.getWarnings();
			assertEquals(1, warnings.size());

			VCard parsedVCard = Ezvcard.parseXml(xml).first();
			assertEquals("individual", parsedVCard.getKind().getValue());
			assertTrue(parsedVCard.getMembers().isEmpty());
		}
	}
}
