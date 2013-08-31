package ezvcard.types;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ImppTypeParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;

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
 * An instant message handle. The handle is represented as a URI in the format "
 * <code>&lt;IM-PROTOCOL&gt;:&lt;IM-HANDLE&gt;</code>". For example, someone
 * with a Yahoo! Messenger handle of "johndoe@yahoo.com" would have an IMPP
 * vCard property value of "ymsgr:johndoe@yahoo.com".
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * //URI
 * ImppType impp = new ImppType(&quot;aim:johndoe@aol.com&quot;);
 * vcard.addImpp(impp);
 * 
 * //static factory methods
 * impp = ImppType.msn(&quot;janedoe@msn.com&quot;);
 * vcard.addImpp(impp);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>IMPP</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>3.0, 4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class ImppType extends MultiValuedTypeParameterType<ImppTypeParameter> implements HasAltId {
	public static final String NAME = "IMPP";

	private static final String AIM = "aim";
	private static final String ICQ = "icq";
	private static final String IRC = "irc";
	private static final String MSN = "msnim";
	private static final String SIP = "sip";
	private static final String SKYPE = "skype";
	private static final String XMPP = "xmpp";
	private static final String YAHOO = "ymsgr";

	/**
	 * List of recognized IM protocols that can be parsed from an HTML link
	 * (hCard).
	 */
	private static final List<ImHtmlLink> htmlParseableProtocols = new ArrayList<ImHtmlLink>();
	static {
		//http://en.wikipedia.org/wiki/AOL_Instant_Messenger#URI_scheme
		htmlParseableProtocols.add(new ImHtmlLink(AIM, "(goim|addbuddy)\\?.*?\\bscreenname=(.*?)(&|$)", 2, "goim?screenname=%s"));

		//http://en.wikipedia.org/wiki/Yahoo!_Messenger#URI_scheme
		htmlParseableProtocols.add(new ImHtmlLink(YAHOO, "(sendim|addfriend|sendfile|call)\\?(.*)", 2, "sendim?%s"));

		//http://developer.skype.com/skype-uri/skype-uri-ref-api
		htmlParseableProtocols.add(new ImHtmlLink(SKYPE, "(.*?)(\\?|$)", 1, "%s"));

		//http://www.tech-recipes.com/rx/1157/msn-messenger-msnim-hyperlink-command-codes/
		htmlParseableProtocols.add(new ImHtmlLink(MSN, "(chat|add|voice|video)\\?contact=(.*?)(&|$)", 2, "chat?contact=%s"));

		//http://www.tech-recipes.com/rx/1157/msn-messenger-msnim-hyperlink-command-codes/
		htmlParseableProtocols.add(new ImHtmlLink(XMPP, "(.*?)(\\?|$)", 1, "%s?message"));

		//http://forums.miranda-im.org/showthread.php?26589-Add-support-to-quot-icq-message-uin-12345-quot-web-links
		htmlParseableProtocols.add(new ImHtmlLink(ICQ, "message\\?uin=(\\d+)", 1, "message?uin=%s"));

		//SIP: http://en.wikipedia.org/wiki/Session_Initiation_Protocol
		//leave as-is
		htmlParseableProtocols.add(new ImHtmlLink(SIP));

		//IRC: http://stackoverflow.com/questions/11970897/how-do-i-open-a-query-window-using-the-irc-uri-scheme
		//IRC handles are not globally unique, so leave as-is
		htmlParseableProtocols.add(new ImHtmlLink(IRC));
	}

	private URI uri;

	/**
	 * Creates an empty IMPP property.
	 */
	public ImppType() {
		super(NAME);
	}

	/**
	 * Creates an IMPP property. Note that this class has static factory methods
	 * for creating IMPP types of common IM protocols.
	 * @param uri the IM URI (e.g. "aim:johndoe@aol.com")
	 * @throws IllegalArgumentException if the URI is not a valid URI
	 */
	public ImppType(String uri) {
		this();
		setUri(uri);
	}

	/**
	 * Constructs a new IMPP type. Note that this class has static factory
	 * methods for creating IMPP types of common IM protocols.
	 * @param uri the IM URI (e.g. "aim:johndoe@aol.com")
	 */
	public ImppType(URI uri) {
		this();
		setUri(uri);
	}

	/**
	 * Constructs a new IMPP type. Note that this class has static factory
	 * methods for creating IMPP types of common IM protocols.
	 * @param protocol the IM protocol (e.g. "aim")
	 * @param handle the IM handle (e.g. "johndoe@aol.com")
	 */
	public ImppType(String protocol, String handle) {
		this();
		setUri(protocol, handle);
	}

	/**
	 * Creates an IMPP property that contains a AOL Instant Messenger handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType aim(String handle) {
		return new ImppType(AIM, handle);
	}

	/**
	 * Determines if this IMPP property contains an AOL Instant Messenger
	 * handle.
	 * @return true if it contains an AOL Instant Messenger handle, false if not
	 */
	public boolean isAim() {
		return isProtocol(AIM);
	}

	/**
	 * Creates an IMPP property that contains a Yahoo! Messenger handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType yahoo(String handle) {
		return new ImppType(YAHOO, handle);
	}

	/**
	 * Determines if this IMPP property contains a Yahoo! Messenger handle.
	 * @return true if it contains a Yahoo! Messenger handle, false if not
	 */
	public boolean isYahoo() {
		return isProtocol(YAHOO);
	}

	/**
	 * Creates an IMPP property that contains an MSN IMPP property.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType msn(String handle) {
		return new ImppType(MSN, handle);
	}

	/**
	 * Determines if this IMPP property contains an MSN handle.
	 * @return true if it contains an MSN handle, false if not
	 */
	public boolean isMsn() {
		return isProtocol(MSN);
	}

	/**
	 * Creates an IMPP property that contains an ICQ handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType icq(String handle) {
		return new ImppType(ICQ, handle);
	}

	/**
	 * Determines if this IMPP property contains an ICQ handle.
	 * @return true if it contains an ICQ handle, false if not
	 */
	public boolean isIcq() {
		return isProtocol(ICQ);
	}

	/**
	 * Creates an IMPP property that contains an IRC handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType irc(String handle) {
		return new ImppType(IRC, handle);
	}

	/**
	 * Determines if this IMPP property contains an IRC handle.
	 * @return true if it contains an IRC handle, false if not
	 */
	public boolean isIrc() {
		return isProtocol(IRC);
	}

	/**
	 * Creates an IMPP property that contains a Session Initiation Protocol
	 * handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType sip(String handle) {
		return new ImppType(SIP, handle);
	}

	/**
	 * Determines if this IMPP property contains a Session Initiation Protocol
	 * handle.
	 * @return true if it contains a SIP handle, false if not
	 */
	public boolean isSip() {
		return isProtocol(SIP);
	}

	/**
	 * Creates an IMPP property that contains a Skype handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType skype(String handle) {
		return new ImppType(SKYPE, handle);
	}

	/**
	 * Determines if this IMPP property contains a Skype handle.
	 * @return true if it contains a Skype handle, false if not
	 */
	public boolean isSkype() {
		return isProtocol(SKYPE);
	}

	/**
	 * Creates an IMPP property that contains an Extensible Messaging and
	 * Presence Protocol handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType xmpp(String handle) {
		return new ImppType(XMPP, handle);
	}

	/**
	 * Determines if this IMPP property contains an Extensible Messaging and
	 * Presence Protocol handle.
	 * @return true if it contains an XMPP handle, false if not
	 */
	public boolean isXmpp() {
		return isProtocol(XMPP);
	}

	private boolean isProtocol(String protocol) {
		return uri != null && protocol.equals(uri.getScheme());
	}

	/**
	 * Gets the IM URI.
	 * @return the IM URI
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * Sets the IM URI.
	 * @param uri the IM URI (e.g. "aim:theuser@aol.com")
	 * @throws IllegalArgumentException if the URI is not a valid URI
	 */
	public void setUri(String uri) {
		setUri(URI.create(uri));
	}

	/**
	 * Sets the IM URI.
	 * @param uri the IM URI (e.g. "aim:theuser@aol.com")
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * Sets the IM URI.
	 * @param protocol the IM protocol (e.g. "aim")
	 * @param handle the IM handle (e.g. "theuser@aol.com")
	 */
	public void setUri(String protocol, String handle) {
		try {
			this.uri = new URI(protocol, handle, null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Gets the IM protocol. Use {@link #setUri(String, String)} to set the
	 * protocol.
	 * @return the IM protocol (e.g. "aim") or null if not set
	 */
	public String getProtocol() {
		if (uri == null) {
			return null;
		}
		return uri.getScheme();
	}

	/**
	 * Gets the IM handle. Use {@link #setUri(String, String)} to set the
	 * handle.
	 * @return the IM handle (e.g. "johndoe@aol.com") or null if not set
	 */
	public String getHandle() {
		if (uri == null) {
			return null;
		}
		return uri.getSchemeSpecificPart();
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return subTypes.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
	}

	@Override
	public List<Integer[]> getPids() {
		return super.getPids();
	}

	@Override
	public void addPid(int localId, int clientPidMapRef) {
		super.addPid(localId, clientPidMapRef);
	}

	@Override
	public void removePids() {
		super.removePids();
	}

	@Override
	public Integer getPref() {
		return super.getPref();
	}

	@Override
	public void setPref(Integer pref) {
		super.setPref(pref);
	}

	//@Override
	public String getAltId() {
		return subTypes.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	@Override
	protected ImppTypeParameter buildTypeObj(String type) {
		return ImppTypeParameter.get(type);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V3_0, VCardVersion.V4_0 };
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			sb.append(uri.toString());
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		parse(value);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		parent.append(VCardDataType.URI, (uri == null) ? "" : uri.toString());
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.first(VCardDataType.URI);
		if (value != null) {
			parse(value);
			return;
		}

		throw missingXmlElements(VCardDataType.URI);
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String href = element.attr("href");
		if (href.length() == 0) {
			href = element.value();
		}

		try {
			URI uri = parseUriFromLink(href);
			if (uri == null) {
				throw new IllegalArgumentException();
			}
			setUri(uri);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("Could not parse instant messenger information from link: " + href);
		}
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		return JCardValue.single(VCardDataType.URI, (uri == null) ? "" : uri.toString());
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		parse(value.getSingleValued());
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (uri == null) {
			warnings.add("Property value is null.");
		}
	}

	private void parse(String value) {
		if (value == null || value.length() == 0) {
			return;
		}

		try {
			setUri(value);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("Cannot parse URI \"" + value + "\": " + e.getMessage());
		}
	}

	/**
	 * Parses an IM URI from an HTML link.
	 * @param linkUri the HTML link (e.g. "aim:goim?screenname=theuser")
	 * @return the IM URI or null if not recognized
	 */
	static URI parseUriFromLink(String linkUri) {
		for (ImHtmlLink imLink : htmlParseableProtocols) {
			String handle = imLink.parseHandle(linkUri);
			if (handle == null) {
				continue;
			}

			try {
				return new URI(imLink.getProtocol(), handle, null);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return null;
	}

	/**
	 * Builds a URI suitable for use as a link on a webpage.
	 * @return the link URI (e.g. "aim:goim?screenname=theuser") or null if the
	 * IMPP URI was never set
	 */
	public String buildLink() {
		if (uri == null) {
			return null;
		}

		String protocol = uri.getScheme();
		String handle = uri.getSchemeSpecificPart();

		for (ImHtmlLink imLink : htmlParseableProtocols) {
			if (protocol.equals(imLink.getProtocol())) {
				return imLink.buildLink(handle);
			}
		}
		return uri.toString();
	}

	private static class ImHtmlLink {
		private final Pattern linkRegex;
		private final String protocol;
		private final int handleGroup;
		private final String linkFormat;

		/**
		 * @param protocol the IM protocol (e.g. "aim")
		 */
		public ImHtmlLink(String protocol) {
			this(protocol, "(.*)", 1, "%s");
		}

		/**
		 * @param protocol the IM protocol (e.g. "aim")
		 * @param linkRegex the regular expression used to parse a link
		 * @param handleGroup the group number from the regular expression that
		 * contains the IM handle
		 * @param linkFormat the format string for building a link
		 */
		public ImHtmlLink(String protocol, String linkRegex, int handleGroup, String linkFormat) {
			this.linkRegex = Pattern.compile('^' + protocol + ':' + linkRegex, Pattern.CASE_INSENSITIVE);
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
			Matcher m = linkRegex.matcher(linkUri);
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

		public String getProtocol() {
			return protocol;
		}
	}
}
