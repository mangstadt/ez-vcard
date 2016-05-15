package ezvcard.parameter;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
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
import ezvcard.property.ClientPidMap;
import ezvcard.property.Email;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.SortString;
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
 * Stores the parameters (also known as "sub types") that belong to a property.
 * @author Michael Angstadt
 */
public class VCardParameters extends ListMultimap<String, String> {
	/**
	 * <p>
	 * Used to specify that the property value is an alternative representation
	 * of another property value.
	 * </p>
	 * <p>
	 * In the example below, the first three {@link Note} properties have the
	 * same ALTID. This means that they each contain the same value, but in
	 * different forms. In this case, each property value is written in a
	 * different language. The other {@link Note} properties in the example have
	 * different (or absent) ALTID values, which means they are not associated
	 * with the top three.
	 * </p>
	 * 
	 * <pre>
	 * NOTE;ALTID=1;LANGUAGE=en:Hello world!
	 * NOTE;ALTID=1;LANGUAGE=fr:Bonjour tout le monde!
	 * NOTE;ALTID=1;LANGUAGE=es:¡Hola, mundo!
	 * NOTE;ALTID=2;LANGUAGE=de:Meine Lieblingsfarbe ist blau.
	 * NOTE;ALTID=2;LANGUAGE=en:My favorite color is blue.
	 * NOTE:This vCard will self-destruct in 5 seconds.
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-18">RFC 6350
	 * p.18</a>
	 */
	public static final String ALTID = "ALTID";

	/**
	 * <p>
	 * Defines the type of calendar that is used in a date or date-time property
	 * value (for example, "gregorian").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-20">RFC 6350
	 * p.20</a>
	 */
	public static final String CALSCALE = "CALSCALE";

	/**
	 * <p>
	 * Defines the character set that the property value is encoded in (for
	 * example, "UTF-8"). Typically, this is only used in 2.1 vCards when the
	 * property value is encoded in quoted-printable encoding.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1}
	 * </p>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public static final String CHARSET = "CHARSET";

	/**
	 * <p>
	 * This parameter is used when the property value is encoded in a form other
	 * than plain text (for example, "base64").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public static final String ENCODING = "ENCODING";

	/**
	 * <p>
	 * Used to associate global positioning information with the property. It
	 * can be used with the {@link Address} property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 */
	public static final String GEO = "GEO";

	/**
	 * <p>
	 * Defines the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low INDEX values
	 * are put at the beginning of the sorted list. Properties with high INDEX
	 * values are put at the end of the list.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-7">RFC 6715
	 * p.7</a>
	 */
	public static final String INDEX = "INDEX";

	/**
	 * <p>
	 * Used by the {@link Address} property to define a mailing label for the
	 * address.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-33">RFC 6350
	 * p.33</a>
	 */
	public static final String LABEL = "LABEL";

	/**
	 * <p>
	 * Defines the language that the property value is written in (for example,
	 * "en" for English").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-16">RFC 6350
	 * p.16</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public static final String LANGUAGE = "LANGUAGE";

	/**
	 * <p>
	 * Used to define the skill or interest level the person has towards the
	 * topic defined by the property (for example, "beginner"). Its value varies
	 * depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-8">RFC 6715
	 * p.8</a>
	 */
	public static final String LEVEL = "LEVEL";

	/**
	 * <p>
	 * Used in properties that have a URL as a value, such as {@link Photo} and
	 * {@link Sound}. It defines the content type of the referenced resource
	 * (for example, "image/png" for a PNG image).
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-20">RFC 6350
	 * p.20</a>
	 */
	public static final String MEDIATYPE = "MEDIATYPE";

	/**
	 * <p>
	 * Defines a property ID. PIDs can exist on any property where multiple
	 * instances are allowed (such as {@link Email} or {@link Address}, but not
	 * {@link StructuredName} because only 1 instance of this property is
	 * allowed per vCard).
	 * </p>
	 * <p>
	 * When used in conjunction with the {@link ClientPidMap} property, it
	 * allows an individual property instance to be uniquely identifiable. This
	 * feature is made use of when two different versions of the same vCard have
	 * to be merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 */
	public static final String PID = "PID";

