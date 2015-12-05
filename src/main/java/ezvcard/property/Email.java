package ezvcard.property;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.parameter.EmailType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Email email = new Email(&quot;johndoe@hotmail.com&quot;);
 * email.addType(EmailType.HOME);
 * vcard.addEmail(email);
 * 
 * email = new Email(&quot;jdoe@company.com&quot;);
 * email.addType(EmailType.WORK);
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
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Email extends TextProperty implements HasAltId {
	/**
	 * Creates an email property.
	 * @param email the email (e.g. "johndoe@example.com")
	 */
	public Email(String email) {
		super(email);
	}

	/**
	 * Gets all the TYPE parameters.
	 * @return the TYPE parameters or empty set if there are none
	 */
	public Set<EmailType> getTypes() {
		Set<String> values = parameters.getTypes();
		Set<EmailType> types = new HashSet<EmailType>(values.size());
		for (String value : values) {
			types.add(EmailType.get(value));
		}
		return types;
	}

	/**
	 * Adds a TYPE parameter.
	 * @param type the TYPE parameter to add
	 */
	public void addType(EmailType type) {
		parameters.addType(type.getValue());
	}

	/**
	 * Removes a TYPE parameter.
	 * @param type the TYPE parameter to remove
	 */
	public void removeType(EmailType type) {
		parameters.removeType(type.getValue());
	}

	@Override
	public List<Integer[]> getPids() {
		return super.getPids();
	}

	@Override
	public void addPid(int localId, int clientPidMapRef) {
		super.addPid(localId, clientPidMapRef);
	}

	@Override
	public void removePids() {
		super.removePids();
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
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		super._validate(warnings, version, vcard);

		for (EmailType type : getTypes()) {
			if (type == EmailType.PREF) {
				//ignore because it is converted to a PREF parameter for 4.0 vCards
				continue;
			}
			if (!type.isSupported(version)) {
				warnings.add(new Warning(9, type.getValue()));
			}
		}
	}
}
