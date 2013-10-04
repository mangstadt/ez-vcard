package ezvcard.types;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.util.UtcOffset;

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
public class TimezoneTypeTest {
	private final TimeZone newYork = TimeZone.getTimeZone("America/New_York");

	@Test
	public void validate() {
		VCard vcard = new VCard();

		TimezoneType empty = new TimezoneType((UtcOffset) null);
		assertWarnings(2, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		TimezoneType withOffset = new TimezoneType(-5, 30);
		assertWarnings(0, withOffset.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, withOffset.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withOffset.validate(VCardVersion.V4_0, vcard));

		TimezoneType withText = new TimezoneType("America/New_York");
		assertWarnings(1, withText.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, withText.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withText.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void constructor_timezone() {
		TimezoneType tz = new TimezoneType(newYork);

		assertEquals(newYork.getID(), tz.getText());
		if (newYork.inDaylightTime(new Date())) {
			assertIntEquals(-4, tz.getHourOffset());
		} else {
			assertIntEquals(-5, tz.getHourOffset());
		}
		assertIntEquals(0, tz.getMinuteOffset());
	}

	@Test
	public void toTimeZone_offset() {
		TimezoneType t = new TimezoneType(-5, 30);
		TimeZone actual = t.toTimeZone();
		assertEquals(-(5 * 1000 * 60 * 60 + 30 * 1000 * 60), actual.getRawOffset());
	}

	@Test
	public void toTimeZone_timezone_id() {
		TimezoneType t = new TimezoneType(newYork.getID());
		TimeZone actual = t.toTimeZone();
		assertEquals(newYork, actual);
	}

	@Test
	public void toTimeZone_text() {
		TimezoneType t = new TimezoneType("text");
		TimeZone actual = t.toTimeZone();
		assertNull(actual);
	}

	@Test
	public void toTimeZone_text_and_offset() {
		TimezoneType t = new TimezoneType(-5, 30, "text");
		TimeZone actual = t.toTimeZone();
		assertEquals(-(5 * 1000 * 60 * 60 + 30 * 1000 * 60), actual.getRawOffset());
		assertEquals("text", actual.getID());
	}
}
