package ezvcard.types;

import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HCardElement;
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
 * Contains the timezone that the person lives/works in.
 * 
 * <pre>
 * VCard vcard = new VCard();
 * TimezoneType tz = new TimezoneType(-5, 0, &quot;America/New_York&quot;);
 * vcard.addTimezone(tz);
 * </pre>
 * 
 * <p>
 * vCard property name: TZ
 * </p>
 * <p>
 * vCard versions: 2.1, 3.0, 4.0
 * </p>
 * 
 * @author Michael Angstadt
 */

//@formatter:off
/* 
 * vCard 2.1, 3.0:
 * Try to parse the beginning as UTC offset.
 * - If valid, parse the rest as text.
 * - If invalid, throw SkipMeException.
 * 
 * vCard 4.0, jCard:
 * VALUE=text:			Try to parse as UTC offset.  If invalid, treat as text
 * VALUE=utc-offset:	Try to parse as UTC offset.  If invalid, throw SkipMeException.
 * VALUE=uri:			Not going to support this, as there is no description of what a timezone URI looks like
 * No VALUE param:		Try to parse as UTC offset.  If invalid, treat as text
 * 
 * hCard:
 * Same as vCard 3.0
 * 
 * xCard:
 * text	| utc-offset	| result
 * no	| no			| throw SkipMeException
 * yes	| no			| OK
 * no	| yes			| OK
 * no	| invalid		| throw SkipMeException
 * yes	| yes			| OK
 * yes	| invalid		| add warning, ignore utc-offset
 */
//@formatter:on
public class TimezoneType extends VCardType {
	public static final String NAME = "TZ";
	private static final Pattern utcOffsetRegex = Pattern.compile("^([-\\+]?\\d{1,2}(:?\\d{2})?)(.*)");

	private Integer hourOffset;
	private Integer minuteOffset;
	private String text;

	public TimezoneType() {
		super(NAME);
	}

	/**
	 * This is the recommended constructor for version 4.0 vCards.
	 * @param text string representing the timezone from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public TimezoneType(String text) {
		this(null, null, text);
	}

	/**
	 * This is the recommended constructor for version 2.1 and 3.0 vCards.
	 * @param hourOffset the hour offset
	 * @param minuteOffset the minute offset
	 */
	public TimezoneType(Integer hourOffset, Integer minuteOffset) {
		this(hourOffset, minuteOffset, null);
	}

	/**
	 * This constructor can be used for all vCard versions.
	 * @param hourOffset the hour offset
	 * @param minuteOffset the minute offset
	 * @param text can be anything, but should be a string representing the
	 * timezone from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public TimezoneType(Integer hourOffset, Integer minuteOffset, String text) {
		super(NAME);
		setHourOffset(hourOffset);
		setMinuteOffset(minuteOffset);
		setText(text);
	}

	/**
	 * Gets the hour offset.
	 * @return the hour offset or null if not set
	 */
	public Integer getHourOffset() {
		return hourOffset;
	}

	/**
	 * Sets the hour offset.
	 * @param hourOffset the hour offset or null to remove
	 */
	public void setHourOffset(Integer hourOffset) {
		this.hourOffset = hourOffset;
	}

	/**
	 * Gets the minute offset.
	 * @return the minute offset or null if not set
	 */
	public Integer getMinuteOffset() {
		return minuteOffset;
	}

	/**
	 * Sets the minute offset.
	 * @param minuteOffset the minute offset or null to remove
	 * @throws IllegalArgumentException if the minute offset is not between 0
	 * and 59
	 */
	public void setMinuteOffset(Integer minuteOffset) {
		if (minuteOffset != null && (minuteOffset < 0 || minuteOffset > 59)) {
			throw new IllegalArgumentException("Minute offset must be between 0 and 59.");
		}
		this.minuteOffset = minuteOffset;
	}

	/**
	 * Gets the text portion of the timezone.
	 * @return the text portion (e.g. "America/New_York")
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text portion of the timezone.
	 * @param text the text portion (e.g. "America/New_York")
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Creates a {@link java.util.TimeZone} representation of this class.
	 * @return a {@link TimeZone} object or null if this object contains no
	 * offset data
	 */
	public TimeZone toTimeZone() {
		if (hourOffset == null || minuteOffset == null) {
			return null;
		}

		int rawHourOffset = hourOffset * 60 * 60 * 1000;
		int rawMinuteOffset = minuteOffset * 60 * 1000;
		if (rawHourOffset < 0) {
			rawMinuteOffset *= -1;
		}
		int rawOffset = rawHourOffset + rawMinuteOffset;
		return new SimpleTimeZone(rawOffset, "");
	}