	/**
	 * <p>
	 * Defines the preference value. The lower this number is, the more
	 * "preferred" the property instance is compared with other properties of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * <p>
	 * In the vCard below, the {@link Address} on the second row is the most
	 * preferred because it has the lowest PREF value.
	 * </p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:;;1600 Amphitheatre Parkway;Mountain View;CA;94043
	 * ADR;TYPE=work;PREF=1:;;One Microsoft Way;Redmond;WA;98052
	 * ADR;TYPE=home:;;123 Maple St;Hometown;KS;12345
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-17">RFC 6350
	 * p.17</a>
	 */
	public static final String PREF = "PREF";

	/**
	 * <p>
	 * This parameter defines how the vCard should be sorted amongst other
	 * vCards. For example, this can be used if the person's last name (defined
	 * in the {@link StructuredName} property) starts with characters that
	 * should be ignored during sorting (such as "d'Aboville").
	 * </p>
	 * <p>
	 * This parameter can be used with the {@link StructuredName} and
	 * {@link Organization} properties. 2.1 and 3.0 vCards should use the
	 * {@link SortString} property instead.
	 * </p>
	 * <p>
	 * This parameter can be multi-valued. The first value is the primary sort
	 * keyword (such as the person's last name), the second value is the
	 * secondary sort keyword (such as the person's first name), etc.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-21">RFC 6350
	 * p.21</a>
	 */
	public static final String SORT_AS = "SORT-AS";

	/**
	 * <p>
	 * The meaning of this parameter varies depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1</a>
	 */
	public static final String TYPE = "TYPE";

	/**
	 * <p>
	 * Used to associate timezone information with an {@link Address} property
	 * (for example, "America/New_York" to indicate that an address adheres to
	 * that timezone).
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 */
	public static final String TZ = "TZ";

	/**
	 * <p>
	 * Defines the data type of the property value (for example, "date" if the
	 * property value is a date without a time component). It is used if the
	 * property accepts multiple values that have different data types.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-16">RFC 6350
	 * p.16</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
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
	 * Gets the ALTID parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to specify that the property value is an
	 * alternative representation of another property value.
	 * </p>
	 * <p>
	 * In the example below, the first three {@link Note} properties have the
	 * same ALTID. This means that they each contain the same value, but in
	 * different forms. In this case, each property value is written in a
	 * different language. The other {@link Note} properties in the example have
	 * different (or absent) ALTID values, which means they are not associated
	 * with the top three.
	 * </p>
	 * 
	 * <pre>
	 * NOTE;ALTID=1;LANGUAGE=en:Hello world!
	 * NOTE;ALTID=1;LANGUAGE=fr:Bonjour tout le monde!
	 * NOTE;ALTID=1;LANGUAGE=es:¡Hola, mundo!
	 * NOTE;ALTID=2;LANGUAGE=de:Meine Lieblingsfarbe ist blau.
	 * NOTE;ALTID=2;LANGUAGE=en:My favorite color is blue.
	 * NOTE:This vCard will self-destruct in 5 seconds.
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the ALTID or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-18">RFC 6350
	 * p.18</a>
	 */
	public String getAltId() {
		return first(ALTID);
	}

	/**
	 * <p>
	 * Sets the ALTID parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to specify that the property value is an
	 * alternative representation of another property value.
	 * </p>
	 * <p>
	 * In the example below, the first three {@link Note} properties have the
	 * same ALTID. This means that they each contain the same value, but in
	 * different forms. In this case, each property value is written in a
	 * different language. The other {@link Note} properties in the example have
	 * different (or absent) ALTID values, which means they are not associated
	 * with the top three.
	 * </p>
	 * 
	 * <pre>
	 * NOTE;ALTID=1;LANGUAGE=en:Hello world!
	 * NOTE;ALTID=1;LANGUAGE=fr:Bonjour tout le monde!
	 * NOTE;ALTID=1;LANGUAGE=es:¡Hola, mundo!
	 * NOTE;ALTID=2;LANGUAGE=de:Meine Lieblingsfarbe ist blau.
	 * NOTE;ALTID=2;LANGUAGE=en:My favorite color is blue.
	 * NOTE:This vCard will self-destruct in 5 seconds.
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altId the ALTID or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-18">RFC 6350
	 * p.18</a>
	 */
	public void setAltId(String altId) {
		replace(ALTID, altId);
	}

