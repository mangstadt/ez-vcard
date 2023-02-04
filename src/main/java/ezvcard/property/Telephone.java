package ezvcard.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.Pid;
import ezvcard.parameter.TelephoneType;
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
 * Defines a telephone number.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //text
 * Telephone tel = new Telephone("(123) 555-6789");
 * tel.getTypes().add(TelephoneType.HOME);
 * tel.setPref(2); //the second-most preferred
 * vcard.addTelephoneNumber(tel);
 * 
 * //URI (vCard version 4.0 only)
 * TelUri uri = new TelUri.Builder("+1-800-555-9876").extension("111").build();
 * tel = new Telephone(uri);
 * tel.getTypes().add(TelephoneType.WORK);
 * tel.setPref(1); //the most preferred
 * vcard.addTelephoneNumber(tel);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code TEL}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-34">RFC 6350 p.34</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-14">RFC 2426 p.14</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.13</a>
 */
public class Telephone extends VCardProperty implements HasAltId {
	private String text;
	private TelUri uri;

	/**
	 * Creates a telephone property.
	 * @param text the telephone number (e.g. "(123) 555-6789")
	 */
	public Telephone(String text) {
		setText(text);
	}

	/**
	 * Creates a telephone property.
	 * @param uri a "tel" URI representing the telephone number (vCard 4.0 only)
	 */
	public Telephone(TelUri uri) {
		setUri(uri);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Telephone(Telephone original) {
		super(original);
		text = original.text;
		uri = original.uri;
	}

	/**
	 * Gets the telephone number as a text value.
	 * @return the telephone number or null if the text value is not set
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the telephone number as a text value.
	 * @param text the telephone number
	 */
	public void setText(String text) {
		this.text = text;
		uri = null;
	}

	/**
	 * Gets a "tel" URI representing the phone number.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the "tel" URI or null if it is not set
	 */
	public TelUri getUri() {
		return uri;
	}

	/**
	 * Sets a "tel" URI representing the phone number.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param uri the "tel" URI
	 */
	public void setUri(TelUri uri) {
		text = null;
		this.uri = uri;
	}

	/**
	 * Gets the list that stores this property's telephone types (TYPE
	 * parameters).
	 * @return the telephone types (e.g. "HOME", "WORK") (this list is mutable)
	 */
	public List<TelephoneType> getTypes() {
		return parameters.new TypeParameterList<TelephoneType>() {
			@Override
			protected TelephoneType _asObject(String value) {
				return TelephoneType.get(value);
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

		if (uri != null && (version == VCardVersion.V2_1 || version == VCardVersion.V3_0)) {
			warnings.add(new ValidationWarning(19));
		}

		for (TelephoneType type : getTypes()) {
			if (type == TelephoneType.PREF) {
				//ignore because it is converted to a PREF parameter for 4.0 vCards
				continue;
			}

			if (!type.isSupportedBy(version)) {
				warnings.add(new ValidationWarning(9, type.getValue()));
			}
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
	public Telephone copy() {
		return new Telephone(this);
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
		Telephone other = (Telephone) obj;
		if (text == null) {
			if (other.text != null) return false;
		} else if (!text.equals(other.text)) return false;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		return true;
	}
}