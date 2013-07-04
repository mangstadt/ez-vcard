package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.FormattedNameType;
import ezvcard.types.NoteType;
import ezvcard.types.RawType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.XmlType;

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
	/**
	 * Tests a basic xCard example
	 */
	@Test
	public void basic() throws Exception {
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

		XCardReader xcr = new XCardReader(xml);
		VCard vcard = xcr.readNext();

		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("House", n.getFamily());
		assertEquals("Gregory", n.getGiven());
		assertTrue(n.getAdditional().isEmpty());
		assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
		assertEquals(Arrays.asList("MD"), n.getSuffixes());

		assertNull(xcr.readNext());
	}

	/**
	 * The parser should preserve whitespace and newlines in the element text.
	 */
	@Test
	public void preserveWhitespace() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<note><text>  This \t  is \n   a   note </text></note>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader xcr = new XCardReader(xml);
		VCard vcard = xcr.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		assertEquals(1, vcard.getNotes().size());

		NoteType note = vcard.getNotes().get(0);
		assertEquals("  This \t  is \n   a   note ", note.getValue());

		assertNull(xcr.readNext());
	}

	/**
	 * Tests to make sure parameters are parsed correctly.
	 */
	@Test
	public void parameters() throws Exception {
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

		XCardReader xcr = new XCardReader(xml);
		VCard vcard = xcr.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		Iterator<NoteType> notesIt = vcard.getNotes().iterator();

		NoteType note = notesIt.next();
		assertEquals("Note 1", note.getValue());
		assertTrue(note.getSubTypes().isEmpty());

		note = notesIt.next();
		assertEquals("Hello world!", note.getValue());
		assertEquals(1, note.getSubTypes().size());
		assertEquals("1", note.getAltId());

		note = notesIt.next();
		assertEquals("Bonjour tout le monde!", note.getValue());
		assertEquals(2, note.getSubTypes().size());
		assertEquals("1", note.getAltId());
		assertEquals("fr", note.getLanguage());

		assertFalse(notesIt.hasNext());

		Iterator<TelephoneType> telIt = vcard.getTelephoneNumbers().iterator();

		TelephoneType tel = telIt.next();
		assertEquals("+1-555-555-1234", tel.getUri().getNumber());
		assertEquals(2, tel.getSubTypes().size());
		assertEquals(2, tel.getTypes().size());
		assertTrue(tel.getTypes().contains(TelephoneTypeParameter.WORK));
		assertTrue(tel.getTypes().contains(TelephoneTypeParameter.VOICE));

		assertFalse(telIt.hasNext());

	}

	/**
	 * Tests to make sure it reads groups correctly.
	 */
	@Test
	public void group() throws Exception {
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

		XCardReader xcr = new XCardReader(xml);
		VCard vcard = xcr.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("John Doe", fn.getValue());
		assertEquals("item1", fn.getGroup());

		assertEquals(2, vcard.getNotes().size());

		NoteType note = vcard.getNotes().get(0);
		assertEquals("Hello world!", note.getValue());
		assertEquals("item1", note.getGroup());

		note = vcard.getNotes().get(1);
		assertEquals("A property without a group", note.getValue());
		assertNull(note.getGroup());

		assertNull(xcr.readNext());
	}

	/**
	 * Tests to make sure it parses custom type classes, RawTypes, and XmlTypes
	 */
	@Test
	public void readNonStandardElements() throws Exception {
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

		XCardReader xcr = new XCardReader(xml);
		xcr.registerExtendedType(LuckyNumType.class);
		xcr.registerExtendedType(SalaryType.class);
		xcr.registerExtendedType(AgeType.class);
		VCard vcard = xcr.readNext();

		List<XmlType> xmlTypes = vcard.getXmls();
		assertEquals(2, xmlTypes.size());
		assertEquals("<a href=\"http://www.website.com\" xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">website</a>", xmlTypes.get(0).getValue());
		//X-AGE was not unmarshalled because its type class does not support xCard;
		assertEquals("<x-age xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\"><integer>24</integer></x-age>", xmlTypes.get(1).getValue());

		List<LuckyNumType> luckyNum = vcard.getProperties(LuckyNumType.class);
		assertEquals(1, luckyNum.size());
		assertEquals(21, luckyNum.get(0).luckyNum);

		List<SalaryType> salary = vcard.getProperties(SalaryType.class);
		assertEquals(1, salary.size());
		assertEquals(1000000, salary.get(0).salary);

		List<RawType> gender = vcard.getExtendedProperties("X-GENDER");
		assertEquals(1, gender.size());
		assertEquals("m", gender.get(0).getValue());

		//warning for AgeType not supporting xCard
		assertEquals(1, xcr.getWarnings().size());

		assertNull(xcr.readNext());
	}

	@Test(expected = RuntimeException.class)
	public void registerExtendedType_no_default_constructor() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
		"</vcards>";
		//@formatter:on

		XCardReader reader = new XCardReader(xml);
		reader.registerExtendedType(BadType.class);
	}

	/**
	 * Make sure it can handle multiple "&lt;vcard&gt;" elements.
	 */
	@Test
	public void readMultipleVCards() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
			"<vcard>" +
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

		XCardReader xcr = new XCardReader(xml);

		VCard vcard = xcr.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());
		assertNull(vcard.getStructuredName());

		vcard = xcr.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("House", n.getFamily());
		assertEquals("Gregory", n.getGiven());
		assertTrue(n.getAdditional().isEmpty());
		assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
		assertEquals(Arrays.asList("MD"), n.getSuffixes());
		assertNull(vcard.getFormattedName());

		assertNull(xcr.readNext());
	}

	/**
	 * xCards without the proper namespace will not be parsed.
	 */
	@Test
	public void namespace() throws Exception {
		//no namespace
		//@formatter:off
		String xml =
		"<vcards>" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader xcr = new XCardReader(xml);
		assertNull(xcr.readNext());

		//wrong namespace
		//@formatter:off
		xml =
		"<vcards xmlns=\"wrong\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		xcr = new XCardReader(xml);
		assertNull(xcr.readNext());
	}

	/**
	 * If the type's unmarshal method throws a {@link SkipMeException}, then a
	 * warning should be added to the warnings list and the type object should
	 * NOT be added to the {@link VCard} object.
	 */
	@Test
	public void skipMeException() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" xmlns:a=\"http://luckynum.com\">" +
			"<vcard>" +
				"<a:lucky-num><a:num>24</a:num></a:lucky-num>" +
				"<a:lucky-num><a:num>13</a:num></a:lucky-num>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardReader xcr = new XCardReader(xml);
		xcr.registerExtendedType(LuckyNumType.class);
		VCard vcard = xcr.readNext();

		assertEquals(1, xcr.getWarnings().size());

		List<LuckyNumType> luckyNum = vcard.getProperties(LuckyNumType.class);
		assertEquals(1, luckyNum.size());
		assertEquals(24, luckyNum.get(0).luckyNum);

		assertNull(xcr.readNext());
	}

	@Test
	public void notRoot() throws Exception {
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

		XCardReader xcr = new XCardReader(xml);
		VCard vcard = xcr.readNext();

		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("House", n.getFamily());
		assertEquals("Gregory", n.getGiven());
		assertTrue(n.getAdditional().isEmpty());
		assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
		assertEquals(Arrays.asList("MD"), n.getSuffixes());

		assertNull(xcr.readNext());
	}

	@Test
	public void with_namespace_prefix() throws Exception {
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

		XCardReader xcr = new XCardReader(xml);

		VCard vcard = xcr.readNext();

		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		FormattedNameType fn = vcard.getFormattedName();
		assertEquals("Dr. Gregory House M.D.", fn.getValue());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("House", n.getFamily());
		assertEquals("Gregory", n.getGiven());
		assertTrue(n.getAdditional().isEmpty());
		assertEquals(Arrays.asList("Dr", "Mr"), n.getPrefixes());
		assertEquals(Arrays.asList("MD"), n.getSuffixes());

		assertNull(xcr.readNext());
	}

	@Test
	public void not_in_same_vcards_element() throws Exception {
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
			"<bar>" +
				"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
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
				"</vcards>" + 
			"</bar>" +
		"</foo>" ;
		//@formatter:on

		XCardReader xcr = new XCardReader(xml);

		{
			VCard vcard = xcr.readNext();

			assertEquals(VCardVersion.V4_0, vcard.getVersion());

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
			VCard vcard = xcr.readNext();

			assertEquals(VCardVersion.V4_0, vcard.getVersion());

			FormattedNameType fn = vcard.getFormattedName();
			assertEquals("Dr. Lisa Cuddy M.D.", fn.getValue());

			StructuredNameType n = vcard.getStructuredName();
			assertEquals("Cuddy", n.getFamily());
			assertEquals("Lisa", n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Dr", "Ms"), n.getPrefixes());
			assertEquals(Arrays.asList("MD"), n.getSuffixes());
		}

		assertNull(xcr.readNext());
	}
}
