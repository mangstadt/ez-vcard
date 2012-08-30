package ezvcard.types;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ValueParameter;

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
 * A set of latitude/longitude coordinates. There is no rule for what these
 * coordinates must represent, but the meaning could vary depending on the value
 * of the vCard KIND type.
 * 
 * <ul>
 * <li>"individual": the location of the person's home or workplace.</li>
 * <li>"group": the location of the group's meeting place.</li>
 * <li>"org": the coordinates of the organization's headquarters.</li>
 * <li>"location": the coordinates of the location itself.</li>
 * </ul>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * GeoType geo = new GeoType(-123.456, 12.54);
 * vcard.setGeo(geo);
 * </pre>
 * 
 * <p>
 * vCard property name: GEO
 * </p>
 * <p>
 * vCard versions: 2.1, 3.0, 4.0
 * </p>
 * @author Michael Angstadt
 */
public class GeoType extends VCardType {
	public static final String NAME = "GEO";
	private Double latitude, longitude;

	public GeoType() {
		this(null, null);
	}

	/**
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public GeoType(Double latitude, Double longitude) {
		super(NAME);
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Gets the latitude.
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude.
	 * @param latitude the latitude
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Gets the longitude
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude.
	 * @param longitude the longitude
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Gets the TYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return subTypes.getType();
	}

	/**
	 * Sets the TYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param type the TYPE value (this should be either "work" or "home") or
	 * null to remove
	 */
	public void setType(String type) {
		subTypes.setType(type);
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return subTypes.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
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

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) throws VCardException {
		if (version == VCardVersion.V4_0) {
			copy.setValue(ValueParameter.URI);
		}
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		NumberFormat nf = new DecimalFormat("0.####");

		if (version == VCardVersion.V4_0) {
			sb.append("geo:");
			sb.append(nf.format(latitude));
			sb.append(',');
			sb.append(nf.format(longitude));
		} else {
			sb.append(nf.format(latitude));
			sb.append(';');
			sb.append(nf.format(longitude));
		}
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		//2.1/3.0 syntax:	GEO:12;34
		//4.0 syntax:		GEO;VALUE=uri:geo:12,23
		Pattern p = Pattern.compile("^(geo:)?(.*?)[,;](.*)$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(value);
		if (m.find()) {
			try {
				latitude = Double.parseDouble(m.group(2));
				longitude = Double.parseDouble(m.group(3));
			} catch (NumberFormatException e) {
				warnings.add("Cannot parse latitude and longitude numbers out of " + NAME + " type value: \"" + value + "\"");
			}
		} else {
			warnings.add("Unrecognized format of " + NAME + " type: \"" + value + "\"");
		}
	}
}
