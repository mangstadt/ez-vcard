package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarnings;
import ezvcard.ValidationWarnings.WarningsGroup;
import ezvcard.types.VCardType;

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
 * Utility classes for unit tests.
 * @author Michael Angstadt
 */
public class TestUtils {
	/**
	 * Asserts that a warnings list is a certain size.
	 * @param expectedSize the expected size of the warnings list
	 * @param warnings the warnings list
	 */
	public static void assertWarnings(int expectedSize, List<String> warnings) {
		assertEquals(warnings.toString(), expectedSize, warnings.size());
	}

	/**
	 * Asserts that a warnings list is a certain size.
	 * @param message a message to print if the test fails
	 * @param expectedSize the expected size of the warnings list
	 * @param warnings the warnings list
	 */
	public static void assertWarnings(String message, int expectedSize, List<String> warnings) {
		assertEquals(message + " " + warnings.toString(), expectedSize, warnings.size());
	}

	/**
	 * Asserts the sizes of each warnings list within a list of warnings lists.
	 * @param warningsLists the list of warnings lists
	 * @param expectedSizes the expected sizes of each warnings list
	 */
	public static void assertWarningsLists(List<List<String>> warningsLists, int... expectedSizes) {
		assertEquals(warningsLists.toString(), expectedSizes.length, warningsLists.size());

		for (int i = 0; i < expectedSizes.length; i++) {
			int expectedSize = expectedSizes[i];
			List<String> warnings = warningsLists.get(i);

			assertWarnings(expectedSize, warnings);
		}
	}

	/**
	 * Asserts that a validation warnings list is correct.
	 * @param warnings the warnings list
	 * @param expectedProperties the property objects that are expected to have
	 * warnings. The object should be added multiple times to this vararg
	 * parameter, depending on how many warnings it is expected to have (e.g. 3
	 * times for 3 warnings)
	 */
	public static void assertValidate(ValidationWarnings warnings, VCardType... expectedProperties) {
		Counts<VCardType> expectedCounts = new Counts<VCardType>();
		for (VCardType expectedProperty : expectedProperties) {
			expectedCounts.increment(expectedProperty);
		}

		Counts<Object> actualCounts = new Counts<Object>();
		for (WarningsGroup warning : warnings) {
			assertTrue(warning.getMessages().size() > 0);
			for (int i = 0; i < warning.getMessages().size(); i++) {
				VCardType property = warning.getProperty();
				actualCounts.increment(property);
			}
		}

		assertEquals(warnings.toString(), expectedCounts, actualCounts);
	}

	/**
	 * Asserts the validation of a property object.
	 * @param property the property object
	 * @return the validation checker object
	 */
	public static ValidateChecker assertValidate(VCardType property) {
		return new ValidateChecker(property);
	}

	public static class ValidateChecker {
		private final VCardType property;
		private VCard vcard;
		private VCardVersion versions[] = VCardVersion.values();

		public ValidateChecker(VCardType property) {
			this.property = property;
			vcard(new VCard());
		}

		/**
		 * Defines the versions to check (defaults to all versions).
		 * @param versions the versions to check
		 * @return this
		 */
		public ValidateChecker versions(VCardVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Defines the vCard instance to use (defaults to an empty vCard).
		 * @param vcard the vCard instance
		 * @return this
		 */
		public ValidateChecker vcard(VCard vcard) {
			vcard.addType(property);
			this.vcard = vcard;
			return this;
		}

		/**
		 * Performs the validation check.
		 * @param expected the expected number of validation warnings
		 */
		public void run(int expected) {
			for (VCardVersion version : versions) {
				assertWarnings("Version " + version, expected, property.validate(version, vcard));
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
	 * Asserts the value of a single-valued jCard value.
	 * @param expectedDataType the expected data type
	 * @param expectedValue the expected value
	 * @param actualValue the actual jCard value
	 */
	public static void assertJCardValue(VCardDataType expectedDataType, String expectedValue, JCardValue actualValue) {
		assertEquals(expectedDataType, actualValue.getDataType());

		List<JsonValue> expected = Arrays.asList(new JsonValue(expectedValue));
		assertEquals(expected, actualValue.getValues());
	}

	/**
	 * Asserts the contents of a set.
	 * @param actualSet the actual set
	 * @param expectedElements the elements that are expected to be in the set
	 */
	public static <T> void assertSetEquals(Set<T> actualSet, T... expectedElements) {
		Set<T> expectedSet = new HashSet<T>(expectedElements.length);
		for (T expectedElement : expectedElements) {
			expectedSet.add(expectedElement);
		}
		assertEquals(expectedSet, actualSet);
	}

	/**
	 * Holds a list of test runs for a unit test.
	 */
	public static class Tests implements Iterable<Object[]> {
		private List<Object[]> tests = new ArrayList<Object[]>();

		/**
		 * Adds a test run.
		 * @param test the test data
		 * @return this
		 */
		public Tests add(Object... test) {
			tests.add(test);
			return this;
		}

		public Iterator<Object[]> iterator() {
			return tests.iterator();
		}
	}

	/**
	 * Keeps a count of how many identical instances of an object there are.
	 */
	private static class Counts<T> {
		private final Map<T, Integer> map = new HashMap<T, Integer>();

		public void increment(T t) {
			Integer value = getCount(t);
			map.put(t, value++);
		}

		public Integer getCount(T t) {
			Integer value = map.get(t);
			return (value == null) ? 0 : value;
		}

		@Override
		public int hashCode() {
			return map.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Counts<?> other = (Counts<?>) obj;
			return map.equals(other.map);
		}

		@Override
		public String toString() {
			return map.toString();
		}
	}

	/**
	 * Creates an array from the given vararg parameters (syntax is less verbose
	 * than creating an array the normal way).
	 * @param values the values
	 * @return the array
	 */
	public static <T> T[] each(T... values) {
		return values;
	}

	private TestUtils() {
		//hide
	}
}
