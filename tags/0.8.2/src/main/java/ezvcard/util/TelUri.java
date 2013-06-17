package ezvcard.util;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
 * Represents a URI for encoding telephone numbers. Use one of the static
 * factory methods to create a new instance.
 * </p>
 * <p>
 * Example: <code>tel:+1-212-555-0101</code>
 * </p>
 * @see <a href="http://tools.ietf.org/html/rfc3966">RFC 3966</a>
 * @author Michael Angstadt
 */
public class TelUri {
	/**
	 * The non-alphanumeric characters which are allowed to exist inside of a
	 * parameter value.
	 */
	private static final char validParamValueChars[] = "!$&'()*+-.:[]_~/".toCharArray();
	static {
		//make sure the array is sorted for binary search
		Arrays.sort(validParamValueChars);
	}

	/**
	 * Finds hex values in an encoded parameter value.
	 */
	private static final Pattern hexPattern = Pattern.compile("(?i)%([0-9a-f]{2})");

	private static final String PARAM_EXTENSION = "ext";
	private static final String PARAM_ISDN_SUBADDRESS = "isub";
	private static final String PARAM_PHONE_CONTEXT = "phone-context";

	private String number;
	private String extension;
	private String isdnSubaddress;
	private String phoneContext;

	//Note: TreeMap is used because parameters should appear in lexicographical order (see RFC 3966 p.5)
	private final Map<String, String> parameters = new TreeMap<String, String>();

	private TelUri() {
		//hide
	}

	/**
	 * <p>
	 * Creates a URI for a global telephone number.
	 * </p>
	 * <p>
	 * Global telephone numbers must:
	 * <ol>
	 * <li>Start with "+"</li>
	 * <li>Contain at least 1 digit</li>
	 * <li>Limit themselves to the following characters:
	 * <ul>
	 * <li><code>0-9</code> (digits)</li>
	 * <li><code>-</code> (hypen)</li>
	 * <li><code>.</code> (period)</li>
	 * <li><code>(</code> (opening paraenthesis)</li>
	 * <li><code>)</code> (closing paraenthesis)</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * </p>
	 * @param globalNumber the telephone number (e.g. "+1-212-555-0101")
	 * @return the tel URI object
	 * @throws IllegalArgumentException if the given telephone number does not
	 * adhere to the above rules
	 */
	public static TelUri global(String globalNumber) {
		if (!globalNumber.matches(".*?[0-9].*")) {
			throw new IllegalArgumentException("Global number must contain at least one digit.");
		}
		if (!globalNumber.startsWith("+")) {
			throw new IllegalArgumentException("Global number must start with \"+\".");
		}
		if (!globalNumber.matches("\\+[-0-9.()]*")) {
			throw new IllegalArgumentException("Global number contains invalid characters.");
		}

		TelUri uri = new TelUri();
		uri.number = globalNumber;
		return uri;
	}

	/**
	 * <p>
	 * Creates a URI for a local telephone number. Note that the global format
	 * is preferred (see {@link #global(String)}).
	 * </p>
	 * <p>
	 * Local telephone numbers must:
	 * <ol>
	 * <li>Contain at least 1 of the following characters:
	 * <ul>
	 * <li><code>0-9</code> (digit)</li>
	 * <li><code>*</code> (asterisk)</li>
	 * <li><code>#</code> (hash)</li>
	 * </ul>
	 * </li>
	 * <li>Limit themselves to the following characters:
	 * <ul>
	 * <li><code>0-9</code> (digits)</li>
	 * <li><code>-</code> (hypen)</li>
	 * <li><code>.</code> (period)</li>
	 * <li><code>(</code> (opening paraenthesis)</li>
	 * <li><code>)</code> (closing paraenthesis)</li>
	 * <li><code>*</code> (asterisk)</li>
	 * <li><code>#</code> (hash)</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * </p>
	 * @param localNumber the telephone number (e.g. "7042")
	 * @param phoneContext the context under which the local number is valid
	 * (e.g. "example.com")
	 * @return the tel URI object
	 * @throws IllegalArgumentException if the given telephone number does not
	 * adhere to the above rules
	 */
	public static TelUri local(String localNumber, String phoneContext) {
		if (!localNumber.matches(".*?[0-9*#].*") || !localNumber.matches("[0-9\\-.()*#]+")) {
			throw new IllegalArgumentException("Local number contains invalid characters.");
		}

		TelUri uri = new TelUri();
		uri.number = localNumber;
		uri.phoneContext = phoneContext;
		return uri;
	}

