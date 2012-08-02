package ezvcard.types;

import java.util.List;
import java.util.Set;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.VCardStringUtils;

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
 * Represents the MEMBER type.
 * @author Michael Angstadt
 */
public class MemberType extends VCardType {
	public static final String NAME = "MEMBER";

	private String uri;

	public MemberType() {
		super(NAME);
	}

	/**
	 * @param uri the URI
	 */
	public MemberType(String uri) {
		super(NAME);
		setUri(uri);
	}

	/**
	 * Gets the URI value.
	 * @return the URI value or null if no URI value is set
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the URI to an email address.
	 * @param email the email address
	 */
	public void setUriEmail(String email) {
		setUri("mailto:" + email);
	}

	/**
	 * Sets the URI to an instant messaging handle.
	 * @param protocol the IM protocol (e.g. "aim")
	 * @param handle the handle
	 */
	public void setUriIM(String protocol, String handle) {
		setUri(protocol + ":" + handle);
	}

	/**
	 * Sets the URI to a telephone number.
	 * @param telephone the telephone number
	 */
	public void setUriTelephone(String telephone) {
		setUri("tel:" + telephone);
	}

	/**
	 * Sets the URI.
	 * @param uri the URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
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

	public Integer getPref() {
		return subTypes.getPref();
	}

	public void setPref(Integer pref) {
		subTypes.setPref(pref);
	}

	public String getMediaType() {
		return subTypes.getMediaType();
	}

	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
	}

	public String getAltId() {
		return subTypes.getAltId();
	}

	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		return VCardStringUtils.escapeText(uri);
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		setUri(VCardStringUtils.unescape(value));
	}
}
