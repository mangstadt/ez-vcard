package ezvcard.parameter;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.property.Address;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;
import ezvcard.util.GeoUri;
import ezvcard.util.ListMultimap;

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

		//don't check LABEL because this is removed and converted to LABEL properties for 2.1 and 3.0 vCards 
		//m.put(LABEL, EnumSet.of(VCardVersion.V4_0));

		m.put(LEVEL, EnumSet.of(VCardVersion.V4_0));
		m.put(MEDIATYPE, EnumSet.of(VCardVersion.V4_0));
		m.put(PID, EnumSet.of(VCardVersion.V4_0));

		//don't check PREF because this is removed and converted to "TYPE=PREF" for 2.1 and 3.0 vCards
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
	 * Removes the VALUE parameter. This defines what kind of data type the
	 * property has, such as "text" or "URI". Only used in text-based vCards.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 */
	public void removeValue() {
		removeAll(VALUE);
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
	 * Gets all TYPE parameters.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the values or empty set if not found
	 */
	public Set<String> getTypes() {
		return new HashSet<String>(get(TYPE));
	}

	/**
	 * <p>
	 * Adds a TYPE parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param type the value
	 */
	public void addType(String type) {
		put(TYPE, type);
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
		Set<String> types = getTypes();
		return types.isEmpty() ? null : types.iterator().next();
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
	 * Removes a TYPE parameter.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param type the value to remove
	 */
	public void removeType(String type) {
		remove(TYPE, type);
	}

	/**
	 * <p>
	 * Removes all TYPE parameters.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 */
	public void removeTypes() {
		removeAll(TYPE);
	}

	/**
	 * <p>
	 * Gets the preference value. The lower the number, the more preferred this
	 * property instance is compared with other properties in the same vCard of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * 
	 * <p>
	 * In the vCard below, the address on the second row is the most preferred
	 * because it has the lowest PREF value.
	 * </p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:
	 * ADR;TYPE=work;PREF=1:
	 * ADR;TYPE=home:
	 * </pre>
	 * 
	 * <p>
	 * Preference values must be numeric and must be between 1 and 100.
	 * </p>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 * @return the preference value or null if it doesn't exist or null if it
	 * couldn't be parsed into a number
	 */
	public Integer getPref() {
		String pref = first(PREF);
		if (pref == null) {
			return null;
		}

		try {
			return Integer.valueOf(pref);
		} catch (NumberFormatException e) {
			throw new IllegalStateException(PREF + " parameter value is malformed and could not be parsed. Retrieve its raw text value instead.", e);
		}
	}

	/**
	 * <p>
	 * Sets the preference value. The lower the number, the more preferred this
	 * property instance is compared with other properties in the same vCard of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * 
	 * <p>
	 * In the vCard below, the address on the second row is the most preferred
	 * because it has the lowest PREF value.
	 * </p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:
	 * ADR;TYPE=work;PREF=1:
	 * ADR;TYPE=home:
	 * </pre>
	 * 
	 * <p>
	 * Preference values must be numeric and must be between 1 and 100.
	 * </p>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param pref the preference value or null to remove
	 * @throws IllegalArgumentException if the value is not between 1 and 100
	 */
	public void setPref(Integer pref) {
		if (pref != null && (pref < 1 || pref > 100)) {
			throw new IllegalArgumentException("Preference value must be between 1 and 100 inclusive.");
		}
		String value = (pref == null) ? null : pref.toString();
		replace(PREF, value);
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
	 * NOTE;ALTID=1;LANGUAGE=es: �Hola, mundo!
	 * NOTE;ALTID=2;LANGUAGE=de: Meine Lieblingsfarbe ist blau.
	 * NOTE;ALTID=2;LANGUAGE=en: My favorite color is blue.
	 * NOTE: This vCard will self-destruct in 5 seconds.
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the ALTID or null if it doesn't exist
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
	 * NOTE;ALTID=1;LANGUAGE=es: �Hola, mundo!
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
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 * @return the latitude (index 0) and longitude (index 1) or null if not
	 * present or null if the parameter value was in an incorrect format
	 */
	public double[] getGeo() {
		String value = first(GEO);
		if (value == null) {
			return null;
		}

		try {
			GeoUri geoUri = GeoUri.parse(value);
			return new double[] { geoUri.getCoordA(), geoUri.getCoordB() };
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(GEO + " parameter value is malformed and could not be parsed. Retrieve its raw text value instead.", e);
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
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public void setGeo(double latitude, double longitude) {
		GeoUri geoUri = new GeoUri.Builder(latitude, longitude).build();
		replace(GEO, geoUri.toString());
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
		if (names != null && names.length > 0) {
			for (String name : names) {
				put(SORT_AS, name);
			}
		}
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
	 * Gets all PID parameter values. PIDs can exist on any property where
	 * multiple instances are allowed (such as EMAIL or ADR, but not N because
	 * only 1 instance of N is allowed).
	 * </p>
	 * <p>
	 * When used in conjunction with the CLIENTPIDMAP property, it allows an
	 * individual property instance to be uniquely identifiable. This feature is
	 * made use of when two different versions of the same vCard have to be
	 * merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 * @return the PID values or empty set if there are none. Index 0 is the
	 * local ID and index 1 is the ID used to reference the CLIENTPIDMAP
	 * property. Index 0 will never be null, but index 1 may be null.
	 */
	public List<Integer[]> getPids() {
		List<String> values = get(PID);
		List<Integer[]> pids = new ArrayList<Integer[]>(values.size());
		for (String value : values) {
			String split[] = value.split("\\.");
			try {
				Integer localId = Integer.valueOf(split[0]);
				Integer clientPidMapRef = (split.length > 1) ? Integer.valueOf(split[1]) : null;
				pids.add(new Integer[] { localId, clientPidMapRef });
			} catch (NumberFormatException e) {
				throw new IllegalStateException(PID + " parameter value is malformed and could not be parsed. Retrieve its raw text value instead.", e);
			}
		}
		return pids;
	}

	/**
	 * <p>
	 * Adds a PID parameter value. PIDs can exist on any property where multiple
	 * instances are allowed (such as EMAIL or ADR, but not N because only 1
	 * instance of N is allowed).
	 * </p>
	 * <p>
	 * When used in conjunction with the CLIENTPIDMAP property, it allows an
	 * individual property instance to be uniquely identifiable. This feature is
	 * made use of when two different versions of the same vCard have to be
	 * merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param localId the local ID
	 */
	public void addPid(int localId) {
		put(PID, localId + "");
	}

	/**
	 * <p>
	 * Adds a PID parameter value. PIDs can exist on any property where multiple
	 * instances are allowed (such as EMAIL or ADR, but not N because only 1
	 * instance of N is allowed).
	 * </p>
	 * <p>
	 * When used in conjunction with the CLIENTPIDMAP property, it allows an
	 * individual property instance to be uniquely identifiable. This feature is
	 * made use of when two different versions of the same vCard have to be
	 * merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param localId the local ID
	 * @param clientPidMapRef the ID used to reference the property's globally
	 * unique identifier in the CLIENTPIDMAP property.
	 */
	public void addPid(int localId, int clientPidMapRef) {
		put(PID, localId + "." + clientPidMapRef);
	}

	/**
	 * <p>
	 * Removes all PID values.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 */
	public void removePids() {
		removeAll(PID);
	}

	/**
	 * <p>
	 * Gets the MEDIATYPE parameter. This is used in properties that have a URL
	 * as a value, such as PHOTO and SOUND. It defines the content type of the
	 * referenced resource.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the media type (e.g. "image/jpeg") or null if it doesn't exist
	 */
	public String getMediaType() {
		return first(MEDIATYPE);
	}

	/**
	 * <p>
	 * Sets the MEDIATYPE parameter. This is used in properties that have a URL
	 * as a value, such as PHOTO and SOUND. It defines the content type of the
	 * referenced resource.
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
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 * @return the INDEX value or null if it doesn't exist or null if it
	 * couldn't be parsed into a number
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
			throw new IllegalStateException(INDEX + " parameter value is malformed and could not be parsed. Retrieve its raw text value instead.", e);
		}
	}

	/**
	 * <p>
	 * Sets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list and properties with high
	 * index values are put at the end of the list.
	 * </p>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param index the INDEX value (must be greater than 0) or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 * @throws IllegalArgumentException if the value is not greater than 0
	 */
	public void setIndex(Integer index) {
		if (index != null && index <= 0) {
			throw new IllegalArgumentException("Index value must be greater than 0.");
		}
		String value = (index == null) ? null : index.toString();
		replace(INDEX, value);
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

		{
			int nonStandardCode = 3;
			int valueNotSupportedCode = 4;

			String value = first(CALSCALE);
			if (value != null && Calscale.find(value) == null) {
				warnings.add(new Warning(nonStandardCode, CALSCALE, value, Calscale.all()));
			}

			value = first(ENCODING);
			if (value != null) {
				Encoding encoding = Encoding.find(value);
				if (encoding == null) {
					warnings.add(new Warning(nonStandardCode, ENCODING, value, Encoding.all()));
				} else if (!encoding.isSupported(version)) {
					warnings.add(new Warning(valueNotSupportedCode, ENCODING, value));
				}
			}

			value = first(VALUE);
			if (value != null) {
				VCardDataType dataType = VCardDataType.find(value);
				if (dataType == null) {
					warnings.add(new Warning(nonStandardCode, VALUE, value, VCardDataType.all()));
				} else if (!dataType.isSupported(version)) {
					warnings.add(new Warning(valueNotSupportedCode, VALUE, value));
				}
			}
		}

		{
			int malformedCode = 5;

			try {
				getGeo();
			} catch (IllegalStateException e) {
				warnings.add(new Warning(malformedCode, GEO, first(GEO)));
			}

			try {
				getIndex();
			} catch (IllegalStateException e) {
				warnings.add(new Warning(malformedCode, INDEX, first(INDEX)));
			}

			try {
				getPids();
			} catch (IllegalStateException e) {
				warnings.add(new Warning(malformedCode, PID, first(PID)));
			}

			try {
				getPref();
			} catch (IllegalStateException e) {
				warnings.add(new Warning(malformedCode, PREF, first(PREF)));
			}
		}

		{
			int paramNotSupportedCode = 6;
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

		{
			int invalidCharsetCode = 22;
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

	@Override
	protected String sanitizeKey(String key) {
		return (key == null) ? null : key.toUpperCase();
	}
}