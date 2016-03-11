package ezvcard.parameter;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ezvcard.Messages;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.property.Address;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.Sound;
import ezvcard.property.StructuredName;
import ezvcard.util.CharacterBitSet;
import ezvcard.util.GeoUri;
import ezvcard.util.ListMultimap;

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
 * Holds the parameters (aka "sub types") of a vCard property.
 * @author Michael Angstadt
 */
public class VCardParameters extends ListMultimap<String, String> {
	public static final String ALTID = "ALTID";
	public static final String CALSCALE = "CALSCALE";
	public static final String CHARSET = "CHARSET";
	public static final String ENCODING = "ENCODING";
	public static final String GEO = "GEO";
	public static final String INDEX = "INDEX";
	public static final String LABEL = "LABEL";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String LEVEL = "LEVEL";
	public static final String MEDIATYPE = "MEDIATYPE";
	public static final String PID = "PID";
	public static final String PREF = "PREF";
	public static final String SORT_AS = "SORT-AS";
	public static final String TYPE = "TYPE";
	public static final String TZ = "TZ";
	public static final String VALUE = "VALUE";

	private static final Map<String, Set<VCardVersion>> supportedVersions;
	static {
		Map<String, Set<VCardVersion>> m = new HashMap<String, Set<VCardVersion>>();
		m.put(ALTID, EnumSet.of(VCardVersion.V4_0));
		m.put(CALSCALE, EnumSet.of(VCardVersion.V4_0));
		m.put(CHARSET, EnumSet.of(VCardVersion.V2_1));
		m.put(GEO, EnumSet.of(VCardVersion.V4_0));
		m.put(INDEX, EnumSet.of(VCardVersion.V4_0));

		/*
		 * Don't check LABEL because this is removed and converted to LABEL
		 * properties for 2.1 and 3.0 vCards.
		 */
		//m.put(LABEL, EnumSet.of(VCardVersion.V4_0));

		m.put(LEVEL, EnumSet.of(VCardVersion.V4_0));
		m.put(MEDIATYPE, EnumSet.of(VCardVersion.V4_0));
		m.put(PID, EnumSet.of(VCardVersion.V4_0));

		/*
		 * Don't check PREF because this is removed and converted to "TYPE=PREF"
		 * for 2.1 and 3.0 vCards.
		 */
		//m.put(PREF, EnumSet.of(VCardVersion.V4_0));

		m.put(SORT_AS, EnumSet.of(VCardVersion.V4_0));
		m.put(TZ, EnumSet.of(VCardVersion.V4_0));

		supportedVersions = Collections.unmodifiableMap(m);
	}

	/**
	 * Creates a list of parameters.
	 */
	public VCardParameters() {
		//empty
	}

	/**
	 * Creates a copy of an existing parameter list.
	 * @param orig the object to copy
	 */
	public VCardParameters(VCardParameters orig) {
		super(orig);
	}

	/**
	 * <p>
	 * Gets the ENCODING parameter. This is used when the property value is
	 * encoded in a form other than plain text.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the encoding or null if not found
	 */
	public Encoding getEncoding() {
		String value = first(ENCODING);
		return (value == null) ? null : Encoding.get(value);
	}

	/**
	 * <p>
	 * Sets the ENCODING parameter. This is used when the property value is
	 * encoded in a form other than plain text.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param encoding the encoding or null to remove
	 */
	public void setEncoding(Encoding encoding) {
		replace(ENCODING, (encoding == null) ? null : encoding.getValue());
	}

	/**
	 * <p>
	 * Gets the VALUE parameter. This defines what kind of data type the
	 * property has, such as "text" or "URI". Only used in text-based vCards.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the value or null if not found
	 */
	public VCardDataType getValue() {
		String value = first(VALUE);
		return (value == null) ? null : VCardDataType.get(value);
	}

	/**
	 * <p>
	 * Sets the VALUE parameter. This defines what kind of data type the
	 * property has, such as "text" or "URI". Only used in text-based vCards.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param value the value or null to remove
	 */
	public void setValue(VCardDataType value) {
		replace(VALUE, (value == null) ? null : value.getName());
	}

	/**
	 * <p>
	 * Gets the CHARSET parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1}
	 * </p>
	 * @return the value or null if not found
	 */
	public String getCharset() {
		return first(CHARSET);
	}

