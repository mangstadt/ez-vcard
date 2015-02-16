package ezvcard.property;

import ezvcard.VCardDataType;

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
}
