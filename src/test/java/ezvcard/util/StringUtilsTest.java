package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import ezvcard.util.StringUtils.JoinCallback;
import ezvcard.util.StringUtils.JoinMapCallback;

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
public class StringUtilsTest {
	@Test
	public void ltrim() {
		String actual, expected;

		actual = StringUtils.ltrim("One two three");
		expected = "One two three";
		assertSame(actual, expected); //a new string instance shouldn't be created

		actual = StringUtils.ltrim("\n \t One two three \t \n ");
		expected = "One two three \t \n ";
		assertEquals(actual, expected);

		actual = StringUtils.ltrim("\n \t \t \n ");
		expected = "";
		assertEquals(actual, expected);

		actual = StringUtils.ltrim("");
		expected = "";
		assertSame(actual, expected);

		assertNull(StringUtils.ltrim(null));
	}

	@Test
	public void rtrim() {
		String actual, expected;

		actual = StringUtils.rtrim("One two three");
		expected = "One two three";
		assertSame(actual, expected); //a new string instance shouldn't be created

		actual = StringUtils.rtrim("\n \t One two three \t \n ");
		expected = "\n \t One two three";
		assertEquals(actual, expected);

		actual = StringUtils.rtrim("\n \t \t \n ");
		expected = "";
		assertEquals(actual, expected);

		actual = StringUtils.rtrim("");
		expected = "";
		assertSame(actual, expected);

		assertNull(StringUtils.rtrim(null));
	}

	@Test
	public void repeat() {
		assertRepeat('*', -1, "");
		assertRepeat("abc", -1, "");
		assertRepeat('*', 0, "");
		assertRepeat("abc", 0, "");
		assertRepeat('*', 5, "*****");
		assertRepeat("abc", 5, "abcabcabcabcabc");
	}

	private static void assertRepeat(char c, int times, String expected) {
		String actual = StringUtils.repeat(c, times);
		assertEquals(expected, actual);
	}

	private static void assertRepeat(String str, int times, String expected) {
		String actual = StringUtils.repeat(str, times);
		assertEquals(expected, actual);
	}

	@Test
	public void join_multiple() {
		Collection<String> values = Arrays.asList("one", "two", "three");
		assertEquals("ONE,TWO,THREE", StringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_single() {
		Collection<String> values = Arrays.asList("one");
		assertEquals("ONE", StringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_empty() {
		Collection<String> values = Arrays.asList();
		assertEquals("", StringUtils.join(values, ",", new JoinCallback<String>() {
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
		assertEquals("false,1,two,null", StringUtils.join(values, ","));
	}

	@Test
	public void join_map() {
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(1, "one");
		map.put(2, "two");
		map.put(3, "three");
		assertEquals("1 - one,2 - two,3 - three", StringUtils.join(map, ",", new JoinMapCallback<Integer, String>() {
			public void handle(StringBuilder sb, Integer key, String value) {
				sb.append(key).append(" - ").append(value);
			}
		}));
	}
}
