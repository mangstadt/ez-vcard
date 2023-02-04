package ezvcard.property;

import java.time.temporal.Temporal;

import ezvcard.SupportedVersions;
import ezvcard.VCardVersion;
import ezvcard.util.PartialDate;

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
 * Defines the person's anniversary (marital or work-related).
 * </p>
 * 
 * <p>
 * <b>Code sample (creating)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //date
 * LocalDate date = LocalDate.of(1970, 3, 21);
 * Anniversary anniversary = new Anniversary(date);
 * vcard.setAnniversary(anniversary);
 * 
 * //partial date (e.g. just the month and date)
 * PartialDate date = PartialDate.date(null, 3, 21);
 * anniversary = new Anniversary(date); //March 21
 * vcard.setAnniversary(anniversary);
 * 
 * //plain text value
 * anniversary = new Anniversary("Over 20 years ago!");
 * vcard.setAnniversary(anniversary);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Anniversary anniversary = vcard.getAnniversary();
 * 
 * Temporal date = anniversary.getDate();
 * if (date != null) {
 *   //property value is a date
 * }
 * 
 * PartialDate partialDate = anniversary.getPartialDate();
 * if (partialDate != null) {
 *   //property value is a partial date
 *   int year = partialDate.getYear();
 *   int month = partialDate.getMonth();
 * }
 * 
 * String text = anniversary.getText();
 * if (text != null) {
 *   //property value is plain text
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code ANNIVERSARY}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-31">RFC 6350 p.31</a>
 */
@SupportedVersions(VCardVersion.V4_0)
public class Anniversary extends DateOrTimeProperty {
	/**
	 * Creates an anniversary property.
	 * @param date the anniversary date
	 */
	public Anniversary(Temporal date) {
		super(date);
	}

	/**
	 * Creates an anniversary property.
	 * @param partialDate the partial anniversary date (vCard 4.0 only)
	 */
	public Anniversary(PartialDate partialDate) {
		super(partialDate);
	}

	/**
	 * Creates an anniversary property.
	 * @param text the text value (vCard 4.0 only)
	 */
	public Anniversary(String text) {
		super(text);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Anniversary(Anniversary original) {
		super(original);
	}

	@Override
	public Anniversary copy() {
		return new Anniversary(this);
	}
}
