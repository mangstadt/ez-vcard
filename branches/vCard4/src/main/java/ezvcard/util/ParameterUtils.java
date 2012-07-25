package ezvcard.util;

import java.lang.reflect.Field;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * Helper class for the parameter classes (such as
 * {@link ezvcard.parameters.AddressTypeParameter AddressTypeParameter}).
 * @author Michael Angstadt
 */
public class ParameterUtils {
	/**
	 * Retrieves one of the static objects in this class by name.
	 * @param clazz the class to retrieve the static object from
	 * @param value the type value (e.g. "home")
	 * @return the object associated with the given type name or null if none
	 * was found
	 */
	@SuppressWarnings("unchecked")
	public static <T> T valueOf(Class<T> clazz, String value) {
		//turn the value into a valid Java variable name
		if (Character.isDigit(value.charAt(0))) {
			value = "_" + value;
		}
		value = value.replace("-", "_").toUpperCase();

		try {
			Field f = clazz.getField(value);
			Object obj = f.get(null);
			if (clazz.isInstance(obj)) {
				return (T) obj;
			}
		} catch (Exception ex) {
			//static field not found
		}
		return null;
	}
	
	private ParameterUtils(){
		//hide constructor
	}
}
