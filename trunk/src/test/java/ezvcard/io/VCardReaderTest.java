package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.KeyTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.BirthdayType;
import ezvcard.types.CategoriesType;
import ezvcard.types.ClassificationType;
import ezvcard.types.EmailType;
import ezvcard.types.FbUrlType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GeoType;
import ezvcard.types.KeyType;
import ezvcard.types.LabelType;
import ezvcard.types.MailerType;
import ezvcard.types.NicknameType;
import ezvcard.types.NoteType;
import ezvcard.types.OrganizationType;
import ezvcard.types.PhotoType;
import ezvcard.types.ProdIdType;
import ezvcard.types.ProfileType;
import ezvcard.types.RawType;
import ezvcard.types.RevisionType;
import ezvcard.types.RoleType;
import ezvcard.types.SortStringType;
import ezvcard.types.SourceDisplayTextType;
import ezvcard.types.SourceType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TimezoneType;
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
	private static final String newline = System.getProperty("line.separator");

	/**
	 * Tests to make sure it can read sub types properly
	 */
	@Test
	public void getSubTypes() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("NOTE;x-size=8: The note\r\n");
		sb.append("ADR;HOME;WORK: ;;;;\r\n"); //nameless parameters
		sb.append("LABEL;type=dOm;TyPE=parcel: \r\n"); //repeated parameter name
		sb.append("END: VCARD\r\n");
		VCardReader reader = new VCardReader(sb.toString());
		VCard vcard = reader.readNext();

		NoteType note = vcard.getNotes().get(0);
		assertEquals("8", note.getSubTypes().getFirst("X-SIZE"));
		assertEquals("8", note.getSubTypes().getFirst("x-size"));
		assertNull(note.getSubTypes().getFirst("non-existant"));

		AddressType adr = vcard.getAddresses().get(0);
		assertEquals(2, adr.getTypes().size());
		assertTrue(adr.getTypes().contains(AddressTypeParameter.HOME));
		assertTrue(adr.getTypes().contains(AddressTypeParameter.WORK));

		LabelType label = vcard.getOrphanedLabels().get(0);
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
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:123 Main St.=0D=0A\r\n");
		sb.append(" Austin, TX 91827=0D=0A\r\n");
		sb.append(" USA\r\n");
		sb.append("END: VCARD\r\n");
		VCardReader reader = new VCardReader(sb.toString());
		VCard vcard = reader.readNext();

		LabelType label = vcard.getOrphanedLabels().get(0);
		assertEquals("123 Main St.\r\nAustin, TX 91827\r\nUSA", label.getValue());
		assertNull(label.getSubTypes().getEncoding()); //ENCODING sub type should be removed
	}

	/**
	 * Tests to make sure it can unfold folded lines.
	 */
	@Test
	public void unfold() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
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
		sb.append("END: VCARD\r\n");
		VCardReader reader = new VCardReader(sb.toString());
		VCard vcard = reader.readNext();

		String expected = "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.";
		String actual = vcard.getNotes().get(0).getValue();
		assertEquals(expected, actual);
	}

	/**
	 * Tests to make sure it can read extended types.
	 */
	@Test
	public void readExtendedType() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("X-LUCKY-NUM: 24\r\n");
		sb.append("X-GENDER: ma\\,le\r\n");
		sb.append("X-LUCKY-NUM: 22\r\n");
		sb.append("END: VCARD\r\n");
		VCardReader reader = new VCardReader(sb.toString());
		reader.registerExtendedType(LuckyNumType.class);
		VCard vcard = reader.readNext();

		//read a type that has a type class
		List<LuckyNumType> luckyNumTypes = vcard.getExtendedType(LuckyNumType.class);
		assertEquals(2, luckyNumTypes.size());
		assertEquals(24, luckyNumTypes.get(0).luckyNum);
		assertEquals(22, luckyNumTypes.get(1).luckyNum);
		assertTrue(vcard.getExtendedType("X-LUCKY-NUM").isEmpty());

		//read a type without a type class
		List<RawType> genderTypes = vcard.getExtendedType("X-GENDER");
		assertEquals(1, genderTypes.size());
		assertEquals("ma\\,le", genderTypes.get(0).getValue()); //raw type values are not unescaped
	}

	/**
	 * Tests to make sure it can read multiple vCards from the same stream.
	 */
	@Test
	public void readMultiple() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("FN: John Doe\r\n");
		sb.append("END: VCARD\r\n");
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 3.0\r\n");
		sb.append("FN: Jane Doe\r\n");
		sb.append("END: VCARD\r\n");
		VCardReader reader = new VCardReader(sb.toString());
		VCard vcard;

		vcard = reader.readNext();
		assertEquals(VCardVersion.V2_1, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = reader.readNext();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	/**
	 * Tests types with nested vCards (i.e AGENT type) in version 2.1.
	 */
	@Test
	public void nestedVCard() throws Exception {
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("AGENT:\r\n");
			sb.append("BEGIN: VCARD\r\n");
			sb.append("VERSION: 2.1\r\n");
			sb.append("FN: Agent 007\r\n");
			sb.append("AGENT:\r\n");
				sb.append("BEGIN: VCARD\r\n");
				sb.append("VERSION: 2.1\r\n");
				sb.append("FN: Agent 009\r\n");
				sb.append("END: VCARD\r\n");
			sb.append("END: VCARD\r\n");
		sb.append("FN: John Doe\r\n");
		sb.append("END: VCARD\r\n");
		//@formatter:on

		VCardReader reader = new VCardReader(sb.toString());
		VCard vcard = reader.readNext();

		assertEquals("John Doe", vcard.getFormattedName().getValue());
		VCard agent1 = vcard.getAgent().getVCard();
		assertEquals("Agent 007", agent1.getFormattedName().getValue());
		VCard agent2 = agent1.getAgent().getVCard();
		assertEquals("Agent 009", agent2.getFormattedName().getValue());
	}

	/**
	 * Tests types with embedded vCards (i.e. AGENT type) in version 3.0.
	 */
	@Test
	public void embeddedVCard() throws Exception {
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 3.0\r\n");
		sb.append("AGENT: ");
			sb.append("BEGIN: VCARD\\n");
			sb.append("VERSION: 3.0\\n");
			sb.append("FN: Agent 007\\n");
			sb.append("AGENT: ");
				sb.append("BEGIN: VCARD\\\\n");
				sb.append("VERSION: 3.0\\\\n");
				sb.append("FN: Agent 009\\\\n");
				sb.append("END: VCARD\\\\n");
			sb.append("END: VCARD\r\n");
		sb.append("FN: John Doe\r\n");
		sb.append("END: VCARD\r\n");
		//@formatter:on

		VCardReader reader = new VCardReader(sb.toString());
		VCard vcard = reader.readNext();

		assertEquals("John Doe", vcard.getFormattedName().getValue());
		VCard agent1 = vcard.getAgent().getVCard();
		assertEquals("Agent 007", agent1.getFormattedName().getValue());
		VCard agent2 = agent1.getAgent().getVCard();
		assertEquals("Agent 009", agent2.getFormattedName().getValue());
	}

	/**
	 * LABEL types should be assigned to an ADR and stored in the
	 * "AddressType.getLabel()" field. LABELs that could not be assigned to an
	 * ADR should go in "VCard.getOrphanedLabels()".
	 */
	@Test
	public void readLabel() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 3.0\r\n");
		sb.append("ADR;TYPE=home:;;123 Main St.;Austin;TX;91827;USA\r\n");
		sb.append("LABEL;TYPE=home:123 Main St.\\nAustin\\, TX 91827\\nUSA\r\n");
		sb.append("ADR;TYPE=work,parcel:;;200 Broadway;New York;NY;12345;USA\r\n");
		sb.append("LABEL;TYPE=work:200 Broadway\\nNew York\\, NY 12345\\nUSA\r\n");
		sb.append("END: VCARD\r\n");
		VCardReader reader = new VCardReader(sb.toString());
		VCard vcard = reader.readNext();

		assertEquals(2, vcard.getAddresses().size());

		AddressType adr = vcard.getAddresses().get(0);
		assertEquals(1, adr.getTypes().size());
		assertTrue(adr.getTypes().contains(AddressTypeParameter.HOME));
		assertEquals("123 Main St." + newline + "Austin, TX 91827" + newline + "USA", adr.getLabel());

		adr = vcard.getAddresses().get(1);
		assertEquals(2, adr.getTypes().size());
		assertTrue(adr.getTypes().contains(AddressTypeParameter.WORK));
		assertTrue(adr.getTypes().contains(AddressTypeParameter.PARCEL));
		assertNull(adr.getLabel());

		assertEquals(1, vcard.getOrphanedLabels().size());
		LabelType label = vcard.getOrphanedLabels().get(0);
		assertEquals("200 Broadway" + newline + "New York, NY 12345" + newline + "USA", label.getValue());
		assertEquals(1, label.getTypes().size());
		assertTrue(label.getTypes().contains(AddressTypeParameter.WORK));
	}

	/**
	 * If the type's unmarshal method throws a {@link SkipMeException}, then a
	 * warning should be added to the warnings list and the type object should
	 * NOT be added to the {@link VCard} object.
	 */
	@Test
	public void skipMeException() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\r\n");
		sb.append("VERSION: 3.0\r\n");
		sb.append("X-LUCKY-NUM: 24\r\n");
		sb.append("X-LUCKY-NUM: 13\r\n");
		sb.append("END: VCARD\r\n");
		VCardReader reader = new VCardReader(sb.toString());
		reader.registerExtendedType(LuckyNumType.class);
		VCard vcard = reader.readNext();

		assertEquals(1, reader.getWarnings().size());

		List<LuckyNumType> luckyNumTypes = vcard.getExtendedType(LuckyNumType.class);
		assertEquals(1, luckyNumTypes.size());
		assertEquals(24, luckyNumTypes.get(0).luckyNum);
	}

	@Test
	public void evolutionVCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_EVOLUTION.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.EVOLUTION);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType t = it.next();
			assertEquals("http://www.ibm.com", t.getValue());
			assertEquals("0abc9b8d-0845-47d0-9a91-3db5bb74620d", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType t = it.next();
			assertEquals("905-666-1234", t.getValue());
			Set<TelephoneTypeParameter> types = t.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));
			assertEquals("c2fa1caa-2926-4087-8971-609cfc7354ce", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			t = it.next();
			assertEquals("905-555-1234", t.getValue());
			types = t.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));
			assertEquals("fbfb2722-4fd8-4dbf-9abd-eeb24072fd8e", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//UID
		{
			UidType t = vcard.getUid();
			assertEquals("477343c8e6bf375a9bac1f96a5000837", t.getValue());
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
			NicknameType t = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), t.getValues());
		}

		//ORG
		{
			OrganizationType t = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting", "Dungeon"), t.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType t = it.next();
			assertEquals("Money Counter", t.getValue());

			assertFalse(it.hasNext());
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
			assertEquals("john.doe@ibm.com", t.getValue());
			Set<EmailTypeParameter> types = t.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(new EmailTypeParameter("work"))); //non-standard type
			assertEquals("83a75a5d-2777-45aa-bab5-76a4bd972490", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType t = it.next();
			assertEquals("ASB-123", t.getPoBox());
			assertEquals(null, t.getExtendedAddress());
			assertEquals("15 Crescent moon drive", t.getStreetAddress());
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
			assertEquals(c.getTime(), t.getTimestamp());
		}

		//extended types
		{
			assertEquals(7, countExtTypes(vcard));

			Iterator<RawType> it = vcard.getExtendedType("X-COUCHDB-APPLICATION-ANNOTATIONS").iterator();
			RawType t = it.next();
			assertEquals("X-COUCHDB-APPLICATION-ANNOTATIONS", t.getTypeName());
			assertEquals("{\"Evolution\":{\"revision\":\"2012-03-05T13:32:54Z\"}}", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedType("X-AIM").iterator();
			t = it.next();
			assertEquals("X-AIM", t.getTypeName());
			assertEquals("johnny5@aol.com", t.getValue());
			assertEquals("HOME", t.getSubTypes().getType());
			assertEquals("cb9e11fc-bb97-4222-9cd8-99820c1de454", t.getSubTypes().getFirst("X-COUCHDB-UUID"));
			assertFalse(it.hasNext());

			it = vcard.getExtendedType("X-EVOLUTION-FILE-AS").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-FILE-AS", t.getTypeName());
			assertEquals("Doe\\, John", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedType("X-EVOLUTION-SPOUSE").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-SPOUSE", t.getTypeName());
			assertEquals("Maria", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedType("X-EVOLUTION-MANAGER").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-MANAGER", t.getTypeName());
			assertEquals("Big Blue", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedType("X-EVOLUTION-ASSISTANT").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-ASSISTANT", t.getTypeName());
			assertEquals("Little Red", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedType("X-EVOLUTION-ANNIVERSARY").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-ANNIVERSARY", t.getTypeName());
			assertEquals("1980-03-22", t.getValue());
			assertFalse(it.hasNext());
		}
	}

	@Test
	public void gmailVCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_GMAIL.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.GMAIL);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Mr. John Richter, James Doe Sr.", f.getValue());
		}

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter, James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("john.doe@ibm.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));
			assertTrue(types.contains(new EmailTypeParameter("home")));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType f = it.next();
			assertEquals("905-555-1234", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));

			f = it.next();
			assertEquals("905-666-1234", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("Crescent moon drive" + newline + "555-asd" + newline + "Nice Area, Albaney, New York12345" + newline + "United States of America", f.getExtendedAddress());
			assertEquals(null, f.getStreetAddress());
			assertEquals(null, f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals(null, f.getPostalCode());
			assertEquals(null, f.getCountry());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));

			assertFalse(it.hasNext());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 22);
			assertEquals(c.getTime(), f.getDate());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("http://www.ibm.com", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("WORK"));

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + newline + "Favotire Color: Blue", f.getValue());

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(6, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-PHONETIC-FIRST-NAME").get(0);
			assertEquals("X-PHONETIC-FIRST-NAME", f.getTypeName());
			assertEquals("Jon", f.getValue());

			f = vcard.getExtendedType("X-PHONETIC-LAST-NAME").get(0);
			assertEquals("X-PHONETIC-LAST-NAME", f.getTypeName());
			assertEquals("Dow", f.getValue());

			f = vcard.getExtendedType("X-ABDATE").get(0);
			assertEquals("X-ABDATE", f.getTypeName());
			assertEquals("1975-03-01", f.getValue());
			assertEquals("item1", f.getGroup());

			f = vcard.getExtendedType("X-ABLABEL").get(0);
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("_$!<Anniversary>!$_", f.getValue());
			assertEquals("item1", f.getGroup());

			f = vcard.getExtendedType("X-ABLABEL").get(1);
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("_$!<Spouse>!$_", f.getValue());
			assertEquals("item2", f.getGroup());

			f = vcard.getExtendedType("X-ABRELATEDNAMES").get(0);
			assertEquals("X-ABRELATEDNAMES", f.getTypeName());
			assertEquals("Jenny", f.getValue());
			assertEquals("item2", f.getGroup());
		}
	}

	/**
	 * This vCard was generated when selecting a list of contacts and exporting
	 * them as a vCard.
	 */
	@Test
	public void gmailList() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("gmail-list.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.GMAIL);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Arnold Smith", f.getValue());
		}

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Smith", f.getFamily());
			assertEquals("Arnold", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("asmithk@gmail.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Chris Beatle", f.getValue());
		}

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Beatle", f.getFamily());
			assertEquals("Chris", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("chrisy55d@yahoo.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Doug White", f.getValue());
		}

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("White", f.getFamily());
			assertEquals("Doug", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("dwhite@gmail.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void gmailSingle() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("gmail-single.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.GMAIL);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Greg Dartmouth", f.getValue());
		}

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Dartmouth", f.getFamily());
			assertEquals("Greg", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("gdartmouth@hotmail.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType f = it.next();
			assertEquals("555 555 1111", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));

			f = it.next();
			assertEquals("item1", f.getGroup());
			assertEquals("555 555 2222", f.getValue());
			types = f.getTypes();
			assertTrue(types.isEmpty());

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("123 Home St" + newline + "Home City, HM 12345", f.getStreetAddress());
			assertEquals(null, f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals(null, f.getPostalCode());
			assertEquals(null, f.getCountry());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));

			f = it.next();
			assertEquals("item2", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("321 Custom St", f.getStreetAddress());
			assertEquals("Custom City", f.getLocality());
			assertEquals("TX", f.getRegion());
			assertEquals("98765", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			types = f.getTypes();
			assertTrue(types.isEmpty());

			assertFalse(it.hasNext());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("TheCompany"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("TheJobTitle", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1960);
			c.set(Calendar.MONTH, Calendar.SEPTEMBER);
			c.set(Calendar.DAY_OF_MONTH, 10);
			assertEquals(c.getTime(), f.getDate());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("http://TheProfile.com", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertTrue(types.isEmpty());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("This is GMail's note field." + newline + "It should be added as a NOTE type." + newline + "ACustomField: CustomField", f.getValue());

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(12, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-PHONETIC-FIRST-NAME").get(0);
			assertEquals("X-PHONETIC-FIRST-NAME", f.getTypeName());
			assertEquals("Grregg", f.getValue());

			f = vcard.getExtendedType("X-PHONETIC-LAST-NAME").get(0);
			assertEquals("X-PHONETIC-LAST-NAME", f.getTypeName());
			assertEquals("Dart-mowth", f.getValue());

			f = vcard.getExtendedType("X-ICQ").get(0);
			assertEquals("X-ICQ", f.getTypeName());
			assertEquals("123456789", f.getValue());

			Iterator<RawType> abLabelIt = vcard.getExtendedType("X-ABLABEL").iterator();
			{
				f = abLabelIt.next();
				assertEquals("item1", f.getGroup());
				assertEquals("GRAND_CENTRAL", f.getValue());

				f = abLabelIt.next();
				assertEquals("item2", f.getGroup());
				assertEquals("CustomAdrType", f.getValue());

				f = abLabelIt.next();
				assertEquals("item3", f.getGroup());
				assertEquals("PROFILE", f.getValue());

				f = abLabelIt.next();
				assertEquals("item4", f.getGroup());
				assertEquals("_$!<Anniversary>!$_", f.getValue());

				f = abLabelIt.next();
				assertEquals("item5", f.getGroup());
				assertEquals("_$!<Spouse>!$_", f.getValue());

				f = abLabelIt.next();
				assertEquals("item6", f.getGroup());
				assertEquals("CustomRelationship", f.getValue());

				assertFalse(abLabelIt.hasNext());
			}

			f = vcard.getExtendedType("X-ABDATE").get(0);
			assertEquals("item4", f.getGroup());
			assertEquals("X-ABDATE", f.getTypeName());
			assertEquals("1970-06-02", f.getValue());

			f = vcard.getExtendedType("X-ABRELATEDNAMES").get(0);
			assertEquals("item5", f.getGroup());
			assertEquals("X-ABRELATEDNAMES", f.getTypeName());
			assertEquals("MySpouse", f.getValue());

			f = vcard.getExtendedType("X-ABRELATEDNAMES").get(1);
			assertEquals("item6", f.getGroup());
			assertEquals("X-ABRELATEDNAMES", f.getTypeName());
			assertEquals("MyCustom", f.getValue());
		}
	}

	@Test
	public void iPhoneVCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_IPHONE.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.I_PHONE);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//PRODID
		{
			ProdIdType f = vcard.getProdId();
			assertEquals("-//Apple Inc.//iOS 5.0.1//EN", f.getValue());
		}

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter", "James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Mr. John Richter James Doe Sr.", f.getValue());
		}

		//NICKNAME
		{
			NicknameType f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), f.getValues());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("item1", f.getGroup());
			assertEquals("john.doe@ibm.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));
			assertTrue(types.contains(EmailTypeParameter.PREF));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType f = it.next();
			assertEquals("905-555-1234", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(3, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));
			assertTrue(types.contains(TelephoneTypeParameter.PREF));

			f = it.next();
			assertEquals("905-666-1234", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("905-777-1234", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("905-888-1234", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));
			assertTrue(types.contains(TelephoneTypeParameter.FAX));

			f = it.next();
			assertEquals("905-999-1234", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.FAX));

			f = it.next();
			assertEquals("905-111-1234", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.PAGER));

			f = it.next();
			assertEquals("905-222-1234", f.getValue());
			assertEquals("item2", f.getGroup());
			types = f.getTypes();
			assertEquals(0, types.size());

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals("item3", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Silicon Alley 5,", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));
			assertTrue(types.contains(AddressTypeParameter.PREF));

			f = it.next();
			assertEquals("item4", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Street4" + newline + "Building 6" + newline + "Floor 8", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("USA", f.getCountry());

			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(AddressTypeParameter.WORK));

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("item5", f.getGroup());
			assertEquals("http://www.ibm.com", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("pref"));

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 6);
			assertEquals(c.getTime(), f.getDate());
		}

		//PHOTO
		{
			Iterator<PhotoType> it = vcard.getPhotos().iterator();

			PhotoType f = it.next();
			assertEquals(ImageTypeParameter.JPEG, f.getContentType());
			assertEquals(32531, f.getData().length);
		}

		//extended types
		{
			assertEquals(4, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-ABLABEL").get(0);
			assertEquals("item2", f.getGroup());
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("_$!<AssistantPhone>!$_", f.getValue());

			f = vcard.getExtendedType("X-ABADR").get(0);
			assertEquals("item3", f.getGroup());
			assertEquals("X-ABADR", f.getTypeName());
			assertEquals("Silicon Alley", f.getValue());

			f = vcard.getExtendedType("X-ABADR").get(1);
			assertEquals("item4", f.getGroup());
			assertEquals("X-ABADR", f.getTypeName());
			assertEquals("Street 4, Building 6,\\n Floor 8\\nNew York\\nUSA", f.getValue());

			f = vcard.getExtendedType("X-ABLABEL").get(1);
			assertEquals("item5", f.getGroup());
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("_$!<HomePage>!$_", f.getValue());
		}
	}

	@Test
	public void lotusNotesVCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_LOTUS_NOTES.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.MAC_ADDRESS_BOOK);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//PRODID
		{
			ProdIdType f = vcard.getProdId();
			assertEquals("-//Apple Inc.//Address Book 6.1//EN", f.getValue());
		}

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Johny"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("I"), f.getSuffixes());
		}

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Mr. Doe John I Johny", f.getValue());
		}

		//NICKNAME
		{
			NicknameType f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny,JayJay"), f.getValues());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "SUN"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("Generic Accountant", f.getValue());

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("john.doe@ibm.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(3, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));
			assertTrue(types.contains(new EmailTypeParameter("WORK")));
			assertTrue(types.contains(EmailTypeParameter.PREF));

			f = it.next();
			assertEquals("billy_bob@gmail.com", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));
			assertTrue(types.contains(new EmailTypeParameter("WORK")));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();
			TelephoneType f = it.next();
			assertEquals("+1 (212) 204-34456", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(3, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));
			assertTrue(types.contains(TelephoneTypeParameter.PREF));

			f = it.next();
			assertEquals("00-1-212-555-7777", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.FAX));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals("item1", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("25334" + newline + "South cresent drive, Building 5, 3rd floo r", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("NYC887", f.getPostalCode());
			assertEquals("U.S.A.", f.getCountry());
			assertNull(f.getLabel());

			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));
			assertTrue(types.contains(AddressTypeParameter.PREF));

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"" + newline + "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO , THE" + newline + "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR P URPOSE" + newline + "ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTOR S BE" + newline + "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR" + newline + "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF" + newline + " SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS " + newline + "INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN" + newline + " CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)" + newline + "A RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE" + newline + " POSSIBILITY OF SUCH DAMAGE.", f.getValue());

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("item2", f.getGroup());
			assertEquals("http://www.sun.com", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("pref"));

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MAY);
			c.set(Calendar.DAY_OF_MONTH, 21);
			assertEquals(c.getTime(), f.getDate());
		}

		//PHOTO
		{
			Iterator<PhotoType> it = vcard.getPhotos().iterator();

			PhotoType f = it.next();
			assertEquals(ImageTypeParameter.JPEG, f.getContentType());
			assertEquals(7957, f.getData().length);

			assertFalse(it.hasNext());
		}

		//UID
		{
			UidType f = vcard.getUid();
			assertEquals("0e7602cc-443e-4b82-b4b1-90f62f99a199", f.getValue());
		}

		//GEO
		{
			GeoType f = vcard.getGeo();
			assertEquals(-2.6, f.getLatitude(), .01);
			assertEquals(3.4, f.getLongitude(), .01);
		}

		//CLASS
		{
			ClassificationType f = vcard.getClassification();
			assertEquals("Public", f.getValue());
		}

		//PROFILE
		{
			ProfileType f = vcard.getProfile();
			assertEquals("VCard", f.getValue());
		}

		//TZ
		{
			TimezoneType f = vcard.getTimezone();
			assertEquals(Integer.valueOf(1), f.getHourOffset());
			assertEquals(Integer.valueOf(0), f.getMinuteOffset());
		}

		//LABEL
		{
			Iterator<LabelType> it = vcard.getOrphanedLabels().iterator();

			LabelType f = it.next();
			assertEquals("John Doe" + newline + "New York, NewYork," + newline + "South Crecent Drive," + newline + "Building 5, floor 3," + newline + "USA", f.getValue());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(3, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));
			assertTrue(types.contains(AddressTypeParameter.PARCEL));
			assertTrue(types.contains(AddressTypeParameter.PREF));

			assertFalse(it.hasNext());
		}

		//SORT-STRING
		{
			SortStringType f = vcard.getSortString();
			assertEquals("JOHN", f.getValue());
		}

		//ROLE
		{
			Iterator<RoleType> it = vcard.getRoles().iterator();

			RoleType f = it.next();
			assertEquals("Counting Money", f.getValue());

			assertFalse(it.hasNext());
		}

		//SOURCE
		{
			Iterator<SourceType> it = vcard.getSources().iterator();

			SourceType f = it.next();
			assertEquals("Whatever", f.getValue());

			assertFalse(it.hasNext());
		}

		//MAILER
		{
			MailerType f = vcard.getMailer();
			assertEquals("Mozilla Thunderbird", f.getValue());
		}

		//NAME
		{
			SourceDisplayTextType f = vcard.getSourceDisplayText();
			assertEquals("VCard for John Doe", f.getValue());
		}

		//extended types
		{
			assertEquals(4, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-ABLABEL").get(0);
			assertEquals("item2", f.getGroup());
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("_$!<HomePage>!$_", f.getValue());

			f = vcard.getExtendedType("X-ABUID").get(0);
			assertEquals("X-ABUID", f.getTypeName());
			assertEquals("0E7602CC-443E-4B82-B4B1-90F62F99A199:ABPerson", f.getValue());

			f = vcard.getExtendedType("X-GENERATOR").get(0);
			assertEquals("X-GENERATOR", f.getTypeName());
			assertEquals("Cardme Generator", f.getValue());

			f = vcard.getExtendedType("X-LONG-STRING").get(0);
			assertEquals("X-LONG-STRING", f.getTypeName());
			assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", f.getValue());
		}
	}

	@Test
	public void msOutlookVCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_MS_OUTLOOK.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.MS_OUTLOOK);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V2_1, vcard.getVersion());

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("en-us", f.getSubTypes().getLanguage());
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter", "James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Mr. John Richter James Doe Sr.", f.getValue());
		}

		//NICKNAME
		{
			NicknameType f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), f.getValues());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.", f.getValue());

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();
			TelephoneType f = it.next();

			assertEquals("(905) 555-1234", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("(905) 666-1234", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Cresent moon drive", f.getStreetAddress());
			assertEquals("Albaney", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertEquals("Cresent moon drive\r\nAlbaney, New York  12345", f.getLabel());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(AddressTypeParameter.WORK));
			assertTrue(types.contains(AddressTypeParameter.PREF));

			f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Silicon Alley 5,", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertEquals("Silicon Alley 5,\r\nNew York, New York  12345", f.getLabel());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));

			assertFalse(it.hasNext());
		}

		//LABEL
		{
			assertTrue(vcard.getOrphanedLabels().isEmpty());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("http://www.ibm.com", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("WORK"));

			assertFalse(it.hasNext());
		}

		//ROLE
		{
			Iterator<RoleType> it = vcard.getRoles().iterator();

			RoleType f = it.next();
			assertEquals("Counting Money", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 22);
			assertEquals(c.getTime(), f.getDate());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("john.doe@ibm.cm", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(EmailTypeParameter.PREF));
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		//PHOTO
		{
			Iterator<PhotoType> it = vcard.getPhotos().iterator();

			PhotoType f = it.next();
			assertEquals(ImageTypeParameter.JPEG, f.getContentType());
			assertEquals(860, f.getData().length);

			assertFalse(it.hasNext());
		}

		//REV
		{
			RevisionType f = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 5);
			c.set(Calendar.HOUR_OF_DAY, 13);
			c.set(Calendar.MINUTE, 19);
			c.set(Calendar.SECOND, 33);
			assertEquals(c.getTime(), f.getTimestamp());
		}

		//extended types
		{
			assertEquals(6, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-MS-OL-DEFAULT-POSTAL-ADDRESS").get(0);
			assertEquals("X-MS-OL-DEFAULT-POSTAL-ADDRESS", f.getTypeName());
			assertEquals("2", f.getValue());

			f = vcard.getExtendedType("X-MS-ANNIVERSARY").get(0);
			assertEquals("X-MS-ANNIVERSARY", f.getTypeName());
			assertEquals("20110113", f.getValue());

			f = vcard.getExtendedType("X-MS-IMADDRESS").get(0);
			assertEquals("X-MS-IMADDRESS", f.getTypeName());
			assertEquals("johny5@aol.com", f.getValue());

			f = vcard.getExtendedType("X-MS-OL-DESIGN").get(0);
			assertEquals("X-MS-OL-DESIGN", f.getTypeName());
			assertEquals("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>", f.getValue());
			assertEquals("utf-8", f.getSubTypes().getCharset());

			f = vcard.getExtendedType("X-MS-MANAGER").get(0);
			assertEquals("X-MS-MANAGER", f.getTypeName());
			assertEquals("Big Blue", f.getValue());

			f = vcard.getExtendedType("X-MS-ASSISTANT").get(0);
			assertEquals("X-MS-ASSISTANT", f.getTypeName());
			assertEquals("Jenny", f.getValue());
		}
	}

	@Test
	public void outlook2007VCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("outlook-2007.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V2_1, vcard.getVersion());

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("en-us", f.getSubTypes().getLanguage());
			assertEquals("Angstadt", f.getFamily());
			assertEquals("Michael", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Jr."), f.getSuffixes());
		}

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Mr. Michael Angstadt Jr.", f.getValue());
		}

		//NICKNAME
		{
			NicknameType f = vcard.getNickname();
			assertEquals(Arrays.asList("Mike"), f.getValues());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("TheCompany", "TheDepartment"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("TheJobTitle", f.getValue());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("This is the NOTE field	\r\nI assume it encodes this text inside a NOTE vCard type.\r\nBut I'm not sure because there's text formatting going on here.\r\nIt does not preserve the formatting", f.getValue());
			assertEquals("us-ascii", f.getSubTypes().getCharset());

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();
			TelephoneType f = it.next();

			assertEquals("(111) 555-1111", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("(111) 555-2222", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("(111) 555-4444", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("(111) 555-3333", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.FAX));
			assertTrue(types.contains(TelephoneTypeParameter.WORK));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("TheOffice", f.getExtendedAddress());
			assertEquals("222 Broadway", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("NY", f.getRegion());
			assertEquals("99999", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			assertEquals("222 Broadway\r\nNew York, NY 99999\r\nUSA", f.getLabel());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(AddressTypeParameter.WORK));
			assertTrue(types.contains(AddressTypeParameter.PREF));

			assertFalse(it.hasNext());
		}

		//LABEL
		{
			assertTrue(vcard.getOrphanedLabels().isEmpty());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("http://mikeangstadt.name", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("HOME"));

			f = it.next();
			assertEquals("http://mikeangstadt.name", f.getValue());
			types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("WORK"));

			assertFalse(it.hasNext());
		}

		//ROLE
		{
			Iterator<RoleType> it = vcard.getRoles().iterator();

			RoleType f = it.next();
			assertEquals("TheProfession", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1922);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 10);
			assertEquals(c.getTime(), f.getDate());
		}

		//KEY
		{
			Iterator<KeyType> it = vcard.getKeys().iterator();

			KeyType f = it.next();
			assertEquals(KeyTypeParameter.X509, f.getContentType());
			assertEquals(514, f.getData().length);

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("mike.angstadt@gmail.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(EmailTypeParameter.PREF));
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		//PHOTO
		{
			Iterator<PhotoType> it = vcard.getPhotos().iterator();

			PhotoType f = it.next();
			assertEquals(ImageTypeParameter.JPEG, f.getContentType());
			assertEquals(2324, f.getData().length);

			assertFalse(it.hasNext());
		}

		//FBURL
		{
			//a 4.0 property in a 2.1 vCard...
			Iterator<FbUrlType> it = vcard.getFbUrls().iterator();

			FbUrlType f = it.next();
			assertEquals("http://website.com/mycal", f.getValue());

			assertFalse(it.hasNext());
		}

		//REV
		{
			RevisionType f = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.AUGUST);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, 18);
			c.set(Calendar.MINUTE, 46);
			c.set(Calendar.SECOND, 31);
			assertEquals(c.getTime(), f.getTimestamp());
		}

		//extended types
		{
			assertEquals(8, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-MS-TEL").get(0);
			assertEquals("X-MS-TEL", f.getTypeName());
			assertEquals("(111) 555-4444", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains("VOICE"));
			assertTrue(types.contains("CALLBACK"));

			f = vcard.getExtendedType("X-MS-OL-DEFAULT-POSTAL-ADDRESS").get(0);
			assertEquals("X-MS-OL-DEFAULT-POSTAL-ADDRESS", f.getTypeName());
			assertEquals("2", f.getValue());

			f = vcard.getExtendedType("X-MS-ANNIVERSARY").get(0);
			assertEquals("X-MS-ANNIVERSARY", f.getTypeName());
			assertEquals("20120801", f.getValue());

			f = vcard.getExtendedType("X-MS-IMADDRESS").get(0);
			assertEquals("X-MS-IMADDRESS", f.getTypeName());
			assertEquals("im@aim.com", f.getValue());

			f = vcard.getExtendedType("X-MS-OL-DESIGN").get(0);
			assertEquals("X-MS-OL-DESIGN", f.getTypeName());
			assertEquals("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telcell\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Mobile</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>", f.getValue());
			assertEquals("utf-8", f.getSubTypes().getCharset());

			f = vcard.getExtendedType("X-MS-MANAGER").get(0);
			assertEquals("X-MS-MANAGER", f.getTypeName());
			assertEquals("TheManagerName", f.getValue());

			f = vcard.getExtendedType("X-MS-ASSISTANT").get(0);
			assertEquals("X-MS-ASSISTANT", f.getTypeName());
			assertEquals("TheAssistantName", f.getValue());

			f = vcard.getExtendedType("X-MS-SPOUSE").get(0);
			assertEquals("X-MS-SPOUSE", f.getTypeName());
			assertEquals("TheSpouse", f.getValue());
		}
	}

	@Test
	public void macAddressBookVCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_MAC_ADDRESS_BOOK.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.MAC_ADDRESS_BOOK);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter,James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("Mr. John Richter,James Doe Sr.", f.getValue());
		}

		//NICKNAME
		{
			NicknameType f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), f.getValues());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("john.doe@ibm.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(3, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));
			assertTrue(types.contains(new EmailTypeParameter("work")));
			assertTrue(types.contains(EmailTypeParameter.PREF));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType f = it.next();
			assertEquals("905-777-1234", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.PREF));

			f = it.next();
			assertEquals("905-666-1234", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));

			f = it.next();
			assertEquals("905-555-1234", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));

			f = it.next();
			assertEquals("905-888-1234", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));
			assertTrue(types.contains(TelephoneTypeParameter.FAX));

			f = it.next();
			assertEquals("905-999-1234", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.FAX));

			f = it.next();
			assertEquals("905-111-1234", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.PAGER));

			f = it.next();
			assertEquals("905-222-1234", f.getValue());
			assertEquals("item1", f.getGroup());
			types = f.getTypes();
			assertEquals(0, types.size());

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals("item2", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Silicon Alley 5,", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());

			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));
			assertTrue(types.contains(AddressTypeParameter.PREF));

			f = it.next();
			assertEquals("item3", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Street4" + newline + "Building 6" + newline + "Floor 8", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("USA", f.getCountry());

			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(AddressTypeParameter.WORK));

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + newline + "Favotire Color: Blue", f.getValue());

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("item4", f.getGroup());
			assertEquals("http://www.ibm.com", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("pref"));

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 6);
			assertEquals(c.getTime(), f.getDate());
		}

		//PHOTO
		{
			Iterator<PhotoType> it = vcard.getPhotos().iterator();

			PhotoType f = it.next();
			assertEquals(null, f.getContentType());
			assertEquals(18242, f.getData().length);

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(9, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-PHONETIC-FIRST-NAME").get(0);
			assertEquals("X-PHONETIC-FIRST-NAME", f.getTypeName());
			assertEquals("Jon", f.getValue());

			f = vcard.getExtendedType("X-PHONETIC-LAST-NAME").get(0);
			assertEquals("X-PHONETIC-LAST-NAME", f.getTypeName());
			assertEquals("Dow", f.getValue());

			f = vcard.getExtendedType("X-ABLABEL").get(0);
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("AssistantPhone", f.getValue());
			assertEquals("item1", f.getGroup());

			f = vcard.getExtendedType("X-ABADR").get(0);
			assertEquals("X-ABADR", f.getTypeName());
			assertEquals("Silicon Alley", f.getValue());
			assertEquals("item2", f.getGroup());

			f = vcard.getExtendedType("X-ABADR").get(1);
			assertEquals("X-ABADR", f.getTypeName());
			assertEquals("Street 4, Building 6,\\nFloor 8\\nNew York\\nUSA", f.getValue());
			assertEquals("item3", f.getGroup());

			f = vcard.getExtendedType("X-ABLABEL").get(1);
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("_$!<HomePage>!$_", f.getValue());
			assertEquals("item4", f.getGroup());

			f = vcard.getExtendedType("X-ABRELATEDNAMES").get(0);
			assertEquals("X-ABRELATEDNAMES", f.getTypeName());
			assertEquals("Jenny", f.getValue());
			assertEquals("item5", f.getGroup());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("pref"));

			f = vcard.getExtendedType("X-ABLABEL").get(2);
			assertEquals("X-ABLABEL", f.getTypeName());
			assertEquals("Spouse", f.getValue());
			assertEquals("item5", f.getGroup());

			f = vcard.getExtendedType("X-ABUID").get(0);
			assertEquals("X-ABUID", f.getTypeName());
			assertEquals("6B29A774-D124-4822-B8D0-2780EC117F60\\:ABPerson", f.getValue());
		}
	}

	@Test
	public void outlook2003VCard() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("outlook-2003.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V2_1, vcard.getVersion());

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("III"), f.getSuffixes());
		}

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("John Doe III", f.getValue());
		}

		//NICKNAME
		{
			NicknameType f = vcard.getNickname();
			assertEquals(Arrays.asList("Joey"), f.getValues());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("Company, The", "TheDepartment"), f.getValues());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("The Job Title", f.getValue());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("This is the note field!!\r\nSecond line\r\n\r\nThird line is empty\r\n", f.getValue());

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();
			TelephoneType f = it.next();

			assertEquals("BusinessPhone", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("HomePhone", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("MobilePhone", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("BusinessFaxPhone", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.FAX));
			assertTrue(types.contains(TelephoneTypeParameter.WORK));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("TheOffice", f.getExtendedAddress());
			assertEquals("123 Main St", f.getStreetAddress());
			assertEquals("Austin", f.getLocality());
			assertEquals("TX", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertEquals("TheOffice\r\n123 Main St\r\nAustin, TX 12345\r\nUnited States of America", f.getLabel());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(AddressTypeParameter.WORK));

			assertFalse(it.hasNext());
		}

		//LABEL
		{
			assertTrue(vcard.getOrphanedLabels().isEmpty());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("http://web-page-address.com", f.getValue());
			Set<String> types = f.getSubTypes().getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains("WORK"));

			assertFalse(it.hasNext());
		}

		//ROLE
		{
			Iterator<RoleType> it = vcard.getRoles().iterator();

			RoleType f = it.next();
			assertEquals("TheProfession", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 21);
			assertEquals(c.getTime(), f.getDate());
		}

		//KEY
		{
			Iterator<KeyType> it = vcard.getKeys().iterator();

			KeyType f = it.next();
			assertEquals(KeyTypeParameter.X509, f.getContentType());
			assertEquals(805, f.getData().length);

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("jdoe@hotmail.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(EmailTypeParameter.PREF));
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		//FBURL
		{
			Iterator<FbUrlType> it = vcard.getFbUrls().iterator();

			//Outlook 2003 apparently doesn't output FBURL correctly:
			//http://help.lockergnome.com/office/BUG-Outlook-2003-exports-FBURL-vCard-incorrectly--ftopict423660.html
			FbUrlType f = it.next();
			assertEquals("????????????????s????????????" + (char) 12, f.getValue());

			assertFalse(it.hasNext());
		}

		//REV
		{
			RevisionType f = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.OCTOBER);
			c.set(Calendar.DAY_OF_MONTH, 12);
			c.set(Calendar.HOUR_OF_DAY, 21);
			c.set(Calendar.MINUTE, 5);
			c.set(Calendar.SECOND, 25);
			assertEquals(c.getTime(), f.getTimestamp());
		}
	}

	@Test
	public void thunderbird() throws Exception {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("thunderbird-MoreFunctionsForAddressBook-extension.vcf"));
		reader.setCompatibilityMode(CompatibilityMode.MAC_ADDRESS_BOOK);
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//N
		{
			StructuredNameType f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//FN
		{
			FormattedNameType f = vcard.getFormattedName();
			assertEquals("John Doe", f.getValue());
		}

		//ORG
		{
			OrganizationType f = vcard.getOrganization();
			assertEquals(Arrays.asList("TheOrganization", "TheDepartment"), f.getValues());
		}

		//NICKNAME
		{
			NicknameType f = vcard.getNickname();
			assertEquals(Arrays.asList("Johnny"), f.getValues());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("222 Broadway", f.getExtendedAddress());
			assertEquals("Suite 100", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("NY", f.getRegion());
			assertEquals("98765", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			Set<AddressTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(AddressTypeParameter.WORK));
			assertTrue(types.contains(AddressTypeParameter.POSTAL));

			f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("123 Main St", f.getExtendedAddress());
			assertEquals("Apt 10", f.getStreetAddress());
			assertEquals("Austin", f.getLocality());
			assertEquals("TX", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("USA", f.getCountry());

			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(AddressTypeParameter.HOME));
			assertTrue(types.contains(AddressTypeParameter.POSTAL));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType f = it.next();
			assertEquals("555-555-1111", f.getValue());
			Set<TelephoneTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.WORK));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("555-555-2222", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.HOME));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("555-555-5555", f.getValue());
			types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.CELL));
			assertTrue(types.contains(TelephoneTypeParameter.VOICE));

			f = it.next();
			assertEquals("555-555-3333", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.FAX));

			f = it.next();
			assertEquals("555-555-4444", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(TelephoneTypeParameter.PAGER));

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType f = it.next();
			assertEquals("doe.john@hotmail.com", f.getValue());
			Set<EmailTypeParameter> types = f.getTypes();
			assertEquals(2, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));
			assertTrue(types.contains(EmailTypeParameter.PREF));

			f = it.next();
			assertEquals("additional-email@company.com", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			f = it.next();
			assertEquals("additional-email1@company.com", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			f = it.next();
			assertEquals("additional-email2@company.com", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			f = it.next();
			assertEquals("additional-email3@company.com", f.getValue());
			types = f.getTypes();
			assertEquals(1, types.size());
			assertTrue(types.contains(EmailTypeParameter.INTERNET));

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType f = it.next();
			assertEquals("http://www.private-webpage.com", f.getValue());
			assertEquals("HOME", f.getType());

			f = it.next();
			assertEquals("http://www.work-webpage.com", f.getValue());
			assertEquals("WORK", f.getType());

			assertFalse(it.hasNext());
		}

		//TITLE
		{
			Iterator<TitleType> it = vcard.getTitles().iterator();

			TitleType f = it.next();
			assertEquals("TheTitle", f.getValue());

			assertFalse(it.hasNext());
		}

		//CATEGORIES
		{
			//commas are incorrectly escaped, so there is only 1 item
			CategoriesType f = vcard.getCategories();
			assertEquals(Arrays.asList("category1, category2, category3"), f.getValues());
		}

		//BDAY
		{
			BirthdayType f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1970);
			c.set(Calendar.MONTH, Calendar.SEPTEMBER);
			c.set(Calendar.DAY_OF_MONTH, 21);
			assertEquals(c.getTime(), f.getDate());
		}

		//NOTE
		{
			Iterator<NoteType> it = vcard.getNotes().iterator();

			NoteType f = it.next();
			assertEquals("This is the notes field." + newline + "Second Line" + newline + newline + "Fourth Line" + newline + "You can put anything in the \"note\" field; even curse words.", f.getValue());

			assertFalse(it.hasNext());
		}

		//PHOTO
		{
			Iterator<PhotoType> it = vcard.getPhotos().iterator();

			PhotoType f = it.next();
			assertEquals(ImageTypeParameter.JPEG, f.getContentType());
			assertEquals(8940, f.getData().length);

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(2, countExtTypes(vcard));

			RawType f = vcard.getExtendedType("X-SPOUSE").get(0);
			assertEquals("X-SPOUSE", f.getTypeName());
			assertEquals("TheSpouse", f.getValue());

			f = vcard.getExtendedType("X-ANNIVERSARY").get(0);
			assertEquals("X-ANNIVERSARY", f.getTypeName());
			assertEquals("1990-04-30", f.getValue());
		}
	}

	/**
	 * Counts the number of extended types in a vCard.
	 * @param vcard the vCard
	 * @return the number of extended types
	 */
	private int countExtTypes(VCard vcard) {
		int count = 0;
		for (List<VCardType> list : vcard.getExtendedTypes().values()) {
			count += list.size();
		}
		return count;
	}
}
