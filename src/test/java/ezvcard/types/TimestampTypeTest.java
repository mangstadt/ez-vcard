package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HtmlUtils;
import ezvcard.util.XCardElement;

/*
 Copyright (c) 2012, Michael Angstadt
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
public class TimestampTypeTest {
	@Test
	public void marshal() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 10);
		c.set(Calendar.SECOND, 20);
		Date date = c.getTime();
		TimestampType t = new TimestampType("DATE");
		t.setTimestamp(date);

		//version 2.1
		VCardVersion version = VCardVersion.V2_1;
		String expected = "19800605T131020Z";
		String actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//version 4.0
		version = VCardVersion.V4_0;
		expected = "19800605T131020Z";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//xCard
		version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("date");
		xe.appendTimestamp("19800605T131020Z");
		Document expectedDoc = xe.getDocument();
		xe = new XCardElement("date");
		Document actualDoc = xe.getDocument();
		Element element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
	}

	@Test
	public void unmarshalValue() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 10);
		c.set(Calendar.SECOND, 20);
		Date expected = c.getTime();

		VCardVersion version = VCardVersion.V2_1;
		TimestampType t = new TimestampType("DATE");
		t.unmarshalText(subTypes, "19800605T081020-0500", version, warnings, compatibilityMode);
		assertEquals(expected, t.getTimestamp());

		//xCard
		version = VCardVersion.V4_0;
		t = new TimestampType("DATE");
		XCardElement xe = new XCardElement("date");
		xe.appendTimestamp("19800605T131020Z");
		Element element = xe.getElement();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals(expected, t.getTimestamp());
	}

	@Test
	public void unmarshalHtml() throws Exception {
		List<String> warnings = new ArrayList<String>();

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, 1970);
		c.set(Calendar.MONTH, Calendar.MARCH);
		c.set(Calendar.DAY_OF_MONTH, 10);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 21);
		c.set(Calendar.SECOND, 3);
		Date expected = c.getTime();

		//valid date string
		{
			warnings.clear();
			org.jsoup.nodes.Element element = HtmlUtils.toElement("<time datetime=\"1970-03-10T13:21:03Z\">March 10, 1970 at 1:21:03 PM</time>");
			TimestampType t = new TimestampType("DATE");
			t.unmarshalHtml(element, warnings);

			assertEquals(expected, t.getTimestamp());
			assertEquals(0, warnings.size());
		}

		//date string in tag text
		{
			warnings.clear();
			org.jsoup.nodes.Element element = HtmlUtils.toElement("<time>1970-03-10T13:21:03Z</time>");
			TimestampType t = new TimestampType("DATE");
			t.unmarshalHtml(element, warnings);

			assertEquals(expected, t.getTimestamp());
			assertEquals(0, warnings.size());
		}

		//invalid date string
		{
			warnings.clear();
			org.jsoup.nodes.Element element = HtmlUtils.toElement("<time>March 10, 1970 at 1:21:03 PM</time>");
			TimestampType t = new TimestampType("DATE");
			t.unmarshalHtml(element, warnings);

			assertNull(t.getTimestamp());
			assertEquals(1, warnings.size());
		}
	}
}
