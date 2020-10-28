package ezvcard.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.Messages;

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
			DateFormat df = new SimpleDateFormat(formatStr, Locale.ROOT){
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
	VCardDateFormat(String formatStr) {
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
		DateFormat df = new SimpleDateFormat(formatStr, Locale.ROOT);
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
		return parseAsCalendar(dateStr).getTime();
	}

	/**
	 * <p>
	 * Parses a date string as a {@link Calendar} object. This allows the caller
	 * to retrieve the individual components of the original date string, which
	 * get lost with {@link Calendar#getTime}. This method was added at the
	 * request of a user who needed the UTC offset from the original timestamp
	 * string.
	 * </p>
	 * <p>
	 * Use {@link Calendar#isSet(int)} to determine if a field was included in the
	 * original timestamp string. Calls to this method should be made before
	 * calling {@link Calendar#get} because calling latter method can cause
	 * unset fields to become populated (as mentioned in the
	 * {@link Calendar#isSet(int) isSet} Javadocs).
	 * </p>
	 * <p>
	 * The calendar's timezone will be set to "GMT" if the "Z" suffix was used
	 * in the timestamp string. If a numeric offset was used, the timezone will
	 * look like "GMT-05:00". If no offset was specified, the timezone will be
	 * set to the local system's default timezone.
	 * </p>
	 * @param dateStr the date string to parse (e.g. "20130609T181023Z")
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Calendar parseAsCalendar(String dateStr) {
		TimestampPattern p = new TimestampPattern(dateStr);
		if (!p.matches()) {
			throw Messages.INSTANCE.getIllegalArgumentException(41, dateStr);
		}

		Calendar c = Calendar.getInstance(p.timezone());
		c.clear();

		c.set(Calendar.YEAR, p.year());
		c.set(Calendar.MONTH, p.month() - 1);
		c.set(Calendar.DATE, p.date());

		if (p.hasTime()) {
			c.set(Calendar.HOUR_OF_DAY, p.hour());
			c.set(Calendar.MINUTE, p.minute());
			c.set(Calendar.SECOND, p.second());
			c.set(Calendar.MILLISECOND, p.millisecond());
		}

		return c;
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
			return parseInt(3, 5);
		}

		public int date() {
			return parseInt(4, 6);
		}

		public boolean hasTime() {
			return m.group(8) != null;
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

		public int millisecond() {
			String s = m.group(11);
			if (s == null) {
				return 0;
			}

			double ms = Double.parseDouble(s) * 1000;
			return (int) Math.round(ms);
		}

		public TimeZone timezone() {
			String offsetStr = m.group(12);
			if (offsetStr == null) {
				return TimeZone.getDefault();
			}

			/*
			 * Use the naked "GMT" timezone when "Z" is specified. This allows
			 * the user to differentiate from when an offset of "00:00" is
			 * explicitly specified (they refer to the same timezone, though).
			 */
			if (offsetStr.equals("Z")) {
				return TimeZone.getTimeZone("GMT");
			}

			/*
			 * Java is lenient regarding the format of the offset string. For
			 * example, all of the following resolve to the same "GMT+03:00"
			 * timezone: "GMT+3", "GMT+03", "GMT+3:00", "GMT+300"
			 */
			return TimeZone.getTimeZone("GMT" + offsetStr);
		}

		private int parseInt(int... group) {
			for (int g : group) {
				String s = m.group(g);
				if (s != null) {
					return Integer.parseInt(s);
				}
			}
			throw new NullPointerException();
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

	/**
	 * Converts a {@link Date} object to a {@link Calendar} object.
	 * @param date the date (can be null)
	 * @return the calendar or null if the date is null
	 */
	public static Calendar toCalendar(Date date) {
		if (date == null) {
			return null;
		}

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}
}
