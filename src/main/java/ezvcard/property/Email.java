package ezvcard.property;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.Pid;

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
 * <p>
 * Defines an email address associated with the person.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Email email = new Email("johndoe@hotmail.com");
 * email.getTypes().add(EmailType.HOME);
 * vcard.addEmail(email);
 * 
 * email = new Email("jdoe@company.com");
 * email.getTypes().add(EmailType.WORK);
 * email.setPref(1); //the most preferred email
 * vcard.addEmail(email);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code EMAIL}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350 p.36</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426 p.15</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
 */
public class Email extends TextProperty implements HasAltId {
	/**
	 * Creates an email property.
	 * @param email the email (e.g. "johndoe@example.com")
	 */
	public Email(String email) {
		super(email);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Email(Email original) {
		super(original);
	}

	/**
	 * Gets the list that stores this property's email types (TYPE parameters).
	 * @return the email types (this list is mutable) (e.g. "INTERNET", "WORK")
	 */
	public List<EmailType> getTypes() {
		return parameters.new TypeParameterList<EmailType>() {
			@Override
			protected EmailType _asObject(String value) {
				return EmailType.get(value);
			}
		};
	}

	@Override
	public List<Pid> getPids() {
		return super.getPids();
	}

	@Override
	public Integer getPref() {
		return super.getPref();
	}

	@Override
	public void setPref(Integer pref) {
		super.setPref(pref);
	}

	//@Override
	public String getAltId() {
		return parameters.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		parameters.setAltId(altId);
	}

	@Override
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		super._validate(warnings, version, vcard);

		for (EmailType type : getTypes()) {
			if (type == EmailType.PREF) {
				//ignore because it is converted to a PREF parameter for 4.0 vCards
				continue;
			}
			if (!type.isSupportedBy(version)) {
				warnings.add(new ValidationWarning(9, type.getValue()));
			}
		}
	}

	@Override
	public Email copy() {
		return new Email(this);
	}
}
