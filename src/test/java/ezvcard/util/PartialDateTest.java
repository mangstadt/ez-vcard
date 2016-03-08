package ezvcard.util;

import static ezvcard.util.PartialDate.builder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class PartialDateTest {
	@Test
	public void builder_month() {
		PartialDate.Builder builder = builder();
		final int start = 0, end = 13;

		try {
			builder.month(start);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		for (int i = start + 1; i < end; i++) {
			builder.month(i);
		}

		try {
			builder.month(end);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}
	}

	@Test
	public void builder_date() {
		PartialDate.Builder builder = builder();
		final int start = 0, end = 32;

		try {
			builder.date(start);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		for (int i = start + 1; i < end; i++) {
			builder.date(i);
		}

		try {
			builder.date(end);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}
	}

	@Test
	public void builder_hour() {
		PartialDate.Builder builder = builder();
		final int start = -1, end = 24;

		try {
			builder.hour(start);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		for (int i = start + 1; i < end; i++) {
			builder.hour(i);
		}

		try {
			builder.hour(end);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}
	}

	@Test
	public void builder_minute() {
		PartialDate.Builder builder = builder();
		final int start = -1, end = 60;

		try {
			builder.minute(start);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		for (int i = start + 1; i < end; i++) {
			builder.minute(i);
		}

		try {
			builder.minute(end);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}
	}

	@Test
	public void builder_second() {
		PartialDate.Builder builder = builder();
		final int start = -1, end = 60;

		try {
			builder.second(start);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		for (int i = start + 1; i < end; i++) {
			builder.second(i);
		}

		try {
			builder.second(end);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}
	}

	@Test
	public void builder_build() {
		try {
			builder().year(2015).date(1).build();
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}

		try {
			builder().hour(16).second(1).build();
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	@Test
	public void builder_copy() {
		PartialDate orig = builder().year(2015).month(3).date(5).hour(12).minute(1).second(20).offset(new UtcOffset(false, -5, 0)).build();
		PartialDate copy = builder(orig).build();
		assertEquals(orig, copy);
	}

	@Test
	public void toISO8601() {
		//date
		assertToISO8601(builder().year(1980), "1980", "1980");
		assertToISO8601(builder().month(4), "--04", "--04");
		assertToISO8601(builder().date(20), "---20", "---20");
		assertToISO8601(builder().year(1980).month(4), "1980-04", "1980-04");
		assertToISO8601(builder().month(4).date(20), "--0420", "--04-20");
		assertToISO8601(builder().year(1980).month(4).date(20), "19800420", "1980-04-20");
		assertToISO8601(builder(), "", "");

		//time
		assertToISO8601(builder().hour(5), "T05", "T05");
		assertToISO8601(builder().minute(20), "T-20", "T-20");
		assertToISO8601(builder().second(32), "T--32", "T--32");
		assertToISO8601(builder().hour(5).minute(20), "T0520", "T05:20");
		assertToISO8601(builder().minute(20).second(32), "T-2032", "T-20:32");
		assertToISO8601(builder().hour(5).minute(20).second(32), "T052032", "T05:20:32");
		assertToISO8601(builder(), "", "");
		assertToISO8601(builder().minute(20).second(32).offset(new UtcOffset(false, -5, 30)), "T-2032-0530", "T-20:32-05:30");
		assertToISO8601(builder().minute(20).second(32).offset(new UtcOffset(false, -5, 0)), "T-2032-0500", "T-20:32-05:00");
		assertToISO8601(builder().minute(20).second(32).offset(new UtcOffset(true, 5, 30)), "T-2032+0530", "T-20:32+05:30");

		//date and time
		assertToISO8601(builder().month(4).date(20).hour(5).offset(new UtcOffset(false, -5, 0)), "--0420T05-0500", "--04-20T05-05:00");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_empty() {
		PartialDate.parse("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		PartialDate.parse("invalid");
	}

	@Test
	public void parse() {
		assertParse("1980", builder().year(1980));
		assertParse("--04", builder().month(4));
		assertParse("---20", builder().date(20));
		assertParse("1980-04", builder().year(1980).month(4));

		assertParse("--0420", builder().month(4).date(20));
		assertParse("--04-20", builder().month(4).date(20));
		assertParse("19800420", builder().year(1980).month(4).date(20));
		assertParse("1980-04-20", builder().year(1980).month(4).date(20));
		assertParse("1980-04-20T", builder().year(1980).month(4).date(20));

		assertParse("T05", builder().hour(5));
		assertParse("T-20", builder().minute(20));
		assertParse("T--32", builder().second(32));
		assertParse("T0520", builder().hour(5).minute(20));
		assertParse("T05:20", builder().hour(5).minute(20));
		assertParse("T-2032", builder().minute(20).second(32));
		assertParse("T-20:32", builder().minute(20).second(32));
		assertParse("T052032", builder().hour(5).minute(20).second(32));
		assertParse("T05:20:32", builder().hour(5).minute(20).second(32));
		assertParse("T-2032-0530", builder().minute(20).second(32).offset(new UtcOffset(false, -5, 30)));
		assertParse("T-20:32-05:30", builder().minute(20).second(32).offset(new UtcOffset(false, -5, 30)));
		assertParse("T-2032-0500", builder().minute(20).second(32).offset(new UtcOffset(false, -5, 0)));
		assertParse("T-20:32-05:00", builder().minute(20).second(32).offset(new UtcOffset(false, -5, 0)));
		assertParse("T-2032-05", builder().minute(20).second(32).offset(new UtcOffset(false, -5, 0)));
		assertParse("T-20:32-05", builder().minute(20).second(32).offset(new UtcOffset(false, -5, 0)));
		assertParse("T-20:32+05:30", builder().minute(20).second(32).offset(new UtcOffset(true, 5, 30)));
		assertParse("T-20:32+00:30", builder().minute(20).second(32).offset(new UtcOffset(true, 0, 30)));
		assertParse("T-20:32-00:30", builder().minute(20).second(32).offset(new UtcOffset(false, 0, 30)));
		assertParse("--0420T05-0500", builder().month(4).date(20).hour(5).offset(new UtcOffset(false, -5, 0)));
		assertParse("--04-20T05-05:00", builder().month(4).date(20).hour(5).offset(new UtcOffset(false, -5, 0)));
	}

	@Test
	public void hasDateComponent() {
		assertTrue(builder().year(1980).build().hasDateComponent());
		assertFalse(builder().hour(5).build().hasDateComponent());
	}

	@Test
	public void hasTimeComponent() {
		assertFalse(builder().year(1980).build().hasTimeComponent());
		assertTrue(builder().hour(5).build().hasTimeComponent());
	}

	@Test
	public void equals_contract() {
		EqualsVerifier.forClass(PartialDate.class).usingGetClass().verify();
	}

	private static void assertToISO8601(PartialDate.Builder dateBuilder, String expectedBasic, String expectedExtended) {
		PartialDate date = dateBuilder.build();
		assertEquals(expectedBasic, date.toISO8601(false));
		assertEquals(expectedExtended, date.toISO8601(true));
	}

	private static void assertParse(String input, PartialDate.Builder expectedBuilder) {
		PartialDate expected = expectedBuilder.build();
		PartialDate actual = PartialDate.parse(input);
		assertEquals(expected, actual);
	}
}
