package ezvcard.types;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;

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
 * An email address associated with a person.
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * EmailType email = new EmailType(&quot;superdude55@hotmail.com&quot;);
 * email.addType(EmailTypeParameter.HOME);
 * vcard.addEmail(email);
 * 
 * email = new EmailType(&quot;doe.john@company.com&quot;);
 * email.addType(EmailTypeParameter.WORK);
 * email.setPref(1); //the most preferred email
 * vcard.addEmail(email);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>EMAIL</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class EmailType extends MultiValuedTypeParameterType<EmailTypeParameter> implements HasAltId {
	public static final String NAME = "EMAIL";

	private String value;

	/**
	 * Creates an empty email property.
	 */
	public EmailType() {
		this(null);
	}

	/**
	 * Creates an email property.
	 * @param email the email (e.g. "johndoe@example.com")
	 */
	public EmailType(String email) {
		super(NAME);
		setValue(email);
	}

	@Override
	protected EmailTypeParameter buildTypeObj(String type) {
		return EmailTypeParameter.get(type);
	}

	/**
	 * Gets the email address.
	 * @return the email address (e.g. "johndoe@example.com")
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the email address
	 * @param email the email address (e.g. "johndoe@example.com")
	 */
	public void setValue(String email) {
		this.value = email;
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
		return subTypes.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		//replace "TYPE=pref" with "PREF=1"
		if (version == VCardVersion.V4_0) {
			if (getTypes().contains(EmailTypeParameter.PREF)) {
				copy.removeType(EmailTypeParameter.PREF.getValue());
				copy.setPref(1);
			}
		} else {
			copy.setPref(null);

			//find the EMAIL with the lowest PREF value in the vCard
			EmailType mostPreferred = null;
			for (EmailType email : vcard.getEmails()) {
				Integer pref = email.getPref();
				if (pref != null) {
					if (mostPreferred == null || pref < mostPreferred.getPref()) {
						mostPreferred = email;
					}
				}
			}
			if (this == mostPreferred) {
				copy.addType(EmailTypeParameter.PREF.getValue());
			}
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		sb.append(VCardStringUtils.escape(getValue()));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		setValue(VCardStringUtils.unescape(value));
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		parent.append(ValueParameter.TEXT, getValue());
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		setValue(element.first(ValueParameter.TEXT));
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		List<String> types = element.types();
		for (String type : types) {
			subTypes.addType(type);
		}

		//check to see if the email address is within in "mailto:" link
		String email = null;
		String href = element.attr("href");
		if (href.length() > 0) {
			Pattern p = Pattern.compile("^mailto:(.*)$", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(href);
			if (m.find()) {
				email = m.group(1);
			}
		}
		if (email == null) {
			email = element.value();
		}
		setValue(email);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		return JCardValue.single(JCardDataType.TEXT, getValue());
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		setValue(value.getSingleValued());
	}
}
