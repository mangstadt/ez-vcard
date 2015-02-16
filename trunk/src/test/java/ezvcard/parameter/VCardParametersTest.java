package ezvcard.parameter;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;

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
 * @author Michael Angstadt
 */
public class VCardParametersTest {
	private VCardParameters parameters;

	@Before
	public void before() {
		parameters = new VCardParameters();
	}

	@Test
	public void validate_non_standard_values() {
		parameters.setCalscale(Calscale.get("foo"));
		parameters.setEncoding(Encoding.get("foo"));
		parameters.setValue(VCardDataType.get("foo"));

		assertValidate(parameters.validate(VCardVersion.V2_1), 3, 6, 3, 3);
		assertValidate(parameters.validate(VCardVersion.V3_0), 3, 3, 3, 6);
		assertValidate(parameters.validate(VCardVersion.V4_0), 3, 3, 3);
	}

	@Test
	public void validate_malformed_values() {
		parameters.put("GEO", "invalid");
		parameters.put("INDEX", "invalid");
		parameters.put("PREF", "invalid");
		parameters.put("PID", "invalid");

		assertValidate(parameters.validate(VCardVersion.V2_1), 5, 5, 5, 5, 6, 6, 6);
		assertValidate(parameters.validate(VCardVersion.V3_0), 5, 5, 5, 5, 6, 6, 6);
		assertValidate(parameters.validate(VCardVersion.V4_0), 5, 5, 5, 5);
	}

	@Test
	public void validate_supported_versions() {
		parameters.setAltId("value");
		parameters.setCalscale(Calscale.GREGORIAN);
		parameters.setCharset("UTF-8");
		parameters.setGeo(1, 1);
		parameters.setIndex(1);
		parameters.setLanguage("value");
		parameters.setLevel("value");
		parameters.setMediaType("value");
		parameters.setSortAs("value");
		parameters.setTimezone("value");

		assertValidate(parameters.validate(VCardVersion.V2_1), 6, 6, 6, 6, 6, 6, 6, 6);
		assertValidate(parameters.validate(VCardVersion.V3_0), 6, 6, 6, 6, 6, 6, 6, 6, 6);
		assertValidate(parameters.validate(VCardVersion.V4_0), 6);
	}

	@Test
	public void validate_value_supported_versions() {
		parameters.setEncoding(Encoding._7BIT);
		parameters.setValue(VCardDataType.CONTENT_ID);
		assertValidate(parameters.validate(VCardVersion.V2_1));
		assertValidate(parameters.validate(VCardVersion.V3_0), 4, 4);
		assertValidate(parameters.validate(VCardVersion.V4_0), 4, 4);

		parameters.setEncoding(Encoding.B);
		parameters.setValue(VCardDataType.BINARY);
		assertValidate(parameters.validate(VCardVersion.V2_1), 4, 4);
		assertValidate(parameters.validate(VCardVersion.V3_0));
		assertValidate(parameters.validate(VCardVersion.V4_0), 4, 4);

		parameters.setEncoding(null);
		parameters.setValue(VCardDataType.DATE_AND_OR_TIME);
		assertValidate(parameters.validate(VCardVersion.V2_1), 4);
		assertValidate(parameters.validate(VCardVersion.V3_0), 4);
		assertValidate(parameters.validate(VCardVersion.V4_0));
	}

	@Test
	public void validate_charset() {
		parameters.setCharset("invalid");
		assertValidate(parameters.validate(VCardVersion.V2_1), 22);
		assertValidate(parameters.validate(VCardVersion.V3_0), 6, 22);
		assertValidate(parameters.validate(VCardVersion.V4_0), 6, 22);

		parameters.setCharset("UTF-8");
		assertValidate(parameters.validate(VCardVersion.V2_1));
		assertValidate(parameters.validate(VCardVersion.V3_0), 6);
		assertValidate(parameters.validate(VCardVersion.V4_0), 6);
	}

	@Test
	public void case_insensitive() {
		//tests to make sure sanitizeKey() is implemented correctly
		//ListMultimapTest tests the rest of the get/put/remove methods
		parameters.put("NUMBERS", "1");
		assertEquals("1", parameters.first("numbers"));
	}

