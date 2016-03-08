package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.parameter.KeyType;

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
public class KeyTest {
	@Test
	public void constructors() throws Exception {
		Key property = new Key();
		assertNull(property.getContentType());
		assertNull(property.getUrl());
		assertNull(property.getData());
		assertNull(property.getText());

		property = new Key("data".getBytes(), KeyType.GPG);
		assertEquals(KeyType.GPG, property.getContentType());
		assertNull(property.getUrl());
		assertArrayEquals("data".getBytes(), property.getData());
		assertNull(property.getText());

		property = new Key("one", KeyType.GPG);
		assertEquals(KeyType.GPG, property.getContentType());
		assertEquals("one", property.getUrl());
		assertNull(property.getData());
		assertNull(property.getText());

		File file = new File("pom.xml");
		property = new Key(file, KeyType.GPG);
		assertEquals(KeyType.GPG, property.getContentType());
		assertNull(property.getUrl());
		assertEquals(file.length(), property.getData().length);
		assertNull(property.getText());

		InputStream in = new ByteArrayInputStream("data".getBytes());
		property = new Key(in, KeyType.GPG);
		assertEquals(KeyType.GPG, property.getContentType());
		assertNull(property.getUrl());
		assertArrayEquals("data".getBytes(), property.getData());
		assertNull(property.getText());
	}

	@Test
	public void set_value() {
		Key property = new Key("url", KeyType.PGP);
		assertEquals("url", property.getUrl());
		assertNull(property.getData());
		assertEquals(KeyType.PGP, property.getContentType());
		assertNull(property.getText());

		property.setText("text", KeyType.GPG);
		assertNull(property.getUrl());
		assertNull(property.getData());
		assertEquals(KeyType.GPG, property.getContentType());
		assertEquals("text", property.getText());

		property.setUrl("url", KeyType.PGP);
		assertEquals("url", property.getUrl());
		assertNull(property.getData());
		assertEquals(KeyType.PGP, property.getContentType());
		assertNull(property.getText());

		property.setText("text", KeyType.GPG);
		property.setData("data".getBytes(), KeyType.PGP);
		assertNull(property.getUrl());
		assertArrayEquals("data".getBytes(), property.getData());
		assertEquals(KeyType.PGP, property.getContentType());
		assertNull(property.getText());
	}

	@Test
	public void validate() {
		Key empty = new Key();
		assertValidate(empty).run(8);

		Key withUrl = new Key("http://example.com", KeyType.PGP);
		assertValidate(withUrl).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(15);
		assertValidate(withUrl).versions(VCardVersion.V4_0).run();

		Key withText = new Key((String) null, KeyType.PGP);
		withText.setText("abc123", KeyType.PGP);
		assertValidate(withText).run();
	}

	@Test
	public void toStringValues() {
		Key property = new Key();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Key original = new Key();
		assertCopy(original);

		original = new Key();
		original.setText("text", KeyType.GPG);
		assertCopy(original);
	}

	@Test
	public void equals() {
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		Key property = new Key();
		properties.add(property);

		property = new Key();
		property.setText("text", KeyType.GPG);
		properties.add(property);

		property = new Key();
		property.setText("text2", KeyType.GPG);
		properties.add(property);

		property = new Key();
		property.setText("text", KeyType.PGP);
		properties.add(property);

		property = new Key();
		property.setText("text", null);
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(Key.class)
		.constructor()
			.test()
			.method("setText", "text", KeyType.GPG).test();
		//@formatter:on
	}
}
