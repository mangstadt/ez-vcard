package ezvcard.util;

import static org.junit.Assert.assertEquals;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class ClearableStringBuilderTest {
	private ClearableStringBuilder sb;

	@Before
	public void before() {
		sb = new ClearableStringBuilder();
	}

	@Test
	public void get() {
		assertEquals("", sb.get());

		sb.append("foo");
		assertEquals("foo", sb.get());

		/*
		 * Internal buffer should not be modified when get() is called.
		 */
		assertEquals("foo", sb.get());
	}

	@Test
	public void clear() {
		sb.append("foo");
		assertEquals("foo", sb.get());

		sb.clear();
		assertEquals("", sb.get());
	}

	@Test
	public void getAndClear() {
		sb.append("foo");
		assertEquals("foo", sb.getAndClear());
		assertEquals("", sb.get());
	}

	@Test
	public void append() {
		sb.append('f').append("oo").append(" bar!".toCharArray(), 0, 4);
		assertEquals("foo bar", sb.get());
	}

	@Test
	public void length() {
		assertEquals(0, sb.length());

		sb.append("foo");
		assertEquals(3, sb.length());
	}
}
