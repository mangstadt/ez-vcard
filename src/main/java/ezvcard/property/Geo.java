package ezvcard.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.Pid;
import ezvcard.util.GeoUri;

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
 * Defines a set of latitude/longitude coordinates. There is no rule as to what
 * these coordinates represent, but their meaning could vary depending on the
 * value of the vCard's {@link Kind} property:
 * </p>
 * 
 * <table class="simpleTable">
 * <caption>{@link Geo} meanings by {@link Kind} value</caption>
 * <tr>
 * <th>{@link Kind} value</th>
 * <th>{@link Geo} meaning</th>
 * </tr>
 * <tr>
 * <td>"individual"</td>
 * <td>The location of the person's home or workplace.</td>
 * </tr>
 * <tr>
 * <td>"group"</td>
 * <td>The location of the group's meeting place.</td>
 * </tr>
 * <tr>
 * <td>"org"</td>
 * <td>The coordinates of the organization's headquarters.</td>
 * </tr>
 * <tr>
 * <td>"location"</td>
 * <td>The coordinates of the location itself.</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Geo geo = new Geo(40.7127, -74.0059);
 * vcard.setGeo(geo);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code GEO}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350 p.38</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426 p.16</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
 */
public class Geo extends VCardProperty implements HasAltId {
	private GeoUri uri;

	/**
	 * Creates a geo property.
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public Geo(Double latitude, Double longitude) {
		this(new GeoUri.Builder(latitude, longitude).build());
	}

	/**
	 * Creates a geo property.
	 * @param uri the geo URI
	 */
	public Geo(GeoUri uri) {
		this.uri = uri;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Geo(Geo original) {
		super(original);
		uri = original.uri;
	}

	/**
	 * Gets the latitude.
	 * @return the latitude
	 */
	public Double getLatitude() {
		return (uri == null) ? null : uri.getCoordA();
	}

	/**
	 * Sets the latitude.
	 * @param latitude the latitude
	 */
	public void setLatitude(Double latitude) {
		if (uri == null) {
			uri = new GeoUri.Builder(latitude, null).build();
		} else {
			uri = new GeoUri.Builder(uri).coordA(latitude).build();
		}
	}

	/**
	 * Gets the longitude.
	 * @return the longitude
	 */
	public Double getLongitude() {
		return (uri == null) ? null : uri.getCoordB();
	}

	/**
	 * Sets the longitude.
	 * @param longitude the longitude
	 */
	public void setLongitude(Double longitude) {
		if (uri == null) {
			uri = new GeoUri.Builder(null, longitude).build();
		} else {
			uri = new GeoUri.Builder(uri).coordB(longitude).build();
		}
	}

	/**
	 * Gets the raw object used for storing the GEO information. This can be
	 * used to supplement the GEO value with additional information (such as
	 * altitude or level of accuracy). Geo URIs are only supported by vCard
	 * version 4.0. Only latitude and longitude values are used when marshalling
	 * to earlier vCard versions.
	 * @return the geo URI object or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5870">RFC 5870</a>
	 */
	public GeoUri getGeoUri() {
		return uri;
	}

	/**
	 * Sets the raw object used for storing the GEO information. This can be
	 * used to supplement the GEO value with additional information (such as
	 * altitude or level of accuracy). Geo URIs are only supported by vCard
	 * version 4.0. Only latitude and longitude values are used when marshalling
	 * to earlier vCard versions.
	 * @param uri the geo URI object
	 * @see <a href="http://tools.ietf.org/html/rfc5870">RFC 5870</a>
	 */
	public void setGeoUri(GeoUri uri) {
		this.uri = uri;
	}

	/**
	 * Gets the TYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return parameters.getType();
	}

	/**
	 * Sets the TYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param type the TYPE value (this should be either "work" or "home") or
	 * null to remove
	 */
	public void setType(String type) {
		parameters.setType(type);
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return parameters.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		parameters.setMediaType(mediaType);
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

	@Override
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		if (getLatitude() == null) {
			warnings.add(new ValidationWarning(13));
		}
		if (getLongitude() == null) {
			warnings.add(new ValidationWarning(14));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("uri", uri);
		return values;
	}

	@Override
	public Geo copy() {
		return new Geo(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Geo other = (Geo) obj;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		return true;
	}
}
