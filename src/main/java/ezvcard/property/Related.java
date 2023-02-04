package ezvcard.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.SupportedVersions;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.Pid;
import ezvcard.parameter.RelatedType;
import ezvcard.util.TelUri;

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
 * Related related = Related.email("bob.smith@example.com");
 * related.getTypes().add(RelatedType.CO_WORKER);
 * related.getTypes().add(RelatedType.FRIEND);
 * vcard.addRelated(related);
 * 
 * //free-form text
 * related = new Related();
 * related.setText("Edna Smith");
 * related.getTypes().add(RelatedType.SPOUSE);
 * vcard.addRelated(related);
 * 
 * //reference another vCard by putting its UID property here
 * related = new Related("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af");
 * related.getTypes().add(RelatedType.SIBLING);
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
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-42">RFC 6350 p.42</a>
 */
@SupportedVersions(VCardVersion.V4_0)
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
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Related(Related original) {
		super(original);
		uri = original.uri;
		text = original.text;
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
	 * Gets the list that stores this property's relationship types (TYPE
	 * parameters).
	 * @return the relationship types (e.g. "child", "co-worker") (this list is
	 * mutable)
	 */
	public List<RelatedType> getTypes() {
		return parameters.new TypeParameterList<RelatedType>() {
			@Override
			protected RelatedType _asObject(String value) {
				return RelatedType.get(value);
			}
		};
	}

	@Override
	public List<Pid> getPids() {
		return super.getPids();
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
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		if (uri == null && text == null) {
			warnings.add(new ValidationWarning(8));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("uri", uri);
		values.put("text", text);
		return values;
	}

	@Override
	public Related copy() {
		return new Related(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Related other = (Related) obj;
		if (text == null) {
			if (other.text != null) return false;
		} else if (!text.equals(other.text)) return false;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		return true;
	}
}
