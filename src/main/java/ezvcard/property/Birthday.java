package ezvcard.property;

import java.util.Date;

import ezvcard.util.PartialDate;
import lombok.*;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Defines the person's birthday.
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
 * Calendar c = Calendar.getInstance();
 * c.clear();
 * c.set(Calendar.YEAR, 1912);
 * c.set(Calendar.MONTH, Calendar.JUNE);
 * c.set(Calendar.DAY_OF_MONTH, 23);
 * Birthday bday = new Birthday(c.getTime());
 * vcard.setBirthday(bday);
 * 
 * //partial date (e.g. just the month and date, vCard 4.0 only)
 * bday = new Birthday(PartialDate.date(null, 6, 23)); //June 23
 * vcard.setBirthday(bday);
 * 
 * //plain text value (vCard 4.0 only)
 * bday = new Birthday(&quot;Don't even go there, dude...&quot;);
 * vcard.setBirthday(bday);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Birthday bday = vcard.getBirthday();
 * 
 * Date date = bday.getDate();
 * if (date != null){
 *   //property value is a date
 * }
 * 
 * PartialDate partialDate = bday.getPartialDate();
 * if (partialDate != null){
 *   //property value is a partial date
 *   int year = partialDate.getYear();
 *   int month = partialDate.getMonth();
 * }
 * 
 * String text = bday.getText();
 * if (text != null){
 *   //property value is plain text
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code BDAY}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Birthday extends DateOrTimeProperty {
	/**
	 * Creates a birthday property.
	 * @param date the birthday
	 */
	public Birthday(Date date) {
		super(date);
	}

	/**
	 * Creates a birthday property.
	 * @param date the birthday
	 * @param hasTime true to include the date's time component, false if it's
	 * strictly a date
	 */
	public Birthday(Date date, boolean hasTime) {
		super(date, hasTime);
	}

	/**
	 * Creates a birthday property.
	 * @param partialDate the birthday (vCard 4.0 only)
	 */
	public Birthday(PartialDate partialDate) {
		super(partialDate);
	}

	/**
	 * Creates a birthday property.
	 * @param text the text value (vCard 4.0 only)
	 */
	public Birthday(String text) {
		super(text);
	}
}
