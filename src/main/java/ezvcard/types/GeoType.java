package ezvcard.types;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.GeoUri;
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
	private GeoUri uri = new GeoUri();

	public GeoType() {
		this(null, null);
	}

	/**
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public GeoType(Double latitude, Double longitude) {
		super(NAME);
		setLatitude(latitude);
		setLongitude(longitude);
	}

	/**
	 * Gets the latitude.
	 * @return the latitude
	 */
	public Double getLatitude() {
		return uri.getCoordA();
	}

	/**
	 * Sets the latitude.
	 * @param latitude the latitude
	 */
	public void setLatitude(Double latitude) {
		uri.setCoordA(latitude);
	}

	/**
	 * Gets the longitude.
	 * @return the longitude
	 */
	public Double getLongitude() {
		return uri.getCoordB();
	}

	/**
	 * Sets the longitude.
	 * @param longitude the longitude
	 */
	public void setLongitude(Double longitude) {
		uri.setCoordB(longitude);
	}

	/**
	 * Gets the raw object used for storing the GEO information. This can be
	 * used to supplement the GEO value with additional information (such as the
	 * altitude). Geo URIs are only supported by vCard version 4.0. Everything
	 * but latitude and longitude will be lost when marshalling to an earlier
	 * vCard version.
	 * @return the geo URI object
	 * @see <a href="http://tools.ietf.org/html/rfc5870">RFC 5870</a>
	 */
	public GeoUri getGeoUri() {
		return uri;
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
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		if (version == VCardVersion.V4_0) {
			copy.setValue(ValueParameter.URI);
		}
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (getLatitude() == null || getLongitude() == null) {
			throw new SkipMeException("Latitude and/or longitude is missing.");
		}

		if (version == VCardVersion.V4_0) {
			sb.append(uri.toString(6));
		} else {
			NumberFormat nf = new DecimalFormat("0.######");
			sb.append(nf.format(getLatitude()));
			sb.append(';');
			sb.append(nf.format(getLongitude()));
		}
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		try {
			//4.0 syntax: GEO;VALUE=uri:geo:12,23
			uri = new GeoUri(value);
		} catch (IllegalArgumentException e) {
			//2.1/3.0 syntax: GEO:12;34
			String split[] = value.split(";");
			uri = new GeoUri();

			try {
				setLatitude(Double.valueOf(split[0]));
			} catch (NumberFormatException e2) {
				//do nothing (handled below)
			}

			boolean longMissing = false;
			if (split.length > 1) {
				try {
					setLongitude(Double.valueOf(split[1]));
				} catch (NumberFormatException e2) {
					//do nothing (handled below)
				}
			} else {
				longMissing = true;
			}

			if (getLatitude() == null && getLongitude() == null) {
				throw new SkipMeException("Unparseable value: \"" + value + "\"");
			} else if (longMissing) {
				warnings.add("Longitude missing from " + NAME + " type value: \"" + value + "\"");
			} else if (getLatitude() == null) {
				warnings.add("Could not parse latitude from " + NAME + " type value: \"" + value + "\"");
			} else if (getLongitude() == null) {
				warnings.add("Could not parse longitude from " + NAME + " type value: \"" + value + "\"");
			}
		}
	}

	@Override
	protected void doMarshalValue(Element parent, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder sb = new StringBuilder();
		doMarshalValue(sb, version, warnings, compatibilityMode);
		XCardUtils.appendChild(parent, "uri", sb.toString(), version);
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = XCardUtils.getFirstChildText(element, "uri");
		if (value != null) {
			doUnmarshalValue(value, version, warnings, compatibilityMode);
		}
	}
}
