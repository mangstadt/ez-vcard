package ezvcard.parameter;

import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertNothingIsEqual;
import static org.junit.Assert.assertEquals;
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
 */

/**
 * @author Michael Angstadt
 */
public class PidTest {
	@Test(expected = NullPointerException.class)
	public void null_local_id() {
		new Pid(null);
	}

	@Test
	public void constructor() {
		Pid pid = new Pid(1);
		assertIntEquals(1, pid.getLocalId());
		assertNull(pid.getClientPidMapReference());

		pid = new Pid(1, 2);
		assertIntEquals(1, pid.getLocalId());
		assertIntEquals(2, pid.getClientPidMapReference());
	}

	@Test
	public void toString_() {
		Pid pid = new Pid(1, 2);
		assertEquals("1.2", pid.toString());

		pid = new Pid(1);
		assertEquals("1", pid.toString());
	}

	@Test
	public void valueOf() {
		Pid expected = new Pid(1, 2);
		Pid actual = Pid.valueOf("1.2");
		assertEquals(expected, actual);

		expected = new Pid(1);
		actual = Pid.valueOf("1.");
		assertEquals(expected, actual);

		expected = new Pid(1);
		actual = Pid.valueOf("1");
		assertEquals(expected, actual);
	}

	@Test(expected = NumberFormatException.class)
	public void valueOf_invalid() {
		Pid.valueOf("1.foo");
	}

	@Test(expected = NumberFormatException.class)
	public void valueOf_more_than_one_dot() {
		Pid.valueOf("1.2.2");
	}

	@Test
	public void equals() {
		Pid pid = new Pid(1, 2);
		assertEqualsMethodEssentials(pid);

		//@formatter:off
		assertNothingIsEqual(
			new Pid(1),
			new Pid(2),
			new Pid(1,2),
			new Pid(2,2),
			new Pid(2,3)
		);
		//@formatter:on

		Pid one = new Pid(1);
		Pid two = new Pid(1);
		assertEqualsAndHash(one, two);

		one = new Pid(1, 2);
		two = new Pid(1, 2);
		assertEqualsAndHash(one, two);
	}
}
