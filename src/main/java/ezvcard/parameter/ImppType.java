package ezvcard.parameter;

import java.util.Collection;

import ezvcard.property.Impp;

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
 * Represents the TYPE parameter of the {@link Impp} property.
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 */
public class ImppType extends VCardParameter {
	private static final VCardParameterCaseClasses<ImppType> enums = new VCardParameterCaseClasses<ImppType>(ImppType.class);

	public static final ImppType PERSONAL = new ImppType("personal");
	public static final ImppType BUSINESS = new ImppType("business");
	public static final ImppType HOME = new ImppType("home");
	public static final ImppType WORK = new ImppType("work");
	public static final ImppType MOBILE = new ImppType("mobile");
	public static final ImppType PREF = new ImppType("pref");

	private ImppType(String value) {
		super(value);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static ImppType find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static ImppType get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<ImppType> all() {
		return enums.all();
	}
}
