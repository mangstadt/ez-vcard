package ezvcard.parameters;

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

/*
 Copyright (c) 2013, Michael Angstadt
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
 * Represents the "ENCODING" sub type.
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0</code>
 * </p>
 * @author George El-Haddad Mar 10, 2010
 * @author Michael Angstadt
 */
public class EncodingParameter extends VCardParameter {
	public static final String NAME = "ENCODING";

	/**
	 * <b>Supported versions:</b> <code>2.1</code>
	 */
	public static final EncodingParameter QUOTED_PRINTABLE = new EncodingParameter("quoted-printable");

	/**
	 * <b>Supported versions:</b> <code>2.1</code>
	 */
	public static final EncodingParameter BASE64 = new EncodingParameter("base64");

	/**
	 * <b>Supported versions:</b> <code>2.1</code>
	 */
	public static final EncodingParameter _8BIT = new EncodingParameter("8bit");

	/**
	 * <b>Supported versions:</b> <code>2.1</code>
	 */
	public static final EncodingParameter _7BIT = new EncodingParameter("7bit");

	/**
	 * <b>Supported versions:</b> <code>3.0</code>
	 */
	public static final EncodingParameter B = new EncodingParameter("b");

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard ENCODINGs. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "b")
	 */
	public EncodingParameter(String value) {
		super(NAME, value);
	}

	/**
	 * Searches the static objects in this class for one that has a certain type
	 * value.
	 * @param value the type value to search for (e.g. "b")
	 * @return the object or null if not found
	 */
	public static EncodingParameter valueOf(String value) {
		return findByValue(value, EncodingParameter.class);
	}
}
