package ezvcard.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

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
public class VCardParameterListTest {
	private final String NAME = "NUMBERS";
	private VCardParameters parameters;
	private List<Integer> list;

	@Before
	public void before() {
		parameters = new VCardParameters();
		list = parameters.new VCardParameterList<Integer>(NAME) {
			@Override
			protected String _asString(Integer value) {
				return value.toString();
			}

			@Override
			protected Integer _asObject(String value) {
				return Integer.valueOf(value);
			}
		};
	}

	@Test
	public void get_set() {
		list.add(1);
		assertEquals(Arrays.asList("1"), parameters.get(NAME));
		assertEquals(Arrays.asList(1), list);

		list.add(2);
		assertEquals(Arrays.asList("1", "2"), parameters.get(NAME));
		assertEquals(Arrays.asList(1, 2), list);

		list.set(1, 10);
		assertEquals(Arrays.asList("1", "10"), parameters.get(NAME));
		assertEquals(Arrays.asList(1, 10), list);

		list.remove(1);
		assertEquals(Arrays.asList("1"), parameters.get(NAME));
		assertEquals(Arrays.asList(1), list);

		parameters.removeAll(NAME);
		assertEquals(Arrays.asList(), parameters.get(NAME));
		assertEquals(Arrays.asList(), list);

		list.add(1);
		assertEquals(Arrays.asList("1"), parameters.get(NAME));
		assertEquals(Arrays.asList(1), list);

		list.clear();
		assertEquals(Arrays.asList(), parameters.get(NAME));
		assertEquals(Arrays.asList(), list);
	}

	@Test
	public void invalid_value() {
		parameters.put(NAME, "foobar");
		try {
			list.get(0);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(e.getCause().getClass(), NumberFormatException.class);
		}
	}
}
