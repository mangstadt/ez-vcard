package ezvcard.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
 * I/O helper classes.
 * @author Michael Angstadt
 */
public class IOUtils {
	/**
	 * Gets the extension off a file's name.
	 * @param file the file
	 * @return its extension (e.g. "jpg") or null if it doesn't have one
	 */
	public static String getFileExtension(File file) {
		String fileName = file.getName();
		int dot = fileName.lastIndexOf('.');
		if (dot >= 0 && dot < fileName.length() - 1) {
			return fileName.substring(dot + 1);
		}
		return null;
	}

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

	private IOUtils() {
		//hide
	}
}