	/**
	 * <p>
	 * Sets the CHARSET parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1}
	 * </p>
	 * @param charset the value or null to remove
	 */
	public void setCharset(String charset) {
		replace(CHARSET, charset);
	}

	/**
	 * <p>
	 * Gets the LANGUAGE parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the language (e.g. "en-US") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5646">RFC 5646</a>
	 */
	public String getLanguage() {
		return first(LANGUAGE);
	}

	/**
	 * <p>
	 * Sets the LANGUAGE parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param language the language (e.g "en-US") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5646">RFC 5646</a>
	 */
	public void setLanguage(String language) {
		replace(LANGUAGE, language);
	}

	/**
	 * <p>
	 * Gets the LABEL parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the address label or null if not set
	 */
	public String getLabel() {
		return first(LABEL);
	}

	/**
	 * <p>
	 * Sets the LABEL parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param label the address label or null to remove
	 */
	public void setLabel(String label) {
		replace(LABEL, label);
	}

	/**
	 * <p>
	 * Gets the TZ parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the timezone (e.g. "America/New_York") or null if not set
	 */
	public String getTimezone() {
		return first(TZ);
	}

	/**
	 * <p>
	 * Sets the TZ parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param tz the timezone (e.g. "America/New_York") or null to remove
	 */
	public void setTimezone(String tz) {
		replace(TZ, tz);
	}

	/**
	 * <p>
	 * Gets the first TYPE parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the value or null if not found.
	 */
	public String getType() {
		return first(TYPE);
	}

	/**
	 * <p>
	 * Sets the TYPE parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param type the value or null to remove
	 */
	public void setType(String type) {
		replace(TYPE, type);
	}

