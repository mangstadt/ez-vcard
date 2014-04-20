package ezvcard.io.text;

import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.LuckyNumType;
import ezvcard.io.LuckyNumType.LuckyNumScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Agent;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Label;
import ezvcard.property.Note;
import ezvcard.property.ProductId;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;

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
@SuppressWarnings("resource")
public class VCardWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void generalStructure() throws Throwable {
		VCard vcard = new VCard();
		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void setAddProdId() throws Throwable {
		VCard vcard = new VCard();
		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		//with X-PRODID (2.1)
		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nX-PRODID:"));

		//with PRODID (3.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//with PRODID (4.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//with X-PRODID (2.1)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nX-PRODID:"));

		//with PRODID (3.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//with PRODID (4.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//without X-PRODID (2.1)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nX-PRODID:"));

		//without PRODID (3.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nPRODID:"));

		//without PRODID (4.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nPRODID:"));
	}

	@Test
	public void setAddProdId_overwrites_existing_prodId() throws Throwable {
		VCard vcard = new VCard();

		FormattedName fn = new FormattedName("John Doe");
		vcard.setFormattedName(fn);

		ProductId prodId = new ProductId("Acme Co.");
		vcard.setProdId(prodId);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:Acme Co."));

		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nPRODID:Acme Co."));
	}

