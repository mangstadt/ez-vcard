package ezvcard.types;

import java.util.HashSet;
import java.util.Set;

import ezvcard.parameters.VCardParameter;

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
 * Represents a vCard Type that has a TYPE parameter that can have multiple
 * values (for example, {@link AddressType}).
 * @param <T> the parameter class
 * @author Michael Angstadt
 * @see SingleValuedTypeParameterType
 */
public abstract class MultiValuedTypeParameterType<T extends VCardParameter> extends TypeParameterType<T> {
	/**
	 * Creates a multi-valued type parameter property.
	 * @param typeName the type name (e.g. "ADR")
	 */
	public MultiValuedTypeParameterType(String typeName) {
		super(typeName);
	}

	/**
	 * Gets all the TYPE parameters.
	 * @return all the TYPE parameters or empty set if there are none
	 */
	public Set<T> getTypes() {
		Set<String> typesStr = subTypes.getTypes();
		Set<T> types = new HashSet<T>();
		for (String value : typesStr) {
			T type = buildTypeObj(value);
			if (type == null) {
				continue;
			}

			types.add(type);
		}
		return types;
	}

	/**
	 * Adds a TYPE parameter.
	 * @param type the TYPE parameter to add
	 */
	public void addType(T type) {
		subTypes.addType(type.getValue());
	}

	/**
	 * Removes a TYPE parameter.
	 * @param type the TYPE parameter to remove
	 */
	public void removeType(T type) {
		subTypes.removeType(type.getValue());
	}
}