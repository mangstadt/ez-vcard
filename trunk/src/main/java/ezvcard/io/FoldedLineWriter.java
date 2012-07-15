package ezvcard.io;

import java.io.IOException;
import java.io.Writer;

/*
Copyright (c) 2012, Michael Angstadt
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
	private int curLineLen = 0;
	private int maxLen;
	private String indent;
	private String newline;
	private final Writer writer;

	/**
	 * @param _writer the write object to wrap
	 * @param _maxLen the max line length (excluding newline character(s))
	 * @param _indent the indent string to use (e.g. a single space character)
	 * @param _newline the newline sequence to use (e.g. "\r\n")
	 */
	public FoldedLineWriter(Writer _writer, int _maxLen, String _indent, String _newline) {
		if (_maxLen <= 0) {
			throw new IllegalArgumentException("Max line length must be greater than 0.");
		}
		if (_indent.length() >= _maxLen) {
			throw new IllegalArgumentException("The length of the indent string must be less than the max line length.");
		}
		writer = _writer;
		maxLen = _maxLen;
		indent = _indent;
		newline = _newline;
	}

	public void writeln(String str) throws IOException {
		write(str);
		write(newline);
	}

	@Override
	public void write(char buf[], int start, int end) throws IOException {
		write(buf, start, end, maxLen, indent);
	}

	public void write(char buf[], int start, int end, int _maxLen, String _indent) throws IOException {
		for (int i = start; i < end; i++) {
			char c = buf[i];
			if (c == '\n') {
				writer.write(buf, start, i - start + 1);
				curLineLen = 0;
				start = i + 1;
			} else if (c == '\r') {
				if (i == end - 1 || buf[i + 1] != '\n') {
					writer.write(buf, start, i - start + 1);
					curLineLen = 0;
					start = i + 1;
				} else {
					curLineLen++;
				}
			} else if (curLineLen >= _maxLen) {
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
				String s = newline + _indent;
				writer.write(s.toCharArray(), 0, s.length());
				start = i;
				curLineLen = _indent.length() + 1;
			} else {
				curLineLen++;
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

	public int getMaxLen() {
		return maxLen;
	}

	public void setMaxLen(int maxLen) {
		this.maxLen = maxLen;
	}

	public String getIndent() {
		return indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	public String getNewline() {
		return newline;
	}

	public void setNewline(String newline) {
		this.newline = newline;
	}
}
