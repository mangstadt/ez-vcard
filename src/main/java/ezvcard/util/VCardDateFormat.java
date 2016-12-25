package ezvcard.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.Messages;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Defines all of the date formats that are used in vCards, and also
 * parses/formats vCard dates. These date formats are defined in the ISO8601
 * specification.
 * @author Michael Angstadt
 */
public enum VCardDateFormat {
	//@formatter:off
	/**
	 * Example: 20120701
	 */
	DATE_BASIC(
	"yyyyMMdd"),
	
	/**
	 * Example: 2012-07-01
	 */
	DATE_EXTENDED(
	"yyyy-MM-dd"),
	
	/**
	 * Example: 20120701T142110-0500
	 */
	DATE_TIME_BASIC(
	"yyyyMMdd'T'HHmmssZ"),
	
	/**
	 * Example: 2012-07-01T14:21:10-05:00
	 */
	DATE_TIME_EXTENDED(
	"yyyy-MM-dd'T'HH:mm:ssZ"){
		@SuppressWarnings("serial")
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			DateFormat df = new SimpleDateFormat(formatStr){
				@Override
				public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition){
					StringBuffer sb = super.format(date, toAppendTo, fieldPosition);
					
					//add a colon between the hour and minute offsets
					sb.insert(sb.length()-2, ':');
					
					return sb;
				}
			};
			
			if (timezone != null){
				df.setTimeZone(timezone);
			}
			
