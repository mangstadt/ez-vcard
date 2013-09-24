package ezvcard.io;

import java.io.IOException;
import java.io.Writer;

import ezvcard.util.org.apache.commons.codec.EncoderException;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
 * Automatically folds lines as they are written.
 * @author Michael Angstadt
 */
public class FoldedLineWriter extends Writer {
	private final Writer writer;
	private int curLineLength = 0;
	private int lineLength;
	private String indent;
	private String newline;

	/**
	 * Creates a folded line writer.
	 * @param writer the writer object to wrap
	 * @param lineLength the maximum length a line can be before it is folded
	 * (excluding the newline), or a value of less than 1 to signal that no
	 * folding should take place
	 * @param indent the string to prepend to each folded line (e.g. a single
	 * space character)
	 * @param newline the newline sequence to use (e.g. "\r\n")
	 * @throws IllegalArgumentException if the line length is less than or equal
	 * to zero
	 * @throws IllegalArgumentException if the length of the indent string is
	 * greater than the max line length
	 */
	public FoldedLineWriter(Writer writer, int lineLength, String indent, String newline) {
		this.writer = writer;
		setLineLength(lineLength);
		setIndent(indent);
		this.newline = newline;
	}

	/**
	 * Writes a string of text, followed by a newline.
	 * @param str the text to write
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void writeln(String str) throws IOException {
		write(str);
		write(newline);
	}

	/**
	 * Writes a portion of an array of characters.
	 * @param str the string to write
	 * @param quotedPrintable true if the string has been encoded in
	 * quoted-printable encoding, false if not
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public FoldedLineWriter append(CharSequence str, boolean quotedPrintable) throws IOException {
		write(str.toString().toCharArray(), 0, str.length(), quotedPrintable);
		return this;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		write(cbuf, off, len, false);
	}

	/**
	 * Writes a portion of an array of characters.
	 * @param buf the array of characters
	 * @param off the offset from which to start writing characters
	 * @param len the number of characters to write
	 * @param quotedPrintable true to convert the string to "quoted-printable"
	 * encoding, false not to
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(char[] cbuf, int off, int len, boolean quotedPrintable) throws IOException {
		int effectiveLineLength = lineLength;

		//encode into quoted-printable
		if (quotedPrintable) {
			QuotedPrintableCodec codec = new QuotedPrintableCodec();
			try {
				cbuf = codec.encode(new String(cbuf, off, len)).toCharArray();
				off = 0;
				len = cbuf.length;
			} catch (EncoderException e) {
				//only thrown if an unsupported charset is passed into the codec
			}

			effectiveLineLength -= 1; //"=" must be appended onto each line
		}

		if (lineLength <= 0) {
			//line folding is disabled
			writer.write(cbuf, off, len);
			return;
		}

		//TODO "len" is being treated as the index in the char array to stop at!
		int encodedCharPos = -1;
		for (int i = off; i < len; i++) {
			char c = cbuf[i];

			//keep track of the quoted-printable characters to prevent them from being cut in two at a folding boundary
			if (encodedCharPos >= 0) {
				encodedCharPos++;
				if (encodedCharPos == 3) {
					encodedCharPos = -1;
				}
			}

			if (c == '\n') {
				writer.write(cbuf, off, i - off + 1);
				curLineLength = 0;
				off = i + 1;
				continue;
			}

			if (c == '\r') {
				if (i == len - 1 || cbuf[i + 1] != '\n') {
					writer.write(cbuf, off, i - off + 1);
					curLineLength = 0;
					off = i + 1;
				} else {
					curLineLength++;
				}
				continue;
			}

			if (c == '=' && quotedPrintable) {
				encodedCharPos = 0;
			}

			if (curLineLength >= effectiveLineLength) {
				//if the last characters on the line are whitespace, then exceed the max line length in order to include the whitespace on the same line
				//otherwise it will be lost because it will merge with the padding on the next line
				if (Character.isWhitespace(c)) {
					while (Character.isWhitespace(c) && i < len - 1) {
						i++;
						c = cbuf[i];
					}
					if (i >= len - 1) {
						//the rest of the char array is whitespace, so leave the loop
						break;
					}
				}

				//if we are in the middle of a quoted-printable encoded char, then exceed the max line length in order to print out the rest of the char
				if (encodedCharPos > 0) {
					i += 3 - encodedCharPos;
					if (i >= len - 1) {
						//the rest of the char array was an encoded char, so leave the loop
						break;
					}
				}

				writer.write(cbuf, off, i - off);
				if (quotedPrintable) {
					writer.write('=');
				}
				writer.write(newline);
				writer.write(indent);
				curLineLength = indent.length() + 1;
				off = i;

				continue;
			}

			curLineLength++;
		}

		writer.write(cbuf, off, len - off);
	}

	/**
	 * Closes the writer.
	 */
	@Override
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Flushes the writer.
	 */
	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Gets the maximum length a line can be before it is folded (excluding the
	 * newline).
	 * @return the line length
	 */
	public int getLineLength() {
		return lineLength;
	}

	/**
	 * Sets the maximum length a line can be before it is folded (excluding the
	 * newline).
	 * @param lineLength the line length or a value &lt;= 0 to disable folding
	 */
	public void setLineLength(int lineLength) {
		this.lineLength = lineLength;
	}

	/**
	 * Gets the string that is prepended to each folded line.
	 * @return the indent string
	 */
	public String getIndent() {
		return indent;
	}

	/**
	 * Sets the string that is prepended to each folded line.
	 * @param indent the indent string (e.g. a single space character)
	 * @throws IllegalArgumentException if the length of the indent string is
	 * greater than the max line length
	 */
	public void setIndent(String indent) {
		if (indent != null && lineLength > 0 && indent.length() >= lineLength) {
			throw new IllegalArgumentException("The length of the indent string must be less than the max line length.");
		}
		this.indent = indent;
	}

	/**
	 * Gets the newline sequence that is used to separate lines.
	 * @return the newline sequence
	 */
	public String getNewline() {
		return newline;
	}

	/**
	 * Sets the newline sequence that is used to separate lines
	 * @param newline the newline sequence
	 */
	public void setNewline(String newline) {
		this.newline = newline;
	}

	/**
	 * Gets the wrapped {@link Writer} object.
	 * @return the wrapped writer
	 */
	public Writer getWriter() {
		return writer;
	}
}
