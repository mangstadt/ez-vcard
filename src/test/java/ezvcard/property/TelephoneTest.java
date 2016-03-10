package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.parameter.TelephoneType;
import ezvcard.util.TelUri;

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
public class TelephoneTest {
	@Test
	public void constructors() throws Exception {
		Telephone property = new Telephone((String) null);
		assertNull(property.getText());
		assertNull(property.getUri());
		assertEquals(Arrays.asList(), property.getTypes());

		property = new Telephone("text");
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertEquals(Arrays.asList(), property.getTypes());

		property = new Telephone(new TelUri.Builder("+1").build());
		assertNull(property.getText());
		assertEquals(new TelUri.Builder("+1").build(), property.getUri());
		assertEquals(Arrays.asList(), property.getTypes());
	}

	@Test
	public void set_value() {
		Telephone property = new Telephone((String) null);

		property.setText("text");
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertEquals(Arrays.asList(), property.getTypes());

		property.setUri(new TelUri.Builder("+1").build());
		assertNull(property.getText());
		assertEquals(new TelUri.Builder("+1").build(), property.getUri());
		assertEquals(Arrays.asList(), property.getTypes());

		property.setText("text");
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertEquals(Arrays.asList(), property.getTypes());

		property.getTypes().add(TelephoneType.WORK);
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertEquals(Arrays.asList(TelephoneType.WORK), property.getTypes());

		property.getTypes().add(TelephoneType.HOME);
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertEquals(Arrays.asList(TelephoneType.WORK, TelephoneType.HOME), property.getTypes());

		property.getTypes().remove(TelephoneType.HOME);
		assertEquals("text", property.getText());
		assertNull(property.getUri());
		assertEquals(Arrays.asList(TelephoneType.WORK), property.getTypes());
	}

	@Test
	public void validate() {
		Telephone empty = new Telephone((String) null);
		assertValidate(empty).run(8);

		Telephone withText = new Telephone("(800) 555-5555");
		assertValidate(withText).run();

		Telephone withUri = new Telephone(new TelUri.Builder("+1-800-555-5555").extension("101").build());
		assertValidate(withUri).versions(VCardVersion.V2_1).run(19);
		assertValidate(withUri).versions(VCardVersion.V3_0).run(19);
		assertValidate(withUri).versions(VCardVersion.V4_0).run();

		Telephone withTypes = new Telephone("(800) 555-5555");
		withTypes.getTypes().add(TelephoneType.TEXTPHONE);
		withTypes.getTypes().add(TelephoneType.PREF);
		assertValidate(withTypes).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(9);
		assertValidate(withTypes).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void toStringValues() {
		Telephone property = new Telephone("text");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Telephone original = new Telephone((String) null);
		assertCopy(original);

		original = new Telephone("text");
		assertCopy(original);

		original = new Telephone(new TelUri.Builder("+1").build());
		assertCopy(original);

		original = new Telephone("text");
		original.getTypes().add(TelephoneType.HOME);
		assertCopy(original);
	}

	@Test
	public void equals() {
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		Telephone property = new Telephone((String) null);
		properties.add(property);

		property = new Telephone("text");
		properties.add(property);

		property = new Telephone("text");
		property.getTypes().add(TelephoneType.HOME);
		properties.add(property);

		property = new Telephone("text2");
		properties.add(property);

		property = new Telephone(new TelUri.Builder("+1").build());
		properties.add(property);

		property = new Telephone(new TelUri.Builder("+1").build());
		property.getTypes().add(TelephoneType.HOME);
		properties.add(property);

		property = new Telephone(new TelUri.Builder("+2").build());
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(Telephone.class, "text")
		.constructor(new Class<?>[]{String.class}, (String)null).test()
		.constructor("text").test()
		.constructor(new TelUri.Builder("+1").build()).test();
		//@formatter:on
	}
}
