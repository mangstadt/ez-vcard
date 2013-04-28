package ezvcard.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

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
 * @author Michael Angstadt
 */
public class PartialDateTest {
	@Test
	public void toDateAndOrTime_date() {
		//@formatter:off
		ToDateAndOrTimeTestCase testCases[] = new ToDateAndOrTimeTestCase[]{
			ToDateAndOrTimeTestCase.test(		new Integer[]{1980, null, null}, "1980", "1980"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 4, null}, "--04", "--04"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, null, 20}, "---20", "---20"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{1980, 4, null}, "1980-04", "1980-04"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 4, 20}, "--0420", "--04-20"),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{1980, null, 20}),
			ToDateAndOrTimeTestCase.test(		new Integer[]{1980, 4, 20}, "19800420", "1980-04-20"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, null, null}, "", ""),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, -1, null}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, null, -1}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, 13, null}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, null, 32}),
		};
		//@formatter:on

		for (ToDateAndOrTimeTestCase testCase : testCases) {
			try {
				PartialDate d = PartialDate.date(testCase.params[0], testCase.params[1], testCase.params[2]);
				if (testCase.exception) {
					fail("IllegalArgumentException expected for parameters: " + Arrays.toString(testCase.params));
				}
				assertEquals("Failed basic for parameters: " + Arrays.toString(testCase.params), testCase.expectedBasic, d.toDateAndOrTime(false));
				assertEquals("Failed extended for parameters: " + Arrays.toString(testCase.params), testCase.expectedExtended, d.toDateAndOrTime(true));
			} catch (IllegalArgumentException e) {
				if (!testCase.exception) {
					throw e;
				}
			}
		}
	}

	@Test
	public void toDateAndOrTime_time() {
		//@formatter:off
		ToDateAndOrTimeTestCase testCases[] = new ToDateAndOrTimeTestCase[]{
			ToDateAndOrTimeTestCase.test(		new Integer[]{5, null, null}, "T05", "T05"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 20, null}, "T-20", "T-20"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, null, 32}, "T--32", "T--32"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{5, 20, null}, "T0520", "T05:20"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 20, 32}, "T-2032", "T-20:32"),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{5, null, 32}),
			ToDateAndOrTimeTestCase.test(		new Integer[]{5, 20, 32}, "T052032", "T05:20:32"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, null, null}, "", ""),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{-1, null, null}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{24, null, null}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, -1, null}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, 60, null}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, null, -1}),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, null, 60}),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 20, 32, -5, 30}, "T-2032-0530", "T-20:32-05:30"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 20, 32, -5, 0}, "T-2032-0500", "T-20:32-05:00"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 20, 32, -5, null}, "T-2032-0500", "T-20:32-05:00"),
			ToDateAndOrTimeTestCase.test(		new Integer[]{null, 20, 32, 5, 30}, "T-2032+0530","T-20:32+05:30"),
			ToDateAndOrTimeTestCase.exception(	new Integer[]{null, 20, 32, null, 30})
		};
		//@formatter:on

		for (ToDateAndOrTimeTestCase testCase : testCases) {
			try {
				PartialDate d;
				if (testCase.params.length == 3) {
					d = PartialDate.time(testCase.params[0], testCase.params[1], testCase.params[2]);
				} else {
					d = PartialDate.time(testCase.params[0], testCase.params[1], testCase.params[2], testCase.params[3], testCase.params[4]);
				}

				if (testCase.exception) {
					fail("IllegalArgumentException expected for parameters: " + Arrays.toString(testCase.params));
				}
				assertEquals("Failed basic for parameters: " + Arrays.toString(testCase.params), testCase.expectedBasic, d.toDateAndOrTime(false));
				assertEquals("Failed extended for parameters: " + Arrays.toString(testCase.params), testCase.expectedExtended, d.toDateAndOrTime(true));
			} catch (IllegalArgumentException e) {
				if (!testCase.exception) {
					fail("IllegalArgumentException was not expected for parameters: " + Arrays.toString(testCase.params));
				}
			}
		}
	}

	@Test
	public void toDateAndOrTime_date_and_time() {
		PartialDate d = PartialDate.dateTime(null, 4, 20, 5, null, null, -5, null);
		assertEquals("--0420T05-0500", d.toDateAndOrTime(false));
		assertEquals("--04-20T05-05:00", d.toDateAndOrTime(true));
	}

	@Test
	public void parse() {
		//@formatter:off
		ParseTestCase testCases[] = new ParseTestCase[]{
			ParseTestCase.exception(	""),
			ParseTestCase.exception(	"invalid"),
			ParseTestCase.test(			"1980", 1980, null, null, null, null, null, null, null),
			ParseTestCase.test(			"--04", null, 4, null, null, null, null, null, null),
			ParseTestCase.test(			"---20", null, null, 20, null, null, null, null, null),
			ParseTestCase.test(			"1980-04", 1980, 4, null, null, null, null, null, null),
			ParseTestCase.test(			"--0420", null, 4, 20, null, null, null, null, null),
			ParseTestCase.test(			"--04-20", null, 4, 20, null, null, null, null, null),
			ParseTestCase.test(			"19800420", 1980, 4, 20, null, null, null, null, null),
			ParseTestCase.test(			"1980-04-20", 1980, 4, 20, null, null, null, null, null),
			ParseTestCase.test(			"T05", null, null, null, 5, null, null, null, null),
			ParseTestCase.test(			"T-20", null, null, null, null, 20, null, null, null),
			ParseTestCase.test(			"T--32", null, null, null, null, null, 32, null, null),
			ParseTestCase.test(			"T0520", null, null, null, 5, 20, null, null, null),
			ParseTestCase.test(			"T05:20", null, null, null, 5, 20, null, null, null),
			ParseTestCase.test(			"T-2032", null, null, null, null, 20, 32, null, null),
			ParseTestCase.test(			"T-20:32", null, null, null, null, 20, 32, null, null),
			ParseTestCase.test(			"T052032", null, null, null, 5, 20, 32, null, null),
			ParseTestCase.test(			"T05:20:32", null, null, null, 5, 20, 32, null, null),
			ParseTestCase.test(			"T-2032-0530", null, null, null, null, 20, 32, -5, 30),
			ParseTestCase.test(			"T-20:32-05:30", null, null, null, null, 20, 32, -5, 30),
			ParseTestCase.test(			"T-2032-0500", null, null, null, null, 20, 32, -5, 0),
			ParseTestCase.test(			"T-20:32-05:00", null, null, null, null, 20, 32, -5, 0),
			ParseTestCase.test(			"T-2032-05", null, null, null, null, 20, 32, -5, null),
			ParseTestCase.test(			"T-20:32-05", null, null, null, null, 20, 32, -5, null),
			ParseTestCase.test(			"T-20:32+05:30", null, null, null, null, 20, 32, 5, 30),
			ParseTestCase.test(			"--0420T05-0500", null, 4, 20, 5, null, null, -5, 0),
			ParseTestCase.test(			"--04-20T05-05:00", null, 4, 20, 5, null, null, -5, 0)
		};
		//@formatter:on

		for (ParseTestCase testCase : testCases) {
			try {
				PartialDate d = new PartialDate(testCase.string);
				if (testCase.exception) {
					fail("IllegalArgumentException expected for string: " + testCase.string);
				}
				assertArrayEquals("Failed for string: " + testCase.string, testCase.expectedComponents, d.components);
			} catch (IllegalArgumentException e) {
				if (!testCase.exception) {
					fail("IllegalArgumentException was not expected for string: " + testCase.string);
				}
			}
		}
	}

	@Test
	public void hasDateComponent() {
		assertTrue(PartialDate.date(1980, null, null).hasDateComponent());
		assertFalse(PartialDate.time(5, null, null).hasDateComponent());
	}

	@Test
	public void hasTimeComponent() {
		assertFalse(PartialDate.date(1980, null, null).hasTimeComponent());
		assertTrue(PartialDate.time(5, null, null).hasTimeComponent());
	}

	@Test
	public void equals() {
		PartialDate d1 = new PartialDate(null, 4, 20, 5, null, null, null, null);
		PartialDate d2 = new PartialDate(null, 4, 20, 5, null, null, null, null);
		PartialDate d3 = new PartialDate(null, 4, 20, 5, 20, null, null, null);
		assertTrue(d1.equals(d2));
		assertTrue(d2.equals(d1));
		assertTrue(d1.equals(d1));
		assertFalse(d1.equals(d3));
	}

	static class ToDateAndOrTimeTestCase {
		Integer[] params;
		String expectedBasic, expectedExtended;
		boolean exception;

		static ToDateAndOrTimeTestCase test(Integer[] params, String expectedBasic, String expectedExtended) {
			ToDateAndOrTimeTestCase testCase = new ToDateAndOrTimeTestCase();
			testCase.params = params;
			testCase.expectedBasic = expectedBasic;
			testCase.expectedExtended = expectedExtended;
			testCase.exception = false;
			return testCase;
		}

		static ToDateAndOrTimeTestCase exception(Integer[] params) {
			ToDateAndOrTimeTestCase testCase = new ToDateAndOrTimeTestCase();
			testCase.params = params;
			testCase.exception = true;
			return testCase;
		}
	}

	static class ParseTestCase {
		String string;
		Integer[] expectedComponents;
		boolean exception;

		static ParseTestCase test(String string, Integer... expectedComponents) {
			ParseTestCase testCase = new ParseTestCase();
			testCase.string = string;
			testCase.expectedComponents = expectedComponents;
			testCase.exception = false;
			return testCase;
		}

		static ParseTestCase exception(String string) {
			ParseTestCase testCase = new ParseTestCase();
			testCase.string = string;
			testCase.exception = true;
			return testCase;
		}
	}
}
