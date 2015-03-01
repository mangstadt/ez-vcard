package ezvcard.util;

import static ezvcard.util.PartialDate.builder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
public class PartialDateTest {
	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Test
	public void builder_month() {
		PartialDate.Builder builder = builder();
		final int end = 13;

		expectedException.expect(IllegalArgumentException.class);
		builder.month(-1);

		for (int i = 0; i < end; i++) {
			builder.month(i);
		}

		expectedException.expect(IllegalArgumentException.class);
		builder.month(end);
	}

	@Test
	public void builder_date() {
		PartialDate.Builder builder = builder();
		final int end = 32;

		expectedException.expect(IllegalArgumentException.class);
		builder.date(-1);

		for (int i = 0; i < end; i++) {
			builder.date(i);
		}

		expectedException.expect(IllegalArgumentException.class);
		builder.date(end);
	}

	@Test
	public void builder_hour() {
		PartialDate.Builder builder = builder();
		final int end = 24;

		expectedException.expect(IllegalArgumentException.class);
		builder.hour(-1);

		for (int i = 0; i < end; i++) {
			builder.hour(i);
		}

		expectedException.expect(IllegalArgumentException.class);
		builder.hour(end);
	}

	@Test
	public void builder_minute() {
		PartialDate.Builder builder = builder();
		final int end = 60;

		expectedException.expect(IllegalArgumentException.class);
		builder.minute(-1);

		for (int i = 0; i < end; i++) {
			builder.minute(i);
		}

		expectedException.expect(IllegalArgumentException.class);
		builder.minute(end);
	}

	@Test
	public void builder_second() {
		PartialDate.Builder builder = builder();
		final int end = 60;

		expectedException.expect(IllegalArgumentException.class);
		builder.second(-1);

		for (int i = 0; i < end; i++) {
			builder.second(i);
		}

		expectedException.expect(IllegalArgumentException.class);
		builder.second(end);
	}

	@Test
	public void builder_build() {
		PartialDate.Builder builder = builder();
		expectedException.expect(IllegalArgumentException.class);
		builder.year(2015).date(1).build();

		builder = builder();
		expectedException.expect(IllegalArgumentException.class);
		builder.hour(16).second(1).build();
	}

	@Test
	public void toDateAndOrTime() {
		//date
		assertToDateAndOrTime(builder().year(1980), "1980", "1980");
		assertToDateAndOrTime(builder().month(4), "--04", "--04");
		assertToDateAndOrTime(builder().date(20), "---20", "---20");
		assertToDateAndOrTime(builder().year(1980).month(4), "1980-04", "1980-04");
		assertToDateAndOrTime(builder().month(4).date(20), "--0420", "--04-20");
		assertToDateAndOrTime(builder().year(1980).month(4).date(20), "19800420", "1980-04-20");
		assertToDateAndOrTime(builder(), "", "");

		//time
		assertToDateAndOrTime(builder().hour(5), "T05", "T05");
		assertToDateAndOrTime(builder().minute(20), "T-20", "T-20");
		assertToDateAndOrTime(builder().second(32), "T--32", "T--32");
		assertToDateAndOrTime(builder().hour(5).minute(20), "T0520", "T05:20");
		assertToDateAndOrTime(builder().minute(20).second(32), "T-2032", "T-20:32");
		assertToDateAndOrTime(builder().hour(5).minute(20).second(32), "T052032", "T05:20:32");
		assertToDateAndOrTime(builder(), "", "");
		assertToDateAndOrTime(builder().minute(20).second(32).offset(-5, 30), "T-2032-0530", "T-20:32-05:30");
		assertToDateAndOrTime(builder().minute(20).second(32).offset(-5, 0), "T-2032-0500", "T-20:32-05:00");
		assertToDateAndOrTime(builder().minute(20).second(32).offset(5, 30), "T-2032+0530", "T-20:32+05:30");

		//date and time
		assertToDateAndOrTime(builder().month(4).date(20).hour(5).offset(-5, 0), "--0420T05-0500", "--04-20T05-05:00");
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

		assertParse("T05", builder().hour(5));
		assertParse("T-20", builder().minute(20));
		assertParse("T--32", builder().second(32));
		assertParse("T0520", builder().hour(5).minute(20));
		assertParse("T05:20", builder().hour(5).minute(20));
		assertParse("T-2032", builder().minute(20).second(32));
		assertParse("T-20:32", builder().minute(20).second(32));
		assertParse("T052032", builder().hour(5).minute(20).second(32));
		assertParse("T05:20:32", builder().hour(5).minute(20).second(32));
		assertParse("T-2032-0530", builder().minute(20).second(32).offset(-5, 30));
		assertParse("T-20:32-05:30", builder().minute(20).second(32).offset(-5, 30));
		assertParse("T-2032-0500", builder().minute(20).second(32).offset(-5, 0));
		assertParse("T-20:32-05:00", builder().minute(20).second(32).offset(-5, 0));
		assertParse("T-2032-05", builder().minute(20).second(32).offset(-5, 0));
		assertParse("T-20:32-05", builder().minute(20).second(32).offset(-5, 0));
		assertParse("T-20:32+05:30", builder().minute(20).second(32).offset(5, 30));
		assertParse("--0420T05-0500", builder().month(4).date(20).hour(5).offset(-5, 0));
		assertParse("--04-20T05-05:00", builder().month(4).date(20).hour(5).offset(-5, 0));
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
	public void equals() {
		PartialDate d1 = builder().month(4).date(20).hour(5).build();
		PartialDate d2 = builder().month(4).date(20).hour(5).build();
		PartialDate d3 = builder().month(4).date(20).hour(5).minute(20).build();
		assertTrue(d1.equals(d2));
		assertTrue(d2.equals(d1));
		assertTrue(d1.equals(d1));
		assertFalse(d1.equals(d3));
	}

	private static void assertToDateAndOrTime(PartialDate.Builder dateBuilder, String expectedBasic, String expectedExtended) {
		PartialDate date = dateBuilder.build();
		assertEquals(expectedBasic, date.toDateAndOrTime(false));
		assertEquals(expectedExtended, date.toDateAndOrTime(true));
	}

	private static void assertParse(String input, PartialDate.Builder expectedBuilder) {
		PartialDate expected = expectedBuilder.build();
		PartialDate actual = PartialDate.parse(input);
		assertEquals(expected, actual);
	}
}
