package ezvcard.types;

import java.util.Date;
import java.util.List;

import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.ISOFormat;
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
 * Represents a type whose value is timestamp (in other words, a date, time, and
 * timezone)
 * @author Michael Angstadt
 */
public class TimestampType extends VCardType {
	protected Date timestamp;

	/**
	 * Creates a timestamp property.
	 * @param typeName the name of the type
	 */
	public TimestampType(String typeName) {
		this(typeName, null);
	}

	/**
	 * Creates a timestamp property.
	 * @param typeName the name of the type
	 * @param timestamp the timestamp
	 */
	public TimestampType(String typeName, Date timestamp) {
		super(typeName);
		this.timestamp = timestamp;
	}

	/**
	 * Gets the timestamp.
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 * @param timestamp the timestamp
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		sb.append(writeValue(false));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		parseValue(VCardStringUtils.unescape(value));
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		parent.append(ValueParameter.TIMESTAMP, writeValue(false));
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.first(ValueParameter.TIMESTAMP);
		if (value == null) {
			throw new SkipMeException("No timestamp value found.");
		}
		parseValue(value);
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
		parseValue(value);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		checkForValue();
		return JCardValue.single(ValueParameter.TIMESTAMP, writeValue(true));
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		parseValue(valueStr);
	}

	private void checkForValue() {
		if (timestamp == null) {
			throw new SkipMeException("Property has no timestamp value associated with it.");
		}
	}

	private String writeValue(boolean extended) {
		checkForValue();
		ISOFormat format = extended ? ISOFormat.UTC_TIME_EXTENDED : ISOFormat.UTC_TIME_BASIC;
		return VCardDateFormatter.format(timestamp, format);
	}

	private void parseValue(String value) {
		try {
			timestamp = VCardDateFormatter.parse(value);
		} catch (IllegalArgumentException e) {
			throw new SkipMeException("Could not parse timestamp: " + value);
		}
	}
}
