package ezvcard.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ezvcard.parameter.Pid;
import ezvcard.parameter.VCardParameters;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
public class PidParameterListTest {
	@Test
	public void get_set() {
		PidParameterListProperty property = new PidParameterListProperty();
		VCardParameters parameters = property.getParameters();
		List<Pid> pids = property.getPids();

		pids.add(new Pid(1, 1));
		parameters.put(VCardParameters.PID, "2");
		assertEquals(Arrays.asList("1.1", "2"), parameters.get(VCardParameters.PID));
		assertEquals(Arrays.asList(new Pid(1, 1), new Pid(2)), pids);
	}

	@Test
	public void invalid_value() {
		PidParameterListProperty property = new PidParameterListProperty();
		VCardParameters parameters = property.getParameters();
		List<Pid> pids = property.getPids();

		parameters.put(VCardParameters.PID, "foobar");
		try {
			pids.get(0);
			fail();
		} catch (IllegalStateException e) {
			assertTrue(e.getCause() instanceof NumberFormatException);
		}
	}

	private static class PidParameterListProperty extends VCardProperty {
		@Override
		public List<Pid> getPids() {
			return super.getPids();
		}
	}
}
