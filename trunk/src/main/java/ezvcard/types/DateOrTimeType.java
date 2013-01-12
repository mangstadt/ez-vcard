package ezvcard.types;

import java.util.Date;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.CalscaleParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HCardUtils;
import ezvcard.util.ISOFormat;
import ezvcard.util.VCardDateFormatter;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;

/*
 Copyright (c) 2012, Michael Angstadt
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
public class DateOrTimeType extends VCardType {
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
	public void setCalsclae(CalscaleParameter calscale) {
		subTypes.setCalscale(calscale);
	}

	/**
	 * Gets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the ALTID or null if it doesn't exist
	 * @see VCardSubTypes#getAltId
	 */
	public String getAltId() {
		return subTypes.getAltId();
	}

	/**
	 * Sets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param altId the ALTID or null to remove
	 * @see VCardSubTypes#setAltId
	 */
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
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			if (text != null) {
				throw new SkipMeException("Text values are not allowed in vCard version " + version + ".");
			} else if (reducedAccuracyDate != null) {
				throw new SkipMeException("Reduced accuracy dates are not allowed in vCard version " + version + ".");
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
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (version == VCardVersion.V4_0) {
			if (subTypes.getValue() == ValueParameter.TEXT) {
				text = VCardStringUtils.unescape(value);
			} else if (value.contains("-")) {
				reducedAccuracyDate = value;
			} else {
				try {
					date = VCardDateFormatter.parse(value);
				} catch (IllegalArgumentException e) {
					//not all reduced accuracy dates have dashes (e.g. "2012")
					if (value.matches("\\d+")) {
						reducedAccuracyDate = value;
					} else {
						warnings.add("Date string \"" + value + "\" could not be parsed.  Assuming it's a text value.");
						text = VCardStringUtils.unescape(value);
					}
				}
			}
		} else {
			try {
				date = VCardDateFormatter.parse(value);
			} catch (IllegalArgumentException e) {
				warnings.add("Date string \"" + value + "\" for type \"" + typeName + "\" could not be parsed.");
			}
		}
	}

	@Override
	protected void doMarshalValue(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (text != null) {
			parent.appendText(text);
		} else if (reducedAccuracyDate != null) {
			parent.appendDateAndOrTime(reducedAccuracyDate);
		} else if (date != null) {
			ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
			String value = VCardDateFormatter.format(date, format);
			parent.appendDateAndOrTime(value);
		} else {
			throw new SkipMeException("Property has no date, reduced accuracy date, or text value associated with it.");
		}
	}

	@Override
	protected void doUnmarshalValue(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.getDateAndOrTime();
		if (value != null) {
			if (value.contains("-")) {
				setReducedAccuracyDate(value);
			} else {
				try {
					boolean hasTime = value.contains("T");
					setDate(VCardDateFormatter.parse(value), hasTime);
				} catch (IllegalArgumentException e) {
					//not all reduced accuracy dates have dashes (e.g. "2012")
					if (value.matches("\\d+")) {
						setReducedAccuracyDate(value);
					} else {
						warnings.add("Date string \"" + value + "\" could not be parsed.  Assuming it's a text value.");
						setText(value);
					}
				}
			}
		} else {
			setText(element.getText());
		}
	}

	@Override
	protected void doUnmarshalHtml(org.jsoup.nodes.Element element, List<String> warnings) {
		String value = null;
		if ("time".equals(element.tagName())) {
			String datetime = element.attr("datetime");
			if (datetime.length() > 0) {
				value = datetime;
			}
		}
		if (value == null) {
			value = HCardUtils.getElementValue(element);
		}
		doUnmarshalValue(value, VCardVersion.V3_0, warnings, CompatibilityMode.RFC);
	}
}
