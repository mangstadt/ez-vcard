package ezvcard.io.text;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import ezvcard.util.org.apache.commons.codec.EncoderException;
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
 * Automatically folds lines as they are written.
 * @author Michael Angstadt
 */
public class FoldedLineWriter extends Writer {
	private final Writer writer;
	private int curLineLength = 0;
	private Integer lineLength = 75;
	private String indent = " ";
	private String newline = "\r\n";

	/**
	 * Creates a folded line writer.
	 * @param writer the writer object to wrap
	 */
	public FoldedLineWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Writes a string, followed by a newline.
	 * @param str the text to write
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void writeln(String str) throws IOException {
		write(str);
		write(newline);
	}

	/**
	 * Writes a string.
	 * @param str the string to write
	 * @param quotedPrintable true to encode the string in quoted-printable
	 * encoding, false not to
	 * @param charset the character set to use when encoding into
	 * quoted-printable, or null to use the writer's character encoding (only
	 * applicable if "quotedPrintable" is set to true)
	 * @return this
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public FoldedLineWriter append(CharSequence str, boolean quotedPrintable, Charset charset) throws IOException {
		write(str, quotedPrintable, charset);
		return this;
	}

	/**
	 * Writes a string.
	 * @param str the string to write
	 * @param quotedPrintable true to encode the string in quoted-printable
	 * encoding, false not to
	 * @param charset the character set to use when encoding into
	 * quoted-printable, or null to use the writer's character encoding (only
	 * applicable if "quotedPrintable" is set to true)
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(CharSequence str, boolean quotedPrintable, Charset charset) throws IOException {
		write(str.toString().toCharArray(), 0, str.length(), quotedPrintable, charset);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		write(cbuf, off, len, false, null);
	}

	/**
	 * Writes a portion of an array of characters.
	 * @param cbuf the array of characters
	 * @param off the offset from which to start writing characters
	 * @param len the number of characters to write
	 * @param quotedPrintable true to encode the string in quoted-printable
	 * encoding, false not to
	 * @param charset the character set to use when encoding into
	 * quoted-printable, or null to use the writer's character encoding (only
	 * applicable if "quotedPrintable" is set to true)
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(char[] cbuf, int off, int len, boolean quotedPrintable, Charset charset) throws IOException {
		if (quotedPrintable) {
			if (charset == null) {
				charset = Charset.forName("UTF-8");
			}

			QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
			try {
				String str = new String(cbuf, off, len);
				String encoded = codec.encode(str);

				cbuf = encoded.toCharArray();
				off = 0;
				len = cbuf.length;
			} catch (EncoderException e) {
				/*
				 * Thrown if an unsupported charset is passed into the codec.
				 * This should never be thrown though, because we already know
				 * the charset is valid (a Charset object is passed into the
				 * method).
				 */
				throw new RuntimeException(e);
			}
		}

		if (lineLength == null) {
			/*
			 * If line folding is disabled, then write directly to the Writer.
			 */
			writer.write(cbuf, off, len);
			return;
		}

		int effectiveLineLength = lineLength;
		if (quotedPrintable) {
			/*
			 * Account for the "=" character that must be appended onto each
			 * line.
			 */
			effectiveLineLength -= 1;
		}

		int encodedCharPos = -1;
		int start = off;
		int end = off + len;
		for (int i = start; i < end; i++) {
			char c = cbuf[i];

			/*
			 * Keep track of the quoted-printable characters to prevent them
			 * from being cut in two at a folding boundary.
			 */
			if (encodedCharPos >= 0) {
				encodedCharPos++;
				if (encodedCharPos == 3) {
					encodedCharPos = -1;
				}
			}

			if (c == '\n') {
				writer.write(cbuf, start, i - start + 1);
				curLineLength = 0;
				start = i + 1;
				continue;
			}

			if (c == '\r') {
				if (i == end - 1 || cbuf[i + 1] != '\n') {
					writer.write(cbuf, start, i - start + 1);
					curLineLength = 0;
					start = i + 1;
				} else {
					curLineLength++;
				}
				continue;
			}

			if (c == '=' && quotedPrintable) {
				encodedCharPos = 0;
			}

			if (curLineLength >= effectiveLineLength) {
				/*
				 * If the last characters on the line are whitespace, then
				 * exceed the max line length in order to include the whitespace
				 * on the same line. Otherwise, the whitespace will be lost
				 * because it will merge with the padding on the next, folded
				 * line.
				 */
				if (Character.isWhitespace(c)) {
					while (Character.isWhitespace(c) && i < end - 1) {
						i++;
						c = cbuf[i];
					}
					if (i >= end - 1) {
						/*
						 * The rest of the char array is whitespace, so leave
						 * the loop.
						 */
						break;
					}
				}

				/*
				 * If we are in the middle of a quoted-printable encoded
				 * character, then exceed the max line length so the sequence
				 * doesn't get split up across multiple lines.
				 */
				if (encodedCharPos > 0) {
					i += 3 - encodedCharPos;
					if (i >= end - 1) {
						/*
						 * The rest of the char array was a quoted-printable
						 * encoded char, so leave the loop.
						 */
						break;
					}
				}

				writer.write(cbuf, start, i - start);
				if (quotedPrintable) {
					writer.write('=');
				}
				writer.write(newline);
				writer.write(indent);
				curLineLength = indent.length() + 1;
				start = i;

				continue;
			}

			curLineLength++;
		}

		writer.write(cbuf, start, end - start);
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
	 * newline, defaults to 75).
	 * @return the line length or null if folding is disabled
	 */
	public Integer getLineLength() {
		return lineLength;
	}

	/**
	 * Sets the maximum length a line can be before it is folded (excluding the
	 * newline, defaults to 75).
	 * @param lineLength the line length or null to disable folding
	 * @throws IllegalArgumentException if the line length is less than or equal
	 * to zero
	 */
	public void setLineLength(Integer lineLength) {
		if (lineLength != null && lineLength <= 0) {
			throw new IllegalArgumentException("Line length must be greater than 0.");
		}
		this.lineLength = lineLength;
	}

	/**
	 * Gets the string that is prepended to each folded line (defaults to a
	 * single space character).
	 * @return the indent string
	 */
	public String getIndent() {
		return indent;
	}

	/**
	 * Sets the string that is prepended to each folded line (defaults to a
	 * single space character).
	 * @param indent the indent string
	 * @throws IllegalArgumentException if the length of the indent string is
	 * greater than the max line length
	 */
	public void setIndent(String indent) {
		if (lineLength != null && indent.length() >= lineLength) {
			throw new IllegalArgumentException("The length of the indent string must be less than the max line length.");
		}
		this.indent = indent;
	}

	/**
	 * Gets the newline sequence that is used to separate lines (defaults to
	 * CRLF).
	 * @return the newline sequence
	 */
	public String getNewline() {
		return newline;
	}

	/**
	 * Sets the newline sequence that is used to separate lines (defaults to
	 * CRLF).
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

	/**
	 * Gets the writer's character encoding.
	 * @return the writer's character encoding or null if undefined
	 */
	public Charset getEncoding() {
		if (!(writer instanceof OutputStreamWriter)) {
			return null;
		}

		OutputStreamWriter osw = (OutputStreamWriter) writer;
		String charsetStr = osw.getEncoding();
		return (charsetStr == null) ? null : Charset.forName(charsetStr);
	}
}
