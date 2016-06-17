package ezvcard.io.scribe;

import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.StringUtils.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.json.JsonValue;
import ezvcard.io.text.VCardRawWriter;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.io.xml.XCardElement.XCardValue;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Categories;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;
import ezvcard.property.VCardProperty;
import ezvcard.util.StringUtils.JoinCallback;
import ezvcard.util.VCardDateFormat;

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
 * Base class for vCard property scribes (aka "marshallers" or "serializers").
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class VCardPropertyScribe<T extends VCardProperty> {
	protected final Class<T> clazz;
	protected final String propertyName;
	protected final QName qname;

	/**
	 * Creates a new scribe.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "FN")
	 */
	public VCardPropertyScribe(Class<T> clazz, String propertyName) {
		this(clazz, propertyName, new QName(VCardVersion.V4_0.getXmlNamespace(), propertyName.toLowerCase()));
	}

	/**
	 * Creates a new scribe.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "FN")
	 * @param qname the XML element name and namespace to use for xCard
	 * documents (by default, the XML element name is set to the lower-cased
	 * property name, and the element namespace is set to the xCard namespace)
	 */
	public VCardPropertyScribe(Class<T> clazz, String propertyName, QName qname) {
		this.clazz = clazz;
		this.propertyName = propertyName;
		this.qname = qname;
	}

	/**
	 * Gets the property class.
	 * @return the property class
	 */
	public Class<T> getPropertyClass() {
		return clazz;
	}

	/**
	 * Gets the property name.
	 * @return the property name (e.g. "FN")
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Gets this property's local name and namespace for xCard documents.
	 * @return the XML local name and namespace
	 */
	public QName getQName() {
		return qname;
	}

	/**
	 * Sanitizes a property's parameters (called before the property is
	 * written). Note that a copy of the parameters is returned so that the
	 * property object does not get modified.
	 * @param property the property
	 * @param version the version of the vCard that is being generated
	 * @param vcard the vCard that the property belongs to
	 * @return the sanitized parameters
	 */
	public final VCardParameters prepareParameters(T property, VCardVersion version, VCard vcard) {
		//make a copy because the property should not get modified when it is marshalled
		VCardParameters copy = new VCardParameters(property.getParameters());
		_prepareParameters(property, copy, version, vcard);
		return copy;
	}

	/**
	 * <p>
	 * Determines the property's default data type.
	 * </p>
	 * <p>
	 * When writing a plain-text vCard, if the data type of a property instance
	 * (as determined by the {@link #dataType} method) matches the default data
	 * type, then a VALUE parameter will *not* be written.
	 * </p>
	 * <p>
	 * When parsing a plain-text vCard, if a property has no VALUE parameter,
	 * then the property's default data type will be passed into the
	 * {@link #parseText} method.
	 * </p>
	 * @param version the vCard version
	 * @return the default data type or null if unknown
	 */
	public final VCardDataType defaultDataType(VCardVersion version) {
		return _defaultDataType(version);
	}

	/**
	 * Determines the data type of a property instance.
	 * @param property the property
	 * @param version the version of the vCard that is being generated
	 * @return the data type or null if unknown
	 */
	public final VCardDataType dataType(T property, VCardVersion version) {
		return _dataType(property, version);
	}

	/**
	 * Marshals a property's value to a string.
	 * @param property the property
	 * @param context contains information about the vCard being written, such
	 * as the target version
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final String writeText(T property, WriteContext context) {
		return _writeText(property, context);
	}

	/**
	 * Marshals a property's value to an XML element (xCard).
	 * @param property the property
	 * @param element the property's XML element.
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final void writeXml(T property, Element element) {
		XCardElement xCardElement = new XCardElement(element);
		_writeXml(property, xCardElement);
	}

	/**
	 * Marshals a property's value to a JSON data stream (jCard).
	 * @param property the property
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final JCardValue writeJson(T property) {
		return _writeJson(property);
	}

	/**
	 * Unmarshals a property from a plain-text vCard.
	 * @param value the value as read off the wire
	 * @param dataType the data type of the property value. The property's VALUE
	 * parameter is used to determine the data type. If the property has no
	 * VALUE parameter, then this parameter will be set to the property's
	 * default datatype, as determined by the {@link #defaultDataType} method.
	 * Note that the VALUE parameter is removed from the property's parameter
	 * list after it has been read.
	 * @param version the version of the vCard that is being parsed
	 * @param parameters the parsed parameters
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the property value is an embedded vCard
	 * (i.e. the AGENT property)
	 */
	public final Result<T> parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseText(value, dataType, version, parameters, warnings);
		property.setParameters(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * Unmarshals a property's value from an XML document (xCard).
	 * @param element the property's XML element
	 * @param parameters the parsed parameters
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	public final Result<T> parseXml(Element element, VCardParameters parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseXml(new XCardElement(element), parameters, warnings);
		property.setParameters(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * Unmarshals the property from an HTML document (hCard).
	 * @param element the property's HTML element
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the property value could not be parsed
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the property value is an embedded vCard
	 * (i.e. the AGENT property)
	 */
	public final Result<T> parseHtml(HCardElement element) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseHtml(element, warnings);
		return new Result<T>(property, warnings);
	}

	/**
	 * Unmarshals a property's value from a JSON data stream (jCard).
	 * @param value the property's JSON value
	 * @param dataType the data type
	 * @param parameters the parsed parameters
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	public final Result<T> parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseJson(value, dataType, parameters, warnings);
		property.setParameters(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * <p>
	 * Sanitizes a property's parameters before the property is written.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to tweak the
	 * property's parameters before the property is written. The default
	 * implementation of this method does nothing.
	 * </p>
	 * @param property the property
	 * @param copy the list of parameters to make modifications to (it is a copy
	 * of the property's parameters)
	 * @param version the version of the vCard that is being generated
	 * @param vcard the vCard that the property belongs to
	 */
	protected void _prepareParameters(T property, VCardParameters copy, VCardVersion version, VCard vcard) {
		//do nothing
	}

	/**
	 * <p>
	 * Determines the property's default data type.
	 * </p>
	 * <p>
	 * When writing a plain-text vCard, if the data type of a property instance
	 * (as determined by the {@link #dataType} method) matches the default data
	 * type, then a VALUE parameter will *not* be written.
	 * </p>
	 * <p>
	 * When parsing a plain-text vCard, if a property has no VALUE parameter,
	 * then the property's default data type will be passed into the
	 * {@link #parseText} method.
	 * </p>
	 * @param version the vCard version
	 * @return the default data type or null if unknown
	 */
	protected abstract VCardDataType _defaultDataType(VCardVersion version);

	/**
	 * <p>
	 * Determines the data type of a property instance.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes if a property's data
	 * type changes depending on its value. The default implementation of this
	 * method calls {@link #_defaultDataType}.
	 * </p>
	 * @param property the property
	 * @param version the version of the vCard that is being generated
	 * @return the data type or null if unknown
	 */
	protected VCardDataType _dataType(T property, VCardVersion version) {
		return _defaultDataType(version);
	}

	/**
	 * Marshals a property's value to a string.
	 * @param property the property
	 * @param context contains information about the vCard being written, such
	 * as the target version
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected abstract String _writeText(T property, WriteContext context);

	/**
	 * <p>
	 * Marshals a property's value to an XML element (xCard).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * xCard. The default implementation of this method will append one child
	 * element to the property's XML element. The child element's name will be
	 * that of the property's data type (retrieved using the {@link #dataType}
	 * method), and the child element's text content will be set to the
	 * property's marshalled plain-text value (retrieved using the
	 * {@link #writeText} method).
	 * </p>
	 * @param property the property
	 * @param element the property's XML element
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected void _writeXml(T property, XCardElement element) {
		String value = writeText(property, new WriteContext(VCardVersion.V4_0, null, false));
		VCardDataType dataType = dataType(property, VCardVersion.V4_0);
		element.append(dataType, value);
	}

	/**
	 * <p>
	 * Marshals a property's value to a JSON data stream (jCard).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * jCard. The default implementation of this method will create a jCard
	 * property that has a single JSON string value (generated by the
	 * {@link #writeText} method).
	 * </p>
	 * @param property the property
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected JCardValue _writeJson(T property) {
		String value = writeText(property, new WriteContext(VCardVersion.V4_0, null, false));
		return JCardValue.single(value);
	}

	/**
	 * Unmarshals a property from a plain-text vCard.
	 * @param value the value as read off the wire
	 * @param dataType the data type of the property value. The property's VALUE
	 * parameter is used to determine the data type. If the property has no
	 * VALUE parameter, then this parameter will be set to the property's
	 * default datatype, as determined by the {@link #defaultDataType} method.
	 * Note that the VALUE parameter is removed from the property's parameter
	 * list after it has been read.
	 * @param version the version of the vCard that is being parsed
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	protected abstract T _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings);

	/**
	 * <p>
	 * Unmarshals a property from an XML document (xCard).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * xCard. The default implementation of this method will find the first
	 * child element with the xCard namespace. The element's name will be used
	 * as the property's data type and its text content (escaped for inclusion
	 * in a text-based vCard, e.g. escaping comma characters) will be passed
	 * into the {@link #_parseText} method. If no such child element is found,
	 * then the parent element's text content will be passed into
	 * {@link #_parseText} and the data type will be {@code null}.
	 * </p>
	 * @param element the property's XML element
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	protected T _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		XCardValue firstValue = element.firstValue();
		VCardDataType dataType = firstValue.getDataType();
		String value = escape(firstValue.getValue());
		return _parseText(value, dataType, element.version(), parameters, warnings);
	}

	/**
	 * <p>
	 * Unmarshals the property from an hCard (HTML document).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * hCard. The default implementation of this method will retrieve the HTML
	 * element's hCard value (as described in {@link HCardElement#value}), and
	 * pass it into the {@link #_parseText} method.
	 * </p>
	 * @param element the property's HTML element
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the property value could not be parsed
	 * @throws SkipMeException if this property should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the value of this property is an
	 * embedded vCard (i.e. the AGENT property)
	 */
	protected T _parseHtml(HCardElement element, List<String> warnings) {
		String value = escape(element.value());
		VCardParameters parameters = new VCardParameters();
		T property = _parseText(value, null, VCardVersion.V3_0, parameters, warnings);
		property.setParameters(parameters);
		return property;
	}

	/**
	 * <p>
	 * Unmarshals a property from a JSON data stream (jCard).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * jCard. The default implementation of this method will convert the jCard
	 * property value to a string and pass it into the {@link #_parseText}
	 * method.
	 * </p>
	 * 
	 * <hr>
	 * 
	 * <p>
	 * The following paragraphs describe the way in which this method's default
	 * implementation converts a jCard value to a string:
	 * </p>
	 * <p>
	 * If the jCard value consists of a single, non-array, non-object value,
	 * then the value is converted to a string. Special characters (backslashes,
	 * commas, and semicolons) are escaped in order to simulate what the value
	 * might look like in a plain-text vCard.<br>
	 * <code>["x-foo", {}, "text", "the;value"] --&gt; "the\;value"</code><br>
	 * <code>["x-foo", {}, "text", 2] --&gt; "2"</code>
	 * </p>
	 * <p>
	 * If the jCard value consists of multiple, non-array, non-object values,
	 * then all the values are appended together in a single string, separated
	 * by commas. Special characters (backslashes, commas, and semicolons) are
	 * escaped for each value in order to prevent commas from being treated as
	 * delimiters, and to simulate what the value might look like in a
	 * plain-text vCard.<br>
	 * <code>["x-foo", {}, "text", "one", "two,three"] --&gt;
	 * "one,two\,three"</code>
	 * </p>
	 * <p>
	 * If the jCard value is a single array, then this array is treated as a
	 * "structured value", and converted its plain-text representation. Special
	 * characters (backslashes, commas, and semicolons) are escaped for each
	 * value in order to prevent commas and semicolons from being treated as
	 * delimiters.<br>
	 * <code>["x-foo", {}, "text", ["one", ["two", "three"], "four;five"]]
	 * --&gt; "one;two,three;four\;five"</code>
	 * </p>
	 * <p>
	 * If the jCard value starts with a JSON object, then it is converted to an
	 * empty string (JSON objects are not supported by this method).<br>
	 * <code>["x-foo", , "text", {"one": 1}] --&gt; ""}</code>
	 * </p>
	 * <p>
	 * For all other cases, behavior is undefined.
	 * </p>
	 * @param value the property's JSON value
	 * @param dataType the data type
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	protected T _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		String valueStr = jcardValueToString(value);
		return _parseText(valueStr, dataType, VCardVersion.V4_0, parameters, warnings);
	}

	/**
	 * Converts a jCard value to its plain-text format representation.
	 * @param value the jCard value
	 * @return the plain-text format representation (for example, "1,2,3" for a
	 * list of values)
	 */
	private static String jcardValueToString(JCardValue value) {
		List<JsonValue> values = value.getValues();
		if (values.size() > 1) {
			List<String> multi = value.asMulti();
			if (!multi.isEmpty()) {
				return list(multi);
			}
		}

		if (!values.isEmpty() && values.get(0).getArray() != null) {
			List<List<String>> structured = value.asStructured();
			if (!structured.isEmpty()) {
				return structured(structured.toArray());
			}
		}

		return escape(value.asSingle());
	}

	/**
	 * Unescapes all special characters that are escaped with a backslash, as
	 * well as escaped newlines.
	 * @param text the text to unescape
	 * @return the unescaped text
	 */
	public static String unescape(String text) {
		if (text == null) {
			return null;
		}

		StringBuilder sb = null; //only instantiate the StringBuilder if the string needs to be modified
		boolean escaped = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);

			if (escaped) {
				if (sb == null) {
					sb = new StringBuilder(text.length());
					sb.append(text.substring(0, i - 1));
				}

				escaped = false;

				if (ch == 'n' || ch == 'N') {
					//newlines appear as "\n" or "\N" (see RFC 5545 p.46)
					sb.append(NEWLINE);
					continue;
				}

				sb.append(ch);
				continue;
			}

			if (ch == '\\') {
				escaped = true;
				continue;
			}

			if (sb != null) {
				sb.append(ch);
			}
		}
		return (sb == null) ? text : sb.toString();
	}

	/**
	 * <p>
	 * Escapes all special characters within a vCard value. These characters
	 * are:
	 * </p>
	 * <ul>
	 * <li>backslashes ({@code \})</li>
	 * <li>commas ({@code ,})</li>
	 * <li>semi-colons ({@code ;})</li>
	 * </ul>
	 * <p>
	 * Newlines are not escaped by this method. They are escaped when the vCard
	 * is serialized (in the {@link VCardRawWriter} class).
	 * </p>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	public static String escape(String text) {
		if (text == null) {
			return null;
		}

		String chars = "\\,;";
		StringBuilder sb = null; //only instantiate the StringBuilder if the string needs to be modified
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (chars.indexOf(ch) >= 0) {
				if (sb == null) {
					sb = new StringBuilder(text.length());
					sb.append(text.substring(0, i));
				}
				sb.append('\\');
			}

			if (sb != null) {
				sb.append(ch);
			}
		}
		return (sb == null) ? text : sb.toString();
	}

	/**
	 * Creates a string splitter (takes escaped characters into account).
	 * @param delimiter the delimiter character (e.g. ',')
	 * @return the splitter object
	 */
	protected static Splitter splitter(char delimiter) {
		return new Splitter(delimiter);
	}

	/**
	 * A helper class for splitting strings.
	 */
	protected static class Splitter {
		private char delimiter;
		private boolean unescape = false;
		private boolean nullEmpties = false;
		private int limit = -1;

		/**
		 * Creates a new splitter object.
		 * @param delimiter the delimiter character (e.g. ',')
		 */
		public Splitter(char delimiter) {
			this.delimiter = delimiter;
		}

		/**
		 * Sets whether to unescape each split string.
		 * @param unescape true to unescape, false not to (default is false)
		 * @return this
		 */
		public Splitter unescape(boolean unescape) {
			this.unescape = unescape;
			return this;
		}

		/**
		 * Sets whether to treat empty elements as null elements.
		 * @param nullEmpties true to treat them as null elements, false to
		 * treat them as empty strings (default is false)
		 * @return this
		 */
		public Splitter nullEmpties(boolean nullEmpties) {
			this.nullEmpties = nullEmpties;
			return this;
		}

		/**
		 * Sets the max number of split strings it should parse.
		 * @param limit the max number of split strings
		 * @return this
		 */
		public Splitter limit(int limit) {
			this.limit = limit;
			return this;
		}

		/**
		 * Performs the split operation.
		 * @param string the string to split (e.g. "one,two,three")
		 * @return the split string
		 */
		public List<String> split(String string) {
			//doing it this way is 10x faster than a regex

			List<String> list = new ArrayList<String>();
			boolean escaped = false;
			int start = 0;
			for (int i = 0; i < string.length(); i++) {
				char ch = string.charAt(i);

				if (escaped) {
					escaped = false;
					continue;
				}

				if (ch == delimiter) {
					add(string.substring(start, i), list);
					start = i + 1;
					if (limit > 0 && list.size() == limit - 1) {
						break;
					}

					continue;
				}

				if (ch == '\\') {
					escaped = true;
					continue;
				}
			}

			add(string.substring(start), list);

			return list;
		}

		private void add(String str, List<String> list) {
			str = str.trim();

			if (nullEmpties && str.length() == 0) {
				str = null;
			} else if (unescape) {
				str = VCardPropertyScribe.unescape(str);
			}

			list.add(str);
		}
	}

	/**
	 * Parses a "list" property value. This is used in plain-text vCards to
	 * parse properties such as {@link Categories}.
	 * @param value the string to parse (e.g. "one,two,three\,four")
	 * @return the parsed list (e.g. ["one", "two", "three,four"])
	 */
	protected static List<String> list(String value) {
		if (value.length() == 0) {
			return new ArrayList<String>(0);
		}
		return splitter(',').unescape(true).split(value);
	}

	/**
	 * Generates a "list" property value. This is used in plain-text vCards to
	 * parse properties such as {@link Categories}.
	 * @param values the values to write (the {@code toString()} method is
	 * invoked on each object, null objects are ignored, e.g. ["one", "two",
	 * "three,four"])
	 * @return the property value (e.g. "one,two,three\,four")
	 */
	protected static String list(Object... values) {
		return list(Arrays.asList(values));
	}

	/**
	 * Generates a "list" property value. This is used in plain-text vCards to
	 * parse properties such as {@link Categories}).
	 * @param values the values to write (the {@code toString()} method is
	 * invoked on each object, null objects are ignored, e.g. ["one", "two",
	 * "three,four"])
	 * @param <T> the property value class
	 * @return the property value (e.g. "one,two,three\,four")
	 */
	protected static <T> String list(Collection<T> values) {
		return join(values, ",", new JoinCallback<T>() {
			public void handle(StringBuilder sb, T value) {
				if (value == null) {
					return;
				}
				sb.append(escape(value.toString()));
			}
		});
	}

	/**
	 * Parses a "semi-structured" property value (a "structured" property value
	 * whose items cannot be multi-valued). This is used in plain-text vCards to
	 * parse properties such as {@link Organization}.
	 * @param value the string to parse (e.g. "one;two;three\;four,five")
	 * @return the parsed values (e.g. ["one", "two", "three;four,five"]
	 */
	protected static SemiStructuredIterator semistructured(String value) {
		return semistructured(value, -1);
	}

	/**
	 * Parses a "semi-structured" property value (a "structured" property value
	 * whose items cannot be multi-valued). This is used in plain-text vCards to
	 * parse properties such as {@link Organization}.
	 * @param value the string to parse (e.g. "one;two;three\;four,five")
	 * @param limit the max number of items to parse (see
	 * {@link String#split(String, int)})
	 * @return the parsed values (e.g. ["one", "two", "three;four,five"]
	 */
	protected static SemiStructuredIterator semistructured(String value, int limit) {
		List<String> split = splitter(';').unescape(true).limit(limit).split(value);
		return new SemiStructuredIterator(split.iterator());
	}

	/**
	 * Parses a "structured" property value. This is used in plain-text vCards
	 * to parse properties such as {@link StructuredName}.
	 * @param value the string to parse (e.g. "one;two,three;four\,five\;six")
	 * @return an iterator for accessing the parsed values (e.g. ["one", ["two",
	 * "three"], "four,five;six"])
	 */
	protected static StructuredIterator structured(String value) {
		List<String> split = splitter(';').split(value);
		List<List<String>> components = new ArrayList<List<String>>(split.size());
		for (String s : split) {
			components.add(list(s));
		}
		return new StructuredIterator(components.iterator());
	}

	/**
	 * Provides a "structured" property value iterator for a jCard value.
	 * @param value the jCard value
	 * @return the iterator
	 */
	protected static StructuredIterator structured(JCardValue value) {
		return new StructuredIterator(value.asStructured().iterator());
	}

	/**
	 * <p>
	 * Writes a "structured" property value. This is used in plain-text vCards
	 * to marshal properties such as {@link StructuredName}.
	 * </p>
	 * <p>
	 * This method accepts a list of {@link Object} instances.
	 * {@link Collection} objects will be treated as multi-valued components.
	 * Null objects will be treated as empty components. All other objects will
	 * have their {@code toString()} method invoked to generate the string
	 * value.
	 * </p>
	 * <p>
	 * The semicolon delimiters for empty or null values at the end of the
	 * values list will not be included in the final string.
	 * </p>
	 * @param values the values to write
	 * @return the structured value string
	 */
	protected static String structured(Object... values) {
		return structured(false, values);
	}

	/**
	 * <p>
	 * Writes a "structured" property value. This is used in plain-text vCards
	 * to marshal properties such as {@link StructuredName}.
	 * </p>
	 * <p>
	 * This method accepts a list of {@link Object} instances.
	 * {@link Collection} objects will be treated as multi-valued components.
	 * Null objects will be treated as empty components. All other objects will
	 * have their {@code toString()} method invoked to generate the string
	 * value.
	 * </p>
	 * @param includeTrailingSemicolons true to include the semicolon delimiters
	 * for empty or null values at the end of the values list, false to trim
	 * them
	 * @param values the values to write
	 * @return the structured value string
	 */
	protected static String structured(boolean includeTrailingSemicolons, Object... values) {
		StringBuilder sb = new StringBuilder();
		int skippedSemicolons = 0;
		boolean first = true;
		for (Object value : values) {
			if (value == null) {
				if (first) {
					first = false;
				} else {
					if (includeTrailingSemicolons) {
						sb.append(';');
					} else {
						skippedSemicolons++;
					}
				}
				continue;
			}

			String strValue;
			if (value instanceof Collection) {
				Collection<?> list = (Collection<?>) value;
				strValue = list(list);
			} else {
				strValue = escape(value.toString());
			}

			if (strValue.length() == 0) {
				if (first) {
					first = false;
				} else {
					if (includeTrailingSemicolons) {
						sb.append(';');
					} else {
						skippedSemicolons++;
					}
				}
				continue;
			}

			if (!includeTrailingSemicolons) {
				for (int i = 0; i < skippedSemicolons; i++) {
					sb.append(';');
				}
				skippedSemicolons = 0;
			}

			if (first) {
				first = false;
			} else {
				sb.append(';');
			}
			sb.append(strValue);
		}

		return sb.toString();
	}

	/**
	 * Iterates over the items in a "structured" property value.
	 */
	protected static class StructuredIterator {
		private final Iterator<List<String>> it;

		/**
		 * Constructs a new structured iterator.
		 * @param it the iterator to wrap
		 */
		public StructuredIterator(Iterator<List<String>> it) {
			this.it = it;
		}

		/**
		 * Gets the first value of the next component.
		 * @return the first value, null if the value is an empty string, or
		 * null if there are no more components
		 */
		public String nextString() {
			if (!hasNext()) {
				return null;
			}

			List<String> list = it.next();
			if (list.isEmpty()) {
				return null;
			}

			String value = list.get(0);
			return (value.length() == 0) ? null : value;
		}

		/**
		 * Gets the next component.
		 * @return the next component, an empty list if the component is empty,
		 * or an empty list of there are no more components
		 */
		public List<String> nextComponent() {
			if (!hasNext()) {
				return new ArrayList<String>(0); //the lists should be mutable so they can be directly assigned to the property object's fields
			}

			List<String> list = it.next();
			if (list.size() == 1 && list.get(0).length() == 0) {
				return new ArrayList<String>(0);
			}

			return list;
		}

		public boolean hasNext() {
			return it.hasNext();
		}
	}

	/**
	 * Iterates over the items in a "semi-structured" property value.
	 */
	protected static class SemiStructuredIterator {
		private final Iterator<String> it;

		/**
		 * Constructs a new structured iterator.
		 * @param it the iterator to wrap
		 */
		public SemiStructuredIterator(Iterator<String> it) {
			this.it = it;
		}

		/**
		 * Gets the next value.
		 * @return the next value or null if there are no more values
		 */
		public String next() {
			return hasNext() ? it.next() : null;
		}

		public boolean hasNext() {
			return it.hasNext();
		}
	}

	/**
	 * Parses a date string.
	 * @param value the date string
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date cannot be parsed
	 */
	protected static Date date(String value) {
		return VCardDateFormat.parse(value);
	}

	/**
	 * Formats a {@link Date} object as a string.
	 * @param date the date
	 * @return a helper object for customizing the write operation
	 */
	protected static DateWriter date(Date date) {
		return new DateWriter(date);
	}

	/**
	 * A helper class for writing date values.
	 */
	protected static class DateWriter {
		private Date date;
		private boolean hasTime = true;
		private boolean extended = false;
		private boolean utc = true;

		/**
		 * Creates a new date writer object.
		 * @param date the date to format
		 */
		public DateWriter(Date date) {
			this.date = date;
		}

		/**
		 * Sets whether to output the date's time component.
		 * @param hasTime true include the time, false if it's strictly a date
		 * (defaults to "true")
		 * @return this
		 */
		public DateWriter time(boolean hasTime) {
			this.hasTime = hasTime;
			return this;
		}

		/**
		 * Sets whether to use extended format or basic.
		 * @param extended true to use extended format, false to use basic
		 * (defaults to "false")
		 * @return this
		 */
		public DateWriter extended(boolean extended) {
			this.extended = extended;
			return this;
		}

		/**
		 * Sets whether to format the date in UTC time, or to include a UTC
		 * offset.
		 * @param utc true to format in UTC time, false to include the local
		 * timezone's UTC offset (defaults to "true")
		 * @return this
		 */
		public DateWriter utc(boolean utc) {
			this.utc = utc;
			return this;
		}

		/**
		 * Creates the date string.
		 * @return the date string
		 */
		public String write() {
			VCardDateFormat format;
			if (hasTime) {
				if (utc) {
					format = extended ? VCardDateFormat.UTC_DATE_TIME_EXTENDED : VCardDateFormat.UTC_DATE_TIME_BASIC;
				} else {
					format = extended ? VCardDateFormat.DATE_TIME_EXTENDED : VCardDateFormat.DATE_TIME_BASIC;
				}
			} else {
				format = extended ? VCardDateFormat.DATE_EXTENDED : VCardDateFormat.DATE_BASIC;
			}

			return format.format(date);
		}
	}

	/**
	 * Creates a {@link CannotParseException} to indicate that a scribe could
	 * not find the necessary XML elements required in order to successfully
	 * parse a property (xCards only).
	 * @param dataTypes the expected data types (null for "unknown")
	 * @return the exception object (note that the exception is NOT thrown!)
	 */
	protected static CannotParseException missingXmlElements(VCardDataType... dataTypes) {
		String[] elements = new String[dataTypes.length];
		for (int i = 0; i < dataTypes.length; i++) {
			VCardDataType dataType = dataTypes[i];
			elements[i] = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
		}
		return missingXmlElements(elements);
	}

	/**
	 * Creates a {@link CannotParseException} to indicate that a scribe could
	 * not find the necessary XML elements required in order to successfully
	 * parse a property (xCards only).
	 * @param elements the names of the expected XML elements.
	 * @return the exception object (note that the exception is NOT thrown!)
	 */
	protected static CannotParseException missingXmlElements(String... elements) {
		return new CannotParseException(0, Arrays.toString(elements));
	}

	/**
	 * A utility method for switching between the "PREF" and "TYPE=PREF"
	 * parameters when marshalling a property (version 4.0 vCards use "PREF=1",
	 * while version 3.0 vCards use "TYPE=PREF"). This method is meant to be
	 * called from a scribe's {@link #_prepareParameters} method.
	 * @param property the property that is being marshalled
	 * @param copy the parameters that are being marshalled
	 * @param version the vCard version
	 * @param vcard the vCard that's being marshalled
	 */
	protected static void handlePrefParam(VCardProperty property, VCardParameters copy, VCardVersion version, VCard vcard) {
		switch (version) {
		case V2_1:
		case V3_0:
			copy.setPref(null);

			//find the property with the lowest PREF value in the vCard
			VCardProperty mostPreferred = null;
			for (VCardProperty p : vcard.getProperties(property.getClass())) {
				Integer pref = p.getParameters().getPref();
				if (pref == null) {
					continue;
				}

				if (mostPreferred == null || pref < mostPreferred.getParameters().getPref()) {
					mostPreferred = p;
				}
			}

			if (property == mostPreferred) {
				copy.put(VCardParameters.TYPE, "pref");
			}

			break;
		case V4_0:
			for (String type : property.getParameters().get(VCardParameters.TYPE)) {
				if ("pref".equalsIgnoreCase(type)) {
					copy.remove(VCardParameters.TYPE, type);
					copy.setPref(1);
					break;
				}
			}
			break;
		}
	}

	/**
	 * Represents the result of an unmarshal operation.
	 * @author Michael Angstadt
	 * @param <T> the unmarshalled property class
	 */
	public static class Result<T extends VCardProperty> {
		private final T property;
		private final List<String> warnings;

		/**
		 * Creates a new result.
		 * @param property the property object
		 * @param warnings the warnings
		 */
		public Result(T property, List<String> warnings) {
			this.property = property;
			this.warnings = warnings;
		}

		/**
		 * Gets the warnings.
		 * @return the warnings
		 */
		public List<String> getWarnings() {
			return warnings;
		}

		/**
		 * Gets the property object.
		 * @return the property object
		 */
		public T getProperty() {
			return property;
		}
	}
}
