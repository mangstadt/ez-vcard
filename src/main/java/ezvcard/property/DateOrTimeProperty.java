package ezvcard.property;

import java.util.Date;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.parameter.Calscale;
import ezvcard.parameter.VCardParameters;
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
 * Represents a property with a date and/or time value.
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DateOrTimeProperty extends VCardProperty implements HasAltId {
	private String text;
	private Date date;
	private PartialDate partialDate;
	private boolean dateHasTime;

	/**
	 * Creates a date-and-or-time property.
	 * @param date the date value
	 */
	public DateOrTimeProperty(Date date) {
		this(date, false);
	}

	/**
	 * Creates a date-and-or-time property.
	 * @param date the date value
	 * @param hasTime true to include the date's time component, false if it's
	 * strictly a date
	 */
	public DateOrTimeProperty(Date date, boolean hasTime) {
		setDate(date, hasTime);
	}

	/**
	 * Creates a date-and-or-time property.
	 * @param partialDate the partial date value (vCard 4.0 only)
	 */
	public DateOrTimeProperty(PartialDate partialDate) {
		setPartialDate(partialDate);
	}

	/**
	 * Creates a date-and-or-time property.
	 * @param text the text value (vCard 4.0 only)
	 */
	public DateOrTimeProperty(String text) {
		setText(text);
	}

	/**
	 * Gets the date value.
	 * @return the date value or null if not set
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the value of this property to a complete date.
	 * @param date the date
	 * @param hasTime true to include the date's time component, false if it's
	 * strictly a date
	 */
	public void setDate(Date date, boolean hasTime) {
		this.date = date;
		this.dateHasTime = (date == null) ? false : hasTime;
		text = null;
		partialDate = null;
	}

	/**
	 * Gets the reduced accuracy or truncated date. This is only supported by
	 * vCard 4.0.
	 * @return the reduced accuracy or truncated date or null if not set
	 * @see "<a href="
	 * http://tools.ietf.org/html/rfc6350">RFC 6350</a> p.12-14 for examples"
	 */
	public PartialDate getPartialDate() {
		return partialDate;
	}

	/**
	 * <p>
	 * Sets the value of this property to a reduced accuracy or truncated date.
	 * This is only supported by vCard 4.0.
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * Birthday bday = new Birthday();
	 * bday.setPartialDate(PartialDate.date(null, 4, 20)); //April 20
	 * </pre>
	 * @param partialDate the reduced accuracy or truncated date
	 * @see "<a href="
	 * http://tools.ietf.org/html/rfc6350">RFC 6350</a> p.12-14 for examples"
	 */
	public void setPartialDate(PartialDate partialDate) {
		this.partialDate = partialDate;
		dateHasTime = (partialDate == null) ? false : partialDate.hasTimeComponent();
		text = null;
		date = null;
	}

	/**
	 * Gets the text value of this type. This is only supported by vCard 4.0.
	 * @return the text value or null if not set
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the value of this property to a text string. This is only supported
	 * by vCard 4.0.
	 * @param text the text value
	 */
	public void setText(String text) {
		this.text = text;
		date = null;
		partialDate = null;
		dateHasTime = false;
	}

	/**
	 * Determines whether the "date" or "partialDate" fields have a time
	 * component.
	 * @return true if the date has a time component, false if it's strictly a
	 * date, and false if a text value is defined
	 */
	public boolean hasTime() {
		return dateHasTime;
	}

	/**
	 * <p>
	 * Gets the type of calendar that is used for a date or date-time property
	 * value.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the type of calendar or null if not found
	 * @see VCardParameters#getCalscale
	 */
	public Calscale getCalscale() {
		return parameters.getCalscale();
	}

	/**
	 * <p>
	 * Sets the type of calendar that is used for a date or date-time property
	 * value.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param calscale the type of calendar or null to remove
	 * @see VCardParameters#setCalscale
	 */
	public void setCalscale(Calscale calscale) {
		parameters.setCalscale(calscale);
	}

	//@Override
	public String getAltId() {
		return parameters.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		parameters.setAltId(altId);
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (date == null && partialDate == null && text == null) {
			warnings.add(new Warning(8));
		}

		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			if (text != null) {
				warnings.add(new Warning(11));
			}
			if (partialDate != null) {
				warnings.add(new Warning(12));
			}
		}
	}
}