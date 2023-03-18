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
 * Identifies the software application that created the vCard.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * ProductId prodid = new ProductId("ez-vcard 0.6.0");
 * vcard.setProdId(prodid);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code PRODID}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350 p.44</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426 p.21</a>
 */
@SupportedVersions({ VCardVersion.V3_0, VCardVersion.V4_0 })
public class ProductId extends TextProperty {
	/**
	 * Creates a product ID property.
	 * @param prodId the prod ID
	 */
	public ProductId(String prodId) {
		super(prodId);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public ProductId(ProductId original) {
		super(original);
	}

	@Override
	public ProductId copy() {
		return new ProductId(this);
	}
}
