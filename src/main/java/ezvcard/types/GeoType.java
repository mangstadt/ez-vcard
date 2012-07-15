package ezvcard.types;

import java.util.List;

import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
 * Represents the GEO type.
 * @author Michael Angstadt
 */
public class GeoType extends VCardType {
	public static final String NAME = "GEO";
	private Double latitude, longitude;

	public GeoType(Double latitude, Double longitude) {
		super(NAME);
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public GeoType(){
		this(null, null);
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder sb = new StringBuilder();
		if (latitude != null){
			sb.append(latitude);
		}
		sb.append(';');
		if (longitude != null){
			sb.append(longitude);
		}
		return sb.toString();
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String split[] = VCardStringUtils.splitBy(value, ';', false, true);
		if (split.length < 2){
			warnings.add("Invalid format of " + NAME + " type: \"" + value + "\"");
		}
		if (split.length > 0){
			latitude = Double.parseDouble(split[0]);
		}
		if (split.length > 1) {
			longitude = Double.parseDouble(split[1]);
		}
	}
}
