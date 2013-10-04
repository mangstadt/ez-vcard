package ezvcard.types.scribes;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.EmailType;
import ezvcard.util.HCardElement;

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
 */

/**
 * Marshals {@link EmailType} properties.
 * @author Michael Angstadt
 */
public class EmailScribe extends StringPropertyScribe<EmailType> {
	public EmailScribe() {
		super(EmailType.class, "EMAIL");
	}

	@Override
	protected void _prepareParameters(EmailType property, VCardSubTypes copy, VCardVersion version, VCard vcard) {
		handlePrefParam(property, copy, version, vcard);
	}

	@Override
	protected EmailType _parseValue(String value) {
		return new EmailType(value);
	}

	@Override
	protected EmailType _parseHtml(HCardElement element, List<String> warnings) {
		//check to see if the email address is within in "mailto:" link
		String email;
		String href = element.attr("href");
		if (href.length() > 0) {
			Pattern p = Pattern.compile("^mailto:(.*)$", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(href);
			email = m.find() ? m.group(1) : element.value();
		} else {
			email = element.value();
		}

		EmailType property = new EmailType(email);

		//add TYPE parameters
		List<String> types = element.types();
		for (String type : types) {
			property.getSubTypes().addType(type);
		}

		return property;
	}
}
