package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.BirthdayType;
import ezvcard.types.CategoriesType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.LabelType;
import ezvcard.types.NicknameType;
import ezvcard.types.NoteType;
import ezvcard.types.OrgType;
import ezvcard.types.RawType;
import ezvcard.types.RevisionType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TitleType;
import ezvcard.types.UidType;
import ezvcard.types.UrlType;
import ezvcard.types.VCardType;

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
public class VCardReaderTest {
	/**
	 * Tests to make sure it can read sub types properly
	 */
	@Test
	public void getSubTypes() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("NOTE;x-size=8: The note\r\n");
		sb.append("ADR;HOME;WORK: ;;;;\r\n"); //nameless parameters
		sb.append("LABEL;type=dOm;TyPE=parcel: \r\n"); //repeated parameter name
		sb.append("END: vcard\r\n");
		VCardReader reader = new VCardReader(new StringReader(sb.toString()));
		VCard vcard = reader.readNext();

		NoteType note = vcard.getNotes().get(0);
		assertEquals("8", note.getSubTypes().getFirst("X-SIZE"));
		assertEquals("8", note.getSubTypes().getFirst("x-size"));
		assertNull(note.getSubTypes().getFirst("non-existant"));

		AddressType adr = vcard.getAddresses().get(0);
		assertEquals(2, adr.getTypes().size());
		assertTrue(adr.getTypes().contains(AddressTypeParameter.HOME));
		assertTrue(adr.getTypes().contains(AddressTypeParameter.WORK));

