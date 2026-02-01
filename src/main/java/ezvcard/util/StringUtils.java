package ezvcard.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
 * Helper class for dealing with strings.
 * @author Michael Angstadt
 */
public final class StringUtils {
	/**
	 * The local computer's newline character sequence.
	 */
	public static final String NEWLINE = System.lineSeparator();

	/**
	 * Creates a copy of the given map, converting its keys and values to
	 * lowercase.
	 * @param map the map
	 * @return the copy with lowercase keys and values
	 */
	static Map<String, String> mapToLowercase(Map<String, String> map) {
		Map<String, String> lowerMap = new HashMap<>(map.size());
		map.forEach((key, value) -> lowerMap.put(toLowerCase(key), toLowerCase(value)));
		return lowerMap;
	}

	private static String toLowerCase(String s) {
		return (s == null) ? null : s.toLowerCase();
	}

	/**
	 * Compares two strings using case-insensitive equality, also checking for
	 * null.
	 * @param a the first string (can be null)
	 * @param b the second string (can be null)
	 * @return true if they are equal, false if not
	 */
	public static boolean equalsIgnoreCase(String a, String b) {
		return (a == null) ? b == null : a.equalsIgnoreCase(b);
	}

	/**
	 * Compares two string-based maps using case-insensitive equality, also
	 * checking for null.
	 * @param a the first map (can be null)
	 * @param b the second map (can be null)
	 * @return true if they are equal, false if not
	 */
	public static boolean equalsIgnoreCase(Map<String, String> a, Map<String, String> b) {
		if (a == null) return b == null;
		if (b == null) return a == null;
		if (a.size() != b.size()) return false;

		return mapToLowercase(a).equals(mapToLowercase(b));
	}

	/**
	 * Compares two lists of strings using case-insensitive equality and
	 * ignoring order. Also checks for null.
	 * @param a the first list (can be null)
	 * @param b the second list (can be null)
	 * @return true if they are equal, false if not
	 */
	public static boolean equalsIgnoreCaseIgnoreOrder(List<String> a, List<String> b) {
		if (a == null) return b == null;
		if (b == null) return a == null;
		if (a.size() != b.size()) return false;

		return listToLowerCaseAndSort(a).equals(listToLowerCaseAndSort(b));
	}

	private static List<String> listToLowerCaseAndSort(List<String> values) {
		//@formatter:off
		return values.stream()
			.map(String::toLowerCase)
			.sorted()
		.collect(Collectors.toList());
		//@formatter:on
	}

	/**
	 * Calls {@link Objects#hash}, replacing all String objects with lowercase
	 * versions.
	 * @param objects the objects to hash
	 * @return the hash code
	 */
	public static int hashIgnoreCase(Object... objects) {
		//@formatter:off
		IntStream.range(0, objects.length)
			.filter(i -> objects[i] instanceof String)
		.forEach(i -> objects[i] = ((String) objects[i]).toLowerCase());
		//@formatter:on

		return Objects.hash(objects);
	}

	/**
	 * Generates a case-insensitive hash code of a string-based map.
	 * @param map the map (can be null)
	 * @return the hash code or 0 if the map is null
	 */
	public static int hashIgnoreCase(Map<String, String> map) {
		return (map == null) ? 0 : mapToLowercase(map).hashCode();
	}

	private StringUtils() {
		//hide
	}
}
