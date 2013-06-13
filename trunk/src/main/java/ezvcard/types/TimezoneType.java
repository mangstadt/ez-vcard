package ezvcard.types;

import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

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
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * TimezoneType tz = new TimezoneType(-5, 0, &quot;America/New_York&quot;);
 * vcard.addTimezone(tz);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>TZ</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * 
 * @author Michael Angstadt
 */

//@formatter:off
/* 
 * Parsing===================
 * 
 * vCard 2.1:
 * Parse as UTC offset.  If invalid, throw SkipMeException.
 * 
 * vCard 3.0, hCard:
 * VALUE=text:			Treat as text
 * No VALUE param:		Parse as UTC offset.  If invalid, add warning and treat as text.
 * 
 * vCard 4.0, jCard:
 * VALUE=text:			Treat as text
 * VALUE=utc-offset:	Parse as UTC offset.  If invalid, throw SkipMeException
 * VALUE=uri:			Not going to support this, as there is no description of what a timezone URI looks like
 * No VALUE param:		Parse as UTC offset.  If invalid, treat as text
 * 
 * xCard:
 * text	| utc-offset	| result
 * no	| no			| throw SkipMeException
 * yes	| no			| OK
 * no	| yes			| OK
 * no	| invalid		| throw SkipMeException
 * yes	| yes			| Parse both
 * yes	| invalid		| Add warning, ignore utc-offset
 * 
 * Writing===================
 * 
 * vCard 2.1:
 * text	| utc-offset	| result
 * no	| no			| SkipMeException
 * no	| yes			| Write UTC offset
 * yes	| no			| SkipMeException
 * yes	| yes			| Write UTC offset
 * 
 * vCard 3.0:
 * text	| utc-offset	| result
 * no	| no			| SkipMeException
 * no	| yes			| Write UTC offset
 * yes	| no			| Write text, add "VALUE=text" parameter
 * yes	| yes			| Write UTC offset
 * 
 * vCard 4.0, xCard, jCard:
 * text	| utc-offset	| result
 * no	| no			| SkipMeException
 * no	| yes			| Write UTC offset, add "VALUE=utc-offset" parameter
 * yes	| no			| Write text
 * yes	| yes			| Write text
 */
//@formatter:on
public class TimezoneType extends VCardType implements HasAltId {
	public static final String NAME = "TZ";

	private Integer hourOffset;
	private Integer minuteOffset;
	private String text;

	/**
	 * Creates an empty timezone property.
	 */
	public TimezoneType() {
		super(NAME);
	}

	/**
	 * Creates a timezone property (this is the recommended constructor for
	 * version 4.0 vCards).
	 * @param text string representing the timezone from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public TimezoneType(String text) {
		this(null, null, text);
	}

	/**
	 * Creates a timezone property (this is the recommended constructor for
	 * version 2.1 and 3.0 vCards).
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
		setOffset(hourOffset, minuteOffset);
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
	 * Gets the minute offset.
	 * @return the minute offset or null if not set
	 */
	public Integer getMinuteOffset() {
		return minuteOffset;
	}

	/**
	 * Sets the UTC offset. Both parameters can be set to null to remove the UTC
	 * offset from this object.
	 * @param hourOffset the hour offset (e.g. -5)
	 * @param minuteOffset the minute offset (e.g. 0)
	 * @throws IllegalArgumentException if the minute offset is not between 0
	 * and 59
	 */
	public void setOffset(Integer hourOffset, Integer minuteOffset) {
		if (minuteOffset != null && (minuteOffset < 0 || minuteOffset > 59)) {
			throw new IllegalArgumentException("Minute offset must be between 0 and 59.");
		}

		if (hourOffset != null && minuteOffset == null) {
			minuteOffset = 0;
		} else if (hourOffset == null && minuteOffset != null) {
			hourOffset = 0;
		}
		this.hourOffset = hourOffset;
		this.minuteOffset = minuteOffset;
	}

	private boolean hasOffset() {
		return hourOffset != null && minuteOffset != null;
	}