	/**
	 * <p>
	 * Gets the CALSCALE (calendar scale) parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the type of calendar that is used in a date or
	 * date-time property value (for example, "gregorian").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the type of calendar or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-20">RFC 6350
	 * p.20</a>
	 */
	public Calscale getCalscale() {
		String value = first(CALSCALE);
		return (value == null) ? null : Calscale.get(value);
	}

	/**
	 * <p>
	 * Sets the CALSCALE (calendar scale) parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the type of calendar that is used in a date or
	 * date-time property value (for example, "gregorian").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param calscale the type of calendar or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-20">RFC 6350
	 * p.20</a>
	 */
	public void setCalscale(Calscale calscale) {
		replace(CALSCALE, (calscale == null) ? null : calscale.getValue());
	}

	/**
	 * <p>
	 * Gets the CHARSET (character set) parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the character set that the property value is
	 * encoded in (for example, "UTF-8"). Typically, this is only used in 2.1
	 * vCards when the property value is encoded in quoted-printable encoding.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1}
	 * </p>
	 * @return the character set or null if not set
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public String getCharset() {
		return first(CHARSET);
	}

	/**
	 * <p>
	 * Sets the CHARSET (character set) parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the character set that the property value is
	 * encoded in (for example, "UTF-8"). Typically, this is only used in 2.1
	 * vCards when the property value is encoded in quoted-printable encoding.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1}
	 * </p>
	 * @param charset the character set or null to remove
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public void setCharset(String charset) {
		replace(CHARSET, charset);
	}

	/**
	 * <p>
	 * Gets the ENCODING parameter value.
	 * </p>
	 * <p>
	 * This parameter is used when the property value is encoded in a form other
	 * than plain text (for example, "base64").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the encoding or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public Encoding getEncoding() {
		String value = first(ENCODING);
		return (value == null) ? null : Encoding.get(value);
	}

	/**
	 * <p>
	 * Sets the ENCODING parameter value.
	 * </p>
	 * <p>
	 * This parameter is used when the property value is encoded in a form other
	 * than plain text (for example, "base64").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param encoding the encoding or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public void setEncoding(Encoding encoding) {
		replace(ENCODING, (encoding == null) ? null : encoding.getValue());
	}

	/**
	 * <p>
	 * Gets the GEO parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to associate global positioning information with
	 * the property. It can be used with the {@link Address} property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the geo URI or null if not set
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed into a geo URI. If this happens, you may use the
	 * {@link ListMultimap#get(Object) get()} method to retrieve its raw value.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
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
	 * Sets the GEO parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to associate global positioning information with
	 * the property. It can be used with the {@link Address} property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param uri the geo URI or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 */
	public void setGeo(GeoUri uri) {
		replace(GEO, (uri == null) ? null : uri.toString());
	}

	/**
	 * <p>
	 * Gets the INDEX parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the sorted position of this property when it is
	 * grouped together with other properties of the same type. Properties with
	 * low INDEX values are put at the beginning of the sorted list. Properties
	 * with high INDEX values are put at the end of the list.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the index or null if not set
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed. If this happens, you may use the
	 * {@link ListMultimap#get(Object) get()} method to retrieve its raw value.
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-7">RFC 6715
	 * p.7</a>
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
	 * Sets the INDEX parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the sorted position of this property when it is
	 * grouped together with other properties of the same type. Properties with
	 * low INDEX values are put at the beginning of the sorted list. Properties
	 * with high INDEX values are put at the end of the list.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param index the index or null to remove
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-7">RFC 6715
	 * p.7</a>
	 */
	public void setIndex(Integer index) {
		replace(INDEX, (index == null) ? null : index.toString());
	}

