package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;

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
public class StringUtilsTest {
	@Test
	public void mapToLowerCase() {
		Map<String, String> input = new HashMap<>();
		input.put("ONE", "TWO");
		input.put(null, "THREE");
		input.put("FOUR", null);

		Map<String, String> expected = new HashMap<>();
		expected.put("one", "two");
		expected.put(null, "three");
		expected.put("four", null);

		Map<String, String> actual = StringUtils.mapToLowercase(input);

		assertEquals(expected, actual);
	}

	@Test
	public void equalsIgnoreCase_string() {
		assertTrue(StringUtils.equalsIgnoreCase((String) null, null));
		assertFalse(StringUtils.equalsIgnoreCase("a", null));
		assertFalse(StringUtils.equalsIgnoreCase(null, "A"));
		assertTrue(StringUtils.equalsIgnoreCase("a", "A"));
		assertFalse(StringUtils.equalsIgnoreCase("a", "B"));
	}

	@Test
	public void equalsIgnoreCase_map() {
		Map<String, String> a = new HashMap<>();
		a.put("one", "TWO");
		assertTrue(StringUtils.equalsIgnoreCase((Map<String, String>) null, null));
		assertFalse(StringUtils.equalsIgnoreCase(a, null));
		assertFalse(StringUtils.equalsIgnoreCase(null, a));

		Map<String, String> b = new HashMap<>();
		b.put("ONE", "two");
		assertTrue(StringUtils.equalsIgnoreCase(a, b));

		Map<String, String> c = new HashMap<>();
		c.put("1", "2");
		assertFalse(StringUtils.equalsIgnoreCase(a, c));

		Map<String, String> d = new HashMap<>();
		d.put("one", "TWO");
		d.put("3", "4");
		assertFalse(StringUtils.equalsIgnoreCase(a, d));
	}

	@Test
	public void equalsIgnoreCaseIgnoreOrder() {
		List<String> a = Arrays.asList("a", "b");
		assertTrue(StringUtils.equalsIgnoreCaseIgnoreOrder(null, null));
		assertFalse(StringUtils.equalsIgnoreCaseIgnoreOrder(a, null));
		assertFalse(StringUtils.equalsIgnoreCaseIgnoreOrder(null, a));

		List<String> b = Arrays.asList("B", "A");
		assertTrue(StringUtils.equalsIgnoreCaseIgnoreOrder(a, b));

		List<String> c = Arrays.asList("C", "D");
		assertFalse(StringUtils.equalsIgnoreCaseIgnoreOrder(a, c));

		List<String> d = Arrays.asList("A", "B", "C", "D");
		assertFalse(StringUtils.equalsIgnoreCaseIgnoreOrder(a, d));
	}

	@Test
	public void hashIgnoreCase_string() {
		Object obj = 1;
		assertEquals(Objects.hash(obj, "abc"), StringUtils.hashIgnoreCase(obj, "AbC"));
	}

	@Test
	public void hashIgnoreCase_map() {
		Map<String, String> actual = new HashMap<>();
		actual.put("ONE", "TWO");
		Map<String, String> expected = new HashMap<>();
		expected.put("one", "two");

		assertEquals(expected.hashCode(), StringUtils.hashIgnoreCase(actual));
	}
}
