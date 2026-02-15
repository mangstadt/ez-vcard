package ezvcard.io.html;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.ParseContext;
import ezvcard.io.scribe.SortStringScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.SoundType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.Impp;
import ezvcard.property.Logo;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.Revision;
import ezvcard.property.SortString;
import ezvcard.property.Sound;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Uid;
import ezvcard.util.TelUri;
import freemarker.template.TemplateException;

/*
 Copyright (c) 2012-2026, Michael Angstadt
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
			SortString ss = scribe.parseHtml(new HCardElement(elements.first()), new ParseContext());
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
			SortString ss = scribe.parseHtml(new HCardElement(elements.first()), new ParseContext());
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
			SortString ss = scribe.parseHtml(new HCardElement(elements.first()), new ParseContext());
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

		try (HCardParser reader = new HCardParser(html)) {
			vcard = reader.readNext();
		}
		assertEquals("one" + NEWLINE + "two" + NEWLINE + "three" + NEWLINE + "four", vcard.getNotes().get(0).getValue());
	}

	/**
	 * Test round-tripping for locales that don't use a period for decimals.
	 */
	@Test
	public void geo() throws Exception {
		Locale defaultLocale = Locale.getDefault();

		try {
			Locale.setDefault(Locale.GERMANY);

			VCard orig = new VCard();
			orig.setGeo(123.456, -98.123);

			HCardPage template = new HCardPage();
			template.add(orig);

			String html = template.write();
			VCard parsed;
			try (HCardParser reader = new HCardParser(html)) {
				parsed = reader.readNext();
			}

			assertEquals(orig.getGeo().getLatitude(), parsed.getGeo().getLatitude());
			assertEquals(orig.getGeo().getLongitude(), parsed.getGeo().getLongitude());
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}

	@Test
	public void create_then_parse() throws Exception {
		//create template
		VCard input = createFullVCard();
		HCardPage template = new HCardPage();
		template.add(input);
		String html = template.write();

		//write to HTML file for manual inspection
		try (Writer writer = Files.newBufferedWriter(Paths.get("target", "HCardPageTest.create_then_parse.html"))) {
			writer.write(html);
		}

		//parse template
		VCard actual;
		try (HCardParser reader = new HCardParser(html)) {
			actual = reader.readNext();
		}

		/*
		 * Output will be slightly different from the original.
		 */
		VCard expected = input;
		{
			/*
			 * SORT-AS parameter is converted to SORT-STRING property.
			 */
			expected.setSortString("Claus");
			expected.getStructuredName().setSortAs(null);

			/*
			 * Timezone text value not written to HTML page.
			 */
			expected.getTimezone().setText(null);

			/*
			 * TEL URIs are converted to text
			 */
			{
				expected.getTelephoneNumbers().clear();

				expected.addTelephoneNumber("+1-555-222-3333 x101");
				expected.addTelephoneNumber("+1-555-333-4444", TelephoneType.WORK);
				expected.addTelephoneNumber("(555) 111-2222", TelephoneType.HOME, TelephoneType.VOICE, TelephoneType.PREF);
			}

			/**
			 * PRODID property added
			 */
			expected.setProductId("ez-vcard " + Ezvcard.VERSION);
		}

		assertEquals(expected, actual);
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

		Birthday bday = new Birthday(LocalDate.of(1970, 3, 8));
		vcard.setBirthday(bday);

		vcard.addUrl("http://company.com");

		vcard.setCategories("business owner", "jolly");

		vcard.addImpp(Impp.aim("myhandle"));
		vcard.addImpp(Impp.yahoo("myhandle@yahoo.com"));

		vcard.addNote("I am proficient in Tiger-Crane Style," + NEWLINE + "and I am more than proficient in the exquisite art of the Samurai sword.");

		vcard.setGeo(123.456, -98.123);

		vcard.setTimezone(new Timezone(ZoneOffset.ofHours(-6), "America/Chicago"));

		InputStream in = getClass().getResourceAsStream("hcard-portrait.jpg");
		Photo photo = new Photo(in, ImageType.JPEG);
		vcard.addPhoto(photo);

		in = getClass().getResourceAsStream("hcard-sound.ogg");
		Sound sound = new Sound(in, SoundType.OGG);
		vcard.addSound(sound);

		vcard.setUid(new Uid("urn:uuid:ffce1595-cbe9-4418-9d0d-b015655d45f6"));

		vcard.setRevision(new Revision(LocalDateTime.of(2000, 3, 15, 13, 22, 44).toInstant(ZoneOffset.UTC)));

		return vcard;
	}

	/**
	 * Builds an hCard-enabled HTML page.
	 * @param vcards the vCards to add to the page
	 * @return the HTML page
	 */
	private Document generate(VCard... vcards) throws TemplateException {
		HCardPage template = new HCardPage();
		Arrays.stream(vcards).forEach(template::add);
		return Jsoup.parse(template.write());
	}
}