	/**
	 * Gets the TYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return subTypes.getType();
	}

	/**
	 * Sets the TYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param type the TYPE value (this should be either "work" or "home") or
	 * null to remove
	 */
	public void setType(String type) {
		subTypes.setType(type);
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return subTypes.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
	}

	/**
	 * Gets all PID parameter values.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the PID values or empty set if there are none
	 * @see VCardSubTypes#getPids
	 */
	public List<Integer[]> getPids() {
		return subTypes.getPids();
	}

	/**
	 * Adds a PID value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param localId the local ID
	 * @param clientPidMapRef the ID used to reference the property's globally
	 * unique identifier in the CLIENTPIDMAP property.
	 * @see VCardSubTypes#addPid(int, int)
	 */
	public void addPid(int localId, int clientPidMapRef) {
		subTypes.addPid(localId, clientPidMapRef);
	}

	/**
	 * Removes all PID values.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @see VCardSubTypes#removePids
	 */
	public void removePids() {
		subTypes.removePids();
	}

	/**
	 * Gets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the preference value or null if it doesn't exist
	 * @see VCardSubTypes#getPref
	 */
	public Integer getPref() {
		return subTypes.getPref();
	}

	/**
	 * Sets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see VCardSubTypes#setPref
	 */
	public void setPref(Integer pref) {
		subTypes.setPref(pref);
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
		if (text != null) {
			copy.setValue(ValueParameter.TEXT);
		} else if (hourOffset != null && minuteOffset != null && version == VCardVersion.V4_0) {
			copy.setValue(ValueParameter.UTC_OFFSET);
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (text != null) {
			if ((version == VCardVersion.V2_1 || version == VCardVersion.V3_0) && hourOffset != null && minuteOffset != null) {
				sb.append(VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true));
				sb.append(';');
			}
			sb.append(VCardStringUtils.escape(text));
		} else if (hourOffset != null && minuteOffset != null) {
			sb.append(VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true));
		} else {
			throw new SkipMeException("Property does not have text or a UTC offset associated with it.");
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		parse(value, subTypes.getValue() == ValueParameter.UTC_OFFSET, version);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (text != null) {
			parent.text(text);
		} else if (hourOffset != null && minuteOffset != null) {
			String offset = VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true);
			parent.append("utc-offset", offset);
		} else {
			throw new SkipMeException("Property does not have text or a UTC offset associated with it.");
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String text = element.get("text");
		String utcOffset = element.get("utc-offset");

		if (text == null && utcOffset == null) {
			throw new SkipMeException("No timezone data found.");
		}

		if (text != null) {
			setText(text);
		}

		if (utcOffset != null) {
			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(utcOffset);
				setMinuteOffset(offsets[1]); //set this one first because it could throw an exception
				setHourOffset(offsets[0]);
			} catch (IllegalArgumentException e) {
				if (text == null) {
					throw new SkipMeException("Unable to parse UTC offset: " + utcOffset);
				} else {
					warnings.add("Ignoring invalid UTC offset: " + utcOffset);
				}
			}
		}
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		parse(element.value(), false, VCardVersion.V3_0);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		if (text != null) {
			return JCardValue.text(text);
		} else if (hourOffset != null && minuteOffset != null) {
			return JCardValue.utcOffset(hourOffset, minuteOffset);
		} else {
			throw new SkipMeException("Property does not have text or a UTC offset associated with it.");
		}
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getFirstValueAsString();
		parse(valueStr, value.getDataType() == JCardDataType.UTC_OFFSET, version);
	}

	private void parse(String value, boolean isUtcOffsetDataType, VCardVersion version) {
		if (version == null || version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			//will always start with a UTC offset
			//e.g. "-05:00;EDT;America/New_York"
			//e.g. "-05:00"
			Matcher m = utcOffsetRegex.matcher(value);
			if (m.find()) {
				try {
					int offsets[] = VCardDateFormatter.parseTimeZone(m.group(1));
					setMinuteOffset(offsets[1]); //set this one first because it could throw an exception
					setHourOffset(offsets[0]);

					String text = m.group(3);
					if (text != null && text.length() == 0) {
						text = null;
					}
					setText(text);
				} catch (IllegalArgumentException e) {
					throw new SkipMeException("Unable to parse UTC offset: " + m.group(1));
				}
			} else {
				throw new SkipMeException("No UTC offset found: " + value);
			}
		} else {
			//e.g. "-05:00"
			//e.g. "America/New_York"
			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(value);
				setMinuteOffset(offsets[1]); //set this one first because it could throw an exception
				setHourOffset(offsets[0]);
			} catch (IllegalArgumentException e) {
				if (isUtcOffsetDataType) {
					throw new SkipMeException("Unable to parse UTC offset: " + value);
				}
				//e.g. "America/New_York"
				setText(value);
			}
		}
	}
}
