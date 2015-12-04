package ezvcard.io.text;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ezvcard.VCardVersion;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;

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
	 * Regular expression used to determine if a parameter value needs to be
	 * quoted.
	 */
	private static final Pattern quoteMeRegex = Pattern.compile(".*?[,:;].*");

	/**
	 * Regular expression used to detect newline character sequences.
	 */
	private static final Pattern newlineRegex = Pattern.compile("\\r\\n|\\r|\\n");

	/**
	 * List of characters which would break the syntax of the vCard if used
	 * inside a group or property name. The list of characters permitted by the
	 * specification is much more strict, but the goal here is to be as lenient
	 * as possible.
	 */
	private static final String invalidPropertyGroupNameCharacters = ".;:\n\r";

	/**
	 * The characters that are not valid in parameter values and that should be
	 * removed.
	 */
	private static final Map<VCardVersion, BitSet> invalidParamValueChars;
	static {
		BitSet controlChars = new BitSet(128);
		controlChars.set(0, 31);
		controlChars.set(127);
		controlChars.set('\t', false); //allow
		controlChars.set('\n', false); //allow
		controlChars.set('\r', false); //allow

		Map<VCardVersion, BitSet> map = new EnumMap<VCardVersion, BitSet>(VCardVersion.class);

		//2.1
		{
			BitSet bitSet = new BitSet(128);
			bitSet.or(controlChars);

			bitSet.set(',');
			bitSet.set('.');
			bitSet.set(':');
			bitSet.set('=');
			bitSet.set('[');
			bitSet.set(']');

			map.put(VCardVersion.V2_1, bitSet);
		}

		//3.0, 4.0
		{
			BitSet bitSet = new BitSet(128);
			bitSet.or(controlChars);

			map.put(VCardVersion.V3_0, bitSet);
			map.put(VCardVersion.V4_0, bitSet);
		}

		invalidParamValueChars = Collections.unmodifiableMap(map);
	}

	private final FoldedLineWriter writer;
	private boolean caretEncodingEnabled = false;
	private boolean propertyValidationEnabled = true;
	private ParameterValueChangedListener parameterValueChangedListener;
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
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <tr>
	 * <th>Character</th>
	 * <th>Replacement<br>
	 * (when disabled)</th>
	 * <th>Replacement<br>
	 * (when enabled)</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code '}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td><code><i>space</i></code></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
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
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <tr>
	 * <th>Character</th>
	 * <th>Replacement<br>
	 * (when disabled)</th>
	 * <th>Replacement<br>
	 * (when enabled)</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code '}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td><code><i>space</i></code></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
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
	 * Gets the parameter values changed listener.
	 * @return the listener or null if not set
	 */
	public ParameterValueChangedListener getParameterValueChangedListener() {
		return parameterValueChangedListener;
	}

	/**
	 * Sets the parameter values changed listener.
	 * @param listener the listener or null to remove
	 */
	public void setParameterValueChangedListener(ParameterValueChangedListener listener) {
		parameterValueChangedListener = listener;
	}

	/**
	 * Whether property names are checked in strict adherence with the spec.
	 */
	public boolean isPropertyValidationEnabled() {
		return propertyValidationEnabled;
	}

	/**
	 * Sets whether property names are checked in strict adherence with the spec, defaults
	 * to on. Turning this off may be useful when dealing with regrettably lenient services,
	 * e.g. iCloud, which calls anything before the first colon and after the
	 * first period (group) as the property name.
	 */
	public void setPropertyValidationEnabled(boolean propertyValidationEnabled) {
		this.propertyValidationEnabled = propertyValidationEnabled;
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
	 * @throws IllegalArgumentException if the group or property name contains
	 * invalid characters
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String group, String propertyName, VCardParameters parameters, String value) throws IOException {
		//validate the group name
		if (group != null) {
			if (!isValidGroupName(group)) {
				throw new IllegalArgumentException("Property \"" + propertyName + "\" has its group name set to \"" + group + "\".  This group name contains one or more invalid characters.  The following characters are not permitted: .;:\\n\\r");
			}
			if (beginsWithWhitespace(group)) {
				throw new IllegalArgumentException("Property \"" + propertyName + "\" has its group name set to \"" + group + "\".  This group name begins with one or more whitespace characters, which is not permitted.");
			}
		}

		//validate the property name
		if (propertyValidationEnabled && !isValidPropertyName(propertyName)) {
			throw new IllegalArgumentException("Property name \"" + propertyName + "\" contains one or more invalid characters.  The following characters are not permitted: .;:\\n\\r");
		}
		if (propertyValidationEnabled && beginsWithWhitespace(propertyName)) {
			throw new IllegalArgumentException("Property name \"" + propertyName + "\" begins with one or more whitespace characters, which is not permitted.");
		}

		value = sanitizeValue(parameters, value);

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
				if (containsSpecialChars(parameterValue)) {
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

	private boolean isValidGroupName(String group) {
		return isValidPropertyName(group);
	}

	private boolean isValidPropertyName(String name) {
		for (int i = 0; i < invalidPropertyGroupNameCharacters.length(); i++) {
			char c = invalidPropertyGroupNameCharacters.charAt(i);
			if (name.indexOf(c) >= 0) {
				return false;
			}
		}
		return true;
	}

	private boolean beginsWithWhitespace(String string) {
		if (string.length() == 0) {
			return false;
		}
		char first = string.charAt(0);
		return (first == ' ' || first == '\t');
	}

	/**
	 * Sanitizes a property value for safe inclusion in a vCard.
	 * @param parameters the property's parameters
	 * @param value the value to sanitize
	 * @return the sanitized value
	 */
	private String sanitizeValue(VCardParameters parameters, String value) {
		if (value == null) {
			return "";
		}

		/*
		 * 2.1 does not support the "\n" escape sequence (see "Delimiters"
		 * sub-section in section 2 of the specs) so encode the value in
		 * quoted-printable encoding if any newline characters exist
		 */
		if (version == VCardVersion.V2_1 && containsNewlines(value)) {
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			return value;
		}

		return escapeNewlines(value);
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
		String modifiedValue = null;
		boolean valueChanged = false;

		switch (version) {
		case V2_1:
			//remove invalid characters
			modifiedValue = removeInvalidParameterValueChars(parameterValue);

			//replace newlines with spaces
			modifiedValue = newlineRegex.matcher(modifiedValue).replaceAll(" ");

			//check to see if value was changed
			valueChanged = !parameterValue.equals(modifiedValue);

			//escape backslashes
			modifiedValue = modifiedValue.replace("\\", "\\\\");

			//escape semi-colons (see section 2)
			modifiedValue = modifiedValue.replace(";", "\\;");

			break;

		case V3_0:
			//remove invalid characters
			modifiedValue = removeInvalidParameterValueChars(parameterValue);

			if (caretEncodingEnabled) {
				valueChanged = !modifiedValue.equals(parameterValue);

				//apply caret encoding
				modifiedValue = applyCaretEncoding(modifiedValue);
			} else {
				//replace double quotes with single quotes
				modifiedValue = modifiedValue.replace('"', '\'');

				//replace newlines with spaces
				modifiedValue = newlineRegex.matcher(modifiedValue).replaceAll(" ");

				valueChanged = !modifiedValue.equals(parameterValue);
			}

			break;

		case V4_0:
			//remove invalid characters
			modifiedValue = removeInvalidParameterValueChars(parameterValue);

			if (caretEncodingEnabled) {
				valueChanged = !modifiedValue.equals(parameterValue);

				//apply caret encoding
				modifiedValue = applyCaretEncoding(modifiedValue);
			} else {
				//replace double quotes with single quotes
				modifiedValue = modifiedValue.replace('"', '\'');

				valueChanged = !modifiedValue.equals(parameterValue);

				//backslash-escape newlines (for the "LABEL" parameter)
				modifiedValue = newlineRegex.matcher(modifiedValue).replaceAll("\\\\\\n");
			}

			break;
		}

		if (valueChanged && parameterValueChangedListener != null) {
			parameterValueChangedListener.onParameterValueChanged(propertyName, parameterName, parameterValue, modifiedValue);
		}

		return modifiedValue;
	}

	/**
	 * Removes invalid characters from a parameter value.
	 * @param value the parameter value
	 * @return the sanitized parameter value
	 */
	private String removeInvalidParameterValueChars(String value) {
		BitSet invalidChars = invalidParamValueChars.get(version);
		StringBuilder sb = null;

		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (invalidChars.get(ch)) {
				if (sb == null) {
					sb = new StringBuilder(value.length());
					sb.append(value.substring(0, i));
				}
				continue;
			}

			if (sb != null) {
				sb.append(ch);
			}
		}

		return (sb == null) ? value : sb.toString();
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
	 * <p>
	 * Escapes all newline character sequences. The newline character sequences
	 * are:
	 * </p>
	 * <ul>
	 * <li>{@code \r\n}</li>
	 * <li>{@code \r}</li>
	 * <li>{@code \n}</li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	private String escapeNewlines(String text) {
		return newlineRegex.matcher(text).replaceAll("\\\\n");
	}

	/**
	 * <p>
	 * Determines if a string has at least one newline character sequence. The
	 * newline character sequences are:
	 * </p>
	 * <ul>
	 * <li>{@code \r\n}</li>
	 * <li>{@code \r}</li>
	 * <li>{@code \n}</li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	private boolean containsNewlines(String text) {
		return newlineRegex.matcher(text).find();
	}

	/**
	 * Determines if a parameter value contains special characters.
	 * @param parameterValue the parameter value
	 * @return true if it contains special characters, false if not
	 */
	private boolean containsSpecialChars(String parameterValue) {
		return quoteMeRegex.matcher(parameterValue).matches();
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

	/**
	 * Invoked when a parameter value is changed in a lossy way, due to it
	 * containing invalid characters. If a character can be escaped (such as the
	 * "^" character when caret encoding is enabled), then this does not count
	 * as the parameter being modified because it can be decoded without losing
	 * any information.
	 * @see VCardRawWriter#setParameterValueChangedListener
	 * @author Michael Angstadt
	 */
	public static interface ParameterValueChangedListener {
		/**
		 * Called when a parameter value is changed in a lossy way.
		 * @param propertyName the name of the property that the parameter
		 * belongs to
		 * @param parameterName the parameter name
		 * @param originalValue the original parameter value
		 * @param modifiedValue the modified parameter value
		 */
		void onParameterValueChanged(String propertyName, String parameterName, String originalValue, String modifiedValue);
	}
}