	/**
	 * <p>
	 * Gets the preference value. The lower the number, the more preferred this
	 * property instance is compared with other properties in the same vCard of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * <p>
	 * In the vCard below, the address on the second row is the most preferred
	 * because it has the lowest PREF value.
	 * </p>
	 * <p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:
	 * ADR;TYPE=work;PREF=1:
	 * ADR;TYPE=home:
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 * @return the preference value or null if not found
	 */
	public Integer getPref() {
		String pref = first(PREF);
		if (pref == null) {
			return null;
		}

		try {
			return Integer.valueOf(pref);
		} catch (NumberFormatException e) {
			throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(15, PREF), e);
		}
	}

	/**
	 * <p>
	 * Sets the preference value. The lower the number, the more preferred this
	 * property instance is compared with other properties in the same vCard of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * <p>
	 * In the vCard below, the address on the second row is the most preferred
	 * because it has the lowest PREF value.
	 * </p>
	 * <p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:
	 * ADR;TYPE=work;PREF=1:
	 * ADR;TYPE=home:
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param pref the preference value (must be between 1 and 100 inclusive) or
	 * null to remove
	 */
	public void setPref(Integer pref) {
		replace(PREF, (pref == null) ? null : pref.toString());
	}

	/**
	 * <p>
	 * Gets the ALTID parameter value. This is used to specify alternative
	 * representations of the same type.
	 * </p>
	 * 
	 * <p>
	 * For example, a vCard may contain multiple NOTE properties that each have
	 * the same ALTID. This means that each NOTE contains a different
	 * representation of the same information. In the example below, the first
	 * three NOTEs have the same ALTID. They each contain the same message, but
	 * each is written in a different language. The other NOTEs have different
	 * (or absent) ALTIDs, which means they are not associated with the top
	 * three.
	 * </p>
	 * 
	 * <pre>
	 * NOTE;ALTID=1;LANGUAGE=en: Hello world!
	 * NOTE;ALTID=1;LANGUAGE=fr: Bonjour tout le monde!
	 * NOTE;ALTID=1;LANGUAGE=es: ¡Hola, mundo!
	 * NOTE;ALTID=2;LANGUAGE=de: Meine Lieblingsfarbe ist blau.
	 * NOTE;ALTID=2;LANGUAGE=en: My favorite color is blue.
	 * NOTE: This vCard will self-destruct in 5 seconds.
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the ALTID or null if not found
	 */
	public String getAltId() {
		return first(ALTID);
	}

	/**
	 * <p>
	 * Sets the ALTID parameter value. This is used to specify alternative
	 * representations of the same type.
	 * </p>
	 * 
	 * <p>
	 * For example, a vCard may contain multiple NOTE properties that each have
	 * the same ALTID. This means that each NOTE contains a different
	 * representation of the same information. In the example below, the first
	 * three NOTEs have the same ALTID. They each contain the same message, but
	 * each is written in a different language. The other NOTEs have different
	 * (or absent) ALTIDs, which means they are not associated with the top
	 * three.
	 * </p>
	 * 
	 * <pre>
	 * NOTE;ALTID=1;LANGUAGE=en: Hello world!
	 * NOTE;ALTID=1;LANGUAGE=fr: Bonjour tout le monde!
	 * NOTE;ALTID=1;LANGUAGE=es: ¡Hola, mundo!
	 * NOTE;ALTID=2;LANGUAGE=de: Meine Lieblingsfarbe ist blau.
	 * NOTE;ALTID=2;LANGUAGE=en: My favorite color is blue.
	 * NOTE: This vCard will self-destruct in 5 seconds.
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altId the ALTID or null to remove
	 */
	public void setAltId(String altId) {
		replace(ALTID, altId);
	}

	/**
	 * <p>
	 * Gets the GEO parameter value. This is used to associate global
	 * positioning information with a vCard property. It can be used with the
	 * {@link Address} property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the geo URI or null if not found
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 */
	public GeoUri getGeo() {
		String value = first(GEO);
		if (value == null) {
			return null;
		}

		try {
			return GeoUri.parse(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(15, GEO), e);
		}
	}

	/**
	 * <p>
	 * Sets the GEO parameter value. This is used to associate global
	 * positioning information with a vCard property. It can be used with the
	 * {@link Address} property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param uri the geo URI or null to remove
	 */
	public void setGeo(GeoUri uri) {
		replace(GEO, (uri == null) ? null : uri.toString());
	}

	/**
	 * <p>
	 * Gets the SORT-AS parameter value(s). This contains typically two string
	 * values which the vCard should be sorted by (family and given names). This
	 * is useful if the person's last name (defined in the N property) starts
	 * with characters that should be ignored during sorting. It can be used
	 * with the {@link StructuredName} and {@link Organization} properties.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the name(s) (e.g. { "Aboville", "Christine" } if the family name
	 * is "d'Aboville" and the given name is "Christine") or empty list of the
	 * parameter doesn't exist
	 */
	public List<String> getSortAs() {
		return get(SORT_AS);
	}

	/**
	 * <p>
	 * Sets the SORT-AS parameter value(s). This is useful with the N property
	 * when the person's last name starts with characters that should be ignored
	 * during sorting. It can be used with the {@link StructuredName} and
	 * {@link Organization} properties.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param names the names in the order they should be sorted in (e.g.
	 * ["Aboville", "Christine"] if the family name is "d'Aboville" and the
	 * given name is "Christine") or empty parameter list to remove
	 */
	public void setSortAs(String... names) {
		removeAll(SORT_AS);

		if (names == null || (names.length == 1 && names[0] == null)) {
			return;
		}

		putAll(SORT_AS, names);
	}

	/**
	 * <p>
	 * Gets the type of calendar that is used for a date or date-time property
	 * value.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the type of calendar or null if not found
	 */
	public Calscale getCalscale() {
		String value = first(CALSCALE);
		return (value == null) ? null : Calscale.get(value);
	}

	/**
	 * <p>
	 * Sets the type of calendar that is used for a date or date-time property
	 * value.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param value the type of calendar or null to remove
	 */
	public void setCalscale(Calscale value) {
		replace(CALSCALE, (value == null) ? null : value.getValue());
	}

	/**
	 * <p>
	 * Gets the MEDIATYPE parameter. This is used in properties that have a URL
	 * as a value, such as {@link Photo} and {@link Sound}. It defines the
	 * content type of the referenced resource.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the media type (e.g. "image/jpeg") or null if not found
	 */
	public String getMediaType() {
		return first(MEDIATYPE);
	}

	/**
	 * <p>
	 * Sets the MEDIATYPE parameter. This is used in properties that have a URL
	 * as a value, such as {@link Photo} and {@link Sound}. It defines the
	 * content type of the referenced resource.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param mediaType the media type (e.g. "image/jpeg") or null to remove
	 */
	public void setMediaType(String mediaType) {
		replace(MEDIATYPE, mediaType);
	}

	/**
	 * <p>
	 * Gets the LEVEL parameter. This is used to define the level of skill or
	 * level of interest the person has towards something.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the level (e.g. "beginner") or null if not found
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public String getLevel() {
		return first(LEVEL);
	}

	/**
	 * <p>
	 * Sets the LEVEL parameter. This is used to define the level of skill or
	 * level of interest the person has towards something.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param level the level (e.g. "beginner") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void setLevel(String level) {
		replace(LEVEL, level);
	}

	/**
	 * <p>
	 * Gets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list and properties with high
	 * index values are put at the end of the list.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the index or null if not found
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public Integer getIndex() {
		String index = first(INDEX);
		if (index == null) {
			return null;
		}

		try {
			return Integer.valueOf(index);
		} catch (NumberFormatException e) {
			throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(15, INDEX), e);
		}
	}

	/**
	 * <p>
	 * Sets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list and properties with high
	 * index values are put at the end of the list.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param index the index (must be greater than 0) or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void setIndex(Integer index) {
		replace(INDEX, (index == null) ? null : index.toString());
	}

	/**
	 * Checks this parameters list for data consistency problems or deviations
	 * from the spec. These problems will not prevent the vCard from being
	 * written to a data stream, but may prevent it from being parsed correctly
	 * by the consuming application.
	 * @param version the vCard version to validate against
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public List<Warning> validate(VCardVersion version) {
		List<Warning> warnings = new ArrayList<Warning>(0);

		/*
		 * Check for invalid characters in names and values.
		 */
		{
			final int invalidCharsInParamValueCode = 25;
			final int invalidCharsInParamNameCode = 26;

			BitSet invalidValueChars = new BitSet(128);
			invalidValueChars.set(0, 31);
			invalidValueChars.set(127);
			invalidValueChars.set('\t', false); //allow
			invalidValueChars.set('\n', false); //allow
			invalidValueChars.set('\r', false); //allow
			if (version == VCardVersion.V2_1) {
				invalidValueChars.set(',');
				invalidValueChars.set('.');
				invalidValueChars.set(':');
				invalidValueChars.set('=');
				invalidValueChars.set('[');
				invalidValueChars.set(']');
			}

			CharacterBitSet validNameChars = new CharacterBitSet("-a-zA-Z0-9");
			for (Map.Entry<String, List<String>> entry : this) {
				String name = entry.getKey();

				/*
				 * Don't check LABEL for 2.1 and 3.0 because this is converted
				 * to a property in those versions.
				 */
				if (version != VCardVersion.V4_0 && LABEL.equalsIgnoreCase(name)) {
					continue;
				}

				//check the parameter name
				if (!validNameChars.containsOnly(name)) {
					warnings.add(new Warning(invalidCharsInParamNameCode, name));
				}

				//check the parameter value(s)
				List<String> values = entry.getValue();
				for (String value : values) {
					for (int i = 0; i < value.length(); i++) {
						char c = value.charAt(i);
						if (invalidValueChars.get(c)) {
							warnings.add(new Warning(invalidCharsInParamValueCode, name, value, (int) c, i));
							break;
						}
					}
				}
			}
		}

		/*
		 * Check for invalid or unsupported values (e.g. "ENCODING=foo").
		 */
		{
			final int nonStandardValueCode = 3;
			final int unsupportedValueCode = 4;

			String value = first(CALSCALE);
			if (value != null && Calscale.find(value) == null) {
				warnings.add(new Warning(nonStandardValueCode, CALSCALE, value, Calscale.all()));
			}

			value = first(ENCODING);
			if (value != null) {
				Encoding encoding = Encoding.find(value);
				if (encoding == null) {
					warnings.add(new Warning(nonStandardValueCode, ENCODING, value, Encoding.all()));
				} else if (!encoding.isSupportedBy(version)) {
					warnings.add(new Warning(unsupportedValueCode, ENCODING, value));
				}
			}

			value = first(VALUE);
			if (value != null) {
				VCardDataType dataType = VCardDataType.find(value);
				if (dataType == null) {
					warnings.add(new Warning(nonStandardValueCode, VALUE, value, VCardDataType.all()));
				} else if (!dataType.isSupportedBy(version)) {
					warnings.add(new Warning(unsupportedValueCode, VALUE, value));
				}
			}
		}

		/*
		 * Check for parameters with malformed values.
		 */
		{
			final int malformedValueCode = 5;

			try {
				getGeo();
			} catch (IllegalStateException e) {
				warnings.add(new Warning(malformedValueCode, GEO, first(GEO)));
			}

			try {
				Integer index = getIndex();
				if (index != null && index <= 0) {
					warnings.add(new Warning(28, index));
				}
			} catch (IllegalStateException e) {
				warnings.add(new Warning(malformedValueCode, INDEX, first(INDEX)));
			}

			List<String> pids = get(PID);
			for (String pid : pids) {
				if (!isPidValid(pid)) {
					warnings.add(new Warning(27, pid));
				}
			}

			try {
				Integer pref = getPref();
				if (pref != null && (pref < 1 || pref > 100)) {
					warnings.add(new Warning(29, pref));
				}
			} catch (IllegalStateException e) {
				warnings.add(new Warning(malformedValueCode, PREF, first(PREF)));
			}
		}

		/*
		 * Check that each parameter is supported by the given vCard version.
		 */
		{
			final int paramNotSupportedCode = 6;

			for (Map.Entry<String, Set<VCardVersion>> entry : supportedVersions.entrySet()) {
				String name = entry.getKey();
				String value = first(name);
				if (value == null) {
					continue;
				}

				Set<VCardVersion> versions = entry.getValue();
				if (!versions.contains(version)) {
					warnings.add(new Warning(paramNotSupportedCode, name));
				}
			}
		}

		/*
		 * Check that the CHARSET parameter has a character set that is
		 * supported by this JVM.
		 */
		{
			final int invalidCharsetCode = 22;

			String charsetStr = getCharset();
			if (charsetStr != null) {
				try {
					Charset.forName(charsetStr);
				} catch (IllegalCharsetNameException e) {
					warnings.add(new Warning(invalidCharsetCode, charsetStr));
				} catch (UnsupportedCharsetException e) {
					warnings.add(new Warning(invalidCharsetCode, charsetStr));
				}
			}
		}

		return warnings;
	}

	private static boolean isPidValid(String pid) {
		boolean dotFound = false;
		for (int i = 0; i < pid.length(); i++) {
			char c = pid.charAt(i);

			if (c == '.') {
				if (i == 0 || i == pid.length() - 1) {
					return false;
				}
				if (dotFound) {
					return false;
				}
				dotFound = true;
				continue;
			}

			if (c >= '0' && c <= '9') {
				continue;
			}

			return false;
		}

		return true;
	}

	@Override
	protected String sanitizeKey(String key) {
		return (key == null) ? null : key.toUpperCase();
	}

	@Override
	public int hashCode() {
		/*
		 * Remember: Keys are case-insensitive, key order does not matter, and
		 * value order does not matter
		 */
		final int prime = 31;
		int result = 1;

		for (Map.Entry<String, List<String>> entry : this) {
			String key = entry.getKey();
			List<String> value = entry.getValue();

			int valueHash = 1;
			for (String v : value) {
				valueHash += v.toLowerCase().hashCode();
			}

			int entryHash = 1;
			entryHash += prime * entryHash + key.toLowerCase().hashCode();
			entryHash += prime * entryHash + valueHash;

			result += entryHash;
		}

		return result;
	}

	/**
	 * <p>
	 * Determines whether the given object is logically equivalent to this list
	 * of vCard parameters.
	 * </p>
	 * <p>
	 * vCard parameters are case-insensitive. Also, the order in which they are
	 * defined does not matter.
	 * </p>
	 * @param obj the object to compare to
	 * @return true if the objects are equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		/*
		 * Remember: Keys are case-insensitive, key order does not matter, and
		 * value order does not matter
		 */
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		VCardParameters other = (VCardParameters) obj;
		if (size() != other.size()) return false;

		for (Map.Entry<String, List<String>> entry : this) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			List<String> otherValue = other.get(key);

			if (value.size() != otherValue.size()) {
				return false;
			}

			List<String> valueLower = new ArrayList<String>(value.size());
			for (String v : value) {
				valueLower.add(v.toLowerCase());
			}
			Collections.sort(valueLower);

			List<String> otherValueLower = new ArrayList<String>(otherValue.size());
			for (String v : otherValue) {
				otherValueLower.add(v.toLowerCase());
			}
			Collections.sort(otherValueLower);

			if (!valueLower.equals(otherValueLower)) {
				return false;
			}
		}

		return true;
	}
}