package ezvcard.io.text;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.Arrays;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.io.text.VCardRawReader.StopReadingException;
import ezvcard.io.text.VCardRawReader.VCardDataStreamListener;
import ezvcard.parameter.VCardParameters;

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
@SuppressWarnings("resource")
public class VCardRawReaderTest {
	@Test
	public void basic() throws Exception {
		//@formatter:off
		String vcard =
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"FN:John Doe\r\n" +
		"NOTE;LANGUAGE=en-us:My vCard\r\n" +
		"END:VCARD";
		//@formatter:on
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void beginComponent_(String actual) {
				assertEquals("VCARD", actual);
			}

			@Override
			protected void readVersion_(VCardVersion version) {
				assertEquals(VCardVersion.V4_0, version);
			}

			@Override
			protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
				if (line == 3) {
					assertNull(group);
					assertEquals("FN", name);
					assertEquals("John Doe", value);
					assertEquals(0, parameters.size());
				} else if (line == 4) {
					assertNull(group);
					assertEquals("NOTE", name);
					assertEquals("My vCard", value);
					assertEquals(1, parameters.size());
					assertEquals(Arrays.asList("en-us"), parameters.get("LANGUAGE"));
				} else {
					fail();
				}
			}

			@Override
			protected void endComponent_(String actual) {
				assertEquals("VCARD", actual);
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledBeginComponent);
		assertEquals(2, listener.calledReadProperty);
		assertEquals(1, listener.calledReadVersion);
		assertEquals(1, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void version() throws Exception {
		String vcard = "verSION:4.0";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void readVersion_(VCardVersion version) {
				assertEquals(VCardVersion.V4_0, version);
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(1, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void version_invalid() throws Exception {
		String vcard = "VERSION:invalid";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void invalidVersion_(String version) {
				assertEquals("invalid", version);
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(1, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void component() throws Exception {
		//@formatter:off
		String vcard =
		"BEGIN:VCARD\r\n" +
		"end:vcard\r\n" + 
		"begin:vcard\r\n" +
		"END:VCARD\r\n";
		//@formatter:on
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void beginComponent_(String actual) {
				if (line == 1) {
					assertEquals("VCARD", actual);
				} else if (line == 3) {
					assertEquals("vcard", actual);
				}
			}

			@Override
			protected void endComponent_(String actual) {
				if (line == 2) {
					assertEquals("vcard", actual);
				} else if (line == 4) {
					assertEquals("VCARD", actual);
				}
			}
		};
		reader.start(listener);

		assertEquals(2, listener.calledBeginComponent);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(2, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void property_with_group() throws Exception {
		String vcard = "iteM1.NoTE:This is a note.";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
				assertEquals("iteM1", group);
				assertEquals("NoTE", name);
				assertEquals("This is a note.", value);
				assertEquals(0, parameters.size());
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void property_without_group() throws Exception {
		String vcard = "NoTE:This is a note.";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
				assertNull(group);
				assertEquals("NoTE", name);
				assertEquals("This is a note.", value);
				assertEquals(0, parameters.size());
			}
		};
		reader.start(listener);

		assertTrue(reader.eof());
	}

	@Test
	public void parameters_nameless() throws Exception {
		String vcard = "ADR;WOrK;dOM:;;123 Main Str;Austin;TX;12345;US";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
				assertNull(group);
				assertEquals("ADR", name);
				assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
				assertEquals(2, parameters.size());
				assertEquals(Arrays.asList("WOrK", "dOM"), parameters.get(null));
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void parameters_whitespace_around_equals() throws Exception {
		//2.1 (removes)
		{
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(2, parameters.size());
					assertEquals(Arrays.asList("WOrK", "dOM"), parameters.get("TYPE"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V2_1, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//3.0 (keeps)
		{
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(2, parameters.size());
					assertEquals(Arrays.asList(" WOrK"), parameters.get("TYPE\t"));
					assertEquals(Arrays.asList("  dOM"), parameters.get("TYPE \t"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V3_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//4.0 (keeps)
		{
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(2, parameters.size());
					assertEquals(Arrays.asList(" WOrK"), parameters.get("TYPE\t"));
					assertEquals(Arrays.asList("  dOM"), parameters.get("TYPE \t"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V4_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}
	}

	@Test
	public void parameters_multi_valued() throws Exception {
		//2.1 (doesn't recognize them)
		{
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			"ADR;TYPE=dom,\"foo,bar\\;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(2, parameters.size());
					assertEquals(Arrays.asList("dom,\"foo,bar;baz\",work,foo=bar"), parameters.get("TYPE"));
					assertEquals(Arrays.asList("1"), parameters.get("PREF"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V2_1, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());

		}

		//3.0
		{
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			"ADR;TYPE=dom,\"foo,bar;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(5, parameters.size());
					assertEquals(Arrays.asList("dom", "foo,bar;baz", "work", "foo=bar"), parameters.get("TYPE"));
					assertEquals(Arrays.asList("1"), parameters.get("PREF"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V3_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//4.0
		{
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			"ADR;TYPE=dom,\"foo,bar;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(5, parameters.size());
					assertEquals(Arrays.asList("dom", "foo,bar;baz", "work", "foo=bar"), parameters.get("TYPE"));
					assertEquals(Arrays.asList("1"), parameters.get("PREF"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V4_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}
	}

	@Test
	public void parameters_escaping() throws Exception {
		//2.1 without caret escaping
		{
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: backslash-escaped semi-colon (must be escaped in 2.1)
			//5: caret-escaped newline (uppercase N)
			//6: backslash-escaped newline (lowercase n)
			//7: backslash-escaped newline (uppercase N)
			//8: caret-escaped double quote
			//9: un-escaped double quote (no special meaning in 2.1)
			//a: caret that doesn't escape anything
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			//          1    2     2     3        4     5            6        7  8       8   9   9     a
			"ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" 123^45:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));
			reader.setCaretDecodingEnabled(false);

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(1, parameters.size());
					assertEquals(Arrays.asList("1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45"), parameters.get("LABEL"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V2_1, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//2.1 with caret escaping (no difference)
		{
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: backslash-escaped semi-colon (must be escaped in 2.1)
			//5: caret-escaped newline (uppercase N)
			//6: backslash-escaped newline (lowercase n)
			//7: backslash-escaped newline (uppercase N)
			//8: caret-escaped double quote
			//9: un-escaped double quote (no special meaning in 2.1)
			//a: caret that doesn't escape anything
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			//          1    2     2     3        4     5            6        7  8       8   9   9     a
			"ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" 123^45:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));
			reader.setCaretDecodingEnabled(true);

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(1, parameters.size());
					assertEquals(Arrays.asList("1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45"), parameters.get("LABEL"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V2_1, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//3.0 without caret escaping
		{
			//0: value double quoted because of semi-colon and comma chars
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped newline (lowercase n)
			//6: backslash-escaped newline (uppercase N)
			//7: caret-escaped double quote
			//8: backslash-escaped double quote (not part of the standard, included for interoperability)
			//9: caret that doesn't escape anything
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));
			reader.setCaretDecodingEnabled(false);

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(1, parameters.size());
					assertEquals(Arrays.asList("1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45"), parameters.get("LABEL"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V3_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//3.0 with caret escaping
		{
			//0: value double quoted because of semi-colon and comma chars
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped newline (lowercase n)
			//6: backslash-escaped newline (uppercase N)
			//7: caret-escaped double quote
			//8: backslash-escaped double quote (not part of the standard, included for interoperability)
			//9: caret that doesn't escape anything
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));
			reader.setCaretDecodingEnabled(true);

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(1, parameters.size());
					assertEquals(Arrays.asList("1\\23 ^Main^ St." + NEWLINE + "Section; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "\"Austin\", \"TX\" 123^45"), parameters.get("LABEL"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V3_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//4.0 without caret escaping
		{
			//0: value double quoted because of semi-colon and comma chars
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped newline (lowercase n)
			//6: backslash-escaped newline (uppercase N)
			//7: caret-escaped double quote
			//8: backslash-escaped double quote (not part of the standard, included for interoperability)
			//9: caret that doesn't escape anything
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));
			reader.setCaretDecodingEnabled(false);

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(1, parameters.size());
					assertEquals(Arrays.asList("1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45"), parameters.get("LABEL"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V4_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}

		//4.0 with caret escaping
		{
			//0: value double quoted because of semi-colon and comma chars
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped newline (lowercase n)
			//6: backslash-escaped newline (uppercase N)
			//7: caret-escaped double quote
			//8: backslash-escaped double quote (not part of the standard, included for interoperability)
			//9: caret that doesn't escape anything
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = new VCardRawReader(new StringReader(vcard));
			reader.setCaretDecodingEnabled(true);

			TestListener listener = new TestListener() {
				@Override
				protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
					assertNull(group);
					assertEquals("ADR", name);
					assertEquals(";;123 Main Str;Austin;TX;12345;US", value);
					assertEquals(1, parameters.size());
					assertEquals(Arrays.asList("1\\23 ^Main^ St." + NEWLINE + "Section; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "\"Austin\", \"TX\" 123^45"), parameters.get("LABEL"));
				}

				@Override
				protected void readVersion_(VCardVersion version) {
					assertEquals(VCardVersion.V4_0, version);
				}
			};
			reader.start(listener);

			assertEquals(0, listener.calledBeginComponent);
			assertEquals(1, listener.calledReadProperty);
			assertEquals(1, listener.calledReadVersion);
			assertEquals(0, listener.calledEndComponent);
			assertEquals(0, listener.calledInvalidLine);
			assertEquals(0, listener.calledInvalidVersion);
			assertTrue(reader.eof());
		}
	}

	@Test
	public void value() throws Exception {
		String vcard = "NOTE:This \\,is a\\n \\;note\\;.";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
				assertNull(group);
				assertEquals("NOTE", name);
				assertEquals("This \\,is a\\n \\;note\\;.", value);
				assertEquals(0, parameters.size());
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void value_empty() throws Exception {
		String vcard = "NOTE:";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
				assertNull(group);
				assertEquals("NOTE", name);
				assertEquals("", value);
				assertEquals(0, parameters.size());
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void invalid_line() throws Exception {
		String vcard = "This is not a valid vCard line.";
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			protected void invalidLine_(String line) {
				assertEquals("This is not a valid vCard line.", line);
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(1, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void stop_and_continue() throws Exception {
		//@formatter:off
		String vcard =
		"BEGIN:VCARD\r\n" +
		"PROP1:one\r\n" +
		"PROP2:two\r\n" +
		"PROP3:three\r\n" +
		"END:VCARD\r\n";
		//@formatter:on
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			@Override
			public void beginComponent_(String actual) {
				assertEquals("VCARD", actual);
			}

			@Override
			public void readProperty_(String group, String name, VCardParameters parameters, String value) {
				if (name.equals("PROP2")) {
					throw new StopReadingException();
				}
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledBeginComponent);
		assertEquals(2, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertFalse(reader.eof());

		//////////////////
		//continue reading
		//////////////////

		listener = new TestListener() {
			@Override
			public void beginComponent_(String actual) {
				//empty
			}

			@Override
			public void readProperty_(String group, String name, VCardParameters parameters, String value) {
				assertEquals("PROP3", name);
			}

			@Override
			public void endComponent_(String actual) {
				assertEquals("VCARD", actual);
			}
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(1, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	@Test
	public void empty() throws Exception {
		//@formatter:off
		String vcard =
		"";
		//@formatter:on
		VCardRawReader reader = new VCardRawReader(new StringReader(vcard));

		TestListener listener = new TestListener() {
			//empty
		};
		reader.start(listener);

		assertEquals(0, listener.calledBeginComponent);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(0, listener.calledReadVersion);
		assertEquals(0, listener.calledEndComponent);
		assertEquals(0, listener.calledInvalidLine);
		assertEquals(0, listener.calledInvalidVersion);
		assertTrue(reader.eof());
	}

	private abstract class TestListener implements VCardDataStreamListener {
		int line = 0;
		int calledBeginComponent = 0, calledReadProperty = 0, calledReadVersion = 0, calledEndComponent = 0, calledInvalidLine = 0, calledInvalidVersion = 0;

		public final void beginComponent(String actual) {
			line++;
			calledBeginComponent++;
			beginComponent_(actual);
		}

		public final void readProperty(String group, String name, VCardParameters parameters, String value) {
			line++;
			calledReadProperty++;
			readProperty_(group, name, parameters, value);
		}

		public final void readVersion(VCardVersion version) {
			line++;
			calledReadVersion++;
			readVersion_(version);
		}

		public final void endComponent(String actual) {
			line++;
			calledEndComponent++;
			endComponent_(actual);
		}

		public final void invalidLine(String line) {
			this.line++;
			calledInvalidLine++;
			invalidLine_(line);
		}

		public final void invalidVersion(String version) {
			line++;
			calledInvalidVersion++;
			invalidVersion_(version);
		}

		protected void beginComponent_(String actual) {
			fail("\"beginComponent\" should not have been called.");
		}

		protected void readProperty_(String group, String name, VCardParameters parameters, String value) {
			fail("\"readProperty\" should not have been called.");
		}

		protected void readVersion_(VCardVersion version) {
			fail("\"readVersion\" should not have been called.");
		}

		protected void endComponent_(String actual) {
			fail("\"endComponent\" should not have been called.");
		}

		protected void invalidLine_(String line) {
			fail("\"invalidLine\" should not have been called.");
		}

		protected void invalidVersion_(String version) {
			fail("\"invalidVersion\" should not have been called.");
		}
	}
}
