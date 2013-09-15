package ezvcard.io;

import org.junit.Test;

import static org.junit.Assert.*;

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
	@Test
	public void parseGroup() {
		VCardLine line = VCardLine.parse("iteM1.NOTE: This is a note.");
		assertEquals("iteM1", line.getGroup()); //it should preserve case
		assertEquals("NOTE", line.getTypeName());
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals(" This is a note.", line.getValue());
	}

	@Test
	public void parseTypeName() {
		VCardLine line = VCardLine.parse("NoTe: This is a note.");
		assertNull(line.getGroup());
		assertEquals("NoTe", line.getTypeName()); //it should preserve case
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals(" This is a note.", line.getValue());
	}

	@Test
	public void parseSubTypes() {
		VCardLine line = VCardLine.parse("ADR;TyPE=worK;TYPE=dom;LABEL=\"123 \\;Main; St.\\n\\\"Austin\\\", :TX: 12345\": ;;123 Main Str;Austin;TX;12345;US");
		assertNull(line.getGroup());
		assertEquals("ADR", line.getTypeName());

		//it should preserve case
		assertEquals("TyPE", line.getSubTypes().get(0)[0]);
		assertEquals("worK", line.getSubTypes().get(0)[1]);

		//sub type with identical name
		assertEquals("TYPE", line.getSubTypes().get(1)[0]);
		assertEquals("dom", line.getSubTypes().get(1)[1]);

		//special chars are OK to use inside of double quotes
		assertEquals("LABEL", line.getSubTypes().get(2)[0]);
		assertEquals("123 ;Main; St.\r\n\"Austin\", :TX: 12345", line.getSubTypes().get(2)[1]);

		assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());

		//nameless sub types (v2.1 style)
		line = VCardLine.parse("ADR;WORK;DOM: ;;123 Main Str;Austin;TX;12345;US");
		assertNull(line.getGroup());
		assertEquals("ADR", line.getTypeName());
		assertNull(line.getSubTypes().get(0)[0]);
		assertEquals("WORK", line.getSubTypes().get(0)[1]);
		assertNull(line.getSubTypes().get(1)[0]);
		assertEquals("DOM", line.getSubTypes().get(1)[1]);
		assertEquals(" ;;123 Main Str;Austin;TX;12345;US", line.getValue());
	}

	@Test
	public void parseValue() {
		VCardLine line;

		//no unescaping should be done on the value
		line = VCardLine.parse("NOTE: This \\,is a\\n \\;note\\;.");
		assertNull(line.getGroup());
		assertEquals("NOTE", line.getTypeName());
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals(" This \\,is a\\n \\;note\\;.", line.getValue());

		//value-less type
		line = VCardLine.parse("NOTE:");
		assertNull(line.getGroup());
		assertEquals("NOTE", line.getTypeName());
		assertTrue(line.getSubTypes().isEmpty());
		assertEquals("", line.getValue());
	}

	@Test
	public void parseInvalidLine() {
		VCardLine line = VCardLine.parse("This is not a valid vCard line.");
		assertNull(line);
	}
}
