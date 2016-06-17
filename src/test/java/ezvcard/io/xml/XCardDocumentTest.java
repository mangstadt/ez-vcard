package ezvcard.io.xml;

import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertValidate;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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
import ezvcard.io.AgeProperty;
import ezvcard.io.AgeProperty.AgeScribe;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.LuckyNumProperty;
import ezvcard.io.LuckyNumProperty.LuckyNumScribe;
import ezvcard.io.SalaryProperty;
import ezvcard.io.SalaryProperty.SalaryScribe;
import ezvcard.io.StreamReader;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardDocument.XCardDocumentStreamWriter;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.Pid;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Language;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.ProductId;
import ezvcard.property.SkipMeProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
import ezvcard.property.asserter.VCardAsserter;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;
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
public class XCardDocumentTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void getVCards_multiple() throws Throwable {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>"
		);

		asserter.next(V4_0);
		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Gregory House M.D.")
		.noMore();
	
		asserter.next(V4_0);
		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Lisa Cuddy M.D.")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void getVCards_single() throws Throwable {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>"
		);
		
		asserter.next(V4_0);
		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Gregory House M.D.")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void add_basicType() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
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
		note.setParameter(VCardParameters.ALTID, "value");
		note.setParameter(VCardParameters.CALSCALE, "value");
		note.setParameter(VCardParameters.GEO, "geo:123.456,234.567");
		note.setParameter(VCardParameters.LABEL, "value");
		note.setParameter(VCardParameters.LANGUAGE, "en");
		note.setParameter(VCardParameters.MEDIATYPE, "text/plain");
		note.getPids().add(new Pid(1, 1));
		note.setParameter(VCardParameters.PREF, "1");
		note.setParameter(VCardParameters.SORT_AS, "value");
		note.setParameter(VCardParameters.TYPE, "home");
		note.setParameter(VCardParameters.TZ, "America/New_York");
		note.setParameter("X-CUSTOM", "xxx");
		note.setParameter("X-INT", "11");
		vcard.addNote(note);

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.registerParameterDataType("X-INT", VCardDataType.INTEGER);
		writer.registerParameterDataType("X-CUSTOM", VCardDataType.BOOLEAN);
		writer.registerParameterDataType("X-CUSTOM", null);
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<note>" +
					"<parameters>" +
						"<altid><text>value</text></altid>" +
						"<calscale><text>value</text></calscale>" +
						"<geo><uri>geo:123.456,234.567</uri></geo>" +
						"<label><text>value</text></label>" +
						"<language><language-tag>en</language-tag></language>" +
						"<mediatype><text>text/plain</text></mediatype>" +
						"<pid><text>1.1</text></pid>" +
						"<pref><integer>1</integer></pref>" +
						"<sort-as><text>value</text></sort-as>" +
						"<type><text>home</text></type>" +
						"<tz><uri>America/New_York</uri></tz>" +
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

	@Test
	public void read_parameters() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
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
					
				//one param, but doesn't have a value element, so it should be ignored
				"<note>" +
					"<parameters>" +
						"<altid>1</altid>" +
					"</parameters>" +
					"<text>Hallo Welt!</text>" +
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
		"</vcards>"
		);

		asserter.next(V4_0);

		asserter.simpleProperty(Note.class)
			.value("Note 1")
		.next()
			.value("Hello world!")
			.param("ALTID", "1")
		.next()
			.value("Hallo Welt!")
		.next()
			.value("Bonjour tout le monde!")
			.param("ALTID", "1")
			.param("LANGUAGE", "fr")
		.noMore();
		
		asserter.telephone()
			.uri(new TelUri.Builder("+1-555-555-1234").build())
			.types(TelephoneType.WORK, TelephoneType.VOICE)
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void add_group() throws Throwable {
		VCard vcard = new VCard();

		vcard.setFormattedName("John Doe");

		Note note = vcard.addNote("This is a\nnote.");
		note.setGroup("group1");
		note.setLanguage("en");

		Photo photo = new Photo("http://example.com/image.jpg", ImageType.JPEG);
		photo.setGroup("group1");
		vcard.addPhoto(photo);

		note = vcard.addNote("Bonjour.");
		note.setGroup("group2");

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
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

	@Test
	public void read_groups() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<group name=\"item1\">" +
					"<fn><text>John Doe</text></fn>" +
					"<note><text>Hello world!</text></note>" +
				"</group>" +
				"<group>" +
					"<prodid><text>no name attribute</text></prodid>" +
				"</group>" +
				"<note><text>A property without a group</text></note>" +
			"</vcard>" +
		"</vcards>"
		);

		asserter.next(V4_0);

		asserter.simpleProperty(FormattedName.class)
			.group("item1")
			.value("John Doe")
		.noMore();
		
		asserter.simpleProperty(Note.class)
			.group("item1")
			.value("Hello world!")
		.next()
			.value("A property without a group")
		.noMore();
		
		asserter.simpleProperty(ProductId.class)
			.value("no name attribute")
		.noMore();
		
		asserter.done();
		//@formatter:on
	}

	@Test
	public void add_multiple() throws Throwable {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName("John Doe");

		VCard vcard2 = new VCard();
		vcard2.addNote("Hello world!");

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard1);
		writer.write(vcard2);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
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
	public void add_skipMeException() throws Throwable {
		VCard vcard = new VCard();
		vcard.addProperty(new SkipMeProperty());
		vcard.addExtendedProperty("x-foo", "value");

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.registerScribe(new SkipMeScribe());
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<x-foo><unknown>value</unknown></x-foo>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void add_no_scribe_registered() throws Throwable {
		VCard vcard = new VCard();

		SkipMeProperty property = new SkipMeProperty();
		vcard.addProperty(property);

		XCardDocument xcard = new XCardDocument();
		xcard.addVCard(vcard);
	}

	@Test
	public void add_xml_property() throws Throwable {
		VCard vcard = new VCard();

		Xml xmlProperty = new Xml("<foo xmlns=\"http://example.com\" a=\"b\">bar<car/></foo>");
		xmlProperty.addParameter("name", "value");
		vcard.addXml(xmlProperty);

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<foo xmlns=\"http://example.com\" a=\"b\">" +
					"<parameters xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
						"<name><unknown>value</unknown></name>" +
					"</parameters>" +
					"bar<car/>" +
				"</foo>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		/*
		 * When using xalan as the JAXP parser, XMLUnit thinks the <name>
		 * element in the "actual" DOM has the wrong namespace. But when you
		 * inspect the DOM yourself, the <name> element *does* have the correct
		 * namespace!
		 * 
		 * As a workaround, let's compare the string versions of the two DOMs.
		 */
		assertEquals(XmlUtils.toString(expected), XmlUtils.toString(actual));
		//assertXMLEqual(expected, actual);
	}

	@Test
	public void add_xml_property_with_null_value() throws Throwable {
		VCard vcard = new VCard();

		Xml xmlProperty = new Xml((Document) null);
		vcard.addXml(xmlProperty);

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard />" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_extendedTypes() throws Throwable {
		VCard vcard = new VCard();

		//contains marshal methods and QName
		LuckyNumProperty num = new LuckyNumProperty(24);
		vcard.addProperty(num);

		//contains marshal methods, but does not have a QName
		SalaryProperty salary = new SalaryProperty(1000000);
		vcard.addProperty(salary);

		//does not contain marshal methods nor QName
		AgeProperty age = new AgeProperty(22);
		vcard.addProperty(age);

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.registerScribe(new LuckyNumScribe());
		writer.registerScribe(new SalaryScribe());
		writer.registerScribe(new AgeScribe());
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
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
		vcard.setFormattedName("John Doe");

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		String actual = xcard.write(2);

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" + NEWLINE +
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
	public void write_prettyPrint_invalid_value() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		String actual = xcard.write();

		String expected = "<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\"><vcard><fn><text>John Doe</text></fn></vcard></vcards>";

		//use "String.contains()" to ignore the XML declaration at the top
		assertTrue("Expected:" + NEWLINE + expected + NEWLINE + NEWLINE + "Actual:" + NEWLINE + actual, actual.contains(expected));
	}

	@Test
	public void write_xmlVerison_default() throws Throwable {
		VCard vcard = new VCard();
		XCardDocument xcard = new XCardDocument();
		xcard.addVCard(vcard);

		String xml = xcard.write();
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.0\".*?\\?>.*"));
	}

	@Test
	public void write_xmlVerison_1_1() throws Throwable {
		VCard vcard = new VCard();
		XCardDocument xcard = new XCardDocument();
		xcard.addVCard(vcard);

		String xml = xcard.write(null, "1.1");
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.1\".*?\\?>.*"));
	}

	@Test
	public void write_xmlVerison_invalid() throws Throwable {
		VCard vcard = new VCard();
		XCardDocument xcard = new XCardDocument();
		xcard.addVCard(vcard);

		String xml = xcard.write(null, "10.17");
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.0\".*?\\?>.*"));
	}

	@Test
	public void write_utf8() throws Throwable {
		VCard vcard = new VCard();
		vcard.addNote("\u019dote");

		XCardDocument xcard = new XCardDocument();
		xcard.addVCard(vcard);

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
		vcard.addProperty(new EmbeddedProperty());

		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.registerScribe(new EmbeddedScribe());
		writer.write(vcard);

		VCard parsedVCard = Ezvcard.parseXml(xcard.write()).first();
		assertTrue(parsedVCard.getExtendedProperties().isEmpty());
	}

	@Test
	public void add_no_existing_vcards_element() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		XCardDocument xcard = new XCardDocument("<root><a /></root>");
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<root>" +
			"<a />" +
			"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
				"<vcard>" +
					"<fn><text>John Doe</text></fn>" +
				"</vcard>" +
			"</vcards>" + 
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_existing_vcards_element() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		XCardDocument xcard = new XCardDocument("<root><vcards xmlns=\"" + V4_0.getXmlNamespace() + "\"><a /></vcards></root>");
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		Document actual = xcard.getDocument();

		//@formatter:off
		String xml =
		"<root>" +
			"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
				"<a />" +
				"<vcard>" +
					"<fn><text>John Doe</text></fn>" +
				"</vcard>" +
			"</vcards>" + 
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, actual);
	}

	@Test
	public void write_rfc6351_example() throws Throwable {
		VCard vcard = new VCard();

		vcard.setFormattedName("Simon Perreault");

		StructuredName n = new StructuredName();
		n.setFamily("Perreault");
		n.setGiven("Simon");
		n.getSuffixes().add("ing. jr");
		n.getSuffixes().add("M.Sc.");
		vcard.setStructuredName(n);

		Birthday bday = new Birthday(PartialDate.builder().month(2).date(3).build());
		vcard.setBirthday(bday);

		Anniversary anniversary = new Anniversary(PartialDate.builder().year(2009).month(8).date(8).hour(14).minute(30).offset(new UtcOffset(false, -5, 0)).build());
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
		adr.getTypes().add(AddressType.WORK);
		adr.setLabel("Simon Perreault\n2875 boul. Laurier, suite D2-630\nQuebec, QC, Canada\nG1V 2M2");
		vcard.addAddress(adr);

		TelUri telUri = new TelUri.Builder("+1-418-656-9254").extension("102").build();
		Telephone tel = new Telephone(telUri);
		tel.getTypes().add(TelephoneType.WORK);
		tel.getTypes().add(TelephoneType.VOICE);
		vcard.addTelephoneNumber(tel);

		tel = new Telephone(new TelUri.Builder("+1-418-262-6501").build());
		tel.getTypes().add(TelephoneType.WORK);
		tel.getTypes().add(TelephoneType.TEXT);
		tel.getTypes().add(TelephoneType.VOICE);
		tel.getTypes().add(TelephoneType.CELL);
		tel.getTypes().add(TelephoneType.VIDEO);
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

		assertValidate(vcard).versions(V4_0).run();

		assertExample(vcard, "rfc6351-example.xml");
	}

	@Test
	public void read_rfc6351_example() throws Throwable {
		VCardAsserter asserter = readFile("rfc6351-example.xml");
		asserter.next(V4_0);

		//@formatter:off
		asserter.simpleProperty(FormattedName.class)
			.value("Simon Perreault")
		.noMore();
		
		asserter.structuredName()
			.family("Perreault")
			.given("Simon")
			.suffixes("ing. jr", "M.Sc.")
		.noMore();
		
		asserter.dateProperty(Birthday.class)
			.partialDate(PartialDate.builder()
				.month(2)
				.date(3)
				.build()
			)
		.noMore();
		
		asserter.dateProperty(Anniversary.class)
			.partialDate(PartialDate.builder()
				.year(2009)
				.month(8)
				.date(8)
				.hour(14)
				.minute(30)
				.offset(new UtcOffset(false, -5, 0))
				.build()
			)
		.noMore();
		
		asserter.property(Gender.class)
			.expected(Gender.male())
		.noMore();
		
		asserter.simpleProperty(Language.class)
			.value("fr")
			.param("PREF", "1")
		.next()
			.value("en")
			.param("PREF", "2")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("Viagenie")
			.param("TYPE", "work")
		.noMore();
		
		asserter.address()
			.streetAddress("2875 boul. Laurier, suite D2-630")
			.locality("Quebec")
			.region("QC")
			.postalCode("G1V 2M2")
			.country("Canada")
			.label("Simon Perreault\n2875 boul. Laurier, suite D2-630\nQuebec, QC, Canada\nG1V 2M2")
			.types(AddressType.WORK)
		.noMore();
		
		asserter.telephone()
			.uri(new TelUri.Builder("+1-418-656-9254").extension("102").build())
			.types(TelephoneType.WORK, TelephoneType.VOICE)
		.next()
			.uri(new TelUri.Builder("+1-418-262-6501").build())
			.types(TelephoneType.WORK, TelephoneType.TEXT, TelephoneType.VOICE, TelephoneType.CELL, TelephoneType.VIDEO)
		.noMore();
		
		asserter.email()
			.value("simon.perreault@viagenie.ca")
			.types(EmailType.WORK)
		.noMore();
		
		asserter.geo()
			.latitude(46.766336)
			.longitude(-71.28955)
			.param("TYPE", "work")
		.noMore();
		
		asserter.binaryProperty(Key.class)
			.url("http://www.viagenie.ca/simon.perreault/simon.asc")
			.param("TYPE", "work")
		.noMore();
		
		asserter.timezone()
			.text("America/Montreal")
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://nomis80.org")
			.param("TYPE", "home")
		.noMore();
		//@formatter:on

		asserter.validate().run();
		asserter.done();
	}

	private static void assertExample(VCard vcard, String exampleFileName) throws IOException, SAXException {
		XCardDocument xcard = new XCardDocument();
		XCardDocumentStreamWriter writer = xcard.writer();
		writer.setAddProdId(false);
		writer.write(vcard);

		Document expected = XmlUtils.toDocument(new InputStreamReader(XCardDocumentTest.class.getResourceAsStream(exampleFileName)));
		Document actual = xcard.getDocument();

		assertXMLEqual(XmlUtils.toString(actual), expected, actual);
	}

	private static VCardAsserter readFile(String file) throws SAXException, IOException {
		XCardDocument document = new XCardDocument(XCardDocumentTest.class.getResourceAsStream(file));
		StreamReader reader = document.reader();
		return new VCardAsserter(reader);
	}

	private static class EmbeddedProperty extends VCardProperty {
		//empty
	}

	private static class EmbeddedScribe extends VCardPropertyScribe<EmbeddedProperty> {
		public EmbeddedScribe() {
			super(EmbeddedProperty.class, "EMBEDDED");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return null;
		}

		@Override
		protected String _writeText(EmbeddedProperty property, WriteContext context) {
			return null;
		}

		@Override
		protected EmbeddedProperty _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			return null;
		}

		@Override
		protected void _writeXml(EmbeddedProperty property, XCardElement parent) {
			throw new EmbeddedVCardException(new VCard());
		}
	}

	private static VCardAsserter readXml(String xml) throws SAXException {
		XCardDocument xcard = new XCardDocument(xml);
		StreamReader reader = xcard.reader();
		return new VCardAsserter(reader);
	}
}