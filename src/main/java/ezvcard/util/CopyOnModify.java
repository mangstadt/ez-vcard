package ezvcard.util;

import java.util.function.UnaryOperator;

import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;

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
*/

/**
 * Copies the wrapped object if modifications are made to it.
 * @param <T> the object
 * @author Michael Angstadt
 */
public class CopyOnModify<T> {
	private T object;
	private final UnaryOperator<T> copyFunction;
	private boolean copied;

	/**
	 * @param object the object to wrap
	 * @param copyFunction function for making a copy
	 */
	public CopyOnModify(T object, UnaryOperator<T> copyFunction) {
		this.object = object;
		this.copyFunction = copyFunction;
	}

	/**
	 * Wraps the given property's parameters.
	 * @param property the property
	 * @return the parameters
	 */
	public static CopyOnModify<VCardParameters> parameters(VCardProperty property) {
		return new CopyOnModify<>(property.getParameters(), VCardParameters::new);
	}

	/**
	 * Return the object's copy so that modifications can be made to it. Use
	 * this method when you want to modify the object.
	 * @return the copy
	 */
	public T modify() {
		if (!copied) {
			object = copyFunction.apply(object);
			copied = true;
		}
		return get();
	}

	/**
	 * Return the object's copy if modifications were made to it, or the
	 * original object if no modifications were made. Use this method when you
	 * only need to read the object.
	 * @return the object
	 */
	public T get() {
		return object;
	}
}
