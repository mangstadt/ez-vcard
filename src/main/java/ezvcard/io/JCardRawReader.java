package ezvcard.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import ezvcard.VCardSubTypes;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.JCardValue;
import ezvcard.util.JsonValue;

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
 * Parses an vCard JSON data stream (jCard).
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-kewisch-vcard-in-json-04">jCard
 * draft</a>
 */
public class JCardRawReader implements Closeable {
	private final Reader reader;
	private JsonParser jp;
	private boolean eof = false;
	private JCardDataStreamListener listener;

	/**
	 * Creates a new reader.
	 * @param reader the reader to the data stream
	 */
	public JCardRawReader(Reader reader) {
		this.reader = reader;
	}

	/**
	 * Gets the current line number.
	 * @return the line number
	 */
	public int getLineNum() {
		return (jp == null) ? 0 : jp.getCurrentLocation().getLineNr();
	}

	/**
	 * Reads the next vCard from the jCard data stream.
	 * @param listener handles the vCard data as it is read off the wire
	 * @throws JCardParseException if the jCard syntax is incorrect (the JSON
	 * syntax may be valid, but it is not in the correct jCard format).
	 * @throws JsonParseException if the JSON syntax is incorrect
	 * @throws IOException if there is a problem reading from the data stream
	 */
	public void readNext(JCardDataStreamListener listener) throws IOException {
		if (jp == null) {
			JsonFactory factory = new JsonFactory();
			jp = factory.createJsonParser(reader);
		} else if (jp.isClosed()) {
			return;
		}

		this.listener = listener;

		//find the next vCard object
		JsonToken prev = null;
		JsonToken cur;
		while ((cur = jp.nextToken()) != null) {
			if (prev == JsonToken.START_ARRAY && cur == JsonToken.VALUE_STRING && "vcard".equals(jp.getValueAsString())) {
				break;
			}
			prev = cur;
		}
		if (cur == null) {
			//EOF
			eof = true;
			return;
		}

		listener.beginVCard();
		parseProperties();
	}

	private void parseProperties() throws IOException {
		//start properties array
		if (jp.nextToken() != JsonToken.START_ARRAY) {
			throw new JCardParseException(JsonToken.START_ARRAY, jp.getCurrentToken());
		}

		//read properties
		while (jp.nextToken() != JsonToken.END_ARRAY) { //until we reach the end properties array
			if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
				throw new JCardParseException(JsonToken.START_ARRAY, jp.getCurrentToken());
			}
			jp.nextToken();
			parseProperty();
		}
	}

	private void parseProperty() throws IOException {
		//get property name
		if (jp.getCurrentToken() != JsonToken.VALUE_STRING) {
			throw new JCardParseException(JsonToken.VALUE_STRING, jp.getCurrentToken());
		}
		String propertyName = jp.getValueAsString().toLowerCase();

		//get parameters
		VCardSubTypes parameters = parseParameters();

		//get group
		List<String> removed = parameters.removeAll("group");
		String group = removed.isEmpty() ? null : removed.get(0);

		//get data type
		if (jp.nextToken() != JsonToken.VALUE_STRING) {
			throw new JCardParseException(JsonToken.VALUE_STRING, jp.getCurrentToken());
		}
		String dataTypeStr = jp.getText().toLowerCase();
		ValueParameter dataType = "unknown".equals(dataTypeStr) ? null : ValueParameter.get(dataTypeStr);

		//get property value(s)
		List<JsonValue> values = parseValues();

		JCardValue value = new JCardValue(dataType, values);
		listener.readProperty(group, propertyName, parameters, value);
	}

	private VCardSubTypes parseParameters() throws IOException {
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			throw new JCardParseException(JsonToken.START_OBJECT, jp.getCurrentToken());
		}

		VCardSubTypes parameters = new VCardSubTypes();
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String parameterName = jp.getText();

			if (jp.nextToken() == JsonToken.START_ARRAY) {
				//multi-valued parameter
				while (jp.nextToken() != JsonToken.END_ARRAY) {
					parameters.put(parameterName, jp.getText());
				}
			} else {
				parameters.put(parameterName, jp.getValueAsString());
			}
		}

		return parameters;
	}

	private List<JsonValue> parseValues() throws IOException {
		List<JsonValue> values = new ArrayList<JsonValue>();
		while (jp.nextToken() != JsonToken.END_ARRAY) { //until we reach the end of the property array
			JsonValue value = parseValue();
			values.add(value);
		}
		return values;
	}

	private Object parseValueElement() throws IOException {
		switch (jp.getCurrentToken()) {
		case VALUE_FALSE:
		case VALUE_TRUE:
			return jp.getBooleanValue();
		case VALUE_NUMBER_FLOAT:
			return jp.getDoubleValue();
		case VALUE_NUMBER_INT:
			return jp.getLongValue();
		case VALUE_NULL:
			return null;
		default:
			return jp.getText();
		}
	}

	private List<JsonValue> parseValueArray() throws IOException {
		List<JsonValue> array = new ArrayList<JsonValue>();

		while (jp.nextToken() != JsonToken.END_ARRAY) {
			JsonValue value = parseValue();
			array.add(value);
		}

		return array;
	}

	private Map<String, JsonValue> parseValueObject() throws IOException {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();

		jp.nextToken();
		while (jp.getCurrentToken() != JsonToken.END_OBJECT) {
			if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
				throw new JCardParseException(JsonToken.FIELD_NAME, jp.getCurrentToken());
			}

			String key = jp.getText();
			jp.nextToken();
			JsonValue value = parseValue();
			object.put(key, value);

			jp.nextToken();
		}

		return object;
	}

	private JsonValue parseValue() throws IOException {
		switch (jp.getCurrentToken()) {
		case START_ARRAY:
			return new JsonValue(parseValueArray());
		case START_OBJECT:
			return new JsonValue(parseValueObject());
		default:
			return new JsonValue(parseValueElement());
		}
	}

	/**
	 * Determines whether the end of the data stream has been reached.
	 * @return true if the end has been reached, false if not
	 */
	public boolean eof() {
		return eof;
	}

	/**
	 * Handles the vCard data as it is read off the data stream.
	 * @author Michael Angstadt
	 */
	public static interface JCardDataStreamListener {
		/**
		 * Called when a vCard has been found in the stream.
		 */
		void beginVCard();

		/**
		 * Called when a property is read.
		 * @param group the group or null if there is not group
		 * @param propertyName the property name (e.g. "summary")
		 * @param parameters the parameters
		 * @param value the property value
		 */
		void readProperty(String group, String propertyName, VCardSubTypes parameters, JCardValue value);
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