	/**
	 * <p>
	 * Gets the LABEL parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Address} property to define a
	 * mailing label for the address.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the label or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-33">RFC 6350
	 * p.33</a>
	 */
	public String getLabel() {
		return first(LABEL);
	}

	/**
	 * <p>
	 * Sets the LABEL parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Address} property to define a
	 * mailing label for the address.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param label the label or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-33">RFC 6350
	 * p.33</a>
	 */
	public void setLabel(String label) {
		replace(LABEL, label);
	}

	/**
	 * <p>
	 * Gets the LANGUAGE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the language that the property value is written in
	 * (for example, "en" for English").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the language or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-16">RFC 6350
	 * p.16</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public String getLanguage() {
		return first(LANGUAGE);
	}

	/**
	 * <p>
	 * Sets the LANGUAGE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the language that the property value is written in
	 * (for example, "en" for English").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param language the language or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-16">RFC 6350
	 * p.16</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public void setLanguage(String language) {
		replace(LANGUAGE, language);
	}

	/**
	 * <p>
	 * Gets the LEVEL parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to define the skill or interest level the person
	 * has towards the topic defined by the property (for example, "beginner").
	 * Its value varies depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the level or null if not set
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-8">RFC 6715
	 * p.8</a>
	 */
	public String getLevel() {
		return first(LEVEL);
	}

	/**
	 * <p>
	 * Sets the LEVEL parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to define the skill or interest level the person
	 * has towards the topic defined by the property (for example, "beginner").
	 * Its value varies depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param level the level or null to remove
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-8">RFC 6715
	 * p.8</a>
	 */
	public void setLevel(String level) {
		replace(LEVEL, level);
	}

	/**
	 * <p>
	 * Gets the MEDIATYPE parameter value.
	 * </p>
	 * <p>
	 * This parameter is used in properties that have a URL as a value, such as
	 * {@link Photo} and {@link Sound}. It defines the content type of the
	 * referenced resource (for example, "image/png" for a PNG image).
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the media type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-20">RFC 6350
	 * p.20</a>
	 */
	public String getMediaType() {
		return first(MEDIATYPE);
	}

	/**
	 * <p>
	 * Sets the MEDIATYPE parameter value.
	 * </p>
	 * <p>
	 * This parameter is used in properties that have a URL as a value, such as
	 * {@link Photo} and {@link Sound}. It defines the content type of the
	 * referenced resource (for example, "image/png" for a PNG image).
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param mediaType the media type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-20">RFC 6350
	 * p.20</a>
	 */
	public void setMediaType(String mediaType) {
		replace(MEDIATYPE, mediaType);
	}

	/**
	 * <p>
	 * Gets the PID (property ID) parameter values.
	 * </p>
	 * <p>
	 * PIDs can exist on any property where multiple instances are allowed (such
	 * as {@link Email} or {@link Address}, but not {@link StructuredName}
	 * because only 1 instance of this property is allowed per vCard).
	 * </p>
	 * <p>
	 * When used in conjunction with the {@link ClientPidMap} property, it
	 * allows an individual property instance to be uniquely identifiable. This
	 * feature is made use of when two different versions of the same vCard have
	 * to be merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link VCardParameters}
	 * object, and vice versa.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the PIDs
	 * @throws IllegalStateException if one or more parameter values are
	 * malformed and cannot be parsed. If this happens, you may use the
	 * {@link ListMultimap#get(Object) get()} method to retrieve its raw values.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 */
	public List<Pid> getPids() {
		return new VCardParameterList<Pid>(PID) {
			@Override
			protected String _asString(Pid value) {
				return value.toString();
			}

			@Override
			protected Pid _asObject(String value) {
				return Pid.valueOf(value);
			}

			@Override
			protected IllegalStateException _exception(String value, Exception thrown) {
				return new IllegalStateException(Messages.INSTANCE.getExceptionMessage(15, PID), thrown);
			}
		};
	}

