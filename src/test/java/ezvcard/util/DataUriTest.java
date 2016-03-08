package ezvcard.util;

import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static ezvcard.util.TestUtils.assertNothingIsEqual;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import ezvcard.util.org.apache.commons.codec.binary.Base64;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class DataUriTest {
	private final String dataString = "test-data";
	private final byte[] dataBytes;
	{
		try {
			dataBytes = dataString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	private final String dataBase64 = Base64.encodeBase64String(dataBytes);

	@Test
	public void constructor() {
		DataUri uri = new DataUri("text");
		assertEquals("text/plain", uri.getContentType());
		assertNull(uri.getData());
		assertEquals("text", uri.getText());

		uri = new DataUri("text/html", "text");
		assertEquals("text/html", uri.getContentType());
		assertNull(uri.getData());
		assertEquals("text", uri.getText());

		//convert content type to lower case
		uri = new DataUri("TEXT/HTML", "text");
		assertEquals("text/html", uri.getContentType());
		assertNull(uri.getData());
		assertEquals("text", uri.getText());

		uri = new DataUri("image/png", dataBytes);
		assertEquals("image/png", uri.getContentType());
		assertArrayEquals(dataBytes, uri.getData());
		assertNull(uri.getText());

		//convert null content type to empty string
		uri = new DataUri(null, dataBytes);
		assertEquals("", uri.getContentType());
	}

	@Test
	public void copy() {
		DataUri uri = new DataUri("text/html", "text");
		DataUri copy = new DataUri(uri);
		assertEquals("text/html", copy.getContentType());
		assertNull(copy.getData());
		assertEquals("text", copy.getText());
		assertEqualsAndHash(uri, copy);

		uri = new DataUri("image/png", dataBytes);
		copy = new DataUri(uri);
		assertEquals("image/png", copy.getContentType());
		assertArrayEquals(dataBytes, copy.getData());
		assertNotSame(uri.getData(), copy.getData());
		assertNull(copy.getText());
		assertEqualsAndHash(uri, copy);
	}

	@Test
	public void parse() {
		DataUri expected = new DataUri("image/png", dataBytes);
		DataUri actual = DataUri.parse("data:image/png;base64," + dataBase64);
		assertEquals(expected, actual);

		expected = new DataUri("", dataBytes);
		actual = DataUri.parse("data:;base64," + dataBase64);
		assertEquals(expected, actual);

		expected = new DataUri("image/png", dataString);
		actual = DataUri.parse("data:image/png;charset=UTF-8;base64," + dataBase64);
		assertEquals(expected, actual);

		//order of charset and base64 matter
		expected = new DataUri("image/png", dataString);
		actual = DataUri.parse("data:image/png;base64;charset=UTF-8," + dataBase64);
		assertEquals(expected, actual);

		//ignore unknown tokens
		expected = new DataUri("image/png", dataString);
		actual = DataUri.parse("data:image/png;charset=UTF-8;foo;base64;bar," + dataBase64);
		assertEquals(expected, actual);

		expected = new DataUri("image/png", dataBase64);
		actual = DataUri.parse("data:image/png;charset=UTF-8," + dataBase64);
		assertEquals(expected, actual);

		expected = new DataUri("image/png", dataBase64);
		actual = DataUri.parse("data:image/png;charset=UTF-8;foobar," + dataBase64);
		assertEquals(expected, actual);
	}

	@Test
	public void parse_comma_in_content_type() {
		DataUri expected = new DataUri("text/pla", "in;base64," + dataBase64);
		DataUri actual = DataUri.parse("data:text/pla,in;base64," + dataBase64);
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_short_string() {
		DataUri.parse("a");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_not_a_uri() {
		DataUri.parse("not-valid");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_wrong_scheme() {
		DataUri.parse("mailto:johndoe@gmail.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_no_comma() {
		DataUri.parse("data:text/plain;base64");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_bad_charset() {
		DataUri.parse("data:text/plain;charset=foobar;base64," + dataBase64);
	}

	@Test
	public void toString_() {
		DataUri uri = new DataUri("text/plain", dataBytes);
		assertEquals("data:text/plain;base64," + dataBase64, uri.toString());

		uri = new DataUri("text/plain", dataBytes);
		assertEquals("data:text/plain;base64," + dataBase64, uri.toString("UTF-8"));

		uri = new DataUri("text/plain", dataString);
		assertEquals("data:text/plain," + dataString, uri.toString());

		uri = new DataUri("text/plain", dataString);
		assertEquals("data:text/plain;charset=UTF-8;base64," + dataBase64, uri.toString("UTF-8"));

		uri = new DataUri("text/plain", (String) null);
		assertEquals("data:text/plain,", uri.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void toString_bad_charset() {
		DataUri uri = new DataUri("text/plain", dataString);
		uri.toString("foobar");
	}

	@Test
	public void equals() {
		assertEqualsMethodEssentials(new DataUri(""));

		//@formatter:off
		assertNothingIsEqual(
			new DataUri("text/plain", dataBytes),
			new DataUri("image/png", dataBytes),
			new DataUri("text/plain", "other-string".getBytes()),
			new DataUri("text/plain", dataString),
			new DataUri("text/html", dataString),
			new DataUri("text/plain", "other-string"),
			new DataUri("text/plain", (String)null)
		);
		//@formatter:on
	}
}
