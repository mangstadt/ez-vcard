package com.ezvcard.android;

import static com.ezvcard.android.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.VCardParameters;
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
 */

/**
 * Utility class used for unit testing property marshallers.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public class Sensei<T extends VCardProperty> {
	private final VCardPropertyScribe<T> scribe;

	/**
	 * Shorthand for creating a new instance of this class.
	 * @param scribe the property scribe
	 * @return the new instance
	 */
	public static <T extends VCardProperty> Sensei<T> create(VCardPropertyScribe<T> scribe) {
		return new Sensei<T>(scribe);
	}

	/**
	 * Creates a new sensei.
	 * @param scribe the property scribe
	 */
	public Sensei(VCardPropertyScribe<T> scribe) {
		this.scribe = scribe;
	}

	/**
	 * Asserts the {@link VCardPropertyScribe#parseText} method.
	 * @param value the value to parse
	 * @return the tester object
	 */
	public ParseTextTest assertParseText(String value) {
		return new ParseTextTest(value);
	}

	/**
	 * Asserts the {@link VCardPropertyScribe#dataType} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public DataTypeTest assertDataType(T property) {
		return new DataTypeTest(property);
	}

	/**
	 * Asserts the {@link VCardPropertyScribe#prepareParameters} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public PrepareParamsTest assertPrepareParams(T property) {
		return new PrepareParamsTest(property);
	}

	/**
	 * Asserts the {@link VCardPropertyScribe#writeText} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public WriteTextTest assertWriteText(T property) {
		return new WriteTextTest(property);
	}

	/**
	 * Tester class used for testing the {@link VCardPropertyScribe#dataType}
	 * method.
	 */
	public class DataTypeTest {
		protected final T property;
		private VCardVersion version[] = VCardVersion.values();

		public DataTypeTest(T property) {
			this.property = property;
		}

		/**
		 * Sets the versions to run this test under (defaults to all versions).
		 * @param version the versions
		 * @return this
		 */
		public DataTypeTest versions(VCardVersion... version) {
			this.version = version;
			return this;
		}

		/**
		 * Runs the test.
		 * @param expected the expected data type
		 */
		public void run(VCardDataType expected) {
			for (VCardVersion version : this.version) {
				VCardDataType actual = scribe.dataType(property, version);
				assertEquals("Version " + version, expected, actual);
			}
		}
	}

	/**
	 * Tester class used for testing the
	 * {@link VCardPropertyScribe#prepareParameters} method.
	 */
	public class PrepareParamsTest {
		protected final T property;
		private VCardVersion versions[] = VCardVersion.values();
		private VCard vcard = new VCard();
		private VCardParameters expected = new VCardParameters();

		public PrepareParamsTest(T property) {
			this.property = property;
		}

		/**
		 * Sets the versions to run this test under (defaults to all versions).
		 * @param versions the versions
		 * @return this
		 */
		public PrepareParamsTest versions(VCardVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Sets the {@link VCard} object to use (defaults to an empty vCard).
		 * @param vcard the vCard
		 * @return this
		 */
		public PrepareParamsTest vcard(VCard vcard) {
			this.vcard = vcard;
			return this;
		}

		/**
		 * Adds an expected parameter.
		 * @param name the parameter name
		 * @param values the parameter value
		 * @return this
		 */
		public PrepareParamsTest expected(String name, String... values) {
			for (String value : values) {
				expected.put(name, value);
			}
			return this;
		}

		/**
		 * Runs the test.
		 */
		public void run() {
			for (VCardVersion version : versions) {
				VCardParameters actual = scribe.prepareParameters(property, version, vcard);
				assertEquals("Actual: " + actual, expected.size(), actual.size());

				for (Map.Entry<String, List<String>> entry : expected) {
					String expectedKey = entry.getKey();
					List<String> expectedValues = entry.getValue();

					List<String> actualValues = actual.get(expectedKey);
					assertEquals("Actual: " + actual, expectedValues.size(), actualValues.size());
					for (String expectedValue : expectedValues) {
						assertTrue("Actual: " + actual, actualValues.contains(expectedValue));
					}
				}
			}
		}
	}

	/**
	 * Tester class used for testing the {@link VCardPropertyScribe#writeText}
	 * method.
	 */
	public class WriteTextTest {
		protected final T property;
		private VCardVersion versions[] = VCardVersion.values();

		public WriteTextTest(T property) {
			this.property = property;
		}

		/**
		 * Sets the versions to run this test under (defaults to all versions).
		 * @param versions the versions
		 * @return this
		 */
		public WriteTextTest versions(VCardVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Runs the test, expecting a {@link SkipMeException} to be thrown.
		 */
		public void skipMe() {
			run(SkipMeException.class);
		}

		/**
		 * Runs the test.
		 * @param expected the expected property value
		 */
		public void run(String expected) {
			for (VCardVersion version : versions) {
				String actual = scribe.writeText(property, version);
				assertEquals("Version " + version, expected, actual);
			}
		}

		public void run(Class<? extends RuntimeException> expected) {
			for (VCardVersion version : versions) {
				try {
					scribe.writeText(property, version);
					fail("Expected " + expected.getSimpleName());
				} catch (RuntimeException t) {
					assertEquals("Expected " + expected.getSimpleName() + ", but was " + t.getClass().getSimpleName(), expected, t.getClass());
				}
			}
		}
	}

	/**
	 * Parent class for the parser testers.
	 */
	private abstract class ParseTest<U extends ParseTest<U>> {
		protected VCardParameters parameters = new VCardParameters();
		protected int warnings = 0;

		@SuppressWarnings("unchecked")
		private final U this_ = (U) this;

		/**
		 * Adds a parameter.
		 * @param name the parameter name
		 * @param value the parameter value
		 * @return this
		 */
		public U param(String name, String value) {
			parameters.put(name, value);
			return this_;
		}

		/**
		 * Sets the parameters.
		 * @param parameters the parameters
		 * @return this
		 */
		public U params(VCardParameters parameters) {
			this.parameters = parameters;
			return this_;
		}

		/**
		 * Sets the expected number of warnings (defaults to "0").
		 * @param warnings the expected number of warnings
		 * @return this
		 */
		public U warnings(int warnings) {
			this.warnings = warnings;
			return this_;
		}

		/**
		 * Runs the test, without testing the returned property object.
		 */
		public void run() {
			run(null, null);
		}

		/**
		 * Runs the test, expecting a {@link CannotParseException} to be thrown.
		 */
		public void cannotParse() {
			run(null, CannotParseException.class);
		}

		/**
		 * Runs the test, expecting a {@link CannotParseException} to be thrown.
		 */
		public void skipMe() {
			run(null, SkipMeException.class);
		}

		/**
		 * Runs the test.
		 * @param check object for validating the parsed property object
		 */
		public void run(Check<T> check) {
			run(check, null);
		}

		/**
		 * Runs the test.
		 * @param check object for validating the parsed property object or null
		 * not to validate the property
		 * @param exception the exception that is expected to be thrown or null
		 * if no exception is expected
		 */
		protected abstract void run(Check<T> check, Class<? extends RuntimeException> exception);
	}

	/**
	 * Tester class used for testing the {@link VCardPropertyScribe#parseText}
	 * method.
	 */
	public class ParseTextTest extends ParseTest<ParseTextTest> {
		private final String value;
		private VCardDataType dataType;
		private VCardVersion versions[] = VCardVersion.values();

		/**
		 * @param value the text to parse
		 */
		public ParseTextTest(String value) {
			this.value = value;
		}

		/**
		 * Sets the data type (defaults to the property's default data type)
		 * @param dataType the data type
		 * @return this
		 */
		public ParseTextTest dataType(VCardDataType dataType) {
			this.dataType = dataType;
			return this;
		}

		/**
		 * Sets the versions to run this test under (defaults to all versions).
		 * @param versions the versions
		 * @return this
		 */
		public ParseTextTest versions(VCardVersion... versions) {
			this.versions = versions;
			return this;
		}

		@Override
		protected void run(Check<T> check, Class<? extends RuntimeException> exception) {
			for (VCardVersion version : this.versions) {
				VCardDataType dataType = this.dataType;
				if (dataType == null) {
					dataType = scribe.defaultDataType(version);
				}

				try {
					Result<T> result = scribe.parseText(value, dataType, version, parameters);
					if (exception != null) {
						fail("Expected " + exception.getSimpleName() + " to be thrown.");
					}
					if (check != null) {
						check.check(result.getProperty());
					}
					assertWarnings(warnings, result.getWarnings());
				} catch (RuntimeException t) {
					if (exception == null) {
						throw t;
					}
					assertEquals(exception, t.getClass());
				}
			}
		}
	}

	/**
	 * Used for validating the contents of a parsed property object.
	 * @param <T> the property class
	 */
	public static interface Check<T extends VCardProperty> {
		/**
		 * Validates the contents of the parsed property object.
		 * @param property the parsed property object
		 */
		void check(T property);
	}
}
