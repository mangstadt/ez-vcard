package ezvcard.io.html;

import static ezvcard.util.HtmlUtils.isChildOf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamReader;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.property.Categories;
import ezvcard.property.Email;
import ezvcard.property.Impp;
import ezvcard.property.Label;
import ezvcard.property.Nickname;
import ezvcard.property.RawProperty;
import ezvcard.property.Telephone;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;

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
 * <p>
 * Parses {@link VCard} objects from an HTML page (hCard format).
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * URL url = new URL(&quot;http://example.com&quot;);
 * HCardParser parser = new HCardParser(url);
 * List&lt;VCard&gt; vcards = parser.parseAll();
 * </pre>
 * @author Michael Angstadt
 * @see <a
 * href="http://microformats.org/wiki/hcard">http://microformats.org/wiki/hcard</a>
 */
public class HCardParser extends StreamReader {
	private final String pageUrl;
	private final Elements vcardElements;
	private final Iterator<Element> vcardElementsIt;
	private final List<Label> labels = new ArrayList<Label>();

	private VCard vcard;
	private Elements embeddedVCards = new Elements();
	private Nickname nickname;
	private Categories categories;

	private final String urlPropertyName = index.getPropertyScribe(Url.class).getPropertyName().toLowerCase();
	private final String categoriesName = index.getPropertyScribe(Categories.class).getPropertyName().toLowerCase();
	private final String emailName = index.getPropertyScribe(Email.class).getPropertyName().toLowerCase();
	private final String telName = index.getPropertyScribe(Telephone.class).getPropertyName().toLowerCase();

	/**
	 * Creates an hCard document.
	 * @param url the URL of the webpage
	 * @throws IOException if there's a problem loading the webpage
	 */
	public HCardParser(URL url) throws IOException {
		this(Jsoup.parse(url, 30000), url.toString());
	}

	/**
	 * Creates an hCard document.
	 * @param in the input stream to the HTML page
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardParser(InputStream in) throws IOException {
		this(in, null);
	}

	/**
	 * Creates an hCard document.
	 * @param in the input stream to the HTML page
	 * @param pageUrl the original URL of the HTML page (used to resolve
	 * relative links)
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardParser(InputStream in, String pageUrl) throws IOException {
		this((pageUrl == null) ? Jsoup.parse(in, null, "") : Jsoup.parse(in, null, pageUrl), pageUrl);
	}

	/**
	 * Creates an hCard document.
	 * @param file the HTML file
	 * @throws IOException if there's a problem reading the HTML file
	 */
	public HCardParser(File file) throws IOException {
		this(file, null);
	}

	/**
	 * Creates an hCard document.
	 * @param file the HTML file
	 * @param pageUrl the original URL of the HTML page (used to resolve
	 * relative links)
	 * @throws IOException if there's a problem reading the HTML file
	 */
	public HCardParser(File file, String pageUrl) throws IOException {
		this((pageUrl == null) ? Jsoup.parse(file, null, "") : Jsoup.parse(file, null, pageUrl), pageUrl);
	}

	/**
	 * Creates an hCard document.
	 * @param reader the input stream to the HTML page
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardParser(Reader reader) throws IOException {
		this(reader, null);
	}

	/**
	 * Creates an hCard document.
	 * @param reader the input stream to the HTML page
	 * @param pageUrl the original URL of the HTML page (used to resolve
	 * relative links)
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardParser(Reader reader, String pageUrl) throws IOException {
		this(IOUtils.toString(reader), pageUrl);
	}

	/**
	 * Creates an hCard document.
	 * @param html the HTML page
	 */
	public HCardParser(String html) {
		this(html, null);
	}

	/**
	 * Creates an hCard document.
	 * @param html the HTML page
	 * @param pageUrl the original URL of the HTML page (used to resolve
	 * relative links)
	 */
	public HCardParser(String html, String pageUrl) {
		this((pageUrl == null) ? Jsoup.parse(html) : Jsoup.parse(html, pageUrl), pageUrl);
	}

	/**
	 * Creates an hCard document.
	 * @param document the HTML page
	 */
	public HCardParser(Document document) {
		this(document, null);
	}

	/**
	 * Creates an hCard document.
	 * @param document the HTML page
	 * @param pageUrl the original URL of the HTML page (used to resolve
	 * relative links)
	 */
	public HCardParser(Document document, String pageUrl) {
		this.pageUrl = pageUrl;

		String anchor = null;
		if (pageUrl != null) {
			try {
				URL url = new URL(pageUrl);
				anchor = url.getRef();
			} catch (MalformedURLException e) {
				anchor = null;
			}
		}

		Element searchUnder = null;
		if (anchor != null) {
			searchUnder = document.getElementById(anchor);
		}
		if (searchUnder == null) {
			searchUnder = document;
		}

		vcardElements = searchUnder.getElementsByClass("vcard");

		//remove nested vcard elements
		Iterator<Element> it = vcardElements.iterator();
		while (it.hasNext()) {
			Element element = it.next();
			if (isChildOf(element, vcardElements)) {
				it.remove();
			}
		}

		vcardElementsIt = vcardElements.iterator();
	}

	/**
	 * Constructor for reading embedded vCards.
	 * @param embeddedVCard the HTML element of the embedded vCard
	 * @param pageUrl the original URL of the HTML page
	 */
	private HCardParser(Element embeddedVCard, String pageUrl) {
		this.pageUrl = pageUrl;
		vcardElements = new Elements(embeddedVCard);
		vcardElementsIt = vcardElements.iterator();
	}

	@Override
	public VCard readNext() {
		try {
			return super.readNext();
		} catch (IOException e) {
			//will not be thrown because reading from DOM
			throw new RuntimeException(e);
		}
	}

	@Override
	protected VCard _readNext() {
		if (!vcardElementsIt.hasNext()) {
			return null;
		}

		parseVCardElement(vcardElementsIt.next());
		return vcard;
	}

	private void parseVCardElement(Element vcardElement) {
		labels.clear();
		nickname = null;
		categories = null;

		vcard = new VCard();
		vcard.setVersion(VCardVersion.V3_0);
		if (pageUrl != null) {
			vcard.addSource(pageUrl);
		}

		//visit all descendant nodes, depth-first
		for (Element child : vcardElement.children()) {
			visit(child);
		}

		//assign labels to their addresses
		assignLabels(vcard, labels);
	}

	private void visit(Element element) {
		boolean visitChildren = true;
		Set<String> classNames = element.classNames();
		for (String className : classNames) {
			className = className.toLowerCase();

			//give special treatment to certain URLs
			if (urlPropertyName.equals(className)) {
				String href = element.attr("href");
				if (href.length() > 0) {
					if (!classNames.contains(emailName) && href.matches("(?i)mailto:.*")) {
						className = emailName;
					} else if (!classNames.contains(telName) && href.matches("(?i)tel:.*")) {
						className = telName;
					} else {
						//try parsing as IMPP
						VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(Impp.class);
						try {
							Result<? extends VCardProperty> result = scribe.parseHtml(new HCardElement(element));
							vcard.addProperty(result.getProperty());
							for (String warning : result.getWarnings()) {
								warnings.add(null, scribe.getPropertyName(), warning);
							}
							continue;
						} catch (SkipMeException e) {
							//URL is not an instant messenger URL
						} catch (CannotParseException e) {
							//URL is not an instant messenger URL
						}
					}
				}
			}

			//hCard uses a different name for the CATEGORIES property
			if ("category".equals(className)) {
				className = categoriesName;
			}

			VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(className);
			if (scribe == null) {
				//if no scribe is found, and the class name doesn't start with "x-", then it must be an arbitrary CSS class that has nothing to do with vCard
				if (!className.startsWith("x-")) {
					continue;
				}
				scribe = new RawPropertyScribe(className);
			}

			VCardProperty property;
			try {
				Result<? extends VCardProperty> result = scribe.parseHtml(new HCardElement(element));

				for (String warning : result.getWarnings()) {
					warnings.add(null, className, warning);
				}

				property = result.getProperty();

				//LABELs must be treated specially so they can be matched up with their ADRs
				if (property instanceof Label) {
					labels.add((Label) property);
					continue;
				}

				//add all NICKNAMEs to the same type object
				if (property instanceof Nickname) {
					Nickname nn = (Nickname) property;
					if (nickname == null) {
						nickname = nn;
						vcard.addProperty(nickname);
					} else {
						nickname.getValues().addAll(nn.getValues());
					}
					continue;
				}

				//add all CATEGORIES to the same type object
				if (property instanceof Categories) {
					Categories c = (Categories) property;
					if (categories == null) {
						categories = c;
						vcard.addProperty(categories);
					} else {
						categories.getValues().addAll(c.getValues());
					}
					continue;
				}
			} catch (SkipMeException e) {
				warnings.add(null, className, 22, e.getMessage());
				continue;
			} catch (CannotParseException e) {
				String html = element.outerHtml();
				warnings.add(null, className, 32, html, e.getMessage());
				property = new RawProperty(className, html);
			} catch (EmbeddedVCardException e) {
				if (isChildOf(element, embeddedVCards)) {
					//prevents multiple-nested embedded elements from overwriting each other
					continue;
				}

				property = e.getProperty();

				embeddedVCards.add(element);
				HCardParser embeddedReader = new HCardParser(element, pageUrl);
				try {
					VCard embeddedVCard = embeddedReader.readNext();
					e.injectVCard(embeddedVCard);
				} finally {
					for (String warning : embeddedReader.getWarnings()) {
						warnings.add(null, className, 26, warning);
					}
					IOUtils.closeQuietly(embeddedReader);
				}
				visitChildren = false;
			}

			vcard.addProperty(property);
		}

		if (visitChildren) {
			for (Element child : element.children()) {
				visit(child);
			}
		}
	}

	public void close() {
		//empty
	}
}
