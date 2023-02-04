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
 * Defines the person's time of death.
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
 * LocalDate date = LocalDate.of(1954, 6, 7);
 * Deathdate deathdate = new Deathdate(date);
 * vcard.setDeathdate(deathdate);
 * 
 * //partial date (e.g. just the month and date)
 * deathdate = new Deathdate(PartialDate.date(null, 6, 7)); //June 7
 * vcard.setDeathdate(deathdate);
 * 
 * //plain text value
 * deathdate = new Deathdate("In the 1950s");
 * vcard.setDeathdate(deathdate);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Deathdate deathdate = vcard.getDeathdate();
 * 
 * Temporal date = deathdate.getDate();
 * if (date != null) {
 *   //property value is a date
 * }
 * 
 * PartialDate partialDate = deathdate.getPartialDate();
 * if (partialDate != null) {
 *   //property value is a partial date
 *   int year = partialDate.getYear();
 *   int month = partialDate.getMonth();
 * }
 * 
 * String text = deathdate.getText();
 * if (text != null) {
 *   //property value is plain text
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code DEATHDATE}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6474#page-4">RFC 6474 p.4</a>
 */
@SupportedVersions(VCardVersion.V4_0)
public class Deathdate extends DateOrTimeProperty {
	/**
	 * Creates a deathdate property.
	 * @param date the deathdate
	 */
	public Deathdate(Temporal date) {
		super(date);
	}

	/**
	 * Creates a deathdate property.
	 * @param partialDate the deathdate (vCard 4.0 only)
	 */
	public Deathdate(PartialDate partialDate) {
		super(partialDate);
	}

	/**
	 * Creates a deathdate property.
	 * @param text the text value (vCard 4.0 only)
	 */
	public Deathdate(String text) {
		super(text);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Deathdate(Deathdate original) {
		super(original);
	}

	@Override
	public Deathdate copy() {
		return new Deathdate(this);
	}
}
