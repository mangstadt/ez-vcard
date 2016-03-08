package ezvcard.util;

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
 * Wraps a {@link StringBuilder} object, providing utility methods for clearing
 * it.
 */
public class ClearableStringBuilder {
	private final StringBuilder sb = new StringBuilder();

	/**
	 * Clears the buffer.
	 * @return this
	 */
	public ClearableStringBuilder clear() {
		sb.setLength(0);
		return this;
	}

	/**
	 * Gets the buffer's contents.
	 * @return the buffer's contents
	 */
	public String get() {
		return sb.toString();
	}

	/**
	 * Gets the buffer's contents, then clears it.
	 * @return the buffer's contents
	 */
	public String getAndClear() {
		String string = get();
		clear();
		return string;
	}

	/**
	 * Appends a character to the buffer.
	 * @param ch the character to append
	 * @return this
	 */
	public ClearableStringBuilder append(char ch) {
		sb.append(ch);
		return this;
	}

	/**
	 * Appends a character sequence to the buffer.
	 * @param string the character sequence to append
	 * @return this
	 */
	public ClearableStringBuilder append(CharSequence string) {
		sb.append(string);
		return this;
	}

	/**
	 * Appends a character array to the buffer.
	 * @param buffer the characters to append
	 * @param start the index of the first char to append
	 * @param length the number of chars to append
	 * @return this
	 */
	public ClearableStringBuilder append(char[] buffer, int start, int length) {
		sb.append(buffer, start, length);
		return this;
	}

	/**
	 * Removes the last character from the buffer.
	 * @return this
	 */
	public ClearableStringBuilder chop() {
		sb.setLength(sb.length() - 1);
		return this;
	}

	/**
	 * Gets the length of the buffer.
	 * @return the buffer's length
	 */
	public int length() {
		return sb.length();
	}
}
