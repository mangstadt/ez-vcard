package ezvcard.io.text;

import static ezvcard.VCardDataType.INTEGER;
import static ezvcard.VCardDataType.TEXT;
import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.property.asserter.PropertyAsserter.assertAddress;
import static ezvcard.property.asserter.PropertyAsserter.assertRawProperty;
import static ezvcard.property.asserter.PropertyAsserter.assertSimpleProperty;
import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertNoMoreVCards;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertSetEquals;
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
import ezvcard.io.LuckyNumType;
import ezvcard.io.LuckyNumType.LuckyNumScribe;
import ezvcard.io.MyFormattedNameType;
import ezvcard.io.MyFormattedNameType.MyFormattedNameScribe;
import ezvcard.io.scribe.CannotParseScribe;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Label;
import ezvcard.property.Note;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
public class VCardReaderTest {
	@Test
	public void getParameters() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"NOTE;x-size=8:The note\r\n" +
			"ADR;HOME;WORK:;;;;\r\n" + //nameless parameters
			"LABEL;type=dOm;TyPE=parcel:\r\n" + //repeated parameter name
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertPropertyCount(3, vcard);

		Note note = vcard.getNotes().get(0);
		assertEquals(1, note.getParameters().size());
		assertEquals("8", note.getParameters().first("X-SIZE"));
		assertEquals("8", note.getParameters().first("x-size"));
		assertNull(note.getParameters().first("non-existant"));

		Address adr = vcard.getAddresses().get(0);
		assertEquals(2, adr.getParameters().size());
		assertSetEquals(adr.getTypes(), AddressType.HOME, AddressType.WORK);

		Label label = vcard.getOrphanedLabels().get(0);
		assertEquals(2, label.getParameters().size());
		assertSetEquals(label.getTypes(), AddressType.DOM, AddressType.PARCEL);

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void type_parameter_enclosed_in_double_quotes() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:4.0\r\n" +
			"ADR;TYPE=\"dom,home,work\":;;;;\r\n" +
			"ADR;TYPE=\"dom\",\"home\",\"work\":;;;;\r\n" +
			"ADR;TYPE=\"dom\",home,work:;;;;\r\n" +
			"ADR;TYPE=dom,home,work:;;;;\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertPropertyCount(4, vcard);

		for (Address adr : vcard.getAddresses()) {
			assertEquals(3, adr.getParameters().size());
			assertSetEquals(adr.getTypes(), AddressType.DOM, AddressType.HOME, AddressType.WORK);
		}

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
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
		assertPropertyCount(1, vcard);

		ValueProp property = vcard.getProperty(ValueProp.class);
		assertEquals(TEXT, property.dataType);
		assertEquals(2, property.getParameters().size());
		assertEquals(Encoding._8BIT, property.getParameters().getEncoding());
		assertEquals("FOO", property.getParameters().getType());

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void decodeQuotedPrintable() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:123 Main St.=0D=0A\r\n" +
			" Austin, TX 91827=0D=0A\r\n" +
			" USA\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertPropertyCount(1, vcard);

		Label label = vcard.getOrphanedLabels().get(0);
		assertEquals("123 Main St.\r\nAustin, TX 91827\r\nUSA", label.getValue());
		assertNull(label.getParameters().getEncoding()); //ENCODING parameter should be removed

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void decodeQuotedPrintable_invalidValue() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:test=nnnn\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertPropertyCount(1, vcard);

		Label label = vcard.getOrphanedLabels().get(0);
		assertEquals("test=nnnn", label.getValue());
		assertNull(label.getParameters().getEncoding()); //ENCODING parameter should be removed

