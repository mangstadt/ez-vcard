package ezvcard.io.scribe;

import java.time.temporal.Temporal;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.ParseContext;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.util.PartialDate;
import ezvcard.util.VCardDateFormat;

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
 */

/**
 * Marshals properties with date-time values.
 * @author Michael Angstadt
 * @param <T> the property class
 */
public abstract class DateOrTimePropertyScribe<T extends DateOrTimeProperty> extends VCardPropertyScribe<T> {
	public DateOrTimePropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		switch (version) {
		case V2_1:
		case V3_0:
			return null;
		case V4_0:
			return VCardDataType.DATE_AND_OR_TIME;
		}
		return null;
	}

	@Override
	protected VCardDataType _dataType(T property, VCardVersion version) {
		switch (version) {
		case V2_1:
		case V3_0:
			return null;
		case V4_0:
			if (property.getText() != null) {
				return VCardDataType.TEXT;
			}
			if (property.getDate() != null) {
				return VCardDateFormat.hasTime(property.getDate()) ? VCardDataType.DATE_TIME : VCardDataType.DATE;
			}
			if (property.getPartialDate() != null) {
				return property.getPartialDate().hasTimeComponent() ? VCardDataType.DATE_TIME : VCardDataType.DATE;
			}
			return VCardDataType.DATE_AND_OR_TIME;
		}
		return null;
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		VCardVersion version = context.getVersion();
		Temporal date = property.getDate();
		if (date != null) {
			boolean extended = (version == VCardVersion.V3_0);
			return date(date).extended(extended).write();
		}

		if (version == VCardVersion.V4_0) {
			String text = property.getText();
			if (text != null) {
				return VObjectPropertyValues.escape(text);
			}

			PartialDate partialDate = property.getPartialDate();
			if (partialDate != null) {
				return partialDate.toISO8601(false);
			}
		}

		return "";
	}

	@Override
	protected T _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		value = VObjectPropertyValues.unescape(value);
		if (context.getVersion() == VCardVersion.V4_0 && dataType == VCardDataType.TEXT) {
			return newInstance(value);
		}

		return parse(value, context);
	}

	@Override
	protected void _writeXml(T property, XCardElement parent) {
		Temporal date = property.getDate();
		if (date != null) {
			String value = date(date).extended(false).write();
			VCardDataType dataType = VCardDateFormat.hasTime(date) ? VCardDataType.DATE_TIME : VCardDataType.DATE;
			parent.append(dataType, value);
			return;
		}

		PartialDate partialDate = property.getPartialDate();
		if (partialDate != null) {
			VCardDataType dataType;
			if (partialDate.hasTimeComponent() && partialDate.hasDateComponent()) {
				dataType = VCardDataType.DATE_TIME;
			} else if (partialDate.hasTimeComponent()) {
				dataType = VCardDataType.TIME;
			} else if (partialDate.hasDateComponent()) {
				dataType = VCardDataType.DATE;
			} else {
				dataType = VCardDataType.DATE_AND_OR_TIME;
			}

			parent.append(dataType, partialDate.toISO8601(false));
			return;
		}

		String text = property.getText();
		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}

		parent.append(VCardDataType.DATE_AND_OR_TIME, "");
	}

	@Override
	protected T _parseXml(XCardElement element, VCardParameters parameters, ParseContext context) {
		String value = element.first(VCardDataType.DATE, VCardDataType.DATE_TIME, VCardDataType.DATE_AND_OR_TIME);
		if (value != null) {
			return parse(value, context);
		}

		value = element.first(VCardDataType.TEXT);
		if (value != null) {
			return newInstance(value);
		}

		throw missingXmlElements(VCardDataType.DATE, VCardDataType.DATE_TIME, VCardDataType.DATE_AND_OR_TIME, VCardDataType.TEXT);
	}

	@Override
	protected T _parseHtml(HCardElement element, ParseContext context) {
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
		return parse(value, context);
	}

	@Override
	protected JCardValue _writeJson(T property) {
		Temporal date = property.getDate();
		if (date != null) {
			String value = date(date).extended(true).write();
			return JCardValue.single(value);
		}

		PartialDate partialDate = property.getPartialDate();
		if (partialDate != null) {
			String value = partialDate.toISO8601(true);
			return JCardValue.single(value);
		}

		String text = property.getText();
		if (text != null) {
			return JCardValue.single(text);
		}

		return JCardValue.single("");
	}

	@Override
	protected T _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		String valueStr = value.asSingle();
		if (dataType == VCardDataType.TEXT) {
			return newInstance(valueStr);
		}

		return parse(valueStr, context);
	}

	private T parse(String value, ParseContext context) {
		try {
			return newInstance(date(value));
		} catch (IllegalArgumentException e) {
			if (context.getVersion() == VCardVersion.V2_1 || context.getVersion() == VCardVersion.V3_0) {
				throw new CannotParseException(5);
			}

			try {
				return newInstance(PartialDate.parse(value));
			} catch (IllegalArgumentException e2) {
				context.addWarning(6);
				return newInstance(value);
			}
		}
	}

	protected abstract T newInstance(String text);

	protected abstract T newInstance(Temporal date);

	protected abstract T newInstance(PartialDate partialDate);
}
