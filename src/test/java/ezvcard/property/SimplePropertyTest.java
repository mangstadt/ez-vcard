package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
public class SimplePropertyTest {
	@Test
	public void constructors() throws Exception {
		SimplePropertyImpl property = new SimplePropertyImpl((String) null);
		assertNull(property.getValue());

		property = new SimplePropertyImpl("value");
		assertEquals("value", property.getValue());
	}

	@Test
	public void set_value() {
		SimplePropertyImpl property = new SimplePropertyImpl("value");

		property.setValue("value2");
		assertEquals("value2", property.getValue());
	}

	@Test
	public void validate() {
		SimplePropertyImpl empty = new SimplePropertyImpl((String) null);
		assertValidate(empty).run(8);

		SimplePropertyImpl withValue = new SimplePropertyImpl("text");
		assertValidate(withValue).run();
	}

	@Test
	public void toStringValues() {
		SimplePropertyImpl property = new SimplePropertyImpl("value");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		SimplePropertyImpl original = new SimplePropertyImpl((String) null);
		assertCopy(original);

		original = new SimplePropertyImpl("value");
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new SimplePropertyImpl((String) null),
			new SimplePropertyImpl("value"),
			new SimplePropertyImpl("value2")
		);
		
		assertEqualsMethod(SimplePropertyImpl.class, "value")
		.constructor(new Class<?>[]{String.class}, (String)null).test()
		.constructor("value").test();
		//@formatter:on
	}

	public static class SimplePropertyImpl extends SimpleProperty<String> {
		public SimplePropertyImpl(String value) {
			super(value);
		}

		public SimplePropertyImpl(SimplePropertyImpl original) {
			super(original);
		}
	}
}
