package ezvcard.io.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonToken;

import ezvcard.Messages;

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
 * Thrown during the parsing of a jCard, when a jCard is not formatted in the
 * correct way (the JSON syntax is valid, but it's not in the correct jCard
 * format).
 * @author Michael Angstadt
 */
public class JCardParseException extends IOException {
	private static final long serialVersionUID = 5139480815617303404L;
	private final JsonToken expected, actual;

	/**
	 * Creates a jCard parse exception.
	 * @param expected the JSON token that the parser was expecting
	 * @param actual the actual JSON token
	 */
	public JCardParseException(JsonToken expected, JsonToken actual) {
		super(Messages.INSTANCE.getExceptionMessage(35, expected, actual));
		this.expected = expected;
		this.actual = actual;
	}

	/**
	 * Creates a jCard parse exception.
	 * @param message the detail message
	 * @param expected the JSON token that the parser was expecting
	 * @param actual the actual JSON token
	 */
	public JCardParseException(String message, JsonToken expected, JsonToken actual) {
		super(message);
		this.expected = expected;
		this.actual = actual;
	}

	/**
	 * Gets the JSON token that the parser was expected.
	 * @return the expected token
	 */
	public JsonToken getExpectedToken() {
		return expected;
	}

	/**
	 * Gets the JSON token that was read.
	 * @return the actual token
	 */
	public JsonToken getActualToken() {
		return actual;
	}
}
