package ezvcard.io.text;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
			//b: backslash-escaped backslash
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			//          1    2     2     3        4     5            6        7  8       8   9   9  b      a
			"ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" \\\\123^45:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" \\123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
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
			//b: backslash-escaped backslash
			//@formatter:off
			String vcard = 
			"VERSION:2.1\r\n" +
			//          1    2     2     3        4     5            6        7  8       8   9   9         a
			"ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" \\\\123^45:;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" \\123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
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
			//a: backslash-escaped backslash
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8    a      9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" \\\\123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" \\123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
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
			//a: backslash-escaped backslash
			//@formatter:off
			String vcard = 
			"VERSION:3.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8    a      9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" \\\\123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^Main^ St." + NEWLINE + "Section; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "\"Austin\", \"TX\" \\123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
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
			//a: backslash-escaped backslash
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8    a      9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" \\\\123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" \\123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
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
			//a: backslash-escaped backslash
			//@formatter:off
			String vcard = 
			"VERSION:4.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8    a      9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" \\\\123^45\":;;123 Main Str;Austin;TX;12345;US";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			VCardRawLine expected = line("ADR").param("LABEL", "1\\23 ^Main^ St." + NEWLINE + "Section; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "\"Austin\", \"TX\" \\123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}
	}

	@Test
	public void non_standard_newlines() throws Throwable {
		//@formatter:off
		String vcard =
		"NOTE:note0\r\n" + //standard newline
		"NOTE:note1\n" +
		"NOTE:note2\r" +
		"NOTE:note3"; //no trailing newline
		//@formatter:on
		VCardRawReader reader = create(vcard);

		assertEquals(line("NOTE").value("note0").build(), reader.readLine());
		assertEquals(line("NOTE").value("note1").build(), reader.readLine());
		assertEquals(line("NOTE").value("note2").build(), reader.readLine());
		assertEquals(line("NOTE").value("note3").build(), reader.readLine());
		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding() throws Throwable {
		//@formatter:off
		String vcard =

		//unfolded line
		"FN:Michael Angstadt\r\n" +

		//empty lines should be ignored
		"\r\n" +

		//this line is folded
		"NOTE:folded \r\n" +
		" line\r\n" +

		//this line is folded with multiple whitespace characters
		"NOTE:one \r\n" +
		" two \r\n" +
		"  three \r\n" +
		"\t \tfour\r\n";
		//@formatter:on

		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("FN").value("Michael Angstadt").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		expected = line("NOTE").value("folded line").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		expected = line("NOTE").value("one two  three  \tfour").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_empty_folded_line() throws Throwable {
		//@formatter:off
		String vcard =
		"NOTE:line1\r\n" +
		" \r\n" +
		" line2\r\n";
		//@formatter:on
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("NOTE").value("line1line2").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_one_character_per_line() throws Throwable {
		//@formatter:off
		String vcard =
		"N\r\n" +
		" O\r\n" +
		" T\r\n" +
		" E\r\n" +
		" :\r\n" +
		" v\r\n" +
		" a\r\n" +
		" l\r\n" +
		" u\r\n" +
		" e\r\n";
		//@formatter:on
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("NOTE").value("value").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_parameters_on_multiple_lines() throws Throwable {
		{
			//@formatter:off
			String vcard =
			"VERSION:2.1\r\n" +
			"NOTE;ONE=1;TWO=the nu\\;m\r\n" +
			" ber two:value\r\n";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("NOTE").param("ONE", "1").param("TWO", "the nu;mber two").value("value").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}

		{
			//@formatter:off
			String vcard =
			"VERSION:3.0\r\n" +
			"NOTE;ONE=1;TWO=\"the nu;m\r\n" +
			" ber two\":value\r\n";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("NOTE").param("ONE", "1").param("TWO", "the nu;mber two").value("value").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}

		{
			//@formatter:off
			String vcard =
			"VERSION:4.0\r\n" +
			"NOTE;ONE=1;TWO=\"the nu;m\r\n" +
			" ber two\":value\r\n";
			//@formatter:on
			VCardRawReader reader = create(vcard);
			reader.readLine();

			VCardRawLine expected = line("NOTE").param("ONE", "1").param("TWO", "the nu;mber two").value("value").build();
			VCardRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			assertNull(reader.readLine());
		}
	}

	/**
	 * Tests the issues that Outlook has with folded lines that are
	 * quoted-printable.
	 */
	@Test
	public void line_unfolding_quoted_printable() throws Throwable {
		//@formatter:off
		String vcard =
		
		//one line
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5\r\n" +

		//two lines
		//(without "ENCODING" subtype name)
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345\r\n" +

		//two lines with an empty line at the end
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345=0D=0A=\r\n" +
		"\r\n" +

		//two lines with an empty line in the middle
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"=\r\n" +
		"New York, New York  12345\r\n" +

		//three lines
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345=0D=0A=\r\n" +
		"USA\r\n" +

		//four lines
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345=0D=0A=\r\n" +
		"USA=0D=0A=\r\n" +
		"4th line\r\n" +

		//a quoted-printable line whose additional lines *are* folded where the lines end in "="
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		" New York, New York  12345=0D=0A=\r\n" +
		"  USA\r\n" +

		//a quoted-printable line whose additional lines *are* folded, where the lines don't end in "="
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A\r\n" +
		" New York, New York  12345=0D=0A\r\n" +
		"  USA\r\n";
		//@formatter:on

		VCardRawReader reader = create(vcard);

		//@formatter:off
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param(null, "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param(null, "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0A")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param(null, "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA")
			.build(),
		reader.readLine());

		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA=0D=0A4th line")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0A USA")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0A USA")
			.build(),
		reader.readLine());

		//@formatter:on

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_quoted_printable_parameters_folded() throws Throwable {
		//@formatter:off
		String vcard =
		"NOTE;ENCOD\r\n" +
		" ING=quoted-printable:foo=\r\n" +
		"bar\r\n";
		//@formatter:on
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("NOTE").param("ENCODING", "quoted-printable").value("foobar").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	/**
	 * Tests to make sure it's not just looking for the string
	 * "quoted-printable" somewhere before the property value (like it used to
	 * do).
	 */
	@Test
	public void quoted_printable_detection_using_parameter() throws Throwable {
		//@formatter:off
		String vcard =
		"NOTE;PARAM=quoted-printable:foo=\r\n" +
		"bar\r\n" +
		"NOTE;quoted-printable:foo=\r\n" +
		"bar\r\n" +
		"NOTE;encoding=quoted-printable:foo=\r\n" +
		"bar\r\n";
		//@formatter:on
		VCardRawReader reader = create(vcard);

		VCardRawLine expected = line("NOTE").param("PARAM", "quoted-printable").value("foo=").build();
		VCardRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		try {
			reader.readLine();
			fail();
		} catch (VCardParseException e) {
			//expected
		}

		expected = line("NOTE").param(null, "quoted-printable").value("foobar").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		expected = line("NOTE").param("ENCODING", "quoted-printable").value("foobar").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void getLineNumber() throws Exception {
		//@formatter:off
		String vcard =
		"NOTE:one\n" +
		" two\n" +
		"\n" +
		"NOTE:three\n" +
		" four\n";
		//@formatter:on

		VCardRawReader reader = create(vcard);

		assertEquals(1, reader.getLineNumber());

		reader.readLine();
		assertEquals(1, reader.getLineNumber());

		reader.readLine();
		assertEquals(4, reader.getLineNumber());

		assertNull(reader.readLine());
	}

	@Test(expected = VCardParseException.class)
	public void invalid_line() throws Throwable {
		String vcard = "This is not a valid vCard line.";
		VCardRawReader reader = create(vcard);
		reader.readLine();
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
	public void empty_input() throws Throwable {
		String vcard = "";
		VCardRawReader reader = create(vcard);
		assertNull(reader.readLine());
	}

	@Test
	public void empty_input_just_newlines() throws Throwable {
		String vcard = "\r\n\r\n\r\n";
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
