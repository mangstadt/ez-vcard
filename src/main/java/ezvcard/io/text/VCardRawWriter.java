package ezvcard.io.text;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ezvcard.Messages;
import ezvcard.VCardVersion;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.util.CharacterBitSet;

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
 * Writes data to an vCard data stream.
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
 */
public class VCardRawWriter implements Closeable, Flushable {
	/**
	 * If any of these characters are found within a parameter value, then the
	 * entire parameter value must be wrapped in double quotes (applies to
	 * versions 3.0 and 4.0 only).
	 */
	private final CharacterBitSet specialParameterCharacters = new CharacterBitSet(",:;");

	/**
	 * Regular expression used to detect newline character sequences.
	 */
	private final Pattern newlineRegex = Pattern.compile("\\r\\n|\\r|\\n");
	private final CharacterBitSet newlineBitSet = new CharacterBitSet("\r\n");

	/**
	 * List of characters which would break the syntax of the vCard if used
	 * inside a property name. The list of characters permitted by the
	 * specification is much more strict, but the goal here is to be as lenient
	 * as possible.
	 */
	private final CharacterBitSet invalidPropertyNameCharacters = new CharacterBitSet(".;:\n\r");

	/**
	 * List of characters which would break the syntax of the vCard if used
	 * inside a group name. The list of characters permitted by the
	 * specification is much more strict, but the goal here is to be as lenient
	 * as possible.
	 */
	private final CharacterBitSet invalidGroupNameCharacters = invalidPropertyNameCharacters;

	/**
	 * List of characters which would break the syntax of the vCard if used
	 * inside a parameter name. The list of characters permitted by the
	 * specification is much more strict, but the goal here is to be as lenient
	 * as possible.
	 */
	private final CharacterBitSet invalidParameterNameCharacters = new CharacterBitSet(";:=\n\r");

	/**
	 * List of characters which would break the syntax of the vCard if used
	 * inside a parameter value with caret encoding disabled. These characters
	 * cannot be escaped or encoded, so they are impossible to include inside of
	 * a parameter value. The list of characters permitted by the specification
	 * is much more strict, but the goal here is to be as lenient as possible.
	 */
	private final Map<VCardVersion, CharacterBitSet> invalidParamValueChars;
	{
		Map<VCardVersion, CharacterBitSet> map = new EnumMap<VCardVersion, CharacterBitSet>(VCardVersion.class);

		map.put(VCardVersion.V2_1, new CharacterBitSet(",:\n\r")); //note: semicolons can be escaped
		map.put(VCardVersion.V3_0, new CharacterBitSet("\"\r\n"));
		map.put(VCardVersion.V4_0, new CharacterBitSet("\""));

		invalidParamValueChars = Collections.unmodifiableMap(map);
	}

	/**
	 * List of characters which would break the syntax of the vCard if used
	 * inside a parameter value with caret encoding enabled. These characters
	 * cannot be escaped or encoded, so they are impossible to include inside of
	 * a parameter value. The list of characters permitted by the specification
	 * is much more strict, but the goal here is to be as lenient as possible.
	 */
	private final Map<VCardVersion, CharacterBitSet> invalidParamValueCharsWithCaretEncoding;
	{
		Map<VCardVersion, CharacterBitSet> map = new EnumMap<VCardVersion, CharacterBitSet>(VCardVersion.class);

		map.put(VCardVersion.V2_1, invalidParamValueChars.get(VCardVersion.V2_1)); //2.1 does not support caret encoding
		map.put(VCardVersion.V3_0, new CharacterBitSet(""));
		map.put(VCardVersion.V4_0, new CharacterBitSet(""));

		invalidParamValueCharsWithCaretEncoding = Collections.unmodifiableMap(map);
	}

	private final FoldedLineWriter writer;
	private boolean caretEncodingEnabled = false;
	private VCardVersion version;

	/**
	 * @param writer the writer to wrap
	 * @param version the vCard version to adhere to
	 */
	public VCardRawWriter(Writer writer, VCardVersion version) {
		this.writer = new FoldedLineWriter(writer);
		this.version = version;
	}

	/**
	 * Gets the writer object that this object wraps.
	 * @return the folded line writer
	 */
	public FoldedLineWriter getFoldedLineWriter() {
		return writer;
	}

