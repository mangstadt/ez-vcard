package ezvcard.io.scribe;

import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.ParseContext;
import ezvcard.io.SkipMeException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.json.JsonValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.io.xml.XCardElement.XCardValue;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.util.VCardDateFormat;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
	 * @return the string value
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
	 * @param parameters the parsed parameters
	 * @param context the parse context
	 * @return the unmarshalled property
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the property value is an embedded vCard
	 * (i.e. the AGENT property)
	 */
	public final T parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		T property = _parseText(value, dataType, parameters, context);
		property.setParameters(parameters);
		return property;
	}

	/**
	 * Unmarshals a property's value from an XML document (xCard).
	 * @param element the property's XML element
	 * @param parameters the parsed parameters
	 * @param context the parse context
	 * @return the unmarshalled property
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	public final T parseXml(Element element, VCardParameters parameters, ParseContext context) {
		T property = _parseXml(new XCardElement(element), parameters, context);
		property.setParameters(parameters);
		return property;
	}

	/**
	 * Unmarshals the property from an HTML document (hCard).
	 * @param element the property's HTML element
	 * @param context the parse context
	 * @return the unmarshalled property
	 * @throws CannotParseException if the property value could not be parsed
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the property value is an embedded vCard
	 * (i.e. the AGENT property)
	 */
	public final T parseHtml(HCardElement element, ParseContext context) {
		return _parseHtml(element, context);
	}

	/**
	 * Unmarshals a property's value from a JSON data stream (jCard).
	 * @param value the property's JSON value
	 * @param dataType the data type
	 * @param parameters the parsed parameters
	 * @param context the parse context
	 * @return the unmarshalled property
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	public final T parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		T property = _parseJson(value, dataType, parameters, context);
		property.setParameters(parameters);
		return property;
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
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param context the parse context
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	protected abstract T _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context);

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
	 * @param context the parse context
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	protected T _parseXml(XCardElement element, VCardParameters parameters, ParseContext context) {
		XCardValue firstValue = element.firstValue();
		VCardDataType dataType = firstValue.getDataType();
		String value = VObjectPropertyValues.escape(firstValue.getValue());
		return _parseText(value, dataType, parameters, context);
	}

	/**
	 * <p>
	 * Unmarshals the property from an hCard (HTML document).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * hCard. The default implementation of this method will retrieve the HTML
	 * element's hCard value (as described in {@link HCardElement#value()}), and
	 * pass it into the {@link #_parseText} method.
	 * </p>
	 * @param element the property's HTML element
	 * @param context the parse context
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the property value could not be parsed
	 * @throws SkipMeException if this property should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the value of this property is an
	 * embedded vCard (i.e. the AGENT property)
	 */
	protected T _parseHtml(HCardElement element, ParseContext context) {
		String value = VObjectPropertyValues.escape(element.value());
		VCardParameters parameters = new VCardParameters();
		T property = _parseText(value, null, parameters, context);
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
	 * @param context the parse context
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link VCard} object
	 */
	protected T _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		String valueStr = jcardValueToString(value);
		return _parseText(valueStr, dataType, parameters, context);
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
				return VObjectPropertyValues.writeList(multi);
			}
		}

		if (!values.isEmpty() && values.get(0).getArray() != null) {
			List<List<String>> structured = value.asStructured();
			if (!structured.isEmpty()) {
				return VObjectPropertyValues.writeStructured(structured, true);
			}
		}

		return VObjectPropertyValues.escape(value.asSingle());
	}

	/**
	 * Parses a date string.
	 * @param value the date string
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date cannot be parsed
	 */
	protected static Temporal date(String value) {
		return VCardDateFormat.parse(value);
	}

	/**
	 * Formats a {@link Temporal} object as a string.
	 * @param date the date
	 * @return a helper object for customizing the write operation
	 */
	protected static DateWriter date(Temporal date) {
		return new DateWriter(date);
	}

	/**
	 * A helper class for writing date values.
	 */
	protected static class DateWriter {
		private Temporal date;
		private boolean extended = false;

		/**
		 * Creates a new date writer object.
		 * @param date the date to format
		 */
		public DateWriter(Temporal date) {
			this.date = date;
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
		 * Creates the date string.
		 * @return the date string
		 */
		public String write() {
			VCardDateFormat format = extended ? VCardDateFormat.EXTENDED : VCardDateFormat.BASIC;
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
	 * @param parameters the parameters that are being marshalled (this should
	 * be a copy of the property's parameters so that changes can be made to
	 * them without affecting the original object)
	 * @param version the vCard version that the vCard is being marshalled to
	 * @param vcard the vCard that's being marshalled
	 */
	protected static void handlePrefParam(VCardProperty property, VCardParameters parameters, VCardVersion version, VCard vcard) {
		switch (version) {
		case V2_1:
		case V3_0:
			parameters.setPref(null);

			//find the property with the lowest PREF value in the vCard
			VCardProperty mostPreferred = null;
			Integer lowestPref = null;
			for (VCardProperty p : vcard.getProperties(property.getClass())) {
				Integer pref;
				try {
					pref = p.getParameters().getPref();
				} catch (IllegalStateException e) {
					continue;
				}

				if (pref == null) {
					continue;
				}

				if (lowestPref == null || pref < lowestPref) {
					mostPreferred = p;
					lowestPref = pref;
				}
			}

			if (property == mostPreferred) {
				parameters.put(VCardParameters.TYPE, "pref");
			}

			break;
		case V4_0:
			for (String type : property.getParameters().get(VCardParameters.TYPE)) {
				if ("pref".equalsIgnoreCase(type)) {
					parameters.remove(VCardParameters.TYPE, type);
					parameters.setPref(1);
					break;
				}
			}
			break;
		}
	}

	/**
	 * <p>
	 * Escapes special characters in a property value for writing to a
	 * plain-text output stream.
	 * </p>
	 * <p>
	 * If the target version is 2.1, then the value will be returned unchanged.
	 * 2.1 only requires special characters to be escaped within properties that
	 * make use of those special characters.
	 * </p>
	 * @param value the value to escape
	 * @param context the write context
	 * @return the escaped value
	 */
	protected static String escape(String value, WriteContext context) {
		if (context.getVersion() == VCardVersion.V2_1) {
			return value;
		}

		return VObjectPropertyValues.escape(value);
	}
}
