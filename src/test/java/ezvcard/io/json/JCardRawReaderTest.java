package ezvcard.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonToken;

import ezvcard.VCardDataType;
import ezvcard.io.json.JCardRawReader.JCardDataStreamListener;
import ezvcard.parameter.VCardParameters;

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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop1", new VCardParameters(), VCardDataType.TEXT, JCardValue.single("one"));
		verify(listener).readProperty(null, "prop2", new VCardParameters(), VCardDataType.INTEGER, JCardValue.single(2L));
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);
		reader.readNext(listener);

		verify(listener, times(2)).beginVCard();
		verify(listener).readProperty(null, "prop1", new VCardParameters(), VCardDataType.TEXT, JCardValue.single("one"));
		verify(listener).readProperty(null, "prop2", new VCardParameters(), VCardDataType.INTEGER, JCardValue.single(2L));
		verify(listener).readProperty(null, "prop1", new VCardParameters(), VCardDataType.TEXT, JCardValue.single("three"));
		verify(listener).readProperty(null, "prop2", new VCardParameters(), VCardDataType.INTEGER, JCardValue.single(4L));
		verifyNoMoreInteractions(listener);
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
					"]" +
				"]" +
		"}";
		//@formatter:on

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.single("value"));
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		try {
			reader.readNext(listener);
			fail("JCardParseException expected.");
		} catch (JCardParseException e) {
			assertEquals(JsonToken.VALUE_STRING, e.getExpectedToken());
			assertEquals(JsonToken.START_ARRAY, e.getActualToken());
		}
		reader.readNext(listener);

		verify(listener, times(2)).beginVCard();
		verify(listener, times(2)).readProperty(null, "prop1", new VCardParameters(), VCardDataType.TEXT, JCardValue.single("one"));
		verify(listener).readProperty(null, "prop2", new VCardParameters(), VCardDataType.INTEGER, JCardValue.single(2L));
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.structured("one", Arrays.asList("two", "three"), "four"));
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.structured());
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.multi("one", "two", "three"));
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.multi("one"));
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.multi(false, true, 1.1, 1L, null, "text"));
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		List<JsonValue> expectedValues = new ArrayList<JsonValue>();
		Map<String, JsonValue> m = new HashMap<String, JsonValue>();
		{
			//@formatter:off
			m.put("a", new JsonValue(Arrays.asList(
				new JsonValue("one"),
				new JsonValue("two")
			)));
			
			Map<String, JsonValue> m2 = new HashMap<String, JsonValue>();
			{
				m2.put("c", new JsonValue(Arrays.asList(
					new JsonValue("three")
				)));
				m2.put("d", new JsonValue(new HashMap<String, JsonValue>()));
			}
			m.put("b", new JsonValue(m2));
			//@formatter:on
		}
		expectedValues.add(new JsonValue(m));

		expectedValues.add(new JsonValue("four"));

		JCardValue expected = new JCardValue(expectedValues);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.TEXT, expected);
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", new VCardParameters(), null, JCardValue.single("value"));
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", new VCardParameters(), VCardDataType.get("foo"), JCardValue.single("value"));
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void empty() throws Throwable {
		//@formatter:off
		String json =
		"";
		//@formatter:on

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener, never()).beginVCard();
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void no_vcard() throws Throwable {
		//@formatter:off
		String json =
		"{" +
			"\"foo\": \"bar\"" +
		"}";
		//@formatter:on

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener, never()).beginVCard();
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		VCardParameters expected = new VCardParameters();
		expected.put("a", "one");
		expected.put("b", "two");
		expected.put("c", "three");
		expected.put("c", "four");

		verify(listener).beginVCard();
		verify(listener).readProperty(null, "prop", expected, VCardDataType.TEXT, JCardValue.single("value"));
		verifyNoMoreInteractions(listener);
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

		JCardRawReader reader = createReader(json);
		JCardDataStreamListener listener = mock(JCardDataStreamListener.class);

		reader.readNext(listener);

		verify(listener).beginVCard();
		verify(listener).readProperty("one", "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.single("value"));
		verifyNoMoreInteractions(listener);
	}

	private static JCardRawReader createReader(String json) {
		return new JCardRawReader(new StringReader(json));
	}
}
