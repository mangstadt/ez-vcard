package ezvcard.io.text;

import static ezvcard.util.StringUtils.NEWLINE;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

import ezvcard.VCardVersion;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.util.StringUtils;

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
 * Parses the components out of each line in a plain-text vCard data stream.
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
 */
public class VCardRawReader implements Closeable {
	private final Reader reader;
	private final Buffer buffer = new Buffer();
	private final Buffer unfoldedLine = new Buffer();

	private boolean eos = false;
	private boolean caretDecodingEnabled = true;
	private VCardVersion version = VCardVersion.V2_1; //initialize to 2.1, since the VERSION property can exist anywhere in the file in this version
	private int prevChar = -1;
	private int propertyLineNum = 1;
	private int lineNum = 1;

	/**
	 * @param reader the reader to read from
	 */
	public VCardRawReader(Reader reader) {
		this.reader = reader;
	}

	/**
	 * Gets the line number of the line that was just read. If the line was
	 * folded, this will be the line number of the first line.
	 * @return the line number
	 */
	public int getLineNumber() {
		return propertyLineNum;
	}

	/**
	 * Gets the vCard version that the reader is currently parsing with.
	 * @return the vCard version
	 */
	public VCardVersion getVersion() {
		return version;
	}

	/**
	 * Reads the next line from the input stream. Folded lines are automatically
	 * unfolded.
	 * @return the parsed components of the line
	 * @throws VCardParseException if a line cannot be parsed
	 * @throws IOException if there's a problem reading from the stream
	 */
	public VCardRawLine readLine() throws IOException {
		if (eos) {
			return null;
		}

		propertyLineNum = lineNum;
		buffer.clear();
		unfoldedLine.clear();

		/*
		 * The property's group.
		 */
		String group = null;

		/*
		 * The property's name.
		 */
		String propertyName = null;

		/*
		 * The name of the parameter we're currently inside of.
		 */
		String curParamName = null;

		/*
		 * The property's parameters.
		 */
		VCardParameters parameters = new VCardParameters();

		/*
		 * The character that used to escape the current character (for
		 * parameter values).
		 */
		char escapeChar = 0;

		/*
		 * Are we currently inside a parameter value that is surrounded with
		 * double-quotes?
		 */
		boolean inQuotes = false;

		/*
		 * Are we currently inside the property value?
		 */
		boolean inValue = false;

		/*
		 * Does the line use quoted-printable encoding, and does it end all of
		 * its folded lines with a "=" character?
		 */
		boolean quotedPrintableLine = false;

		/*
		 * The current character.
		 */
		char ch = 0;

		/*
		 * The previous character.
		 */
		char prevChar;

		while (true) {
			prevChar = ch;

			int read = nextChar();
			if (read < 0) {
				eos = true;
				break;
			}

			ch = (char) read;

			if (prevChar == '\r' && ch == '\n') {
				/*
				 * The newline was already processed when the "\r" character was
				 * encountered, so ignore the accompanying "\n" character.
				 */
				continue;
			}

			if (isNewline(ch)) {
				quotedPrintableLine = (inValue && prevChar == '=' && isQuotedPrintable(parameters));
				if (quotedPrintableLine) {
					/*
					 * Remove the "=" character that some vCards put at the end
					 * of quoted-printable lines that are followed by a folded
					 * line.
					 */
					buffer.chop();
					unfoldedLine.chop();
				}

				//keep track of the current line number
				lineNum++;

				continue;
			}

			if (isNewline(prevChar)) {
				if (isWhitespace(ch)) {
					/*
					 * This line is a continuation of the previous line (the
					 * line is folded).
					 */
					continue;
				}

				if (quotedPrintableLine) {
					/*
					 * The property's parameters indicate that the property
					 * value is quoted-printable. And the previous line ended
					 * with an equals sign. This means that folding whitespace
					 * may not be prepended to folded lines like it should...
					 */
				} else {
					/*
					 * We're reached the end of the property.
					 */
					this.prevChar = ch;
					break;
				}
			}

			unfoldedLine.append(ch);

			if (inValue) {
				buffer.append(ch);
				continue;
			}

			if (escapeChar != 0) {
				//this character was escaped
				if (escapeChar == '\\') {
					if (ch == '\\') {
						buffer.append(ch);
					} else if (ch == 'n' || ch == 'N') {
						//newlines appear as "\n" or "\N" (see RFC 2426 p.7)
						buffer.append(NEWLINE);
					} else if (ch == '"' && version != VCardVersion.V2_1) {
						//double quotes don't need to be escaped in 2.1 parameter values because they have no special meaning
						buffer.append(ch);
					} else if (ch == ';' && version == VCardVersion.V2_1) {
						//semi-colons can only be escaped in 2.1 parameter values (see section 2 of specs)
						//if a 3.0/4.0 param value has semi-colons, the value should be surrounded in double quotes
						buffer.append(ch);
					} else {
						//treat the escape character as a normal character because it's not a valid escape sequence
						buffer.append(escapeChar).append(ch);
					}
				} else if (escapeChar == '^') {
					if (ch == '^') {
						buffer.append(ch);
					} else if (ch == 'n') {
						buffer.append(NEWLINE);
					} else if (ch == '\'') {
						buffer.append('"');
					} else {
						//treat the escape character as a normal character because it's not a valid escape sequence
						buffer.append(escapeChar).append(ch);
					}
				}
				escapeChar = 0;
				continue;
			}

			if (ch == '\\' || (ch == '^' && version != VCardVersion.V2_1 && caretDecodingEnabled)) {
				//an escape character was read
				escapeChar = ch;
				continue;
			}

			if (ch == '.' && group == null && propertyName == null) {
				//set the group
				group = buffer.getAndClear();
				continue;
			}

			if ((ch == ';' || ch == ':') && !inQuotes) {
				if (propertyName == null) {
					//property name
					propertyName = buffer.getAndClear();
				} else {
					//parameter value
					String paramValue = buffer.getAndClear();
					if (version == VCardVersion.V2_1) {
						//2.1 allows whitespace to surround the "=", so remove it
						paramValue = StringUtils.ltrim(paramValue);
					}
					parameters.put(curParamName, paramValue);
					curParamName = null;
				}

				if (ch == ':') {
					//the rest of the line is the property value
					inValue = true;
				}
				continue;
			}

			if (ch == ',' && !inQuotes && version != VCardVersion.V2_1) {
				//multi-valued parameter
				parameters.put(curParamName, buffer.getAndClear());
				continue;
			}

			if (ch == '=' && curParamName == null) {
				//parameter name
				String paramName = buffer.getAndClear();
				if (version == VCardVersion.V2_1) {
					//2.1 allows whitespace to surround the "=", so remove it
					paramName = StringUtils.rtrim(paramName);
				}
				curParamName = paramName;
				continue;
			}

			if (ch == '"' && version != VCardVersion.V2_1) {
				//2.1 doesn't use the quoting mechanism
				inQuotes = !inQuotes;
				continue;
			}

			buffer.append(ch);
		}

		if (unfoldedLine.length() == 0) {
			//input stream was empty
			return null;
		}

		if (propertyName == null) {
			throw new VCardParseException(unfoldedLine.get(), propertyLineNum);
		}

		String value = buffer.getAndClear();

		if ("VERSION".equalsIgnoreCase(propertyName)) {
			VCardVersion version = VCardVersion.valueOfByStr(value);
			if (version == null) {
				throw new InvalidVersionException(value, unfoldedLine.get(), propertyLineNum);
			}
			this.version = version;
		}

		return new VCardRawLine(group, propertyName, parameters, value);
	}

