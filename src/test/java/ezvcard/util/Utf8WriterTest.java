package ezvcard.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
public class Utf8WriterTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void outputStream() throws Exception {
		String data = "one two three";

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Utf8Writer writer = new Utf8Writer(out);
		writer.write(data);
		writer.close();

		String expected = data;
		String actual = new String(out.toByteArray(), "UTF-8");
		assertEquals(expected, actual);
	}

	@Test
	public void file() throws Exception {
		String data = "one two three";

		File file = folder.newFile();
		Utf8Writer writer = new Utf8Writer(file);
		writer.write(data);
		writer.close();

		String expected = data;
		String actual = new Gobble(file).asString("UTF-8");
		assertEquals(expected, actual);
	}

	@Test
	public void file_append() throws Exception {
		File file = folder.newFile();
		Utf8Writer writer = new Utf8Writer(file);
		writer.write("one");
		writer.close();

		writer = new Utf8Writer(file, false);
		writer.write("two");
		writer.close();

		writer = new Utf8Writer(file, true);
		writer.write(" three");
		writer.close();

		String expected = "two three";
		String actual = new Gobble(file).asString("UTF-8");
		assertEquals(expected, actual);
	}
}
