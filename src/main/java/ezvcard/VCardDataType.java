package ezvcard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ezvcard.util.CaseClasses;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Defines the data type of a property's value.
 * @author Michael Angstadt
 */
public class VCardDataType {
	private static final CaseClasses<VCardDataType, String> enums = new CaseClasses<VCardDataType, String>(VCardDataType.class) {
		@Override
		protected VCardDataType create(String value) {
			return new VCardDataType(value);
		}

		@Override
		protected boolean matches(VCardDataType dataType, String value) {
			return dataType.name.equalsIgnoreCase(value);
		}
	};

	/**
	 * <p>
	 * A uniform resource locator (e.g. "http://www.example.com/image.jpg").
	 * This data type is only used in 2.1 vCards. All other vCard versions use
	 * {@link #URI}.
	 * </p>
	 * <b>Supported versions:</b> {@code 2.1 (p.18-9)}
	 */
	public static final VCardDataType URL = new VCardDataType("url", VCardVersion.V2_1);

	/**
	 * <p>
	 * Refers to a MIME entity within an email.
	 * </p>
	 * <b>Supported versions:</b> {@code 2.1 (p.8-9)}
	 */
	public static final VCardDataType CONTENT_ID = new VCardDataType("content-id", VCardVersion.V2_1);

	/**
	 * <p>
	 * A non-textual value, such as a picture or sound file.
	 * </p>
	 * <b>Supported versions:</b> {@code 3.0}
	 */
	public static final VCardDataType BINARY = new VCardDataType("binary", VCardVersion.V3_0);

	/**
	 * <p>
	 * A uniform resource identifier (e.g. "http://www.example.com/image.jpg").
	 * 2.1 vCards use {@link #URL} instead.
	 * </p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 */
	public static final VCardDataType URI = new VCardDataType("uri", VCardVersion.V3_0, VCardVersion.V4_0);

	/**
	 * <p>
	 * A plain text value.
	 * </p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 */
	public static final VCardDataType TEXT = new VCardDataType("text");

	/**
	 * <p>
	 * A date that does not have a time component (e.g. "2015-02-16").
	 * </p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 */
	public static final VCardDataType DATE = new VCardDataType("date", VCardVersion.V3_0, VCardVersion.V4_0);

	/**
	 * <p>
	 * A time that does not have a date component (e.g. "08:34:00").
	 * </p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 */
	public static final VCardDataType TIME = new VCardDataType("time", VCardVersion.V3_0, VCardVersion.V4_0);

	/**
	 * <p>
	 * A date with a time component (e.g. "2015-02-16 08:34:00").
	 * </p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 */
	public static final VCardDataType DATE_TIME = new VCardDataType("date-time", VCardVersion.V3_0, VCardVersion.V4_0);

	/**
	 * <p>
	 * Any sort of date/time combination. The value can be a date (e.g.
	 * "2015-02-16"), a time (e.g. "08:34:00"), or a date with a time component
	 * (e.g. "2015-02-16 08:34:00").
	 * </p>
	 * <b>Supported versions:</b> {@code 4.0}
	 */
	public static final VCardDataType DATE_AND_OR_TIME = new VCardDataType("date-and-or-time", VCardVersion.V4_0);

	/**
	 * <p>
	 * A specific moment in time. Timestamps should be in UTC time.
	 * </p>
	 * <b>Supported versions:</b> {@code 4.0}
	 */
	public static final VCardDataType TIMESTAMP = new VCardDataType("timestamp", VCardVersion.V4_0);

	/**
	 * <p>
	 * A boolean value ("true" or "false").
	 * </p>
	 * <b>Supported versions:</b> {@code 4.0}
	 */
	public static final VCardDataType BOOLEAN = new VCardDataType("boolean", VCardVersion.V4_0);

	/**
	 * <p>
	 * An integer value (e.g. "42").
	 * </p>
	 * <b>Supported versions:</b> {@code 4.0}
	 */
	public static final VCardDataType INTEGER = new VCardDataType("integer", VCardVersion.V4_0);

	/**
	 * <p>
	 * A floating-point value (e.g. "3.14").
	 * </p>
	 * <b>Supported versions:</b> {@code 4.0}
	 */
	public static final VCardDataType FLOAT = new VCardDataType("float", VCardVersion.V4_0);

	/**
	 * <p>
	 * An offset from UTC time, in hours and minutes (e.g. "-0500").
	 * </p>
	 * <b>Supported versions:</b> {@code 4.0}
	 */
	public static final VCardDataType UTC_OFFSET = new VCardDataType("utc-offset", VCardVersion.V4_0);

	/**
	 * <p>
	 * A standardized abbreviation for a language (e.g. "en-us" for American
	 * English).
	 * </p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @see <a href="http://tools.ietf.org/html/rfc5646">RFC 5646</a>
	 */
	public static final VCardDataType LANGUAGE_TAG = new VCardDataType("language-tag", VCardVersion.V4_0);

	private final String name;
	private final Set<VCardVersion> supportedVersions;

	/**
	 * @param name the data type name
	 * @param supportedVersions the vCard versions it is supported in
	 */
	private VCardDataType(String name, VCardVersion... supportedVersions) {
		this.name = name;
		if (supportedVersions.length == 0) {
			supportedVersions = VCardVersion.values();
		}

		Set<VCardVersion> set = EnumSet.copyOf(Arrays.asList(supportedVersions));
		this.supportedVersions = Collections.unmodifiableSet(set);
	}

	/**
	 * Gets the name of the data type.
	 * @return the name of the data type (e.g. "text")
	 */
	public String getName() {
		return name;
	}

	/**
	 * Determines if the data type is supported by the given vCard version.
	 * @param version the vCard version
	 * @return true if it is supported, false if not
	 */
	public boolean isSupported(VCardVersion version) {
		return supportedVersions.contains(version);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Searches for a data type that is defined as a static constant in this
	 * class.
	 * @param dataType the data type name (e.g. "text")
	 * @return the data type or null if not found
	 */
	public static VCardDataType find(String dataType) {
		return enums.find(dataType);
	}

	/**
	 * Searches for a data type and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param dataType data type name (e.g. "text")
	 * @return the data type
	 */
	public static VCardDataType get(String dataType) {
		return enums.get(dataType);
	}

	/**
	 * Gets all of the data types that are defined as static constants in this
	 * class.
	 * @return the data types
	 */
	public static Collection<VCardDataType> all() {
		return enums.all();
	}
}
