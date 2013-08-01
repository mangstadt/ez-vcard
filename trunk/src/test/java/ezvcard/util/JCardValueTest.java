package ezvcard.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

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
public class JCardValueTest {
	@Test
	public void single() {
		JCardValue value = JCardValue.single(JCardDataType.TEXT, "value");

		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue("value")
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void single_null() {
		JCardValue value = JCardValue.single(JCardDataType.TEXT, null);

		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue((Object)null)
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void getSingleValued() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue("value1"), new JsonValue("value2"));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals("value1", value.getSingleValued());
	}

	@Test
	public void getSingleValued_non_string() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue(false));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals("false", value.getSingleValued());
	}

	@Test
	public void getSingleValued_null() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue((Object) null));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(null, value.getSingleValued());
	}

	@Test
	public void getSingleValued_array() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue(Arrays.asList(new JsonValue("value1"), new JsonValue("value1"))));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals("value1", value.getSingleValued());
	}

	@Test
	public void getSingleValued_object() {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();
		object.put("a", new JsonValue("one"));
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue(object));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(null, value.getSingleValued());
	}

	@Test
	public void multi() {
		JCardValue value = JCardValue.multi(JCardDataType.TEXT, "value", 42, false, null);

		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue("value"),
			new JsonValue(42),
			new JsonValue(false),
			new JsonValue((Object)null)
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void getMultivalued() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue("value1"), new JsonValue(false), new JsonValue((Object) null));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(Arrays.asList("value1", "false", null), value.getMultivalued());
	}

	@Test
	public void getMultivalued_array() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue(Arrays.asList(new JsonValue("value1"), new JsonValue(false))));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(Arrays.asList(), value.getMultivalued());
	}

	@Test
	public void getMultivalued_object() {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();
		object.put("a", new JsonValue("one"));
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue(object));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(Arrays.asList(), value.getMultivalued());
	}

	@Test
	public void structured() {
		JCardValue value = JCardValue.structured(JCardDataType.TEXT, "value", 42, false, null);

		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue("value"), new JsonValue(42), new JsonValue(false), new JsonValue("")
			))
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getStructured() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue(Arrays.asList(new JsonValue("value1"), new JsonValue(false), new JsonValue((Object) null))));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(Arrays.asList(Arrays.asList("value1"), Arrays.asList("false"), Arrays.asList((String) null)), value.getStructured());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getStructured_single_value() {
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue("value1"));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(Arrays.asList(Arrays.asList("value1")), value.getStructured());
	}

	@Test
	public void getStructured_object() {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();
		object.put("a", new JsonValue("one"));
		JCardValue value = new JCardValue(JCardDataType.TEXT, new JsonValue(object));
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(Arrays.asList(), value.getStructured());
	}
}
