package ezvcard.parameter;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.parameter.VCardParameters.ALTID;
import static ezvcard.parameter.VCardParameters.CALSCALE;
import static ezvcard.parameter.VCardParameters.ENCODING;
import static ezvcard.parameter.VCardParameters.GEO;
import static ezvcard.parameter.VCardParameters.INDEX;
import static ezvcard.parameter.VCardParameters.LABEL;
import static ezvcard.parameter.VCardParameters.LANGUAGE;
import static ezvcard.parameter.VCardParameters.LEVEL;
import static ezvcard.parameter.VCardParameters.MEDIATYPE;
import static ezvcard.parameter.VCardParameters.PID;
import static ezvcard.parameter.VCardParameters.PREF;
import static ezvcard.parameter.VCardParameters.SORT_AS;
import static ezvcard.parameter.VCardParameters.TYPE;
import static ezvcard.parameter.VCardParameters.TZ;
import static ezvcard.parameter.VCardParameters.VALUE;
import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertNotEqualsBothWays;
import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.util.GeoUri;

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
 * @author Michael Angstadt
 */
public class VCardParametersTest {
	private VCardParameters parameters;

	@Before
	public void before() {
		parameters = new VCardParameters();
	}

	@Test
	public void copy() {
		parameters.put("NAME", "value");
		VCardParameters copy = new VCardParameters(parameters);
		assertEquals(copy, parameters);
	}

	@Test
	public void validate_non_standard_values() {
		parameters.setCalscale(Calscale.get("foo"));
		parameters.setEncoding(Encoding.get("foo"));
		parameters.setValue(VCardDataType.get("foo"));

		assertValidate(parameters.validate(V2_1), 3, 6, 3, 3);
		assertValidate(parameters.validate(V3_0), 3, 3, 3, 6);
		assertValidate(parameters.validate(V4_0), 3, 3, 3);
	}

	@Test
	public void validate_malformed_values() {
		parameters.put(GEO, "invalid");
		parameters.put(INDEX, "invalid");
		parameters.put(PREF, "invalid");
		parameters.put(PID, "invalid");

		assertValidate(parameters.validate(V2_1), 5, 5, 27, 5, 6, 6, 6);
		assertValidate(parameters.validate(V3_0), 5, 5, 27, 5, 6, 6, 6);
		assertValidate(parameters.validate(V4_0), 5, 5, 27, 5);
	}

	@Test
	public void validate_pid() {
		parameters.replace(PID, "invalid"); //ASCII value is after "9"
		assertValidate(parameters.validate(V2_1), 27, 6);
		assertValidate(parameters.validate(V3_0), 27, 6);
		assertValidate(parameters.validate(V4_0), 27);

		parameters.replace(PID, "*"); //ASCII value is before "0"
		assertValidate(parameters.validate(V2_1), 27, 6);
		assertValidate(parameters.validate(V3_0), 27, 6);
		assertValidate(parameters.validate(V4_0), 27);

		parameters.replace(PID, ".1");
		assertValidate(parameters.validate(V2_1), 25, 27, 6);
		assertValidate(parameters.validate(V3_0), 27, 6);
		assertValidate(parameters.validate(V4_0), 27);

		parameters.replace(PID, "1.");
		assertValidate(parameters.validate(V2_1), 25, 27, 6);
		assertValidate(parameters.validate(V3_0), 27, 6);
		assertValidate(parameters.validate(V4_0), 27);

		parameters.replace(PID, "1");
		assertValidate(parameters.validate(V2_1), 6);
		assertValidate(parameters.validate(V3_0), 6);
		assertValidate(parameters.validate(V4_0));

		parameters.replace(PID, "1.1");
		assertValidate(parameters.validate(V2_1), 25, 6);
		assertValidate(parameters.validate(V3_0), 6);
		assertValidate(parameters.validate(V4_0));

		parameters.replace(PID, "1.1.1");
		assertValidate(parameters.validate(V2_1), 25, 27, 6);
		assertValidate(parameters.validate(V3_0), 27, 6);
		assertValidate(parameters.validate(V4_0), 27);
	}

