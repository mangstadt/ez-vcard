package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;

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
public class AddressTest {
	@Test
	public void extendedAddress() {
		Address property = new Address();
		assertNull(property.getExtendedAddress());
		assertTrue(property.getExtendedAddresses().isEmpty());

		property.setExtendedAddress("zero");
		assertEquals("zero", property.getExtendedAddress());
		assertEquals(Arrays.asList("zero"), property.getExtendedAddresses());

		property.getExtendedAddresses().add("one");
		assertEquals("zero", property.getExtendedAddress());
		assertEquals(Arrays.asList("zero", "one"), property.getExtendedAddresses());

		property.setExtendedAddress(null);
		assertNull(property.getExtendedAddress());
		assertTrue(property.getExtendedAddresses().isEmpty());

		property.setExtendedAddress("three");
		assertEquals("three", property.getExtendedAddress());
		assertEquals(Arrays.asList("three"), property.getExtendedAddresses());
	}

	@Test
	public void poBox() {
		Address property = new Address();
		assertNull(property.getPoBox());
		assertTrue(property.getPoBoxes().isEmpty());

		property.setPoBox("zero");
		assertEquals("zero", property.getPoBox());
		assertEquals(Arrays.asList("zero"), property.getPoBoxes());

		property.getPoBoxes().add("one");
		assertEquals("zero", property.getPoBox());
		assertEquals(Arrays.asList("zero", "one"), property.getPoBoxes());

		property.setPoBox(null);
		assertNull(property.getPoBox());
		assertTrue(property.getPoBoxes().isEmpty());

		property.setPoBox("three");
		assertEquals("three", property.getPoBox());
		assertEquals(Arrays.asList("three"), property.getPoBoxes());
	}

	@Test
	public void streetAddress() {
		Address property = new Address();
		assertNull(property.getStreetAddress());
		assertTrue(property.getStreetAddresses().isEmpty());

		property.setStreetAddress("zero");
		assertEquals("zero", property.getStreetAddress());
		assertEquals(Arrays.asList("zero"), property.getStreetAddresses());

		property.getStreetAddresses().add("one");
		assertEquals("zero", property.getStreetAddress());
		assertEquals(Arrays.asList("zero", "one"), property.getStreetAddresses());

		property.setStreetAddress(null);
		assertNull(property.getStreetAddress());
		assertTrue(property.getStreetAddresses().isEmpty());

		property.setStreetAddress("three");
		assertEquals("three", property.getStreetAddress());
		assertEquals(Arrays.asList("three"), property.getStreetAddresses());
	}

	@Test
	public void locality() {
		Address property = new Address();
		assertNull(property.getLocality());
		assertTrue(property.getLocalities().isEmpty());

		property.setLocality("zero");
		assertEquals("zero", property.getLocality());
		assertEquals(Arrays.asList("zero"), property.getLocalities());

		property.getLocalities().add("one");
		assertEquals("zero", property.getLocality());
		assertEquals(Arrays.asList("zero", "one"), property.getLocalities());

		property.setLocality(null);
		assertNull(property.getLocality());
		assertTrue(property.getLocalities().isEmpty());

		property.setLocality("three");
		assertEquals("three", property.getLocality());
		assertEquals(Arrays.asList("three"), property.getLocalities());
	}

	@Test
	public void region() {
		Address property = new Address();
		assertNull(property.getRegion());
		assertTrue(property.getRegions().isEmpty());

		property.setRegion("zero");
		assertEquals("zero", property.getRegion());
		assertEquals(Arrays.asList("zero"), property.getRegions());

		property.getRegions().add("one");
		assertEquals("zero", property.getRegion());
		assertEquals(Arrays.asList("zero", "one"), property.getRegions());

		property.setRegion(null);
		assertNull(property.getRegion());
		assertTrue(property.getRegions().isEmpty());

		property.setRegion("three");
		assertEquals("three", property.getRegion());
		assertEquals(Arrays.asList("three"), property.getRegions());
	}

