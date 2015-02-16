package ezvcard.property;

import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
public class GeoTest {
	@Test
	public void validate() {
		Geo empty = new Geo(null);
		assertValidate(empty).run(13, 14);

		Geo withValue = new Geo(-12.34, 56.78);
		assertValidate(withValue).run();
	}

	@Test
	public void latitude() {
		Geo property = new Geo(null);
		assertNull(property.getLatitude());
		assertNull(property.getGeoUri());

		property.setLatitude(-12.34);
		assertEquals(-12.34, property.getLatitude(), 0.1);
		assertEquals(-12.34, property.getGeoUri().getCoordA(), 0.1);
	}

	@Test
	public void longitude() {
		Geo property = new Geo(null);
		assertNull(property.getLongitude());
		assertNull(property.getGeoUri());

		property.setLongitude(56.7878);
		assertEquals(56.7878, property.getLongitude(), 0.1);
		assertEquals(56.7878, property.getGeoUri().getCoordB(), 0.1);
	}
}
