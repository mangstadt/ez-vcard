package ezvcard.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.SoundTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.BirthdayType;
import ezvcard.types.CategoriesType;
import ezvcard.types.ClassificationType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GeoType;
import ezvcard.types.ImppType;
import ezvcard.types.LogoType;
import ezvcard.types.MailerType;
import ezvcard.types.NicknameType;
import ezvcard.types.NoteType;
import ezvcard.types.OrganizationType;
import ezvcard.types.PhotoType;
import ezvcard.types.RevisionType;
import ezvcard.types.RoleType;
import ezvcard.types.SortStringType;
import ezvcard.types.SoundType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TimezoneType;
import ezvcard.types.TitleType;
import ezvcard.types.UidType;
import ezvcard.types.UrlType;
import freemarker.template.TemplateException;

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
public class HCardPageTest {
	private byte[] mockData = "data".getBytes();

	@Test
	public void zero_vcards() throws Exception {
		Document document = generate();
		assertTrue(document.getElementsByClass("vcard").isEmpty());
	}

	@Test
	public void one_vcard() throws Exception {
		Document document = generate(new VCard());
		assertEquals(1, document.getElementsByClass("vcard").size());
	}

	@Test
	public void multiple_vcards() throws Exception {
		Document document = generate(new VCard(), new VCard());
		assertEquals(2, document.getElementsByClass("vcard").size());
	}

	@Test
	public void prodId() throws Exception {
		Document document = generate(new VCard());
		assertEquals(1, document.select(".vcard .prodid").size());
	}

	@Test
	public void logic_for_displaying_a_photo() throws Exception {
		VCard vcard = new VCard();
		Document document = generate(vcard);
		assertTrue(document.select(".vcard .photo").isEmpty());
		assertTrue(document.select(".vcard .logo").isEmpty());

		vcard = new VCard();
		PhotoType photo = new PhotoType(mockData, ImageTypeParameter.JPEG);
		vcard.addPhoto(photo);
		document = generate(vcard);
		assertEquals(1, document.select(".vcard .photo").size());
		assertTrue(document.select(".vcard .logo").isEmpty());

		vcard = new VCard();
		LogoType logo = new LogoType(mockData, ImageTypeParameter.PNG);
		vcard.addLogo(logo);
		document = generate(vcard);
		assertTrue(document.select(".vcard .photo").isEmpty());
		assertEquals(1, document.select(".vcard .logo").size());

		vcard = new VCard();
		photo = new PhotoType(mockData, ImageTypeParameter.JPEG);
		vcard.addPhoto(photo);
		logo = new LogoType(mockData, ImageTypeParameter.PNG);
		vcard.addLogo(logo);
		document = generate(vcard);
		assertEquals(1, document.select(".vcard .photo").size());
		assertTrue(document.select(".vcard .logo").isEmpty());
	}

	@Test
	public void sort_string_logic() throws Exception {
		//N;SORT-AS, ORG;SORT-AS, SORT_STRING
		{
			VCard vcard = new VCard();
			StructuredNameType n = new StructuredNameType();
			n.setSortAs("Smith");
			vcard.setStructuredName(n);
			OrganizationType org = new OrganizationType();
			org.setSortAs("Jones");
			vcard.setOrganization(org);
			vcard.setSortString(new SortStringType("Doe"));
			Document document = generate(vcard);

			Elements elements = document.select(".vcard .sort-string");
			SortStringType ss = new SortStringType();
			ss.unmarshalHtml(elements.first(), null);
			assertEquals("Doe", ss.getValue());
		}

		//N;SORT-AS, ORG;SORT-AS
		{
			VCard vcard = new VCard();
			StructuredNameType n = new StructuredNameType();
			n.setSortAs("Smith");
			vcard.setStructuredName(n);
			OrganizationType org = new OrganizationType();
			org.setSortAs("Jones");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			Elements elements = document.select(".vcard .sort-string");
			SortStringType ss = new SortStringType();
			ss.unmarshalHtml(elements.first(), null);
			assertEquals("Smith", ss.getValue());
		}

		//ORG;SORT-AS
		{
			VCard vcard = new VCard();
			OrganizationType org = new OrganizationType();
			org.setSortAs("Jones");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			Elements elements = document.select(".vcard .sort-string");
			SortStringType ss = new SortStringType();
			ss.unmarshalHtml(elements.first(), null);
			assertEquals("Jones", ss.getValue());
		}
	}

