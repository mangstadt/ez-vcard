package ezvcard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
	 * Creates a "text" value.
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
	 * Creates an empty "uri" value.
	 */
	public static JCardValue uri() {
		return uri(null);
	}

	/**
	 * Creates a "uri" value.
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

	/**
	 * Creates an empty "date" value.
	 */
	public static JCardValue date() {
		return date(null);
	}

	/**
	 * Creates a "date" value.
	 * @param date the date
	 */
	public static JCardValue date(Date date) {
		JCardValue jcardValue = new JCardValue();
		jcardValue.dataType = JCardDataType.DATE;
		if (date != null) {
			jcardValue.addValues(VCardDateFormatter.format(date, ISOFormat.DATE_EXTENDED));
		}
		return jcardValue;
	}

	/**
	 * Creates an empty "datetime" value.
	 */
	public static JCardValue dateTime() {
		return dateTime(null);
	}

	/**
	 * Creates a "datetime" value.
	 * @param date the date
	 */
	public static JCardValue dateTime(Date date) {
		JCardValue jcardValue = new JCardValue();
		jcardValue.dataType = JCardDataType.DATE_TIME;
		if (date != null) {
			jcardValue.addValues(VCardDateFormatter.format(date, ISOFormat.TIME_EXTENDED));
		}
		return jcardValue;
	}

	/**
	 * Creates an empty "timestamp" value.
	 */
	public static JCardValue timestamp() {
		return timestamp(null);
	}

	/**
	 * Creates a a "timestamp" value.
	 * @param timestamp the timestamp
	 */
	public static JCardValue timestamp(Date timestamp) {
		JCardValue jcardValue = new JCardValue();
		jcardValue.dataType = JCardDataType.TIMESTAMP;
		if (timestamp != null) {
			jcardValue.addValues(VCardDateFormatter.format(timestamp, ISOFormat.UTC_TIME_EXTENDED));
		}
		return jcardValue;
	}

	/**
	 * Creates an empty "utc-offset" value.
	 */
	public static JCardValue utcOffset() {
		return utcOffset(null, null);
	}

	/**
	 * Creates a "utc-offset" value.
	 * @param hourOffset the hour offset (e.g. -5)
	 * @param minuteOffset the minute offset (e.g. 0)
	 */
	public static JCardValue utcOffset(Integer hourOffset, Integer minuteOffset) {
		JCardValue jcardValue = new JCardValue();
		jcardValue.dataType = JCardDataType.UTC_OFFSET;
		if (hourOffset != null) {
			String offset = VCardDateFormatter.formatTimeZone(hourOffset, minuteOffset, true);
			jcardValue.addValues(offset);
		}
		return jcardValue;
	}

	/**
	 * Gets the jCard data type
	 * @return the data type (e.g. "text")
	 */
	public JCardDataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the jCard data type.
	 * @param dataType the data type (e.g. "text")
	 */
	public void setDataType(JCardDataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * Gets whether the value is a structured value.
	 * @return true if it's structured, false if not
	 */
	public boolean isStructured() {
		return structured;
	}

	/**
	 * Sets whether the value is a structured value
	 * @param structured true if it's structured, false if not
	 */
	public void setStructured(boolean structured) {
		this.structured = structured;
	}

	/**
	 * Gets all the values.
	 * @return the values
	 */
	public List<List<Object>> getValues() {
		return values;
	}

	/**
	 * Gets all the values as strings.
	 * @return the values as strings
	 */
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

	/**
	 * Gets the first value at the first index.
	 * @return the value or null if there are no values
	 */
	public Object getFirstValue() {
		return getFirstValue(0);
	}

	/**
	 * Gets the first value at the given index.
	 * @param index the index
	 * @return the value or null if the specified index does not exist
	 */
	public Object getFirstValue(int index) {
		if (index >= values.size()) {
			return null;
		}
		return values.get(index).get(0);
	}

	/**
	 * Gets the first value at the first index as a string.
	 * @return the value or null if there are no values
	 */
	public String getFirstValueAsString() {
		return getFirstValueAsString(0);
	}

	/**
	 * Gets the first value at the given index as a string.
	 * @param index the index
	 * @return the value or null if the specified index does not exist
	 */
	public String getFirstValueAsString(int index) {
		Object value = getFirstValue(index);
		return (value == null) ? null : value.toString();
	}

	/**
	 * Adds one or more values to the jCard value. {@link List} objects that are
	 * passed into this method will be treated as multi-valued components.
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
