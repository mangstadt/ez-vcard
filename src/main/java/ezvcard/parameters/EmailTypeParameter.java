package ezvcard.parameters;

import java.util.Collection;

import ezvcard.types.EmailType;

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
 * Represents the TYPE parameter of the {@link EmailType} type.
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class EmailTypeParameter extends TypeParameter {
	private static final SingleValuedCaseClasses<EmailTypeParameter> enums = new SingleValuedCaseClasses<EmailTypeParameter>(EmailTypeParameter.class);

	/**
	 * <b>Supported versions:</b> <code>2.1, 3.0</code>
	 */
	public static final EmailTypeParameter INTERNET = new EmailTypeParameter("internet");

	/**
	 * <b>Supported versions:</b> <code>2.1, 3.0</code>
	 */
	public static final EmailTypeParameter X400 = new EmailTypeParameter("x400");

	/**
	 * <b>Supported versions:</b> <code>2.1, 3.0</code>
	 */
	public static final EmailTypeParameter PREF = new EmailTypeParameter("pref");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter AOL = new EmailTypeParameter("aol");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter APPLELINK = new EmailTypeParameter("applelink");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter ATTMAIL = new EmailTypeParameter("attmail");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter CIS = new EmailTypeParameter("cis");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter EWORLD = new EmailTypeParameter("eworld");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter IBMMAIL = new EmailTypeParameter("ibmmail");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter MCIMAIL = new EmailTypeParameter("mcimail");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter POWERSHARE = new EmailTypeParameter("powershare");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter PRODIGY = new EmailTypeParameter("prodigy");

	/**
	 * <b>Supported versions:</b> <code>2.1 (suggested)</code>
	 */
	public static final EmailTypeParameter TLX = new EmailTypeParameter("tlx");

	/**
	 * <b>Supported versions:</b> <code>4.0</code>
	 */
	public static final EmailTypeParameter HOME = new EmailTypeParameter("home");

	/**
	 * <b>Supported versions:</b> <code>4.0</code>
	 */
	public static final EmailTypeParameter WORK = new EmailTypeParameter("work");

	private EmailTypeParameter(String value) {
		super(value);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static EmailTypeParameter find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * <code>==</code> equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static EmailTypeParameter get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<EmailTypeParameter> all() {
		return enums.all();
	}
}
