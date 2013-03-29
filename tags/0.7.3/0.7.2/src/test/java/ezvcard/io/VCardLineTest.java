package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import ezvcard.VCardVersion;

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
public class VCardLineTest {
	private String newline = System.getProperty("line.separator");

	@Test
	public void group() {
		VCardLine line = VCardLine.parse("iteM1.NOTE: This is a note.", VCardVersion.V2_1, false);
		assertEquals("iteM1", line.getGroup()); //it should preserve case
		assertEquals("NOTE", line.getTypeName());
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals(" This is a note.", line.getValue());
	}

	@Test
	public void no_group() {
		VCardLine line = VCardLine.parse("NOTE: This is a note.", VCardVersion.V2_1, false);
		assertNull(line.getGroup());
		assertEquals("NOTE", line.getTypeName());
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals(" This is a note.", line.getValue());
	}

	@Test
	public void property_name() {
		VCardLine line = VCardLine.parse("NoTe: This is a note.", VCardVersion.V2_1, false);
		assertNull(line.getGroup());
		assertEquals("NoTe", line.getTypeName()); //it should preserve case
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals(" This is a note.", line.getValue());
	}

	@Test
	public void nameless_v21_parameters() {
		VCardLine line = VCardLine.parse("ADR;WOrK;dOM: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V2_1, false);

		Iterator<List<String>> it = line.getSubTypes().iterator();

		List<String> subType = it.next();
		assertEquals(Arrays.asList(null, "WOrK"), subType);

		subType = it.next();
		assertEquals(Arrays.asList(null, "dOM"), subType);

		assertFalse(it.hasNext());

		assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
	}

	@Test
	public void whitespace_around_equals() {
		//2.1 (removes)
		{
			VCardLine line = VCardLine.parse("ADR;TYPE\t= WOrK;TYPE \t=  dOM: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V2_1, false);

			Iterator<List<String>> it = line.getSubTypes().iterator();

			List<String> subType = it.next();
			assertEquals(Arrays.asList("TYPE", "WOrK"), subType);

			subType = it.next();
			assertEquals(Arrays.asList("TYPE", "dOM"), subType);

			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
		}

		//3.0 (keeps)
		{
			VCardLine line = VCardLine.parse("ADR;TYPE\t= WOrK;TYPE \t=  dOM: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V3_0, false);

			Iterator<List<String>> it = line.getSubTypes().iterator();

			List<String> subType = it.next();
			assertEquals(Arrays.asList("TYPE\t", " WOrK"), subType);

			subType = it.next();
			assertEquals(Arrays.asList("TYPE \t", "  dOM"), subType);

			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
		}

		//4.0 (keeps)
		{
			VCardLine line = VCardLine.parse("ADR;TYPE\t= WOrK;TYPE \t=  dOM: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V4_0, false);

			Iterator<List<String>> it = line.getSubTypes().iterator();

			List<String> subType = it.next();
			assertEquals(Arrays.asList("TYPE\t", " WOrK"), subType);

			subType = it.next();
			assertEquals(Arrays.asList("TYPE \t", "  dOM"), subType);

			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
		}
	}

	@Test
	public void multi_valued_parameters() {
		//2.1 (doesn't recognize them)
		{
			VCardLine line = VCardLine.parse("ADR;TYPE=dom,\"foo,bar\\;baz\",work,foo=bar;PREF=1: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V2_1, false);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("TYPE", "dom,\"foo,bar;baz\",work,foo=bar"), subType);
			subType = it.next();
			assertEquals(Arrays.asList("PREF", "1"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());

		}
		//3.0
		{
			VCardLine line = VCardLine.parse("ADR;TYPE=dom,\"foo,bar;baz\",work,foo=bar;PREF=1: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V3_0, false);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("TYPE", "dom", "foo,bar;baz", "work", "foo=bar"), subType);
			subType = it.next();
			assertEquals(Arrays.asList("PREF", "1"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
		}

		//4.0
		{
			VCardLine line = VCardLine.parse("ADR;TYPE=dom,\"foo,bar;baz\",work,foo=bar;PREF=1: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V4_0, false);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("TYPE", "dom", "foo,bar;baz", "work", "foo=bar"), subType);
			subType = it.next();
			assertEquals(Arrays.asList("PREF", "1"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
		}
	}

	@Test
	public void escaping() {
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
			//                                           1    2     2     3        4     5            6        7  8       8   9   9     a
			VCardLine line = VCardLine.parse("ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" 123^45: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V2_1, false);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + newline + "Apt 10" + newline + "^'Austin^', \"TX\" 123^45"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
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
			//                                           1    2     2     3        4     5            6        7  8       8   9   9     a
			VCardLine line = VCardLine.parse("ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" 123^45: ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V2_1, false);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + newline + "Apt 10" + newline + "^'Austin^', \"TX\" 123^45"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
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
			//                                          0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			VCardLine line = VCardLine.parse("ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\": ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V3_0, false);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + newline + "Apt 10" + newline + "^'Austin^', \"TX\" 123^45"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
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
			//                                          0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			VCardLine line = VCardLine.parse("ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\": ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V3_0, true);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("LABEL", "1\\23 ^Main^ St." + newline + "Section; 12^NBuilding 20" + newline + "Apt 10" + newline + "\"Austin\", \"TX\" 123^45"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
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
			//                                          0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			VCardLine line = VCardLine.parse("ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\": ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V4_0, false);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + newline + "Apt 10" + newline + "^'Austin^', \"TX\" 123^45"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
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
			//                                          0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			VCardLine line = VCardLine.parse("ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\": ;;123 Main Str;Austin;TX;12345;US", VCardVersion.V4_0, true);
			assertNull(line.getGroup());
			assertEquals("ADR", line.getTypeName());

			Iterator<List<String>> it = line.getSubTypes().iterator();
			List<String> subType = it.next();
			assertEquals(Arrays.asList("LABEL", "1\\23 ^Main^ St." + newline + "Section; 12^NBuilding 20" + newline + "Apt 10" + newline + "\"Austin\", \"TX\" 123^45"), subType);
			assertFalse(it.hasNext());

			assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
		}
	}

	@Test
	public void value() {
		VCardLine line;

		//no unescaping should be done on the value
		line = VCardLine.parse("NOTE: This \\,is a\\n \\;note\\;.", VCardVersion.V2_1, false);
		assertNull(line.getGroup());
		assertEquals("NOTE", line.getTypeName());
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals(" This \\,is a\\n \\;note\\;.", line.getValue());

		//value-less type
		line = VCardLine.parse("NOTE:", VCardVersion.V2_1, false);
		assertNull(line.getGroup());
		assertEquals("NOTE", line.getTypeName());
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals("", line.getValue());
	}

	@Test
	public void invalid_line() {
		VCardLine line = VCardLine.parse("This is not a valid vCard line.", VCardVersion.V2_1, false);
		assertNull(line);
	}
}
