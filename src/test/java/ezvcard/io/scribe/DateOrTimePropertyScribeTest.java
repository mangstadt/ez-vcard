package ezvcard.io.scribe;

import static ezvcard.VCardDataType.DATE;
import static ezvcard.VCardDataType.DATE_TIME;
import static ezvcard.VCardDataType.TEXT;
import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import ezvcard.io.scribe.Sensei.Check;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.util.DefaultTimezoneRule;
import ezvcard.util.PartialDate;

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
public class DateOrTimePropertyScribeTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final DateOrTimeScribeImpl scribe = new DateOrTimeScribeImpl();
	private final Sensei<DateOrTimeTypeImpl> sensei = new Sensei<DateOrTimeTypeImpl>(scribe);

	private final Date date = date("1980-06-05");
	private final String dateStr = "19800605";
	private final String dateExtendedStr = "1980-06-05";

	private final Date dateTime = date("1980-06-05 13:10:20");
	private final String dateTimeStr = dateStr + "T131020+0100";
	private final String dateTimeExtendedStr = dateExtendedStr + "T13:10:20+01:00";

	private final PartialDate partialDate = PartialDate.builder().month(6).date(5).build();
	private final PartialDate partialTime = PartialDate.builder().hour(12).build();
	private final PartialDate partialDateTime = PartialDate.builder().month(6).date(5).hour(13).minute(10).second(20).build();

	private final String text = "Sometime in, ;1980;";
	private final String textEscaped = "Sometime in\\, \\;1980\\;";

	private final DateOrTimeTypeImpl withDate = new DateOrTimeTypeImpl();
	{
		withDate.setDate(date, false);
	}
	private final DateOrTimeTypeImpl withDateTime = new DateOrTimeTypeImpl();
	{
		withDateTime.setDate(dateTime, true);
	}
	private final DateOrTimeTypeImpl withPartialDate = new DateOrTimeTypeImpl();
	{
		withPartialDate.setPartialDate(partialDate);
	}
	private final DateOrTimeTypeImpl withPartialTime = new DateOrTimeTypeImpl();
	{
		withPartialTime.setPartialDate(partialTime);
	}
	private final DateOrTimeTypeImpl withPartialDateTime = new DateOrTimeTypeImpl();
	{
		withPartialDateTime.setPartialDate(partialDateTime);
	}
	private final DateOrTimeTypeImpl withText = new DateOrTimeTypeImpl();
	{
		withText.setText(text);
	}
	private final DateOrTimeTypeImpl empty = new DateOrTimeTypeImpl();

	@Test
	public void dataType() {
		sensei.assertDataType(withDate).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withDate).versions(V4_0).run(DATE);
		sensei.assertDataType(withDateTime).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withDateTime).versions(V4_0).run(DATE_TIME);
		sensei.assertDataType(withPartialDate).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withPartialDate).versions(V4_0).run(DATE);
		sensei.assertDataType(withPartialTime).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withPartialTime).versions(V4_0).run(DATE_TIME);
		sensei.assertDataType(withPartialDateTime).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withPartialDateTime).versions(V4_0).run(DATE_TIME);
		sensei.assertDataType(withText).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withText).versions(V4_0).run(TEXT);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withDate).versions(V2_1, V4_0).run(dateStr);
		sensei.assertWriteText(withDate).versions(V3_0).run(dateExtendedStr);
		sensei.assertWriteText(withDateTime).versions(V2_1, V4_0).run(dateTimeStr);
		sensei.assertWriteText(withDateTime).versions(V3_0).run(dateTimeExtendedStr);
		sensei.assertWriteText(withPartialDate).versions(V2_1, V3_0).run("");
		sensei.assertWriteText(withPartialDate).versions(V4_0).run(partialDate.toISO8601(false));
		sensei.assertWriteText(withPartialTime).versions(V2_1, V3_0).run("");
		sensei.assertWriteText(withPartialTime).versions(V4_0).run(partialTime.toISO8601(false));
		sensei.assertWriteText(withPartialDateTime).versions(V2_1, V3_0).run("");
		sensei.assertWriteText(withPartialDateTime).versions(V4_0).run(partialDateTime.toISO8601(false));
		sensei.assertWriteText(withText).versions(V2_1, V3_0).run("");
		sensei.assertWriteText(withText).versions(V4_0).run(textEscaped);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withDate).run("<date>" + dateStr + "</date>");
		sensei.assertWriteXml(withDateTime).run("<date-time>" + dateTimeStr + "</date-time>");
		sensei.assertWriteXml(withPartialDate).run("<date>" + partialDate.toISO8601(false) + "</date>");
		sensei.assertWriteXml(withPartialTime).run("<time>" + partialTime.toISO8601(false) + "</time>");
		sensei.assertWriteXml(withPartialDateTime).run("<date-time>" + partialDateTime.toISO8601(false) + "</date-time>");
		sensei.assertWriteXml(withText).run("<text>" + text + "</text>");
		sensei.assertWriteXml(empty).run("<date-and-or-time/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withDate).run(dateExtendedStr);
		sensei.assertWriteJson(withDateTime).run(dateTimeExtendedStr);
		sensei.assertWriteJson(withPartialDate).run(partialDate.toISO8601(true));
		sensei.assertWriteJson(withPartialTime).run(partialTime.toISO8601(true));
		sensei.assertWriteJson(withPartialDateTime).run(partialDateTime.toISO8601(true));
		sensei.assertWriteJson(withText).run(text);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(dateExtendedStr).run(withDate);
		sensei.assertParseText(dateTimeExtendedStr).run(withDateTime);
		sensei.assertParseText(partialDate.toISO8601(false)).versions(V2_1, V3_0).cannotParse();
		sensei.assertParseText(partialDate.toISO8601(false)).versions(V4_0).run(withPartialDate);
		sensei.assertParseText(partialTime.toISO8601(false)).versions(V2_1, V3_0).cannotParse();
		sensei.assertParseText(partialTime.toISO8601(false)).versions(V4_0).run(withPartialTime);
		sensei.assertParseText(partialDateTime.toISO8601(false)).versions(V2_1, V3_0).cannotParse();
		sensei.assertParseText(partialDateTime.toISO8601(false)).versions(V4_0).run(withPartialDateTime);
		sensei.assertParseText(text).versions(V2_1, V3_0).cannotParse();
		sensei.assertParseText(text).versions(V4_0).warnings(1).run(hasText(text));
		sensei.assertParseText(text).versions(V2_1, V3_0).dataType(TEXT).cannotParse();
		sensei.assertParseText(text).versions(V4_0).dataType(TEXT).run(withText);
	}

	@Test
	public void parseXml() {
		String tags[] = { "date", "date-time", "date-and-or-time" };
		for (String tag : tags) {
			String format = "<" + tag + ">%s</" + tag + ">";
			sensei.assertParseXml(String.format(format, dateStr)).run(withDate);
			sensei.assertParseXml(String.format(format, dateTimeStr)).run(withDateTime);
			sensei.assertParseXml(String.format(format, partialDate.toISO8601(false))).run(withPartialDate);
			sensei.assertParseXml(String.format(format, partialTime.toISO8601(false))).run(withPartialTime);
			sensei.assertParseXml(String.format(format, partialDateTime.toISO8601(false))).run(withPartialDateTime);
			sensei.assertParseXml(String.format(format, "invalid")).warnings(1).run(hasText("invalid"));
		}

		sensei.assertParseXml("<text>" + text + "</text>").run(withText);
		sensei.assertParseXml("<date>" + dateStr + "</date><date>invalid</date>").run(withDate); //only considers the first
		sensei.assertParseXml("").cannotParse();

	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(dateExtendedStr).run(withDate);
		sensei.assertParseJson(dateTimeExtendedStr).run(withDateTime);
		sensei.assertParseJson(partialDate.toISO8601(true)).run(withPartialDate);
		sensei.assertParseJson(partialTime.toISO8601(true)).run(withPartialTime);
		sensei.assertParseJson(partialDateTime.toISO8601(true)).run(withPartialDateTime);
		sensei.assertParseJson(text).warnings(1).run(withText);
		sensei.assertParseJson(text).dataType(TEXT).run(withText);
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<time datetime=\"" + dateExtendedStr + "\">June 5, 1980</time>").run(withDate);
		sensei.assertParseHtml("<time>" + dateExtendedStr + "</time>").run(withDate);
		sensei.assertParseHtml("<div>" + dateExtendedStr + "</div>").run(withDate);
		sensei.assertParseHtml("<time>June 5, 1980</time>").cannotParse();
	}

	private static class DateOrTimeScribeImpl extends DateOrTimePropertyScribe<DateOrTimeTypeImpl> {
		public DateOrTimeScribeImpl() {
			super(DateOrTimeTypeImpl.class, "DATETIME");
		}

		@Override
		protected DateOrTimeTypeImpl newInstance(String text) {
			DateOrTimeTypeImpl property = new DateOrTimeTypeImpl();
			property.setText(text);
			return property;
		}

		@Override
		protected DateOrTimeTypeImpl newInstance(Date date, boolean hasTime) {
			DateOrTimeTypeImpl property = new DateOrTimeTypeImpl();
			property.setDate(date, hasTime);
			return property;
		}

		@Override
		protected DateOrTimeTypeImpl newInstance(PartialDate partialDate) {
			DateOrTimeTypeImpl property = new DateOrTimeTypeImpl();
			property.setPartialDate(partialDate);
			return property;
		}
	}

	private static class DateOrTimeTypeImpl extends DateOrTimeProperty {
		public DateOrTimeTypeImpl() {
			super((Date) null);
		}
	}

	private Check<DateOrTimeTypeImpl> hasText(final String text) {
		return new Check<DateOrTimeTypeImpl>() {
			public void check(DateOrTimeTypeImpl property) {
				assertNull(property.getDate());
				assertNull(property.getPartialDate());
				assertEquals(text, property.getText());
			}
		};
	}
}
