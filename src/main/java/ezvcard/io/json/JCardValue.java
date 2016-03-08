package ezvcard.io.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ezvcard.property.Categories;
import ezvcard.property.Note;
import ezvcard.property.StructuredName;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Holds the data type and value of a jCard property.
 * @author Michael Angstadt
 */
public class JCardValue {
	private final List<JsonValue> values;

	/**
	 * Creates a new jCard value.
	 * @param values the values
	 */
	public JCardValue(List<JsonValue> values) {
		this.values = Collections.unmodifiableList(values);
	}

	/**
	 * Creates a new jCard value.
	 * @param values the values
	 */
	public JCardValue(JsonValue... values) {
		this.values = Arrays.asList(values); //unmodifiable
	}

	/**
	 * Creates a single-valued value.
	 * @param value the value
	 * @return the jCard value
	 */
	public static JCardValue single(Object value) {
		return new JCardValue(new JsonValue(value));
	}

	/**
	 * Creates a multi-valued value.
	 * @param values the values
	 * @return the jCard value
	 */
	public static JCardValue multi(Object... values) {
		return multi(Arrays.asList(values));
	}

	/**
	 * Creates a multi-valued value.
	 * @param values the values
	 * @return the jCard value
	 */
	public static JCardValue multi(List<?> values) {
		List<JsonValue> multiValues = new ArrayList<JsonValue>(values.size());
		for (Object value : values) {
			multiValues.add(new JsonValue(value));
		}
		return new JCardValue(multiValues);
	}

	/**
	 * <p>
	 * Creates a structured value.
	 * </p>
	 * <p>
	 * This method accepts a vararg of {@link Object} instances. {@link List}
	 * objects will be treated as multi-valued components. Null objects will be
	 * treated as empty components.
	 * </p>
	 * @param values the values
	 * @return the jCard value
	 */
	public static JCardValue structured(Object... values) {
		List<List<?>> valuesList = new ArrayList<List<?>>(values.length);
		for (Object value : values) {
			List<?> list = (value instanceof List) ? (List<?>) value : Arrays.asList(value);
			valuesList.add(list);
		}
		return structured(valuesList);
	}

	/**
	 * Creates a structured value.
	 * @param values the values
	 * @return the jCard value
	 */
	public static JCardValue structured(List<List<?>> values) {
		List<JsonValue> array = new ArrayList<JsonValue>(values.size());

		for (List<?> list : values) {
			if (list.isEmpty()) {
				array.add(new JsonValue(""));
				continue;
			}

			if (list.size() == 1) {
				Object value = list.get(0);
				if (value == null) {
					value = "";
				}
				array.add(new JsonValue(value));
				continue;
			}

			List<JsonValue> subArray = new ArrayList<JsonValue>(list.size());
			for (Object value : list) {
				if (value == null) {
					value = "";
				}
				subArray.add(new JsonValue(value));
			}
			array.add(new JsonValue(subArray));
		}

		return new JCardValue(new JsonValue(array));
	}

	/**
	 * Gets all the JSON values.
	 * @return the JSON values
	 */
	public List<JsonValue> getValues() {
		return values;
	}

	/**
	 * Gets the value of a single-valued property (such as {@link Note}).
	 * @return the value or empty string if not found
	 */
	public String asSingle() {
		if (values.isEmpty()) {
			return "";
		}

		JsonValue first = values.get(0);
		if (first.isNull()) {
			return "";
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

		return "";
	}

	/**
	 * Gets the value of a structured property (such as
	 * {@link StructuredName}).
	 * @return the values or empty list if not found
	 */
	public List<List<String>> asStructured() {
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
					valuesStr.add(Arrays.asList(""));
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
							subValuesStr.add("");
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
			values.add(Arrays.asList(""));
			return values;
		}

		return Collections.emptyList();
	}

	/**
	 * Gets the value of a multi-valued property (such as {@link Categories}
	 * ).
	 * @return the values or empty list if not found
	 */
	public List<String> asMulti() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> multi = new ArrayList<String>(values.size());
		for (JsonValue value : values) {
			if (value.isNull()) {
				multi.add("");
				continue;
			}

			Object obj = value.getValue();
			if (obj != null) {
				multi.add(obj.toString());
				continue;
			}
		}
		return multi;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JCardValue that = (JCardValue) o;

		if (values != null ? !values.equals(that.values) : that.values != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return values != null ? values.hashCode() : 0;
	}
}
