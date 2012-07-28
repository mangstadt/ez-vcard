package ezvcard.parameters;

import org.junit.Test;
import static org.junit.Assert.*;

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
public class TypeParameterTest {
	@Test
	public void findByTypeParam() {
		NumberTypeParameter expected = NumberTypeParameter.second;
		NumberTypeParameter actual = NumberTypeParameter.findByValue("two", NumberTypeParameter.class);
		assertEquals(expected, actual);

		actual = NumberTypeParameter.findByValue("five", NumberTypeParameter.class);
		assertNull(actual);
	}

	@Test
	public void equals() {
		NumberTypeParameter two = new NumberTypeParameter("two");
		assertTrue(two.equals(NumberTypeParameter.second));

		assertFalse(NumberTypeParameter.first.equals(OtherTypeParameter.first));
	}

	public static class NumberTypeParameter extends TypeParameter {
		public static final NumberTypeParameter first = new NumberTypeParameter("one");
		public static final NumberTypeParameter second = new NumberTypeParameter("two");
		public static final NumberTypeParameter third = new NumberTypeParameter("three");

		public NumberTypeParameter(String value) {
			super(value);
		}
	}

	public static class OtherTypeParameter extends TypeParameter {
		public static final OtherTypeParameter first = new OtherTypeParameter("one");

		public OtherTypeParameter(String value) {
			super(value);
		}
	}
}
