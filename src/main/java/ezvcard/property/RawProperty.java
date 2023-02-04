package ezvcard.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.validate.AllowedCharacters;
import com.github.mangstadt.vinnie.validate.VObjectValidator;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * Holds the property value as-is. No escaping or unescaping is done on the
 * value.
 * @author Michael Angstadt
 */
public class RawProperty extends TextProperty {
	private String propertyName;
	private VCardDataType dataType;

	/**
	 * Creates a raw property.
	 * @param propertyName the property name (e.g. "X-GENDER")
	 * @param value the property value
	 */
	public RawProperty(String propertyName, String value) {
		this(propertyName, value, null);
	}

	/**
	 * Creates a raw property.
	 * @param propertyName the property name (e.g. "X-GENDER")
	 * @param value the property value
	 * @param dataType the value's data type
	 */
	public RawProperty(String propertyName, String value, VCardDataType dataType) {
		super(value);
		this.propertyName = propertyName;
		this.dataType = dataType;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RawProperty(RawProperty original) {
		super(original);
		propertyName = original.propertyName;
		dataType = original.dataType;
	}

	/**
	 * Gets the name of the property.
	 * @return the property name
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Sets the name of the property.
	 * @param propertyName the property name
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Gets the data type of the property's value.
	 * @return the data type or null if unknown
	 */
	public VCardDataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the data type of the property's value.
	 * @param dataType the data type or null if unknown
	 */
	public void setDataType(VCardDataType dataType) {
		this.dataType = dataType;
	}

	@Override
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		SyntaxStyle syntax = version.getSyntaxStyle();
		AllowedCharacters allowed = VObjectValidator.allowedCharactersParameterName(syntax, true);
		if (!allowed.check(propertyName)) {
			if (syntax == SyntaxStyle.OLD) {
				AllowedCharacters notAllowed = allowed.flip();
				warnings.add(new ValidationWarning(33, propertyName, notAllowed.toString(true)));
			} else {
				warnings.add(new ValidationWarning(24, propertyName));
			}
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("propertyName", propertyName);
		values.put("dataType", dataType);
		values.put("value", value);
		return values;
	}

	@Override
	public RawProperty copy() {
		return new RawProperty(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((propertyName == null) ? 0 : propertyName.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		RawProperty other = (RawProperty) obj;
		if (dataType == null) {
			if (other.dataType != null) return false;
		} else if (!dataType.equals(other.dataType)) return false;
		if (propertyName == null) {
			if (other.propertyName != null) return false;
		} else if (!propertyName.equalsIgnoreCase(other.propertyName)) return false;
		return true;
	}
}
