package ezvcard.io.text;

import static ezvcard.util.VCardStringUtils.NEWLINE;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.util.VCardStringUtils;

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
 * Parses a vCard data stream.
 * @author Michael Angstadt
 */
public class VCardRawReader implements Closeable {
	private final FoldedLineReader reader;
	private boolean caretDecodingEnabled = true;
	private boolean eof = false;
	private VCardVersion version = VCardVersion.V2_1;

	/**
	 * Creates a new reader.
	 * @param reader the reader to the data stream
	 */
	public VCardRawReader(Reader reader) {
		this.reader = new FoldedLineReader(reader);
	}

	/**
	 * Gets the line number of the last line that was read.
	 * @return the line number
	 */
	public int getLineNum() {
		return reader.getLineNum();
	}

	/**
	 * Starts or continues reading from the vCard data stream.
	 * @param listener handles the vCard data as it is read off the wire
	 * @throws IOException if there is an I/O problem
	 */
	public void start(VCardDataStreamListener listener) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				parseLine(line, listener);
			} catch (StopReadingException e) {
				return;
			}
		}
		eof = true;
	}

	private void parseLine(String line, VCardDataStreamListener listener) {
		String group = null;
		String propertyName = null;
		VCardSubTypes parameters = new VCardSubTypes();
		String value = null;

		char escapeChar = 0; //is the next char escaped?
		boolean inQuotes = false; //are we inside of double quotes?
		StringBuilder buffer = new StringBuilder();
		String curParamName = null;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (escapeChar != 0) {
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
			} else if (ch == '\\' || (ch == '^' && version != VCardVersion.V2_1 && caretDecodingEnabled)) {
				escapeChar = ch;
			} else if (ch == '.' && group == null && propertyName == null) {
				group = buffer.toString();
				buffer.setLength(0);
			} else if ((ch == ';' || ch == ':') && !inQuotes) {
				if (propertyName == null) {
					propertyName = buffer.toString();
				} else {
					//parameter value
					String paramValue = buffer.toString();
					if (version == VCardVersion.V2_1) {
						//2.1 allows whitespace to surround the "=", so remove it
						paramValue = VCardStringUtils.ltrim(paramValue);
					}
					parameters.put(curParamName, paramValue);
					curParamName = null;
				}
				buffer.setLength(0);

				if (ch == ':') {
					if (i < line.length() - 1) {
						value = line.substring(i + 1);
					} else {
						value = "";
					}
					break;
				}
			} else if (ch == ',' && !inQuotes && version != VCardVersion.V2_1) {
				//multi-valued parameter
				parameters.put(curParamName, buffer.toString());
				buffer.setLength(0);
			} else if (ch == '=' && curParamName == null) {
				//parameter name
				String paramName = buffer.toString();
				if (version == VCardVersion.V2_1) {
					//2.1 allows whitespace to surround the "=", so remove it
					paramName = VCardStringUtils.rtrim(paramName);
				}
				curParamName = paramName;
				buffer.setLength(0);
			} else if (ch == '"' && version != VCardVersion.V2_1) {
				//2.1 doesn't use the quoting mechanism
				inQuotes = !inQuotes;
			} else {
				buffer.append(ch);
			}
		}

		if (propertyName == null || value == null) {
			listener.invalidLine(line);
			return;
		}
		if ("VERSION".equalsIgnoreCase(propertyName)) {
			VCardVersion version = VCardVersion.valueOfByStr(value.trim());
			if (version == null) {
				listener.invalidVersion(value);
			} else {
				this.version = version;
				listener.readVersion(version);
			}
			return;
		}
		if ("BEGIN".equalsIgnoreCase(propertyName)) {
			listener.beginComponent(value.trim());
			return;
		}
		if ("END".equalsIgnoreCase(propertyName)) {
			listener.endComponent(value.trim());
			return;
		}
		listener.readProperty(group, propertyName, parameters, value);
	}

	/**
	 * <p>
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <table border="1">
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
	 * <table border="1">
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
	 * Determines whether the end of the data stream has been reached.
	 * @return true if the end has been reached, false if not
	 */
	public boolean eof() {
		return eof;
	}

	/**
	 * Handles the vCard data as it is read off the data stream. Each one of
	 * this interface's methods may throw a {@link StopReadingException} at any
	 * time to force the parser to stop reading from the data stream. This will
	 * cause the reader to return from the {@link VCardRawReader#start} method.
	 * To continue reading from the data stream, simply call the
	 * {@link VCardRawReader#start} method again.
	 * @author Michael Angstadt
	 */
	public static interface VCardDataStreamListener {
		/**
		 * Called when a component begins (when a "BEGIN:NAME" property is
		 * reached).
		 * @param name the component name (e.g. "VCARD")
		 * @throws StopReadingException to force the reader to stop reading from
		 * the data stream
		 */
		void beginComponent(String name);

		/**
		 * Called when a property is read.
		 * @param group the group name or null if no group was defined
		 * @param name the property name (e.g. "VERSION")
		 * @param parameters the parameters
		 * @param value the property value
		 * @throws StopReadingException to force the reader to stop reading from
		 * the data stream
		 */
		void readProperty(String group, String name, VCardSubTypes parameters, String value);

		/**
		 * Called when the vCard's VERSION property is read.
		 * @param version the version that was read
		 */
		void readVersion(VCardVersion version);

		/**
		 * Called when a component ends (when a "END:NAME" property is reached).
		 * @param name the component name (e.g. "VCARD")
		 * @throws StopReadingException to force the reader to stop reading from
		 * the data stream
		 */
		void endComponent(String name);

		/**
		 * Called when a line cannot be parsed.
		 * @param line the unparseable line
		 * @throws StopReadingException to force the reader to stop reading from
		 * the data stream
		 */
		void invalidLine(String line);

		/**
		 * Called when an invalid VERSION property is encountered.
		 * @param version the invalid version
		 */
		void invalidVersion(String version);
	}

	/**
	 * Instructs a {@link VCardRawReader} to stop reading from the data stream
	 * when thrown from a {@link VCardDataStreamListener} implementation.
	 * @author Michael Angstadt
	 */
	@SuppressWarnings("serial")
	public static class StopReadingException extends VCardException {
		//empty
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
