package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.ValidationWarnings;
import ezvcard.io.ParseWarning;
import ezvcard.io.StreamReader;
import ezvcard.property.VCardProperty;

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
 * Utility classes for unit tests.
 * @author Michael Angstadt
 */
public class TestUtils {
	/**
	 * Tests the version assigned to a {@link VCard} object.
	 * @param expected the expected version
	 * @param vcard the vCard object
	 */
	public static void assertVersion(VCardVersion expected, VCard vcard) {
		VCardVersion actual = vcard.getVersion();
		assertEquals(expected, actual);
	}

	/**
	 * Tests how many properties are in a vCard.
	 * @param expected the expected number of properties
	 * @param vcard the vCard
	 */
	public static void assertPropertyCount(int expected, VCard vcard) {
		int actual = vcard.getProperties().size();
		assertEquals(expected, actual);
	}

	/**
	 * Tests to make sure there are no more vCards on a data stream.
	 * @param reader the data stream
	 */
	public static void assertNoMoreVCards(StreamReader reader) {
		try {
			assertNull(reader.readNext());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Asserts a list of parse warnings.
	 * @param warnings the parse warnings
	 * @param expectedCodes the expected warning codes (order does not matter,
	 * use "null" for warnings that do not have a code)
	 */
	public static void assertParseWarnings(List<ParseWarning> warnings, Integer... expectedCodes) {
		List<Integer> expectedWarnings = new ArrayList<Integer>(Arrays.asList(expectedCodes));
		List<Integer> actualWarnings = new ArrayList<Integer>(warnings.size());
		for (ParseWarning warning : warnings) {
			actualWarnings.add(warning.getCode());
		}

		for (Integer actualWarning : actualWarnings) {
			if (!expectedWarnings.remove(actualWarning)) {
				fail("Expected these warnings " + expectedWarnings + ", but was this: " + actualWarnings + ".  Actual warnings: " + warnings);
			}
		}
	}

	/**
	 * Asserts the parse warnings of a {@link StreamReader}.
	 * @param reader the {@link StreamReader}
	 * @param expectedCodes the expected warning codes (order does not matter,
	 * use "null" for warnings that do not have a code)
	 */
	public static void assertParseWarnings(StreamReader reader, Integer... expectedCodes) {
		assertParseWarnings(reader.getWarnings(), expectedCodes);
	}

	/**
	 * Asserts that a list is a certain size.
	 * @param expectedSize the expected size of the list
	 * @param list the list
	 */
	public static void assertListSize(int expectedSize, List<?> list) {
		int actualSize = list.size();
		assertEquals(list.toString(), expectedSize, actualSize);
	}

	public static boolean checkCodes(List<ValidationWarning> warnings, Integer... expectedCodes) {
		if (warnings.size() != expectedCodes.length) {
			return false;
		}

		/*
		 * Don't use a Set because there can be multiple warnings with the same
		 * code.
		 */
		List<Integer> expected = new ArrayList<Integer>(Arrays.asList(expectedCodes));

		for (ValidationWarning warning : warnings) {
			Integer code = warning.getCode();
			boolean removed = expected.remove((Object) code);
			if (!removed) {
				return false;
			}
		}
		return true;
	}

	public static void assertValidate(List<ValidationWarning> warnings, Integer... expectedCodes) {
		boolean passed = checkCodes(warnings, expectedCodes);
		if (!passed) {
			fail("Expected codes were " + Arrays.toString(expectedCodes) + " but were actually:\n" + warnings);
		}
	}

	public static VCardValidateChecker assertValidate(VCard vcard) {
		return new VCardValidateChecker(vcard);
	}

	public static class VCardValidateChecker {
		private final VCard vcard;
		private VCardVersion versions[] = VCardVersion.values();
		private Map<VCardProperty, Integer[]> expectedPropCodes = new HashMap<VCardProperty, Integer[]>();

		public VCardValidateChecker(VCard vcard) {
			this.vcard = vcard;
		}

		/**
		 * Defines the versions to check (defaults to all versions).
		 * @param versions the versions to check
		 * @return this
		 */
		public VCardValidateChecker versions(VCardVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Assigns the warning codes that a property is expected to generate.
		 * @param property the property or null to represent the vCard warnings
		 * @param expectedCodes the expected warning codes
		 * @return this
		 */
		public VCardValidateChecker prop(VCardProperty property, Integer... expectedCodes) {
			expectedPropCodes.put(property, expectedCodes);
			return this;
		}

		/**
		 * Performs the validation check.
		 */
		public void run() {
			for (VCardVersion version : versions) {
				Map<VCardProperty, Integer[]> expectedPropCodes = new HashMap<VCardProperty, Integer[]>(this.expectedPropCodes);
				ValidationWarnings warnings = vcard.validate(version);
				for (Map.Entry<VCardProperty, List<ValidationWarning>> entry : warnings) {
					VCardProperty property = entry.getKey();
					List<ValidationWarning> actualWarnings = entry.getValue();

					Integer[] expectedCodes = expectedPropCodes.remove(property);
					if (expectedCodes == null) {
						String className = (property == null) ? "vCard" : property.getClass().getSimpleName();
						fail("For version " + version + ", " + className + " had " + actualWarnings.size() + " warnings, but none were expected.  Actual warnings:\n" + warnings);
					}

					boolean passed = checkCodes(actualWarnings, expectedCodes);
					if (!passed) {
						fail("For version " + version + ", expected validation warnings did not match actual warnings.  Actual warnings:\n" + warnings);
					}
				}

				if (!expectedPropCodes.isEmpty()) {
					List<String> lines = new ArrayList<String>();
					for (Map.Entry<VCardProperty, Integer[]> entry : expectedPropCodes.entrySet()) {
						VCardProperty property = entry.getKey();
						String className = (property == null) ? null : property.getClass().getSimpleName();
						Integer[] expectedCodes = entry.getValue();
						lines.add(className + ": " + Arrays.toString(expectedCodes));
					}
					fail("For version " + version + ", the following validation warnings were expected, but NOT thrown:\n" + lines + "\nActual warnings:\n" + warnings);
				}
			}
		}
	}

	/**
	 * Asserts the value of an {@link Integer} object.
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertIntEquals(int expected, Integer actual) {
		assertEquals(Integer.valueOf(expected), actual);
	}

	/**
	 * Asserts the contents of a collection. Does not check for order.
	 * @param actual the actual collection
	 * @param expectedElements the elements that are expected to be in the
	 * collection (order does not matter)
	 */
	public static <T> void assertCollectionContains(Collection<T> actual, T... expectedElements) {
		assertEquals(expectedElements.length, actual.size());

		Collection<T> actualCopy = new ArrayList<T>(actual);
		for (T expectedElement : expectedElements) {
			assertTrue("Collection did not contain: " + expectedElement, actualCopy.remove(expectedElement));
		}
	}

	/**
	 * Builds a timezone object with the given offset.
	 * @param hours the hour offset
	 * @param minutes the minute offset
	 * @return the timezone object
	 */
	public static TimeZone buildTimezone(int hours, int minutes) {
		int hourMillis = 1000 * 60 * 60 * hours;

		int minuteMillis = 1000 * 60 * minutes;
		if (hours < 0) {
			minuteMillis *= -1;
		}

		return new SimpleTimeZone(hourMillis + minuteMillis, "");
	}

	/**
	 * Asserts the contents of a set.
	 * @param actualSet the actual set
	 * @param expectedElements the elements that are expected to be in the set
	 */
	public static <T> void assertSetEquals(Set<T> actualSet, T... expectedElements) {
		Set<T> expectedSet = new HashSet<T>(expectedElements.length);
		expectedSet.addAll(Arrays.asList(expectedElements));
		assertEquals(expectedSet, actualSet);
	}

	//@formatter:off
	private static DateFormat dfs[] = new DateFormat[]{
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z"),
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
		new SimpleDateFormat("yyyy-MM-dd")
	};
	//@formatter:on

	/**
	 * <p>
	 * Creates a {@link Date} object.
	 * </p>
	 * <p>
	 * The following date string formats are accepted.
	 * </p>
	 * <ul>
	 * <li>yyyy-MM-dd</li>
	 * <li>yyyy-MM-dd HH:mm:ss</li>
	 * <li>yyyy-MM-dd HH:mm:ss Z</li>
	 * </ul>
	 * <p>
	 * If no UTC offset is specified, the default timezone will be used.
	 * </p>
	 * @param text the date string to parse
	 * @return the parsed date
	 * @throws IllegalArgumentException if it couldn't be parsed
	 */
	public static Date date(String text) {
		return date(text, TimeZone.getDefault());
	}

	/**
	 * <p>
	 * Creates a {@link Date} object.
	 * </p>
	 * <p>
	 * The following date string formats are accepted.
	 * </p>
	 * <ul>
	 * <li>yyyy-MM-dd</li>
	 * <li>yyyy-MM-dd HH:mm:ss</li>
	 * <li>yyyy-MM-dd HH:mm:ss Z</li>
	 * </ul>
	 * @param text the date string
	 * @param timezone the timezone the date string is in (ignored if the date
	 * string contains a UTC offset)
	 * @return the parsed date
	 * @throws IllegalArgumentException if it couldn't be parsed
	 */
	public static Date date(String text, TimeZone timezone) {
		for (DateFormat df : dfs) {
			try {
				df.setTimeZone(timezone);
				return df.parse(text);
			} catch (ParseException e) {
				//try the next date formatter
			}
		}
		throw new IllegalArgumentException("Invalid date string: " + text);
	}

	/**
	 * Creates a {@link Calendar} object using the local system's default
	 * timezone.
	 * @param year the year
	 * @param month the month (0-11)
	 * @param dayOfMonth the day of month
	 * @param hourOfDay the hour of day (0-23)
	 * @param minute the minute
	 * @param second the second
	 * @return the calendar
	 */
	public static Calendar date(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
		return date(year, month, dayOfMonth, hourOfDay, minute, second, TimeZone.getDefault());
	}

	/**
	 * Creates a {@link Calendar} object with UTC timezone.
	 * @param year the year
	 * @param month the month (0-11)
	 * @param dayOfMonth the day of month
	 * @param hourOfDay the hour of day (0-23)
	 * @param minute the minute
	 * @param second the second
	 * @return the calendar
	 */
	public static Calendar utc(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
		return date(year, month, dayOfMonth, hourOfDay, minute, second, TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Creates a {@link Calendar} object.
	 * @param year the year
	 * @param month the month (0-11)
	 * @param dayOfMonth the day of month
	 * @param hourOfDay the hour of day (0-23)
	 * @param minute the minute
	 * @param second the second
	 * @param hourOffset the timezone's offset in hours
	 * @return the calendar
	 */
	public static Calendar date(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int hourOffset) {
		return date(year, month, dayOfMonth, hourOfDay, minute, second, gmtTz(TimeUnit.HOURS.toMillis(hourOffset)));
	}

	/**
	 * Creates a GMT timezone.
	 * @param offsetMillis the offset in milliseconds
	 * @return the timezone
	 */
	public static TimeZone gmtTz(long offsetMillis) {
		long totalMinutes = Math.abs(offsetMillis / 1000 / 60);
		long hours = totalMinutes / 60;
		long minutes = totalMinutes % 60;

		StringBuilder tzid = new StringBuilder("GMT");
		tzid.append((offsetMillis >= 0) ? '+' : '-');

		if (hours < 10) {
			tzid.append('0');
		}
		tzid.append(hours);

		tzid.append(':');

		if (minutes < 10) {
			tzid.append('0');
		}
		tzid.append(minutes);

		return TimeZone.getTimeZone(tzid.toString());
	}

	/**
	 * Creates a {@link Calendar} object.
	 * @param year the year
	 * @param month the month (0-11)
	 * @param dayOfMonth the day of month
	 * @param hourOfDay the hour of day (0-23)
	 * @param minute the minute
	 * @param second the second
	 * @param tz the timezone
	 * @return the calendar
	 */
	public static Calendar date(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, TimeZone tz) {
		Calendar c = Calendar.getInstance(tz);
		c.clear();
		c.set(year, month, dayOfMonth, hourOfDay, minute, second);
		return c;
	}

	/**
	 * Creates a {@link Calendar} object.
	 * @param year the year
	 * @param month the month (0-11)
	 * @param dayOfMonth the day of month
	 * @return the calendar
	 */
	public static Calendar date(int year, int month, int dayOfMonth) {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(year, month, dayOfMonth);
		return c;
	}

	/**
	 * Creates a {@link Date} object.
	 * @param text the date string (e.g. "2000-01-30 02:21:00", see code for
	 * acceptable formats)
	 * @return the parsed date in the UTC timezone or null if it couldn't be
	 * parsed
	 * @throws IllegalArgumentException if it couldn't be parsed
	 */
	public static Date utc(String text) {
		return date(text + " +0000");
	}

	/**
	 * <p>
	 * Asserts some of the basic rules for the equals() method:
	 * </p>
	 * <ul>
	 * <li>The same object instance is equal to itself.</li>
	 * <li>Passing {@code null} into the method returns false.</li>
	 * <li>Passing an instance of a different class into the method returns
	 * false.</li>
	 * </ul>
	 * @param object an instance of the class to test.
	 */
	public static void assertEqualsMethodEssentials(Object object) {
		assertEquals(object, object);
		assertFalse(object.equals(null));
		assertFalse(object.equals("other class"));
	}

	/**
	 * Asserts that two objects are equal according to their equals() method.
	 * Also asserts that their hash codes are the same.
	 * @param one the first object
	 * @param two the second object
	 */
	public static void assertEqualsAndHash(Object one, Object two) {
		assertEquals(one, two);
		assertEquals(two, one);
		assertEquals(one.hashCode(), two.hashCode());
	}

	/**
	 * Asserts that calling {@code one.equals(two)} and {@code two.equals(one)}
	 * will both return false.
	 * @param one the first object
	 * @param two the second object
	 */
	public static void assertNotEqualsBothWays(Object one, Object two) {
		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	/**
	 * Asserts that none of the given objects are equal to each other.
	 * @param objects the objects
	 */
	public static void assertNothingIsEqual(Object... objects) {
		assertNothingIsEqual(Arrays.asList(objects));
	}

	/**
	 * Asserts that none of the given objects are equal to each other.
	 * @param objects the objects
	 */
	public static void assertNothingIsEqual(Iterable<Object> objects) {
		for (Object object1 : objects) {
			for (Object object2 : objects) {
				if (object1 != object2) {
					assertNotEquals("Objects should not be equal:\n" + object1 + "\n" + object2, object1, object2);
				}
			}
		}
	}

	public static <T> T[] each(T... t) {
		return t;
	}

	private TestUtils() {
		//hide
	}
}
