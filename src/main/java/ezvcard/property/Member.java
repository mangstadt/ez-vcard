package ezvcard.property;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.util.TelUri;
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
 * Defines the members that make up the group. This property can only be used if
 * the vCard's {@link Kind} property is set to "group".
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //kind property must be set to &quot;group&quot; in order to add members
 * vcard.setKind(Kind.group());
 * 
 * //static factory methods
 * Member member = Member.email(&quot;johndoe@hotmail.com&quot;);
 * vcard.addMember(member);
 * 
 * //reference another vCard by putting its UID property here
 * member = new Member(&quot;urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af&quot;);
 * vcard.addMember(member);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code MEMBER}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Member extends UriProperty implements HasAltId {
	/**
	 * Creates a member property.
	 * @param uri the URI representing the member
	 */
	public Member(String uri) {
		super(uri);
	}

	/**
	 * Creates a member property whose value is an email address.
	 * @param email the email address
	 * @return the property
	 */
	public static Member email(String email) {
		return new Member("mailto:" + email);
	}

	/**
	 * Creates a member property whose value is an instant messenger handle.
	 * @param protocol the instant messenger protocol (e.g. "aim")
	 * @param handle the instant messenger handle (e.g. "johndoe")
	 * @return the property
	 */
	public static Member im(String protocol, String handle) {
		return new Member(protocol + ":" + handle);
	}

	/**
	 * Creates a member property whose value is a telephone number.
	 * @param telUri the telephone number
	 * @return the property
	 */
	public static Member telephone(TelUri telUri) {
		return new Member(telUri.toString());
	}

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V4_0);
	}

	/**
	 * Gets the URI value.
	 * @return the URI value or null if no URI value is set
	 */
	public String getUri() {
		return getValue();
	}

	/**
	 * Sets the URI.
	 * @param uri the URI
	 */
	public void setUri(String uri) {
		setValue(uri);
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
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		super._validate(warnings, version, vcard);

		if (vcard.getKind() == null || !vcard.getKind().isGroup()) {
			warnings.add(new Warning(17));
		}
	}
}
