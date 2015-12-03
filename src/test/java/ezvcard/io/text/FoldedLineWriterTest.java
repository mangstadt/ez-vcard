package ezvcard.io.text;

import static ezvcard.util.TestUtils.assertIntEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.junit.Test;

import ezvcard.util.StringUtils;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
public class FoldedLineWriterTest {
	@Test
	public void write() throws Throwable {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);

		writer.write("line\r\nThis line should be    ");
		writer.write("new line");
		writer.write("aa");
		writer.write("line");
		writer.write("\r\n0123456789\r\n");

		writer.write("0123456789", true, null);
		writer.write("\r\n");
		writer.write("01234567=", true, null);
		writer.write("\r\n");
		writer.write("01234567==", true, null);
		writer.write("\r\n");
		writer.write("short", true, null);
		writer.write("\r\n");
		writer.write("quoted-printable line", true, null);

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
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);

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

	@Test
	public void write_charset() throws Throwable {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);

		String str = "test\n\u00e4\u00f6\u00fc\u00df\ntest";
		writer.write(str, true, Charset.forName("ISO-8859-1"));
		writer.close();
		String actual = sw.toString();

		//@formatter:off
		String expected =	
		"test=0A=E4=\r\n" +
		" =F6=FC=DF=\r\n" +
		" =0Atest";
		//@formatter:on

		assertEquals(expected, actual);

		QuotedPrintableCodec codec = new QuotedPrintableCodec("ISO-8859-1");
		assertEquals("test\n\u00e4\u00f6\u00fc\u00df\ntest", codec.decode("test=0A=E4=F6=FC=DF=0Atest"));
	}

	@Test
	public void write_surrogate_pair() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(5);

		String str = "test\uD83D\uDCF0test"; // should not be split
		writer.write(str, false, Charset.forName("UTF-8"));
		writer.close();
		String actual = sw.toString();

		assertEquals("test\uD83D\uDCF0\r\n test", actual);
	}

	@Test
	public void write_different_newlines() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(8);
		writer.write("one\r\ntwo three\rthree\nfour five");
		writer.close();
		String actual = sw.toString();

		//@formatter:off
		String expected =	
		"one\r\n" +
		"two thre\r\n" + //folded lines always use the newline sequence defined with writer.setNewline()
		" e\r" +
		"three\n" +
		"four fiv\r\n" +
		" e";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void getLineLength_default() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		assertIntEquals(75, writer.getLineLength());
		writer.close();
	}

	@Test
	public void getIndent_default() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		assertEquals(" ", writer.getIndent());
		writer.close();
	}

	@Test
	public void setLineLength() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(1);
		assertIntEquals(1, writer.getLineLength());
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setLineLength_zero() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(0);
		writer.close();
	}

	@Test
	public void setLineLength_null() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(null); //valid value
		writer.close();
	}

	@Test
	public void getEncoding() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		assertNull(writer.getEncoding());
		writer.close();

		Charset charset = Charset.availableCharsets().values().iterator().next();
		OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream(), charset);
		writer = new FoldedLineWriter(osw);
		assertEquals(charset, writer.getEncoding());
		writer.close();
	}

	@Test
	public void setIndent() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);
		String indent = StringUtils.repeat(' ', 5);
		writer.setIndent(indent);
		assertEquals(indent, writer.getIndent());
		writer.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndent_invalid() throws Exception {
		StringWriter sw = new StringWriter();
		FoldedLineWriter writer = new FoldedLineWriter(sw);
		writer.setLineLength(10);
		writer.setIndent(StringUtils.repeat(' ', 10));
		writer.close();
	}
}
