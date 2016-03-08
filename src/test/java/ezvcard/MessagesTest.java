package ezvcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
public class MessagesTest {
	private final Messages messages = Messages.INSTANCE;

	@Test
	public void getValidationWarning() {
		String actual = messages.getValidationWarning(0);
		String expected = messages.getMessage("validate.0");
		assertEquals(expected, actual);
	}

	@Test
	public void getValidationWarning_does_not_exist() {
		assertNull(messages.getValidationWarning(5000));
	}

	@Test
	public void getParseMessage() {
		String actual = messages.getParseMessage(0);
		String expected = messages.getMessage("parse.0");
		assertEquals(expected, actual);
	}

	@Test
	public void getParseMessage_does_not_exist() {
		assertNull(messages.getParseMessage(5000));
	}

	@Test
	public void getExceptionMessage() {
		String actual = messages.getExceptionMessage(1);
		String expected = messages.getMessage("exception.0", 1, messages.getMessage("exception.1"));
		assertEquals(expected, actual);
	}

	@Test
	public void getExceptionMessage_does_not_exist() {
		assertNull(messages.getExceptionMessage(5000));
	}

	@Test
	public void getIllegalArgumentException() {
		String actual = messages.getIllegalArgumentException(1).getMessage();
		String expected = messages.getMessage("exception.0", 1, messages.getMessage("exception.1"));
		assertEquals(expected, actual);
	}

	@Test
	public void getIllegalArgumentException_does_not_exist() {
		assertNull(messages.getIllegalArgumentException(5000));
	}

	@Test
	public void getMessage() throws Exception {
		String expected = load().getProperty("parse.0");
		String actual = messages.getMessage("parse.0");
		assertEquals(expected, actual);
	}

	@Test
	public void getMessage_does_not_exist() {
		assertNull(messages.getMessage("does-not-exist"));
	}

	private Properties load() throws IOException {
		Properties properties = new Properties();
		InputStream in = getClass().getResourceAsStream("/ezvcard/messages.properties");
		try {
			properties.load(in);
		} finally {
			in.close();
		}
		return properties;
	}
}