	@Test
	public void getPref() {
		assertNull(parameters.getPref());
		parameters.put("PREF", "1");
		assertIntEquals(1, parameters.getPref());
	}

	@Test(expected = IllegalStateException.class)
	public void getPref_malformed() {
		parameters.put("PREF", "invalid");
		parameters.getPref();
	}

	@Test
	public void setPref() {
		parameters.setPref(1);
		assertEquals("1", parameters.first("PREF"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void setPref_too_low() {
		parameters.setPref(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setPref_too_high() {
		parameters.setPref(101);
	}

	/**
	 * Make sure it handles GEO values correctly.
	 */
	@Test
	public void geo() {
		assertNull(parameters.getGeo());
		parameters.setGeo(-10.98887888, 20.12344111);

		//make sure it builds the correct text value
		{
			String expected = "geo:-10.988879,20.123441"; //it should round to 6 decimal places
			String actual = parameters.first("GEO");
			assertEquals(expected, actual);
		}

		//make sure it unmarshals the text value correctly
		{
			double[] expected = new double[] { -10.988879, 20.123441 };
			double[] actual = parameters.getGeo();
			assertArrayEquals(expected, actual, .00001);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void geo_malformed() {
		parameters.put("GEO", "invalid");
		parameters.getGeo();
	}

	/**
	 * Make sure it handles PID values correctly.
	 */
	@Test
	public void pid() {
		assertTrue(parameters.getPids().isEmpty());
		parameters.addPid(1);
		parameters.addPid(2, 1);

		//make sure it builds the correct string values
		assertEquals(Arrays.asList("1", "2.1"), parameters.get("PID"));

		//make sure it unmarshals the string values correctly
		Iterator<Integer[]> it = parameters.getPids().iterator();
		Integer[] pid = it.next();
		assertIntEquals(1, pid[0]);
		assertNull(pid[1]);
		pid = it.next();
		assertIntEquals(2, pid[0]);
		assertIntEquals(1, pid[1]);
		assertFalse(it.hasNext());
	}

	@Test(expected = IllegalStateException.class)
	public void pid_malformed() {
		parameters.put("PID", "1.1");
		parameters.put("PID", "invalid");
		parameters.getPids();
	}

	@Test
	public void getIndex() {
		assertNull(parameters.getIndex());
		parameters.put("INDEX", "1");
		assertIntEquals(1, parameters.getIndex());
	}

	@Test(expected = IllegalStateException.class)
	public void getIndex_malformed() {
		parameters.put("INDEX", "invalid");
		parameters.getIndex();
	}

	@Test
	public void setIndex() {
		parameters.setIndex(1);
		assertEquals("1", parameters.first("INDEX"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndex_negative() {
		parameters.setIndex(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndex_zero() {
		parameters.setIndex(0);
	}

	@Test
	public void sortAs() {
		assertTrue(parameters.getSortAs().isEmpty());

		parameters = new VCardParameters();
		parameters.setSortAs("one", "two");
		assertEquals(Arrays.asList("one", "two"), parameters.getSortAs());
		assertEquals(Arrays.asList("one", "two"), parameters.get("SORT-AS"));

		parameters = new VCardParameters();
		parameters.put("SORT-AS", "one");
		parameters.put("SORT-AS", "two");
		assertEquals(Arrays.asList("one", "two"), parameters.getSortAs());

		parameters = new VCardParameters();

		parameters.setSortAs("one", "three");
		assertEquals(Arrays.asList("one", "three"), parameters.getSortAs());

		parameters.setSortAs();
		assertTrue(parameters.getSortAs().isEmpty());

		parameters.setSortAs("one", "two");
		parameters.setSortAs((String[]) null);
		assertTrue(parameters.getSortAs().isEmpty());

		parameters.setSortAs("one", "two");
		parameters.setSortAs((String) null);
		assertEquals(Arrays.asList((String) null), parameters.getSortAs());
	}
}
