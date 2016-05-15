package ezvcard.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 * Helper class for dealing with strings.
 * @author Michael Angstadt
 */
public final class StringUtils {
	/**
	 * The local computer's newline character sequence.
	 */
	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * Trims the whitespace off the left side of a string.
	 * @param string the string to trim
	 * @return the trimmed string
	 */
	public static String ltrim(String string) {
		if (string == null) {
			return null;
		}

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
		if (string == null) {
			return null;
		}

		int i;
		for (i = string.length() - 1; i >= 0 && Character.isWhitespace(string.charAt(i)); i--) {
			//do nothing
		}
		return (i == 0) ? "" : string.substring(0, i + 1);
	}

	/**
	 * Creates a string consisting of "count" occurrences of char "c".
	 * @param c the character to repeat
	 * @param count the number of times to repeat the character
	 * @return the resulting string
	 */
	public static String repeat(char c, int count) {
		if (count <= 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Creates a string consisting of "count" occurrences of string "str".
	 * @param str the string to repeat
	 * @param count the number of times to repeat the string
	 * @return the resulting string
	 */
	public static String repeat(String str, int count) {
		if (count <= 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(count * str.length());
		for (int i = 0; i < count; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param <T> the value class
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
	 * @param <T> the value class
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
	 * @param <T> the value class
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
	 * @param <T> the value class
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
	 * @param <K> the key class
	 * @param <V> the value class
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
	 * Callback interface used with various {@code StringUtils.join()} methods.
	 * @author Michael Angstadt
	 * @param <T> the value class
	 */
	public interface JoinCallback<T> {
		void handle(StringBuilder sb, T value);
	}

	/**
	 * Callback interface used with the
	 * {@link #join(Map, String, JoinMapCallback)} method.
	 * @author Michael Angstadt
	 * @param <K> the key class
	 * @param <V> the value class
	 */
	public interface JoinMapCallback<K, V> {
		void handle(StringBuilder sb, K key, V value);
	}

	/**
	 * Creates a copy of the given map, converting its keys and values to
	 * lowercase.
	 * @param map the map
	 * @return the copy with lowercase keys and values
	 */
	public static Map<String, String> toLowerCase(Map<String, String> map) {
		Map<String, String> lowerCaseMap = new HashMap<String, String>(map.size());
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey().toLowerCase();
			String value = entry.getValue();
			value = (value == null) ? null : value.toLowerCase();

			lowerCaseMap.put(key, value);
		}
		return lowerCaseMap;
	}

	private StringUtils() {
		//hide
	}
}