	/**
	 * <p>
	 * Adds a PID (property ID) parameter value.
	 * </p>
	 * <p>
	 * PIDs can exist on any property where multiple instances are allowed (such
	 * as {@link Email} or {@link Address}, but not {@link StructuredName}
	 * because only 1 instance of this property is allowed per vCard).
	 * </p>
	 * <p>
	 * When used in conjunction with the {@link ClientPidMap} property, it
	 * allows an individual property instance to be uniquely identifiable. This
	 * feature is made use of when two different versions of the same vCard have
	 * to be merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param pid the PID to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 */
	public void addPid(Pid pid) {
		put(PID, pid.toString());
	}

	/**
	 * <p>
	 * Removes a PID (property ID) parameter value.
	 * </p>
	 * <p>
	 * PIDs can exist on any property where multiple instances are allowed (such
	 * as {@link Email} or {@link Address}, but not {@link StructuredName}
	 * because only 1 instance of this property is allowed per vCard).
	 * </p>
	 * <p>
	 * When used in conjunction with the {@link ClientPidMap} property, it
	 * allows an individual property instance to be uniquely identifiable. This
	 * feature is made use of when two different versions of the same vCard have
	 * to be merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param pid the PID to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 */
	public void removePid(Pid pid) {
		String value = pid.toString();
		remove(PID, value);
	}

	/**
	 * <p>
	 * Removes all PID (property ID) parameter values.
	 * </p>
	 * <p>
	 * PIDs can exist on any property where multiple instances are allowed (such
	 * as {@link Email} or {@link Address}, but not {@link StructuredName}
	 * because only 1 instance of this property is allowed per vCard).
	 * </p>
	 * <p>
	 * When used in conjunction with the {@link ClientPidMap} property, it
	 * allows an individual property instance to be uniquely identifiable. This
	 * feature is made use of when two different versions of the same vCard have
	 * to be merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 */
	public void removePids() {
		removeAll(PID);
	}

	/**
	 * <p>
	 * Gets the PREF (preference) parameter value.
	 * </p>
	 * <p>
	 * The lower this number is, the more "preferred" the property instance is
	 * compared with other properties of the same type. If a property doesn't
	 * have a preference value, then it is considered the least preferred.
	 * </p>
	 * <p>
	 * In the vCard below, the {@link Address} on the second row is the most
	 * preferred because it has the lowest PREF value.
	 * </p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:;;1600 Amphitheatre Parkway;Mountain View;CA;94043
	 * ADR;TYPE=work;PREF=1:;;One Microsoft Way;Redmond;WA;98052
	 * ADR;TYPE=home:;;123 Maple St;Hometown;KS;12345
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the preference value or null if not set
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed. If this happens, you may use the
	 * {@link ListMultimap#get(Object) get()} method to retrieve its raw value.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-17">RFC 6350
	 * p.17</a>
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
	 * Sets the PREF (preference) parameter value.
	 * </p>
	 * <p>
	 * The lower this number is, the more "preferred" the property instance is
	 * compared with other properties of the same type. If a property doesn't
	 * have a preference value, then it is considered the least preferred.
	 * </p>
	 * <p>
	 * In the vCard below, the {@link Address} on the second row is the most
	 * preferred because it has the lowest PREF value.
	 * </p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:;;1600 Amphitheatre Parkway;Mountain View;CA;94043
	 * ADR;TYPE=work;PREF=1:;;One Microsoft Way;Redmond;WA;98052
	 * ADR;TYPE=home:;;123 Maple St;Hometown;KS;12345
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-17">RFC 6350
	 * p.17</a>
	 */
	public void setPref(Integer pref) {
		replace(PREF, (pref == null) ? null : pref.toString());
	}

