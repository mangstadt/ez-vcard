package ezvcard.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.
*/

/**
 * Creates {@link Reader} and {@link Writer} objects that use UTF-8 encoding.
 * @author Michael Angstadt
 */
public final class Utf8Streams {
	/**
	 * Creates a reader that uses UTF-8 encoding.
	 * @param in the input stream to read from
	 * @return the reader
	 */
	public static Reader reader(InputStream in) {
		return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
	}

	/**
	 * Creates a writer that uses UTF-8 encoding.
	 * @param out the output stream to write to
	 * @return the writer
	 */
	public static Writer writer(OutputStream out) {
		return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
	}

	/**
	 * Creates a file reader that uses UTF-8 encoding.
	 * @param file the file to read from
	 * @return the reader
	 * @throws IOException if there's a problem opening the file
	 */
	public static Reader reader(Path file) throws IOException {
		return Files.newBufferedReader(file);
	}

	/**
	 * Creates a file writer that uses UTF-8 encoding.
	 * @param file the file to write to
	 * @return the writer
	 * @throws IOException if there's a problem opening the file
	 */
	public static Writer writer(Path file) throws IOException {
		return writer(file, false);
	}

	/**
	 * Creates a file writer that uses UTF-8 encoding.
	 * @param file the file to write to
	 * @param append true to append to the file if it exists, false to overwrite
	 * it
	 * @return the writer
	 * @throws IOException if there's a problem opening the file
	 */
	public static Writer writer(Path file, boolean append) throws IOException {
		StandardOpenOption option = append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
		return Files.newBufferedWriter(file, option);
	}

	private Utf8Streams() {
		//hide
	}
}
