package ezvcard.types;

import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ValueParameter;
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
 * Represents the TZ type.
 * @author Michael Angstadt
 */
public class TimezoneType extends VCardType {
	public static final String NAME = "TZ";

	private int hourOffset;
	private int minuteOffset;
	private String shortText;
	private String longText;

	public TimezoneType() {
		super(NAME);
	}

	/**
	 * @param hourOffset the hour offset
	 * @param minuteOffset the minute offset
	 */
	public TimezoneType(int hourOffset, int minuteOffset) {
		this(hourOffset, minuteOffset, null, null);
	}

	/**
	 * 
	 * @param hourOffset the hour offset
	 * @param minuteOffset the minute offset
	 * @param shortText the short text (e.g. "EST")
	 * @param longText
	 */
	public TimezoneType(int hourOffset, int minuteOffset, String shortText, String longText) {
		super(NAME);
		setHourOffset(hourOffset);
		setMinuteOffset(minuteOffset);
		setShortText(shortText);
		setLongText(longText);
	}

	public int getHourOffset() {
		return hourOffset;
	}

	public void setHourOffset(int hourOffset) {
		this.hourOffset = hourOffset;
	}

	public int getMinuteOffset() {
		return minuteOffset;
	}

	public void setMinuteOffset(int minuteOffset) {
		if (minuteOffset < 0 || minuteOffset > 59) {
			throw new IllegalArgumentException("Minute offset must be between 0 and 59.");
		}
		this.minuteOffset = minuteOffset;
	}

	public String getShortText() {
		return shortText;
	}

	public void setShortText(String shortText) {
		this.shortText = shortText;
	}

	public String getLongText() {
		return longText;
	}

	public void setLongText(String longText) {
		this.longText = longText;
	}

	/**
	 * Creates a {@link java.util.TimeZone} representation of this class.
	 * @return a {@link TimeZone} object
	 */
	public TimeZone toTimeZone() {
		int rawHourOffset = hourOffset * 60 * 60 * 1000;
		int rawMinuteOffset = minuteOffset * 60 * 1000;
		if (rawHourOffset < 0) {
			rawMinuteOffset *= -1;
		}
		int rawOffset = rawHourOffset + rawMinuteOffset;
		return new SimpleTimeZone(rawOffset, "");
	}
	
	@Override
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		VCardSubTypes copy = new VCardSubTypes(subTypes);

		if (shortText != null || longText != null) {
			//add sub type "VALUE=text"
			copy.setValue(ValueParameter.TEXT);
		}

		return copy;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder sb = new StringBuilder();

		sb.append(VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true));
		if (shortText != null || longText != null) {
			sb.append(';');
			if (shortText != null) {
				sb.append(VCardStringUtils.escapeText(shortText));
			}
			sb.append(';');
			if (longText != null) {
				sb.append(VCardStringUtils.escapeText(longText));
			}
		}

		return sb.toString();
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String split[] = VCardStringUtils.splitBy(value, ';', false, true);

		int i = 0;
		if (split.length > i && split[i].length() > 0) {
			int offsets[] = VCardDateFormatter.parseTimeZone(split[i]);
			hourOffset = offsets[0];
			minuteOffset = offsets[1];
		}
		i++;

		shortText = (split.length > i && split[i].length() > 0) ? split[i] : null;
		i++;

		longText = (split.length > i && split[i].length() > 0) ? split[i] : null;
	}
}
