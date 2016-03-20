package ezvcard.io.html;

import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.date;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.scribe.SortStringScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.SoundType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.Email;
import ezvcard.property.Impp;
import ezvcard.property.Logo;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.Role;
import ezvcard.property.SortString;
import ezvcard.property.Sound;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Title;
import ezvcard.property.Uid;
import ezvcard.property.Url;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;
import freemarker.template.TemplateException;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
		Photo photo = new Photo(mockData, ImageType.JPEG);
		vcard.addPhoto(photo);
		document = generate(vcard);
		assertEquals(1, document.select(".vcard .photo").size());
		assertTrue(document.select(".vcard .logo").isEmpty());

		vcard = new VCard();
		Logo logo = new Logo(mockData, ImageType.PNG);
		vcard.addLogo(logo);
		document = generate(vcard);
		assertTrue(document.select(".vcard .photo").isEmpty());
		assertEquals(1, document.select(".vcard .logo").size());

		vcard = new VCard();
		photo = new Photo(mockData, ImageType.JPEG);
		vcard.addPhoto(photo);
		logo = new Logo(mockData, ImageType.PNG);
		vcard.addLogo(logo);
		document = generate(vcard);
		assertEquals(1, document.select(".vcard .photo").size());
		assertTrue(document.select(".vcard .logo").isEmpty());
	}

	@Test
	public void sort_string_logic() throws Exception {
		SortStringScribe scribe = new SortStringScribe();

		//N;SORT-AS, ORG;SORT-AS, SORT_STRING
		{
			VCard vcard = new VCard();
			StructuredName n = new StructuredName();
			n.setSortAs("Smith");
			vcard.setStructuredName(n);
			Organization org = new Organization();
			org.getSortAs().add("Jones");
			vcard.setOrganization(org);
			vcard.setSortString(new SortString("Doe"));
			Document document = generate(vcard);

			Elements elements = document.select(".vcard .sort-string");
			SortString ss = scribe.parseHtml(new HCardElement(elements.first())).getProperty();
			assertEquals("Doe", ss.getValue());
		}

		//N;SORT-AS, ORG;SORT-AS
		{
			VCard vcard = new VCard();
			StructuredName n = new StructuredName();
			n.setSortAs("Smith");
			vcard.setStructuredName(n);
			Organization org = new Organization();
			org.getSortAs().add("Jones");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			Elements elements = document.select(".vcard .sort-string");
			SortString ss = scribe.parseHtml(new HCardElement(elements.first())).getProperty();
			assertEquals("Smith", ss.getValue());
		}

		//ORG;SORT-AS
		{
			VCard vcard = new VCard();
			Organization org = new Organization();
			org.getSortAs().add("Jones");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			Elements elements = document.select(".vcard .sort-string");
			SortString ss = scribe.parseHtml(new HCardElement(elements.first())).getProperty();
			assertEquals("Jones", ss.getValue());
		}
	}

	@Test
	public void org() throws Exception {
		//1 value
		{
			VCard vcard = new VCard();
			Organization org = new Organization();
			org.getValues().add("Google");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			assertEquals(1, document.select(".vcard .org .organization-name").size());
			assertEquals(0, document.select(".vcard .org .organization-unit").size());
		}

		//2 values
		{
			VCard vcard = new VCard();
			Organization org = new Organization();
			org.getValues().add("Google");
			org.getValues().add("GMail Team");
			vcard.setOrganization(org);
			Document document = generate(vcard);

			assertEquals(1, document.select(".vcard .org .organization-name").size());
			assertEquals(1, document.select(".vcard .org .organization-unit").size());
		}
	}

	@Test
	public void note() throws Exception {
		VCard vcard = new VCard();
		vcard.addNote("one\rtwo\nthree\r\nfour");

		HCardPage template = new HCardPage();
		template.add(vcard);
		String html = template.write();

		HCardParser reader = new HCardParser(html);
		vcard = reader.readNext();
		reader.close();
		assertEquals("one" + NEWLINE + "two" + NEWLINE + "three" + NEWLINE + "four", vcard.getNotes().get(0).getValue());
	}

	@Test
	public void create_then_parse() throws Exception {
		//create template
		VCard expected = createFullVCard();
		HCardPage template = new HCardPage();
		template.add(expected);
		String html = template.write();

		//write to file for manual inspection
		FileWriter writer = new FileWriter(new File("target", "vcard.html"));
		writer.write(html);
		writer.close();

		//parse template
		HCardParser reader = new HCardParser(html);
		VCard actual = reader.readNext();
		reader.close();

		assertEquals("Claus", actual.getSortString().getValue());

		assertEquals(expected.getClassification().getValue(), actual.getClassification().getValue());

		assertEquals(expected.getMailer().getValue(), actual.getMailer().getValue());

		assertEquals(expected.getFormattedName().getValue(), actual.getFormattedName().getValue());

		assertEquals(expected.getUid().getValue(), actual.getUid().getValue());

		assertEquals(expected.getNickname().getValues(), actual.getNickname().getValues());

		assertEquals(expected.getOrganization().getValues(), actual.getOrganization().getValues());

		assertEquals(expected.getCategories().getValues(), actual.getCategories().getValues());

		assertEquals(expected.getBirthday().getDate(), actual.getBirthday().getDate());

		assertEquals(expected.getRevision().getValue(), actual.getRevision().getValue());

		assertEquals(expected.getGeo().getLatitude(), actual.getGeo().getLatitude());
		assertEquals(expected.getGeo().getLongitude(), actual.getGeo().getLongitude());

		assertEquals(expected.getTimezone().getOffset(), actual.getTimezone().getOffset());
		assertNull(actual.getTimezone().getText()); //text value is not written

		{
			StructuredName e = expected.getStructuredName();
			StructuredName a = actual.getStructuredName();
			assertEquals(e.getFamily(), a.getFamily());
			assertEquals(e.getGiven(), a.getGiven());
			assertEquals(e.getAdditionalNames(), a.getAdditionalNames());
			assertEquals(e.getPrefixes(), a.getPrefixes());
			assertEquals(e.getSuffixes(), a.getSuffixes());
			assertTrue(a.getSortAs().isEmpty());
		}

		assertEquals(expected.getTitles().size(), actual.getTitles().size());
		for (int i = 0; i < expected.getTitles().size(); i++) {
			Title e = expected.getTitles().get(i);
			Title a = actual.getTitles().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getRoles().size(), actual.getRoles().size());
		for (int i = 0; i < expected.getRoles().size(); i++) {
			Role e = expected.getRoles().get(i);
			Role a = actual.getRoles().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getNotes().size(), actual.getNotes().size());
		for (int i = 0; i < expected.getNotes().size(); i++) {
			Note e = expected.getNotes().get(i);
			Note a = actual.getNotes().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getUrls().size(), actual.getUrls().size());
		for (int i = 0; i < expected.getUrls().size(); i++) {
			Url e = expected.getUrls().get(i);
			Url a = actual.getUrls().get(i);
			assertEquals(e.getValue(), a.getValue());
		}

		assertEquals(expected.getImpps().size(), actual.getImpps().size());
		for (int i = 0; i < expected.getImpps().size(); i++) {
			Impp e = expected.getImpps().get(i);
			Impp a = actual.getImpps().get(i);
			assertEquals(e.getUri(), a.getUri());
		}

		assertEquals(expected.getEmails().size(), actual.getEmails().size());
		for (int i = 0; i < expected.getEmails().size(); i++) {
			Email e = expected.getEmails().get(i);
			Email a = actual.getEmails().get(i);
			assertEquals(e.getValue(), a.getValue());
			assertEquals(e.getTypes(), a.getTypes());
		}

		assertEquals(expected.getTelephoneNumbers().size(), actual.getTelephoneNumbers().size());
		for (int i = 0; i < expected.getTelephoneNumbers().size(); i++) {
			Telephone e = expected.getTelephoneNumbers().get(i);
			Telephone a = actual.getTelephoneNumbers().get(i);
			if (e.getText() != null) {
				assertEquals(e.getText(), a.getText());
			} else {
				TelUri uri = e.getUri();
				if (uri.getExtension() == null) {
					assertEquals(e.getUri().getNumber(), a.getText());
				} else {
					assertEquals(e.getUri().getNumber() + " x" + uri.getExtension(), a.getText());
				}
			}
			assertEquals(e.getTypes(), a.getTypes());
		}

		assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
		for (int i = 0; i < expected.getAddresses().size(); i++) {
			Address e = expected.getAddresses().get(i);
			Address a = actual.getAddresses().get(i);
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
			Photo e = expected.getPhotos().get(i);
			Photo a = actual.getPhotos().get(i);
			assertEquals(e.getContentType(), a.getContentType());
			assertArrayEquals(e.getData(), a.getData());
		}

		assertEquals(expected.getSounds().size(), actual.getSounds().size());
		for (int i = 0; i < expected.getSounds().size(); i++) {
			Sound e = expected.getSounds().get(i);
			Sound a = actual.getSounds().get(i);
			assertEquals(e.getContentType(), a.getContentType());
			assertArrayEquals(e.getData(), a.getData());
		}

		assertEquals("ez-vcard " + Ezvcard.VERSION, actual.getProductId().getValue());
	}

	private VCard createFullVCard() throws IOException {
		VCard vcard = new VCard();

		StructuredName n = new StructuredName();
		n.setFamily("Claus");
		n.setGiven("Santa");
		n.getAdditionalNames().add("Saint Nicholas");
		n.getAdditionalNames().add("Father Christmas");
		n.getPrefixes().add("Mr");
		n.getPrefixes().add("Dr");
		n.getSuffixes().add("M.D.");
		n.setSortAs("Claus");
		vcard.setStructuredName(n);

		vcard.setClassification("public");

		vcard.setMailer("Thunderbird");

		vcard.setFormattedName("Santa Claus");

		vcard.setNickname("Kris Kringle");

		vcard.addTitle("Manager");

		vcard.addRole("Executive");
		vcard.addRole("Team Builder");

		vcard.addEmail("johndoe@hotmail.com", EmailType.HOME, EmailType.WORK);

		vcard.addEmail("doe.john@company.com", EmailType.WORK);

		Telephone tel = new Telephone(new TelUri.Builder("+1-555-222-3333").extension("101").build());
		vcard.addTelephoneNumber(tel);

		tel = new Telephone(new TelUri.Builder("+1-555-333-4444").build());
		tel.getTypes().add(TelephoneType.WORK);
		vcard.addTelephoneNumber(tel);

		vcard.addTelephoneNumber("(555) 111-2222", TelephoneType.HOME, TelephoneType.VOICE, TelephoneType.PREF);

		Address adr = new Address();
		adr.setStreetAddress("123 Main St");
		adr.setExtendedAddress("Apt 11");
		adr.setLocality("Austin");
		adr.setRegion("Tx");
		adr.setPostalCode("12345");
		adr.setCountry("USA");
		adr.setLabel("123 Main St." + NEWLINE + "Austin TX, 12345" + NEWLINE + "USA");
		adr.getTypes().add(AddressType.HOME);
		vcard.addAddress(adr);

		adr = new Address();
		adr.setPoBox("123");
		adr.setStreetAddress("456 Wall St.");
		adr.setLocality("New York");
		adr.setRegion("NY");
		adr.setPostalCode("11111");
		adr.setCountry("USA");
		adr.getTypes().add(AddressType.PREF);
		adr.getTypes().add(AddressType.WORK);
		vcard.addAddress(adr);

		vcard.setOrganization("Google", "GMail");

		Birthday bday = new Birthday(date("1970-03-08"), false);
		vcard.setBirthday(bday);

		vcard.addUrl("http://company.com");

		vcard.setCategories("business owner", "jolly");

		vcard.addImpp(Impp.aim("myhandle"));
		vcard.addImpp(Impp.yahoo("myhandle@yahoo.com"));

		vcard.addNote("I am proficient in Tiger-Crane Style," + NEWLINE + "and I am more than proficient in the exquisite art of the Samurai sword.");

		vcard.setGeo(123.456, -98.123);

		vcard.setTimezone(new Timezone(new UtcOffset(false, -6, 0), "America/Chicago"));

		InputStream in = getClass().getResourceAsStream("hcard-portrait.jpg");
		Photo photo = new Photo(in, ImageType.JPEG);
		vcard.addPhoto(photo);

		in = getClass().getResourceAsStream("hcard-sound.ogg");
		Sound sound = new Sound(in, SoundType.OGG);
		vcard.addSound(sound);

		vcard.setUid(new Uid("urn:uuid:ffce1595-cbe9-4418-9d0d-b015655d45f6"));

		vcard.setRevision(date("2000-03-10 13:22:44"));

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
			template.add(vcard);
		}
		return Jsoup.parse(template.write());
	}
}
