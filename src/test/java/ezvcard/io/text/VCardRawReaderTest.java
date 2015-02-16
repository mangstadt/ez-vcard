package ezvcard.io.text;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import org.junit.Test;

import ezvcard.VCardVersion;

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
public class VCardRawReaderTest {
	@Test
	public void basic() throws Throwable {
		//@formatter:off
		String vcard =
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"FN:John Doe\r\n" +
		"NOTE;LANGUAGE=en-us:My vCard\r\n" +
		"END:VCARD";
		//@formatter:on
		VCardRawReader reader = create(vcard);

		assertEquals(line("BEGIN").value("VCARD").build(), reader.readLine());
		assertEquals(line("VERSION").value("4.0").build(), reader.readLine());
		assertEquals(line("FN").value("John Doe").build(), reader.readLine());
		assertEquals(line("NOTE").param("LANGUAGE", "en-us").value("My vCard").build(), reader.readLine());
		assertEquals(line("END").value("VCARD").build(), reader.readLine());
		assertNull(reader.readLine());
	}

	@Test
	public void version() throws Throwable {
		String vcard = "verSION:4.0";
		VCardRawReader reader = create(vcard);

		assertEquals(VCardVersion.V2_1, reader.getVersion());
		assertEquals(line("verSION").value("4.0").build(), reader.readLine());
		assertEquals(VCardVersion.V4_0, reader.getVersion());
		assertNull(reader.readLine());
	}

	@Test(expected = InvalidVersionException.class)
	public void invalid_version() throws Throwable {
		String vcard = "VERSION:invalid";
		VCardRawReader reader = create(vcard);
		reader.readLine();
	}

	@Test
	public void group() throws Throwable {
		String vcard = "iteM1.NOTE:This is a note.";
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("NOTE").group("iteM1").value("This is a note.").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void nameless_parameters() throws Throwable {
		String vcard = "ADR;WOrK;dOM:;;123 Main Str;Austin;TX;12345;US";
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("ADR").param(null, "WOrK", "dOM").value(";;123 Main Str;Austin;TX;12345;US").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void parameters_with_whitespace_around_equals() throws Throwable {
		//2.1 (removes)
		{
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("TYPE", "WOrK").param("TYPE", "dOM").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}

		//3.0 (keeps)
		{
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("TYPE\t", " WOrK").param("TYPE \t", "  dOM").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}

		//4.0 (keeps)
		{
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("TYPE\t", " WOrK").param("TYPE \t", "  dOM").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}
	}

	@Test
	public void multi_valued_parameters() throws Throwable {
		//2.1 (doesn't recognize them)
		{
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			"ADR;TYPE=dom,\"foo,bar\\;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("TYPE", "dom,\"foo,bar;baz\",work,foo=bar").param("PREF", "1").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}

		//3.0
		{
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			"ADR;TYPE=dom,\"foo,bar;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("TYPE", "dom", "foo,bar;baz", "work", "foo=bar").param("PREF", "1").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}

		//4.0
		{
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			"ADR;TYPE=dom,\"foo,bar;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("TYPE", "dom", "foo,bar;baz", "work", "foo=bar").param("PREF", "1").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}
	}

	@Test
	public void character_escaping_in_parameters() throws Throwable {
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
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
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
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
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
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
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
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^Main^ St." + NEWLINE + "Section; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "\"Austin\", \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
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
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
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
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^Main^ St." + NEWLINE + "Section; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "\"Austin\", \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}
	}

	@Test
	public void empty_value() throws Throwable {
		String vcard = "NOTE:";
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("NOTE").value("").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void trim_value() throws Throwable {
		String vcard = "NOTE: Hello world!\t";
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("NOTE").value("Hello world!").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test(expected = VCardParseException.class)
	public void invalid_line() throws Throwable {
		String vcard = "This is not a valid vCard line.";
		VCardRawReader reader = create(vcard);
		reader.readLine();
	}

	@Test
	public void empty_input() throws Throwable {
		String vcard = "";
		VCardRawReader reader = create(vcard);
		assertNull(reader.readLine());
	}

	private static VCardRawReader create(String vcard) {
		return new VCardRawReader(new StringReader(vcard));
	}

	private static VCardRawLine.Builder line(String name) {
		return new VCardRawLine.Builder().name(name);
	}
}