	@Test
	public void validate_index() {
		parameters.setIndex(0);
		assertValidate(parameters.validate(V2_1), 28, 6);
		assertValidate(parameters.validate(V3_0), 28, 6);
		assertValidate(parameters.validate(V4_0), 28);

		parameters.setIndex(1);
		assertValidate(parameters.validate(V2_1), 6);
		assertValidate(parameters.validate(V3_0), 6);
		assertValidate(parameters.validate(V4_0));
	}

	@Test
	public void validate_pref() {
		parameters.setPref(0);
		assertValidate(parameters.validate(V2_1), 29);
		assertValidate(parameters.validate(V3_0), 29);
		assertValidate(parameters.validate(V4_0), 29);

		parameters.setPref(101);
		assertValidate(parameters.validate(V2_1), 29);
		assertValidate(parameters.validate(V3_0), 29);
		assertValidate(parameters.validate(V4_0), 29);

		parameters.setPref(50);
		assertValidate(parameters.validate(V2_1));
		assertValidate(parameters.validate(V3_0));
		assertValidate(parameters.validate(V4_0));
	}

	@Test
	public void validate_supported_versions() {
		parameters.setAltId("value");
		parameters.setCalscale(Calscale.GREGORIAN);
		parameters.setCharset("UTF-8");
		parameters.setGeo(new GeoUri.Builder(1.0, 1.0).build());
		parameters.setIndex(1);
		parameters.setLanguage("value");
		parameters.setLevel("value");
		parameters.setMediaType("value");
		parameters.setSortAs("value");
		parameters.setTimezone("value");

		assertValidate(parameters.validate(V2_1), 25, 6, 6, 6, 6, 6, 6, 6, 6);
		assertValidate(parameters.validate(V3_0), 6, 6, 6, 6, 6, 6, 6, 6, 6);
		assertValidate(parameters.validate(V4_0), 6);
	}

	@Test
	public void validate_value_supported_versions() {
		parameters.setEncoding(Encoding._7BIT);
		parameters.setValue(VCardDataType.CONTENT_ID);
		assertValidate(parameters.validate(V2_1));
		assertValidate(parameters.validate(V3_0), 4, 4);
		assertValidate(parameters.validate(V4_0), 4, 4);

		parameters.setEncoding(Encoding.B);
		parameters.setValue(VCardDataType.BINARY);
		assertValidate(parameters.validate(V2_1), 4, 4);
		assertValidate(parameters.validate(V3_0));
		assertValidate(parameters.validate(V4_0), 4, 4);

		parameters.setEncoding(null);
		parameters.setValue(VCardDataType.DATE_AND_OR_TIME);
		assertValidate(parameters.validate(V2_1), 4);
		assertValidate(parameters.validate(V3_0), 4);
		assertValidate(parameters.validate(V4_0));
	}

	@Test
	public void validate_charset() {
		parameters.setCharset("invalid");
		assertValidate(parameters.validate(V2_1), 22);
		assertValidate(parameters.validate(V3_0), 6, 22);
		assertValidate(parameters.validate(V4_0), 6, 22);

		parameters.setCharset("UTF-8");
		assertValidate(parameters.validate(V2_1));
		assertValidate(parameters.validate(V3_0), 6);
		assertValidate(parameters.validate(V4_0), 6);
	}

	@Test
	public void validate_parameter_name() {
		parameters.replace("YES/NO", "value");
		for (VCardVersion version : VCardVersion.values()) {
			assertValidate(parameters.validate(version), 26);
		}
	}

	@Test
	public void validate_parameter_value_characters() {
		for (char c : ",.:=[]".toCharArray()) {
			parameters.replace("NAME", "value" + c);
			assertValidate(parameters.validate(V2_1), 25);
		}

		char c = (char) 7;
		parameters.replace("NAME", "value" + c);
		for (VCardVersion version : VCardVersion.values()) {
			assertValidate(parameters.validate(version), 25);
		}
	}

