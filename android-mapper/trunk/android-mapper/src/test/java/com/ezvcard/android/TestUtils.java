package com.ezvcard.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.property.VCardProperty;

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
		 * @param expected the expected number of warnings
		 */
		public void run(int expected) {
			for (VCardVersion version : versions) {
				List<Warning> warnings = property.validate(version, vcard);
				assertEquals(expected, warnings.size());
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

	private TestUtils() {
		//hide
	}
}
