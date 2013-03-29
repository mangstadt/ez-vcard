package ezvcard.parameters;

import ezvcard.util.ParameterUtils;

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
 * Represents a VALUE parameter.
 * @author Michael Angstadt
 */
public class ValueParameter {
	public static final String NAME = "VALUE";

	/**
	 * vCard 2.1 defines a "VALUE=url" parameter (p.18-9).
	 */
	public static final ValueParameter URL = new ValueParameter("url");

	/**
	 * vCard 3.0 defines a "VALUE=uri" parameter (p.11,19,24).
	 */
	public static final ValueParameter URI = new ValueParameter("uri");

	public static final ValueParameter TEXT = new ValueParameter("text");

	/**
	 * vCard v2.1 defines a "VALUE=content-id" parameter (p.8-9)
	 */
	public static final ValueParameter CONTENT_ID = new ValueParameter("content-id");

	private final String value;

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard VALUEs. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "uri")
	 */
	public ValueParameter(String value) {
		this.value = value.toLowerCase();
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		ValueParameter that = (ValueParameter) obj;
		return value.equals(that.value);
	}

	/**
	 * Retrieves one of the static objects in this class by name.
	 * @param value the type value (e.g. "home")
	 * @return the object associated with the given type name or null if none
	 * was found
	 */
	public static ValueParameter valueOf(String value) {
		return ParameterUtils.valueOf(ValueParameter.class, value);
	}
}
