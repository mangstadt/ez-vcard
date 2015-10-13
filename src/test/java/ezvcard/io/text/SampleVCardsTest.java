package ezvcard.io.text;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.property.asserter.PropertyAsserter.assertAddress;
import static ezvcard.property.asserter.PropertyAsserter.assertBinaryProperty;
import static ezvcard.property.asserter.PropertyAsserter.assertDateProperty;
import static ezvcard.property.asserter.PropertyAsserter.assertEmail;
import static ezvcard.property.asserter.PropertyAsserter.assertGeo;
import static ezvcard.property.asserter.PropertyAsserter.assertListProperty;
import static ezvcard.property.asserter.PropertyAsserter.assertRawProperty;
import static ezvcard.property.asserter.PropertyAsserter.assertSimpleProperty;
import static ezvcard.property.asserter.PropertyAsserter.assertStructuredName;
import static ezvcard.property.asserter.PropertyAsserter.assertTelephone;
import static ezvcard.property.asserter.PropertyAsserter.assertTimezone;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.assertWarnings;
import static ezvcard.util.TestUtils.utc;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.KeyType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Classification;
import ezvcard.property.Mailer;
import ezvcard.property.ProductId;
import ezvcard.property.Profile;
import ezvcard.property.Revision;
import ezvcard.property.SortString;
import ezvcard.property.SourceDisplayText;
import ezvcard.property.Uid;
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
public class SampleVCardsTest {
	@Test
	public void androidVCard() throws Throwable {
		VCardReader reader = read("John_Doe_ANDROID.vcf");

		{
			VCard vcard = reader.readNext();
			assertVersion(V2_1, vcard);
			assertPropertyCount(2, vcard);

			//@formatter:off
			assertEmail(vcard)
				.types(EmailType.PREF)
				.value("john.doe@company.com")
			.noMore();

			assertListProperty(vcard.getCategoriesList())
				.values("My Contacts")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).prop(vcard.getCategories(), 2).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V2_1, vcard);
			assertPropertyCount(2, vcard);

			//@formatter:off
			assertEmail(vcard)
				.types(EmailType.PREF)
				.value("jane.doe@company.com")
			.noMore();

