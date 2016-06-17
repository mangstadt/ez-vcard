package ezvcard.io.json;

import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.WriteContext;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.SkipMeProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;

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
public class JCardWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void write_single_vcard() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"version\",{},\"text\",\"4.0\"]," +
				"[\"fn\",{},\"text\",\"John Doe\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_multiple_vcards() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw, true);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("Jane Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[" +
			"[\"vcard\"," +
				"[" +
					"[\"version\",{},\"text\",\"4.0\"]," +
					"[\"fn\",{},\"text\",\"John Doe\"]" +
				"]" +
			"]," +
			"[\"vcard\"," +
				"[" +
					"[\"version\",{},\"text\",\"4.0\"]," +
					"[\"fn\",{},\"text\",\"Jane Doe\"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void setPrettyPrint() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw, true);
		writer.setAddProdId(false);
		writer.setPrettyPrint(true);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"  [" + NEWLINE +
		"    \"vcard\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"version\", { }, \"text\", \"4.0\" ]," + NEWLINE +
		"      [ \"fn\", { }, \"text\", \"John Doe\" ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]," + NEWLINE +
		"  [" + NEWLINE +
		"    \"vcard\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"version\", { }, \"text\", \"4.0\" ]," + NEWLINE +
		"      [ \"fn\", { }, \"text\", \"John Doe\" ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]" + NEWLINE +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void setPrettyPrinter() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw, true);
		writer.setAddProdId(false);
		writer.setPrettyPrinter(new JCardPrettyPrinter());

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"  [" + NEWLINE +
		"    \"vcard\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"version\", { }, \"text\", \"4.0\" ]," + NEWLINE +
		"      [ \"fn\", { }, \"text\", \"John Doe\" ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]," + NEWLINE +
		"  [" + NEWLINE +
		"    \"vcard\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"version\", { }, \"text\", \"4.0\" ]," + NEWLINE +
		"      [ \"fn\", { }, \"text\", \"John Doe\" ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]" + NEWLINE +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_no_vcards() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.close();
		assertEquals("", sw.toString());
	}

	@Test
	public void write_raw_property() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addExtendedProperty("x-type", "value");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"version\",{},\"text\",\"4.0\"]," +
				"[\"fn\",{},\"text\",\"John Doe\"]," +
				"[\"x-type\",{},\"unknown\",\"value\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_extended_property() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.registerScribe(new TestScribe());
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addProperty(new TestProperty(JCardValue.single("value")));
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"version\",{},\"text\",\"4.0\"]," +
				"[\"fn\",{},\"text\",\"John Doe\"]," +
				"[\"x-type\",{},\"text\",\"value\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void skipMeException() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.registerScribe(new SkipMeScribe());
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addProperty(new SkipMeProperty());
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"version\",{},\"text\",\"4.0\"]," +
				"[\"fn\",{},\"text\",\"John Doe\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void utf8() throws Throwable {
		VCard vcard = new VCard();
		vcard.addNote("\u019dote");

		File file = tempFolder.newFile();
		JCardWriter writer = new JCardWriter(file);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"version\",{},\"text\",\"4.0\"]," +
				"[\"note\",{},\"text\",\"\u019dote\"]" +
			"]" +
		"]";
		//@formatter:on
		String actual = IOUtils.getFileContents(file, "UTF-8");
		assertEquals(expected, actual);
	}

	@Test
	public void jcard_example() throws Throwable {
		VCard vcard = createExample();

		assertValidate(vcard).versions(VCardVersion.V4_0).run();
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();
		String actual = sw.toString();

		assertExample(actual, "jcard-example.json");
	}

	public static VCard createExample() {
		VCard vcard = new VCard();

		vcard.setFormattedName("SimonPerreault");

		StructuredName n = new StructuredName();
		n.setFamily("Perreault");
		n.setGiven("Simon");
		n.getSuffixes().add("ing.jr");
		n.getSuffixes().add("M.Sc.");
		vcard.setStructuredName(n);

		Birthday bday = new Birthday(PartialDate.builder().month(2).date(3).build());
		vcard.setBirthday(bday);

		Anniversary anniversary = new Anniversary(PartialDate.builder().year(2009).month(8).date(8).hour(14).minute(30).second(0).offset(new UtcOffset(false, -5, 0)).build());
		vcard.setAnniversary(anniversary);

		vcard.setGender(Gender.male());

		vcard.addLanguage("fr").setPref(1);
		vcard.addLanguage("en").setPref(2);

		vcard.setOrganization("Viagenie").setType("work");

		Address adr = new Address();
		adr.setExtendedAddress("SuiteD2-630");
		adr.setStreetAddress("2875Laurier");
		adr.setLocality("Quebec");
		adr.setRegion("QC");
		adr.setPostalCode("G1V2M2");
		adr.setCountry("Canada");
		adr.getTypes().add(AddressType.WORK);
		vcard.addAddress(adr);

		TelUri telUri = new TelUri.Builder("+1-418-656-9254").extension("102").build();
		Telephone tel = new Telephone(telUri);
		tel.getTypes().add(TelephoneType.WORK);
		tel.getTypes().add(TelephoneType.VOICE);
		tel.setPref(1);
		vcard.addTelephoneNumber(tel);

		tel = new Telephone(new TelUri.Builder("+1-418-262-6501").build());
		tel.getTypes().add(TelephoneType.WORK);
		tel.getTypes().add(TelephoneType.CELL);
		tel.getTypes().add(TelephoneType.VOICE);
		tel.getTypes().add(TelephoneType.VIDEO);
		tel.getTypes().add(TelephoneType.TEXT);
		vcard.addTelephoneNumber(tel);

		vcard.addEmail("simon.perreault@viagenie.ca", EmailType.WORK);

		Geo geo = new Geo(46.772673, -71.282945);
		geo.setType("work");
		vcard.setGeo(geo);

		Key key = new Key("http://www.viagenie.ca/simon.perreault/simon.asc", null);
		key.setType("work");
		vcard.addKey(key);

		vcard.setTimezone(new Timezone(new UtcOffset(false, -5, 0)));

		vcard.addUrl("http://nomis80.org").setType("home");
		return vcard;
	}

	public static void assertExample(String actual, String exampleFileName) throws IOException {
		Filter filter = new Filter() {
			public String filter(String json) {
				//replace "date-and-or-time" data types with the data types ez-vcard uses
				//ez-vcard avoids the use of "date-and-or-time"
				json = json.replaceAll("\"bday\",\\{\\},\"date-and-or-time\"", "\"bday\",{},\"date\"");
				json = json.replaceAll("\"anniversary\",\\{\\},\"date-and-or-time\"", "\"anniversary\",{},\"date-time\"");
				return json;
			}
		};

		String expected = new String(IOUtils.toByteArray(JCardWriterTest.class.getResourceAsStream(exampleFileName)));
		expected = expected.replaceAll("\\s", "");
		if (filter != null) {
			expected = filter.filter(expected);
		}

		assertEquals(expected, actual);
	}

	private interface Filter {
		String filter(String json);
	}

	private static class TestProperty extends VCardProperty {
		public JCardValue value;

		public TestProperty(JCardValue value) {
			this.value = value;
		}

		@Override
		public TestProperty copy() {
			throw new UnsupportedOperationException("Copy method should not be used.");
		}
	}

	private static class TestScribe extends VCardPropertyScribe<TestProperty> {
		public TestScribe() {
			super(TestProperty.class, "X-TYPE");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return VCardDataType.TEXT;
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			return null;
		}

		@Override
		protected TestProperty _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			return null;
		}

		@Override
		protected JCardValue _writeJson(TestProperty property) {
			return property.value;
		}
	}
}
