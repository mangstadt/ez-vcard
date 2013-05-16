package ezvcard.types;

import java.util.Date;

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
 * Defines the person's birthday.
 * 
 * <p>
 * <b>Setting the birthday</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * //complete date
 * Calendar c = Calendar.getInstance();
 * c.set(Calendar.YEAR, 1986);
 * c.set(Calendar.MONTH, Calendar.MARCH);
 * c.set(Calendar.DAY_OF_MONTH, 21);
 * BirthdayType bday = new BirthdayType();
 * bday.setDate(c.getTime(), false);
 * vcard.setBirthday(bday);
 * 
 * //reduced accuracy date (vCard 4.0 only, see RFC 6350 p.12-14 for examples)
 * bday = new BirthdayType();
 * bday.setPartialDate(PartialDate.date(null, 3, 21)); //March 21
 * vcard.setBirthday(bday);
 * 
 * //plain text value (vCard 4.0 only)
 * bday = new BirthdayType();
 * bday.setText(&quot;a long time ago&quot;);
 * vcard.setBirthday(bday);
 * </pre>
 * 
 * <p>
 * <b>Getting the birthday</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = ...
 * BirthdayType bday = vcard.getBirthday();
 * if (bday != null){
 *   if (bday.getDate() != null){
 *     System.out.println(bday.getDate());
 *   } else if (bday.getPartialDate() != null){
 *     System.out.println("Year: " + bday.getPartialDate().getYear());
 *     System.out.println("Month: " + bday.getPartialDate().getMonth());
 *     //...
 *   } else if (bday.getText() != null){
 *     System.out.println(bday.getText());
 *   }
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>BDAY</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class BirthdayType extends DateOrTimeType {
	public static final String NAME = "BDAY";

	public BirthdayType() {
		super(NAME);
	}

	/**
	 * @param date the birthday
	 */
	public BirthdayType(Date date) {
		super(NAME, date);
	}
}
