package ezvcard;

import static ezvcard.util.VCardStringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import ezvcard.ValidationWarnings.WarningsGroup;
import ezvcard.property.VCardType;
import ezvcard.util.TestUtils.Tests;

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
 */

/**
 * @author Michael Angstadt
 */
public class WarningsGroupTest {
	@Test
	public void toString_() {
		//@formatter:off
		Tests tests = new Tests();
		tests.add(
			"one",
			new WarningsGroup(null, Arrays.asList("one"))
		);
		tests.add(
			"[TestProperty]: one" + NEWLINE + 
			"[TestProperty]: two",
			new WarningsGroup(new TestProperty(), Arrays.asList("one", "two"))
		);
		//@formatter:on

		for (Object[] test : tests) {
			String expected = (String) test[0];
			WarningsGroup group = (WarningsGroup) test[1];
			String actual = group.toString();

			assertEquals(expected, actual);
		}
	}

	private class TestProperty extends VCardType {
		//empty
	}
}
