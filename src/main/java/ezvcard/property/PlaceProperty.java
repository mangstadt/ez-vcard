package ezvcard.property;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.util.GeoUri;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Represents the location of a physical place.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PlaceProperty extends VCardProperty implements HasAltId {
	protected GeoUri geoUri;
	protected String uri;
	protected String text;

	/**
	 * Creates a new place property.
	 */
	public PlaceProperty() {
		//empty
	}

	/**
	 * Creates a new place property.
	 * @param latitude the latitude coordinate of the place
	 * @param longitude the longitude coordinate of the place
	 */
	public PlaceProperty(double latitude, double longitude) {
		setCoordinates(latitude, longitude);
	}

	/**
	 * Creates a new place property.
	 * @param text a text value representing the place
	 */
	public PlaceProperty(String text) {
		setText(text);
	}

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V4_0);
	}

	/**
	 * Gets the latitude of the location.
	 * @return the latitude or null if there is no geo information
	 */
	public Double getLatitude() {
		return (geoUri == null) ? null : geoUri.getCoordA();
	}

	/**
	 * Gets the longitude of the location.
	 * @return the longitude or null if there is no geo information
	 */
	public Double getLongitude() {
		return (geoUri == null) ? null : geoUri.getCoordB();
	}

	/**
	 * Gets the location's geo position.
	 * @return the geo position or null if there is no geo information
	 */
	public GeoUri getGeoUri() {
		return geoUri;
	}

	/**
	 * Sets the property's value to a set of geo coordinates.
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public void setCoordinates(double latitude, double longitude) {
		setGeoUri(new GeoUri.Builder(latitude, longitude).build());
	}

	/**
	 * Sets the property's value to a set of geo coordinates.
	 * @param geoUri the geo URI
	 */
	public void setGeoUri(GeoUri geoUri) {
		this.geoUri = geoUri;
		uri = null;
		text = null;
	}

	/**
	 * Gets the URI representing the location.
	 * @return the URI or null if no URI is set
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the property's value to a URI.
	 * @param uri the URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
		geoUri = null;
		text = null;
	}

	/**
	 * Gets the text value representing the location.
	 * @return the text value or null if no text value is set
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the property's value to a text value.
	 * @param text the text
	 */
	public void setText(String text) {
		this.text = text;
		geoUri = null;
		uri = null;
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
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (uri == null && text == null && geoUri == null) {
			warnings.add(new Warning(8));
		}
	}
}
