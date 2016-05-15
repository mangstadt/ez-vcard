package ezvcard.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
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
 * <p>
 * Represents a date in which some of the components are missing. This is used
 * to represent reduced accuracy and truncated dates, as defined in ISO8601.
 * </p>
 * <p>
 * A <b>reduced accuracy date</b> is a date where the "lesser" components are
 * missing. For example, "12:30" is reduced accuracy because the "seconds"
 * component is missing.
 * </p>
 * 
 * <pre class="brush:java">
 * PartialDate date = PartialDate.builder().hour(12).minute(30).build();
 * </pre>
 * 
 * <p>
 * A <b>truncated date</b> is a date where the "greater" components are missing.
 * For example, "April 20" is truncated because the "year" component is missing.
 * </p>
 * 
 * <pre class="brush:java">
 * PartialDate date = PartialDate.builder().month(4).date(20).build();
 * </pre>
 * @author Michael Angstadt
 */
public final class PartialDate {
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
		new Format("(\\d{2})" + timezoneRegex, HOUR, null, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("(\\d{2}):?(\\d{2})" + timezoneRegex, HOUR, MINUTE, null, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("(\\d{2}):?(\\d{2}):?(\\d{2})" + timezoneRegex, HOUR, MINUTE, SECOND, null, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("-(\\d{2}):?(\\d{2})" + timezoneRegex, MINUTE, SECOND, null, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("-(\\d{2})" + timezoneRegex, MINUTE, null, TIMEZONE_HOUR, TIMEZONE_MINUTE),
		new Format("--(\\d{2})" + timezoneRegex, SECOND, null, TIMEZONE_HOUR, TIMEZONE_MINUTE)
	};
	//@formatter:on

	private final Integer[] components;
	private final UtcOffset offset;

	/**
	 * @param components the date/time components array
	 * @param offset the UTC offset or null if not set
	 */
	private PartialDate(Integer[] components, UtcOffset offset) {
		this.components = components;
		this.offset = offset;
	}

	/**
	 * Creates a builder object.
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a builder object.
	 * @param orig the object to copy
	 * @return the builder
	 */
	public static Builder builder(PartialDate orig) {
		return new Builder(orig);
	}

	/**
	 * Parses a partial date from a string.
	 * @param string the string (e.g. "--0420T15")
	 * @return the parsed date
	 * @throws IllegalArgumentException if there's a problem parsing the date
	 * string
	 */
	public static PartialDate parse(String string) {
		int t = string.indexOf('T');
		String beforeT, afterT;
		if (t < 0) {
			beforeT = string;
			afterT = null;
		} else {
			beforeT = string.substring(0, t);
			afterT = (t < string.length() - 1) ? string.substring(t + 1) : null;
		}

		Builder builder = new Builder();
		boolean success;
		if (afterT == null) {
			//date or time
			success = parseDate(beforeT, builder) || parseTime(beforeT, builder);
		} else if (beforeT.length() == 0) {
			//time
			success = parseTime(afterT, builder);
		} else {
			//date and time
			success = parseDate(beforeT, builder) && parseTime(afterT, builder);
		}

		if (!success) {
			throw Messages.INSTANCE.getIllegalArgumentException(36, string);
		}
		return builder.build();
	}

	private static boolean parseDate(String value, Builder builder) {
		return parseFormats(value, builder, dateFormats);
	}

	private static boolean parseTime(String value, Builder builder) {
		return parseFormats(value, builder, timeFormats);
	}

	private static boolean parseFormats(String value, Builder builder, Format formats[]) {
		for (Format regex : formats) {
			if (regex.parse(builder, value)) {
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

	/**
	 * Determines if the year component is set.
	 * @return true if the component is set, false if not
	 */
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

	/**
	 * Determines if the month component is set.
	 * @return true if the component is set, false if not
	 */
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

	/**
	 * Determines if the date component is set.
	 * @return true if the component is set, false if not
	 */
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

	/**
	 * Determines if the hour component is set.
	 * @return true if the component is set, false if not
	 */
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

	/**
	 * Determines if the minute component is set.
	 * @return true if the component is set, false if not
	 */
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

	/**
	 * Determines if the second component is set.
	 * @return true if the component is set, false if not
	 */
	private boolean hasSecond() {
		return getSecond() != null;
	}

	/**
	 * Gets the UTC offset.
	 * @return the UTC offset or null if not set
	 */
	public UtcOffset getUtcOffset() {
		return offset;
	}

	/**
	 * Determines if this date has a timezone component.
	 * @return true if the component is set, false if not
	 */
	private boolean hasUtcOffset() {
		return offset != null;
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
	 * @return the ISO 8601 representation (e.g. "--0416")
	 * @throws IllegalStateException if an ISO 8601 representation of the date
	 * cannot be created because the date's components are invalid. This will
	 * not happen if the partial date is constructed using the
	 * {@link #builder()} method
	 */
	public String toISO8601(boolean extended) {
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
			throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(38));
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
				throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(39));
			} else if (hasHour() && hasMinute() && hasSecond()) {
				sb.append(hourStr).append(dash).append(minuteStr).append(dash).append(secondStr);
			}

			if (hasUtcOffset()) {
				sb.append(offset.toString(extended));
			}
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(components);
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PartialDate other = (PartialDate) obj;
		if (!Arrays.equals(components, other.components)) return false;
		if (offset == null) {
			if (other.offset != null) return false;
		} else if (!offset.equals(other.offset)) return false;
		return true;
	}

	@Override
	public String toString() {
		return toISO8601(true);
	}

	/**
	 * Represents a string format that a partial date can be in.
	 */
	private static class Format {
		private Pattern regex;
		private Integer[] componentIndexes;

		/**
		 * @param regex the regular expression that describes the format
		 * @param componentIndexes the indexes of the
		 * {@link PartialDate#components} array to assign the value of each
		 * regex group to, or -1 to ignore the group
		 */
		public Format(String regex, Integer... componentIndexes) {
			this.regex = Pattern.compile('^' + regex + '$');
			this.componentIndexes = componentIndexes;
		}

		/**
		 * Tries to parse a given string.
		 * @param components the date/time components array
		 * @param value the string
		 * @return true if the string was successfully parsed, false if not
		 */
		public boolean parse(Builder builder, String value) {
			Matcher m = regex.matcher(value);
			if (!m.find()) {
				return false;
			}

			boolean offsetPositive = false;
			Integer offsetHour = null, offsetMinute = null;
			for (int i = 0; i < componentIndexes.length; i++) {
				Integer index = componentIndexes[i];
				if (index == null) {
					continue;
				}

				int group = i + 1;
				String groupStr = m.group(group);
				if (groupStr != null) {
					boolean startsWithPlus = groupStr.startsWith("+");
					if (startsWithPlus) {
						groupStr = groupStr.substring(1);
					}

					int component = Integer.parseInt(groupStr);
					if (index == TIMEZONE_HOUR) {
						offsetHour = component;
						offsetPositive = startsWithPlus;
						continue;
					}
					if (index == TIMEZONE_MINUTE) {
						offsetMinute = component;
						continue;
					}
					builder.components[index] = component;
				}
			}

			if (offsetHour != null) {
				if (offsetMinute == null) {
					offsetMinute = 0;
				}
				builder.offset = new UtcOffset(offsetPositive, offsetHour, offsetMinute);
			}
			return true;
		}
	}

	/**
	 * Constructs instances of the {@link PartialDate} class.
	 * @author Michael Angstadt
	 */
	public static class Builder {
		private final Integer[] components;
		private UtcOffset offset;

		public Builder() {
			components = new Integer[6];
		}

		/**
		 * @param original the partial date to copy
		 */
		public Builder(PartialDate original) {
			components = Arrays.copyOf(original.components, original.components.length);
			offset = original.offset;
		}

		/**
		 * Sets the year component.
		 * @param year the year
		 * @return this
		 */
		public Builder year(Integer year) {
			components[YEAR] = year;
			return this;
		}

		/**
		 * Sets the month component.
		 * @param month the month (1-12)
		 * @return this
		 * @throws IllegalArgumentException if the month is not between 1 and 12
		 * inclusive
		 */
		public Builder month(Integer month) {
			if (month != null && (month < 1 || month > 12)) {
				throw Messages.INSTANCE.getIllegalArgumentException(37, "Month", 1, 12);
			}

			components[MONTH] = month;
			return this;
		}

		/**
		 * Sets the date component.
		 * @param date the date
		 * @return this
		 * @throws IllegalArgumentException if the date is not between 1 and 31
		 * inclusive
		 */
		public Builder date(Integer date) {
			if (date != null && (date < 1 || date > 31)) {
				throw Messages.INSTANCE.getIllegalArgumentException(37, "Date", 1, 31);
			}

			components[DATE] = date;
			return this;
		}

		/**
		 * Sets the hour component.
		 * @param hour the hour
		 * @return this
		 * @throws IllegalArgumentException if the hour is not between 0 and 23
		 * inclusive
		 */
		public Builder hour(Integer hour) {
			if (hour != null && (hour < 0 || hour > 23)) {
				throw Messages.INSTANCE.getIllegalArgumentException(37, "Hour", 0, 23);
			}

			components[HOUR] = hour;
			return this;
		}

		/**
		 * Sets the minute component.
		 * @param minute the minute
		 * @return this
		 * @throws IllegalArgumentException if the minute is not between 0 and
		 * 59 inclusive
		 */
		public Builder minute(Integer minute) {
			if (minute != null && (minute < 0 || minute > 59)) {
				throw Messages.INSTANCE.getIllegalArgumentException(37, "Minute", 0, 59);
			}

			components[MINUTE] = minute;
			return this;
		}

		/**
		 * Sets the second component.
		 * @param second the second
		 * @return this
		 * @throws IllegalArgumentException if the second is not between 0 and
		 * 59 inclusive
		 */
		public Builder second(Integer second) {
			if (second != null && (second < 0 || second > 59)) {
				throw Messages.INSTANCE.getIllegalArgumentException(37, "Second", 0, 59);
			}

			components[SECOND] = second;
			return this;
		}

		/**
		 * Sets the timezone offset.
		 * @param offset the timezone offset
		 * @return this
		 */
		public Builder offset(UtcOffset offset) {
			this.offset = offset;
			return this;
		}

		/**
		 * Builds the {@link PartialDate} object.
		 * @return the {@link PartialDate} object
		 * @throws IllegalArgumentException if the year and date are defined,
		 * but the month is not, or if the hour and second are defined, but the
		 * minute is not
		 */
		public PartialDate build() {
			if (components[YEAR] != null && components[MONTH] == null && components[DATE] != null) {
				throw Messages.INSTANCE.getIllegalArgumentException(38);
			}
			if (components[HOUR] != null && components[MINUTE] == null && components[SECOND] != null) {
				throw Messages.INSTANCE.getIllegalArgumentException(39);
			}

			return new PartialDate(components, offset);
		}
	}
}
