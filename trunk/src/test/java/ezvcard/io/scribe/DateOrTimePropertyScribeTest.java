package ezvcard.io.scribe;

import static ezvcard.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.util.DefaultTimezoneRule;
import ezvcard.util.PartialDate;

/*
 Copyright (c) 2012-2014, Michael Angstadt
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

	private final DateOrTimeMarshallerImpl scribe = new DateOrTimeMarshallerImpl();
	private final Sensei<DateOrTimeTypeImpl> sensei = new Sensei<DateOrTimeTypeImpl>(scribe);

	private final Date date = date("1980-06-05");
	private final String dateStr = "19800605";
	private final String dateExtendedStr = "1980-06-05";

	private final Date dateTime = date("1980-06-05 13:10:20");
	private final String dateTimeStr = dateStr + "T131020+0100";
	private final String dateTimeExtendedStr = dateExtendedStr + "T13:10:20+01:00";

	private final PartialDate partialDate = PartialDate.date(null, 6, 5);
	private final PartialDate partialDateTime = PartialDate.dateTime(null, 6, 5, 13, 10, 20);

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
	public void writeText() {
		sensei.assertWriteText(withDate).versions(VCardVersion.V2_1, VCardVersion.V4_0).run(dateStr);
		sensei.assertWriteText(withDate).versions(VCardVersion.V3_0).run(dateExtendedStr);
		sensei.assertWriteText(withDateTime).versions(VCardVersion.V2_1, VCardVersion.V4_0).run(dateTimeStr);
		sensei.assertWriteText(withDateTime).versions(VCardVersion.V3_0).run(dateTimeExtendedStr);
		sensei.assertWriteText(withPartialDate).versions(VCardVersion.V2_1, VCardVersion.V3_0).run("");
		sensei.assertWriteText(withPartialDate).versions(VCardVersion.V4_0).run(partialDate.toDateAndOrTime(false));
		sensei.assertWriteText(withPartialDateTime).versions(VCardVersion.V2_1, VCardVersion.V3_0).run("");
		sensei.assertWriteText(withPartialDateTime).versions(VCardVersion.V4_0).run(partialDateTime.toDateAndOrTime(false));
		sensei.assertWriteText(withText).versions(VCardVersion.V2_1, VCardVersion.V3_0).run("");
		sensei.assertWriteText(withText).versions(VCardVersion.V4_0).run(textEscaped);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withDate).run("<date>" + dateStr + "</date>");
		sensei.assertWriteXml(withDateTime).run("<date-time>" + dateTimeStr + "</date-time>");
		sensei.assertWriteXml(withPartialDate).run("<date>" + partialDate.toDateAndOrTime(false) + "</date>");
		sensei.assertWriteXml(withPartialDateTime).run("<date-time>" + partialDateTime.toDateAndOrTime(false) + "</date-time>");
		sensei.assertWriteXml(withText).run("<text>" + text + "</text>");
		sensei.assertWriteXml(empty).run("<date-and-or-time/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withDate).run(dateExtendedStr);
		sensei.assertWriteJson(withDateTime).run(dateTimeExtendedStr);
		sensei.assertWriteJson(withPartialDate).run(partialDate.toDateAndOrTime(true));
		sensei.assertWriteJson(withPartialDateTime).run(partialDateTime.toDateAndOrTime(true));
		sensei.assertWriteJson(withText).run(text);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(dateExtendedStr).run(is(withDate));
		sensei.assertParseText(dateTimeExtendedStr).run(is(withDateTime));
		sensei.assertParseText(partialDate.toDateAndOrTime(false)).versions(VCardVersion.V2_1, VCardVersion.V3_0).cannotParse();
		sensei.assertParseText(partialDate.toDateAndOrTime(false)).versions(VCardVersion.V4_0).run(is(withPartialDate));
		sensei.assertParseText(partialDateTime.toDateAndOrTime(false)).versions(VCardVersion.V2_1, VCardVersion.V3_0).cannotParse();
		sensei.assertParseText(partialDateTime.toDateAndOrTime(false)).versions(VCardVersion.V4_0).run(is(withPartialDateTime));
		sensei.assertParseText(text).versions(VCardVersion.V2_1, VCardVersion.V3_0).cannotParse();
		sensei.assertParseText(text).versions(VCardVersion.V4_0).warnings(1).run(hasText(text));
		sensei.assertParseText(text).versions(VCardVersion.V2_1, VCardVersion.V3_0).dataType(VCardDataType.TEXT).cannotParse();
		sensei.assertParseText(text).versions(VCardVersion.V4_0).dataType(VCardDataType.TEXT).run(is(withText));
	}

	@Test
	public void parseXml() {
		String tags[] = { "date", "date-time", "date-and-or-time" };
		for (String tag : tags) {
			sensei.assertParseXml("<" + tag + ">" + dateStr + "</" + tag + ">").run(is(withDate));
			sensei.assertParseXml("<" + tag + ">" + dateTimeStr + "</" + tag + ">").run(is(withDateTime));
			sensei.assertParseXml("<" + tag + ">" + partialDate.toDateAndOrTime(false) + "</" + tag + ">").run(is(withPartialDate));
			sensei.assertParseXml("<" + tag + ">" + partialDateTime.toDateAndOrTime(false) + "</" + tag + ">").run(is(withPartialDateTime));
			sensei.assertParseXml("<" + tag + ">invalid</" + tag + ">").warnings(1).run(hasText("invalid"));
		}

		sensei.assertParseXml("<text>" + text + "</text>").run(is(withText));
		sensei.assertParseXml("<date>" + dateStr + "</date><date>invalid</date>").run(is(withDate)); //only considers the first
		sensei.assertParseXml("").cannotParse();

	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(dateExtendedStr).run(is(withDate));
		sensei.assertParseJson(dateTimeExtendedStr).run(is(withDateTime));
		sensei.assertParseJson(partialDate.toDateAndOrTime(true)).run(is(withPartialDate));
		sensei.assertParseJson(partialDateTime.toDateAndOrTime(true)).run(is(withPartialDateTime));
		sensei.assertParseJson(text).warnings(1).run(is(withText));
		sensei.assertParseJson(text).dataType(VCardDataType.TEXT).run(is(withText));
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<time datetime=\"" + dateExtendedStr + "\">June 5, 1980</time>").run(is(withDate));
		sensei.assertParseHtml("<time>" + dateExtendedStr + "</time>").run(is(withDate));
		sensei.assertParseHtml("<div>" + dateExtendedStr + "</div>").run(is(withDate));
		sensei.assertParseHtml("<time>June 5, 1980</time>").cannotParse();
	}

	private static class DateOrTimeMarshallerImpl extends DateOrTimePropertyScribe<DateOrTimeTypeImpl> {
		public DateOrTimeMarshallerImpl() {
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

	private Check<DateOrTimeTypeImpl> is(final DateOrTimeTypeImpl expected) {
		return new Check<DateOrTimeTypeImpl>() {
			public void check(DateOrTimeTypeImpl actual) {
				assertEquals(expected.getDate(), actual.getDate());
				assertEquals(expected.hasTime(), actual.hasTime());
				assertEquals(expected.getPartialDate(), actual.getPartialDate());
				assertEquals(expected.getText(), actual.getText());
			}
		};
	}
}