			return df;
		}
	},
	
	/**
	 * Example: 20120701T192110Z
	 */
	UTC_DATE_TIME_BASIC(
	"yyyyMMdd'T'HHmmss'Z'"){
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			//always use the UTC timezone
			TimeZone utc = TimeZone.getTimeZone("UTC");
			return super.getDateFormat(utc);
		}
	},
	
	/**
	 * Example: 2012-07-01T19:21:10Z
	 */
	UTC_DATE_TIME_EXTENDED(
	"yyyy-MM-dd'T'HH:mm:ss'Z'"){
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			//always use the UTC timezone
			TimeZone utc = TimeZone.getTimeZone("UTC");
			return super.getDateFormat(utc);
		}
	},
	
	/**
	 * Example: 2012-07-01T14:21:10-0500
	 */
	HCARD_DATE_TIME(
	"yyyy-MM-dd'T'HH:mm:ssZ")
	
	;
	//@formatter:on

	/**
	 * The {@link SimpleDateFormat} format string used for parsing dates.
	 */
	protected final String formatStr;

	/**
	 * @param formatStr the {@link SimpleDateFormat} format string used for
	 * formatting dates.
	 */
	private VCardDateFormat(String formatStr) {
		this.formatStr = formatStr;
	}

	/**
	 * Builds a {@link DateFormat} object for parsing and formating dates in
	 * this format.
	 * @return the {@link DateFormat} object
	 */
	public DateFormat getDateFormat() {
		return getDateFormat(null);
	}

	/**
	 * Builds a {@link DateFormat} object for parsing and formating dates in
	 * this format.
	 * @param timezone the timezone the date is in or null for the default
	 * timezone
	 * @return the {@link DateFormat} object
	 */
	public DateFormat getDateFormat(TimeZone timezone) {
		DateFormat df = new SimpleDateFormat(formatStr);
		if (timezone != null) {
			df.setTimeZone(timezone);
		}
		return df;
	}

	/**
	 * Formats a date in this vCard date format.
	 * @param date the date to format
	 * @return the date string
	 */
	public String format(Date date) {
		return format(date, null);
	}

	/**
	 * Formats a date in this vCard date format.
	 * @param date the date to format
	 * @param timezone the timezone to format the date in or null for the
	 * default timezone
	 * @return the date string
	 */
	public String format(Date date, TimeZone timezone) {
		DateFormat df = getDateFormat(timezone);
		return df.format(date);
	}

	/**
	 * Parses a date string.
	 * @param dateStr the date string to parse (e.g. "20130609T181023Z")
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Date parse(String dateStr) {
		TimestampPattern p = new TimestampPattern(dateStr);
		if (!p.matches()) {
			throw Messages.INSTANCE.getIllegalArgumentException(41, dateStr);
		}

		TimeZone timezone = p.hasOffset() ? TimeZone.getTimeZone("UTC") : TimeZone.getDefault();
		Calendar c = Calendar.getInstance(timezone);
		c.clear();

		c.set(Calendar.YEAR, p.year());
		c.set(Calendar.MONTH, p.month() - 1);
		c.set(Calendar.DATE, p.date());

		if (p.hasTime()) {
			c.set(Calendar.HOUR_OF_DAY, p.hour());
			c.set(Calendar.MINUTE, p.minute());
			c.set(Calendar.SECOND, p.second());

			if (p.hasOffset()) {
				c.set(Calendar.ZONE_OFFSET, p.offsetMillis());
			}
		}

		return c.getTime();
	}

	/**
	 * Wrapper for a complex regular expression that parses multiple date
	 * formats.
	 */
	private static class TimestampPattern {
		//@formatter:off
		private static final Pattern regex = Pattern.compile(
			"^(\\d{4})-?(\\d{2})-?(\\d{2})" +
			"(" +
				"T(\\d{2}):?(\\d{2}):?(\\d{2})" +
				"(" +
					"Z|([-+])((\\d{2})|((\\d{2}):?(\\d{2})))" +
				")?" +
			")?$"
		);
		//@formatter:on

		private final Matcher m;
		private final boolean matches;

		public TimestampPattern(String str) {
			m = regex.matcher(str);
			matches = m.find();
		}

		public boolean matches() {
			return matches;
		}

		public int year() {
			return parseInt(1);
		}

		public int month() {
			return parseInt(2);
		}

		public int date() {
			return parseInt(3);
		}

		public boolean hasTime() {
			return m.group(5) != null;
		}

		public int hour() {
			return parseInt(5);
		}

		public int minute() {
			return parseInt(6);
		}

		public int second() {
			return parseInt(7);
		}

		public boolean hasOffset() {
			return m.group(8) != null;
		}

		public int offsetMillis() {
			if (m.group(8).equals("Z")) {
				return 0;
			}

			int positive = m.group(9).equals("+") ? 1 : -1;

			int offsetHour, offsetMinute;
			if (m.group(11) != null) {
				offsetHour = parseInt(11);
				offsetMinute = 0;
			} else {
				offsetHour = parseInt(13);
				offsetMinute = parseInt(14);
			}

			return (offsetHour * 60 * 60 * 1000 + offsetMinute * 60 * 1000) * positive;
		}

		private int parseInt(int group) {
			return Integer.parseInt(m.group(group));
		}
	}

	/**
	 * Determines whether a date string has a time component.
	 * @param dateStr the date string (e.g. "20130601T120000")
	 * @return true if it has a time component, false if not
	 */
	public static boolean dateHasTime(String dateStr) {
		return dateStr.contains("T");
	}

	/**
	 * Determines whether a date string is in UTC time or has a timezone offset.
	 * @param dateStr the date string (e.g. "20130601T120000Z",
	 * "20130601T120000-0400")
	 * @return true if it has a timezone, false if not
	 */
	public static boolean dateHasTimezone(String dateStr) {
		return dateStr.endsWith("Z") || dateStr.matches(".*?[-+]\\d\\d:?\\d\\d");
	}

	/**
	 * Gets the {@link TimeZone} object that corresponds to the given ID.
	 * @param timezoneId the timezone ID (e.g. "America/New_York")
	 * @return the timezone object or null if not found
	 */
	public static TimeZone parseTimeZoneId(String timezoneId) {
		TimeZone timezone = TimeZone.getTimeZone(timezoneId);
		return "GMT".equals(timezone.getID()) ? null : timezone;
	}
}
