package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
public class VCardDateFormatterTest {
	@Test
	public void format() {
		TimeZone tz = TimeZone.getTimeZone("Asia/Beirut");
		Calendar cal = Calendar.getInstance(tz);
		cal.clear();
		cal.set(Calendar.YEAR, 2006);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 2);
		cal.set(Calendar.HOUR_OF_DAY, 10);
		cal.set(Calendar.MINUTE, 20);
		cal.set(Calendar.SECOND, 30);
		Date date = cal.getTime();

		assertEquals("20060102", VCardDateFormatter.format(date, ISOFormat.DATE_BASIC, tz));
		assertEquals("2006-01-02", VCardDateFormatter.format(date, ISOFormat.DATE_EXTENDED, tz));
		assertEquals("20060102T102030+0200", VCardDateFormatter.format(date, ISOFormat.TIME_BASIC, tz));
		assertEquals("2006-01-02T10:20:30+02:00", VCardDateFormatter.format(date, ISOFormat.TIME_EXTENDED, tz));
		assertEquals("2006-01-02T10:20:30+0200", VCardDateFormatter.format(date, ISOFormat.HCARD_TIME_TAG, tz));
		assertEquals("20060102T082030Z", VCardDateFormatter.format(date, ISOFormat.UTC_TIME_BASIC, tz));
		assertEquals("2006-01-02T08:20:30Z", VCardDateFormatter.format(date, ISOFormat.UTC_TIME_EXTENDED, tz));
	}

	@Test
	public void parse() throws Exception {
		Calendar c;
		Date expected, actual;

		//test date
		c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2012);
		c.set(Calendar.MONTH, Calendar.JULY);
		c.set(Calendar.DAY_OF_MONTH, 1);
		expected = c.getTime();
		actual = VCardDateFormatter.parse("20120701");
		assertEquals(expected, actual);

		actual = VCardDateFormatter.parse("2012-07-01");
		assertEquals(expected, actual);

		//test date-time
		c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.clear();
		c.set(Calendar.YEAR, 2012);
		c.set(Calendar.MONTH, Calendar.JULY);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 1);
		c.set(Calendar.SECOND, 30);
		expected = c.getTime();
		actual = VCardDateFormatter.parse("20120701T080130Z");
		assertEquals(expected, actual);

		actual = VCardDateFormatter.parse("2012-07-01T08:01:30Z");
		assertEquals(expected, actual);

		actual = VCardDateFormatter.parse("2012-07-01T11:01:30+03:00");
		assertEquals(expected, actual);

		actual = VCardDateFormatter.parse("2012-07-01T11:01:30+0300");
		assertEquals(expected, actual);
	}

	@Test
	public void parseTimezoneId() {
		TimeZone tz = VCardDateFormatter.parseTimeZoneId("America/New_York");
		assertEquals("America/New_York", tz.getID());

		tz = VCardDateFormatter.parseTimeZoneId("Bogus/Timezone");
		assertNull(tz);
	}
}
