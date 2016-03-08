package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

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
public class HtmlUtilsTest {
	@Test
	public void isChildOf() {
		//@formatter:off
		String html =
		"<html><body>" +
			"<div id=\"one\"></div>" +
			"<div id=\"two\">" +
				"<div id=\"three\"></div>" +
			"</div>" +
		"</body></html>";
		//@formatter:on
		Document document = Jsoup.parse(html);

		Element one = document.getElementById("one");
		Element two = document.getElementById("two");
		Element three = document.getElementById("three");

		Elements elements = new Elements(one, two);
		assertTrue(HtmlUtils.isChildOf(three, elements));

		elements = new Elements(one, three);
		assertFalse(HtmlUtils.isChildOf(two, elements));
	}

	@Test
	public void toElement_without_base_url() {
		Element element = HtmlUtils.toElement("<img src=\"image.png\" />");
		assertEquals(element.tagName(), "img");
		assertEquals("", element.absUrl("src"));
	}

	@Test
	public void toElement_with_base_url() {
		Element element = HtmlUtils.toElement("<img src=\"image.png\" />", "http://example.com/");
		assertEquals(element.tagName(), "img");
		assertEquals("http://example.com/image.png", element.absUrl("src"));
	}
}
