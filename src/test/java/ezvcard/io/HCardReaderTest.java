package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.EmailType;
import ezvcard.types.ImppType;
import ezvcard.types.LabelType;
import ezvcard.types.TelephoneType;
import ezvcard.types.UrlType;

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
public class HCardReaderTest {
	@Test
	public void html_without_vcard() {
		//@formatter:off
		String html =
		"<html>" +
			"<body></body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());
		assertNull(reader.readNext());
	}

	@Test
	public void empty_vcard() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\" />" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());
		assertNotNull(reader.readNext());
		assertNull(reader.readNext());
	}

	@Test
	public void always_version_3() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\" />" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		assertNull(reader.readNext());
	}

	@Test
	public void vcard_element_has_other_classes() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"foo bar vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());
		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void case_insensitive_property_names() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fN\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());
		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void single_tag_with_multiple_properties() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"fn url\" href=\"http://johndoe.com\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());
		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, vcard.getUrls().size());
		assertEquals("http://johndoe.com", vcard.getUrls().get(0).getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void property_tags_are_not_direct_children_of_root_tag() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<h1>Welcome to my webpage</h1>" +
					"<table>" +
						"<tr>" +
							"<td>" +
								"<a class=\"fn url\" href=\"http://johndoe.com\">John Doe</span>" +
							"</td>" +
							"<td>" +
								"<span class=\"tel\">(555) 555-1234</span>" +
							"</td>" +
						"</tr>" +
					"</table>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());
		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals("http://johndoe.com", vcard.getUrls().get(0).getValue());
		assertEquals("(555) 555-1234", vcard.getTelephoneNumbers().get(0).getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void property_tags_within_other_property_tags() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<div class=\"n\">" +
						"<span class=\"family-name org\">Smith</span>" +
					"</div>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());
		VCard vcard = reader.readNext();
		assertEquals("Smith", vcard.getStructuredName().getFamily());
		assertEquals("Smith", vcard.getOrganization().getValues().get(0));

		assertNull(reader.readNext());
	}

	@Test
	public void read_multiple() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
				"</div>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">Jane Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = reader.readNext();
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void embedded_vcards() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<div class=\"fn\">John Doe</div>" +
					"<div class=\"agent vcard\">" +
						"<span class=\"fn\">Jane Doe</span>" +
						"<div class=\"agent vcard\">" +
							"<span class=\"fn\">Joseph Doe</span>" +
						"</div>" +
					"</div>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals("Jane Doe", vcard.getAgent().getVCard().getFormattedName().getValue());
		assertEquals("Joseph Doe", vcard.getAgent().getVCard().getAgent().getVCard().getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void url_of_vcard_specified() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"fn url\" href=\"index.html\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString(), "http://johndoe.com/vcard.html");

		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals("http://johndoe.com/index.html", vcard.getUrls().get(0).getValue());
		assertEquals("http://johndoe.com/vcard.html", vcard.getSources().get(0).getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void convert_instant_messenging_urls_to_impp_types() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"url\" href=\"aim:goim?screenname=ShoppingBuddy\">IM with the AIM ShoppingBuddy</a>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertTrue(vcard.getUrls().isEmpty());

		{
			Iterator<ImppType> it = vcard.getImpps().iterator();

			ImppType impp = it.next();
			assertEquals(URI.create("aim:ShoppingBuddy"), impp.getUri());

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void convert_mailto_urls_to_email_types() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"url\" href=\"mailto:jdoe@hotmail.com\">Email me</a>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertTrue(vcard.getUrls().isEmpty());

		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType email = it.next();
			assertEquals("jdoe@hotmail.com", email.getValue());

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void convert_tel_urls_to_tel_types() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"url\" href=\"tel:+15555551234\">Call me</a>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertTrue(vcard.getUrls().isEmpty());

		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType tel = it.next();
			assertEquals("+15555551234", tel.getValue());

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void mailto_url_with_email_and_url_class_names() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"email url\" href=\"mailto:jdoe@hotmail.com\">Email me</a>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();

		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType url = it.next();
			assertEquals("mailto:jdoe@hotmail.com", url.getValue());

			assertFalse(it.hasNext());
		}

		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType email = it.next();
			assertEquals("jdoe@hotmail.com", email.getValue());

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void tel_url_with_tel_and_url_class_names() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"tel url\" href=\"tel:+15555551234\">Call me</a>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();

		{
			Iterator<UrlType> it = vcard.getUrls().iterator();

			UrlType url = it.next();
			assertEquals("tel:+15555551234", url.getValue());

			assertFalse(it.hasNext());
		}

		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType tel = it.next();
			assertEquals("+15555551234", tel.getValue());

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void assign_labels_to_addresses() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<div class=\"adr\">" +
						"<span class=\"type\">Home</span>:<br>" +
						"<span class=\"street-address\">123 Main St.</span><br>" +
						"<span class=\"locality\">Austin</span>, <span class=\"region\">TX</span> <span class=\"postal-code\">12345</span>" +
					"</div>" +
					"<div class=\"label\"><abbr class=\"type\" title=\"home\"></abbr>123 Main St.\nAustin, TX 12345</div>" +
					"<abbr class=\"label\" title=\"456 Wall St., New York, NY 67890\"><abbr class=\"type\" title=\"work\"></abbr></abbr>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();

		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType adr = it.next();
			assertEquals("123 Main St.", adr.getStreetAddress());
			assertEquals("Austin", adr.getLocality());
			assertEquals("TX", adr.getRegion());
			assertEquals("12345", adr.getPostalCode());
			assertEquals("123 Main St. Austin, TX 12345", adr.getLabel());
			assertEquals(1, adr.getTypes().size());
			assertTrue(adr.getTypes().contains(AddressTypeParameter.HOME));

			assertFalse(it.hasNext());
		}

		{
			Iterator<LabelType> it = vcard.getOrphanedLabels().iterator();

			LabelType label = it.next();
			assertEquals("456 Wall St., New York, NY 67890", label.getValue());
			assertEquals(1, label.getTypes().size());
			assertTrue(label.getTypes().contains(AddressTypeParameter.WORK));

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void anchor_in_url() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
				"</div>" +
				"<div id=\"anchor\">" +
					"<div class=\"vcard\">" +
						"<span class=\"fn\">Jane Doe</span>" +
					"</div>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString(), "http://johndoe.com/vcard.html#anchor");

		VCard vcard = reader.readNext();
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void non_existant_anchor() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
				"</div>" +
				"<div id=\"anchor\">" +
					"<div class=\"vcard\">" +
						"<span class=\"fn\">Jane Doe</span>" +
					"</div>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString(), "http://johndoe.com/vcard.html#non-existant");

		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = reader.readNext();
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void add_all_nicknames_to_the_same_object() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
					"<span class=\"nickname\">Johnny</span>" +
					"<span class=\"nickname\">Johnny 5</span>" +
					"<span class=\"nickname\">Johnster</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, vcard.getNicknames().size());
		assertEquals(Arrays.asList("Johnny", "Johnny 5", "Johnster"), vcard.getNickname().getValues());

		assertNull(reader.readNext());
	}

	@Test
	public void add_all_categories_to_the_same_object() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
					"<span class=\"category\">programmer</span>" +
					"<span class=\"category\">swimmer</span>" +
					"<span class=\"category\" rel=\"singer\">I also sing</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, vcard.getCategoriesList().size());
		assertEquals(Arrays.asList("programmer", "swimmer", "singer"), vcard.getCategories().getValues());

		assertNull(reader.readNext());
	}

	@Test
	public void complete_vcard() {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"fn org url\" href=\"http://www.commerce.net/\">CommerceNet</a>" +
					"<div class=\"adr\">" +
						"<span class=\"type\">Work</span>:" +
						"<div class=\"street-address\">169 University Avenue</div>" +
						"<span class=\"locality\">Palo Alto</span>,  " +
						"<abbr class=\"region\" title=\"California\">CA</abbr>&nbsp;&nbsp;" +
						"<span class=\"postal-code\">94301</span>" +
						"<div class=\"country-name\">USA</div>" +
					"</div>" +
					"<div class=\"tel\">" +
						"<span class=\"type\">Work</span> +1-650-289-4040" +
					"</div>" +
					"<div class=\"tel\">" +
						"<span class=\"type\">Fax</span> +1-650-289-4041" +
					"</div>" +
					"<div>Email:" +
						"<span class=\"email\">info@commerce.net</span>" +
					"</div>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardReader reader = new HCardReader(html.toString());

		VCard vcard = reader.readNext();
		assertEquals("CommerceNet", vcard.getFormattedName().getValue());
		assertEquals(Arrays.asList("CommerceNet"), vcard.getOrganization().getValues());

		//URL
		{
			Iterator<UrlType> it = vcard.getUrls().iterator();
			assertEquals("http://www.commerce.net/", it.next().getValue());
			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<AddressType> it = vcard.getAddresses().iterator();

			AddressType adr = it.next();
			assertEquals(1, adr.getTypes().size());
			assertTrue(adr.getTypes().contains(AddressTypeParameter.WORK));
			assertNull(adr.getPoBox());
			assertNull(adr.getExtendedAddress());
			assertEquals("169 University Avenue", adr.getStreetAddress());
			assertEquals("Palo Alto", adr.getLocality());
			assertEquals("California", adr.getRegion());
			assertEquals("94301", adr.getPostalCode());
			assertEquals("USA", adr.getCountry());

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType tel = it.next();
			assertEquals(1, tel.getTypes().size());
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.WORK));
			assertEquals("+1-650-289-4040", tel.getValue());

			tel = it.next();
			assertEquals(1, tel.getTypes().size());
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.FAX));
			assertEquals("+1-650-289-4041", tel.getValue());

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<EmailType> it = vcard.getEmails().iterator();

			EmailType email = it.next();
			assertEquals("info@commerce.net", email.getValue());

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test(expected = RuntimeException.class)
	public void registerExtendedType_no_default_constructor() throws Exception {
		HCardReader reader = new HCardReader("");
		reader.registerExtendedType(BadType.class);
	}
}
