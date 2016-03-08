package ezvcard.util;

import static ezvcard.util.TestUtils.assertIntEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

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
public class CaseClassesTest {
	private CaseClassesImpl cc;

	@Before
	public void before() {
		cc = new CaseClassesImpl();
	}

	@Test
	public void find() {
		PrimeNumber found = cc.find(1);
		assertSame(PrimeNumber.ONE, found);

		assertNull(cc.find(4));
	}

	@Test
	public void get() {
		PrimeNumber found = cc.get(3);
		assertSame(PrimeNumber.THREE, found);

		PrimeNumber eleven1 = cc.get(11);
		assertIntEquals(11, eleven1.value);

		PrimeNumber eleven2 = cc.get(11);
		assertSame(eleven1, eleven2);
	}

	@Test
	public void all() {
		Collection<PrimeNumber> dataTypes = cc.all();

		assertEquals(4, dataTypes.size());
		assertTrue(dataTypes.contains(PrimeNumber.ONE));
		assertTrue(dataTypes.contains(PrimeNumber.THREE));
		assertTrue(dataTypes.contains(PrimeNumber.FIVE));
		assertTrue(dataTypes.contains(PrimeNumber.SEVEN));
	}

	@Test
	public void all_does_not_include_runtime_objects() {
		cc.get(13);

		Collection<PrimeNumber> dataTypes = cc.all();

		assertEquals(4, dataTypes.size());
		assertTrue(dataTypes.contains(PrimeNumber.ONE));
		assertTrue(dataTypes.contains(PrimeNumber.THREE));
		assertTrue(dataTypes.contains(PrimeNumber.FIVE));
		assertTrue(dataTypes.contains(PrimeNumber.SEVEN));
	}

	private class CaseClassesImpl extends CaseClasses<PrimeNumber, Integer> {
		public CaseClassesImpl() {
			super(PrimeNumber.class);
		}

		@Override
		protected PrimeNumber create(Integer value) {
			return new PrimeNumber(value);
		}

		@Override
		protected boolean matches(PrimeNumber object, Integer value) {
			return object.value.equals(value);
		}
	}

	@SuppressWarnings("unused")
	private static class PrimeNumber {
		public static final PrimeNumber ONE = new PrimeNumber(1);
		public static final PrimeNumber THREE = new PrimeNumber(3);
		public static final PrimeNumber FIVE = new PrimeNumber(5);
		public static final PrimeNumber SEVEN = new PrimeNumber(7);

		private static final PrimeNumber NINE = new PrimeNumber(9); //not public
		public static final String TEST = "test"; //not a PrimeNumber object

		public Integer value;

		public PrimeNumber(Integer value) {
			this.value = value;
		}
	}
}
