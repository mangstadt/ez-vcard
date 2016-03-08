package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.util.GeoUri;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
	public void constructors() throws Exception {
		PlaceProperty property = new PlaceProperty();
		assertNull(property.getText());
		assertNull(property.getUri());
		assertNull(property.getGeoUri());
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());

		property = new PlaceProperty("text");
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertNull(property.getGeoUri());
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());

		property = new PlaceProperty(12.34, 56.78);
		assertNull(property.getText());
		assertNull(property.getUri());
		assertEquals(new GeoUri.Builder(12.34, 56.78).build(), property.getGeoUri());
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
	}

	@Test
	public void set_value() {
		PlaceProperty property = new PlaceProperty();

		property.setText("text");
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertNull(property.getGeoUri());
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());

		property.setCoordinates(12.34, 56.78);
		assertNull(property.getText());
		assertNull(property.getUri());
		assertEquals(new GeoUri.Builder(12.34, 56.78).build(), property.getGeoUri());
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);

		property.setUri("uri");
		assertNull(property.getText());
		assertEquals("uri", property.getUri());
		assertNull(property.getGeoUri());
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());

		property.setGeoUri(new GeoUri.Builder(12.34, 56.78).coordC(1.0).build());
		assertNull(property.getText());
		assertNull(property.getUri());
		assertEquals(new GeoUri.Builder(12.34, 56.78).coordC(1.0).build(), property.getGeoUri());
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);

		property.setCoordinates(12.34, 56.78);
		assertNull(property.getText());
		assertNull(property.getUri());
		assertEquals(new GeoUri.Builder(12.34, 56.78).build(), property.getGeoUri());
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);

		property.setText("text");
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertNull(property.getGeoUri());
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());
	}

	@Test
	public void validate() {
		PlaceProperty empty = new PlaceProperty();
		assertValidate(empty).run(8);

		PlaceProperty withText = new PlaceProperty();
		withText.setText(text);
		assertValidate(withText).run();

		PlaceProperty withUri = new PlaceProperty();
		withUri.setUri(uri);
		assertValidate(withUri).run();

		PlaceProperty withGeoUri = new PlaceProperty();
		withGeoUri.setGeoUri(geoUriObj);
		assertValidate(withGeoUri).run();
	}

	@Test
	public void toStringValues() {
		PlaceProperty property = new PlaceProperty();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		PlaceProperty original = new PlaceProperty();
		assertCopy(original);

		original = new PlaceProperty("text");
		assertCopy(original);

		original = new PlaceProperty(12.34, 56.78);
		assertCopy(original);

		original = new PlaceProperty();
		original.setUri("uri");
		assertCopy(original);
	}

	@Test
	public void equals() {
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		PlaceProperty property = new PlaceProperty();
		properties.add(property);

		property = new PlaceProperty("text");
		properties.add(property);

		property = new PlaceProperty("text2");
		properties.add(property);

		property = new PlaceProperty(12.34, 56.78);
		properties.add(property);

		property = new PlaceProperty(-12.34, -56.78);
		properties.add(property);

		property = new PlaceProperty();
		property.setUri("uri");
		properties.add(property);

		property = new PlaceProperty();
		property.setUri("uri2");
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(PlaceProperty.class)
		.constructor().test()
		.constructor("text")
		.constructor(new Class<?>[]{double.class, double.class}, 12.34, 56.78)
		.constructor().method("setUri", "uri").test();
		//@formatter:on
	}
}