	/**
	 * <p>
	 * Gets the SORT-AS parameter values.
	 * </p>
	 * <p>
	 * This parameter defines how the vCard should be sorted amongst other
	 * vCards. For example, this can be used if the person's last name (defined
	 * in the {@link StructuredName} property) starts with characters that
	 * should be ignored during sorting (such as "d'Aboville").
	 * </p>
	 * <p>
	 * This parameter can be used with the {@link StructuredName} and
	 * {@link Organization} properties. 2.1 and 3.0 vCards should use the
	 * {@link SortString} property instead.
	 * </p>
	 * <p>
	 * This parameter can be multi-valued. The first value is the primary sort
	 * keyword (such as the person's last name), the second value is the
	 * secondary sort keyword (such as the person's first name), etc.
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link VCardParameters}
	 * object, and vice versa.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the sort strings
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-21">RFC 6350
	 * p.21</a>
	 */
	public List<String> getSortAs() {
		return get(SORT_AS);
	}

	/**
	 * <p>
	 * Sets the SORT-AS parameter values.
	 * </p>
	 * <p>
	 * This parameter defines how the vCard should be sorted amongst other
	 * vCards. For example, this can be used if the person's last name (defined
	 * in the {@link StructuredName} property) starts with characters that
	 * should be ignored during sorting (such as "d'Aboville").
	 * </p>
	 * <p>
	 * This parameter can be used with the {@link StructuredName} and
	 * {@link Organization} properties. 2.1 and 3.0 vCards should use the
	 * {@link SortString} property instead.
	 * </p>
	 * <p>
	 * This parameter can be multi-valued. The first value is the primary sort
	 * keyword (such as the person's last name), the second value is the
	 * secondary sort keyword (such as the person's first name), etc.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param sortStrings the sort strings or an empty parameter list to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-21">RFC 6350
	 * p.21</a>
	 */
	public void setSortAs(String... sortStrings) {
		removeAll(SORT_AS);
		putAll(SORT_AS, Arrays.asList(sortStrings));
	}

	/**
	 * <p>
	 * Gets the TYPE parameter values.
	 * </p>
	 * <p>
	 * The meaning of this parameter varies depending on the property.
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link VCardParameters}
	 * object, and vice versa.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the types
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1</a>
	 */
	public List<String> getTypes() {
		return get(TYPE);
	}

	/**
	 * <p>
	 * Gets the first TYPE parameter value.
	 * </p>
	 * <p>
	 * The meaning of this parameter varies depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1</a>
	 */
	public String getType() {
		return first(TYPE);
	}

	/**
	 * <p>
	 * Adds a TYPE parameter value.
	 * </p>
	 * <p>
	 * The meaning of this parameter varies depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param type the type to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1</a>
	 */
	public void addType(String type) {
		put(TYPE, type);
	}

	/**
	 * <p>
	 * Removes a TYPE parameter value.
	 * </p>
	 * <p>
	 * The meaning of this parameter varies depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param type the type to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1</a>
	 */
	public void removeType(String type) {
		remove(TYPE, type);
	}

	/**
	 * <p>
	 * Sets the TYPE parameter value.
	 * </p>
	 * <p>
	 * The meaning of this parameter varies depending on the property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param type the type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1</a>
	 */
	public void setType(String type) {
		replace(TYPE, type);
	}

	/**
	 * <p>
	 * Gets the TZ (timezone) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to associate timezone information with an
	 * {@link Address} property (for example, "America/New_York" to indicate
	 * that an address adheres to that timezone).
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the timezone or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 */
	public String getTimezone() {
		return first(TZ);
	}

	/**
	 * <p>
	 * Sets the TZ (timezone) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used to associate timezone information with an
	 * {@link Address} property (for example, "America/New_York" to indicate
	 * that an address adheres to that timezone).
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param timezone the timezone or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 */
	public void setTimezone(String timezone) {
		replace(TZ, timezone);
	}

