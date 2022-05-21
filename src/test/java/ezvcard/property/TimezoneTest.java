package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.Test;

import ezvcard.VCardVersion;

/*
 Copyright (c) 2012-2021, Michael Angstadt
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
public class TimezoneTest {
	@Test
	public void constructors() throws Exception {
		Timezone property = new Timezone((String) null);
		assertNull(property.getOffset());
		assertNull(property.getText());

		property = new Timezone(ZoneOffset.ofHours(1));
		assertEquals(ZoneOffset.ofHours(1), property.getOffset());
		assertNull(property.getText());

		property = new Timezone(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(1)));
		assertEquals(ZoneOffset.ofHours(1), property.getOffset());
		assertEquals("GMT+01:00", property.getText());

		property = new Timezone("text");
		assertNull(property.getOffset());
		assertEquals("text", property.getText());

		property = new Timezone(ZoneOffset.ofHours(1), "text");
		assertEquals(ZoneOffset.ofHours(1), property.getOffset());
		assertEquals("text", property.getText());
	}

	@Test
	public void set_value() {
		Timezone property = new Timezone((String) null);

		property.setOffset(ZoneOffset.ofHours(1));
		assertEquals(ZoneOffset.ofHours(1), property.getOffset());
		assertNull(property.getText());

		property.setText("text");
		assertEquals(ZoneOffset.ofHours(1), property.getOffset());
		assertEquals("text", property.getText());
	}

	@Test
	public void validate() {
		Timezone empty = new Timezone((ZoneOffset) null);
		assertValidate(empty).versions(VCardVersion.V2_1).run(8, 20);
		assertValidate(empty).versions(VCardVersion.V3_0).run(8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		Timezone withOffset = new Timezone(ZoneOffset.ofHoursMinutes(-5, -30));
		assertValidate(withOffset).run();

		Timezone withText = new Timezone("America/New_York");
		assertValidate(withText).versions(VCardVersion.V2_1).run(20);
		assertValidate(withText).versions(VCardVersion.V3_0).run();
		assertValidate(withText).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void toTimeZone_offset() {
		Timezone t = new Timezone(ZoneOffset.ofHoursMinutes(-5, -30));
		ZoneId actual = t.toTimeZone();
		assertEquals(ZoneOffset.ofHoursMinutes(-5, -30), actual);
	}

	@Test
	public void toTimeZone_timezone_id() {
		Timezone t = new Timezone("America/New_York");
		ZoneId actual = t.toTimeZone();
		assertEquals(ZoneId.of("America/New_York"), actual);
	}

	@Test
	public void toTimeZone_text() {
		Timezone t = new Timezone("text");
		ZoneId actual = t.toTimeZone();
		assertNull(actual);
	}

	@Test
	public void toTimeZone_text_and_offset() {
		Timezone t = new Timezone(ZoneOffset.ofHoursMinutes(-5, -30), "text");
		ZoneId actual = t.toTimeZone();
		
		assertEquals(ZoneOffset.ofHoursMinutes(-5, -30), actual.getRules().getOffset(Instant.now()));
		assertEquals("-05:30", actual.getId());
	}

	@Test
	public void toStringValues() {
		Timezone property = new Timezone("text");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Timezone original = new Timezone((String) null);
		assertCopy(original);

		original = new Timezone("text");
		assertCopy(original);

		original = new Timezone(ZoneOffset.ofHours(1));
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new Timezone((String) null),
			new Timezone("text"),
			new Timezone("text2"),
			new Timezone(ZoneOffset.ofHours(1)),
			new Timezone(ZoneOffset.ofHours(2)),
			new Timezone(ZoneOffset.ofHours(1), "text")
		);
		
		assertEqualsMethod(Timezone.class, "text")
		.constructor(new Class<?>[]{String.class}, (String)null).test()
		.constructor("text").test()
		.constructor(ZoneOffset.ofHours(1)).test()
		.constructor(ZoneOffset.ofHours(1), "text").test();
		//@formatter:on
	}
}
