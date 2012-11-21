package ezvcard.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
public class VCardStringUtilsTest {
	private static final String newline = System.getProperty("line.separator");

	@Test
	public void unescape() {
		String expected, actual;

		actual = VCardStringUtils.unescape("\\\\ \\, \\; \\n \\\\\\,");
		expected = "\\ , ; " + newline + " \\,";
		assertEquals(expected, actual);
	}

	@Test
	public void escape() {
		String actual, expected;

		actual = VCardStringUtils.escape("One; Two, Three\\ Four\n Five\r\n Six\r");
		expected = "One\\; Two\\, Three\\\\ Four\n Five\r\n Six\r";
		assertEquals(expected, actual);
	}

	@Test
	public void escapeNewlines() {
		String actual, expected;

		actual = VCardStringUtils.escapeNewlines("One; Two, Three\\ Four\n Five\r\n Six\r");
		expected = "One; Two, Three\\ Four\\n Five\\n Six\\n";
		assertEquals(expected, actual);
	}

	@Test
	public void ltrim() {
		String actual, expected;

		actual = VCardStringUtils.ltrim("\n \t One two three \t \n ");
		expected = "One two three \t \n ";
		assertEquals(actual, expected);
	}

	@Test
	public void splitBy() {
		String[] actual, expected;

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', false, false);
		expected = new String[] { "Doe", "John", "Joh\\,\\;nny", "", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', true, false);
		expected = new String[] { "Doe", "John", "Joh\\,\\;nny", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', false, true);
		expected = new String[] { "Doe", "John", "Joh,;nny", "", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = VCardStringUtils.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', true, true);
		expected = new String[] { "Doe", "John", "Joh,;nny", "Sr.,III" };
		assertArrayEquals(expected, actual);
	}
}