	/**
	 * <p>
	 * Gets the VALUE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the data type of the property value (for example,
	 * "date" if the property value is a date without a time component). It is
	 * used if the property accepts multiple values that have different data
	 * types.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the data type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-16">RFC 6350
	 * p.16</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public VCardDataType getValue() {
		String value = first(VALUE);
		return (value == null) ? null : VCardDataType.get(value);
	}

	/**
	 * <p>
	 * Sets the VALUE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the data type of the property value (for example,
	 * "date" if the property value is a date without a time component). It is
	 * used if the property accepts multiple values that have different data
	 * types.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param dataType the data type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-16">RFC 6350
	 * p.16</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-6">RFC 2426 p.6</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public void setValue(VCardDataType dataType) {
		replace(VALUE, (dataType == null) ? null : dataType.getName());
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

	/**
	 * <p>
	 * A list that converts the raw string values of a TYPE parameter to the
	 * appropriate {@link VCardParameter} object that some parameters use.
	 * </p>
	 * <p>
	 * This list is backed by the {@link VCardParameters} object. Any changes
	 * made to the list will affect the {@link VCardParameters} object and vice
	 * versa.
	 * </p>
	 * @param <T> the parameter class
	 */
	public abstract class TypeParameterList<T extends VCardParameter> extends EnumParameterList<T> {
		public TypeParameterList() {
			super(TYPE);
		}
	}

	/**
	 * <p>
	 * A list that converts the raw string values of a parameter to the
	 * appropriate {@link VCardParameter} object that some parameters use.
	 * </p>
	 * <p>
	 * This list is backed by the {@link VCardParameters} object. Any changes
	 * made to the list will affect the {@link VCardParameters} object and vice
	 * versa.
	 * </p>
	 * @param <T> the parameter class
	 */
	public abstract class EnumParameterList<T extends VCardParameter> extends VCardParameterList<T> {
		public EnumParameterList(String parameterName) {
			super(parameterName);
		}

		@Override
		protected String _asString(T value) {
			return value.getValue();
		}
	}

	/**
	 * <p>
	 * A list that converts the raw string values of a parameter to another kind
	 * of value (for example, Integers).
	 * </p>
	 * <p>
	 * This list is backed by the {@link VCardParameters} object. Any changes
	 * made to the list will affect the {@link VCardParameters} object and vice
	 * versa.
	 * </p>
	 * <p>
	 * If a String value cannot be converted to the appropriate data type, an
	 * {@link IllegalStateException} is thrown.
	 * </p>
	 */
	public abstract class VCardParameterList<T> extends AbstractList<T> {
		protected final String parameterName;
		protected final List<String> parameterValues;

		/**
		 * @param parameterName the name of the parameter (case insensitive)
		 */
		public VCardParameterList(String parameterName) {
			this.parameterName = parameterName;
			parameterValues = VCardParameters.this.get(parameterName);
		}

		@Override
		public void add(int index, T value) {
			String valueStr = _asString(value);
			parameterValues.add(index, valueStr);
		}

		@Override
		public T remove(int index) {
			String removed = parameterValues.remove(index);
			return asObject(removed);
		}

		@Override
		public T get(int index) {
			String value = parameterValues.get(index);
			return asObject(value);
		}

		@Override
		public T set(int index, T value) {
			String valueStr = _asString(value);
			String replaced = parameterValues.set(index, valueStr);
			return asObject(replaced);
		}

		@Override
		public int size() {
			return parameterValues.size();
		}

		private T asObject(String value) {
			try {
				return _asObject(value);
			} catch (Exception e) {
				throw _exception(value, e);
			}
		}

		/**
		 * Converts the object to a String value for storing in the
		 * {@link VCardParameters} object.
		 * @param value the value
		 * @return the string value
		 */
		protected abstract String _asString(T value);

		/**
		 * Converts a String value to its object form.
		 * @param value the string value
		 * @return the object
		 * @throws Exception if there is a problem parsing the string
		 */
		protected abstract T _asObject(String value) throws Exception;

		/**
		 * Creates the exception that is thrown when the raw string value cannot
		 * be parsed into its object form.
		 * @param value the raw string value
		 * @param thrown the thrown exception
		 * @return the exception to throw
		 */
		protected IllegalStateException _exception(String value, Exception thrown) {
			return new IllegalStateException(Messages.INSTANCE.getExceptionMessage(26, parameterName), thrown);
		}
	}
}