	private boolean hasText() {
		return text != null;
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
		if (!hasOffset()) {
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return subTypes.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		subTypes.setMediaType(mediaType);
	}

	/**
	 * Gets all PID parameter values.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @see VCardSubTypes#removePids
	 */
	public void removePids() {
		subTypes.removePids();
	}

	/**
	 * Gets the preference value.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see VCardSubTypes#setPref
	 */
	public void setPref(Integer pref) {
		subTypes.setPref(pref);
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
		switch (version) {
		case V3_0:
			if (!hasOffset() && hasText()) {
				copy.setValue(ValueParameter.TEXT);
			}
			break;
		case V4_0:
			if (hasOffset() && !hasText()) {
				copy.setValue(ValueParameter.UTC_OFFSET);
			}
			break;
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		checkForValue();

		if (version == null) {
			version = VCardVersion.V2_1;
		}

		boolean writeText = true;
		switch (version) {
		case V2_1:
			if (!hasOffset()) {
				throw new SkipMeException("Version " + VCardVersion.V2_1 + " requires a UTC offset.");
			}
			writeText = false;
			break;
		case V3_0:
			if (hasOffset()) {
				writeText = false;
			} else {
				writeText = true;
			}
			break;
		case V4_0:
			if (hasText()) {
				writeText = true;
			} else {
				writeText = false;
			}
			break;
		}

		if (writeText) {
			sb.append(VCardStringUtils.escape(text));
		} else {
			sb.append(VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true));
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		ValueParameter valueParam = subTypes.getValue();
		parse(value, valueParam == ValueParameter.TEXT, valueParam == ValueParameter.UTC_OFFSET, version, warnings);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		checkForValue();

		if (hasText()) {
			parent.text(text);
		} else {
			String offset = VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true);
			parent.utcOffset(offset);
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String text = element.get("text");
		String utcOffset = element.utcOffset();

		if (text == null && utcOffset == null) {
			throw new SkipMeException("No timezone data found.");
		}

		if (text != null) {
			setText(text);
		}

		if (utcOffset != null) {
			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(utcOffset);
				setOffset(offsets[0], offsets[1]);
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
		parse(element.value(), false, false, VCardVersion.V3_0, warnings);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		checkForValue();

		return hasText() ? JCardValue.text(text) : JCardValue.utcOffset(hourOffset, minuteOffset);
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getFirstValueAsString();
		JCardDataType dataType = value.getDataType();
		parse(valueStr, dataType == JCardDataType.TEXT, dataType == JCardDataType.UTC_OFFSET, version, warnings);
	}

	private void checkForValue() {
		if (!hasText() && !hasOffset()) {
			throw new SkipMeException("Property does not have text or a UTC offset associated with it.");
		}
	}

	private void parse(String value, boolean isTextDataType, boolean isUtcOffsetDataType, VCardVersion version, List<String> warnings) {
		if (version == null) {
			version = VCardVersion.V2_1;
		}

		switch (version) {
		case V2_1:
			//e.g. "-05:00"
			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(value);
				setOffset(offsets[0], offsets[1]);
			} catch (IllegalArgumentException e) {
				throw new SkipMeException("Unable to parse UTC offset: " + value);
			}
			break;
		case V3_0:
			//e.g. "-05:00"
			//e.g. "-05:00;EDT;America/New_York"
			if (isTextDataType) {
				setText(value);
			} else {
				try {
					int offsets[] = VCardDateFormatter.parseTimeZone(value);
					setOffset(offsets[0], offsets[1]);
				} catch (IllegalArgumentException e) {
					warnings.add("Unable to parse UTC offset.  Treating as text: " + value);
					setText(value);
				}
			}
			break;
		case V4_0:
			//e.g. "-05:00"
			//e.g. "America/New_York"
			if (isTextDataType) {
				setText(value);
			} else {
				try {
					int offsets[] = VCardDateFormatter.parseTimeZone(value);
					setOffset(offsets[0], offsets[1]);
				} catch (IllegalArgumentException e) {
					if (isUtcOffsetDataType) {
						throw new SkipMeException("Unable to parse UTC offset: " + value);
					}
					setText(value);
				}
			}
			break;
		}
	}
}
