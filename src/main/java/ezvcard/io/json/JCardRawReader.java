package ezvcard.io.json;

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

import ezvcard.VCardDataType;
import ezvcard.parameter.VCardParameters;

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
 * Parses an vCard JSON data stream (jCard).
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
 */
public class JCardRawReader implements Closeable {
	private final Reader reader;
	private JsonParser parser;
	private boolean eof = false;
	private JCardDataStreamListener listener;
	private boolean strict = false;

	/**
	 * @param reader the reader to wrap
	 */
	public JCardRawReader(Reader reader) {
		this.reader = reader;
	}

	/**
	 * @param parser the parser to read from
	 * @param strict true if the parser's current token is expected to be
	 * positioned at the start of a jCard, false if not. If this is true, and
	 * the parser is not positioned at the beginning of a jCard, a
	 * {@link JCardParseException} will be thrown. If this if false, the parser
	 * will consume input until it reaches the beginning of a jCard.
	 */
	public JCardRawReader(JsonParser parser, boolean strict) {
		reader = null;
		this.parser = parser;
		this.strict = strict;
	}

	/**
	 * Gets the current line number.
	 * @return the line number
	 */
	public int getLineNum() {
		return (parser == null) ? 0 : parser.getCurrentLocation().getLineNr();
	}

	/**
	 * Reads the next vCard from the jCard data stream.
	 * @param listener handles the vCard data as it is read off the wire
	 * @throws JCardParseException if the jCard syntax is incorrect (the JSON
	 * syntax may be valid, but it is not in the correct jCard format).
	 * @throws JsonParseException if the JSON syntax is incorrect
	 * @throws IOException if there is a problem reading from the input stream
	 */
	public void readNext(JCardDataStreamListener listener) throws IOException {
		if (parser == null) {
			JsonFactory factory = new JsonFactory();
			parser = factory.createParser(reader);
		} else if (parser.isClosed()) {
			return;
		}

		this.listener = listener;

		//find the next vCard object
		JsonToken prev = parser.getCurrentToken();
		JsonToken cur;
		while ((cur = parser.nextToken()) != null) {
			if (prev == JsonToken.START_ARRAY && cur == JsonToken.VALUE_STRING && "vcard".equals(parser.getValueAsString())) {
				//found
				break;
			}

			if (strict) {
				//the parser was expecting the jCard to be there
				if (prev != JsonToken.START_ARRAY) {
					throw new JCardParseException(JsonToken.START_ARRAY, prev);
				}

				if (cur != JsonToken.VALUE_STRING) {
					throw new JCardParseException(JsonToken.VALUE_STRING, cur);
				}

				throw new JCardParseException("Invalid value for first token: expected \"vcard\" , was \"" + parser.getValueAsString() + "\"", JsonToken.VALUE_STRING, cur);
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

		check(JsonToken.END_ARRAY, parser.nextToken());
	}

	private void parseProperties() throws IOException {
		//start properties array
		checkNext(JsonToken.START_ARRAY);

		//read properties
		while (parser.nextToken() != JsonToken.END_ARRAY) { //until we reach the end properties array
			checkCurrent(JsonToken.START_ARRAY);
			parser.nextToken();
			parseProperty();
		}
	}

	private void parseProperty() throws IOException {
		//get property name
		checkCurrent(JsonToken.VALUE_STRING);
		String propertyName = parser.getValueAsString().toLowerCase();

		//get parameters
		VCardParameters parameters = parseParameters();

		//get group
		List<String> removed = parameters.removeAll("group");
		String group = removed.isEmpty() ? null : removed.get(0);

		//get data type
		checkNext(JsonToken.VALUE_STRING);
		String dataTypeStr = parser.getText().toLowerCase();
		VCardDataType dataType = "unknown".equals(dataTypeStr) ? null : VCardDataType.get(dataTypeStr);

		//get property value(s)
		List<JsonValue> values = parseValues();

		JCardValue value = new JCardValue(values);
		listener.readProperty(group, propertyName, parameters, dataType, value);
	}

	private VCardParameters parseParameters() throws IOException {
		checkNext(JsonToken.START_OBJECT);

		VCardParameters parameters = new VCardParameters();
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String parameterName = parser.getText();

			if (parser.nextToken() == JsonToken.START_ARRAY) {
				//multi-valued parameter
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					parameters.put(parameterName, parser.getText());
				}
			} else {
				parameters.put(parameterName, parser.getValueAsString());
			}
		}

		return parameters;
	}

	private List<JsonValue> parseValues() throws IOException {
		List<JsonValue> values = new ArrayList<JsonValue>();
		while (parser.nextToken() != JsonToken.END_ARRAY) { //until we reach the end of the property array
			JsonValue value = parseValue();
			values.add(value);
		}
		return values;
	}

	private Object parseValueElement() throws IOException {
		switch (parser.getCurrentToken()) {
		case VALUE_FALSE:
		case VALUE_TRUE:
			return parser.getBooleanValue();
		case VALUE_NUMBER_FLOAT:
			return parser.getDoubleValue();
		case VALUE_NUMBER_INT:
			return parser.getLongValue();
		case VALUE_NULL:
			return null;
		default:
			return parser.getText();
		}
	}

	private List<JsonValue> parseValueArray() throws IOException {
		List<JsonValue> array = new ArrayList<JsonValue>();

		while (parser.nextToken() != JsonToken.END_ARRAY) {
			JsonValue value = parseValue();
			array.add(value);
		}

		return array;
	}

	private Map<String, JsonValue> parseValueObject() throws IOException {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			checkCurrent(JsonToken.FIELD_NAME);

			String key = parser.getText();
			parser.nextToken();
			JsonValue value = parseValue();
			object.put(key, value);
		}

		return object;
	}

	private JsonValue parseValue() throws IOException {
		switch (parser.getCurrentToken()) {
		case START_ARRAY:
			return new JsonValue(parseValueArray());
		case START_OBJECT:
			return new JsonValue(parseValueObject());
		default:
			return new JsonValue(parseValueElement());
		}
	}

	private void checkNext(JsonToken expected) throws IOException {
		JsonToken actual = parser.nextToken();
		check(expected, actual);
	}

	private void checkCurrent(JsonToken expected) throws JCardParseException {
		JsonToken actual = parser.getCurrentToken();
		check(expected, actual);
	}

	private void check(JsonToken expected, JsonToken actual) throws JCardParseException {
		if (actual != expected) {
			throw new JCardParseException(expected, actual);
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
	public interface JCardDataStreamListener {
		/**
		 * Called when a vCard has been found in the stream.
		 */
		void beginVCard();

		/**
		 * Called when a property is read.
		 * @param group the group or null if there is not group
		 * @param propertyName the property name (e.g. "summary")
		 * @param parameters the parameters
		 * @param dataType the data type or null for "unknown"
		 * @param value the property value
		 */
		void readProperty(String group, String propertyName, VCardParameters parameters, VCardDataType dataType, JCardValue value);
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		if (parser != null) {
			parser.close();
		}
		if (reader != null) {
			reader.close();
		}
	}
}