	/**
	 * <p>
	 * Gets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default, only applies to 3.0 and 4.0
	 * vCards). This escaping mechanism allows for newlines and double quotes to
	 * be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * Note that this encoding mechanism is defined separately from the
	 * iCalendar specification and may not be supported by the vCard consumer.
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
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPittsburgh, PA 15212":40.446816;80.00566
	 * </pre>
	 * 
	 * @return true if circumflex accent encoding is enabled, false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public boolean isCaretEncodingEnabled() {
		return caretEncodingEnabled;
	}

	/**
	 * <p>
	 * Sets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default, only applies to 3.0 and 4.0
	 * vCards). This escaping mechanism allows for newlines and double quotes to
	 * be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * Note that this encoding mechanism is defined separately from the
	 * iCalendar specification and may not be supported by the vCard consumer.
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
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPittsburgh, PA 15212":40.446816;80.00566
	 * </pre>
	 * 
	 * @param enable true to use circumflex accent encoding, false not to
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public void setCaretEncodingEnabled(boolean enable) {
		caretEncodingEnabled = enable;
	}

	/**
	 * Gets the vCard version that the writer is adhering to.
	 * @return the version
	 */
	public VCardVersion getVersion() {
		return version;
	}

	/**
	 * Sets the vCard version that the writer should adhere to.
	 * @param version the version
	 */
	public void setVersion(VCardVersion version) {
		this.version = version;
	}

	/**
	 * Writes a property marking the beginning of a component (in other words,
	 * writes a "BEGIN:NAME" property).
	 * @param componentName the component name (e.g. "VCARD")
	 * @throws IOException if there's an I/O problem
	 */
	public void writeBeginComponent(String componentName) throws IOException {
		writeProperty("BEGIN", componentName);
	}

	/**
	 * Writes a property marking the end of a component (in other words, writes
	 * a "END:NAME" property).
	 * @param componentName the component name (e.g. "VCARD")
	 * @throws IOException if there's an I/O problem
	 */
	public void writeEndComponent(String componentName) throws IOException {
		writeProperty("END", componentName);
	}

	/**
	 * Writes a "VERSION" property, based on the vCard version that the writer
	 * is adhering to.
	 * @throws IOException if there's an I/O problem
	 */
	public void writeVersion() throws IOException {
		writeProperty("VERSION", version.getVersion());
	}

	/**
	 * Writes a property to the vCard data stream.
	 * @param propertyName the property name (e.g. "FN")
	 * @param value the property value
	 * @throws IllegalArgumentException if the property name contains invalid
	 * characters
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String propertyName, String value) throws IOException {
		writeProperty(null, propertyName, new VCardParameters(), value);
	}

	/**
	 * Writes a property to the vCard data stream.
	 * @param group the group or null if there is no group
	 * @param propertyName the property name (e.g. "FN")
	 * @param parameters the property parameters
	 * @param value the property value (will be converted to "quoted-printable"
	 * encoding if the {@link Encoding#QUOTED_PRINTABLE} parameter is set)
	 * @throws IllegalArgumentException if the property data contains one or
	 * more characters which break the vCard syntax and which cannot be escaped
	 * or encoded
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String group, String propertyName, VCardParameters parameters, String value) throws IOException {
		//validate the group name
		if (group != null) {
			if (invalidGroupNameCharacters.containsAny(group)) {
				throw Messages.INSTANCE.getIllegalArgumentException(7, propertyName, group, printableCharacterList(invalidGroupNameCharacters.characters()));
			}
			if (beginsWithWhitespace(group)) {
				throw Messages.INSTANCE.getIllegalArgumentException(8, propertyName, group);
			}
		}

		//validate the property name
		if (invalidPropertyNameCharacters.containsAny(propertyName)) {
			throw Messages.INSTANCE.getIllegalArgumentException(9, propertyName, printableCharacterList(invalidPropertyNameCharacters.characters()));
		}
		if (beginsWithWhitespace(propertyName)) {
			throw Messages.INSTANCE.getIllegalArgumentException(10, propertyName);
		}

		value = sanitizePropertyValue(value, parameters);

		/*
		 * Determine if the property value must be encoded in quoted printable
		 * encoding. If so, then determine what charset to use for the encoding.
		 */
		boolean useQuotedPrintable = (parameters.getEncoding() == Encoding.QUOTED_PRINTABLE);
		Charset quotedPrintableCharset = null;
		if (useQuotedPrintable) {
			String charsetParam = parameters.getCharset();
			if (charsetParam == null) {
				quotedPrintableCharset = Charset.forName("UTF-8");
			} else {
				try {
					quotedPrintableCharset = Charset.forName(charsetParam);
				} catch (Throwable t) {
					quotedPrintableCharset = Charset.forName("UTF-8");
				}
			}
			parameters.setCharset(quotedPrintableCharset.name());
		}

		//write the group
		if (group != null) {
			writer.append(group).append('.');
		}

		//write the property name
		writer.append(propertyName);

