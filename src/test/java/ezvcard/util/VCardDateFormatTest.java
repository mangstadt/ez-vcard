package ezvcard.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/*
 Copyright (c) 2012-2026, Michael Angstadt
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
class VCardDateFormatTest {
	@Test
	void format_date() {
		LocalDate date = LocalDate.of(2006, 1, 2);
		assertEquals("20060102", VCardDateFormat.BASIC.format(date));
		assertEquals("2006-01-02", VCardDateFormat.EXTENDED.format(date));
	}

	@Test
	void format_datetime() {
		LocalDateTime dateTime = LocalDateTime.of(2006, 1, 2, 10, 20, 30);
		assertEquals("20060102T102030", VCardDateFormat.BASIC.format(dateTime));
		assertEquals("2006-01-02T10:20:30", VCardDateFormat.EXTENDED.format(dateTime));
	}

	@Test
	void format_offset_positive() {
		OffsetDateTime offsetPositive = OffsetDateTime.of(2006, 1, 2, 10, 20, 30, 0, ZoneOffset.ofHours(1));
		assertEquals("20060102T102030+0100", VCardDateFormat.BASIC.format(offsetPositive));
		assertEquals("2006-01-02T10:20:30+01:00", VCardDateFormat.EXTENDED.format(offsetPositive));
	}

	@Test
	void format_offset_positive_minutes() {
		OffsetDateTime offsetPositiveMinutes = OffsetDateTime.of(2006, 1, 2, 10, 20, 30, 0, ZoneOffset.ofHoursMinutes(1, 30));
		assertEquals("20060102T102030+0130", VCardDateFormat.BASIC.format(offsetPositiveMinutes));
		assertEquals("2006-01-02T10:20:30+01:30", VCardDateFormat.EXTENDED.format(offsetPositiveMinutes));
	}

	@Test
	void format_offset_negative() {
		OffsetDateTime offsetNegative = OffsetDateTime.of(2006, 1, 2, 10, 20, 30, 0, ZoneOffset.ofHours(-1));
		assertEquals("20060102T102030-0100", VCardDateFormat.BASIC.format(offsetNegative));
		assertEquals("2006-01-02T10:20:30-01:00", VCardDateFormat.EXTENDED.format(offsetNegative));
	}

	@Test
	void format_offset_negative_minutes() {
		OffsetDateTime offsetNegativeMinutes = OffsetDateTime.of(2006, 1, 2, 10, 20, 30, 0, ZoneOffset.ofHoursMinutes(-1, -30));
		assertEquals("20060102T102030-0130", VCardDateFormat.BASIC.format(offsetNegativeMinutes));
		assertEquals("2006-01-02T10:20:30-01:30", VCardDateFormat.EXTENDED.format(offsetNegativeMinutes));
	}

	@Test
	void format_instant() {
		Instant instant = LocalDateTime.of(2006, 1, 2, 10, 20, 30).toInstant(ZoneOffset.UTC);
		assertEquals("20060102T102030Z", VCardDateFormat.BASIC.format(instant));
		assertEquals("2006-01-02T10:20:30Z", VCardDateFormat.EXTENDED.format(instant));
	}

	@Test
	void format_zoned_datetime() {
		ZonedDateTime zonedDateTime = ZonedDateTime.of(2006, 1, 2, 10, 20, 30, 0, ZoneId.of("America/New_York"));
		assertEquals("20060102T102030-0500", VCardDateFormat.BASIC.format(zonedDateTime));
		assertEquals("2006-01-02T10:20:30-05:00", VCardDateFormat.EXTENDED.format(zonedDateTime));
	}

	@Test
	void format_different_locales() {
		OffsetDateTime date = OffsetDateTime.of(2006, 1, 2, 10, 20, 30, 0, ZoneOffset.ofHours(1));

		Locale defaultLocale = Locale.getDefault();
		try {
			for (Locale locale : Locale.getAvailableLocales()) {
				Locale.setDefault(locale);
				assertLocale(locale, VCardDateFormat.BASIC, date, "20060102T102030+0100");
				assertLocale(locale, VCardDateFormat.EXTENDED, date, "2006-01-02T10:20:30+01:00");
			}
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}

	private static void assertLocale(Locale locale, VCardDateFormat df, OffsetDateTime date, String expected) {
		String actual = df.format(date);
		assertEquals(expected, actual, () -> "Test failed for " + df.name() + " with locale \"" + locale + "\".");
	}

	@Test
	void parse_date() {
		LocalDate date = LocalDate.of(2012, 7, 1);
		assertEquals(date, VCardDateFormat.parse("20120701"));
		assertEquals(date, VCardDateFormat.parse("2012-07-01"));
	}

	@Test
	void parse_datetime() {
		LocalDateTime datetime = LocalDateTime.of(2012, 7, 1, 7, 1, 30);
		assertEquals(datetime, VCardDateFormat.parse("20120701T070130"));
		assertEquals(datetime, VCardDateFormat.parse("2012-07-01T07:01:30"));
	}

	@Test
	void parse_instant() {
		Instant instant = LocalDateTime.of(2012, 7, 1, 7, 1, 30).toInstant(ZoneOffset.UTC);
		assertEquals(instant, VCardDateFormat.parse("20120701T070130Z"));
		assertEquals(instant, VCardDateFormat.parse("2012-07-01T07:01:30Z"));
	}

	@Test
	void parse_offset() {
		LocalDateTime datetime = LocalDateTime.of(2012, 7, 1, 7, 1, 30);
		OffsetDateTime positiveOffset = OffsetDateTime.of(datetime, ZoneOffset.ofHours(3));
		OffsetDateTime negativeOffset = OffsetDateTime.of(datetime, ZoneOffset.ofHours(-3));

		assertEquals(positiveOffset, VCardDateFormat.parse("20120701T070130+0300"));
		assertEquals(positiveOffset, VCardDateFormat.parse("20120701T070130+03"));
		assertEquals(negativeOffset, VCardDateFormat.parse("20120701T070130-0300"));
		assertEquals(negativeOffset, VCardDateFormat.parse("20120701T070130-03"));

		assertEquals(positiveOffset, VCardDateFormat.parse("2012-07-01T07:01:30+03:00"));
		assertEquals(positiveOffset, VCardDateFormat.parse("2012-07-01T07:01:30+03"));
		assertEquals(negativeOffset, VCardDateFormat.parse("2012-07-01T07:01:30-03:00"));
		assertEquals(negativeOffset, VCardDateFormat.parse("2012-07-01T07:01:30-03"));
	}

	/*
	 * hCard uses extended format, but does not put a colon in the offset.
	 */
	@Test
	void parse_hcard() {
		OffsetDateTime datetime = OffsetDateTime.of(LocalDateTime.of(2012, 7, 1, 7, 1, 30), ZoneOffset.ofHours(3));
		assertEquals(datetime, VCardDateFormat.parse("2012-07-01T07:01:30+0300"));
	}

	@Test
	void parse_datetime_nanoseconds() {
		Instant instant = LocalDateTime.of(2012, 7, 1, 7, 1, 30).toInstant(ZoneOffset.UTC);

		Instant instantWithNanos = instant.plus(100_000_000, ChronoUnit.NANOS);
		assertEquals(instantWithNanos, VCardDateFormat.parse("20120701T070130.1Z"));

		instantWithNanos = instant.plus(123_900_000, ChronoUnit.NANOS);
		assertEquals(instantWithNanos, VCardDateFormat.parse("20120701T070130.1239Z"));

		instantWithNanos = instant.plus(123_456_789, ChronoUnit.NANOS);
		assertEquals(instantWithNanos, VCardDateFormat.parse("20120701T070130.1234567888Z")); //round
	}

	/**
	 * Allow single digit month and/or date as long as there are dashes.
	 */
	@Test
	void parse_single_digit_month_and_date() {
		{
			LocalDate date = LocalDate.of(2012, 7, 1);

			assertEquals(date, VCardDateFormat.parse("2012-07-1"));
			assertEquals(date, VCardDateFormat.parse("2012-7-01"));
			assertEquals(date, VCardDateFormat.parse("2012-7-1"));

			assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("201271"));
		}

		{
			LocalDate ambiguous = LocalDate.of(2012, 11, 3);

			assertEquals(ambiguous, VCardDateFormat.parse("2012-11-3"));

			assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("2012113")); //Jan 13 or Nov 3?
		}
	}

	@Test
	void parse_invalid() {
		assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("invalid"));
	}

	@Test
	void parse_invalid_month() {
		assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("19879215"));
	}

	@Test
	void parse_invalid_date() {
		assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("19871233"));
	}

	@Test
	void parse_invalid_hour() {
		assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("19871231T240000"));
	}

	@Test
	void parse_invalid_minute() {
		assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("19871231T006100"));
	}

	@Test
	void parse_invalid_second() {
		assertThrows(IllegalArgumentException.class, () -> VCardDateFormat.parse("19871231T000061"));
	}

	@Test
	void hasTime() {
		assertFalse(VCardDateFormat.hasTime(LocalDate.now()));
		assertTrue(VCardDateFormat.hasTime(LocalDateTime.now()));
		assertTrue(VCardDateFormat.hasTime(OffsetDateTime.now()));
		assertTrue(VCardDateFormat.hasTime(Instant.now()));
	}
}
