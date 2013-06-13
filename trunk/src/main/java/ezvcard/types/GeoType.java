package ezvcard.types;

import java.text.NumberFormat;
import java.util.List;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.util.GeoUri;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
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
 * <p>
 * A set of latitude/longitude coordinates. There is no rule for what these
 * coordinates must represent, but the meaning could vary depending on the value
 * of {@link KindType}:
 * </p>
 * 
 * <table border="1">
 * <tr>
 * <th>KIND value</th>
 * <th>GEO meaning</th>
 * </tr>
 * <tr>
 * <td>"individual"</td>
 * <td>the location of the person's home or workplace.</td>
 * </tr>
 * <tr>
 * <td>"group"</td>
 * <td>the location of the group's meeting place.</td>
 * </tr>
 * <tr>
 * <td>"org"</td>
 * <td>the coordinates of the organization's headquarters.</td>
 * </tr>
 * <tr>
 * <td>"location"</td>
 * <td>the coordinates of the location itself.</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * GeoType geo = new GeoType(-123.456, 12.54);
 * vcard.setGeo(geo);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>GEO</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class GeoType extends VCardType implements HasAltId {
	public static final String NAME = "GEO";
	private GeoUri uri = new GeoUri();

	/**
	 * Creates an empty geo property.
	 */
	public GeoType() {
		this(null, null);
	}

	/**
	 * Creates a geo property.
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return subTypes.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
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

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		sb.append(write(version));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		parse(value, version, warnings);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		parent.uri(write(parent.version()));
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.uri();
		if (value != null) {
			parse(value, element.version(), warnings);
		} else {
			throw new SkipMeException("No URI found.");
		}
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		uri = new GeoUri();

		String latitude = element.firstValue("latitude");
		if (latitude == null) {
			throw new SkipMeException("Latitude missing.");
		}
		try {
			setLatitude(Double.parseDouble(latitude));
		} catch (NumberFormatException e) {
			throw new SkipMeException("Could not parse latitude: " + latitude);
		}

		String longitude = element.firstValue("longitude");
		if (longitude == null) {
			throw new SkipMeException("Longitude missing.");
		}
		try {
			setLongitude(Double.parseDouble(longitude));
		} catch (NumberFormatException e) {
			throw new SkipMeException("Could not parse longitude: " + longitude);
		}
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		return JCardValue.uri(write(version));
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		parse(value.getFirstValueAsString(), version, warnings);
	}

	private void parse(String value, VCardVersion version, List<String> warnings) {
		if (version == VCardVersion.V4_0) {
			try {
				uri = new GeoUri(value);
			} catch (IllegalArgumentException e) {
				throw new SkipMeException("Invalid geo URI: " + value);
			}
		} else {
			String split[] = value.split(";");

			if (split.length != 2) {
				throw new SkipMeException("Invalid value: " + value);
			}

			uri = new GeoUri();
			String latitude = split[0];
			String longitude = split[1];

			try {
				setLatitude(Double.valueOf(latitude));
			} catch (NumberFormatException e) {
				throw new SkipMeException("Could not parse latitude: " + latitude);
			}

			try {
				setLongitude(Double.valueOf(longitude));
			} catch (NumberFormatException e) {
				throw new SkipMeException("Could not parse longtude: " + longitude);
			}
		}
	}

	private String write(VCardVersion version) {
		if (getLatitude() == null || getLongitude() == null) {
			throw new SkipMeException("Latitude and/or longitude is missing.");
		}

		if (version == VCardVersion.V4_0) {
			return uri.toString(6);
		} else {
			NumberFormat nf = GeoUri.buildNumberFormat(6);
			return nf.format(getLatitude()) + ';' + nf.format(getLongitude());
		}
	}
}