		LabelType label = vcard.getLabels().get(0);
		assertEquals(2, adr.getTypes().size());
		assertTrue(label.getTypes().contains(AddressTypeParameter.DOM));
		assertTrue(label.getTypes().contains(AddressTypeParameter.PARCEL));
	}

	/**
	 * All "quoted-printable" values should be decoded by the VCardReader.
	 */
	@Test
	public void decodeQuotedPrintable() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:123 Main St.=0D=0A\r\n");
		sb.append(" Austin, TX 91827=0D=0A\r\n");
		sb.append(" USA\r\n");
		sb.append("END: vcard\r\n");
		VCardReader reader = new VCardReader(new StringReader(sb.toString()));
		VCard vcard = reader.readNext();

		LabelType label = vcard.getLabels().get(0);
		assertEquals("123 Main St.\r\nAustin, TX 91827\r\nUSA", label.getValue());
		assertNull(label.getSubTypes().getEncoding()); //ENCODING sub type should be removed
	}

	/**
	 * Tests to make sure it can unfold folded lines.
	 */
	@Test
	public void unfold() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("NOTE: The vCard MIME Directory Profile also provides support for represent\r\n");
		sb.append(" ing other important information about the person associated with the dire\r\n");
		sb.append(" ctory entry. For instance, the date of birth of the person\\; an audio clip \r\n");
		sb.append(" describing the pronunciation of the name associated with the directory en\r\n");
		sb.append(" try, or some other application of the digital sound\\; longitude and latitu\r\n");
		sb.append(" de geo-positioning information related to the person associated with the \r\n");
		sb.append(" directory entry\\; date and time that the directory information was last up\r\n");
		sb.append(" dated\\; annotations often written on a business card\\; Uniform Resource Loc\r\n");
		sb.append(" ators (URL) for a website\\; public key information.\r\n");
		sb.append("END: vcard\r\n");
		VCardReader reader = new VCardReader(new StringReader(sb.toString()));
		VCard vcard = reader.readNext();

		String expected = "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.";
		String actual = vcard.getNotes().get(0).getValue();
		assertEquals(expected, actual);
	}

	/**
	 * Tests to make sure it can read custom types.
	 */
	@Test
	public void readCustomType() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("X-LUCKY-NUM: 24\r\n");
		sb.append("X-GENDER: ma\\,le\r\n");
		sb.append("X-LUCKY-NUM: 22\r\n");
		sb.append("END: vcard\r\n");
		VCardReader reader = new VCardReader(new StringReader(sb.toString()));
		reader.registerCustomType(LuckyNumType.class);
		VCard vcard = reader.readNext();

		//read a type that has a type class
		List<LuckyNumType> luckyNumTypes = vcard.getCustomType(LuckyNumType.class);
		assertEquals(2, luckyNumTypes.size());
		assertEquals(24, luckyNumTypes.get(0).luckyNum);
		assertEquals(22, luckyNumTypes.get(1).luckyNum);
		assertTrue(vcard.getCustomType("X-LUCKY-NUM").isEmpty());

		//read a type without a type class
		List<RawType> genderTypes = vcard.getCustomType("X-GENDER");
		assertEquals(1, genderTypes.size());
		assertEquals("ma\\,le", genderTypes.get(0).getValue()); //raw type values are not unescaped
	}

	/**
	 * Tests to make sure it can read multiple vCards from the same stream.
	 */
	@Test
	public void readMultiple() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("FN: John Doe\r\n");
		sb.append("END: vcard\r\n");
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 3.0\r\n");
		sb.append("FN: Jane Doe\r\n");
		sb.append("END: vcard\r\n");
		VCardReader reader = new VCardReader(new StringReader(sb.toString()));
		VCard vcard;

		vcard = reader.readNext();
		assertEquals(VCardVersion.V2_1, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = reader.readNext();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void evolutionVCard() throws Exception {
		VCardReader reader = new VCardReader(new InputStreamReader(getClass().getResourceAsStream("John_Doe_EVOLUTION.vcf")));
		reader.setCompatibilityMode(CompatibilityMode.EVOLUTION);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType t = it.next();
			assertEquals("http://www.ibm.com", t.getValue());
			//FIXME double quotes are not removed
			//parameter values can be enclosed in double quotes to auto-escape special chars
			//see RFC 2426 p.29 -- "param-value = ptext / quoted-string"
			//assertEquals("0abc9b8d-0845-47d0-9a91-3db5bb74620d", t.getSubTypes().getFirst("X-COUCHDB-UUID"));
			assertEquals("\"0abc9b8d-0845-47d0-9a91-3db5bb74620d\"", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType t = it.next();
			assertEquals("905-666-1234", t.getPhoneNumber());
			Set<TelephoneTypeParameter> types = t.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));
			//FIXME double quotes are not removed
			//assertEquals("c2fa1caa-2926-4087-8971-609cfc7354ce", t.getSubTypes().getFirst("X-COUCHDB-UUID"));
			assertEquals("\"c2fa1caa-2926-4087-8971-609cfc7354ce\"", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			t = it.next();
			assertEquals("905-555-1234", t.getPhoneNumber());
			types = t.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));
			//FIXME double quotes are not removed
			//assertEquals("fbfb2722-4fd8-4dbf-9abd-eeb24072fd8e", t.getSubTypes().getFirst("X-COUCHDB-UUID"));
			assertEquals("\"fbfb2722-4fd8-4dbf-9abd-eeb24072fd8e\"", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//UID
		{
			Iterator<UidType> it = vcard.getUids().iterator();

			UidType t = it.next();
			assertEquals("477343c8e6bf375a9bac1f96a5000837", t.getValue());

			assertFalse(it.hasNext());
		}

		//N
		{
			StructuredNameType t = vcard.getStructuredName();
			assertEquals("Doe", t.getFamily());
			assertEquals("John", t.getGiven());
			List<String> list = t.getAdditional();
			assertEquals(Arrays.asList("Richter, James"), list);
			list = t.getPrefixes();
			assertEquals(Arrays.asList("Mr."), list);
			list = t.getSuffixes();
			assertEquals(Arrays.asList("Sr."), list);
		}

		//FN
		{
			FormattedNameType t = vcard.getFormattedName();
			assertEquals("Mr. John Richter, James Doe Sr.", t.getValue());
		}

		//NICKNAME
		{
			NicknameType t = vcard.getNicknames();
			assertEquals(Arrays.asList("Johny"), t.getValues());
		}

		//ORG
		{
			OrgType t = vcard.getOrganizations();
			assertEquals(Arrays.asList("IBM", "Accounting", "Dungeon"), t.getValues());
		}

		//TITLE
		{
			TitleType t = vcard.getTitle();
			assertEquals("Money Counter", t.getValue());
		}

		//CATEGORIES
		{
			CategoriesType t = vcard.getCategories();
			assertEquals(Arrays.asList("VIP"), t.getValues());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();
			NoteType t = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.", t.getValue());
			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType t = it.next();
			assertEquals("john.doe@ibm.com", t.getEmail());
			Set<EmailTypeParameter> types = t.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(new EmailTypeParameter("work"))); //non-standard type
			//FIXME double quotes are not removed
			//assertEquals("83a75a5d-2777-45aa-bab5-76a4bd972490", t.getSubTypes().getFirst("X-COUCHDB-UUID"));
			assertEquals("\"83a75a5d-2777-45aa-bab5-76a4bd972490\"", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType t = it.next();
			assertEquals("ASB-123", t.getPoBox());
			assertEquals(null, t.getExtendedAddr());
			assertEquals("15 Crescent moon drive", t.getStreetAddr());
			assertEquals("Albaney", t.getLocality());
			assertEquals("New York", t.getRegion());
			assertEquals("12345", t.getPostalCode());
			//the space between "United" and "States" is lost because it was included with the folding character and ignored (see .vcf file)
			assertEquals("UnitedStates of America", t.getCountry());

			Set<AddressTypeParameter> types = t.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType t = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 22);
			Date expected = c.getTime();
			assertEquals(expected, t.getDate());
		}

		//REV
		{
			RevisionType t = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 5);
			c.set(Calendar.HOUR_OF_DAY, 13);
			c.set(Calendar.MINUTE, 32);
			c.set(Calendar.SECOND, 54);
			assertEquals(c.getTime(), t.getDate());
		}

		//custom types
		{
			assertEquals(7, vcard.getCustomTypes().values().size());

			Iterator<RawType> it = vcard.getCustomType("X-COUCHDB-APPLICATION-ANNOTATIONS").iterator();
			RawType t = it.next();
			assertEquals("X-COUCHDB-APPLICATION-ANNOTATIONS", t.getTypeName());
			assertEquals("{\"Evolution\":{\"revision\":\"2012-03-05T13:32:54Z\"}}", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getCustomType("X-AIM").iterator();
			t = it.next();
			assertEquals("X-AIM", t.getTypeName());
			assertEquals("johnny5@aol.com", t.getValue());
			assertEquals("HOME", t.getSubTypes().getType());
			//FIXME double quotes are not removed
			//assertEquals("cb9e11fc-bb97-4222-9cd8-99820c1de454", t.getSubTypes().getFirst("X-COUCHDB-UUID"));
			assertEquals("\"cb9e11fc-bb97-4222-9cd8-99820c1de454\"", t.getSubTypes().getFirst("X-COUCHDB-UUID"));
			assertFalse(it.hasNext());

			it = vcard.getCustomType("X-EVOLUTION-FILE-AS").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-FILE-AS", t.getTypeName());
			assertEquals("Doe\\, John", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getCustomType("X-EVOLUTION-SPOUSE").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-SPOUSE", t.getTypeName());
			assertEquals("Maria", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getCustomType("X-EVOLUTION-MANAGER").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-MANAGER", t.getTypeName());
			assertEquals("Big Blue", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getCustomType("X-EVOLUTION-ASSISTANT").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-ASSISTANT", t.getTypeName());
			assertEquals("Little Red", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getCustomType("X-EVOLUTION-ANNIVERSARY").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-ANNIVERSARY", t.getTypeName());
			assertEquals("1980-03-22", t.getValue());
			assertFalse(it.hasNext());
		}
	}

	public static class LuckyNumType extends VCardType {
		public int luckyNum;

		public LuckyNumType() {
			super("X-LUCKY-NUM");
		}

		@Override
		protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
			return "" + luckyNum;
		}

		@Override
		protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
			luckyNum = Integer.parseInt(value);
		}
	}
}