			assertListProperty(vcard.getCategoriesList())
				.values("My Contacts")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).prop(vcard.getCategories(), 2).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V2_1, vcard);
			assertPropertyCount(4, vcard);

			//@formatter:off
			assertStructuredName(vcard)
				.family("\u00d1 \u00d1 \u00d1 \u00d1")
			.noMore();

			assertSimpleProperty(vcard.getFormattedNames())
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ")
			.noMore();
			
			assertTelephone(vcard)
				.types(TelephoneType.CELL, TelephoneType.PREF)
				.text("123456789")
			.noMore();

			assertListProperty(vcard.getCategoriesList())
				.values("My Contacts")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getCategories(), 2).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V2_1, vcard);
			assertPropertyCount(9, vcard);

			//@formatter:off
			assertStructuredName(vcard)
				.family("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1")
			.noMore();

			assertSimpleProperty(vcard.getFormattedNames())
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1")
			.noMore();
			
			assertTelephone(vcard)
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

			assertListProperty(vcard.getCategoriesList())
				.values("My Contacts")
			.noMore();

			assertSimpleProperty(vcard.getNotes())
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ")
			.next()
				.value("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).prop(vcard.getCategories(), 2).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V2_1, vcard);
			assertPropertyCount(12, vcard);

			//@formatter:off
			assertStructuredName(vcard)
				.family("\u00d1 \u00d1")
				.given("\u00d1 \u00d1 \u00d1")
			.noMore();

			assertSimpleProperty(vcard.getFormattedNames())
				.value("\u00d1 \u00d1 \u00d1 \u00d1 ")
			.noMore();
			
			assertTelephone(vcard)
				.types(TelephoneType.CELL, TelephoneType.PREF)
				.text("123456")
			.next()
				.types(TelephoneType.WORK)
				.text("123456")
			.next()
				.types(TelephoneType.WORK, TelephoneType.FAX)
				.text("123456")
			.noMore();

			assertEmail(vcard)
				.types(EmailType.PREF, EmailType.WORK)
				.value("bob@company.com")
			.next()
				.types(EmailType.PREF)
				.value("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.noMore();
			
			assertListProperty(vcard.getOrganizations())
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.next()
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.noMore();

			assertSimpleProperty(vcard.getUrls())
				.value("www.company.com")
			.next()
				.value("http://www.company.com")
			.noMore();
			
			assertBinaryProperty(vcard.getPhotos())
				.contentType(ImageType.JPEG)
				.dataLength(876)
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V2_1, vcard);
			assertPropertyCount(8, vcard);

			//@formatter:off
			assertStructuredName(vcard)
				.family("\u00d1\u00d1\u00d1\u00d1")
			.noMore();

			assertSimpleProperty(vcard.getFormattedNames())
				.value("\u00d1\u00d1\u00d1\u00d1")
			.noMore();
			
			assertTelephone(vcard)
				.types(TelephoneType.CELL, TelephoneType.PREF)
				.text("55556666")
			.noMore();

			assertEmail(vcard)
				.types(EmailType.PREF)
				.value("henry@company.com")
			.noMore();
			
			assertListProperty(vcard.getOrganizations())
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.next()
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1" + (char) 65533)
			.next()
				.values("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1")
			.noMore();

			assertListProperty(vcard.getCategoriesList())
				.values("My Contacts")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getCategories(), 2).run();
			assertWarnings(0, reader);
		}

		assertNoMoreVCards(reader);
	}

	@Test
	public void blackBerryVCard() throws Throwable {
		VCardReader reader = read("John_Doe_BLACK_BERRY.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(6, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();

		assertStructuredName(vcard)
			.family("Doe")
			.given("john")
		.noMore();
		
		assertTelephone(vcard)
			.types(TelephoneType.CELL)
			.text("+96123456789")
		.noMore();
		
		assertListProperty(vcard.getOrganizations())
			.values("Acme Solutions")
		.noMore();

		assertSimpleProperty(vcard.getNotes())
			.value("")
		.noMore();
		
		assertBinaryProperty(vcard.getPhotos())
			.dataLength(1674)
		.noMore();
		//@formatter:on

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void evolutionVCard() throws Throwable {
		VCardReader reader = read("John_Doe_EVOLUTION.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(22, vcard);

		//@formatter:off
		//URL
		assertSimpleProperty(vcard.getUrls())
			.value("http://www.ibm.com")
			.param("X-COUCHDB-UUID", "0abc9b8d-0845-47d0-9a91-3db5bb74620d")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
			.types(TelephoneType.CELL)
			.text("905-666-1234")
			.param("X-COUCHDB-UUID", "c2fa1caa-2926-4087-8971-609cfc7354ce")
		.next()
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("905-555-1234")
			.param("X-COUCHDB-UUID", "fbfb2722-4fd8-4dbf-9abd-eeb24072fd8e")
		.noMore();

		//UID
		assertSimpleProperty(vcard.getProperties(Uid.class))
			.value("477343c8e6bf375a9bac1f96a5000837")
		.noMore();

		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Richter, James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();
		
		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Mr. John Richter, James Doe Sr.")
		.noMore();
		
		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Johny")
		.noMore();

		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("IBM", "Accounting", "Dungeon")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();

		//CATEGORIES
		assertListProperty(vcard.getCategoriesList())
			.values("VIP")
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.")
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.WORK)
			.value("john.doe@ibm.com")
			.param("X-COUCHDB-UUID", "83a75a5d-2777-45aa-bab5-76a4bd972490")
		.noMore();
		
		//ADR
		assertAddress(vcard)
			.poBox("ASB-123")
			.streetAddress("15 Crescent moon drive")
			.locality("Albaney")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.types(AddressType.HOME)
		.noMore();
		
		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-03-22")
		.noMore();
		
		//REV
		assertSimpleProperty(vcard.getProperties(Revision.class))
			.value(utc("2012-03-05 13:32:54"))
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-COUCHDB-APPLICATION-ANNOTATIONS", vcard)
				.value("{\"Evolution\":{\"revision\":\"2012-03-05T13:32:54Z\"}}")
			.noMore();
			
			assertRawProperty("X-AIM", vcard)
				.value("johnny5@aol.com")
				.param("TYPE", "HOME")
				.param("X-COUCHDB-UUID", "cb9e11fc-bb97-4222-9cd8-99820c1de454")
			.noMore();
			
			assertRawProperty("X-EVOLUTION-FILE-AS", vcard)
				.value("Doe\\, John")
			.noMore();
			
			assertRawProperty("X-EVOLUTION-SPOUSE", vcard)
				.value("Maria")
			.noMore();
			
			assertRawProperty("X-EVOLUTION-MANAGER", vcard)
				.value("Big Blue")
			.noMore();
			
			assertRawProperty("X-EVOLUTION-ASSISTANT", vcard)
				.value("Little Red")
			.noMore();
			
			assertRawProperty("X-EVOLUTION-ANNIVERSARY", vcard)
				.value("1980-03-22")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void gmailVCard() throws Throwable {
		VCardReader reader = read("John_Doe_GMAIL.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(17, vcard);

		//@formatter:off
		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Mr. John Richter, James Doe Sr.")
		.noMore();

		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Richter, James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.HOME)
			.value("john.doe@ibm.com")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
			.types(TelephoneType.CELL)
			.text("905-555-1234")
		.next()
			.types(TelephoneType.HOME)
			.text("905-666-1234")
		.noMore();
		
		//ADR
		assertAddress(vcard)
			.extendedAddress("Crescent moon drive" + NEWLINE + "555-asd" + NEWLINE + "Nice Area, Albaney, New York 12345" + NEWLINE + "United States of America")
			.types(AddressType.HOME)
		.noMore();
		
		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("IBM")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();
		
		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-03-22")
		.noMore();

		//URL
		assertSimpleProperty(vcard.getUrls())
			.value("http://www.ibm.com")
			.param("TYPE", "WORK")
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue")
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-PHONETIC-FIRST-NAME", vcard)
				.value("Jon")
			.noMore();
			
			assertRawProperty("X-PHONETIC-LAST-NAME", vcard)
				.value("Dow")
			.noMore();
			
			assertRawProperty("X-ABDATE", vcard)
				.group("item1")
				.value("1975-03-01")
			.noMore();
			
			assertRawProperty("X-ABLABEL", vcard)
				.group("item1")
				.value("_$!<Anniversary>!$_")
			.next()
				.group("item2")
				.value("_$!<Spouse>!$_")
			.noMore();
			
			assertRawProperty("X-ABRELATEDNAMES", vcard)
				.group("item2")
				.value("Jenny")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void gmailList() throws Throwable {
		VCardReader reader = read("gmail-list.vcf");

		{
			VCard vcard = reader.readNext();
			assertVersion(V3_0, vcard);
			assertPropertyCount(3, vcard);

			//@formatter:off
			//FN
			assertSimpleProperty(vcard.getFormattedNames())
				.value("Arnold Smith")
			.noMore();

			//N
			assertStructuredName(vcard)
				.family("Smith")
				.given("Arnold")
			.noMore();

			//EMAIL
			assertEmail(vcard)
				.types(EmailType.INTERNET)
				.value("asmithk@gmail.com")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V3_0, vcard);
			assertPropertyCount(3, vcard);

			//@formatter:off
			//FN
			assertSimpleProperty(vcard.getFormattedNames())
				.value("Chris Beatle")
			.noMore();

			//N
			assertStructuredName(vcard)
				.family("Beatle")
				.given("Chris")
			.noMore();

			//EMAIL
			assertEmail(vcard)
				.types(EmailType.INTERNET)
				.value("chrisy55d@yahoo.com")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V3_0, vcard);
			assertPropertyCount(3, vcard);

			//@formatter:off
			//FN
			assertSimpleProperty(vcard.getFormattedNames())
				.value("Doug White")
			.noMore();

			//N
			assertStructuredName(vcard)
				.family("White")
				.given("Doug")
			.noMore();

			//EMAIL
			assertEmail(vcard)
				.types(EmailType.INTERNET)
				.value("dwhite@gmail.com")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).run();
			assertWarnings(0, reader);
		}

		assertNoMoreVCards(reader);
	}

	@Test
	public void gmailSingle() throws Throwable {
		VCardReader reader = read("gmail-single.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(25, vcard);

		//@formatter:off
		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Greg Dartmouth")
		.noMore();

		//N
		assertStructuredName(vcard)
			.family("Dartmouth")
			.given("Greg")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Gman")
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET)
			.value("gdartmouth@hotmail.com")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
			.types(TelephoneType.CELL)
			.text("555 555 1111")
		.next()
			.group("item1")
			.text("555 555 2222")
		.noMore();
		
		//ADR
		assertAddress(vcard)
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
		
		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("TheCompany")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("TheJobTitle")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1960-09-10")
		.noMore();

		//URL
		assertSimpleProperty(vcard.getUrls())
			.group("item3")
			.value("http://TheProfile.com")
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("This is GMail's note field." + NEWLINE + "It should be added as a NOTE type." + NEWLINE + "ACustomField: CustomField")
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-PHONETIC-FIRST-NAME", vcard)
				.value("Grregg")
			.noMore();
			
			assertRawProperty("X-PHONETIC-LAST-NAME", vcard)
				.value("Dart-mowth")
			.noMore();
			
			assertRawProperty("X-ICQ", vcard)
				.value("123456789")
			.noMore();
			
			assertRawProperty("X-ABLABEL", vcard)
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
			
			assertRawProperty("X-ABDATE", vcard)
				.group("item4")
				.value("1970-06-02")
			.noMore();
			
			assertRawProperty("X-ABRELATEDNAMES", vcard)
				.group("item5")
				.value("MySpouse")
			.next()
				.group("item6")
				.value("MyCustom")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void iPhoneVCard() throws Throwable {
		VCardReader reader = read("John_Doe_IPHONE.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(23, vcard);

		//@formatter:off
		//PRODID
		assertSimpleProperty(vcard.getProperties(ProductId.class))
			.value("-//Apple Inc.//iOS 5.0.1//EN")
		.noMore();

		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Richter", "James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Mr. John Richter James Doe Sr.")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Johny")
		.noMore();

		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("IBM", "Accounting")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.group("item1")
			.types(EmailType.INTERNET, EmailType.PREF)
			.value("john.doe@ibm.com")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
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
		
		//ADR
		assertAddress(vcard)
			.group("item3")
			.streetAddress("Silicon Alley 5")
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

		//URL
		assertSimpleProperty(vcard.getUrls())
			.group("item5")
			.value("http://www.ibm.com")
			.param("TYPE", "pref")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("2012-06-06")
		.noMore();
		
		//PHOTO
		assertBinaryProperty(vcard.getPhotos())
			.contentType(ImageType.JPEG)
			.dataLength(32531)
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-ABLABEL", vcard)
				.group("item2")
				.value("_$!<AssistantPhone>!$_")
			.next()
				.group("item5")
				.value("_$!<HomePage>!$_")
			.noMore();
			
			assertRawProperty("X-ABADR", vcard)
				.group("item3")
				.value("Silicon Alley")
			.next()
				.group("item4")
				.value("Street 4, Building 6,\\n Floor 8\\nNew York\\nUSA")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void lotusNotesVCard() throws Throwable {
		VCardReader reader = read("John_Doe_LOTUS_NOTES.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(30, vcard);

		//@formatter:off
		//PRODID
		assertSimpleProperty(vcard.getProperties(ProductId.class))
			.value("-//Apple Inc.//Address Book 6.1//EN")
		.noMore();

		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Johny")
			.prefixes("Mr.")
			.suffixes("I")
		.noMore();
		
		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Mr. Doe John I Johny")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Johny,JayJay")
		.noMore();

		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("IBM", "SUN")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("Generic Accountant")
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.WORK, EmailType.PREF)
			.value("john.doe@ibm.com")
		.next()
			.types(EmailType.INTERNET, EmailType.WORK)
			.value("billy_bob@gmail.com")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
			.types(TelephoneType.CELL, TelephoneType.VOICE, TelephoneType.PREF)
			.text("+1 (212) 204-34456")
		.next()
			.types(TelephoneType.WORK, TelephoneType.FAX)
			.text("00-1-212-555-7777")
		.noMore();
		
		//ADR
		assertAddress(vcard)
			.group("item1")
			.streetAddress("25334" + NEWLINE + "South cresent drive, Building 5, 3rd floo r")
			.locality("New York")
			.region("New York")
			.postalCode("NYC887")
			.country("U.S.A.")
			.types(AddressType.HOME, AddressType.PREF)
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"" + NEWLINE + "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO , THE" + NEWLINE + "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR P URPOSE" + NEWLINE + "ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTOR S BE" + NEWLINE + "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR" + NEWLINE + "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF" + NEWLINE + " SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS " + NEWLINE + "INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN" + NEWLINE + " CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)" + NEWLINE + "A RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE" + NEWLINE + " POSSIBILITY OF SUCH DAMAGE.")
		.noMore();

		//URL
		assertSimpleProperty(vcard.getUrls())
			.group("item2")
			.value("http://www.sun.com")
			.param("TYPE", "pref")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-05-21")
		.noMore();
		
		//PHOTO
		assertBinaryProperty(vcard.getPhotos())
			.contentType(ImageType.JPEG)
			.dataLength(7957)
		.noMore();

		//UID
		assertSimpleProperty(vcard.getProperties(Uid.class))
			.value("0e7602cc-443e-4b82-b4b1-90f62f99a199")
		.noMore();
		
		//GEO
		assertGeo(vcard)
			.latitude(-2.6)
			.longitude(3.4)
		.noMore();

		//CLASS
		assertSimpleProperty(vcard.getProperties(Classification.class))
			.value("Public")
		.noMore();

		//PROFILE
		assertSimpleProperty(vcard.getProperties(Profile.class))
			.value("VCard")
		.noMore();

		//TZ
		assertTimezone(vcard)
			.offset(new UtcOffset(true, 1, 0))
		.noMore();

		//LABEL
		assertSimpleProperty(vcard.getOrphanedLabels())
			.value("John Doe" + NEWLINE + "New York, NewYork," + NEWLINE + "South Crecent Dr ive," + NEWLINE + "Building 5, floor 3," + NEWLINE + "USA")
			.param("TYPE", "HOME")
			.param("TYPE", "PARCEL")
			.param("TYPE", "PREF")
		.noMore();

		//SORT-STRING
		assertSimpleProperty(vcard.getProperties(SortString.class))
			.value("JOHN")
		.noMore();

		//ROLE
		assertSimpleProperty(vcard.getRoles())
			.value("Counting Money")
		.noMore();

		//SOURCE
		assertSimpleProperty(vcard.getSources())
			.value("Whatever")
		.noMore();

		//MAILER
		assertSimpleProperty(vcard.getProperties(Mailer.class))
			.value("Mozilla Thunderbird")
		.noMore();

		//NAME
		assertSimpleProperty(vcard.getProperties(SourceDisplayText.class))
			.value("VCard for John Doe")
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-ABLABEL", vcard)
				.group("item2")
				.value("_$!<HomePage>!$_")
			.noMore();
			
			assertRawProperty("X-ABUID", vcard)
				.value("0E7602CC-443E-4B82-B4B1-90F62F99A199:ABPerson")
			.noMore();
			
			assertRawProperty("X-GENERATOR", vcard)
				.value("Cardme Generator")
			.noMore();
			
			assertRawProperty("X-LONG-STRING", vcard)
				.value("12345678901234567890123456789012345678901234567890123456789012 34567890123456789012345678901234567890")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).prop(vcard.getEmails().get(1), 9).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void msOutlookVCard() throws Throwable {
		VCardReader reader = read("John_Doe_MS_OUTLOOK.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(22, vcard);

		//@formatter:off
		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Richter", "James")
			.prefixes("Mr.")
			.suffixes("Sr.")
			.param(VCardParameters.LANGUAGE, "en-us")
		.noMore();

		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Mr. John Richter James Doe Sr.")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Johny")
		.noMore();

		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("IBM", "Accounting")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("(905) 555-1234")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("(905) 666-1234")
		.noMore();
		
		//ADR
		assertAddress(vcard)
			.streetAddress("Cresent moon drive")
			.locality("Albaney")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.label("Cresent moon drive\r\nAlbaney, New York  12345")
			.types(AddressType.WORK, AddressType.PREF)
		.next()
			.streetAddress("Silicon Alley 5")
			.locality("New York")
			.region("New York")
			.postalCode("12345")
			.country("United States of America")
			.label("Silicon Alley 5,\r\nNew York, New York  12345")
			.types(AddressType.HOME)
		.noMore();

		//URL
		assertSimpleProperty(vcard.getUrls())
			.value("http://www.ibm.com")
			.param("TYPE", "WORK")
		.noMore();

		//ROLE
		assertSimpleProperty(vcard.getRoles())
			.value("Counting Money")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-03-22")
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.PREF)
			.value("john.doe@ibm.cm")
		.noMore();
		
		//PHOTO
		assertBinaryProperty(vcard.getPhotos())
			.contentType(ImageType.JPEG)
			.dataLength(860)
		.noMore();
		
		//REV
		assertSimpleProperty(vcard.getProperties(Revision.class))
			.value(utc("2012-03-05 13:19:33"))
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-MS-OL-DEFAULT-POSTAL-ADDRESS", vcard)
				.value("2")
			.noMore();
			
			assertRawProperty("X-MS-ANNIVERSARY", vcard)
				.value("20110113")
			.noMore();
			
			assertRawProperty("X-MS-IMADDRESS", vcard)
				.value("johny5@aol.com")
			.noMore();
			
			assertRawProperty("X-MS-OL-DESIGN", vcard)
				.value("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>")
				.param("CHARSET", "utf-8")
			.noMore();
			
			assertRawProperty("X-MS-MANAGER", vcard)
				.value("Big Blue")
			.noMore();
			
			assertRawProperty("X-MS-ASSISTANT", vcard)
				.value("Jenny")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getNickname(), 2).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void outlook2007VCard() throws Throwable {
		VCardReader reader = read("outlook-2007.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(28, vcard);

		//@formatter:off
		//N
		assertStructuredName(vcard)
			.family("Angstadt")
			.given("Michael")
			.prefixes("Mr.")
			.suffixes("Jr.")
			.param(VCardParameters.LANGUAGE, "en-us")
		.noMore();

		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Mr. Michael Angstadt Jr.")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Mike")
		.noMore();

		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("TheCompany", "TheDepartment")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("TheJobTitle")
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("This is the NOTE field	\r\nI assume it encodes this text inside a NOTE vCard type.\r\nBut I'm not sure because there's text formatting going on here.\r\nIt does not preserve the formatting")
			.param("CHARSET", "us-ascii")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("(111) 555-1111")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("(111) 555-2222")
		.next()
			.types(TelephoneType.CELL, TelephoneType.VOICE)
			.text("(111) 555-4444")
		.next()
			.types(TelephoneType.FAX, TelephoneType.WORK)
			.text("(111) 555-3333")
		.noMore();
		
		//ADR
		assertAddress(vcard)
			.extendedAddress("TheOffice")
			.streetAddress("222 Broadway")
			.locality("New York")
			.region("NY")
			.postalCode("99999")
			.country("USA")
			.label("222 Broadway\r\nNew York, NY 99999\r\nUSA")
			.types(AddressType.WORK, AddressType.PREF)
		.noMore();

		//URL
		assertSimpleProperty(vcard.getUrls())
			.value("http://mikeangstadt.name")
			.param("TYPE", "HOME")
		.next()
			.value("http://mikeangstadt.name")
			.param("TYPE", "WORK")
		.noMore();

		//ROLE
		assertSimpleProperty(vcard.getRoles())
			.value("TheProfession")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1922-03-10")
		.noMore();
		
		//KEY
		assertBinaryProperty(vcard.getKeys())
			.contentType(KeyType.X509)
			.dataLength(514)
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.PREF)
			.value("mike.angstadt@gmail.com")
		.noMore();
			
		//PHOTO
		assertBinaryProperty(vcard.getPhotos())
			.contentType(ImageType.JPEG)
			.dataLength(2324)
		.noMore();

		//FBURL
		assertSimpleProperty(vcard.getFbUrls())
			.value("http://website.com/mycal") //a 4.0 property in a 2.1 vCard...
		.noMore();
		
		//REV
		assertSimpleProperty(vcard.getProperties(Revision.class))
			.value(utc("2012-08-01 18:46:31"))
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-MS-TEL", vcard)
				.value("(111) 555-4444")
				.param("TYPE", "VOICE")
				.param("TYPE", "CALLBACK")
			.noMore();
			
			assertRawProperty("X-MS-OL-DEFAULT-POSTAL-ADDRESS", vcard)
				.value("2")
			.noMore();
			
			assertRawProperty("X-MS-ANNIVERSARY", vcard)
				.value("20120801")
			.noMore();
			
			assertRawProperty("X-MS-IMADDRESS", vcard)
				.value("im@aim.com")
			.noMore();
			
			assertRawProperty("X-MS-OL-DESIGN", vcard)
				.value("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telcell\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Mobile</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>")
				.param("CHARSET", "utf-8")
			.noMore();
			
			assertRawProperty("X-MS-MANAGER", vcard)
				.value("TheManagerName")
			.noMore();
			
			assertRawProperty("X-MS-ASSISTANT", vcard)
				.value("TheAssistantName")
			.noMore();
			
			assertRawProperty("X-MS-SPOUSE", vcard)
				.value("TheSpouse")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getNickname(), 2).prop(vcard.getFbUrls().get(0), 2).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void macAddressBookVCard() throws Throwable {
		VCardReader reader = read("John_Doe_MAC_ADDRESS_BOOK.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(28, vcard);

		//@formatter:off
		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Richter,James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Mr. John Richter,James Doe Sr.")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Johny")
		.noMore();

		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("IBM", "Accounting")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.WORK, EmailType.PREF)
			.value("john.doe@ibm.com")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
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
		
		//ADR
		assertAddress(vcard)
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

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue")
		.noMore();

		//URL
		assertSimpleProperty(vcard.getUrls())
			.group("item4")
			.value("http://www.ibm.com")
			.param("TYPE", "pref")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("2012-06-06")
		.noMore();
		
		//PHOTO
		assertBinaryProperty(vcard.getPhotos())
			.dataLength(18242)
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-PHONETIC-FIRST-NAME", vcard)
				.value("Jon")
			.noMore();
			
			assertRawProperty("X-PHONETIC-LAST-NAME", vcard)
				.value("Dow")
			.noMore();
			
			assertRawProperty("X-ABLABEL", vcard)
				.group("item1")
				.value("AssistantPhone")
			.next()
				.group("item4")
				.value("_$!<HomePage>!$_")
			.next()
				.group("item5")
				.value("Spouse")
			.noMore();
			
			assertRawProperty("X-ABADR", vcard)
				.group("item2")
				.value("Silicon Alley")
			.next()
				.group("item3")
				.value("Street 4, Building 6,\\nFloor 8\\nNew York\\nUSA")
			.noMore();
			
			assertRawProperty("X-ABRELATEDNAMES", vcard)
				.group("item5")
				.value("Jenny")
				.param("TYPE",  "pref")
			.noMore();
			
			assertRawProperty("X-ABUID", vcard)
				.value("6B29A774-D124-4822-B8D0-2780EC117F60\\:ABPerson")
			.noMore();
			//@formatter:on
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).prop(vcard.getPhotos().get(0), 4).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void outlook2003VCard() throws Throwable {
		VCardReader reader = read("outlook-2003.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(18, vcard);

		//@formatter:off
		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.prefixes("Mr.")
			.suffixes("III")
		.noMore();

		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe III")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Joey")
		.noMore();

		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("Company, The", "TheDepartment")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("The Job Title")
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("This is the note field!!\r\nSecond line\r\n\r\nThird line is empty\r\n")
		.noMore();
		
		//TEL
		assertTelephone(vcard)
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.text("BusinessPhone")
		.next()
			.types(TelephoneType.HOME, TelephoneType.VOICE)
			.text("HomePhone")
		.next()
			.types(TelephoneType.CELL, TelephoneType.VOICE)
			.text("MobilePhone")
		.next()
			.types(TelephoneType.FAX, TelephoneType.WORK)
			.text("BusinessFaxPhone")
		.noMore();
		
		//ADR
		assertAddress(vcard)
			.extendedAddress("TheOffice")
			.streetAddress("123 Main St")
			.locality("Austin")
			.region("TX")
			.postalCode("12345")
			.country("United States of America")
			.label("TheOffice\r\n123 Main St\r\nAustin, TX 12345\r\nUnited States of America")
			.types(AddressType.WORK)
		.noMore();

		//URL
		assertSimpleProperty(vcard.getUrls())
			.value("http://web-page-address.com")
			.param("TYPE", "WORK")
		.noMore();

		//ROLE
		assertSimpleProperty(vcard.getRoles())
			.value("TheProfession")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-03-21")
		.noMore();
		
		//KEY
		assertBinaryProperty(vcard.getKeys())
			.contentType(KeyType.X509)
			.dataLength(805)
		.noMore();

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.PREF)
			.value("jdoe@hotmail.com")
		.noMore();

		//FBURL
		//Outlook 2003 apparently doesn't output FBURL correctly:
		//http://help.lockergnome.com/office/BUG-Outlook-2003-exports-FBURL-vCard-incorrectly--ftopict423660.html
		assertSimpleProperty(vcard.getFbUrls())
			.value("????????????????s????????????" + (char) 12)
		.noMore();
		
		//REV
		assertSimpleProperty(vcard.getProperties(Revision.class))
			.value(utc("2012-10-12 21:05:25"))
		.noMore();
		//@formatter:on

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getNickname(), 2).prop(vcard.getFbUrls().get(0), 2).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void thunderbird() throws Throwable {
		VCardReader reader = read("thunderbird-MoreFunctionsForAddressBook-extension.vcf");
		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(25, vcard);

		//@formatter:off
		//N
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
		.noMore();

		//FN
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();
		
		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("TheOrganization", "TheDepartment")
		.noMore();

		//NICKNAME
		assertListProperty(vcard.getNicknames())
			.values("Johnny")
		.noMore();

		//ADR
		assertAddress(vcard)
			.extendedAddress("222 Broadway")
			.streetAddress("Suite 100")
			.locality("New York")
			.region("NY")
			.postalCode("98765")
			.country("USA")
			.types(AddressType.WORK, AddressType.POSTAL)
		.next()
			.extendedAddress("123 Main St")
			.streetAddress("Apt 10")
			.locality("Austin")
			.region("TX")
			.postalCode("12345")
			.country("USA")
			.types(AddressType.HOME, AddressType.POSTAL)
		.noMore();

		//TEL
		assertTelephone(vcard)
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

		//EMAIL
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.PREF)
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

		//URL
		assertSimpleProperty(vcard.getUrls())
			.value("http://www.private-webpage.com")
			.param("TYPE", "HOME")
		.next()
			.value("http://www.work-webpage.com")
			.param("TYPE", "WORK")
		.noMore();

		//TITLE
		assertSimpleProperty(vcard.getTitles())
			.value("TheTitle")
		.noMore();

		//CATEGORIES
		assertListProperty(vcard.getCategoriesList())
			.values("category1, category2, category3") //commas are incorrectly escaped, so there is only 1 item
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1970-09-21")
		.noMore();

		//NOTE
		assertSimpleProperty(vcard.getNotes())
			.value("This is the notes field." + NEWLINE + "Second Line" + NEWLINE + NEWLINE + "Fourth Line" + NEWLINE + "You can put anything in the \"note\" field; even curse words.")
		.noMore();
		
		//PHOTO
		assertBinaryProperty(vcard.getPhotos())
			.contentType(ImageType.JPEG)
			.dataLength(8940)
		.noMore();
		//@formatter:on

		//extended properties
		{
			//@formatter:off
			assertRawProperty("X-SPOUSE", vcard)
				.value("TheSpouse")
			.noMore();
			
			assertRawProperty("X-ANNIVERSARY", vcard)
				.value("1990-04-30")
			.noMore();
			//@formatter:on
		}

		//@formatter:off
		assertValidate(vcard)
		.versions(vcard.getVersion())
		.prop(vcard.getStructuredName(), 6)
		.prop(vcard.getFormattedName(), 6)
		.prop(vcard.getOrganization(), 6)
		.prop(vcard.getNickname(), 6)
		.prop(vcard.getAddresses().get(0), 6)
		.prop(vcard.getAddresses().get(1), 6)
		.prop(vcard.getTitles().get(0), 6)
		.prop(vcard.getCategories(), 6)
		.prop(vcard.getNotes().get(0), 6)
		.run();
		//@formatter:on
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void rfc6350_example() throws Throwable {
		VCardReader reader = read("rfc6350-example.vcf");

		VCard vcard = reader.readNext();
		assertVersion(V4_0, vcard);
		assertPropertyCount(16, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Simon Perreault")
		.noMore();
		
		assertStructuredName(vcard)
			.family("Perreault")
			.given("Simon")
			.suffixes("ing. jr", "M.Sc.")
		.noMore();
		
		assertDateProperty(vcard.getBirthdays())
			.partialDate(PartialDate.builder().month(2).date(3).build())
		.noMore();
		
		assertDateProperty(vcard.getAnniversaries())
			.partialDate(PartialDate.builder().year(2009).month(8).date(8).hour(14).minute(30).offset(new UtcOffset(false, -5, 0)).build())
		.noMore();

		assertTrue(vcard.getGender().isMale());

		assertSimpleProperty(vcard.getLanguages())
			.value("fr")
			.param("PREF", "1")
		.next()
			.value("en")
			.param("PREF", "2")
		.noMore();
		
		assertAddress(vcard)
			.extendedAddress("Suite D2-630")
			.streetAddress("2875 Laurier")
			.locality("Quebec")
			.region("QC")
			.postalCode("G1V 2M2")
			.country("Canada")
			.types(AddressType.WORK)
		.noMore();

		assertTelephone(vcard)
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.uri(new TelUri.Builder("+1-418-656-9254").extension("102").build())
			.param("PREF", "1")
		.next()
			.types(TelephoneType.WORK, TelephoneType.VOICE, TelephoneType.CELL, TelephoneType.VIDEO, TelephoneType.TEXT)
			.uri(new TelUri.Builder("+1-418-262-6501").build())
		.noMore();

		assertEmail(vcard)
			.types(EmailType.WORK)
			.value("simon.perreault@viagenie.ca")
		.noMore();
		
		assertGeo(vcard)
			.latitude(46.772673)
			.longitude(-71.282945)
			.param("TYPE", "work")
		.noMore();
		
		assertBinaryProperty(vcard.getKeys())
			.url("http://www.viagenie.ca/simon.perreault/simon.asc")
			.param("TYPE", "work")
		.noMore();
		
		assertTimezone(vcard)
			.offset(new UtcOffset(false, -5, 0))
		.noMore();

		assertSimpleProperty(vcard.getUrls())
			.value("http://nomis80.org")
			.param("TYPE", "home")
		.noMore();
		//@formatter:on

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void rfc2426_example() throws Throwable {
		VCardReader reader = read("rfc2426-example.vcf");

		{
			VCard vcard = reader.readNext();
			assertVersion(V3_0, vcard);
			assertPropertyCount(8, vcard);

			//@formatter:off
			assertSimpleProperty(vcard.getFormattedNames())
				.value("Frank Dawson")
			.noMore();
			
			assertListProperty(vcard.getOrganizations())
				.values("Lotus Development Corporation")
			.noMore();

			assertAddress(vcard)
				.streetAddress("6544 Battleford Drive")
				.locality("Raleigh")
				.region("NC")
				.postalCode("27613-3502")
				.country("U.S.A.")
				.types(AddressType.WORK, AddressType.POSTAL, AddressType.PARCEL)
			.noMore();

			assertTelephone(vcard)
				.types(TelephoneType.WORK, TelephoneType.VOICE, TelephoneType.MSG)
				.text("+1-919-676-9515")
			.next()
				.types(TelephoneType.WORK, TelephoneType.FAX)
				.text("+1-919-676-9564")
			.noMore();

			assertEmail(vcard)
				.types(EmailType.INTERNET, EmailType.PREF)
				.value("Frank_Dawson@Lotus.com")
			.next()
				.types(EmailType.INTERNET)
				.value("fdawson@earthlink.net")
			.noMore();

			assertSimpleProperty(vcard.getUrls())
				.value("http://home.earthlink.net/~fdawson")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).run();
			assertWarnings(0, reader);
		}

		{
			VCard vcard = reader.readNext();
			assertVersion(V3_0, vcard);
			assertPropertyCount(6, vcard);

			//@formatter:off
			assertSimpleProperty(vcard.getFormattedNames())
				.value("Tim Howes")
			.noMore();
			
			assertListProperty(vcard.getOrganizations())
				.values("Netscape Communications Corp.")
			.noMore();

			assertAddress(vcard)
				.streetAddress("501 E. Middlefield Rd.")
				.locality("Mountain View")
				.region("CA")
				.postalCode("94043")
				.country("U.S.A.")
				.types(AddressType.WORK)
			.noMore();

			assertTelephone(vcard)
				.types(TelephoneType.WORK, TelephoneType.VOICE, TelephoneType.MSG)
				.text("+1-415-937-3419")
			.next()
				.types(TelephoneType.WORK, TelephoneType.FAX)
				.text("+1-415-528-4164")
			.noMore();

			assertEmail(vcard)
				.types(EmailType.INTERNET)
				.value("howes@netscape.com")
			.noMore();
			//@formatter:on

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).run();
			assertWarnings(0, reader);
		}

		assertNoMoreVCards(reader);
	}

	private static VCardReader read(String filename) {
		return new VCardReader(SampleVCardsTest.class.getResourceAsStream(filename));
	}
}
