package ezvcard.io;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

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
public class FoldedLineWriterTest {
	@Test
	public void write() throws Throwable {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw, 10, " ", "\r\n");

		writer.write("line\r\nThis line should be    ");
		writer.write("new line");
		writer.write("aa");
		writer.write("line");
		writer.write("\r\n0123456789\r\n");

		writer.write("0123456789", true);
		writer.write("\r\n");
		writer.write("01234567=", true);
		writer.write("\r\n");
		writer.write("01234567==", true);
		writer.write("\r\n");
		writer.write("short", true);
		writer.write("\r\n");
		writer.write("quoted-printable line", true);

		writer.close();
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"line\r\n" +
		"This line \r\n" +
		" should be    \r\n" +
		" new linea\r\n" +
		" aline\r\n" +
		"0123456789\r\n" +
		"012345678=\r\n" +
		" 9\r\n" +
		"01234567=3D\r\n" +
		"01234567=3D=\r\n" +
		" =3D\r\n" +
		"short\r\n" +
		"quoted-pr=\r\n" +
		" intable =\r\n" +
		" line";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void write_sub_array() throws Throwable {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw, 10, " ", "\r\n");

		String str = "This line should be folded.";
		writer.write(str, 5, 14);

		writer.close();
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"line shoul\r\n" +
		" d be";
		//@formatter:on

		assertEquals(expected, actual);
	}
}
