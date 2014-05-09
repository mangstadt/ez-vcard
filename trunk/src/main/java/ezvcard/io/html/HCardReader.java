package ezvcard.io.html;

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
import ezvcard.io.ParseWarnings;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.property.Address;
import ezvcard.property.Categories;
import ezvcard.property.Email;
import ezvcard.property.Impp;
import ezvcard.property.Label;
import ezvcard.property.Nickname;
import ezvcard.property.RawProperty;
import ezvcard.property.Source;
import ezvcard.property.Telephone;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.util.HtmlUtils;
import ezvcard.util.IOUtils;

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
 * <p>
 * Parses {@link VCard} objects from an HTML page (hCard format).
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * URL url = new URL("http://example.com");
 * HCardReader hcardReader = new HCardReader(url);
 * VCard vcard;
 * while ((vcard = hcardReader.readNext()) != null){
 *   ...
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a
 * href="http://microformats.org/wiki/hcard">http://microformats.org/wiki/hcard</a>
 */
public class HCardReader {
	private ScribeIndex index = new ScribeIndex();
	private String pageUrl;
	private final ParseWarnings warnings = new ParseWarnings();
	private Elements vcardElements;
	private Iterator<Element> it;
	private final List<Label> labels = new ArrayList<Label>();
	private VCard curVCard;
	private Elements embeddedVCards = new Elements();
	private Nickname nickname;
	private Categories categories;

	private final String urlPropertyName = index.getPropertyScribe(Url.class).getPropertyName().toLowerCase();
	private final String categoriesName = index.getPropertyScribe(Categories.class).getPropertyName().toLowerCase();
	private final String emailName = index.getPropertyScribe(Email.class).getPropertyName().toLowerCase();
	private final String telName = index.getPropertyScribe(Telephone.class).getPropertyName().toLowerCase();

	/**
	 * Creates a reader that parses hCards from a URL.
	 * @param url the URL of the webpage
	 * @throws IOException if there's a problem loading the webpage
	 */
	public HCardReader(URL url) throws IOException {
		pageUrl = url.toString();
		Document document = Jsoup.parse(url, 30000);
		init(document, url.getRef());
	}

	/**
	 * Creates a reader that parses hCards from an input stream.
	 * @param in the input stream to the HTML page
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardReader(InputStream in) throws IOException {
		this(in, null);
	}

	/**
	 * Creates a reader that parses hCards from an input stream.
	 * @param in the input stream to the HTML page
	 * @param pageUrl the original URL of the HTML page
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardReader(InputStream in, String pageUrl) throws IOException {
		this.pageUrl = pageUrl;
		Document document = (pageUrl == null) ? Jsoup.parse(in, null, "") : Jsoup.parse(in, null, pageUrl);
		String anchor = getAnchor(pageUrl);
		init(document, anchor);
	}

	/**
	 * Creates a reader that parses hCards from a file.
	 * @param file the HTML file
	 * @throws IOException if there's a problem reading the HTML file
	 */
	public HCardReader(File file) throws IOException {
		this(file, null);
	}

	/**
	 * Creates a reader that parses hCards from a file.
	 * @param file the HTML file
	 * @param pageUrl the original URL of the HTML page
	 * @throws IOException if there's a problem reading the HTML file
	 */
	public HCardReader(File file, String pageUrl) throws IOException {
		this.pageUrl = pageUrl;
		Document document = (pageUrl == null) ? Jsoup.parse(file, null, "") : Jsoup.parse(file, null, pageUrl);
		String anchor = getAnchor(pageUrl);
		init(document, anchor);
	}

	/**
	 * Creates a reader that parses hCards from a reader.
	 * @param reader the input stream to the HTML page
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardReader(Reader reader) throws IOException {
		this(reader, null);
	}

	/**
	 * Creates a reader that parses hCards from a reader.
	 * @param reader the input stream to the HTML page
	 * @param pageUrl the original URL of the HTML page
	 * @throws IOException if there's a problem reading the HTML page
	 */
	public HCardReader(Reader reader, String pageUrl) throws IOException {
		this(IOUtils.toString(reader), pageUrl);
	}

	/**
	 * Creates a reader that parses hCards from a string.
	 * @param html the HTML page
	 */
	public HCardReader(String html) {
		this(html, null);
	}

	/**
	 * Creates a reader that parses hCards from a string.
	 * @param html the HTML page
	 * @param pageUrl the original URL of the HTML page
	 */
	public HCardReader(String html, String pageUrl) {
		this.pageUrl = pageUrl;

		Document document = (pageUrl == null) ? Jsoup.parse(html) : Jsoup.parse(html, pageUrl);
		String anchor = getAnchor(pageUrl);
		init(document, anchor);
	}

	/**
	 * Constructor for reading embedded vCards.
	 * @param embeddedVCard the HTML element of the embedded vCard
	 * @param pageUrl the original URL of the HTML page
	 */
	private HCardReader(Element embeddedVCard, String pageUrl) {
		this.pageUrl = pageUrl;
		vcardElements = new Elements(embeddedVCard);
		it = vcardElements.iterator();
	}

	private void init(Document document, String anchor) {
		Element searchIn = null;
		if (anchor != null) {
			searchIn = document.getElementById(anchor);
		}
		if (searchIn == null) {
			searchIn = document;
		}

		vcardElements = searchIn.getElementsByClass("vcard");
		it = vcardElements.iterator();
	}

	/**
	 * Gets the anchor part of a URL.
	 * @param urlStr the URL
	 * @return the anchor (e.g. "foo" from the URL
	 * "http://example.com/index.php#foo")
	 */
	private static String getAnchor(String urlStr) {
		if (urlStr == null) {
			return null;
		}

		try {
			URL url = new URL(urlStr);
			return url.getRef();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(VCardPropertyScribe<? extends VCardProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the scribe index.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

	/**
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return warnings.copy();
	}

	/**
	 * Reads the next vCard from the data stream.
	 * @return the next vCard or null if there are no more
	 */
	public VCard readNext() {
		Element vcardElement = null;
		while (it.hasNext() && vcardElement == null) {
			vcardElement = it.next();

			//if this element is a child of another "vcard" element, then ignore it because it's an embedded vcard
			if (HtmlUtils.isChildOf(vcardElement, vcardElements)) {
				vcardElement = null;
			}
		}
		if (vcardElement == null) {
			return null;
		}

		warnings.clear();
		labels.clear();
		nickname = null;
		categories = null;

		curVCard = new VCard();
		curVCard.setVersion(VCardVersion.V3_0);
		if (pageUrl != null) {
			curVCard.addSource(new Source(pageUrl));
		}

		//visit all descendant nodes, depth-first
		for (Element child : vcardElement.children()) {
			visit(child);
		}

		//assign labels to their addresses
		for (Label label : labels) {
			boolean orphaned = true;
			for (Address adr : curVCard.getAddresses()) {
				if (adr.getLabel() == null && adr.getTypes().equals(label.getTypes())) {
					adr.setLabel(label.getValue());
					orphaned = false;
					break;
				}
			}
			if (orphaned) {
				curVCard.addOrphanedLabel(label);
			}
		}

		return curVCard;
	}

	private void visit(Element element) {
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
							curVCard.addProperty(result.getProperty());
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
						curVCard.addProperty(nickname);
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
						curVCard.addProperty(categories);
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
				if (HtmlUtils.isChildOf(element, embeddedVCards)) {
					//prevents multiple-nested embedded elements from overwriting each other
					continue;
				}

				property = e.getProperty();

				embeddedVCards.add(element);
				HCardReader embeddedReader = new HCardReader(element, pageUrl);
				try {
					VCard embeddedVCard = embeddedReader.readNext();
					e.injectVCard(embeddedVCard);
				} finally {
					for (String warning : embeddedReader.getWarnings()) {
						warnings.add(null, className, 26, warning);
					}
				}
			}

			curVCard.addProperty(property);
		}

		for (Element child : element.children()) {
			visit(child);
		}
	}
}
