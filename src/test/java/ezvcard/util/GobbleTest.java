package ezvcard.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

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
public class GobbleTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void file() throws Exception {
		String data = "one two three";

		File file = folder.newFile();
		Writer writer = new FileWriter(file);
		writer.write(data);
		writer.close();

		Gobble stream = new Gobble(file);
		assertEquals(data, stream.asString());
		assertArrayEquals(data.getBytes(), stream.asByteArray());
	}

	@Test
	public void inputStream() throws Exception {
		String data = "one two three";

		InputStream in = new ByteArrayInputStream(data.getBytes());
		Gobble stream = new Gobble(in);
		assertEquals(data, stream.asString());
		assertArrayEquals(new byte[0], stream.asByteArray()); //input stream was consumed

		in = new ByteArrayInputStream(data.getBytes());
		stream = new Gobble(in);
		assertArrayEquals(data.getBytes(), stream.asByteArray());
	}

	@Test
	public void reader() throws Exception {
		String data = "one two three";

		Reader reader = new StringReader(data);
		Gobble stream = new Gobble(reader);
		assertEquals(data, stream.asString());
		try {
			stream.asByteArray();
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
	}
}
