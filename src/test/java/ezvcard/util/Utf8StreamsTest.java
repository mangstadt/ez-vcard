package ezvcard.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/*
Copyright (c) 2012-2026, Michael Angstadt
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
*/

/**
 * @author Michael Angstadt
 */
class Utf8StreamsTest {
	@Test
	void inputStream() throws Exception {
		String data = "one two three";
		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

		Reader reader = Utf8Streams.reader(new ByteArrayInputStream(bytes));

		String expected = data;
		String actual = new Gobble(reader).asString();
		assertEquals(expected, actual);
	}

	@Test
	void read_file(@TempDir Path tempDir) throws Exception {
		String data = "one two three";

		Path file = tempDir.resolve("test.txt");
		Writer writer = Utf8Streams.writer(file);
		writer.write(data);
		writer.close();

		Reader reader = Utf8Streams.reader(file);

		String expected = data;
		String actual = new Gobble(reader).asString();
		assertEquals(expected, actual);
	}

	@Test
	void outputStream() throws Exception {
		String data = "one two three";

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Writer writer = Utf8Streams.writer(out);
		writer.write(data);
		writer.close();

		String expected = data;
		String actual = new String(out.toByteArray(), StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void write_file(@TempDir Path tempDir) throws Exception {
		String data = "one two three";

		Path file = tempDir.resolve("test.txt");
		Writer writer = Utf8Streams.writer(file);
		writer.write(data);
		writer.close();

		String expected = data;
		String actual = new Gobble(file).asString(StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Test
	void write_file_append(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("test.txt");
		Writer writer = Utf8Streams.writer(file);
		writer.write("one");
		writer.close();

		writer = Utf8Streams.writer(file, false);
		writer.write("two");
		writer.close();

		writer = Utf8Streams.writer(file, true);
		writer.write(" three");
		writer.close();

		String expected = "two three";
		String actual = new Gobble(file).asString(StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}
}
