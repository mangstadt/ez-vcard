package ezvcard.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
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
 */

/**
 * @author Michael Angstadt
 */
public class EnumParameterListTest {
	private final String NAME = "NUM";

	@Test
	public void get_set() {
		VCardParameters parameters = new VCardParameters();
		List<VCardParameterImpl> list = parameters.new EnumParameterList<VCardParameterImpl>(NAME) {
			@Override
			protected VCardParameterImpl _asObject(String value) throws Exception {
				if ("0".equals(value)) {
					return VCardParameterImpl.ZERO;
				}
				if ("1".equals(value)) {
					return VCardParameterImpl.ONE;
				}
				if ("2".equals(value)) {
					return VCardParameterImpl.TWO;
				}
				throw new NumberFormatException();
			}
		};

		assertEquals(Arrays.asList(), list);
		assertEquals(Arrays.asList(), parameters.get(NAME));

		list.add(VCardParameterImpl.ZERO);
		assertEquals(Arrays.asList(VCardParameterImpl.ZERO), list);
		assertEquals(Arrays.asList("0"), parameters.get(NAME));

		parameters.put(NAME, "1");
		assertEquals(Arrays.asList(VCardParameterImpl.ZERO, VCardParameterImpl.ONE), list);
		assertEquals(Arrays.asList("0", "1"), parameters.get(NAME));

		list.clear();
		assertEquals(Arrays.asList(), list);
		assertEquals(Arrays.asList(), parameters.get(NAME));

		list.add(VCardParameterImpl.ZERO);
		assertEquals(Arrays.asList(VCardParameterImpl.ZERO), list);
		assertEquals(Arrays.asList("0"), parameters.get(NAME));

		parameters.removeAll(NAME);
		assertEquals(Arrays.asList(), list);
		assertEquals(Arrays.asList(), parameters.get(NAME));

		parameters.put(NAME, "3");
		try {
			list.get(0);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(e.getCause().getClass(), NumberFormatException.class);
		}
	}

	private static class VCardParameterImpl extends VCardParameter {
		public static final VCardParameterImpl ZERO = new VCardParameterImpl("0");
		public static final VCardParameterImpl ONE = new VCardParameterImpl("1");
		public static final VCardParameterImpl TWO = new VCardParameterImpl("2");

		public VCardParameterImpl(String value) {
			super(value);
		}
	}
}
