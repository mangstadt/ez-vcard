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
	 */
	RFC2426,

	MS_OUTLOOK,

	I_PHONE,

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
	 */
	KDE_ADDRESS_BOOK,

	IOS_EXPORTER,

	EVOLUTION,

	GMAIL;
}
