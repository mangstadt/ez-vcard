package ezvcard.parameter;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;

import java.util.Collection;

import ezvcard.SupportedVersions;

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
 * Represents the "ENCODING" parameter.
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0}
 * </p>
 * @author Michael Angstadt
 */
public class Encoding extends VCardParameter {
	private static final VCardParameterCaseClasses<Encoding> enums = new VCardParameterCaseClasses<>(Encoding.class);

	/**
	 * Note: This specific parameter value is in upper-case in order to resolve
	 * a compatibility issue with the Windows 10 Contacts app. The 2.1 spec does
	 * not seem to explicitly state that the various ENCODING parameter values
	 * are case-insensitive, so this is probably the better approach anyway.
	 * @see <a href="https://github.com/mangstadt/ez-vcard/issues/56">Issue
	 * 56</a>
	 */
	@SupportedVersions({ V2_1 })
	public static final Encoding QUOTED_PRINTABLE = new Encoding("QUOTED-PRINTABLE", true);

	@SupportedVersions({ V2_1 })
	public static final Encoding BASE64 = new Encoding("BASE64", true);

	@SupportedVersions({ V2_1 })
	public static final Encoding _8BIT = new Encoding("8BIT", true);

	@SupportedVersions({ V2_1 })
	public static final Encoding _7BIT = new Encoding("7BIT", true);

	@SupportedVersions({ V3_0 })
	public static final Encoding B = new Encoding("b");

	private Encoding(String value) {
		super(value);
	}

	private Encoding(String value, boolean preserveCase) {
		super(value, preserveCase);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static Encoding find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static Encoding get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<Encoding> all() {
		return enums.all();
	}
}
