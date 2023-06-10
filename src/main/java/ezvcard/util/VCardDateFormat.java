package ezvcard.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.Messages;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * Parses and formats vCard timestamp values. These date formats are defined in
 * the ISO8601 specification.
 * @author Michael Angstadt
 */
public enum VCardDateFormat {
	/**
	 * <p>
	 * Formats dates using "extended" format. In this format, dashes separate
	 * the date components, and colons separate the time components.
	 * </p>
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>2012-07-01 ({@link LocalDate})</li>
	 * <li>2012-07-01T14:21:10 ({@link LocalDateTime})</li>
	 * <li>2012-07-01T14:21:10-05:00 ({@link OffsetDateTime})</li>
	 * <li>2012-07-01T14:21:10Z ({@link Instant})</li>
	 * </ul>
	 */
	EXTENDED {
		@Override
		String getPattern(TemporalAccessor temporal) {
			if (temporal instanceof ZoneOffset) {
				return "xxx";
			}
			if (temporal instanceof Instant) {
				return "yyyy-MM-dd'T'HH:mm:ssX";
			}
			if (hasOffset(temporal)) {
				return "yyyy-MM-dd'T'HH:mm:ssxxx";
			}
			if (hasTime(temporal)) {
				return "yyyy-MM-dd'T'HH:mm:ss";
			}
			return "yyyy-MM-dd";
		}
	},

	/**
	 * <p>
	 * Formats dates using "basic" format. In this format, nothing separates the
	 * date and time components from each other.
	 * </p>
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>20120701 ({@link LocalDate})</li>
	 * <li>20120701T142110 ({@link LocalDateTime})</li>
	 * <li>20120701T142110-0500 ({@link OffsetDateTime})</li>
	 * <li>20120701T142110Z ({@link Instant})</li>
	 * </ul>
	 */
	BASIC {
		@Override
		String getPattern(TemporalAccessor temporal) {
			if (temporal instanceof ZoneOffset) {
				return "xx";
			}
			if (temporal instanceof Instant) {
				return "yyyyMMdd'T'HHmmssX";
			}
			if (hasOffset(temporal)) {
				return "yyyyMMdd'T'HHmmssxx";
			}
			if (hasTime(temporal)) {
				return "yyyyMMdd'T'HHmmss";
			}
			return "yyyyMMdd";
		}
	};

	/**
	 * Formats a date (also accepts {@link ZoneOffset}).
	 * @param temporalAccessor the date
	 * @return the formatted date
	 */
	public String format(TemporalAccessor temporalAccessor) {
		String pattern = getPattern(temporalAccessor);
		DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern, Locale.ROOT);

		/*
		 * Instants must be converted to OffsetDateTime in order to be formatted
		 * using a format pattern.
		 */
		if (temporalAccessor instanceof Instant) {
			temporalAccessor = ((Instant) temporalAccessor).atOffset(ZoneOffset.UTC);
		}

		return df.format(temporalAccessor);
	}

	abstract String getPattern(TemporalAccessor temporal);

	/**
	 * <p>
	 * Parses a date string. String can be in basic or extended formats.
	 * </p>
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>"2012-07-01" returns {@link LocalDate}</li>
	 * <li>"2012-07-01T14:21:10" returns {@link LocalDateTime}</li>
	 * <li>"2012-07-01T14:21:10-05:00" returns {@link OffsetDateTime}</li>
	 * <li>"2012-07-01T14:21:10Z" returns {@link Instant}</li>
	 * </ul>
	 * @param string the string to parse
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats or if it contains an invalid value (e.g. "13"
	 * for the month)
	 */
	public static Temporal parse(String string) {
		TimestampPattern p = TimestampPattern.parse(string);
		if (p == null) {
			throw Messages.INSTANCE.getIllegalArgumentException(41, string);
		}

		try {
			LocalDate date = LocalDate.of(p.year(), p.month(), p.date());
			if (!p.hasTime()) {
				return date;
			}

			LocalTime time = LocalTime.of(p.hour(), p.minute(), p.second(), p.nanosecond());
			LocalDateTime datetime = LocalDateTime.of(date, time);

			ZoneOffset offset = p.offset();
			if (offset == null) {
				return datetime;
			}

			OffsetDateTime offsetDateTime = OffsetDateTime.of(datetime, offset);
			return "Z".equals(offset.getId()) ? Instant.from(offsetDateTime) : offsetDateTime;
		} catch (DateTimeException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Wrapper for a complex regular expression that parses multiple date
	 * formats.
	 */
	private static class TimestampPattern {
		//@formatter:off
		private static final Pattern regex = Pattern.compile(
			"^(\\d{4})(" +
				"-?(\\d{2})-?(\\d{2})|" +
				"-(\\d{1,2})-(\\d{1,2})" + //allow single digit month and/or date as long as there are dashes
			")" + 
			"(" +
				"T(\\d{2}):?(\\d{2}):?(\\d{2})(\\.\\d+)?" +
				"(" +
					"Z|([-+])((\\d{2})|((\\d{2}):?(\\d{2})))" +
				")?" +
			")?$"
		);
		//@formatter:on

		private final Matcher matcher;

		private TimestampPattern(Matcher matcher) {
			this.matcher = matcher;
		}

		/**
		 * Attempts to match the given string against the timestamp regex.
		 * @param string the string to parse
		 * @return the matched pattern or null if the string did not match the
		 * pattern
		 */
		public static TimestampPattern parse(String string) {
			Matcher m = regex.matcher(string);
			return m.find() ? new TimestampPattern(m) : null;
		}

		public int year() {
			return parseInt(1);
		}

		public int month() {
			return parseInt(3, 5);
		}

		public int date() {
			return parseInt(4, 6);
		}

		public boolean hasTime() {
			return matcher.group(8) != null;
		}

		public int hour() {
			return parseInt(8);
		}

		public int minute() {
			return parseInt(9);
		}

		public int second() {
			return parseInt(10);
		}

		public int nanosecond() {
			String s = matcher.group(11);
			if (s == null) {
				return 0;
			}

			double nanos = Double.parseDouble(s) * TimeUnit.SECONDS.toNanos(1);
			return (int) Math.round(nanos);
		}

		public ZoneOffset offset() {
			String offsetStr = matcher.group(12);
			return (offsetStr == null) ? null : ZoneOffset.of(offsetStr);
		}

		private int parseInt(int... group) {
			for (int g : group) {
				String s = matcher.group(g);
				if (s != null) {
					return Integer.parseInt(s);
				}
			}
			throw new NullPointerException();
		}
	}

	/**
	 * Determines if the given date has a time component
	 * @param temporalAccessor the date
	 * @return true if it has a time component, false if not
	 */
	public static boolean hasTime(TemporalAccessor temporalAccessor) {
		return temporalAccessor instanceof Instant || temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY);
	}

	private static boolean hasOffset(TemporalAccessor temporalAccessor) {
		return temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS);
	}
}
