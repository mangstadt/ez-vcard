package ezvcard.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class VCardParameterTest {
	@Test
	public void findByValue() {
		ColorParameter param = VCardParameter.findByValue("red", ColorParameter.class);
		assertTrue(ColorParameter.RED == param);
	}

	@Test
	public void findByValue_not_found() {
		ColorParameter param = VCardParameter.findByValue("yellow", ColorParameter.class);
		assertNull(param);
	}

	@Test
	public void findByValue_case_insensitive() {
		ColorParameter param = VCardParameter.findByValue("rEd", ColorParameter.class);
		assertTrue(ColorParameter.RED == param);
	}

	@Test
	public void all() {
		Set<ColorParameter> params = VCardParameter.all(ColorParameter.class);
		assertEquals(3, params.size());
		assertTrue(params.contains(ColorParameter.RED));
		assertTrue(params.contains(ColorParameter.GREEN));
		assertTrue(params.contains(ColorParameter.BLUE));
	}

	@Test
	public void all_none() {
		Set<NoValuesParameter> params = VCardParameter.all(NoValuesParameter.class);
		assertTrue(params.isEmpty());
	}

	private static class ColorParameter extends VCardParameter {
		public static final ColorParameter RED = new ColorParameter("red");
		public static final ColorParameter GREEN = new ColorParameter("green");
		public static final ColorParameter BLUE = new ColorParameter("blue");

		public ColorParameter(String value) {
			super("COLOR", value);
		}
	}

	private static class NoValuesParameter extends VCardParameter {
		public NoValuesParameter(String value) {
			super("NO-VALUES", value);
		}
	}
}
