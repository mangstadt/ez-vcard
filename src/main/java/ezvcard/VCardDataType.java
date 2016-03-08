package ezvcard;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import ezvcard.util.CaseClasses;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
	 * A uniform resource locator (e.g. "http://www.example.com/image.jpg").
	 * This data type is only used in 2.1 vCards. All other vCard versions use
	 * {@link #URI}.
	 */
	@SupportedVersions(V2_1)
	public static final VCardDataType URL = new VCardDataType("url");

	/**
	 * Refers to a MIME entity within an email.
	 */
	@SupportedVersions(V2_1)
	public static final VCardDataType CONTENT_ID = new VCardDataType("content-id");

	/**
	 * A non-textual value, such as a picture or sound file.
	 */
	@SupportedVersions(V3_0)
	public static final VCardDataType BINARY = new VCardDataType("binary");

	/**
	 * A uniform resource identifier (e.g. "http://www.example.com/image.jpg").
	 * 2.1 vCards use {@link #URL} instead.
	 */
	@SupportedVersions({ V3_0, V4_0 })
	public static final VCardDataType URI = new VCardDataType("uri");

	/**
	 * A plain text value.
	 */
	public static final VCardDataType TEXT = new VCardDataType("text");

	/**
	 * A date that does not have a time component (e.g. "2015-02-16").
	 */
	@SupportedVersions({ V3_0, V4_0 })
	public static final VCardDataType DATE = new VCardDataType("date");

	/**
	 * A time that does not have a date component (e.g. "08:34:00").
	 */
	@SupportedVersions({ V3_0, V4_0 })
	public static final VCardDataType TIME = new VCardDataType("time");

	/**
	 * A date with a time component (e.g. "2015-02-16 08:34:00").
	 */
	@SupportedVersions({ V3_0, V4_0 })
	public static final VCardDataType DATE_TIME = new VCardDataType("date-time");

	/**
	 * Any sort of date/time combination. The value can be a date (e.g.
	 * "2015-02-16"), a time (e.g. "08:34:00"), or a date with a time component
	 * (e.g. "2015-02-16 08:34:00").
	 */
	@SupportedVersions(V4_0)
	public static final VCardDataType DATE_AND_OR_TIME = new VCardDataType("date-and-or-time");

	/**
	 * A specific moment in time. Timestamps should be in UTC time.
	 */
	@SupportedVersions(V4_0)
	public static final VCardDataType TIMESTAMP = new VCardDataType("timestamp");

	/**
	 * A boolean value ("true" or "false").
	 */
	@SupportedVersions(V4_0)
	public static final VCardDataType BOOLEAN = new VCardDataType("boolean");

	/**
	 * An integer value (e.g. "42").
	 */
	@SupportedVersions(V4_0)
	public static final VCardDataType INTEGER = new VCardDataType("integer");

	/**
	 * A floating-point value (e.g. "3.14").
	 */
	@SupportedVersions(V4_0)
	public static final VCardDataType FLOAT = new VCardDataType("float");

	/**
	 * An offset from UTC time, in hours and minutes (e.g. "-0500").
	 */
	@SupportedVersions(V4_0)
	public static final VCardDataType UTC_OFFSET = new VCardDataType("utc-offset");

	/**
	 * A standardized abbreviation for a language (e.g. "en-us" for American
	 * English).
	 * @see <a href="http://tools.ietf.org/html/rfc5646">RFC 5646</a>
	 */
	@SupportedVersions(V4_0)
	public static final VCardDataType LANGUAGE_TAG = new VCardDataType("language-tag");

	private final String name;

	/**
	 * @param name the data type name
	 */
	private VCardDataType(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the data type.
	 * @return the name of the data type (e.g. "text")
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>
	 * Gets the vCard versions that support this data type.
	 * </p>
	 * <p>
	 * The supported versions are defined by assigning a
	 * {@link SupportedVersions} annotation to the data type's static field (for
	 * example, {@link VCardDataType#CONTENT_ID}). Dynamically-created data
	 * types (i.e. non-standard data types) are considered to be supported by
	 * all versions.
	 * </p>
	 * @return the vCard versions that support this data type
	 */
	public VCardVersion[] getSupportedVersions() {
		for (Field field : getClass().getFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			Object fieldValue;
			try {
				fieldValue = field.get(null);
			} catch (IllegalArgumentException e) {
				//should never be thrown because we check for the static modified
				continue;
			} catch (IllegalAccessException e) {
				continue;
			}

			if (fieldValue == this) {
				SupportedVersions supportedVersionsAnnotation = field.getAnnotation(SupportedVersions.class);
				return (supportedVersionsAnnotation == null) ? VCardVersion.values() : supportedVersionsAnnotation.value();
			}
		}

		return VCardVersion.values();
	}

	/**
	 * <p>
	 * Determines if this data type is supported by the given vCard version.
	 * </p>
	 * <p>
	 * The supported versions are defined by assigning a
	 * {@link SupportedVersions} annotation to the data type's static field (for
	 * example, {@link VCardDataType#CONTENT_ID}). Dynamically-created data
	 * types (i.e. non-standard data types) are considered to be supported by
	 * all versions.
	 * </p>
	 * @param version the vCard version
	 * @return true if it is supported, false if not
	 */
	public boolean isSupportedBy(VCardVersion version) {
		for (VCardVersion supportedVersion : getSupportedVersions()) {
			if (supportedVersion == version) {
				return true;
			}
		}
		return false;
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

	/*
	 * Note: This class doesn't need equals() or hashCode() because the
	 * CaseClasses object enforces reference equality.
	 */

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