		assertWarnings(1, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void decodeQuotedPrintableCharset() throws Throwable {
		String expectedValue = "\u00e4\u00f6\u00fc\u00df";

		//UTF-8
		{
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=C3=A4=C3=B6=C3=BC=C3=9F\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();
			assertPropertyCount(1, vcard);

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING parameter should be removed

			assertWarnings(0, reader);
			assertNoMoreVCards(reader);
		}

		//ISO-8859-1
		{
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=ISO-8859-1:=E4=F6=FC=DF\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();
			assertPropertyCount(1, vcard);

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING parameter should be removed

			assertWarnings(0, reader);
			assertNoMoreVCards(reader);
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
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.setDefaultQuotedPrintableCharset(Charset.forName("ISO-8859-1"));
			VCard vcard = reader.readNext();
			assertPropertyCount(1, vcard);

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING parameter should be removed

			assertWarnings(0, reader);
			assertNoMoreVCards(reader);
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
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			reader.setDefaultQuotedPrintableCharset(Charset.forName("ISO-8859-1"));
			VCard vcard = reader.readNext();
			assertPropertyCount(1, vcard);

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING parameter should be removed

			assertWarnings(1, reader);
			assertNoMoreVCards(reader);
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
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE:" + encoded + "\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();
			assertPropertyCount(1, vcard);

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING parameter should be removed

			assertWarnings(0, reader);
			assertNoMoreVCards(reader);
		}

		//invalid CHARSET parameter
		//with default charset
		{
			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=invalid:" + encoded + "\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();
			assertPropertyCount(1, vcard);

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING parameter should be removed

			assertWarnings(1, reader);
			assertNoMoreVCards(reader);
		}
	}

	@Test
	public void unfold() throws Throwable {
		//@formatter:off
		String str =
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
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertPropertyCount(1, vcard);

		String expected = "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.";
		String actual = vcard.getNotes().get(0).getValue();
		assertEquals(expected, actual);

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void readExtendedType() throws Throwable {
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
		assertPropertyCount(3, vcard);

		//read a type that has a type class
		List<LuckyNumType> luckyNumTypes = vcard.getProperties(LuckyNumType.class);
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
	public void readExtendedType_override_standard_type_classes() throws Throwable {
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
		assertPropertyCount(1, vcard);

		//read a type that has a type class
		MyFormattedNameType fn = vcard.getProperty(MyFormattedNameType.class);
		assertEquals("JOHN DOE", fn.value);

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void readMultiple() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"FN:John Doe\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"FN:Jane Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard;

		vcard = reader.readNext();
		assertPropertyCount(1, vcard);
		assertVersion(V2_1, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader);

		vcard = reader.readNext();
		assertPropertyCount(1, vcard);
		assertVersion(V3_0, vcard);
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader);

		assertNoMoreVCards(reader);
	}

	@Test
	public void nestedVCard() throws Throwable {
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
		assertPropertyCount(2, vcard);
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		VCard agent1 = vcard.getAgent().getVCard();
		assertPropertyCount(2, agent1);
		assertEquals("Agent 007", agent1.getFormattedName().getValue());
		VCard agent2 = agent1.getAgent().getVCard();
		assertPropertyCount(2, vcard);
		assertEquals("Agent 009", agent2.getFormattedName().getValue());

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void nestedVCard_missing_vcard() throws Throwable {
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
		assertPropertyCount(2, vcard);

		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertNull(vcard.getProperty(Nested.class).vcard);

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	private static class Nested extends VCardProperty {
		private VCard vcard = new VCard(); //init this to a new VCard instance to test for the fact that this should be set to null
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
		protected String _writeText(Nested property, VCardVersion version) {
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
	public void nestedVCard_labels() throws Throwable {
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

		{
			assertPropertyCount(3, vcard);

			Iterator<Address> adrs = vcard.getAddresses().iterator();

			Address adr = adrs.next();
			assertSetEquals(adr.getTypes(), AddressType.HOME);
			assertNull(adr.getLabel());

			adr = adrs.next();
			assertSetEquals(adr.getTypes(), AddressType.WORK);
			assertEquals("work label", adr.getLabel());
		}

		vcard = vcard.getAgent().getVCard();
		{
			assertPropertyCount(3, vcard);

			Address adr = vcard.getAddresses().get(0);
			assertSetEquals(adr.getTypes(), AddressType.DOM);
			assertNull(adr.getLabel());

			Label label = vcard.getOrphanedLabels().get(0);
			assertSetEquals(label.getTypes(), AddressType.HOME);
		}

		vcard = vcard.getAgent().getVCard();
		{
			assertPropertyCount(1, vcard);

			Address adr = vcard.getAddresses().get(0);
			assertSetEquals(adr.getTypes(), AddressType.DOM);
			assertEquals("dom label", adr.getLabel());
		}

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void embeddedVCard() throws Throwable {
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

		//@formatter:off
		assertPropertyCount(2, vcard);
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();
		
		VCard agent1 = vcard.getAgent().getVCard();
		{
			assertPropertyCount(2, agent1);
			assertSimpleProperty(agent1.getFormattedNames())
				.value("Agent 007")
			.noMore();
			
			VCard agent2 = agent1.getAgent().getVCard();
			{
				assertPropertyCount(1, agent2);
				assertSimpleProperty(agent2.getFormattedNames())
					.value("Agent 009")
				.noMore();
			}
		}
		//@formatter:on

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	/*
	 * LABEL types should be assigned to an ADR and stored in the
	 * "Address.getLabel()" field. LABELs that could not be assigned to an ADR
	 * should go in "VCard.getOrphanedLabels()".
	 */
	@Test
	public void readLabel() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"ADR;TYPE=home:;;123 Main St.;Austin;TX;91827;USA\r\n" +
			"LABEL;TYPE=home:123 Main St.\\nAustin\\, TX 91827\\nUSA\r\n" +
			"ADR;TYPE=work,parcel:;;200 Broadway;New York;NY;12345;USA\r\n" +
			"LABEL;TYPE=work:200 Broadway\\nNew York\\, NY 12345\\nUSA\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertPropertyCount(3, vcard);

		//@formatter:off
		assertAddress(vcard)
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
		
		assertSimpleProperty(vcard.getOrphanedLabels())
			.value("200 Broadway" + NEWLINE + "New York, NY 12345" + NEWLINE + "USA")
			.param("TYPE", "work")
		.noMore();
		//@formatter:on

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"SKIPME:value\r\n" +
			"X-FOO:value\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new SkipMeScribe());
		VCard vcard = reader.readNext();
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertRawProperty("X-FOO", vcard)
			.value("value")
		.noMore();
		//@formatter:on

		assertWarnings(1, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void cannotParseException() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"CANNOTPARSE:value\r\n" +
			"group.CANNOTPARSE:value\r\n" +
			"X-FOO:value\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new CannotParseScribe());

		VCard vcard = reader.readNext();
		assertPropertyCount(3, vcard);

		//@formatter:off
		assertRawProperty("X-FOO", vcard)
			.value("value")
		.noMore();
		
		assertRawProperty("CANNOTPARSE", vcard)
			.value("value")
		.next()
			.group("group")
			.value("value")
		.noMore();
		//@formatter:on

		assertWarnings(2, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void invalid_line() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"bad-line\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.readNext();

		assertWarnings(1, reader);
		assertNoMoreVCards(reader);
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
		reader.readNext();

		assertEquals(Arrays.asList("Line 3 (WARNINGS property): one"), reader.getWarnings());

		assertWarnings(1, reader);
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
		protected String _writeText(WarningsProperty property, VCardVersion version) {
			return null;
		}

		@Override
		protected WarningsProperty _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			warnings.add("one");
			return new WarningsProperty();
		}
	}

	@Test
	public void warnings_list_cleared() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"bad-line\r\n" +
		"END:VCARD\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);

		reader.readNext();
		assertWarnings(1, reader);

		reader.readNext();
		assertWarnings(0, reader);

		assertNoMoreVCards(reader);
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

		assertWarnings(1, reader);
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

		assertWarnings(1, reader);
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
		protected String _writeText(ValueProp property, VCardVersion version) {
			return null;
		}

		@Override
		protected ValueProp _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			return new ValueProp(dataType);
		}
	}

	@Test
	public void invalid_version() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
			"VERSION:invalid\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		VCard vcard = reader.readNext();
		assertVersion(V2_1, vcard); //default to 2.1

		assertWarnings(1, reader);
		assertNoMoreVCards(reader);
	}

	@Test
	public void skip_non_vcard_components() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID:-//Company//Application//EN" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);

		VCard vcard = reader.readNext();
		assertVersion(V3_0, vcard);
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();
		//@formatter:on

		assertWarnings(0, reader);
		assertNoMoreVCards(reader);
	}
}
