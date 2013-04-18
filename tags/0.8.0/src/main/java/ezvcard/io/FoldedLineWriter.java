package ezvcard.io;

import java.io.IOException;
import java.io.Writer;

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
	private int curLineLength = 0;
	private int lineLength;
	private String indent;
	private String newline;
	private final Writer writer;

	/**
	 * @param writer the writer object to wrap
	 * @param lineLength the maximum length a line can be before it is folded
	 * (excluding the newline)
	 * @param indent the string to prepend to each folded line (e.g. a single
	 * space character)
	 * @param newline the newline sequence to use (e.g. "\r\n")
	 * @throws IllegalArgumentException if the line length is less than or equal
	 * to zero
	 * @throws IllegalArgumentException if the length of the indent string is
	 * greater than the max line length
	 */
	public FoldedLineWriter(Writer writer, int lineLength, String indent, String newline) {
		setLineLength(lineLength);
		setIndent(indent);
		this.writer = writer;
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

	@Override
	public void write(char buf[], int start, int end) throws IOException {
		write(buf, start, end, lineLength, indent);
	}

	/**
	 * Writes a portion of an array of characters.
	 * @param buf the array of characters
	 * @param start the offset from which to start writing characters
	 * @param end the number of characters to write
	 * @param lineLength the maximum length a line can be before it is folded
	 * (excluding the newline)
	 * @param indent the indent string to use (e.g. a single space character)
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(char buf[], int start, int end, int lineLength, String indent) throws IOException {
		for (int i = start; i < end; i++) {
			char c = buf[i];
			if (c == '\n') {
				writer.write(buf, start, i - start + 1);
				curLineLength = 0;
				start = i + 1;
			} else if (c == '\r') {
				if (i == end - 1 || buf[i + 1] != '\n') {
					writer.write(buf, start, i - start + 1);
					curLineLength = 0;
					start = i + 1;
				} else {
					curLineLength++;
				}
			} else if (curLineLength >= lineLength) {
				//if the last characters on the line are whitespace, then exceed the max line length in order to include the whitespace on the same line
				//otherwise it will be lost because it will merge with the padding on the next line
				if (Character.isWhitespace(c)) {
					while (Character.isWhitespace(c) && i < end - 1) {
						i++;
						c = buf[i];
					}
					if (i == end - 1) {
						//the rest of the char array is whitespace, so leave the loop
						break;
					}
				}

				writer.write(buf, start, i - start);
				String s = newline + indent;
				writer.write(s.toCharArray(), 0, s.length());
				start = i;
				curLineLength = indent.length() + 1;
			} else {
				curLineLength++;
			}
		}
		writer.write(buf, start, end - start);
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

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
	 * @param lineLength the line length
	 * @throws IllegalArgumentException if the line length is less than or equal
	 * to zero
	 */
	public void setLineLength(int lineLength) {
		if (lineLength <= 0) {
			throw new IllegalArgumentException("Line length must be greater than 0.");
		}
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
		if (indent.length() >= lineLength) {
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
}
