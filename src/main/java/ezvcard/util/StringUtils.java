package ezvcard.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @return the delimited string
	 */
	public static String join(Collection<?> collection, String delimiter) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (Object value : collection) {
			if (!first) {
				sb.append(delimiter);
			}

			sb.append(value);
			first = false;
		}

		return sb.toString();
	}

	/**
	 * Creates a copy of the given map, converting its keys and values to
	 * lowercase.
	 * @param map the map
	 * @return the copy with lowercase keys and values
	 */
	public static Map<String, String> toLowerCase(Map<String, String> map) {
		Map<String, String> lowerCaseMap = new HashMap<>(map.size());
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			key = (key == null) ? null : key.toLowerCase();

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
