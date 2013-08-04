package ezvcard.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
 * Helper class for dealing with vCard strings.
 * @author Michael Angstadt
 */
public class VCardStringUtils {
	/**
	 * The local computer's newline character sequence.
	 */
	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * Unescapes all special characters that are escaped with a backslash, as
	 * well as escaped newlines.
	 * @param text the text
	 * @return the unescaped text
	 */
	public static String unescape(String text) {
		StringBuilder sb = new StringBuilder(text.length());
		boolean escaped = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (escaped) {
				if (ch == 'n' || ch == 'N') {
					//newlines appear as "\n" or "\N" (see RFC 2426 p.7)
					sb.append(NEWLINE);
				} else {
					sb.append(ch);
				}
				escaped = false;
			} else if (ch == '\\') {
				escaped = true;
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * Escapes all special characters within a vCard value.
	 * <p>
	 * These characters are:
	 * </p>
	 * <ul>
	 * <li>backslashes (<code>\</code>)</li>
	 * <li>commas (<code>,</code>)</li>
	 * <li>semi-colons (<code>;</code>)</li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	public static String escape(String text) {
		String chars = "\\,;";
		for (int i = 0; i < chars.length(); i++) {
			String ch = chars.substring(i, i + 1);
			text = text.replace(ch, "\\" + ch);
		}
		return text;
	}

	/**
	 * Escapes all newline characters within a vCard value.
	 * <p>
	 * This method escapes the following newline sequences:
	 * </p>
	 * <ul>
	 * <li><code>\r\n</code></li>
	 * <li><code>\r</code></li>
	 * <li><code>\n</code></li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	public static String escapeNewlines(String text) {
		return text.replaceAll("\\r\\n|\\r|\\n", "\\\\n");
	}

	/**
	 * Determines if a string contains one or more newline characters.
	 * @param text the string
	 * @return true if it contains one or more newline characters, false if not
	 */
	public static boolean containsNewlines(String text) {
		return text.contains("\n") || text.contains("\r");
	}

	/**
	 * Splits a string by a character, taking escaped characters into account.
	 * Each split value is also trimmed.
	 * <p>
	 * For example:
	 * <p>
	 * <code>splitBy("HE\:LLO::WORLD", ':', false, true)</code>
	 * <p>
	 * returns
	 * <p>
	 * <code>["HE:LLO", "", "WORLD"]</code>
	 * @param str the string to split
	 * @param ch the character to split by
	 * @param removeEmpties true to remove empty elements, false not to
	 * @param unescape true to unescape each split string, false not to
	 * @return the split string
	 * @see <a
	 * href="http://stackoverflow.com/q/820172">http://stackoverflow.com/q/820172</a>
	 */
	public static List<String> splitBy(String str, char ch, boolean removeEmpties, boolean unescape) {
		str = str.trim();
		String split[] = str.split("\\s*(?<!\\\\)" + Pattern.quote(ch + "") + "\\s*", -1);

		List<String> list = new ArrayList<String>(split.length);
		for (String s : split) {
			if (s.length() == 0 && removeEmpties) {
				continue;
			}

			if (unescape) {
				s = unescape(s);
			}

			list.add(s);
		}
		return list;
	}

	/**
	 * Trims the whitespace off the left side of a string.
	 * @param string the string to trim
	 * @return the trimmed string
	 */
	public static String ltrim(String string) {
		int i;
		for (i = 0; i < string.length() && Character.isWhitespace(string.charAt(i)); i++) {
			//do nothing
		}
		return (i == string.length()) ? "" : string.substring(i);
	}

	/**
	 * Trims the whitespace off the right side of a string.
	 * @param string the string to trim
	 * @return the trimmed string
	 */
	public static String rtrim(String string) {
		int i;
		for (i = string.length() - 1; i >= 0 && Character.isWhitespace(string.charAt(i)); i--) {
			//do nothing
		}
		return (i == 0) ? "" : string.substring(0, i + 1);
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @return the final string
	 */
	public static <T> String join(Collection<T> collection, String delimiter) {
		StringBuilder sb = new StringBuilder();
		join(collection, delimiter, sb);
		return sb.toString();
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param sb the string builder to append onto
	 */
	public static <T> void join(Collection<T> collection, String delimiter, StringBuilder sb) {
		join(collection, delimiter, sb, new JoinCallback<T>() {
			public void handle(StringBuilder sb, T value) {
				sb.append(value);
			}
		});
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param join callback function to call on every element in the collection
	 * @return the final string
	 */
	public static <T> String join(Collection<T> collection, String delimiter, JoinCallback<T> join) {
		StringBuilder sb = new StringBuilder();
		join(collection, delimiter, sb, join);
		return sb.toString();
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param sb the string builder to append onto
	 * @param join callback function to call on every element in the collection
	 */
	public static <T> void join(Collection<T> collection, String delimiter, StringBuilder sb, JoinCallback<T> join) {
		boolean first = true;
		for (T element : collection) {
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			join.handle(sb, element);
		}
	}

	/**
	 * Joins a map into a delimited list.
	 * @param map the map
	 * @param delimiter the delimiter (e.g. ",")
	 * @param join callback function to call on every element in the collection
	 * @return the final string
	 */
	public static <K, V> String join(Map<K, V> map, String delimiter, final JoinMapCallback<K, V> join) {
		return join(map.entrySet(), delimiter, new JoinCallback<Map.Entry<K, V>>() {
			public void handle(StringBuilder sb, Map.Entry<K, V> entry) {
				join.handle(sb, entry.getKey(), entry.getValue());
			}
		});
	}

	/**
	 * Callback interface used with various <code>VCardStringUtils.join()</code>
	 * methods.
	 * @author Michael Angstadt
	 * @param <T> the value type
	 */
	public static interface JoinCallback<T> {
		void handle(StringBuilder sb, T value);
	}

	/**
	 * Callback interface used with the
	 * {@link #join(Map, String, JoinMapCallback)} method.
	 * @author Michael Angstadt
	 * @param <K> the key class
	 * @param <V> the value class
	 */
	public static interface JoinMapCallback<K, V> {
		void handle(StringBuilder sb, K key, V value);
	}

	private VCardStringUtils() {
		//hide constructor
	}
}
