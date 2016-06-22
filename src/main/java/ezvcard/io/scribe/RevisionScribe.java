package ezvcard.io.scribe;

import java.util.Date;
import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Revision;

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
 * Marshals {@link Revision} properties.
 * @author Michael Angstadt
 */
public class RevisionScribe extends VCardPropertyScribe<Revision> {
	public RevisionScribe() {
		super(Revision.class, "REV");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TIMESTAMP;
	}

	@Override
	protected String _writeText(Revision property, WriteContext context) {
		boolean extended = (context.getVersion() == VCardVersion.V3_0);
		return write(property, extended);
	}

	@Override
	protected Revision _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		return parse(value);
	}

	@Override
	protected void _writeXml(Revision property, XCardElement parent) {
		parent.append(VCardDataType.TIMESTAMP, write(property, false));
	}

	@Override
	protected Revision _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String value = element.first(VCardDataType.TIMESTAMP);
		if (value != null) {
			return parse(value);
		}

		throw missingXmlElements(VCardDataType.TIMESTAMP);
	}

	@Override
	protected Revision _parseHtml(HCardElement element, List<String> warnings) {
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
	protected JCardValue _writeJson(Revision property) {
		return JCardValue.single(write(property, true));
	}

	@Override
	protected Revision _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		String valueStr = value.asSingle();
		return parse(valueStr);
	}

	private String write(Revision property, boolean extended) {
		Date timestamp = property.getValue();
		if (timestamp == null) {
			return "";
		}

		return date(timestamp).time(true).utc(true).extended(extended).write();
	}

	private Revision parse(String value) {
		if (value == null || value.length() == 0) {
			return new Revision((Date) null);
		}

		try {
			return new Revision(date(value));
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(5);
		}
	}
}
