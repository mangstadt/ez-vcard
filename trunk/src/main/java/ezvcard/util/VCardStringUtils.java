package ezvcard.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/*
 Copyright (c) 2012, Michael Angstadt
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
					sb.append("\r\n");
				} else if (ch == 't' || ch == 'T') {
					//just to be consistent with "\n"
					//don't know if this is part of the standard
					sb.append('\t');
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
	 * Escapes text for safe inclusion in a vCard value.
	 * @param text the text to escape
	 * @return the escaped text
	 */
	public static String escapeText(String text) {
		//escape backslashes, commas, and semi-colons
		String chars = "\\,;";
		for (int i = 0; i < chars.length(); i++) {
			String ch = chars.substring(i, i + 1);
			text = text.replace(ch, "\\" + ch);
		}

		//escape newlines
		text = text.replaceAll("\\r\\n|\\r|\\n", "\\\\n");

		return text;
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
	 * @see http://stackoverflow.com/q/820172
	 */
	public static String[] splitBy(String str, char ch, boolean removeEmpties, boolean unescape) {
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

		return list.toArray(new String[0]);
	}

	/**
	 * Folds a line of text. This wraps the line according to a max line length
	 * and then inserts a whitespace character at the beginning of each
	 * additional line. This is used for email headers and vCards. See RFC 5322
	 * p.8.
	 * @param line the line to fold
	 * @param maxLength the max length each line can be
	 * @return the folded line
	 */
	public static String fold(String line, int maxLength) {
		if (line.length() <= maxLength) {
			return line;
		}

		StringBuilder sb = new StringBuilder();
		int cur = 0;
		boolean firstLine = true;
		while (cur < line.length()) {
			if (!firstLine) {
				sb.append("\r\n").append(" ");
			}

			int end = cur + maxLength;
			if (end > line.length()) {
				end = line.length();
			}
			String sub = line.substring(cur, end);

			int space = sub.lastIndexOf(" ");
			if (space == -1) {
				sb.append(sub);
				cur += maxLength;
			} else {
				sb.append(sub.substring(0, space));
				cur += space + 1;
			}

			if (firstLine) {
				//after the first line, the max length is decremented because all subsequent lines must begin with a space
				maxLength--;
				firstLine = false;
			}
		}

		return sb.toString();
	}

	/**
	 * Inserts new lines in the text to prevent any one line from being too
	 * long.
	 * @param line the line
	 * @param maxLength the max length the line can be
	 * @return the line with CRLF chars inserted where need be
	 */
	public static String wrap(String line, int maxLength) {
		if (line.length() <= maxLength) {
			return line;
		}

		StringBuilder sb = new StringBuilder();
		int cur = 0;
		while (cur < line.length()) {
			if (cur > 0) {
				sb.append("\r\n");
			}

			int end = cur + maxLength;
			if (end > line.length()) {
				end = line.length();
			}
			String sub = line.substring(cur, end);
			int space = sub.lastIndexOf(" ");
			if (space == -1) {
				sb.append(sub);
				cur += maxLength;
			} else {
				sb.append(sub.substring(0, space));
				cur += space + 1;
			}
		}

		return sb.toString();
	}

	/**
	 * Trims the whitespace off the left side of a string.
	 * @param string the string to trim
	 * @return the trimmed string
	 */
	public static String ltrim(String string) {
		int i;
		for (i = 0; i < string.length() && Character.isWhitespace(string.charAt(i)); i++) {
			;
		}
		return (i == string.length()) ? "" : string.substring(i);
	}

	private VCardStringUtils() {
		//hide constructor
	}
}
