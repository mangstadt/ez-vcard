package ezvcard.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import ezvcard.SupportedVersions;
import ezvcard.VCardVersion;

/*
 Copyright (c) 2012-2026, Michael Angstadt
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
 * Provides methods for getting the value of the {@link SupportedVersions}
 * annotation for "public static" fields.
 * @author Michael Angstadt
 */
public class SupportedVersionsHelper {
	/**
	 * Gets the vCard versions that are defined by the given object's
	 * {@link SupportedVersions} annotation.
	 * @param obj the object, must be "public static"
	 * @return the versions in the annotation, all versions if the annotation is
	 * not present, or all versions if the object is not a public static field
	 * of its class
	 */
	public static VCardVersion[] getSupportedVersions(Object obj) {
		//@formatter:off
		return Arrays.stream(obj.getClass().getFields())
			.filter(SupportedVersionsHelper::isFieldStatic)
			.filter(field -> isFieldMe(field, obj))
			.map(field -> field.getAnnotation(SupportedVersions.class))
			.map(annotation -> (annotation == null) ? VCardVersion.values() : annotation.value())
		.findFirst().orElseGet(VCardVersion::values);
		//@formatter:on
	}

	private static boolean isFieldStatic(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

	private static boolean isFieldMe(Field field, Object obj) {
		Object fieldValue;
		try {
			fieldValue = field.get(null);
		} catch (IllegalArgumentException e) {
			//should never be thrown because we check for the static modified
			return false;
		} catch (IllegalAccessException e) {
			//if the field is not accessible
			return false;
		}

		return fieldValue == obj;
	}

	/**
	 * Determines if this object is supported by the given vCard version. Uses
	 * the object's {@link SupportedVersions} annotation.
	 * @param version the vCard version
	 * @param obj the object
	 * @return true if it is supported, false if not
	 */
	public static boolean isSupportedBy(VCardVersion version, Object obj) {
		return Arrays.stream(getSupportedVersions(obj)).anyMatch(supportedVersion -> supportedVersion == version);
	}

	private SupportedVersionsHelper() {
		//hide
	}
}
