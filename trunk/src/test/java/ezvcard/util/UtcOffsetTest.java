package ezvcard.util;

import static ezvcard.util.TestUtils.buildTimezone;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
 */

/**
 * @author Michael Angstadt
 */
public class UtcOffsetTest {
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
		assertParse("10:00", 10, 0);
		assertParse("+5:00", 5, 0);
		assertParse("+10:00", 10, 0);
		assertParse("-5:00", -5, 0);
		assertParse("-10:00", -10, 0);
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
	}

	private void assertParse(String input, int expectedHour, int expectedMinute) {
		UtcOffset expected = new UtcOffset(expectedHour, expectedMinute);
		UtcOffset actual = UtcOffset.parse(input);
		assertEquals(expected, actual);
	}

	@Test
	public void parse_timezone() {
		UtcOffset expected = new UtcOffset(1, 0);
		UtcOffset actual = UtcOffset.parse(buildTimezone(1, 0));
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		UtcOffset.parse("invalid");
	}

	@Test
	public void toString_() {
		assertToString(0, 0, false, "+0000");
		assertToString(1, 0, false, "+0100");
		assertToString(10, 0, false, "+1000");
		assertToString(1, 30, false, "+0130");
		assertToString(10, 30, false, "+1030");
		assertToString(0, 30, false, "+0030");
		assertToString(0, 30, false, "+0030");
		assertToString(-1, 0, false, "-0100");
		assertToString(-10, 0, false, "-1000");
		assertToString(-1, 30, false, "-0130");
		assertToString(-10, 30, false, "-1030");

		assertToString(0, 0, true, "+00:00");
		assertToString(1, 0, true, "+01:00");
		assertToString(10, 0, true, "+10:00");
		assertToString(1, 30, true, "+01:30");
		assertToString(10, 30, true, "+10:30");
		assertToString(0, 30, true, "+00:30");
		assertToString(0, 30, true, "+00:30");
		assertToString(-1, 0, true, "-01:00");
		assertToString(-10, 0, true, "-10:00");
		assertToString(-1, 30, true, "-01:30");
		assertToString(-10, 30, true, "-10:30");
	}

	private void assertToString(int hour, int minute, boolean extended, String expected) {
		UtcOffset offset = new UtcOffset(hour, minute);
		String actual = offset.toString(extended);
		assertEquals(expected, actual);
	}
}
