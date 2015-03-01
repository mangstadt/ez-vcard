package ezvcard.util;

import static ezvcard.util.TestUtils.assertIntEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
	public void toDateAndOrTime() {
		//date
		assertToDateAndOrTime().year(1980).run("1980", "1980");
		assertToDateAndOrTime().month(4).run("--04", "--04");
		assertToDateAndOrTime().date(20).run("---20", "---20");
		assertToDateAndOrTime().year(1980).month(4).run("1980-04", "1980-04");
		assertToDateAndOrTime().month(4).date(20).run("--0420", "--04-20");
		assertToDateAndOrTime().year(1980).date(20).exception();
		assertToDateAndOrTime().year(1980).month(4).date(20).run("19800420", "1980-04-20");
		assertToDateAndOrTime().run("", "");
		assertToDateAndOrTime().month(-1).exception();
		assertToDateAndOrTime().date(-1).exception();
		assertToDateAndOrTime().month(13).exception();
		assertToDateAndOrTime().date(32).exception();

		//time
		assertToDateAndOrTime().hour(5).run("T05", "T05");
		assertToDateAndOrTime().minute(20).run("T-20", "T-20");
		assertToDateAndOrTime().second(32).run("T--32", "T--32");
		assertToDateAndOrTime().hour(5).minute(20).run("T0520", "T05:20");
		assertToDateAndOrTime().minute(20).second(32).run("T-2032", "T-20:32");
		assertToDateAndOrTime().hour(5).second(32).exception();
		assertToDateAndOrTime().hour(5).minute(20).second(32).run("T052032", "T05:20:32");
		assertToDateAndOrTime().run("", "");
		assertToDateAndOrTime().hour(-1).exception();
		assertToDateAndOrTime().hour(24).exception();
		assertToDateAndOrTime().minute(-1).exception();
		assertToDateAndOrTime().minute(60).exception();
		assertToDateAndOrTime().second(-1).exception();
		assertToDateAndOrTime().second(60).exception();
		assertToDateAndOrTime().minute(20).second(32).offset(-5, 30).run("T-2032-0530", "T-20:32-05:30");
		assertToDateAndOrTime().minute(20).second(32).offset(-5, 0).run("T-2032-0500", "T-20:32-05:00");
		assertToDateAndOrTime().minute(20).second(32).offset(5, 30).run("T-2032+0530", "T-20:32+05:30");

		//date and time
		assertToDateAndOrTime().month(4).date(20).hour(5).offset(-5, 0).run("--0420T05-0500", "--04-20T05-05:00");
	}

	private TestCase assertToDateAndOrTime() {
		return new TestCase();
	}

	@Test
	public void parse() {
		assertParse().exception("");
		assertParse().exception("invalid");
		assertParse().year(1980).run("1980");
		assertParse().month(4).run("--04");
		assertParse().date(20).run("--20");
		assertParse().year(1980).month(4).run("1980-04");

		assertParse().month(4).date(20).run("--0420");
		assertParse().month(4).date(20).run("--04-20");
		assertParse().year(1980).month(4).date(20).run("19800420");
		assertParse().year(1980).month(4).date(20).run("1980-04-20");

		assertParse().hour(5).run("T05");
		assertParse().minute(20).run("T-20");
		assertParse().second(32).run("T--32");
		assertParse().hour(5).minute(20).run("T0520");
		assertParse().hour(5).minute(20).run("T05:20");
		assertParse().minute(20).second(32).run("T-2032");
		assertParse().minute(20).second(32).run("T-20:32");
		assertParse().hour(5).minute(20).second(32).run("T052032");
		assertParse().hour(5).minute(20).second(32).run("T05:20:32");
		assertParse().minute(20).second(32).offset(-5, 30).run("T-2032-0530");
		assertParse().minute(20).second(32).offset(-5, 30).run("T-20:32-05:30");
		assertParse().minute(20).second(32).offset(-5, 0).run("T-2032-0500");
		assertParse().minute(20).second(32).offset(-5, 0).run("T-20:32-05:00");
		assertParse().minute(20).second(32).offset(-5, 0).run("T-2032-05");
		assertParse().minute(20).second(32).offset(-5, 0).run("T-20:32-05");
		assertParse().minute(20).second(32).offset(5, 30).run("T-20:32+05:30");
		assertParse().hour(5).offset(-5, 0).run("--0420T05-0500");
		assertParse().hour(5).offset(-5, 0).run("--04-20T05-05:00");
	}

	public TestCase assertParse() {
		return new TestCase();
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
		PartialDate d1 = new PartialDate(null, 4, 20, 5, null, null, null);
		PartialDate d2 = new PartialDate(null, 4, 20, 5, null, null, null);
		PartialDate d3 = new PartialDate(null, 4, 20, 5, 20, null, null);
		assertTrue(d1.equals(d2));
		assertTrue(d2.equals(d1));
		assertTrue(d1.equals(d1));
		assertFalse(d1.equals(d3));
	}

	private class TestCase {
		private Integer year, month, date, hour, minute, second;
		private UtcOffset offset;

		public TestCase year(Integer year) {
			this.year = year;
			return this;
		}

		public TestCase month(Integer month) {
			this.month = month;
			return this;
		}

		public TestCase date(Integer date) {
			this.date = date;
			return this;
		}

		public TestCase hour(Integer hour) {
			this.hour = hour;
			return this;
		}

		public TestCase minute(Integer minute) {
			this.minute = minute;
			return this;
		}

		public TestCase second(Integer second) {
			this.second = second;
			return this;
		}

		public TestCase offset(Integer hour, Integer minute) {
			this.offset = new UtcOffset(hour, minute);
			return this;
		}

		public void run(String expectedBasic, String expectedExtended) {
			PartialDate d = new PartialDate(year, month, date, hour, minute, second, offset);
			assertEquals(expectedBasic, d.toDateAndOrTime(false));
			assertEquals(expectedExtended, d.toDateAndOrTime(true));
		}

		public void exception() {
			expectedException.expect(IllegalArgumentException.class);
			new PartialDate(year, month, date, hour, minute, second, offset);
		}

		public void run(String input) {
			PartialDate date = new PartialDate(input);
			assertEquals(year, date.getYear());
			assertEquals(month, date.getMinute());
			assertEquals(this.date, date.getDate());
			assertEquals(hour, date.getHour());
			assertEquals(minute, date.getMinute());
			assertEquals(second, date.getSecond());
			if (offset == null) {
				assertNull(date.getTimezone());
			} else {
				Integer[] tz = date.getTimezone();
				assertIntEquals(offset.getHour(), tz[0]);
				assertIntEquals(offset.getMinute(), tz[1]);
			}
		}

		public void exception(String input) {
			expectedException.expect(IllegalArgumentException.class);
			new PartialDate(input);
		}
	}
}
