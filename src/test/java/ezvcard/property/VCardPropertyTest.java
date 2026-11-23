package ezvcard.property;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ezvcard.SupportedVersions;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.VCardParameters;

/*
 Copyright (c) 2012-2026, Michael Angstadt
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
class VCardPropertyTest {
	@SuppressWarnings("unchecked")
	@Test
	void validate_overrideable_method_called() {
		VCardPropertyImpl property = spy(new VCardPropertyImpl());
		assertValidate(property).versions(V2_1).run();
		verify(property)._validate(anyList(), eq(V2_1), any(VCard.class));
	}

	@Test
	void validate_unsupported_version() {
		Version3Property property = new Version3Property();
		assertValidate(property).versions(V2_1, V4_0).run(2);
		assertValidate(property).versions(V3_0).run();
	}

	@Test
	void validate_valid_group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setGroup("Group-1");
		assertValidate(property).run();
	}

	@Test
	void validate_invalid_group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setGroup("mr.smith");
		assertValidate(property).versions(V2_1).run(32);
		assertValidate(property).versions(V3_0, V4_0).run(23);
	}

	@Test
	void validate_parameters() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setParameter("ALTID", "1");
		assertValidate(property).versions(V2_1, V3_0).run(6);
		assertValidate(property).versions(V4_0).run();
	}

	@Test
	void getSupportedVersions() {
		VCardPropertyImpl withoutSupportedVersions = new VCardPropertyImpl();
		assertArrayEquals(VCardVersion.values(), withoutSupportedVersions.getSupportedVersions());

		Version3Property withSupportedVersions = new Version3Property();
		assertArrayEquals(new VCardVersion[] { V3_0 }, withSupportedVersions.getSupportedVersions());
	}

	@Test
	void isSupportedBy() {
		VCardPropertyImpl withoutSupportedVersions = new VCardPropertyImpl();
		for (VCardVersion version : VCardVersion.values()) {
			assertTrue(withoutSupportedVersions.isSupportedBy(version));
		}

		Version3Property withSupportedVersions = new Version3Property();
		assertFalse(withSupportedVersions.isSupportedBy(V2_1));
		assertTrue(withSupportedVersions.isSupportedBy(V3_0));
		assertFalse(withSupportedVersions.isSupportedBy(V4_0));
	}

	@Test
	void group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		assertNull(property.getGroup());

		property.setGroup("group");
		assertEquals("group", property.getGroup());
	}

	@Test
	void parameters() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		assertEquals(new VCardParameters(), property.getParameters());
		assertThrows(NullPointerException.class, () -> property.setParameters(null));

		VCardParameters parameters = new VCardParameters();
		property.setParameters(parameters);
		assertSame(parameters, property.getParameters());

		property.addParameter("PARAM", "value");
		property.addParameter("PARAM", "value2");
		assertEquals("value", property.getParameter("PARAM"));
		assertEquals(Arrays.asList("value", "value2"), property.getParameters("PARAM"));
		VCardParameters expected = new VCardParameters();
		expected.put("PARAM", "value");
		expected.put("PARAM", "value2");
		assertEquals(expected, property.getParameters());

		property.setParameter("PARAM", "one");
		assertEquals("one", property.getParameter("PARAM"));
		assertEquals(Arrays.asList("one"), property.getParameters("PARAM"));
		expected = new VCardParameters();
		expected.put("PARAM", "one");
		assertEquals(expected, property.getParameters());

		property.removeParameter("PARAM");
		assertNull(property.getParameter("PARAM"));
		assertEquals(Arrays.asList(), property.getParameters("PARAM"));
		expected = new VCardParameters();
		assertEquals(expected, property.getParameters());
	}

	@Test
	void compareTo() {
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
	void copy() {
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
	void copy_constructor_throws_exception() {
		RuntimeException exception = new RuntimeException();
		CopyConstructorThrowsExceptionTest property = new CopyConstructorThrowsExceptionTest(exception);

		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, property::copy);
		assertTrue(e.getCause() instanceof InvocationTargetException);
		assertSame(e.getCause().getCause(), exception);
	}

	@Test
	void copy_no_copy_constructor() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, property::copy);
		assertTrue(e.getCause() instanceof NoSuchMethodException);
	}

	@Test
	void toStringValues() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		assertTrue(property.toStringValues().isEmpty());
	}

	@Test
	void toString_() {
		VCardProperty property = new VCardPropertyImpl();
		assertEquals(VCardPropertyImpl.class.getName() + " [ group=null | parameters={} ]", property.toString());

		property = new CopyConstructorTest("text");
		property.setGroup("group");
		property.addParameter("PARAM", "value");
		assertEquals(CopyConstructorTest.class.getName() + " [ group=group | parameters={PARAM=[value]} | value=text ]", property.toString());
	}

	@Test
	void equals() {
		//@formatter:off
		assertNothingIsEqual(new CopyConstructorTest(""));
		
		assertEqualsMethod(VCardPropertyImpl.class)
		.constructor();
		//@formatter:on
	}

	@Test
	void equals_group_ignore_case() {
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

		@Override
		protected Map<String, Object> toStringValues() {
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("value", value);
			return values;
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

	public static class VCardPropertyImpl extends VCardProperty {
		//empty
	}

	@SupportedVersions(V3_0)
	private class Version3Property extends VCardProperty {
		//empty
	}
}
