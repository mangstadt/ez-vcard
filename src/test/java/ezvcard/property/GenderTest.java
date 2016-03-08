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
import java.util.List;

import org.junit.Test;

import ezvcard.VCardVersion;

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
public class GenderTest {
	@Test
	public void constructors() throws Exception {
		Gender property = new Gender((String) null);
		assertNull(property.getGender());
		assertNull(property.getText());

		property = new Gender("male");
		assertEquals("male", property.getGender());
		assertNull(property.getText());
	}

	@Test
	public void set_value() {
		Gender property = new Gender("male");

		property.setText("text");
		assertEquals("male", property.getGender());
		assertEquals("text", property.getText());

		property.setGender("female");
		assertEquals("female", property.getGender());
		assertEquals("text", property.getText());
	}

	@Test
	public void isMale() {
		Gender gender = new Gender("M");
		assertTrue(gender.isMale());
		assertFalse(gender.isFemale());
		assertFalse(gender.isOther());
		assertFalse(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isFemale() {
		Gender gender = new Gender("F");
		assertFalse(gender.isMale());
		assertTrue(gender.isFemale());
		assertFalse(gender.isOther());
		assertFalse(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isOther() {
		Gender gender = new Gender("O");
		assertFalse(gender.isMale());
		assertFalse(gender.isFemale());
		assertTrue(gender.isOther());
		assertFalse(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isNone() {
		Gender gender = new Gender("N");
		assertFalse(gender.isMale());
		assertFalse(gender.isFemale());
		assertFalse(gender.isOther());
		assertTrue(gender.isNone());
		assertFalse(gender.isUnknown());
	}

	@Test
	public void isUnknown() {
		Gender gender = new Gender("U");
		assertFalse(gender.isMale());
		assertFalse(gender.isFemale());
		assertFalse(gender.isOther());
		assertFalse(gender.isNone());
		assertTrue(gender.isUnknown());
	}

	@Test
	public void male() {
		Gender gender = Gender.male();
		assertEquals("M", gender.getGender());
	}

	@Test
	public void female() {
		Gender gender = Gender.female();
		assertEquals("F", gender.getGender());
	}

	@Test
	public void other() {
		Gender gender = Gender.other();
		assertEquals("O", gender.getGender());
	}

	@Test
	public void none() {
		Gender gender = Gender.none();
		assertEquals("N", gender.getGender());
	}

	@Test
	public void unknown() {
		Gender gender = Gender.unknown();
		assertEquals("U", gender.getGender());
	}

	@Test
	public void validate() {
		Gender empty = new Gender((String) null);
		assertValidate(empty).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		Gender male = Gender.male();
		assertValidate(male).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2);
		assertValidate(male).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void toStringValues() {
		Gender property = new Gender("male");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Gender original = new Gender((String) null);
		assertCopy(original);

		original = new Gender("male");
		original.setText("text");
		assertCopy(original);
	}

	@Test
	public void equals() {
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		Gender property = new Gender((String) null);
		properties.add(property);

		property = new Gender("male");
		properties.add(property);

		property = new Gender("male");
		property.setText("text");
		properties.add(property);

		property = new Gender("female");
		properties.add(property);

		property = new Gender((String) null);
		property.setText("text");
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(Gender.class, "male")
		.constructor(new Class<?>[]{String.class}, (String)null).test()
		.constructor("male")
			.test()
			.method("setText", "text").test();
		//@formatter:on
	}
}
