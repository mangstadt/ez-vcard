package ezvcard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Represents the value of a jCard property.
 * @author Michael Angstadt
 */
public class JCardValue {
	private JCardDataType dataType = JCardDataType.TEXT;
	private boolean structured = false;
	private final List<List<Object>> values = new ArrayList<List<Object>>();

	/**
	 * Helper constructor for creating a "text" value.
	 * @param values the text values
	 */
	public static JCardValue text(String... values) {
		JCardValue jcardValue = new JCardValue();
		jcardValue.dataType = JCardDataType.TEXT;
		if (values.length > 0) {
			jcardValue.addValues((Object[]) values);
		}
		return jcardValue;
	}

	/**
	 * Helper constructor for creating a "uri" value.
	 * @param uri the URI
	 */
	public static JCardValue uri(String uri) {
		JCardValue jcardValue = new JCardValue();
		jcardValue.dataType = JCardDataType.URI;
		if (uri != null) {
			jcardValue.addValues(uri);
		}
		return jcardValue;
	}

	public JCardDataType getDataType() {
		return dataType;
	}

	public void setDataType(JCardDataType dataType) {
		this.dataType = dataType;
	}

	public boolean isStructured() {
		return structured;
	}

	public void setStructured(boolean structured) {
		this.structured = structured;
	}

	public List<List<Object>> getValues() {
		return values;
	}

	public List<List<String>> getValuesAsStrings() {
		List<List<String>> valuesStr = new ArrayList<List<String>>(values.size());
		for (List<Object> value : values) {
			List<String> valueStr = new ArrayList<String>(value.size());
			for (Object v : value) {
				valueStr.add(v.toString());
			}
			valuesStr.add(valueStr);
		}
		return valuesStr;
	}

	public Object getFirstValue() {
		return getFirstValue(0);
	}

	public Object getFirstValue(int index) {
		if (index >= values.size()) {
			return null;
		}
		return values.get(index).get(0);
	}

	public String getFirstValueAsString() {
		return getFirstValueAsString(0);
	}

	public String getFirstValueAsString(int index) {
		Object value = getFirstValue(index);
		return (value == null) ? null : value.toString();
	}

	/**
	 * Adds one or more values to the jCard value. {@link List} objects that are
	 * passed into this method will be created as multi-valued components.
	 * @param values the value(s) to add
	 */
	@SuppressWarnings("unchecked")
	public void addValues(Object... values) {
		for (Object value : values) {
			if (value instanceof List) {
				this.values.add((List<Object>) value);
			} else {
				this.values.add(Arrays.asList(value));
			}
		}
	}
}
