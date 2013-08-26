package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.PartialDate;
import ezvcard.util.XCardElement;
import ezvcard.util.XmlUtils;

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
public class DateOrTimeTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCard vcard = new VCard();
	final VCardSubTypes subTypes = new VCardSubTypes();

	final Date date;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		date = c.getTime();
	}
	final String dateStr = "19800605";
	final String dateStrExtended = "1980-06-05";

	final Date dateTime;
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 10);
		c.set(Calendar.SECOND, 20);
		dateTime = c.getTime();
	}
	final String dateTimeRegex = dateStr + "T131020[-\\+]\\d{4}"; //account for local machine's timezone
	final String dateTimeExtendedRegex = dateStrExtended + "T13:10:20[-\\+]\\d{2}:\\d{2}";

	final PartialDate reducedAccuracyDate = PartialDate.date(null, 6, 5);
	final PartialDate reducedAccuracyDateTime = PartialDate.dateTime(null, 6, 5, 13, 10, 20);

	final String text = "Sometime in, ;1980;";
	final String textEscaped = "Sometime in\\, \\;1980\\;";

	/////////////////////////////////////////////////////////////////////////////////////////

	final DateOrTimeTypeImpl dateType = new DateOrTimeTypeImpl();
	{
		dateType.setDate(date, false);
	}
	DateOrTimeTypeImpl type;

	@Before
	public void before() {
		type = new DateOrTimeTypeImpl();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void marshalText_date_2_1() {
		VCardVersion version = VCardVersion.V2_1;

		String actual = dateType.marshalText(version, warnings, compatibilityMode);
		assertEquals(dateStr, actual);

		VCardSubTypes subTypes = dateType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_date_3_0() {
		VCardVersion version = VCardVersion.V3_0;

		String actual = dateType.marshalText(version, warnings, compatibilityMode);
		assertEquals(dateStr, actual);

		VCardSubTypes subTypes = dateType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_date_4_0() {
		VCardVersion version = VCardVersion.V4_0;

		String actual = dateType.marshalText(version, warnings, compatibilityMode);
		assertEquals(dateStr, actual);

		VCardSubTypes subTypes = dateType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_date() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append("date", dateStr);
		Document expected = xe.document();
		xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();
		dateType.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_date() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = dateType.marshalJson(version, warnings);

		assertJCardValue(JCardDataType.DATE, dateStrExtended, value);
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	final DateOrTimeTypeImpl dateTimeType = new DateOrTimeTypeImpl();
	{
		dateTimeType.setDate(dateTime, true);
	}

	@Test
	public void marshalText_datetime_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = dateTimeType.marshalText(version, warnings, compatibilityMode);
		assertTrue(actual.matches(dateTimeRegex));

		VCardSubTypes subTypes = dateTimeType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_datetime_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = dateTimeType.marshalText(version, warnings, compatibilityMode);
		assertTrue(actual.matches(dateTimeRegex));

		VCardSubTypes subTypes = dateTimeType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalText_datetime_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = dateTimeType.marshalText(version, warnings, compatibilityMode);
		assertTrue(actual.matches(dateTimeRegex));
		VCardSubTypes subTypes = dateTimeType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_datetime() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		Element element = xe.element();
		dateTimeType.marshalXml(element, version, warnings, compatibilityMode);

		assertTrue(XmlUtils.getFirstChildElement(element).getTextContent().matches(dateTimeRegex));
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_datetime() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = dateTimeType.marshalJson(version, warnings);

		assertEquals(JCardDataType.DATE_TIME, value.getDataType());
		assertEquals(1, value.getValues().size());
		String valueStr = (String) value.getValues().get(0).getValue();
		assertTrue(valueStr, valueStr.matches(dateTimeExtendedRegex));
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	final DateOrTimeTypeImpl reducedAccuracyDateType = new DateOrTimeTypeImpl();
	{
		reducedAccuracyDateType.setPartialDate(reducedAccuracyDate);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_reducedAccuracy_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		reducedAccuracyDateType.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_reducedAccuracy_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		reducedAccuracyDateType.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalText_reducedAccuracy_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = reducedAccuracyDateType.marshalText(version, warnings, compatibilityMode);
		assertEquals(reducedAccuracyDate.toDateAndOrTime(false), actual);

		VCardSubTypes subTypes = reducedAccuracyDateType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_reducedAccuracy() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append("date", reducedAccuracyDate.toDateAndOrTime(false));
		Document expected = xe.document();
		xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();
		reducedAccuracyDateType.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_reducedAccuracy() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = reducedAccuracyDateType.marshalJson(version, warnings);

		assertJCardValue(JCardDataType.DATE, reducedAccuracyDate.toDateAndOrTime(true), value);
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	final DateOrTimeTypeImpl reducedAccuracyDateTimeType = new DateOrTimeTypeImpl();
	{
		reducedAccuracyDateTimeType.setPartialDate(reducedAccuracyDateTime);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_reducedAccuracyDateTime_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		reducedAccuracyDateTimeType.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_reducedAccuracyDateTime_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		reducedAccuracyDateTimeType.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalText_reducedAccuracyDateTime_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = reducedAccuracyDateTimeType.marshalText(version, warnings, compatibilityMode);
		assertEquals(reducedAccuracyDateTime.toDateAndOrTime(false), actual);

		VCardSubTypes subTypes = reducedAccuracyDateTimeType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getValue());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_reducedAccuracyDateTime() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append("date-time", reducedAccuracyDateTime.toDateAndOrTime(false));
		Document expected = xe.document();
		xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();
		reducedAccuracyDateTimeType.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_reducedAccuracyDateTime() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = reducedAccuracyDateTimeType.marshalJson(version, warnings);

		assertJCardValue(JCardDataType.DATE_TIME, reducedAccuracyDateTime.toDateAndOrTime(true), value);
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	final DateOrTimeTypeImpl textType = new DateOrTimeTypeImpl();
	{
		textType.setText(text);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_text_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		textType.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_text_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		textType.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalText_text_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = textType.marshalText(version, warnings, compatibilityMode);
		assertEquals(actual, textEscaped);

		VCardSubTypes subTypes = textType.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertNull(subTypes.getCalscale());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append(ValueParameter.TEXT, text);
		Document expected = xe.document();
		xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();
		textType.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = textType.marshalJson(version, warnings);

		assertJCardValue(JCardDataType.TEXT, text, value);
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	final DateOrTimeTypeImpl nothingType = new DateOrTimeTypeImpl();

	@Test(expected = SkipMeException.class)
	public void marshalText_nothing_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		nothingType.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_nothing_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		nothingType.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_nothing_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		nothingType.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalXml_nothing() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		nothingType.marshalXml(xe.element(), version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalJson_nothing() {
		VCardVersion version = VCardVersion.V4_0;
		nothingType.marshalJson(version, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void unmarshalText_date_2_1() {
		VCardVersion version = VCardVersion.V2_1;

		type.unmarshalText(subTypes, dateStrExtended, version, warnings, compatibilityMode);

		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_date_3_0() {
		VCardVersion version = VCardVersion.V3_0;

		type.unmarshalText(subTypes, dateStrExtended, version, warnings, compatibilityMode);

		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_date_4_0() {
		VCardVersion version = VCardVersion.V4_0;

		type.unmarshalText(subTypes, dateStrExtended, version, warnings, compatibilityMode);

		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_date_invalid_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		type.unmarshalText(subTypes, "invalid", version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertNull(type.getPartialDate());
		assertEquals("invalid", type.getText());
		assertWarnings(1, warnings);
	}

	@Test
	public void unmarshalXml_date() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append("date", dateStrExtended);
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_date_invalid() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append("date", "invalid");
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertNull(type.getPartialDate());
		assertEquals("invalid", type.getText());
		assertWarnings(1, warnings);
	}

	@Test
	public void unmarshalJson_date() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(JCardDataType.DATE, dateStrExtended);

		type.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_date_invalid() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(JCardDataType.DATE, "invalid");

		type.unmarshalJson(subTypes, value, version, warnings);

		assertNull(type.getDate());
		assertNull(type.getPartialDate());
		assertEquals("invalid", type.getText());
		assertWarnings(1, warnings);
	}

	@Test
	public void unmarshalHtml_date_in_attribute() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<time datetime=\"" + dateStrExtended + "\">June 5, 1980</time>");

		type.unmarshalHtml(element, warnings);

		assertEquals(date, type.getDate());
		assertWarnings(0, warnings);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_date_in_text_content() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<time>" + dateStrExtended + "</time>");

		type.unmarshalHtml(element, warnings);

		assertEquals(date, type.getDate());
		assertWarnings(0, warnings);
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_date_invalid() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<time>June 5, 1980</time>");

		type.unmarshalHtml(element, warnings);
	}

	@Test
	public void unmarshalHtml_date_not_time_tag() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + dateStrExtended + "</div>");

		type.unmarshalHtml(element, warnings);

		assertEquals(date, type.getDate());
		assertWarnings(0, warnings);
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void unmarshalText_datetime_2_1() {
		VCardVersion version = VCardVersion.V2_1;

		type.unmarshalText(subTypes, dateStr, version, warnings, compatibilityMode);
		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_datetime_3_0() {
		VCardVersion version = VCardVersion.V3_0;

		type.unmarshalText(subTypes, dateStr, version, warnings, compatibilityMode);
		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_datetime_4_0() {
		VCardVersion version = VCardVersion.V4_0;

		type.unmarshalText(subTypes, dateStr, version, warnings, compatibilityMode);
		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_datetime() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append("date-time", dateStr);
		type.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_date_and_or_time() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append(ValueParameter.DATE_AND_OR_TIME, dateStr);
		type.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_datetime() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(JCardDataType.DATE, dateStr);

		type.unmarshalJson(subTypes, value, version, warnings);
		assertEquals(date, type.getDate());
		assertNull(type.getPartialDate());
		assertNull(type.getText());
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	@Test(expected = SkipMeException.class)
	public void unmarshalText_reducedAccuracyDate_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		type.unmarshalText(subTypes, reducedAccuracyDate.toDateAndOrTime(false), version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_reducedAccuracyDate_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		type.unmarshalText(subTypes, reducedAccuracyDate.toDateAndOrTime(false), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalText_reducedAccuracyDate_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		type.unmarshalText(subTypes, reducedAccuracyDate.toDateAndOrTime(false), version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertEquals(reducedAccuracyDate, type.getPartialDate());
		assertNull(type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_reducedAccuracyDate() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append(ValueParameter.DATE_AND_OR_TIME, reducedAccuracyDate.toDateAndOrTime(false));
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertEquals(reducedAccuracyDate, type.getPartialDate());
		assertNull(type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_reducedAccuracyDate() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(JCardDataType.DATE, reducedAccuracyDate.toDateAndOrTime(true));

		type.unmarshalJson(subTypes, value, version, warnings);

		assertNull(type.getDate());
		assertEquals(reducedAccuracyDate, type.getPartialDate());
		assertNull(type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	@Test(expected = SkipMeException.class)
	public void unmarshalText_reducedAccuracyDateTime_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		type.unmarshalText(subTypes, reducedAccuracyDateTime.toDateAndOrTime(false), version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_reducedAccuracyDateTime_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		type.unmarshalText(subTypes, reducedAccuracyDateTime.toDateAndOrTime(false), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalText_reducedAccuracyDateTime_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		type.unmarshalText(subTypes, reducedAccuracyDateTime.toDateAndOrTime(false), version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertEquals(reducedAccuracyDateTime, type.getPartialDate());
		assertNull(type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_reducedAccuracyDateTime() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append(ValueParameter.DATE_AND_OR_TIME, reducedAccuracyDateTime.toDateAndOrTime(false));
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertEquals(reducedAccuracyDateTime, type.getPartialDate());
		assertNull(type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_reducedAccuracyDateTime() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(JCardDataType.DATE_TIME, reducedAccuracyDateTime.toDateAndOrTime(true));

		type.unmarshalJson(subTypes, value, version, warnings);

		assertNull(type.getDate());
		assertEquals(reducedAccuracyDateTime, type.getPartialDate());
		assertNull(type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	@Test(expected = SkipMeException.class)
	public void unmarshalText_text_2_1() {
		VCardVersion version = VCardVersion.V2_1;

		type.unmarshalText(subTypes, textEscaped, version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_text_3_0() {
		VCardVersion version = VCardVersion.V3_0;

		type.unmarshalText(subTypes, textEscaped, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalText_text_4_0_with_value_parameter() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setValue(ValueParameter.TEXT);

		type.unmarshalText(subTypes, textEscaped, version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertNull(type.getPartialDate());
		assertEquals(text, type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_text_4_0_without_value_parameter() {
		VCardVersion version = VCardVersion.V4_0;

		type.unmarshalText(subTypes, textEscaped, version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertNull(type.getPartialDate());
		assertEquals(text, type.getText());
		assertWarnings(1, warnings);
	}

	@Test
	public void unmarshalXml_text() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(DateOrTimeTypeImpl.NAME.toLowerCase());
		xe.append("text", text);
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, version, warnings, compatibilityMode);

		assertNull(type.getDate());
		assertNull(type.getPartialDate());
		assertEquals(text, type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_text() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(JCardDataType.TEXT, text);

		type.unmarshalJson(subTypes, value, version, warnings);

		assertNull(type.getDate());
		assertNull(type.getPartialDate());
		assertEquals(text, type.getText());
		assertTrue(warnings.isEmpty());
		assertWarnings(0, warnings);
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static class DateOrTimeTypeImpl extends DateOrTimeType {
		public static final String NAME = "DATE";

		public DateOrTimeTypeImpl() {
			super(NAME);
		}
	}
}
