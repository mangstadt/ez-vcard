package ezvcard.util;

/*
Copyright (c) 2012-2026, Michael Angstadt
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
 * Iterates over the characters in a String. Provides additional functionality
 * over {@link String#chars}.
 * @author Michael Angstadt
 */
public class CharIterator {
	private final String s;
	private int i;

	/**
	 * @param s the string to iterate over
	 */
	public CharIterator(String s) {
		this(s, 0);
	}

	/**
	 * @param s the string to iterate over
	 * @param startIndex the index to start at
	 */
	public CharIterator(String s, int startIndex) {
		this.s = s;
		i = startIndex - 1;
	}

	/**
	 * Determines if there are more characters to iterate over.
	 * @return true if there are more characters, false if not
	 */
	public boolean hasNext() {
		return i + 1 < s.length();
	}

	/**
	 * Advances to the next character.
	 * @return the next character
	 */
	public char next() {
		return s.charAt(++i);
	}

	/**
	 * Gets the previous character.
	 * @return the previous character or 0 if the iterator is at the beginning
	 * of the string
	 */
	public char prev() {
		return (i <= 0) ? 0 : s.charAt(i - 1);
	}

	/**
	 * Gets the index of the current character in the string.
	 * @return the index
	 */
	public int index() {
		return i;
	}
}
