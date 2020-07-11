package ezvcard.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
public class StringUtilsTest {
	@Test
	public void join() {
		Collection<Object> values = Arrays.<Object> asList("one", "two", 3);
		String expected = "one,two,3";
		String actual = StringUtils.join(values, ",");
		assertEquals(expected, actual);

		values = Arrays.<Object> asList("one");
		expected = "one";
		actual = StringUtils.join(values, ",");
		assertEquals(expected, actual);

		values = Arrays.asList();
		expected = "";
		actual = StringUtils.join(values, ",");
		assertEquals(expected, actual);
	}

	@Test
	public void toLowerCase() {
		Map<String, String> input = new HashMap<String, String>();
		input.put("ONE", "TWO");
		input.put(null, "THREE");
		input.put("FOUR", null);

		Map<String, String> expected = new HashMap<String, String>();
		expected.put("one", "two");
		expected.put(null, "three");
		expected.put("four", null);

		Map<String, String> actual = StringUtils.toLowerCase(input);

		assertEquals(expected, actual);
	}
}