	@Test
	public void org() throws Exception {
		//1 value
		{
			VCard vcard = new VCard();
			OrganizationType org = new OrganizationType();
			org.addValue("Google");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			assertEquals(1, document.select(".vcard .org .organization-name").size());
			assertEquals(0, document.select(".vcard .org .organization-unit").size());
		}

		//2 values
		{
			VCard vcard = new VCard();
			OrganizationType org = new OrganizationType();
			org.addValue("Google");
			org.addValue("GMail Team");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			assertEquals(1, document.select(".vcard .org .organization-name").size());
			assertEquals(1, document.select(".vcard .org .organization-unit").size());
		}
	}

	@Test
	public void create_then_parse() throws Exception {
		//create template
		VCard expected = createFullVCard();
		HCardPage template = new HCardPage();
		template.addVCard(expected);
		String html = template.write();

		//write to file for manual inspection
		FileWriter writer = new FileWriter("target/vcard.html");
		writer.write(html);
		writer.close();

		//parse template
		HCardReader reader = new HCardReader(html);
		VCard actual = reader.readNext();

		assertEquals("Claus", actual.getSortString().getValue());

		assertEquals(expected.getClassification().getValue(), actual.getClassification().getValue());

		assertEquals(expected.getMailer().getValue(), actual.getMailer().getValue());

		assertEquals(expected.getFormattedName().getValue(), actual.getFormattedName().getValue());

		assertEquals(expected.getUid().getValue(), actual.getUid().getValue());

		assertEquals(expected.getNickname().getValues(), actual.getNickname().getValues());

		assertEquals(expected.getOrganization().getValues(), actual.getOrganization().getValues());

		assertEquals(expected.getCategories().getValues(), actual.getCategories().getValues());

		assertEquals(expected.getBirthday().getDate(), actual.getBirthday().getDate());

		assertEquals(expected.getRevision().getTimestamp(), actual.getRevision().getTimestamp());

		assertEquals(expected.getGeo().getLatitude(), actual.getGeo().getLatitude());
		assertEquals(expected.getGeo().getLongitude(), actual.getGeo().getLongitude());

		assertEquals(expected.getTimezone().getHourOffset(), actual.getTimezone().getHourOffset());
		assertEquals(expected.getTimezone().getMinuteOffset(), actual.getTimezone().getMinuteOffset());

		{
			StructuredNameType e = expected.getStructuredName();
			StructuredNameType a = actual.getStructuredName();
			assertEquals(e.getFamily(), a.getFamily());
			assertEquals(e.getGiven(), a.getGiven());
			assertEquals(e.getAdditional(), a.getAdditional());
			assertEquals(e.getPrefixes(), a.getPrefixes());
			assertEquals(e.getSuffixes(), a.getSuffixes());
			assertTrue(a.getSortAs().isEmpty());
		}

		assertEquals(expected.getTitles().size(), actual.getTitles().size());
		for (int i = 0; i < expected.getTitles().size(); i++) {
			TitleType e = expected.getTitles().get(i);
			TitleType a = actual.getTitles().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getRoles().size(), actual.getRoles().size());
		for (int i = 0; i < expected.getRoles().size(); i++) {
			RoleType e = expected.getRoles().get(i);
			RoleType a = actual.getRoles().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getNotes().size(), actual.getNotes().size());
		for (int i = 0; i < expected.getNotes().size(); i++) {
			NoteType e = expected.getNotes().get(i);
			NoteType a = actual.getNotes().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getUrls().size(), actual.getUrls().size());
		for (int i = 0; i < expected.getUrls().size(); i++) {
			UrlType e = expected.getUrls().get(i);
			UrlType a = actual.getUrls().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getImpps().size(), actual.getImpps().size());
		for (int i = 0; i < expected.getImpps().size(); i++) {
			ImppType e = expected.getImpps().get(i);
			ImppType a = actual.getImpps().get(i);
			assertEquals(e.getUri(), a.getUri());
		}

		assertEquals(expected.getEmails().size(), actual.getEmails().size());
		for (int i = 0; i < expected.getEmails().size(); i++) {
			EmailType e = expected.getEmails().get(i);
			EmailType a = actual.getEmails().get(i);
			assertEquals(e.getValue(), a.getValue());
			assertEquals(e.getTypes(), a.getTypes());
		}

		assertEquals(expected.getTelephoneNumbers().size(), actual.getTelephoneNumbers().size());
		for (int i = 0; i < expected.getTelephoneNumbers().size(); i++) {
			TelephoneType e = expected.getTelephoneNumbers().get(i);
			TelephoneType a = actual.getTelephoneNumbers().get(i);
			assertEquals(e.getValue(), a.getValue());
			assertEquals(e.getTypes(), a.getTypes());
		}

		assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
		for (int i = 0; i < expected.getAddresses().size(); i++) {
			AddressType e = expected.getAddresses().get(i);
			AddressType a = actual.getAddresses().get(i);
			assertEquals(e.getPoBox(), a.getPoBox());
			assertEquals(e.getExtendedAddress(), a.getExtendedAddress());
			assertEquals(e.getStreetAddress(), a.getStreetAddress());
			assertEquals(e.getLocality(), a.getLocality());
			assertEquals(e.getRegion(), a.getRegion());
			assertEquals(e.getPostalCode(), a.getPostalCode());
			assertEquals(e.getCountry(), a.getCountry());
			assertEquals(e.getLabel(), a.getLabel());
			assertEquals(e.getTypes(), a.getTypes());
		}

		assertEquals(expected.getPhotos().size(), actual.getPhotos().size());
		for (int i = 0; i < expected.getPhotos().size(); i++) {
			PhotoType e = expected.getPhotos().get(i);
			PhotoType a = actual.getPhotos().get(i);
			assertEquals(e.getContentType(), a.getContentType());
			assertArrayEquals(e.getData(), a.getData());
		}

		assertEquals(expected.getSounds().size(), actual.getSounds().size());
		for (int i = 0; i < expected.getSounds().size(); i++) {
			SoundType e = expected.getSounds().get(i);
			SoundType a = actual.getSounds().get(i);
			assertEquals(e.getContentType(), a.getContentType());
			assertArrayEquals(e.getData(), a.getData());
		}

		assertEquals("ez-vcard " + Ezvcard.VERSION, actual.getProdId().getValue());

		assertNull(reader.readNext());
	}

