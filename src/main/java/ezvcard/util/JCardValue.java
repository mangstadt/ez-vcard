package ezvcard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.types.CategoriesType;
import ezvcard.types.NoteType;
import ezvcard.types.StructuredNameType;

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
 * Holds the data type and value of a jCal property.
 * @author Michael Angstadt
 */
public class JCardValue {
	private final VCardDataType dataType;
	private final List<JsonValue> values;

	/**
	 * Creates a new jCal value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 */
	public JCardValue(VCardDataType dataType, List<JsonValue> values) {
		this.dataType = dataType;
		this.values = Collections.unmodifiableList(values);
	}

	/**
	 * Creates a new jCal value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 */
	public JCardValue(VCardDataType dataType, JsonValue... values) {
		this.dataType = dataType;
		this.values = Arrays.asList(values); //unmodifiable
	}

	/**
	 * Creates a single-valued value.
	 * @param dataType the data type or null for "unknown"
	 * @param value the value
	 * @return the jCal value
	 */
	public static JCardValue single(VCardDataType dataType, Object value) {
		return new JCardValue(dataType, new JsonValue(value));
	}

	/**
	 * Creates a multi-valued value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCardValue multi(VCardDataType dataType, Object... values) {
		return multi(dataType, Arrays.asList(values));
	}

	/**
	 * Creates a multi-valued value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCardValue multi(VCardDataType dataType, List<?> values) {
		List<JsonValue> multiValues = new ArrayList<JsonValue>(values.size());
		for (Object value : values) {
			multiValues.add(new JsonValue(value));
		}
		return new JCardValue(dataType, multiValues);
	}

	/**
	 * Creates a structured value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCardValue structured(VCardDataType dataType, Object... values) {
		List<List<?>> valuesList = new ArrayList<List<?>>(values.length);
		for (Object value : values) {
			valuesList.add(Arrays.asList(value));
		}
		return structured(dataType, valuesList);
	}

	/**
	 * Creates a structured value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCardValue structured(VCardDataType dataType, List<List<?>> values) {
		List<JsonValue> array = new ArrayList<JsonValue>(values.size());
		for (List<?> list : values) {
			if (list.isEmpty()) {
				array.add(new JsonValue(""));
			} else if (list.size() == 1) {
				Object value = list.get(0);
				if (value == null) {
					value = "";
				}
				array.add(new JsonValue(value));
			} else {
				List<JsonValue> subArray = new ArrayList<JsonValue>(list.size());
				for (Object value : list) {
					if (value == null) {
						value = "";
					}
					subArray.add(new JsonValue(value));
				}
				array.add(new JsonValue(subArray));
			}
		}
		return new JCardValue(dataType, new JsonValue(array));
	}

	/**
	 * Gets the jCard data type
	 * @return the data type or null for "unknown"
	 */
	public VCardDataType getDataType() {
		return dataType;
	}

	/**
	 * Gets all the JSON values.
	 * @return the JSON values
	 */
	public List<JsonValue> getValues() {
		return values;
	}

	/**
	 * Gets the value of a single-valued property (such as {@link NoteType}).
	 * @return the value or null if not found
	 */
	public String getSingleValued() {
		if (values.isEmpty()) {
			return null;
		}

		JsonValue first = values.get(0);

		if (first.isNull()) {
			return null;
		}

		Object obj = first.getValue();
		if (obj != null) {
			return obj.toString();
		}

		//get the first element of the array
		List<JsonValue> array = first.getArray();
		if (array != null && !array.isEmpty()) {
			obj = array.get(0).getValue();
			if (obj != null) {
				return obj.toString();
			}
		}

		return null;
	}

	/**
	 * Gets the value of a structured property (such as
	 * {@link StructuredNameType}).
	 * @return the values or empty list if not found
	 */
	public List<List<String>> getStructured() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		JsonValue first = values.get(0);

		//["gender", {}, "text", ["M", "text"] ]
		List<JsonValue> array = first.getArray();
		if (array != null) {
			List<List<String>> valuesStr = new ArrayList<List<String>>(array.size());
			for (JsonValue value : array) {
				if (value.isNull()) {
					valuesStr.add(Arrays.asList((String) null));
					continue;
				}

				Object obj = value.getValue();
				if (obj != null) {
					valuesStr.add(Arrays.asList(obj.toString()));
					continue;
				}

				List<JsonValue> subArray = value.getArray();
				if (subArray != null) {
					List<String> subValuesStr = new ArrayList<String>(subArray.size());
					for (JsonValue subArrayValue : subArray) {
						if (subArrayValue.isNull()) {
							subValuesStr.add(null);
							continue;
						}

						obj = subArrayValue.getValue();
						if (obj != null) {
							subValuesStr.add(obj.toString());
							continue;
						}
					}
					valuesStr.add(subValuesStr);
				}
			}
			return valuesStr;
		}

		//get the first value if it's not enclosed in an array
		//["gender", {}, "text", "M"]
		Object obj = first.getValue();
		if (obj != null) {
			List<List<String>> values = new ArrayList<List<String>>(1);
			values.add(Arrays.asList(obj.toString()));
			return values;
		}

		//["gender", {}, "text", null]
		if (first.isNull()) {
			List<List<String>> values = new ArrayList<List<String>>(1);
			values.add(Arrays.asList((String) null));
			return values;
		}

		return Collections.emptyList();
	}

	/**
	 * Gets the value of a multi-valued property (such as {@link CategoriesType}
	 * ).
	 * @return the values or empty list if not found
	 */
	public List<String> getMultivalued() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> multi = new ArrayList<String>(values.size());
		for (JsonValue value : values) {
			if (value.isNull()) {
				multi.add(null);
				continue;
			}

			Object obj = value.getValue();
			if (obj != null) {
				multi.add(obj.toString());
			}
		}
		return multi;
	}
}
