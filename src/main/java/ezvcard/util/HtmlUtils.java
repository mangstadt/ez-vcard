package ezvcard.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
 * Generic HTML utility methods.
 * @author Michael Angstadt
 */
public class HtmlUtils {
	/**
	 * Determines whether the given element is a child of one of the given
	 * parent elements.
	 * @param child the child element
	 * @param possibleParents the possible parents
	 * @return true if it is a child, false if not
	 */
	public static boolean isChildOf(Element child, Elements possibleParents) {
		for (Element parent : child.parents()) {
			if (possibleParents.contains(parent)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts an HTML string to an HTML element.
	 * @param html the HTML
	 * @return the HTML element
	 */
	public static Element toElement(String html) {
		return toElement(html, null);
	}

	/**
	 * Converts an HTML string to an HTML element.
	 * @param html the HTML
	 * @param baseUrl the base URL
	 * @return the HTML element
	 */
	public static Element toElement(String html, String baseUrl) {
		Document d = (baseUrl == null) ? Jsoup.parse(html) : Jsoup.parse(html, baseUrl);
		return d.getElementsByTag("body").first().children().first();
	}

	private HtmlUtils() {
		//hide
	}
}
