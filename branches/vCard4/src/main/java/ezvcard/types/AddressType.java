package ezvcard.types;

import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.AddressTypeParameter;
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
 * Represents an address from the "ADR" type.
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
	public Set<String> getPids() {
		return subTypes.getPids();
	}

	/**
	 * Adds a PID value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param pid the PID value
	 * @see VCardSubTypes#addPid
	 */
	public void addPid(String pid) {
		subTypes.addPid(pid);
	}

	/**
	 * Removes a PID value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param pid the PID value to remove
	 * @see VCardSubTypes#removePid
	 */
	public void removePid(String pid) {
		subTypes.removePid(pid);
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
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) throws VCardException {
		VCardSubTypes copy = new VCardSubTypes(subTypes);

		//replace "TYPE=pref" with "PREF=1"
		if (version == VCardVersion.V4_0) {
			if (getTypes().contains(AddressTypeParameter.PREF)) {
				copy.removeType(AddressTypeParameter.PREF.getValue());
				copy.setPref(1);
			}
		} else {
			copy.setPref(null);

			//find the ADR with the lowest PREF value in the vCard (and the same ALTID)
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

		return copy;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder sb = new StringBuilder();

		if (poBox != null) {
			sb.append(VCardStringUtils.escapeText(poBox));
		}
		sb.append(';');

		if (extendedAddress != null) {
			sb.append(VCardStringUtils.escapeText(extendedAddress));
		}
		sb.append(';');

		if (streetAddress != null) {
			sb.append(VCardStringUtils.escapeText(streetAddress));
		}
		sb.append(';');

		if (locality != null) {
			sb.append(VCardStringUtils.escapeText(locality));
		}
		sb.append(';');

		if (region != null) {
			sb.append(VCardStringUtils.escapeText(region));
		}
		sb.append(';');

		if (postalCode != null) {
			sb.append(VCardStringUtils.escapeText(postalCode));
		}
		sb.append(';');

		if (country != null) {
			sb.append(VCardStringUtils.escapeText(country));
		}

		return sb.toString();
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
}