package ezvcard.util;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 */

/**
 * Represents a UTC offset.
 * @author Michael Angstadt
 */
public final class UtcOffset {
	private final int hour;
	private final int minute;

	/**
	 * Creates a new UTC offset.
	 * @param hour the hour component (may be negative)
	 * @param minute the minute component (must be between 0 and 59)
	 */
	public UtcOffset(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	/**
	 * Parses a UTC offset from a string.
	 * @param text the text to parse (e.g. "-0500")
	 * @return the parsed UTC offset
	 * @throws IllegalArgumentException if the text cannot be parsed
	 */
	public static UtcOffset parse(String text) {
		Pattern timeZoneRegex = Pattern.compile("^([-\\+])?(\\d{1,2})(:?(\\d{2}))?$");
		Matcher m = timeZoneRegex.matcher(text);

		if (!m.find()) {
			throw new IllegalArgumentException("Offset string is not in ISO8610 format: " + text);
		}

		String sign = m.group(1);
		boolean positive;
		if ("-".equals(sign)) {
			positive = false;
		} else {
			positive = true;
		}

		String hourStr = m.group(2);
		int hourOffset = Integer.parseInt(hourStr);
		if (!positive) {
			hourOffset *= -1;
		}

		String minuteStr = m.group(4);
		int minuteOffset = (minuteStr == null) ? 0 : Integer.parseInt(minuteStr);

		return new UtcOffset(hourOffset, minuteOffset);
	}

	/**
	 * Creates a UTC offset from a {@link TimeZone} object.
	 * @param timezone the timezone
	 * @return the UTC offset
	 */
	public static UtcOffset parse(TimeZone timezone) {
		long offsetMs = timezone.getOffset(System.currentTimeMillis());
		int hours = (int) (offsetMs / 1000 / 60 / 60);
		int minutes = (int) ((offsetMs / 1000 / 60) % 60);
		if (minutes < 0) {
			minutes *= -1;
		}
		return new UtcOffset(hours, minutes);
	}

	/**
	 * Gets the hour component.
	 * @return the hour component
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * Gets the minute component.
	 * @return the minute component
	 */
	public int getMinute() {
		return minute;
	}

	/**
	 * Converts this offset to its ISO string representation using "basic"
	 * format.
	 * @return the ISO string representation (e.g. "-0500")
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Converts this offset to its ISO string representation.
	 * @param extended true to use extended format (e.g. "-05:00"), false to use
	 * basic format (e.g. "-0500")
	 * @return the ISO string representation
	 */
	public String toString(boolean extended) {
		StringBuilder sb = new StringBuilder();

		boolean positive = hour >= 0;
		sb.append(positive ? '+' : '-');

		int hour = Math.abs(this.hour);
		if (hour < 10) {
			sb.append('0');
		}
		sb.append(hour);

		if (extended) {
			sb.append(':');
		}

		if (minute < 10) {
			sb.append('0');
		}
		sb.append(minute);

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hour;
		result = prime * result + minute;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UtcOffset other = (UtcOffset) obj;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		return true;
	}
}