	/**
	 * Parses a tel URI.
	 * @param uri the URI
	 * @throws IllegalArgumentException if the URI cannot be parsed
	 */
	public static TelUri parse(String uri) {
		Pattern p = Pattern.compile("(?i)^tel:(.*?)(;(.*))?$");
		Matcher m = p.matcher(uri);
		if (m.find()) {
			TelUri telUri = new TelUri();
			telUri.number = m.group(1);

			String paramsStr = m.group(3);
			if (paramsStr != null) {
				String paramsArray[] = paramsStr.split(";");

				for (String param : paramsArray) {
					String paramSplit[] = param.split("=", 2);
					String paramName = paramSplit[0];
					String paramValue = paramSplit.length > 1 ? paramSplit[1] : "";
					if (PARAM_EXTENSION.equalsIgnoreCase(paramName)) {
						telUri.extension = paramValue;
					} else if (PARAM_ISDN_SUBADDRESS.equalsIgnoreCase(paramName)) {
						telUri.isdnSubaddress = paramValue;
					} else if (PARAM_PHONE_CONTEXT.equalsIgnoreCase(paramName)) {
						telUri.phoneContext = paramValue;
					} else {
						paramValue = decodeParamValue(paramValue);
						telUri.parameters.put(paramName, paramValue);
					}
				}
			}

			return telUri;
		}
		throw new IllegalArgumentException("Invalid tel URI: " + uri);
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
	 * Sets the extension.
	 * @param extension the extension (e.g. "101") or null to remove
	 * @throws IllegalArgumentException if the extension contains characters
	 * other than the following: digits, hypens, parenthesis, periods
	 */
	public void setExtension(String extension) {
		if (extension != null && !isPhonedigit(extension)) {
			throw new IllegalArgumentException("Extension contains invalid characters.");
		}
		this.extension = extension;
	}

	/**
	 * Gets the ISDN sub address.
	 * @return the ISDN sub address or null if not set
	 */
	public String getIsdnSubaddress() {
		return isdnSubaddress;
	}

	/**
	 * Sets the ISDN sub address.
	 * @param isdnSubaddress the ISDN sub address or null to remove
	 */
	public void setIsdnSubaddress(String isdnSubaddress) {
		this.isdnSubaddress = isdnSubaddress;
	}

	/**
	 * Adds a parameter.
	 * @param name the parameter name (can only contain letters, numbers, and
	 * hyphens)
	 * @param value the parameter value
	 * @throws IllegalArgumentException if the parameter name contains invalid
	 * characters
	 */
	public void addParameter(String name, String value) {
		if (!isPname(name)) {
			throw new IllegalArgumentException("Parameter names can only contain letters, numbers, and hyphens.");
		}

		if (value == null) {
			value = "";
		}

		parameters.put(name, value);
	}

	/**
	 * Removes a parameter.
	 * @param name the name of the parameter to remove
	 */
	public void removeParameter(String name) {
		parameters.remove(name);
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
			writeParameter(name, value, sb); //note: the param name is validated in "addParameter()"
		}

		return sb.toString();
	}

	/**
	 * Writes a parameter to a string.
	 * @param name the parameter name
	 * @param value the parameter value
	 * @param sb the string to write to
	 */
	private void writeParameter(String name, String value, StringBuilder sb) {
		sb.append(';').append(name).append('=').append(encodeParamValue(value));
	}

	/**
	 * Determines if a given string can be used as a parameter name.
	 * @param text the parameter name
	 * @return true if it contains all valid characters, false if not
	 * @see "RFC 3966 p.5 ('pname' definition)"
	 */
	static boolean isPname(String text) {
		return text.matches("(?i)[-a-z0-9]+");
	}

	/**
	 * Determines if a given string is a phone digit.
	 * @param text the string
	 * @return true if it's a phone digit, false if not
	 * @see "RFC 3966 p.5 ('phonedigit' definition)"
	 */
	static boolean isPhonedigit(String text) {
		return text.matches("[-0-9.()]+");
	}

	/**
	 * Encodes a string for safe inclusion in a parameter value.
	 * @param value the string to encode
	 * @return the encoded value
	 */
	static String encodeParamValue(String value) {
		StringBuilder sb = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || Arrays.binarySearch(validParamValueChars, c) >= 0) {
				sb.append(c);
			} else {
				int cInt = (int) c;
				sb.append('%');
				sb.append(Integer.toString(cInt, 16));
			}
		}
		return sb.toString();
	}

	/**
	 * Decodes escaped characters in a parameter value.
	 * @param value the parameter value
	 * @return the decoded value
	 */
	static String decodeParamValue(String value) {
		Matcher m = hexPattern.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			int hex = Integer.parseInt(m.group(1), 16);
			m.appendReplacement(sb, "" + (char) hex);
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
