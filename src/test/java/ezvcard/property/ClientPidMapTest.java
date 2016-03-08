package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static ezvcard.util.TestUtils.assertIntEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
public class ClientPidMapTest {
	@Test
	public void constructors() throws Exception {
		ClientPidMap property = new ClientPidMap(null, null);
		assertNull(property.getPid());
		assertNull(property.getUri());

		property = new ClientPidMap(1, "uri");
		assertIntEquals(1, property.getPid());
		assertEquals("uri", property.getUri());
	}

	@Test
	public void set_value() {
		ClientPidMap property = new ClientPidMap(1, "uri");

		property.setPid(2);
		assertIntEquals(2, property.getPid());
		assertEquals("uri", property.getUri());

		property.setUri("uri2");
		assertIntEquals(2, property.getPid());
		assertEquals("uri2", property.getUri());

		property.setPid(null);
		assertNull(property.getPid());
		assertEquals("uri2", property.getUri());

		property.setUri(null);
		assertNull(property.getPid());
		assertNull(property.getUri());
	}

	@Test
	public void random() {
		ClientPidMap clientpidmap = ClientPidMap.random(2);
		assertIntEquals(2, clientpidmap.getPid());
		assertTrue(clientpidmap.getUri().matches("urn:uuid:[-\\da-f]+"));
	}

	@Test
	public void validate() {
		ClientPidMap empty = new ClientPidMap(null, null);
		assertValidate(empty).versions(VCardVersion.V2_1).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V3_0).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		ClientPidMap withValue = new ClientPidMap(1, "urn:uuid:1234");
		assertValidate(withValue).versions(VCardVersion.V2_1).run(2);
		assertValidate(withValue).versions(VCardVersion.V3_0).run(2);
		assertValidate(withValue).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void toStringValues() {
		ClientPidMap property = new ClientPidMap(1, "uri");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		ClientPidMap original = new ClientPidMap(1, "uri");
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new ClientPidMap(null, null),
			new ClientPidMap(1, null),
			new ClientPidMap(2, null),
			new ClientPidMap(null, "uri"),
			new ClientPidMap(null, "uri2"),
			new ClientPidMap(1, "uri"),
			new ClientPidMap(2, "uri"),
			new ClientPidMap(1, "uri2")
		);
		
		assertEqualsMethod(ClientPidMap.class, 1, "uri")
		.constructor(new Class<?>[] { Integer.class, String.class }, null, null).test()
		.constructor(1, "uri").test();
		//@formatter:on
	}
}
