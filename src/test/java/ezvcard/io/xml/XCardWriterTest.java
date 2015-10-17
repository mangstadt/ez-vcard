package ezvcard.io.xml;

import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.assertValidate;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.AgeType;
import ezvcard.io.AgeType.AgeScribe;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.LuckyNumType;
import ezvcard.io.LuckyNumType.LuckyNumScribe;
import ezvcard.io.SalaryType;
import ezvcard.io.SalaryType.SalaryScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Note;
import ezvcard.property.Photo;
import ezvcard.property.SkipMeProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;
import ezvcard.util.XmlUtils;

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
 * @author Michael Angstadt
 */
public class XCardWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private StringWriter sw;
	private XCardWriter writer;

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Before
	public void before() {
		sw = new StringWriter();
		writer = new XCardWriter(sw);
		writer.setAddProdId(false);
	}

	@Test
	public void write_single() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_multiple() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("Jane Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Jane Doe</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_empty() throws Exception {
		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\" />";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_xml_property() throws Exception {
		VCard vcard = new VCard();
		Xml xml = new Xml("<foo xmlns=\"http://example.com\" a=\"b\">bar<car/></foo>");
		xml.setParameter("x-foo", "bar");
		vcard.addXml(xml);
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<foo xmlns=\"http://example.com\" a=\"b\">" +
					"<parameters xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
						"<x-foo><unknown>bar</unknown></x-foo>" +
					"</parameters>" +
					"bar<car/>" +
				"</foo>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_xml_property_null_value() throws Exception {
		VCard vcard = new VCard();
		vcard.addXml(new Xml((Document) null));
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard />" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_existing_dom_document() throws Exception {
		Document document = XmlUtils.toDocument("<root><a /><b /></root>");
		XCardWriter writer = new XCardWriter(document);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String xml =
		"<root>" +
			"<a />" +
			"<b />" +
			"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
				"<vcard>" +
					"<fn><text>John Doe</text></fn>" +
				"</vcard>" +
			"</vcards>" +
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_existing_dom_element() throws Exception {
		Document document = XmlUtils.toDocument("<root><a /><b /></root>");
		Node element = document.getFirstChild().getFirstChild();
		XCardWriter writer = new XCardWriter(element);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String xml =
		"<root>" +
			"<a>" +
				"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
					"<vcard>" +
						"<fn><text>John Doe</text></fn>" +
					"</vcard>" +
				"</vcards>" +
			"</a>" +
			"<b />" +
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_existing_vcards_document() throws Exception {
		Document document = XmlUtils.toDocument("<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\" />");
		XCardWriter writer = new XCardWriter(document);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
		"</vcards>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_existing_vcards_element() throws Exception {
		Document document = XmlUtils.toDocument("<root><a><vcards xmlns=\"" + V4_0.getXmlNamespace() + "\" /></a><b /></root>");
		Node element = document.getFirstChild().getFirstChild().getFirstChild();
		XCardWriter writer = new XCardWriter(element);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String xml =
		"<root>" +
			"<a>" +
				"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
					"<vcard>" +
						"<fn><text>John Doe</text></fn>" +
					"</vcard>" +
				"</vcards>" +
			"</a>" +
			"<b />" +
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_parameters() throws Exception {
		writer.registerParameterDataType("X-INT", VCardDataType.INTEGER);

		VCard vcard = new VCard();
		Note note = new Note("This is a\nnote.");
		note.setParameter(VCardParameters.ALTID, "value");
		note.setParameter(VCardParameters.CALSCALE, "value");
		note.setParameter(VCardParameters.GEO, "geo:123.456,234.567");
		note.setParameter(VCardParameters.LABEL, "value");
		note.setParameter(VCardParameters.LANGUAGE, "en");
		note.setParameter(VCardParameters.MEDIATYPE, "text/plain");
		note.addPid(1, 1);
		note.setParameter(VCardParameters.PREF, "1");
		note.setParameter(VCardParameters.SORT_AS, "value");
		note.setParameter(VCardParameters.TYPE, "home");
		note.setParameter(VCardParameters.TZ, "America/New_York");
		note.setParameter("X-CUSTOM", "xxx");
		note.setParameter("X-INT", "11");
		vcard.addNote(note);
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
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
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_group() throws Exception {
		VCard vcard = new VCard();

		vcard.setFormattedName("John Doe");

		Note note = vcard.addNote("This is a\nnote.");
		note.setGroup("group1");
		note.setLanguage("en");

		Photo photo = new Photo("http://example.com/image.jpg", ImageType.JPEG);
		photo.setGroup("group1");
		vcard.addPhoto(photo);

		note = new Note("Bonjour.");
		note.setGroup("group2");
		vcard.addNote(note);

		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
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
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_prodId() throws Exception {
		//default
		{
			StringWriter sw = new StringWriter();
			XCardWriter writer = new XCardWriter(sw);

			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");
			writer.write(vcard);

			writer.close();

			String actual = sw.toString();
			assertTrue(actual.matches(".*?<prodid><text>.*?</text></prodid>.*"));
		}

		//false
		{
			StringWriter sw = new StringWriter();
			XCardWriter writer = new XCardWriter(sw);
			writer.setAddProdId(false);

			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");
			writer.write(vcard);

			writer.close();

			String actual = sw.toString();
			assertFalse(actual.matches(".*?<prodid><text>.*?</text></prodid>.*"));
		}

		//true
		{
			StringWriter sw = new StringWriter();
			XCardWriter writer = new XCardWriter(sw);
			writer.setAddProdId(true);

			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");
			writer.write(vcard);

			writer.close();

			String actual = sw.toString();
			assertTrue(actual.matches(".*?<prodid><text>.*?</text></prodid>.*"));
		}
	}

	@Test
	public void skipMeException() throws Exception {
		writer.registerScribe(new SkipMeScribe());

		VCard vcard = new VCard();
		vcard.addProperty(new SkipMeProperty());
		vcard.addExtendedProperty("x-foo", "value");
		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<x-foo><unknown>value</unknown></x-foo>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_no_scribe_registered() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("Jane Doe");
		vcard.addProperty(new SkipMeProperty());

		try {
			writer.write(vcard);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		writer.close();

		//the writer should check for scribes before writing anything to the stream
		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_extended_properties() throws Exception {
		writer.registerScribe(new LuckyNumScribe());
		writer.registerScribe(new SalaryScribe());
		writer.registerScribe(new AgeScribe());

		VCard vcard = new VCard();

		//contains marshal methods and QName
		LuckyNumType num = new LuckyNumType(24);
		vcard.addProperty(num);

		//contains marshal methods, but does not have a QName
		SalaryType salary = new SalaryType(1000000);
		vcard.addProperty(salary);

		//does not contain marshal methods nor QName
		AgeType age = new AgeType(22);
		vcard.addProperty(age);

		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<a:lucky-num xmlns:a=\"http://luckynum.com\">24</a:lucky-num>" +
				"<x-salary>1000000</x-salary>" +
				"<x-age><unknown>22</unknown></x-age>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_prettyPrint() throws Exception {
		StringWriter sw = new StringWriter();
		XCardWriter writer = new XCardWriter(sw, 2);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		FormattedName fn = vcard.setFormattedName("John Doe");
		fn.setParameter("x-foo", "bar");
		Note note = vcard.addNote("note");
		note.setGroup("group");
		writer.write(vcard);

		writer.close();

		String actual = sw.toString();

		String nl = "(\r\n|\n|\r)";
		//@formatter:off
		String expectedRegex =
		"<\\?xml version=\"1.0\" encoding=\"(utf|UTF)-8\"\\?><vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" + nl +
		"  <vcard>" + nl +
		"    <fn>" + nl +
		"      <parameters>" + nl +
		"        <x-foo>" + nl +
		"          <unknown>bar</unknown>" + nl +
		"        </x-foo>" + nl +
		"      </parameters>" + nl +
		"      <text>John Doe</text>" + nl +
		"    </fn>" + nl +
		"    <group name=\"group\">" + nl +
		"      <note>" + nl +
		"        <text>note</text>" + nl +
		"      </note>" + nl +
		"    </group>" + nl +
		"  </vcard>" + nl +
		"</vcards>" + nl;
		//@formatter:on

		assertTrue(actual.matches(expectedRegex));
	}

	@Test
	public void write_xmlVersion_default() throws Exception {
		StringWriter sw = new StringWriter();
		XCardWriter writer = new XCardWriter(sw);
		VCard vcard = new VCard();
		writer.write(vcard);
		writer.close();

		String xml = sw.toString();
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.0\".*?\\?>.*"));
	}

	@Test
	public void write_xmlVersion_1_1() throws Exception {
		StringWriter sw = new StringWriter();
		XCardWriter writer = new XCardWriter(sw, -1, "1.1");
		VCard vcard = new VCard();
		writer.write(vcard);
		writer.close();

		String xml = sw.toString();
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.1\".*?\\?>.*"));
	}

	@Test
	public void write_xmlVersion_invalid() throws Exception {
		StringWriter sw = new StringWriter();
		XCardWriter writer = new XCardWriter(sw, -1, "10.17");
		VCard vcard = new VCard();
		writer.write(vcard);
		writer.close();

		String xml = sw.toString();
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.0\".*?\\?>.*"));
	}

	@Test
	public void write_utf8() throws Exception {
		File file = tempFolder.newFile();
		XCardWriter writer = new XCardWriter(file);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.addNote("\u019dote");
		writer.write(vcard);

		writer.close();

		String xml = IOUtils.getFileContents(file, "UTF-8");
		assertTrue(xml.matches("(?i)<\\?xml.*?encoding=\"utf-8\".*?\\?>.*"));
		assertTrue(xml.matches(".*?<note><text>\u019dote</text></note>.*"));
	}

	@Test
	public void write_embedded_vcards_not_supported() throws Exception {
		writer.registerScribe(new EmbeddedScribe());

		VCard vcard = new VCard();
		vcard.addProperty(new EmbeddedProperty());
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard />" +
		"</vcards>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_rfc6351_example() throws Exception {
		VCard vcard = new VCard();

		vcard.setFormattedName("Simon Perreault");

		StructuredName n = new StructuredName();
		n.setFamily("Perreault");
		n.setGiven("Simon");
		n.addSuffix("ing. jr");
		n.addSuffix("M.Sc.");
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

		assertValidate(vcard).versions(V4_0).run();

		assertExample(vcard, "rfc6351-example.xml");
	}

	private void assertOutput(String expected) throws SAXException, IOException {
		String actual = sw.toString();
		assertXMLEqual(expected, actual);
	}

	private void assertExample(VCard vcard, String exampleFileName) throws IOException, SAXException {
		writer.write(vcard);
		writer.close();

		String expected = IOUtils.toString(new InputStreamReader(getClass().getResourceAsStream(exampleFileName)));
		String actual = sw.toString();

		assertXMLEqual(expected, actual);
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
		protected String _writeText(EmbeddedProperty property, VCardVersion version) {
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
}
