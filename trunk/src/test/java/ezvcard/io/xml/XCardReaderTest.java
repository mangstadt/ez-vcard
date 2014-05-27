package ezvcard.io.xml;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.assertWarningsLists;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.AgeType;
import ezvcard.io.AgeType.AgeScribe;
import ezvcard.io.LuckyNumType;
import ezvcard.io.LuckyNumType.LuckyNumScribe;
import ezvcard.io.MyFormattedNameType;
import ezvcard.io.MyFormattedNameType.MyFormattedNameScribe;
import ezvcard.io.SalaryType;
import ezvcard.io.SalaryType.SalaryScribe;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Language;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.RawProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Url;
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
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(2, vcard.getProperties().size());

			FormattedName fn = vcard.getFormattedName();
			assertEquals("Dr. Gregory House M.D.", fn.getValue());

			StructuredName n = vcard.getStructuredName();
			assertEquals("House", n.getFamily());
			assertEquals("Gregory", n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
			assertEquals(Arrays.asList("MD"), n.getSuffixes());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	@Test
	public void read_multiple() throws Exception {
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

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(2, vcard.getProperties().size());

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
			assertEquals(2, vcard.getProperties().size());

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
		assertWarningsLists(listener.warnings, 0, 0);
	}

	@Test
	public void read_default_namespace() throws Exception {
		//@formatter:off
		String xml =
		"<vcards>" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings);
	}

	@Test
	public void read_wrong_namespace() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"wrong\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings);
	}

	@Test
	public void read_namespace_prefix() throws Exception {
		//@formatter:off
		String xml =
		"<v:vcards xmlns:v=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<v:vcard>" +
				"<v:fn><x:text xmlns:x=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">Dr. Gregory House M.D.</x:text></v:fn>" +
			"</v:vcard>" +
		"</v:vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());
			assertEquals("Dr. Gregory House M.D.", vcard.getFormattedName().getValue());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	@Test
	public void read_preserve_whitespace() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<note><text>  This \t  is \n   a   note </text></note>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			Note note = vcard.getNotes().get(0);
			assertEquals("  This \t  is \n   a   note ", note.getValue());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	@Test
	public void read_ignore_other_namespaces() throws Exception {
		//@formatter:off
		String xml =
		"<root>" +
			"<ignore xmlns=\"one\" />" +
			"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
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
		"</root>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			FormattedName fn = vcard.getFormattedName();
			assertEquals("Dr. Gregory House M.D.", fn.getValue());
			assertEquals(fn.getParameters().toString(), Arrays.asList("1", "2"), fn.getParameters("PREF"));
		}

		assertWarningsLists(listener.warnings, 0);
		assertFalse(it.hasNext());
	}

	@Test
	public void read_identical_element_names() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<vcard>" +
					"<parameters>" +
						"<parameters><text>paramValue</text></parameters>" +
					"</parameters>" +
					"<vcard>propValue</vcard>" +
				"</vcard>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			RawProperty property = vcard.getExtendedProperty("VCARD");
			assertEquals("propValue", property.getValue());
			assertEquals("paramValue", property.getParameter("PARAMETERS"));
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	@Test
	public void read_bad_xml() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>John Doe</fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		try {
			reader.read(listener);
			fail();
		} catch (TransformerException e) {
			assertTrue(e.getCause() instanceof SAXException);
		}

		Iterator<VCard> it = listener.vcards.iterator();

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings);
	}

	@Test
	public void read_multiple_vcards_elements() throws Exception {
		//@formatter:off
		String xml =
		"<root>" +
			"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
				"<vcard>" +
					"<fn><text>Dr. Gregory House M.D.</text></fn>" +
				"</vcard>" +
			"</vcards>" +
			"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
				"<vcard>" +
					"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
				"</vcard>" +
			"</vcards>" +
		"</root>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			assertEquals("Dr. Gregory House M.D.", vcard.getFormattedName().getValue());
		}

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			assertEquals("Dr. Lisa Cuddy M.D.", vcard.getFormattedName().getValue());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0, 0);
	}

	@Test
	public void read_parameters() throws Exception {
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

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(4, vcard.getProperties().size());

			{
				Iterator<Note> notesIt = vcard.getNotes().iterator();

				Note note = notesIt.next();
				assertEquals("Note 1", note.getValue());
				assertTrue(note.getParameters().isEmpty());

				note = notesIt.next();
				assertEquals("Hello world!", note.getValue());
				assertEquals(1, note.getParameters().size());
				assertEquals("1", note.getAltId());

				note = notesIt.next();
				assertEquals("Bonjour tout le monde!", note.getValue());
				assertEquals(2, note.getParameters().size());
				assertEquals("1", note.getAltId());
				assertEquals("fr", note.getLanguage());

				assertFalse(notesIt.hasNext());
			}

			{
				Iterator<Telephone> telIt = vcard.getTelephoneNumbers().iterator();

				Telephone tel = telIt.next();
				assertEquals("+1-555-555-1234", tel.getUri().getNumber());
				assertEquals(2, tel.getParameters().size());
				assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

				assertFalse(telIt.hasNext());
			}
		}

		assertWarningsLists(listener.warnings, 0);
		assertFalse(it.hasNext());
	}

	@Test
	public void read_groups() throws Exception {
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

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(3, vcard.getProperties().size());

			FormattedName fn = vcard.getFormattedName();
			assertEquals("John Doe", fn.getValue());
			assertEquals("item1", fn.getGroup());

			{
				Iterator<Note> notesIt = vcard.getNotes().iterator();

				Note note = notesIt.next();
				assertEquals("Hello world!", note.getValue());
				assertEquals("item1", note.getGroup());

				note = notesIt.next();
				assertEquals("A property without a group", note.getValue());
				assertNull(note.getGroup());

				assertFalse(notesIt.hasNext());
			}
		}

		assertWarningsLists(listener.warnings, 0);
		assertFalse(it.hasNext());
	}

	@Test
	public void read_non_standard_properties() throws Exception {
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

		XCardReader reader = new XCardReader(xml);
		reader.registerScribe(new LuckyNumScribe());
		reader.registerScribe(new SalaryScribe());
		reader.registerScribe(new AgeScribe());
		reader.registerScribe(new MyFormattedNameScribe());

		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(6, vcard.getProperties().size());

			{
				Iterator<Xml> xmlIt = vcard.getXmls().iterator();

				Xml xmlType = xmlIt.next();
				assertXMLEqual(XmlUtils.toDocument("<foo xmlns=\"http://example.com\">bar</foo>"), xmlType.getValue());

				assertFalse(xmlIt.hasNext());
			}

			LuckyNumType luckyNum = vcard.getProperty(LuckyNumType.class);
			assertEquals(21, luckyNum.luckyNum);

			SalaryType salary = vcard.getProperty(SalaryType.class);
			assertEquals(1000000, salary.salary);

			AgeType age = vcard.getProperty(AgeType.class);
			assertEquals(24, age.age);

			RawProperty gender = vcard.getExtendedProperty("X-GENDER");
			assertEquals(VCardDataType.TEXT, gender.getDataType());
			assertEquals("m", gender.getValue());

			MyFormattedNameType fn = vcard.getProperty(MyFormattedNameType.class);
			assertEquals("JOHN DOE", fn.value);
		}

		assertWarningsLists(listener.warnings, 0);
		assertFalse(it.hasNext());
	}

	@Test
	public void read_xml_property() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
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
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			Xml property = vcard.getXmls().get(0);
			assertIntEquals(1, property.getParameters().getPref());
			Document actual = property.getValue();

			//@formatter:off
			String propertyXml =
			"<foo xmlns=\"http://example.com\">" +
				"<a />" +
				"<b attr=\"value\">text</b>" +
				"<c>text<child>child</child></c>" +
			"</foo>";
			Document expected = XmlUtils.toDocument(propertyXml);
			//@formatter:on

			assertXMLEqual(expected, actual);
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	@Test
	public void skipMeException() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<skipme><text>value</text></skipme>" +
				"<x-foo><text>value</text></x-foo>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		reader.registerScribe(new SkipMeScribe());

		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			RawProperty property = vcard.getExtendedProperty("x-foo");
			assertEquals("X-FOO", property.getPropertyName());
			assertEquals("value", property.getValue());
		}

		assertWarningsLists(listener.warnings, 1);
		assertFalse(it.hasNext());
	}

	@Test
	public void cannotParseException() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<cannotparse><text>value</text></cannotparse>" +
				"<x-foo><text>value</text></x-foo>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		reader.registerScribe(new CannotParseScribe());

		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(2, vcard.getProperties().size());

			RawProperty property = vcard.getExtendedProperty("x-foo");
			assertEquals("X-FOO", property.getPropertyName());
			assertEquals("value", property.getValue());

			Xml xmlProperty = vcard.getXmls().get(0);
			assertXMLEqual(XmlUtils.toString(xmlProperty.getValue()), XmlUtils.toDocument("<cannotparse xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\"><text>value</text></cannotparse>"), xmlProperty.getValue());
		}

		assertWarningsLists(listener.warnings, 1);
		assertFalse(it.hasNext());
	}

	@Test
	public void stopParsingException() throws Exception {
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

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl() {
			@Override
			public void vcardRead(VCard vcard, List<String> warnings) throws StopReadingException {
				super.vcardRead(vcard, warnings);
				throw new StopReadingException();
			}
		};
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			FormattedName fn = vcard.getFormattedName();
			assertEquals("Dr. Gregory House M.D.", fn.getValue());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	@Test
	public void read_utf8() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
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

		XCardReader reader = new XCardReader(file);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());
			assertEquals("\u019dote", vcard.getNotes().get(0).getValue());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	@Test
	public void read_empty() throws Exception {
		String xml = "<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />";

		XCardReader reader = new XCardReader(xml);
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings);
	}

	@Test
	public void read_rfc6351_example() throws Throwable {
		XCardReader reader = read("rfc6351-example.xml");
		XCardListenerImpl listener = new XCardListenerImpl();
		reader.read(listener);
		Iterator<VCard> it = listener.vcards.iterator();

		{
			VCard vcard = it.next();

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
		}

		assertFalse(it.hasNext());
		assertWarningsLists(listener.warnings, 0);
	}

	private XCardReader read(String file) throws SAXException, IOException {
		return new XCardReader(getClass().getResourceAsStream(file));
	}

	private class XCardListenerImpl implements XCardListener {
		private final List<VCard> vcards = new ArrayList<VCard>();
		private final List<List<String>> warnings = new ArrayList<List<String>>();

		public void vcardRead(VCard vcard, List<String> warnings) throws StopReadingException {
			this.vcards.add(vcard);
			this.warnings.add(warnings);
		}
	}
}
