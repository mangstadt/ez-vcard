package ezvcard.util;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.Messages;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * <p>
 * Represents a URI for encoding telephone numbers.
 * </p>
 * <p>
 * Example tel URI: {@code tel:+1-212-555-0101}
 * </p>
 * <p>
 * This class is immutable. Use the {@link Builder} class to construct a new
 * instance, or the {@link #parse} method to parse a tel URI string.
 * </p>
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * TelUri uri = new TelUri.Builder("+1-212-555-0101").extension("123").build();
 * TelUri uri = TelUri.parse("tel:+1-212-555-0101;ext=123");
 * TelUri copy = new TelUri.Builder(uri).extension("124").build();
 * </pre>
 * @see <a href="http://tools.ietf.org/html/rfc3966">RFC 3966</a>
 * @author Michael Angstadt
 */
public final class TelUri {
	/**
	 * The characters which are allowed to exist unencoded inside of a parameter
	 * value.
	 */
	private static final boolean validParameterValueCharacters[] = new boolean[128];
	static {
		for (int i = '0'; i <= '9'; i++) {
			validParameterValueCharacters[i] = true;
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			validParameterValueCharacters[i] = true;
		}
		for (int i = 'a'; i <= 'z'; i++) {
			validParameterValueCharacters[i] = true;
		}
		String s = "!$&'()*+-.:[]_~/";
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			validParameterValueCharacters[c] = true;
		}
	}

	/**
	 * Finds hex values in an encoded parameter value.
	 */
	private static final Pattern hexPattern = Pattern.compile("(?i)%([0-9a-f]{2})");

	private static final String PARAM_EXTENSION = "ext";
	private static final String PARAM_ISDN_SUBADDRESS = "isub";
	private static final String PARAM_PHONE_CONTEXT = "phone-context";

	private final String number;
	private final String extension;
	private final String isdnSubaddress;
	private final String phoneContext;
	private final Map<String, String> parameters;

	private TelUri(Builder builder) {
		number = builder.number;
		extension = builder.extension;
		isdnSubaddress = builder.isdnSubaddress;
		phoneContext = builder.phoneContext;
		parameters = Collections.unmodifiableMap(builder.parameters);
	}

	/**
	 * Parses a tel URI.
	 * @param uri the URI (e.g. "tel:+1-610-555-1234;ext=101")
	 * @return the parsed tel URI
	 * @throws IllegalArgumentException if the string is not a valid tel URI
	 */
	public static TelUri parse(String uri) {
		//URI format: tel:number;prop1=value1;prop2=value2

		String scheme = "tel:";
		if (uri.length() < scheme.length() || !uri.substring(0, scheme.length()).equalsIgnoreCase(scheme)) {
			//not a tel URI
			throw Messages.INSTANCE.getIllegalArgumentException(18, scheme);
		}

		Builder builder = new Builder();
		ClearableStringBuilder buffer = new ClearableStringBuilder();
		String paramName = null;
		for (int i = scheme.length(); i < uri.length(); i++) {
			char c = uri.charAt(i);

			if (c == '=' && builder.number != null && paramName == null) {
				paramName = buffer.getAndClear();
				continue;
			}

			if (c == ';') {
				handleEndOfParameter(buffer, paramName, builder);
				paramName = null;
				continue;
			}

			buffer.append(c);
		}

		handleEndOfParameter(buffer, paramName, builder);

		return builder.build();
	}

	private static void addParameter(String name, String value, Builder builder) {
		value = decodeParameterValue(value);

		if (PARAM_EXTENSION.equalsIgnoreCase(name)) {
			builder.extension = value;
			return;
		}

		if (PARAM_ISDN_SUBADDRESS.equalsIgnoreCase(name)) {
			builder.isdnSubaddress = value;
			return;
		}

		if (PARAM_PHONE_CONTEXT.equalsIgnoreCase(name)) {
			builder.phoneContext = value;
			return;
		}

		builder.parameters.put(name, value);
	}

	private static void handleEndOfParameter(ClearableStringBuilder buffer, String paramName, Builder builder) {
		String s = buffer.getAndClear();

		if (builder.number == null) {
			builder.number = s;
			return;
		}

		if (paramName == null) {
			if (s.length() > 0) {
				addParameter(s, "", builder);
			}
			return;
		}

		addParameter(paramName, s, builder);
	}

	/**
	 * Gets the phone number.
	 * @return the phone number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Gets the phone context.
	 * @return the phone context (e.g. "example.com") or null if not set
	 */
	public String getPhoneContext() {
		return phoneContext;
	}

	/**
	 * Gets the extension.
	 * @return the extension (e.g. "101") or null if not set
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Gets the ISDN sub address.
	 * @return the ISDN sub address or null if not set
	 */
	public String getIsdnSubaddress() {
		return isdnSubaddress;
	}

	/**
	 * Gets a parameter value.
	 * @param name the parameter name
	 * @return the parameter value or null if not found
	 */
	public String getParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Gets all parameters.
	 * @return all parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Converts this tel URI to its string representation.
	 * @return the tel URI's string representation
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("tel:");

		sb.append(number);

		if (extension != null) {
			writeParameter(PARAM_EXTENSION, extension, sb);
		}
		if (isdnSubaddress != null) {
			writeParameter(PARAM_ISDN_SUBADDRESS, isdnSubaddress, sb);
		}
		if (phoneContext != null) {
			writeParameter(PARAM_PHONE_CONTEXT, phoneContext, sb);
		}

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			writeParameter(name, value, sb);
		}

		return sb.toString();
	}

	/**
	 * Writes a parameter to a string.
	 * @param name the parameter name
	 * @param value the parameter value
	 * @param sb the string to write to
	 */
	private static void writeParameter(String name, String value, StringBuilder sb) {
		sb.append(';').append(name).append('=').append(encodeParameterValue(value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extension == null) ? 0 : extension.toLowerCase().hashCode());
		result = prime * result + ((isdnSubaddress == null) ? 0 : isdnSubaddress.toLowerCase().hashCode());
		result = prime * result + ((number == null) ? 0 : number.toLowerCase().hashCode());
		result = prime * result + ((parameters == null) ? 0 : StringUtils.toLowerCase(parameters).hashCode());
		result = prime * result + ((phoneContext == null) ? 0 : phoneContext.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TelUri other = (TelUri) obj;
		if (extension == null) {
			if (other.extension != null) return false;
		} else if (!extension.equalsIgnoreCase(other.extension)) return false;
		if (isdnSubaddress == null) {
			if (other.isdnSubaddress != null) return false;
		} else if (!isdnSubaddress.equalsIgnoreCase(other.isdnSubaddress)) return false;
		if (number == null) {
			if (other.number != null) return false;
		} else if (!number.equalsIgnoreCase(other.number)) return false;
		if (parameters == null) {
			if (other.parameters != null) return false;
		} else {
			if (other.parameters == null) return false;
			if (parameters.size() != other.parameters.size()) return false;

			Map<String, String> parametersLower = StringUtils.toLowerCase(parameters);
			Map<String, String> otherParametersLower = StringUtils.toLowerCase(other.parameters);
			if (!parametersLower.equals(otherParametersLower)) return false;
		}
		if (phoneContext == null) {
			if (other.phoneContext != null) return false;
		} else if (!phoneContext.equalsIgnoreCase(other.phoneContext)) return false;
		return true;
	}

	/**
	 * Encodes a string for safe inclusion in a parameter value.
	 * @param value the string to encode
	 * @return the encoded value
	 */
	private static String encodeParameterValue(String value) {
		StringBuilder sb = null;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < validParameterValueCharacters.length && validParameterValueCharacters[c]) {
				if (sb != null) {
					sb.append(c);
				}
			} else {
				if (sb == null) {
					sb = new StringBuilder(value.length() * 2);
					sb.append(value, 0, i);
				}
				String hex = Integer.toString(c, 16);
				sb.append('%').append(hex);
			}
		}
		return (sb == null) ? value : sb.toString();
	}

	/**
	 * Decodes escaped characters in a parameter value.
	 * @param value the parameter value
	 * @return the decoded value
	 */
	private static String decodeParameterValue(String value) {
		Matcher m = hexPattern.matcher(value);
		StringBuffer sb = null;

		while (m.find()) {
			if (sb == null) {
				sb = new StringBuffer(value.length());
			}

			int hex = Integer.parseInt(m.group(1), 16);
			m.appendReplacement(sb, Character.toString((char) hex));
		}

		if (sb == null) {
			return value;
		}

		m.appendTail(sb);
		return sb.toString();
	}

	public static class Builder {
		private String number;
		private String extension;
		private String isdnSubaddress;
		private String phoneContext;
		private Map<String, String> parameters;
		private CharacterBitSet validParamNameChars = new CharacterBitSet("a-zA-Z0-9-");

		private Builder() {
			/*
			 * TreeMap is used because parameters should appear in
			 * lexicographical (alphabetical) order (see RFC 3966 p.5)
			 */
			parameters = new TreeMap<>();
		}

		/**
		 * <p>
		 * Initializes the builder with a global telephone number.
		 * </p>
		 * <p>
		 * Global telephone numbers must:
		 * </p>
		 * <ol>
		 * <li>Start with "+"</li>
		 * <li>Contain at least 1 digit</li>
		 * <li>Limit themselves to the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digits)</li>
		 * <li>{@code -} (hyphen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening parenthesis)</li>
		 * <li>{@code )} (closing parenthesis)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * @param globalNumber the telephone number (e.g. "+1-212-555-0101")
		 * @throws IllegalArgumentException if the given telephone number does
		 * not adhere to the above rules
		 */
		public Builder(String globalNumber) {
			this();
			globalNumber(globalNumber);
		}

		/**
		 * <p>
		 * Initializes the builder with a local telephone number. Note, however,
		 * that the global format is preferred.
		 * </p>
		 * <p>
		 * Local telephone numbers must:
		 * </p>
		 * <ol>
		 * <li>Contain at least 1 of the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digit)</li>
		 * <li>{@code *} (asterisk)</li>
		 * <li>{@code #} (hash)</li>
		 * </ul>
		 * </li>
		 * <li>Limit themselves to the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digits)</li>
		 * <li>{@code -} (hyphen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening parenthesis)</li>
		 * <li>{@code )} (closing parenthesis)</li>
		 * <li>{@code *} (asterisk)</li>
		 * <li>{@code #} (hash)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * @param localNumber the telephone number (e.g. "7042")
		 * @param phoneContext the context under which the local number is valid
		 * (e.g. "example.com")
		 * @throws IllegalArgumentException if the given telephone number does
		 * not adhere to the above rules
		 */
		public Builder(String localNumber, String phoneContext) {
			this();
			localNumber(localNumber, phoneContext);
		}

		/**
		 * Creates a new {@link TelUri} builder.
		 * @param original the {@link TelUri} object to copy from
		 */
		public Builder(TelUri original) {
			number = original.number;
			extension = original.extension;
			isdnSubaddress = original.isdnSubaddress;
			phoneContext = original.phoneContext;
			parameters = new TreeMap<>(original.parameters);
		}

		/**
		 * <p>
		 * Sets the telephone number as a global number.
		 * </p>
		 * <p>
		 * Global telephone numbers must:
		 * </p>
		 * <ol>
		 * <li>Start with "+"</li>
		 * <li>Contain at least 1 digit</li>
		 * <li>Limit themselves to the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digits)</li>
		 * <li>{@code -} (hyphen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening parenthesis)</li>
		 * <li>{@code )} (closing parenthesis)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * @param globalNumber the telephone number (e.g. "+1-212-555-0101")
		 * @return this
		 * @throws IllegalArgumentException if the given telephone number does
		 * not adhere to the above rules
		 */
		public Builder globalNumber(String globalNumber) {
			if (!globalNumber.startsWith("+")) {
				throw Messages.INSTANCE.getIllegalArgumentException(26);
			}

			CharacterBitSet validChars = new CharacterBitSet("0-9.()-");
			if (!validChars.containsOnly(globalNumber, 1)) {
				throw Messages.INSTANCE.getIllegalArgumentException(27);
			}

			CharacterBitSet requiredChars = new CharacterBitSet("0-9");
			if (!requiredChars.containsAny(globalNumber, 1)) {
				throw Messages.INSTANCE.getIllegalArgumentException(25);
			}

			number = globalNumber;
			phoneContext = null;
			return this;
		}

		/**
		 * <p>
		 * Sets the telephone number as a local number. Note, however, that the
		 * global format is preferred.
		 * </p>
		 * <p>
		 * Local telephone numbers must:
		 * </p>
		 * <ol>
		 * <li>Contain at least 1 of the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digit)</li>
		 * <li>{@code *} (asterisk)</li>
		 * <li>{@code #} (hash)</li>
		 * </ul>
		 * </li>
		 * <li>Limit themselves to the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digits)</li>
		 * <li>{@code -} (hyphen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening parenthesis)</li>
		 * <li>{@code )} (closing parenthesis)</li>
		 * <li>{@code *} (asterisk)</li>
		 * <li>{@code #} (hash)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * @param localNumber the telephone number (e.g. "7042")
		 * @param phoneContext the context under which the local number is valid
		 * (e.g. "example.com")
		 * @return this
		 * @throws IllegalArgumentException if the given telephone number does
		 * not adhere to the above rules
		 */
		public Builder localNumber(String localNumber, String phoneContext) {
			CharacterBitSet validChars = new CharacterBitSet("0-9.()*#-");
			if (!validChars.containsOnly(localNumber)) {
				throw Messages.INSTANCE.getIllegalArgumentException(28);
			}

			CharacterBitSet requiredChars = new CharacterBitSet("0-9*#");
			if (!requiredChars.containsAny(localNumber)) {
				throw Messages.INSTANCE.getIllegalArgumentException(28);
			}

			number = localNumber;
			this.phoneContext = phoneContext;
			return this;
		}

		/**
		 * Sets the extension.
		 * @param extension the extension (e.g. "101") or null to remove
		 * @return this
		 * @throws IllegalArgumentException if the extension contains characters
		 * other than the following: digits, hypens, parenthesis, periods
		 */
		public Builder extension(String extension) {
			if (extension != null) {
				CharacterBitSet validChars = new CharacterBitSet("0-9.()-");
				if (!validChars.containsOnly(extension)) {
					throw Messages.INSTANCE.getIllegalArgumentException(29);
				}
			}

			this.extension = extension;
			return this;
		}

		/**
		 * Sets the ISDN sub address.
		 * @param isdnSubaddress the ISDN sub address or null to remove
		 * @return this
		 */
		public Builder isdnSubaddress(String isdnSubaddress) {
			this.isdnSubaddress = isdnSubaddress;
			return this;
		}

		/**
		 * Adds a parameter.
		 * @param name the parameter name (can only contain letters, numbers,
		 * and hyphens)
		 * @param value the parameter value or null to remove it
		 * @return this
		 * @throws IllegalArgumentException if the parameter name contains
		 * invalid characters
		 */
		public Builder parameter(String name, String value) {
			if (!validParamNameChars.containsOnly(name)) {
				throw Messages.INSTANCE.getIllegalArgumentException(23);
			}

			if (value == null) {
				parameters.remove(name);
			} else {
				parameters.put(name, value);
			}
			return this;
		}

		/**
		 * Builds the final {@link TelUri} object.
		 * @return the object
		 */
		public TelUri build() {
			return new TelUri(this);
		}
	}
}
