package ezvcard.parameter;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;

import java.util.Collection;

import ezvcard.SupportedVersions;
import ezvcard.property.Email;

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
 * Represents the TYPE parameter of the {@link Email} properties.
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
public class EmailType extends VCardParameter {
	private static final VCardParameterCaseClasses<EmailType> enums = new VCardParameterCaseClasses<EmailType>(EmailType.class);

	@SupportedVersions({ V2_1, V3_0 })
	public static final EmailType INTERNET = new EmailType("internet");

	@SupportedVersions({ V2_1, V3_0 })
	public static final EmailType X400 = new EmailType("x400");

	@SupportedVersions({ V2_1, V3_0 })
	public static final EmailType PREF = new EmailType("pref");

	@SupportedVersions(V2_1)
	public static final EmailType AOL = new EmailType("aol");

	@SupportedVersions(V2_1)
	public static final EmailType APPLELINK = new EmailType("applelink");

	@SupportedVersions(V2_1)
	public static final EmailType ATTMAIL = new EmailType("attmail");

	@SupportedVersions(V2_1)
	public static final EmailType CIS = new EmailType("cis");

	@SupportedVersions(V2_1)
	public static final EmailType EWORLD = new EmailType("eworld");

	@SupportedVersions(V2_1)
	public static final EmailType IBMMAIL = new EmailType("ibmmail");

	@SupportedVersions(V2_1)
	public static final EmailType MCIMAIL = new EmailType("mcimail");

	@SupportedVersions(V2_1)
	public static final EmailType POWERSHARE = new EmailType("powershare");

	@SupportedVersions(V2_1)
	public static final EmailType PRODIGY = new EmailType("prodigy");

	@SupportedVersions(V2_1)
	public static final EmailType TLX = new EmailType("tlx");

	@SupportedVersions(V4_0)
	public static final EmailType HOME = new EmailType("home");

	@SupportedVersions(V4_0)
	public static final EmailType WORK = new EmailType("work");

	private EmailType(String value) {
		super(value);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static EmailType find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static EmailType get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<EmailType> all() {
		return enums.all();
	}
}
