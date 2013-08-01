package ezvcard.types;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.VCardStringUtils.JoinCallback;
import ezvcard.util.XCardElement;

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
 * <b>Property name:</b> <code>ADR</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class AddressType extends MultiValuedTypeParameterType<AddressTypeParameter> implements HasAltId {
	public static final String NAME = "ADR";

	private String poBox;
	private String extendedAddress;
	private String streetAddress;
	private String locality;
	private String region;
	private String postalCode;
	private String country;

	/**
	 * Creates an address property.
	 */
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
		return subTypes.first("LABEL");
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @return the PID values or empty set if there are none
	 * @see VCardSubTypes#getPids
	 */
	public List<Integer[]> getPids() {
		return subTypes.getPids();
	}

	/**
	 * Adds a PID value.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @see VCardSubTypes#removePids
	 */
	public void removePids() {
		subTypes.removePids();
	}

	/**
	 * Gets the preference value.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see VCardSubTypes#setPref
	 */
	public void setPref(Integer pref) {
		subTypes.setPref(pref);
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
	 * Gets the timezone that's associated with this address.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @return the timezone (e.g. "America/New_York") or null if it doesn't
	 * exist
	 */
	public String getTimezone() {
		return subTypes.first("TZ");
	}

	/**
	 * Sets the timezone that's associated with this address.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param timezone the timezone (e.g. "America/New_York") or null to remove
	 */
	public void setTimezone(String timezone) {
		subTypes.replace("TZ", timezone);
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
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		List<String> values = Arrays.asList(poBox, extendedAddress, streetAddress, locality, region, postalCode, country);
		VCardStringUtils.join(values, ";", sb, new JoinCallback<String>() {
			public void handle(StringBuilder sb, String value) {
				if (value != null) {
					sb.append(VCardStringUtils.escape(value));
				}
			}
		});
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String split[] = VCardStringUtils.splitBy(value, ';', false, true);
		Iterator<String> it = Arrays.asList(split).iterator();

		poBox = nextTextComponent(it);
		extendedAddress = nextTextComponent(it);
		streetAddress = nextTextComponent(it);
		locality = nextTextComponent(it);
		region = nextTextComponent(it);
		postalCode = nextTextComponent(it);
		country = nextTextComponent(it);
	}

	private String nextTextComponent(Iterator<String> it) {
		if (!it.hasNext()) {
			return null;
		}

		String value = it.next();
		return (value.length() == 0) ? null : value;
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		Map<String, String> values = new LinkedHashMap<String, String>();
		values.put("pobox", poBox);
		values.put("ext", extendedAddress);
		values.put("street", streetAddress);
		values.put("locality", locality);
		values.put("region", region);
		values.put("code", postalCode);
		values.put("country", country);

		for (Map.Entry<String, String> entry : values.entrySet()) {
			String value = entry.getValue();
			if (value != null) {
				String name = entry.getKey();
				parent.append(name, value);
			}
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		poBox = element.get("pobox");
		extendedAddress = element.get("ext");
		streetAddress = element.get("street");
		locality = element.get("locality");
		region = element.get("region");
		postalCode = element.get("code");
		country = element.get("country");
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		poBox = element.firstValue("post-office-box");
		extendedAddress = element.firstValue("extended-address");
		streetAddress = element.firstValue("street-address");
		locality = element.firstValue("locality");
		region = element.firstValue("region");
		postalCode = element.firstValue("postal-code");
		country = element.firstValue("country-name");
		List<String> types = element.types();
		for (String type : types) {
			subTypes.addType(type);
		}
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		return JCardValue.structured(JCardDataType.TEXT, poBox, extendedAddress, streetAddress, locality, region, postalCode, country);
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		Iterator<List<String>> it = value.getStructured().iterator();

		poBox = nextJsonComponent(it);
		extendedAddress = nextJsonComponent(it);
		streetAddress = nextJsonComponent(it);
		locality = nextJsonComponent(it);
		region = nextJsonComponent(it);
		postalCode = nextJsonComponent(it);
		country = nextJsonComponent(it);
	}

	private String nextJsonComponent(Iterator<List<String>> it) {
		if (!it.hasNext()) {
			return null;
		}

		List<String> values = it.next();
		if (values.isEmpty()) {
			return null;
		}

		String value = values.get(0);
		return (value == null || value.length() == 0) ? null : value;
	}
}