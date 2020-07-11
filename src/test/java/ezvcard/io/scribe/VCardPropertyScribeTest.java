package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.date;
import static ezvcard.util.TestUtils.each;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Date;

import javax.xml.namespace.QName;

import org.junit.ClassRule;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.ParseContext;
import ezvcard.io.json.JCardValue;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.io.text.WriteContext;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.util.DefaultTimezoneRule;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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

	@Test
	public void date_parse_local() {
		String value = "20130611T144302";

		Date actual = VCardPropertyScribe.date(value);

		assertEquals(datetime, actual);
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
	public void handlePrefParam_to_4() {
		VCard vcard = new VCard();

		TestProperty two = new TestProperty("");
		two.getParameters().setType("HOME");
		vcard.addProperty(two);

		TestProperty one = new TestProperty("");
		one.getParameters().setType("PREF");
		vcard.addProperty(one);

		TestProperty three = new TestProperty("");
		vcard.addProperty(three);

		//no change
		for (VCardVersion version : each(V2_1, V3_0)) {
			for (TestProperty p : vcard.getProperties(TestProperty.class)) {
				VCardParameters copy = new VCardParameters(p.getParameters());
				VCardPropertyScribe.handlePrefParam(p, copy, version, vcard);
				assertEquals(p.getParameters(), copy);
			}
		}

		{
			VCardVersion version = V4_0;

			VCardParameters copy = new VCardParameters(one.getParameters());
			VCardPropertyScribe.handlePrefParam(one, copy, version, vcard);
			assertIntEquals(1, copy.getPref());
			assertNull(copy.getType());

			for (TestProperty p : each(two, three)) {
				copy = new VCardParameters(p.getParameters());
				VCardPropertyScribe.handlePrefParam(p, copy, version, vcard);
				assertEquals(p.getParameters(), copy);
			}
		}
	}

	@Test
	public void handlePrefParam_from_4() {
		VCard vcard = new VCard();

		TestProperty two = new TestProperty("");
		two.getParameters().setPref(2);
		vcard.addProperty(two);

		TestProperty nullPref = new TestProperty("");
		vcard.addProperty(nullPref);

		TestProperty invalidPref = new TestProperty("");
		invalidPref.getParameters().replace("PREF", "invalid");
		vcard.addProperty(invalidPref);

		TestProperty one = new TestProperty("");
		one.getParameters().setPref(1);
		vcard.addProperty(one);

		TestProperty three = new TestProperty("");
		three.getParameters().setPref(3);
		vcard.addProperty(three);

		//no change
		{
			VCardVersion version = V4_0;
			for (TestProperty p : vcard.getProperties(TestProperty.class)) {
				VCardParameters copy = new VCardParameters(p.getParameters());
				VCardPropertyScribe.handlePrefParam(p, copy, version, vcard);
				assertEquals(p.getParameters(), copy);
			}
		}

		for (VCardVersion version : each(V2_1, V3_0)) {
			VCardParameters copy = new VCardParameters(one.getParameters());
			VCardPropertyScribe.handlePrefParam(one, copy, version, vcard);
			assertEquals("pref", copy.getType());
			assertNull(copy.getPref());

			for (TestProperty p : each(nullPref, invalidPref, two, three)) {
				copy = new VCardParameters(p.getParameters());
				VCardPropertyScribe.handlePrefParam(p, copy, version, vcard);
				assertNull(copy.getType());
				assertNull(copy.getPref());
			}
		}
	}

	@Test
	public void writeText() {
		TestProperty property = new TestProperty("value");
		sensei.assertWriteText(property).run("value");
	}

	@Test
	public void parseText() {
		final VCardParameters params = new VCardParameters();
		sensei.assertParseText("value").dataType(VCardDataType.TEXT).warnings((Integer) null).params(params).run(new Check<TestProperty>() {
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
		"<text>ignore-me</text>").params(params).warnings((Integer)null).run(new Check<TestProperty>(){
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
		sensei.assertParseXml("<one xmlns=\"http://example.com\">1</one><two xmlns=\"http://example.com\">2</two>").warnings((Integer) null).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("12", property.value);
				assertNull(property.parsedDataType);
			}
		});
	}

	@Test
	public void parseXml_no_child_elements() {
		sensei.assertParseXml("value").warnings((Integer) null).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertNull(property.parsedDataType);
			}
		});
	}

	@Test
	public void parseXml_unknown() {
		sensei.assertParseXml("<unknown>value</unknown>").warnings((Integer) null).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertEquals(null, property.parsedDataType);
			}
		});
		sensei.assertParseXml("<one xmlns=\"http://example.com\">1</one><unknown>value</unknown>").warnings((Integer) null).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertEquals(null, property.parsedDataType);
			}
		});
		sensei.assertParseXml("<unknown />").warnings((Integer) null).run(new Check<TestProperty>() {
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
		sensei.assertParseHtml("<div>value<del>ignore</del>value</div>").warnings((Integer) null).run(new Check<TestProperty>() {
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
		sensei.assertParseJson(value).dataType(VCardDataType.TEXT).params(params).warnings((Integer) null).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value", property.value);
				assertSame(params, property.getParameters());
			}
		});
	}

	@Test
	public void parseJson_list() {
		JCardValue value = JCardValue.multi("value1", "val,;ue2");
		sensei.assertParseJson(value).dataType(VCardDataType.TEXT).warnings((Integer) null).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertEquals("value1,val\\,\\;ue2", property.value);
			}
		});
	}

	@Test
	public void parseJson_structured() {
		JCardValue value = JCardValue.structured(null, "value1", "val,;ue2", Arrays.asList("value3", "value4"));
		sensei.assertParseJson(value).dataType(VCardDataType.TEXT).warnings((Integer) null).run(new Check<TestProperty>() {
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
		protected TestProperty _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
			context.addWarning("parseText");
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
