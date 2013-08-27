package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardValue;
import ezvcard.util.XCardElement;

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
public class TimestampTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();

	final String basic = "19800605T131020Z";
	final String extended = "1980-06-05T13:10:20Z";
	final Date timestampDate;
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 10);
		c.set(Calendar.SECOND, 20);
		timestampDate = c.getTime();
	}
	final TimestampType timestamp = new TimestampType("NAME", timestampDate);
	final TimestampType noValue = new TimestampType("NAME");
	TimestampType t;

	@Before
	public void before() {
		t = new TimestampType("NAME");
		subTypes.clear();
		warnings.clear();
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = timestamp.marshalText(version, warnings, compatibilityMode);

		assertEquals(basic, actual);
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_no_value() {
		VCardVersion version = VCardVersion.V2_1;
		noValue.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(timestamp.getTypeName().toLowerCase());
		xe.append(ValueParameter.TIMESTAMP, basic);
		Document expectedDoc = xe.document();
		xe = new XCardElement(timestamp.getTypeName().toLowerCase());
		Document actualDoc = xe.document();
		Element element = xe.element();
		timestamp.marshalXml(element, version, warnings, compatibilityMode);

		assertXMLEqual(expectedDoc, actualDoc);
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void marshalXml_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(noValue.getTypeName().toLowerCase());
		noValue.marshalXml(xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = timestamp.marshalJson(version, warnings);

		assertJCardValue(ValueParameter.TIMESTAMP, extended, value);
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void marshalJson_no_value() {
		VCardVersion version = VCardVersion.V4_0;
		noValue.marshalJson(version, warnings);
	}

	@Test
	public void unmarshalText_basic() {
		VCardVersion version = VCardVersion.V2_1;
		t.unmarshalText(subTypes, basic, version, warnings, compatibilityMode);

		assertEquals(timestampDate, t.getTimestamp());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_extended() {
		VCardVersion version = VCardVersion.V2_1;
		t.unmarshalText(subTypes, extended, version, warnings, compatibilityMode);

		assertEquals(timestampDate, t.getTimestamp());
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_bad_timestamp() {
		VCardVersion version = VCardVersion.V2_1;
		t.unmarshalText(subTypes, "bad value", version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("name");
		xe.append(ValueParameter.TIMESTAMP, basic);
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertEquals(timestampDate, t.getTimestamp());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_no_timestamp_element() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("name");
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_bad_timestamp() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("name");
		xe.append(ValueParameter.TIMESTAMP, "bad value");
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalHtml_datetime_attribute() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<time datetime=\"" + extended + "\">June 5, 1980</time>");
		t.unmarshalHtml(element, warnings);

		assertEquals(timestampDate, t.getTimestamp());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_time_tag_text() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<time>" + extended + "</time>");
		t.unmarshalHtml(element, warnings);

		assertEquals(timestampDate, t.getTimestamp());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_tag_text() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + extended + "</div>");
		t.unmarshalHtml(element, warnings);

		assertEquals(timestampDate, t.getTimestamp());
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_bad_value() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>bad value</div>");
		t.unmarshalHtml(element, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(ValueParameter.TIMESTAMP, extended);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(timestampDate, t.getTimestamp());
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalJson_bad_value() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(ValueParameter.TIMESTAMP, "bad value");

		t.unmarshalJson(subTypes, value, version, warnings);
	}

	@Test
	public void setTimestamp() {
		TimestampType t = new TimestampType("NAME");
		assertNull(t.getTimestamp());
		t.setTimestamp(timestampDate);
		assertEquals(timestampDate, t.getTimestamp());
	}
}
