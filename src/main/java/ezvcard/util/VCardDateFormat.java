package ezvcard.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
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
	"\\d{8}",
	"yyyyMMdd"),
	
	/**
	 * Example: 2012-07-01
	 */
	DATE_EXTENDED(
	"\\d{4}-\\d{2}-\\d{2}",
	"yyyy-MM-dd"),
	
	/**
	 * Example: 20120701T142110-0500
	 */
	DATE_TIME_BASIC(
	"\\d{8}T\\d{6}[-\\+]\\d{4}",
	"yyyyMMdd'T'HHmmssZ"),
	
	/**
	 * Example: 2012-07-01T14:21:10-05:00
	 */
	DATE_TIME_EXTENDED(
	"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-\\+]\\d{2}:\\d{2}",
	"yyyy-MM-dd'T'HH:mm:ssZ"){
		@SuppressWarnings("serial")
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			DateFormat df = new SimpleDateFormat(formatStr){
				@Override
				public Date parse(String str) throws ParseException {
					//remove the colon from the timezone offset
					//SimpleDateFormat doesn't recognize timezone offsets that have colons
					int index = str.lastIndexOf(':');
					str = str.substring(0, index) + str.substring(index+1);

					return super.parse(str);
				}
				
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
	"\\d{8}T\\d{6}Z",
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
	"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
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
	"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-\\+]\\d{2}:?\\d{2}",
	"yyyy-MM-dd'T'HH:mm:ssZ"){
		@SuppressWarnings("serial")
		@Override
		public DateFormat getDateFormat(TimeZone timezone) {
			DateFormat df = new SimpleDateFormat(formatStr){
				@Override
				public Date parse(String str) throws ParseException {
					//remove the colon from the timezone offset if present
					//SimpleDateFormat doesn't recognize timezone offsets that have colons
					str = str.replaceAll("([-\\+]\\d{2}):(\\d{2})$", "$1$2");
					
					return super.parse(str);
				}
			};
			
			if (timezone != null){
				df.setTimeZone(timezone);
			}
			
			return df;
		}
	};
	//@formatter:on

	/**
	 * The regular expression pattern for the date format.
	 */
	private final Pattern pattern;

	/**
	 * The {@link SimpleDateFormat} format string used for parsing dates.
	 */
	protected final String formatStr;

	/**
	 * @param regex the regular expression for the date format
	 * @param formatStr the {@link SimpleDateFormat} format string used for
	 * parsing dates.
	 */
	private VCardDateFormat(String regex, String formatStr) {
		pattern = Pattern.compile(regex);
		this.formatStr = formatStr;
	}

	/**
	 * Determines whether a date string is in this ISO format.
	 * @param dateStr the date string
	 * @return true if it matches the date format, false if not
	 */
	public boolean matches(String dateStr) {
		return pattern.matcher(dateStr).matches();
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
	 * Determines the ISO format a date string is in.
	 * @param dateStr the date string (e.g. "20140322T120000Z")
	 * @return the ISO format (e.g. DATETIME_BASIC) or null if not found
	 */
	public static VCardDateFormat find(String dateStr) {
		for (VCardDateFormat format : values()) {
			if (format.matches(dateStr)) {
				return format;
			}
		}
		return null;
	}

	/**
	 * Parses a date string.
	 * @param dateStr the date string to parse (e.g. "20130609T181023Z")
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Date parse(String dateStr) {
		//determine which ISOFormat the date is in
		VCardDateFormat format = find(dateStr);
		if (format == null) {
			throw Messages.INSTANCE.getIllegalArgumentException(41, dateStr);
		}

		//parse the date
		DateFormat df = format.getDateFormat();
		try {
			return df.parse(dateStr);
		} catch (ParseException e) {
			//should never be thrown because the string is checked against a regex before being parsed
			throw Messages.INSTANCE.getIllegalArgumentException(41, dateStr);
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
