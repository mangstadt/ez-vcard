package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ezvcard.util.VCardStringUtils.JoinCallback;
import ezvcard.util.VCardStringUtils.JoinMapCallback;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class VCardStringUtilsTest {
	@Test
	public void unescape() {
		String expected, actual;

		actual = VCardStringUtils.unescape("\\\\ \\, \\; \\n \\\\\\,");
		expected = "\\ , ; " + VCardStringUtils.NEWLINE + " \\,";
		assertEquals(expected, actual);

		assertNull(VCardStringUtils.unescape(null));
	}

	@Test
	public void escape() {
		String actual, expected;

		actual = VCardStringUtils.escape("One; Two, Three\\ Four\n Five\r\n Six\r");
		expected = "One\\; Two\\, Three\\\\ Four\n Five\r\n Six\r";
		assertEquals(expected, actual);

		assertNull(VCardStringUtils.escape(null));
	}

	@Test
	public void escapeNewlines() {
		String actual, expected;

		actual = VCardStringUtils.escapeNewlines("One; Two, Three\\ Four\n Five\r\n Six\r");
		expected = "One; Two, Three\\ Four\\n Five\\n Six\\n";
		assertEquals(expected, actual);

		assertNull(VCardStringUtils.escapeNewlines(null));
	}

	@Test
	public void containsNewlines() {
		assertTrue(VCardStringUtils.containsNewlines("One\nTwo"));
		assertTrue(VCardStringUtils.containsNewlines("One\rTwo"));
		assertTrue(VCardStringUtils.containsNewlines("One\r\nTwo"));
		assertFalse(VCardStringUtils.containsNewlines("One, Two"));
		assertFalse(VCardStringUtils.containsNewlines(null));
	}

	@Test
	public void ltrim() {
		String actual, expected;

		actual = VCardStringUtils.ltrim("\n \t One two three \t \n ");
		expected = "One two three \t \n ";
		assertEquals(actual, expected);

		actual = VCardStringUtils.ltrim("\n \t \t \n ");
		expected = "";
		assertEquals(actual, expected);

		assertNull(VCardStringUtils.ltrim(null));
	}

	@Test
	public void rtrim() {
		String actual, expected;

		actual = VCardStringUtils.rtrim("\n \t One two three \t \n ");
		expected = "\n \t One two three";
		assertEquals(actual, expected);

		actual = VCardStringUtils.rtrim("\n \t \t \n ");
		expected = "";
		assertEquals(actual, expected);

		assertNull(VCardStringUtils.rtrim(null));
	}

	@Test
	public void splitBy() {
		List<String> actual, expected;

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', false, false);
		expected = Arrays.asList("Doe", "John", "Joh\\,\\;nny", "", "Sr.,III");
		assertEquals(expected, actual);

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', true, false);
		expected = Arrays.asList("Doe", "John", "Joh\\,\\;nny", "Sr.,III");
		assertEquals(expected, actual);

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', false, true);
		expected = Arrays.asList("Doe", "John", "Joh,;nny", "", "Sr.,III");
		assertEquals(expected, actual);

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', true, true);
		expected = Arrays.asList("Doe", "John", "Joh,;nny", "Sr.,III");
		assertEquals(expected, actual);
	}

	@Test
	public void join_multiple() {
		Collection<String> values = Arrays.asList("one", "two", "three");
		assertEquals("ONE,TWO,THREE", VCardStringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_single() {
		Collection<String> values = Arrays.asList("one");
		assertEquals("ONE", VCardStringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_empty() {
		Collection<String> values = Arrays.asList();
		assertEquals("", VCardStringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_objects() {
		Collection<Object> values = new ArrayList<Object>();
		values.add(false);
		values.add(1);
		values.add("two");
		values.add(null);
		assertEquals("false,1,two,null", VCardStringUtils.join(values, ","));
	}

	@Test
	public void join_map() {
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(1, "one");
		map.put(2, "two");
		map.put(3, "three");
		assertEquals("1 - one,2 - two,3 - three", VCardStringUtils.join(map, ",", new JoinMapCallback<Integer, String>() {
			public void handle(StringBuilder sb, Integer key, String value) {
				sb.append(key).append(" - ").append(value);
			}
		}));
	}
}
