package ezvcard.types;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;

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
 * The members that make up the group. This type can only be used if
 * {@link KindType} is set to "group".
 * 
 * <p>
 * <b>Adding members</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //KIND must be set to &quot;group&quot; in order to add MEMBERs
 * vcard.setKind(KindType.group());
 * 
 * MemberType member = new MemberType();
 * member.setUriEmail(&quot;funkyjoe@hotmail.com&quot;);
 * vcard.addMember(member);
 * member = new MemberType();
 * member.setUriIM(&quot;aol&quot;, &quot;joesmoe@aol.com&quot;);
 * vcard.addMember(member);
 * member = new MemberType();
 * member.setUriTelephone(&quot;+1-123-555-6789&quot;);
 * vcard.addMember(member);
 * member = new MemberType();
 * member.setUri(&quot;urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af&quot;); //references the UID from another vCard
 * vcard.addMember(member);
 * </pre>
 * 
 * <p>
 * <b>Getting members</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * KindType kind = vcard.getKind();
 * if (kind != null){
 *   if (kind.isGroup()){
 *     System.out.println("The group's members are:");
 *     for (MemberType member : vcard.getMembers()){
 *       System.out.println(member.getUri());
 *     }
 *   }
 * }
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
public class MemberType extends UriType implements HasAltId {
	public static final String NAME = "MEMBER";

	/**
	 * Creates an empty member property.
	 */
	public MemberType() {
		super(NAME);
	}

	/**
	 * Creates a member property.
	 * @param uri the URI representing the member
	 */
	public MemberType(String uri) {
		super(NAME);
		setUri(uri);
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
		return subTypes.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return subTypes.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		super._validate(warnings, version, vcard);

		if (vcard.getKind() == null || !vcard.getKind().isGroup()) {
			warnings.add("The " + KindType.class.getSimpleName() + " property must be set to \"group\" if the vCard contains " + MemberType.class.getSimpleName() + " properties.");
		}
	}
}
