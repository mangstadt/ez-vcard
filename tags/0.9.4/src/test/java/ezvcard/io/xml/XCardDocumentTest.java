package ezvcard.io.xml;

import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.assertWarningsLists;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
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
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Language;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.SkipMeProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
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
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		Iterator<VCard> it = xcard.parseAll().iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			FormattedName fn = vcard.getFormattedName();
			assertEquals("Dr. Gregory House M.D.", fn.getValue());
		}

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			FormattedName fn = vcard.getFormattedName();
			assertEquals("Dr. Lisa Cuddy M.D.", fn.getValue());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(xcard.getParseWarnings(), 0, 0);
	}

	@Test
	public void parseFirst() throws Throwable {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);

		VCard vcard = xcard.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());

		FormattedName fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		assertWarningsLists(xcard.getParseWarnings(), 0);
	}

	@Test
	public void parse_clear_warnings() throws Throwable {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<skipme />" +
			"</vcard>" +
			"<vcard>" +
				"<x-foo />" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcr = new XCardDocument(xml);
		xcr.registerScribe(new SkipMeScribe());

		xcr.parseAll();
		assertWarningsLists(xcr.getParseWarnings(), 1, 0);

		xcr.parseAll();
		assertWarningsLists(xcr.getParseWarnings(), 1, 0);
	}

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
	public void add_skipMeException() throws Throwable {
		VCard vcard = new VCard();
		vcard.addProperty(new SkipMeProperty());
		vcard.addExtendedProperty("x-foo", "value");

		XCardDocument xcm = new XCardDocument();
		xcm.setAddProdId(false);
		xcm.registerScribe(new SkipMeScribe());
		xcm.add(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
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

		XCardDocument xcm = new XCardDocument();
		xcm.add(vcard);
	}

	@Test
	public void add_extendedTypes() throws Throwable {
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
		vcard.addProperty(new EmbeddedProperty());

		XCardDocument doc = new XCardDocument();
		doc.registerScribe(new EmbeddedScribe());
		doc.add(vcard);

		VCard parsedVCard = Ezvcard.parseXml(doc.write()).first();
		assertTrue(parsedVCard.getExtendedProperties().isEmpty());
	}

	@Test
	public void add_no_existing_vcards_element() throws Throwable {
		VCard vcard = new VCard();
		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		XCardDocument xcm = new XCardDocument("<root><a /></root>");
		xcm.setAddProdId(false);
		xcm.add(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		String xml =
		"<root>" +
			"<a />" +
			"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
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
		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		XCardDocument xcm = new XCardDocument("<root><vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\"><a /></vcards></root>");
		xcm.setAddProdId(false);
		xcm.add(vcard);

		Document actual = xcm.getDocument();

		//@formatter:off
		String xml =
		"<root>" +
			"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
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

		assertValidate(vcard).versions(VCardVersion.V4_0).run();

		assertExample(vcard, "rfc6351-example.xml");
	}

	@Test
	public void read_rfc6351_example() throws Throwable {
		XCardDocument xcard = read("rfc6351-example.xml");

		List<VCard> vcards = xcard.parseAll();
		assertEquals(1, vcards.size());

		VCard vcard = vcards.get(0);
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(16, vcard.getProperties().size());

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

		assertValidate(vcard).versions(vcard.getVersion()).run();
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

	private XCardDocument read(String file) throws SAXException, IOException {
		return new XCardDocument(getClass().getResourceAsStream(file));
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