	/**
	 * Checks that the LABEL parameter is *not* checked for correctness if the
	 * vCard version is 2.1 or 3.0, because the writer converts the parameter to
	 * a LABEL property for these versions.
	 */
	@Test
	public void validate_label_parameter_value_characters() {
		parameters.replace(LABEL, "value.");
		assertValidate(parameters.validate(V2_1));

		char c = (char) 7;
		parameters.replace(LABEL, "value" + c);
		assertValidate(parameters.validate(V3_0));
		assertValidate(parameters.validate(V4_0), 25);
	}

	/**
	 * Tests to make sure {@link VCardParameters#sanitizeKey(String)
	 * sanitizeKey()} is implemented correctly.
	 */
	@Test
	public void case_insensitive() {
		parameters.put("NUMBERS", "1");
		assertEquals(Arrays.asList("1"), parameters.get("numbers"));
		parameters.put("numbers", "2");
		assertEquals(Arrays.asList("1", "2"), parameters.get("NUMBERS"));
		parameters.put(null, "3");
		assertEquals(Arrays.asList("3"), parameters.get(null));
	}

	@Test
	public void encoding() {
		assertNull(parameters.getEncoding());

		parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
		assertEquals(Encoding.QUOTED_PRINTABLE.getValue(), parameters.first(ENCODING));
		assertEquals(Encoding.QUOTED_PRINTABLE, parameters.getEncoding());

		parameters.setEncoding(null);
		assertNull(parameters.getEncoding());
	}

	@Test
	public void value() {
		assertNull(parameters.getValue());

		parameters.setValue(VCardDataType.TEXT);
		assertEquals(VCardDataType.TEXT.getName(), parameters.first(VALUE));
		assertEquals(VCardDataType.TEXT, parameters.getValue());

		parameters.setValue(null);
		assertNull(parameters.getValue());
	}

	@Test
	public void pref() {
		assertNull(parameters.getPref());

		parameters.setPref(1);
		assertEquals("1", parameters.first(PREF));
		assertIntEquals(1, parameters.getPref());

		parameters.setPref(null);
		assertNull(parameters.getPref());
	}

	@Test(expected = IllegalStateException.class)
	public void pref_malformed() {
		parameters.put(PREF, "invalid");
		parameters.getPref();
	}

	@Test
	public void geo() {
		assertNull(parameters.getGeo());

		parameters.setGeo(new GeoUri.Builder(-10.98887888, 20.12344111).build());
		assertEquals("geo:-10.988879,20.123441", parameters.first(GEO)); //it rounds to 6 decimal places
		assertEquals(new GeoUri.Builder(-10.988879, 20.123441).build(), parameters.getGeo());

		parameters.setGeo(null);
		assertNull(parameters.first(GEO));
	}

	@Test(expected = IllegalStateException.class)
	public void geo_malformed() {
		parameters.put(GEO, "invalid");
		parameters.getGeo();
	}

	@Test
	public void index() {
		assertNull(parameters.getIndex());

		parameters.setIndex(1);
		assertEquals("1", parameters.first(INDEX));
		assertIntEquals(1, parameters.getIndex());

		parameters.setIndex(null);
		assertNull(parameters.getIndex());
	}

	@Test(expected = IllegalStateException.class)
	public void index_malformed() {
		parameters.put(INDEX, "invalid");
		parameters.getIndex();
	}

	@Test
	public void calscale() {
		assertNull(parameters.getCalscale());

		parameters.setCalscale(Calscale.GREGORIAN);
		assertEquals(Calscale.GREGORIAN.getValue(), parameters.first(CALSCALE));
		assertEquals(Calscale.GREGORIAN, parameters.getCalscale());

		parameters.setCalscale(null);
		assertNull(parameters.getCalscale());
	}

	@Test
	public void language() {
		assertNull(parameters.getLanguage());

		parameters.setLanguage("en");
		assertEquals("en", parameters.first(LANGUAGE));
		assertEquals("en", parameters.getLanguage());

		parameters.setLanguage(null);
		assertNull(parameters.getLanguage());
	}

	@Test
	public void label() {
		assertNull(parameters.getLabel());

		parameters.setLabel("value");
		assertEquals("value", parameters.first(LABEL));
		assertEquals("value", parameters.getLabel());

		parameters.setLabel(null);
		assertNull(parameters.getLabel());
	}

