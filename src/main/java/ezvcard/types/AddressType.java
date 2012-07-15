package ezvcard.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	private List<String> poBox = new ArrayList<String>();
	private List<String> extendedAddr = new ArrayList<String>();
	private List<String> streetAddr = new ArrayList<String>();
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

	public List<String> getPoBox() {
		return poBox;
	}

	public List<String> getExtendedAddr() {
		return extendedAddr;
	}

	public List<String> getStreetAddr() {
		return streetAddr;
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
	
	public String getLanguage(){
		return subTypes.getLanguage();
	}
	
	public void setLanguage(String language){
		subTypes.setLanguage(language);
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder sb = new StringBuilder();

		if (!poBox.isEmpty()) {
			for (String s : poBox) {
				sb.append(VCardStringUtils.escapeText(s));
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(';');

		if (!extendedAddr.isEmpty()) {
			for (String s : extendedAddr) {
				sb.append(VCardStringUtils.escapeText(s));
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(';');

		if (!streetAddr.isEmpty()) {
			for (String s : streetAddr) {
				sb.append(VCardStringUtils.escapeText(s));
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
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
		String split[] = VCardStringUtils.splitBy(value, ';', true, false);

		int i = 0;
		if (split.length > i){
			poBox = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		} else {
			poBox = new ArrayList<String>();
		}
		i++;

		if (split.length > i){
			extendedAddr = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		} else {
			extendedAddr = new ArrayList<String>();
		}
		i++;

		if (split.length > i){
			streetAddr = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		} else {
			streetAddr = new ArrayList<String>();
		}
		i++;

		locality = (split.length > i && !split[i].isEmpty()) ? VCardStringUtils.unescape(split[i]) : null;
		i++;

		region = (split.length > i && !split[i].isEmpty()) ? VCardStringUtils.unescape(split[i]) : null;
		i++;

		postalCode = (split.length > i && !split[i].isEmpty()) ? VCardStringUtils.unescape(split[i]) : null;
		i++;

		country = (split.length > i && !split[i].isEmpty()) ? VCardStringUtils.unescape(split[i]) : null;
	}
}