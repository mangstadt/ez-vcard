package ezvcard.util;

import static ezvcard.util.TestUtils.buildTimezone;
import static ezvcard.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;

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
	public void format_different_locales() {
		Date date = date("2020-10-28 12:00:00");

		Locale defaultLocale = Locale.getDefault();
		try {
			for (Locale locale : Locale.getAvailableLocales()) {
				Locale.setDefault(locale);
				assertLocale(locale, VCardDateFormat.DATE_BASIC, date, "20201028");
				assertLocale(locale, VCardDateFormat.DATE_EXTENDED, date, "2020-10-28");
				assertLocale(locale, VCardDateFormat.DATE_TIME_BASIC, date, "20201028T120000+0100");
				assertLocale(locale, VCardDateFormat.DATE_TIME_EXTENDED, date, "2020-10-28T12:00:00+01:00");
				assertLocale(locale, VCardDateFormat.UTC_DATE_TIME_BASIC, date, "20201028T110000Z");
				assertLocale(locale, VCardDateFormat.UTC_DATE_TIME_EXTENDED, date, "2020-10-28T11:00:00Z");
				assertLocale(locale, VCardDateFormat.HCARD_DATE_TIME, date, "2020-10-28T12:00:00+0100");
			}
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}

	private static void assertLocale(Locale locale, VCardDateFormat df, Date date, String expected) {
		String actual = df.format(date);
		String message = "Test failed for " + df.name() + " with locale \"" + locale + "\".";
		assertEquals(message, expected, actual);
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
		assertEquals(datetime, VCardDateFormat.parse("20120701T100130+03"));
		assertEquals(datetime, VCardDateFormat.parse("20120701T040130-0300"));

		//extended, datetime, timezone
		assertEquals(datetime, VCardDateFormat.parse("2012-07-01T10:01:30+03:00"));

		//hCard, datetime, timezone
		assertEquals(datetime, VCardDateFormat.parse("2012-07-01T10:01:30+0300"));

		//no offset
		assertEquals(datetime, VCardDateFormat.parse("20120701T080130"));

		//with milliseconds
		Calendar c = Calendar.getInstance();
		c.setTime(datetime);
		c.set(Calendar.MILLISECOND, 100);
		assertEquals(c.getTime(), VCardDateFormat.parse("20120701T070130.1Z"));
		c.set(Calendar.MILLISECOND, 124);
		assertEquals(c.getTime(), VCardDateFormat.parse("20120701T070130.1239Z")); //round
	}

	@Test
	public void parseAsCalendar_date() throws Exception {
		Calendar actual = VCardDateFormat.parseAsCalendar("20120701");

		/*
		 * "isSet()" should be called before any calls to "get()" are made
		 * because some unset fields get computed when other fields are
		 * retrieved using "get()". See the Javadoc for "isSet()".
		 */
		assertFalse(actual.isSet(Calendar.HOUR_OF_DAY));
		assertFalse(actual.isSet(Calendar.MINUTE));
		assertFalse(actual.isSet(Calendar.SECOND));
		assertFalse(actual.isSet(Calendar.ZONE_OFFSET));

		assertEquals(TimeZone.getDefault().getID(), actual.getTimeZone().getID());
		assertEquals(2012, actual.get(Calendar.YEAR));
		assertEquals(6, actual.get(Calendar.MONTH));
		assertEquals(1, actual.get(Calendar.DAY_OF_MONTH));

		assertEquals(date("2012-07-01"), actual.getTime());
	}

	@Test
	public void parseAsCalendar_with_offset() throws Exception {
		Calendar actual = VCardDateFormat.parseAsCalendar("20120701T100130+0300");

		assertEquals(TimeUnit.HOURS.toMillis(3), actual.get(Calendar.ZONE_OFFSET));
		assertEquals("GMT+03:00", actual.getTimeZone().getID());
		assertEquals(2012, actual.get(Calendar.YEAR));
		assertEquals(6, actual.get(Calendar.MONTH));
		assertEquals(1, actual.get(Calendar.DAY_OF_MONTH));
		assertEquals(10, actual.get(Calendar.HOUR_OF_DAY));
		assertEquals(1, actual.get(Calendar.MINUTE));
		assertEquals(30, actual.get(Calendar.SECOND));

		assertEquals(date("2012-07-01 08:01:30"), actual.getTime());
	}

	@Test
	public void parseAsCalendar_with_z() throws Exception {
		Calendar actual = VCardDateFormat.parseAsCalendar("20120701T100130Z");

		/*
		 * "isSet()" should be called before any calls to "get()" are made
		 * because some unset fields get computed when other fields are
		 * retrieved using "get()". See the Javadoc for "isSet()".
		 */
		assertFalse(actual.isSet(Calendar.ZONE_OFFSET));

		assertEquals("GMT", actual.getTimeZone().getID());
		assertEquals(2012, actual.get(Calendar.YEAR));
		assertEquals(6, actual.get(Calendar.MONTH));
		assertEquals(1, actual.get(Calendar.DAY_OF_MONTH));
		assertEquals(10, actual.get(Calendar.HOUR_OF_DAY));
		assertEquals(1, actual.get(Calendar.MINUTE));
		assertEquals(30, actual.get(Calendar.SECOND));
		assertEquals(0, actual.get(Calendar.ZONE_OFFSET));

		assertEquals(date("2012-07-01 11:01:30"), actual.getTime());
	}

	@Test
	public void parseAsCalendar_without_offset() throws Exception {
		Calendar actual = VCardDateFormat.parseAsCalendar("20120701T100130");

		/*
		 * "isSet()" should be called before any calls to "get()" are made
		 * because some unset fields get computed when other fields are
		 * retrieved using "get()". See the Javadoc for "isSet()".
		 */
		assertFalse(actual.isSet(Calendar.ZONE_OFFSET));

		assertEquals(TimeZone.getDefault().getID(), actual.getTimeZone().getID());
		assertEquals(2012, actual.get(Calendar.YEAR));
		assertEquals(6, actual.get(Calendar.MONTH));
		assertEquals(1, actual.get(Calendar.DAY_OF_MONTH));
		assertEquals(10, actual.get(Calendar.HOUR_OF_DAY));
		assertEquals(1, actual.get(Calendar.MINUTE));
		assertEquals(30, actual.get(Calendar.SECOND));
		assertEquals(TimeZone.getDefault().getRawOffset(), actual.get(Calendar.ZONE_OFFSET));

		assertEquals(date("2012-07-01 10:01:30"), actual.getTime());
	}

	/**
	 * Allow single digit month and/or date as long as there are dashes.
	 */
	@Test
	public void parse_single_digit_month_and_date() throws Exception {
		{
			Date date = date("2012-07-01");

			assertEquals(date, VCardDateFormat.parse("2012-07-1"));
			assertEquals(date, VCardDateFormat.parse("2012-7-01"));
			assertEquals(date, VCardDateFormat.parse("2012-7-1"));

			try {
				VCardDateFormat.parse("201271");
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}
		}

		{
			Date ambiguous = date("2012-11-03");

			assertEquals(ambiguous, VCardDateFormat.parse("2012-11-3"));

			try {
				VCardDateFormat.parse("2012113"); //Jan 13 or Nov 3?
				fail();
			} catch (IllegalArgumentException e) {
				//expected
			}
		}
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
