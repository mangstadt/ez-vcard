package ezvcard.io.xml;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.AgeType;
import ezvcard.io.AgeType.AgeScribe;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.LuckyNumType;
import ezvcard.io.LuckyNumType.LuckyNumScribe;
import ezvcard.io.MyFormattedNameType;
import ezvcard.io.MyFormattedNameType.MyFormattedNameScribe;
import ezvcard.io.SalaryType;
import ezvcard.io.SalaryType.SalaryScribe;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Language;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.ProductId;
import ezvcard.property.RawProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;
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
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

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

			FormattedName fn = vcard.getFormattedName();
			assertEquals("Dr. Gregory House M.D.", fn.getValue());

			StructuredName n = vcard.getStructuredName();
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

			FormattedName fn = vcard.getFormattedName();
			assertEquals("Dr. Lisa Cuddy M.D.", fn.getValue());

			StructuredName n = vcard.getStructuredName();
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

		Note note = vcard.getNotes().get(0);
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
			Iterator<Note> it = vcard.getNotes().iterator();

			Note note = it.next();
			assertEquals("Note 1", note.getValue());
			assertTrue(note.getParameters().isEmpty());

			note = it.next();
			assertEquals("Hello world!", note.getValue());
			assertEquals(1, note.getParameters().size());
			assertEquals("1", note.getAltId());

			note = it.next();
			assertEquals("Bonjour tout le monde!", note.getValue());
			assertEquals(2, note.getParameters().size());
			assertEquals("1", note.getAltId());
			assertEquals("fr", note.getLanguage());

			assertFalse(it.hasNext());
		}

		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

			Telephone tel = it.next();
			assertEquals("+1-555-555-1234", tel.getUri().getNumber());
			assertEquals(2, tel.getParameters().size());
			assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

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

		FormattedName fn = vcard.getFormattedName();
		assertEquals("John Doe", fn.getValue());
		assertEquals("item1", fn.getGroup());

		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note note = it.next();
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
				//xCard namespace:  no
				//scribe:           no
				//expected:         XML property
				"<foo xmlns=\"http://example.com\">bar</foo>" +
				
				//xCard namespace:  no
				//scribe:           yes
				//parseXml impl:    yes
				//expected:         LuckyNumType
				"<a:lucky-num xmlns:a=\"http://luckynum.com\"><a:num>21</a:num></a:lucky-num>" +
				
				//xCard namespace:  yes
				//scribe:           yes
				//parseXml impl:    yes
				//expected:         SalaryType
				"<x-salary><integer>1000000</integer></x-salary>" +
				
				//xCard namespace:  yes
				//parseXml impl:    no
				//expected:         AgeType (should be unmarshalled using the default parseXml implementation)
				"<x-age><integer>24</integer></x-age>" +
				
				//xCard namespace:  yes
				//scribe:           no
				//expected:         RawProperty
				"<x-gender><text>m</text></x-gender>" +
				
				//xCard namespace:  yes
				//scribe:           yes (standard scribe overridden)
				//expected:         MyFormattedNameType
				"<fn><name>John Doe</name></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		xcard.registerScribe(new LuckyNumScribe());
		xcard.registerScribe(new SalaryScribe());
		xcard.registerScribe(new AgeScribe());
		xcard.registerScribe(new MyFormattedNameScribe());

		VCard vcard = xcard.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(6, vcard.getAllTypes().size());

		{
			Iterator<Xml> it = vcard.getXmls().iterator();

			Xml xmlType = it.next();
			assertXMLEqual(XmlUtils.toDocument("<foo xmlns=\"http://example.com\">bar</foo>"), xmlType.getValue());

			assertFalse(it.hasNext());
		}

		LuckyNumType luckyNum = vcard.getType(LuckyNumType.class);
		assertEquals(21, luckyNum.luckyNum);

		SalaryType salary = vcard.getType(SalaryType.class);
		assertEquals(1000000, salary.salary);

		AgeType age = vcard.getType(AgeType.class);
		assertEquals(24, age.age);

		RawProperty gender = vcard.getExtendedType("X-GENDER");
		assertEquals("m", gender.getValue());

		MyFormattedNameType fn = vcard.getType(MyFormattedNameType.class);
		assertEquals("JOHN DOE", fn.value);

		//warning for AgeType not supporting xCard
		assertWarningsLists(xcard.getParseWarnings(), 0);
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
		xcr.registerScribe(new LuckyNumScribe());

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

		FormattedName fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		StructuredName n = vcard.getStructuredName();
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

		FormattedName fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		StructuredName n = vcard.getStructuredName();
		assertEquals("House", n.getFamily());
		assertEquals("Gregory", n.getGiven());
		assertTrue(n.getAdditional().isEmpty());
		assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
		assertEquals(Arrays.asList("MD"), n.getSuffixes());

		assertWarningsLists(xcr.getParseWarnings(), 0);
	}

	@Test
	public void parse_utf8() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
					"<note><text>\u019dote</text></note>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on
		File file = tempFolder.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write(xml);
		writer.close();

		XCardDocument xcard = new XCardDocument(file);
		VCard vcard = xcard.parseFirst();
		assertEquals("\u019dote", vcard.getNotes().get(0).getValue());
	}

	/**
	 * A basic test with one type.
	 */
	@Test
	public void add_basicType() throws Throwable {
		VCard vcard = new VCard();
		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.add(vcard);

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

	@Test
	public void add_parameters() throws Throwable {
		VCard vcard = new VCard();
		Note note = new Note("This is a\nnote.");
		note.setLanguage("en");
		note.addPid(1, 1);
		note.addPid(2, 2);
		note.getParameters().put("X-CUSTOM", "xxx");
		note.getParameters().put("X-INT", "11");
		vcard.addNote(note);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.registerParameterDataType("X-INT", VCardDataType.INTEGER);
		xcm.add(vcard);

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
						"<x-int><integer>11</integer></x-int>" +
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
	public void add_group() throws Throwable {
		VCard vcard = new VCard();

		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		Note note = new Note("This is a\nnote.");
		note.setGroup("group1");
		note.setLanguage("en");
		vcard.addNote(note);

		Photo photo = new Photo("http://example.com/image.jpg", ImageType.JPEG);
		photo.setGroup("group1");
		vcard.addPhoto(photo);

		note = new Note("Bonjour.");
		note.setGroup("group2");
		vcard.addNote(note);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.add(vcard);

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
	public void add_multiple() throws Throwable {
		VCard vcard1 = new VCard();
		FormattedName fn = new FormattedName("John Doe");
		vcard1.setFormattedName(fn);

		VCard vcard2 = new VCard();
		Note note = new Note("Hello world!");
		vcard2.addNote(note);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.add(vcard1);
		xcm.add(vcard2);

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
		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new XCardNamespaceContext(VCardVersion.V4_0, "v"));

		XCardDocument xcm = new XCardDocument();
		xcm.add(vcard);
		Double actual = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", xcm.getDocument(), XPathConstants.NUMBER);
		assertEquals(Double.valueOf(1), actual);

		xcm = new XCardDocument();
		xcm.setAddProdId(true);
		xcm.add(vcard);
		actual = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", xcm.getDocument(), XPathConstants.NUMBER);
		assertEquals(Double.valueOf(1), actual);

		xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.add(vcard);
		actual = (Double) xpath.evaluate("count(/v:vcards/v:vcard/v:prodid)", xcm.getDocument(), XPathConstants.NUMBER);
		assertEquals(Double.valueOf(0), actual);
	}

	@Test
	public void setAddProdId_overwrites_existing_prodId() throws Throwable {
		VCard vcard = new VCard();

		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		ProductId prodId = new ProductId("Acme Co.");
		vcard.setProdId(prodId);

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new XCardNamespaceContext(VCardVersion.V4_0, "v"));

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.add(vcard);
		String actual = (String) xpath.evaluate("/v:vcards/v:vcard/v:prodid/v:text", xcm.getDocument(), XPathConstants.STRING);
		assertEquals(prodId.getValue(), actual);

		xcm = new XCardDocument();
		xcm.setAddProdId(true);
		xcm.add(vcard);
		actual = (String) xpath.evaluate("/v:vcards/v:vcard/v:prodid/v:text", xcm.getDocument(), XPathConstants.STRING);
		assertTrue("Actual: " + actual, actual.startsWith("ez-vcard"));
	}

	@Test
	public void setVersionStrict() throws Throwable {
		VCard vcard = new VCard();
		vcard.setMailer("mailer"); //only supported by 2.1 and 3.0

		XCardDocument xcard = new XCardDocument();
		xcard.setAddProdId(false);

		xcard.add(vcard);

		xcard.setVersionStrict(false);
		xcard.add(vcard);

		xcard.setVersionStrict(true);
		xcard.add(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard/>" +
			"<vcard>" +
				"<mailer><text>mailer</text></mailer>" +
			"</vcard>" +
			"<vcard/>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	/**
	 * If the type's marshal method throws a {@link SkipMeException}, then a
	 * warning should be added to the warnings list and the type object should
	 * NOT be marshalled.
	 */
	@Test
	public void add_skipMeException() throws Throwable {
		VCard vcard = new VCard();

		//add FN property so a warning isn't generated (4.0 requires FN to be present)
		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		LuckyNumType num = new LuckyNumType(24);
		vcard.addType(num);

		//should be skipped
		num = new LuckyNumType(13);
		vcard.addType(num);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.registerScribe(new LuckyNumScribe());
		xcm.add(vcard);

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

	@Test(expected = IllegalArgumentException.class)
	public void add_no_scribe_registered() throws Throwable {
		VCard vcard = new VCard();

		LuckyNumType num = new LuckyNumType(24);
		vcard.addType(num);

		XCardDocument xcm = new XCardDocument();
		xcm.add(vcard);
	}

	/**
	 * Tests how extended types are marshalled.
	 */
	@Test
	public void add_extendedTypes() throws Throwable {
		VCard vcard = new VCard();

		//contains marshal methods and QName
		LuckyNumType num = new LuckyNumType(24);
		vcard.addType(num);

		//contains marshal methods, but does not have a QName
		SalaryType salary = new SalaryType(1000000);
		vcard.addType(salary);

		//does not contain marshal methods nor QName
		AgeType age = new AgeType(22);
		vcard.addType(age);

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.registerScribe(new LuckyNumScribe());
		xcm.registerScribe(new SalaryScribe());
		xcm.registerScribe(new AgeScribe());
		xcm.add(vcard);

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

		assertXMLEqual(XmlUtils.toString(actual), expected, actual);
	}

	@Test
	public void write_prettyPrint() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName(new FormattedName("John Doe"));

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.add(vcard);

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
	public void write_utf8() throws Throwable {
		VCard vcard = new VCard();
		vcard.addNote("\u019dote");

		XCardDocument xcard = new XCardDocument();
		xcard.add(vcard);

		File file = tempFolder.newFile();
		xcard.write(file);

		String xml = IOUtils.getFileContents(file, "UTF-8");
		assertTrue(xml.matches("(?i)<\\?xml.*?encoding=\"utf-8\".*?\\?>.*"));
		assertTrue(xml.matches(".*?<note><text>\u019dote</text></note>.*"));
	}

	@Test
	public void add_embedded_vcards_not_supported() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addType(new EmbeddedType());

		XCardDocument doc = new XCardDocument();
		doc.registerScribe(new EmbeddedTypeScribe());
		doc.add(vcard);

		VCard parsedVCard = Ezvcard.parseXml(doc.write()).first();
		assertTrue(parsedVCard.getExtendedTypes().isEmpty());
	}

	@Test
	public void write_rfc6351_example() throws Throwable {
		VCard vcard = new VCard();

		vcard.setFormattedName("Simon Perreault");

		StructuredName n = new StructuredName();
		n.setFamily("Perreault");
		n.setGiven("Simon");
		n.addSuffix("ing. jr");
		n.addSuffix("M.Sc.");
		vcard.setStructuredName(n);

		Birthday bday = new Birthday(PartialDate.date(null, 2, 3));
		vcard.setBirthday(bday);

		Anniversary anniversary = new Anniversary(PartialDate.dateTime(2009, 8, 8, 14, 30, null, new UtcOffset(-5, 0)));
		vcard.setAnniversary(anniversary);

		vcard.setGender(Gender.male());

		vcard.addLanguage("fr").setPref(1);
		vcard.addLanguage("en").setPref(2);

		vcard.setOrganization("Viagenie").setType("work");

		Address adr = new Address();
		adr.setStreetAddress("2875 boul. Laurier, suite D2-630");
		adr.setLocality("Quebec");
		adr.setRegion("QC");
		adr.setPostalCode("G1V 2M2");
		adr.setCountry("Canada");
		adr.addType(AddressType.WORK);
		adr.setLabel("Simon Perreault\n2875 boul. Laurier, suite D2-630\nQuebec, QC, Canada\nG1V 2M2");
		vcard.addAddress(adr);

		TelUri telUri = new TelUri.Builder("+1-418-656-9254").extension("102").build();
		Telephone tel = new Telephone(telUri);
		tel.addType(TelephoneType.WORK);
		tel.addType(TelephoneType.VOICE);
		vcard.addTelephoneNumber(tel);

		tel = new Telephone(new TelUri.Builder("+1-418-262-6501").build());
		tel.addType(TelephoneType.WORK);
		tel.addType(TelephoneType.TEXT);
		tel.addType(TelephoneType.VOICE);
		tel.addType(TelephoneType.CELL);
		tel.addType(TelephoneType.VIDEO);
		vcard.addTelephoneNumber(tel);

		vcard.addEmail("simon.perreault@viagenie.ca", EmailType.WORK);

		Geo geo = new Geo(46.766336, -71.28955);
		geo.setType("work");
		vcard.setGeo(geo);

		Key key = new Key("http://www.viagenie.ca/simon.perreault/simon.asc", null);
		key.setType("work");
		vcard.addKey(key);

		vcard.setTimezone(new Timezone("America/Montreal"));

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

		StructuredName n = vcard.getStructuredName();
		assertEquals("Perreault", n.getFamily());
		assertEquals("Simon", n.getGiven());
		assertEquals(Arrays.asList(), n.getAdditional());
		assertEquals(Arrays.asList(), n.getPrefixes());
		assertEquals(Arrays.asList("ing. jr", "M.Sc."), n.getSuffixes());

		PartialDate expectedBday = PartialDate.date(null, 2, 3);
		PartialDate actualBday = vcard.getBirthday().getPartialDate();
		assertEquals(expectedBday, actualBday);

		PartialDate expectedAnniversary = PartialDate.dateTime(2009, 8, 8, 14, 30, null, new UtcOffset(-5, 0));
		PartialDate actualAnniversary = vcard.getAnniversary().getPartialDate();
		assertEquals(expectedAnniversary, actualAnniversary);

		assertTrue(vcard.getGender().isMale());

		Language lang = vcard.getLanguages().get(0);
		assertEquals("fr", lang.getValue());
		assertIntEquals(1, lang.getPref());

		lang = vcard.getLanguages().get(1);
		assertEquals("en", lang.getValue());
		assertIntEquals(2, lang.getPref());

		Organization org = vcard.getOrganization();
		assertEquals(Arrays.asList("Viagenie"), org.getValues());
		assertEquals("work", org.getType());

		Address adr = vcard.getAddresses().get(0);
		assertNull(adr.getPoBox());
		assertNull(adr.getExtendedAddress());
		assertEquals("2875 boul. Laurier, suite D2-630", adr.getStreetAddress());
		assertEquals("Quebec", adr.getLocality());
		assertEquals("QC", adr.getRegion());
		assertEquals("G1V 2M2", adr.getPostalCode());
		assertEquals("Canada", adr.getCountry());
		assertEquals("Simon Perreault\n2875 boul. Laurier, suite D2-630\nQuebec, QC, Canada\nG1V 2M2", adr.getLabel());
		assertSetEquals(adr.getTypes(), AddressType.WORK);

		Telephone tel = vcard.getTelephoneNumbers().get(0);
		TelUri expectedUri = new TelUri.Builder("+1-418-656-9254").extension("102").build();
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

		tel = vcard.getTelephoneNumbers().get(1);
		expectedUri = new TelUri.Builder("+1-418-262-6501").build();
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE, TelephoneType.CELL, TelephoneType.VIDEO, TelephoneType.TEXT);

		Email email = vcard.getEmails().get(0);
		assertEquals("simon.perreault@viagenie.ca", email.getValue());
		assertSetEquals(email.getTypes(), EmailType.WORK);

		Geo geo = vcard.getGeo();
		assertEquals(Double.valueOf(46.766336), geo.getLatitude());
		assertEquals(Double.valueOf(-71.28955), geo.getLongitude());
		assertEquals("work", geo.getType());

		Key key = vcard.getKeys().get(0);
		assertEquals("http://www.viagenie.ca/simon.perreault/simon.asc", key.getUrl());
		assertEquals("work", key.getType());

		assertEquals("America/Montreal", vcard.getTimezone().getText());

		Url url = vcard.getUrls().get(0);
		assertEquals("http://nomis80.org", url.getValue());
		assertEquals("home", url.getType());

		assertValidate(vcard.validate(VCardVersion.V4_0));
		assertWarningsLists(xcard.getParseWarnings(), 0);
	}

	private void assertExample(VCard vcard, String exampleFileName) throws IOException, SAXException {
		XCardDocument xcard = new XCardDocument();
		xcard.setAddProdId(false);
		xcard.add(vcard);

		Document expected = XmlUtils.toDocument(new InputStreamReader(getClass().getResourceAsStream(exampleFileName)));
		Document actual = xcard.getDocument();

		assertXMLEqual(XmlUtils.toString(actual), expected, actual);
	}

	private static class EmbeddedType extends VCardProperty {
		//empty
	}

	private static class EmbeddedTypeScribe extends VCardPropertyScribe<EmbeddedType> {
		public EmbeddedTypeScribe() {
			super(EmbeddedType.class, "EMBEDDED");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return null;
		}

		@Override
		protected String _writeText(EmbeddedType property, VCardVersion version) {
			return null;
		}

		@Override
		protected EmbeddedType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			return null;
		}

		@Override
		protected void _writeXml(EmbeddedType property, XCardElement parent) {
			throw new EmbeddedVCardException(new VCard());
		}
	}
}
