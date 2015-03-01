package ezvcard.property;

import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.util.GeoUri;

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
 * @author Michael Angstadt
 */
public class PlacePropertyTest {
	private final String text = "New York, NY";
	private final String uri = "http://newyork.gov";
	private final GeoUri geoUriObj = new GeoUri.Builder(40.71448, -74.00598).build();

	@Test
	public void validate() {
		PlaceProperty empty = new PlaceProperty();
		assertValidate(empty).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		PlaceProperty withText = new PlaceProperty();
		withText.setText(text);
		assertValidate(withText).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2);
		assertValidate(withText).versions(VCardVersion.V4_0).run();

		PlaceProperty withUri = new PlaceProperty();
		withUri.setUri(uri);
		assertValidate(withUri).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2);
		assertValidate(withUri).versions(VCardVersion.V4_0).run();

		PlaceProperty withGeoUri = new PlaceProperty();
		withGeoUri.setGeoUri(geoUriObj);
		assertValidate(withGeoUri).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2);
		assertValidate(withGeoUri).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void setUri() {
		PlaceProperty property = new PlaceProperty();

		assertNull(property.getUri());

		property.setText(text);
		property.setGeoUri(geoUriObj);
		property.setUri(uri);

		assertEquals(uri, property.getUri());
		assertNull(property.getText());
		assertNull(property.getGeoUri());
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());
	}

	@Test
	public void setText() {
		PlaceProperty property = new PlaceProperty();

		assertNull(property.getText());

		property.setUri(uri);
		property.setGeoUri(geoUriObj);
		property.setText(text);

		assertEquals(text, property.getText());
		assertNull(property.getUri());
		assertNull(property.getGeoUri());
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());
	}

	@Test
	public void setGeoUri() {
		PlaceProperty property = new PlaceProperty();

		assertNull(property.getGeoUri());

		property.setText(text);
		property.setUri(uri);
		property.setGeoUri(geoUriObj);

		assertSame(property.getGeoUri(), geoUriObj);
		assertEquals(geoUriObj.getCoordA(), property.getLatitude());
		assertEquals(geoUriObj.getCoordB(), property.getLongitude());
	}
}
