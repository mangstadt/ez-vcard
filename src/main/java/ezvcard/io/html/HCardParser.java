package ezvcard.io.html;

import static ezvcard.util.HtmlUtils.isChildOf;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.ParseWarning;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamReader;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.property.Categories;
import ezvcard.property.Email;
import ezvcard.property.Impp;
import ezvcard.property.Label;
import ezvcard.property.Nickname;
import ezvcard.property.RawProperty;
import ezvcard.property.Telephone;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.util.Gobble;
import ezvcard.util.HtmlUtils;
import ezvcard.util.IOUtils;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * URL url = new URL("http://example.com");
 * HCardParser parser = new HCardParser(url);
 * List&lt;VCard&gt; vcards = parser.parseAll();
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://microformats.org/wiki/hcard">http://microformats.org/
 * wiki/hcard</a>
 */
public class HCardParser extends StreamReader {
	private static final Duration urlTimeout = Duration.ofSeconds(30);

	private final String pageUrl;
	private final Iterator<Element> vcardElementsIt;
	private final List<Label> labels = new ArrayList<>();

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
		this(Jsoup.parse(url, (int) urlTimeout.toMillis()), url.toString());
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
	public HCardParser(Path file) throws IOException {
		this(file, null);
	}

	/**
	 * Creates an hCard document.
	 * @param file the HTML file
	 * @param pageUrl the original URL of the HTML page (used to resolve
	 * relative links)
	 * @throws IOException if there's a problem reading the HTML file
	 */
	public HCardParser(Path file, String pageUrl) throws IOException {
		this((pageUrl == null) ? Jsoup.parse(file.toFile(), null, "") : Jsoup.parse(file.toFile(), null, pageUrl), pageUrl);
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
		this(new Gobble(reader).asString(), pageUrl);
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

		String anchor = (pageUrl == null) ? null : HtmlUtils.getAnchorFromUrl(pageUrl);

		Element searchUnder = (anchor == null) ? null : document.getElementById(anchor);
		if (searchUnder == null) {
			searchUnder = document;
		}

		/*
		 * Nested vCards also show up in this list as separate list items. For
		 * example, if the HTML document has one vCard and that vCard has one
		 * nested vCard (i.e. AGENT property), this list will have two elements.
		 * 
		 * Exclude the nested vCards from being processed as their own,
		 * independent vCards.
		 */
		Elements vcardElementsIncludingNested = searchUnder.getElementsByClass("vcard");

		//@formatter:off
		List<Element> vcardElementsWithoutNested = vcardElementsIncludingNested.stream()
			.filter(element -> !isChildOf(element, vcardElementsIncludingNested))
		.collect(Collectors.toList());
		//@formatter:on

		vcardElementsIt = vcardElementsWithoutNested.iterator();
	}

	/**
	 * Constructor for reading embedded vCards.
	 * @param embeddedVCard the HTML element of the embedded vCard
	 * @param pageUrl the original URL of the HTML page
	 */
	private HCardParser(Element embeddedVCard, String pageUrl) {
		this.pageUrl = pageUrl;
		vcardElementsIt = new Elements(embeddedVCard).iterator();
	}

	@Override
	public VCard readNext() {
		try {
			return super.readNext();
		} catch (IOException e) {
			//will not be thrown because reading from DOM
			throw new UncheckedIOException(e);
		}
	}

	@Override
	protected VCard _readNext() {
		if (!vcardElementsIt.hasNext()) {
			return null;
		}

		context.setVersion(VCardVersion.V3_0);
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
		vcardElement.children().forEach(this::visit);

		//assign labels to their addresses
		assignLabels(vcard, labels);
	}

	private void visit(Element element) {
		int embeddedVCardCount = embeddedVCards.size();

		Set<String> classNames = adjustClassNames(element);

		classNames.forEach(className -> parseProperty(element, className));

		boolean noEmbeddedVCardsWereAdded = (embeddedVCardCount == embeddedVCards.size());
		if (noEmbeddedVCardsWereAdded) {
			//do not visit children if there are any embedded vCards
			element.children().forEach(this::visit);
		}
	}

	private Set<String> adjustClassNames(Element element) {
		//@formatter:off
		Set<String> classNamesToLower = element.classNames().stream()
			.map(String::toLowerCase)
		.collect(Collectors.toSet());

		return classNamesToLower.stream()
			.map(className -> adjustClassName(className, element, classNamesToLower))
		.collect(Collectors.toSet());
		//@formatter:on
	}

