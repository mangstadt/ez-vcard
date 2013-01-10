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
public class XCardReaderTest {
	/**
	 * Tests a basic xCard example
	 */
	@Test
	public void basic() throws Exception {
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<fn><text>Dr. Gregory House M.D.</text></fn>");
				sb.append("<n>");
					sb.append("<surname>House</surname>");
					sb.append("<given>Gregory</given>");
					sb.append("<additional />");
					sb.append("<prefix>Dr</prefix>");
					sb.append("<prefix>Mr</prefix>");
					sb.append("<suffix>MD</suffix>");
				sb.append("</n>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
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
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<note><text>  This \t  is \n   a   note </text></note>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
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
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				//zero params
				sb.append("<note>");
					sb.append("<text>Note 1</text>");
				sb.append("</note>");
				
				//one param
				sb.append("<note>");
					sb.append("<parameters>");
						sb.append("<altid><text>1</text></altid>");
					sb.append("</parameters>");
					sb.append("<text>Hello world!</text>");
				sb.append("</note>");
				
				//two params
				sb.append("<note>");
					sb.append("<parameters>");
						sb.append("<altid><text>1</text></altid>");
						sb.append("<language><language-tag>fr</language-tag></language>");
					sb.append("</parameters>");
					sb.append("<text>Bonjour tout le monde!</text>");
				sb.append("</note>");
				
				//a param with multiple values
				sb.append("<tel>");
					sb.append("<parameters>");
						sb.append("<type>");
							sb.append("<text>work</text>");
							sb.append("<text>voice</text>");
						sb.append("</type>");
					sb.append("</parameters>");
					sb.append("<uri>tel:+1-555-555-1234</uri>");
				sb.append("</tel>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
		VCard vcard = xcr.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		Iterator<NoteType> notesIt = vcard.getNotes().iterator();

		NoteType note = notesIt.next();
		assertEquals("Note 1", note.getValue());
		assertTrue(note.getSubTypes().getMultimap().isEmpty());

		note = notesIt.next();
		assertEquals("Hello world!", note.getValue());
		assertEquals(1, note.getSubTypes().getMultimap().size());
		assertEquals("1", note.getAltId());

		note = notesIt.next();
		assertEquals("Bonjour tout le monde!", note.getValue());
		assertEquals(2, note.getSubTypes().getMultimap().size());
		assertEquals("1", note.getAltId());
		assertEquals("fr", note.getLanguage());

		assertFalse(notesIt.hasNext());

		Iterator<TelephoneType> telIt = vcard.getTelephoneNumbers().iterator();

		TelephoneType tel = telIt.next();
		assertEquals("+1-555-555-1234", tel.getValue());
		assertEquals(2, tel.getSubTypes().getMultimap().size());
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
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<group name=\"item1\">");
					sb.append("<fn><text>John Doe</text></fn>");
					sb.append("<note><text>Hello world!</text></note>");
				sb.append("</group>");
				sb.append("<note><text>A property without a group</text></note>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
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
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				//XmlType (non-standard type that does not start with "x-")
				sb.append("<a href=\"http://www.website.com\">website</a>");
				
				//LuckyNumType (with marshal methods and with QName)
				sb.append("<a:lucky-num xmlns:a=\"http://luckynum.com\"><integer>21</integer></a:lucky-num>");
				
				//SalaryType (with marshal methods and without QName)
				sb.append("<x-salary><integer>1000000</integer></x-salary>");
				
				//AgeType (without marshal methods and without QName)
				//should be unmarshalled as XmlType
				sb.append("<x-age><integer>24</integer></x-age>");
				
				//RawType (extended type that starts with "x-")
				sb.append("<x-gender><text>m</text></x-gender>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
		xcr.registerExtendedType(LuckyNumType.class);
		xcr.registerExtendedType(SalaryType.class);
		xcr.registerExtendedType(AgeType.class);
		VCard vcard = xcr.readNext();

		List<XmlType> xmlTypes = vcard.getXmls();
		assertEquals(2, xmlTypes.size());
		assertEquals("<a href=\"http://www.website.com\" xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">website</a>", xmlTypes.get(0).getValue());
		//X-AGE was not unmarshalled because its type class does not support xCard;
		assertEquals("<x-age xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\"><integer>24</integer></x-age>", xmlTypes.get(1).getValue());

		List<LuckyNumType> luckyNum = vcard.getExtendedType(LuckyNumType.class);
		assertEquals(1, luckyNum.size());
		assertEquals(21, luckyNum.get(0).luckyNum);

		List<SalaryType> salary = vcard.getExtendedType(SalaryType.class);
		assertEquals(1, salary.size());
		assertEquals(1000000, salary.get(0).salary);

		List<RawType> gender = vcard.getExtendedType("X-GENDER");
		assertEquals(1, gender.size());
		assertEquals("m", gender.get(0).getValue());

		//warning for AgeType not supporting xCard
		assertEquals(1, xcr.getWarnings().size());

		assertNull(xcr.readNext());
	}

	/**
	 * Make sure it can handle multiple "&lt;vcard&gt;" elements.
	 */
	@Test
	public void readMultipleVCards() throws Exception {
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
			sb.append("<vcard>");
				sb.append("<fn><text>Dr. Gregory House M.D.</text></fn>");
			sb.append("</vcard>");
			sb.append("<vcard>");
				sb.append("<n>");
					sb.append("<surname>House</surname>");
					sb.append("<given>Gregory</given>");
					sb.append("<additional />");
					sb.append("<prefix>Dr</prefix>");
					sb.append("<prefix>Mr</prefix>");
					sb.append("<suffix>MD</suffix>");
				sb.append("</n>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());

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
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards>");
			sb.append("<vcard>");
				sb.append("<fn><text>Dr. Gregory House M.D.</text></fn>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
		assertNull(xcr.readNext());

		//wrong namespace
		//@formatter:off
		sb = new StringBuilder();
		sb.append("<vcards xmlns=\"wrong\">");
			sb.append("<vcard>");
				sb.append("<fn><text>Dr. Gregory House M.D.</text></fn>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		xcr = new XCardReader(sb.toString());
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
		StringBuilder sb = new StringBuilder();
		sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\" xmlns:a=\"http://luckynum.com\">");
			sb.append("<vcard>");
				sb.append("<a:lucky-num><integer>24</integer></a:lucky-num>");
				sb.append("<a:lucky-num><integer>13</integer></a:lucky-num>");
			sb.append("</vcard>");
		sb.append("</vcards>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
		xcr.registerExtendedType(LuckyNumType.class);
		VCard vcard = xcr.readNext();

		assertEquals(1, xcr.getWarnings().size());

		List<LuckyNumType> luckyNum = vcard.getExtendedType(LuckyNumType.class);
		assertEquals(1, luckyNum.size());
		assertEquals(24, luckyNum.get(0).luckyNum);

		assertNull(xcr.readNext());
	}

	@Test
	public void notRoot() throws Exception {
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("<foo xmlns=\"http://foobar.com\">");
			sb.append("<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">");
				sb.append("<vcard>");
					sb.append("<fn><text>Dr. Gregory House M.D.</text></fn>");
					sb.append("<n>");
						sb.append("<surname>House</surname>");
						sb.append("<given>Gregory</given>");
						sb.append("<additional />");
						sb.append("<prefix>Dr</prefix>");
						sb.append("<prefix>Mr</prefix>");
						sb.append("<suffix>MD</suffix>");
					sb.append("</n>");
				sb.append("</vcard>");
			sb.append("</vcards>");
		sb.append("</foo>");
		//@formatter:on

		XCardReader xcr = new XCardReader(sb.toString());
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
}
