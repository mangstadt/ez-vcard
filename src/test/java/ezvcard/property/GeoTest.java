package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

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
public class GeoTest {
	@Test
	public void constructors() throws Exception {
		Geo property = new Geo(12.34, 56.78);
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(12.34, 56.78).build(), property.getGeoUri());

		GeoUri uri = new GeoUri.Builder(12.34, 56.78).build();
		property = new Geo(uri);
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
		assertEquals(uri, property.getGeoUri());

		property = new Geo((GeoUri) null);
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());
		assertNull(property.getGeoUri());
	}

	@Test
	public void set_value() {
		/*
		 * Make sure the GeoUri value does not loose its other properties when
		 * setLatitude or setLongitude is called.
		 */

		Geo property = new Geo(new GeoUri.Builder(12.34, 56.78).coordC(1.0).build());

		property.setLatitude(-12.34);
		assertEquals(-12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(-12.34, 56.78).coordC(1.0).build(), property.getGeoUri());

		property.setLatitude(null);
		assertEquals(0.0, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(null, 56.78).coordC(1.0).build(), property.getGeoUri());

		property.setLongitude(-56.78);
		assertEquals(0.0, property.getLatitude(), 0.1);
		assertEquals(-56.78, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(null, -56.78).coordC(1.0).build(), property.getGeoUri());

		property.setLongitude(null);
		assertEquals(0.0, property.getLatitude(), 0.1);
		assertEquals(0.0, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(null, null).coordC(1.0).build(), property.getGeoUri());

		property.setGeoUri(new GeoUri.Builder(12.34, 56.78).coordC(2.0).build());
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(12.34, 56.78).coordC(2.0).build(), property.getGeoUri());

		property.setGeoUri(null);
		property.setLatitude(12.34);
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(0.0, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(12.34, null).build(), property.getGeoUri());

		property.setGeoUri(null);
		property.setLongitude(56.78);
		assertEquals(0.0, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
		assertEquals(new GeoUri.Builder(null, 56.78).build(), property.getGeoUri());
	}

	@Test
	public void validate() {
		Geo empty = new Geo((GeoUri) null);
		assertValidate(empty).run(13, 14);

		Geo withValue = new Geo(-12.34, 56.78);
		assertValidate(withValue).run();
	}

	@Test
	public void toStringValues() {
		Geo property = new Geo(12.34, 56.78);
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Geo original = new Geo((GeoUri) null);
		assertCopy(original);

		GeoUri uri = new GeoUri.Builder(12.34, 56.78).coordC(2.0).build();
		original = new Geo(uri);
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new Geo((GeoUri) null),
			new Geo(new GeoUri.Builder(12.34, 56.78).build()),
			new Geo(new GeoUri.Builder(12.34, -56.78).build())
		);
		
		assertEqualsMethod(Geo.class, new GeoUri.Builder(12.34, 56.78).build())
		.constructor(new Class<?>[]{GeoUri.class}, (GeoUri)null).test()
		.constructor(new GeoUri.Builder(12.34, 56.78).build()).test();
		//@formatter:on
	}
}
