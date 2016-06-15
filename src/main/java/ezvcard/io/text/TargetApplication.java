package ezvcard.io.text;

import ezvcard.parameter.Encoding;
import ezvcard.property.StructuredName;

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
 * Lists various vCard-consuming applications whose vCards require special
 * processing.
 * @author Michael Angstadt
 */
public enum TargetApplication {
	/**
	 * <p>
	 * iCloud
	 * </p>
	 * <ul>
	 * <li>{@link StructuredName} property values must not contain trailing
	 * semi-colons when used to define a group name (<a
	 * href="https://github.com/mangstadt/ez-vcard/issues/57">Issue 57</a>).</li>
	 * </ul>
	 */
	ICLOUD,

	/**
	 * <p>
	 * Microsoft Outlook
	 * </p>
	 * <ul>
	 * <li>An empty line is required after all base64-encoded property values
	 * for vCard versions 2.1 and 3.0 (<a
	 * href="https://github.com/mangstadt/ez-vcard/issues/21">Issue 21</a>).</li>
	 * </ul>
	 */
	OUTLOOK,

	/**
	 * <p>
	 * Windows 10 Contacts
	 * </p>
	 * <ul>
	 * <li>The {@link Encoding#QUOTED_PRINTABLE} parameter value must be in all
	 * caps (<a href="https://github.com/mangstadt/ez-vcard/issues/56">Issue
	 * 56</a>).</li>
	 * </ul>
	 */
	WINDOWS_10_CONTACTS
}