	/**
	 * <p>
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <tr>
	 * <th>Raw Character</th>
	 * <th>Encoded Character</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
	 * <td>{@code ^^}</td>
	 * </tr>
	 * </table>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPitt
	 *  sburgh, PA 15212":40.446816;80.00566
	 * </pre>
	 * 
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public boolean isCaretDecodingEnabled() {
		return caretDecodingEnabled;
	}

	/**
	 * <p>
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <tr>
	 * <th>Raw Character</th>
	 * <th>Encoded Character</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
	 * <td>{@code ^^}</td>
	 * </tr>
	 * </table>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPitt
	 *  sburgh, PA 15212":geo:40.446816,-80.00566
	 * </pre>
	 * 
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		caretDecodingEnabled = enable;
	}

	/**
	 * Gets the character encoding of the reader.
	 * @return the character encoding or null if none is defined
	 */
	public Charset getEncoding() {
		if (reader instanceof InputStreamReader) {
			InputStreamReader isr = (InputStreamReader) reader;
			String charsetStr = isr.getEncoding();
			return (charsetStr == null) ? null : Charset.forName(charsetStr);
		}

		return null;
	}

	private int nextChar() throws IOException {
		if (prevChar >= 0) {
			/*
			 * Use the character that was left over from the previous invocation
			 * of "readLine()".
			 */
			int ch = prevChar;
			prevChar = -1;
			return ch;
		}

		return reader.read();
	}

	private boolean isNewline(char ch) {
		return ch == '\n' || ch == '\r';
	}

	private boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\t';
	}

	/**
	 * Determines if the property value is quoted-printable. This must be
	 * checked in order to account for the fact that some vCards fold
	 * quoted-printed lines in a non-standard way.
	 * @param parameters the property's parameters
	 * @return true if the property is quoted-printable, false if not
	 */
	private boolean isQuotedPrintable(VCardParameters parameters) {
		if (parameters.getEncoding() == Encoding.QUOTED_PRINTABLE) {
			return true;
		}

		List<String> namelessValues = parameters.get(null);
		for (String value : namelessValues) {
			if ("QUOTED-PRINTABLE".equalsIgnoreCase(value)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Wraps a {@link StringBuilder} object, providing utility methods.
	 */
	private static class Buffer {
		private final StringBuilder sb = new StringBuilder();

		/**
		 * Clears the buffer.
		 * @return this
		 */
		public Buffer clear() {
			sb.setLength(0);
			return this;
		}

		/**
		 * Gets the buffer's contents.
		 * @return the buffer's contents
		 */
		public String get() {
			return sb.toString();
		}

		/**
		 * Gets the buffer's contents, then clears it.
		 * @return the buffer's contents
		 */
		public String getAndClear() {
			String string = get();
			clear();
			return string;
		}

		/**
		 * Appends a character to the buffer.
		 * @param ch the character to append
		 * @return this
		 */
		public Buffer append(char ch) {
			sb.append(ch);
			return this;
		}

		/**
		 * Appends a character sequence to the buffer.
		 * @param string the character sequence to append
		 * @return this
		 */
		public Buffer append(CharSequence string) {
			sb.append(string);
			return this;
		}

		/**
		 * Removes the last character from the buffer.
		 * @return this
		 */
		public Buffer chop() {
			sb.setLength(sb.length() - 1);
			return this;
		}

		/**
		 * Gets the length of the buffer.
		 * @return the buffer's length
		 */
		public int length() {
			return sb.length();
		}
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
