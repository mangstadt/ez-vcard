package ezvcard.io;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.assertWarningsLists;
import static ezvcard.util.VCardStringUtils.NEWLINE;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.AnniversaryType;
import ezvcard.types.BirthdayType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GenderType;
import ezvcard.types.GeoType;
import ezvcard.types.KeyType;
import ezvcard.types.LanguageType;
import ezvcard.types.NoteType;
import ezvcard.types.OrganizationType;
import ezvcard.types.PhotoType;
import ezvcard.types.ProdIdType;
import ezvcard.types.RawType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TimezoneType;
import ezvcard.types.UrlType;
import ezvcard.types.VCardType;
import ezvcard.types.XmlType;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.XCardElement;
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
	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void parseAll() throws Throwable {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
				"<n>" +
					"<surname>House</surname>" +
					"<given>Gregory</given>" +
					"<additional />" +
					"<prefix>Dr</prefix>" +
					"<prefix>Mr</prefix>" +
					"<suffix>MD</suffix>" +
				"</n>" +
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
				"<n>" +
					"<surname>Cuddy</surname>" +
					"<given>Lisa</given>" +
					"<additional />" +
					"<prefix>Dr</prefix>" +
					"<prefix>Ms</prefix>" +
					"<suffix>MD</suffix>" +
				"</n>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		Iterator<VCard> it = xcard.parseAll().iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(2, vcard.getAllTypes().size());

			FormattedNameType fn = vcard.getFormattedName();
			assertEquals("Dr. Gregory House M.D.", fn.getValue());

			StructuredNameType n = vcard.getStructuredName();
			assertEquals("House", n.getFamily());
			assertEquals("Gregory", n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
			assertEquals(Arrays.asList("MD"), n.getSuffixes());
		}

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(2, vcard.getAllTypes().size());

			FormattedNameType fn = vcard.getFormattedName();
			assertEquals("Dr. Lisa Cuddy M.D.", fn.getValue());

			StructuredNameType n = vcard.getStructuredName();
			assertEquals("Cuddy", n.getFamily());
			assertEquals("Lisa", n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Dr", "Ms"), n.getPrefixes());
			assertEquals(Arrays.asList("MD"), n.getSuffixes());
		}

		assertFalse(it.hasNext());

		assertWarningsLists(xcard.getParseWarnings(), 0, 0);
	}

	@Test
	public void parse_default_namespace() throws Exception {
		//no namespace
		//@formatter:off
		String xml =
		"<vcards>" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		assertNull(xcard.parseFirst());
		assertWarningsLists(xcard.getParseWarnings());
	}

	@Test
	public void parse_wrong_namespace() throws Exception {
		//wrong namespace
		//@formatter:off
		String xml =
		"<vcards xmlns=\"wrong\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		assertNull(xcard.parseFirst());
		assertWarningsLists(xcard.getParseWarnings());
	}

	@Test
	public void parse_preserve_whitespace() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<note><text>  This \t  is \n   a   note </text></note>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		VCard vcard = xcard.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		assertEquals(1, vcard.getNotes().size());

		NoteType note = vcard.getNotes().get(0);
		assertEquals("  This \t  is \n   a   note ", note.getValue());

		assertWarningsLists(xcard.getParseWarnings(), 0);
	}

	public void parse_parameters() throws Throwable {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				//zero params
				"<note>" +
					"<text>Note 1</text>" +
				"</note>" +
				
				//one param
				"<note>" +
					"<parameters>" +
						"<altid><text>1</text></altid>" +
					"</parameters>" +
					"<text>Hello world!</text>" +
				"</note>" +
				
				//two params
				"<note>" +
					"<parameters>" +
						"<altid><text>1</text></altid>" +
						"<language><language-tag>fr</language-tag></language>" +
					"</parameters>" +
					"<text>Bonjour tout le monde!</text>" +
				"</note>" +
				
				//a param with multiple values
				"<tel>" +
					"<parameters>" +
						"<type>" +
							"<text>work</text>" +
							"<text>voice</text>" +
						"</type>" +
					"</parameters>" +
					"<uri>tel:+1-555-555-1234</uri>" +
				"</tel>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		VCard vcard = xcard.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(4, vcard.getAllTypes().size());

		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType note = it.next();
			assertEquals("Note 1", note.getValue());
			assertTrue(note.getSubTypes().isEmpty());

			note = it.next();
			assertEquals("Hello world!", note.getValue());
			assertEquals(1, note.getSubTypes().size());
			assertEquals("1", note.getAltId());

			note = it.next();
			assertEquals("Bonjour tout le monde!", note.getValue());
			assertEquals(2, note.getSubTypes().size());
			assertEquals("1", note.getAltId());
			assertEquals("fr", note.getLanguage());

			assertFalse(it.hasNext());
		}

		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType tel = it.next();
			assertEquals("+1-555-555-1234", tel.getUri().getNumber());
			assertEquals(2, tel.getSubTypes().size());
			assertSetEquals(tel.getTypes(), TelephoneTypeParameter.WORK, TelephoneTypeParameter.VOICE);

			assertFalse(it.hasNext());
		}

		assertWarningsLists(xcard.getParseWarnings(), 0);
	}

	@Test
	public void parse_group() throws Throwable {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<group name=\"item1\">" +
					"<fn><text>John Doe</text></fn>" +
					"<note><text>Hello world!</text></note>" +
				"</group>" +
				"<note><text>A property without a group</text></note>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		VCard vcard = xcard.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(3, vcard.getAllTypes().size());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("John Doe", fn.getValue());
		assertEquals("item1", fn.getGroup());

		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType note = it.next();
			assertEquals("Hello world!", note.getValue());
			assertEquals("item1", note.getGroup());

			note = it.next();
			assertEquals("A property without a group", note.getValue());
			assertNull(note.getGroup());

			assertFalse(it.hasNext());
		}

		assertWarningsLists(xcard.getParseWarnings(), 0);
	}

	@Test
	public void parse_non_standard_elements() throws Throwable {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				//XmlType (non-standard type that does not start with "x-")
				"<a href=\"http://www.website.com\">website</a>" +
				
				//LuckyNumType (with marshal methods and with QName)
				"<a:lucky-num xmlns:a=\"http://luckynum.com\"><a:num>21</a:num></a:lucky-num>" +
				
				//SalaryType (with marshal methods and without QName)
				"<x-salary><integer>1000000</integer></x-salary>" +
				
				//AgeType (without marshal methods and without QName)
				//should be unmarshalled as XmlType
				"<x-age><integer>24</integer></x-age>" +
				
				//RawType (extended type that starts with "x-")
				"<x-gender><text>m</text></x-gender>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		xcard.registerExtendedType(LuckyNumType.class);
		xcard.registerExtendedType(SalaryType.class);
		xcard.registerExtendedType(AgeType.class);

		VCard vcard = xcard.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(5, vcard.getAllTypes().size());

		{
			Iterator<XmlType> it = vcard.getXmls().iterator();

			XmlType xmlType = it.next();
			assertXMLEqual(XmlUtils.toDocument("<a href=\"http://www.website.com\" xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">website</a>"), xmlType.getDocument());

			xmlType = it.next();
			//X-AGE was not unmarshalled because its type class does not support xCard;
			assertXMLEqual(XmlUtils.toDocument("<x-age xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\"><integer>24</integer></x-age>"), xmlType.getDocument());

			assertFalse(it.hasNext());
		}

		LuckyNumType luckyNum = vcard.getType(LuckyNumType.class);
		assertEquals(21, luckyNum.luckyNum);

		SalaryType salary = vcard.getType(SalaryType.class);
		assertEquals(1000000, salary.salary);

		RawType gender = vcard.getExtendedType("X-GENDER");
		assertEquals("m", gender.getValue());

		//warning for AgeType not supporting xCard
		assertWarningsLists(xcard.getParseWarnings(), 1);
	}

	@Test
	public void parse_skip_vcard_without_namespace() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard xmlns=\"http://example.com\">" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		assertNull(xcard.parseFirst());
		assertWarningsLists(xcard.getParseWarnings());
	}

	@Test
	public void parse_skipMeException() throws Throwable {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" xmlns:a=\"http://luckynum.com\">" +
			"<vcard>" +
				"<a:lucky-num><a:num>24</a:num></a:lucky-num>" +
				"<a:lucky-num><a:num>13</a:num></a:lucky-num>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcr = new XCardDocument(xml);
		xcr.registerExtendedType(LuckyNumType.class);

		VCard vcard = xcr.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getAllTypes().size());

		LuckyNumType luckyNum = vcard.getType(LuckyNumType.class);
		assertEquals(24, luckyNum.luckyNum);

		assertWarningsLists(xcr.getParseWarnings(), 1);
	}

	@Test
	public void parse_not_root() throws Throwable {
		//@formatter:off
		String xml =
		"<foo xmlns=\"http://foobar.com\">" +
			"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
				"<vcard>" +
					"<fn><text>Dr. Gregory House M.D.</text></fn>" +
					"<n>" +
						"<surname>House</surname>" +
						"<given>Gregory</given>" +
						"<additional />" +
						"<prefix>Dr</prefix>" +
						"<prefix>Mr</prefix>" +
						"<suffix>MD</suffix>" +
					"</n>" +
				"</vcard>" +
			"</vcards>" +
		"</foo>";
		//@formatter:on

		XCardDocument xcr = new XCardDocument(xml);
		VCard vcard = xcr.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(2, vcard.getAllTypes().size());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("House", n.getFamily());
		assertEquals("Gregory", n.getGiven());
		assertTrue(n.getAdditional().isEmpty());
		assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
		assertEquals(Arrays.asList("MD"), n.getSuffixes());

		assertWarningsLists(xcr.getParseWarnings(), 0);
	}

	@Test
	public void parse_empty() throws Throwable {
		XCardDocument xcard = new XCardDocument();

		Document actual = xcard.getDocument();
		Document expected = XmlUtils.toDocument("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\"/>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void parse_with_namespace_prefix() throws Throwable {
		//@formatter:off
		String xml =
		"<v:vcards xmlns:v=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<v:vcard>" +
				"<v:fn><v:text>Dr. Gregory House M.D.</v:text></v:fn>" +
				"<v:n>" +
					"<v:surname>House</v:surname>" +
					"<v:given>Gregory</v:given>" +
					"<v:additional />" +
					"<v:prefix>Dr</v:prefix>" +
					"<v:prefix>Mr</v:prefix>" +
					"<v:suffix>MD</v:suffix>" +
				"</v:n>" +
			"</v:vcard>" +
		"</v:vcards>";
		//@formatter:on

		XCardDocument xcr = new XCardDocument(xml);

		VCard vcard = xcr.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(2, vcard.getAllTypes().size());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("House", n.getFamily());
		assertEquals("Gregory", n.getGiven());
		assertTrue(n.getAdditional().isEmpty());
		assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
		assertEquals(Arrays.asList("MD"), n.getSuffixes());

		assertWarningsLists(xcr.getParseWarnings(), 0);
	}

	@Test(expected = RuntimeException.class)
	public void registerExtendedType_no_default_constructor() throws Throwable {
		XCardDocument xcard = new XCardDocument();
		xcard.registerExtendedType(BadType.class);
	}

	/**
	 * A basic test with one type.
	 */
	@Test
	public void addVCard_basicType() throws Throwable {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can marshal parameters.
	 */
	@Test
	public void addVCard_parameters() throws Throwable {
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
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<note>" +
					"<parameters>" +
						"<language><language-tag>en</language-tag></language>" +
						"<pid><text>1.1</text><text>2.2</text></pid>" +
						"<x-custom><unknown>xxx</unknown></x-custom>" +
					"</parameters>" +
					"<text>This is a\nnote.</text>" +
				"</note>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can marshal groups.
	 */
	@Test
	public void addVCard_group() throws Throwable {
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
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
				"<group name=\"group1\">" +
					"<photo>" +
						"<parameters>" +
							"<mediatype><text>image/jpeg</text></mediatype>" +
						"</parameters>" +
						"<uri>http://example.com/image.jpg</uri>" +
					"</photo>" +
					"<note>" +
						"<parameters>" +
							"<language><language-tag>en</language-tag></language>" +
						"</parameters>" +
						"<text>This is a\nnote.</text>" +
					"</note>" +
				"</group>" +
				"<group name=\"group2\">" +
					"<note><text>Bonjour.</text></note>" +
				"</group>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Makes sure it can add multiple vCards to the same document.
	 */
	@Test
	public void addVCard_multiple() throws Throwable {
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
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
			"<vcard>" +
				"<note><text>Hello world!</text></note>" +
				"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void setAddProdId() throws Throwable {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new XCardNamespaceContext(VCardVersion.V4_0, "v"));

		XCardDocument xcm = new XCardDocument();
		xcm.addVCard(vcard);
		Double actual = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", xcm.getDocument(), XPathConstants.NUMBER);
		assertEquals(Double.valueOf(1), actual);

		xcm = new XCardDocument();
		xcm.setAddProdId(true);
		xcm.addVCard(vcard);
		actual = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", xcm.getDocument(), XPathConstants.NUMBER);
		assertEquals(Double.valueOf(1), actual);

		xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);
		actual = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", xcm.getDocument(), XPathConstants.NUMBER);
		assertEquals(Double.valueOf(0), actual);
	}

	@Test
	public void setAddProdId_overwrites_existing_prodId() throws Throwable {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		ProdIdType prodId = new ProdIdType("Acme Co.");
		vcard.setProdId(prodId);

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new XCardNamespaceContext(VCardVersion.V4_0, "v"));

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);
		String actual = (String) xpath.evaluate("/v:vcards/v:vcard/v:prodid/v:text", xcm.getDocument(), XPathConstants.STRING);
		assertEquals(prodId.getValue(), actual);

		xcm = new XCardDocument();
		xcm.setAddProdId(true);
		xcm.addVCard(vcard);
		actual = (String) xpath.evaluate("/v:vcards/v:vcard/v:prodid/v:text", xcm.getDocument(), XPathConstants.STRING);
		assertEquals(new EzvcardProdIdType(VCardVersion.V4_0).getValue(), actual);
	}

	/**
	 * If the type's marshal method throws a {@link SkipMeException}, then a
	 * warning should be added to the warnings list and the type object should
	 * NOT be marshalled.
	 */
	@Test
	public void addVCard_skipMeException() throws Throwable {
		VCard vcard = new VCard();

		//add FN property so a warning isn't generated (4.0 requires FN to be present)
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		LuckyNumType num = new LuckyNumType();
		num.luckyNum = 24;
		vcard.addType(num);

		//should be skipped
		num = new LuckyNumType();
		num.luckyNum = 13;
		vcard.addType(num);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" xmlns:a=\"http://luckynum.com\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
				"<a:lucky-num>24</a:lucky-num>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * Tests how extended types are marshalled.
	 */
	@Test
	public void addVCard_extendedTypes() throws Throwable {
		VCard vcard = new VCard();

		//contains marshal methods and QName
		LuckyNumType num = new LuckyNumType();
		num.luckyNum = 24;
		vcard.addType(num);

		//contains marshal methods, but does not have a QName
		SalaryType salary = new SalaryType();
		salary.salary = 1000000;
		vcard.addType(salary);

		//does not contain marshal methods nor QName
		AgeType age = new AgeType();
		age.age = 22;
		vcard.addType(age);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<a:lucky-num xmlns:a=\"http://luckynum.com\">24</a:lucky-num>" +
				"<x-salary>1000000</x-salary>" +
				"<x-age><unknown>22</unknown></x-age>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void write_prettyPrint() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedNameType("John Doe"));

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.addVCard(vcard);

		String actual = xcm.write(2);

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" + NEWLINE +
		"  <vcard>" + NEWLINE +
		"    <fn>" + NEWLINE +
		"      <text>John Doe</text>" + NEWLINE +
		"    </fn>" + NEWLINE +
		"  </vcard>" + NEWLINE +
		"</vcards>";
		//@formatter:on

		//use "String.contains()" to ignore the XML declaration at the top
		assertTrue("Expected:" + NEWLINE + expected + NEWLINE + NEWLINE + "Actual:" + NEWLINE + actual, actual.contains(expected));
	}

	@Test
	public void addVCard_embedded_vcards_not_supported() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addType(new EmbeddedType());

		XCardDocument doc = new XCardDocument();
		doc.addVCard(vcard);

		VCard parsedVCard = Ezvcard.parseXml(doc.write()).first();
		assertTrue(parsedVCard.getExtendedTypes().isEmpty());
	}

	@Test
	public void write_rfc6351_example() throws Throwable {
		VCard vcard = new VCard();

		vcard.setFormattedName("Simon Perreault");

		StructuredNameType n = new StructuredNameType();
		n.setFamily("Perreault");
		n.setGiven("Simon");
		n.addSuffix("ing. jr");
		n.addSuffix("M.Sc.");
		vcard.setStructuredName(n);

		BirthdayType bday = new BirthdayType();
		bday.setPartialDate(PartialDate.date(null, 2, 3));
		vcard.setBirthday(bday);

		AnniversaryType anniversary = new AnniversaryType();
		anniversary.setPartialDate(PartialDate.dateTime(2009, 8, 8, 14, 30, null, -5, 0));
		vcard.setAnniversary(anniversary);

		vcard.setGender(GenderType.male());

		vcard.addLanguage("fr").setPref(1);
		vcard.addLanguage("en").setPref(2);

		vcard.setOrganization("Viagenie").setType("work");

		AddressType adr = new AddressType();
		adr.setStreetAddress("2875 boul. Laurier, suite D2-630");
		adr.setLocality("Quebec");
		adr.setRegion("QC");
		adr.setPostalCode("G1V 2M2");
		adr.setCountry("Canada");
		adr.addType(AddressTypeParameter.WORK);
		adr.setLabel("Simon Perreault\n2875 boul. Laurier, suite D2-630\nQuebec, QC, Canada\nG1V 2M2");
		vcard.addAddress(adr);

		TelUri telUri = TelUri.global("+1-418-656-9254");
		telUri.setExtension("102");
		TelephoneType tel = new TelephoneType(telUri);
		tel.addType(TelephoneTypeParameter.WORK);
		tel.addType(TelephoneTypeParameter.VOICE);
		vcard.addTelephoneNumber(tel);

		tel = new TelephoneType(TelUri.global("+1-418-262-6501"));
		tel.addType(TelephoneTypeParameter.WORK);
		tel.addType(TelephoneTypeParameter.TEXT);
		tel.addType(TelephoneTypeParameter.VOICE);
		tel.addType(TelephoneTypeParameter.CELL);
		tel.addType(TelephoneTypeParameter.VIDEO);
		vcard.addTelephoneNumber(tel);

		vcard.addEmail("simon.perreault@viagenie.ca", EmailTypeParameter.WORK);

		GeoType geo = new GeoType(46.766336, -71.28955);
		geo.setType("work");
		vcard.setGeo(geo);

		KeyType key = new KeyType("http://www.viagenie.ca/simon.perreault/simon.asc", null);
		key.setType("work");
		vcard.addKey(key);

		vcard.setTimezone(new TimezoneType("America/Montreal"));

		vcard.addUrl("http://nomis80.org").setType("home");

		assertValidate(vcard.validate(VCardVersion.V4_0));

		assertExample(vcard, "rfc6351-example.xml");
	}

	@Test
	public void read_rfc6351_example() throws Throwable {
		XCardDocument xcard = new XCardDocument(getClass().getResourceAsStream("rfc6351-example.xml"));

		List<VCard> vcards = xcard.parseAll();
		assertEquals(1, vcards.size());

		VCard vcard = vcards.get(0);
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(16, vcard.getAllTypes().size());

		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("Perreault", n.getFamily());
		assertEquals("Simon", n.getGiven());
		assertEquals(Arrays.asList(), n.getAdditional());
		assertEquals(Arrays.asList(), n.getPrefixes());
		assertEquals(Arrays.asList("ing. jr", "M.Sc."), n.getSuffixes());

		PartialDate expectedBday = PartialDate.date(null, 2, 3);
		PartialDate actualBday = vcard.getBirthday().getPartialDate();
		assertEquals(expectedBday, actualBday);

		PartialDate expectedAnniversary = PartialDate.dateTime(2009, 8, 8, 14, 30, null, -5, 0);
		PartialDate actualAnniversary = vcard.getAnniversary().getPartialDate();
		assertEquals(expectedAnniversary, actualAnniversary);

		assertTrue(vcard.getGender().isMale());

		LanguageType lang = vcard.getLanguages().get(0);
		assertEquals("fr", lang.getValue());
		assertIntEquals(1, lang.getPref());

		lang = vcard.getLanguages().get(1);
		assertEquals("en", lang.getValue());
		assertIntEquals(2, lang.getPref());

		OrganizationType org = vcard.getOrganization();
		assertEquals(Arrays.asList("Viagenie"), org.getValues());
		assertEquals("work", org.getType());

		AddressType adr = vcard.getAddresses().get(0);
		assertNull(adr.getPoBox());
		assertNull(adr.getExtendedAddress());
		assertEquals("2875 boul. Laurier, suite D2-630", adr.getStreetAddress());
		assertEquals("Quebec", adr.getLocality());
		assertEquals("QC", adr.getRegion());
		assertEquals("G1V 2M2", adr.getPostalCode());
		assertEquals("Canada", adr.getCountry());
		assertEquals("Simon Perreault\n2875 boul. Laurier, suite D2-630\nQuebec, QC, Canada\nG1V 2M2", adr.getLabel());
		assertSetEquals(adr.getTypes(), AddressTypeParameter.WORK);

		TelephoneType tel = vcard.getTelephoneNumbers().get(0);
		TelUri expectedUri = TelUri.global("+1-418-656-9254");
		expectedUri.setExtension("102");
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneTypeParameter.WORK, TelephoneTypeParameter.VOICE);

		tel = vcard.getTelephoneNumbers().get(1);
		expectedUri = TelUri.global("+1-418-262-6501");
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneTypeParameter.WORK, TelephoneTypeParameter.VOICE, TelephoneTypeParameter.CELL, TelephoneTypeParameter.VIDEO, TelephoneTypeParameter.TEXT);

		EmailType email = vcard.getEmails().get(0);
		assertEquals("simon.perreault@viagenie.ca", email.getValue());
		assertSetEquals(email.getTypes(), EmailTypeParameter.WORK);

		GeoType geo = vcard.getGeo();
		assertEquals(Double.valueOf(46.766336), geo.getLatitude());
		assertEquals(Double.valueOf(-71.28955), geo.getLongitude());
		assertEquals("work", geo.getType());

		KeyType key = vcard.getKeys().get(0);
		assertEquals("http://www.viagenie.ca/simon.perreault/simon.asc", key.getUrl());
		assertEquals("work", key.getType());

		assertEquals("America/Montreal", vcard.getTimezone().getText());

		UrlType url = vcard.getUrls().get(0);
		assertEquals("http://nomis80.org", url.getValue());
		assertEquals("home", url.getType());

		assertValidate(vcard.validate(VCardVersion.V4_0));
		assertWarningsLists(xcard.getParseWarnings(), 0);
	}

	private void assertExample(VCard vcard, String exampleFileName) throws IOException, SAXException {
		XCardDocument xcard = new XCardDocument();
		xcard.setAddProdId(false);
		xcard.addVCard(vcard);

		Document expected = XmlUtils.toDocument(new InputStreamReader(getClass().getResourceAsStream(exampleFileName)));
		Document actual = xcard.getDocument();

		assertXMLEqual(XmlUtils.toString(actual), expected, actual);
	}

	private static class EmbeddedType extends VCardType {
		public EmbeddedType() {
			super("EMBEDDED");
		}

		@Override
		protected void doMarshalText(StringBuilder value, VCardVersion version, CompatibilityMode compatibilityMode) {
			//do nothing
		}

		@Override
		protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			//do nothing
		}

		@Override
		protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
			throw new EmbeddedVCardException(new VCard());
		}
	}
}
