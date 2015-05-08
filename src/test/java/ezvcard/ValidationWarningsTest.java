package ezvcard;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.regex.Pattern;

import org.junit.Test;

import ezvcard.property.VCardProperty;

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
 */

/**
 * @author Michael Angstadt
 */
public class ValidationWarningsTest {
	@Test
	public void isEmpty() {
		ValidationWarnings warnings = new ValidationWarnings();
		assertTrue(warnings.isEmpty());

		warnings.add(null, new Warning(0));
		assertFalse(warnings.isEmpty());
	}

	@Test
	public void getByProperty() {
		ValidationWarnings warnings = new ValidationWarnings();

		Warning v0 = new Warning(0);
		Warning v1 = new Warning(1);
		Warning v2 = new Warning(2);
		Warning v3 = new Warning(3);

		warnings.add(new TestProperty1(), v0);
		warnings.add(new TestProperty1(), v1);
		warnings.add(new TestProperty2(), v2);
		warnings.add(null, v3);

		Set<Warning> v0Warnings = new HashSet<Warning>(
			warnings.getByProperty(TestProperty1.class));
		assertEquals(new HashSet<Warning>(Arrays.asList(v0, v1)), v0Warnings);

		assertEquals(Arrays.asList(v2), warnings.getByProperty(TestProperty2.class));
		assertEquals(Arrays.asList(v3), warnings.getByProperty(null));
		assertEquals(Arrays.asList(), warnings.getByProperty(TestProperty3.class));
	}

	@Test
	public void toString_() {
		ValidationWarnings warnings = new ValidationWarnings();

		warnings.add(null, new Warning("one"));
		warnings.add(null, new Warning("two", 2));
		warnings.add(new TestProperty1(), new Warning("three"));
		warnings.add(new TestProperty1(), new Warning("four", 4));

		//@formatter:off
		List<String> expected = Arrays.asList(
			"one",
			"W02: two",
			"[TestProperty1] | three",
			"[TestProperty1] | W04: four");
		//@formatter:on
		// XXX warning ordering is dependent on IdentityHashMap ordering, so sort lines.
		Collections.sort(expected);
		String actual = warnings.toString();
		List<String> actualSplit = Arrays.asList(Pattern.compile(NEWLINE).split(actual));
		Collections.sort(actualSplit);
		assertEquals(expected, actualSplit);
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
