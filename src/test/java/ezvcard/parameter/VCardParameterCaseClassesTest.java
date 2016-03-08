package ezvcard.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
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
 */

/**
 * @author Michael Angstadt
 */
public class VCardParameterCaseClassesTest {
	private VCardParameterCaseClasses<Pet> caseClasses;

	@Before
	public void before() {
		caseClasses = new VCardParameterCaseClasses<Pet>(Pet.class);
	}

	@Test
	public void get() {
		assertSame(Pet.CAT, caseClasses.get("cAt"));

		Pet gerbil = caseClasses.get("gerbil");
		Pet gerbil2 = caseClasses.get("gERbil");
		assertEquals("gerbil", gerbil2.getValue());
		assertSame(gerbil, gerbil2);
	}

	@Test
	public void find() {
		assertSame(Pet.CAT, caseClasses.find("cAt"));
		assertNull(caseClasses.find("gerbil"));
	}

	public static class Pet extends VCardParameter {
		public static final Pet CAT = new Pet("cat");
		public static final Pet DOG = new Pet("dog");
		public static final Pet FISH = new Pet("fish");

		private Pet(String value) {
			super(value);
		}
	}
}
