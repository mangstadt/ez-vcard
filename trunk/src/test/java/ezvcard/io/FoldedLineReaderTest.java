package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
public class FoldedLineReaderTest {
	@Test
	public void readLine() throws Exception {
		StringBuilder sb = new StringBuilder();

		//unfolded line
		sb.append("FN: Michael Angstadt\n");

		//empty line
		sb.append("\n");

		//=========
		//quoted-printable lines whose additional lines are not folded (Outlook craziness)
		
		//one line
		sb.append("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5\n");
		
		//two lines
		//(without "ENCODING" subtype name)
		sb.append("LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n");
		sb.append("New York, New York  12345\n");
		
		//three lines
		sb.append("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n");
		sb.append("New York, New York  12345=0D=0A=\n");
		sb.append("USA\n");
		
		//it should recognize when the string "QUOTED-PRINTABLE" is not to the left of the colon
		sb.append("LABEL;HOME:Some text QUOTED-PRINTABLE more text=\n");
		
		//four lines
		sb.append("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n");
		sb.append("New York, New York  12345=0D=0A=\n");
		sb.append("USA=0D=0A=\n");
		sb.append("4th line\n");
		
		//=========
		
		//a quoted-printable line whose additional lines *are* folded where the lines end in "="
		sb.append("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n");
		sb.append(" New York, New York  12345=0D=0A=\n");
		sb.append(" USA\n");
		
		//a quoted-printable line whose additional lines *are* folded, where the lines don't end in "="
		sb.append("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A\n");
		sb.append(" New York, New York  12345=0D=0A\n");
		sb.append(" USA\n");

		//this line is folded
		sb.append("NOTE:folded \n line\n");

		//this line is folded with multiple whitespace characters
		sb.append("NOTE:one \n two \n  three \n \t four");

		FoldedLineReader reader = new FoldedLineReader(sb.toString());
		assertEquals("FN: Michael Angstadt", reader.readLine());
		assertEquals("", reader.readLine()); //empty lines are not ignored
		
		//test the issues Outlook has with folding lines that are QUOTED-PRINTABLE
		assertEquals("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5", reader.readLine());
		assertEquals("LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0ANew York, New York  12345", reader.readLine());
		assertEquals("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA", reader.readLine());
		assertEquals("LABEL;HOME:Some text QUOTED-PRINTABLE more text=", reader.readLine());
		assertEquals("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA=0D=0A4th line", reader.readLine());
		assertEquals("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA", reader.readLine());
		assertEquals("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA", reader.readLine());
		
		assertEquals("NOTE:folded line", reader.readLine());
		assertEquals("NOTE:one two three four", reader.readLine());
		assertNull(reader.readLine());
	}
}
