package ezvcard.io.scribe;

import static ezvcard.util.VCardStringUtils.NEWLINE;
import static ezvcard.util.VCardStringUtils.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.VCardRawWriter;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardSubTypes;
import ezvcard.property.VCardType;
import ezvcard.util.HCardElement;
import ezvcard.util.ISOFormat;
import ezvcard.util.VCardDateFormatter;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.VCardStringUtils.JoinCallback;
import ezvcard.util.XmlUtils;

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
 * Base class for vCard property marshallers.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class VCardPropertyScribe<T extends VCardType> {
	protected final Class<T> clazz;
	protected final String propertyName;
	protected final QName qname;

	/**
	 * Creates a new marshaller.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "FN")
	 */
	public VCardPropertyScribe(Class<T> clazz, String propertyName) {
		this(clazz, propertyName, new QName(VCardVersion.V4_0.getXmlNamespace(), propertyName.toLowerCase()));
	}

	/**
	 * Creates a new marshaller.
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
	public final VCardSubTypes prepareParameters(T property, VCardVersion version, VCard vcard) {
		//make a copy because the property should not get modified when it is marshalled
		VCardSubTypes copy = new VCardSubTypes(property.getSubTypes());
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
	 * @param version the version of the vCard that is being generated
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final String writeText(T property, VCardVersion version) {
		return _writeText(property, version);
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
	 * @throws EmbeddedVCardException if the property value is an embedded
	 * vCard (i.e. the AGENT property)
	 */
	public final Result<T> parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseText(value, dataType, version, parameters, warnings);
		property.setSubTypes(parameters);
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
	public final Result<T> parseXml(Element element, VCardSubTypes parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseXml(new XCardElement(element), parameters, warnings);
		property.setSubTypes(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * Unmarshals the property from an HTML document (hCard).
	 * @param element the property's HTML element
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the property value could not be parsed
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the property value is an embedded
	 * vCard (i.e. the AGENT property)
	 */
	public final Result<T> parseHtml(org.jsoup.nodes.Element element) {
		HCardElement hcardElement = new HCardElement(element);
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseHtml(hcardElement, warnings);
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
	public final Result<T> parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseJson(value, dataType, parameters, warnings);
		property.setSubTypes(parameters);
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
	protected void _prepareParameters(T property, VCardSubTypes copy, VCardVersion version, VCard vcard) {
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
	 * @param version the version of the vCard that is being generated
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected abstract String _writeText(T property, VCardVersion version);

	/**
	 * <p>
	 * Marshals a property's value to an XML element (xCard).
	 * <p>
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
		String value = writeText(property, VCardVersion.V4_0);
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
		String value = writeText(property, VCardVersion.V4_0);
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
	protected abstract T _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings);

	/**
	 * <p>
	 * Unmarshals a property from an XML document (xCard).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * xCard. The default implementation of this method will find the first
	 * child element with the xCard namespace. The element's name will be used
	 * as the property's data type and its text content will be passed into the
	 * {@link #_parseText} method. If no such child element is found, then the
	 * parent element's text content will be passed into {@link #_parseText} and
	 * the data type will be null.
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
	protected T _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		String value = null;
		VCardDataType dataType = null;
		Element rawElement = element.element();

		//get the text content of the first child element with the xCard namespace
		List<Element> children = XmlUtils.toElementList(rawElement.getChildNodes());
		for (Element child : children) {
			if (!element.version().getXmlNamespace().equals(child.getNamespaceURI())) {
				continue;
			}

			dataType = VCardDataType.get(child.getLocalName());
			value = child.getTextContent();
			break;
		}

		if (dataType == null) {
			//get the text content of the property element
			value = rawElement.getTextContent();
		}

		value = escape(value);
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
	 * @throws CannotParseException if the property value could not be parsed
	 * @throws SkipMeException if this property should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the value of this property is an
	 * embedded vCard (i.e. the AGENT property)
	 */
	protected T _parseHtml(HCardElement element, List<String> warnings) {
		String value = escape(element.value());
		VCardSubTypes parameters = new VCardSubTypes();
		T property = _parseText(value, null, VCardVersion.V3_0, parameters, warnings);
		property.setSubTypes(parameters);
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
	protected T _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		return _parseText(jcardValueToString(value), dataType, VCardVersion.V4_0, parameters, warnings);
	}

	private static String jcardValueToString(JCardValue value) {
		if (value.getValues().size() > 1) {
			List<String> multi = value.asMulti();
			if (!multi.isEmpty()) {
				return list(multi);
			}
		}

		if (!value.getValues().isEmpty() && value.getValues().get(0).getArray() != null) {
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
	protected static String unescape(String text) {
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
	protected static String escape(String text) {
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
	 * Splits a string by a delimiter.
	 * @param string the string to split (e.g. "one,two,three")
	 * @param delimiter the delimiter (e.g. ",")
	 * @return the factory object
	 */
	protected static Splitter split(String string, String delimiter) {
		return new Splitter(string, delimiter);
	}

	/**
	 * Factory class for splitting strings.
	 */
	protected static class Splitter {
		private String string;
		private String delimiter;
		private boolean removeEmpties = false;
		private boolean unescape = false;
		private int limit = -1;

		/**
		 * Creates a new splitter object.
		 * @param string the string to split (e.g. "one,two,three")
		 * @param delimiter the delimiter (e.g. ",")
		 */
		public Splitter(String string, String delimiter) {
			this.string = string;
			this.delimiter = delimiter;
		}

		/**
		 * Sets whether to remove empty elements.
		 * @param removeEmpties true to remove empty elements, false not to
		 * (default is false)
		 * @return this
		 */
		public Splitter removeEmpties(boolean removeEmpties) {
			this.removeEmpties = removeEmpties;
			return this;
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
		 * @return the split string
		 */
		public List<String> split() {
			//from: http://stackoverflow.com/q/820172">http://stackoverflow.com/q/820172
			String split[] = string.split("\\s*(?<!\\\\)" + Pattern.quote(delimiter) + "\\s*", limit);

			List<String> list = new ArrayList<String>(split.length);
			for (String s : split) {
				if (s.length() == 0 && removeEmpties) {
					continue;
				}

				if (unescape) {
					s = VCardPropertyScribe.unescape(s);
				}

				list.add(s);
			}
			return list;
		}
	}

	/**
	 * Parses a comma-separated list of values.
	 * @param value the string to parse (e.g. "one,two,th\,ree")
	 * @return the parsed values
	 */
	protected static List<String> list(String value) {
		if (value.length() == 0) {
			return new ArrayList<String>(0);
		}
		return split(value, ",").unescape(true).split();
	}

	/**
	 * Writes a comma-separated list of values.
	 * @param values the values to write
	 * @return the list
	 */
	protected static String list(Object... values) {
		return list(Arrays.asList(values));
	}

	/**
	 * Writes a comma-separated list of values.
	 * @param values the values to write
	 * @return the list
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
	 * Parses a list of values that are delimited by semicolons. Unlike
	 * structured value components, semi-structured components cannot be
	 * multi-valued.
	 * @param value the string to parse (e.g. "one;two;three")
	 * @return the parsed values
	 */
	protected static SemiStructuredIterator semistructured(String value) {
		return semistructured(value, -1);
	}

	/**
	 * Parses a list of values that are delimited by semicolons. Unlike
	 * structured value components, semi-structured components cannot be
	 * multi-valued.
	 * @param value the string to parse (e.g. "one;two;three")
	 * @param limit the max number of components to parse
	 * @return the parsed values
	 */
	protected static SemiStructuredIterator semistructured(String value, int limit) {
		List<String> split = split(value, ";").unescape(true).limit(limit).split();
		return new SemiStructuredIterator(split.iterator());
	}

	/**
	 * Parses a structured value.
	 * @param value the string to parse (e.g. "one;two,three;four")
	 * @return the parsed values
	 */
	protected static StructuredIterator structured(String value) {
		List<String> split = split(value, ";").split();
		List<List<String>> components = new ArrayList<List<String>>(split.size());
		for (String s : split) {
			components.add(list(s));
		}
		return new StructuredIterator(components.iterator());
	}

	/**
	 * Provides an iterator for a jCard structured value.
	 * @param value the jCard value
	 * @return the parsed values
	 */
	protected static StructuredIterator structured(JCardValue value) {
		return new StructuredIterator(value.asStructured().iterator());
	}

	/**
	 * <p>
	 * Writes a structured value.
	 * </p>
	 * <p>
	 * This method accepts a list of {@link Object} instances.
	 * {@link Collection} objects will be treated as multi-valued components.
	 * Null objects will be treated as empty components. All other objects will
	 * have their {@code toString()} method invoked to generate the string
	 * value.
	 * </p>
	 * @param values the values to write
	 * @return the structured value string
	 */
	protected static String structured(Object... values) {
		return join(Arrays.asList(values), ";", new JoinCallback<Object>() {
			public void handle(StringBuilder sb, Object value) {
				if (value == null) {
					return;
				}

				if (value instanceof Collection) {
					Collection<?> list = (Collection<?>) value;
					sb.append(list(list));
					return;
				}

				sb.append(escape(value.toString()));
			}
		});
	}

	/**
	 * Iterates over the fields in a structured value.
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
	 * Iterates over the fields in a semi-structured value (a structured value
	 * whose components cannot be multi-valued).
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
		 * @return the next value, null if the value is an empty string, or null
		 * if there are no more values
		 */
		public String next() {
			if (!hasNext()) {
				return null;
			}

			String value = it.next();
			return (value.length() == 0) ? null : value;
		}

		public boolean hasNext() {
			return it.hasNext();
		}
	}

	/**
	 * Parses a date string.
	 * @param value the date string
	 * @return the factory object
	 */
	protected static Date date(String value) {
		return VCardDateFormatter.parse(value);
	}

	/**
	 * Formats a {@link Date} object as a string.
	 * @param date the date
	 * @return the factory object
	 */
	protected static DateWriter date(Date date) {
		return new DateWriter(date);
	}

	/**
	 * Factory class for writing dates.
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
		 * timezone's UTC offset
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
			ISOFormat format;
			if (hasTime) {
				if (utc) {
					format = extended ? ISOFormat.UTC_TIME_EXTENDED : ISOFormat.UTC_TIME_BASIC;
				} else {
					format = extended ? ISOFormat.TIME_EXTENDED : ISOFormat.TIME_BASIC;
				}
			} else {
				format = extended ? ISOFormat.DATE_EXTENDED : ISOFormat.DATE_BASIC;
			}

			return VCardDateFormatter.format(date, format);
		}
	}

	/**
	 * Creates a {@link CannotParseException}, indicating that the XML elements
	 * that the parser expected to find are missing from the property's XML
	 * element.
	 * @param dataTypes the expected data types (null for "unknown")
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
	 * Creates a {@link CannotParseException}, indicating that the XML elements
	 * that the parser expected to find are missing from property's XML element.
	 * @param elements the names of the expected XML elements.
	 */
	protected static CannotParseException missingXmlElements(String... elements) {
		String message;

		switch (elements.length) {
		case 0:
			message = "Property value empty.";
			break;
		case 1:
			message = "Property value empty (no <" + elements[0] + "> element found).";
			break;
		case 2:
			message = "Property value empty (no <" + elements[0] + "> or <" + elements[1] + "> elements found).";
			break;
		default:
			StringBuilder sb = new StringBuilder();

			sb.append("Property value empty (no ");
			VCardStringUtils.join(Arrays.asList(elements).subList(0, elements.length - 1), ", ", sb, new JoinCallback<String>() {
				public void handle(StringBuilder sb, String value) {
					sb.append('<').append(value).append('>');
				}
			});
			sb.append(", or <").append(elements[elements.length - 1]).append("> elements found).");

			message = sb.toString();
			break;
		}

		return new CannotParseException(message);
	}

	/**
	 * Utility method for switching between the "PREF" and "TYPE=PREF"
	 * parameters, depending on the target vCard version. Meant to be called
	 * from a scribe's {@link #_prepareParameters} method.
	 * @param property the property that's being marshalled
	 * @param copy the parameters that are being marshalled
	 * @param version the vCard version
	 * @param vcard the vCard that's being marshalled
	 */
	protected static void handlePrefParam(VCardType property, VCardSubTypes copy, VCardVersion version, VCard vcard) {
		switch (version) {
		case V2_1:
		case V3_0:
			copy.setPref(null);

			//find the property with the lowest PREF value in the vCard
			VCardType mostPreferred = null;
			for (VCardType p : vcard.getTypes(property.getClass())) {
				Integer pref = p.getSubTypes().getPref();
				if (pref == null) {
					continue;
				}

				if (mostPreferred == null || pref < mostPreferred.getSubTypes().getPref()) {
					mostPreferred = p;
				}
			}

			if (property == mostPreferred) {
				copy.addType("pref");
			}

			break;
		case V4_0:
			for (String type : property.getSubTypes().getTypes()) {
				if ("pref".equalsIgnoreCase(type)) {
					copy.removeType(type);
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
	public static class Result<T extends VCardType> {
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
