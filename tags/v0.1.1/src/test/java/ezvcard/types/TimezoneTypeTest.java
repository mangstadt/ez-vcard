package ezvcard.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ValueParameter;

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
public class TimezoneTypeTest {
	@Test
	public void setMinuteOffset() {
		try {
			new TimezoneType().setMinuteOffset(-1);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		try {
			new TimezoneType().setMinuteOffset(60);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		try {
			new TimezoneType(0, -1);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		try {
			new TimezoneType(0, 60);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}
	}

	@Test
	public void toTimeZone() {
		TimezoneType t = new TimezoneType(-5, 30);
		TimeZone tz = t.toTimeZone();
		assertEquals(-(5 * 1000 * 60 * 60 + 30 * 1000 * 60), tz.getRawOffset());
	}

	@Test
	public void doMarshalValue() {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC2426;
		TimezoneType t;
		String expected, actual;

		//just offset
		t = new TimezoneType(-5, 30);
		expected = "-05:30";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertNull(t.getSubTypes().getValue());

		//offset and short text
		t = new TimezoneType(-5, 30, "EST", null);
		expected = "-05:30;EST;";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(ValueParameter.TEXT, t.getSubTypes().getValue());

		//offset and long text
		t = new TimezoneType(-5, 30, null, "America/New;_;York");
		expected = "-05:30;;America/New\\;_\\;York";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(ValueParameter.TEXT, t.getSubTypes().getValue());

		//offset and both text
		t = new TimezoneType(-5, 30, "EST", "America/New;_;York");
		expected = "-05:30;EST;America/New\\;_\\;York";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(ValueParameter.TEXT, t.getSubTypes().getValue());

		//no offset, no text
		t = new TimezoneType();
		expected = "+00:00";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertNull(t.getSubTypes().getValue());
	}

	@Test
	public void doUnmarshalValue() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC2426;
		VCardSubTypes subTypes = new VCardSubTypes();
		TimezoneType t;

		//offset
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "-05:30", version, warnings, compatibilityMode);
		assertEquals(-5, t.getHourOffset());
		assertEquals(30, t.getMinuteOffset());

		//offset and short text
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "-05:30;EST", version, warnings, compatibilityMode);
		assertEquals(-5, t.getHourOffset());
		assertEquals(30, t.getMinuteOffset());
		assertEquals("EST", t.getShortText());
		assertEquals(null, t.getLongText());

		//offset and short text with trailing semicolon
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "-05:30;EST;", version, warnings, compatibilityMode);
		assertEquals(-5, t.getHourOffset());
		assertEquals(30, t.getMinuteOffset());
		assertEquals("EST", t.getShortText());
		assertEquals(null, t.getLongText());

		//offset and long text
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "-05:30;;America/New\\;_\\;York", version, warnings, compatibilityMode);
		assertEquals(-5, t.getHourOffset());
		assertEquals(30, t.getMinuteOffset());
		assertEquals(null, t.getShortText());
		assertEquals("America/New;_;York", t.getLongText());

		//offset, short text, and long text
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "-05:30;EST;America/New\\;_\\;York", version, warnings, compatibilityMode);
		assertEquals(-5, t.getHourOffset());
		assertEquals(30, t.getMinuteOffset());
		assertEquals("EST", t.getShortText());
		assertEquals("America/New;_;York", t.getLongText());
	}
}
