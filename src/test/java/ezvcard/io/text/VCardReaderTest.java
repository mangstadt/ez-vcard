package ezvcard.io.text;

import static ezvcard.VCardDataType.INTEGER;
import static ezvcard.VCardDataType.TEXT;
import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertParseWarnings;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.each;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.github.mangstadt.vinnie.codec.QuotedPrintableCodec;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.LuckyNumProperty;
import ezvcard.io.LuckyNumProperty.LuckyNumScribe;
import ezvcard.io.MyFormattedNameProperty;
import ezvcard.io.MyFormattedNameProperty.MyFormattedNameScribe;
import ezvcard.io.ParseContext;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Label;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.property.asserter.VCardAsserter;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
public class VCardReaderTest {
	/**
	 * Account for an error in the 4.0 specification, which places multi-valued
	 * TYPE parameters in double quotes.
	 */
	@Test
	public void type_parameter_enclosed_in_double_quotes() throws Exception {
		VCardVersion version = V2_1;
		{
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"PROP;TYPE=\"one,two,,three\";FOO=\"four,five,,six\":\r\n" +
			"END:VCARD\r\n"
			);
	
			asserter.next(version);
	
			asserter.rawProperty("PROP")
				.param("TYPE", "\"one,two,,three\"")
				.param("FOO", "\"four,five,,six\"")
				.value("")
			.noMore();
	
			asserter.done();
			//@formatter:on
		}
		for (VCardVersion v : each(V3_0, V4_0)) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + v + "\r\n" +
				"PROP;TYPE=\"one,two,,three\";FOO=\"four,five,,six\":\r\n" +
			"END:VCARD\r\n"
			);
	
			asserter.next(v);
	
			asserter.rawProperty("PROP")
				.param("TYPE", "one", "two", "", "three")
				.param("FOO", "four,five,,six")
				.value("")
			.noMore();
	
			asserter.done();
			//@formatter:on
		}
	}

	/**
	 * All nameless parameters should be assigned a name.
	 */
	@Test
	public void nameless_parameters() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"PROP;TEXT;QUOTED-PRINTABLE;HOME;FOO:\r\n" +
			"END:VCARD\r\n"
			);
	
			asserter.next(version);
	
			asserter.rawProperty("PROP")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.param("TYPE", "HOME", "FOO")
				.dataType(VCardDataType.TEXT)
				.value("")
			.noMore();
	
			asserter.done();
			//@formatter:on
		}
	}

	@Test
	public void quoted_printable_encoding_invalid_value() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"PROP;ENCODING=QUOTED-PRINTABLE:=nnnn\r\n" +
			"END:VCARD\r\n"
			);
			
			asserter.next(version);
			
			asserter.rawProperty("PROP")
				.param("ENCODING", "QUOTED-PRINTABLE")
				.value("=nnnn")
			.noMore();
			
			asserter.warnings(27);
			asserter.done();
			//@formatter:on
		}
	}

	@Test
	public void quoted_printable_invalid_charset() throws Exception {
		String decoded = "one=two";

		for (VCardVersion version : VCardVersion.values()) {
			//without default charset defined
			{
				Charset charset = Charset.defaultCharset();
				QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
				String encoded = codec.encode(decoded);

				//@formatter:off
				VCardAsserter asserter = read(
				"BEGIN:VCARD\r\n" +
					"VERSION:" + version + "\r\n" +
					"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=invalid:" + encoded + "\r\n" +
				"END:VCARD\r\n"
				);
				
				asserter.next(version);
				
				asserter.rawProperty("PROP")
					.param("ENCODING", "QUOTED-PRINTABLE")
					.param("CHARSET", "invalid")
					.value(decoded)
				.noMore();
				
				asserter.warnings(27);
				asserter.done();
				//@formatter:on
			}

			//with default charset defined
			{
				Charset charset = Charset.forName("UTF-16");
				QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
				String encoded = codec.encode(decoded);

				//@formatter:off
				String str = 
				"BEGIN:VCARD\r\n" +
					"VERSION:" + version + "\r\n" +
					"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=invalid:" + encoded + "\r\n" +
				"END:VCARD\r\n";
				
				VCardReader reader = new VCardReader(str);
				reader.setDefaultQuotedPrintableCharset(charset);
				VCardAsserter asserter = new VCardAsserter(reader);
				
				asserter.next(version);
				
				asserter.rawProperty("PROP")
					.param("ENCODING", "QUOTED-PRINTABLE")
					.param("CHARSET", "invalid")
					.value(decoded)
				.noMore();
				
				asserter.warnings(27);
				asserter.done();
				//@formatter:on
			}
		}
	}

	@Test
	public void extended_properties() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"X-LUCKY-NUM:24\r\n" +
				"X-GENDER:ma\\,le\r\n" +
				"X-LUCKY-NUM:22\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new LuckyNumScribe());
			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(3, vcard);

			//read a type that has a type class
			List<LuckyNumProperty> luckyNumTypes = vcard.getProperties(LuckyNumProperty.class);
			assertEquals(2, luckyNumTypes.size());
			assertEquals(24, luckyNumTypes.get(0).luckyNum);
			assertEquals(22, luckyNumTypes.get(1).luckyNum);
			assertTrue(vcard.getExtendedProperties("X-LUCKY-NUM").isEmpty());

			//read a type without a type class
			List<RawProperty> genderTypes = vcard.getExtendedProperties("X-GENDER");
			assertEquals(1, genderTypes.size());
			assertEquals("ma\\,le", genderTypes.get(0).getValue()); //raw type values are not unescaped

			assertParseWarnings(reader);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void extended_properties_override_standard_property_scribes() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"FN:John Doe\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new MyFormattedNameScribe());
			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(1, vcard);

			//read a type that has a type class
			MyFormattedNameProperty fn = vcard.getProperty(MyFormattedNameProperty.class);
			assertEquals("JOHN DOE", fn.value);

			assertParseWarnings(reader);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void read_multiple() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"FN:John Doe\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"FN:Jane Doe\r\n" +
		"END:VCARD\r\n"
		);
		
		asserter.next(V2_1);
		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();
		
		asserter.next(V3_0);
		asserter.simpleProperty(FormattedName.class)
			.value("Jane Doe")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void nested_vcard() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			/*
			 * Test against all versions, even though nested vCards are only
			 * supported by 2.1.
			 */

			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
			"VERSION:" + version + "\r\n" +
			"AGENT:\r\n" +
				"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"FN:Agent 007\r\n" +
				"AGENT:\r\n" +
					"BEGIN:VCARD\r\n" +
					"VERSION:" + version + "\r\n" +
					"FN:Agent 009\r\n" +
					"END:VCARD\r\n" +
				"END:VCARD\r\n" +
			"FN:John Doe\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);

			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(2, vcard);
			assertEquals("John Doe", vcard.getFormattedName().getValue());
			{
				VCard agent1 = vcard.getAgent().getVCard();
				assertVersion(version, agent1);
				assertPropertyCount(2, agent1);
				assertEquals("Agent 007", agent1.getFormattedName().getValue());
				{
					VCard agent2 = agent1.getAgent().getVCard();
					assertVersion(version, agent2);
					assertPropertyCount(1, agent2);
					assertEquals("Agent 009", agent2.getFormattedName().getValue());
				}
			}

			assertParseWarnings(reader);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void nested_vcard_missing_vcard() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			/*
			 * Test against all versions, even though nested vCards are only
			 * supported by 2.1.
			 */

			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"NESTED:\r\n" +
				"FN:John Doe\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new NestedScribe());
			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(2, vcard);

			assertEquals("John Doe", vcard.getFormattedName().getValue());
			assertNull(vcard.getProperty(Nested.class).vcard);

			assertParseWarnings(reader);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void nested_vcard_with_labels() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			/*
			 * Test against all versions, even though nested vCards are only
			 * supported by 2.1.
			 */

			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"ADR;TYPE=home:;;;;;\r\n" +
				"ADR;TYPE=work:;;;;;\r\n" +
				"AGENT:\r\n" +
				"BEGIN:VCARD\r\n" +
					"VERSION:" + version + "\r\n" +
					"LABEL;TYPE=home:home label\r\n" +
					"AGENT:\r\n" +
					"BEGIN:VCARD\r\n" +
						"VERSION:" + version + "\r\n" +
						"ADR;TYPE=dom:;;;;;\r\n" +
						"LABEL;TYPE=dom:dom label\r\n" +
					"END:VCARD\r\n" +
					"ADR;TYPE=dom:;;;;;\r\n" +
				"END:VCARD\r\n" +
				"LABEL;TYPE=work:work label\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(3, vcard);

			Iterator<Address> adrs = vcard.getAddresses().iterator();

			Address adr = adrs.next();
			assertEquals(Arrays.asList(AddressType.HOME), adr.getTypes());
			assertNull(adr.getLabel());

			adr = adrs.next();
			assertEquals(Arrays.asList(AddressType.WORK), adr.getTypes());
			assertEquals("work label", adr.getLabel());

			{
				VCard agentVCard = vcard.getAgent().getVCard();
				assertVersion(version, agentVCard);
				assertPropertyCount(3, agentVCard);

				adr = agentVCard.getAddresses().get(0);
				assertEquals(Arrays.asList(AddressType.DOM), adr.getTypes());
				assertNull(adr.getLabel());

				Label label = agentVCard.getOrphanedLabels().get(0);
				assertEquals(Arrays.asList(AddressType.HOME), label.getTypes());

				{
					VCard agentAgentVCard = agentVCard.getAgent().getVCard();
					assertVersion(version, agentAgentVCard);
					assertPropertyCount(1, agentAgentVCard);

					adr = agentAgentVCard.getAddresses().get(0);
					assertEquals(Arrays.asList(AddressType.DOM), adr.getTypes());
					assertEquals("dom label", adr.getLabel());
				}
			}

			assertParseWarnings(reader);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void embedded_vcard() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			/*
			 * Test against all versions, even though embedded vCards are only
			 * supported by 3.0.
			 */

			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
			"VERSION:" + version + "\r\n" +
			"AGENT:" +
				"BEGIN:VCARD\\n" +
				"VERSION:" + version + "\\n" +
				"FN:Agent 007\\n" +
				"AGENT:" +
					"BEGIN:VCARD\\\\n" +
					"VERSION:" + version + "\\\\n" +
					"FN:Agent 009\\\\n" +
					"END:VCARD\\\\n" +
				"END:VCARD\r\n" +
			"FN:John Doe\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(2, vcard);
			assertEquals("John Doe", vcard.getFormattedName().getValue());
			{
				VCard agentVCard = vcard.getAgent().getVCard();
				assertVersion(version, agentVCard);
				assertPropertyCount(2, agentVCard);
				assertEquals("Agent 007", agentVCard.getFormattedName().getValue());
				{
					VCard agentAgentVCard = agentVCard.getAgent().getVCard();
					assertVersion(version, agentAgentVCard);
					assertPropertyCount(1, agentAgentVCard);
					assertEquals("Agent 009", agentAgentVCard.getFormattedName().getValue());
				}
			}

			assertParseWarnings(reader);
			assertNoMoreVCards(reader);
		}
	}

	/**
	 * LABEL properties should be assigned to an ADR and stored in the
	 * "Address.getLabel()" field. LABELs that could not be assigned to an ADR
	 * should go in "VCard.getOrphanedLabels()".
	 */
	@Test
	public void label_properties() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version.getVersion() + "\r\n" +
				"ADR;TYPE=home;TYPE=parcel:;;123 Main St.;Austin;TX;91827;USA\r\n" +
				"LABEL;TYPE=parcel;TYPE=home:123 Main St.\\nAustin\\, TX 91827\\nUSA\r\n" +
				"ADR;TYPE=work:;;200 Broadway;New York;NY;12345;USA\r\n" +
				"LABEL;TYPE=parcel:200 Broadway\\nNew York\\, NY 12345\\nUSA\r\n" +
			"END:VCARD\r\n"
			);
	
			asserter.next(version);
			
			asserter.address()
				.streetAddress("123 Main St.")
				.locality("Austin")
				.region("TX")
				.postalCode("91827")
				.country("USA")
				.label("123 Main St." + NEWLINE + "Austin, TX 91827" + NEWLINE + "USA")
				.types(AddressType.HOME, AddressType.PARCEL)
			.next()
				.streetAddress("200 Broadway")
				.locality("New York")
				.region("NY")
				.postalCode("12345")
				.country("USA")
				.types(AddressType.WORK)
			.noMore();
			
			asserter.simpleProperty(Label.class)
				.value("200 Broadway" + NEWLINE + "New York, NY 12345" + NEWLINE + "USA")
				.param("TYPE", "parcel")
			.noMore();
	
			asserter.done();
			//@formatter:on
		}
	}

	/**
	 * Escaped newlines should ONLY be unescaped in LABEL parameters of ADR
	 * properties.
	 */
	@Test
	public void label_parameters() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version.getVersion() + "\r\n" +
				"ADR;LABEL=one\\ntwo;PARAM=one\\ntwo:\r\n" +
				"PROP;LABEL=one\\ntwo;PARAM=one\\ntwo:\r\n" +
			"END:VCARD\r\n"
			);
	
			asserter.next(version);
			
			asserter.address()
				.label("one" + NEWLINE + "two")
				.param("PARAM", "one\\ntwo")
			.noMore();
			asserter.rawProperty("PROP")
				.param("LABEL", "one\\ntwo")
				.param("PARAM", "one\\ntwo")
				.value("")
			.noMore();
	
			asserter.done();
			//@formatter:on
		}
	}

	@Test
	public void skipMeException() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"SKIPME:value\r\n" +
				"X-FOO:value\r\n" +
			"END:VCARD\r\n";
	
			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new SkipMeScribe());
			VCardAsserter asserter = new VCardAsserter(reader);
	
			asserter.next(version);
	
			asserter.rawProperty("X-FOO")
				.value("value")
			.noMore();
	
			asserter.warnings(22);
			asserter.done();
			//@formatter:on
		}
	}

	@Test
	public void cannotParseException() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"group.CANNOTPARSE;PARAM=value;VALUE=text:value\r\n" +
				"X-FOO:value\r\n" +
			"END:VCARD\r\n";
	
			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new CannotParseScribe());
			VCardAsserter asserter = new VCardAsserter(reader);
			
			asserter.next(version);
	
			asserter.rawProperty("X-FOO")
				.value("value")
			.noMore();
			
			asserter.rawProperty("CANNOTPARSE")
				.group("group")
				.param("PARAM", "value")
				.dataType(VCardDataType.TEXT)
				.value("value")
			.noMore();
	
			asserter.warnings(25);
			asserter.done();
			//@formatter:on
		}
	}

	@Test
	public void invalid_line() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"bad-line\r\n" +
			"END:VCARD\r\n"
			);
			//@formatter:on

			asserter.next(version);
			asserter.warnings(27);
			asserter.done();
		}
	}

	@Test
	public void property_warning() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"WARNINGS:foo\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new WarningsScribe());
			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(1, vcard);
			assertParseWarnings(reader, (Integer) null);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void warnings_list_cleared() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"bad-line\r\n" +
			"END:VCARD\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
			"END:VCARD\r\n"
			);
			//@formatter:on

			asserter.next(version);
			asserter.warnings(27);

			asserter.next(version);

			asserter.done();
		}
	}

	@Test
	public void warnings_in_nested_vcard() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"AGENT:\r\n" +
				"BEGIN:VCARD\r\n" +
					"VERSION:" + version + "\r\n" +
					"WARNINGS:value\r\n" +
				"END:VCARD\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new WarningsScribe());
			reader.readNext();

			assertParseWarnings(reader, (Integer) null);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void warnings_in_embedded_vcard() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"AGENT:BEGIN:VCARD\\nVERSION:" + version + "\\nWARNINGS:value\\nEND:VCARD\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new WarningsScribe());
			reader.readNext();

			assertParseWarnings(reader, (Integer) null);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void value_parameter() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"VAL;VALUE=text;LANGUAGE=en:value\r\n" +
				"VAL;LANGUAGE=en:value\r\n" +
				"VAL;VALUE=text:value\r\n" +
				"VAL:value\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.registerScribe(new ValueScribe());
			VCard vcard = reader.readNext();
			assertVersion(version, vcard);
			assertPropertyCount(4, vcard);

			Iterator<ValueProp> it = vcard.getProperties(ValueProp.class).iterator();

			ValueProp prop = it.next();
			assertEquals(TEXT, prop.dataType);
			assertEquals(1, prop.getParameters().size());
			assertNull(prop.getParameters().getValue());
			assertEquals("en", prop.getParameters().getLanguage());

			prop = it.next();
			assertEquals(INTEGER, prop.dataType);
			assertEquals(1, prop.getParameters().size());
			assertNull(prop.getParameters().getValue());
			assertEquals("en", prop.getParameters().getLanguage());

			prop = it.next();
			assertEquals(TEXT, prop.dataType);
			assertEquals(0, prop.getParameters().size());
			assertNull(prop.getParameters().getValue());
			assertNull(prop.getParameters().getLanguage());

			prop = it.next();
			assertEquals(INTEGER, prop.dataType);
			assertEquals(0, prop.getParameters().size());
			assertNull(prop.getParameters().getValue());
			assertNull(prop.getParameters().getLanguage());

			assertParseWarnings(reader);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void invalid_version() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:invalid\r\n" +
		"END:VCARD\r\n"
		);

		asserter.next(V2_1); //default to 2.1
		asserter.rawProperty("VERSION")
			.value("invalid")
		.noMore();
		asserter.warnings(27);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void skip_non_vcard_components() throws Exception {
		for (VCardVersion version : VCardVersion.values()) {
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"invalid line--warning should not be logged\r\n" +
				"PRODID:-//Company//Application//EN\r\n" +
			"END:VCALENDAR\r\n" +
			"invalid line--warning should not be logged\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:" + version + "\r\n" +
				"FN:John Doe\r\n" +
			"END:VCARD\r\n"
			);
			
			asserter.next(version);
	
			asserter.simpleProperty(FormattedName.class)
				.value("John Doe")
			.noMore();
	
			asserter.done();
			//@formatter:on
		}
	}

	private static class ValueProp extends VCardProperty {
		private final VCardDataType dataType;

		public ValueProp(VCardDataType dataType) {
			this.dataType = dataType;
		}
	}

	private static class ValueScribe extends VCardPropertyScribe<ValueProp> {
		public ValueScribe() {
			super(ValueProp.class, "VAL");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return INTEGER;
		}

		@Override
		protected String _writeText(ValueProp property, WriteContext context) {
			return null;
		}

		@Override
		protected ValueProp _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
			return new ValueProp(dataType);
		}
	}

	private static class WarningsProperty extends VCardProperty {
		//empty
	}

	private static class WarningsScribe extends VCardPropertyScribe<WarningsProperty> {
		public WarningsScribe() {
			super(WarningsProperty.class, "WARNINGS");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return null;
		}

		@Override
		protected String _writeText(WarningsProperty property, WriteContext context) {
			return null;
		}

		@Override
		protected WarningsProperty _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
			context.addWarning("one");
			return new WarningsProperty();
		}
	}

	private static class Nested extends VCardProperty {
		/*
		 * Initialize this to a new VCard instance to test for the fact that
		 * this should be set to null.
		 */
		private VCard vcard = new VCard();
	}

	private static class NestedScribe extends VCardPropertyScribe<Nested> {
		public NestedScribe() {
			super(Nested.class, "NESTED");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return null;
		}

		@Override
		protected String _writeText(Nested property, WriteContext context) {
			return "";
		}

		@Override
		protected Nested _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
			throw new EmbeddedVCardException(new EmbeddedVCardException.InjectionCallback() {
				private final Nested property = new Nested();

				public void injectVCard(VCard vcard) {
					property.vcard = vcard;
				}

				public VCardProperty getProperty() {
					return property;
				}
			});
		}
	}

	private static VCardAsserter read(String str) {
		VCardReader reader = new VCardReader(str);
		return new VCardAsserter(reader);
	}
}
