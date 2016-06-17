package ezvcard.io.scribe;

import java.util.List;
import java.util.TimeZone;

import ezvcard.Messages;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Timezone;
import ezvcard.util.UtcOffset;

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
 * Marshals {@link Timezone} properties.
 * @author Michael Angstadt
 */
//@formatter:off
/* 
 * Parsing===================
 * 
 * vCard 2.1:
 * Parse as UTC offset.  If invalid, throw CannotParseException.
 * 
 * vCard 3.0, hCard:
 * VALUE=text:			Treat as text
 * No VALUE param:		Parse as UTC offset.  If invalid, add warning and treat as text.
 * 
 * vCard 4.0, jCard:
 * VALUE=text:			Treat as text
 * VALUE=utc-offset:	Parse as UTC offset.  If invalid, throw CannotParseException
 * VALUE=uri:			Not going to support this, as there is no description of what a timezone URI looks like
 * No VALUE param:		Parse as UTC offset.  If invalid, treat as text
 * 
 * xCard:
 * text	| utc-offset	| result
 * no	| no			| throw CannotParseException
 * yes	| no			| OK
 * no	| yes			| OK
 * no	| invalid		| throw CannotParseException
 * yes	| yes			| Parse text
 * yes	| invalid		| Parse text
 * 
 * Writing===================
 * 
 * vCard 2.1:
 * text	| utc-offset	| result
 * no	| no			| empty string (validation warning)
 * no	| yes			| Write UTC offset
 * yes	| no			| empty string (validation warning)
 * yes	| yes			| Write UTC offset
 * 
 * vCard 3.0:
 * text	| utc-offset	| result
 * no	| no			| empty string (validation warning)
 * no	| yes			| Write UTC offset
 * yes	| no			| Write text, add "VALUE=text" parameter
 * yes	| yes			| Write UTC offset
 * 
 * vCard 4.0, xCard, jCard:
 * text	| utc-offset	| result
 * no	| no			| empty string (validation warning)
 * no	| yes			| Write UTC offset, add "VALUE=utc-offset" parameter
 * yes	| no			| Write text
 * yes	| yes			| Write text
 */
//@formatter:on
public class TimezoneScribe extends VCardPropertyScribe<Timezone> {
	public TimezoneScribe() {
		super(Timezone.class, "TZ");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		switch (version) {
		case V2_1:
		case V3_0:
			return VCardDataType.UTC_OFFSET;
		case V4_0:
			return VCardDataType.TEXT;
		}
		return null;
	}

	@Override
	protected VCardDataType _dataType(Timezone property, VCardVersion version) {
		String text = property.getText();
		UtcOffset offset = property.getOffset();

		switch (version) {
		case V2_1:
			return VCardDataType.UTC_OFFSET;
		case V3_0:
			if (offset != null) {
				return VCardDataType.UTC_OFFSET;
			}
			if (text != null) {
				return VCardDataType.TEXT;
			}
			break;
		case V4_0:
			if (text != null) {
				return VCardDataType.TEXT;
			}
			if (offset != null) {
				return VCardDataType.UTC_OFFSET;
			}
			break;
		}

		return _defaultDataType(version);
	}

	@Override
	protected String _writeText(Timezone property, WriteContext context) {
		String text = property.getText();
		UtcOffset offset = property.getOffset();

		switch (context.getVersion()) {
		case V2_1:
			if (offset != null) {
				return offset.toString(false); //2.1 allows either basic or extended
			}

			if (text != null) {
				//attempt to find the offset by treating the text as a timezone ID, like "America/New_York"
				TimeZone timezone = timezoneFromId(text);
				if (timezone != null) {
					UtcOffset tzOffset = offsetFromTimezone(timezone);
					return tzOffset.toString(false);
				}
			}
			break;
		case V3_0:
			if (offset != null) {
				return offset.toString(true); //3.0 only allows extended
			}

			if (text != null) {
				return escape(text);
			}
			break;
		case V4_0:
			if (text != null) {
				return escape(text);
			}

			if (offset != null) {
				return offset.toString(false); //4.0 only allows basic
			}
			break;
		}

		return "";
	}

	@Override
	protected Timezone _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value, dataType, version, warnings);
	}

	@Override
	protected void _writeXml(Timezone property, XCardElement parent) {
		String text = property.getText();
		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}

		UtcOffset offset = property.getOffset();
		if (offset != null) {
			parent.append(VCardDataType.UTC_OFFSET, offset.toString(false));
			return;
		}

		parent.append(VCardDataType.TEXT, "");
	}

	@Override
	protected Timezone _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String text = element.first(VCardDataType.TEXT);
		if (text != null) {
			return new Timezone(text);
		}

		String utcOffset = element.first(VCardDataType.UTC_OFFSET);
		if (utcOffset != null) {
			try {
				return new Timezone(UtcOffset.parse(utcOffset));
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(19);
			}
		}

		throw missingXmlElements(VCardDataType.TEXT, VCardDataType.UTC_OFFSET);
	}

	@Override
	protected Timezone _parseHtml(HCardElement element, List<String> warnings) {
		return parse(element.value(), null, VCardVersion.V3_0, warnings);
	}

	@Override
	protected JCardValue _writeJson(Timezone property) {
		String text = property.getText();
		if (text != null) {
			return JCardValue.single(text);
		}

		UtcOffset offset = property.getOffset();
		if (offset != null) {
			return JCardValue.single(offset.toString(true));
		}

		return JCardValue.single("");
	}

	@Override
	protected Timezone _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		String valueStr = value.asSingle();
		return parse(valueStr, dataType, VCardVersion.V4_0, warnings);
	}

	private Timezone parse(String value, VCardDataType dataType, VCardVersion version, List<String> warnings) {
		if (value == null || value.length() == 0) {
			return new Timezone((String) null);
		}

		switch (version) {
		case V2_1:
			//e.g. "-05:00"
			try {
				return new Timezone(UtcOffset.parse(value));
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(19);
			}
		case V3_0:
		case V4_0:
			try {
				return new Timezone(UtcOffset.parse(value));
			} catch (IllegalArgumentException e) {
				if (dataType == VCardDataType.UTC_OFFSET) {
					warnings.add(Messages.INSTANCE.getParseMessage(20));
				}
				return new Timezone(value);
			}
		}

		return new Timezone((String) null);
	}

	private UtcOffset offsetFromTimezone(TimeZone timezone) {
		long offsetMs = timezone.getOffset(System.currentTimeMillis());
		return new UtcOffset(offsetMs);
	}

	private TimeZone timezoneFromId(String id) {
		TimeZone timezone = TimeZone.getTimeZone(id);
		return "GMT".equals(timezone.getID()) ? null : timezone;
	}
}
