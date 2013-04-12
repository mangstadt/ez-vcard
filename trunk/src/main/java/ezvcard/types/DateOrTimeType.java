package ezvcard.types;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.CalscaleParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.ISOFormat;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
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
 * Represents a type that contains a date and/or time (for example, the BDAY
 * type).
 * @author Michael Angstadt
 */
public class DateOrTimeType extends VCardType implements HasAltId {
	static final Pattern reducedAccuracyRegex = Pattern.compile("([-\\d]+T[-+:\\dZ]+)|([-\\d]+)|(T[-+:\\dZ]+)");
	private String text;
	private Date date;
	private String reducedAccuracyDate;

	/**
	 * True if the "date" or "reduceAccuracyDate" fields have a time component,
	 * false if they just contain a date.
	 */
	private boolean dateHasTime;

	/**
	 * @param typeName the name of the type (e.g. "BDAY")
	 */
	public DateOrTimeType(String typeName) {
		super(typeName);
	}

	/**
	 * @param typeName the name of the type (e.g. "BDAY")
	 * @param date the date value
	 */
	public DateOrTimeType(String typeName, Date date) {
		super(typeName);
		setDate(date, false);
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
		reducedAccuracyDate = null;
	}

	/**
	 * Gets the reduced accuracy date string. This is only supported by vCard
	 * 4.0.
	 * @return the reduced accuracy date string or null if not set
	 * @see "<a href="
	 * http://tools.ietf.org/html/rfc6350">RFC 6350</a> p.12-14 for examples"
	 */
	public String getReducedAccuracyDate() {
		return reducedAccuracyDate;
	}

	/**
	 * Sets the value of this type to a "reduced accuracy" date. This is only
	 * supported by vCard 4.0.
	 * @param reducedAccuracyDate the reduced accuracy date (e.g "--0210" for
	 * "February 10")
	 * @see "<a href="
	 * http://tools.ietf.org/html/rfc6350">RFC 6350</a> p.12-14 for examples"
	 */
	public void setReducedAccuracyDate(String reducedAccuracyDate) {
		this.reducedAccuracyDate = reducedAccuracyDate;
		dateHasTime = reducedAccuracyDate.contains("T");
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
		reducedAccuracyDate = null;
	}

	/**
	 * Gets the type of calendar this date uses.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the type of calendar or null if not set
	 */
	public CalscaleParameter getCalscale() {
		return subTypes.getCalscale();
	}

	/**
	 * Sets the type of calendar this date uses.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param calscale the type of calendar or null to remove
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
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		if (version == VCardVersion.V4_0) {
			if (date != null || reducedAccuracyDate != null) {
				copy.setValue(ValueParameter.DATE_AND_OR_TIME);
				if (getCalscale() == null) {
					copy.setCalscale(CalscaleParameter.GREGORIAN);
				}
			} else if (text != null) {
				copy.setValue(ValueParameter.TEXT);
			}
		} else {
			if (dateHasTime) {
				copy.setValue(ValueParameter.DATE_TIME);
			} else {
				copy.setValue(ValueParameter.DATE);
			}
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			if (text != null) {
				throw new SkipMeException("Text values are not supported in vCard version " + version + ".");
			} else if (reducedAccuracyDate != null) {
				throw new SkipMeException("Reduced accuracy dates are not supported in vCard version " + version + ".");
			} else if (date != null) {
				ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
				sb.append(VCardDateFormatter.format(date, format));
			} else {
				throw new SkipMeException("Property has no date value associated with it.");
			}
		} else {
			if (text != null) {
				sb.append(VCardStringUtils.escape(text));
			} else if (reducedAccuracyDate != null) {
				sb.append(reducedAccuracyDate);
			} else if (date != null) {
				ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
				sb.append(VCardDateFormatter.format(date, format));
			} else {
				throw new SkipMeException("Property has no date, reduced accuracy date, or text value associated with it.");
			}
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		if (version == VCardVersion.V4_0 && subTypes.getValue() == ValueParameter.TEXT) {
			setText(value);
		} else {
			parseDate(value, version, warnings);
		}
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (text != null) {
			parent.text(text);
		} else if (reducedAccuracyDate != null) {
			parent.dateAndOrTime(reducedAccuracyDate);
		} else if (date != null) {
			ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
			String value = VCardDateFormatter.format(date, format);
			parent.dateAndOrTime(value);
		} else {
			throw new SkipMeException("Property has no date, reduced accuracy date, or text value associated with it.");
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.dateAndOrTime();
		if (value != null) {
			parseDate(value, element.version(), warnings);
		} else {
			setText(element.text());
		}
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
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		if (text != null) {
			return JCardValue.text(text);
		} else {
			if (date != null) {
				return dateHasTime ? JCardValue.dateTime(date) : JCardValue.date(date);
			} else if (reducedAccuracyDate != null) {
				JCardValue value = dateHasTime ? JCardValue.dateTime() : JCardValue.date();
				value.addValues(reducedAccuracyDate);
				return value;
			} else {
				throw new SkipMeException("Property has no date, reduced accuracy date, or text value associated with it.");
			}
		}
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getFirstValueAsString();
		if (value.getDataType() == JCardDataType.TEXT) {
			setText(valueStr);
		} else {
			parseDate(valueStr, version, warnings);
		}
	}

	private void parseDate(String value, VCardVersion version, List<String> warnings) {
		try {
			boolean hasTime = value.contains("T");
			setDate(VCardDateFormatter.parse(value), hasTime);
		} catch (IllegalArgumentException e) {
			if (version == VCardVersion.V4_0) {
				if (reducedAccuracyRegex.matcher(value).matches()) {
					setReducedAccuracyDate(value);
				} else {
					warnings.add("Date string \"" + value + "\" could not be parsed.  Assuming it's a text value.");
					setText(value);
				}
			} else {
				throw new SkipMeException("Date string \"" + value + "\" could not be parsed.");
			}
		}
	}
}
