package ezvcard.io;

import static ezvcard.util.TestUtils.assertIntEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.Messages;

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
*/

/**
 * @author Michael Angstadt
 */
public class ParseWarningTest {
	@Test
	public void builder_empty() {
		ParseWarning.Builder builder = new ParseWarning.Builder();
		ParseWarning warning = builder.build();
		assertNull(warning.getLineNumber());
		assertNull(warning.getPropertyName());
		assertNull(warning.getCode());
		assertNull(warning.getMessage());
	}

	@Test
	public void builder_string_message() {
		ParseWarning.Builder builder = new ParseWarning.Builder();
		ParseWarning warning = builder.lineNumber(1).propertyName("PROP").message("message").build();
		assertIntEquals(1, warning.getLineNumber());
		assertEquals("PROP", warning.getPropertyName());
		assertNull(warning.getCode());
		assertEquals("message", warning.getMessage());
	}

	@Test
	public void builder_code_message() {
		ParseWarning.Builder builder = new ParseWarning.Builder();
		ParseWarning warning = builder.lineNumber(1).propertyName("PROP").message(2, "arg").build();
		assertIntEquals(1, warning.getLineNumber());
		assertEquals("PROP", warning.getPropertyName());
		assertIntEquals(2, warning.getCode());
		assertEquals(Messages.INSTANCE.getParseMessage(2, "arg"), warning.getMessage());
	}

	@Test
	public void builder_CannotParseException_reason() {
		CannotParseException e = new CannotParseException("reason");
		ParseWarning.Builder builder = new ParseWarning.Builder();
		ParseWarning warning = builder.message(e).build();
		assertNull(warning.getLineNumber());
		assertNull(warning.getPropertyName());
		assertIntEquals(25, warning.getCode());
		assertEquals(Messages.INSTANCE.getParseMessage(25, "reason"), warning.getMessage());
	}

	@Test
	public void builder_CannotParseException_code() {
		CannotParseException e = new CannotParseException(2, "arg");
		ParseWarning.Builder builder = new ParseWarning.Builder();
		ParseWarning warning = builder.message(e).build();
		assertNull(warning.getLineNumber());
		assertNull(warning.getPropertyName());
		assertIntEquals(2, warning.getCode());
		assertEquals(Messages.INSTANCE.getParseMessage(2, "arg"), warning.getMessage());
	}

	@Test
	public void builder_ParseContext() {
		ParseContext context = new ParseContext();
		context.setLineNumber(1);
		context.setPropertyName("PROP");
		ParseWarning.Builder builder = new ParseWarning.Builder(context);
		ParseWarning warning = builder.build();
		assertIntEquals(1, warning.getLineNumber());
		assertEquals("PROP", warning.getPropertyName());
		assertNull(warning.getCode());
		assertNull(warning.getMessage());
	}

	@Test
	public void toString_() {
		//without code
		{
			ParseWarning.Builder builder = new ParseWarning.Builder();
			ParseWarning warning = builder.message("message").build();

			String expected = "message";
			String actual = warning.toString();
			assertEquals(expected, actual);
		}

		//with code
		{
			ParseWarning.Builder builder = new ParseWarning.Builder();
			ParseWarning warning = builder.message(2, "message").build();

			String expected = "(2) " + Messages.INSTANCE.getParseMessage(2, "message");
			String actual = warning.toString();
			assertEquals(expected, actual);
		}

		//with line and property name
		{
			ParseWarning.Builder builder = new ParseWarning.Builder();
			ParseWarning warning = builder.lineNumber(1).propertyName("PROP").message("message").build();

			String expected = Messages.INSTANCE.getParseMessage(36, 1, "PROP", "message");
			String actual = warning.toString();
			assertEquals(expected, actual);
		}

		//with line
		{
			ParseWarning.Builder builder = new ParseWarning.Builder();
			ParseWarning warning = builder.lineNumber(1).message("message").build();

			String expected = Messages.INSTANCE.getParseMessage(37, 1, "PROP", "message");
			String actual = warning.toString();
			assertEquals(expected, actual);
		}

		//with property name
		{
			ParseWarning.Builder builder = new ParseWarning.Builder();
			ParseWarning warning = builder.propertyName("PROP").message("message").build();

			String expected = Messages.INSTANCE.getParseMessage(35, 1, "PROP", "message");
			String actual = warning.toString();
			assertEquals(expected, actual);
		}
	}
}
