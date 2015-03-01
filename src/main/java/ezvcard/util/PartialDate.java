package ezvcard.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * <p>
 * Represents a date in which some of the components are missing. This is used
 * to represent reduced accuracy and truncated dates, as defined in ISO8601.
 * </p>
 * <p>
 * A <b>truncated date</b> is a date where the "lesser" components are missing.
 * For example, "12:30" is truncated because the "seconds" component is missing.
 * 
 * <pre class="brush:java">
 * PartialDate date = PartialDate.time(12, 30, null);
 * </pre>
 * 
 * </p>
 * <p>
 * A <b>reduced accuracy date</b> is a date where the "greater" components are
 * missing. For example, "April 20" is reduced accuracy because the "year"
 * component is missing.
 * 
 * <pre class="brush:java">
 * PartialDate date = PartialDate.date(null, 4, 20);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public final class PartialDate {
	private static final int SKIP = -1;
	private static final int YEAR = 0;
	private static final int MONTH = 1;
	private static final int DATE = 2;
	private static final int HOUR = 3;
	private static final int MINUTE = 4;
	private static final int SECOND = 5;
	private static final int TIMEZONE_HOUR = 6;
	private static final int TIMEZONE_MINUTE = 7;

	//@formatter:off
	private static final Format dateFormats[] = new Format[] {
		new Format("(\\d{4})", YEAR),
		new Format("(\\d{4})-(\\d{2})", YEAR, MONTH),
		new Format("(\\d{4})-?(\\d{2})-?(\\d{2})", YEAR, MONTH, DATE),
		new Format("--(\\d{2})-?(\\d{2})", MONTH, DATE),
		new Format("--(\\d{2})", MONTH),
		new Format("---(\\d{2})", DATE)
	};
	//@formatter:on

	private static final String timezoneRegex = "(([-+]\\d{1,2}):?(\\d{2})?)?";

	//@formatter:off
	private static final Format timeFormats[] = new Format[] {
		new Format("(\\d{2})" + timezoneRegex, HOUR, SKIP, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("(\\d{2}):?(\\d{2})" + timezoneRegex, HOUR, MINUTE, SKIP, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("(\\d{2}):?(\\d{2}):?(\\d{2})" + timezoneRegex, HOUR, MINUTE, SECOND, SKIP, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("-(\\d{2}):?(\\d{2})" + timezoneRegex, MINUTE, SECOND, SKIP, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("-(\\d{2})" + timezoneRegex, MINUTE, SKIP, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("--(\\d{2})" + timezoneRegex, SECOND, SKIP, TIMEZONE_HOUR, TIMEZONE_MINUTE)
	};
	//@formatter:on

	private final Integer[] components = new Integer[8];

	/**
	 * <p>
	 * Creates a partial date containing only date components.
	 * </p>
	 * <p>
	 * The following combinations are not allowed and will result in an
	 * {@link IllegalArgumentException} being thrown:
	 * </p>
	 * <ul>
	 * <li>year, date (month missing)</li>
	 * </ul>
	 * @param year the year or null to exclude
	 * @param month the month or null to exclude
	 * @param date the day of the month or null to exclude
	 * @return the partial date
	 * @throws IllegalArgumentException if an invalid combination is entered or
	 * a component value is invalid (e.g. a negative month)
	 */
	public static PartialDate date(Integer year, Integer month, Integer date) {
		return new PartialDate(year, month, date, null, null, null, null);
	}

	/**
	 * <p>
	 * Creates a partial date containing only time components.
	 * </p>
	 * <p>
	 * The following combinations are not allowed and will result in an
	 * {@link IllegalArgumentException} being thrown:
	 * </p>
	 * <ul>
	 * <li>hour, second (minute missing)</li>
	 * </ul>
	 * @param hour the hour or null to exclude
	 * @param minute the minute or null to exclude
	 * @param second the second or null to exclude
	 * @return the partial date
	 * @throws IllegalArgumentException if an invalid combination is entered or
	 * a component value is invalid (e.g. a negative minute)
	 */
	public static PartialDate time(Integer hour, Integer minute, Integer second) {
		return time(hour, minute, second, null);
	}

	/**
	 * <p>
	 * Creates a partial date containing only time components.
	 * </p>
	 * <p>
	 * The following combinations are not allowed and will result in an
	 * {@link IllegalArgumentException} being thrown:
	 * </p>
	 * <ul>
	 * <li>hour, second (minute missing)</li>
	 * <li>timezoneMinute (timezoneHour missing)</li>
	 * </ul>
	 * @param hour the hour or null to exclude
	 * @param minute the minute or null to exclude
	 * @param second the second or null to exclude
	 * @param offset the UTC offset or null to exclude
	 * @return the partial date
	 * @throws IllegalArgumentException if an invalid combination is entered or
	 * a component value is invalid (e.g. a negative minute)
	 */
	public static PartialDate time(Integer hour, Integer minute, Integer second, UtcOffset offset) {
		return new PartialDate(null, null, null, hour, minute, second, offset);
	}

	/**
	 * <p>
	 * Creates a partial date containing date and time components, without a
	 * timezone.
	 * </p>
	 * <p>
	 * The following combinations are not allowed and will result in an
	 * {@link IllegalArgumentException} being thrown:
	 * </p>
	 * <ul>
	 * <li>year, date (month missing)</li>
	 * <li>hour, second (minute missing)</li>
	 * </ul>
	 * @param year the year or null to exclude
	 * @param month the month or null to exclude
	 * @param date the day of the month or null to exclude
	 * @param hour the hour or null to exclude
	 * @param minute the minute or null to exclude
	 * @param second the second or null to exclude
	 * @return the partial date
	 * @throws IllegalArgumentException if an invalid combination is entered or
	 * a component value is invalid (e.g. a negative minute)
	 */
	public static PartialDate dateTime(Integer year, Integer month, Integer date, Integer hour, Integer minute, Integer second) {
		return dateTime(year, month, date, hour, minute, second, null);
	}

	/**
	 * <p>
	 * Creates a partial date containing date and time components.
	 * </p>
	 * <p>
	 * The following combinations are not allowed and will result in an
	 * {@link IllegalArgumentException} being thrown:
	 * </p>
	 * <ul>
	 * <li>year, date (month missing)</li>
	 * <li>hour, second (minute missing)</li>
	 * <li>timezoneMinute (timezoneHour missing)</li>
	 * </ul>
	 * @param year the year or null to exclude
	 * @param month the month or null to exclude
	 * @param date the day of the month or null to exclude
	 * @param hour the hour or null to exclude
	 * @param minute the minute or null to exclude
	 * @param second the second or null to exclude
	 * @param offset the UTC offset or null to exclude
	 * @return the partial date
	 * @throws IllegalArgumentException if an invalid combination is entered or
	 * a component value is invalid (e.g. a negative minute)
	 */
	public static PartialDate dateTime(Integer year, Integer month, Integer date, Integer hour, Integer minute, Integer second, UtcOffset offset) {
		return new PartialDate(year, month, date, hour, minute, second, offset);
	}

	/**
	 * <p>
	 * Creates a new partial date.
	 * </p>
	 * <p>
	 * The following combinations are not allowed and will result in an
	 * {@link IllegalArgumentException} being thrown:
	 * </p>
	 * <ul>
	 * <li>year, date (month missing)</li>
	 * <li>hour, second (minute missing)</li>
	 * <li>timezoneMinute (timezoneHour missing)</li>
	 * </ul>
	 * @param year the year or null to exclude
	 * @param month the month or null to exclude
	 * @param date the day of the month or null to exclude
	 * @param hour the hour or null to exclude
	 * @param minute the minute or null to exclude
	 * @param second the second or null to exclude
	 * @param offset the UTC offset or null to exclude
	 * @throws IllegalArgumentException if an invalid combination is entered or
	 * a component value is invalid (e.g. a negative minute)
	 */
	public PartialDate(Integer year, Integer month, Integer date, Integer hour, Integer minute, Integer second, UtcOffset offset) {
		//check for illegal values
		if (month != null && (month < 1 || month > 12)) {
			throw new IllegalArgumentException("Month must be between 1 and 12 inclusive.");
		}
		if (date != null && (date < 1 || date > 31)) {
			throw new IllegalArgumentException("Date must be between 1 and 31 inclusive.");
		}
		if (hour != null && (hour < 0 || hour > 23)) {
			throw new IllegalArgumentException("Hour must be between 0 and 23 inclusive.");
		}
		if (minute != null && (minute < 0 || minute > 59)) {
			throw new IllegalArgumentException("Minute must be between 0 and 59 inclusive.");
		}
		if (second != null && (second < 0 || second > 59)) {
			throw new IllegalArgumentException("Second must be between 0 and 59 inclusive.");
		}
		if (offset != null && (offset.getMinute() < 0 || offset.getMinute() > 59)) {
			throw new IllegalArgumentException("Timezone minute must be between 0 and 59 inclusive.");
		}

		//check for illegal combinations
		if (year != null && month == null && date != null) {
			throw new IllegalArgumentException("Invalid date component combination: year, date");
		}
		if (hour != null && minute == null && second != null) {
			throw new IllegalArgumentException("Invalid time component combination: hour, second");
		}

		//assign values
		components[YEAR] = year;
		components[MONTH] = month;
		components[DATE] = date;
		components[HOUR] = hour;
		components[MINUTE] = minute;
		components[SECOND] = second;
		components[TIMEZONE_HOUR] = (offset == null) ? null : offset.getHour();
		components[TIMEZONE_MINUTE] = (offset == null) ? null : offset.getMinute();
	}

	/**
	 * Parses a partial date from a string.
	 * @param string the string (e.g. "--0420T15")
	 */
	public PartialDate(String string) {
		String split[] = string.split("T");
		boolean success;
		if (split.length == 1) {
			//date or time
			success = parseDate(string) || parseTime(string);
		} else if (split[0].length() == 0) {
			//time
			success = parseTime(split[1]);
		} else {
			//date and time
			success = parseDate(split[0]) && parseTime(split[1]);
		}

		if (!success) {
			throw new IllegalArgumentException("Could not parse date: " + string);
		}
	}

	private boolean parseDate(String value) {
		for (Format regex : dateFormats) {
			if (regex.parse(this, value)) {
				return true;
			}
		}
		return false;
	}

	private boolean parseTime(String value) {
		for (Format regex : timeFormats) {
			if (regex.parse(this, value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the year component.
	 * @return the year component or null if not set
	 */
	public Integer getYear() {
		return components[YEAR];
	}

	private boolean hasYear() {
		return getYear() != null;
	}

	/**
	 * Gets the month component.
	 * @return the month component or null if not set
	 */
	public Integer getMonth() {
		return components[MONTH];
	}

	private boolean hasMonth() {
		return getMonth() != null;
	}

	/**
	 * Gets the date component.
	 * @return the date component or null if not set
	 */
	public Integer getDate() {
		return components[DATE];
	}

	private boolean hasDate() {
		return getDate() != null;
	}

	/**
	 * Gets the hour component.
	 * @return the hour component or null if not set
	 */
	public Integer getHour() {
		return components[HOUR];
	}

	private boolean hasHour() {
		return getHour() != null;
	}

	/**
	 * Gets the minute component.
	 * @return the minute component or null if not set
	 */
	public Integer getMinute() {
		return components[MINUTE];
	}

	private boolean hasMinute() {
		return getMinute() != null;
	}

	/**
	 * Gets the second component.
	 * @return the second component or null if not set
	 */
	public Integer getSecond() {
		return components[SECOND];
	}

	private boolean hasSecond() {
		return getSecond() != null;
	}

	/**
	 * Gets the timezone component.
	 * @return the timezone component (index 0 = hour, index 1 = minute) or null
	 * if not set
	 */
	public Integer[] getTimezone() {
		if (!hasTimezone()) {
			return null;
		}
		return new Integer[] { components[TIMEZONE_HOUR], components[TIMEZONE_MINUTE] };
	}

	private boolean hasTimezone() {
		return components[TIMEZONE_HOUR] != null; //minute component is optional
	}

	/**
	 * Determines if there are any date components.
	 * @return true if it has at least one date component, false if not
	 */
	public boolean hasDateComponent() {
		return hasYear() || hasMonth() || hasDate();
	}

	/**
	 * Determines if there are any time components.
	 * @return true if there is at least one time component, false if not
	 */
	public boolean hasTimeComponent() {
		return hasHour() || hasMinute() || hasSecond();
	}

	/**
	 * Converts this partial date to its ISO 8601 representation.
	 * @param extended true to use extended format, false to use basic
	 * @return the ISO 8601 representation
	 */
	public String toDateAndOrTime(boolean extended) {
		StringBuilder sb = new StringBuilder();
		NumberFormat nf = new DecimalFormat("00");

		String yearStr = hasYear() ? getYear().toString() : null;
		String monthStr = hasMonth() ? nf.format(getMonth()) : null;
		String dateStr = hasDate() ? nf.format(getDate()) : null;

		String dash = extended ? "-" : "";
		if (hasYear() && !hasMonth() && !hasDate()) {
			sb.append(yearStr);
		} else if (!hasYear() && hasMonth() && !hasDate()) {
			sb.append("--").append(monthStr);
		} else if (!hasYear() && !hasMonth() && hasDate()) {
			sb.append("---").append(dateStr);
		} else if (hasYear() && hasMonth() && !hasDate()) {
			sb.append(yearStr).append("-").append(monthStr);
		} else if (!hasYear() && hasMonth() && hasDate()) {
			sb.append("--").append(monthStr).append(dash).append(dateStr);
		} else if (hasYear() && !hasMonth() && hasDate()) {
			throw new IllegalStateException("Invalid date component combination: year, date");
		} else if (hasYear() && hasMonth() && hasDate()) {
			sb.append(yearStr).append(dash).append(monthStr).append(dash).append(dateStr);
		}

		if (hasTimeComponent()) {
			sb.append('T');

			String hourStr = hasHour() ? nf.format(getHour()) : null;
			String minuteStr = hasMinute() ? nf.format(getMinute()) : null;
			String secondStr = hasSecond() ? nf.format(getSecond()) : null;

			dash = extended ? ":" : "";
			if (hasHour() && !hasMinute() && !hasSecond()) {
				sb.append(hourStr);
			} else if (!hasHour() && hasMinute() && !hasSecond()) {
				sb.append("-").append(minuteStr);
			} else if (!hasHour() && !hasMinute() && hasSecond()) {
				sb.append("--").append(secondStr);
			} else if (hasHour() && hasMinute() && !hasSecond()) {
				sb.append(hourStr).append(dash).append(minuteStr);
			} else if (!hasHour() && hasMinute() && hasSecond()) {
				sb.append("-").append(minuteStr).append(dash).append(secondStr);
			} else if (hasHour() && !hasMinute() && hasSecond()) {
				throw new IllegalStateException("Invalid time component combination: hour, second");
			} else if (hasHour() && hasMinute() && hasSecond()) {
				sb.append(hourStr).append(dash).append(minuteStr).append(dash).append(secondStr);
			}

			if (hasTimezone()) {
				Integer[] timezone = getTimezone();
				if (timezone[1] == null) {
					timezone[1] = 0;
				}
				sb.append(new UtcOffset(timezone[0], timezone[1]).toString(extended));
			}
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(components);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PartialDate other = (PartialDate) obj;
		if (!Arrays.equals(components, other.components)) return false;
		return true;
	}

	@Override
	public String toString() {
		return toDateAndOrTime(true);
	}

	/**
	 * Represents a string format that a partial date can be in.
	 */
	private static class Format {
		private Pattern regex;
		private int[] componentIndexes;

		/**
		 * @param regex the regular expression that describes the format
		 * @param componentIndexes the indexes of the
		 * {@link PartialDate#components} array to assign the value of each
		 * regex group to, or -1 to ignore the group
		 */
		public Format(String regex, int... componentIndexes) {
			this.regex = Pattern.compile("^" + regex + "$");
			this.componentIndexes = componentIndexes;
		}

		/**
		 * Tries to parse a given string.
		 * @param partialDate the {@link PartialDate} object
		 * @param value the string
		 * @return true if the string was successfully parsed, false if not
		 */
		public boolean parse(PartialDate partialDate, String value) {
			Matcher m = regex.matcher(value);
			if (m.find()) {
				for (int i = 0; i < componentIndexes.length; i++) {
					int index = componentIndexes[i];
					if (index == SKIP) {
						continue;
					}

					int group = i + 1;
					String groupStr = m.group(group);
					if (groupStr != null) {
						if (groupStr.startsWith("+")) {
							groupStr = groupStr.substring(1);
						}
						partialDate.components[index] = Integer.valueOf(groupStr);
					}
				}
				return true;
			}
			return false;
		}
	}
}
