package ezvcard.io.html;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import ezvcard.util.HtmlUtils;

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
public class HCardElementTest {
	@Test
	public void value_text_content() {
		assertValue("<div>The text</div>", "The text");
	}

	@Test
	public void value_line_breaks() {
		//@formatter:off
		assertValue(
		"<div>" +
			"The<br>text" +
		"</div>",
		"The" + NEWLINE + "text");

		//"value" element
		assertValue(
		"<div>" +
			"The "+
			"<span class=\"value\">"+
				"real<br>text" +
			"</span>" +
		"</div>", "real" + NEWLINE + "text");

		//nested "value" element
		assertValue(
		"<div>" +
			"The " +
			"<span class=\"value\">" +
				"real<br>text " +
				"<span class=\"value\">" +
					"goes<br>here" +
				"</span>" +
			"</span>" +
		"</div>", 
		"real" + NEWLINE + "text goes" + NEWLINE + "here");
		//@formatter:on
	}

	@Test
	public void value_ignore_child_tags() {
		//@formatter:off
		assertValue(
		"<div>" +
			"The<b>text</b>" +
		"</div>", 
		"Thetext");
		//@formatter:on
	}

	@Test
	public void value_ignore_type_text() {
		//@formatter:off
		assertValue(
		"<div>" +
			"<span class=\"type\">Work</span>" +
			" is boring." +
		"</div>", 
		"is boring.");
		
		assertValue(
		"<div>" +
			"<b>" +
				"All " +
				"<span class=\"type\">work</span>" +
				" is boring." +
			"</b>" +
		"</div>", 
		"All  is boring.");
		//@formatter:on
	}

	@Test
	public void value_value_tag() {
		//@formatter:off
		assertValue(
		"<div>" +
			"This is " +
			"<span class=\"value\">the text</span>" +
			"." +
		"</div>", 
		"the text");
		//@formatter:on
	}

	@Test
	public void value_multiple_value_tags() {
		//@formatter:off
		assertValue(
		"<div>" +
			"<span class=\"value\">+1</span>" +
			"." +
			"<span class=\"value\">415</span>" +
			"." +
			"<span class=\"value\">555</span>" +
			"." +
			"<span class=\"value\">1212</span>" +
		"</div>", 
		"+14155551212");
		//@formatter:on
	}

	@Test
	public void value_multiple_value_tags_not_direct_child() {
		//@formatter:off
		assertValue(
		"<div>" +
			"<div>Some text</div>" +
			"<div>" +
				"<span class=\"value\">This is</span>" +
				"<div>" +
					"<div class=\"value\">the value</div>" +
				"</div>" +
				"<div class=\"value\">of the element.</div>" +
			"</div>" +
		"</div>",		
		"This isthe valueof the element.");
		//@formatter:on
	}

	@Test
	public void value_nested_value_tags() {
		//@formatter:off
		assertValue(
		"<div>" +
			"<div class=\"value\">" +
				"the value" +
				"<span class=\"value\">nested</span>" +
			"</div>" +
		"</div>",		
		"the valuenested");
		//@formatter:on
	}

	@Test
	public void value_abbr_value() {
		//@formatter:off
		assertValue(
		"<div>" +
			"This is " +
			"<abbr class=\"value\" title=\"1234\">the text</abbr>" +
			"." +
		"</div>",
		"1234");
		//@formatter:on
	}

	@Test
	public void value_abbr_tag_with_title() {
		assertValue("<abbr class=\"latitude\" title=\"48.816667\">N 48� 81.6667</abbr>", "48.816667");
	}

	@Test
	public void value_abbr_tag_without_title() {
		assertValue("<abbr class=\"latitude\">N 48� 81.6667</abbr>", "N 48� 81.6667");
	}

	@Test
	public void value_skip_del_tags() {
		//@formatter:off
		assertValue(
		"<div>" +
			"This element contains " +
			"<del>a lot of</del>" +
			" text" +
		"</div>",
		"This element contains  text");
		//@formatter:on
	}

	@Test
	public void value_skip_del_tags_in_value() {
		//@formatter:off
		assertValue(
		"<div>" +
			"This element " +
			"<span class=\"value\">" +
				"contains " +
				"<del>a lot of</del>" +
				" text" +
			"</span>" +
		"</div>",
		"contains  text");
		//@formatter:on
	}