	private String adjustClassName(String className, Element element, Set<String> origClassNames) {
		/*
		 * hCard uses a different name for the CATEGORIES property.
		 */
		if ("category".equals(className)) {
			return categoriesName;
		}

		/*
		 * Give special treatment to certain URLs.
		 */
		if (urlPropertyName.equals(className)) {
			String href = element.attr("href");
			if (!origClassNames.contains(emailName) && href.matches("(?i)mailto:.*")) {
				return emailName;
			}
			if (!origClassNames.contains(telName) && href.matches("(?i)tel:.*")) {
				return telName;
			}
		}

		return className;
	}

	private VCardProperty tryToParseAsImpp(Element element) {
		String href = element.attr("href");
		if (href.isEmpty()) {
			return null;
		}

		VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(Impp.class);

		context.getWarnings().clear();
		context.setPropertyName(scribe.getPropertyName());
		try {
			VCardProperty property = scribe.parseHtml(new HCardElement(element), context);
			warnings.addAll(context.getWarnings());
			return property;
		} catch (SkipMeException | CannotParseException e) {
			//URL is not an instant messenger URL
			return null;
		}
	}

	private VCardPropertyScribe<? extends VCardProperty> getPropertyScribe(String className) {
		VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(className);

		if (scribe == null) {
			/*
			 * If no scribe is found, and the class name doesn't start with
			 * "x-", then it must be an arbitrary CSS class that has nothing to
			 * do with vCard
			 */
			if (!className.startsWith("x-")) {
				return null;
			}

			scribe = new RawPropertyScribe(className);
		}

		return scribe;
	}

	private VCard parseEmbeddedVCard(Element element) {
		embeddedVCards.add(element);
		HCardParser embeddedReader = new HCardParser(element, pageUrl);
		try {
			return embeddedReader.readNext();
		} finally {
			warnings.addAll(embeddedReader.getWarnings());
			IOUtils.closeQuietly(embeddedReader);
		}
	}

	private void parseProperty(Element element, String className) {
		if (urlPropertyName.equals(className)) {
			VCardProperty impp = tryToParseAsImpp(element);
			if (impp != null) {
				vcard.addProperty(impp);
				return;
			}
		}

		VCardPropertyScribe<? extends VCardProperty> scribe = getPropertyScribe(className);
		if (scribe == null) {
			//it's a CSS class that's unrelated to hCard
			return;
		}

		context.getWarnings().clear();
		context.setPropertyName(scribe.getPropertyName());

		VCardProperty property;
		try {
			property = scribe.parseHtml(new HCardElement(element), context);
		} catch (SkipMeException e) {
			//@formatter:off
			warnings.add(new ParseWarning.Builder(context)
				.message(22, e.getMessage())
				.build()
			);
			//@formatter:on

			return;
		} catch (CannotParseException e) {
			//@formatter:off
			warnings.add(new ParseWarning.Builder(context)
				.message(e)
				.build()
			);
			//@formatter:on

			property = new RawProperty(className, element.outerHtml());
			vcard.addProperty(property);
			return;
		} catch (EmbeddedVCardException e) {
			if (isChildOf(element, embeddedVCards)) {
				//prevents multiple-nested embedded elements from overwriting each other
				return;
			}

			property = e.getProperty();

			VCard embeddedVCard = parseEmbeddedVCard(element);
			e.injectVCard(embeddedVCard);
			vcard.addProperty(property);
			return;
		}

		warnings.addAll(context.getWarnings());

		/*
		 * LABELs must be treated specially so they can be matched up with their
		 * ADRs.
		 */
		if (property instanceof Label) {
			handleLabel((Label) property);
			return;
		}

		/*
		 * Add all NICKNAMEs to the same type object.
		 */
		if (property instanceof Nickname) {
			handleNickname((Nickname) property);
			return;
		}

		/*
		 * Add all CATEGORIES to the same type object.
		 */
		if (property instanceof Categories) {
			handleCategories((Categories) property);
			return;
		}

		vcard.addProperty(property);
	}

	private void handleLabel(Label property) {
		labels.add(property);
	}

	private void handleNickname(Nickname property) {
		if (nickname == null) {
			nickname = property;
			vcard.addProperty(nickname);
		} else {
			nickname.getValues().addAll(property.getValues());
		}
	}

	private void handleCategories(Categories property) {
		if (categories == null) {
			categories = property;
			vcard.addProperty(categories);
		} else {
			categories.getValues().addAll(property.getValues());
		}
	}

	public void close() {
		//empty
	}
}
