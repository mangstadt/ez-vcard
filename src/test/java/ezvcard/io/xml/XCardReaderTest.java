package ezvcard.io.xml;

import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.io.AgeProperty;
import ezvcard.io.AgeProperty.AgeScribe;
import ezvcard.io.LuckyNumProperty;
import ezvcard.io.LuckyNumProperty.LuckyNumScribe;
import ezvcard.io.MyFormattedNameProperty;
import ezvcard.io.MyFormattedNameProperty.MyFormattedNameScribe;
import ezvcard.io.SalaryProperty;
import ezvcard.io.SalaryProperty.SalaryScribe;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Key;
import ezvcard.property.Language;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.ProductId;
import ezvcard.property.RawProperty;
import ezvcard.property.Url;
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
public class XCardReaderTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void read_single() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<!-- ignore -->" +
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
				"<n>" +
					"<surname>House</surname>" +
					"<given>Gregory</given>" +
					"<!-- ignore -->" +
					"<additional />" +
					"<prefix>Dr</prefix>" +
					"<prefix>Mr</prefix>" +
					"<suffix>MD</suffix>" +
				"</n>" +
			"</vcard>" +
		"</vcards>"
		);

		asserter.next(V4_0);

		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Gregory House M.D.")
		.noMore();
		
		asserter.structuredName()
			.family("House")
			.given("Gregory")
			.prefixes("Dr", "Mr")
			.suffixes("MD")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_multiple() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
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
		"</vcards>"
		);

		asserter.next(V4_0);

		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Gregory House M.D.")
		.noMore();
		
		asserter.structuredName()
			.family("House")
			.given("Gregory")
			.prefixes("Dr", "Mr")
			.suffixes("MD")
		.noMore();

		asserter.next(V4_0);

		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Lisa Cuddy M.D.")
		.noMore();
		
		asserter.structuredName()
			.family("Cuddy")
			.given("Lisa")
			.prefixes("Dr", "Ms")
			.suffixes("MD")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_default_namespace() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards>" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>"
		);
		//@formatter:on

		asserter.done();
	}

	@Test
	public void read_wrong_namespace() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"wrong\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>"
		);
		//@formatter:on

		asserter.done();
	}

	@Test
	public void read_namespace_prefix() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<v:vcards xmlns:v=\"" + V4_0.getXmlNamespace() + "\">" +
			"<v:vcard>" +
				"<v:fn><x:text xmlns:x=\"" + V4_0.getXmlNamespace() + "\">Dr. Gregory House M.D.</x:text></v:fn>" +
			"</v:vcard>" +
		"</v:vcards>"
		);
		
		asserter.next(V4_0);

		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Gregory House M.D.")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_preserve_whitespace() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<note><text>  This \t  is \n   a   note </text></note>" +
			"</vcard>" +
		"</vcards>"
		);
		
		asserter.next(V4_0);

		asserter.simpleProperty(Note.class)
			.value("  This \t  is \n   a   note ")
		.noMore();
		
		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_ignore_other_namespaces() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<root>" +
			"<ignore xmlns=\"one\" />" +
			"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
				"<ignore xmlns=\"two\">text</ignore>" +
				"<vcard>" +
					"<fn>" +
						"<parameters>" +
							"<ignore xmlns=\"three\"><foo>bar</foo></ignore>" +
							"<pref><ignore xmlns=\"four\">bar</ignore><integer>1</integer></pref>" +
							"<pref><integer>2</integer></pref>" +
						"</parameters>" +
						"<text>Dr. Gregory House M.D.</text>" +
					"</fn>" +
				"</vcard>" +
			"</vcards>" +
		"</root>"
		);
		
		asserter.next(V4_0);

		asserter.simpleProperty(FormattedName.class)
			.value("Dr. Gregory House M.D.")
			.param("PREF", "1")
			.param("PREF", "2")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_identical_element_names() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<vcard>" +
					"<parameters>" +
						"<parameters><text>paramValue1</text></parameters>" +
						"<group><text>paramValue2</text></group>" +
					"</parameters>" +
					"<vcard>propValue</vcard>" +
				"</vcard>" +
				"<group name=\"grp\">" +
					"<group>" +
						"<text>value</text>" +
					"</group>" +
				"</group>" +
			"</vcard>" +
		"</vcards>"
		);

		asserter.next(V4_0);

		asserter.rawProperty("VCARD")
			.dataType(VCardDataType.get("VCARD"))
			.value("propValue")
			.param("PARAMETERS", "paramValue1")
			.param("GROUP", "paramValue2")
		.noMore();
		
		asserter.rawProperty("GROUP")
			.dataType(VCardDataType.TEXT)
			.group("grp")
			.value("value")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_bad_xml() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);

		try {
			reader.readNext();
			fail();
		} catch (IOException e) {
			assertTrue(e.getCause() instanceof TransformerException);
			assertTrue(e.getCause().getCause() instanceof SAXException);
		}

		assertNoMoreVCards(reader);
	}

	@Test
	public void read_multiple_vcards_elements() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<root>" +
			"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
				"<vcard>" +
					"<fn><text>Dr. Gregory House M.D.</text></fn>" +
				"</vcard>" +
			"</vcards>" +
			"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
				"<vcard>" +
					"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
				"</vcard>" +
			"</vcards>" +
		"</root>"
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
	public void read_non_standard_properties() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				//xCard namespace:  no
				//scribe:           no
				//expected:         XML property
				"<foo xmlns=\"http://example.com\">bar</foo>" +
				
				//xCard namespace:  no
				//scribe:           yes
				//parseXml impl:    yes
				//expected:         LuckyNumProperty
				"<a:lucky-num xmlns:a=\"http://luckynum.com\"><a:num>21</a:num></a:lucky-num>" +
				
				//xCard namespace:  yes
				//scribe:           yes
				//parseXml impl:    yes
				//expected:         SalaryProperty
				"<x-salary><integer>1000000</integer></x-salary>" +
				
				//xCard namespace:  yes
				//parseXml impl:    no
				//expected:         AgeProperty (should be unmarshalled using the default parseXml implementation)
				"<x-age><integer>24</integer></x-age>" +
				
				//xCard namespace:  yes
				//scribe:           no
				//expected:         RawProperty
				"<x-gender><text>m</text></x-gender>" +
				
				//xCard namespace:  yes
				//scribe:           yes (standard scribe overridden)
				//expected:         MyFormattedNameProperty
				"<fn><name>John Doe</name></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		reader.registerScribe(new LuckyNumScribe());
		reader.registerScribe(new SalaryScribe());
		reader.registerScribe(new AgeScribe());
		reader.registerScribe(new MyFormattedNameScribe());

		{
			VCard vcard = reader.readNext();
			assertVersion(V4_0, vcard);
			assertPropertyCount(6, vcard);

			{
				Iterator<Xml> xmlIt = vcard.getXmls().iterator();

				Xml xmlType = xmlIt.next();
				assertXMLEqual(XmlUtils.toDocument("<foo xmlns=\"http://example.com\">bar</foo>"), xmlType.getValue());

				assertFalse(xmlIt.hasNext());
			}

			LuckyNumProperty luckyNum = vcard.getProperty(LuckyNumProperty.class);
			assertEquals(21, luckyNum.luckyNum);

			SalaryProperty salary = vcard.getProperty(SalaryProperty.class);
			assertEquals(1000000, salary.salary);

			AgeProperty age = vcard.getProperty(AgeProperty.class);
			assertEquals(24, age.age);

			RawProperty gender = vcard.getExtendedProperty("X-GENDER");
			assertEquals(VCardDataType.TEXT, gender.getDataType());
			assertEquals("m", gender.getValue());

			MyFormattedNameProperty fn = vcard.getProperty(MyFormattedNameProperty.class);
			assertEquals("JOHN DOE", fn.value);

			assertWarnings(0, reader);
		}

		assertNoMoreVCards(reader);
	}

	@Test
	public void read_xml_property() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readXml(
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<x:foo xmlns:x=\"http://example.com\">" +
					"<parameters>" +
						"<pref><integer>1</integer></pref>" +
					"</parameters>" +
					"<!-- comment -->" +
					"<x:a />" +
					"<x:b attr=\"value\">text</x:b>" +
					"<x:c>text<x:child>child</x:child></x:c>" +
				"</x:foo>" +
			"</vcard>" +
		"</vcards>"
		);
		
		asserter.next(V4_0);

		asserter.xml()
			.param("PREF", "1")
			.value(
				"<foo xmlns=\"http://example.com\">" +
					"<a />" +
					"<b attr=\"value\">text</b>" +
					"<c>text<child>child</child></c>" +
				"</foo>"
			)
		.noMore();

		asserter.done();
		//@formatter:off
	}

	@Test
	public void skipMeException() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<skipme><text>value</text></skipme>" +
				"<x-foo><text>value</text></x-foo>" +
			"</vcard>" +
		"</vcards>";
		
		XCardReader reader = new XCardReader(xml);
		reader.registerScribe(new SkipMeScribe());
		VCardAsserter asserter = new VCardAsserter(reader);

		asserter.next(V4_0);

		asserter.rawProperty("x-foo")
			.dataType(VCardDataType.TEXT)
			.value("value")
		.noMore();
		
		asserter.warnings(1);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void cannotParseException() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<group name=\"grp\">" +
					"<cannotparse><text>value1</text></cannotparse>" +
				"</group>" +
				"<cannotparse><text>value2</text></cannotparse>" +
				"<x-foo><text>value</text></x-foo>" +
			"</vcard>" +
		"</vcards>";
		
		XCardReader reader = new XCardReader(xml);
		reader.registerScribe(new CannotParseScribe());
		VCardAsserter asserter = new VCardAsserter(reader);

		asserter.next(V4_0);

		asserter.rawProperty("x-foo")
			.dataType(VCardDataType.TEXT)
			.value("value")
		.noMore();
		
		asserter.xml()
			.group("grp")
			.value("<cannotparse xmlns=\"" + V4_0.getXmlNamespace() + "\"><text>value1</text></cannotparse>")
		.next()
			.value("<cannotparse xmlns=\"" + V4_0.getXmlNamespace() + "\"><text>value2</text></cannotparse>")
		.noMore();

		asserter.warnings(2);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void close_before_stream_ends() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);

		VCard vcard = reader.readNext();
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);
		assertEquals("Dr. Gregory House M.D.", vcard.getFormattedName().getValue());
		assertWarnings(0, reader);

		reader.close();

		assertNoMoreVCards(reader);
	}

	@Test
	public void read_utf8() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
					"<note><text>\u019dote</text></note>" +
			"</vcard>" +
		"</vcards>";

		File file = tempFolder.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write(xml);
		writer.close();

		XCardReader reader = new XCardReader(file);
		VCardAsserter asserter = new VCardAsserter(reader);

		asserter.next(V4_0);

		asserter.simpleProperty(Note.class)
			.value("\u019dote")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_empty() throws Exception {
		VCardAsserter asserter = readXml("<vcards xmlns=\"" + V4_0.getXmlNamespace() + "\" />");
		asserter.done();
	}

	@Test
	public void read_rfc6351_example() throws Throwable {
		VCardAsserter asserter = read("rfc6351-example.xml");

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
			.partialDate(PartialDate.builder().month(2).date(3).build())
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

	private static VCardAsserter read(String file) throws SAXException, IOException {
		XCardReader reader = new XCardReader(XCardReaderTest.class.getResourceAsStream(file));
		return new VCardAsserter(reader);
	}

	private static VCardAsserter readXml(String xml) {
		XCardReader reader = new XCardReader(xml);
		return new VCardAsserter(reader);
	}
}
