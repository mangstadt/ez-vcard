package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.property.Timezone;
import ezvcard.util.UtcOffset;

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
public class TimezoneScribeTest {
	private final TimezoneScribe scribe = new TimezoneScribe();
	private final Sensei<Timezone> sensei = new Sensei<Timezone>(scribe);

	private final UtcOffset offset = new UtcOffset(false, -5, 0);
	private final String offsetStrExtended = "-05:00";
	private final String offsetStrBasic = "-0500";
	private final String timezoneIdStr = "America/New_York";
	private final String textStr = "some text";
	private final TimeZone newYork = TimeZone.getTimeZone(timezoneIdStr);

	private final Timezone withOffset = new Timezone(offset);
	private final Timezone withTimezoneId = new Timezone(timezoneIdStr);
	private final Timezone withText = new Timezone(textStr);
	private final Timezone withOffsetAndTimezoneId = new Timezone(offset, timezoneIdStr);
	private final Timezone empty = new Timezone((String) null);

	@Test
	public void prepareParameters() {
		sensei.assertDataType(withOffset).run(VCardDataType.UTC_OFFSET);

		sensei.assertDataType(withTimezoneId).versions(V2_1).run(VCardDataType.UTC_OFFSET);
		sensei.assertDataType(withTimezoneId).versions(V3_0, V4_0).run(VCardDataType.TEXT);

		sensei.assertDataType(withText).versions(V2_1).run(VCardDataType.UTC_OFFSET);
		sensei.assertDataType(withText).versions(V3_0, V4_0).run(VCardDataType.TEXT);

		sensei.assertDataType(withOffsetAndTimezoneId).versions(V2_1, V3_0).run(VCardDataType.UTC_OFFSET);
		sensei.assertDataType(withOffsetAndTimezoneId).versions(V4_0).run(VCardDataType.TEXT);

		sensei.assertDataType(empty).versions(V2_1, V3_0).run(VCardDataType.UTC_OFFSET);
		sensei.assertDataType(empty).versions(V4_0).run(VCardDataType.TEXT);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withOffset).versions(V2_1, V4_0).run(offsetStrBasic);
		sensei.assertWriteText(withOffset).versions(V3_0).run(offsetStrExtended);

		sensei.assertWriteText(withTimezoneId).versions(V2_1).run(newYork.inDaylightTime(new Date()) ? "-0400" : "-0500");
		sensei.assertWriteText(withTimezoneId).versions(V3_0, V4_0).run(timezoneIdStr);

		sensei.assertWriteText(withText).versions(V2_1).run("");
		sensei.assertWriteText(withText).versions(V3_0, V4_0).run(textStr);

		sensei.assertWriteText(withOffsetAndTimezoneId).versions(V2_1).run(offsetStrBasic);
		sensei.assertWriteText(withOffsetAndTimezoneId).versions(V3_0).run(offsetStrExtended);
		sensei.assertWriteText(withOffsetAndTimezoneId).versions(V4_0).run(timezoneIdStr);

		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withOffset).run("<utc-offset>" + offsetStrBasic + "</utc-offset>");
		sensei.assertWriteXml(withTimezoneId).run("<text>" + timezoneIdStr + "</text>");
		sensei.assertWriteXml(withOffsetAndTimezoneId).run("<text>" + timezoneIdStr + "</text>");
		sensei.assertWriteXml(empty).run("<text/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withOffset).run(offsetStrExtended);
		sensei.assertWriteJson(withTimezoneId).run(timezoneIdStr);
		sensei.assertWriteJson(withOffsetAndTimezoneId).run(timezoneIdStr);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(offsetStrExtended).run(withOffset);
		sensei.assertParseText(offsetStrExtended).dataType(VCardDataType.UTC_OFFSET).run(withOffset);
		sensei.assertParseText(offsetStrExtended).dataType(VCardDataType.TEXT).run(withOffset);

		sensei.assertParseText(timezoneIdStr).versions(V2_1).cannotParse();
		sensei.assertParseText(timezoneIdStr).versions(V3_0).warnings(1).run(withTimezoneId);
		sensei.assertParseText(timezoneIdStr).versions(V3_0).dataType(VCardDataType.UTC_OFFSET).warnings(1).run(withTimezoneId);
		sensei.assertParseText(timezoneIdStr).versions(V3_0).dataType(VCardDataType.TEXT).run(withTimezoneId);
		sensei.assertParseText(timezoneIdStr).versions(V4_0).run(withTimezoneId);
		sensei.assertParseText(timezoneIdStr).versions(V4_0).dataType(VCardDataType.UTC_OFFSET).warnings(1).run(withTimezoneId);
		sensei.assertParseText(timezoneIdStr).versions(V4_0).dataType(VCardDataType.TEXT).run(withTimezoneId);

		sensei.assertParseText("").run(empty);
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<utc-offset>" + offsetStrBasic + "</utc-offset>").run(withOffset);
		sensei.assertParseXml("<text>" + timezoneIdStr + "</text>").run(withTimezoneId);
		sensei.assertParseXml("<utc-offset>" + offsetStrBasic + "</utc-offset><text>" + timezoneIdStr + "</text>").run(withTimezoneId);
		sensei.assertParseXml("<utc-offset>invalid</utc-offset>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<div>" + offsetStrExtended + "</div>").run(withOffset);
		sensei.assertParseHtml("<div>" + timezoneIdStr + "</div>").run(withTimezoneId);
		sensei.assertParseHtml("<div></div>").run(empty);
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(offsetStrExtended).run(withOffset);
		sensei.assertParseJson(offsetStrExtended).dataType(VCardDataType.UTC_OFFSET).run(withOffset);
		sensei.assertParseJson(offsetStrExtended).dataType(VCardDataType.TEXT).run(withOffset);

		sensei.assertParseJson(timezoneIdStr).run(withTimezoneId);
		sensei.assertParseJson(timezoneIdStr).dataType(VCardDataType.UTC_OFFSET).warnings(1).run(withTimezoneId);
		sensei.assertParseJson(timezoneIdStr).dataType(VCardDataType.TEXT).run(withTimezoneId);

		sensei.assertParseJson("").dataType(VCardDataType.TEXT).run(empty);
	}
}
