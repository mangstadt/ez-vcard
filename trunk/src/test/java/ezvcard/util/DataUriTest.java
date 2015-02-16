package ezvcard.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

import ezvcard.util.org.apache.commons.codec.binary.Base64;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
public class DataUriTest {
	private final byte[] testBytes = "test-data".getBytes();
	private final String testBase64 = Base64.encodeBase64String(testBytes);

	@Test
	public void parse_valid() {
		DataUri uri = new DataUri("data:text/plain;base64," + testBase64);
		assertEquals("text/plain", uri.getContentType());
		assertArrayEquals(testBytes, uri.getData());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		new DataUri("not-valid");
	}

	@Test
	public void toString_() {
		DataUri uri = new DataUri("text/plain", testBytes);
		assertEquals("text/plain", uri.getContentType());
		assertArrayEquals(testBytes, uri.getData());
		assertEquals("data:text/plain;base64," + testBase64, uri.toString());
	}

	@Test
	public void toUri() {
		DataUri uri = new DataUri("text/plain", testBytes);
		URI theUri = uri.toUri();
		assertEquals("data", theUri.getScheme());
		assertEquals("text/plain;base64," + testBase64, theUri.getRawSchemeSpecificPart());
	}
}
