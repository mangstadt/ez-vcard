package ezvcard.io.json;

import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.VCardStringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.LuckyNumType;
import ezvcard.io.LuckyNumType.LuckyNumScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
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
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;

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
	public void setAddProdid() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(true);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		String regex = Pattern.quote("[\"prodid\",{},\"text\",") + "\".*?\"" + Pattern.quote("]");
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sw.toString());
		assertTrue(m.find());
		assertFalse(m.find());
	}

	@Test
	public void setVersionStrict() throws Throwable {
		VCard vcard = new VCard();
		vcard.setMailer("mailer"); //only supported by 2.1 and 3.0

		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw, true);
		writer.setAddProdId(false);

		writer.write(vcard);

		writer.setVersionStrict(false);
		writer.write(vcard);

		writer.setVersionStrict(true);
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[" +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]" +
		    "]" +
		  "]," +
	  	  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"mailer\",{},\"text\",\"mailer\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void setIndent() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw, true);
		writer.setAddProdId(false);
		writer.setIndent(true);

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
		"[" + NEWLINE +
		"\"vcard\",[[" + NEWLINE +
		"  \"version\",{},\"text\",\"4.0\"],[" + NEWLINE +
		"  \"fn\",{},\"text\",\"John Doe\"]]],[" + NEWLINE +
		"\"vcard\",[[" + NEWLINE +
		"  \"version\",{},\"text\",\"4.0\"],[" + NEWLINE +
		"  \"fn\",{},\"text\",\"John Doe\"]]]" + NEWLINE +
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
		vcard.addExtendedType("x-type", "value");
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
		writer.registerScribe(new TypeForTestingScribe());
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addType(new TypeForTesting(JCardValue.single("value")));
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
		writer.registerScribe(new LuckyNumScribe());
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		LuckyNumType prop = new LuckyNumType(0);
		prop.luckyNum = 13;
		vcard.addType(prop);
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
		VCard vcard = new VCard();

		vcard.setFormattedName("SimonPerreault");

		StructuredName n = new StructuredName();
		n.setFamily("Perreault");
		n.setGiven("Simon");
		n.addSuffix("ing.jr");
		n.addSuffix("M.Sc.");
		vcard.setStructuredName(n);

		Birthday bday = new Birthday(PartialDate.date(null, 2, 3));
		vcard.setBirthday(bday);

		Anniversary anniversary = new Anniversary(PartialDate.dateTime(2009, 8, 8, 14, 30, 0, new UtcOffset(-5, 0)));
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
		adr.addType(AddressType.WORK);
		vcard.addAddress(adr);

		TelUri telUri = new TelUri.Builder("+1-418-656-9254").extension("102").build();
		Telephone tel = new Telephone(telUri);
		tel.addType(TelephoneType.WORK);
		tel.addType(TelephoneType.VOICE);
		tel.setPref(1);
		vcard.addTelephoneNumber(tel);

		tel = new Telephone(new TelUri.Builder("+1-418-262-6501").build());
		tel.addType(TelephoneType.WORK);
		tel.addType(TelephoneType.CELL);
		tel.addType(TelephoneType.VOICE);
		tel.addType(TelephoneType.VIDEO);
		tel.addType(TelephoneType.TEXT);
		vcard.addTelephoneNumber(tel);

		vcard.addEmail("simon.perreault@viagenie.ca", EmailType.WORK);

		Geo geo = new Geo(46.772673, -71.282945);
		geo.setType("work");
		vcard.setGeo(geo);

		Key key = new Key("http://www.viagenie.ca/simon.perreault/simon.asc", null);
		key.setType("work");
		vcard.addKey(key);

		vcard.setTimezone(new Timezone(-5, 0));

		vcard.addUrl("http://nomis80.org").setType("home");

		assertValidate(vcard.validate(VCardVersion.V4_0));

		assertExample(vcard, "jcard-example.json", new Filter() {
			public String filter(String json) {
				//replace "date-and-or-time" data types with the data types ez-vcard uses
				//ez-vcard avoids the use of "date-and-or-time"
				json = json.replaceAll("\"bday\",\\{\\},\"date-and-or-time\"", "\"bday\",{},\"date\"");
				json = json.replaceAll("\"anniversary\",\\{\\},\"date-and-or-time\"", "\"anniversary\",{},\"date-time\"");
				return json;
			}
		});
	}

	private void assertExample(VCard vcard, String exampleFileName, Filter filter) throws IOException {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();

		String expected = new String(IOUtils.toByteArray(getClass().getResourceAsStream(exampleFileName)));
		expected = expected.replaceAll("\\s", "");
		if (filter != null) {
			expected = filter.filter(expected);
		}

		String actual = sw.toString();

		assertEquals(expected, actual);
	}

	private interface Filter {
		String filter(String json);
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
			return null;
		}

		@Override
		protected TypeForTesting _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			return null;
		}

		@Override
		protected JCardValue _writeJson(TypeForTesting property) {
			return property.value;
		}
	}
}
