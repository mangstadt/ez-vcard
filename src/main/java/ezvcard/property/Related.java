package ezvcard.property;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.parameter.RelatedType;
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
 * Defines someone that the person is related to.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //static factory methods
 * Related related = Related.email(&quot;bob.smith@example.com&quot;);
 * related.addType(RelatedType.CO_WORKER);
 * related.addType(RelatedType.FRIEND);
 * vcard.addRelated(related);
 * 
 * //free-form text
 * related = new Related();
 * related.setText(&quot;Edna Smith&quot;);
 * related.addType(RelatedType.SPOUSE);
 * vcard.addRelated(related);
 * 
 * //reference another vCard by putting its UID property here
 * related = new Related(&quot;urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af&quot;);
 * related.addType(RelatedType.SIBLING);
 * vcard.addRelated(related);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code RELATED}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Related extends VCardProperty implements HasAltId {
	private String uri;
	private String text;

	/**
	 * Creates a related property
	 */
	public Related() {
		//empty
	}

	/**
	 * Creates a related property.
	 * @param uri the URI representing the person
	 */
	public Related(String uri) {
		setUri(uri);
	}

	/**
	 * Creates a related property whose value is an email address.
	 * @param email the email address
	 * @return the property
	 */
	public static Related email(String email) {
		return new Related("mailto:" + email);
	}

	/**
	 * Creates a related property whose value is an instant messenger handle.
	 * @param protocol the instant messenger protocol (e.g. "aim")
	 * @param handle the instant messenger handle (e.g. "johndoe")
	 * @return the property
	 */
	public static Related im(String protocol, String handle) {
		return new Related(protocol + ":" + handle);
	}

	/**
	 * Creates a related property whose value is a telephone number.
	 * @param telUri the telephone number
	 * @return the property
	 */
	public static Related telephone(TelUri telUri) {
		return new Related(telUri.toString());
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
		return uri;
	}

	/**
	 * Sets the URI.
	 * @param uri the URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
		text = null;
	}

	/**
	 * Gets the text value.
	 * @return the text value or null if no text value is set
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the value to free-form text instead of a URI.
	 * @param text the text
	 */
	public void setText(String text) {
		this.text = text;
		uri = null;
	}

	/**
	 * Gets all the TYPE parameters.
	 * @return the TYPE parameters or empty set if there are none
	 */
	public Set<RelatedType> getTypes() {
		Set<String> values = parameters.getTypes();
		Set<RelatedType> types = new HashSet<RelatedType>(values.size());
		for (String value : values) {
			types.add(RelatedType.get(value));
		}
		return types;
	}

	/**
	 * Adds a TYPE parameter.
	 * @param type the TYPE parameter to add
	 */
	public void addType(RelatedType type) {
		parameters.addType(type.getValue());
	}

	/**
	 * Removes a TYPE parameter.
	 * @param type the TYPE parameter to remove
	 */
	public void removeType(RelatedType type) {
		parameters.removeType(type.getValue());
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
		if (uri == null && text == null) {
			warnings.add(new Warning(8));
		}
	}
}