	@Test
	public void nestedVCard() throws Throwable {
		VCard vcard = new VCard();

		FormattedName fn = new FormattedName("Michael Angstadt");
		vcard.setFormattedName(fn);

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName(new FormattedName("Agent 007"));
		agentVcard.addNote(new Note("Make sure that it properly folds long lines which are part of a nested AGENT type in a version 2.1 vCard."));
		Agent agent = new Agent(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName(new FormattedName("Agent 009"));
		secondAgentVCard.addNote(new Note("Make sure that it ALSO properly folds THIS long line because it's part of an AGENT that's inside of an AGENT."));
		Agent secondAgent = new Agent(secondAgentVCard);
		agentVcard.setAgent(secondAgent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"FN:Michael Angstadt\r\n" +
			"AGENT:\r\n" + //nested types should not have X-GENERATOR
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"FN:Agent 007\r\n" +
				"NOTE:Make sure that it properly folds long lines which are part of a nested \r\n" +
				" AGENT type in a version 2.1 vCard.\r\n" +
				"AGENT:\r\n" +
				"BEGIN:VCARD\r\n" +
					"VERSION:2.1\r\n" +
					"FN:Agent 009\r\n" +
					"NOTE:Make sure that it ALSO properly folds THIS long line because it's part \r\n" +
					" of an AGENT that's inside of an AGENT.\r\n" +
				"END:VCARD\r\n" +
			"END:VCARD\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void embeddedVCard() throws Throwable {
		VCard vcard = new VCard();

		FormattedName fn = new FormattedName("Michael Angstadt");
		vcard.setFormattedName(fn);

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName(new FormattedName("Agent 007"));
		Note note = new Note("Bonne soir�e, 007.");
		note.setLanguage("fr");
		agentVcard.addNote(note);
		Agent agent = new Agent(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName(new FormattedName("Agent 009"));
		note = new Note("Good evening, 009.");
		note.setLanguage("en");
		secondAgentVCard.addNote(note);
		Agent secondAgent = new Agent(secondAgentVCard);
		agentVcard.setAgent(secondAgent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0, null, "\r\n");
		vcw.setAddProdId(false);
		vcw.write(vcard);
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"FN:Michael Angstadt\r\n" +
			"AGENT:" + //nested types should not have PRODID
			"BEGIN:VCARD\\n" +
				"VERSION:3.0\\n" +
				"FN:Agent 007\\n" +
				"NOTE\\;LANGUAGE=fr:Bonne soir�e\\\\\\, 007.\\n" +
				"AGENT:" +
				"BEGIN:VCARD\\\\n" +
					"VERSION:3.0\\\\n" +
					"FN:Agent 009\\\\n" +
					"NOTE\\\\\\;LANGUAGE=en:Good evening\\\\\\\\\\\\\\, 009.\\\\n" +
				"END:VCARD\\\\n\\n" +
			"END:VCARD\\n\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void labels() throws Throwable {
		VCard vcard = new VCard();

		//address with label
		Address adr = new Address();
		adr.setStreetAddress("123 Main St.");
		adr.setLocality("Austin");
		adr.setRegion("TX");
		adr.setPostalCode("12345");
		adr.setLabel("123 Main St.\r\nAustin, TX 12345");
		adr.addType(AddressType.HOME);
		vcard.addAddress(adr);

		//address without label
		adr = new Address();
		adr.setStreetAddress("222 Broadway");
		adr.setLocality("New York");
		adr.setRegion("NY");
		adr.setPostalCode("99999");
		adr.addType(AddressType.WORK);
		vcard.addAddress(adr);

		//orphaned label
		Label label = new Label("22 Spruce Ln.\r\nChicago, IL 11111");
		label.addType(AddressType.PARCEL);
		vcard.addOrphanedLabel(label);

		//3.0
		//LABEL types should be used
		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0, null, "\r\n");
		vcw.setAddProdId(false);
		vcw.write(vcard);
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"ADR;TYPE=home:;;123 Main St.;Austin;TX;12345;\r\n" +
		"LABEL;TYPE=home:123 Main St.\\nAustin\\, TX 12345\r\n" +
		"ADR;TYPE=work:;;222 Broadway;New York;NY;99999;\r\n" +
		"LABEL;TYPE=parcel:22 Spruce Ln.\\nChicago\\, IL 11111\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);

		//4.0
		//LABEL parameters should be used
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0, null, "\r\n");
		vcw.setAddProdId(false);
		vcw.write(vcard);
		actual = sw.toString();

		//@formatter:off
		expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"ADR;LABEL=\"123 Main St.\\nAustin, TX 12345\";TYPE=home:;;123 Main St.;Austin;TX;12345;\r\n" +
		"ADR;TYPE=work:;;222 Broadway;New York;NY;99999;\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/*
	 * If the type's marshal method throws a SkipMeException, then a warning
	 * should be added to the warnings list and the type object should NOT be
	 * marshalled.
	 */
	@Test
	public void skipMeException() throws Throwable {
		VCard vcard = new VCard();

		//add N property so a warning isn't generated (2.1 requires N to be present)
		StructuredName n = new StructuredName();
		vcard.setStructuredName(n);

		LuckyNumType num = new LuckyNumType(24);
		vcard.addProperty(num);

		//should be skipped
		num = new LuckyNumType(13);
		vcard.addProperty(num);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.registerScribe(new LuckyNumScribe());
		vcw.write(vcard);

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"N:;;;;\r\n" +
		"X-LUCKY-NUM:24\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void setVersionStrict() throws Throwable {
		VCard vcard = new VCard();
		vcard.setMailer("mailer"); //only supported by 2.1 and 3.0

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		vcw.setVersionStrict(false);
		vcw.write(vcard);
		vcw.setVersionStrict(true);
		vcw.write(vcard);

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"MAILER:mailer\r\n" +
		"END:VCARD\r\n" + 
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void utf8() throws Throwable {
		VCard vcard = new VCard();
		vcard.addNote("\u019dote");

		File file = tempFolder.newFile();

		//should be written as UTF-8
		{
			VCardWriter writer = new VCardWriter(file, false, VCardVersion.V4_0);
			writer.setAddProdId(false);
			writer.write(vcard);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCARD\r\n" +
				"VERSION:4.0\r\n" +
				"NOTE:\u019dote\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			String actual = IOUtils.getFileContents(file, "UTF-8");
			assertEquals(expected, actual);
		}

		//should be written using default encoding
		if (!Charset.defaultCharset().name().equalsIgnoreCase("UTF-8")) { //don't test if the local machine's default encoding is UTF-8
			VCardWriter writer = new VCardWriter(file);
			writer.setAddProdId(false);
			writer.write(vcard);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCARD\r\n" +
				"VERSION:3.0\r\n" +
				"NOTE:?ote\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			String actual = IOUtils.getFileContents(file, "UTF-8");
			assertEquals(expected, actual);
		}
	}

	@Test
	public void rfc6350_example() throws Throwable {
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
		adr.setExtendedAddress("Suite D2-630");
		adr.setStreetAddress("2875 Laurier");
		adr.setLocality("Quebec");
		adr.setRegion("QC");
		adr.setPostalCode("G1V 2M2");
		adr.setCountry("Canada");
		adr.addType(AddressType.WORK);
		vcard.addAddress(adr);

		TelUri telUri = new TelUri.Builder("+1-418-656-9254").extension("102").build();
		Telephone tel = new Telephone(telUri);
		tel.setPref(1);
		tel.addType(TelephoneType.WORK);
		tel.addType(TelephoneType.VOICE);
		vcard.addTelephoneNumber(tel);

		tel = new Telephone(new TelUri.Builder("+1-418-262-6501").build());
		tel.addType(TelephoneType.WORK);
		tel.addType(TelephoneType.VOICE);
		tel.addType(TelephoneType.CELL);
		tel.addType(TelephoneType.VIDEO);
		tel.addType(TelephoneType.TEXT);
		vcard.addTelephoneNumber(tel);

		vcard.addEmail("simon.perreault@viagenie.ca", EmailType.WORK);

		Geo geo = new Geo(46.772673, -71.282945);
		geo.setType("work");
		vcard.setGeo(geo);

		Key key = new Key("http://www.viagenie.ca/simon.perreault/simon.asc", null);
		key.setType("work");
		vcard.addKey(key);

		vcard.setTimezone(new Timezone(-5, 0));

		vcard.addUrl("http://nomis80.org").setType("home");

		assertValidate(vcard).versions(VCardVersion.V4_0).run();

		StringWriter sw = new StringWriter();
		VCardWriter writer = new VCardWriter(sw, VCardVersion.V4_0);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"FN:Simon Perreault\r\n" +
		"N:Perreault;Simon;;;ing. jr,M.Sc.\r\n" +
		"BDAY:--0203\r\n" +
		"ANNIVERSARY:20090808T1430-0500\r\n" +
		"GENDER:M\r\n" +
		"LANG;PREF=1:fr\r\n" +
		"LANG;PREF=2:en\r\n" +
		"ORG;TYPE=work:Viagenie\r\n" +
		"ADR;TYPE=work:;Suite D2-630;2875 Laurier;Quebec;QC;G1V 2M2;Canada\r\n" +
		"TEL;PREF=1;TYPE=work,voice;VALUE=uri:tel:+1-418-656-9254;ext=102\r\n" +
		"TEL;TYPE=work,voice,cell,video,text;VALUE=uri:tel:+1-418-262-6501\r\n" +
		"EMAIL;TYPE=work:simon.perreault@viagenie.ca\r\n" +
		"GEO;TYPE=work:geo:46.772673,-71.282945\r\n" +
		"KEY;TYPE=work:http://www.viagenie.ca/simon.perreault/simon.asc\r\n" +
		"TZ;VALUE=utc-offset:-0500\r\n" +
		"URL;TYPE=home:http://nomis80.org\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		String actual = sw.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void rfc2426_example() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardWriter writer = new VCardWriter(sw, VCardVersion.V3_0, null, "\r\n");
		writer.setAddProdId(false);

		{
			VCard vcard = new VCard();

			vcard.setFormattedName("Frank Dawson");

			vcard.setOrganization("Lotus Development Corporation");

			Address adr = new Address();
			adr.setStreetAddress("6544 Battleford Drive");
			adr.setLocality("Raleigh");
			adr.setRegion("NC");
			adr.setPostalCode("27613-3502");
			adr.setCountry("U.S.A.");
			adr.addType(AddressType.WORK);
			adr.addType(AddressType.POSTAL);
			adr.addType(AddressType.PARCEL);
			vcard.addAddress(adr);

			vcard.addTelephoneNumber("+1-919-676-9515", TelephoneType.VOICE, TelephoneType.MSG, TelephoneType.WORK);
			vcard.addTelephoneNumber("+1-919-676-9564", TelephoneType.FAX, TelephoneType.WORK);

			vcard.addEmail("Frank_Dawson@Lotus.com", EmailType.INTERNET, EmailType.PREF);
			vcard.addEmail("fdawson@earthlink.net", EmailType.INTERNET);

			vcard.addUrl("http://home.earthlink.net/�fdawson");

			assertValidate(vcard).versions(VCardVersion.V3_0).prop(null, 0).run();

			writer.write(vcard);
		}

		{
			VCard vcard = new VCard();

			vcard.setFormattedName("Tim Howes");

			vcard.setOrganization("Netscape Communications Corp.");

			Address adr = new Address();
			adr.setStreetAddress("501 E. Middlefield Rd.");
			adr.setLocality("Mountain View");
			adr.setRegion("CA");
			adr.setPostalCode("94043");
			adr.setCountry("U.S.A.");
			adr.addType(AddressType.WORK);
			vcard.addAddress(adr);

			vcard.addTelephoneNumber("+1-415-937-3419", TelephoneType.VOICE, TelephoneType.MSG, TelephoneType.WORK);
			vcard.addTelephoneNumber("+1-415-528-4164", TelephoneType.FAX, TelephoneType.WORK);

			vcard.addEmail("howes@netscape.com", EmailType.INTERNET);

			assertValidate(vcard).versions(VCardVersion.V3_0).prop(null, 0).run();

			writer.write(vcard);
		}

		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"FN:Frank Dawson\r\n" +
		"ORG:Lotus Development Corporation\r\n" +
		"ADR;TYPE=work,postal,parcel:;;6544 Battleford Drive;Raleigh;NC;27613-3502;U.S.A.\r\n" +
		"TEL;TYPE=voice,msg,work:+1-919-676-9515\r\n" +
		"TEL;TYPE=fax,work:+1-919-676-9564\r\n" +
		"EMAIL;TYPE=internet,pref:Frank_Dawson@Lotus.com\r\n" +
		"EMAIL;TYPE=internet:fdawson@earthlink.net\r\n" +
		"URL:http://home.earthlink.net/�fdawson\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"FN:Tim Howes\r\n" +
		"ORG:Netscape Communications Corp.\r\n" +
		"ADR;TYPE=work:;;501 E. Middlefield Rd.;Mountain View;CA;94043;U.S.A.\r\n" +
		"TEL;TYPE=voice,msg,work:+1-415-937-3419\r\n" +
		"TEL;TYPE=fax,work:+1-415-528-4164\r\n" +
		"EMAIL;TYPE=internet:howes@netscape.com\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		String actual = sw.toString();

		assertEquals(expected, actual);
	}
}
