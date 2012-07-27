package ezvcard.types;

import java.util.Date;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.CalscaleParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.ISOFormat;
import ezvcard.util.VCardDateFormatter;
import ezvcard.util.VCardStringUtils;

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

	/**
	 * True if the "date" or "reduceAccuracyDate" fields have a time component,
	 * false if it just contains a date.
	 */
	private boolean dateHasTime;

	private String reducedAccuracyDate;

	/**
	 * @param typeName the name of the type
	 */
	public DateOrTimeType(String typeName) {
		super(typeName);
	}

	/**
	 * @param typeName the name of the type
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
	}

	/**
	 * Gets the reduced accuracy date string.
	 * @return the reduced accuracy date string or null if not set
	 */
	public String getReducedAccuracyDate() {
		return reducedAccuracyDate;
	}

	/**
	 * Sets the value of this type to a "reduced accuracy" date. This is only
	 * supported by vCard 4.0.
	 * @param reducedAccuracyDate the reduced accuracy date (e.g "--0210" for
	 * "February 10")
	 * @see "ISO 8601 specs"
	 */
	public void setReducedAccuracyDate(String reducedAccuracyDate) {
		this.reducedAccuracyDate = reducedAccuracyDate;
		dateHasTime = reducedAccuracyDate.contains("T");
		text = null;
	}

	/**
	 * Gets the text value.
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

	public CalscaleParameter getCalscale() {
		return subTypes.getCalscale();
	}

	public void setCalsclae(CalscaleParameter value) {
		subTypes.setCalscale(value);
	}

	@Override
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		VCardSubTypes copy = new VCardSubTypes(subTypes);
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
		return copy;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			if (text != null) {
				warnings.add("Text values for the " + typeName + " type are not allowed in vCard version " + version + ".  This type will not be added to the vCard.");
				return null;
			} else if (reducedAccuracyDate != null) {
				warnings.add("Reduced accuracy dates for the " + typeName + " type are not allowed in vCard version " + version + ".  This type will not be added to the vCard.");
				return null;
			} else if (date != null) {
				ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
				return VCardDateFormatter.format(date, format);
			} else {
				return null;
			}
		} else {
			if (text != null) {
				return VCardStringUtils.escapeText(text);
			} else if (reducedAccuracyDate != null) {
				return reducedAccuracyDate;
			} else if (date != null) {
				ISOFormat format = dateHasTime ? ISOFormat.TIME_BASIC : ISOFormat.DATE_BASIC;
				return VCardDateFormatter.format(date, format);
			} else {
				return null;
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
					warnings.add("Date string \"" + value + "\" could not be parsed.  Assuming it's a text value.");
					text = VCardStringUtils.unescape(value);
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
}
