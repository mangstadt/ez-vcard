package ezvcard;

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
 */

/**
 * Represents a warning.
 * @author Michael Angstadt
 */
public class Warning {
	private final Integer code;
	private final String message;

	/**
	 * Creates a new warning.
	 * @param message the warning message
	 */
	public Warning(String message) {
		this(message, null);
	}

	/**
	 * Creates a new warning whose message text is defined in the resource
	 * bundle.
	 * @param code the message code
	 * @param args the message arguments
	 */
	public Warning(int code, Object... args) {
		this(Messages.INSTANCE.getValidationWarning(code, args), code);
	}

	/**
	 * Creates a new warning.
	 * @param message the warning message
	 * @param code the message code
	 */
	public Warning(String message, Integer code) {
		this.code = code;
		this.message = message;
	}

	/**
	 * Gets the warning code.
	 * @return the warning code or null if no code was specified
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * Gets the warning message
	 * @return the warning message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		if (code == null) {
			return message;
		}
		return "(" + code + ") " + message;
	}
}
