package ezvcard.io.html;

import static ezvcard.VCardVersion.V3_0;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.io.LuckyNumProperty;
import ezvcard.io.LuckyNumProperty.LuckyNumScribe;
import ezvcard.io.MyFormattedNameProperty;
import ezvcard.io.MyFormattedNameProperty.MyFormattedNameScribe;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Categories;
import ezvcard.property.FormattedName;
import ezvcard.property.Label;
import ezvcard.property.Nickname;
import ezvcard.property.Organization;
import ezvcard.property.Source;
import ezvcard.property.Url;
import ezvcard.property.asserter.VCardAsserter;
import ezvcard.util.TelUri;

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
public class HCardParserTest {
	@Test
	public void html_without_vcard() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body></body>" +
		"</html>"
		);
		//@formatter:on

		asserter.done();
	}

	@Test
	public void empty_vcard() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\" />" +
			"</body>" +
		"</html>"
		);
		//@formatter:on

		asserter.next(V3_0);
		asserter.done();
	}

	@Test
	public void vcard_element_has_other_classes() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"foo bar vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void case_insensitive_property_names() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fN\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void single_tag_with_multiple_properties() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"fn url\" href=\"http://johndoe.com\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://johndoe.com")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void property_tags_are_not_direct_children_of_root_tag() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
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
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://johndoe.com")
		.noMore();
		
		asserter.telephone()
			.text("(555) 555-1234")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void property_tags_within_other_property_tags() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<div class=\"n\">" +
						"<span class=\"family-name org\">Smith</span>" +
					"</div>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.structuredName()
			.family("Smith")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("Smith")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_multiple() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
				"</div>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">Jane Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);
		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.next(V3_0);
		asserter.simpleProperty(FormattedName.class)
			.value("Jane Doe")
		.noMore();

		asserter.done();
		//@formatter:on
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

		HCardParser parser = new HCardParser(html);

		VCard vcard = parser.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(2, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		{
			VCard embedded = vcard.getAgent().getVCard();
			assertVersion(V3_0, embedded);
			assertPropertyCount(2, embedded);
			assertEquals("Jane Doe", embedded.getFormattedName().getValue());
			{
				VCard embedded2 = embedded.getAgent().getVCard();
				assertVersion(V3_0, embedded2);
				assertPropertyCount(1, embedded2);
				assertEquals("Joseph Doe", embedded2.getFormattedName().getValue());
			}
		}

		assertWarnings(0, parser);

		assertNoMoreVCards(parser);
	}

	@Test
	public void url_of_vcard_specified() throws Exception {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"fn url\" href=\"index.html\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>";

		HCardParser parser = new HCardParser(html, "http://johndoe.com/vcard.html");
		VCardAsserter asserter = new VCardAsserter(parser);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://johndoe.com/index.html")
		.noMore();
		
		asserter.simpleProperty(Source.class)
			.value("http://johndoe.com/vcard.html")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void convert_instant_messenging_urls_to_impp_types() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"url\" href=\"aim:goim?screenname=ShoppingBuddy\">IM with the AIM ShoppingBuddy</a>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.impp()
			.uri("aim:ShoppingBuddy")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void convert_mailto_urls_to_email_types() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"url\" href=\"mailto:jdoe@hotmail.com\">Email me</a>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.email()
			.value("jdoe@hotmail.com")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void convert_tel_urls_to_tel_types() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"url\" href=\"tel:+15555551234\">Call me</a>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.telephone()
			.uri(new TelUri.Builder("+15555551234").build())
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void mailto_url_with_email_and_url_class_names() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"email url\" href=\"mailto:jdoe@hotmail.com\">Email me</a>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(Url.class)
			.value("mailto:jdoe@hotmail.com")
		.noMore();
		
		asserter.email()
			.value("jdoe@hotmail.com")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void tel_url_with_tel_and_url_class_names() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<a class=\"tel url\" href=\"tel:+15555551234\">Call me</a>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(Url.class)
			.value("tel:+15555551234")
		.noMore();
		
		asserter.telephone()
			.uri(new TelUri.Builder("+15555551234").build())
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void assign_labels_to_addresses() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
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
		"</html>"
		);

		asserter.next(V3_0);

		asserter.address()
			.streetAddress("123 Main St.")
			.locality("Austin")
			.region("TX")
			.postalCode("12345")
			.label("123 Main St. Austin, TX 12345")
			.types(AddressType.HOME)
		.noMore();
		
		asserter.simpleProperty(Label.class)
			.value("456 Wall St., New York, NY 67890")
			.param("TYPE", "work")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void anchor_in_url() throws Exception {
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

		HCardParser parser = new HCardParser(html, "http://johndoe.com/vcard.html#anchor");
		VCardAsserter asserter = new VCardAsserter(parser);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("Jane Doe")
		.noMore();

		asserter.simpleProperty(Source.class)
			.value("http://johndoe.com/vcard.html#anchor")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void non_existant_anchor() throws Exception {
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

		HCardParser parser = new HCardParser(html, "http://johndoe.com/vcard.html#non-existant");
		VCardAsserter asserter = new VCardAsserter(parser);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.simpleProperty(Source.class)
			.value("http://johndoe.com/vcard.html#non-existant")
		.noMore();

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("Jane Doe")
		.noMore();

		asserter.simpleProperty(Source.class)
			.value("http://johndoe.com/vcard.html#non-existant")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void add_all_nicknames_to_the_same_object() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
					"<span class=\"nickname\">Johnny</span>" +
					"<span class=\"nickname\">Johnny 5</span>" +
					"<span class=\"nickname\">Johnster</span>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.listProperty(Nickname.class)
			.values("Johnny", "Johnny 5", "Johnster")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void add_all_categories_to_the_same_object() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
					"<span class=\"category\">programmer</span>" +
					"<span class=\"category\">swimmer</span>" +
					"<span class=\"category\" rel=\"singer\">I also sing</span>" +
				"</div>" +
			"</body>" +
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.listProperty(Categories.class)
			.values("programmer", "swimmer", "singer")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void complete_vcard() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readHtml(
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
		"</html>"
		);

		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("CommerceNet")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("CommerceNet")
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://www.commerce.net/")
		.noMore();
		
		asserter.address()
			.types(AddressType.WORK)
			.streetAddress("169 University Avenue")
			.locality("Palo Alto")
			.region("California")
			.postalCode("94301")
			.country("USA")
		.noMore();
		
		asserter.telephone()
			.types(TelephoneType.WORK)
			.text("+1-650-289-4040")
		.next()
			.types(TelephoneType.FAX)
			.text("+1-650-289-4041")
		.noMore();
		
		asserter.email()
			.value("info@commerce.net")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void registerExtendedProperty() throws Exception {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"x-lucky-num\">24</span>" +
					"<span class=\"x-lucky-num\">22</span>" +
					"<span class=\"x-gender\">male</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardParser parser = new HCardParser(html);
		parser.registerScribe(new LuckyNumScribe());

		VCard vcard = parser.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(3, vcard);

		//read a type that has a type class
		List<LuckyNumProperty> luckyNumTypes = vcard.getProperties(LuckyNumProperty.class);
		assertEquals(2, luckyNumTypes.size());
		assertEquals(24, luckyNumTypes.get(0).luckyNum);
		assertEquals(22, luckyNumTypes.get(1).luckyNum);

		//read a type without a type class
		assertEquals("male", vcard.getExtendedProperty("X-GENDER").getValue());

		assertWarnings(0, parser);

		assertNoMoreVCards(parser);
	}

	@Test
	public void registerExtendedProperty_overrides_standard_type_classes() throws Exception {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"fn\">John Doe</span>" +
				"</div>" +
			"</body>" +
		"</html>";
		//@formatter:on

		HCardParser parser = new HCardParser(html);
		parser.registerScribe(new MyFormattedNameScribe());

		VCard vcard = parser.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(1, vcard);

		//read a type that has a type class
		assertEquals("JOHN DOE", vcard.getProperty(MyFormattedNameProperty.class).value);

		assertWarnings(0, parser);

		assertNoMoreVCards(parser);
	}

	@Test
	public void skipMeException() throws Exception {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"skipme\">value</span>" +
					"<span class=\"x-foo\">value</span>" +
				"</div>" +
			"</body>" +
		"</html>";

		HCardParser parser = new HCardParser(html);
		parser.registerScribe(new SkipMeScribe());
		VCardAsserter asserter = new VCardAsserter(parser);

		asserter.next(V3_0);

		asserter.rawProperty("x-foo")
			.value("value")
		.noMore();

		asserter.warnings(1);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void cannotParseException() throws Exception {
		//@formatter:off
		String html =
		"<html>" +
			"<body>" +
				"<div class=\"vcard\">" +
					"<span class=\"cannotparse\">value</span>" +
					"<span class=\"x-foo\">value</span>" +
				"</div>" +
			"</body>" +
		"</html>";

		HCardParser parser = new HCardParser(html);
		parser.registerScribe(new CannotParseScribe());
		VCardAsserter asserter = new VCardAsserter(parser);

		asserter.next(V3_0);

		asserter.rawProperty("x-foo")
			.value("value")
		.noMore();
		
		asserter.rawProperty("cannotparse")
			.value("<span class=\"cannotparse\">value</span>")
		.noMore();

		asserter.warnings(1);
		asserter.done();
		//@formatter:on
	}

	private static VCardAsserter readHtml(String html) {
		HCardParser parser = new HCardParser(html);
		return new VCardAsserter(parser);
	}
}
