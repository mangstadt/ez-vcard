package ezvcard.types;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardValue;
import ezvcard.util.TestUtils.Tests;
import ezvcard.util.XCardElement;

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
public class TimezoneTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final VCard vcard = new VCard();

	private final TimeZone newYork = TimeZone.getTimeZone("America/New_York");
	private final Integer hourOffset = -5;
	private final Integer minuteOffset = 30;
	private final String offsetStrExtended = "-05:30";
	private final String offsetStrBasic = "-0530";
	private final String timezoneIdStr = "America/New_York";
	private final String textStr = "some text";
	private final String offsetWithText = offsetStrExtended + ";EDT;" + timezoneIdStr;
	private final TimezoneType offset = new TimezoneType(hourOffset, minuteOffset);
	private final TimezoneType timezoneId = new TimezoneType(timezoneIdStr);
	private final TimezoneType text = new TimezoneType(textStr);
	private final TimezoneType offsetAndTimezoneId = new TimezoneType(hourOffset, minuteOffset, timezoneIdStr);
	private final TimezoneType empty = new TimezoneType();
	private TimezoneType t;

	@Before
	public void before() {
		t = new TimezoneType();
		subTypes.clear();
		warnings.clear();
	}

	@Test
	public void validate() {
		assertWarnings(2, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		assertWarnings(0, offset.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, offset.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, offset.validate(VCardVersion.V4_0, vcard));

		assertWarnings(1, timezoneId.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, timezoneId.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, timezoneId.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void constructor_timezone() {
		TimezoneType tz = new TimezoneType(newYork);

		assertEquals(newYork.getID(), tz.getText());
		if (newYork.inDaylightTime(new Date())) {
			assertIntEquals(-4, tz.getHourOffset());
		} else {
			assertIntEquals(-5, tz.getHourOffset());
		}
		assertIntEquals(0, tz.getMinuteOffset());
	}

	@Test
	public void setOffset_null_hour() {
		t.setOffset(null, 30);
		assertIntEquals(0, t.getHourOffset());
		assertIntEquals(30, t.getMinuteOffset());
	}

	@Test
	public void setOffset_null_minute() {
		t.setOffset(-5, null);
		assertEquals(Integer.valueOf(-5), t.getHourOffset());
		assertIntEquals(0, t.getMinuteOffset());
	}

	@Test
	public void setOffset_null_hour_and_minute() {
		t.setOffset(null, null);
		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
	}

	@Test
	public void toTimeZone_offset() {
		TimezoneType t = new TimezoneType(-5, 30);
		TimeZone actual = t.toTimeZone();
		assertEquals(-(5 * 1000 * 60 * 60 + 30 * 1000 * 60), actual.getRawOffset());
	}

	@Test
	public void toTimeZone_timezone_id() {
		TimezoneType t = new TimezoneType(newYork.getID());
		TimeZone actual = t.toTimeZone();
		assertEquals(newYork, actual);
	}

	@Test
	public void toTimeZone_text() {
		TimezoneType t = new TimezoneType("text");
		TimeZone actual = t.toTimeZone();
		assertNull(actual);
	}

	@Test
	public void toTimeZone_text_and_offset() {
		TimezoneType t = new TimezoneType(-5, 30, "text");
		TimeZone actual = t.toTimeZone();
		assertEquals(-(5 * 1000 * 60 * 60 + 30 * 1000 * 60), actual.getRawOffset());
		assertEquals("text", actual.getID());
	}

	@Test
	public void marshalSubTypes() {
		Tests tests = new Tests();
		tests.add(VCardVersion.V2_1, offset, null);
		tests.add(VCardVersion.V3_0, offset, null);
		tests.add(VCardVersion.V4_0, offset, VCardDataType.UTC_OFFSET);

		tests.add(VCardVersion.V2_1, timezoneId, null);
		tests.add(VCardVersion.V3_0, timezoneId, VCardDataType.TEXT);
		tests.add(VCardVersion.V4_0, timezoneId, null);

		tests.add(VCardVersion.V2_1, text, null);
		tests.add(VCardVersion.V3_0, text, VCardDataType.TEXT);
		tests.add(VCardVersion.V4_0, text, null);

		tests.add(VCardVersion.V2_1, offsetAndTimezoneId, null);
		tests.add(VCardVersion.V3_0, offsetAndTimezoneId, null);
		tests.add(VCardVersion.V4_0, offsetAndTimezoneId, null);

		int i = 0;
		for (Object[] test : tests) {
			VCardVersion version = (VCardVersion) test[0];
			TimezoneType type = (TimezoneType) test[1];
			VCardDataType expectedDataType = (VCardDataType) test[2];

			VCardSubTypes subTypes = type.marshalSubTypes(version, compatibilityMode, vcard);
			assertEquals("Test " + i, expectedDataType, subTypes.getValue());
			i++;
		}
	}

	@Test
	public void marshalText() {
		Tests tests = new Tests();
		tests.add(VCardVersion.V2_1, offset, offsetStrBasic);
		tests.add(VCardVersion.V3_0, offset, offsetStrExtended);
		tests.add(VCardVersion.V4_0, offset, offsetStrBasic);

		tests.add(VCardVersion.V2_1, timezoneId, newYork.inDaylightTime(new Date()) ? "-0400" : "-0500");
		tests.add(VCardVersion.V3_0, timezoneId, timezoneIdStr);
		tests.add(VCardVersion.V4_0, timezoneId, timezoneIdStr);

		tests.add(VCardVersion.V2_1, text, "");
		tests.add(VCardVersion.V3_0, text, textStr);
		tests.add(VCardVersion.V4_0, text, textStr);

		tests.add(VCardVersion.V2_1, offsetAndTimezoneId, offsetStrBasic);
		tests.add(VCardVersion.V3_0, offsetAndTimezoneId, offsetStrExtended);
		tests.add(VCardVersion.V4_0, offsetAndTimezoneId, timezoneIdStr);

		tests.add(VCardVersion.V2_1, empty, "");
		tests.add(VCardVersion.V3_0, empty, "");
		tests.add(VCardVersion.V4_0, empty, "");

		int i = 0;
		for (Object[] test : tests) {
			VCardVersion version = (VCardVersion) test[0];
			TimezoneType type = (TimezoneType) test[1];
			String expected = (String) test[2];

			String actual = type.marshalText(version, compatibilityMode);
			assertEquals("Test " + i, expected, actual);
			i++;
		}
	}

	/////////////////////////////////////////////////

	@Test
	public void marshalXml_offset() {
		assertMarshalXml(offset, "<utc-offset>" + offsetStrBasic + "</utc-offset>");
	}

	@Test
	public void marshalXml_text() {
		assertMarshalXml(timezoneId, "<text>" + timezoneIdStr + "</text>");
	}

	@Test
	public void marshalXml_offset_and_text() {
		assertMarshalXml(offsetAndTimezoneId, "<text>" + timezoneIdStr + "</text>");
	}

	@Test
	public void marshalXml_no_offset_or_text() {
		assertMarshalXml(empty, "<text/>");
	}

	/////////////////////////////////////////////////

	@Test
	public void marshalJson_offset() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = offset.marshalJson(version);

		assertJCardValue(VCardDataType.UTC_OFFSET, offsetStrExtended, value);
	}

	@Test
	public void marshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = timezoneId.marshalJson(version);

		assertJCardValue(VCardDataType.TEXT, timezoneIdStr, value);
	}

	@Test
	public void marshalJson_offset_and_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = offsetAndTimezoneId.marshalJson(version);

		assertJCardValue(VCardDataType.TEXT, timezoneIdStr, value);
	}

	@Test
	public void marshalJson_no_offset_or_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = empty.marshalJson(version);

		assertJCardValue(VCardDataType.TEXT, "", value);
	}

	/////////////////////////////////////////////////

	@Test
	public void unmarshalText_2_1_offset() {
		VCardVersion version = VCardVersion.V2_1;
		t.unmarshalText(subTypes, offsetStrExtended, version, warnings, compatibilityMode);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_2_1_invalid_offset() {
		VCardVersion version = VCardVersion.V2_1;
		t.unmarshalText(subTypes, "invalid", version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_2_1_text() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setValue(VCardDataType.TEXT);
		t.unmarshalText(subTypes, offsetWithText, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalText_3_0_offset() {
		VCardVersion version = VCardVersion.V3_0;
		t.unmarshalText(subTypes, offsetStrExtended, version, warnings, compatibilityMode);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_3_0_invalid_offset() {
		VCardVersion version = VCardVersion.V3_0;
		t.unmarshalText(subTypes, "invalid", version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals("invalid", t.getText());
		assertWarnings(1, warnings);
	}

	@Test
	public void unmarshalText_3_0_text() {
		VCardVersion version = VCardVersion.V3_0;
		subTypes.setValue(VCardDataType.TEXT);
		t.unmarshalText(subTypes, offsetWithText, version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(offsetWithText, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_offset__no_value() {
		VCardVersion version = VCardVersion.V4_0;
		t.unmarshalText(subTypes, offsetStrExtended, version, warnings, compatibilityMode);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_invalid_offset__no_value() {
		VCardVersion version = VCardVersion.V4_0;
		t.unmarshalText(subTypes, "invalid", version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals("invalid", t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_offset__utc_offset_value() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.UTC_OFFSET);
		t.unmarshalText(subTypes, offsetStrExtended, version, warnings, compatibilityMode);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_4_0_invalid_offset__utc_offset_value() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.UTC_OFFSET);
		t.unmarshalText(subTypes, "invalid", version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalText_4_0_offset__text_value() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.TEXT);
		t.unmarshalText(subTypes, offsetStrExtended, version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(offsetStrExtended, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_invalid_offset__text_value() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.TEXT);
		t.unmarshalText(subTypes, "invalid", version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals("invalid", t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0_text__no_value() {
		VCardVersion version = VCardVersion.V4_0;
		t.unmarshalText(subTypes, timezoneIdStr, version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(timezoneIdStr, t.getText());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_4_0_text__utc_offset_value() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.UTC_OFFSET);
		t.unmarshalText(subTypes, timezoneIdStr, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalText_4_0_text__text_value() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(VCardDataType.TEXT);
		t.unmarshalText(subTypes, timezoneIdStr, version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(timezoneIdStr, t.getText());
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_empty() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TimezoneType.NAME.toLowerCase());
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TimezoneType.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, timezoneIdStr);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(timezoneIdStr, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_offset() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TimezoneType.NAME.toLowerCase());
		xe.append(VCardDataType.UTC_OFFSET, offsetStrExtended);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_invalid_offset() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TimezoneType.NAME.toLowerCase());
		xe.append(VCardDataType.UTC_OFFSET, "invalid");
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml_offset_and_text() {
		//text is preferred
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TimezoneType.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, timezoneIdStr);
		xe.append(VCardDataType.UTC_OFFSET, offsetStrExtended);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(timezoneIdStr, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_invalid_offset_and_text() {
		//text is preferred
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TimezoneType.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, timezoneIdStr);
		xe.append(VCardDataType.UTC_OFFSET, "invalid");
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(timezoneIdStr, t.getText());
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////

	@Test
	public void unmarshalHtml_offset() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + offsetStrExtended + "</div>");

		t.unmarshalHtml(element, warnings);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_invalid_offset() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>invalid</div>");

		t.unmarshalHtml(element, warnings);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals("invalid", t.getText());
		assertWarnings(1, warnings);
	}

	@Test
	public void unmarshalHtml_text() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + offsetStrExtended + ";EDT;" + timezoneIdStr + "</div>");

		t.unmarshalHtml(element, warnings);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(offsetStrExtended + ";EDT;" + timezoneIdStr, t.getText());
		assertWarnings(1, warnings);
	}

	/////////////////////////////////////////////////

	@Test
	public void unmarshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.TEXT, timezoneIdStr);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(timezoneIdStr, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_text_other_data_type() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.BOOLEAN, timezoneIdStr);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals(timezoneIdStr, t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_offset() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.UTC_OFFSET, offsetStrExtended);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalJson_invalid_offset() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.UTC_OFFSET, "invalid");

		t.unmarshalJson(subTypes, value, version, warnings);
	}

	@Test
	public void unmarshalJson_offset_other_data_type() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.BOOLEAN, offsetStrExtended);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(hourOffset, t.getHourOffset());
		assertEquals(minuteOffset, t.getMinuteOffset());
		assertNull(t.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_invalid_offset_other_data_type() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.BOOLEAN, "invalid");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals("invalid", t.getText());
		assertWarnings(0, warnings);
	}
}
