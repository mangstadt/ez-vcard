package ezvcard.property;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.parameter.ImppType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * <p>
 * Defines an instant messenger handle. The handle is represented as a URI in the
 * format "{@code <PROTOCOL>:<HANDLE>}". For example, a Yahoo! Messenger handle
 * of "johndoe@yahoo.com" would look like this: "ymsgr:johndoe@yahoo.com".
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //URI
 * Impp impp = new Impp(&quot;aim:johndoe@aol.com&quot;);
 * vcard.addImpp(impp);
 * 
 * //static factory methods
 * impp = Impp.msn(&quot;janedoe@msn.com&quot;);
 * vcard.addImpp(impp);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code IMPP}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Impp extends VCardProperty implements HasAltId {
	private static final String AIM = "aim";
	private static final String ICQ = "icq";
	private static final String IRC = "irc";
	private static final String MSN = "msnim";
	private static final String SIP = "sip";
	private static final String SKYPE = "skype";
	private static final String XMPP = "xmpp";
	private static final String YAHOO = "ymsgr";

	private URI uri;

	/**
	 * Creates an IMPP property. Note that this class has static factory methods
	 * for creating IMPP properties of common IM protocols.
	 * @param uri the IM URI (e.g. "aim:johndoe@aol.com")
	 * @throws IllegalArgumentException if the URI is not a valid URI
	 */
	public Impp(String uri) {
		setUri(uri);
	}

	/**
	 * Creates an IMPP property. Note that this class has static factory methods
	 * for creating IMPP properties of common IM protocols.
	 * @param uri the IM URI (e.g. "aim:johndoe@aol.com")
	 */
	public Impp(URI uri) {
		setUri(uri);
	}

	/**
	 * Creates an IMPP property. Note that this class has static factory methods
	 * for creating IMPP properties of common IM protocols.
	 * @param protocol the IM protocol (e.g. "aim")
	 * @param handle the IM handle (e.g. "johndoe@aol.com")
	 */
	public Impp(String protocol, String handle) {
		setUri(protocol, handle);
	}

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V3_0, VCardVersion.V4_0);
	}

	/**
	 * Creates an IMPP property that contains a AOL Instant Messenger handle.
	 * @param handle the IM handle
	 * @return the IMPP property instance
	 */
	public static Impp aim(String handle) {
		return new Impp(AIM, handle);
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
	public static Impp yahoo(String handle) {
		return new Impp(YAHOO, handle);
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
	public static Impp msn(String handle) {
		return new Impp(MSN, handle);
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
	public static Impp icq(String handle) {
		return new Impp(ICQ, handle);
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
	public static Impp irc(String handle) {
		return new Impp(IRC, handle);
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
	public static Impp sip(String handle) {
		return new Impp(SIP, handle);
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
	public static Impp skype(String handle) {
		return new Impp(SKYPE, handle);
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
	public static Impp xmpp(String handle) {
		return new Impp(XMPP, handle);
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
		setUri((uri == null) ? null : URI.create(uri));
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
	 * Gets all the TYPE parameters.
	 * @return the TYPE parameters or empty set if there are none
	 */
	public Set<ImppType> getTypes() {
		Set<String> values = parameters.getTypes();
		Set<ImppType> types = new HashSet<ImppType>(values.size());
		for (String value : values) {
			types.add(ImppType.get(value));
		}
		return types;
	}

	/**
	 * Adds a TYPE parameter.
	 * @param type the TYPE parameter to add
	 */
	public void addType(ImppType type) {
		parameters.addType(type.getValue());
	}

	/**
	 * Removes a TYPE parameter.
	 * @param type the TYPE parameter to remove
	 */
	public void removeType(ImppType type) {
		parameters.removeType(type.getValue());
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return parameters.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		parameters.setMediaType(mediaType);
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
		return parameters.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		parameters.setAltId(altId);
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (uri == null) {
			warnings.add(new Warning(8));
		}
	}
}
