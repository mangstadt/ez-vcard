package ezvcard.io.html;

import static ezvcard.util.StringUtils.NEWLINE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

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
 * Wraps hCard functionality around an HTML {@link Element} object.
 * @author Michael Angstadt
 */
public class HCardElement {
	private final Element element;

	/**
	 * Creates an hCard element.
	 * @param element the HTML element to wrap
	 */
	public HCardElement(Element element) {
		this.element = element;
	}

	/**
	 * Gets the name of the HTML element.
	 * @return the tag name
	 */
	public String tagName() {
		return element.tagName();
	}

	/**
	 * Gets an attribute value.
	 * @param name the attribute name
	 * @return the attribute value or empty string if it doesn't exist
	 */
	public String attr(String name) {
		return element.attr(name);
	}

	/**
	 * Gets the absolute URL of an attribute that has a URL.
	 * @param name the attribute name
	 * @return the absolute URL or empty string if it doesn't exist
	 */
	public String absUrl(String name) {
		String url = element.absUrl(name); //returns empty string for some protocols like "tel:" and "data:", but not for "http:" or "mailto:"
		if (url.length() == 0) {
			url = element.attr(name);
		}
		return url;
	}

	/**
	 * Gets the element's CSS classes.
	 * @return the CSS classes
	 */
	public Set<String> classNames() {
		return element.classNames();
	}

	/**
	 * Gets the hCard value of this element. The value is determined based on
	 * the following:
	 * <ol>
	 * <li>If the element is {@code <abbr>} and contains a {@code title}
	 * attribute, then the value of the {@code title} attribute is returned.</li>
	 * <li>Else, if the element contains one or more child elements that have a
	 * CSS class of {@code value}, then append together the text contents of
	 * these elements.</li>
	 * <li>Else, use the text content of the element itself.</li>
	 * </ol>
	 * All {@code <br>} tags are converted to newlines. All text within
	 * {@code <del>} tags are ignored.
	 * @return the element's hCard value
	 */
	public String value() {
		return value(element);
	}

	/**
	 * Gets the hCard value of the first descendant element that has the given
	 * CSS class name.
	 * @param cssClass the CSS class name
	 * @return the hCard value or null if not found
	 */
	public String firstValue(String cssClass) {
		Elements elements = element.getElementsByClass(cssClass);
		return elements.isEmpty() ? null : value(elements.first());
	}

	/**
	 * Gets the hCard values of all descendant elements that have the given CSS
	 * class name.
	 * @param cssClass the CSS class name
	 * @return the hCard values
	 */
	public List<String> allValues(String cssClass) {
		Elements elements = element.getElementsByClass(cssClass);
		List<String> values = new ArrayList<String>(elements.size());
		for (Element element : elements) {
			values.add(value(element));
		}
		return values;
	}

	/**
	 * Gets all type values (for example, "home" and "cell" for the "tel" type).
	 * @return the type values (in lower-case)
	 */
	public List<String> types() {
		List<String> types = allValues("type");
		List<String> lowerCaseTypes = new ArrayList<String>(types.size());
		for (String type : types) {
			lowerCaseTypes.add(type.toLowerCase());
		}
		return lowerCaseTypes;
	}

	/**
	 * Appends text to the element, replacing newlines with {@code <br>} tags.
	 * @param text the text to append
	 */
	public void append(String text) {
		boolean first = true;
		String lines[] = text.split("\\r\\n|\\n|\\r");
		for (String line : lines) {
			if (!first) {
				//replace newlines with "<br>" tags
				element.appendElement("br");
			}

			if (line.length() > 0) {
				element.appendText(line);
			}

			first = false;
		}
	}

	/**
	 * Gets the wrapped HTML element.
	 * @return the wrapped HTML element
	 */
	public Element getElement() {
		return element;
	}

	private String value(Element element) {
		//value of "title" attribute should be returned if it's a "<abbr>" tag
		//example: <abbr class="latitude" title="48.816667">N 48� 81.6667</abbr>
		if ("abbr".equals(element.tagName())) {
			String title = element.attr("title");
			if (title.length() > 0) {
				return title;
			}
		}

		StringBuilder value = new StringBuilder();
		Elements valueElements = element.getElementsByClass("value");
		if (valueElements.isEmpty()) {
			//get the text content of all child nodes except "type" elements
			visitForValue(element, value);
		} else {
			//append together all children whose CSS class is "value"
			for (Element valueElement : valueElements) {
				if (HtmlUtils.isChildOf(valueElement, valueElements)) {
					//ignore "value" elements that are descendants of other "value" elements
					continue;
				}

				if ("abbr".equals(valueElement.tagName())) {
					String title = valueElement.attr("title");
					if (title.length() > 0) {
						value.append(title);
						continue;
					}
				}
				visitForValue(valueElement, value);
			}
		}
		return value.toString().trim();
	}

	private void visitForValue(Element element, StringBuilder value) {
		for (Node node : element.childNodes()) {
			if (node instanceof Element) {
				Element e = (Element) node;
				if (e.classNames().contains("type")) {
					//ignore "type" elements
					continue;
				}

				if ("br".equals(e.tagName())) {
					//convert "<br>" to a newline
					value.append(NEWLINE);
					continue;
				}

				if ("del".equals(e.tagName())) {
					//skip "<del>" tags
					continue;
				}

				visitForValue(e, value);
				continue;
			}

			if (node instanceof TextNode) {
				TextNode t = (TextNode) node;
				value.append(t.text());
				continue;
			}
		}
	}
}
