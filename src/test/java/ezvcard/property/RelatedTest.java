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
import ezvcard.parameter.RelatedType;
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
public class RelatedTest {
	private final String text = "Edna Smith";
	private final String uri = "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af";

	@Test
	public void constructors() throws Exception {
		Related property = new Related();
		assertNull(property.getUri());
		assertNull(property.getText());
		assertEquals(Arrays.asList(), property.getTypes());

		property = new Related(uri);
		assertEquals(uri, property.getUri());
		assertNull(property.getText());
		assertEquals(Arrays.asList(), property.getTypes());
	}

	@Test
	public void set_value() {
		Related property = new Related(uri);

		property.setText(text);
		assertNull(property.getUri());
		assertEquals(text, property.getText());
		assertEquals(Arrays.asList(), property.getTypes());

		property.setUri(uri);
		assertEquals(uri, property.getUri());
		assertNull(property.getText());
		assertEquals(Arrays.asList(), property.getTypes());

		property.getTypes().add(RelatedType.ACQUAINTANCE);
		assertEquals(uri, property.getUri());
		assertNull(property.getText());
		assertEquals(Arrays.asList(RelatedType.ACQUAINTANCE), property.getTypes());

		property.getTypes().add(RelatedType.AGENT);
		assertEquals(uri, property.getUri());
		assertNull(property.getText());
		assertEquals(Arrays.asList(RelatedType.ACQUAINTANCE, RelatedType.AGENT), property.getTypes());

		property.getTypes().remove(RelatedType.AGENT);
		assertEquals(uri, property.getUri());
		assertNull(property.getText());
		assertEquals(Arrays.asList(RelatedType.ACQUAINTANCE), property.getTypes());
	}

	@Test
	public void validate() {
		Related empty = new Related();
		assertValidate(empty).versions(VCardVersion.V2_1).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V3_0).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		Related withText = new Related();
		withText.setText(text);
		assertValidate(withText).versions(VCardVersion.V2_1).run(2);
		assertValidate(withText).versions(VCardVersion.V3_0).run(2);
		assertValidate(withText).versions(VCardVersion.V4_0).run();

		Related withUri = new Related();
		withUri.setUri(uri);
		assertValidate(withUri).versions(VCardVersion.V2_1).run(2);
		assertValidate(withUri).versions(VCardVersion.V3_0).run(2);
		assertValidate(withUri).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void email() {
		Related property = Related.email("john.doe@example.com");
		assertNull(property.getText());
		assertEquals("mailto:john.doe@example.com", property.getUri());
	}

	@Test
	public void im() {
		Related property = Related.im("aim", "john.doe");
		assertNull(property.getText());
		assertEquals("aim:john.doe", property.getUri());
	}

	@Test
	public void telephone() {
		Related property = Related.telephone(new TelUri.Builder("+1-555-555-5555").build());
		assertNull(property.getText());
		assertEquals("tel:+1-555-555-5555", property.getUri());
	}

	@Test
	public void toStringValues() {
		Related property = new Related(uri);
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Related original = new Related();
		assertCopy(original);

		original = new Related(uri);
		assertCopy(original);

		original = new Related(uri);
		original.getTypes().add(RelatedType.ACQUAINTANCE);
		assertCopy(original);

		original = new Related(uri);
		original.setText("text");
		assertCopy(original);
	}

	@Test
	public void equals() {
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		Related property = new Related();
		properties.add(property);

		property = new Related("uri");
		properties.add(property);

		property = new Related("uri");
		property.getTypes().add(RelatedType.ACQUAINTANCE);
		properties.add(property);

		property = new Related("uri2");
		properties.add(property);

		property = new Related();
		property.setText("text");
		properties.add(property);

		property = new Related();
		property.setText("text");
		property.getTypes().add(RelatedType.ACQUAINTANCE);
		properties.add(property);

		property = new Related();
		property.setText("text2");
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(Related.class)
		.constructor(new Class<?>[]{String.class}, (String)null).test()
		.constructor("uri")
			.test()
			.method("setText", "text").test();
		//@formatter:on
	}
}
