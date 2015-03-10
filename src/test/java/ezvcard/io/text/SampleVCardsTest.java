package ezvcard.io.text;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.assertWarnings;
import static ezvcard.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.KeyType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Classification;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.property.Email;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.ListProperty;
import ezvcard.property.Mailer;
import ezvcard.property.Photo;
import ezvcard.property.ProductId;
import ezvcard.property.Profile;
import ezvcard.property.RawProperty;
import ezvcard.property.Revision;
import ezvcard.property.SortString;
import ezvcard.property.SourceDisplayText;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.TextProperty;
import ezvcard.property.Timezone;
import ezvcard.property.Uid;
import ezvcard.property.VCardProperty;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.TestUtils;
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

			assertTextProperty(vcard.getFormattedNames())
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

			assertTextProperty(vcard.getFormattedNames())
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

			assertTextProperty(vcard.getNotes())
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

			assertTextProperty(vcard.getFormattedNames())
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

			assertTextProperty(vcard.getUrls())
				.value("www.company.com")
			.next()
				.value("http://www.company.com")
			.noMore();
			//@formatter:on

			assertEquals(1, vcard.getPhotos().size());

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

			assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getFormattedNames())
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

		assertTextProperty(vcard.getNotes())
			.value("")
		.noMore();
		//@formatter:on

		assertEquals(1, vcard.getPhotos().size());

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
		assertTextProperty(vcard.getUrls())
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
		assertTextProperty(vcard.getProperties(Uid.class))
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
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();

		//CATEGORIES
		assertListProperty(vcard.getCategoriesList())
			.values("VIP")
		.noMore();

		//NOTE
		assertTextProperty(vcard.getNotes())
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
			.country("UnitedStates of America") //the space between "United" and "States" is lost because it was included with the folding character and ignored (see .vcf file)
			.types(AddressType.HOME)
		.noMore();
		
		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-03-22")
		.noMore();
		//@formatter:on

		//REV
		{
			Revision t = vcard.getRevision();
			assertEquals(utc("2012-03-05 13:32:54"), t.getValue());
		}

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
		assertTextProperty(vcard.getFormattedNames())
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
			.extendedAddress("Crescent moon drive" + NEWLINE + "555-asd" + NEWLINE + "Nice Area, Albaney, New York12345" + NEWLINE + "United States of America")
			.types(AddressType.HOME)
		.noMore();
		
		//ORG
		assertListProperty(vcard.getOrganizations())
			.values("IBM")
		.noMore();

		//TITLE
		assertTextProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();
		
		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-03-22")
		.noMore();

		//URL
		assertTextProperty(vcard.getUrls())
			.value("http://www.ibm.com")
			.param("TYPE", "WORK")
		.noMore();

		//NOTE
		assertTextProperty(vcard.getNotes())
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

			//FN
			//@formatter:off
			assertTextProperty(vcard.getFormattedNames())
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

			//FN
			//@formatter:off
			assertTextProperty(vcard.getFormattedNames())
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

			//FN
			//@formatter:off
			assertTextProperty(vcard.getFormattedNames())
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

		//FN
		//@formatter:off
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
			.value("TheJobTitle")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1960-09-10")
		.noMore();

		//URL
		assertTextProperty(vcard.getUrls())
			.group("item3")
			.value("http://TheProfile.com")
		.noMore();

		//NOTE
		assertTextProperty(vcard.getNotes())
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

		//PRODID
		//@formatter:off
		assertTextProperty(vcard.getProperties(ProductId.class))
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
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
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
		assertTextProperty(vcard.getUrls())
			.group("item5")
			.value("http://www.ibm.com")
			.param("TYPE", "pref")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("2012-06-06")
		.noMore();
		//@formatter:on

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(32531, f.getData().length);
		}

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

		//PRODID
		//@formatter:off
		assertTextProperty(vcard.getProperties(ProductId.class))
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
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
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
		assertTextProperty(vcard.getNotes())
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"" + NEWLINE + "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO , THE" + NEWLINE + "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR P URPOSE" + NEWLINE + "ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTOR S BE" + NEWLINE + "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR" + NEWLINE + "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF" + NEWLINE + " SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS " + NEWLINE + "INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN" + NEWLINE + " CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)" + NEWLINE + "A RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE" + NEWLINE + " POSSIBILITY OF SUCH DAMAGE.")
		.noMore();

		//URL
		assertTextProperty(vcard.getUrls())
			.group("item2")
			.value("http://www.sun.com")
			.param("TYPE", "pref")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-05-21")
		.noMore();
		//@formatter:on

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(7957, f.getData().length);

			assertFalse(it.hasNext());
		}

		//UID
		//@formatter:off
		assertTextProperty(vcard.getProperties(Uid.class))
			.value("0e7602cc-443e-4b82-b4b1-90f62f99a199")
		.noMore();
		//@formatter:on

		//GEO
		{
			Geo f = vcard.getGeo();
			assertEquals(-2.6, f.getLatitude(), .01);
			assertEquals(3.4, f.getLongitude(), .01);
		}

		//CLASS
		//@formatter:off
		assertTextProperty(vcard.getProperties(Classification.class))
			.value("Public")
		.noMore();

		//PROFILE
		assertTextProperty(vcard.getProperties(Profile.class))
			.value("VCard")
		.noMore();
		//@formatter:on

		//TZ
		{
			Timezone f = vcard.getTimezone();
			assertEquals(new UtcOffset(true, 1, 0), f.getOffset());
		}

		//LABEL
		//@formatter:off
		assertTextProperty(vcard.getOrphanedLabels())
			.value("John Doe" + NEWLINE + "New York, NewYork," + NEWLINE + "South Crecent Drive," + NEWLINE + "Building 5, floor 3," + NEWLINE + "USA")
			.param("TYPE", "HOME")
			.param("TYPE", "PARCEL")
			.param("TYPE", "PREF")
		.noMore();

		//SORT-STRING
		assertTextProperty(vcard.getProperties(SortString.class))
			.value("JOHN")
		.noMore();

		//ROLE
		assertTextProperty(vcard.getRoles())
			.value("Counting Money")
		.noMore();

		//SOURCE
		assertTextProperty(vcard.getSources())
			.value("Whatever")
		.noMore();

		//MAILER
		assertTextProperty(vcard.getProperties(Mailer.class))
			.value("Mozilla Thunderbird")
		.noMore();

		//NAME
		assertTextProperty(vcard.getProperties(SourceDisplayText.class))
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
				.value("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
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

		//N
		//@formatter:off
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Richter", "James")
			.prefixes("Mr.")
			.suffixes("Sr.")
			.param(VCardParameters.LANGUAGE, "en-us")
		.noMore();

		//FN
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
			.value("Money Counter")
		.noMore();

		//NOTE
		assertTextProperty(vcard.getNotes())
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
		assertTextProperty(vcard.getUrls())
			.value("http://www.ibm.com")
			.param("TYPE", "WORK")
		.noMore();

		//ROLE
		assertTextProperty(vcard.getRoles())
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
		//@formatter:on

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(860, f.getData().length);

			assertFalse(it.hasNext());
		}

		//REV
		{
			Revision f = vcard.getRevision();
			assertEquals(utc("2012-03-05 13:19:33"), f.getValue());
		}

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

		//N
		//@formatter:off
		assertStructuredName(vcard)
			.family("Angstadt")
			.given("Michael")
			.prefixes("Mr.")
			.suffixes("Jr.")
			.param(VCardParameters.LANGUAGE, "en-us")
		.noMore();

		//FN
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
			.value("TheJobTitle")
		.noMore();

		//NOTE
		assertTextProperty(vcard.getNotes())
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
		assertTextProperty(vcard.getUrls())
			.value("http://mikeangstadt.name")
			.param("TYPE", "HOME")
		.next()
			.value("http://mikeangstadt.name")
			.param("TYPE", "WORK")
		.noMore();

		//ROLE
		assertTextProperty(vcard.getRoles())
			.value("TheProfession")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1922-03-10")
		.noMore();
		//@formatter:on

		//KEY
		{
			Iterator<Key> it = vcard.getKeys().iterator();

			Key f = it.next();
			assertEquals(KeyType.X509, f.getContentType());
			assertEquals(514, f.getData().length);

			assertFalse(it.hasNext());
		}

		//EMAIL
		//@formatter:off
			assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.PREF)
			.value("mike.angstadt@gmail.com")
		.noMore();
		//@formatter:on

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(2324, f.getData().length);

			assertFalse(it.hasNext());
		}

		//FBURL
		//@formatter:off
		assertTextProperty(vcard.getFbUrls())
			.value("http://website.com/mycal") //a 4.0 property in a 2.1 vCard...
		.noMore();
		//@formatter:on

		//REV
		{
			Revision f = vcard.getRevision();
			assertEquals(utc("2012-08-01 18:46:31"), f.getValue());
		}

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

		//N
		//@formatter:off
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.additional("Richter,James")
			.prefixes("Mr.")
			.suffixes("Sr.")
		.noMore();

		//FN
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
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
		assertTextProperty(vcard.getNotes())
			.value("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue")
		.noMore();

		//URL
		assertTextProperty(vcard.getUrls())
			.group("item4")
			.value("http://www.ibm.com")
			.param("TYPE", "pref")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("2012-06-06")
		.noMore();
		//@formatter:on

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(null, f.getContentType());
			assertEquals(18242, f.getData().length);

			assertFalse(it.hasNext());
		}

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

		//N
		//@formatter:off
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
			.prefixes("Mr.")
			.suffixes("III")
		.noMore();

		//FN
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getTitles())
			.value("The Job Title")
		.noMore();

		//NOTE
		assertTextProperty(vcard.getNotes())
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
		assertTextProperty(vcard.getUrls())
			.value("http://web-page-address.com")
			.param("TYPE", "WORK")
		.noMore();

		//ROLE
		assertTextProperty(vcard.getRoles())
			.value("TheProfession")
		.noMore();

		//BDAY
		assertDateProperty(vcard.getBirthdays())
			.date("1980-03-21")
		.noMore();
		//@formatter:on

		//KEY
		{
			Iterator<Key> it = vcard.getKeys().iterator();

			Key f = it.next();
			assertEquals(KeyType.X509, f.getContentType());
			assertEquals(805, f.getData().length);

			assertFalse(it.hasNext());
		}

		//EMAIL
		//@formatter:off
		assertEmail(vcard)
			.types(EmailType.INTERNET, EmailType.PREF)
			.value("jdoe@hotmail.com")
		.noMore();

		//FBURL
		//Outlook 2003 apparently doesn't output FBURL correctly:
		//http://help.lockergnome.com/office/BUG-Outlook-2003-exports-FBURL-vCard-incorrectly--ftopict423660.html
		assertTextProperty(vcard.getFbUrls())
			.value("????????????????s????????????" + (char) 12)
		.noMore();
		//@formatter:on

		//REV
		{
			Revision f = vcard.getRevision();
			assertEquals(utc("2012-10-12 21:05:25"), f.getValue());
		}

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

		//N
		//@formatter:off
		assertStructuredName(vcard)
			.family("Doe")
			.given("John")
		.noMore();

		//FN
		assertTextProperty(vcard.getFormattedNames())
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
		assertTextProperty(vcard.getUrls())
			.value("http://www.private-webpage.com")
			.param("TYPE", "HOME")
		.next()
			.value("http://www.work-webpage.com")
			.param("TYPE", "WORK")
		.noMore();

		//TITLE
		assertTextProperty(vcard.getTitles())
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
		assertTextProperty(vcard.getNotes())
			.value("This is the notes field." + NEWLINE + "Second Line" + NEWLINE + NEWLINE + "Fourth Line" + NEWLINE + "You can put anything in the \"note\" field; even curse words.")
		.noMore();
		//@formatter:on

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(8940, f.getData().length);

			assertFalse(it.hasNext());
		}

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
		assertTextProperty(vcard.getFormattedNames())
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

		assertTextProperty(vcard.getLanguages())
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
		//@formatter:on

		Geo geo = vcard.getGeo();
		assertEquals(Double.valueOf(46.772673), geo.getLatitude());
		assertEquals(Double.valueOf(-71.282945), geo.getLongitude());
		assertEquals("work", geo.getType());

		Key key = vcard.getKeys().get(0);
		assertEquals("http://www.viagenie.ca/simon.perreault/simon.asc", key.getUrl());
		assertEquals("work", key.getType());

		Timezone tz = vcard.getTimezone();
		assertEquals(new UtcOffset(false, -5, 0), tz.getOffset());

		//@formatter:off
		assertTextProperty(vcard.getUrls())
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
			assertTextProperty(vcard.getFormattedNames())
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

			assertTextProperty(vcard.getUrls())
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
			assertTextProperty(vcard.getFormattedNames())
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

	private static RawPropertyAsserter assertRawProperty(String name, VCard vcard) {
		return new RawPropertyAsserter(vcard.getExtendedProperties(name), name);
	}

	private static <T extends TextProperty> TextPropertyAsserter<T> assertTextProperty(List<T> list) {
		return new TextPropertyAsserter<T>(list);
	}

	private static <T extends ListProperty<String>> ListPropertyAsserter<T> assertListProperty(List<T> list) {
		return new ListPropertyAsserter<T>(list);
	}

	private static <T extends DateOrTimeProperty> DateOrTimePropertyAsserter<T> assertDateProperty(List<T> list) {
		return new DateOrTimePropertyAsserter<T>(list);
	}

	private static EmailAsserter assertEmail(VCard vcard) {
		return new EmailAsserter(vcard.getEmails());
	}

	private static AddressAsserter assertAddress(VCard vcard) {
		return new AddressAsserter(vcard.getAddresses());
	}

	private static TelephoneAsserter assertTelephone(VCard vcard) {
		return new TelephoneAsserter(vcard.getTelephoneNumbers());
	}

	private static StructuredNameAsserter assertStructuredName(VCard vcard) {
		return new StructuredNameAsserter(vcard.getStructuredNames());
	}

	private static abstract class PropertyAsserter<T, P extends VCardProperty> {
		@SuppressWarnings("unchecked")
		protected final T this_ = (T) this;
		private final Iterator<P> it;

		private String group;
		private VCardParameters parameters;

		public PropertyAsserter(List<P> list) {
			it = list.iterator();
			reset();
		}

		public T group(String group) {
			this.group = group;
			return this_;
		}

		public T param(String name, String value) {
			parameters.put(name, value);
			return this_;
		}

		public T next() {
			P property = it.next();

			assertEquals(group, property.getGroup());
			for (Map.Entry<String, List<String>> entry : parameters) {
				assertEquals(entry.getValue(), property.getParameters(entry.getKey()));
			}

			_run(property);

			reset();
			return this_;
		}

		public void noMore() {
			next();
			assertFalse(it.hasNext());
		}

		private void reset() {
			group = null;
			parameters = new VCardParameters();
			_reset();
		}

		protected abstract void _run(P property);

		protected abstract void _reset();

		protected static <T> List<T> arrayToList(T[] array) {
			return (array == null) ? Collections.<T> emptyList() : Arrays.asList(array);
		}
	}

	private static class RawPropertyAsserter extends PropertyAsserter<RawPropertyAsserter, RawProperty> {
		private final String name;
		private String value;

		public RawPropertyAsserter(List<RawProperty> properties, String name) {
			super(properties);
			this.name = name;
		}

		public RawPropertyAsserter value(String value) {
			this.value = value;
			return this_;
		}

		@Override
		protected void _run(RawProperty property) {
			assertTrue(name.equalsIgnoreCase(property.getPropertyName()));
			assertEquals(value, property.getValue());
		}

		@Override
		protected void _reset() {
			value = null;
		}
	}

	private static class TextPropertyAsserter<T extends TextProperty> extends PropertyAsserter<TextPropertyAsserter<T>, T> {
		private String value;

		public TextPropertyAsserter(List<T> properties) {
			super(properties);
		}

		public TextPropertyAsserter<T> value(String value) {
			this.value = value;
			return this_;
		}

		@Override
		protected void _run(TextProperty property) {
			assertEquals(value, property.getValue());
		}

		@Override
		protected void _reset() {
			value = null;
		}
	}

	private static class EmailAsserter extends PropertyAsserter<EmailAsserter, Email> {
		private EmailType[] types;
		private String value;

		public EmailAsserter(List<Email> emails) {
			super(emails);
		}

		public EmailAsserter types(EmailType... types) {
			this.types = types;
			return this_;
		}

		public EmailAsserter value(String value) {
			this.value = value;
			return this_;
		}

		@Override
		protected void _run(Email property) {
			assertEquals(value, property.getValue());
			assertSetEquals(property.getTypes(), types);
		}

		@Override
		protected void _reset() {
			types = new EmailType[0];
			value = null;
		}
	}

	private static class ListPropertyAsserter<T extends ListProperty<String>> extends PropertyAsserter<ListPropertyAsserter<T>, T> {
		private String[] values;

		public ListPropertyAsserter(List<T> properties) {
			super(properties);
		}

		public ListPropertyAsserter<T> values(String... values) {
			this.values = values;
			return this_;
		}

		@Override
		protected void _run(T property) {
			List<String> expected = (values == null) ? Collections.<String> emptyList() : Arrays.asList(values);
			assertEquals(expected, property.getValues());
		}

		@Override
		protected void _reset() {
			values = null;
		}
	}

	private static class DateOrTimePropertyAsserter<T extends DateOrTimeProperty> extends PropertyAsserter<DateOrTimePropertyAsserter<T>, T> {
		private Date date;
		private PartialDate partialDate;

		public DateOrTimePropertyAsserter(List<T> properties) {
			super(properties);
		}

		public DateOrTimePropertyAsserter<T> date(String dateStr) {
			this.date = TestUtils.date(dateStr);
			return this_;
		}

		public DateOrTimePropertyAsserter<T> partialDate(PartialDate partialDate) {
			this.partialDate = partialDate;
			return this_;
		}

		@Override
		protected void _run(T property) {
			assertEquals(date, property.getDate());
			assertEquals(partialDate, property.getPartialDate());
		}

		@Override
		protected void _reset() {
			date = null;
			partialDate = null;
		}
	}

	private static class StructuredNameAsserter extends PropertyAsserter<StructuredNameAsserter, StructuredName> {
		private String family, given;
		private String[] prefixes, suffixes, additional;

		public StructuredNameAsserter(List<StructuredName> properties) {
			super(properties);
		}

		public StructuredNameAsserter family(String family) {
			this.family = family;
			return this_;
		}

		public StructuredNameAsserter given(String given) {
			this.given = given;
			return this_;
		}

		public StructuredNameAsserter prefixes(String... prefixes) {
			this.prefixes = prefixes;
			return this_;
		}

		public StructuredNameAsserter suffixes(String... suffixes) {
			this.suffixes = suffixes;
			return this_;
		}

		public StructuredNameAsserter additional(String... additional) {
			this.additional = additional;
			return this_;
		}

		@Override
		protected void _run(StructuredName property) {
			assertEquals(family, property.getFamily());
			assertEquals(given, property.getGiven());
			assertEquals(arrayToList(prefixes), property.getPrefixes());
			assertEquals(arrayToList(suffixes), property.getSuffixes());
			assertEquals(arrayToList(additional), property.getAdditional());
		}

		@Override
		protected void _reset() {
			family = given = null;
			prefixes = suffixes = additional = null;
		}
	}

	private static class TelephoneAsserter extends PropertyAsserter<TelephoneAsserter, Telephone> {
		private String text;
		private TelUri uri;
		private TelephoneType[] types;

		public TelephoneAsserter(List<Telephone> properties) {
			super(properties);
		}

		public TelephoneAsserter text(String text) {
			this.text = text;
			return this_;
		}

		public TelephoneAsserter uri(TelUri uri) {
			this.uri = uri;
			return this_;
		}

		public TelephoneAsserter types(TelephoneType... types) {
			this.types = types;
			return this_;
		}

		@Override
		protected void _run(Telephone property) {
			assertEquals(text, property.getText());
			assertEquals(uri, property.getUri());
			assertSetEquals(property.getTypes(), types);
		}

		@Override
		protected void _reset() {
			text = null;
			uri = null;
			types = new TelephoneType[0];
		}
	}

	private static class AddressAsserter extends PropertyAsserter<AddressAsserter, Address> {
		private String poBox, extendedAddress, streetAddress, locality, region, postalCode, country, label;
		private AddressType[] types;

		public AddressAsserter(List<Address> properties) {
			super(properties);
		}

		public AddressAsserter poBox(String poBox) {
			this.poBox = poBox;
			return this_;
		}

		public AddressAsserter extendedAddress(String extendedAddress) {
			this.extendedAddress = extendedAddress;
			return this_;
		}

		public AddressAsserter streetAddress(String streetAddress) {
			this.streetAddress = streetAddress;
			return this_;
		}

		public AddressAsserter locality(String locality) {
			this.locality = locality;
			return this_;
		}

		public AddressAsserter region(String region) {
			this.region = region;
			return this_;
		}

		public AddressAsserter postalCode(String postalCode) {
			this.postalCode = postalCode;
			return this_;
		}

		public AddressAsserter country(String country) {
			this.country = country;
			return this_;
		}

		public AddressAsserter label(String label) {
			this.label = label;
			return this_;
		}

		public AddressAsserter types(AddressType... types) {
			this.types = types;
			return this_;
		}

		@Override
		protected void _run(Address property) {
			assertEquals(poBox, property.getPoBox());
			assertEquals(extendedAddress, property.getExtendedAddress());
			assertEquals(streetAddress, property.getStreetAddress());
			assertEquals(locality, property.getLocality());
			assertEquals(region, property.getRegion());
			assertEquals(postalCode, property.getPostalCode());
			assertEquals(country, property.getCountry());
			assertEquals(label, property.getLabel());
			assertSetEquals(property.getTypes(), types);
		}

		@Override
		protected void _reset() {
			poBox = extendedAddress = streetAddress = locality = region = postalCode = country = label = null;
			types = new AddressType[0];
		}
	}
}
