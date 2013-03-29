package ezvcard.types;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ImppTypeParameter;
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
 * Contains an instant messenger handle.
 * 
 * <pre>
 * VCard vcard = new VCard();
 * ImppType impp = new ImppType(&quot;aim&quot;, &quot;hunkydude22@aol.com&quot;);
 * vcard.addImpp(impp);
 * </pre>
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

	/**
	 * A list of possible IM protocols, as listed in RFC 4770 p.2.
	 */
	public static enum Protocol {
		SIP, XMPP, IRC, YMSGR, MSN, AIM, IM, PRES;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	/**
	 * The IM protocol (e.g. "aim").
	 */
	private String protocol;

	/**
	 * The IM URI.
	 */
	private String uri;

	public ImppType() {
		this((String) null, null);
	}

	/**
	 * @param protocol the IM protocol
	 * @param uri the IM URI
	 */
	public ImppType(String protocol, String uri) {
		super(NAME);
		this.protocol = protocol;
		this.uri = uri;
	}

	/**
	 * @param protocol the IM protocol
	 * @param uri the IM URI
	 */
	public ImppType(Protocol protocol, String uri) {
		this(protocol.toString(), uri);
	}

	/**
	 * Gets the IM protocol.
	 * @return the IM protocol (e.g. "aim")
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Sets the IM protocol.
	 * @param protocol the IM protocol
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Sets the IM protocol using example protocols provided by the
	 * {@link ImppType.Protocol} enum.
	 * @param protocol the IM protocol
	 */
	public void setProtocol(Protocol protocol) {
		setProtocol(protocol.toString());
	}

	/**
	 * Gets the TYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return subTypes.getType();
	}

	/**
	 * Sets the TYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param type the TYPE value (this should be either "work" or "home") or
	 * null to remove
	 */
	public void setType(String type) {
		subTypes.setType(type);
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
		String value = writeValue();
		sb.append(VCardStringUtils.escapeText(value));
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		parseValue(value, warnings);
	}

	@Override
	protected void doMarshalValue(Element parent, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		String value = writeValue();
		XCardUtils.appendChild(parent, "uri", value, version);
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		String value = XCardUtils.getFirstChildText(element, "uri");
		if (value != null) {
			parseValue(value, warnings);
		}
	}

	private String writeValue() {
		StringBuilder sb = new StringBuilder();
		if (protocol != null) {
			sb.append(protocol);
			sb.append(':');
		}
		sb.append(uri);
		return sb.toString();
	}

	private void parseValue(String value, List<String> warnings) {
		String split[] = value.split(":", 2);
		if (split.length < 2) {
			warnings.add(NAME + " type is not in the correct format.  Assuming that the entire value is a URI.");
			uri = split[0];
		} else {
			protocol = split[0];
			uri = split[1];
		}
	}
}
