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
 * Specifies which mail client the vCard is coming from or is destined for.
 * @author George El-Haddad
 */
public enum CompatibilityMode {

	/**
	 * <p>
	 * Pure RFC-2426 compatibility.
	 * </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	RFC2426,

	/**
	 * <p>
	 * <ol>
	 * <li>The parameter type &quot;WORK&quot; is added to the URL feature.</li>
	 * <li>Encoding parameter type uses BASE64 instead of B.</li>
	 * <li>Base64 encoded text start on a new line and end with a new line (v2.1
	 * style)</li>
	 * <li>Compensates for missing mandatory semi-colons in name (N type) when
	 * parsing.</li>
	 * <li>Compensates for missing = sign delimiting parameter types in PHOTO,
	 * LOGO, SOUND and KEY types when parsing.</li>
	 * </ol>
	 * </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	MS_OUTLOOK,

	/**
	 * <p>
	 * iPhone exported vcards
	 * <ol>
	 * <li>Encoding parameter type uses BASE64 instead of B.</li>
	 * <li>Compensates for missing = sign delimiting parameter types in PHOTO,
	 * LOGO, SOUND and KEY types when parsing.</li>
	 * <li>Parameter types inside the URL feature.</li>
	 * <li>Escapes special characters in URL feature.</li>
	 * </ol>
	 * </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	I_PHONE,

	/**
	 * <p>
	 * <ol>
	 * <li>Does not include the ENCODING parameter type for PHOTO, LOGO, SOUND
	 * or KEY.</li>
	 * <li>Compensates for missing = sign delimiting parameter types in PHOTO,
	 * LOGO, SOUND and KEY types when parsing.</li>
	 * </ol>
	 * </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	MAC_ADDRESS_BOOK,

	/**
	 * <p>
	 * Compatibility mode for use with the KDE Address Book application.
	 * <ol>
	 * <li>Uses escaped commas in the CATEGORIES feature when there is more than
	 * one category. The RFC-2426 explicitly states &quot;One or more text
	 * values separated by a COMMA character&quot;</li>
	 * <li>Escapes commas in CATEGORIES feature when writing vcard.</li>
	 * </ol>
	 * </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	KDE_ADDRESS_BOOK,

	/**
	 * <p>
	 * Compatibility mode to emulate iOS Exporter's quirks
	 * <ol>
	 * <li>Escapes special characters in the TEL type value.
	 * </p>
	 * <li>Escapes special characters in URL feature.</li> </ol> </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	IOS_EXPORTER,

	/**
	 * <p>
	 * <ol>
	 * <li></li>
	 * </ol>
	 * </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	EVOLUTION,

	/**
	 * <p>
	 * <ol>
	 * <li>Parameter types inside the URL feature.</li>
	 * <li>Escapes special characters in URL feature.</li>
	 * </ol>
	 * </p>
	 * 
	 * @see BinaryFoldingScheme
	 * @see FoldingScheme
	 */
	GMAIL;
}
