package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.parameter.VCardParameters;
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
 */

/**
 * Marshals {@link Email} properties.
 * @author Michael Angstadt
 */
public class EmailScribe extends StringPropertyScribe<Email> {
	public EmailScribe() {
		super(Email.class, "EMAIL");
	}

	@Override
	protected void _prepareParameters(Email property, VCardParameters copy, VCardVersion version, VCard vcard) {
		handlePrefParam(property, copy, version, vcard);
	}

	@Override
	protected Email _parseValue(String value) {
		return new Email(value);
	}

	@Override
	protected Email _parseHtml(HCardElement element, List<String> warnings) {
		String href = element.attr("href");
		String email = extractEmailFromHrefAttribute(href);
		if (email == null) {
			email = element.value();
		}

		Email property = new Email(email);

		List<String> types = element.types();
		property.getParameters().putAll(VCardParameters.TYPE, types);

		return property;
	}

	private static String extractEmailFromHrefAttribute(String value) {
		int colon = value.indexOf(':');
		if (colon < 0) {
			return null;
		}

		String scheme = value.substring(0, colon);
		return scheme.equalsIgnoreCase("mailto") ? value.substring(colon + 1) : null;
	}
}
