package ezvcard.io;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.EmailType;
import ezvcard.types.GeoType;
import ezvcard.types.KeyType;
import ezvcard.types.LanguageType;
import ezvcard.types.OrganizationType;
import ezvcard.types.RawType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TimezoneType;
import ezvcard.types.UrlType;
import ezvcard.types.VCardType;
import ezvcard.util.JCardValue;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class JCardReaderTest {
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
		assertEquals(1, vcard.getAllTypes().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

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
		assertEquals(1, vcard.getAllTypes().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getAllTypes().size());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

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
		assertEquals(1, vcard.getAllTypes().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(1, reader.getWarnings());

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
		assertEquals(1, vcard.getAllTypes().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(1, reader.getWarnings());

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
		assertEquals(0, vcard.getAllTypes().size());
		assertWarnings(1, reader.getWarnings()); //missing VERSION property

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
		assertEquals(0, vcard.getAllTypes().size());
		assertWarnings(1, reader.getWarnings()); //missing VERSION property

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals(0, vcard.getAllTypes().size());
		assertWarnings(1, reader.getWarnings()); //missing VERSION property

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
		assertEquals(1, vcard.getAllTypes().size());
		RawType prop = vcard.getExtendedType("x-type");
		assertEquals("value", prop.getValue());
		assertWarnings(0, reader.getWarnings());
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
		reader.registerExtendedType(TypeForTesting.class);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getAllTypes().size());
		TypeForTesting prop = vcard.getType(TypeForTesting.class);
		assertEquals("value", prop.value.getSingleValued());
		assertWarnings(0, reader.getWarnings());
	}

	@Test(expected = RuntimeException.class)
	public void registerExtendedType_no_default_constructor() throws Throwable {
		JCardReader reader = new JCardReader("");
		reader.registerExtendedType(BadType.class);
	}

	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-lucky-num\", {}, \"text\", \"13\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerExtendedType(LuckyNumType.class);

		VCard vcard = reader.readNext();
		assertEquals(0, vcard.getAllTypes().size());
		assertWarnings(1, reader.getWarnings());
	}

	private static class TypeForTesting extends VCardType {
		public JCardValue value;

		public TypeForTesting() {
			super("X-TYPE");
		}

		@Override
		protected void doMarshalText(StringBuilder value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			//empty
		}

		@Override
		protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			//empty
		}

		@Override
		protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
			this.value = value;
		}
	}

	@Test
	public void jcard_example() throws Throwable {
		JCardReader reader = new JCardReader(getClass().getResourceAsStream("jcard-example.json"));

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(16, vcard.getAllTypes().size());

		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());

		StructuredNameType n = vcard.getStructuredName();
		assertEquals("Perreault", n.getFamily());
		assertEquals("Simon", n.getGiven());
		assertEquals(Arrays.asList(), n.getAdditional());
		assertEquals(Arrays.asList(), n.getPrefixes());
		assertEquals(Arrays.asList("ing. jr", "M.Sc."), n.getSuffixes());

		PartialDate expectedBday = PartialDate.date(null, 2, 3);
		PartialDate actualBday = vcard.getBirthday().getPartialDate();
		assertEquals(expectedBday, actualBday);

		Date expectedAnniversary = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").parse("2009-08-08 14:30:00-0500");
		Date actualAnniversary = vcard.getAnniversary().getDate();
		assertEquals(expectedAnniversary, actualAnniversary);

		assertTrue(vcard.getGender().isMale());

		LanguageType lang = vcard.getLanguages().get(0);
		assertEquals("fr", lang.getValue());
		assertIntEquals(1, lang.getPref());

		lang = vcard.getLanguages().get(1);
		assertEquals("en", lang.getValue());
		assertIntEquals(2, lang.getPref());

		OrganizationType org = vcard.getOrganization();
		assertEquals(Arrays.asList("Viagenie"), org.getValues());
		assertEquals("work", org.getType());

		AddressType adr = vcard.getAddresses().get(0);
		assertNull(adr.getPoBox());
		assertEquals("Suite D2-630", adr.getExtendedAddress());
		assertEquals("2875 Laurier", adr.getStreetAddress());
		assertEquals("Quebec", adr.getLocality());
		assertEquals("QC", adr.getRegion());
		assertEquals("G1V 2M2", adr.getPostalCode());
		assertEquals("Canada", adr.getCountry());
		assertSetEquals(adr.getTypes(), AddressTypeParameter.WORK);

		TelephoneType tel = vcard.getTelephoneNumbers().get(0);
		TelUri expectedUri = TelUri.global("+1-418-656-9254");
		expectedUri.setExtension("102");
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneTypeParameter.WORK, TelephoneTypeParameter.VOICE);
		assertIntEquals(1, tel.getPref());

		tel = vcard.getTelephoneNumbers().get(1);
		expectedUri = TelUri.global("+1-418-262-6501");
		assertEquals(expectedUri, tel.getUri());
		assertSetEquals(tel.getTypes(), TelephoneTypeParameter.WORK, TelephoneTypeParameter.VOICE, TelephoneTypeParameter.CELL, TelephoneTypeParameter.VIDEO, TelephoneTypeParameter.TEXT);

		EmailType email = vcard.getEmails().get(0);
		assertEquals("simon.perreault@viagenie.ca", email.getValue());
		assertSetEquals(email.getTypes(), EmailTypeParameter.WORK);

		GeoType geo = vcard.getGeo();
		assertEquals(Double.valueOf(46.772673), geo.getLatitude());
		assertEquals(Double.valueOf(-71.282945), geo.getLongitude());
		assertEquals("work", geo.getType());

		KeyType key = vcard.getKeys().get(0);
		assertEquals("http://www.viagenie.ca/simon.perreault/simon.asc", key.getUrl());
		assertEquals("work", key.getType());

		TimezoneType tz = vcard.getTimezone();
		assertIntEquals(-5, tz.getHourOffset());
		assertIntEquals(0, tz.getMinuteOffset());

		UrlType url = vcard.getUrls().get(0);
		assertEquals("http://nomis80.org", url.getValue());
		assertEquals("home", url.getType());

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}
}
