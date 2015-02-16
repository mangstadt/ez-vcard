package ezvcard.util;

import static ezvcard.util.TestUtils.assertSetEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

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
public class ListMultimapTest {
	@Test
	public void first() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "11");
		map.put("one", "111");

		assertEquals("1", map.first("one"));
	}

	@Test
	public void get() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("two", "22");
		map.put("two", "2");

		assertEquals(Arrays.asList("1"), map.get("one"));
		assertEquals(Arrays.asList("22", "2"), map.get("two"));
		assertTrue(map.get("three").isEmpty());
	}

	@Test
	public void containsKey() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("two", "22");
		map.put("two", "2");

		assertTrue(map.containsKey("one"));
		assertTrue(map.containsKey("two"));
		assertFalse(map.containsKey("three"));
	}

	@Test
	public void put() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");
		map.put("one", "11");
		map.put("two", "2");

		assertEquals(Arrays.asList("1", "111", "11"), map.get("one"));
		assertEquals(Arrays.asList("2"), map.get("two"));
	}

	@Test
	public void putAll() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.putAll("one", Arrays.asList("111", "11"));
		map.put("two", "2");

		assertEquals(Arrays.asList("1", "111", "11"), map.get("one"));
		assertEquals(Arrays.asList("2"), map.get("two"));
	}

	@Test
	public void replace_string() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");

		assertEquals(Arrays.asList("1", "111"), map.replace("one", "11"));
		assertEquals(Arrays.asList("11"), map.get("one"));
	}

	@Test
	public void replace_collection() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");

		assertEquals(Arrays.asList("1", "111"), map.replace("one", Arrays.asList("11", "1111")));
		assertEquals(Arrays.asList("11", "1111"), map.get("one"));
	}

	@Test
	public void replace_null_value_string() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");

		assertEquals(Arrays.asList("1", "111"), map.replace("one", (String) null));
		assertTrue(map.isEmpty());
	}

	@Test
	public void replace_null_value_collection() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");

		assertEquals(Arrays.asList("1", "111"), map.replace("one", (Collection<String>) null));
		assertTrue(map.isEmpty());
	}

	@Test
	public void remove() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("two", "22");
		map.put("two", "2");

		assertFalse(map.remove("three", "3"));
		assertFalse(map.remove("two", "222"));
		assertTrue(map.remove("two", "2"));
		assertEquals(Arrays.asList("22"), map.get("two"));
		assertEquals(Arrays.asList("1"), map.get("one"));
	}

	@Test
	public void removeAll() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("two", "22");
		map.put("two", "2");

		assertTrue(map.removeAll("three").isEmpty());
		assertEquals(Arrays.asList("22", "2"), map.removeAll("two"));
		assertTrue(map.get("two").isEmpty());
		assertEquals(Arrays.asList("1"), map.get("one"));
	}

	@Test
	public void keySet() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");
		map.put("one", "11");
		map.put("two", "2");
		map.put("three", "3");

		assertSetEquals(map.keySet(), "one", "two", "three");
	}

	@Test
	public void values() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");
		map.put("one", "11");
		map.put("two", "2");
		map.put("three", "3");

		Collection<String> actual = map.values();
		assertEquals(5, actual.size());
		assertTrue(actual.contains("1"));
		assertTrue(actual.contains("111"));
		assertTrue(actual.contains("11"));
		assertTrue(actual.contains("2"));
		assertTrue(actual.contains("3"));
	}

	@Test
	public void isEmpty() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();

		assertTrue(map.isEmpty());
		map.put("one", "1");
		assertFalse(map.isEmpty());
		map.removeAll("one");
		assertTrue(map.isEmpty());
	}

	@Test
	public void size() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();

		assertEquals(0, map.size());

		map.put("one", "1");
		map.put("one", "111");
		map.put("one", "11");
		map.put("two", "2");
		map.put("three", "3");

		assertEquals(5, map.size());

		map.removeAll("one");

		assertEquals(2, map.size());
	}

	@Test
	public void copy_constructor() {
		ListMultimap<String, String> original = new ListMultimap<String, String>();
		original.put("one", "1");
		original.put("one", "111");
		original.put("one", "11");
		original.put("two", "2");
		original.put("three", "3");

		//make sure the copy was successful
		ListMultimap<String, String> copy = new ListMultimap<String, String>(original);
		assertEquals(Arrays.asList("1", "111", "11"), copy.get("one"));
		assertEquals(Arrays.asList("2"), copy.get("two"));
		assertEquals(Arrays.asList("3"), copy.get("three"));

		//make sure the objects aren't linked

		original.removeAll("one");
		assertEquals(Arrays.asList("1", "111", "11"), copy.get("one"));

		original.put("four", "4");
		assertTrue(copy.get("four").isEmpty());

		original.put("two", "22");
		assertEquals(Arrays.asList("2"), copy.get("two"));

		copy.removeAll("two");
		assertEquals(Arrays.asList("2", "22"), original.get("two"));

		copy.put("five", "5");
		assertTrue(original.get("five").isEmpty());

		copy.put("three", "33");
		assertEquals(Arrays.asList("3"), original.get("three"));
	}

	@Test
	public void clear() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		assertEquals(1, map.size());
		map.clear();
		assertEquals(0, map.size());
	}

	@Test
	public void sanitizeKey() {
		ListMultimap<String, String> map = new ListMultimap<String, String>() {
			@Override
			protected String sanitizeKey(String key) {
				return key.toLowerCase();
			}
		};
		map.put("one", "1");
		map.put("One", "111");
		map.putAll("oNe", Arrays.asList("1111"));

		assertEquals("1", map.first("onE"));

		List<String> expected = Arrays.asList("1", "111", "1111");
		assertEquals(expected, map.get("ONe"));

		assertTrue(map.remove("oNE", "1"));
		assertEquals(Arrays.asList("111", "1111"), map.removeAll("OnE"));
		assertTrue(map.isEmpty());
	}
}
