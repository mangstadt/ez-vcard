package ezvcard.property;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import ezvcard.VCardVersion;
import ezvcard.util.PartialDate;

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
 * Defines the person's anniversary.
 * 
 * <p>
 * <b>Setting the anniversary</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //complete date
 * Calendar c = Calendar.getInstance();
 * c.set(Calendar.YEAR, 1986);
 * c.set(Calendar.MONTH, Calendar.MARCH);
 * c.set(Calendar.DAY_OF_MONTH, 21);
 * Anniversary anniversary = new Anniversary();
 * anniversary.setDate(c.getTime(), false);
 * vcard.setAnniversary(anniversary);
 * 
 * //reduced accuracy date (see RFC 6350 p.12-14 for examples)
 * anniversary = new Anniversary();
 * anniversary.setPartialDate(PartialDate.date(null, 3, 21)); //March 21
 * vcard.setAnniversary(anniversary);
 * 
 * //plain text value
 * anniversary = new Anniversary();
 * anniversary.setText(&quot;more than 20 years ago&quot;);
 * vcard.setAnniversary(anniversary);
 * </pre>
 * 
 * <p>
 * <b>Getting the anniversary</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Anniversary anniversary = vcard.getAnniversary();
 * if (anniversary != null){
 *   if (anniversary.getDate() != null){
 *     System.out.println(anniversary.getDate());
 *   } else if (anniversary.getPartialDate() != null){
 *     System.out.println("Year: " + anniversary.getPartialDate().getYear());
 *     System.out.println("Month: " + anniversary.getPartialDate().getMonth());
 *     //...
 *   } else if (anniversary.getText() != null){
 *     System.out.println(anniversary.getText());
 *   }
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
 */
public class Anniversary extends DateOrTimeProperty {
	/**
	 * Creates an anniversary property.
	 * @param date the anniversary date
	 */
	public Anniversary(Date date) {
		super(date);
	}

	/**
	 * Creates an anniversary property.
	 * @param date the anniversary date
	 * @param hasTime true to include the date's time component, false if it's
	 * strictly a date
	 */
	public Anniversary(Date date, boolean hasTime) {
		super(date, hasTime);
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

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V4_0);
	}
}
