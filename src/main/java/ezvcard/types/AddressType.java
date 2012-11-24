package ezvcard.types;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.AddressTypeParameter;
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
 * A mailing address.
 * 
 * <p>
 * <b>Adding an address</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * AddressType adr = new AddressType();
 * adr.setStreetAddress(&quot;123 Main St.&quot;);
 * adr.setLocality(&quot;Austin&quot;);
 * adr.setRegion(&quot;TX&quot;);
 * adr.setPostalCode(&quot;12345&quot;);
 * adr.setCountry(&quot;USA&quot;);
 * adr.addType(AddressTypeParameter.WORK);
 * adr.addType(AddressTypeParameter.DOM);
 * 
 * //optionally, provide the exact text to print out on the mailing label
 * adr.setLabel(&quot;123 Main St.\nAustin, Tx 12345\nUSA&quot;);
 * 
 * vcard.addAddress(adr);
 * </pre>
 * 
 * <p>
 * <b>Getting the addresses</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = ...
 * for (AddressType adr : vcard.getAddresses()){
 *   ...
 * }
 * </pre>
 * 
 * <p>
 * vCard property name: ADR
 * </p>
 * <p>
 * vCard versions: 2.1, 3.0, 4.0
 * </p>
 * @author Michael Angstadt
 */
public class AddressType extends MultiValuedTypeParameterType<AddressTypeParameter> {
	public static final String NAME = "ADR";

	private String poBox;
	private String extendedAddress;
	private String streetAddress;
	private String locality;
	private String region;
	private String postalCode;
	private String country;

	public AddressType() {
		super(NAME);
	}

	@Override
	protected AddressTypeParameter buildTypeObj(String type) {
		AddressTypeParameter param = AddressTypeParameter.valueOf(type);
		if (param == null) {
			param = new AddressTypeParameter(type);
		}
		return param;
	}

	/**
	 * Gets the P.O. (post office) box.
	 * @return the P.O. box or null if not set
	 */
	public String getPoBox() {
		return poBox;
	}

	/**
	 * Sets the P.O. (post office) box.
	 * @param poBox the P.O. box or null to remove
	 */
	public void setPoBox(String poBox) {
		this.poBox = poBox;
	}

	/**
	 * Gets the extended address.
	 * @return the extended address (e.g. "Suite 200") or null if not set
	 */
	public String getExtendedAddress() {
		return extendedAddress;
	}

	/**
	 * Sets the extended address.
	 * @param extendedAddress the extended address (e.g. "Suite 200") or null to
	 * remove
	 */
	public void setExtendedAddress(String extendedAddress) {
		this.extendedAddress = extendedAddress;
	}

	/**
	 * Gets the street address
	 * @return the street address (e.g. "123 Main St")
	 */
	public String getStreetAddress() {
		return streetAddress;
	}

	/**
	 * Sets the street address.
	 * @param streetAddress the street address (e.g. "123 Main St") or null to
	 * remove
	 */
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	/**
	 * Gets the locality (city)
	 * @return the locality (e.g. "Boston") or null if not set
	 */
	public String getLocality() {
		return locality;
	}

	/**
	 * Sets the locality (city).
	 * @param locality the locality or null to remove
	 */
	public void setLocality(String locality) {
		this.locality = locality;
	}

	/**
	 * Gets the region.
	 * @return the region (e.g. "Texas") or null if not set
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * Sets the region.
	 * @param region the region (e.g. "Texas") or null to remove
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * Gets the postal code.
	 * @return the postal code (e.g. "90210") or null if not set
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * Sets the postal code.
	 * @param postalCode the postal code (e.g. "90210") or null to remove
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * Gets the country.
	 * @return the country (e.g. "USA") or null if not set
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Sets the country.
	 * @param country the country (e.g. "USA") or null to remove
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Gets the language that the address is written in.
	 * @return the language or null if not set
	 * @see VCardSubTypes#getLanguage
	 */
	public String getLanguage() {
		return subTypes.getLanguage();
	}

	/**
	 * Sets the language that the address is written in.
	 * @param language the language or null to remove
	 * @see VCardSubTypes#setLanguage
	 */
	public void setLanguage(String language) {
		subTypes.setLanguage(language);
	}

	/**
	 * Gets the label of the address.
	 * @return the label or null if it doesn't have one
	 */
	public String getLabel() {
		return subTypes.getFirst("LABEL");
	}

	/**
	 * Sets the label of the address.
	 * @param label the label or null to remove
	 */
	public void setLabel(String label) {
		subTypes.replace("LABEL", label);
	}

	/**
	 * Gets the global positioning coordinates that are associated with this
	 * address.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the latitude (index 0) and longitude (index 1) or null if not set
	 * or null if the parameter value was in an incorrect format
	 * @see VCardSubTypes#getGeo
	 */
	public double[] getGeo() {
		return subTypes.getGeo();
	}

