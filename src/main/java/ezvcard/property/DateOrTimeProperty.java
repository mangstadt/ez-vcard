package ezvcard.property;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.Calscale;
import ezvcard.parameter.VCardParameters;
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
 * Represents a property that typically contains a date/time value, but can also
 * contain a partial date or free-text value.
 * @author Michael Angstadt
 */
public class DateOrTimeProperty extends VCardProperty implements HasAltId {
	private String text;
	private Temporal date;
	private PartialDate partialDate;

	/**
	 * Creates a date-and-or-time property.
	 * @param date the date value (should be one of the following:
	 * {@link LocalDate}, {@link LocalDateTime}, {@link OffsetDateTime},
	 * {@link Instant})
	 */
	public DateOrTimeProperty(Temporal date) {
		this.date = date;
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
		date = original.date;
		partialDate = original.partialDate;
	}

	/**
	 * Gets the date value. It should be an instance of one of the following
	 * classes: {@link LocalDate}, {@link LocalDateTime},
	 * {@link OffsetDateTime}, {@link Instant}.
	 * @return the date value or null if not set
	 */
	public Temporal getDate() {
		return date;
	}

	/**
	 * Sets the value of this property to a date. It should be an instance of
	 * one of the following classes: {@link LocalDate}, {@link LocalDateTime},
	 * {@link OffsetDateTime}, {@link Instant}.
	 * @param date the date
	 */
	public void setDate(Temporal date) {
		this.date = date;
		text = null;
		partialDate = null;
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
	 * 
	 * @param partialDate the reduced accuracy or truncated date
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 p.12-14</a>
	 */
	public void setPartialDate(PartialDate partialDate) {
		this.partialDate = partialDate;
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
	 * @param text the text value or null if not set
	 */
	public void setText(String text) {
		this.text = text;
		date = null;
		partialDate = null;
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
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("text", text);
		values.put("date", date);
		values.put("partialDate", partialDate);
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(date, partialDate, text);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		DateOrTimeProperty other = (DateOrTimeProperty) obj;
		return Objects.equals(date, other.date) && Objects.equals(partialDate, other.partialDate) && Objects.equals(text, other.text);
	}
}