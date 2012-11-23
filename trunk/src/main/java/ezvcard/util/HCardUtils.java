package ezvcard.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * Contains helper methods for dealing with hCards.
 * @author Michael Angstadt
 */
public class HCardUtils {
	/**
	 * Gets the hCard value of an element. The value is determined based on the
	 * following:
	 * <ol>
	 * <li>If the element is "&lt;abbr&gt;" and contains a "title" attribute,
	 * then use the value of the "title" attribute.</li>
	 * <li>Else, if the element contains one or more child elements that have a
	 * CSS class of "value", then append together the text contents of these
	 * elements.</li>
	 * <li>Else, use the text content of the element itself.</li>
	 * </ol>
	 * @param element the HTML element
	 * @return the element's hCard value
	 */
	public static String getElementValue(Element element) {
		//value of "title" attribute should be returned if it's a "<abbr>" tag
		//example: <abbr class="latitude" title="48.816667">N 48¡ 81.6667</abbr>
		if ("abbr".equalsIgnoreCase(element.tagName())) {
			String title = element.attr("title");
			if (title.length() > 0) {
				return title;
			}
		}

		StringBuilder value = new StringBuilder();
		Elements children = element.getElementsByClass("value");
		if (children.isEmpty()) {
			//get the text content of all child nodes except "type" elements
			for (Node node : element.childNodes()) {
				if (node instanceof Element) {
					Element e = (Element) node;
					if (!e.classNames().contains("type")) {
						value.append(e.text());
					}
				} else if (node instanceof TextNode) {
					TextNode t = (TextNode) node;
					value.append(t.text());
				}
			}
		} else {
			//append together all children whose CSS class is "value"
			for (Element child : children) {
				value.append(child.text());
			}
		}
		return value.toString();

	}

	/**
	 * Gets the hCard values of all the given elements and indexes them by CSS
	 * class.
	 * @param elements the HTML elements to get the values of
	 * @return the values of all the elements, indexed by CSS class
	 */
	public static ListMultimap<String, String> getElementValuesAndIndexByCssClass(Elements elements) {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		for (Element element : elements) {
			Set<String> classNames = element.classNames();
			if (classNames.isEmpty()) {
				continue;
			}

			String value = getElementValue(element);
			for (String className : classNames) {
				map.put(className, value);
			}
		}
		return map;
	}

	/**
	 * Gets the "type" parameter values associated with the given element.
	 * @param element the HTML element
	 * @return the type values in lower-case
	 */
	public static List<String> getTypes(Element element) {
		List<String> types = new ArrayList<String>();
		for (Element child : element.children()) {
			Set<String> classes = child.classNames();
			if (classes.contains("type")) {
				types.add(getElementValue(child).toLowerCase());
			}
		}
		return types;
	}

	private HCardUtils() {
		//hide constructor
	}
}
