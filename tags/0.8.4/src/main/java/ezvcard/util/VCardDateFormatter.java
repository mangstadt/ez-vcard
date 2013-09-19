package ezvcard.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

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
 * Helper class that formats and parses vCard dates. vCard dates adhere to the
 * ISO8601 date format standard.
 * @author Michael Angstadt
 */
public class VCardDateFormatter {
	/**
	 * Formats a date for inclusion in a vCard.
	 * @param date the date to format
	 * @param format the format to use
	 * @return the formatted date
	 */
	public static String format(Date date, ISOFormat format) {
		return format(date, format, TimeZone.getDefault());
	}

	/**
	 * Formats a date for inclusion in a vCard.
	 * @param date the date to format
	 * @param format the format to use
	 * @param timeZone the time zone to format the date in. This will be ignored
	 * if the specified ISOFormat is a "UTC" format
	 * @return the formatted date
	 */
	public static String format(Date date, ISOFormat format, TimeZone timeZone) {
		switch (format) {
		case UTC_TIME_BASIC:
		case UTC_TIME_EXTENDED:
			timeZone = TimeZone.getTimeZone("UTC");
			break;
		}

		DateFormat df = format.getFormatDateFormat();
		df.setTimeZone(timeZone);
		String str = df.format(date);

		switch (format) {
		case TIME_EXTENDED:
			//add a colon to the timezone
			//example: converts "2012-07-05T22:31:41-0400" to "2012-07-05T22:31:41-04:00"
			str = str.replaceAll("([-\\+]\\d{2})(\\d{2})$", "$1:$2");
			break;
		}

		return str;
	}

	/**
	 * Parses a vCard date.
	 * @param dateStr the date string to parse
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Date parse(String dateStr) {
		//find out what ISOFormat the date is in
		ISOFormat format = null;
		for (ISOFormat f : ISOFormat.values()) {
			if (f.matches(dateStr)) {
				format = f;
				break;
			}
		}
		if (format == null) {
			throw new IllegalArgumentException("Date string is not in a valid ISO-8601 format.");
		}

		//tweak the date string to make it work with SimpleDateFormat
		switch (format) {
		case TIME_EXTENDED:
		case HCARD_TIME_TAG:
			//SimpleDateFormat doesn't recognize timezone offsets that have colons
			//so remove the colon from the timezone offset
			dateStr = dateStr.replaceAll("([-\\+]\\d{2}):(\\d{2})$", "$1$2");
			break;
		case UTC_TIME_BASIC:
		case UTC_TIME_EXTENDED:
			//SimpleDateFormat doesn't recognize "Z"
			dateStr = dateStr.replace("Z", "+0000");
			break;
		}

		//parse the date
		DateFormat df = format.getParseDateFormat();
		try {
			return df.parse(dateStr);
		} catch (ParseException e) {
			//should never be thrown because the string is checked against a regex
			throw new RuntimeException(e);
		}
	}

	private VCardDateFormatter() {
		//hide constructor
	}
}
