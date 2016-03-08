package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCardDataType;

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
public class RawPropertyTest {
	@Test
	public void constructors() throws Exception {
		RawProperty property = new RawProperty("name", "value");
		assertEquals("name", property.getPropertyName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());

		property = new RawProperty("name", "value", VCardDataType.TEXT);
		assertEquals("name", property.getPropertyName());
		assertEquals("value", property.getValue());
		assertEquals(VCardDataType.TEXT, property.getDataType());
	}

	@Test
	public void set_value() {
		RawProperty property = new RawProperty("name", "value");

		property.setPropertyName("name2");
		assertEquals("name2", property.getPropertyName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());

		property.setValue("value2");
		assertEquals("name2", property.getPropertyName());
		assertEquals("value2", property.getValue());
		assertNull(property.getDataType());

		property.setDataType(VCardDataType.TEXT);
		assertEquals("name2", property.getPropertyName());
		assertEquals("value2", property.getValue());
		assertEquals(VCardDataType.TEXT, property.getDataType());
	}

	@Test
	public void validate() {
		RawProperty property = new RawProperty("foo.bar", "value");
		assertValidate(property).run(24);

		property = new RawProperty("foobar", "value");
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		RawProperty property = new RawProperty("name", "value");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		RawProperty original = new RawProperty("name", "value", VCardDataType.TEXT);
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new RawProperty(null, null),
			new RawProperty("name", null),
			new RawProperty(null, "value"),
			new RawProperty(null, null, VCardDataType.TEXT),
			new RawProperty("name", null, VCardDataType.TEXT),
			new RawProperty(null, "value", VCardDataType.TEXT),
			new RawProperty("name", "value"),
			new RawProperty("name2", "value"),
			new RawProperty("name", "value2"),
			new RawProperty("name2", "value2"),
			new RawProperty("name", "value", VCardDataType.TEXT),
			new RawProperty("name", "value", VCardDataType.URI)
		);
		
		assertEqualsMethod(RawProperty.class, "name", "value")
		.constructor(new Class<?>[]{String.class, String.class}, null, null).test()
		.constructor("name", "value").test()
		.constructor("NAME", "value").test()
		.constructor("name", "value", VCardDataType.TEXT).test();
		//@formatter:on
	}
}