	@Test
	public void timezone() {
		assertNull(parameters.getTimezone());

		parameters.setTimezone("value");
		assertEquals("value", parameters.first(TZ));
		assertEquals("value", parameters.getTimezone());

		parameters.setTimezone(null);
		assertNull(parameters.getTimezone());
	}

	@Test
	public void type() {
		assertNull(parameters.getType());

		parameters.setType("value");
		assertEquals("value", parameters.first(TYPE));
		assertEquals("value", parameters.getType());

		parameters.setType(null);
		assertNull(parameters.getType());
	}

	@Test
	public void altId() {
		assertNull(parameters.getAltId());

		parameters.setAltId("value");
		assertEquals("value", parameters.first(ALTID));
		assertEquals("value", parameters.getAltId());

		parameters.setAltId(null);
		assertNull(parameters.getAltId());
	}

	@Test
	public void mediaType() {
		assertNull(parameters.getMediaType());

		parameters.setMediaType("value");
		assertEquals("value", parameters.first(MEDIATYPE));
		assertEquals("value", parameters.getMediaType());

		parameters.setMediaType(null);
		assertNull(parameters.getMediaType());
	}

	@Test
	public void level() {
		assertNull(parameters.getLevel());

		parameters.setLevel("value");
		assertEquals("value", parameters.first(LEVEL));
		assertEquals("value", parameters.getLevel());

		parameters.setLevel(null);
		assertNull(parameters.getLevel());
	}

	@Test
	public void sortAs() {
		assertTrue(parameters.getSortAs().isEmpty());

		parameters.setSortAs("one", "two");
		assertEquals(Arrays.asList("one", "two"), parameters.getSortAs());
		assertEquals(Arrays.asList("one", "two"), parameters.get(SORT_AS));

		parameters.clear();
		parameters.put(SORT_AS, "one");
		parameters.put(SORT_AS, "two");
		assertEquals(Arrays.asList("one", "two"), parameters.getSortAs());

		parameters.clear();

		parameters.setSortAs("one", "three");
		assertEquals(Arrays.asList("one", "three"), parameters.getSortAs());

		parameters.setSortAs();
		assertTrue(parameters.getSortAs().isEmpty());
	}

	@Test
	public void equals_essentials() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "bar");
		assertEqualsMethodEssentials(one);
	}

	@Test
	public void equals_different_size() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "one");

		VCardParameters two = new VCardParameters();
		two.put("foo", "one");
		two.put("foo", "two");

		assertNotEqualsBothWays(one, two);
	}

	@Test
	public void equals_different_value_size() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "one");
		one.put("bar", "three");

		VCardParameters two = new VCardParameters();
		two.put("foo", "one");
		two.put("foo", "two");

		assertNotEqualsBothWays(one, two);
	}

	@Test
	public void equals_case_insensitive() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "bar");

		VCardParameters two = new VCardParameters();
		two.put("FOO", "BAR");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_order_does_not_matter() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "one");
		one.put("foo", "two");
		one.put("foo", "three");

		VCardParameters two = new VCardParameters();
		two.put("foo", "TWO");
		two.put("foo", "one");
		two.put("foo", "three");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_duplicate_values() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "one");
		one.put("foo", "one");
		one.put("foo", "two");

		VCardParameters two = new VCardParameters();
		two.put("foo", "one");
		two.put("foo", "one");
		two.put("foo", "two");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_different_duplicates_same_value_size() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "one");
		one.put("foo", "one");
		one.put("foo", "two");

		VCardParameters two = new VCardParameters();
		two.put("foo", "one");
		two.put("foo", "two");
		two.put("foo", "two");

		assertNotEqualsBothWays(one, two);
	}

	@Test
	public void equals_multiple_keys() {
		VCardParameters one = new VCardParameters();
		one.put("foo", "BAR");
		one.put("super", "man");
		one.put("super", "bad");
		one.put("hello", "world");

		VCardParameters two = new VCardParameters();
		two.put("hello", "world");
		two.put("super", "MAN");
		two.put("foo", "bar");
		two.put("super", "bad");

		assertEqualsAndHash(one, two);
	}
}
