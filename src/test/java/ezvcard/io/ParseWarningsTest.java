package ezvcard.io;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ezvcard.Messages;

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
public class ParseWarningsTest {
	@Test
	public void add() {
		ParseWarnings warnings = new ParseWarnings();
		warnings.add(10, "PROP", "Error parsing property.");

		List<String> expected = Arrays.asList("Line 10 (PROP property): Error parsing property.");
		List<String> actual = warnings.copy();
		assertEquals(expected, actual);
	}

	@Test
	public void add_no_line() {
		ParseWarnings warnings = new ParseWarnings();
		warnings.add(null, "PROP", "Error parsing property.");

		List<String> expected = Arrays.asList("PROP property: Error parsing property.");
		List<String> actual = warnings.copy();
		assertEquals(expected, actual);
	}

	@Test
	public void add_no_property() {
		ParseWarnings warnings = new ParseWarnings();
		warnings.add(10, null, "Error parsing property.");

		List<String> expected = Arrays.asList("Line 10: Error parsing property.");
		List<String> actual = warnings.copy();
		assertEquals(expected, actual);
	}

	@Test
	public void add_no_line_no_property() {
		ParseWarnings warnings = new ParseWarnings();
		warnings.add(null, null, "Error parsing property.");

		List<String> expected = Arrays.asList("Error parsing property.");
		List<String> actual = warnings.copy();
		assertEquals(expected, actual);
	}

	@Test
	public void add_with_code() {
		ParseWarnings warnings = new ParseWarnings();
		warnings.add(10, "PROP", 0);

		List<String> expected = Arrays.asList("Line 10 (PROP property): " + Messages.INSTANCE.getParseMessage(0));
		List<String> actual = warnings.copy();
		assertEquals(expected, actual);
	}
}
