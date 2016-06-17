package ezvcard.io.json;

import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.Writer;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.MyFormattedNameProperty;
import ezvcard.io.MyFormattedNameProperty.MyFormattedNameScribe;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.WriteContext;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Key;
import ezvcard.property.Language;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.property.asserter.VCardAsserter;
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
public class JCardReaderTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void read_single() throws Throwable {
		//@formatter:off
		VCardAsserter asserter = readJson(
		"[\"vcard\"," +
			"[" +
				"[\"version\", {}, \"text\", \"4.0\"]," +
				"[\"fn\", {}, \"text\", \"John Doe\"]" +
			"]" +
		"]"
		);

		asserter.next(V4_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.done();
		//@formatter:on
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		VCardAsserter asserter = readJson(
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
		"]"
		);

		asserter.next(V4_0);
		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.next(V4_0);
		asserter.simpleProperty(FormattedName.class)
			.value("Jane Doe")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void no_version() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readJson(
		"[\"vcard\"," +
			"[" +
				"[\"fn\", {}, \"text\", \"John Doe\"]" +
			"]" +
		"]"
		);

		asserter.next(V4_0); //default to 4.0

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.warnings(1);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void invalid_version() throws Exception {
		//@formatter:off
		VCardAsserter asserter = readJson(
		"[\"vcard\"," +
			"[" +
				"[\"version\", {}, \"text\", \"3.0\"]," +
				"[\"fn\", {}, \"text\", \"John Doe\"]" +
			"]" +
		"]"
		);

		asserter.next(V4_0); //should still set the version to 4.0

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.warnings(1);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void no_properties() throws Throwable {
		//@formatter:off
		VCardAsserter asserter = readJson(
		"[\"vcard\"," +
			"[" +
			"]" +
		"]"
		);

		asserter.next(V4_0); //default to 4.0
		asserter.warnings(1); //missing VERSION property
		asserter.done();
		//@formatter:on
	}

	@Test
	public void no_properties_multiple() throws Throwable {
		//@formatter:off
		VCardAsserter asserter = readJson(
		"[" +
			"[\"vcard\"," +
				"[" +
				"]" +
			"]," +
			"[\"vcard\"," +
				"[" +
				"]" +
			"]" +
		"]"
		);

		asserter.next(V4_0); //default to 4.0
		asserter.warnings(1); //missing VERSION property

		asserter.next(V4_0); //default to 4.0
		asserter.warnings(1); //missing VERSION property

		asserter.done();
		//@formatter:on
	}

	@Test
	public void extendedType() throws Throwable {
		//@formatter:off
		VCardAsserter asserter = readJson(
		"[\"vcard\"," +
			"[" +
				"[\"version\", {}, \"text\", \"4.0\"]," +
				"[\"x-type\", {}, \"text\", \"value\"]" +
			"]" +
		"]"
		);

		asserter.next(V4_0);

		asserter.rawProperty("x-type")
			.dataType(VCardDataType.TEXT)
			.value("value")
		.noMore();

		asserter.done();
		//@formatter:on
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
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);

		TypeForTesting prop = vcard.getProperty(TypeForTesting.class);
		assertEquals("value", prop.value.asSingle());

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
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
		assertPropertyCount(1, vcard);
		assertVersion(V4_0, vcard);

		MyFormattedNameProperty prop = vcard.getProperty(MyFormattedNameProperty.class);
		assertEquals("JOHN DOE", prop.value);

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
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
		VCardAsserter asserter = new VCardAsserter(reader);

		asserter.next(V4_0);
		asserter.warnings(1);
		asserter.done();
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

		JCardReader reader = new JCardReader(json);
		reader.registerScribe(new CannotParseScribe());
		VCardAsserter asserter = new VCardAsserter(reader);
		
		asserter.next(V4_0);

		asserter.rawProperty("x-foo")
			.dataType(VCardDataType.TEXT)
			.value("value")
		.noMore();
		
		asserter.rawProperty("cannotparse")
			.dataType(VCardDataType.TEXT)
			.value("value")
		.noMore();

		asserter.warnings(1);
		asserter.done();
		//@formatter:on
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

		File file = tempFolder.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write(json);
		writer.close();

		JCardReader reader = new JCardReader(file);
		VCardAsserter asserter = new VCardAsserter(reader);
		
		asserter.next(V4_0);

		asserter.simpleProperty(Note.class)
			.value("\u019dote")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	private static class TypeForTesting extends VCardProperty {
		public JCardValue value;

		public TypeForTesting(JCardValue value) {
			this.value = value;
		}

		@Override
		public TypeForTesting copy() {
			throw new UnsupportedOperationException("Copy method should not be used.");
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
		protected String _writeText(TypeForTesting property, WriteContext context) {
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
		VCardAsserter asserter = new VCardAsserter(reader);

		asserter.next(V4_0);

		//@formatter:off
		asserter.simpleProperty(FormattedName.class)
			.value("Simon Perreault")
		.noMore();
		
		asserter.structuredName()
			.family("Perreault")
			.given("Simon")
			.suffixes("ing. jr", "M.Sc.")
			.noMore();
		
		asserter.dateProperty(Birthday.class)
			.partialDate(PartialDate.builder().month(2).date(3).build())
		.noMore();
		
		asserter.dateProperty(Anniversary.class)
			.date("2009-08-08 19:30:00 +0000")
		.noMore();
		
		asserter.property(Gender.class)
			.expected(Gender.male())
		.noMore();
		
		asserter.simpleProperty(Language.class)
			.value("fr")
			.param("PREF", "1")
		.next()
			.value("en")
			.param("PREF", "2")
		.noMore();
		
		asserter.listProperty(Organization.class)
			.values("Viagenie")
			.param("TYPE", "work")
		.noMore();
		
		asserter.address()
			.extendedAddress("Suite D2-630")
			.streetAddress("2875 Laurier")
			.locality("Quebec")
			.region("QC")
			.postalCode("G1V 2M2")
			.country("Canada")
			.types(AddressType.WORK)
		.noMore();
		
		asserter.telephone()
			.uri(new TelUri.Builder("+1-418-656-9254").extension("102").build())
			.types(TelephoneType.WORK, TelephoneType.VOICE)
			.param("PREF", "1")
		.next()
			.uri(new TelUri.Builder("+1-418-262-6501").build())
			.types(TelephoneType.WORK, TelephoneType.CELL, TelephoneType.VOICE, TelephoneType.VIDEO, TelephoneType.TEXT)
		.noMore();
		
		asserter.email()
			.value("simon.perreault@viagenie.ca")
			.types(EmailType.WORK)
		.noMore();
		
		asserter.geo()
			.latitude(46.772673)
			.longitude(-71.282945)
			.param("TYPE", "work")
		.noMore();
		
		asserter.binaryProperty(Key.class)
			.url("http://www.viagenie.ca/simon.perreault/simon.asc")
			.param("TYPE","work")
		.noMore();
		
		asserter.timezone()
			.offset(new UtcOffset(false, -5, 0))
		.noMore();
		
		asserter.simpleProperty(Url.class)
			.value("http://nomis80.org")
			.param("TYPE", "home")
		.noMore();
		//@formatter:on

		asserter.validate().run();
		asserter.done();
	}

	private static VCardAsserter readJson(String json) {
		JCardReader reader = new JCardReader(json);
		return new VCardAsserter(reader);
	}
}
