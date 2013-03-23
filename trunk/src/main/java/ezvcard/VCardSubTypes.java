package ezvcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ezvcard.parameters.CalscaleParameter;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.LevelParameter;
import ezvcard.parameters.TypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.GeoUri;
import ezvcard.util.ListMultimap;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * Holds the parameters (aka "sub types") of a vCard Type.
 * @author Michael Angstadt
 */
public class VCardSubTypes {
	private final ListMultimap<String, String> subTypes;

	public VCardSubTypes() {
		subTypes = new ListMultimap<String, String>();
	}

	/**
	 * Copy constructor.
	 * @param orig the object to copy
	 */
	public VCardSubTypes(VCardSubTypes orig) {
		subTypes = new ListMultimap<String, String>(orig.subTypes);
	}

	/**
	 * Adds a value to a Sub Type.
	 * @param name the Sub Type name
	 * @param value the value to add
	 */
	public void put(String name, String value) {
		subTypes.put(name.toUpperCase(), value);
	}

	/**
	 * Adds multiple values to a Sub Type.
	 * @param name the Sub Type name
	 * @param values the values to add
	 */
	public void putAll(String name, Collection<String> values) {
		subTypes.putAll(name.toUpperCase(), values);
	}

	/**
	 * Adds a value to a Sub Type, replacing all existing values that the Sub
	 * Type has.
	 * @param name the Sub Type name
	 * @param value the values to replace all existing values with
	 * @return the values of the Sub Type that were replaced
	 */
	public List<String> replace(String name, String value) {
		List<String> replaced = removeAll(name);
		if (value != null) {
			put(name, value);
		}
		return replaced;
	}

	/**
	 * Removes a Sub Type.
	 * @param name the Sub Type name
	 * @return the values of the Sub Type that were removed
	 */
	public List<String> removeAll(String name) {
		return subTypes.remove(name.toUpperCase());
	}

	/**
	 * Removes a value from a Sub Type.
	 * @param name the Sub Type name
	 * @param value the value to remove
	 */
	public void remove(String name, String value) {
		subTypes.remove(name.toUpperCase(), value);
	}

	/**
	 * Gets the values of a Sub Type
	 * @param name the Sub Type name
	 * @return the values or an empty list if the Sub Type doesn't exist
	 */
	public List<String> get(String name) {
		return subTypes.get(name.toUpperCase());
	}

