package ezvcard.property;

import java.util.Calendar;
import java.util.Date;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
 * Defines the date that the vCard was last modified by its owner.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Revision rev = new Revision(new Date());
 * vcard.setRevision(rev);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code REV}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-45">RFC 6350 p.45</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-22">RFC 2426 p.22</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
 */
public class Revision extends SimpleProperty<Date> {
	private Calendar calendar;

	/**
	 * Creates a revision property.
	 * @param date the date the vCard was last modified
	 */
	public Revision(Date date) {
		super(date);
	}

	/**
	 * Creates a revision property.
	 * @param calendar the date the vCard was last modified
	 */
	public Revision(Calendar calendar) {
		this(calendar.getTime());
		this.calendar = calendar;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Revision(Revision original) {
		super(original);
		value = (original.value == null) ? null : new Date(original.value.getTime());
		calendar = (original.calendar == null) ? null : (Calendar) original.calendar.clone();
	}

	/**
	 * <p>
	 * Gets the value of this property as a {@link Calendar} object. This is
	 * useful for retrieving the components of the original timestamp string
	 * from the source vCard data.
	 * </p>
	 * <p>
	 * Use {@link Calendar#isSet} to determine if a field was included in the
	 * original timestamp string. Calls to this method should be made before
	 * calling {@link Calendar#get} because calling latter method can cause
	 * unset fields to become populated (as mentioned in the
	 * {@link Calendar#isSet isSet} Javadocs).
	 * </p>
	 * <p>
	 * The calendar's timezone will be set to "GMT" if the "Z" suffix was used
	 * in the timestamp string. If a numeric offset was used, the timezone will
	 * look like "GMT-05:00". If no offset was specified, the timezone will be
	 * set to the local system's default timezone.
	 * </p>
	 * @return the value or null if not set
	 */
	public Calendar getCalendar() {
		return (calendar == null) ? null : (Calendar) calendar.clone();
	}

	/**
	 * Sets the value of this property.
	 * @param calendar the value
	 */
	public void setValue(Calendar calendar) {
		setValue(calendar.getTime());
		this.calendar = calendar;
	}

	/**
	 * Creates a revision property whose value is the current time.
	 * @return the property
	 */
	public static Revision now() {
		return new Revision(new Date());
	}

	@Override
	public Revision copy() {
		return new Revision(this);
	}
}
