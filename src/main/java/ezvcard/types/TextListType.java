package ezvcard.types;

import java.util.ArrayList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.VCardStringUtils.JoinCallback;
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
 * Represents a type whose value is a list of textual values.
 * @author Michael Angstadt
 */
public class TextListType extends VCardType {
	protected List<String> values = new ArrayList<String>();
	private final char separator;

	/**
	 * Creates a property that contains a list of textual values.
	 * @param name the type name (e.g. "NICKNAME")
	 * @param separator the delimiter to use (e.g. ",")
	 */
	public TextListType(String name, char separator) {
		super(name);
		this.separator = separator;
	}

	/**
	 * Gest the list of values.
	 * @return the list of values
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Adds a value to the list.
	 * @param value the value to add
	 */
	public void addValue(String value) {
		values.add(value);
	}

	/**
	 * Removes a value from the list.
	 * @param value the value to remove
	 */
	public void removeValue(String value) {
		values.remove(value);
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		VCardStringUtils.join(values, separator + "", sb, new JoinCallback<String>() {
			public void handle(StringBuilder sb, String value) {
				sb.append(VCardStringUtils.escape(value));
			}
		});
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		values = VCardStringUtils.splitBy(value, separator, true, true);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		parent.append("text", values);
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		values.clear();
		values.addAll(element.all(VCardDataType.TEXT));
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		Object[] values = this.values.toArray(new Object[0]);
		if (separator == ';' && values.length > 1) {
			return JCardValue.structured(VCardDataType.TEXT, values);
		}
		return JCardValue.multi(VCardDataType.TEXT, values);
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		values.clear();
		if (separator == ';') {
			for (List<String> valueStr : value.getStructured()) {
				if (!valueStr.isEmpty()) {
					values.add(valueStr.get(0));
				}
			}
		} else {
			for (String valueStr : value.getMultivalued()) {
				values.add(valueStr);
			}
		}
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (values.isEmpty()) {
			warnings.add("Property value is empty.");
		}
	}
}