	/**
	 * Gets the first value of a Sub Type.
	 * @param name the Sub Type name
	 * @return the first value or null if the Sub Type doesn't exist
	 */
	public String getFirst(String name) {
		List<String> list = get(name);
		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * Gets the names of all the Sub Types.
	 * @return the names of all the Sub Types or an empty set if there are no
	 * Sub Types
	 */
	public Set<String> getNames() {
		return subTypes.keySet();
	}

	/**
	 * Gets the object used to store the Sub Types.
	 * @return the object used to store the Sub Types
	 */
	public ListMultimap<String, String> getMultimap() {
		return subTypes;
	}

	/**
	 * Gets the ENCODING sub type. This is used when the type value is encoded
	 * in a form other than plain text.
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @return the encoding or null if not found
	 */
	public EncodingParameter getEncoding() {
		String value = getFirst(EncodingParameter.NAME);
		if (value == null) {
			return null;
		}
		EncodingParameter encoding = EncodingParameter.valueOf(value);
		if (encoding == null) {
			encoding = new EncodingParameter(value);
		}
		return encoding;
	}

	/**
	 * Sets the ENCODING sub type. This is used when the type value is encoded
	 * in a form other than plain text.
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @param encoding the encoding or null to remove
	 */
	public void setEncoding(EncodingParameter encoding) {
		replace(EncodingParameter.NAME, (encoding == null) ? null : encoding.getValue());
	}

	/**
	 * Gets the VALUE sub type. This defines what kind of value the type has,
	 * such as "text" or "URI".
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the value or null if not found
	 */
	public ValueParameter getValue() {
		String value = getFirst(ValueParameter.NAME);
		if (value == null) {
			return null;
		}
		ValueParameter p = ValueParameter.valueOf(value);
		if (p == null) {
			p = new ValueParameter(value);
		}
		return p;
	}

	/**
	 * Sets the VALUE sub type. This defines what kind of value the type has,
	 * such as "text" or "URI".
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param value the value or null to remove
	 */
	public void setValue(ValueParameter value) {
		replace(ValueParameter.NAME, (value == null) ? null : value.getValue());
	}

	/**
	 * Gets the CHARSET sub type.
	 * <p>
	 * vCard versions: 2.1
	 * </p>
	 * @return the value or null if not found
	 */
	public String getCharset() {
		return getFirst("CHARSET");
	}

	/**
	 * Sets the CHARSET sub type
	 * <p>
	 * vCard versions: 2.1
	 * </p>
	 * @param charset the value or null to remove
	 */
	public void setCharset(String charset) {
		replace("CHARSET", charset);
	}

	/**
	 * Gets the LANGUAGE sub type.
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the language (e.g. "en-US") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5646">RFC 5646</a>
	 */
	public String getLanguage() {
		return getFirst("LANGUAGE");
	}

	/**
	 * Sets the LANGUAGE sub type.
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param language the language (e.g "en-US") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5646">RFC 5646</a>
	 */
	public void setLanguage(String language) {
		replace("LANGUAGE", language);
	}

	/**
	 * Gets all TYPE sub types.
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the values or empty set if not found
	 */
	public Set<String> getTypes() {
		return new HashSet<String>(get(TypeParameter.NAME));
	}

	/**
	 * Adds a TYPE sub type
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param type the value
	 */
	public void addType(String type) {
		put(TypeParameter.NAME, type);
	}

	/**
	 * Gets the first TYPE sub type.
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the value or null if not found.
	 */
	public String getType() {
		Set<String> types = getTypes();
		return types.isEmpty() ? null : types.iterator().next();
	}

	/**
	 * Sets the TYPE sub type.
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param type the value or null to remove
	 */
	public void setType(String type) {
		replace(TypeParameter.NAME, type);
	}

	/**
	 * Removes a TYPE sub type.
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param type the value to remove
	 */
	public void removeType(String type) {
		remove(TypeParameter.NAME, type);
	}

	/**
	 * <p>
	 * Gets the preference value. The lower the number, the more preferred this
	 * type is compared to other types in the vCard with the same name. If a
	 * type doesn't have a preference value, then it is considered the
	 * <b>least</b> preferred.
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
	 * vCard versions: 4.0
	 * </p>
	 * @return the preference value or null if it doesn't exist or null if it
	 * couldn't be parsed into a number
	 */
	public Integer getPref() {
		String pref = getFirst("PREF");
		if (pref == null) {
			return null;
		}

		try {
			return Integer.valueOf(pref);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * <p>
	 * Sets the preference value. The lower the number, the more preferred this
	 * type is compared to other types in the vCard with the same name. If a
	 * type doesn't have a preference value, then it is considered the
	 * <b>least</b> preferred.
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
	 * vCard versions: 4.0
	 * </p>
	 * @param pref the preference value or null to remove
	 * @throws IllegalArgumentException if the value is not between 1 and 100
	 */
	public void setPref(Integer pref) {
		if (pref != null && (pref < 1 || pref > 100)) {
			throw new IllegalArgumentException("Preference value must be between 1 and 100 inclusive.");
		}
		String value = (pref == null) ? null : pref.toString();
		replace("PREF", value);
	}

	/**
	 * Gets the ALTID parameter value. This is used to specify alternative
	 * representations of the same type.
	 * 
	 * <p>
	 * For example, a vCard may contain multiple NOTE types that each have the
	 * same ALTID. This means that each NOTE contains a different representation
	 * of the same information. In the example below, the first three NOTEs have
	 * the same ALTID. They each contain the same message, but each is written
	 * in a different language. The other NOTEs have different (or absent)
	 * ALTIDs, which means they are not associated with the top three.
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
	 * vCard versions: 4.0
	 * </p>
	 * @return the ALTID or null if it doesn't exist
	 */
	public String getAltId() {
		return getFirst("ALTID");
	}

	/**
	 * Sets the ALTID parameter value. This is used to specify alternative
	 * representations of the same type.
	 * 
	 * <p>
	 * For example, a vCard may contain multiple NOTE types that each have the
	 * same ALTID. This means that each NOTE contains a different representation
	 * of the same information. In the example below, the first three NOTEs have
	 * the same ALTID. They each contain the same message, but each is written
	 * in a different language. The other NOTEs have different (or absent)
	 * ALTIDs, which means they are not associated with the top three.
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
	 * vCard versions: 4.0
	 * </p>
	 * @param altId the ALTID or null to remove
	 */
	public void setAltId(String altId) {
		replace("ALTID", altId);
	}

	/**
	 * Gets the GEO parameter value. This is used to associate global
	 * positioning information with a vCard type. It can be used with the ADR
	 * type.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the latitude (index 0) and longitude (index 1) or null if not
	 * present or null if the parameter value was in an incorrect format
	 */
	public double[] getGeo() {
		String value = getFirst("GEO");
		if (value == null) {
			return null;
		}

		try {
			GeoUri geoUri = new GeoUri(value);
			return new double[] { geoUri.getCoordA(), geoUri.getCoordB() };
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Sets the GEO parameter value. This is used to associate global
	 * positioning information with a vCard type. It can be used with the ADR
	 * type.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public void setGeo(double latitude, double longitude) {
		GeoUri geoUri = new GeoUri(latitude, longitude, null, null, null);
		replace("GEO", geoUri.toString());
	}

	/**
	 * Gets the SORT-AS parameter value(s). This contains typically two string
	 * values which the vCard should be sorted by (family and given names). This
	 * is useful if the person's last name (defined in the N property) starts
	 * with characters that should be ignored during sorting. It can be used
	 * with the N and ORG types.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the name(s) (e.g. { "Aboville", "Christine" } if the family name
	 * is "d'Aboville" and the given name is "Christine") or empty list of the
	 * parameter doesn't exist
	 */
	public List<String> getSortAs() {
		return get("SORT-AS");
	}

	/**
	 * Sets the SORT-AS parameter value(s). This is useful with the N property
	 * when the person's last name starts with characters that should be ignored
	 * during sorting. It can be used with the N and ORG types.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param names the names in the order they should be sorted in (e.g.
	 * ["Aboville", "Christine"] if the family name is "d'Aboville" and the
	 * given name is "Christine") or empty parameter list to remove
	 */
	public void setSortAs(String... names) {
		removeAll("SORT-AS");
		if (names != null && names.length > 0) {
			for (String name : names) {
				put("SORT-AS", name);
			}
		}
	}

	/**
	 * Gets the CALSCALE parameter value. This defines the type of calendar that
	 * is used.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the type of calendar or null if not found
	 */
	public CalscaleParameter getCalscale() {
		String value = getFirst(CalscaleParameter.NAME);
		if (value == null) {
			return null;
		}
		CalscaleParameter p = CalscaleParameter.valueOf(value);
		if (p == null) {
			p = new CalscaleParameter(value);
		}
		return p;
	}

	/**
	 * Gets the CALSCALE parameter value. This is used with date/time types and
	 * defines the type of calendar that is used.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param value the type of calendar or null to remove
	 */
	public void setCalscale(CalscaleParameter value) {
		replace(CalscaleParameter.NAME, (value == null) ? null : value.getValue());
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
	 * vCard versions: 4.0
	 * </p>
	 * @return the PID values or empty set if there are none. Index 0 is the
	 * local ID and index 1 is the ID used to reference the CLIENTPIDMAP
	 * property. Index 0 will never be null, but index 1 may be null.
	 */
	public List<Integer[]> getPids() {
		List<String> values = get("PID");
		List<Integer[]> pids = new ArrayList<Integer[]>(values.size());
		for (String value : values) {
			try {
				String split[] = value.split("\\.");
				Integer pid[] = new Integer[2];
				pid[0] = Integer.valueOf(split[0]);
				if (split.length > 1) {
					pid[1] = Integer.valueOf(split[1]);
				}
				pids.add(pid);
			} catch (NumberFormatException e) {
				//skip
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
	 * vCard versions: 4.0
	 * </p>
	 * @param localId the local ID
	 */
	public void addPid(int localId) {
		put("PID", Integer.toString(localId));
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
	 * vCard versions: 4.0
	 * </p>
	 * @param localId the local ID
	 * @param clientPidMapRef the ID used to reference the property's globally
	 * unique identifier in the CLIENTPIDMAP property.
	 */
	public void addPid(int localId, int clientPidMapRef) {
		put("PID", localId + "." + clientPidMapRef);
	}

	/**
	 * Removes all PID values.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 */
	public void removePids() {
		removeAll("PID");
	}

	/**
	 * Gets the MEDIATYPE parameter. This is used in properties that have a URL
	 * as a value, such as PHOTO and SOUND. It defines the content type of the
	 * referenced resource.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the media type (e.g. "image/jpeg") or null if it doesn't exist
	 */
	public String getMediaType() {
		return getFirst("MEDIATYPE");
	}

	/**
	 * Sets the MEDIATYPE parameter. This is used in properties that have a URL
	 * as a value, such as PHOTO and SOUND. It defines the content type of the
	 * referenced resource.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param mediaType the media type (e.g. "image/jpeg") or null to remove
	 */
	public void setMediaType(String mediaType) {
		replace("MEDIATYPE", mediaType);
	}

	/**
	 * Gets the LEVEL parameter. This is used to define the level of skill or
	 * level of interest the person has towards something.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the level (e.g. "beginner") or null if not found
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public String getLevel() {
		return getFirst(LevelParameter.NAME);
	}

	/**
	 * Sets the LEVEL parameter. This is used to define the level of skill or
	 * level of interest the person has towards something.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param level the level (e.g. "beginner") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void setLevel(String level) {
		replace(LevelParameter.NAME, level);
	}

	/**
	 * Gets the INDEX parameter. When present, it specifies the order in which a
	 * collection of multi-valued property instances should be sorted.
	 * Properties with low INDEX values are put at the beginning of the sorted
	 * list.
	 * 
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the INDEX value or null if it doesn't exist or null if it
	 * couldn't be parsed into a number
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public Integer getIndex() {
		String index = getFirst("INDEX");
		if (index == null) {
			return null;
		}

		try {
			return Integer.valueOf(index);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Sets the INDEX parameter. When present, it specifies the order in which a
	 * collection of multi-valued property instances should be sorted.
	 * Properties with low INDEX values are put at the beginning of the sorted
	 * list.
	 * 
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param index the INDEX value (must be greater than 0) or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 * @throws IllegalArgumentException if the value is not greater than 0
	 */
	public void setIndex(Integer index) {
		if (index != null && index <= 0) {
			throw new IllegalArgumentException("INDEX value must be greater than 0.");
		}
		String value = (index == null) ? null : index.toString();
		replace("INDEX", value);
	}
}
