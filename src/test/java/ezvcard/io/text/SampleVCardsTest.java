package ezvcard.io.text;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.KeyType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.Categories;
import ezvcard.property.Classification;
import ezvcard.property.FormattedName;
import ezvcard.property.FreeBusyUrl;
import ezvcard.property.Gender;
import ezvcard.property.Key;
import ezvcard.property.Label;
import ezvcard.property.Language;
import ezvcard.property.Mailer;
import ezvcard.property.Nickname;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.ProductId;
import ezvcard.property.Profile;
import ezvcard.property.Revision;
import ezvcard.property.Role;
import ezvcard.property.SortString;
import ezvcard.property.Source;
import ezvcard.property.SourceDisplayText;
import ezvcard.property.Title;
import ezvcard.property.Uid;
import ezvcard.property.Url;
import ezvcard.property.asserter.VCardAsserter;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
public class SampleVCardsTest {
	@Test
	public void androidVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_ANDROID.vcf");

		{
			asserter.next(V2_1);

			//@formatter:off
			asserter.email()
				.types(EmailType.PREF)
				.value("john.doe@company.com")
			.noMore();

			asserter.listProperty(Categories.class)
				.values("My Contacts")
			.noMore();
			
			VCard vcard = asserter.getVCard();
			asserter.validate()
				.prop(null, 0) //N property required
				.prop(vcard.getCategories(), 2) //not supported in 2.1
			.run();
			//@formatter:on
		}

		{
			asserter.next(V2_1);

			//@formatter:off
			asserter.email()
				.types(EmailType.PREF)
				.value("jane.doe@company.com")
			.noMore();

			asserter.listProperty(Categories.class)
				.values("My Contacts")
			.noMore();
			
			VCard vcard = asserter.getVCard();
			asserter.validate()
				.prop(null, 0) //N property required
				.prop(vcard.getCategories(), 2) //not supported in 2.1
			.run();
			//@formatter:on
		}

		{
			asserter.next(V2_1);

			//@formatter:off
			asserter.structuredName()
				.param("ENCODING", "QUOTED-PRINTABLE")
				.param("CHARSET", "UTF-8")
				.family("\u00d1 \u00d1 \u00d1 \u00d1 ")
			.noMore();

			asserter.simpleProperty(FormattedName.class)
				.param("ENCODING", "QUOTED-PRINTABLE")
				.param("CHARSET", "UTF-8")
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ")
			.noMore();
			
			asserter.telephone()
				.types(TelephoneType.CELL, TelephoneType.PREF)
				.text("123456789")
			.noMore();

			asserter.listProperty(Categories.class)
				.values("My Contacts")
			.noMore();
			
			VCard vcard = asserter.getVCard();
			asserter.validate()
				.prop(vcard.getCategories(), 2) //not supported in 2.1
			.run();
			//@formatter:on
		}

		{
			asserter.next(V2_1);

			//@formatter:off
			asserter.structuredName()
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.family("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1")
			.noMore();

			asserter.simpleProperty(FormattedName.class)
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1")
			.noMore();
			
			asserter.telephone()
				.types(TelephoneType.CELL, TelephoneType.PREF)
				.text("123456")
			.next()
				.types(TelephoneType.HOME)
				.text("234567")
			.next()
				.types(TelephoneType.CELL)
				.text("3456789")
			.next()
				.types(TelephoneType.HOME)
				.text("45678901")
			.noMore();

			asserter.listProperty(Categories.class)
				.values("My Contacts")
			.noMore();

			asserter.simpleProperty(Note.class)
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ")
			.next()
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ")
			.noMore();
			
			VCard vcard = asserter.getVCard();
			asserter.validate()
				.prop(vcard.getCategories(), 2) //not supported in 2.1
			.run();
			//@formatter:on
		}

		{
			asserter.next(V2_1);

			//@formatter:off
			asserter.structuredName()
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.family("\u00d1 \u00d1 ")
				.given("\u00d1 \u00d1 \u00d1 ")
			.noMore();

			asserter.simpleProperty(FormattedName.class)
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.value("\u00d1 \u00d1 \u00d1 \u00d1 ")
			.noMore();
			
			asserter.telephone()
				.types(TelephoneType.CELL, TelephoneType.PREF)
				.text("123456")
			.next()
				.types(TelephoneType.WORK)
				.text("123456")
			.next()
				.types(TelephoneType.WORK, TelephoneType.FAX)
				.text("123456")
			.noMore();

			asserter.email()
				.types(EmailType.PREF, EmailType.WORK)
				.value("bob@company.com")
			.next()
				.types(EmailType.PREF)
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.value("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.noMore();
			
			asserter.listProperty(Organization.class)
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.next()
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.noMore();

			asserter.simpleProperty(Url.class)
				.value("www.company.com")
			.next()
				.value("http://www.company.com")
			.noMore();
			
			asserter.binaryProperty(Photo.class)
				.param("ENCODING", "BASE64")
				.param("TYPE", "JPEG")
				.contentType(ImageType.JPEG)
				.dataLength(876)
			.noMore();
			
			VCard vcard = asserter.getVCard();
			asserter.validate()
				.prop(vcard.getEmails().get(0), 9) //"TYPE=WORK" not valid in vCard 2.1
			.run();
			//@formatter:on
		}

		{
			asserter.next(V2_1);

			//@formatter:off
			asserter.structuredName()
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.family("\u00d1\u00d1\u00d1\u00d1")
			.noMore();

			asserter.simpleProperty(FormattedName.class)
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.value("\u00d1\u00d1\u00d1\u00d1")
			.noMore();
			
			asserter.telephone()
				.types(TelephoneType.CELL, TelephoneType.PREF)
				.text("55556666")
			.noMore();

			asserter.email()
				.types(EmailType.PREF)
				.value("henry@company.com")
			.noMore();
			
			asserter.listProperty(Organization.class)
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.next()
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1" + (char) 65533)
			.next()
				.param("CHARSET", "UTF-8")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.noMore();

			asserter.listProperty(Categories.class)
				.values("My Contacts")
			.noMore();
			
			VCard vcard = asserter.getVCard();
			asserter.validate()
				.prop(vcard.getCategories(), 2) //not supported in 2.1
			.run();
			//@formatter:on
		}

		asserter.done();
	}

	@Test
	public void blackBerryVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_BLACK_BERRY.vcf");
		asserter.next(V2_1);

