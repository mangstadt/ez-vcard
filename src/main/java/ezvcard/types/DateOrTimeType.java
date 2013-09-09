package ezvcard.types;

import java.util.Date;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.CalscaleParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.ISOFormat;
import ezvcard.util.JCardValue;
import ezvcard.util.PartialDate;
import ezvcard.util.VCardDateFormatter;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;

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
 * Represents a property whose value contains a date and/or a time (for example,
 * {@link BirthdayType}).
 * @author Michael Angstadt
 */
public class DateOrTimeType extends VCardType implements HasAltId {
	private String text;
	private Date date;
	private PartialDate partialDate;
	private boolean dateHasTime;

	/**
	 * Creates a date-and-or-time property.
	 * @param typeName the name of the type (e.g. "BDAY")
	 */
	public DateOrTimeType(String typeName) {
		super(typeName);
	}

	/**
	 * Creates a date-and-or-time property.
	 * @param typeName the name of the type (e.g. "BDAY")
	 * @param date the date value
	 */
	public DateOrTimeType(String typeName, Date date) {
		super(typeName);
		setDate(date, false);
	}

	/**
	 * Creates a date-and-or-time property.
	 * @param typeName the name of the type (e.g. "BDAY")
	 * @param partialDate the date value
	 */
	public DateOrTimeType(String typeName, PartialDate partialDate) {
		super(typeName);
		setPartialDate(partialDate);
	}

	/**
	 * Gets the date value.
	 * @return the date value or null if not set
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the value of this type to a complete date.
	 * @param date the date
	 * @param dateHasTime true if the date contains a time component, false if
	 * it's just a date
	 */
	public void setDate(Date date, boolean dateHasTime) {
		this.date = date;
		this.dateHasTime = dateHasTime;
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
	 * Sets the value of this type to a reduced accuracy or truncated date. This
	 * is only supported by vCard 4.0.
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * BirthdayType bday = new BirthdayType();
	 * bday.setPartialDate(PartialDate.date(null, 4, 20)); //April 20
	 * </pre>
	 * @param partialDate the reduced accuracy or truncated date
	 * @see "<a href="
	 * http://tools.ietf.org/html/rfc6350">RFC 6350</a> p.12-14 for examples"
	 */
	public void setPartialDate(PartialDate partialDate) {
		this.partialDate = partialDate;
		dateHasTime = partialDate.hasTimeComponent();
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
	 * Sets the value of this type to a text string. This is only supported by
	 * vCard 4.0.
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
	 * @see VCardSubTypes#getCalscale
	 */
	public CalscaleParameter getCalscale() {
		return subTypes.getCalscale();
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
	 * @see VCardSubTypes#setCalscale
	 */
	public void setCalscale(CalscaleParameter calscale) {
		subTypes.setCalscale(calscale);
	}

	//@Override
	public String getAltId() {
		return subTypes.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, CompatibilityMode compatibilityMode, VCard vcard) {
		VCardDataType dataType = null;
		if (text != null && version == VCardVersion.V4_0) {
			dataType = VCardDataType.TEXT;
		}
		copy.setValue(dataType);
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		if (date != null) {
			ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
			sb.append(VCardDateFormatter.format(date, format));
			return;
		}

		if (version == VCardVersion.V4_0) {
			if (text != null) {
				sb.append(VCardStringUtils.escape(text));
				return;
			}
			if (partialDate != null) {
				sb.append(partialDate.toDateAndOrTime(false));
				return;
			}
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		if (version == VCardVersion.V4_0 && subTypes.getValue() == VCardDataType.TEXT) {
			setText(value);
			return;
		}

		parseDate(value, version, warnings);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		if (date != null) {
			ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
			String value = VCardDateFormatter.format(date, format);
			VCardDataType dataType = dateHasTime ? VCardDataType.DATE_TIME : VCardDataType.DATE;

			parent.append(dataType, value);
			return;
		}

		if (partialDate != null) {
			VCardDataType dataType;
			if (partialDate.hasTimeComponent() && partialDate.hasDateComponent()) {
				dataType = VCardDataType.DATE_TIME;
			} else if (partialDate.hasTimeComponent()) {
				dataType = VCardDataType.TIME;
			} else if (partialDate.hasDateComponent()) {
				dataType = VCardDataType.DATE;
			} else {
				dataType = VCardDataType.DATE_AND_OR_TIME;
			}

			parent.append(dataType, partialDate.toDateAndOrTime(false));
			return;
		}

		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}

		parent.append(VCardDataType.DATE_AND_OR_TIME, "");
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.first(VCardDataType.DATE, VCardDataType.DATE_TIME, VCardDataType.DATE_AND_OR_TIME);
		if (value != null) {
			parseDate(value, element.version(), warnings);
			return;
		}

		value = element.first(VCardDataType.TEXT);
		if (value != null) {
			setText(value);
			return;
		}

		throw missingXmlElements(VCardDataType.DATE, VCardDataType.DATE_TIME, VCardDataType.DATE_AND_OR_TIME, VCardDataType.TEXT);
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String value = null;
		if ("time".equals(element.tagName())) {
			String datetime = element.attr("datetime");
			if (datetime.length() > 0) {
				value = datetime;
			}
		}
		if (value == null) {
			value = element.value();
		}
		parseDate(value, VCardVersion.V3_0, warnings);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		if (date != null) {
			VCardDataType dataType = dateHasTime ? VCardDataType.DATE_TIME : VCardDataType.DATE;

			ISOFormat format = dateHasTime ? ISOFormat.TIME_EXTENDED : ISOFormat.DATE_EXTENDED;
			String value = VCardDateFormatter.format(date, format);

			return JCardValue.single(dataType, value);
		}

		if (partialDate != null) {
			VCardDataType dataType;
			if (partialDate.hasTimeComponent() && partialDate.hasDateComponent()) {
				dataType = VCardDataType.DATE_TIME;
			} else if (partialDate.hasTimeComponent()) {
				dataType = VCardDataType.TIME;
			} else if (partialDate.hasDateComponent()) {
				dataType = VCardDataType.DATE;
			} else {
				dataType = VCardDataType.DATE_AND_OR_TIME;
			}

			String value = partialDate.toDateAndOrTime(true);

			return JCardValue.single(dataType, value);
		}

		if (text != null) {
			return JCardValue.single(VCardDataType.TEXT, text);
		}

		return JCardValue.single(VCardDataType.DATE_AND_OR_TIME, "");
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		if (value.getDataType() == VCardDataType.TEXT) {
			setText(valueStr);
			return;
		}

		parseDate(valueStr, version, warnings);
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (date == null && partialDate == null && text == null) {
			warnings.add("Property has no value associated with it.");
		}

		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			if (text != null) {
				warnings.add("Text values are not supported in version " + version + ".");
			}
			if (partialDate != null) {
				warnings.add("Reduced accuracy or truncated dates are not supported in version " + version + ".");
			}
		}
	}

	private void parseDate(String value, VCardVersion version, List<String> warnings) {
		try {
			boolean hasTime = value.contains("T");
			setDate(VCardDateFormatter.parse(value), hasTime);
		} catch (IllegalArgumentException e) {
			if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
				throw new CannotParseException("Date string could not be parsed.");
			}

			try {
				setPartialDate(new PartialDate(value));
			} catch (IllegalArgumentException e2) {
				warnings.add("Date string could not be parsed.  Treating it as a text value.");
				setText(value);
			}
		}
	}
}