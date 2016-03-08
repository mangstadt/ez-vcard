package ezvcard.util;

import java.util.TimeZone;

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
 */

/**
 * Represents a UTC offset.
 * @author Michael Angstadt
 */
public final class UtcOffset {
	private final long millis;

	/**
	 * @param positive true if the offset is positive, false if it is negative
	 * @param hour the hour component of the offset (the sign of this integer is
	 * ignored)
	 * @param minute the minute component of the offset (the sign of this
	 * integer is ignored)
	 */
	public UtcOffset(boolean positive, int hour, int minute) {
		/*
		 * Note: The (hour, minute) constructor was removed because it could not
		 * handle timezones such as "-0030".
		 */
		int sign = positive ? 1 : -1;
		hour = Math.abs(hour);
		minute = Math.abs(minute);

		millis = sign * (hoursToMillis(hour) + minutesToMillis(minute));
	}

	/**
	 * @param millis the offset in milliseconds
	 */
	public UtcOffset(long millis) {
		this.millis = millis;
	}

	/**
	 * Parses a UTC offset from a string.
	 * @param text the text to parse (e.g. "-0500")
	 * @return the parsed UTC offset
	 * @throws IllegalArgumentException if the text cannot be parsed
	 */
	public static UtcOffset parse(String text) {
		int i = 0;
		char sign = text.charAt(i);
		boolean positive = true;
		if (sign == '-') {
			positive = false;
			i++;
		} else if (sign == '+') {
			i++;
		}

		int maxLength = i + 4;
		int colon = text.indexOf(':', i);
		if (colon >= 0) {
			maxLength++;
		}
		if (text.length() > maxLength) {
			throw Messages.INSTANCE.getIllegalArgumentException(40, text);
		}

		String hourStr, minuteStr = null;
		if (colon < 0) {
			hourStr = text.substring(i);
			int minutePos = hourStr.length() - 2;
			if (minutePos > 0) {
				minuteStr = hourStr.substring(minutePos);
				hourStr = hourStr.substring(0, minutePos);
			}
		} else {
			hourStr = text.substring(i, colon);
			if (colon < text.length() - 1) {
				minuteStr = text.substring(colon + 1);
			}
		}

		int hour, minute;
		try {
			hour = Integer.parseInt(hourStr);
			minute = (minuteStr == null) ? 0 : Integer.parseInt(minuteStr);
		} catch (NumberFormatException e) {
			throw Messages.INSTANCE.getIllegalArgumentException(40, text);
		}

		return new UtcOffset(positive, hour, minute);
	}

	/**
	 * Creates a UTC offset from a {@link TimeZone} object.
	 * @param timezone the timezone
	 * @return the UTC offset
	 */
	public static UtcOffset parse(TimeZone timezone) {
		long offset = timezone.getOffset(System.currentTimeMillis());
		return new UtcOffset(offset);
	}

	/**
	 * Gets the offset in milliseconds.
	 * @return the offset in milliseconds
	 */
	public long getMillis() {
		return millis;
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

		boolean positive = (millis >= 0);
		long hour = Math.abs(millisToHours(millis));
		long minute = Math.abs(millisToMinutes(millis));

		sb.append(positive ? '+' : '-');

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
		result = prime * result + (int) (millis ^ (millis >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UtcOffset other = (UtcOffset) obj;
		if (millis != other.millis) return false;
		return true;
	}

	private static long hoursToMillis(long hours) {
		return hours * 60 * 60 * 1000;
	}

	private static long minutesToMillis(long minutes) {
		return minutes * 60 * 1000;
	}

	private static long millisToHours(long millis) {
		return millis / 1000 / 60 / 60;
	}

	private static long millisToMinutes(long millis) {
		return (millis / 1000 / 60) % 60;
	}
}
