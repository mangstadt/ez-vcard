package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

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
public class TelUriTest {
	@Test
	public void parse_number() {
		TelUri uri = TelUri.parse("tel:+1-212-555-0101");
		assertEquals("+1-212-555-0101", uri.getNumber());
		assertNull(uri.getExtension());
		assertNull(uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertTrue(uri.getParameters().isEmpty());
	}

	@Test
	public void parse_extension() {
		TelUri uri = TelUri.parse("tel:+1-212-555-0101;ext=101");
		assertEquals("+1-212-555-0101", uri.getNumber());
		assertEquals("101", uri.getExtension());
		assertNull(uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertTrue(uri.getParameters().isEmpty());
	}

	@Test
	public void parse_phoneContext() {
		TelUri uri = TelUri.parse("tel:0101;phone-context=example.com");
		assertEquals("0101", uri.getNumber());
		assertNull(uri.getExtension());
		assertEquals("example.com", uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertTrue(uri.getParameters().isEmpty());
	}

	@Test
	public void parse_isdnSubaddress() {
		TelUri uri = TelUri.parse("tel:+1-212-555-0101;isub=value");
		assertEquals("+1-212-555-0101", uri.getNumber());
		assertNull(uri.getExtension());
		assertNull(uri.getPhoneContext());
		assertEquals("value", uri.getIsdnSubaddress());
		assertTrue(uri.getParameters().isEmpty());
	}

	@Test
	public void parse_params() {
		TelUri uri = TelUri.parse("tel:+1-212-555-0101;param1=value1;param2=value2");
		assertEquals("+1-212-555-0101", uri.getNumber());
		assertNull(uri.getExtension());
		assertNull(uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertEquals("value1", uri.getParameter("param1"));
		assertEquals("value2", uri.getParameter("param2"));
		Map<String, String> params = new HashMap<String, String>();
		params.put("param1", "value1");
		params.put("param2", "value2");
		assertEquals(params, uri.getParameters());
	}

	@Test
	public void parse_decode_special_chars_in_param_value() {
		TelUri uri = TelUri.parse("tel:+1-212-555-0101;param=with%20%3d%20special%20&%20chars");
		assertEquals("+1-212-555-0101", uri.getNumber());
		assertNull(uri.getExtension());
		assertNull(uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertEquals("with = special & chars", uri.getParameter("param"));
	}

	@Test
	public void parse_empty() {
		TelUri uri = TelUri.parse("tel:");
		assertEquals("", uri.getNumber());
		assertNull(uri.getExtension());
		assertNull(uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertTrue(uri.getParameters().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_not_tel_uri() {
		TelUri.parse("http://www.ietf.org");
	}

	@Test
	public void builder_global() {
		TelUri uri = new TelUri.Builder("+1-212-555-0101").build();
		assertEquals("+1-212-555-0101", uri.getNumber());
		assertNull(uri.getExtension());
		assertNull(uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertTrue(uri.getParameters().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void builder_global_no_plus() {
		new TelUri.Builder("1-212-555-0101");
	}

	@Test(expected = IllegalArgumentException.class)
	public void builder_global_invalid() {
		new TelUri.Builder("abc123");
	}

	@Test
	public void builder_local() {
		TelUri uri = new TelUri.Builder("0101", "example.com").build();
		assertEquals("0101", uri.getNumber());
		assertNull(uri.getExtension());
		assertEquals("example.com", uri.getPhoneContext());
		assertNull(uri.getIsdnSubaddress());
		assertTrue(uri.getParameters().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void builder_local_invalid() {
		new TelUri.Builder("abc123", "example.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void builder_extension_invalid() {
		new TelUri.Builder("+1-212-555-0101").extension("!not-valid!");
	}

	@Test
	public void builder_parameter() {
		TelUri uri = new TelUri.Builder("+1-212-555-0101").parameter("one", "1").parameter("two", "2").parameter("one", null).build();

		assertNull(uri.getParameter("one")); //removed
		assertEquals("2", uri.getParameter("two"));
		assertNull(uri.getParameter("three"));

		Map<String, String> expected = new HashMap<String, String>();
		expected.put("two", "2");
		assertEquals(expected, uri.getParameters());
	}

	@Test(expected = IllegalArgumentException.class)
	public void builder_parmaeter_invalid_name() {
		new TelUri.Builder("+1-212-555-0101").parameter("!not-valid!", "the value");
	}

	@Test
	public void builder_copy_constructor() {
		TelUri orig = new TelUri.Builder("+1-212-555-0101").extension("111").isdnSubaddress("isdn").parameter("name", "value").build();
		TelUri copy = new TelUri.Builder(orig).build();
		assertEquals(orig, copy);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getParameters_unmodifiable() {
		TelUri uri = new TelUri.Builder("+1-212-555-0101").build();
		uri.getParameters().put("one", "1");
	}

	@Test
	public void toString_() {
		TelUri uri = new TelUri.Builder("+1-212-555-0101").extension("101").build();
		assertEquals("tel:+1-212-555-0101;ext=101", uri.toString());
	}

	//see RFC 3966, bottom of p.5
	@Test
	public void toString_parameter_order() {
		TelUri uri = new TelUri.Builder("1010", "example.com").extension("101").isdnSubaddress("isdn").parameter("zebra", "z-value").parameter("apple", "a-value").build();
		assertEquals("tel:1010;ext=101;isub=isdn;phone-context=example.com;apple=a-value;zebra=z-value", uri.toString());
	}

	@Test
	public void toString_special_chars_in_param_value() {
		TelUri uri = new TelUri.Builder("+1-212-555-0101").parameter("param", "with = special & chars " + (char) 128).build();
		assertEquals("tel:+1-212-555-0101;param=with%20%3d%20special%20&%20chars%20%80", uri.toString());
	}
}
