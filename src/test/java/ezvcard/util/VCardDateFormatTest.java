package ezvcard.util;

import static ezvcard.util.TestUtils.buildTimezone;
import static ezvcard.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import org.junit.ClassRule;
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
 */

/**
 * @author Michael Angstadt
 */
public class VCardDateFormatTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	@Test
	public void format() {
		Date date = date("2006-01-02 10:20:30");

		assertEquals("20060102", VCardDateFormat.DATE_BASIC.format(date));
		assertEquals("2006-01-02", VCardDateFormat.DATE_EXTENDED.format(date));
		assertEquals("20060102T102030+0100", VCardDateFormat.DATE_TIME_BASIC.format(date));
		assertEquals("2006-01-02T10:20:30+01:00", VCardDateFormat.DATE_TIME_EXTENDED.format(date));
		assertEquals("2006-01-02T10:20:30+0100", VCardDateFormat.HCARD_DATE_TIME.format(date));
		assertEquals("20060102T092030Z", VCardDateFormat.UTC_DATE_TIME_BASIC.format(date));
		assertEquals("2006-01-02T09:20:30Z", VCardDateFormat.UTC_DATE_TIME_EXTENDED.format(date));
	}

	@Test
	public void format_timezone() {
		TimeZone timezone = buildTimezone(-2, 0);

		Date datetime = date("2006-01-02 10:20:30");

		assertEquals("20060102T072030-0200", VCardDateFormat.DATE_TIME_BASIC.format(datetime, timezone));
		assertEquals("2006-01-02T07:20:30-02:00", VCardDateFormat.DATE_TIME_EXTENDED.format(datetime, timezone));
		assertEquals("2006-01-02T07:20:30-0200", VCardDateFormat.HCARD_DATE_TIME.format(datetime, timezone));
	}

	@Test
	public void parse() throws Exception {
		Date date = date("2012-07-01");
		Date datetime = date("2012-07-01 08:01:30");

		//basic, date
		assertEquals(date, VCardDateFormat.parse("20120701"));

		//extended, date
		assertEquals(date, VCardDateFormat.parse("2012-07-01"));

		//basic, datetime, GMT
		assertEquals(datetime, VCardDateFormat.parse("20120701T070130Z"));

		//extended, datetime, GMT
		assertEquals(datetime, VCardDateFormat.parse("2012-07-01T07:01:30Z"));

		//basic, datetime, timezone
		assertEquals(datetime, VCardDateFormat.parse("20120701T100130+0300"));

		//extended, datetime, timezone
		assertEquals(datetime, VCardDateFormat.parse("2012-07-01T10:01:30+03:00"));

		//hCard, datetime, timezone
		assertEquals(datetime, VCardDateFormat.parse("2012-07-01T10:01:30+0300"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		VCardDateFormat.parse("invalid");
	}

	@Test
	public void dateHasTime() {
		assertFalse(VCardDateFormat.dateHasTime("20130601"));
		assertTrue(VCardDateFormat.dateHasTime("20130601T120000"));
	}

	@Test
	public void dateHasTimezone() {
		assertFalse(VCardDateFormat.dateHasTimezone("20130601T120000"));
		assertTrue(VCardDateFormat.dateHasTimezone("20130601T120000Z"));
		assertTrue(VCardDateFormat.dateHasTimezone("20130601T120000+0100"));
		assertTrue(VCardDateFormat.dateHasTimezone("20130601T120000-0100"));
		assertTrue(VCardDateFormat.dateHasTimezone("2013-06-01T12:00:00+01:00"));
		assertTrue(VCardDateFormat.dateHasTimezone("2013-06-01T12:00:00-01:00"));
	}

	@Test
	public void parseTimezoneId() {
		TimeZone tz = VCardDateFormat.parseTimeZoneId("America/New_York");
		assertEquals("America/New_York", tz.getID());

		tz = VCardDateFormat.parseTimeZoneId("Bogus/Timezone");
		assertNull(tz);
	}
}
