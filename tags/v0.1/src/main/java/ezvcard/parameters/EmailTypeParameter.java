package ezvcard.parameters;

import ezvcard.util.ParameterUtils;

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
 * Represents the TYPE parameter of the EMAIL type.
 * @author Michael Angstadt
 */
public class EmailTypeParameter extends TypeParameter {
	public static final EmailTypeParameter INTERNET = new EmailTypeParameter("internet");
	public static final EmailTypeParameter X400 = new EmailTypeParameter("x400");
	public static final EmailTypeParameter PREF = new EmailTypeParameter("pref");

	//these are not standard--they are suggestions included in the v2.1 doc
	public static final EmailTypeParameter AOL = new EmailTypeParameter("aol");
	public static final EmailTypeParameter APPLELINK = new EmailTypeParameter("applelink");
	public static final EmailTypeParameter ATTMAIL = new EmailTypeParameter("attmail");
	public static final EmailTypeParameter CIS = new EmailTypeParameter("cis");
	public static final EmailTypeParameter EWORLD = new EmailTypeParameter("eworld");
	public static final EmailTypeParameter IBMMAIL = new EmailTypeParameter("ibmmail");
	public static final EmailTypeParameter MCIMAIL = new EmailTypeParameter("mcimail");
	public static final EmailTypeParameter POWERSHARE = new EmailTypeParameter("powershare");
	public static final EmailTypeParameter PRODIGY = new EmailTypeParameter("prodigy");
	public static final EmailTypeParameter TLX = new EmailTypeParameter("tlx");

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard TYPEs. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "internet")
	 */
	public EmailTypeParameter(String value) {
		super(value);
	}

	/**
	 * Retrieves one of the static objects in this class by name.
	 * @param value the type value (e.g. "home")
	 * @return the object associated with the given type name or null if none
	 * was found
	 */
	public static EmailTypeParameter valueOf(String value) {
		return ParameterUtils.valueOf(EmailTypeParameter.class, value);
	}
}
