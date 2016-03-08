package ezvcard.util;

import static ezvcard.util.TestUtils.buildTimezone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

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
 * @author Michael Angstadt
 */
public class UtcOffsetTest {
	@Test
	public void constructor() {
		final int HOURS = 60 * 60 * 1000;
		final int MINUTES = 60 * 1000;

		UtcOffset offset = new UtcOffset(true, -5, -30);
		assertEquals(5 * HOURS + 30 * MINUTES, offset.getMillis());

		offset = new UtcOffset(false, 5, 30);
		assertEquals(-(5 * HOURS + 30 * MINUTES), offset.getMillis());

		offset = new UtcOffset(false, 5, 70);
		assertEquals(-(5 * HOURS + 70 * MINUTES), offset.getMillis());
	}

	@Test
	public void parse() {
		assertParse("5", 5, 0);
		assertParse("10", 10, 0);
		assertParse("+5", 5, 0);
		assertParse("+10", 10, 0);
		assertParse("-5", -5, 0);
		assertParse("-10", -10, 0);
		assertParse("05", 5, 0);
		assertParse("+05", 5, 0);
		assertParse("-05", -5, 0);
		assertParse("500", 5, 0);
		assertParse("+500", 5, 0);
		assertParse("-500", -5, 0);
		assertParse("530", 5, 30);
		assertParse("+530", 5, 30);
		assertParse("-530", -5, 30);
		assertParse("5:00", 5, 0);
		assertParse("5:", 5, 0);
		assertParse("10:00", 10, 0);
		assertParse("10:", 10, 0);
		assertParse("+5:00", 5, 0);
		assertParse("+5:", 5, 0);
		assertParse("+10:00", 10, 0);
		assertParse("+10:", 10, 0);
		assertParse("-5:00", -5, 0);
		assertParse("-5:", -5, 0);
		assertParse("-10:00", -10, 0);
		assertParse("-10:", -10, 0);
		assertParse("5:30", 5, 30);
		assertParse("10:30", 10, 30);
		assertParse("+5:30", 5, 30);
		assertParse("+10:30", 10, 30);
		assertParse("-5:30", -5, 30);
		assertParse("-10:30", -10, 30);
		assertParse("0500", 5, 0);
		assertParse("1000", 10, 0);
		assertParse("+0500", 5, 0);
		assertParse("+1000", 10, 0);
		assertParse("-0500", -5, 0);
		assertParse("-1000", -10, 0);
		assertParse("0530", 5, 30);
		assertParse("1030", 10, 30);
		assertParse("+0530", 5, 30);
		assertParse("+1030", 10, 30);
		assertParse("-0530", -5, 30);
		assertParse("-1030", -10, 30);
		assertParse("05:00", 5, 0);
		assertParse("10:00", 10, 0);
		assertParse("+05:00", 5, 0);
		assertParse("+10:00", 10, 0);
		assertParse("-05:00", -5, 0);
		assertParse("-10:00", -10, 0);
		assertParse("05:30", 5, 30);
		assertParse("10:30", 10, 30);
		assertParse("+05:30", 5, 30);
		assertParse("+10:30", 10, 30);
		assertParse("-05:30", -5, 30);
		assertParse("-10:30", -10, 30);
		assertParseInvalid("invalid");
		assertParseInvalid("050030");
		assertParseInvalid("05:00:30");
		assertParseInvalid("four"); //expected number of characters, but they are not numbers
	}

	@Test
	public void parse_timezone() {
		UtcOffset expected = new UtcOffset(true, 1, 0);
		UtcOffset actual = UtcOffset.parse(buildTimezone(1, 0));
		assertEquals(expected, actual);
	}

	@Test
	public void toString_with_argument() {
		{
			boolean extended = false;

			{
				boolean positive = true;
				assertToString(positive, 0, 0, extended, "+0000");
				assertToString(positive, 1, 0, extended, "+0100");
				assertToString(positive, 10, 0, extended, "+1000");
				assertToString(positive, 1, 30, extended, "+0130");
				assertToString(positive, 10, 30, extended, "+1030");
				assertToString(positive, 0, 30, extended, "+0030");
				assertToString(positive, 0, 5, extended, "+0005");
			}

			{
				boolean positive = false;
				assertToString(positive, 0, 0, extended, "+0000");
				assertToString(positive, -1, 0, extended, "-0100");
				assertToString(positive, -10, 0, extended, "-1000");
				assertToString(positive, -1, 30, extended, "-0130");
				assertToString(positive, -10, 30, extended, "-1030");
				assertToString(positive, 0, 30, extended, "-0030");
				assertToString(positive, 0, 5, extended, "-0005");
			}
		}

		{
			boolean extended = true;

			{
				boolean positive = true;
				assertToString(positive, 0, 0, extended, "+00:00");
				assertToString(positive, 1, 0, extended, "+01:00");
				assertToString(positive, 10, 0, extended, "+10:00");
				assertToString(positive, 1, 30, extended, "+01:30");
				assertToString(positive, 10, 30, extended, "+10:30");
				assertToString(positive, 0, 30, extended, "+00:30");
				assertToString(positive, 0, 5, extended, "+00:05");
			}

			{
				boolean positive = false;
				assertToString(positive, 0, 0, extended, "+00:00");
				assertToString(positive, -1, 0, extended, "-01:00");
				assertToString(positive, -10, 0, extended, "-10:00");
				assertToString(positive, -1, 30, extended, "-01:30");
				assertToString(positive, -10, 30, extended, "-10:30");
				assertToString(positive, 0, 30, extended, "-00:30");
				assertToString(positive, 0, 5, extended, "-00:05");
			}
		}

	}

	@Test
	public void toString_no_arguments() {
		UtcOffset offset = new UtcOffset(true, 5, 0);
		String actual = offset.toString();
		assertEquals("+0500", actual);
	}

	private static void assertParseInvalid(String input) {
		try {
			UtcOffset.parse(input);
			fail("IllegalArgumentException expected.");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	private static void assertParse(String input, int expectedHour, int expectedMinute) {
		UtcOffset expected = new UtcOffset(expectedHour >= 0, expectedHour, expectedMinute);
		UtcOffset actual = UtcOffset.parse(input);
		assertEquals(expected, actual);
	}

	private static void assertToString(boolean positive, int hour, int minute, boolean extended, String expected) {
		UtcOffset offset = new UtcOffset(positive, hour, minute);
		String actual = offset.toString(extended);
		assertEquals(expected, actual);
	}

	@Test
	public void equals_contract() {
		EqualsVerifier.forClass(UtcOffset.class).usingGetClass().verify();
	}
}
