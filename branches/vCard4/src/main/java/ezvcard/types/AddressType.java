package ezvcard.types;

import java.util.List;

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

	public String getPoBox() {
		return poBox;
	}

	public void setPoBox(String poBox) {
		this.poBox = poBox;
	}

	public String getExtendedAddress() {
		return extendedAddress;
	}

	public void setExtendedAddress(String extendedAddress) {
		this.extendedAddress = extendedAddress;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLanguage() {
		return subTypes.getLanguage();
	}

	public void setLanguage(String language) {
		subTypes.setLanguage(language);
	}

	public String getLabel() {
		return subTypes.getFirst("LABEL");
	}

	public void setLabel(String label) {
		subTypes.replace("LABEL", label);
	}

	@Override
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) throws VCardException {
		if (version != VCardVersion.V4_0) {
			VCardSubTypes copy = new VCardSubTypes(subTypes);
			copy.removeAll("LABEL");
			return copy;
		}
		return super.doMarshalSubTypes(version, warnings, compatibilityMode, vcard);
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