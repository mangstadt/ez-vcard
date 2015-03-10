package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarnings;
import ezvcard.Warning;
import ezvcard.io.StreamReader;
import ezvcard.property.VCardProperty;

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
	 * Asserts that a warnings list is a certain size.
	 * @param expectedSize the expected size of the warnings list
	 * @param warnings the warnings list
	 */
	public static void assertWarnings(int expectedSize, List<String> warnings) {
		assertEquals(warnings.toString(), expectedSize, warnings.size());
	}

	/**
	 * Asserts that a StreamReader's warnings list is a certain size.
	 * @param expectedSize the expected size of the warnings list
	 * @param reader the reader
	 */
	public static void assertWarnings(int expectedSize, StreamReader reader) {
		assertWarnings(expectedSize, reader.getWarnings());
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

	private static boolean checkCodes(List<Warning> warnings, Integer... expectedCodes) {
		if (warnings.size() != expectedCodes.length) {
			return false;
		}

		List<Integer> actualCodes = new ArrayList<Integer>(); //don't use a Set because there can be multiple warnings with the same code
		for (Warning warning : warnings) {
			actualCodes.add(warning.getCode());
		}

		for (Integer code : expectedCodes) {
			boolean found = actualCodes.remove((Object) code);
			if (!found) {
				return false;
			}
		}

		return true;
	}

	public static void assertValidate(List<Warning> warnings, Integer... expectedCodes) {
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
				ValidationWarnings warnings = vcard.validate(version);
				for (Map.Entry<VCardProperty, List<Warning>> entry : warnings) {
					VCardProperty property = entry.getKey();
					List<Warning> actualWarnings = entry.getValue();

					Integer[] expectedCodes = expectedPropCodes.get(property);
					if (expectedCodes == null) {
						String className = (property == null) ? "vCard" : property.getClass().getSimpleName();
						fail("For version " + version + ", " + className + " had " + actualWarnings.size() + " warnings, but none were expected.  Actual warnings:\n" + warnings);
					}

					boolean passed = checkCodes(actualWarnings, expectedCodes);
					if (!passed) {
						fail("For version " + version + ", expected validation warnings did not match actual warnings.  Actual warnings:\n" + warnings);
					}
				}
			}
		}
	}

	/**
	 * Asserts the validation of a property object.
	 * @param property the property object
	 * @return the validation checker object
	 */
	public static PropValidateChecker assertValidate(VCardProperty property) {
		return new PropValidateChecker(property);
	}

	public static class PropValidateChecker {
		private final VCardProperty property;
		private VCard vcard;
		private VCardVersion versions[] = VCardVersion.values();

		public PropValidateChecker(VCardProperty property) {
			this.property = property;
			vcard(new VCard());
		}

		/**
		 * Defines the versions to check (defaults to all versions).
		 * @param versions the versions to check
		 * @return this
		 */
		public PropValidateChecker versions(VCardVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Defines the vCard instance to use (defaults to an empty vCard).
		 * @param vcard the vCard instance
		 * @return this
		 */
		public PropValidateChecker vcard(VCard vcard) {
			vcard.addProperty(property);
			this.vcard = vcard;
			return this;
		}

		/**
		 * Performs the validation check.
		 * @param expectedCodes the expected warning codes
		 */
		public void run(Integer... expectedCodes) {
			for (VCardVersion version : versions) {
				List<Warning> warnings = property.validate(version, vcard);
				boolean passed = checkCodes(warnings, expectedCodes);
				if (!passed) {
					fail("For version " + version + ", expected codes were " + Arrays.toString(expectedCodes) + " but were actually:\n" + warnings);
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
		for (T expectedElement : expectedElements) {
			expectedSet.add(expectedElement);
		}
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
	 * Creates a {@link Date} object.
	 * @param text the date string (e.g. "2000-01-30", see code for acceptable
	 * formats)
	 * @return the parsed date or null if it couldn't be parsed
	 * @throws IllegalArgumentExcpetion if it couldn't be parsed
	 */
	public static Date date(String text) {
		return date(text, TimeZone.getDefault());
	}

	/**
	 * Creates a {@link Date} object.
	 * @param text the date string (e.g. "2000-01-30", see code for acceptable
	 * formats)
	 * @param timezone the timezone the date string is in
	 * @return the parsed date
	 * @throws IllegalArgumentExcpetion if it couldn't be parsed
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
	 * Creates a {@link Date} object.
	 * @param text the date string (e.g. "2000-01-30 02:21:00", see code for
	 * acceptable formats)
	 * @return the parsed date in the UTC timezone or null if it couldn't be
	 * parsed
	 * @throws IllegalArgumentExcpetion if it couldn't be parsed
	 */
	public static Date utc(String text) {
		return date(text + " +0000");
	}

	public static <T> T[] each(T... t) {
		return t;
	}

	private TestUtils() {
		//hide
	}
}
