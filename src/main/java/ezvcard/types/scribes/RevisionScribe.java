package ezvcard.types.scribes;

import java.util.Date;
import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.types.RevisionType;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
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
 */

/**
 * Marshals {@link RevisionType} properties.
 * @author Michael Angstadt
 */
public class RevisionScribe extends VCardPropertyScribe<RevisionType> {
	public RevisionScribe() {
		super(RevisionType.class, "REV");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TIMESTAMP;
	}

	@Override
	protected String _writeText(RevisionType property, VCardVersion version) {
		return write(property, false);
	}

	@Override
	protected RevisionType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		return parse(value);
	}

	@Override
	protected void _writeXml(RevisionType property, XCardElement parent) {
		parent.append(VCardDataType.TIMESTAMP, write(property, false));
	}

	@Override
	protected RevisionType _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		String value = element.first(VCardDataType.TIMESTAMP);
		if (value != null) {
			return parse(value);
		}

		throw missingXmlElements(VCardDataType.TIMESTAMP);
	}

	@Override
	protected RevisionType _parseHtml(HCardElement element, List<String> warnings) {
		String value = null;
		if ("time".equals(element.tagName())) {
			String datetime = element.attr("datetime");
			if (datetime.length() > 0) {
				value = datetime;
			}
		}
		if (value == null) {
			value = element.value();
		}

		return parse(value);
	}

	@Override
	protected JCardValue _writeJson(RevisionType property) {
		return JCardValue.single(VCardDataType.TIMESTAMP, write(property, true));
	}

	@Override
	protected RevisionType _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		String valueStr = value.asSingle();
		return parse(valueStr);
	}

	private String write(RevisionType property, boolean extended) {
		Date timestamp = property.getTimestamp();
		if (timestamp == null) {
			return "";
		}

		return date(timestamp).time(true).utc(true).extended(extended).write();
	}

	private RevisionType parse(String value) {
		if (value == null || value.length() == 0) {
			return new RevisionType(null);
		}

		try {
			return new RevisionType(date(value));
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("Could not parse timestamp.");
		}
	}
}
