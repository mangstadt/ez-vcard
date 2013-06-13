package ezvcard.types;

import ezvcard.parameters.TypeParameter;

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
 * Represents a vCard Type that has a TYPE parameter that contains only one
 * value (for example, {@link PhotoType}).
 * @author Michael Angstadt
 * @see MultiValuedTypeParameterType
 */
public abstract class SingleValuedTypeParameterType<T extends TypeParameter> extends TypeParameterType<T> {
	/**
	 * Creates a single-valued type parameter property.
	 * @param name the type name (e.g. "PHOTO")
	 */
	public SingleValuedTypeParameterType(String name) {
		super(name);
	}

	/**
	 * Gets the value of the TYPE parameter.
	 * @return the TYPE value or null if it has no TYPE value
	 */
	public T getType() {
		String type = subTypes.getType();
		return (type == null) ? null : buildTypeObj(type);
	}

	/**
	 * Sets the value of the TYPE parameter.
	 * @param type the TYPE value to set
	 */
	public void setType(T type) {
		subTypes.setType(type.getValue());
	}
}