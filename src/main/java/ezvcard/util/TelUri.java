package ezvcard.util;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
 * Represents a URI for encoding telephone numbers.
 * </p>
 * <p>
 * Example tel URI: {@code tel:+1-212-555-0101}
 * </p>
 * <p>
 * This class is immutable. Use the {@link Builder} object to construct a new
 * instance, or the {@link #parse} method to parse a tel URI string.
 * </p>
 * 
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * TelUri uri = new TelUri.Builder(&quot;+1-212-555-0101&quot;).extension(&quot;123&quot;).build();
 * TelUri uri = TelUri.parse(&quot;tel:+1-212-555-0101;ext=123&quot;);
 * TelUri copy = new TelUri.Builder(original).extension(&quot;124&quot;).build();
 * </pre>
 * @see <a href="http://tools.ietf.org/html/rfc3966">RFC 3966</a>
 * @author Michael Angstadt
 */
public final class TelUri {
	/**
	 * The characters which are allowed to exist un-encoded inside of a
	 * parameter value.
	 */
	private static final boolean validParamValueChars[] = new boolean[128];
	static {
		for (int i = '0'; i <= '9'; i++) {
			validParamValueChars[i] = true;
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			validParamValueChars[i] = true;
		}
		for (int i = 'a'; i <= 'z'; i++) {
			validParamValueChars[i] = true;
		}
		for (char c : "!$&'()*+-.:[]_~/".toCharArray()) {
			validParamValueChars[c] = true;
		}
	}

	/**
	 * Finds hex values in an encoded parameter value.
	 */
	private static final Pattern hexPattern = Pattern.compile("(?i)%([0-9a-f]{2})");

	/**
	 * Validates parameter names.
	 */
	private static final Pattern labelTextPattern = Pattern.compile("(?i)^[-a-z0-9]+$");

	/**
	 * Regular expression for parsing tel URIs.
	 */
	private static final Pattern uriPattern = Pattern.compile("(?i)^tel:(.*?)(;(.*))?$");

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
	 * @param uri the URI
	 * @return the parsed tel URI
	 * @throws IllegalArgumentException if the URI cannot be parsed
	 */
	public static TelUri parse(String uri) {
		Matcher m = uriPattern.matcher(uri);
		if (!m.find()) {
			throw new IllegalArgumentException("Invalid tel URI: " + uri);
		}

		Builder builder = new Builder();
		builder.number = m.group(1);

		String paramsStr = m.group(3);
		if (paramsStr != null) {
			String paramsArray[] = paramsStr.split(";");

			for (String param : paramsArray) {
				String paramSplit[] = param.split("=", 2);
				String paramName = paramSplit[0];
				String paramValue = paramSplit.length > 1 ? decodeParamValue(paramSplit[1]) : "";

				if (PARAM_EXTENSION.equalsIgnoreCase(paramName)) {
					builder.extension = paramValue;
					continue;
				}

				if (PARAM_ISDN_SUBADDRESS.equalsIgnoreCase(paramName)) {
					builder.isdnSubaddress = paramValue;
					continue;
				}

				if (PARAM_PHONE_CONTEXT.equalsIgnoreCase(paramName)) {
					builder.phoneContext = paramValue;
					continue;
				}

				builder.parameters.put(paramName, paramValue);
			}
		}

		return builder.build();
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
		sb.append(';').append(name).append('=').append(encodeParamValue(value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((isdnSubaddress == null) ? 0 : isdnSubaddress.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((phoneContext == null) ? 0 : phoneContext.hashCode());
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
		} else if (!extension.equals(other.extension)) return false;
		if (isdnSubaddress == null) {
			if (other.isdnSubaddress != null) return false;
		} else if (!isdnSubaddress.equals(other.isdnSubaddress)) return false;
		if (number == null) {
			if (other.number != null) return false;
		} else if (!number.equals(other.number)) return false;
		if (parameters == null) {
			if (other.parameters != null) return false;
		} else if (!parameters.equals(other.parameters)) return false;
		if (phoneContext == null) {
			if (other.phoneContext != null) return false;
		} else if (!phoneContext.equals(other.phoneContext)) return false;
		return true;
	}

	/**
	 * Determines if a given string can be used as a parameter name.
	 * @param text the parameter name
	 * @return true if it contains all valid characters, false if not
	 * @see "RFC 3966 p.5 ('pname' definition)"
	 */
	private static boolean isParameterName(String text) {
		return labelTextPattern.matcher(text).matches();
	}

	/**
	 * Determines if a given string is a phone digit.
	 * @param text the string
	 * @return true if it's a phone digit, false if not
	 * @see "RFC 3966 p.5 ('phonedigit' definition)"
	 */
	private static boolean isPhoneDigit(String text) {
		return text.matches("[-0-9.()]+");
	}

	/**
	 * Encodes a string for safe inclusion in a parameter value.
	 * @param value the string to encode
	 * @return the encoded value
	 */
	private static String encodeParamValue(String value) {
		StringBuilder sb = null;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < validParamValueChars.length && validParamValueChars[c]) {
				if (sb != null) {
					sb.append(c);
				}
			} else {
				if (sb == null) {
					sb = new StringBuilder(value.substring(0, i));
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
	private static String decodeParamValue(String value) {
		Matcher m = hexPattern.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			int hex = Integer.parseInt(m.group(1), 16);
			m.appendReplacement(sb, "" + (char) hex);
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

		private Builder() {
			//TreeMap is used because parameters should appear in lexicographical (alphabetical) order (see RFC 3966 p.5)
			parameters = new TreeMap<String, String>();
		}

		/**
		 * <p>
		 * Initializes the builder with a global telephone number.
		 * </p>
		 * <p>
		 * Global telephone numbers must:
		 * <ol>
		 * <li>Start with "+"</li>
		 * <li>Contain at least 1 digit</li>
		 * <li>Limit themselves to the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digits)</li>
		 * <li>{@code -} (hypen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening paraenthesis)</li>
		 * <li>{@code )} (closing paraenthesis)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * </p>
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
		 * <li>{@code -} (hypen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening paraenthesis)</li>
		 * <li>{@code )} (closing paraenthesis)</li>
		 * <li>{@code *} (asterisk)</li>
		 * <li>{@code #} (hash)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * </p>
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
			parameters = new TreeMap<String, String>(original.parameters);
		}

		/**
		 * <p>
		 * Sets the telephone number as a global number.
		 * </p>
		 * <p>
		 * Global telephone numbers must:
		 * <ol>
		 * <li>Start with "+"</li>
		 * <li>Contain at least 1 digit</li>
		 * <li>Limit themselves to the following characters:
		 * <ul>
		 * <li>{@code 0-9} (digits)</li>
		 * <li>{@code -} (hypen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening paraenthesis)</li>
		 * <li>{@code )} (closing paraenthesis)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * </p>
		 * @param globalNumber the telephone number (e.g. "+1-212-555-0101")
		 * @return this
		 * @throws IllegalArgumentException if the given telephone number does
		 * not adhere to the above rules
		 */
		public Builder globalNumber(String globalNumber) {
			if (!globalNumber.matches(".*?[0-9].*")) {
				throw new IllegalArgumentException("Global number must contain at least one digit.");
			}
			if (!globalNumber.startsWith("+")) {
				throw new IllegalArgumentException("Global number must start with \"+\".");
			}
			if (!globalNumber.matches("\\+[-0-9.()]*")) {
				throw new IllegalArgumentException("Global number contains invalid characters.");
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
		 * <li>{@code -} (hypen)</li>
		 * <li>{@code .} (period)</li>
		 * <li>{@code (} (opening paraenthesis)</li>
		 * <li>{@code )} (closing paraenthesis)</li>
		 * <li>{@code *} (asterisk)</li>
		 * <li>{@code #} (hash)</li>
		 * </ul>
		 * </li>
		 * </ol>
		 * </p>
		 * @param localNumber the telephone number (e.g. "7042")
		 * @param phoneContext the context under which the local number is valid
		 * (e.g. "example.com")
		 * @return this
		 * @throws IllegalArgumentException if the given telephone number does
		 * not adhere to the above rules
		 */
		public Builder localNumber(String localNumber, String phoneContext) {
			if (!localNumber.matches(".*?[0-9*#].*") || !localNumber.matches("[0-9\\-.()*#]+")) {
				throw new IllegalArgumentException("Local number contains invalid characters.");
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
			if (extension != null && !isPhoneDigit(extension)) {
				throw new IllegalArgumentException("Extension contains invalid characters.");
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
			if (!isParameterName(name)) {
				throw new IllegalArgumentException("Parameter names can only contain letters, numbers, and hyphens.");
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
