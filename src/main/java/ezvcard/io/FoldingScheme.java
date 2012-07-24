package ezvcard.io;

/**
 * Copyright 2011 George El-Haddad. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of George El-Haddad.
 */

/**
 * Specifies how a vCard should be folded when written to a string.
 * @author George El-Haddad
 */
public class FoldingScheme {
	/**
	 * <p>
	 * Folds lines at 75 characters (not including CRLF) and uses 1 space as
	 * indentation.
	 * </p>
	 * @see "RFC2426 p.8"
	 */
	public static final FoldingScheme MIME_DIR = new FoldingScheme(75, " ");

	/**
	 * <p>
	 * Folds lines at 72 characters (not including CRLF) and uses 1 space as
	 * indentation.
	 * </p>
	 */
	public static final FoldingScheme MS_OUTLOOK = new FoldingScheme(72, " ");

	/**
	 * <p>
	 * Folds lines at 76 characters (not including CRLF) and uses 2 spaces as
	 * indentation.
	 * </p>
	 */
	public static final FoldingScheme MAC_ADDRESS_BOOK = new FoldingScheme(76, "  ");

	private final int maxChars;
	private final String indent;

	public FoldingScheme(int _maxChars, String _indent) {
		if (_maxChars <= 0) {
			throw new IllegalArgumentException("The max line length must be greater than 0.");
		}
		if (_indent.length() > _maxChars) {
			throw new IllegalArgumentException("The max line length must be greater than the length of the indentation string.");
		}
		maxChars = _maxChars;
		indent = _indent;
	}

	/**
	 * Gets the maximum number of characters that can exist on a line before
	 * needing to be folded.
	 * @return the max line length
	 */
	public int getMaxChars() {
		return maxChars;
	}

	/**
	 * Returns the string that is used to indent the folded line.
	 * @return the indent string
	 */
	public String getIndent() {
		return indent;
	}
}
