package ezvcard.property;

import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import ezvcard.SupportedVersions;
import ezvcard.VCard;
import ezvcard.VCardVersion;

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
public class VCardPropertyTest {
	@SuppressWarnings("unchecked")
	@Test
	public void validate_overrideable_method_called() {
		VCardPropertyImpl property = spy(new VCardPropertyImpl());
		assertValidate(property).versions(VCardVersion.V2_1).run();
		verify(property)._validate(anyList(), eq(VCardVersion.V2_1), any(VCard.class));
	}

	@Test
	public void validate_unsupported_version() {
		Version3Property property = new Version3Property();
		assertValidate(property).versions(VCardVersion.V2_1).run(2);
		assertValidate(property).versions(VCardVersion.V3_0).run();
		assertValidate(property).versions(VCardVersion.V4_0).run(2);
	}

	@Test
	public void validate_valid_group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setGroup("Group-1");
		assertValidate(property).run();
	}

	@Test
	public void validate_invalid_group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setGroup("mr.smith");
		assertValidate(property).run(23);
	}

	@Test
	public void validate_parameters() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setParameter("ALTID", "1");
		assertValidate(property).versions(VCardVersion.V2_1).run(6);
		assertValidate(property).versions(VCardVersion.V3_0).run(6);
		assertValidate(property).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void getSupportedVersions() {
		VCardPropertyImpl withoutSupportedVersions = new VCardPropertyImpl();
		assertArrayEquals(VCardVersion.values(), withoutSupportedVersions.getSupportedVersions());

		Version3Property withSupportedVersions = new Version3Property();
		assertArrayEquals(new VCardVersion[] { VCardVersion.V3_0 }, withSupportedVersions.getSupportedVersions());
	}

	@Test
	public void isSupportedBy() {
		VCardPropertyImpl withoutSupportedVersions = new VCardPropertyImpl();
		for (VCardVersion version : VCardVersion.values()) {
			assertTrue(withoutSupportedVersions.isSupportedBy(version));
		}

		Version3Property withSupportedVersions = new Version3Property();
		assertFalse(withSupportedVersions.isSupportedBy(VCardVersion.V2_1));
		assertTrue(withSupportedVersions.isSupportedBy(VCardVersion.V3_0));
		assertFalse(withSupportedVersions.isSupportedBy(VCardVersion.V4_0));
	}

	@Test
	public void group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		assertNull(property.getGroup());

		property.setGroup("group");
		assertEquals("group", property.getGroup());
	}

	@Test
	public void compareTo() {
		VCardPropertyImpl one = new VCardPropertyImpl();
		one.getParameters().setPref(1);

		VCardPropertyImpl two = new VCardPropertyImpl();
		one.getParameters().setPref(2);

		VCardPropertyImpl null1 = new VCardPropertyImpl();
		VCardPropertyImpl null2 = new VCardPropertyImpl();

		assertEquals(-1, one.compareTo(two));
		assertEquals(1, two.compareTo(one));
		assertEquals(0, one.compareTo(one));
		assertEquals(-1, one.compareTo(null1));
		assertEquals(1, null1.compareTo(one));
		assertEquals(0, null1.compareTo(null2));
	}

	@Test
	public void copy() {
		CopyConstructorTest property = new CopyConstructorTest("value");
		property.setGroup("group");
		property.getParameters().setLanguage("en-us");
		CopyConstructorTest copy = (CopyConstructorTest) property.copy();

		assertNotSame(property, copy);
		assertEquals(property.value, copy.value);

		assertEquals(property.getGroup(), copy.getGroup());
		assertNotSame(property.getParameters(), copy.getParameters());
		assertEquals(property.getParameters(), copy.getParameters());
	}

	@Test
	public void copy_constructor_throws_exception() {
		RuntimeException exception = new RuntimeException();
		CopyConstructorThrowsExceptionTest property = new CopyConstructorThrowsExceptionTest(exception);
		try {
			property.copy();
			fail("Expected an exception to be thrown.");
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getCause() instanceof InvocationTargetException);
			assertSame(e.getCause().getCause(), exception);
		}
	}

	@Test
	public void copy_no_copy_constructor() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		try {
			property.copy();
			fail("Expected an exception to be thrown.");
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getCause() instanceof NoSuchMethodException);
		}
	}

	@Test
	public void equals_essentials() {
		VCardPropertyImpl one = new VCardPropertyImpl();
		assertEqualsMethodEssentials(one);
	}

	@Test
	public void equals_different_group() {
		VCardPropertyImpl one = new VCardPropertyImpl();
		one.setGroup("one");
		VCardPropertyImpl two = new VCardPropertyImpl();
		two.setGroup("two");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_different_parameters() {
		VCardPropertyImpl one = new VCardPropertyImpl();
		one.addParameter("one", "value");
		VCardPropertyImpl two = new VCardPropertyImpl();
		two.addParameter("two", "value");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_group_ignore_case() {
		VCardPropertyImpl one = new VCardPropertyImpl();
		one.setGroup("GROUP");
		VCardPropertyImpl two = new VCardPropertyImpl();
		two.setGroup("group");
		assertEqualsAndHash(one, two);
	}

	private static class CopyConstructorTest extends VCardProperty {
		private String value;

		public CopyConstructorTest(String value) {
			this.value = value;
		}

		@SuppressWarnings("unused")
		public CopyConstructorTest(CopyConstructorTest original) {
			super(original);
			value = original.value;
		}
	}

	private static class CopyConstructorThrowsExceptionTest extends VCardProperty {
		private final RuntimeException e;

		public CopyConstructorThrowsExceptionTest(RuntimeException e) {
			this.e = e;
		}

		@SuppressWarnings("unused")
		public CopyConstructorThrowsExceptionTest(CopyConstructorThrowsExceptionTest original) {
			throw original.e;
		}
	}

	private class VCardPropertyImpl extends VCardProperty {
		//empty
	}

	@SupportedVersions(VCardVersion.V3_0)
	private class Version3Property extends VCardProperty {
		//empty
	}
}
