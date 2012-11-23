package ezvcard.types;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ImppTypeParameter;
import ezvcard.util.HCardUtils;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardUtils;

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
 * An instant message handle. The handle is represented as a URI in the format "
 * <code>&lt;IM-PROTOCOL&gt;:&lt;IM-HANDLE&gt;</code>". For example, someone
 * with a Yahoo! Messenger handle of "jdoe@yahoo.com" would have an IMPP vCard
 * property value of "ymsgr:jdoe@yahoo.com".
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * //URI
 * ImppType impp = new ImppType(&quot;aim:hunkydude22@aol.com&quot;);
 * vcard.addImpp(impp);
 * 
 * //static helper constructors
 * impp = ImppType.msn(&quot;steve99@msn.com&quot;);
 * vcard.addImpp(impp);
 * </pre>
 * 
 * <p>
 * Suggested URI protocols (from RFC 4770):
 * <ul>
 * <li>"aim:" - AOL Instant Messenger</li>
 * <li>"ymsgr:" - Yahoo! Messenger</li>
 * <li>"msn:" - MSN</li>
 * <li>"irc:" - IRC handle</li>
 * <li>"sip:" - Session Initiation Protocol</li>
 * <li>"xmpp:" - Extensible Messaging and Presence Protocol</li>
 * </ul>
 * </p>
 * 
 * <p>
 * vCard property name: IMPP
 * </p>
 * <p>
 * vCard versions: 4.0
 * </p>
 * @author Michael Angstadt
 */
public class ImppType extends MultiValuedTypeParameterType<ImppTypeParameter> {
	public static final String NAME = "IMPP";

	private static final String AIM = "aim:";
	private static final String YAHOO = "ymsgr:";
	private static final String MSN = "msn:";
	private static final String IRC = "irc:";
	private static final String SIP = "sip:";
	private static final String XMPP = "xmpp:";

	private String uri;

	public ImppType() {
		this(null);
	}

	/**
	 * @param uri the IM URI (e.g. "aim:johndoe@aol.com")
	 */
	public ImppType(String uri) {
		super(NAME);
		this.uri = uri;
	}

	/**
	 * Creates an AOL Instant Messenger IMPP property.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType aim(String handle) {
		return new ImppType(AIM + handle);
	}

	/**
	 * Determines if this IMPP property represents an AOL Instant Messenger
	 * handle.
	 * @return true if it's an AOL Instant Messenger handle, false if not
	 */
	public boolean isAim() {
		return uri != null && uri.startsWith(AIM);
	}

	/**
	 * Creates an Yahoo! Messenger IMPP property.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType yahoo(String handle) {
		return new ImppType(YAHOO + handle);
	}

	/**
	 * Determines if this IMPP property represents a Yahoo! Messenger handle.
	 * @return true if it's a Yahoo! Messenger handle, false if not
	 */
	public boolean isYahoo() {
		return uri != null && uri.startsWith(YAHOO);
	}

	/**
	 * Creates an MSN IMPP property.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType msn(String handle) {
		return new ImppType(MSN + handle);
	}

	/**
	 * Determines if this IMPP property represents an MSN handle.
	 * @return true if it's an MSN handle, false if not
	 */
	public boolean isMsn() {
		return uri != null && uri.startsWith(MSN);
	}

	/**
	 * Creates an IRC IMPP property.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType irc(String handle) {
		return new ImppType(IRC + handle);
	}

	/**
	 * Determines if this IMPP property represents an IRC handle.
	 * @return true if it's an IRC handle, false if not
	 */
	public boolean isIrc() {
		return uri != null && uri.startsWith(IRC);
	}

	/**
	 * Creates a Session Initiation Protocol IMPP property.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType sip(String handle) {
		return new ImppType(SIP + handle);
	}

	/**
	 * Determines if this IMPP property represents a Session Initiation Protocol
	 * handle.
	 * @return true if it's a SIP handle, false if not
	 */
	public boolean isSip() {
		return uri != null && uri.startsWith(SIP);
	}

	/**
	 * Creates an Extensible Messaging and Presence Protocol IMPP property.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static ImppType xmpp(String handle) {
		return new ImppType(XMPP + handle);
	}

	/**
	 * Determines if this IMPP property represents an Extensible Messaging and
	 * Presence Protocol handle.
	 * @return true if it's an XMPP handle, false if not
	 */
	public boolean isXmpp() {
		return uri != null && uri.startsWith(XMPP);
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return subTypes.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
	}

	/**
	 * Gets all PID parameter values.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the PID values or empty set if there are none
	 * @see VCardSubTypes#getPids
	 */
	public Set<Integer[]> getPids() {
		return subTypes.getPids();
	}

	/**
	 * Adds a PID value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param localId the local ID
	 * @param clientPidMapRef the ID used to reference the property's globally
	 * unique identifier in the CLIENTPIDMAP property.
	 * @see VCardSubTypes#addPid(int, int)
	 */
	public void addPid(int localId, int clientPidMapRef) {
		subTypes.addPid(localId, clientPidMapRef);
	}

	/**
	 * Removes all PID values.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @see VCardSubTypes#removePids
	 */
	public void removePids() {
		subTypes.removePids();
	}

	/**
	 * Gets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the preference value or null if it doesn't exist
	 * @see VCardSubTypes#getPref
	 */
	public Integer getPref() {
		return subTypes.getPref();
	}

	/**
	 * Sets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see VCardSubTypes#setPref
	 */
	public void setPref(Integer pref) {
		subTypes.setPref(pref);
	}

	/**
	 * Gets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the ALTID or null if it doesn't exist
	 * @see VCardSubTypes#getAltId
	 */
	public String getAltId() {
		return subTypes.getAltId();
	}

	/**
	 * Sets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param altId the ALTID or null to remove
	 * @see VCardSubTypes#setAltId
	 */
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	@Override
	protected ImppTypeParameter buildTypeObj(String type) {
		ImppTypeParameter param = ImppTypeParameter.valueOf(type);
		if (param == null) {
			param = new ImppTypeParameter(type);
		}
		return param;
	}

	/**
	 * Gets the IM URI.
	 * @return the IM URI
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the IM URI.
	 * @param uri the IM URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			sb.append(VCardStringUtils.escape(uri));
		}
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		uri = VCardStringUtils.unescape(value);
	}

	@Override
	protected void doMarshalValue(Element parent, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			XCardUtils.appendChild(parent, "uri", uri, version);
		}
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		uri = XCardUtils.getFirstChildText(element, "uri");
	}

	@Override
	protected void doUnmarshalHtml(org.jsoup.nodes.Element element, List<String> warnings) {
		String href = element.attr("href");
		if (href.length() == 0) {
			href = HCardUtils.getElementValue(element);
		}
		setUri(href);
	}
}
