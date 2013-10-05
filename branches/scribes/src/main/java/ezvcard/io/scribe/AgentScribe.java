package ezvcard.io.scribe;

import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.property.AgentType;
import ezvcard.property.VCardType;
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
 * Marshals {@link AgentType} properties.
 * @author Michael Angstadt
 */
public class AgentScribe extends VCardPropertyScribe<AgentType> {
	public AgentScribe() {
		super(AgentType.class, "AGENT");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return null;
	}

	@Override
	protected VCardDataType _dataType(AgentType property, VCardVersion version) {
		if (property.getUrl() != null) {
			return (version == VCardVersion.V2_1) ? VCardDataType.URL : VCardDataType.URI;
		}
		return null;
	}

	@Override
	protected String _writeText(AgentType property, VCardVersion version) {
		String url = property.getUrl();
		if (url != null) {
			return url;
		}

		VCard vcard = property.getVCard();
		if (vcard != null) {
			throw new EmbeddedVCardException(vcard);
		}

		//don't write an empty value because parsers could interpret that as there being an embedded vCard on the next line
		throw new SkipMeException("Property has neither a URL nor an embedded vCard.");
	}

	@Override
	protected AgentType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		AgentType property = new AgentType();

		if (dataType == null) {
			throw new EmbeddedVCardException(new Injector(property));
		}

		property.setUrl(unescape(value));
		return property;
	}

	@Override
	protected AgentType _parseHtml(HCardElement element, List<String> warnings) {
		AgentType property = new AgentType();

		Set<String> classes = element.classNames();
		if (classes.contains("vcard")) {
			throw new EmbeddedVCardException(new Injector(property));
		}

		String url = element.absUrl("href");
		if (url.length() == 0) {
			url = element.value();
		}
		property.setUrl(url);

		return property;
	}

	private static class Injector implements EmbeddedVCardException.InjectionCallback {
		private final AgentType property;

		public Injector(AgentType property) {
			this.property = property;
		}

		public void injectVCard(VCard vcard) {
			property.setVCard(vcard);
		}

		public VCardType getProperty() {
			return property;
		}
	}
}
