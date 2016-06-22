package ezvcard.parameter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import ezvcard.SupportedVersions;
import ezvcard.VCardVersion;

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
 * Represents a vCard parameter (aka "sub type") whose values are pre-defined.
 * @author Michael Angstadt
 */
public class VCardParameter {
	/**
	 * The value (for example, "home").
	 */
	protected final String value;

	/**
	 * Creates a new parameter.
	 * @param value the value
	 */
	public VCardParameter(String value) {
		this(value, false);
	}

	/**
	 * Creates a new parameter.
	 * @param value the value
	 * @param preserveCase true to preserve the case of the value, false convert
	 * it to lower-case
	 */
	protected VCardParameter(String value, boolean preserveCase) {
		this.value = (value == null || preserveCase) ? value : value.toLowerCase();
	}

	/**
	 * Gets the value of the parameter.
	 * @return the value of the parameter (e.g. "home")
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <p>
	 * Gets the vCard versions that support this parameter value.
	 * </p>
	 * <p>
	 * The supported versions are defined by assigning a
	 * {@link SupportedVersions} annotation to the parameter value's static
	 * field (for example, {@link AddressType#DOM}). Dynamically-created
	 * parameter values (i.e. non-standard values) are considered to be
	 * supported by all versions.
	 * </p>
	 * @return the vCard versions that support this parameter.
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
	 * Determines if this parameter value is supported by the given vCard
	 * version.
	 * </p>
	 * <p>
	 * The supported versions are defined by assigning a
	 * {@link SupportedVersions} annotation to the parameter value's static
	 * field (for example, {@link AddressType#DOM}). Dynamically-created
	 * parameter values (i.e. non-standard values) are considered to be
	 * supported by all versions.
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
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VCardParameter other = (VCardParameter) obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}
}
