package ezvcard.io.text;

import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.KeyType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Agent;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Logo;
import ezvcard.property.Note;
import ezvcard.property.Photo;
import ezvcard.property.SkipMeProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;

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
@SuppressWarnings("resource")
public class VCardWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void generalStructure() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

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
	public void nestedVCard() throws Throwable {
		VCard vcard = new VCard();

		vcard.setFormattedName("Michael Angstadt");

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName("Agent 007");
		agentVcard.addNote("Make sure that it properly folds long lines which are part of a nested AGENT type in a version 2.1 vCard.");
		Agent agent = new Agent(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName("Agent 009");
		secondAgentVCard.addNote("Make sure that it ALSO properly folds THIS long line because it's part of an AGENT that's inside of an AGENT.");
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

		vcard.setFormattedName("Michael Angstadt");

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName("Agent 007");
		Note note = agentVcard.addNote("Bonne soir�e, 007.");
		note.setLanguage("fr");
		Agent agent = new Agent(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName("Agent 009");
		note = secondAgentVCard.addNote("Good evening, 009.");
		note.setLanguage("en");
		Agent secondAgent = new Agent(secondAgentVCard);
		agentVcard.setAgent(secondAgent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.getRawWriter().getFoldedLineWriter().setLineLength(null);
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
	public void skipMeException() throws Throwable {
		VCard vcard = new VCard();
		vcard.addProperty(new SkipMeProperty());
		vcard.addExtendedProperty("X-FOO", "value");

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(false);
		vcw.registerScribe(new SkipMeScribe());
		vcw.write(vcard);

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"X-FOO:value\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void setVersionStrict_nested() throws Throwable {
		VCard vcard = new VCard();

		VCard agentVCard = new VCard();
		agentVCard.setGender(Gender.male()); //only supported by 4.0
		Agent agent = new Agent(agentVCard);
		vcard.setAgent(agent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.setVersionStrict(false);
		vcw.write(vcard);

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"AGENT:\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"GENDER:M\r\n" +
			"END:VCARD\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void setVersionStrict_embedded() throws Throwable {
		VCard vcard = new VCard();

		VCard agentVCard = new VCard();
		agentVCard.setGender(Gender.male()); //only supported by 4.0
		Agent agent = new Agent(agentVCard);
		vcard.setAgent(agent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(false);
		vcw.setVersionStrict(false);
		vcw.write(vcard);

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"AGENT:BEGIN:VCARD\\nVERSION:3.0\\nGENDER:M\\nEND:VCARD\\n\r\n" +
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
			assertEquals(Charset.forName("UTF-8"), writer.getRawWriter().getFoldedLineWriter().getEncoding());
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
			VCardWriter writer = new VCardWriter(file, VCardVersion.V3_0);
			assertEquals(Charset.defaultCharset(), writer.getRawWriter().getFoldedLineWriter().getEncoding());
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
	public void date_time_properties_should_not_have_a_VALUE_parameter() throws Throwable {
		class DateTestScribe<T extends VCardProperty> extends VCardPropertyScribe<T> {
			private final VCardDataType dataType;

			public DateTestScribe(Class<T> clazz, String name, VCardDataType dataType) {
				super(clazz, name);
				this.dataType = dataType;
			}

			@Override
			protected VCardDataType _defaultDataType(VCardVersion version) {
				return VCardDataType.DATE_AND_OR_TIME;
			}

			@Override
			protected VCardDataType _dataType(T property, VCardVersion version) {
				return dataType;
			}

			@Override
			protected String _writeText(T property, VCardVersion version) {
				return "value";
			}

			@Override
			protected T _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
				return null;
			}
		}

		class DateProperty extends VCardProperty {
			//empty
		}
		class DateTimeProperty extends VCardProperty {
			//empty
		}
		class TimeProperty extends VCardProperty {
			//empty
		}
		class DateAndOrTimeProperty extends VCardProperty {
			//empty
		}

		VCard vcard = new VCard();
		vcard.addProperty(new DateProperty());
		vcard.addProperty(new DateTimeProperty());
		vcard.addProperty(new TimeProperty());
		vcard.addProperty(new DateAndOrTimeProperty());

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.registerScribe(new DateTestScribe<DateProperty>(DateProperty.class, "DATE", VCardDataType.DATE));
		vcw.registerScribe(new DateTestScribe<DateTimeProperty>(DateTimeProperty.class, "DATETIME", VCardDataType.DATE_TIME));
		vcw.registerScribe(new DateTestScribe<TimeProperty>(TimeProperty.class, "TIME", VCardDataType.TIME));
		vcw.registerScribe(new DateTestScribe<DateAndOrTimeProperty>(DateAndOrTimeProperty.class, "DATEANDORTIME", VCardDataType.DATE_AND_OR_TIME));
		vcw.setAddProdId(false);
		vcw.write(vcard);

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:4.0\r\n" +
			"DATE:value\r\n" +
			"DATETIME:value\r\n" +
			"TIME:value\r\n" +
			"DATEANDORTIME:value\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void outlookCompatibility() throws Throwable {
		VCard vcard = new VCard();
		byte data[] = "foobar".getBytes();
		vcard.addKey(new Key(data, KeyType.X509));
		vcard.addPhoto(new Photo(data, ImageType.JPEG));
		vcard.addLogo(new Logo("http://www.company.com/logo.png", ImageType.PNG));
		vcard.addNote("note");

		{
			StringWriter sw = new StringWriter();
			VCardWriter writer = new VCardWriter(sw, VCardVersion.V2_1);
			writer.setAddProdId(false);
			writer.write(vcard);
			writer.setOutlookCompatibility(true);
			writer.write(vcard);

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"KEY;ENCODING=base64;X509:Zm9vYmFy\r\n" +
				"PHOTO;ENCODING=base64;JPEG:Zm9vYmFy\r\n" +
				"LOGO;PNG;VALUE=url:http://www.company.com/logo.png\r\n" +
				"NOTE:note\r\n" +
			"END:VCARD\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"KEY;ENCODING=base64;X509:Zm9vYmFy\r\n" +
				"\r\n" +
				"PHOTO;ENCODING=base64;JPEG:Zm9vYmFy\r\n" +
				"\r\n" +
				"LOGO;PNG;VALUE=url:http://www.company.com/logo.png\r\n" +
				"NOTE:note\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			assertEquals(actual, expected);
		}

		{
			StringWriter sw = new StringWriter();
			VCardWriter writer = new VCardWriter(sw, VCardVersion.V3_0);
			writer.setAddProdId(false);
			writer.write(vcard);
			writer.setOutlookCompatibility(true);
			writer.write(vcard);

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"BEGIN:VCARD\r\n" +
				"VERSION:3.0\r\n" +
				"KEY;ENCODING=b;TYPE=x509:Zm9vYmFy\r\n" +
				"PHOTO;ENCODING=b;TYPE=jpeg:Zm9vYmFy\r\n" +
				"LOGO;TYPE=png;VALUE=uri:http://www.company.com/logo.png\r\n" +
				"NOTE:note\r\n" +
			"END:VCARD\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:3.0\r\n" +
				"KEY;ENCODING=b;TYPE=x509:Zm9vYmFy\r\n" +
				"\r\n" +
				"PHOTO;ENCODING=b;TYPE=jpeg:Zm9vYmFy\r\n" +
				"\r\n" +
				"LOGO;TYPE=png;VALUE=uri:http://www.company.com/logo.png\r\n" +
				"NOTE:note\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			assertEquals(actual, expected);
		}

		{
			StringWriter sw = new StringWriter();
			VCardWriter writer = new VCardWriter(sw, VCardVersion.V4_0);
			writer.setAddProdId(false);
			writer.write(vcard);
			writer.setOutlookCompatibility(true);
			writer.write(vcard);

			String actual = sw.toString();

			//@formatter:off
			String expected =
			"BEGIN:VCARD\r\n" +
				"VERSION:4.0\r\n" +
				"KEY:data:application/x509;base64,Zm9vYmFy\r\n" +
				"PHOTO:data:image/jpeg;base64,Zm9vYmFy\r\n" +
				"LOGO;MEDIATYPE=image/png:http://www.company.com/logo.png\r\n" +
				"NOTE:note\r\n" +
			"END:VCARD\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:4.0\r\n" +
				"KEY:data:application/x509;base64,Zm9vYmFy\r\n" +
				"PHOTO:data:image/jpeg;base64,Zm9vYmFy\r\n" +
				"LOGO;MEDIATYPE=image/png:http://www.company.com/logo.png\r\n" +
				"NOTE:note\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			assertEquals(actual, expected);
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

		Birthday bday = new Birthday(PartialDate.builder().month(2).date(3).build());
		vcard.setBirthday(bday);

		Anniversary anniversary = new Anniversary(PartialDate.builder().year(2009).month(8).date(8).hour(14).minute(30).offset(new UtcOffset(false, -5, 0)).build());
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

		vcard.setTimezone(new Timezone(new UtcOffset(false, -5, 0)));

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
		VCardWriter writer = new VCardWriter(sw, VCardVersion.V3_0);
		writer.getRawWriter().getFoldedLineWriter().setLineLength(null);
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
