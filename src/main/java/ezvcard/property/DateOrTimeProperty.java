package ezvcard.property;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.Calscale;
import ezvcard.parameter.VCardParameters;
import ezvcard.util.PartialDate;
import ezvcard.util.VCardDateFormat;

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
 * Represents a property with a date and/or time value.
 * @author Michael Angstadt
 */
public class DateOrTimeProperty extends VCardProperty implements HasAltId {
	private String text;
	private Calendar date;
	private PartialDate partialDate;
	private boolean dateHasTime;

	/**
	 * Creates a date-and-or-time property.
	 * @param date the date value
	 */
	public DateOrTimeProperty(Date date) {
		this(VCardDateFormat.toCalendar(date));
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
	 * @param date the date value
	 */
	public DateOrTimeProperty(Calendar date) {
		this(date, false);
	}

	/**
	 * Creates a date-and-or-time property.
	 * @param date the date value
	 * @param hasTime true to include the date's time component, false if it's
	 * strictly a date
	 */
	public DateOrTimeProperty(Calendar date, boolean hasTime) {
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
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public DateOrTimeProperty(DateOrTimeProperty original) {
		super(original);
		text = original.text;
		date = (original.date == null) ? null : (Calendar) original.date.clone();
		partialDate = original.partialDate;
		dateHasTime = original.dateHasTime;
	}

	/**
	 * Gets the date value.
	 * @return the date value or null if not set
	 */
	public Date getDate() {
		return (date == null) ? null : date.getTime();
	}

	/**
	 * Sets the value of this property to a complete date.
	 * @param date the date
	 * @param hasTime true to include the date's time component, false if it's
	 * strictly a date
	 */
	public void setDate(Date date, boolean hasTime) {
		setDate(VCardDateFormat.toCalendar(date), hasTime);
	}

	/**
	 * Sets the value of this property to a complete date.
	 * @param date the date
	 * @param hasTime true to include the date's time component, false if it's
	 * strictly a date
	 */
	public void setDate(Calendar date, boolean hasTime) {
		this.date = date;
		this.dateHasTime = (date != null) && hasTime;
		text = null;
		partialDate = null;
	}

	/**
	 * <p>
	 * Gets the date value as a {@link Calendar} object. This is useful for
	 * retrieving the components of the original timestamp string from the
	 * source vCard data.
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
	 * @return the date value or null if not set
	 */
	public Calendar getCalendar() {
		return (date == null) ? null : (Calendar) date.clone();
	}

	/**
	 * Gets the reduced accuracy or truncated date. This is only supported by
	 * vCard 4.0.
	 * @return the reduced accuracy or truncated date or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 p.12-14</a>
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
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 p.12-14</a>
	 */
	public void setPartialDate(PartialDate partialDate) {
		this.partialDate = partialDate;
		dateHasTime = (partialDate != null) && partialDate.hasTimeComponent();
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

	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
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
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		if (date == null && partialDate == null && text == null) {
			warnings.add(new ValidationWarning(8));
		}

		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			if (text != null) {
				warnings.add(new ValidationWarning(11));
			}
			if (partialDate != null) {
				warnings.add(new ValidationWarning(12));
			}
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("text", text);
		values.put("date", getDate());
		values.put("dateHasTime", dateHasTime);
		values.put("partialDate", partialDate);
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();

		/*
		 * Call getDate() method to compare the actual timestamp of the calendar
		 * object.
		 */
		result = prime * result + ((getDate() == null) ? 0 : getDate().hashCode());

		result = prime * result + (dateHasTime ? 1231 : 1237);
		result = prime * result + ((partialDate == null) ? 0 : partialDate.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		DateOrTimeProperty other = (DateOrTimeProperty) obj;

		/*
		 * Call getDate() method to compare the actual timestamp of the calendar
		 * object.
		 */
		if (getDate() == null) {
			if (other.getDate() != null) return false;
		} else if (!getDate().equals(other.getDate())) return false;

		if (dateHasTime != other.dateHasTime) return false;
		if (partialDate == null) {
			if (other.partialDate != null) return false;
		} else if (!partialDate.equals(other.partialDate)) return false;
		if (text == null) {
			if (other.text != null) return false;
		} else if (!text.equals(other.text)) return false;
		return true;
	}
}