package ezvcard.io.json;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ezvcard.VCardDataType;
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
@SuppressWarnings("resource")
public class JCardRawWriterTest {
	@Test
	public void write_multiple() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, true);

		writer.writeStartVCard();
		writer.writeProperty("prop", VCardDataType.TEXT, JCardValue.single("value"));
		writer.writeEndVCard();
		writer.writeStartVCard();
		writer.writeProperty("prop", VCardDataType.TEXT, JCardValue.single("value"));
		writer.writeEndVCard();
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[" +
			"[\"vcard\"," +
				"[" +
					"[\"prop\",{},\"text\",\"value\"]" +
				"]" +
			"]," +
			"[\"vcard\"," +
				"[" +
					"[\"prop\",{},\"text\",\"value\"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void writeProperty() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		writer.writeProperty("prop", VCardDataType.TEXT, JCardValue.single("value\nvalue"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"prop\",{},\"text\",\"value\\nvalue\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void writeProperty_null_value() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		writer.writeProperty("prop", VCardDataType.TEXT, JCardValue.single(null));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"prop\",{},\"text\",null]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void parameters() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		VCardParameters parameters = new VCardParameters();
		parameters.put("a", "value1");
		parameters.put("b", "value2");
		parameters.put("b", "value3");
		writer.writeProperty(null, "prop", parameters, VCardDataType.TEXT, JCardValue.single("value"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"prop\",{\"a\":\"value1\",\"b\":[\"value2\",\"value3\"]},\"text\",\"value\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void group() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		writer.writeProperty("one", "prop", new VCardParameters(), VCardDataType.TEXT, JCardValue.single("value"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"prop\",{\"group\":\"one\"},\"text\",\"value\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void complex_value() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		List<JsonValue> jsonValues = new ArrayList<JsonValue>();
		Map<String, JsonValue> m = new LinkedHashMap<String, JsonValue>();
		m.put("a", new JsonValue(Arrays.asList(new JsonValue("one"), new JsonValue("two"))));
		Map<String, JsonValue> m2 = new LinkedHashMap<String, JsonValue>();
		m2.put("c", new JsonValue(Arrays.asList(new JsonValue("three"))));
		m2.put("d", new JsonValue(new LinkedHashMap<String, JsonValue>()));
		m.put("b", new JsonValue(m2));
		jsonValues.add(new JsonValue(m));
		jsonValues.add(new JsonValue("four"));
		writer.writeProperty("prop", VCardDataType.TEXT, new JCardValue(jsonValues));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"prop\",{},\"text\",{" +
					"\"a\":[\"one\",\"two\"]," +
					"\"b\":{" +
						"\"c\":[\"three\"]," +
						"\"d\":{}" +
					"}" +
				"},\"four\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void data_type_unknown() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		writer.writeProperty("prop", null, JCardValue.single("value"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"prop\",{},\"unknown\",\"value\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void different_value_types() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		writer.writeProperty("prop", VCardDataType.TEXT, JCardValue.multi(false, true, 1.1, 1, null, "text"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"prop\",{},\"text\",false,true,1.1,1,null,\"text\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void empty() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.close();

		String actual = sw.toString();
		String expected = "";
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalStateException.class)
	public void write_property_before_starting_vcard() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeProperty("prop", VCardDataType.TEXT, JCardValue.single("value"));
	}

	@Test(expected = IllegalStateException.class)
	public void write_end_vcard_before_starting_vcard() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeEndVCard();
	}

	@Test(expected = IllegalStateException.class)
	public void write_property_after_ending_vcard() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		writer.writeEndVCard();
		writer.writeProperty("prop", VCardDataType.TEXT, JCardValue.single("value"));
	}

	@Test
	public void write_empty_vcard() throws Exception {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, false);

		writer.writeStartVCard();
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void writeStartVCard_multiple_calls() throws Exception {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, true);

		writer.writeStartVCard();
		writer.writeStartVCard();
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[" +
			"[\"vcard\"," +
				"[" +
				"]" +
			"]," + 
			"[\"vcard\"," +
				"[" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void indent() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardRawWriter writer = new JCardRawWriter(sw, true);
		writer.setPrettyPrint(true);

		//@formatter:off
		writer.writeStartVCard();
			writer.writeProperty("prop1", VCardDataType.TEXT, JCardValue.single("value1"));
			writer.writeProperty("prop2", VCardDataType.TEXT, JCardValue.single("value2"));
		writer.writeEndVCard();
		writer.writeStartVCard();
			writer.writeProperty("prop3", VCardDataType.TEXT, JCardValue.single("value3"));
		writer.writeEndVCard();
		//@formatter:on
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"  [" + NEWLINE +
		"    \"vcard\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"prop1\", { }, \"text\", \"value1\" ]," + NEWLINE +
		"      [ \"prop2\", { }, \"text\", \"value2\" ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]," + NEWLINE +
		"  [" + NEWLINE +
		"    \"vcard\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"prop3\", { }, \"text\", \"value3\" ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]" + NEWLINE +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}
}