	/**
	 * Sets the global positioning coordinates that are associated with this
	 * address.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @see VCardSubTypes#setGeo
	 */
	public void setGeo(double latitude, double longitude) {
		subTypes.setGeo(latitude, longitude);
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

	/**
	 * Gets the timezone that's associated with this address.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the timezone (e.g. "America/New_York") or null if it doesn't
	 * exist
	 */
	public String getTimezone() {
		String value = subTypes.getFirst("TZ");
		if (value.matches("(?i)tz:.*")) {
			//remove the "tz:"
			value = (value.length() > 3) ? value.substring(3) : "";
		}
		return value;
	}

	/**
	 * Sets the timezone that's associated with this address.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param timezone the timezone (e.g. "America/New_York") or null to remove
	 */
	public void setTimezone(String timezone) {
		subTypes.replace("TZ", "tz:" + timezone);
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		//replace "TYPE=pref" with "PREF=1"
		if (version == VCardVersion.V4_0) {
			if (getTypes().contains(AddressTypeParameter.PREF)) {
				copy.removeType(AddressTypeParameter.PREF.getValue());
				copy.setPref(1);
			}
		} else {
			copy.setPref(null);

			//find the ADR with the lowest PREF value in the vCard
			AddressType mostPreferred = null;
			for (AddressType adr : vcard.getAddresses()) {
				Integer pref = adr.getPref();
				if (pref != null) {
					if (mostPreferred == null || pref < mostPreferred.getPref()) {
						mostPreferred = adr;
					}
				}
			}
			if (this == mostPreferred) {
				copy.addType(AddressTypeParameter.PREF.getValue());
			}
		}

		//remove the LABEL parameter
		if (version != VCardVersion.V4_0) {
			copy.removeAll("LABEL");
		}
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (poBox != null) {
			sb.append(VCardStringUtils.escape(poBox));
		}
		sb.append(';');

		if (extendedAddress != null) {
			sb.append(VCardStringUtils.escape(extendedAddress));
		}
		sb.append(';');

		if (streetAddress != null) {
			sb.append(VCardStringUtils.escape(streetAddress));
		}
		sb.append(';');

		if (locality != null) {
			sb.append(VCardStringUtils.escape(locality));
		}
		sb.append(';');

		if (region != null) {
			sb.append(VCardStringUtils.escape(region));
		}
		sb.append(';');

		if (postalCode != null) {
			sb.append(VCardStringUtils.escape(postalCode));
		}
		sb.append(';');

		if (country != null) {
			sb.append(VCardStringUtils.escape(country));
		}
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String split[] = VCardStringUtils.splitBy(value, ';', false, true);

		int i = 0;

		poBox = (split.length > i && split[i].length() > 0) ? split[i] : null;
		i++;

		extendedAddress = (split.length > i && split[i].length() > 0) ? split[i] : null;
		i++;

		streetAddress = (split.length > i && split[i].length() > 0) ? split[i] : null;
		i++;

		locality = (split.length > i && split[i].length() > 0) ? split[i] : null;
		i++;

		region = (split.length > i && split[i].length() > 0) ? split[i] : null;
		i++;

		postalCode = (split.length > i && split[i].length() > 0) ? split[i] : null;
		i++;

		country = (split.length > i && split[i].length() > 0) ? split[i] : null;
	}

	@Override
	protected void doMarshalValue(Element parent, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (poBox != null) {
			XCardUtils.appendChild(parent, "pobox", poBox, version);
		}
		if (extendedAddress != null) {
			XCardUtils.appendChild(parent, "ext", extendedAddress, version);
		}
		if (streetAddress != null) {
			XCardUtils.appendChild(parent, "street", streetAddress, version);
		}
		if (locality != null) {
			XCardUtils.appendChild(parent, "locality", locality, version);
		}
		if (region != null) {
			XCardUtils.appendChild(parent, "region", region, version);
		}
		if (postalCode != null) {
			XCardUtils.appendChild(parent, "code", postalCode, version);
		}
		if (country != null) {
			XCardUtils.appendChild(parent, "country", country, version);
		}
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		poBox = XCardUtils.getFirstChildText(element, "pobox");
		extendedAddress = XCardUtils.getFirstChildText(element, "ext");
		streetAddress = XCardUtils.getFirstChildText(element, "street");
		locality = XCardUtils.getFirstChildText(element, "locality");
		region = XCardUtils.getFirstChildText(element, "region");
		postalCode = XCardUtils.getFirstChildText(element, "code");
		country = XCardUtils.getFirstChildText(element, "country");
	}

	@Override
	protected void doUnmarshalHtml(org.jsoup.nodes.Element element, List<String> warnings) {
		List<String> values = HCardUtils.getElementValuesByCssClass(element, "post-office-box");
		poBox = values.isEmpty() ? null : values.get(0);

		values = HCardUtils.getElementValuesByCssClass(element, "extended-address");
		extendedAddress = values.isEmpty() ? null : values.get(0);

		values = HCardUtils.getElementValuesByCssClass(element, "street-address");
		streetAddress = values.isEmpty() ? null : values.get(0);

		values = HCardUtils.getElementValuesByCssClass(element, "locality");
		locality = values.isEmpty() ? null : values.get(0);

		values = HCardUtils.getElementValuesByCssClass(element, "region");
		region = values.isEmpty() ? null : values.get(0);

		values = HCardUtils.getElementValuesByCssClass(element, "postal-code");
		postalCode = values.isEmpty() ? null : values.get(0);

		values = HCardUtils.getElementValuesByCssClass(element, "country-name");
		country = values.isEmpty() ? null : values.get(0);

		values = HCardUtils.getTypes(element);
		for (String v : values) {
			subTypes.addType(v);
		}
	}
}