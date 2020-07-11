package ezvcard;

import static ezvcard.util.TestUtils.assertSetEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ezvcard.property.VCardProperty;
import ezvcard.util.StringUtils;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
 */

/**
 * @author Michael Angstadt
 */
public class ValidationWarningsTest {
	@Test
	public void isEmpty() {
		ValidationWarnings warnings = new ValidationWarnings();
		assertTrue(warnings.isEmpty());

		warnings.add(null, new ValidationWarning(0));
		assertFalse(warnings.isEmpty());
	}

	@Test
	public void getByProperty() {
		ValidationWarnings warnings = new ValidationWarnings();

		ValidationWarning v0 = new ValidationWarning(0);
		ValidationWarning v1 = new ValidationWarning(1);
		ValidationWarning v2 = new ValidationWarning(2);
		ValidationWarning v3 = new ValidationWarning(3);

		warnings.add(new TestProperty1(), v0);
		warnings.add(new TestProperty1(), v1);
		warnings.add(new TestProperty2(), v2);
		warnings.add(null, v3);

		assertSetEquals(new HashSet<ValidationWarning>(warnings.getByProperty(TestProperty1.class)), v0, v1);
		assertEquals(Arrays.asList(v2), warnings.getByProperty(TestProperty2.class));
		assertEquals(Arrays.asList(v3), warnings.getByProperty(null));
		assertEquals(Arrays.asList(), warnings.getByProperty(TestProperty3.class));
	}

	@Test
	public void toString_() {
		ValidationWarnings warnings = new ValidationWarnings();

		warnings.add(null, new ValidationWarning("one"));
		warnings.add(null, new ValidationWarning(2, "two"));
		warnings.add(new TestProperty1(), new ValidationWarning("three"));
		warnings.add(new TestProperty1(), new ValidationWarning(4, "four"));

		//@formatter:off
		List<String> expectedLines = Arrays.asList(
			"one",
			"W02: " + Messages.INSTANCE.getValidationWarning(2, "two"),
			"[TestProperty1] | three",
			"[TestProperty1] | W04: " + Messages.INSTANCE.getValidationWarning(4, "four")
		);
		//@formatter:on
		Collections.sort(expectedLines);

		String actual = warnings.toString();
		List<String> actualLines = Arrays.asList(actual.split(StringUtils.NEWLINE));
		Collections.sort(actualLines);

		assertEquals(expectedLines, actualLines);
	}

	private class TestProperty1 extends VCardProperty {
		//empty
	}

	private class TestProperty2 extends VCardProperty {
		//empty
	}

	private class TestProperty3 extends VCardProperty {
		//empty
	}
}
