package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static ezvcard.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import ezvcard.VCardVersion;
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
public class DateOrTimePropertyTest {
	@Test
	public void constructors() throws Exception {
		DateOrTimePropertyImpl property = new DateOrTimePropertyImpl();
		assertNull(property.getDate());
		assertFalse(property.hasTime());
		assertNull(property.getPartialDate());
		assertNull(property.getText());

		Date date = date("2016-01-14");
		property = new DateOrTimePropertyImpl(date, true);
		assertEquals(date, property.getDate());
		assertTrue(property.hasTime());
		assertNull(property.getPartialDate());
		assertNull(property.getText());

		property = new DateOrTimePropertyImpl(date, false);
		assertEquals(date, property.getDate());
		assertFalse(property.hasTime());
		assertNull(property.getPartialDate());
		assertNull(property.getText());

		PartialDate partialDate = new PartialDate.Builder().month(1).date(14).build();
		property = new DateOrTimePropertyImpl(partialDate);
		assertNull(property.getDate());
		assertFalse(property.hasTime());
		assertEquals(partialDate, property.getPartialDate());
		assertNull(property.getText());

		partialDate = new PartialDate.Builder().month(1).date(14).hour(12).build();
		property = new DateOrTimePropertyImpl(partialDate);
		assertNull(property.getDate());
		assertTrue(property.hasTime());
		assertEquals(partialDate, property.getPartialDate());
		assertNull(property.getText());

		String text = "text";
		property = new DateOrTimePropertyImpl(text);
		assertNull(property.getDate());
		assertFalse(property.hasTime());
		assertNull(property.getPartialDate());
		assertEquals(text, property.getText());
	}

	@Test
	public void set_value() {
		DateOrTimePropertyImpl property = new DateOrTimePropertyImpl();

		Date date = date("2016-01-14");
		property.setDate(date, true);
		assertEquals(date, property.getDate());
		assertTrue(property.hasTime());
		assertNull(property.getPartialDate());
		assertNull(property.getText());

		property.setDate(null, true);
		assertNull(property.getDate());
		assertFalse(property.hasTime());
		assertNull(property.getPartialDate());
		assertNull(property.getText());

		PartialDate partialDate = new PartialDate.Builder().month(1).date(14).build();
		property.setPartialDate(partialDate);
		assertNull(property.getDate());
		assertFalse(property.hasTime());
		assertEquals(partialDate, property.getPartialDate());
		assertNull(property.getText());

		String text = "text";
		property.setText(text);
		assertNull(property.getDate());
		assertFalse(property.hasTime());
		assertNull(property.getPartialDate());
		assertEquals(text, property.getText());

		property.setDate(date, true);
		assertEquals(date, property.getDate());
		assertTrue(property.hasTime());
		assertNull(property.getPartialDate());
		assertNull(property.getText());

		property.setPartialDate(null);
		assertNull(property.getDate());
		assertFalse(property.hasTime());
		assertNull(property.getPartialDate());
		assertNull(property.getText());
	}

	@Test
	public void validate() {
		DateOrTimePropertyImpl empty = new DateOrTimePropertyImpl();
		assertValidate(empty).run(8);

		DateOrTimePropertyImpl withDate = new DateOrTimePropertyImpl();
		Date date = date("1980-06-05");
		withDate.setDate(date, false);
		assertValidate(withDate).run();

		DateOrTimePropertyImpl withPartialDate = new DateOrTimePropertyImpl();
		withPartialDate.setPartialDate(PartialDate.builder().month(6).date(5).build());
		assertValidate(withPartialDate).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(12);
		assertValidate(withPartialDate).versions(VCardVersion.V4_0).run();

		DateOrTimePropertyImpl withText = new DateOrTimePropertyImpl();
		withText.setText("text");
		assertValidate(withText).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(11);
		assertValidate(withText).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void toStringValues() {
		DateOrTimePropertyImpl property = new DateOrTimePropertyImpl();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		DateOrTimePropertyImpl original = new DateOrTimePropertyImpl();
		assertCopy(original);

		original = new DateOrTimePropertyImpl();
		original.setDate(new Date(), true);
		assertCopy(original).notSame("getDate");

		original = new DateOrTimePropertyImpl();
		original.setPartialDate(new PartialDate.Builder().month(1).date(14).build());
		assertCopy(original);
	}

	@Test
	public void equals() {
		Date date1 = date("2016-01-14");
		Date date2 = date("2016-01-15");
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		DateOrTimePropertyImpl property = new DateOrTimePropertyImpl();
		properties.add(property);

		property = new DateOrTimePropertyImpl();
		property.setDate(date1, true);
		properties.add(property);

		property = new DateOrTimePropertyImpl();
		property.setDate(date1, false);
		properties.add(property);

		property = new DateOrTimePropertyImpl();
		property.setDate(date2, true);
		properties.add(property);

		property = new DateOrTimePropertyImpl();
		property.setPartialDate(new PartialDate.Builder().month(1).date(14).build());
		properties.add(property);

		property = new DateOrTimePropertyImpl();
		property.setPartialDate(new PartialDate.Builder().month(1).date(15).build());
		properties.add(property);

		property = new DateOrTimePropertyImpl();
		property.setText("text");
		properties.add(property);

		property = new DateOrTimePropertyImpl();
		property.setText("text2");
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(DateOrTimePropertyImpl.class)
		.constructor().test()
		.constructor(date1, true).test()
		.constructor(date1, false).test()
		.constructor(new PartialDate.Builder().year(2016).month(1).date(14).build()).test()
		.constructor("text").test();
		//@formatter:on
	}

	public static class DateOrTimePropertyImpl extends DateOrTimeProperty {
		public DateOrTimePropertyImpl() {
			super((Date) null);
		}

		public DateOrTimePropertyImpl(Date date, Boolean hasTime) {
			super(date, hasTime);
		}

		public DateOrTimePropertyImpl(PartialDate partialDate) {
			super(partialDate);
		}

		public DateOrTimePropertyImpl(String text) {
			super(text);
		}

		public DateOrTimePropertyImpl(DateOrTimePropertyImpl original) {
			super(original);
		}
	}
}
