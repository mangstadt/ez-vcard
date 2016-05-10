package ezvcard.util;

import static ezvcard.util.TestUtils.assertCollectionContains;
import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static ezvcard.util.TestUtils.assertNotEqualsBothWays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

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
public class ListMultimapTest {
	@Test
	public void first() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "11");
		map.put("one", "111");

		assertEquals("1", map.first("one"));
		assertNull(map.first("two"));
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

		map.putAll("one", new ArrayList<String>());
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
		assertCollectionContains(map.keySet(), "one", "two");

		//make sure it remove an empty list from the map
		assertTrue(map.remove("one", "1"));
		assertCollectionContains(map.keySet(), "two");
	}

	@Test
	public void removeAll() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("two", "22");
		map.put("two", "2");
		assertEquals(3, map.size());

		assertTrue(map.removeAll("three").isEmpty());
		assertEquals(3, map.size());

		List<String> two = map.get("two");
		assertEquals(Arrays.asList("22", "2"), map.removeAll("two"));
		assertTrue(two.isEmpty());
		assertTrue(map.get("two").isEmpty());
		assertEquals(1, map.size());

		try {
			map.removeAll("one").add("2");
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
	}

	@Test
	public void keySet() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("one", "1");
		map.put("one", "111");
		map.put("one", "11");
		map.put("two", "2");
		map.put("three", "3");

		Set<String> actual = map.keySet();
		assertCollectionContains(actual, "one", "two", "three");
		try {
			actual.add("four");
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
		try {
			actual.remove("one");
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
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

	@Test
	public void asMap() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("bar", "1");

		Map<String, List<String>> m = map.asMap();
		assertEquals(2, m.size());
		assertEquals(Arrays.asList("1", "2"), m.get("foo"));
		assertEquals(Arrays.asList("1"), m.get("bar"));

		try {
			m.get("foo").add("3");
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}

		try {
			m.put("foo", new ArrayList<String>());
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
	}

	@Test
	public void iterator() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("bar", "1");

		Iterator<Map.Entry<String, List<String>>> it = map.iterator();

		Map.Entry<String, List<String>> entry = it.next();
		assertEquals("foo", entry.getKey());
		assertEquals(Arrays.asList("1", "2"), entry.getValue());
		try {
			entry.getValue().add("3");
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
		try {
			entry.setValue(new ArrayList<String>());
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
		try {
			it.remove();
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}

		entry = it.next();
		assertEquals("bar", entry.getKey());
		assertEquals(Arrays.asList("1"), entry.getValue());
		try {
			entry.getValue().add("3");
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
		try {
			entry.setValue(new ArrayList<String>());
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}
		try {
			it.remove();
			fail();
		} catch (UnsupportedOperationException e) {
			//expected
		}

		assertFalse(it.hasNext());
	}

	@Test
	public void equals() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		assertEqualsMethodEssentials(map);

		ListMultimap<String, String> one = new ListMultimap<String, String>();
		ListMultimap<String, String> two = new ListMultimap<String, String>();
		two.put("foo", "1");
		assertNotEqualsBothWays(one, two);

		one.put("foo", "1");
		assertEqualsAndHash(one, two);

	}

	@Test
	public void WrappedList_addAll() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");

		assertFalse(list.addAll(new ArrayList<String>()));

		assertTrue(list.addAll(Arrays.asList("1", "2")));
		assertEquals(Arrays.asList("1", "2"), list);
		assertEquals(Arrays.asList("1", "2"), map.get("foo"));

		assertTrue(list.addAll(Arrays.asList("3", "4")));
		assertEquals(Arrays.asList("1", "2", "3", "4"), list);
		assertEquals(Arrays.asList("1", "2", "3", "4"), map.get("foo"));
	}

	@Test
	public void WrappedList_addAll_index() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");

		assertFalse(list.addAll(0, new ArrayList<String>()));

		assertTrue(list.addAll(0, Arrays.asList("1", "2")));
		assertEquals(Arrays.asList("1", "2"), list);
		assertEquals(Arrays.asList("1", "2"), map.get("foo"));

		assertTrue(list.addAll(0, Arrays.asList("3", "4")));
		assertEquals(Arrays.asList("3", "4", "1", "2"), list);
		assertEquals(Arrays.asList("3", "4", "1", "2"), map.get("foo"));
	}

	@Test
	public void WrappedList_get() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");

		map.put("foo", "1");
		assertEquals("1", list.get(0));
	}

	@Test
	public void WrappedList_set() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");

		map.put("foo", "1");
		assertEquals("1", list.set(0, "2"));
		assertEquals(Arrays.asList("2"), list);
		assertEquals(Arrays.asList("2"), map.get("foo"));
	}

	@Test
	public void WrappedList_add() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");

		list.add("1");
		assertEquals(Arrays.asList("1"), list);
		assertEquals(Arrays.asList("1"), map.get("foo"));
	}

	@Test
	public void WrappedList_add_index() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");

		list.add(0, "1");
		assertEquals(Arrays.asList("1"), list);
		assertEquals(Arrays.asList("1"), map.get("foo"));
	}

	@Test
	public void WrappedList_remove() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");

		assertFalse(list.remove("2"));
		assertTrue(list.remove("1"));

		assertEquals(Arrays.asList(), list);
		assertEquals(Arrays.asList(), map.get("foo"));
		assertCollectionContains(map.keySet());
	}

	@Test
	public void WrappedList_removeAll() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("foo", "3");

		assertFalse(list.removeAll(Arrays.asList()));
		assertEquals(Arrays.asList("1", "2", "3"), list);
		assertCollectionContains(map.keySet(), "foo");

		assertFalse(list.removeAll(Arrays.asList("4")));
		assertEquals(Arrays.asList("1", "2", "3"), list);
		assertCollectionContains(map.keySet(), "foo");

		assertTrue(list.removeAll(Arrays.asList("1", "2")));
		assertEquals(Arrays.asList("3"), list);
		assertCollectionContains(map.keySet(), "foo");

		assertTrue(list.removeAll(Arrays.asList("3")));
		assertEquals(Arrays.asList(), list);
		assertCollectionContains(map.keySet());
	}

	@Test
	public void WrappedList_retainAll() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("foo", "3");

		assertFalse(list.retainAll(Arrays.asList("1", "2", "3")));
		assertEquals(Arrays.asList("1", "2", "3"), list);
		assertCollectionContains(map.keySet(), "foo");

		assertTrue(list.retainAll(Arrays.asList("1", "4")));
		assertEquals(Arrays.asList("1"), list);
		assertCollectionContains(map.keySet(), "foo");

		assertTrue(list.retainAll(Arrays.asList()));
		assertEquals(Arrays.asList(), list);
		assertCollectionContains(map.keySet());
	}

	@Test
	public void WrappedList_clear() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("foo", "3");

		list.clear();
		assertTrue(list.isEmpty());
		assertCollectionContains(map.keySet());

		list.clear();
		assertTrue(list.isEmpty());
		assertCollectionContains(map.keySet());
	}

	@Test
	public void WrappedList_indexOf() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "1");

		assertEquals(-1, list.indexOf("2"));
		assertEquals(0, list.indexOf("1"));
		assertEquals(0, map.get("foo").indexOf("1"));
	}

	@Test
	public void WrappedList_lastIndexOf() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "1");

		assertEquals(-1, list.lastIndexOf("2"));
		assertEquals(1, list.lastIndexOf("1"));
		assertEquals(1, map.get("foo").lastIndexOf("1"));
	}

	@Test
	public void WrappedList_subList() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.putAll("foo", Arrays.asList("1", "2", "3", "4"));

		List<String> list = map.get("foo");
		List<String> subList = list.subList(0, 2);

		assertEquals("1", subList.remove(0));
		assertEquals(Arrays.asList("2"), subList);
		assertEquals(Arrays.asList("2", "3", "4"), list);
		assertEquals(Arrays.asList("2", "3", "4"), map.get("foo"));

		subList.add(0, "5");
		assertEquals(Arrays.asList("5", "2"), subList);
		assertEquals(Arrays.asList("5", "2", "3", "4"), list);
		assertEquals(Arrays.asList("5", "2", "3", "4"), map.get("foo"));

		subList.clear();
		assertEquals(Arrays.asList(), subList);
		assertEquals(Arrays.asList("3", "4"), list);
		assertEquals(Arrays.asList("3", "4"), map.get("foo"));

		list = map.get("bar");
		subList = list.subList(0, 0);

		subList.add("1");
		assertEquals(Arrays.asList("1"), subList);
		assertEquals(Arrays.asList("1"), list);
		assertEquals(Arrays.asList("1"), map.get("bar"));
	}

	@Test
	public void WrappedList_subSubList() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		map.putAll("foo", Arrays.asList("1", "2", "3", "4"));

		List<String> list = map.get("foo");
		List<String> subList = list.subList(0, 2);
		List<String> subSubList = subList.subList(0, 1);

		assertEquals("1", subSubList.remove(0));
		assertEquals(Arrays.asList(), subSubList);
		assertEquals(Arrays.asList("2"), subList);
		assertEquals(Arrays.asList("2", "3", "4"), list);
		assertEquals(Arrays.asList("2", "3", "4"), map.get("foo"));

		subSubList.add(0, "5");
		assertEquals(Arrays.asList("5"), subSubList);
		assertEquals(Arrays.asList("5", "2"), subList);
		assertEquals(Arrays.asList("5", "2", "3", "4"), list);
		assertEquals(Arrays.asList("5", "2", "3", "4"), map.get("foo"));

		subSubList.clear();
		assertEquals(Arrays.asList(), subSubList);
		assertEquals(Arrays.asList("2"), subList);
		assertEquals(Arrays.asList("2", "3", "4"), list);
		assertEquals(Arrays.asList("2", "3", "4"), map.get("foo"));
	}

	@Test
	public void WrappedList_equals() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");

		assertEqualsMethodEssentials(list);
		assertTrue(list.equals(Arrays.asList("1")));
		assertFalse(list.equals(Arrays.asList("2")));
	}

	@Test
	public void WrappedList_hashCode() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("bar", "1");

		assertEquals(list.hashCode(), map.get("bar").hashCode());
	}

	@Test
	public void WrappedList_contains() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");

		assertFalse(list.contains("3"));
		assertTrue(list.contains("1"));
	}

	@Test
	public void WrappedList_containsAll() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("foo", "3");

		assertFalse(list.containsAll(Arrays.asList("4")));
		assertTrue(list.containsAll(Arrays.asList("1")));
	}

	@Test
	public void WrappedList_listIterator() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("foo", "3");
		map.put("foo", "4");

		ListIterator<String> it = list.listIterator();

		assertFalse(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(-1, it.previousIndex());
		assertEquals(0, it.nextIndex());
		assertEquals("1", it.next());
		assertEquals(Arrays.asList("1", "2", "3", "4"), list);
		assertEquals(Arrays.asList("1", "2", "3", "4"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(0, it.previousIndex());
		assertEquals(1, it.nextIndex());
		it.remove();
		assertEquals(Arrays.asList("2", "3", "4"), list);
		assertEquals(Arrays.asList("2", "3", "4"), map.get("foo"));

		assertFalse(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(-1, it.previousIndex());
		assertEquals(0, it.nextIndex());
		assertEquals("2", it.next());
		assertEquals(Arrays.asList("2", "3", "4"), list);
		assertEquals(Arrays.asList("2", "3", "4"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(0, it.previousIndex());
		assertEquals(1, it.nextIndex());
		it.set("5");
		assertEquals(Arrays.asList("5", "3", "4"), list);
		assertEquals(Arrays.asList("5", "3", "4"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(0, it.previousIndex());
		assertEquals(1, it.nextIndex());
		assertEquals("3", it.next());
		assertEquals(Arrays.asList("5", "3", "4"), list);
		assertEquals(Arrays.asList("5", "3", "4"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(1, it.previousIndex());
		assertEquals(2, it.nextIndex());
		assertEquals("3", it.previous());
		assertEquals(Arrays.asList("5", "3", "4"), list);
		assertEquals(Arrays.asList("5", "3", "4"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(0, it.previousIndex());
		assertEquals(1, it.nextIndex());
		assertEquals("3", it.next());
		assertEquals(Arrays.asList("5", "3", "4"), list);
		assertEquals(Arrays.asList("5", "3", "4"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(1, it.previousIndex());
		assertEquals(2, it.nextIndex());
		assertEquals("4", it.next());
		assertEquals(Arrays.asList("5", "3", "4"), list);
		assertEquals(Arrays.asList("5", "3", "4"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertFalse(it.hasNext());
		assertEquals(2, it.previousIndex());
		assertEquals(3, it.nextIndex());
		it.add("6");
		assertEquals(Arrays.asList("5", "3", "4", "6"), list);
		assertEquals(Arrays.asList("5", "3", "4", "6"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertFalse(it.hasNext());
		assertEquals(3, it.previousIndex());
		assertEquals(4, it.nextIndex());
	}

	@Test
	public void WrappedList_listIterator_empty_add() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");

		ListIterator<String> it = list.listIterator();

		assertFalse(it.hasPrevious());
		assertFalse(it.hasNext());
		assertEquals(-1, it.previousIndex());
		assertEquals(0, it.nextIndex());
		it.add("1");
		assertEquals(Arrays.asList("1"), list);
		assertEquals(Arrays.asList("1"), map.get("foo"));

		assertTrue(it.hasPrevious());
		assertFalse(it.hasNext());
		assertEquals(0, it.previousIndex());
		assertEquals(1, it.nextIndex());
	}

	@Test
	public void WrappedList_listIterator_index() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("foo", "3");
		map.put("foo", "4");

		ListIterator<String> it = list.listIterator(1);

		assertTrue(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(0, it.previousIndex());
		assertEquals(1, it.nextIndex());
		assertEquals("2", it.next());
	}

	@Test
	public void WrappedList_iterator() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> list = map.get("foo");
		map.put("foo", "1");
		map.put("foo", "2");
		map.put("foo", "3");
		map.put("foo", "4");

		Iterator<String> it = list.iterator();

		assertTrue(it.hasNext());
		assertEquals("1", it.next());
		assertTrue(it.hasNext());
		assertEquals("2", it.next());
		assertTrue(it.hasNext());
		assertEquals("3", it.next());
		assertTrue(it.hasNext());
		assertEquals("4", it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void WrappedList_two_instances() {
		ListMultimap<String, String> map = new ListMultimap<String, String>();
		List<String> one = map.get("foo");
		List<String> two = map.get("foo");

		assertTrue(one.isEmpty());
		assertTrue(two.isEmpty());
		assertCollectionContains(map.keySet());

		one.add("1");
		assertEquals(Arrays.asList("1"), one);
		assertEquals(Arrays.asList("1"), two);
		assertCollectionContains(map.keySet(), "foo");

		two.add("2");
		assertEquals(Arrays.asList("1", "2"), one);
		assertEquals(Arrays.asList("1", "2"), two);
		assertCollectionContains(map.keySet(), "foo");

		one.addAll(Arrays.asList("3", "4"));
		assertEquals(Arrays.asList("1", "2", "3", "4"), one);
		assertEquals(Arrays.asList("1", "2", "3", "4"), two);
		assertCollectionContains(map.keySet(), "foo");

		map.clear();
		assertTrue(one.isEmpty());
		assertTrue(two.isEmpty());
		assertCollectionContains(map.keySet());

		map.put("foo", "1");
		assertEquals(Arrays.asList("1"), one);
		assertEquals(Arrays.asList("1"), two);
		assertCollectionContains(map.keySet(), "foo");

		one.clear();
		assertTrue(one.isEmpty());
		assertTrue(two.isEmpty());
		assertCollectionContains(map.keySet());
	}
}