	private VCard createFullVCard() throws IOException {
		VCard vcard = new VCard();

		StructuredNameType n = new StructuredNameType();
		n.setFamily("Claus");
		n.setGiven("Santa");
		n.addAdditional("Saint Nicholas");
		n.addAdditional("Father Christmas");
		n.addPrefix("Mr");
		n.addPrefix("Dr");
		n.addSuffix("M.D.");
		n.setSortAs("Claus");
		vcard.setStructuredName(n);

		vcard.setClassification(new ClassificationType("public"));

		vcard.setMailer(new MailerType("Thunderbird"));

		vcard.setFormattedName(new FormattedNameType("Santa Claus"));

		NicknameType nickname = new NicknameType();
		nickname.addValue("Kris Kringle");
		vcard.setNickname(nickname);

		vcard.addTitle(new TitleType("Manager"));

		vcard.addRole(new RoleType("Executive"));
		vcard.addRole(new RoleType("Team Builder"));

		EmailType email = new EmailType("johndoe@hotmail.com");
		email.addType(EmailTypeParameter.HOME);
		email.addType(EmailTypeParameter.PREF);
		vcard.addEmail(email);

		email = new EmailType("doe.john@company.com");
		email.addType(EmailTypeParameter.WORK);
		vcard.addEmail(email);

		TelephoneType tel = new TelephoneType("(555) 222-3333");
		vcard.addTelephoneNumber(tel);

		tel = new TelephoneType("(555) 333-4444");
		tel.addType(TelephoneTypeParameter.WORK);
		vcard.addTelephoneNumber(tel);

		tel = new TelephoneType("(555) 111-2222");
		tel.addType(TelephoneTypeParameter.HOME);
		tel.addType(TelephoneTypeParameter.VOICE);
		tel.addType(TelephoneTypeParameter.PREF);
		vcard.addTelephoneNumber(tel);

		AddressType adr = new AddressType();
		adr.setStreetAddress("123 Main St");
		adr.setExtendedAddress("Apt 11");
		adr.setLocality("Austin");
		adr.setRegion("Tx");
		adr.setPostalCode("12345");
		adr.setCountry("USA");
		adr.setLabel("123 Main St.\nAustin TX, 12345\nUSA");
		adr.addType(AddressTypeParameter.HOME);
		vcard.addAddress(adr);

		adr = new AddressType();
		adr.setPoBox("123");
		adr.setStreetAddress("456 Wall St.");
		adr.setLocality("New York");
		adr.setRegion("NY");
		adr.setPostalCode("11111");
		adr.setCountry("USA");
		adr.addType(AddressTypeParameter.PREF);
		adr.addType(AddressTypeParameter.WORK);
		vcard.addAddress(adr);

		OrganizationType org = new OrganizationType();
		org.addValue("Google");
		org.addValue("GMail");
		vcard.setOrganization(org);

		BirthdayType bday = new BirthdayType();
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 1970);
		c.set(Calendar.MONTH, Calendar.MARCH);
		c.set(Calendar.DATE, 8);
		bday.setDate(c.getTime(), false);
		vcard.setBirthday(bday);

