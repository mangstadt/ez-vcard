package ezvcard.io.json;

import static ezvcard.util.StringUtils.NEWLINE;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

import ezvcard.VCardDataType;
import ezvcard.parameter.VCardParameters;
import ezvcard.util.StringUtils;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Writes data to an vCard JSON data stream (jCard).
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
 */
public class JCardRawWriter implements Closeable, Flushable {
	private final Writer writer;
	private final boolean wrapInArray;
	private JsonGenerator generator;
	private boolean indent = false;
	private boolean open = false;

	/**
	 * @param writer the writer to wrap
	 * @param wrapInArray true to wrap everything in an array, false not to
	 * (useful when writing more than one vCard)
	 */
	public JCardRawWriter(Writer writer, boolean wrapInArray) {
		this.writer = writer;
		this.wrapInArray = wrapInArray;
	}

	/**
	 * Gets whether or not the JSON will be pretty-printed.
	 * @return true if it will be pretty-printed, false if not (defaults to
	 * false)
	 */
	public boolean isIndent() {
		return indent;
	}

	/**
	 * Sets whether or not to pretty-print the JSON.
	 * @param indent true to pretty-print it, false not to (defaults to false)
	 */
	public void setIndent(boolean indent) {
		this.indent = indent;
	}

	/**
	 * Writes the beginning of a new "vcard" component.
	 * @throws IOException if there's an I/O problem
	 */
	public void writeStartVCard() throws IOException {
		if (generator == null) {
			init();
		}

		if (open) {
			writeEndVCard();
		}

		generator.writeStartArray();
		indent(0);
		generator.writeString("vcard");
		generator.writeStartArray(); //start properties array

		open = true;
	}

	/**
	 * Closes the "vcard" component array.
	 * @throws IllegalStateException if the component was never opened (
	 * {@link #writeStartVCard} must be called first)
	 * @throws IOException if there's an I/O problem
	 */
	public void writeEndVCard() throws IOException {
		if (!open) {
			throw new IllegalStateException("Call \"writeStartVCard\" first.");
		}

		generator.writeEndArray(); //end the properties array
		generator.writeEndArray(); //end the "vcard" component array

		open = false;
	}

	/**
	 * Writes a property to the current component.
	 * @param propertyName the property name (e.g. "version")
	 * @param dataType the data type or null for "unknown"
	 * @param value the property value
	 * @throws IllegalStateException if the "vcard" component was never opened
	 * or was just closed ({@link #writeStartVCard} must be called first)
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String propertyName, VCardDataType dataType, JCardValue value) throws IOException {
		writeProperty(null, propertyName, new VCardParameters(), dataType, value);
	}

	/**
	 * Writes a property to the current vCard.
	 * @param group the group or null if there is no group
	 * @param propertyName the property name (e.g. "version")
	 * @param parameters the parameters
	 * @param dataType the data type or null for "unknown"
	 * @param value the property value
	 * @throws IllegalStateException if the "vcard" component was never opened
	 * or was just closed ({@link #writeStartVCard} must be called first)
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String group, String propertyName, VCardParameters parameters, VCardDataType dataType, JCardValue value) throws IOException {
		if (!open) {
			throw new IllegalStateException("Call \"writeStartVCard\" first.");
		}

		generator.writeStartArray();
		indent(2);

		//write the property name
		generator.writeString(propertyName);

		//write parameters
		generator.writeStartObject();
		for (Map.Entry<String, List<String>> entry : parameters) {
			String name = entry.getKey().toLowerCase();
			List<String> values = entry.getValue();
			if (values.isEmpty()) {
				continue;
			}

			if (values.size() == 1) {
				generator.writeStringField(name, values.get(0));
			} else {
				generator.writeArrayFieldStart(name);
				for (String paramValue : values) {
					generator.writeString(paramValue);
				}
				generator.writeEndArray();
			}
		}

		//write group
		if (group != null) {
			generator.writeStringField("group", group);
		}

		generator.writeEndObject(); //end parameters object

		//write data type
		generator.writeString((dataType == null) ? "unknown" : dataType.getName().toLowerCase());

		//write value
		if (value.getValues().isEmpty()) {
			generator.writeString("");
		} else {
			for (JsonValue jsonValue : value.getValues()) {
				writeValue(jsonValue);
			}
		}

		generator.writeEndArray();
	}

	private void writeValue(JsonValue jsonValue) throws IOException {
		if (jsonValue.isNull()) {
			generator.writeNull();
			return;
		}

		Object val = jsonValue.getValue();
		if (val != null) {
			if (val instanceof Byte) {
				generator.writeNumber((Byte) val);
			} else if (val instanceof Short) {
				generator.writeNumber((Short) val);
			} else if (val instanceof Integer) {
				generator.writeNumber((Integer) val);
			} else if (val instanceof Long) {
				generator.writeNumber((Long) val);
			} else if (val instanceof Float) {
				generator.writeNumber((Float) val);
			} else if (val instanceof Double) {
				generator.writeNumber((Double) val);
			} else if (val instanceof Boolean) {
				generator.writeBoolean((Boolean) val);
			} else {
				generator.writeString(val.toString());
			}
			return;
		}

		List<JsonValue> array = jsonValue.getArray();
		if (array != null) {
			generator.writeStartArray();
			for (JsonValue element : array) {
				writeValue(element);
			}
			generator.writeEndArray();
			return;
		}

		Map<String, JsonValue> object = jsonValue.getObject();
		if (object != null) {
			generator.writeStartObject();
			for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
				generator.writeFieldName(entry.getKey());
				writeValue(entry.getValue());
			}
			generator.writeEndObject();
			return;
		}
	}

	/**
	 * Checks to see if pretty-printing is enabled, and adds indentation
	 * whitespace if it is.
	 * @param spaces the number of spaces to indent with
	 * @throws IOException
	 */
	private void indent(int spaces) throws IOException {
		if (!indent) {
			return;
		}

		generator.writeRaw(NEWLINE);
		generator.writeRaw(StringUtils.repeat(' ', spaces));
	}

	/**
	 * Flushes the JSON stream.
	 */
	public void flush() throws IOException {
		if (generator == null) {
			return;
		}

		generator.flush();
	}

	/**
	 * Finishes writing the JSON document so that it is syntactically correct.
	 * No more data can be written once this method is called.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void closeJsonStream() throws IOException {
		if (generator == null) {
			return;
		}

		while (open) {
			writeEndVCard();
		}

		if (wrapInArray) {
			indent(0);
			generator.writeEndArray();
		}

		generator.close();
	}

	/**
	 * Finishes writing the JSON document and closes the underlying
	 * {@link Writer}.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void close() throws IOException {
		if (generator == null) {
			return;
		}

		closeJsonStream();
		writer.close();
	}

	private void init() throws IOException {
		JsonFactory factory = new JsonFactory();
		factory.configure(Feature.AUTO_CLOSE_TARGET, false);
		generator = factory.createGenerator(writer);

		if (wrapInArray) {
			generator.writeStartArray();
			indent(0);
		}
	}
}
