package ezvcard.util;

import java.util.BitSet;

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
 * Represents a collection of unique characters. This class is used in place of
 * regular expressions to improve performance.
 * @author Michael Angstadt
 */
public class CharacterBitSet {
	private final BitSet bitSet = new BitSet(128);
	private final String characters;

	/**
	 * @param characters the list of characters to check for. Ranges of
	 * characters are represented using a hyphen. Therefore, to include a raw
	 * hyphen in the character list, it must come at the very beginning or very
	 * end of the given string. In this example, "a-f_:-", the following
	 * characters are included in the final character list: lowercase letters a
	 * through f, underscores, colons, and hyphens
	 */
	public CharacterBitSet(String characters) {
		this.characters = characters;
		for (int i = 0; i < characters.length(); i++) {
			char c = characters.charAt(i);
			char next = (i < characters.length() - 2) ? characters.charAt(i + 1) : 0;

			if (next == '-') {
				char start = c;
				char end = characters.charAt(i + 2);
				if (start > end) {
					//swap them
					char temp = start;
					start = end;
					end = temp;
				}

				bitSet.set(start, end + 1);
				i += 2;
				continue;
			}

			bitSet.set(c);
		}
	}

	/**
	 * Gets the character list that was originally passed into this object.
	 * @return the character list
	 */
	public String characters() {
		return characters;
	}

	/**
	 * Gets the underlying {@link BitSet} object.
	 * @return the {@link BitSet} object
	 */
	public BitSet bitSet() {
		return bitSet;
	}

	/**
	 * Determines if the given string contains *only* the characters in this bit
	 * set.
	 * @param string the string
	 * @return true if the string contains only the specified characters, false
	 * if not
	 */
	public boolean containsOnly(String string) {
		return containsOnly(string, 0);
	}

	/**
	 * Determines if the given string contains *only* the characters in this bit
	 * set.
	 * @param string the string
	 * @param startIndex the index to start at in the string
	 * @return true if the string contains only the specified characters, false
	 * if not
	 */
	public boolean containsOnly(String string, int startIndex) {
		for (int i = startIndex; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!bitSet.get(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines if the given string contains at least one of the characters in
	 * this bit set.
	 * @param string the string
	 * @return true if the string contains at least one of the characters, false
	 * if not
	 */
	public boolean containsAny(String string) {
		return containsAny(string, 0);
	}

	/**
	 * Determines if the given string contains at least one of the characters in
	 * this bit set.
	 * @param string the string
	 * @param startIndex the index to start at in the string
	 * @return true if the string contains at least one of the characters, false
	 * if not
	 */
	public boolean containsAny(String string, int startIndex) {
		for (int i = startIndex; i < string.length(); i++) {
			char c = string.charAt(i);
			if (bitSet.get(c)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return bitSet.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CharacterBitSet other = (CharacterBitSet) obj;
		return bitSet.equals(other.bitSet);
	}

	@Override
	public String toString() {
		return characters;
	}
}