		vcard.addUrl(new UrlType("http://company.com"));

		CategoriesType categories = new CategoriesType();
		categories.addValue("business owner");
		categories.addValue("jolly");
		vcard.setCategories(categories);

		vcard.addImpp(ImppType.aim("myhandle"));
		vcard.addImpp(ImppType.yahoo("myhandle@yahoo.com"));

		vcard.addNote(new NoteType("I am proficient in Tiger-Crane Style,\nand I am more than proficient in the exquisite art of the Samurai sword."));

		vcard.setGeo(new GeoType(123.456, -98.123));

		vcard.setTimezone(new TimezoneType(-6, 0, "America/Chicago"));

		InputStream in = getClass().getResourceAsStream("hcard-portrait.jpg");
		PhotoType photo = new PhotoType(in, ImageTypeParameter.JPEG);
		vcard.addPhoto(photo);

		in = getClass().getResourceAsStream("hcard-sound.ogg");
		SoundType sound = new SoundType(in, SoundTypeParameter.OGG);
		vcard.addSound(sound);

		vcard.setUid(new UidType("urn:uuid:ffce1595-cbe9-4418-9d0d-b015655d45f6"));

		c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2000);
		c.set(Calendar.MONTH, Calendar.MARCH);
		c.set(Calendar.DATE, 10);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 22);
		c.set(Calendar.SECOND, 44);
		vcard.setRevision(new RevisionType(c.getTime()));

		return vcard;
	}

	/**
	 * Builds an hCard-enabled HTML page.
	 * @param vcards the vCards to add to the page
	 * @return the HTML page
	 * @throws TemplateException
	 */
	private Document generate(VCard... vcards) throws TemplateException {
		HCardPage template = new HCardPage();
		for (VCard vcard : vcards) {
			template.addVCard(vcard);
		}
		return Jsoup.parse(template.write());
	}
}
