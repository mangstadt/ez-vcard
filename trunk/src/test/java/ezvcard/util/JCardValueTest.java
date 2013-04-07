package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
public class JCardValueTest {
	@Test
	public void text() {
		JCardValue value = JCardValue.text("one", "two");
		assertEquals(JCardDataType.TEXT, value.getDataType());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "one" }),
			Arrays.asList(new Object[]{ "two" })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void text_empty() {
		JCardValue value = JCardValue.text();
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertEquals(0, value.getValues().size());
	}

	@Test
	public void uri() {
		JCardValue value = JCardValue.uri("http://json.org");
		assertEquals(JCardDataType.URI, value.getDataType());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "http://json.org" })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void uri_empty() {
		JCardValue value = JCardValue.uri();
		assertEquals(JCardDataType.URI, value.getDataType());
		assertEquals(0, value.getValues().size());
	}

	@Test
	public void date() {
		Date date;
		{
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 5);
			c.set(Calendar.HOUR_OF_DAY, 13);
			c.set(Calendar.MINUTE, 10);
			c.set(Calendar.SECOND, 20);
			date = c.getTime();
		}
		JCardValue value = JCardValue.date(date);
		assertEquals(JCardDataType.DATE, value.getDataType());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "1980-06-05" })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void date_empty() {
		JCardValue value = JCardValue.date();
		assertEquals(JCardDataType.DATE, value.getDataType());
		assertEquals(0, value.getValues().size());
	}

	@Test
	public void dateTime() {
		Date date;
		{
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 5);
			c.set(Calendar.HOUR_OF_DAY, 13);
			c.set(Calendar.MINUTE, 10);
			c.set(Calendar.SECOND, 20);
			date = c.getTime();
		}
		JCardValue value = JCardValue.dateTime(date);
		assertEquals(JCardDataType.DATE_TIME, value.getDataType());
		assertEquals(1, value.getValues().size());
		assertEquals(1, value.getValues().get(0).size());
		assertTrue(((String) value.getValues().get(0).get(0)).matches("1980-06-05T13:10:20[-+]\\d+:\\d+"));
	}

	@Test
	public void dateTime_empty() {
		JCardValue value = JCardValue.dateTime();
		assertEquals(JCardDataType.DATE_TIME, value.getDataType());
		assertEquals(0, value.getValues().size());
	}

	@Test
	public void timestamp() {
		Date date;
		{
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 5);
			c.set(Calendar.HOUR_OF_DAY, 13);
			c.set(Calendar.MINUTE, 10);
			c.set(Calendar.SECOND, 20);
			date = c.getTime();
		}
		JCardValue value = JCardValue.timestamp(date);
		assertEquals(JCardDataType.TIMESTAMP, value.getDataType());
		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "1980-06-05T13:10:20Z" })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void timestamp_empty() {
		JCardValue value = JCardValue.timestamp();
		assertEquals(JCardDataType.TIMESTAMP, value.getDataType());
		assertEquals(0, value.getValues().size());
	}

	@Test
	public void structured() {
		JCardValue value = new JCardValue();
		assertFalse(value.isStructured());

		value.setStructured(true);
		assertTrue(value.isStructured());
	}

	@Test
	public void dataType() {
		JCardValue value = new JCardValue();
		assertEquals(JCardDataType.TEXT, value.getDataType());

		value.setDataType(JCardDataType.URI);
		assertEquals(JCardDataType.URI, value.getDataType());
	}

	@Test
	public void getFirstValue() {
		JCardValue value = new JCardValue();
		value.addValues(1, 2, Arrays.asList(3, 4));
		assertEquals(1, value.getFirstValue());
		assertEquals(1, value.getFirstValue(0));
		assertEquals(2, value.getFirstValue(1));
		assertEquals(3, value.getFirstValue(2));
		assertNull(value.getFirstValue(3));
	}

	@Test
	public void getFirstValueAsString() {
		JCardValue value = new JCardValue();
		value.addValues(1, 2, Arrays.asList(3, 4));
		assertEquals("1", value.getFirstValueAsString());
		assertEquals("1", value.getFirstValueAsString(0));
		assertEquals("2", value.getFirstValueAsString(1));
		assertEquals("3", value.getFirstValueAsString(2));
		assertNull(value.getFirstValueAsString(3));
	}

	@Test
	public void getValuesAsStrings() {
		JCardValue value = new JCardValue();
		value.addValues(1, 2, Arrays.asList(3, 4));
		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<String>> expected = Arrays.asList(
			Arrays.asList("1"),
			Arrays.asList("2"),
			Arrays.asList("3", "4")
		);
		//@formatter:on
		assertEquals(expected, value.getValuesAsStrings());
	}

	@Test
	public void addValues() {
		JCardValue value = new JCardValue();
		value.addValues(1, 2, Arrays.asList(3, 4));
		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ 1 }),
			Arrays.asList(new Object[]{ 2 }),
			Arrays.asList(new Object[]{ 3, 4 })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}
}
