package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonToken;

import ezvcard.VCardSubTypes;
import ezvcard.io.JCardRawReader.JCardDataStreamListener;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.JsonValue;

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
public class JCardRawReaderTest {
	@Test
	public void basic() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop1\", {}, \"text\", \"one\"]," +
				"[\"prop2\", {}, \"integer\", 2]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop1", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("one", value.getSingleValued());
					break;
				case 2:
					assertNull(group);
					assertEquals("prop2", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.INTEGER, value.getDataType());
					assertEquals("2", value.getSingleValued());
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(2, listener.calledReadProperty);
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		String json =
		"[" +
			"[\"vcard\"," +
				"[" +
					"[\"prop1\", {}, \"text\", \"one\"]," +
					"[\"prop2\", {}, \"integer\", 2]" +
				"]" +
			"]," +
			"[\"vcard\"," +
				"[" +
					"[\"prop1\", {}, \"text\", \"three\"]," +
					"[\"prop2\", {}, \"integer\", 4]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop1", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("one", value.getSingleValued());
					break;
				case 2:
					assertNull(group);
					assertEquals("prop2", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.INTEGER, value.getDataType());
					assertEquals("2", value.getSingleValued());
					break;
				}
			}
		};

		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(2, listener.calledReadProperty);

		//it should continue to read the rest of the vCards

		listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop1", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("three", value.getSingleValued());
					break;
				case 2:
					assertNull(group);
					assertEquals("prop2", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.INTEGER, value.getDataType());
					assertEquals("4", value.getSingleValued());
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(2, listener.calledReadProperty);
	}

	@Test
	public void ignore_other_json() throws Throwable {
		//@formatter:off
		String json =
		"{" +
			"\"website\": \"example.com\"," +
			"\"vcard\":" +
				"[\"vcard\"," +
					"[" +
						"[\"prop\", {}, \"text\", \"value\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
		"}";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("value", value.getSingleValued());
					break;
				}
			}
		};

		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);
	}

	@Test
	public void bad_snytax() throws Throwable {
		//@formatter:off
		String json =
		"[" +
			"[\"vcard\"," +
				"[" +
					"[\"prop1\", {}, \"text\", \"one\"]," +
					"[\"prop2\", {}, []]" +
				"]" +
			"]," +
			"[\"vcard\"," +
				"[" +
					"[\"prop1\", {}, \"text\", \"one\"]," +
					"[\"prop2\", {}, \"integer\", 2]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop1", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("one", value.getSingleValued());
					break;
				}
			}
		};

		try {
			reader.readNext(listener);
		} catch (JCardParseException e) {
			assertEquals(JsonToken.VALUE_STRING, e.getExpectedToken());
			assertEquals(JsonToken.START_ARRAY, e.getActualToken());
		}

		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);

		//it should continue to read the rest of the vCards

		listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop1", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("one", value.getSingleValued());
					break;
				case 2:
					assertNull(group);
					assertEquals("prop2", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.INTEGER, value.getDataType());
					assertEquals("2", value.getSingleValued());
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(2, listener.calledReadProperty);
	}

	@Test
	public void empty_properties_array() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				fail("Should not be called.");
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(0, listener.calledReadProperty);
	}

	@Test
	public void structured_value() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", {}, \"text\", [\"one\", [\"two\", \"three\"], \"four\"] ]," +
				"[\"prop\", {}, \"text\", [] ]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@SuppressWarnings("unchecked")
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals(Arrays.asList(Arrays.asList("one"), Arrays.asList("two", "three"), Arrays.asList("four")), value.getStructured());
					break;
				case 2:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals(Arrays.asList(), value.getStructured());
					break;
				}
			}
		};
		reader.readNext(listener);

		assertEquals(1, listener.calledBeginVCard);
		assertEquals(2, listener.calledReadProperty);
	}

	@Test
	public void multi_value() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", {}, \"text\", \"one\", \"two\", \"three\" ]," +
				"[\"prop\", {}, \"text\", \"one\" ]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals(Arrays.asList("one", "two", "three"), value.getMultivalued());
					break;
				case 2:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals(Arrays.asList("one"), value.getMultivalued());
					break;
				}
			}
		};
		reader.readNext(listener);

		assertEquals(1, listener.calledBeginVCard);
		assertEquals(2, listener.calledReadProperty);
	}

	@Test
	public void different_data_types() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", {}, \"text\", false, true, 1.1, 1, null, \"text\" ]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());

					List<JsonValue> expected = new ArrayList<JsonValue>();
					expected.add(new JsonValue(false));
					expected.add(new JsonValue(true));
					expected.add(new JsonValue(1.1));
					expected.add(new JsonValue(1L));
					expected.add(new JsonValue((Object) null));
					expected.add(new JsonValue("text"));
					assertEquals(expected, value.getValues());
					break;
				}
			}
		};
		reader.readNext(listener);

		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);
	}

	@Test
	public void complex_value() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", {}, \"text\", {" +
					"\"a\":[\"one\",\"two\"]," +
					"\"b\":{" +
						"\"c\":[\"three\"]," +
						"\"d\":{}" +
					"}" +
				"}, \"four\" ]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());

					List<JsonValue> expected = new ArrayList<JsonValue>();
					Map<String, JsonValue> m = new HashMap<String, JsonValue>();
					m.put("a", new JsonValue(Arrays.asList(new JsonValue("one"), new JsonValue("two"))));
					Map<String, JsonValue> m2 = new HashMap<String, JsonValue>();
					m2.put("c", new JsonValue(Arrays.asList(new JsonValue("three"))));
					m2.put("d", new JsonValue(new HashMap<String, JsonValue>()));
					m.put("b", new JsonValue(m2));
					expected.add(new JsonValue(m));
					expected.add(new JsonValue("four"));

					assertEquals(expected, value.getValues());
					break;
				}
			}
		};
		reader.readNext(listener);

		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);
	}

	@Test
	public void data_type_unknown() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", {}, \"unknown\", \"value\"]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.UNKNOWN, value.getDataType()); //TODO change so null == unknown
					assertEquals("value", value.getSingleValued());
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);
	}

	@Test
	public void data_type_unrecognized() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", {}, \"foo\", \"value\"]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertTrue(JCardDataType.get("foo") == value.getDataType());
					assertEquals("value", value.getSingleValued());
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);
	}

	@Test
	public void empty() throws Throwable {
		//@formatter:off
		String json =
		"";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				//empty
			}
		};
		reader.readNext(listener);
		assertEquals(0, listener.calledBeginVCard);
		assertEquals(0, listener.calledReadProperty);
	}

	@Test
	public void no_vcard() throws Throwable {
		//@formatter:off
		String json =
		"{" +
			"\"foo\": \"bar\"" +
		"}";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				//empty
			}
		};
		reader.readNext(listener);
		assertEquals(0, listener.calledBeginVCard);
		assertEquals(0, listener.calledReadProperty);
	}

	@Test
	public void parameters() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", { \"a\": \"one\", \"b\": [\"two\"], \"c\": [\"three\", \"four\"] }, \"text\", \"value\"]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertNull(group);
					assertEquals("prop", name);
					assertEquals(4, parameters.size());
					assertEquals(Arrays.asList("one"), parameters.get("a"));
					assertEquals(Arrays.asList("two"), parameters.get("b"));
					assertEquals(Arrays.asList("three", "four"), parameters.get("c"));
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("value", value.getSingleValued());
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);
	}

	@Test
	public void group() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"prop\", { \"group\": \"one\" }, \"text\", \"value\"]" +
			"]" +
		"]";
		//@formatter:on

		JCardRawReader reader = new JCardRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals("one", group);
					assertEquals("prop", name);
					assertTrue(parameters.isEmpty());
					assertEquals(JCardDataType.TEXT, value.getDataType());
					assertEquals("value", value.getSingleValued());
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledBeginVCard);
		assertEquals(1, listener.calledReadProperty);
	}

	private abstract class TestListener implements JCardDataStreamListener {
		protected int calledReadProperty = 0, calledBeginVCard = 0;

		public final void beginVCard() {
			calledBeginVCard++;
		}

		public final void readProperty(String group, String name, VCardSubTypes parameters, JCardValue value) {
			calledReadProperty++;
			readProperty_(group, name, parameters, value);
		}

		protected abstract void readProperty_(String group, String name, VCardSubTypes parameters, JCardValue value);
	}
}