	@Test
	public void firstValue() {
		//@formatter:off
		String html =
		"<div class=\"n\">" +
			"<div>" +
				"<span class=\"family-name\"><b>Doe</b></span>" +
			"</div>" +
		"</div>";
		//@formatter:on

		HCardElement element = build(html);

		assertEquals("Doe", element.firstValue("family-name"));
		assertNull(element.firstValue("non-existant"));
	}

	@Test
	public void allValues() {
		//@formatter:off
		String html =
		"<div class=\"n\">" +
			"<div>" +
				"<div>" +
					"<span class=\"additional-name\">Smith</span>" +
				"</div>" +
			"</div>" +
			"<span class=\"additional-name\">" +
				"(" +
				"<span class=\"value\">Barney</span>" +
				")" +
			"</span>" +
		"</div>";
		//@formatter:on

		HCardElement element = build(html);

		assertEquals(Arrays.asList("Smith", "Barney"), element.allValues("additional-name"));
		assertTrue(element.allValues("non-existant").isEmpty());
	}

	@Test
	public void types_none() {
		assertTypes("<div class=\"adr\"></div>");
	}

	@Test
	public void types_multiple() {
		//@formatter:off
		assertTypes(
		"<div class=\"adr\">" +
			"<span class=\"type\">work</span>" +
			"<span class=\"type\">pref</span>" +
		"</div>",
		"work", "pref");
		//@formatter:on
	}

	@Test
	public void types_convert_to_lower_case() {
		//@formatter:off
		assertTypes(
		"<div class=\"adr\">" +
			"<span class=\"type\">wOrk</span>" +
			"<span class=\"type\">prEf</span>" +
		"</div>",
		"work", "pref");
		//@formatter:on
	}

	@Test
	public void types_not_direct_descendant() {
		//@formatter:off
		assertTypes(
		"<div class=\"adr\">" +
			"<span class=\"type\">work</span>" +
			"<div>" +
				"<span class=\"type\">pref</span>" +
			"<div>" +
		"</div>",
		"work", "pref");
		//@formatter:on
	}

	@Test
	public void absUrl() {
		HCardElement element = build("<a href=\"data:foo\" />", "http://example.com");
		assertEquals("data:foo", element.absUrl("href"));
		assertEquals("", element.absUrl("non-existant"));

		element = build("<a href=\"index.html\" />", "http://example.com");
		assertEquals("http://example.com/index.html", element.absUrl("href"));

		element = build("<a href=\"mailto:jdoe@hotmail.com\" />", "http://example.com");
		assertEquals("mailto:jdoe@hotmail.com", element.absUrl("href"));

		element = build("<a href=\"http://foobar.com/index.html\" />", "http://example.com");
		assertEquals("http://foobar.com/index.html", element.absUrl("href"));
	}

	@Test
	public void append_with_newlines() {
		HCardElement element = build("<div />");
		element.append("Append\rthis\n\ntext\r\nplease.");

		Iterator<Node> it = element.getElement().childNodes().iterator();
		assertTextNodeValue(it.next(), "Append");
		assertTagName(it.next(), "br");
		assertTextNodeValue(it.next(), "this");
		assertTagName(it.next(), "br");
		assertTagName(it.next(), "br");
		assertTextNodeValue(it.next(), "text");
		assertTagName(it.next(), "br");
		assertTextNodeValue(it.next(), "please.");
		assertFalse(it.hasNext());
	}

	@Test
	public void append_without_newlines() {
		HCardElement element = build("<div />");
		element.append("Without newlines.");

		Iterator<Node> it = element.getElement().childNodes().iterator();
		assertTextNodeValue(it.next(), "Without newlines.");
		assertFalse(it.hasNext());
	}

	private static void assertValue(String html, String expected) {
		HCardElement element = build(html);
		String actual = element.value();
		assertEquals(expected, actual);
	}

	private static void assertTypes(String html, String... expected) {
		HCardElement element = build(html);
		List<String> actual = element.types();
		assertEquals(Arrays.asList(expected), actual);
	}

	private static void assertTagName(Node node, String expected) {
		Element element = (Element) node;
		String actual = element.tagName();
		assertEquals(expected, actual);
	}

	private static void assertTextNodeValue(Node node, String expected) {
		TextNode textNode = (TextNode) node;
		String actual = textNode.text();
		assertEquals(expected, actual);
	}

	private static HCardElement build(String html) {
		return build(html, null);
	}

	private static HCardElement build(String html, String baseUrl) {
		return new HCardElement(HtmlUtils.toElement(html, baseUrl));
	}
}
