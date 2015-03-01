package ezvcard.io.json;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.assertWarnings;
import static ezvcard.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.MyFormattedNameType;
import ezvcard.io.MyFormattedNameType.MyFormattedNameScribe;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Language;
import ezvcard.property.Organization;
import ezvcard.property.RawProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;

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
 * @author Michael Angstadt
 */
public class JCardReaderTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void read_single() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		String json =
		"[" +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"Jane Doe\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader);

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void no_version() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(1, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void invalid_version() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"3.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //should still set the version to 4.0
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(1, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader); //missing VERSION property

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties_multiple() throws Throwable {
		//@formatter:off
		String json =
		  "[" +
		    "[\"vcard\"," +
		      "[" +
		      "]" +
		    "]," +
		    "[\"vcard\"," +
		      "[" +
		      "]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader); //missing VERSION property

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader); //missing VERSION property

		assertNull(reader.readNext());
	}

	@Test
	public void extendedType() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-type\", {}, \"text\", \"value\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		RawProperty prop = vcard.getExtendedProperty("x-type");
		assertEquals("value", prop.getValue());
		assertWarnings(0, reader);
	}

	@Test
	public void registerExtendedType() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-type\", {}, \"text\", \"value\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerScribe(new TypeForTestingScribe());

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		TypeForTesting prop = vcard.getProperty(TypeForTesting.class);
		assertEquals("value", prop.value.asSingle());
		assertWarnings(0, reader);
	}

	@Test
	public void readExtendedType_override_standard_type_classes() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerScribe(new MyFormattedNameScribe());
		VCard vcard = reader.readNext();
		assertEquals(1, vcard.getProperties().size());
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		MyFormattedNameType prop = vcard.getProperty(MyFormattedNameType.class);
		assertEquals("JOHN DOE", prop.value);
		assertWarnings(0, reader);
	}

	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"skipme\", {}, \"text\", \"value\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerScribe(new SkipMeScribe());

		VCard vcard = reader.readNext();
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader);
	}

	@Test
	public void cannotParseException() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"cannotparse\", {}, \"text\", \"value\"]," +
		      "[\"x-foo\", {}, \"text\", \"value\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerScribe(new CannotParseScribe());
		VCard vcard = reader.readNext();

		assertEquals(2, vcard.getProperties().size());
		RawProperty property = vcard.getExtendedProperty("x-foo");
		assertEquals("x-foo", property.getPropertyName());
		assertEquals("value", property.getValue());
		property = vcard.getExtendedProperty("cannotparse");
		assertEquals("cannotparse", property.getPropertyName());
		assertEquals("value", property.getValue());

		assertWarnings(1, reader);
	}

	@Test
	public void utf8() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"version\", {}, \"text\", \"4.0\"]," +
				"[\"note\", {}, \"text\", \"\u019dote\"]" +
			"]" +
		"]";
		//@formatter:on
		File file = tempFolder.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write(json);
		writer.close();

		JCardReader reader = new JCardReader(file);
		VCard vcard = reader.readNext();
		assertEquals("\u019dote", vcard.getNotes().get(0).getValue());

		assertWarnings(0, reader);
		assertNull(reader.readNext());
	}

	private static class TypeForTesting extends VCardProperty {
		public JCardValue value;

		public TypeForTesting(JCardValue value) {
			this.value = value;
		}
	}

	private static class TypeForTestingScribe extends VCardPropertyScribe<TypeForTesting> {
		public TypeForTestingScribe() {
			super(TypeForTesting.class, "X-TYPE");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return VCardDataType.TEXT;
		}

		@Override
		protected String _writeText(TypeForTesting property, VCardVersion version) {
			return "";
		}

		@Override
		protected TypeForTesting _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			return new TypeForTesting(null);
		}

		@Override
		protected TypeForTesting _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
			return new TypeForTesting(value);
		}

	}

	@Test
	public void jcard_example() throws Throwable {
		JCardReader reader = new JCardReader(getClass().getResourceAsStream("jcard-example.json"));

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(16, vcard.getProperties().size());

		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());

		StructuredName n = vcard.getStructuredName();
		assertEquals("Perreault", n.getFamily());
		assertEquals("Simon", n.getGiven());
		assertEquals(Arrays.asList(), n.getAdditional());
		assertEquals(Arrays.asList(), n.getPrefixes());
		assertEquals(Arrays.asList("ing. jr", "M.Sc."), n.getSuffixes());

		PartialDate expectedBday = PartialDate.builder().month(2).date(3).build();
		PartialDate actualBday = vcard.getBirthday().getPartialDate();
		assertEquals(expectedBday, actualBday);

		Date expectedAnniversary = utc("2009-08-08 19:30:00");
		Date actualAnniversary = vcard.getAnniversary().getDate();
		assertEquals(expectedAnniversary, actualAnniversary);

		assertTrue(vcard.getGender().isMale());

		Language lang = vcard.getLanguages().get(0);
		assertEquals("fr", lang.getValue());
		assertIntEquals(1, lang.getPref());

		lang = vcard.getLanguages().get(1);
		assertEquals("en", lang.getValue());
		assertIntEquals(2, lang.getPref());

		Organization org = vcard.getOrganization();
		assertEquals(Arrays.asList("Viagenie"), org.getValues());
		assertEquals("work", org.getType());

		Address adr = vcard.getAddresses().get(0);
		assertNull(adr.getPoBox());
		assertEquals("Suite D2-630", adr.getExtendedAddress());
		assertEquals("2875 Laurier", adr.getStreetAddress());
		assertEquals("Quebec", adr.getLocality());
		assertEquals("QC", adr.getRegion());
		assertEquals("G1V 2M2", adr.getPostalCode());
		assertEquals("Canada", adr.getCountry());
		assertSetEquals(adr.getTypes(), AddressType.WORK);

		Telephone tel = vcard.getTelephoneNumbers().get(0);
		TelUri expectedUri = new TelUri.Builder("+1-418-656-9254").extension("102").build();
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);
		assertIntEquals(1, tel.getPref());

		tel = vcard.getTelephoneNumbers().get(1);
		expectedUri = new TelUri.Builder("+1-418-262-6501").build();
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE, TelephoneType.CELL, TelephoneType.VIDEO, TelephoneType.TEXT);

		Email email = vcard.getEmails().get(0);
		assertEquals("simon.perreault@viagenie.ca", email.getValue());
		assertSetEquals(email.getTypes(), EmailType.WORK);

		Geo geo = vcard.getGeo();
		assertEquals(Double.valueOf(46.772673), geo.getLatitude());
		assertEquals(Double.valueOf(-71.282945), geo.getLongitude());
		assertEquals("work", geo.getType());

		Key key = vcard.getKeys().get(0);
		assertEquals("http://www.viagenie.ca/simon.perreault/simon.asc", key.getUrl());
		assertEquals("work", key.getType());

		Timezone tz = vcard.getTimezone();
		assertIntEquals(-5, tz.getHourOffset());
		assertIntEquals(0, tz.getMinuteOffset());

		Url url = vcard.getUrls().get(0);
		assertEquals("http://nomis80.org", url.getValue());
		assertEquals("home", url.getType());

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader);
		assertNull(reader.readNext());
	}
}
