package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.ClassRule;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardValue;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.io.scribe.VCardPropertyScribe.SemiStructuredIterator;
import ezvcard.io.scribe.VCardPropertyScribe.StructuredIterator;
import ezvcard.io.text.WriteContext;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.util.DefaultTimezoneRule;

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
public class VCardPropertyScribeTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final VCardPropertyScribeImpl scribe = new VCardPropertyScribeImpl();
	private final Sensei<TestProperty> sensei = new Sensei<TestProperty>(scribe);

	private final Date datetime = date("2013-06-11 14:43:02");

	@Test
	public void unescape() {
		String expected, actual;

		actual = VCardPropertyScribe.unescape("\\\\ \\, \\; \\n\\N \\\\\\,");
		expected = "\\ , ; " + NEWLINE + NEWLINE + " \\,";
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.unescape(null);
		expected = null;
		assertEquals(expected, actual);
	}

	@Test
	public void escape() {
		String actual, expected;

		actual = VCardPropertyScribe.escape("One; Two, Three\\ Four\n Five\r\n Six\r");
		expected = "One\\; Two\\, Three\\\\ Four\n Five\r\n Six\r";
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.escape(null);
		expected = null;
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_limit() {
		String str = "one,two,three,four";
		List<String> actual, expected;

		actual = VCardPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("one", "two", "three", "four");
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.splitter(',').limit(2).split(str);
		expected = Arrays.asList("one", "two,three,four");
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.splitter(',').limit(4).split(str);
		expected = Arrays.asList("one", "two", "three", "four");
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.splitter(',').limit(10).split(str);
		expected = Arrays.asList("one", "two", "three", "four");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_unescape() {
		String str = "one,two\\,\\;three";
		List<String> actual, expected;

		actual = VCardPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("one", "two\\,\\;three");
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.splitter(',').unescape(true).split(str);
		expected = Arrays.asList("one", "two,;three");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_nullEmpties() {
		String str = ",one,,two,";
		List<String> actual, expected;

		actual = VCardPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("", "one", "", "two", "");
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.splitter(',').nullEmpties(true).split(str);
		expected = Arrays.asList(null, "one", null, "two", null);
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_trim() {
		List<String> actual = VCardPropertyScribe.splitter(',').split("one , two");
		List<String> expected = Arrays.asList("one", "two");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_empty() {
		List<String> actual = VCardPropertyScribe.splitter(',').split("");
		List<String> expected = Arrays.asList("");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_all_settings() {
		String str = "one ,two\\,three,,four,five";
		List<String> actual, expected;

		actual = VCardPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("one", "two\\,three", "", "four", "five");
		assertEquals(expected, actual);

		actual = VCardPropertyScribe.splitter(',').unescape(true).limit(4).nullEmpties(true).split(str);
		expected = Arrays.asList("one", "two,three", null, "four,five");
		assertEquals(expected, actual);
	}

	@Test
	public void date_parse_utc() {
		String value = "20130611T134302Z";

		Date actual = VCardPropertyScribe.date(value);

		assertEquals(datetime, actual);
	}

	@Test
	public void date_parse_offset() {
		String value = "20130611T144302+0100";

		Date actual = VCardPropertyScribe.date(value);

		assertEquals(datetime, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void date_parse_local() {
		//date-times must have an offset or be in UTC
		String value = "20130611T144302";
		VCardPropertyScribe.date(value);
	}

	@Test
	public void date_write_datetime() {
		assert_date_write_datetime("20130611", false, false, false);
		assert_date_write_datetime("20130611T144302+0100", true, false, false);
		assert_date_write_datetime("2013-06-11", false, true, false);
		assert_date_write_datetime("20130611", false, false, true);
		assert_date_write_datetime("2013-06-11T14:43:02+01:00", true, true, false);
		assert_date_write_datetime("2013-06-11", false, true, true);
		assert_date_write_datetime("20130611T134302Z", true, false, true);
		assert_date_write_datetime("2013-06-11T13:43:02Z", true, true, true);
	}

	private void assert_date_write_datetime(String expected, boolean time, boolean extended, boolean utc) {
		String actual = VCardPropertyScribe.date(datetime).time(time).extended(extended).utc(utc).write();
		assertEquals(expected, actual);
	}

	@Test
	public void list_parse() {
		List<String> actual = VCardPropertyScribe.list("one ,, two,three\\,four");
		List<String> expected = Arrays.asList("one", "", "two", "three,four");
		assertEquals(expected, actual);
	}

	@Test
	public void list_parse_empty() {
		List<String> actual = VCardPropertyScribe.list("");
		List<String> expected = Arrays.asList();
		assertEquals(expected, actual);
	}

	@Test
	public void list_write() {
		String actual = VCardPropertyScribe.list("one", null, "two", "three,four");
		String expected = "one,,two,three\\,four";
		assertEquals(expected, actual);
	}

	@Test
	public void semistructured_parse() {
		String input = "one;two,three\\,four;;;five\\;six";

		SemiStructuredIterator it = VCardPropertyScribe.semistructured(input);
		assertEquals("one", it.next());
		assertEquals("two,three,four", it.next());
		assertEquals("", it.next());
		assertEquals("", it.next());
		assertEquals("five;six", it.next());
		assertEquals(null, it.next());
	}

	@Test
	public void semistructured_parse_limit() {
		String input = "one;two,three\\,four;;;five\\;six";

		SemiStructuredIterator it = VCardPropertyScribe.semistructured(input, 2);
		assertEquals("one", it.next());
		assertEquals("two,three,four;;;five;six", it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
	}

	@Test
	public void structured_parse_string() {
		String input = "one;two,three\\,four;;;five\\;six";

		//using "nextComponent()"
		StructuredIterator it = VCardPropertyScribe.structured(input);
		assertEquals(Arrays.asList("one"), it.nextComponent());
		assertEquals(Arrays.asList("two", "three,four"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList("five;six"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());

		//using "nextString()"
		it = VCardPropertyScribe.structured(input);
		assertEquals("one", it.nextString());
		assertEquals("two", it.nextString());
		assertEquals(null, it.nextString());
		assertEquals(null, it.nextString());
		assertEquals("five;six", it.nextString());
		assertEquals(null, it.nextString());
	}

	@Test
	public void structured_parse_jcard_value() {
		JCardValue input = JCardValue.structured("one", Arrays.asList("two", "three,four"), null, "", "five;six");

		//using "nextComponent()"
		StructuredIterator it = VCardPropertyScribe.structured(input);
		assertEquals(Arrays.asList("one"), it.nextComponent());
		assertEquals(Arrays.asList("two", "three,four"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList("five;six"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());

		//using "nextString()"
		it = VCardPropertyScribe.structured(input);
		assertEquals("one", it.nextString());
		assertEquals("two", it.nextString());
		assertEquals(null, it.nextString());
		assertEquals(null, it.nextString());
		assertEquals("five;six", it.nextString());
		assertEquals(null, it.nextString());
	}

	@Test
	public void structured_write() {
		String actual = VCardPropertyScribe.structured("one", 2, null, "four;five,six\\seven", Arrays.asList("eight"), Arrays.asList("nine", null, "ten;eleven,twelve\\thirteen"));
		assertEquals("one;2;;four\\;five\\,six\\\\seven;eight;nine,,ten\\;eleven\\,twelve\\\\thirteen", actual);
	}

	@Test
	public void structured_write_trailing_semicolons() {
		String actual = VCardPropertyScribe.structured(false, new Object[] { "one", "two", Arrays.asList(), "", null, "three", Arrays.asList(), "", null });
		assertEquals("one;two;;;;three", actual);

		actual = VCardPropertyScribe.structured(true, new Object[] { "one", "two", Arrays.asList(), "", null, "three", Arrays.asList(), "", null });
		assertEquals("one;two;;;;three;;;", actual);
	}

	@Test
	public void dataType_default() {
		TestProperty property = new TestProperty("value");
		sensei.assertDataType(property).run(VCardDataType.TEXT);
	}

	@Test
	public void dataType_custom() {
		VCardPropertyScribeImpl scribe = new VCardPropertyScribeImpl() {
			@Override
			protected VCardDataType _dataType(TestProperty property, VCardVersion version) {
				return VCardDataType.URI;
			}
		};
		TestProperty property = new TestProperty("value");
		new Sensei<TestProperty>(scribe).assertDataType(property).run(VCardDataType.URI);
	}

	@Test
	public void prepareParameters() {
		VCardPropertyScribeImpl m = new VCardPropertyScribeImpl() {
			@Override
			protected void _prepareParameters(TestProperty property, VCardParameters copy, VCardVersion version, VCard vcard) {
				copy.put("PARAM", "value");
			}
		};

		TestProperty property = new TestProperty("value");
		VCardParameters copy = m.prepareParameters(property, V4_0, new VCard());

		assertNotSame(property.getParameters(), copy);
		assertEquals("value", copy.first("PARAM"));
	}

	@Test
	public void writeText() {
		TestProperty property = new TestProperty("value");
		sensei.assertWriteText(property).run("value");
	}

	@Test
	public void parseText() {
		final VCardParameters params = new VCardParameters();
		sensei.assertParseText("value").dataType(VCardDataType.TEXT).warnings(1).params(params).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertEquals(VCardDataType.TEXT, property.parsedDataType);
				assertSame(params, property.getParameters());
			}
		});
	}

	@Test
	public void writeXml() {
		TestProperty prop = new TestProperty("value");
		sensei.assertWriteXml(prop).run("<text>value</text>");
	}

	@Test
	public void parseXml() {
		final VCardParameters params = new VCardParameters();

		//@formatter:off
		sensei.assertParseXml(
		"<ignore xmlns=\"http://example.com\">ignore-me</ignore>" +
		"<integer>value</integer>" +
		"<text>ignore-me</text>").params(params).warnings(1).run(new Check<TestProperty>(){
			public void check(TestProperty property){
				assertEquals("value", property.value);
				assertEquals(VCardDataType.INTEGER, property.parsedDataType);
				assertSame(params,property.getParameters());
			}
		});
		//@formatter:on
	}

	@Test
	public void parseXml_no_xcard_element() {
		sensei.assertParseXml("<one xmlns=\"http://example.com\">1</one><two xmlns=\"http://example.com\">2</two>").warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("12", property.value);
				assertNull(property.parsedDataType);
			}
		});
	}

	@Test
	public void parseXml_no_child_elements() {
		sensei.assertParseXml("value").warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertNull(property.parsedDataType);
			}
		});
	}

	@Test
	public void parseXml_unknown() {
		sensei.assertParseXml("<unknown>value</unknown>").warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertEquals(null, property.parsedDataType);
			}
		});
		sensei.assertParseXml("<one xmlns=\"http://example.com\">1</one><unknown>value</unknown>").warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertEquals(null, property.parsedDataType);
			}
		});
		sensei.assertParseXml("<unknown />").warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("", property.value);
				assertEquals(null, property.parsedDataType);
			}
		});
	}

	@Test
	public void getQName_default() {
		QName expected = new QName(V4_0.getXmlNamespace(), "test");
		QName actual = scribe.getQName();
		assertEquals(expected, actual);
	}

	@Test
	public void getQName_custom() {
		QName expected = new QName("http://example.com", "foo");
		VCardPropertyScribeImpl m = new VCardPropertyScribeImpl(expected);
		QName actual = m.getQName();
		assertEquals(expected, actual);
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<div>value<del>ignore</del>value</div>").warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("valuevalue", property.value);
				assertNull(property.parsedDataType);
			}
		});
	}

	@Test
	public void writeJson() {
		TestProperty property = new TestProperty("value");
		sensei.assertWriteJson(property).run("value");
	}

	@Test
	public void parseJson_single() {
		final VCardParameters params = new VCardParameters();
		JCardValue value = JCardValue.single("value");
		sensei.assertParseJson(value).dataType(VCardDataType.TEXT).params(params).warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertSame(params, property.getParameters());
			}
		});
	}

	@Test
	public void parseJson_list() {
		JCardValue value = JCardValue.multi("value1", "val,;ue2");
		sensei.assertParseJson(value).dataType(VCardDataType.TEXT).warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value1,val\\,\\;ue2", property.value);
			}
		});
	}

	@Test
	public void parseJson_structured() {
		JCardValue value = JCardValue.structured(null, "value1", "val,;ue2", Arrays.asList("value3", "value4"));
		sensei.assertParseJson(value).dataType(VCardDataType.TEXT).warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals(";value1;val\\,\\;ue2;value3,value4", property.value);
			}
		});
	}

	private class VCardPropertyScribeImpl extends VCardPropertyScribe<TestProperty> {
		private VCardPropertyScribeImpl() {
			super(TestProperty.class, "TEST");
		}

		private VCardPropertyScribeImpl(QName qname) {
			super(TestProperty.class, "TEST", qname);
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return VCardDataType.TEXT;
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			return property.value;
		}

		@Override
		protected TestProperty _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			warnings.add("parseText");
			return new TestProperty(value, dataType);
		}
	}

	private class TestProperty extends VCardProperty {
		public String value;
		public VCardDataType parsedDataType;

		public TestProperty(String value) {
			this(value, null);
		}

		public TestProperty(String value, VCardDataType parsedDataType) {
			this.value = value;
			this.parsedDataType = parsedDataType;
		}
	}
}
