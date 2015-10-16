package ezvcard.io.scribe;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Impp;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 */

/**
 * Marshals {@link Impp} properties.
 * @author Michael Angstadt
 */
public class ImppScribe extends VCardPropertyScribe<Impp> {
	public static final String AIM = "aim";
	public static final String ICQ = "icq";
	public static final String IRC = "irc";
	public static final String MSN = "msnim";
	public static final String SIP = "sip";
	public static final String SKYPE = "skype";
	public static final String XMPP = "xmpp";
	public static final String YAHOO = "ymsgr";

	public ImppScribe() {
		super(Impp.class, "IMPP");
	}

	@Override
	protected void _prepareParameters(Impp property, VCardParameters copy, VCardVersion version, VCard vcard) {
		handlePrefParam(property, copy, version, vcard);
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.URI;
	}

	@Override
	protected String _writeText(Impp property, VCardVersion version) {
		return write(property);
	}

	@Override
	protected Impp _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value);
	}

	@Override
	protected void _writeXml(Impp property, XCardElement parent) {
		parent.append(VCardDataType.URI, write(property));
	}

	@Override
	protected Impp _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String value = element.first(VCardDataType.URI);
		if (value != null) {
			return parse(value);
		}

		throw missingXmlElements(VCardDataType.URI);
	}

	@Override
	protected Impp _parseHtml(HCardElement element, List<String> warnings) {
		String href = element.attr("href");
		if (href.length() == 0) {
			href = element.value();
		}

		try {
			URI uri = parseHtmlLink(href);
			if (uri == null) {
				throw new IllegalArgumentException();
			}
			return new Impp(uri);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(14, href);
		}
	}

	@Override
	protected JCardValue _writeJson(Impp property) {
		return JCardValue.single(write(property));
	}

	@Override
	protected Impp _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		return parse(value.asSingle());
	}

	private String write(Impp property) {
		URI uri = property.getUri();
		return (uri == null) ? "" : uri.toString();
	}

	private Impp parse(String value) {
		if (value == null || value.length() == 0) {
			return new Impp((URI) null);
		}

		try {
			return new Impp(value);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(15, value, e.getMessage());
		}
	}

	/**
	 * List of recognized IM protocols that can be parsed from an HTML link
	 * (hCard).
	 */
	private static final List<HtmlLinkFormat> htmlLinkFormats;
	static {
		List<HtmlLinkFormat> list = new ArrayList<HtmlLinkFormat>();

		//http://en.wikipedia.org/wiki/AOL_Instant_Messenger#URI_scheme
		list.add(new HtmlLinkFormat(AIM, "(goim|addbuddy)\\?.*?\\bscreenname=(.*?)(&|$)", 2, "goim?screenname=%s"));

		//http://en.wikipedia.org/wiki/Yahoo!_Messenger#URI_scheme
		list.add(new HtmlLinkFormat(YAHOO, "(sendim|addfriend|sendfile|call)\\?(.*)", 2, "sendim?%s"));

		//http://developer.skype.com/skype-uri/skype-uri-ref-api
		list.add(new HtmlLinkFormat(SKYPE, "(.*?)(\\?|$)", 1, "%s"));

		//http://www.tech-recipes.com/rx/1157/msn-messenger-msnim-hyperlink-command-codes/
		list.add(new HtmlLinkFormat(MSN, "(chat|add|voice|video)\\?contact=(.*?)(&|$)", 2, "chat?contact=%s"));

		//http://www.tech-recipes.com/rx/1157/msn-messenger-msnim-hyperlink-command-codes/
		list.add(new HtmlLinkFormat(XMPP, "(.*?)(\\?|$)", 1, "%s?message"));

		//http://forums.miranda-im.org/showthread.php?26589-Add-support-to-quot-icq-message-uin-12345-quot-web-links
		list.add(new HtmlLinkFormat(ICQ, "message\\?uin=(\\d+)", 1, "message?uin=%s"));

		//SIP: http://en.wikipedia.org/wiki/Session_Initiation_Protocol
		//leave as-is
		list.add(new HtmlLinkFormat(SIP));

		//IRC: http://stackoverflow.com/questions/11970897/how-do-i-open-a-query-window-using-the-irc-uri-scheme
		//IRC handles are not globally unique, so leave as-is
		list.add(new HtmlLinkFormat(IRC));

		htmlLinkFormats = Collections.unmodifiableList(list);
	}

	/**
	 * Parses an IM URI from an HTML link.
	 * @param linkUri the HTML link (e.g. "aim:goim?screenname=theuser")
	 * @return the IM URI or null if not recognized
	 */
	public URI parseHtmlLink(String linkUri) {
		for (HtmlLinkFormat format : htmlLinkFormats) {
			String handle = format.parseHandle(linkUri);
			if (handle == null) {
				continue;
			}

			try {
				return new URI(format.getProtocol(), handle, null);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return null;
	}

	/**
	 * Builds a URI suitable for use as a link on a webpage.
	 * @param property the property
	 * @return the link URI (e.g. "aim:goim?screenname=theuser") or null if the
	 * property has no URI
	 */
	public String writeHtmlLink(Impp property) {
		URI uri = property.getUri();
		if (uri == null) {
			return null;
		}

		String protocol = uri.getScheme();
		String handle = uri.getSchemeSpecificPart();

		for (HtmlLinkFormat format : htmlLinkFormats) {
			if (protocol.equals(format.getProtocol())) {
				return format.buildLink(handle);
			}
		}
		return uri.toString();
	}

	private static class HtmlLinkFormat {
		private final Pattern parseRegex;
		private final String protocol;
		private final int handleGroup;
		private final String linkFormat;

		/**
		 * @param protocol the IM protocol (e.g. "aim")
		 */
		public HtmlLinkFormat(String protocol) {
			this(protocol, "(.*)", 1, "%s");
		}

		/**
		 * @param protocol the IM protocol (e.g. "aim")
		 * @param linkRegex the regular expression used to parse a link
		 * @param handleGroup the group number from the regular expression that
		 * contains the IM handle
		 * @param linkFormat the format string for building a link
		 */
		public HtmlLinkFormat(String protocol, String linkRegex, int handleGroup, String linkFormat) {
			this.parseRegex = Pattern.compile('^' + protocol + ':' + linkRegex, Pattern.CASE_INSENSITIVE);
			this.protocol = protocol;
			this.handleGroup = handleGroup;
			this.linkFormat = protocol + ':' + linkFormat;
		}

		/**
		 * Parses the IM handle out of a link.
		 * @param linkUri the link
		 * @return the IM handle or null if it can't be found
		 */
		public String parseHandle(String linkUri) {
			Matcher m = parseRegex.matcher(linkUri);
			return m.find() ? m.group(handleGroup) : null;
		}

		/**
		 * Builds a link for inclusion in a webpage.
		 * @param handle the IM handle
		 * @return the link
		 */
		public String buildLink(String handle) {
			return String.format(linkFormat, handle);
		}

		/**
		 * Gets the protocol.
		 * @return the protocol (e.g. "aim")
		 */
		public String getProtocol() {
			return protocol;
		}
	}
}
