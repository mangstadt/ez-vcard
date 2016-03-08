package ezvcard.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * I/O helper classes.
 * @author Michael Angstadt
 */
public final class IOUtils {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * Reads all the bytes from an input stream.
	 * @param in the input stream
	 * @return the bytes
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public static byte[] toByteArray(InputStream in) throws IOException {
		return toByteArray(in, false);
	}

	/**
	 * Reads all the bytes from an input stream.
	 * @param in the input stream
	 * @param close true to close the input stream when done, false not to
	 * @return the bytes
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public static byte[] toByteArray(InputStream in, boolean close) throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toByteArray();
		} finally {
			if (close) {
				closeQuietly(in);
			}
		}
	}

	/**
	 * Reads the contents of a {@link Reader} into a String.
	 * @param reader the reader
	 * @return the string
	 * @throws IOException if there was a problem reading from the reader
	 */
	public static String toString(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		char buffer[] = new char[4096];
		int read;
		while ((read = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, read);
		}
		return sb.toString();
	}

	/**
	 * Reads the contents of a text file.
	 * @param file the file to read
	 * @return the file contents
	 * @throws IOException if there's a problem reading the file
	 */
	public static String getFileContents(File file) throws IOException {
		return getFileContents(file, Charset.defaultCharset().name());
	}

	/**
	 * Reads the contents of a text file.
	 * @param file the file to read
	 * @param charset the character encoding of the file
	 * @return the file contents
	 * @throws IOException if there's a problem reading the file
	 */
	public static String getFileContents(File file, String charset) throws IOException {
		byte[] bytes = toByteArray(new FileInputStream(file), true);
		return new String(bytes, charset);
	}

	/**
	 * Closes a closeable resource, catching its {@link IOException}.
	 * @param closeable the resource to close (can be null)
	 */
	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * Creates a writer whose character encoding is set to "UTF-8".
	 * @param out the output stream to write to
	 * @return the writer
	 */
	public static Writer utf8Writer(OutputStream out) {
		return new OutputStreamWriter(out, UTF8);
	}

	/**
	 * Creates a writer whose character encoding is set to "UTF-8".
	 * @param file the file to write to
	 * @return the writer
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public static Writer utf8Writer(File file) throws FileNotFoundException {
		return utf8Writer(file, false);
	}

	/**
	 * Creates a writer whose character encoding is set to "UTF-8".
	 * @param file the file to write to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @return the writer
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public static Writer utf8Writer(File file, boolean append) throws FileNotFoundException {
		return utf8Writer(new FileOutputStream(file, append));
	}

	/**
	 * Creates a reader whose character encoding is set to "UTF-8".
	 * @param in the input stream to read from
	 * @return the reader
	 */
	public static Reader utf8Reader(InputStream in) {
		return new InputStreamReader(in, UTF8);
	}

	/**
	 * Creates a reader whose character encoding is set to "UTF-8".
	 * @param file the file to read from
	 * @return the reader
	 * @throws FileNotFoundException if the file can't be read
	 */
	public static Reader utf8Reader(File file) throws FileNotFoundException {
		return utf8Reader(new FileInputStream(file));
	}

	private IOUtils() {
		//hide
	}
}
