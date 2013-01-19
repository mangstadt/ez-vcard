package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.CalscaleParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HtmlUtils;
import ezvcard.util.XCardElement;
import ezvcard.util.XmlUtils;

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
public class DateOrTimeTypeTest {
	@Test
	public void marshalDate() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		Date date = c.getTime();
		DateOrTimeType t = new DateOrTimeType("DATE");
		t.setDate(date, false);

		String expected = "19800605";

		//v2.1
		VCardVersion version = VCardVersion.V2_1;
		String actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.DATE, subTypes.getValue());

		//v3.0
		version = VCardVersion.V3_0;
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.DATE, subTypes.getValue());

		//v4.0
		version = VCardVersion.V4_0;
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.DATE_AND_OR_TIME, subTypes.getValue());
		assertEquals(CalscaleParameter.GREGORIAN, subTypes.getCalscale());

		//xCard
		version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("date");
		xe.append("date-and-or-time", expected);
		Document expectedDoc = xe.getDocument();
		xe = new XCardElement("date");
		Document actualDoc = xe.getDocument();
		Element element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
	}

	@Test
	public void marshalDateTime() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 10);
		c.set(Calendar.SECOND, 20);

		Date date = c.getTime();
		DateOrTimeType t = new DateOrTimeType("DATE");
		t.setDate(date, true);

		//uses the local machine's timezone
		String expected = "19800605T131020[-\\+]\\d{4}";

		//v2.1
		VCardVersion version = VCardVersion.V2_1;
		String actual = t.marshalText(version, warnings, compatibilityMode);
		assertTrue(actual.matches(expected));
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.DATE_TIME, subTypes.getValue());

		//v3.0
		version = VCardVersion.V3_0;
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertTrue(actual.matches(expected));
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.DATE_TIME, subTypes.getValue());

		//v4.0
		version = VCardVersion.V4_0;
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertTrue(actual.matches(expected));
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.DATE_AND_OR_TIME, subTypes.getValue());
		assertEquals(CalscaleParameter.GREGORIAN, subTypes.getCalscale());

		//xCard
		version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("date");
		Element element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertTrue(XmlUtils.getFirstChildElement(element).getTextContent().matches(expected));
	}

	@Test
	public void marshalReducedAccuracyDate() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		DateOrTimeType t = new DateOrTimeType("DATE");
		t.setReducedAccuracyDate("--0214");

		String expected = "--0214";

		//v2.1
		VCardVersion version = VCardVersion.V2_1;
		try {
			t.marshalText(version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
			//reduced accuracy dates are not supported in 2.1
		}

		//v3.0
		version = VCardVersion.V3_0;
		try {
			t.marshalText(version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
			//reduced accuracy dates are not supported in 3.0
		}

		//v4.0
		version = VCardVersion.V4_0;
		String actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(actual, expected);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.DATE_AND_OR_TIME, subTypes.getValue());
		assertEquals(CalscaleParameter.GREGORIAN, subTypes.getCalscale());

		//xCard
		version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("date");
		xe.append("date-and-or-time", expected);
		Document expectedDoc = xe.getDocument();
		xe = new XCardElement("date");
		Document actualDoc = xe.getDocument();
		Element element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
	}

	@Test
	public void marshalText() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		DateOrTimeType t = new DateOrTimeType("DATE");
		t.setText("Sometime around ;1980;");

		String expected = "Sometime around \\;1980\\;";

		//v2.1
		VCardVersion version = VCardVersion.V2_1;
		try {
			t.marshalText(version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
			//text values are not supported in 2.1
		}

		//v3.0
		version = VCardVersion.V3_0;
		try {
			t.marshalText(version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
			//text values are not supported in 3.0
		}

		//v4.0
		version = VCardVersion.V4_0;
		String actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(actual, expected);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertNull(subTypes.getCalscale());

		//xCard
		version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("date");
		xe.appendText("Sometime around ;1980;");
		Document expectedDoc = xe.getDocument();
		xe = new XCardElement("date");
		Document actualDoc = xe.getDocument();
		Element element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
	}

	@Test
	public void unmarshalDate() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();

		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		Date expected = c.getTime();

		//2.1
		VCardVersion version = VCardVersion.V2_1;
		DateOrTimeType t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, "1980-06-05", version, warnings, compatibilityMode);
		assertEquals(expected, t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());

		//3.0
		version = VCardVersion.V3_0;
		t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, "1980-06-05", version, warnings, compatibilityMode);
		assertEquals(expected, t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());

		//4.0
		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, "1980-06-05", version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertEquals("1980-06-05", t.getReducedAccuracyDate()); //it thinks it's reduced accuracy because it has dashes
		assertNull(t.getText());

		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, "19800605", version, warnings, compatibilityMode);
		assertEquals(expected, t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());

		//xCard
		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		XCardElement xe = new XCardElement("date");
		xe.append("date-and-or-time", "1980-06-05");
		Element input = xe.getElement();
		t.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertEquals("1980-06-05", t.getReducedAccuracyDate()); //it thinks it's reduced accuracy because it has dashes
		assertNull(t.getText());

		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		xe = new XCardElement("date");
		xe.append("date-and-or-time", "19800605");
		input = xe.getElement();
		t.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);
		assertEquals(expected, t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());
	}

	@Test
	public void unmarshalReducedAccuracyDate() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();

		String value = "--0201";
		String expected = value;

		//2.1
		VCardVersion version = VCardVersion.V2_1;
		DateOrTimeType t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, value, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());

		//3.0
		version = VCardVersion.V3_0;
		t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, value, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());

		//4.0
		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, value, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertEquals(expected, t.getReducedAccuracyDate());
		assertNull(t.getText());

		//xCard
		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		XCardElement xe = new XCardElement("date");
		xe.append("date-and-or-time", value);
		Element input = xe.getElement();
		t.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertEquals(expected, t.getReducedAccuracyDate());
		assertNull(t.getText());
	}

	@Test
	public void unmarshalText() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setValue(ValueParameter.TEXT);

		String value = "Some \\;text\\;.";
		String expected = "Some ;text;.";

		//2.1
		VCardVersion version = VCardVersion.V2_1;
		DateOrTimeType t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, value, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());

		//3.0
		version = VCardVersion.V3_0;
		t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, value, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertNull(t.getText());

		//4.0
		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		t.unmarshalText(subTypes, value, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertEquals(expected, t.getText());

		//xCard
		version = VCardVersion.V4_0;
		t = new DateOrTimeType("DATE");
		XCardElement xe = new XCardElement("date");
		xe.append("date-and-or-time", "Some ;text;.");
		Element input = xe.getElement();
		t.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);
		assertNull(t.getDate());
		assertNull(t.getReducedAccuracyDate());
		assertEquals(expected, t.getText());
	}

	@Test
	public void unmarshalHtml() throws Exception {
		List<String> warnings = new ArrayList<String>();

		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 1970);
		c.set(Calendar.MONTH, Calendar.MARCH);
		c.set(Calendar.DAY_OF_MONTH, 10);
		Date expected = c.getTime();

		//valid date string
		{
			warnings.clear();
			org.jsoup.nodes.Element element = HtmlUtils.toElement("<time datetime=\"1970-03-10\">March 10, 1970</time>");
			DateOrTimeType t = new DateOrTimeType("DATE");
			t.unmarshalHtml(element, warnings);

			assertEquals(expected, t.getDate());
			assertEquals(0, warnings.size());
		}

		//date string in tag text
		{
			warnings.clear();
			org.jsoup.nodes.Element element = HtmlUtils.toElement("<time>1970-03-10</time>");
			DateOrTimeType t = new DateOrTimeType("DATE");
			t.unmarshalHtml(element, warnings);

			assertEquals(expected, t.getDate());
			assertEquals(0, warnings.size());
		}

		//invalid date string
		{
			warnings.clear();
			org.jsoup.nodes.Element element = HtmlUtils.toElement("<time>March 10, 1970</time>");
			DateOrTimeType t = new DateOrTimeType("DATE");
			t.unmarshalHtml(element, warnings);

			assertNull(t.getDate());
			assertEquals(1, warnings.size());
		}
	}
}
