package ezvcard;

import static ezvcard.util.VCardStringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ezvcard.ValidationWarnings.WarningsGroup;
import ezvcard.property.VCardProperty;

/*
 Copyright (c) 2013, Michael Angstadt
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
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		ValidationWarnings warnings = new ValidationWarnings(groups, VCardVersion.V2_1);
		assertTrue(warnings.isEmpty());

		groups.add(new WarningsGroup(new TestProperty1(), Arrays.asList("four")));
		assertFalse(warnings.isEmpty());
	}

	@Test
	public void getByProperty() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		WarningsGroup group1 = new WarningsGroup(new TestProperty1(), Arrays.asList("one", "two"));
		groups.add(group1);
		WarningsGroup group2 = new WarningsGroup(new TestProperty1(), Arrays.asList("three"));
		groups.add(group2);
		groups.add(new WarningsGroup(new TestProperty2(), Arrays.asList("four")));
		WarningsGroup group3 = new WarningsGroup(null, Arrays.asList("five"));
		groups.add(group3);
		ValidationWarnings warnings = new ValidationWarnings(groups, VCardVersion.V2_1);

		assertEquals(Arrays.asList(group1, group2), warnings.getByProperty(TestProperty1.class));
		assertEquals(Arrays.asList(group3), warnings.getByProperty(null));
	}

	@Test
	public void getByProperty_empty() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.asList("one", "two")));
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.asList("three")));
		ValidationWarnings warnings = new ValidationWarnings(groups, VCardVersion.V2_1);
		assertEquals(Arrays.asList(), warnings.getByProperty(TestProperty2.class));
	}

	@Test
	public void toString_() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		groups.add(new WarningsGroup(null, Arrays.asList("one", "two")));
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.asList("three", "four")));
		ValidationWarnings warnings = new ValidationWarnings(groups, VCardVersion.V2_1);

		//@formatter:off
		String expected =
		"one" + NEWLINE +
		"two" + NEWLINE +
		"[TestProperty1]: three" + NEWLINE +
		"[TestProperty1]: four";
		//@formatter:on
		String actual = warnings.toString();
		assertEquals(expected, actual);
	}

	private class TestProperty1 extends VCardProperty {
		//empty
	}

	private class TestProperty2 extends VCardProperty {
		//empty
	}
}
