package ezvcard.io.text;

import static ezvcard.VCardDataType.INTEGER;
import static ezvcard.VCardDataType.TEXT;
import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertVersion;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.LuckyNumProperty;
import ezvcard.io.LuckyNumProperty.LuckyNumScribe;
import ezvcard.io.MyFormattedNameProperty;
import ezvcard.io.MyFormattedNameProperty.MyFormattedNameScribe;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Label;
import ezvcard.property.Note;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.property.asserter.VCardAsserter;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
public class VCardReaderTest {
	@Test
	public void getParameters() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"NOTE;x-size=8:The note\r\n" +
			"ADR;HOME;WORK:;;;;\r\n" + //nameless parameters
			"LABEL;type=dOm;TyPE=parcel:\r\n" + //repeated parameter name
		"END:VCARD\r\n"
		);

		asserter.next(V2_1);

		asserter.simpleProperty(Note.class)
			.param("x-size", "8")
			.value("The note")
		.noMore();
		
		asserter.address()
			.types(AddressType.HOME, AddressType.WORK)
		.noMore();
		
		asserter.simpleProperty(Label.class)
			.param("TYPE", "DOM", "PARCEL")
			.value("")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void type_parameter_enclosed_in_double_quotes() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:4.0\r\n" +
			"ADR;TYPE=\"dom,home,work\":;;;;\r\n" +
			"ADR;TYPE=\"dom\",\"home\",\"work\":;;;;\r\n" +
			"ADR;TYPE=\"dom\",home,work:;;;;\r\n" +
			"ADR;TYPE=dom,home,work:;;;;\r\n" +
		"END:VCARD\r\n"
		);

		asserter.next(V4_0);

		asserter.address()
			.types(AddressType.DOM, AddressType.HOME, AddressType.WORK)
		.next()
			.types(AddressType.DOM, AddressType.HOME, AddressType.WORK)
		.next()
			.types(AddressType.DOM, AddressType.HOME, AddressType.WORK)
		.next()
			.types(AddressType.DOM, AddressType.HOME, AddressType.WORK)
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void type_parameter_enclosed_in_double_quotes_extra_commas() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:4.0\r\n" +
			"ADR;TYPE=\"dom,home,,work,\":;;;;\r\n" +
		"END:VCARD\r\n"
		);
		
		asserter.next(V4_0);
		
		asserter.address()
			.types(AddressType.DOM, AddressType.HOME, AddressType.get(""), AddressType.WORK, AddressType.get(""))
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void nameless_parameters() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"VAL;TEXT;8BIT;FOO:value\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new ValueScribe());
		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(1, vcard);

		ValueProp property = vcard.getProperty(ValueProp.class);
		assertEquals(TEXT, property.dataType);

		VCardParameters parameters = property.getParameters();
		assertEquals(2, parameters.size());
		assertEquals(Encoding._8BIT, parameters.getEncoding());
		assertEquals("FOO", parameters.getType());

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void decodeQuotedPrintable() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:123 Main St.=0D=0A\r\n" +
			" Austin, TX 91827=0D=0A\r\n" +
			" USA\r\n" +
		"END:VCARD\r\n"
		);

		asserter.next(V2_1);
		
		asserter.simpleProperty(Label.class)
			//.param("ENCODING", "QUOTED-PRINTABLE") ENCODING parameter should be removed
			.param("TYPE", "HOME")
			.value("123 Main St.\r\nAustin, TX 91827\r\nUSA")
		.noMore();
		
		asserter.done();
		//@formatter:on
	}

	@Test
	public void decodeQuotedPrintable_invalidValue() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:test=nnnn\r\n" +
		"END:VCARD\r\n"
		);
		
		asserter.next(V2_1);
		
		asserter.simpleProperty(Label.class)
			//.param("ENCODING", "QUOTED-PRINTABLE") ENCODING parameter should be removed
			.param("TYPE", "HOME")
			.value("test=nnnn")
		.noMore();
		
		asserter.warnings(1);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void decodeQuotedPrintableCharset() throws Exception {
		String expectedValue = "\u00e4\u00f6\u00fc\u00df";

		//UTF-8
		{
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=C3=A4=C3=B6=C3=BC=C3=9F\r\n" +
			"END:VCARD\r\n"
			);
			
			asserter.next(V2_1);
			
			asserter.simpleProperty(Note.class)
				//.param("ENCODING", "QUOTED-PRINTABLE") should be removed
				.param("CHARSET", "UTF-8")
				.value(expectedValue)
			.noMore();

			asserter.done();
			//@formatter:on
		}

		//ISO-8859-1
		{
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=ISO-8859-1:=E4=F6=FC=DF\r\n" +
			"END:VCARD\r\n"
			);

			asserter.next(V2_1);
			
			asserter.simpleProperty(Note.class)
				//.param("ENCODING", "QUOTED-PRINTABLE") should be removed
				.param("CHARSET", "ISO-8859-1")
				.value(expectedValue)
			.noMore();

			asserter.done();
			//@formatter:on
		}

		//no CHARSET parameter
		//with default charset
		{
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE:=E4=F6=FC=DF\r\n" +
			"END:VCARD\r\n";

			VCardReader reader = new VCardReader(str);
			reader.setDefaultQuotedPrintableCharset(Charset.forName("ISO-8859-1"));
			VCardAsserter asserter = new VCardAsserter(reader);
			
			asserter.next(V2_1);
			
			asserter.simpleProperty(Note.class)
				//.param("ENCODING", "QUOTED-PRINTABLE") should be removed
				.value(expectedValue)
			.noMore();

			asserter.done();
			//@formatter:on
		}

		//invalid CHARSET parameter
		//with default charset
		{
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=invalid:=E4=F6=FC=DF\r\n" +
			"END:VCARD\r\n";

			VCardReader reader = new VCardReader(str);
			reader.setDefaultQuotedPrintableCharset(Charset.forName("ISO-8859-1"));
			VCardAsserter asserter = new VCardAsserter(reader);
			
			asserter.next(V2_1);
			
			asserter.simpleProperty(Note.class)
				//.param("ENCODING", "QUOTED-PRINTABLE") should be removed
				.param("CHARSET", "invalid")
				.value(expectedValue)
			.noMore();

			asserter.warnings(1);
			asserter.done();
			//@formatter:on
		}

		String defaultCharset = Charset.defaultCharset().name();
		QuotedPrintableCodec codec = new QuotedPrintableCodec(defaultCharset);
		String encoded = codec.encode(expectedValue);
		if ("????".equals(encoded)) {
			//default charset is US-ASCII, can't run test
			return;
		}

		//no CHARSET parameter specified
		//without default charset
		{
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE:" + encoded + "\r\n" +
			"END:VCARD\r\n"
			);

			asserter.next(V2_1);
			
			asserter.simpleProperty(Note.class)
				//.param("ENCODING", "QUOTED-PRINTABLE") should be removed
				.value(expectedValue)
			.noMore();

			asserter.done();
			//@formatter:on
		}

		//invalid CHARSET parameter
		//with default charset
		{
			//@formatter:off
			VCardAsserter asserter = read(
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=invalid:" + encoded + "\r\n" +
			"END:VCARD\r\n"
			);

			asserter.next(V2_1);
			
			asserter.simpleProperty(Note.class)
				//.param("ENCODING", "QUOTED-PRINTABLE") should be removed
				.param("CHARSET", "invalid")
				.value(expectedValue)
			.noMore();

			asserter.warnings(1);
			asserter.done();
			//@formatter:on
		}
	}

	@Test
	public void unfold() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"NOTE:The vCard MIME Directory Profile also provides support for represent\r\n" +
			" ing other important information about the person associated with the dire\r\n" +
			" ctory entry. For instance, the date of birth of the person\\; an audio clip \r\n" +
			" describing the pronunciation of the name associated with the directory en\r\n" +
			" try, or some other application of the digital sound\\; longitude and latitu\r\n" +
			" de geo-positioning information related to the person associated with the \r\n" +
			" directory entry\\; date and time that the directory information was last up\r\n" +
			" dated\\; annotations often written on a business card\\; Uniform Resource Loc\r\n" +
			" ators (URL) for a website\\; public key information.\r\n" +
		"END:VCARD\r\n"
		);

		asserter.next(V2_1);
		
		asserter.simpleProperty(Note.class)
			.value("The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void readExtendedType() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"X-LUCKY-NUM:24\r\n" +
			"X-GENDER:ma\\,le\r\n" +
			"X-LUCKY-NUM:22\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new LuckyNumScribe());
		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
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

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void readExtendedType_override_standard_type_classes() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new MyFormattedNameScribe());
		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(1, vcard);

		//read a type that has a type class
		MyFormattedNameProperty fn = vcard.getProperty(MyFormattedNameProperty.class);
		assertEquals("JOHN DOE", fn.value);

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void readMultiple() throws Exception {
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
	public void nestedVCard() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"AGENT:\r\n" +
			"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"FN:Agent 007\r\n" +
			"AGENT:\r\n" +
				"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"FN:Agent 009\r\n" +
				"END:VCARD\r\n" +
			"END:VCARD\r\n" +
		"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);

		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(2, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		{
			VCard agent1 = vcard.getAgent().getVCard();
			assertVersion(V2_1, agent1);
			assertPropertyCount(2, agent1);
			assertEquals("Agent 007", agent1.getFormattedName().getValue());
			{
				VCard agent2 = agent1.getAgent().getVCard();
				assertVersion(V2_1, agent2);
				assertPropertyCount(1, agent2);
				assertEquals("Agent 009", agent2.getFormattedName().getValue());
			}
		}

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void nestedVCard_missing_vcard() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"NESTED:\r\n" +
			"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new NestedScribe());
		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(2, vcard);

		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertNull(vcard.getProperty(Nested.class).vcard);

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
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
		protected Nested _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
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

	@Test
	public void nestedVCard_labels() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"ADR;TYPE=home:;;;;;\r\n" +
			"ADR;TYPE=work:;;;;;\r\n" +
			"AGENT:\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"LABEL;TYPE=home:home label\r\n" +
				"AGENT:\r\n" +
				"BEGIN:VCARD\r\n" +
					"VERSION:2.1\r\n" +
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
		assertVersion(V2_1, vcard);
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
			assertVersion(V2_1, agentVCard);
			assertPropertyCount(3, agentVCard);

			adr = agentVCard.getAddresses().get(0);
			assertEquals(Arrays.asList(AddressType.DOM), adr.getTypes());
			assertNull(adr.getLabel());

			Label label = agentVCard.getOrphanedLabels().get(0);
			assertEquals(Arrays.asList(AddressType.HOME), label.getTypes());

			{
				VCard agentAgentVCard = agentVCard.getAgent().getVCard();
				assertVersion(V2_1, agentAgentVCard);
				assertPropertyCount(1, agentAgentVCard);

				adr = agentAgentVCard.getAddresses().get(0);
				assertEquals(Arrays.asList(AddressType.DOM), adr.getTypes());
				assertEquals("dom label", adr.getLabel());
			}
		}

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void embeddedVCard() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"AGENT:" +
			"BEGIN:VCARD\\n" +
			"VERSION:3.0\\n" +
			"FN:Agent 007\\n" +
			"AGENT:" +
				"BEGIN:VCARD\\\\n" +
				"VERSION:3.0\\\\n" +
				"FN:Agent 009\\\\n" +
				"END:VCARD\\\\n" +
			"END:VCARD\r\n" +
		"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(2, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		{
			VCard agentVCard = vcard.getAgent().getVCard();
			assertVersion(V3_0, agentVCard);
			assertPropertyCount(2, agentVCard);
			assertEquals("Agent 007", agentVCard.getFormattedName().getValue());
			{
				VCard agentAgentVCard = agentVCard.getAgent().getVCard();
				assertVersion(V3_0, agentAgentVCard);
				assertPropertyCount(1, agentAgentVCard);
				assertEquals("Agent 009", agentAgentVCard.getFormattedName().getValue());
			}
		}

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	/*
	 * LABEL types should be assigned to an ADR and stored in the
	 * "Address.getLabel()" field. LABELs that could not be assigned to an ADR
	 * should go in "VCard.getOrphanedLabels()".
	 */
	@Test
	public void readLabel() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"ADR;TYPE=home:;;123 Main St.;Austin;TX;91827;USA\r\n" +
			"LABEL;TYPE=home:123 Main St.\\nAustin\\, TX 91827\\nUSA\r\n" +
			"ADR;TYPE=work,parcel:;;200 Broadway;New York;NY;12345;USA\r\n" +
			"LABEL;TYPE=work:200 Broadway\\nNew York\\, NY 12345\\nUSA\r\n" +
		"END:VCARD\r\n"
		);

		asserter.next(V3_0);
		
		asserter.address()
			.streetAddress("123 Main St.")
			.locality("Austin")
			.region("TX")
			.postalCode("91827")
			.country("USA")
			.label("123 Main St." + NEWLINE + "Austin, TX 91827" + NEWLINE + "USA")
			.types(AddressType.HOME)
		.next()
			.streetAddress("200 Broadway")
			.locality("New York")
			.region("NY")
			.postalCode("12345")
			.country("USA")
			.types(AddressType.WORK, AddressType.PARCEL)
		.noMore();
		
		asserter.simpleProperty(Label.class)
			.value("200 Broadway" + NEWLINE + "New York, NY 12345" + NEWLINE + "USA")
			.param("TYPE", "work")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	@Test
	public void skipMeException() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"SKIPME:value\r\n" +
			"X-FOO:value\r\n" +
		"END:VCARD\r\n";

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new SkipMeScribe());
		VCardAsserter asserter = new VCardAsserter(reader);

		asserter.next(V3_0);

		asserter.rawProperty("X-FOO")
			.value("value")
		.noMore();

		asserter.warnings(1);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void cannotParseException() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"CANNOTPARSE:value\r\n" +
			"group.CANNOTPARSE:value\r\n" +
			"X-FOO:value\r\n" +
		"END:VCARD\r\n";

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new CannotParseScribe());
		VCardAsserter asserter = new VCardAsserter(reader);
		
		asserter.next(V3_0);

		asserter.rawProperty("X-FOO")
			.value("value")
		.noMore();
		
		asserter.rawProperty("CANNOTPARSE")
			.value("value")
		.next()
			.group("group")
			.value("value")
		.noMore();

		asserter.warnings(2);
		asserter.done();
		//@formatter:on
	}

	@Test
	public void invalid_line() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"bad-line\r\n" +
		"END:VCARD\r\n"
		);
		//@formatter:on

		asserter.next(V2_1);
		asserter.warnings(1);
		asserter.done();
	}

	@Test
	public void property_warning() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"WARNINGS:foo\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new WarningsScribe());
		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard);
		assertPropertyCount(1, vcard);
		assertEquals(Arrays.asList("Line 3 (WARNINGS property): one"), reader.getWarnings());
		assertNoMoreVCards(reader);
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
		protected WarningsProperty _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			warnings.add("one");
			return new WarningsProperty();
		}
	}

	@Test
	public void warnings_list_cleared() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"bad-line\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
		"END:VCARD\r\n"
		);
		//@formatter:on

		asserter.next(V2_1);
		asserter.warnings(1);

		asserter.next(V2_1);

		asserter.done();
	}

	@Test
	public void warnings_in_nested_vcard() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"AGENT:\r\n" +
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"WARNINGS:value\r\n" +
			"END:VCARD\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new WarningsScribe());
		reader.readNext();

		assertEquals(Arrays.asList("Line 6 (WARNINGS property): one"), reader.getWarnings());
		assertNoMoreVCards(reader);
	}

	@Test
	public void warnings_in_embedded_vcard() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"AGENT:BEGIN:VCARD\\nVERSION:3.0\\nWARNINGS:value\\nEND:VCARD\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new WarningsScribe());
		reader.readNext();

		assertEquals(Arrays.asList("Line 3 (AGENT property): Problem parsing property in nested vCard: Line 3 (WARNINGS property): one"), reader.getWarnings());
		assertNoMoreVCards(reader);
	}

	@Test
	public void value_parameter() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:4.0\r\n" +
			"VAL;VALUE=text;LANGUAGE=en:value\r\n" +
			"VAL;LANGUAGE=en:value\r\n" +
			"VAL;VALUE=text:value\r\n" +
			"VAL:value\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new ValueScribe());
		VCard vcard = reader.readNext();
		assertVersion(V4_0, vcard);
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

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
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
		protected ValueProp _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			return new ValueProp(dataType);
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
		//@formatter:on

		asserter.next(V2_1); //default to 2.1
		asserter.warnings(1);
		asserter.done();
	}

	@Test
	public void setVersionAlias() throws Exception {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:invalid\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.setVersionAlias("invalid", V4_0);

		VCardAsserter asserter = new VCardAsserter(reader);
		asserter.next(V4_0);
		asserter.done();
	}

	@Test
	public void skip_non_vcard_components() throws Exception {
		//@formatter:off
		VCardAsserter asserter = read(
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID:-//Company//Application//EN" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"FN:John Doe\r\n" +
		"END:VCARD\r\n"
		);
		
		asserter.next(V3_0);

		asserter.simpleProperty(FormattedName.class)
			.value("John Doe")
		.noMore();

		asserter.done();
		//@formatter:on
	}

	private static VCardAsserter read(String str) {
		VCardReader reader = new VCardReader(str);
		return new VCardAsserter(reader);
	}
}