		//write the parameters
		for (Map.Entry<String, List<String>> parameter : parameters) {
			String parameterName = parameter.getKey();
			List<String> parameterValues = parameter.getValue();
			if (parameterValues.isEmpty()) {
				continue;
			}

			//check the parameter name for invalid characters
			if (invalidParameterNameCharacters.containsAny(parameterName)) {
				throw Messages.INSTANCE.getIllegalArgumentException(11, propertyName, parameterName, printableCharacterList(invalidParameterNameCharacters.characters()));
			}

			if (version == VCardVersion.V2_1) {
				boolean isTypeParameter = VCardParameters.TYPE.equalsIgnoreCase(parameterName);
				for (String parameterValue : parameterValues) {
					parameterValue = sanitizeParameterValue(parameterValue, parameterName, propertyName);

					if (isTypeParameter) {
						//e.g. ADR;HOME;WORK:
						writer.append(';').append(parameterValue.toUpperCase());
					} else {
						//e.g. ADR;FOO=bar;FOO=car:
						writer.append(';').append(parameterName).append('=').append(parameterValue);
					}
				}
				continue;
			}

			//e.g. ADR;TYPE=home,work,"another,value":
			boolean first = true;
			writer.append(';').append(parameterName).append('=');
			for (String parameterValue : parameterValues) {
				if (!first) {
					writer.append(',');
				}

				parameterValue = sanitizeParameterValue(parameterValue, parameterName, propertyName);
				if (specialParameterCharacters.containsAny(parameterValue)) {
					writer.append('"').append(parameterValue).append('"');
				} else {
					writer.append(parameterValue);
				}

				first = false;
			}
		}

		writer.append(':');
		writer.append(value, useQuotedPrintable, quotedPrintableCharset);
		writer.append(writer.getNewline());
	}

	/**
	 * Determines if a given string starts with whitespace.
	 * @param string the string
	 * @return true if it starts with whitespace, false if not
	 */
	private boolean beginsWithWhitespace(String string) {
		if (string.length() == 0) {
			return false;
		}
		char first = string.charAt(0);
		return (first == ' ' || first == '\t');
	}

	/**
	 * Sanitizes a property value for safe inclusion in a vCard.
	 * @param value the value to sanitize
	 * @param parameters the property's parameters
	 * @return the sanitized value
	 */
	private String sanitizePropertyValue(String value, VCardParameters parameters) {
		if (value == null) {
			return "";
		}

		/*
		 * 2.1 does not support the "\n" escape sequence (see "Delimiters"
		 * sub-section in section 2 of the specs) so encode the value in
		 * quoted-printable encoding if any newline characters exist.
		 */
		if (version == VCardVersion.V2_1 && newlineBitSet.containsAny(value)) {
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			return value;
		}

		//escape newlines
		return newlineRegex.matcher(value).replaceAll("\\\\n");
	}

	/**
	 * Removes or escapes all invalid characters in a parameter value.
	 * @param parameterValue the parameter value
	 * @param parameterName the parameter name
	 * @param propertyName the name of the property to which the parameter
	 * belongs
	 * @return the sanitized parameter value
	 */
	private String sanitizeParameterValue(String parameterValue, String parameterName, String propertyName) {
		CharacterBitSet invalidChars = (caretEncodingEnabled ? invalidParamValueCharsWithCaretEncoding : invalidParamValueChars).get(version);
		if (invalidChars.containsAny(parameterValue)) {
			throw Messages.INSTANCE.getIllegalArgumentException(12, propertyName, parameterName, printableCharacterList(invalidChars.characters()));
		}

		String sanitizedValue = parameterValue;
		switch (version) {
		case V2_1:
			//Note: 2.1 does not support caret encoding.

			//escape backslashes
			sanitizedValue = sanitizedValue.replace("\\", "\\\\");

			//escape semi-colons (see section 2)
			return sanitizedValue.replace(";", "\\;");

		case V3_0:
			if (caretEncodingEnabled) {
				return applyCaretEncoding(sanitizedValue);
			}
			return sanitizedValue;

		case V4_0:
			if (caretEncodingEnabled) {
				return applyCaretEncoding(sanitizedValue);
			}

			/*
			 * 4.0 allows newlines to be escaped (for the LABEL parameter).
			 */
			return newlineRegex.matcher(sanitizedValue).replaceAll("\\\\\\n");
		}

		return "";
	}

	private String printableCharacterList(String list) {
		return list.replace("\n", "\\n").replace("\r", "\\r");
	}

	/**
	 * Applies circumflex accent encoding to a string.
	 * @param value the string
	 * @return the encoded string
	 */
	private String applyCaretEncoding(String value) {
		value = value.replace("^", "^^");
		value = newlineRegex.matcher(value).replaceAll("^n");
		value = value.replace("\"", "^'");
		return value;
	}

	/**
	 * Flushes the underlying {@link Writer} object.
	 * @throws IOException if there's a problem flushing the writer
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 * @throws IOException if there's a problem closing the writer
	 */
	public void close() throws IOException {
		writer.close();
	}
}
