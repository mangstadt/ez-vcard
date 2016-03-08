package ezvcard.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
public class IOUtilsTest {
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void utf8Writer() throws Throwable {
		File file = temp.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write("�");
		writer.close();

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		assertEquals("�", reader.readLine());
		reader.close();
	}

	@Test
	public void utf8Reader() throws Throwable {
		File file = temp.newFile();
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
		writer.write("�");
		writer.close();

		BufferedReader reader = new BufferedReader(IOUtils.utf8Reader(file));
		assertEquals("�", reader.readLine());
		reader.close();
	}

	@Test
	public void utf8ReaderWriter() throws Throwable {
		File file = temp.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write("�");
		writer.close();

		BufferedReader reader = new BufferedReader(IOUtils.utf8Reader(file));
		assertEquals("�", reader.readLine());
		reader.close();
	}

	@Test
	public void closeQuietly() throws Exception {
		IOUtils.closeQuietly(null);

		Closeable closeable = mock(Closeable.class);
		IOUtils.closeQuietly(closeable);
		verify(closeable).close();

		closeable = mock(Closeable.class);
		doThrow(new IOException()).when(closeable).close();
		IOUtils.closeQuietly(closeable);
		verify(closeable).close();
	}

	@Test
	public void toByteArray() throws Exception {
		byte[] data = { 'a', 'b', 'c' };

		InputStream in = Mockito.spy(new ByteArrayInputStream(data));
		assertArrayEquals(data, IOUtils.toByteArray(in));
		verify(in, never()).close();

		in = Mockito.spy(new ByteArrayInputStream(data));
		assertArrayEquals(data, IOUtils.toByteArray(in, false));
		verify(in, never()).close();

		in = Mockito.spy(new ByteArrayInputStream(data));
		assertArrayEquals(data, IOUtils.toByteArray(in, true));
		verify(in).close();
	}

	@Test
	public void getFileContents() throws Exception {
		String contents = "abc";
		File file = temp.newFile();
		Writer writer = new OutputStreamWriter(new FileOutputStream(file));
		writer.write(contents);
		writer.close();

		assertEquals(contents, IOUtils.getFileContents(file));
		assertTrue(file.delete()); //make sure the input stream is closed
	}
}