		//@formatter:off
		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.structuredName()
			.family("Doe")
			.given("john")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.CELL)
			.text("+96123456789")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("Acme Solutions")
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("")
		.noMore();
		
		asserter.binaryProperty(Photo.class)
			.param("ENCODING", "BASE64")
			.dataLength(1674)
		.noMore();
		//@formatter:on

		asserter.validate().run();
		asserter.done();
	}

	@Test
	public void evolutionVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_EVOLUTION.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.simpleProperty(Url.class)
			.value("http://www.ibm.com")
			.param("X-COUCHDB-UUID", "0abc9b8d-0845-47d0-9a91-3db5bb74620d")
		.noMore();

		asserter.telephone()
			.types(TelephoneType.CELL)
			.text("905-666-1234")
			.param("X-COUCHDB-UUID", "c2fa1caa-2926-4087-8971-609cfc7354ce")
		.next()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("905-555-1234")
			.param("X-COUCHDB-UUID", "fbfb2722-4fd8-4dbf-9abd-eeb24072fd8e")
		.noMore();

		asserter.simpleProperty(Uid.class)
			.value("477343c8e6bf375a9bac1f96a5000837")
		.noMore();

		asserter.structuredName()
			.family("Doe")
			.given("John")
			.additional("Richter, James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		asserter.simpleProperty(FormattedName.class)
			.value("Mr. John Richter, James Doe Sr.")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Johny")
		.noMore();

		asserter.listProperty(Organization.class)
			.values("IBM", "Accounting", "Dungeon")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("Money Counter")
		.noMore();

		asserter.listProperty(Categories.class)
			.values("VIP")
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.")
		.noMore();

		asserter.email()
			.types(EmailType.WORK)
			.value("john.doe@ibm.com")
			.param("X-COUCHDB-UUID", "83a75a5d-2777-45aa-bab5-76a4bd972490")
		.noMore();

		asserter.address()
			.poBox("ASB-123")
			.streetAddress("15 Crescent moon drive")
			.locality("Albaney")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.types(AddressType.HOME)
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1980-03-22")
		.noMore();

		asserter.simpleProperty(Revision.class)
			.value(utc("2012-03-05 13:32:54"))
		.noMore();
		Revision rev = asserter.getVCard().getRevision();
		assertEquals(utc(2012, Calendar.MARCH, 5, 13, 32, 54), rev.getCalendar());
		
		asserter.rawProperty("X-COUCHDB-APPLICATION-ANNOTATIONS")
			.value("{\"Evolution\":{\"revision\":\"2012-03-05T13:32:54Z\"}}")
		.noMore();
		
		asserter.rawProperty("X-AIM")
			.value("johnny5@aol.com")
			.param("TYPE", "HOME")
			.param("X-COUCHDB-UUID", "cb9e11fc-bb97-4222-9cd8-99820c1de454")
		.noMore();
		
		asserter.rawProperty("X-EVOLUTION-FILE-AS")
			.value("Doe\\, John")
		.noMore();
		
		asserter.rawProperty("X-EVOLUTION-SPOUSE")
			.value("Maria")
		.noMore();
		
		asserter.rawProperty("X-EVOLUTION-MANAGER")
			.value("Big Blue")
		.noMore();
		
		asserter.rawProperty("X-EVOLUTION-ASSISTANT")
			.value("Little Red")
		.noMore();
		
		asserter.rawProperty("X-EVOLUTION-ANNIVERSARY")
			.value("1980-03-22")
		.noMore();
		
		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getEmails().get(0), 9) //"TYPE=WORK" not valid in vCard 3.0
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void fullcontact() throws Throwable {
		VCardAsserter asserter = read("fullcontact.vcf");
		asserter.next(V4_0);

		//@formatter:off
		asserter.structuredName()
			.family("LastName")
			.given("FirstName")
			.additional("MiddleName")
			.prefixes("Prefix")
			.suffixes("Suffix")
		.noMore();
		
		asserter.simpleProperty(FormattedName.class)
			.value("Prefix FirstName MiddleName LastName Suffix")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("555-555-1111")
		.next()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("555-555-1112")
		.next()
			.types(TelephoneType.CELL, TelephoneType.VOICE)
			.text("555-555-1113")
		.next()
			.types(TelephoneType.CELL, TelephoneType.VOICE)
			.text("555-555-1114")
		.next()
			.types(TelephoneType.VOICE)
			.text("555-555-1115")
		.next()
			.types(TelephoneType.HOME, TelephoneType.FAX)
			.text("555-555-1116")
		.next()
			.types(TelephoneType.WORK, TelephoneType.FAX)
			.text("555-555-1117")
		.next()
			.types(TelephoneType.VOICE)
			.text("555-555-1118")
		.next()
			.types(TelephoneType.VOICE)
			.text("555-555-1119")
		.noMore();

		asserter.email()
			.types(EmailType.HOME)
			.value("home@example.com")
		.next()
			.types(EmailType.WORK)
			.value("work@example.com")
		.next()
			.types(EmailType.get("SCHOOL"))
			.value("school@example.com")
		.next()
			.types(EmailType.get("OTHER"))
			.value("other@example.com")
		.next()
			.types(EmailType.get("CUSTOMTYPE"))
			.value("custom@example.com")
		.noMore();
		
		asserter.binaryProperty(Photo.class)
			.url("https://d3m0kzytmr41b1.cloudfront.net/c335e945d1b60edd9d75eb4837c432f637e95c8a")
		.next()
			.url("https://d3m0kzytmr41b1.cloudfront.net/c335e945d1b60edd9d75eb4837c432f637e95c8a")
		.next()
			.url("https://d2ojpxxtu63wzl.cloudfront.net/static/aa915d1f29f19baf560e5491decdd30a_67c95da9133249fde8b0da7ceebc298bf680117e6f52054f7f5f7a95e8377238")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("Organization1", "Department1")
		.next()
			.values("Organization2", "Department2")
		.noMore();
		
		asserter.simpleProperty(Title.class)
			.value("Title1")
		.next()
			.value("Title2")
		.noMore();
		
		asserter.dateProperty(Birthday.class)
			.param("ALTID", "1")
			.date("2016-08-01")
		.next()
			.param("ALTID", "1")
			.text("2016-08-01")
		.noMore();
		
		asserter.property(Gender.class)
			.expected(Gender.male())
		.noMore();
		
		asserter.rawProperty("X-GENDER")
			.value("male")
		.noMore();
		
		asserter.rawProperty("X-ID")
			.value("14f9aba0c9422da9ae376fe28bd89c2a.0")
		.noMore();
		
		asserter.rawProperty("X-ETAG")
			.value("fffffea9056d8166e2b7a427977e570c87dd51279d11d9b137c593eb")
		.noMore();
		
		asserter.rawProperty("X-FC-TAGS")
			.value("579c773f-736d-11e6-8dff-0ac8448704fb")
		.noMore();
		
		asserter.rawProperty("X-FC-LIST-ID")
			.value("8ad23200aa3e1984736b11e688dc0add41994b95")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A4D6F74686572")
			.value("Mother")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A466174686572")
			.value("Father")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A506172656E74")
			.value("Parent")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A42726F74686572")
			.value("Brother")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A536973746572")
			.value("Sister")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A4368696C64")
			.value("Child")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A467269656E64")
			.value("Friend")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A53706F757365")
			.value("Spouse")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A4669616E63C3A9")
			.value("Fiance")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A506172746E6572")
			.value("Partner")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A417373697374616E74")
			.value("Assistant")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A4D616E61676572")
			.value("Manager")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A4F74686572")
			.value("Other")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D52656C617465644E616D65733A437573746F6D54595045")
			.value("Custom")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D4F7468657244617465733A416E6E6976657273617279")
			.value("2016-08-02")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D4F7468657244617465733A4F74686572")
			.value("2016-08-03")
		.noMore();
		
		asserter.rawProperty("X-FCENCODED-582D46432D4F7468657244617465733A437573746F6D54595045")
			.value("2016-08-04")
		.noMore();
		
		asserter.simpleProperty(Note.class)
			.value("Notes line 1" + NEWLINE + "Notes line 2")
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://www.homepage.com")
		.next()
			.value("http://www.blog.com")
		.next()
			.value("http://www.other.com")
		.next()
			.value("http://www.custom.com")
		.noMore();
		
		asserter.address()
			.types(AddressType.HOME)
			.extendedAddress("HomeExtended")
			.streetAddress("HomeStreet")
			.locality("HomeCity")
			.region("HomeState")
			.postalCode("HomePostal")
			.country("HomeCountry")
		.next()
			.types(AddressType.WORK)
			.extendedAddress("WorkExtended")
			.streetAddress("WorkStreet")
			.locality("WorkCity")
			.region("WorkState")
			.postalCode("WorkPostal")
			.country("WorkCountry")
		.next()
			.types(AddressType.get("OTHER"))
			.extendedAddress("OtherExtended")
			.streetAddress("OtherStreet")
			.locality("OtherCity")
			.region("OtherState")
			.postalCode("OtherPostal")
			.country("OtherCountry")
		.next()
			.types(AddressType.get("CUSTOMTYPE"))
			.extendedAddress("CustomExtended")
			.streetAddress("CustomStreet")
			.locality("CustomCity")
			.region("CustomState")
			.postalCode("CustomPostal")
			.country("CustomCountry")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("NickName")
		.noMore();
		
		asserter.impp()
			.param("X-SERVICE-TYPE", "GTalk")
			.uri("xmpp:gtalk")
		.next()
			.param("X-SERVICE-TYPE", "Skype")
			.uri("skype:skype")
		.next()
			.param("X-SERVICE-TYPE", "Yahoo")
			.uri("ymsgr:yahoo")
		.next()
			.param("X-SERVICE-TYPE", "AIM")
			.uri("aim:aim")
		.next()
			.param("X-SERVICE-TYPE", "Jabber")
			.uri("xmpp:jabber")
		.next()
			.param("X-SERVICE-TYPE", "Other")
			.uri("other:other")
		.next()
			.param("X-SERVICE-TYPE", "CustomTYPE")
			.uri("customtype:custom")
		.noMore();
		
		asserter.listProperty(Categories.class)
			.values("Tag")
		.noMore();
		
		asserter.simpleProperty(ProductId.class)
			.value("ez-vcard 0.9.14-fc")
		.noMore();
		//@formatter:on

		asserter.validate().run();
		asserter.done();
	}

	@Test
	public void gmailVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_GMAIL.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.simpleProperty(FormattedName.class)
			.value("Mr. John Richter, James Doe Sr.")
		.noMore();

		asserter.structuredName()
			.family("Doe")
			.given("John")
			.additional("Richter, James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		asserter.email()
			.types(EmailType.INTERNET, EmailType.HOME)
			.value("john.doe@ibm.com")
		.noMore();

		asserter.telephone()
			.types(TelephoneType.CELL)
			.text("905-555-1234")
		.next()
			.types(TelephoneType.HOME)
			.text("905-666-1234")
		.noMore();

		asserter.address()
			.extendedAddress("Crescent moon drive" + NEWLINE + "555-asd" + NEWLINE + "Nice Area, Albaney, New York 12345" + NEWLINE + "United States of America")
			.types(AddressType.HOME)
		.noMore();

		asserter.listProperty(Organization.class)
			.values("IBM")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("Money Counter")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1980-03-22")
		.noMore();

		asserter.simpleProperty(Url.class)
			.value("http://www.ibm.com")
			.param("TYPE", "WORK")
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue")
		.noMore();

		asserter.rawProperty("X-PHONETIC-FIRST-NAME")
			.value("Jon")
		.noMore();
		
		asserter.rawProperty("X-PHONETIC-LAST-NAME")
			.value("Dow")
		.noMore();
		
		asserter.rawProperty("X-ABDATE")
			.group("item1")
			.value("1975-03-01")
		.noMore();
		
		asserter.rawProperty("X-ABLABEL")
			.group("item1")
			.value("_$!<Anniversary>!$_")
		.next()
			.group("item2")
			.value("_$!<Spouse>!$_")
		.noMore();
		
		asserter.rawProperty("X-ABRELATEDNAMES")
			.group("item2")
			.value("Jenny")
		.noMore();
		
		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getEmails().get(0), 9) //"TYPE=WORK" not valid in vCard 3.0
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void gmailList() throws Throwable {
		VCardAsserter asserter = read("gmail-list.vcf");

		{
			asserter.next(V3_0);

			//@formatter:off
			asserter.simpleProperty(FormattedName.class)
				.value("Arnold Smith")
			.noMore();

			asserter.structuredName()
				.family("Smith")
				.given("Arnold")
			.noMore();

			asserter.email()
				.types(EmailType.INTERNET)
				.value("asmithk@gmail.com")
			.noMore();
			//@formatter:on

			asserter.validate().run();
		}

		{
			asserter.next(V3_0);

			//@formatter:off
			asserter.simpleProperty(FormattedName.class)
				.value("Chris Beatle")
			.noMore();

			asserter.structuredName()
				.family("Beatle")
				.given("Chris")
			.noMore();

			asserter.email()
				.types(EmailType.INTERNET)
				.value("chrisy55d@yahoo.com")
			.noMore();
			//@formatter:on

			asserter.validate().run();
		}

		{
			asserter.next(V3_0);

			//@formatter:off
			asserter.simpleProperty(FormattedName.class)
				.value("Doug White")
			.noMore();

			asserter.structuredName()
				.family("White")
				.given("Doug")
			.noMore();

			asserter.email()
				.types(EmailType.INTERNET)
				.value("dwhite@gmail.com")
			.noMore();
			//@formatter:on

			asserter.validate().run();
		}

		asserter.done();
	}

	@Test
	public void gmailSingle() throws Throwable {
		VCardAsserter asserter = read("gmail-single.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.simpleProperty(FormattedName.class)
			.value("Greg Dartmouth")
		.noMore();

		asserter.structuredName()
			.family("Dartmouth")
			.given("Greg")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Gman")
		.noMore();

		asserter.email()
			.types(EmailType.INTERNET)
			.value("gdartmouth@hotmail.com")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.CELL)
			.text("555 555 1111")
		.next()
			.group("item1")
			.text("555 555 2222")
		.noMore();
		
		asserter.address()
			.streetAddress("123 Home St" + NEWLINE + "Home City, HM 12345")
			.types(AddressType.HOME)
		.next()
			.group("item2")
			.streetAddress("321 Custom St")
			.locality("Custom City")
			.region("TX")
			.postalCode("98765")
			.country("USA")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("TheCompany")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("TheJobTitle")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1960-09-10")
		.noMore();

		asserter.simpleProperty(Url.class)
			.group("item3")
			.value("http://TheProfile.com")
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("This is GMail's note field." + NEWLINE + "It should be added as a NOTE type." + NEWLINE + "ACustomField: CustomField")
		.noMore();

		asserter.rawProperty("X-PHONETIC-FIRST-NAME")
			.value("Grregg")
		.noMore();
		
		asserter.rawProperty("X-PHONETIC-LAST-NAME")
			.value("Dart-mowth")
		.noMore();
		
		asserter.rawProperty("X-ICQ")
			.value("123456789")
		.noMore();
		
		asserter.rawProperty("X-ABLABEL")
			.group("item1")
			.value("GRAND_CENTRAL")
		.next()
			.group("item2")
			.value("CustomAdrType")
		.next()
			.group("item3")
			.value("PROFILE")
		.next()
			.group("item4")
			.value("_$!<Anniversary>!$_")
		.next()
			.group("item5")
			.value("_$!<Spouse>!$_")
		.next()
			.group("item6")
			.value("CustomRelationship")
		.noMore();
		
		asserter.rawProperty("X-ABDATE")
			.group("item4")
			.value("1970-06-02")
		.noMore();
		
		asserter.rawProperty("X-ABRELATEDNAMES")
			.group("item5")
			.value("MySpouse")
		.next()
			.group("item6")
			.value("MyCustom")
		.noMore();
		//@formatter:on

		asserter.validate().run();
		asserter.done();
	}

	/**
	 * File generated on 5/7/2016.
	 */
	@Test
	public void gmailSingle2() throws Throwable {
		VCardAsserter asserter = read("gmail-single2.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.simpleProperty(FormattedName.class)
			.value("VCard Test")
		.noMore();
		
		asserter.structuredName()
			.family("Test")
			.given("VCard")
		.noMore();
		
		asserter.listProperty(Nickname.class)
			.values("TheNickname")
		.noMore();
		
		asserter.email()
			.types(EmailType.INTERNET)
			.value("email@example.com")
		.next()
			.types(EmailType.INTERNET, EmailType.HOME)
			.value("homeemail@example.com")
		.next()
			.types(EmailType.INTERNET, EmailType.WORK)
			.value("workemail@example.com")
		.next()
			.types(EmailType.INTERNET)
			.value("otheremail@example.com")
		.next()
			.types(EmailType.INTERNET)
			.group("item1")
			.value("customcategory@example.com")
		.noMore();
		
		asserter.telephone()
			.text("5555551111")
		.next()
			.types(TelephoneType.HOME)
			.text("5555551112")
		.next()
			.types(TelephoneType.WORK)
			.text("5555551113")
		.next()
			.text("5555551114")
		.next()
			.types(TelephoneType.CELL)
			.text("5555551115")
		.next()
			.types(TelephoneType.get("MAIN"))
			.text("5555551116")
		.next()
			.types(TelephoneType.HOME, TelephoneType.FAX)
			.text("5555551117")
		.next()
			.types(TelephoneType.WORK, TelephoneType.FAX)
			.text("5555551118")
		.next()
			.group("item2")
			.text("5555551119")
		.next()
			.types(TelephoneType.PAGER)
			.text("5555551120")
		.next()
			.group("item3")
			.text("5555551121")
		.noMore();
		
		asserter.address()
			.streetAddress("111 Main St")
			.locality("NY")
			.region("New York")
			.postalCode("10011")
		.next()
			.types(AddressType.HOME)
			.streetAddress("112 Main St")
			.locality("NY")
			.region("New York")
			.postalCode("10011")
		.next()
			.types(AddressType.WORK)
			.streetAddress("113 Main St")
			.locality("NY")
			.region("New York")
			.postalCode("10011")
		.next()
			.streetAddress("114 Main St")
			.locality("NY")
			.region("New York")
			.postalCode("10011")
		.next()
			.group("item4")
			.streetAddress("115 Main St")
			.locality("NY")
			.region("New York")
			.postalCode("10011")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("TheCompany")
		.noMore();
		
		asserter.simpleProperty(Title.class)
			.value("TheJobTitle")
		.noMore();
		
		asserter.dateProperty(Birthday.class)
			.date("1912-06-23")
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://www.example1.com")
		.next()
			.group("item5")
			.value("http://www.example2.com")
		.next()
			.group("item6")
			.value("http://www.example3.com")
		.next()
			.group("item7")
			.value("http://www.example4.com")
		.next()
			.param("TYPE", "WORK")
			.value("http://www.example5.com")
		.next()
			.group("item8")
			.value("http://www.example6.com")
		.noMore();
		
		asserter.simpleProperty(Note.class)
			.value("note line 1" + NEWLINE + "note line 2" + NEWLINE + "CustomField: field value")
		.noMore();
		
		asserter.rawProperty("X-PHONETIC-FIRST-NAME")
			.value("ThePhoneticFirstName")
		.noMore();
		asserter.rawProperty("X-PHONETIC-LAST-NAME")
			.value("ThePhoneticLastName")
		.noMore();
		
		asserter.rawProperty("X-ABLabel")
			.group("item1")
			.value("CustomEmailCategory")
		.next()
			.group("item2")
			.value("GRAND_CENTRAL")
		.next()
			.group("item3")
			.value("CustomePhoneCategory")
		.next()
			.group("item4")
			.value("CustomAddressCategory")
		.next()
			.group("item5")
			.value("PROFILE")
		.next()
			.group("item6")
			.value("BLOG")
		.next()
			.group("item7")
			.value("_$!<HomePage>!$_")
		.next()
			.group("item8")
			.value("CustomWebsiteCategory")
		.next()
			.group("item9")
			.value("_$!<Anniversary>!$_")
		.next()
			.group("item10")
			.value("CustomDateCategory")
		.next()
			.group("item11")
			.value("_$!<Spouse>!$_")
		.next()
			.group("item12")
			.value("_$!<Child>!$_")
		.next()
			.group("item13")
			.value("_$!<Mother>!$_")
		.next()
			.group("item14")
			.value("_$!<Father>!$_")
		.next()
			.group("item15")
			.value("_$!<Parent>!$_")
		.next()
			.group("item16")
			.value("_$!<Brother>!$_")
		.next()
			.group("item17")
			.value("_$!<Sister>!$_")
		.next()
			.group("item18")
			.value("_$!<Friend>!$_")
		.next()
			.group("item19")
			.value("RELATIVE")
		.next()
			.group("item20")
			.value("_$!<Manager>!$_")
		.next()
			.group("item21")
			.value("_$!<Assistant>!$_")
		.next()
			.group("item22")
			.value("REFERRED_BY")
		.next()
			.group("item23")
			.value("_$!<Partner>!$_")
		.next()
			.group("item24")
			.value("DOMESTIC_PARTNER")
		.next()
			.group("item25")
			.value("CustomRelationCategory")
		.noMore();
		
		asserter.rawProperty("X-GTALK")
			.value("IM2")
		.noMore();
		asserter.rawProperty("X-AIM")
			.value("IM3")
		.noMore();
		asserter.rawProperty("X-YAHOO")
			.value("IM4")
		.noMore();
		asserter.rawProperty("X-SKYPE")
			.value("IM5")
		.noMore();
		asserter.rawProperty("X-QQ")
			.value("IM6")
		.noMore();
		asserter.rawProperty("X-MSN")
			.value("IM7")
		.noMore();
		asserter.rawProperty("X-ICQ")
			.value("IM8")
		.noMore();
		asserter.rawProperty("X-JABBER")
			.value("IM9")
		.noMore();
		
		asserter.rawProperty("X-ABDATE")
			.group("item9")
			.value("1930-03-20")
		.next()
			.value("1776-07-04")
		.next()
			.group("item10")
			.value("2000-09-12")
		.noMore();
		
		asserter.rawProperty("X-ABRELATEDNAMES")
			.value("Name1")
		.next()
			.group("item11")
			.value("Name2")
		.next()
			.group("item12")
			.value("Name3")
		.next()
			.group("item13")
			.value("Name4")
		.next()
			.group("item14")
			.value("Name5")
		.next()
			.group("item15")
			.value("Name6")
		.next()
			.group("item16")
			.value("Name7")
		.next()
			.group("item17")
			.value("Name8")
		.next()
			.group("item18")
			.value("Name9")
		.next()
			.group("item19")
			.value("Name10")
		.next()
			.group("item20")
			.value("Name11")
		.next()
			.group("item21")
			.value("Name12")
		.next()
			.group("item22")
			.value("Name13")
		.next()
			.group("item23")
			.value("Name14")
		.next()
			.group("item24")
			.value("Name15")
		.next()
			.group("item25")
			.value("Name16")
		.noMore();

		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getEmails().get(1), 9) //"TYPE=HOME" not valid in vCard 3.0 
			.prop(vcard.getEmails().get(2), 9) //"TYPE=WORK" not valid in vCard 3.0
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void iPhoneVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_IPHONE.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.simpleProperty(ProductId.class)
			.value("-//Apple Inc.//iOS 5.0.1//EN")
		.noMore();

		asserter.structuredName()
			.family("Doe")
			.given("John")
			.additional("Richter", "James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		asserter.simpleProperty(FormattedName.class)
			.value("Mr. John Richter James Doe Sr.")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Johny")
		.noMore();

		asserter.listProperty(Organization.class)
			.values("IBM", "Accounting")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("Money Counter")
		.noMore();

		asserter.email()
			.group("item1")
			.types(EmailType.INTERNET, EmailType.PREF)
			.value("john.doe@ibm.com")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.CELL, TelephoneType.VOICE, TelephoneType.PREF)
			.text("905-555-1234")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("905-666-1234")
		.next()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("905-777-1234")
		.next()
			.types(TelephoneType.HOME, TelephoneType.FAX)
			.text("905-888-1234")
		.next()
			.types(TelephoneType.WORK, TelephoneType.FAX)
			.text("905-999-1234")
		.next()
			.types(TelephoneType.PAGER)
			.text("905-111-1234")
		.next()
			.group("item2")
			.text("905-222-1234")
		.noMore();
		
		asserter.address()
			.group("item3")
			.streetAddress("Silicon Alley 5", "")
			.locality("New York")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.types(AddressType.HOME, AddressType.PREF)
		.next()
			.group("item4")
			.streetAddress("Street4" + NEWLINE + "Building 6" + NEWLINE + "Floor 8")
			.locality("New York")
			.postalCode("12345")
			.country("USA")
			.types(AddressType.WORK)
		.noMore();

		asserter.simpleProperty(Url.class)
			.group("item5")
			.value("http://www.ibm.com")
			.param("TYPE", "pref")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("2012-06-06")
		.noMore();
		
		asserter.binaryProperty(Photo.class)
			.param("ENCODING", "b")
			.param("TYPE", "JPEG")
			.contentType(ImageType.JPEG)
			.dataLength(32531)
		.noMore();

		asserter.rawProperty("X-ABLABEL")
			.group("item2")
			.value("_$!<AssistantPhone>!$_")
		.next()
			.group("item5")
			.value("_$!<HomePage>!$_")
		.noMore();
		
		asserter.rawProperty("X-ABADR")
			.group("item3")
			.value("Silicon Alley")
		.next()
			.group("item4")
			.value("Street 4, Building 6,\\n Floor 8\\nNew York\\nUSA")
		.noMore();
		//@formatter:on

		asserter.validate().run();
		asserter.done();
	}

	@Test
	public void lotusNotesVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_LOTUS_NOTES.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.simpleProperty(ProductId.class)
			.value("-//Apple Inc.//Address Book 6.1//EN")
		.noMore();

		asserter.structuredName()
			.family("Doe")
			.given("John")
			.additional("Johny")
			.prefixes("Mr.")
			.suffixes("I")
		.noMore();
		
		asserter.simpleProperty(FormattedName.class)
			.value("Mr. Doe John I Johny")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Johny,JayJay")
		.noMore();

		asserter.listProperty(Organization.class)
			.values("IBM", "SUN")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("Generic Accountant")
		.noMore();

		asserter.email()
			.types(EmailType.INTERNET, EmailType.WORK, EmailType.PREF)
			.value("john.doe@ibm.com")
		.next()
			.types(EmailType.INTERNET, EmailType.WORK)
			.value("billy_bob@gmail.com")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.CELL, TelephoneType.VOICE, TelephoneType.PREF)
			.text("+1 (212) 204-34456")
		.next()
			.types(TelephoneType.WORK, TelephoneType.FAX)
			.text("00-1-212-555-7777")
		.noMore();
		
		asserter.address()
			.group("item1")
			.streetAddress("25334" + NEWLINE + "South cresent drive, Building 5, 3rd floo r")
			.locality("New York")
			.region("New York")
			.postalCode("NYC887")
			.country("U.S.A.")
			.types(AddressType.HOME, AddressType.PREF)
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"" + NEWLINE + "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO , THE" + NEWLINE + "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR P URPOSE" + NEWLINE + "ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTOR S BE" + NEWLINE + "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR" + NEWLINE + "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF" + NEWLINE + " SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS " + NEWLINE + "INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN" + NEWLINE + " CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)" + NEWLINE + "A RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE" + NEWLINE + " POSSIBILITY OF SUCH DAMAGE.")
		.noMore();

		asserter.simpleProperty(Url.class)
			.group("item2")
			.value("http://www.sun.com")
			.param("TYPE", "pref")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1980-05-21")
		.noMore();
		
		asserter.binaryProperty(Photo.class)
			.param("ENCODING", "b")
			.param("TYPE", "JPEG")
			.contentType(ImageType.JPEG)
			.dataLength(7957)
		.noMore();

		asserter.simpleProperty(Uid.class)
			.value("0e7602cc-443e-4b82-b4b1-90f62f99a199")
		.noMore();
		
		asserter.geo()
			.latitude(-2.6)
			.longitude(3.4)
		.noMore();

		asserter.simpleProperty(Classification.class)
			.value("Public")
		.noMore();

		asserter.simpleProperty(Profile.class)
			.value("VCard")
		.noMore();

		asserter.timezone()
			.offset(new UtcOffset(true, 1, 0))
		.noMore();

		asserter.simpleProperty(Label.class)
			.value("John Doe" + NEWLINE + "New York, NewYork," + NEWLINE + "South Crecent Dr ive," + NEWLINE + "Building 5, floor 3," + NEWLINE + "USA")
			.param("TYPE", "HOME")
			.param("TYPE", "PARCEL")
			.param("TYPE", "PREF")
		.noMore();

		asserter.simpleProperty(SortString.class)
			.value("JOHN")
		.noMore();

		asserter.simpleProperty(Role.class)
			.value("Counting Money")
		.noMore();

		asserter.simpleProperty(Source.class)
			.value("Whatever")
		.noMore();

		asserter.simpleProperty(Mailer.class)
			.value("Mozilla Thunderbird")
		.noMore();

		asserter.simpleProperty(SourceDisplayText.class)
			.value("VCard for John Doe")
		.noMore();

		asserter.rawProperty("X-ABLABEL")
			.group("item2")
			.value("_$!<HomePage>!$_")
		.noMore();
		
		asserter.rawProperty("X-ABUID")
			.value("0E7602CC-443E-4B82-B4B1-90F62F99A199:ABPerson")
		.noMore();
		
		asserter.rawProperty("X-GENERATOR")
			.value("Cardme Generator")
		.noMore();
		
		asserter.rawProperty("X-LONG-STRING")
			.value("12345678901234567890123456789012345678901234567890123456789012 34567890123456789012345678901234567890")
		.noMore();
		
		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getEmails().get(0), 9) //"TYPE=WORK" not valid in vCard 3.0
			.prop(vcard.getEmails().get(1), 9) //"TYPE=WORK" not valid in vCard 3.0
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void msOutlookVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_MS_OUTLOOK.vcf");
		asserter.next(V2_1);

		//@formatter:off
		asserter.structuredName()
			.family("Doe")
			.given("John")
			.additional("Richter,James")
			.prefixes("Mr.")
			.suffixes("Sr.")
			.param(VCardParameters.LANGUAGE, "en-us")
		.noMore();

		asserter.simpleProperty(FormattedName.class)
			.value("Mr. John Richter James Doe Sr.")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Johny")
		.noMore();

		asserter.listProperty(Organization.class)
			.values("IBM", "Accounting")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("Money Counter")
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("(905) 555-1234")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("(905) 666-1234")
		.noMore();
		
		asserter.address()
			.streetAddress("Cresent moon drive")
			.locality("Albaney")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.label("Cresent moon drive\r\nAlbaney, New York  12345")
			.types(AddressType.WORK, AddressType.PREF)
		.next()
			.streetAddress("Silicon Alley 5,")
			.locality("New York")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.label("Silicon Alley 5,\r\nNew York, New York  12345")
			.types(AddressType.HOME)
		.noMore();

		asserter.simpleProperty(Url.class)
			.value("http://www.ibm.com")
			.param("TYPE", "WORK")
		.noMore();

		asserter.simpleProperty(Role.class)
			.value("Counting Money")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1980-03-22")
		.noMore();

		asserter.email()
			.types(EmailType.PREF, EmailType.INTERNET)
			.value("john.doe@ibm.cm")
		.noMore();
		
		asserter.binaryProperty(Photo.class)
			.param("ENCODING", "BASE64")
			.param("TYPE", "JPEG")
			.contentType(ImageType.JPEG)
			.dataLength(860)
		.noMore();
		
		asserter.simpleProperty(Revision.class)
			.value(utc("2012-03-05 13:19:33"))
		.noMore();
		Revision rev = asserter.getVCard().getRevision();
		assertEquals(utc(2012, Calendar.MARCH, 5, 13, 19, 33), rev.getCalendar());
		
		asserter.rawProperty("X-MS-OL-DEFAULT-POSTAL-ADDRESS")
			.value("2")
		.noMore();
		
		asserter.rawProperty("X-MS-ANNIVERSARY")
			.value("20110113")
		.noMore();
		
		asserter.rawProperty("X-MS-IMADDRESS")
			.value("johny5@aol.com")
		.noMore();
		
		asserter.rawProperty("X-MS-OL-DESIGN")
			.value("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>")
			.param("CHARSET", "utf-8")
		.noMore();
		
		asserter.rawProperty("X-MS-MANAGER")
			.value("Big Blue")
		.noMore();
		
		asserter.rawProperty("X-MS-ASSISTANT")
			.value("Jenny")
		.noMore();
		
		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getNickname(), 2) //not supported in 2.1
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void outlook2007VCard() throws Throwable {
		VCardAsserter asserter = read("outlook-2007.vcf");
		asserter.next(V2_1);

		//@formatter:off
		asserter.structuredName()
			.family("Angstadt")
			.given("Michael")
			.prefixes("Mr.")
			.suffixes("Jr.")
			.param(VCardParameters.LANGUAGE, "en-us")
		.noMore();

		asserter.simpleProperty(FormattedName.class)
			.value("Mr. Michael Angstadt Jr.")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Mike")
		.noMore();

		asserter.listProperty(Organization.class)
			.values("TheCompany", "TheDepartment")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("TheJobTitle")
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("This is the NOTE field	\r\nI assume it encodes this text inside a NOTE vCard type.\r\nBut I'm not sure because there's text formatting going on here.\r\nIt does not preserve the formatting")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.param("CHARSET", "us-ascii")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("(111) 555-1111")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("(111) 555-2222")
		.next()
			.types(TelephoneType.CELL, TelephoneType.VOICE)
			.text("(111) 555-4444")
		.next()
			.types(TelephoneType.WORK, TelephoneType.FAX)
			.text("(111) 555-3333")
		.noMore();
		
		asserter.address()
			.extendedAddress("TheOffice")
			.streetAddress("222 Broadway")
			.locality("New York")
			.region("NY")
			.postalCode("99999")
			.country("USA")
			.label("222 Broadway\r\nNew York, NY 99999\r\nUSA")
			.types(AddressType.WORK, AddressType.PREF)
		.noMore();

		asserter.simpleProperty(Url.class)
			.value("http://mikeangstadt.name")
			.param("TYPE", "HOME")
		.next()
			.value("http://mikeangstadt.name")
			.param("TYPE", "WORK")
		.noMore();

		asserter.simpleProperty(Role.class)
			.value("TheProfession")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1922-03-10")
		.noMore();
		
		asserter.binaryProperty(Key.class)
			.param("ENCODING", "BASE64")
			.param("TYPE", "X509")
			.contentType(KeyType.X509)
			.dataLength(514)
		.noMore();

		asserter.email()
			.types(EmailType.PREF, EmailType.INTERNET)
			.value("mike.angstadt@gmail.com")
		.noMore();
			
		asserter.binaryProperty(Photo.class)
			.param("ENCODING", "BASE64")
			.param("TYPE", "JPEG")
			.contentType(ImageType.JPEG)
			.dataLength(2324)
		.noMore();

		asserter.simpleProperty(FreeBusyUrl.class)
			.value("http://website.com/mycal")
		.noMore();
		
		asserter.simpleProperty(Revision.class)
			.value(utc("2012-08-01 18:46:31"))
		.noMore();
		Revision rev = asserter.getVCard().getRevision();
		assertEquals(utc(2012, Calendar.AUGUST, 1, 18, 46, 31), rev.getCalendar());

		asserter.rawProperty("X-MS-TEL")
			.value("(111) 555-4444")
			.param("TYPE", "VOICE")
			.param("TYPE", "CALLBACK")
		.noMore();
		
		asserter.rawProperty("X-MS-OL-DEFAULT-POSTAL-ADDRESS")
			.value("2")
		.noMore();
		
		asserter.rawProperty("X-MS-ANNIVERSARY")
			.value("20120801")
		.noMore();
		
		asserter.rawProperty("X-MS-IMADDRESS")
			.value("im@aim.com")
		.noMore();
		
		asserter.rawProperty("X-MS-OL-DESIGN")
			.value("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telcell\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Mobile</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>")
			.param("CHARSET", "utf-8")
		.noMore();
		
		asserter.rawProperty("X-MS-MANAGER")
			.value("TheManagerName")
		.noMore();
		
		asserter.rawProperty("X-MS-ASSISTANT")
			.value("TheAssistantName")
		.noMore();
		
		asserter.rawProperty("X-MS-SPOUSE")
			.value("TheSpouse")
		.noMore();
		
		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getNickname(), 2) //not supported in 2.1
			.prop(vcard.getFbUrls().get(0), 2) //not supported in 2.1
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void macAddressBookVCard() throws Throwable {
		VCardAsserter asserter = read("John_Doe_MAC_ADDRESS_BOOK.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.structuredName()
			.family("Doe")
			.given("John")
			.additional("Richter,James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		asserter.simpleProperty(FormattedName.class)
			.value("Mr. John Richter,James Doe Sr.")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Johny")
		.noMore();

		asserter.listProperty(Organization.class)
			.values("IBM", "Accounting")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("Money Counter")
		.noMore();

		asserter.email()
			.types(EmailType.INTERNET, EmailType.WORK, EmailType.PREF)
			.value("john.doe@ibm.com")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.WORK, TelephoneType.PREF)
			.text("905-777-1234")
		.next()
			.types(TelephoneType.HOME)
			.text("905-666-1234")
		.next()
			.types(TelephoneType.CELL)
			.text("905-555-1234")
		.next()
			.types(TelephoneType.HOME, TelephoneType.FAX)
			.text("905-888-1234")
		.next()
			.types(TelephoneType.WORK,TelephoneType.FAX)
			.text("905-999-1234")
		.next()
			.types(TelephoneType.PAGER)
			.text("905-111-1234")
		.next()
			.group("item1")
			.text("905-222-1234")
		.noMore();
		
		asserter.address()
			.group("item2")
			.streetAddress("Silicon Alley 5,")
			.locality("New York")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.types(AddressType.HOME, AddressType.PREF)
		.next()
			.group("item3")
			.streetAddress("Street4" + NEWLINE + "Building 6" + NEWLINE + "Floor 8")
			.locality("New York")
			.postalCode("12345")
			.country("USA")
			.types(AddressType.WORK)
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue")
		.noMore();

		asserter.simpleProperty(Url.class)
			.group("item4")
			.value("http://www.ibm.com")
			.param("TYPE", "pref")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("2012-06-06")
		.noMore();
		
		asserter.binaryProperty(Photo.class)
			.param("ENCODING", "BASE64")
			.dataLength(18242)
		.noMore();

		asserter.rawProperty("X-PHONETIC-FIRST-NAME")
			.value("Jon")
		.noMore();
		
		asserter.rawProperty("X-PHONETIC-LAST-NAME")
			.value("Dow")
		.noMore();
		
		asserter.rawProperty("X-ABLABEL")
			.group("item1")
			.value("AssistantPhone")
		.next()
			.group("item4")
			.value("_$!<HomePage>!$_")
		.next()
			.group("item5")
			.value("Spouse")
		.noMore();
		
		asserter.rawProperty("X-ABADR")
			.group("item2")
			.value("Silicon Alley")
		.next()
			.group("item3")
			.value("Street 4, Building 6,\\nFloor 8\\nNew York\\nUSA")
		.noMore();
		
		asserter.rawProperty("X-ABRELATEDNAMES")
			.group("item5")
			.value("Jenny")
			.param("TYPE",  "pref")
		.noMore();
		
		asserter.rawProperty("X-ABUID")
			.value("6B29A774-D124-4822-B8D0-2780EC117F60\\:ABPerson")
		.noMore();
		
		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getEmails().get(0), 9) //"TYPE=WORK" not valid in vCard 3.0
			.prop(vcard.getPhotos().get(0), 4) //"BASE64" not valid parameter
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void outlook2003VCard() throws Throwable {
		VCardAsserter asserter = read("outlook-2003.vcf");
		asserter.next(V2_1);

		//@formatter:off
		asserter.structuredName()
			.family("Doe")
			.given("John")
			.prefixes("Mr.")
			.suffixes("III")
		.noMore();

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe III")
		.noMore();

		asserter.listProperty(Nickname.class)
			.values("Joey")
		.noMore();

		asserter.listProperty(Organization.class)
			.values("Company, The", "TheDepartment")
		.noMore();

		asserter.simpleProperty(Title.class)
			.value("The Job Title")
		.noMore();

		asserter.simpleProperty(Note.class)
			.value("This is the note field!!\r\nSecond line\r\n\r\nThird line is empty\r\n")
			.param("ENCODING", "QUOTED-PRINTABLE")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("BusinessPhone")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("HomePhone")
		.next()
			.types(TelephoneType.CELL, TelephoneType.VOICE)
			.text("MobilePhone")
		.next()
			.types(TelephoneType.WORK, TelephoneType.FAX)
			.text("BusinessFaxPhone")
		.noMore();
		
		asserter.address()
			.extendedAddress("TheOffice")
			.streetAddress("123 Main St")
			.locality("Austin")
			.region("TX")
			.postalCode("12345")
			.country("United States of America")
			.label("TheOffice\r\n123 Main St\r\nAustin, TX 12345\r\nUnited States of America")
			.types(AddressType.WORK)
		.noMore();

		asserter.simpleProperty(Url.class)
			.value("http://web-page-address.com")
			.param("TYPE", "WORK")
		.noMore();

		asserter.simpleProperty(Role.class)
			.value("TheProfession")
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1980-03-21")
		.noMore();
		
		asserter.binaryProperty(Key.class)
			.param("ENCODING", "BASE64")
			.param("TYPE", "X509")
			.contentType(KeyType.X509)
			.dataLength(805)
		.noMore();

		asserter.email()
			.types(EmailType.PREF, EmailType.INTERNET)
			.value("jdoe@hotmail.com")
		.noMore();

		/*
		 * Outlook 2003 apparently doesn't output FBURL correctly:
		 * http://help.lockergnome.com/office/BUG-Outlook-2003-exports-FBURL-vCard-incorrectly--ftopict423660.html
		 */
		asserter.simpleProperty(FreeBusyUrl.class)
			.value("????????????????s????????????" + (char) 12)
			.param("ENCODING", "QUOTED-PRINTABLE")
		.noMore();
		
		asserter.simpleProperty(Revision.class)
			.value(utc("2012-10-12 21:05:25"))
		.noMore();
		Revision rev = asserter.getVCard().getRevision();
		assertEquals(utc(2012, Calendar.OCTOBER, 12, 21, 5, 25), rev.getCalendar());
		
		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getNickname(), 2) //not supported in 2.1
			.prop(vcard.getFbUrls().get(0), 2) //not supported in 2.1
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void thunderbird() throws Throwable {
		VCardAsserter asserter = read("thunderbird-MoreFunctionsForAddressBook-extension.vcf");
		asserter.next(V3_0);

		//@formatter:off
		asserter.structuredName()
			.param("CHARSET", "UTF-8")
			.family("Doe")
			.given("John")
		.noMore();

		asserter.simpleProperty(FormattedName.class)
			.param("CHARSET", "UTF-8")
			.value("John Doe")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.param("CHARSET", "UTF-8")
			.values("TheOrganization", "TheDepartment")
		.noMore();

		asserter.listProperty(Nickname.class)
			.param("CHARSET", "UTF-8")
			.values("Johnny")
		.noMore();

		asserter.address()
			.param("CHARSET", "UTF-8")
			.extendedAddress("222 Broadway")
			.streetAddress("Suite 100")
			.locality("New York")
			.region("NY")
			.postalCode("98765")
			.country("USA")
			.types(AddressType.WORK, AddressType.POSTAL)
		.next()
			.param("CHARSET", "UTF-8")
			.extendedAddress("123 Main St")
			.streetAddress("Apt 10")
			.locality("Austin")
			.region("TX")
			.postalCode("12345")
			.country("USA")
			.types(AddressType.HOME, AddressType.POSTAL)
		.noMore();

		asserter.telephone()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("555-555-1111")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("555-555-2222")
		.next()
			.types(TelephoneType.CELL, TelephoneType.VOICE)
			.text("555-555-5555")
		.next()
			.types(TelephoneType.FAX)
			.text("555-555-3333")
		.next()
			.types(TelephoneType.PAGER)
			.text("555-555-4444")
		.noMore();

		asserter.email()
			.types(EmailType.PREF, EmailType.INTERNET)
			.value("doe.john@hotmail.com")
		.next()
			.types(EmailType.INTERNET)
			.value("additional-email@company.com")
		.next()
			.types(EmailType.INTERNET)
			.value("additional-email1@company.com")
		.next()
			.types(EmailType.INTERNET)
			.value("additional-email2@company.com")
		.next()
			.types(EmailType.INTERNET)
			.value("additional-email3@company.com")
		.noMore();

		asserter.simpleProperty(Url.class)
			.value("http://www.private-webpage.com")
			.param("TYPE", "HOME")
		.next()
			.value("http://www.work-webpage.com")
			.param("TYPE", "WORK")
		.noMore();

		asserter.simpleProperty(Title.class)
			.param("CHARSET", "UTF-8")
			.value("TheTitle")
		.noMore();

		asserter.listProperty(Categories.class)
			.param("CHARSET", "UTF-8")
			.values("category1, category2, category3") //commas are incorrectly escaped, so there is only 1 item
		.noMore();

		asserter.dateProperty(Birthday.class)
			.date("1970-09-21")
		.noMore();

		asserter.simpleProperty(Note.class)
			.param("CHARSET", "UTF-8")
			.value("This is the notes field." + NEWLINE + "Second Line" + NEWLINE + NEWLINE + "Fourth Line" + NEWLINE + "You can put anything in the \"note\" field; even curse words.")
		.noMore();
		
		asserter.binaryProperty(Photo.class)
			.param("ENCODING", "b")
			.param("TYPE", "JPEG")
			.contentType(ImageType.JPEG)
			.dataLength(8940)
		.noMore();

		asserter.rawProperty("X-SPOUSE")
			.value("TheSpouse")
		.noMore();
		
		asserter.rawProperty("X-ANNIVERSARY")
			.value("1990-04-30")
		.noMore();

		VCard vcard = asserter.getVCard();
		asserter.validate()
			.prop(vcard.getStructuredName(), 6) //CHARSET not supported in 3.0
			.prop(vcard.getFormattedName(), 6) //CHARSET not supported in 3.0
			.prop(vcard.getOrganization(), 6) //CHARSET not supported in 3.0
			.prop(vcard.getNickname(), 6) //CHARSET not supported in 3.0
			.prop(vcard.getAddresses().get(0), 6) //CHARSET not supported in 3.0
			.prop(vcard.getAddresses().get(1), 6) //CHARSET not supported in 3.0
			.prop(vcard.getTitles().get(0), 6) //CHARSET not supported in 3.0
			.prop(vcard.getCategories(), 6) //CHARSET not supported in 3.0
			.prop(vcard.getNotes().get(0), 6) //CHARSET not supported in 3.0
		.run();
		//@formatter:on

		asserter.done();
	}

	@Test
	public void rfc6350_example() throws Throwable {
		VCardAsserter asserter = read("rfc6350-example.vcf");
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
			.partialDate(PartialDate.builder().year(2009).month(8).date(8).hour(14).minute(30).offset(new UtcOffset(false, -5, 0)).build())
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
			.param("TYPE", "work")
			.values("Viagenie")
		.noMore();
		
		asserter.address()
			.extendedAddress("Suite D2-630")
			.streetAddress("2875 Laurier")
			.locality("Quebec")
			.region("QC")
			.postalCode("G1V 2M2")
			.country("Canada")
			.types(AddressType.WORK)
		.noMore();

		asserter.telephone()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.uri(new TelUri.Builder("+1-418-656-9254").extension("102").build())
			.param("PREF", "1")
		.next()
			.types(TelephoneType.WORK, TelephoneType.CELL, TelephoneType.VOICE, TelephoneType.VIDEO, TelephoneType.TEXT)
			.uri(new TelUri.Builder("+1-418-262-6501").build())
		.noMore();

		asserter.email()
			.types(EmailType.WORK)
			.value("simon.perreault@viagenie.ca")
		.noMore();
		
		asserter.geo()
			.latitude(46.772673)
			.longitude(-71.282945)
			.param("TYPE", "work")
		.noMore();
		
		asserter.binaryProperty(Key.class)
			.url("http://www.viagenie.ca/simon.perreault/simon.asc")
			.param("TYPE", "work")
		.noMore();
		
		asserter.timezone()
			.offset(new UtcOffset(false, -5, 0))
		.noMore();

		asserter.simpleProperty(Url.class)
			.value("http://nomis80.org")
			.param("TYPE", "home")
		.noMore();
		//@formatter:on

		asserter.validate().run();
		asserter.done();
	}

	@Test
	public void rfc2426_example() throws Throwable {
		VCardAsserter asserter = read("rfc2426-example.vcf");

		{
			asserter.next(V3_0);

			//@formatter:off
			asserter.simpleProperty(FormattedName.class)
				.value("Frank Dawson")
			.noMore();
			
			asserter.listProperty(Organization.class)
				.values("Lotus Development Corporation")
			.noMore();

			asserter.address()
				.streetAddress("6544 Battleford Drive")
				.locality("Raleigh")
				.region("NC")
				.postalCode("27613-3502")
				.country("U.S.A.")
				.types(AddressType.WORK, AddressType.POSTAL, AddressType.PARCEL)
			.noMore();

			asserter.telephone()
				.types(TelephoneType.VOICE, TelephoneType.MSG, TelephoneType.WORK)
				.text("+1-919-676-9515")
			.next()
				.types(TelephoneType.FAX, TelephoneType.WORK)
				.text("+1-919-676-9564")
			.noMore();

			asserter.email()
				.types(EmailType.INTERNET, EmailType.PREF)
				.value("Frank_Dawson@Lotus.com")
			.next()
				.types(EmailType.INTERNET)
				.value("fdawson@earthlink.net")
			.noMore();

			asserter.simpleProperty(Url.class)
				.value("http://home.earthlink.net/~fdawson")
			.noMore();
			
			asserter.validate()
				.prop(null, 0) //N property required
			.run();
			//@formatter:on
		}

		{
			asserter.next(V3_0);

			//@formatter:off
			asserter.simpleProperty(FormattedName.class)
				.value("Tim Howes")
			.noMore();
			
			asserter.listProperty(Organization.class)
				.values("Netscape Communications Corp.")
			.noMore();

			asserter.address()
				.streetAddress("501 E. Middlefield Rd.")
				.locality("Mountain View")
				.region("CA")
				.postalCode(" 94043")
				.country("U.S.A.")
				.types(AddressType.WORK)
			.noMore();

			asserter.telephone()
				.types(TelephoneType.VOICE, TelephoneType.MSG, TelephoneType.WORK)
				.text("+1-415-937-3419")
			.next()
				.types(TelephoneType.FAX, TelephoneType.WORK)
				.text("+1-415-528-4164")
			.noMore();

			asserter.email()
				.types(EmailType.INTERNET)
				.value("howes@netscape.com")
			.noMore();
			
			asserter.validate()
				.prop(null, 0) //N property required
			.run();
			//@formatter:on
		}

		asserter.done();
	}

	private static VCardAsserter read(String filename) {
		VCardReader reader = new VCardReader(SampleVCardsTest.class.getResourceAsStream(filename));
		return new VCardAsserter(reader);
	}
}
