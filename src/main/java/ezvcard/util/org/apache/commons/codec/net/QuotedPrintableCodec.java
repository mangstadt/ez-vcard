/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

package ezvcard.util.org.apache.commons.codec.net;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import ezvcard.util.org.apache.commons.codec.DecoderException;
import ezvcard.util.org.apache.commons.codec.EncoderException;

/**
 * <p>
 * Encodes and decodes strings using quoted-printable encoding.
 * </p>
 * <p>
 * The majority of this class's source code was taken from the <a
 * href="http://commons.apache.org/proper/commons-codec/">Apache Commons
 * Codec</a> project (version 1.10). Defining this library as a project
 * dependency causes an issue with Android devices, which is why parts of its
 * source code have been directly incorporated into the ez-vcard code base.
 * </p>
 * @author Apache Software Foundation
 * @author Michael Angstadt
 * @see <a href="http://commons.apache.org/proper/commons-codec/">Apache Commons
 * Codec</a>
 */
public class QuotedPrintableCodec {
	private static final byte ESCAPE_CHAR = '=';
	private static final byte TAB = 9;
	private static final byte SPACE = 32;

	private static final BitSet PRINTABLE_CHARS = new BitSet(256);
	static {
		// alpha characters
		for (int i = 33; i <= 60; i++) {
			PRINTABLE_CHARS.set(i);
		}
		for (int i = 62; i <= 126; i++) {
			PRINTABLE_CHARS.set(i);
		}
		PRINTABLE_CHARS.set(TAB);
		PRINTABLE_CHARS.set(SPACE);
	}

	private final String charset;

	public QuotedPrintableCodec(String charset) {
		this.charset = charset;
	}

	/**
	 * Encodes a string into its quoted-printable form.
	 * @param string the string to convert to quoted-printable form
	 * @return the quoted-printable string
	 * @throws EncoderException if the charset is not supported by the JVM
	 */
	public String encode(String string) throws EncoderException {
		byte bytes[];
		try {
			bytes = string.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (byte c : bytes) {
			int b = c;
			if (b < 0) {
				b = 256 + b;
			}
			if (PRINTABLE_CHARS.get(b)) {
				buffer.write(b);
			} else {
				encodeQuotedPrintable(b, buffer);
			}
		}

		try {
			return new String(buffer.toByteArray(), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			//should never be thrown because all JVMs must support US-ASCII
			throw new EncoderException(e);
		}
	}

	/**
	 * Decodes a quoted-printable string into its original form.
	 * @param string the quoted-printable string
	 * @return the original string
	 * @throws DecoderException if the charset is not supported by the JVM or if
	 * there's a problem decoding the string
	 */
	public String decode(String string) throws DecoderException {
		byte bytes[];
		try {
			bytes = string.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			//should never be thrown because all JVMs must support US-ASCII
			throw new DecoderException(e);
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			if (b == ESCAPE_CHAR) {
				try {
					int u = digit16(bytes[++i]);
					int l = digit16(bytes[++i]);
					buffer.write((char) ((u << 4) + l));
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new DecoderException("Invalid quoted-printable encoding", e);
				}
			} else {
				buffer.write(b);
			}
		}

		try {
			return new String(buffer.toByteArray(), charset);
		} catch (UnsupportedEncodingException e) {
			throw new DecoderException(e);
		}
	}

	/**
	 * Returns the numeric value of the given character in radix 16.
	 * @param b the character to be converted.
	 * @return the numeric value represented by the character in radix 16.
	 * @throws DecoderException when the byte is not valid per
	 * {@link Character#digit(char,int)}
	 */
	private static int digit16(byte b) throws DecoderException {
		int i = Character.digit((char) b, 16);
		if (i == -1) {
			throw new DecoderException("Invalid URL encoding: not a valid digit (radix 16): " + b);
		}
		return i;

	}

	/**
	 * Encodes byte into its quoted-printable representation.
	 * @param b the byte to encode
	 * @param buffer the buffer to write to
	 */
	private static void encodeQuotedPrintable(int b, ByteArrayOutputStream buffer) {
		buffer.write(ESCAPE_CHAR);
		char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
		char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
		buffer.write(hex1);
		buffer.write(hex2);
	}
}
