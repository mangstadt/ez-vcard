package ezvcard.property;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.Pid;
import ezvcard.parameter.VCardParameters;
import ezvcard.util.GeoUri;
import ezvcard.util.StringUtils;

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
 * Defines a mailing address.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Address adr = new Address();
 * adr.setStreetAddress("123 Main St.");
 * adr.setLocality("Austin");
 * adr.setRegion("TX");
 * adr.setPostalCode("12345");
 * adr.setCountry("USA");
 * adr.getTypes().add(AddressType.WORK);
 * 
 * //optionally, set the text to print on the mailing label
 * adr.setLabel("123 Main St.\nAustin, TX 12345\nUSA");
 * 
 * vcard.addAddress(adr);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * for (Address adr : vcard.getAddresses()) {
 *   String street = adr.getStreetAddress();
 *   String city = adr.getLocality();
 *   //etc.
 * }
 * </pre>
 * 
 * <p>
 * <b>Only part of the street address is being returned!</b>
 * </p>
 * <p>
 * This usually means that the vCard you parsed contains unescaped comma
 * characters. To get the full address, use the {@link #getStreetAddressFull}
 * method.
 * </p>
 * 
 * <p>
 * <b>Property name:</b> {@code ADR}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-32">RFC 6350 p.32</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426 p.11</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
 */
public class Address extends VCardProperty implements HasAltId {
	private final List<String> poBoxes;
	private final List<String> extendedAddresses;
	private final List<String> streetAddresses;
	private final List<String> localities;
	private final List<String> regions;
	private final List<String> postalCodes;
	private final List<String> countries;

	public Address() {
		poBoxes = new ArrayList<>(1);
		extendedAddresses = new ArrayList<>(1);
		streetAddresses = new ArrayList<>(1);
		localities = new ArrayList<>(1);
		regions = new ArrayList<>(1);
		postalCodes = new ArrayList<>(1);
		countries = new ArrayList<>(1);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Address(Address original) {
		super(original);
		poBoxes = new ArrayList<>(original.poBoxes);
		extendedAddresses = new ArrayList<>(original.extendedAddresses);
		streetAddresses = new ArrayList<>(original.streetAddresses);
		localities = new ArrayList<>(original.localities);
		regions = new ArrayList<>(original.regions);
		postalCodes = new ArrayList<>(original.postalCodes);
		countries = new ArrayList<>(original.countries);
	}

	/**
	 * Gets the P.O. (post office) box.
	 * @return the P.O. box or null if not set
	 */
	public String getPoBox() {
		return first(poBoxes);
	}

	/**
	 * Gets the list that holds the P.O. (post office) boxes that are assigned
	 * to this address. An address is unlikely to have more than one, but it's
	 * possible nonetheless.
	 * @return the P.O. boxes (this list is mutable)
	 */
	public List<String> getPoBoxes() {
		return poBoxes;
	}

	/**
	 * Sets the P.O. (post office) box.
	 * @param poBox the P.O. box or null to remove
	 */
	public void setPoBox(String poBox) {
		set(poBoxes, poBox);
	}

	/**
	 * Gets the extended address.
	 * @return the extended address (e.g. "Suite 200") or null if not set
	 */
	public String getExtendedAddress() {
		return first(extendedAddresses);
	}

	/**
	 * Gets the list that holds the extended addresses that are assigned to this
	 * address. An address is unlikely to have more than one, but it's possible
	 * nonetheless.
	 * @return the extended addresses (this list is mutable)
	 */
	public List<String> getExtendedAddresses() {
		return extendedAddresses;
	}

	/**
	 * Gets the extended address. Use this method when the ADR property of the
	 * vCard you are parsing contains unescaped comma characters.
	 * @return the extended address or null if not set
	 */
	public String getExtendedAddressFull() {
		return getAddressFull(extendedAddresses);
	}

	/**
	 * Sets the extended address.
	 * @param extendedAddress the extended address (e.g. "Suite 200") or null to
	 * remove
	 */
	public void setExtendedAddress(String extendedAddress) {
		set(extendedAddresses, extendedAddress);
	}

	/**
	 * Gets the street address
	 * @return the street address (e.g. "123 Main St")
	 */
	public String getStreetAddress() {
		return first(streetAddresses);
	}

	/**
	 * Gets the list that holds the street addresses that are assigned to this
	 * address. An address is unlikely to have more than one, but it's possible
	 * nonetheless.
	 * @return the street addresses (this list is mutable)
	 */
	public List<String> getStreetAddresses() {
		return streetAddresses;
	}

	/**
	 * Gets the street address. Use this method when the ADR property of the
	 * vCard you are parsing contains unescaped comma characters.
	 * @return the street address or null if not set
	 */
	public String getStreetAddressFull() {
		return getAddressFull(streetAddresses);
	}

	/**
	 * Sets the street address.
	 * @param streetAddress the street address (e.g. "123 Main St") or null to
	 * remove
	 */
	public void setStreetAddress(String streetAddress) {
		set(streetAddresses, streetAddress);
	}

	/**
	 * Gets the locality (city)
	 * @return the locality (e.g. "Boston") or null if not set
	 */
	public String getLocality() {
		return first(localities);
	}

	/**
	 * Gets the list that holds the localities that are assigned to this
	 * address. An address is unlikely to have more than one, but it's possible
	 * nonetheless.
	 * @return the localities (this list is mutable)
	 */
	public List<String> getLocalities() {
		return localities;
	}

	/**
	 * Sets the locality (city).
	 * @param locality the locality or null to remove
	 */
	public void setLocality(String locality) {
		set(localities, locality);
	}

	/**
	 * Gets the region (state).
	 * @return the region (e.g. "Texas") or null if not set
	 */
	public String getRegion() {
		return first(regions);
	}

	/**
	 * Gets the list that holds the regions that are assigned to this address.
	 * An address is unlikely to have more than one, but it's possible
	 * nonetheless.
	 * @return the regions (this list is mutable)
	 */
	public List<String> getRegions() {
		return regions;
	}

	/**
	 * Sets the region (state).
	 * @param region the region (e.g. "Texas") or null to remove
	 */
	public void setRegion(String region) {
		set(regions, region);
	}

	/**
	 * Gets the postal code (zip code).
	 * @return the postal code (e.g. "90210") or null if not set
	 */
	public String getPostalCode() {
		return first(postalCodes);
	}

	/**
	 * Gets the list that holds the postal codes that are assigned to this
	 * address. An address is unlikely to have more than one, but it's possible
	 * nonetheless.
	 * @return the postal codes (this list is mutable)
	 */
	public List<String> getPostalCodes() {
		return postalCodes;
	}

	/**
	 * Sets the postal code (zip code).
	 * @param postalCode the postal code (e.g. "90210") or null to remove
	 */
	public void setPostalCode(String postalCode) {
		set(postalCodes, postalCode);
	}

	/**
	 * Gets the country.
	 * @return the country (e.g. "USA") or null if not set
	 */
	public String getCountry() {
		return first(countries);
	}

	/**
	 * Gets the list that holds the countries that are assigned to this address.
	 * An address is unlikely to have more than one, but it's possible
	 * nonetheless.
	 * @return the countries (this list is mutable)
	 */
	public List<String> getCountries() {
		return countries;
	}

	/**
	 * Sets the country.
	 * @param country the country (e.g. "USA") or null to remove
	 */
	public void setCountry(String country) {
		set(countries, country);
	}

	/**
	 * Gets the list that stores this property's address types (TYPE
	 * parameters).
	 * @return the address types (e.g. "HOME", "WORK") (this list is mutable)
	 */
	public List<AddressType> getTypes() {
		return parameters.new TypeParameterList<AddressType>() {
			@Override
			protected AddressType _asObject(String value) {
				return AddressType.get(value);
			}
		};
	}

	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	/**
	 * Gets the label of the address.
	 * @return the label or null if not set
	 */
	public String getLabel() {
		return parameters.getLabel();
	}

	/**
	 * Sets the label of the address.
	 * @param label the label or null to remove
	 */
	public void setLabel(String label) {
		parameters.setLabel(label);
	}

	/**
	 * <p>
	 * Gets the global positioning coordinates that are associated with this
	 * address.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the geo URI or not if not found
	 * @see VCardParameters#getGeo
	 */
	public GeoUri getGeo() {
		return parameters.getGeo();
	}

	/**
	 * <p>
	 * Sets the global positioning coordinates that are associated with this
	 * address.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param uri the geo URI or null to remove
	 * @see VCardParameters#setGeo
	 */
	public void setGeo(GeoUri uri) {
		parameters.setGeo(uri);
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

	/**
	 * Gets the timezone that's associated with this address.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the timezone (e.g. "America/New_York") or null if not set
	 */
	public String getTimezone() {
		return parameters.getTimezone();
	}

	/**
	 * Sets the timezone that's associated with this address.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param timezone the timezone (e.g. "America/New_York") or null to remove
	 */
	public void setTimezone(String timezone) {
		parameters.setTimezone(timezone);
	}

	@Override
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		for (AddressType type : getTypes()) {
			if (type == AddressType.PREF) {
				//ignore because it is converted to a PREF parameter for 4.0 vCards
				continue;
			}

			if (!type.isSupportedBy(version)) {
				warnings.add(new ValidationWarning(9, type.getValue()));
			}
		}

		/*
		 * 2.1 does not allow multi-valued components.
		 */
		if (version == VCardVersion.V2_1) {
			//@formatter:off
			if (poBoxes.size() > 1 ||
				extendedAddresses.size() > 1 ||
				streetAddresses.size() > 1 ||
				localities.size() > 1 ||
				regions.size() > 1 ||
				postalCodes.size() > 1 ||
				countries.size() > 1) {
				warnings.add(new ValidationWarning(35));
			}
			//@formatter:on
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("poBoxes", poBoxes);
		values.put("extendedAddresses", extendedAddresses);
		values.put("streetAddresses", streetAddresses);
		values.put("localities", localities);
		values.put("regions", regions);
		values.put("postalCodes", postalCodes);
		values.put("countries", countries);
		return values;
	}

	@Override
	public Address copy() {
		return new Address(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + countries.hashCode();
		result = prime * result + extendedAddresses.hashCode();
		result = prime * result + localities.hashCode();
		result = prime * result + poBoxes.hashCode();
		result = prime * result + postalCodes.hashCode();
		result = prime * result + regions.hashCode();
		result = prime * result + streetAddresses.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Address other = (Address) obj;
		if (!countries.equals(other.countries)) return false;
		if (!extendedAddresses.equals(other.extendedAddresses)) return false;
		if (!localities.equals(other.localities)) return false;
		if (!poBoxes.equals(other.poBoxes)) return false;
		if (!postalCodes.equals(other.postalCodes)) return false;
		if (!regions.equals(other.regions)) return false;
		if (!streetAddresses.equals(other.streetAddresses)) return false;
		return true;
	}

	private static String first(List<String> list) {
		return list.isEmpty() ? null : list.get(0);
	}

	private static void set(List<String> list, String value) {
		list.clear();
		if (value != null) {
			list.add(value);
		}
	}

	private static String getAddressFull(List<String> list) {
		return list.isEmpty() ? null : StringUtils.join(list, ",");
	}
}