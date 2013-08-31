package ezvcard.types;

import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HCardElement;
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
 * Parse as UTC offset.  If invalid, throw CannotParseException.
 * 
 * vCard 3.0, hCard:
 * VALUE=text:			Treat as text
 * No VALUE param:		Parse as UTC offset.  If invalid, add warning and treat as text.
 * 
 * vCard 4.0, jCard:
 * VALUE=text:			Treat as text
 * VALUE=utc-offset:	Parse as UTC offset.  If invalid, throw CannotParseException
 * VALUE=uri:			Not going to support this, as there is no description of what a timezone URI looks like
 * No VALUE param:		Parse as UTC offset.  If invalid, treat as text
 * 
 * xCard:
 * text	| utc-offset	| result
 * no	| no			| throw CannotParseException
 * yes	| no			| OK
 * no	| yes			| OK
 * no	| invalid		| throw CannotParseException
 * yes	| yes			| Parse text
 * yes	| invalid		| Parse text
 * 
 * Writing===================
 * 
 * vCard 2.1:
 * text	| utc-offset	| result
 * no	| no			| empty string (validation warning)
 * no	| yes			| Write UTC offset
 * yes	| no			| empty string (validation warning)
 * yes	| yes			| Write UTC offset
 * 
 * vCard 3.0:
 * text	| utc-offset	| result
 * no	| no			| empty string (validation warning)
 * no	| yes			| Write UTC offset
 * yes	| no			| Write text, add "VALUE=text" parameter
 * yes	| yes			| Write UTC offset
 * 
 * vCard 4.0, xCard, jCard:
 * text	| utc-offset	| result
 * no	| no			| empty string (validation warning)
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
		this(null, null, null);
	}

	/**
	 * Creates a timezone property.
	 * @param text a free-form string representing the timezone, preferably a
	 * timezone ID from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public TimezoneType(String text) {
		this(null, null, text);
	}

	/**
	 * Creates a timezone property.
	 * @param hourOffset the hour component of the UTC offset (e.g. -5)
	 * @param minuteOffset the minute component of the UTC offset (e.g. 0)
	 */
	public TimezoneType(Integer hourOffset, Integer minuteOffset) {
		this(hourOffset, minuteOffset, null);
	}

	/**
	 * Creates a timezone property.
	 * @param hourOffset the hour component of the UTC offset (e.g. -5)
	 * @param minuteOffset the minute component of the UTC offset (e.g. 0)
	 * @param text a free-form string representing the timezone, preferably a
	 * timezone ID from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public TimezoneType(Integer hourOffset, Integer minuteOffset, String text) {
		super(NAME);
		setOffset(hourOffset, minuteOffset);
		setText(text);
	}

	/**
	 * Creates a timezone property.
	 * @param timezone the timezone
	 */
	public TimezoneType(TimeZone timezone) {
		super(NAME);

		setText(timezone.getID());

		Integer[] offset = offsetFromTimezone(timezone);
		setOffset(offset[0], offset[1]);
	}

	/**
	 * Gets the hour component of the UTC offset.
	 * @return the hour component of the UTC offset or null if not set
	 */
	public Integer getHourOffset() {
		return hourOffset;
	}

	/**
	 * Gets the minute component of the UTC offset.
	 * @return the minute component of the UTC offset or null if not set
	 */
	public Integer getMinuteOffset() {
		return minuteOffset;
	}

	/**
	 * Sets the UTC offset.
	 * @param hourOffset the hour offset (e.g. -5) or null to remove
	 * @param minuteOffset the minute offset (e.g. 0) or null to remove
	 */
	public void setOffset(Integer hourOffset, Integer minuteOffset) {
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
	 * @return the free-form string representing the timezone, such as a
	 * timezone ID from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text portion of the timezone.
	 * @param text a free-form string representing the timezone, preferably a
	 * timezone ID from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
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
		if (hasText()) {
			TimeZone timezone = timezoneFromId(text);
			if (timezone != null) {
				return timezone;
			}
		}

		if (hasOffset()) {
			int rawHourOffset = hourOffset * 60 * 60 * 1000;
			int rawMinuteOffset = minuteOffset * 60 * 1000;
			if (rawHourOffset < 0) {
				rawMinuteOffset *= -1;
			}
			int rawOffset = rawHourOffset + rawMinuteOffset;

			String id = hasText() ? text : "";

			return new SimpleTimeZone(rawOffset, id);
		}

		return null;
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

	@Override
	public List<Integer[]> getPids() {
		return super.getPids();
	}

	@Override
	public void addPid(int localId, int clientPidMapRef) {
		super.addPid(localId, clientPidMapRef);
	}

	@Override
	public void removePids() {
		super.removePids();
	}

	@Override
	public Integer getPref() {
		return super.getPref();
	}

	@Override
	public void setPref(Integer pref) {
		super.setPref(pref);
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

		switch (version) {
		case V2_1:
			dataType = null;
			break;
		case V3_0:
			if (!hasOffset() && hasText()) {
				dataType = VCardDataType.TEXT;
			}
			break;
		case V4_0:
			if (hasOffset() && !hasText()) {
				dataType = VCardDataType.UTC_OFFSET;
			}
			break;
		}

		copy.setValue(dataType);
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		switch (version) {
		case V2_1:
			if (hasOffset()) {
				sb.append(VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, false)); //2.1 allows either basic or extended
				return;
			}

			if (hasText()) {
				//attempt to find the offset by treating the text as a timezone ID, like "America/New_York"
				TimeZone timezone = timezoneFromId(text);
				if (timezone != null) {
					Integer offset[] = offsetFromTimezone(timezone);
					sb.append(VCardDateFormatter.formatTimeZone(offset[0], offset[1], false));
				}
				return;
			}
		case V3_0:
			if (hasOffset()) {
				sb.append(VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true)); //3.0 only allows extended
			} else if (hasText()) {
				sb.append(VCardStringUtils.escape(text));
			}
			return;
		case V4_0:
			if (hasText()) {
				sb.append(VCardStringUtils.escape(text));
			} else if (hasOffset()) {
				sb.append(VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, false)); //4.0 only allows basic
			}
			return;
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		VCardDataType dataType = subTypes.getValue();
		parse(value, dataType, version, warnings);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		if (hasText()) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}

		if (hasOffset()) {
			String offset = VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, false);
			parent.append(VCardDataType.UTC_OFFSET, offset);
			return;
		}

		parent.append(VCardDataType.TEXT, "");
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String text = element.first(VCardDataType.TEXT);
		if (text != null) {
			setText(text);
			return;
		}

		String utcOffset = element.first(VCardDataType.UTC_OFFSET);
		if (utcOffset != null) {
			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(utcOffset);
				setOffset(offsets[0], offsets[1]);
			} catch (IllegalArgumentException e) {
				throw new CannotParseException("Unable to parse UTC offset.");
			}
			return;
		}

		throw missingXmlElements(VCardDataType.TEXT, VCardDataType.UTC_OFFSET);
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		parse(element.value(), null, VCardVersion.V3_0, warnings);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		if (hasText()) {
			return JCardValue.single(VCardDataType.TEXT, text);
		}

		if (hasOffset()) {
			String value = VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true);
			return JCardValue.single(VCardDataType.UTC_OFFSET, value);
		}

		return JCardValue.single(VCardDataType.TEXT, "");
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		VCardDataType dataType = value.getDataType();
		parse(valueStr, dataType, version, warnings);
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (!hasOffset() && !hasText()) {
			warnings.add("Property does not have text or UTC offset values associated with it.");
		}
		if (!hasOffset() && version == VCardVersion.V2_1) {
			warnings.add("Property requires a UTC offset for its value in version " + version.getVersion() + ".");
		}
		if (hasOffset() && (minuteOffset < 0 || minuteOffset > 59)) {
			warnings.add("Minute offset must be between 0 and 59.");
		}
	}

	private void parse(String value, VCardDataType dataType, VCardVersion version, List<String> warnings) {
		if (value == null || value.length() == 0) {
			return;
		}

		switch (version) {
		case V2_1:
			//e.g. "-05:00"
			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(value);
				setOffset(offsets[0], offsets[1]);
			} catch (IllegalArgumentException e) {
				throw new CannotParseException("Unable to parse UTC offset.");
			}
			break;
		case V3_0:
			//e.g. "-05:00"
			//e.g. "-05:00;EDT;America/New_York"
			if (dataType == VCardDataType.TEXT) {
				setText(value);
				return;
			}

			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(value);
				setOffset(offsets[0], offsets[1]);
			} catch (IllegalArgumentException e) {
				warnings.add("Unable to parse UTC offset.  Treating as text: " + value);
				setText(value);
			}
			break;
		case V4_0:
			//e.g. "-0500"
			//e.g. "America/New_York"
			if (dataType == VCardDataType.TEXT) {
				setText(value);
				return;
			}

			try {
				int offsets[] = VCardDateFormatter.parseTimeZone(value);
				setOffset(offsets[0], offsets[1]);
			} catch (IllegalArgumentException e) {
				if (dataType == VCardDataType.UTC_OFFSET) {
					throw new CannotParseException("Unable to parse UTC offset.");
				}
				setText(value);
			}
			break;
		}
	}

	private Integer[] offsetFromTimezone(TimeZone timezone) {
		long offsetMs = timezone.getOffset(System.currentTimeMillis());
		int hours = (int) (offsetMs / 1000 / 60 / 60);
		int minutes = (int) ((offsetMs / 1000 / 60) % 60);
		if (minutes < 0) {
			minutes *= -1;
		}
		return new Integer[] { hours, minutes };
	}

	private TimeZone timezoneFromId(String id) {
		TimeZone timezone = TimeZone.getTimeZone(id);
		return "GMT".equals(timezone.getID()) ? null : timezone;
	}
}