	@Test
	public void postalCode() {
		Address property = new Address();
		assertNull(property.getPostalCode());
		assertTrue(property.getPostalCodes().isEmpty());

		property.setPostalCode("zero");
		assertEquals("zero", property.getPostalCode());
		assertEquals(Arrays.asList("zero"), property.getPostalCodes());

		property.getPostalCodes().add("one");
		assertEquals("zero", property.getPostalCode());
		assertEquals(Arrays.asList("zero", "one"), property.getPostalCodes());

		property.setPostalCode(null);
		assertNull(property.getPostalCode());
		assertTrue(property.getPostalCodes().isEmpty());

		property.setPostalCode("three");
		assertEquals("three", property.getPostalCode());
		assertEquals(Arrays.asList("three"), property.getPostalCodes());
	}

	@Test
	public void country() {
		Address property = new Address();
		assertNull(property.getCountry());
		assertTrue(property.getCountries().isEmpty());

		property.setCountry("zero");
		assertEquals("zero", property.getCountry());
		assertEquals(Arrays.asList("zero"), property.getCountries());

		property.getCountries().add("one");
		assertEquals("zero", property.getCountry());
		assertEquals(Arrays.asList("zero", "one"), property.getCountries());

		property.setCountry(null);
		assertNull(property.getCountry());
		assertTrue(property.getCountries().isEmpty());

		property.setCountry("three");
		assertEquals("three", property.getCountry());
		assertEquals(Arrays.asList("three"), property.getCountries());
	}

	@Test
	public void getExtendedAddressFull() {
		Address property = new Address();
		assertNull(property.getExtendedAddressFull());
		property.getExtendedAddresses().add("one");
		property.getExtendedAddresses().add("two");
		assertEquals("one,two", property.getExtendedAddressFull());
	}

	@Test
	public void getStreetAddressFull() {
		Address property = new Address();
		assertNull(property.getStreetAddressFull());
		property.getStreetAddresses().add("one");
		property.getStreetAddresses().add("two");
		assertEquals("one,two", property.getStreetAddressFull());
	}

	@Test
	public void validate() {
		Address property = new Address();
		assertValidate(property).run();

		property.getTypes().add(AddressType.DOM);
		property.getTypes().add(AddressType.HOME);
		property.getTypes().add(AddressType.PREF);
		assertValidate(property).versions(VCardVersion.V2_1, VCardVersion.V3_0).run();
		assertValidate(property).versions(VCardVersion.V4_0).run(9);
	}

	@Test
	public void toStringValues() {
		Address property = new Address();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Address original = new Address();
		original.setExtendedAddress("one");
		original.setPoBox("two");
		original.setStreetAddress("three");
		original.setLocality("four");
		original.setRegion("five");
		original.setPostalCode("six");
		original.setCountry("seven");

		//@formatter:off
		assertCopy(original)
		.notSame("getExtendedAddresses")
		.notSame("getPoBoxes")
		.notSame("getStreetAddresses")
		.notSame("getLocalities")
		.notSame("getRegions")
		.notSame("getPostalCodes")
		.notSame("getCountries");
		//@formatter:on
	}

	@Test
	public void equals() {
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		Address property = new Address();
		properties.add(property);

		property = new Address();
		property.setExtendedAddress("value");
		properties.add(property);

		property = new Address();
		property.setExtendedAddress("value2");
		properties.add(property);

		property = new Address();
		property.setPoBox("value");
		properties.add(property);

		property = new Address();
		property.setPoBox("value2");
		properties.add(property);

		property = new Address();
		property.setStreetAddress("value");
		properties.add(property);

		property = new Address();
		property.setStreetAddress("value2");
		properties.add(property);

		property = new Address();
		property.setLocality("value");
		properties.add(property);

		property = new Address();
		property.setLocality("value2");
		properties.add(property);

		property = new Address();
		property.setRegion("value");
		properties.add(property);

		property = new Address();
		property.setRegion("value2");
		properties.add(property);

		property = new Address();
		property.setPostalCode("value");
		properties.add(property);

		property = new Address();
		property.setCountry("value");
		properties.add(property);

		property = new Address();
		property.setCountry("value2");
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(Address.class)
		.constructor()
			.test()
			.method("setExtendedAddress", "value").test()
			.method("setPoBox", "value").test()
			.method("setStreetAddress", "value").test()
			.method("setLocality", "value").test()
			.method("setRegion", "value").test()
			.method("setPostalCode", "value").test()
			.method("setCountry", "value").test();
		//@formatter:on
	}
}
