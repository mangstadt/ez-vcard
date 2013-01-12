package ezvcard.util;

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
public class HCardElementTest {
	private final String newline = System.getProperty("line.separator");

	@Test
	public void value_text_content() {
		HCardElement element = build("<div>The text</div>");
		assertEquals("The text", element.value());
	}

	@Test
	public void value_line_breaks() {
		HCardElement element = build("<div>The<br>text</div>");
		assertEquals("The" + newline + "text", element.value());

		//"value" element
		element = build("<div>The <span class=\"value\">real<br>text</span></div>");
		assertEquals("real" + newline + "text", element.value());

		//nested "value" element
		element = build("<div>The <span class=\"value\">real<br>text <span class=\"value\">goes<br>here</span></span></div>");
		assertEquals("real" + newline + "text goes" + newline + "here", element.value());
	}

	@Test
	public void value_ignore_child_tags() {
		HCardElement element = build("<div>The<b>text</b></div>");
		assertEquals("Thetext", element.value());
	}

	@Test
	public void value_ignore_type_text() {
		HCardElement element = build("<div><span class=\"type\">Work</span> is boring.</div>");
		assertEquals("is boring.", element.value());

		element = build("<div><b>All <span class=\"type\">work</span> is boring.</b></div>");
		assertEquals("All  is boring.", element.value());
	}

	@Test
	public void value_value_tag() {
		HCardElement element = build("<div>This is <span class=\"value\">the text</span>.</div>");
		assertEquals("the text", element.value());
	}

	@Test
	public void value_multiple_value_tags() {
		HCardElement element = build("<div><span class=\"value\">+1</span>.<span class=\"value\">415</span>.<span class=\"value\">555</span>.<span class=\"value\">1212</span></div>");
		assertEquals("+14155551212", element.value());
	}

	@Test
	public void value_multiple_value_tags_not_direct_child() {
		//@formatter:off
		String html = "";
		html += "<div>";
			html += "<div>Some text</div>";
			html += "<div>";
				html += "<span class=\"value\">This is</span>";
				html += "<div>";
					html += "<div class=\"value\">the value</div>";
				html += "</div>";
				html += "<div class=\"value\">of the element.</div>";
			html += "</div>";
		html += "</div>";
		
		HCardElement element = build(html);
		assertEquals("This isthe valueof the element.", element.value());
	}
	
	@Test
	public void value_nested_value_tags() {
		//@formatter:off
		String html = "";
		html += "<div>";
			html += "<div class=\"value\">the value<span class=\"value\">nested</span></div>";
		html += "</div>";
		//@formatter:on

		HCardElement element = build(html);
		assertEquals("the valuenested", element.value());
	}

	@Test
	public void value_abbr_value() {
		HCardElement element = build("<div>This is <abbr class=\"value\" title=\"1234\">the text</abbr>.</div>");
		assertEquals("1234", element.value());
	}

	@Test
	public void value_abbr_tag_with_title() {
		HCardElement element = build("<abbr class=\"latitude\" title=\"48.816667\">N 48� 81.6667</abbr>");
		assertEquals("48.816667", element.value());
	}

	@Test
	public void value_abbr_tag_without_title() {
		HCardElement element = build("<abbr class=\"latitude\">N 48� 81.6667</abbr>");
		assertEquals("N 48� 81.6667", element.value());
	}

	@Test
	public void value_skip_del_tags() {
		HCardElement element = build("<div>This element contains <del>a lot of</del> text</div>");
		assertEquals("This element contains  text", element.value());
	}

	@Test
	public void value_skip_del_tags_in_value() {
		HCardElement element = build("<div>This element <span class=\"value\">contains <del>a lot of</del> text</span></div>");
		assertEquals("contains  text", element.value());
	}

	@Test
	public void firstValue() {
		//@formatter:off
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"n\">");
			html.append("<div>");
				html.append("<span class=\"family-name\"><b>Doe</b></span>");
			html.append("</div>");
		html.append("</div>");
		//@formatter:on

		HCardElement element = build(html.toString());

		assertEquals("Doe", element.firstValue("family-name"));
		assertNull(element.firstValue("non-existant"));
	}

	@Test
	public void allValues() {
		//@formatter:off
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"n\">");
			html.append("<div>");
				html.append("<div>");
					html.append("<span class=\"additional-name\">Smith</span>");
				html.append("</div>");
			html.append("</div>");
			html.append("<span class=\"additional-name\">(<span class=\"value\">Barney</span>)</span>");
		html.append("</div>");
		//@formatter:on

		HCardElement element = build(html.toString());

		assertEquals(Arrays.asList("Smith", "Barney"), element.allValues("additional-name"));
		assertTrue(element.allValues("non-existant").isEmpty());
	}

	@Test
	public void types_none() {
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"adr\">");
		html.append("</div>");

		HCardElement element = build(html.toString());
		List<String> actual = element.types();
		assertTrue(actual.isEmpty());
	}

	@Test
	public void types_multiple() {
		//@formatter:off
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"adr\">");
			html.append("<span class=\"type\">work</span>");
			html.append("<span class=\"type\">pref</span>");
		html.append("</div>");
		//@formatter:on

		HCardElement element = build(html.toString());
		assertEquals(Arrays.asList("work", "pref"), element.types());
	}

	@Test
	public void types_convert_to_lower_case() {
		//@formatter:off
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"adr\">");
			html.append("<span class=\"type\">wOrk</span>");
			html.append("<span class=\"type\">prEf</span>");
		html.append("</div>");
		//@formatter:on

		HCardElement element = build(html.toString());
		assertEquals(Arrays.asList("work", "pref"), element.types());
	}

	@Test
	public void types_not_direct_descendant() {
		//@formatter:off
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"adr\">");
			html.append("<span class=\"type\">work</span>");
			html.append("<div>");
				html.append("<span class=\"type\">pref</span>");
			html.append("<div>");
		html.append("</div>");
		//@formatter:on

		HCardElement element = build(html.toString());
		assertEquals(Arrays.asList("work", "pref"), element.types());
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
		assertEquals("Append", ((TextNode) it.next()).text());
		assertEquals("br", ((Element) it.next()).tagName());
		assertEquals("this", ((TextNode) it.next()).text());
		assertEquals("br", ((Element) it.next()).tagName());
		assertEquals("br", ((Element) it.next()).tagName());
		assertEquals("text", ((TextNode) it.next()).text());
		assertEquals("br", ((Element) it.next()).tagName());
		assertEquals("please.", ((TextNode) it.next()).text());
		assertFalse(it.hasNext());
	}

	@Test
	public void append_without_newlines() {
		HCardElement element = build("<div />");
		element.append("Without newlines.");

		Iterator<Node> it = element.getElement().childNodes().iterator();
		assertEquals("Without newlines.", ((TextNode) it.next()).text());
		assertFalse(it.hasNext());
	}

	private HCardElement build(String html) {
		return build(html, null);
	}

	private HCardElement build(String html, String baseUrl) {
		return new HCardElement(HtmlUtils.toElement(html, baseUrl));
	}
}
