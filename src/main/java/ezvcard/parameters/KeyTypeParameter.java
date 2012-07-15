package ezvcard.parameters;

import ezvcard.util.ParameterUtils;

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
 * Represents the TYPE parameter of the KEY type.
 * @author George El-Haddad Mar 10, 2010
 * @author Michael Angstadt Jul 06, 2012
 */
public class KeyTypeParameter extends TypeParameter {
	public static final KeyTypeParameter PGP = new KeyTypeParameter("PGP", "PGP", "pgp");
	public static final KeyTypeParameter GPG = new KeyTypeParameter("GPG", "GPG", "gpg");
	public static final KeyTypeParameter X509 = new KeyTypeParameter("X509", "X509", "");
	public static final KeyTypeParameter B = new KeyTypeParameter("B", "B", "");

	private final String ianaRegisteredName;
	private final String extension;

	public KeyTypeParameter(String _typeName) {
		this(_typeName, null, null);
	}

	/**
	 * Use of this constructor is discouraged. Please use one of the predefined
	 * static objects.
	 * @param _typeName the type name (e.g. "PGP")
	 * @param _ianaRegisteredName the IANA registered name (e.g. "PGP")
	 * @param _extension the file extension used for this type (e.g. "pgp")
	 */
	public KeyTypeParameter(String _typeName, String _ianaRegisteredName, String _extension) {
		super(_typeName);
		ianaRegisteredName = _ianaRegisteredName;
		extension = _extension;
	}

	public String getIanaRegisteredName() {
		return ianaRegisteredName;
	}

	public String getExtension() {
		return extension;
	}

	/**
	 * Retrieves one of the static objects in this class by name.
	 * @param typeName the type name (e.g. "PGP")
	 * @return the object associated with the given type name or null if none
	 * was found
	 */
	public static KeyTypeParameter valueOf(String typeName) {
		return ParameterUtils.valueOf(KeyTypeParameter.class, typeName);
	}
}
