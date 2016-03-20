package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

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
public class ListPropertyTest {
	@Test
	public void validate() {
		ListPropertyImpl zeroItems = new ListPropertyImpl();
		assertValidate(zeroItems).run(8);

		ListPropertyImpl oneItem = new ListPropertyImpl();
		oneItem.getValues().add("one");
		assertValidate(oneItem).run();
	}

	@Test
	public void toStringValues() {
		ListPropertyImpl property = new ListPropertyImpl();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		ListPropertyImpl original = new ListPropertyImpl();
		assertCopy(original);

		original = new ListPropertyImpl();
		original.getValues().add("value");
		assertCopy(original).notSame("getValues");
	}

	@Test
	public void equals() {
		List<VCardProperty> properties = new ArrayList<VCardProperty>();

		ListPropertyImpl property = new ListPropertyImpl();
		properties.add(property);

		property = new ListPropertyImpl();
		property.getValues().add("value");
		properties.add(property);

		property = new ListPropertyImpl();
		property.getValues().add("value2");
		properties.add(property);

		property = new ListPropertyImpl();
		property.getValues().add("value");
		property.getValues().add("value2");
		properties.add(property);

		assertNothingIsEqual(properties);

		ListPropertyImpl one = new ListPropertyImpl();
		assertEqualsMethodEssentials(one);

		ListPropertyImpl two = new ListPropertyImpl();
		assertEqualsAndHash(one, two);

		one.getValues().add("value");
		two.getValues().add("value");
		assertEqualsAndHash(one, two);
	}

	public static class ListPropertyImpl extends ListProperty<String> {
		public ListPropertyImpl() {
			super();
		}

		public ListPropertyImpl(ListPropertyImpl original) {
			super(original);
		}
	}
}
