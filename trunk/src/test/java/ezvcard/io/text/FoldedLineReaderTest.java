package ezvcard.io.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
@SuppressWarnings("resource")
public class FoldedLineReaderTest {
	@Test
	public void readLine() throws Exception {
		//@formatter:off
		String vcardStr =

		//unfolded line
		"FN: Michael Angstadt\n" +

		//empty lines should be ignored
		"\n" +

		//=========
		//quoted-printable lines whose additional lines are not folded (Outlook craziness)

		//one line
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5\n" +

		//two lines
		//(without "ENCODING" subtype name)
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n" +
		"New York, New York  12345\n" +

		//two lines with an empty line at the end
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n" +
		"New York, New York  12345=0D=0A=\n" +
		"\n" +

		//two lines with an empty line in the middle
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n" +
		"=\n" +
		"New York, New York  12345\n" +

		//three lines
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n" +
		"New York, New York  12345=0D=0A=\n" +
		"USA\n" +

		//it should recognize when the string "QUOTED-PRINTABLE" is not to the left of the colon
		"LABEL;HOME:Some text QUOTED-PRINTABLE more text=\n" +

		//four lines
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n" +
		"New York, New York  12345=0D=0A=\n" +
		"USA=0D=0A=\n" +
		"4th line\n" +

		//=========

		//a quoted-printable line whose additional lines *are* folded where the lines end in "="
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\n" +
		" New York, New York  12345=0D=0A=\n" +
		" USA\n" +

		//a quoted-printable line whose additional lines *are* folded, where the lines don't end in "="
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A\n" +
		" New York, New York  12345=0D=0A\n" +
		" USA\n" +

		//this line is folded
		"NOTE:folded \n line\n" +

		//this line is folded with multiple whitespace characters
		"NOTE:one \n two \n  three \n \t four";
		//@formatter:on

		FoldedLineReader reader = new FoldedLineReader(vcardStr);
		assertEquals("FN: Michael Angstadt", reader.readLine());
		//assertEquals("", reader.readLine()); //empty lines should be ignored

		//test the issues Outlook has with folding lines that are QUOTED-PRINTABLE
		assertEquals("LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5", reader.readLine());
		assertEquals("LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0ANew York, New York  12345", reader.readLine());
		assertEquals("LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0A", reader.readLine());
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

	@Test
	public void getLineNum() throws Exception {
		//@formatter:off
		String vcardStr =
		"NOTE:one\n" +
		" two\n" +
		"\n" +
		"NOTE:three\n" +
		" four\n";
		//@formatter:on

		FoldedLineReader reader = new FoldedLineReader(vcardStr);

		assertEquals(0, reader.getLineNum());

		reader.readLine();
		assertEquals(1, reader.getLineNum());

		reader.readLine();
		assertEquals(4, reader.getLineNum());

		assertNull(reader.readLine());
	}
}
