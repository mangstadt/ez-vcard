package ezvcard.property;

import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.util.ISOFormat;
import ezvcard.util.UtcOffset;

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
 * <p>
 * Defines the timezone that the person lives/works in.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Timezone tz = new Timezone(-5, 0, &quot;America/New_York&quot;);
 * vcard.addTimezone(tz);
 * 
 * //using a Java &quot;TimeZone&quot; object
 * java.util.TimeZone javaTz = java.util.TimeZone.getTimeZone(&quot;America/New_York&quot;);
 * tz = new Timezone(javaTz);
 * vcard.addTimezone(tz);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code TZ}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
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
public class Timezone extends VCardProperty implements HasAltId {
	private UtcOffset offset;
	private String text;

	/**
	 * Creates a timezone property.
	 * @param text a free-form string representing the timezone, preferably a
	 * timezone ID from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public Timezone(String text) {
		this(null, text);
	}

	/**
	 * Creates a timezone property.
	 * @param hourOffset the hour component of the UTC offset (e.g. -5)
	 * @param minuteOffset the minute component of the UTC offset (e.g. 0)
	 */
	public Timezone(Integer hourOffset, Integer minuteOffset) {
		this(new UtcOffset(hourOffset, minuteOffset));
	}

	/**
	 * Creates a timezone property.
	 * @param offset the UTC offset
	 */
	public Timezone(UtcOffset offset) {
		this(offset, null);
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
	public Timezone(Integer hourOffset, Integer minuteOffset, String text) {
		this(new UtcOffset(hourOffset, minuteOffset), text);
	}

	/**
	 * Creates a timezone property.
	 * @param offset the UTC offset
	 * @param text a free-form string representing the timezone, preferably a
	 * timezone ID from the <a
	 * href="http://en.wikipedia.org/wiki/List_of_tz_database_time_zones">Olson
	 * Database</a> (e.g. "America/New_York")
	 */
	public Timezone(UtcOffset offset, String text) {
		setOffset(offset);
		setText(text);
	}

	/**
	 * Creates a timezone property.
	 * @param timezone the timezone
	 */
	public Timezone(TimeZone timezone) {
		this(UtcOffset.parse(timezone), timezone.getID());
	}

	/**
	 * Gets the hour component of the UTC offset.
	 * @return the hour component of the UTC offset or null if not set
	 */
	public Integer getHourOffset() {
		return (offset == null) ? null : offset.getHour();
	}

	/**
	 * Gets the minute component of the UTC offset.
	 * @return the minute component of the UTC offset or null if not set
	 */
	public Integer getMinuteOffset() {
		return (offset == null) ? null : offset.getMinute();
	}

	/**
	 * Gets the UTC offset.
	 * @return the UTC offset or null if not set
	 */
	public UtcOffset getOffset() {
		return offset;
	}

	/**
	 * Sets the UTC offset.
	 * @param hourOffset the hour offset (e.g. -5)
	 * @param minuteOffset the minute offset (e.g. 0)
	 */
	public void setOffset(int hourOffset, int minuteOffset) {
		setOffset(new UtcOffset(hourOffset, minuteOffset));
	}

	/**
	 * Sets the UTC offset.
	 * @param offset the UTC offset
	 */
	public void setOffset(UtcOffset offset) {
		this.offset = offset;
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
		if (text != null) {
			TimeZone timezone = ISOFormat.parseTimeZoneId(text);
			if (timezone != null) {
				return timezone;
			}
		}

		if (offset != null) {
			int rawHourOffset = offset.getHour() * 60 * 60 * 1000;
			int rawMinuteOffset = offset.getMinute() * 60 * 1000;
			if (rawHourOffset < 0) {
				rawMinuteOffset *= -1;
			}
			int rawOffset = rawHourOffset + rawMinuteOffset;

			String id = (text == null) ? "" : text;

			return new SimpleTimeZone(rawOffset, id);
		}

		return null;
	}

	/**
	 * Gets the TYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return parameters.getType();
	}

	/**
	 * Sets the TYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param type the TYPE value (this should be either "work" or "home") or
	 * null to remove
	 */
	public void setType(String type) {
		parameters.setType(type);
	}

	/**
	 * Gets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the media type or null if not set
	 */
	public String getMediaType() {
		return parameters.getMediaType();
	}

	/**
	 * Sets the MEDIATYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param mediaType the media type or null to remove
	 */
	public void setMediaType(String mediaType) {
		parameters.setMediaType(mediaType);
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
		return parameters.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		parameters.setAltId(altId);
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (offset == null && text == null) {
			warnings.add(new Warning(8));
		}
		if (offset == null && version == VCardVersion.V2_1) {
			warnings.add(new Warning(20));
		}
		if (offset != null && (offset.getMinute() < 0 || offset.getMinute() > 59)) {
			warnings.add(new Warning(21));
		}
	}
}
