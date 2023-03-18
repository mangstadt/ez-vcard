package ezvcard.property;

import ezvcard.SupportedVersions;
import ezvcard.VCardVersion;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * <p>
 * Defines the sensitivity of the information in the vCard.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Classification classification = new Classification("PUBLIC");
 * vcard.setClassification(classification);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code CLASS}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 3.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-26">RFC 2426 p.26</a>
 */
@SupportedVersions(VCardVersion.V3_0)
public class Classification extends TextProperty {
	/**
	 * Creates a classification property.
	 * @param classValue the classification (e.g. "PUBLIC", "PRIVATE",
	 * "CONFIDENTIAL")
	 */
	public Classification(String classValue) {
		super(classValue);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Classification(Classification original) {
		super(original);
	}

	@Override
	public Classification copy() {
		return new Classification(this);
	}
}
