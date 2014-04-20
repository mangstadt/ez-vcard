package ezvcard.io.text;

import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.LuckyNumType;
import ezvcard.io.LuckyNumType.LuckyNumScribe;
import ezvcard.io.MyFormattedNameType;
import ezvcard.io.MyFormattedNameType.MyFormattedNameScribe;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.KeyType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.Categories;
import ezvcard.property.Classification;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.FreeBusyUrl;
import ezvcard.property.Geo;
import ezvcard.property.Key;
import ezvcard.property.Label;
import ezvcard.property.Language;
import ezvcard.property.Mailer;
import ezvcard.property.Nickname;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.ProductId;
import ezvcard.property.Profile;
import ezvcard.property.RawProperty;
import ezvcard.property.Revision;
import ezvcard.property.Role;
import ezvcard.property.SortString;
import ezvcard.property.Source;
import ezvcard.property.SourceDisplayText;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Title;
import ezvcard.property.Uid;
import ezvcard.property.Url;
import ezvcard.util.PartialDate;
import ezvcard.util.TelUri;
import ezvcard.util.UtcOffset;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
@SuppressWarnings("resource")
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

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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

		Address adr = vcard.getAddresses().get(0);
		assertEquals(3, adr.getParameters().size());
		assertSetEquals(adr.getTypes(), AddressType.DOM, AddressType.HOME, AddressType.WORK);

		adr = vcard.getAddresses().get(1);
		assertEquals(3, adr.getParameters().size());
		assertSetEquals(adr.getTypes(), AddressType.DOM, AddressType.HOME, AddressType.WORK);

		adr = vcard.getAddresses().get(2);
		assertEquals(3, adr.getParameters().size());
		assertSetEquals(adr.getTypes(), AddressType.DOM, AddressType.HOME, AddressType.WORK);

		adr = vcard.getAddresses().get(3);
		assertEquals(3, adr.getParameters().size());
		assertSetEquals(adr.getTypes(), AddressType.DOM, AddressType.HOME, AddressType.WORK);

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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

		Label label = vcard.getOrphanedLabels().get(0);
		assertEquals("123 Main St.\r\nAustin, TX 91827\r\nUSA", label.getValue());
		assertNull(label.getParameters().getEncoding()); //ENCODING sub type should be removed

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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

		Label label = vcard.getOrphanedLabels().get(0);
		assertEquals("test=nnnn", label.getValue());
		assertNull(label.getParameters().getEncoding()); //ENCODING sub type should be removed

		assertWarnings(1, reader.getWarnings());
		assertNull(reader.readNext());
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

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING sub type should be removed

			assertWarnings(0, reader.getWarnings());
			assertNull(reader.readNext());
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

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING sub type should be removed

			assertWarnings(0, reader.getWarnings());
			assertNull(reader.readNext());
		}

		//invalid CHARSET parameter
		//without default charset
		{
			String defaultCharset = Charset.defaultCharset().name();
			QuotedPrintableCodec codec = new QuotedPrintableCodec(defaultCharset);
			String encoded = codec.encode(expectedValue);

			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=invalid:" + encoded + "\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING sub type should be removed

			assertWarnings(1, reader.getWarnings());
			assertNull(reader.readNext());
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

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING sub type should be removed

			assertWarnings(1, reader.getWarnings());
			assertNull(reader.readNext());
		}

		//no CHARSET parameter specified
		//without default charset
		{
			String defaultCharset = Charset.defaultCharset().name();
			QuotedPrintableCodec codec = new QuotedPrintableCodec(defaultCharset);
			String encoded = codec.encode(expectedValue);

			//@formatter:off
			String str =
			"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"NOTE;ENCODING=QUOTED-PRINTABLE:" + encoded + "\r\n" +
			"END:VCARD\r\n";
			//@formatter:on

			VCardReader reader = new VCardReader(str);
			VCard vcard = reader.readNext();

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING sub type should be removed

			assertWarnings(0, reader.getWarnings());
			assertNull(reader.readNext());
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

			Note note = vcard.getNotes().get(0);
			assertEquals(expectedValue, note.getValue());
			assertNull(note.getParameters().getEncoding()); //ENCODING sub type should be removed

			assertWarnings(0, reader.getWarnings());
			assertNull(reader.readNext());
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

		String expected = "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.";
		String actual = vcard.getNotes().get(0).getValue();
		assertEquals(expected, actual);

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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
		assertEquals(1, vcard.getProperties().size());

		//read a type that has a type class
		MyFormattedNameType fn = vcard.getProperty(MyFormattedNameType.class);
		assertEquals("JOHN DOE", fn.value);

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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
		assertEquals(VCardVersion.V2_1, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

		vcard = reader.readNext();
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

		assertNull(reader.readNext());
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
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		VCard agent1 = vcard.getAgent().getVCard();
		assertEquals("Agent 007", agent1.getFormattedName().getValue());
		VCard agent2 = agent1.getAgent().getVCard();
		assertEquals("Agent 009", agent2.getFormattedName().getValue());

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void nestedVCard_missing_vcard() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"AGENT:\r\n" +
		"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);

		VCard vcard = reader.readNext();
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertNull(vcard.getAgent().getVCard());

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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

		assertEquals("John Doe", vcard.getFormattedName().getValue());
		VCard agent1 = vcard.getAgent().getVCard();
		assertEquals("Agent 007", agent1.getFormattedName().getValue());
		VCard agent2 = agent1.getAgent().getVCard();
		assertEquals("Agent 009", agent2.getFormattedName().getValue());

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
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

		assertEquals(2, vcard.getAddresses().size());

		Address adr = vcard.getAddresses().get(0);
		assertSetEquals(adr.getTypes(), AddressType.HOME);
		assertEquals("123 Main St." + NEWLINE + "Austin, TX 91827" + NEWLINE + "USA", adr.getLabel());

		adr = vcard.getAddresses().get(1);
		assertSetEquals(adr.getTypes(), AddressType.WORK, AddressType.PARCEL);
		assertNull(adr.getLabel());

		assertEquals(1, vcard.getOrphanedLabels().size());
		Label label = vcard.getOrphanedLabels().get(0);
		assertEquals("200 Broadway" + NEWLINE + "New York, NY 12345" + NEWLINE + "USA", label.getValue());
		assertSetEquals(label.getTypes(), AddressType.WORK);

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	/*
	 * If the type's unmarshal method throws a SkipMeException, then a warning
	 * should be added to the warnings list and the type object should NOT be
	 * added to the {@link VCard} object.
	 */
	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String str =
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"X-LUCKY-NUM:24\r\n" +
		"X-LUCKY-NUM:13\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		VCardReader reader = new VCardReader(str);
		reader.registerScribe(new LuckyNumScribe());
		VCard vcard = reader.readNext();

		List<LuckyNumType> luckyNumTypes = vcard.getProperties(LuckyNumType.class);
		assertEquals(1, luckyNumTypes.size());
		assertEquals(24, luckyNumTypes.get(0).luckyNum);

		assertWarnings(1, reader.getWarnings());
		assertNull(reader.readNext());
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

		assertWarnings(1, reader.getWarnings());
		assertNull(reader.readNext());
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
		assertEquals(VCardVersion.V2_1, vcard.getVersion()); //default to 2.1

		assertWarnings(1, reader.getWarnings());
		assertNull(reader.readNext());
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
		assertEquals(VCardVersion.V3_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertNull(vcard.getProdId());

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void androidVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_ANDROID.vcf"));

		{
			VCard vcard = reader.readNext();
			assertEquals(VCardVersion.V2_1, vcard.getVersion());

			Email email = vcard.getEmails().get(0);
			assertEquals("john.doe@company.com", email.getValue());
			assertSetEquals(email.getTypes(), EmailType.PREF);

			assertEquals(Arrays.asList("My Contacts"), vcard.getCategories().getValues());

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).run();
			assertWarnings(0, reader.getWarnings());
		}

		{
			VCard vcard = reader.readNext();
			assertEquals(VCardVersion.V2_1, vcard.getVersion());

			Email email = vcard.getEmails().get(0);
			assertEquals("jane.doe@company.com", email.getValue());
			assertSetEquals(email.getTypes(), EmailType.PREF);

			assertEquals(Arrays.asList("My Contacts"), vcard.getCategories().getValues());

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).run();
			assertWarnings(0, reader.getWarnings());
		}

		{
			VCard vcard = reader.readNext();
			assertEquals(VCardVersion.V2_1, vcard.getVersion());

			StructuredName n = vcard.getStructuredName();
			assertEquals("\u00d1 \u00d1 \u00d1 \u00d1", n.getFamily());
			assertNull(n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertTrue(n.getPrefixes().isEmpty());
			assertTrue(n.getSuffixes().isEmpty());

			assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ", vcard.getFormattedName().getValue());

			Telephone tel = vcard.getTelephoneNumbers().get(0);
			assertSetEquals(tel.getTypes(), TelephoneType.CELL, TelephoneType.PREF);
			assertEquals("123456789", tel.getText());

			assertEquals(Arrays.asList("My Contacts"), vcard.getCategories().getValues());

			assertValidate(vcard).versions(vcard.getVersion()).run();
			assertWarnings(0, reader.getWarnings());
		}

		{
			VCard vcard = reader.readNext();
			assertEquals(VCardVersion.V2_1, vcard.getVersion());

			StructuredName n = vcard.getStructuredName();
			assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1", n.getFamily());
			assertNull(n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertTrue(n.getPrefixes().isEmpty());
			assertTrue(n.getSuffixes().isEmpty());

			assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1", vcard.getFormattedName().getValue());

			{
				Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

				Telephone tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.CELL, TelephoneType.PREF);
				assertEquals("123456", tel.getText());

				tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.HOME);
				assertEquals("234567", tel.getText());

				tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.CELL);
				assertEquals("3456789", tel.getText());

				tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.HOME);
				assertEquals("45678901", tel.getText());

				assertFalse(it.hasNext());
			}

			assertEquals(Arrays.asList("My Contacts"), vcard.getCategories().getValues());

			{
				Iterator<Note> it = vcard.getNotes().iterator();
				assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ", it.next().getValue());
				assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1 \u00d1\u00d1 \u00d1 \u00d1 \u00d1 \u00d1 ", it.next().getValue());
				assertFalse(it.hasNext());
			}

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).run();
			assertWarnings(0, reader.getWarnings());
		}

		{
			VCard vcard = reader.readNext();
			assertEquals(VCardVersion.V2_1, vcard.getVersion());

			StructuredName n = vcard.getStructuredName();
			assertEquals("\u00d1 \u00d1", n.getFamily());
			assertEquals("\u00d1 \u00d1 \u00d1", n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertTrue(n.getPrefixes().isEmpty());
			assertTrue(n.getSuffixes().isEmpty());

			assertEquals("\u00d1 \u00d1 \u00d1 \u00d1 ", vcard.getFormattedName().getValue());

			{
				Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

				Telephone tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.CELL, TelephoneType.PREF);
				assertEquals("123456", tel.getText());

				tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.WORK);
				assertEquals("123456", tel.getText());

				tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.FAX);
				assertEquals("123456", tel.getText());

				assertFalse(it.hasNext());
			}

			{
				Iterator<Email> it = vcard.getEmails().iterator();

				Email email = it.next();
				assertSetEquals(email.getTypes(), EmailType.PREF, EmailType.WORK);
				assertEquals("bob@company.com", email.getValue());

				email = it.next();
				assertEquals("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1", email.getValue());

				assertFalse(it.hasNext());
			}

			{
				Iterator<Organization> it = vcard.getOrganizations().iterator();

				assertEquals(Arrays.asList("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1"), it.next().getValues());
				assertEquals(Arrays.asList("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1"), it.next().getValues());
				assertFalse(it.hasNext());
			}

			{
				Iterator<Url> it = vcard.getUrls().iterator();

				assertEquals("www.company.com", it.next().getValue());
				assertEquals("http://www.company.com", it.next().getValue());
				assertFalse(it.hasNext());
			}

			assertEquals(1, vcard.getPhotos().size());

			assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).run();
			assertWarnings(0, reader.getWarnings());
		}

		{
			VCard vcard = reader.readNext();
			assertEquals(VCardVersion.V2_1, vcard.getVersion());

			StructuredName n = vcard.getStructuredName();
			assertEquals("\u00d1\u00d1\u00d1\u00d1", n.getFamily());
			assertNull(n.getGiven());
			assertTrue(n.getAdditional().isEmpty());
			assertTrue(n.getPrefixes().isEmpty());
			assertTrue(n.getSuffixes().isEmpty());

			assertEquals("\u00d1\u00d1\u00d1\u00d1", vcard.getFormattedName().getValue());

			{
				Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

				Telephone tel = it.next();
				assertSetEquals(tel.getTypes(), TelephoneType.CELL, TelephoneType.PREF);
				assertEquals("55556666", tel.getText());

				assertFalse(it.hasNext());
			}

			{
				Iterator<Email> it = vcard.getEmails().iterator();

				Email email = it.next();
				assertSetEquals(email.getTypes(), EmailType.PREF);
				assertEquals("henry@company.com", email.getValue());

				assertFalse(it.hasNext());
			}

			{
				Iterator<Organization> it = vcard.getOrganizations().iterator();

				assertEquals(Arrays.asList("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1"), it.next().getValues());
				assertEquals(Arrays.asList("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1" + (char) 65533), it.next().getValues());
				assertEquals(Arrays.asList("\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1\u00d1"), it.next().getValues());
				assertFalse(it.hasNext());
			}

			assertEquals(Arrays.asList("My Contacts"), vcard.getCategories().getValues());

			assertValidate(vcard).versions(vcard.getVersion()).run();
			assertWarnings(0, reader.getWarnings());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void blackBerryVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_BLACK_BERRY.vcf"));

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V2_1, vcard.getVersion());

		assertEquals("John Doe", vcard.getFormattedName().getValue());

		StructuredName n = vcard.getStructuredName();
		assertEquals("Doe", n.getFamily());
		assertEquals("john", n.getGiven());
		assertEquals(Arrays.asList(), n.getAdditional());
		assertEquals(Arrays.asList(), n.getPrefixes());
		assertEquals(Arrays.asList(), n.getSuffixes());

		assertEquals(Arrays.asList("Acme Solutions"), vcard.getOrganization().getValues());

		Telephone tel = vcard.getTelephoneNumbers().get(0);
		assertSetEquals(tel.getTypes(), TelephoneType.CELL);
		assertEquals("+96123456789", tel.getText());

		assertEquals(1, vcard.getPhotos().size());

		assertEquals("", vcard.getNotes().get(0).getValue());

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void evolutionVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_EVOLUTION.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url t = it.next();
			assertEquals("http://www.ibm.com", t.getValue());
			assertEquals("0abc9b8d-0845-47d0-9a91-3db5bb74620d", t.getParameters().first("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

			Telephone t = it.next();
			assertEquals("905-666-1234", t.getText());
			assertSetEquals(t.getTypes(), TelephoneType.CELL);
			assertEquals("c2fa1caa-2926-4087-8971-609cfc7354ce", t.getParameters().first("X-COUCHDB-UUID"));

			t = it.next();
			assertEquals("905-555-1234", t.getText());
			assertSetEquals(t.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);
			assertEquals("fbfb2722-4fd8-4dbf-9abd-eeb24072fd8e", t.getParameters().first("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//UID
		{
			Uid t = vcard.getUid();
			assertEquals("477343c8e6bf375a9bac1f96a5000837", t.getValue());
		}

		//N
		{
			StructuredName t = vcard.getStructuredName();
			assertEquals("Doe", t.getFamily());
			assertEquals("John", t.getGiven());
			List<String> list = t.getAdditional();
			assertEquals(Arrays.asList("Richter, James"), list);
			list = t.getPrefixes();
			assertEquals(Arrays.asList("Mr."), list);
			list = t.getSuffixes();
			assertEquals(Arrays.asList("Sr."), list);
		}

		//FN
		{
			FormattedName t = vcard.getFormattedName();
			assertEquals("Mr. John Richter, James Doe Sr.", t.getValue());
		}

		//NICKNAME
		{
			Nickname t = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), t.getValues());
		}

		//ORG
		{
			Organization t = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting", "Dungeon"), t.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title t = it.next();
			assertEquals("Money Counter", t.getValue());

			assertFalse(it.hasNext());
		}

		//CATEGORIES
		{
			Categories t = vcard.getCategories();
			assertEquals(Arrays.asList("VIP"), t.getValues());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();
			Note t = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.", t.getValue());
			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email t = it.next();
			assertEquals("john.doe@ibm.com", t.getValue());
			assertSetEquals(t.getTypes(), EmailType.WORK);
			assertEquals("83a75a5d-2777-45aa-bab5-76a4bd972490", t.getParameters().first("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address t = it.next();
			assertEquals("ASB-123", t.getPoBox());
			assertEquals(null, t.getExtendedAddress());
			assertEquals("15 Crescent moon drive", t.getStreetAddress());
			assertEquals("Albaney", t.getLocality());
			assertEquals("New York", t.getRegion());
			assertEquals("12345", t.getPostalCode());
			//the space between "United" and "States" is lost because it was included with the folding character and ignored (see .vcf file)
			assertEquals("UnitedStates of America", t.getCountry());
			assertSetEquals(t.getTypes(), AddressType.HOME);

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday t = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 22);
			Date expected = c.getTime();
			assertEquals(expected, t.getDate());
		}

		//REV
		{
			Revision t = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 5);
			c.set(Calendar.HOUR_OF_DAY, 13);
			c.set(Calendar.MINUTE, 32);
			c.set(Calendar.SECOND, 54);
			assertEquals(c.getTime(), t.getValue());
		}

		//extended types
		{
			assertEquals(7, countExtTypes(vcard));

			Iterator<RawProperty> it = vcard.getExtendedProperties("X-COUCHDB-APPLICATION-ANNOTATIONS").iterator();
			RawProperty t = it.next();
			assertEquals("X-COUCHDB-APPLICATION-ANNOTATIONS", t.getPropertyName());
			assertEquals("{\"Evolution\":{\"revision\":\"2012-03-05T13:32:54Z\"}}", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedProperties("X-AIM").iterator();
			t = it.next();
			assertEquals("X-AIM", t.getPropertyName());
			assertEquals("johnny5@aol.com", t.getValue());
			assertEquals("HOME", t.getParameters().getType());
			assertEquals("cb9e11fc-bb97-4222-9cd8-99820c1de454", t.getParameters().first("X-COUCHDB-UUID"));
			assertFalse(it.hasNext());

			it = vcard.getExtendedProperties("X-EVOLUTION-FILE-AS").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-FILE-AS", t.getPropertyName());
			assertEquals("Doe\\, John", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedProperties("X-EVOLUTION-SPOUSE").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-SPOUSE", t.getPropertyName());
			assertEquals("Maria", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedProperties("X-EVOLUTION-MANAGER").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-MANAGER", t.getPropertyName());
			assertEquals("Big Blue", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedProperties("X-EVOLUTION-ASSISTANT").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-ASSISTANT", t.getPropertyName());
			assertEquals("Little Red", t.getValue());
			assertFalse(it.hasNext());

			it = vcard.getExtendedProperties("X-EVOLUTION-ANNIVERSARY").iterator();
			t = it.next();
			assertEquals("X-EVOLUTION-ANNIVERSARY", t.getPropertyName());
			assertEquals("1980-03-22", t.getValue());
			assertFalse(it.hasNext());
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void gmailVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_GMAIL.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Mr. John Richter, James Doe Sr.", f.getValue());
		}

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter, James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("john.doe@ibm.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET, EmailType.HOME);

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

			Telephone f = it.next();
			assertEquals("905-555-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL);

			f = it.next();
			assertEquals("905-666-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME);

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("Crescent moon drive" + NEWLINE + "555-asd" + NEWLINE + "Nice Area, Albaney, New York12345" + NEWLINE + "United States of America", f.getExtendedAddress());
			assertEquals(null, f.getStreetAddress());
			assertEquals(null, f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals(null, f.getPostalCode());
			assertEquals(null, f.getCountry());
			assertSetEquals(f.getTypes(), AddressType.HOME);

			assertFalse(it.hasNext());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 22);
			assertEquals(c.getTime(), f.getDate());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("http://www.ibm.com", f.getValue());
			assertEquals("WORK", f.getType());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue", f.getValue());

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(6, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-PHONETIC-FIRST-NAME").get(0);
			assertEquals("X-PHONETIC-FIRST-NAME", f.getPropertyName());
			assertEquals("Jon", f.getValue());

			f = vcard.getExtendedProperties("X-PHONETIC-LAST-NAME").get(0);
			assertEquals("X-PHONETIC-LAST-NAME", f.getPropertyName());
			assertEquals("Dow", f.getValue());

			f = vcard.getExtendedProperties("X-ABDATE").get(0);
			assertEquals("X-ABDATE", f.getPropertyName());
			assertEquals("1975-03-01", f.getValue());
			assertEquals("item1", f.getGroup());

			f = vcard.getExtendedProperties("X-ABLABEL").get(0);
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("_$!<Anniversary>!$_", f.getValue());
			assertEquals("item1", f.getGroup());

			f = vcard.getExtendedProperties("X-ABLABEL").get(1);
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("_$!<Spouse>!$_", f.getValue());
			assertEquals("item2", f.getGroup());

			f = vcard.getExtendedProperties("X-ABRELATEDNAMES").get(0);
			assertEquals("X-ABRELATEDNAMES", f.getPropertyName());
			assertEquals("Jenny", f.getValue());
			assertEquals("item2", f.getGroup());
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void gmailList() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("gmail-list.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Arnold Smith", f.getValue());
		}

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Smith", f.getFamily());
			assertEquals("Arnold", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("asmithk@gmail.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader.getWarnings());
		vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Chris Beatle", f.getValue());
		}

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Beatle", f.getFamily());
			assertEquals("Chris", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("chrisy55d@yahoo.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader.getWarnings());
		vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Doug White", f.getValue());
		}

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("White", f.getFamily());
			assertEquals("Doug", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("dwhite@gmail.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void gmailSingle() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("gmail-single.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Greg Dartmouth", f.getValue());
		}

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Dartmouth", f.getFamily());
			assertEquals("Greg", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("gdartmouth@hotmail.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

			Telephone f = it.next();
			assertEquals("555 555 1111", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL);

			f = it.next();
			assertEquals("item1", f.getGroup());
			assertEquals("555 555 2222", f.getText());
			assertSetEquals(f.getTypes());

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("123 Home St" + NEWLINE + "Home City, HM 12345", f.getStreetAddress());
			assertEquals(null, f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals(null, f.getPostalCode());
			assertEquals(null, f.getCountry());
			assertSetEquals(f.getTypes(), AddressType.HOME);

			f = it.next();
			assertEquals("item2", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("321 Custom St", f.getStreetAddress());
			assertEquals("Custom City", f.getLocality());
			assertEquals("TX", f.getRegion());
			assertEquals("98765", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			assertSetEquals(f.getTypes());

			assertFalse(it.hasNext());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("TheCompany"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("TheJobTitle", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1960);
			c.set(Calendar.MONTH, Calendar.SEPTEMBER);
			c.set(Calendar.DAY_OF_MONTH, 10);
			assertEquals(c.getTime(), f.getDate());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("http://TheProfile.com", f.getValue());
			assertNull(f.getType());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("This is GMail's note field." + NEWLINE + "It should be added as a NOTE type." + NEWLINE + "ACustomField: CustomField", f.getValue());

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(12, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-PHONETIC-FIRST-NAME").get(0);
			assertEquals("X-PHONETIC-FIRST-NAME", f.getPropertyName());
			assertEquals("Grregg", f.getValue());

			f = vcard.getExtendedProperties("X-PHONETIC-LAST-NAME").get(0);
			assertEquals("X-PHONETIC-LAST-NAME", f.getPropertyName());
			assertEquals("Dart-mowth", f.getValue());

			f = vcard.getExtendedProperties("X-ICQ").get(0);
			assertEquals("X-ICQ", f.getPropertyName());
			assertEquals("123456789", f.getValue());

			Iterator<RawProperty> abLabelIt = vcard.getExtendedProperties("X-ABLABEL").iterator();
			{
				f = abLabelIt.next();
				assertEquals("item1", f.getGroup());
				assertEquals("GRAND_CENTRAL", f.getValue());

				f = abLabelIt.next();
				assertEquals("item2", f.getGroup());
				assertEquals("CustomAdrType", f.getValue());

				f = abLabelIt.next();
				assertEquals("item3", f.getGroup());
				assertEquals("PROFILE", f.getValue());

				f = abLabelIt.next();
				assertEquals("item4", f.getGroup());
				assertEquals("_$!<Anniversary>!$_", f.getValue());

				f = abLabelIt.next();
				assertEquals("item5", f.getGroup());
				assertEquals("_$!<Spouse>!$_", f.getValue());

				f = abLabelIt.next();
				assertEquals("item6", f.getGroup());
				assertEquals("CustomRelationship", f.getValue());

				assertFalse(abLabelIt.hasNext());
			}

			f = vcard.getExtendedProperties("X-ABDATE").get(0);
			assertEquals("item4", f.getGroup());
			assertEquals("X-ABDATE", f.getPropertyName());
			assertEquals("1970-06-02", f.getValue());

			f = vcard.getExtendedProperties("X-ABRELATEDNAMES").get(0);
			assertEquals("item5", f.getGroup());
			assertEquals("X-ABRELATEDNAMES", f.getPropertyName());
			assertEquals("MySpouse", f.getValue());

			f = vcard.getExtendedProperties("X-ABRELATEDNAMES").get(1);
			assertEquals("item6", f.getGroup());
			assertEquals("X-ABRELATEDNAMES", f.getPropertyName());
			assertEquals("MyCustom", f.getValue());
		}

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void iPhoneVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_IPHONE.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//PRODID
		{
			ProductId f = vcard.getProdId();
			assertEquals("-//Apple Inc.//iOS 5.0.1//EN", f.getValue());
		}

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter", "James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Mr. John Richter James Doe Sr.", f.getValue());
		}

		//NICKNAME
		{
			Nickname f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), f.getValues());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("item1", f.getGroup());
			assertEquals("john.doe@ibm.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET, EmailType.PREF);

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

			Telephone f = it.next();
			assertEquals("905-555-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL, TelephoneType.VOICE, TelephoneType.PREF);

			f = it.next();
			assertEquals("905-666-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME, TelephoneType.VOICE);

			f = it.next();
			assertEquals("905-777-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

			f = it.next();
			assertEquals("905-888-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME, TelephoneType.FAX);

			f = it.next();
			assertEquals("905-999-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.FAX);

			f = it.next();
			assertEquals("905-111-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.PAGER);

			f = it.next();
			assertEquals("905-222-1234", f.getText());
			assertEquals("item2", f.getGroup());
			assertSetEquals(f.getTypes());

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals("item3", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Silicon Alley 5", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertSetEquals(f.getTypes(), AddressType.HOME, AddressType.PREF);

			f = it.next();
			assertEquals("item4", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Street4" + NEWLINE + "Building 6" + NEWLINE + "Floor 8", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("USA", f.getCountry());

			assertSetEquals(f.getTypes(), AddressType.WORK);

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("item5", f.getGroup());
			assertEquals("http://www.ibm.com", f.getValue());
			assertEquals("pref", f.getType());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 6);
			assertEquals(c.getTime(), f.getDate());
		}

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(32531, f.getData().length);
		}

		//extended types
		{
			assertEquals(4, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-ABLABEL").get(0);
			assertEquals("item2", f.getGroup());
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("_$!<AssistantPhone>!$_", f.getValue());

			f = vcard.getExtendedProperties("X-ABADR").get(0);
			assertEquals("item3", f.getGroup());
			assertEquals("X-ABADR", f.getPropertyName());
			assertEquals("Silicon Alley", f.getValue());

			f = vcard.getExtendedProperties("X-ABADR").get(1);
			assertEquals("item4", f.getGroup());
			assertEquals("X-ABADR", f.getPropertyName());
			assertEquals("Street 4, Building 6,\\n Floor 8\\nNew York\\nUSA", f.getValue());

			f = vcard.getExtendedProperties("X-ABLABEL").get(1);
			assertEquals("item5", f.getGroup());
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("_$!<HomePage>!$_", f.getValue());
		}

		assertValidate(vcard).versions(vcard.getVersion()).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void lotusNotesVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_LOTUS_NOTES.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//PRODID
		{
			ProductId f = vcard.getProdId();
			assertEquals("-//Apple Inc.//Address Book 6.1//EN", f.getValue());
		}

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Johny"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("I"), f.getSuffixes());
		}

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Mr. Doe John I Johny", f.getValue());
		}

		//NICKNAME
		{
			Nickname f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny,JayJay"), f.getValues());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "SUN"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("Generic Accountant", f.getValue());

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("john.doe@ibm.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET, EmailType.WORK, EmailType.PREF);

			f = it.next();
			assertEquals("billy_bob@gmail.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET, EmailType.WORK);

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();
			Telephone f = it.next();
			assertEquals("+1 (212) 204-34456", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL, TelephoneType.VOICE, TelephoneType.PREF);

			f = it.next();
			assertEquals("00-1-212-555-7777", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.FAX);

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals("item1", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("25334" + NEWLINE + "South cresent drive, Building 5, 3rd floo r", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("NYC887", f.getPostalCode());
			assertEquals("U.S.A.", f.getCountry());
			assertNull(f.getLabel());
			assertSetEquals(f.getTypes(), AddressType.HOME, AddressType.PREF);

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"" + NEWLINE + "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO , THE" + NEWLINE + "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR P URPOSE" + NEWLINE + "ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTOR S BE" + NEWLINE + "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR" + NEWLINE + "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF" + NEWLINE + " SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS " + NEWLINE + "INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN" + NEWLINE + " CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)" + NEWLINE + "A RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE" + NEWLINE + " POSSIBILITY OF SUCH DAMAGE.", f.getValue());

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("item2", f.getGroup());
			assertEquals("http://www.sun.com", f.getValue());
			assertEquals("pref", f.getType());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MAY);
			c.set(Calendar.DAY_OF_MONTH, 21);
			assertEquals(c.getTime(), f.getDate());
		}

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(7957, f.getData().length);

			assertFalse(it.hasNext());
		}

		//UID
		{
			Uid f = vcard.getUid();
			assertEquals("0e7602cc-443e-4b82-b4b1-90f62f99a199", f.getValue());
		}

		//GEO
		{
			Geo f = vcard.getGeo();
			assertEquals(-2.6, f.getLatitude(), .01);
			assertEquals(3.4, f.getLongitude(), .01);
		}

		//CLASS
		{
			Classification f = vcard.getClassification();
			assertEquals("Public", f.getValue());
		}

		//PROFILE
		{
			Profile f = vcard.getProfile();
			assertEquals("VCard", f.getValue());
		}

		//TZ
		{
			Timezone f = vcard.getTimezone();
			assertIntEquals(1, f.getHourOffset());
			assertIntEquals(0, f.getMinuteOffset());
		}

		//LABEL
		{
			Iterator<Label> it = vcard.getOrphanedLabels().iterator();

			Label f = it.next();
			assertEquals("John Doe" + NEWLINE + "New York, NewYork," + NEWLINE + "South Crecent Drive," + NEWLINE + "Building 5, floor 3," + NEWLINE + "USA", f.getValue());
			assertSetEquals(f.getTypes(), AddressType.HOME, AddressType.PARCEL, AddressType.PREF);

			assertFalse(it.hasNext());
		}

		//SORT-STRING
		{
			SortString f = vcard.getSortString();
			assertEquals("JOHN", f.getValue());
		}

		//ROLE
		{
			Iterator<Role> it = vcard.getRoles().iterator();

			Role f = it.next();
			assertEquals("Counting Money", f.getValue());

			assertFalse(it.hasNext());
		}

		//SOURCE
		{
			Iterator<Source> it = vcard.getSources().iterator();

			Source f = it.next();
			assertEquals("Whatever", f.getValue());

			assertFalse(it.hasNext());
		}

		//MAILER
		{
			Mailer f = vcard.getMailer();
			assertEquals("Mozilla Thunderbird", f.getValue());
		}

		//NAME
		{
			SourceDisplayText f = vcard.getSourceDisplayText();
			assertEquals("VCard for John Doe", f.getValue());
		}

		//extended types
		{
			assertEquals(4, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-ABLABEL").get(0);
			assertEquals("item2", f.getGroup());
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("_$!<HomePage>!$_", f.getValue());

			f = vcard.getExtendedProperties("X-ABUID").get(0);
			assertEquals("X-ABUID", f.getPropertyName());
			assertEquals("0E7602CC-443E-4B82-B4B1-90F62F99A199:ABPerson", f.getValue());

			f = vcard.getExtendedProperties("X-GENERATOR").get(0);
			assertEquals("X-GENERATOR", f.getPropertyName());
			assertEquals("Cardme Generator", f.getValue());

			f = vcard.getExtendedProperties("X-LONG-STRING").get(0);
			assertEquals("X-LONG-STRING", f.getPropertyName());
			assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", f.getValue());
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).prop(vcard.getEmails().get(1), 9).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void msOutlookVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_MS_OUTLOOK.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V2_1, vcard.getVersion());

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("en-us", f.getParameters().getLanguage());
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter", "James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Mr. John Richter James Doe Sr.", f.getValue());
		}

		//NICKNAME
		{
			Nickname f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), f.getValues());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.", f.getValue());

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();
			Telephone f = it.next();

			assertEquals("(905) 555-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

			f = it.next();
			assertEquals("(905) 666-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME, TelephoneType.VOICE);

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Cresent moon drive", f.getStreetAddress());
			assertEquals("Albaney", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertEquals("Cresent moon drive\r\nAlbaney, New York  12345", f.getLabel());
			assertSetEquals(f.getTypes(), AddressType.WORK, AddressType.PREF);

			f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Silicon Alley 5", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertEquals("Silicon Alley 5,\r\nNew York, New York  12345", f.getLabel());
			assertSetEquals(f.getTypes(), AddressType.HOME);

			assertFalse(it.hasNext());
		}

		//LABEL
		{
			assertTrue(vcard.getOrphanedLabels().isEmpty());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("http://www.ibm.com", f.getValue());
			assertEquals("WORK", f.getType());

			assertFalse(it.hasNext());
		}

		//ROLE
		{
			Iterator<Role> it = vcard.getRoles().iterator();

			Role f = it.next();
			assertEquals("Counting Money", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 22);
			assertEquals(c.getTime(), f.getDate());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("john.doe@ibm.cm", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.PREF, EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(860, f.getData().length);

			assertFalse(it.hasNext());
		}

		//REV
		{
			Revision f = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 5);
			c.set(Calendar.HOUR_OF_DAY, 13);
			c.set(Calendar.MINUTE, 19);
			c.set(Calendar.SECOND, 33);
			assertEquals(c.getTime(), f.getValue());
		}

		//extended types
		{
			assertEquals(6, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-MS-OL-DEFAULT-POSTAL-ADDRESS").get(0);
			assertEquals("X-MS-OL-DEFAULT-POSTAL-ADDRESS", f.getPropertyName());
			assertEquals("2", f.getValue());

			f = vcard.getExtendedProperties("X-MS-ANNIVERSARY").get(0);
			assertEquals("X-MS-ANNIVERSARY", f.getPropertyName());
			assertEquals("20110113", f.getValue());

			f = vcard.getExtendedProperties("X-MS-IMADDRESS").get(0);
			assertEquals("X-MS-IMADDRESS", f.getPropertyName());
			assertEquals("johny5@aol.com", f.getValue());

			f = vcard.getExtendedProperties("X-MS-OL-DESIGN").get(0);
			assertEquals("X-MS-OL-DESIGN", f.getPropertyName());
			assertEquals("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>", f.getValue());
			assertEquals("utf-8", f.getParameters().getCharset());

			f = vcard.getExtendedProperties("X-MS-MANAGER").get(0);
			assertEquals("X-MS-MANAGER", f.getPropertyName());
			assertEquals("Big Blue", f.getValue());

			f = vcard.getExtendedProperties("X-MS-ASSISTANT").get(0);
			assertEquals("X-MS-ASSISTANT", f.getPropertyName());
			assertEquals("Jenny", f.getValue());
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getNickname(), 2).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void outlook2007VCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("outlook-2007.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V2_1, vcard.getVersion());

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("en-us", f.getParameters().getLanguage());
			assertEquals("Angstadt", f.getFamily());
			assertEquals("Michael", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Jr."), f.getSuffixes());
		}

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Mr. Michael Angstadt Jr.", f.getValue());
		}

		//NICKNAME
		{
			Nickname f = vcard.getNickname();
			assertEquals(Arrays.asList("Mike"), f.getValues());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("TheCompany", "TheDepartment"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("TheJobTitle", f.getValue());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("This is the NOTE field	\r\nI assume it encodes this text inside a NOTE vCard type.\r\nBut I'm not sure because there's text formatting going on here.\r\nIt does not preserve the formatting", f.getValue());
			assertEquals("us-ascii", f.getParameters().getCharset());

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();
			Telephone f = it.next();

			assertEquals("(111) 555-1111", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

			f = it.next();
			assertEquals("(111) 555-2222", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME, TelephoneType.VOICE);

			f = it.next();
			assertEquals("(111) 555-4444", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL, TelephoneType.VOICE);

			f = it.next();
			assertEquals("(111) 555-3333", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.FAX, TelephoneType.WORK);

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("TheOffice", f.getExtendedAddress());
			assertEquals("222 Broadway", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("NY", f.getRegion());
			assertEquals("99999", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			assertEquals("222 Broadway\r\nNew York, NY 99999\r\nUSA", f.getLabel());
			assertSetEquals(f.getTypes(), AddressType.WORK, AddressType.PREF);

			assertFalse(it.hasNext());
		}

		//LABEL
		{
			assertTrue(vcard.getOrphanedLabels().isEmpty());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("http://mikeangstadt.name", f.getValue());
			assertEquals("HOME", f.getType());

			f = it.next();
			assertEquals("http://mikeangstadt.name", f.getValue());
			assertEquals("WORK", f.getType());

			assertFalse(it.hasNext());
		}

		//ROLE
		{
			Iterator<Role> it = vcard.getRoles().iterator();

			Role f = it.next();
			assertEquals("TheProfession", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1922);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 10);
			assertEquals(c.getTime(), f.getDate());
		}

		//KEY
		{
			Iterator<Key> it = vcard.getKeys().iterator();

			Key f = it.next();
			assertEquals(KeyType.X509, f.getContentType());
			assertEquals(514, f.getData().length);

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("mike.angstadt@gmail.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.PREF, EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(2324, f.getData().length);

			assertFalse(it.hasNext());
		}

		//FBURL
		{
			//a 4.0 property in a 2.1 vCard...
			Iterator<FreeBusyUrl> it = vcard.getFbUrls().iterator();

			FreeBusyUrl f = it.next();
			assertEquals("http://website.com/mycal", f.getValue());

			assertFalse(it.hasNext());
		}

		//REV
		{
			Revision f = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.AUGUST);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, 18);
			c.set(Calendar.MINUTE, 46);
			c.set(Calendar.SECOND, 31);
			assertEquals(c.getTime(), f.getValue());
		}

		//extended types
		{
			assertEquals(8, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-MS-TEL").get(0);
			assertEquals("X-MS-TEL", f.getPropertyName());
			assertEquals("(111) 555-4444", f.getValue());
			assertSetEquals(f.getParameters().getTypes(), "VOICE", "CALLBACK");

			f = vcard.getExtendedProperties("X-MS-OL-DEFAULT-POSTAL-ADDRESS").get(0);
			assertEquals("X-MS-OL-DEFAULT-POSTAL-ADDRESS", f.getPropertyName());
			assertEquals("2", f.getValue());

			f = vcard.getExtendedProperties("X-MS-ANNIVERSARY").get(0);
			assertEquals("X-MS-ANNIVERSARY", f.getPropertyName());
			assertEquals("20120801", f.getValue());

			f = vcard.getExtendedProperties("X-MS-IMADDRESS").get(0);
			assertEquals("X-MS-IMADDRESS", f.getPropertyName());
			assertEquals("im@aim.com", f.getValue());

			f = vcard.getExtendedProperties("X-MS-OL-DESIGN").get(0);
			assertEquals("X-MS-OL-DESIGN", f.getPropertyName());
			assertEquals("<card xmlns=\"http://schemas.microsoft.com/office/outlook/12/electronicbusinesscards\" ver=\"1.0\" layout=\"left\" bgcolor=\"ffffff\"><img xmlns=\"\" align=\"tleft\" area=\"32\" use=\"photo\"/><fld xmlns=\"\" prop=\"name\" align=\"left\" dir=\"ltr\" style=\"b\" color=\"000000\" size=\"10\"/><fld xmlns=\"\" prop=\"org\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"title\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"dept\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"telwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Work</label></fld><fld xmlns=\"\" prop=\"telcell\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Mobile</label></fld><fld xmlns=\"\" prop=\"telhome\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"><label align=\"right\" color=\"626262\">Home</label></fld><fld xmlns=\"\" prop=\"email\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"addrwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"webwork\" align=\"left\" dir=\"ltr\" color=\"000000\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/><fld xmlns=\"\" prop=\"blank\" size=\"8\"/></card>", f.getValue());
			assertEquals("utf-8", f.getParameters().getCharset());

			f = vcard.getExtendedProperties("X-MS-MANAGER").get(0);
			assertEquals("X-MS-MANAGER", f.getPropertyName());
			assertEquals("TheManagerName", f.getValue());

			f = vcard.getExtendedProperties("X-MS-ASSISTANT").get(0);
			assertEquals("X-MS-ASSISTANT", f.getPropertyName());
			assertEquals("TheAssistantName", f.getValue());

			f = vcard.getExtendedProperties("X-MS-SPOUSE").get(0);
			assertEquals("X-MS-SPOUSE", f.getPropertyName());
			assertEquals("TheSpouse", f.getValue());
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getNickname(), 2).prop(vcard.getFbUrls().get(0), 2).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void macAddressBookVCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("John_Doe_MAC_ADDRESS_BOOK.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertEquals(Arrays.asList("Richter,James"), f.getAdditional());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("Sr."), f.getSuffixes());
		}

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("Mr. John Richter,James Doe Sr.", f.getValue());
		}

		//NICKNAME
		{
			Nickname f = vcard.getNickname();
			assertEquals(Arrays.asList("Johny"), f.getValues());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("IBM", "Accounting"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("Money Counter", f.getValue());

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("john.doe@ibm.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET, EmailType.WORK, EmailType.PREF);

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

			Telephone f = it.next();
			assertEquals("905-777-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.PREF);

			f = it.next();
			assertEquals("905-666-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME);

			f = it.next();
			assertEquals("905-555-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL);

			f = it.next();
			assertEquals("905-888-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME, TelephoneType.FAX);

			f = it.next();
			assertEquals("905-999-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.FAX);

			f = it.next();
			assertEquals("905-111-1234", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.PAGER);

			f = it.next();
			assertEquals("905-222-1234", f.getText());
			assertEquals("item1", f.getGroup());
			assertSetEquals(f.getTypes());

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals("item2", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Silicon Alley 5,", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("New York", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertSetEquals(f.getTypes(), AddressType.HOME, AddressType.PREF);

			f = it.next();
			assertEquals("item3", f.getGroup());
			assertEquals(null, f.getPoBox());
			assertEquals(null, f.getExtendedAddress());
			assertEquals("Street4" + NEWLINE + "Building 6" + NEWLINE + "Floor 8", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals(null, f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			assertSetEquals(f.getTypes(), AddressType.WORK);

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE." + NEWLINE + "Favotire Color: Blue", f.getValue());

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("item4", f.getGroup());
			assertEquals("http://www.ibm.com", f.getValue());
			assertEquals("pref", f.getType());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 6);
			assertEquals(c.getTime(), f.getDate());
		}

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(null, f.getContentType());
			assertEquals(18242, f.getData().length);

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(9, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-PHONETIC-FIRST-NAME").get(0);
			assertEquals("X-PHONETIC-FIRST-NAME", f.getPropertyName());
			assertEquals("Jon", f.getValue());

			f = vcard.getExtendedProperties("X-PHONETIC-LAST-NAME").get(0);
			assertEquals("X-PHONETIC-LAST-NAME", f.getPropertyName());
			assertEquals("Dow", f.getValue());

			f = vcard.getExtendedProperties("X-ABLABEL").get(0);
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("AssistantPhone", f.getValue());
			assertEquals("item1", f.getGroup());

			f = vcard.getExtendedProperties("X-ABADR").get(0);
			assertEquals("X-ABADR", f.getPropertyName());
			assertEquals("Silicon Alley", f.getValue());
			assertEquals("item2", f.getGroup());

			f = vcard.getExtendedProperties("X-ABADR").get(1);
			assertEquals("X-ABADR", f.getPropertyName());
			assertEquals("Street 4, Building 6,\\nFloor 8\\nNew York\\nUSA", f.getValue());
			assertEquals("item3", f.getGroup());

			f = vcard.getExtendedProperties("X-ABLABEL").get(1);
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("_$!<HomePage>!$_", f.getValue());
			assertEquals("item4", f.getGroup());

			f = vcard.getExtendedProperties("X-ABRELATEDNAMES").get(0);
			assertEquals("X-ABRELATEDNAMES", f.getPropertyName());
			assertEquals("Jenny", f.getValue());
			assertEquals("item5", f.getGroup());
			assertSetEquals(f.getParameters().getTypes(), "pref");

			f = vcard.getExtendedProperties("X-ABLABEL").get(2);
			assertEquals("X-ABLabel", f.getPropertyName());
			assertEquals("Spouse", f.getValue());
			assertEquals("item5", f.getGroup());

			f = vcard.getExtendedProperties("X-ABUID").get(0);
			assertEquals("X-ABUID", f.getPropertyName());
			assertEquals("6B29A774-D124-4822-B8D0-2780EC117F60\\:ABPerson", f.getValue());
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getEmails().get(0), 9).prop(vcard.getPhotos().get(0), 4).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void outlook2003VCard() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("outlook-2003.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V2_1, vcard.getVersion());

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertEquals(Arrays.asList("Mr."), f.getPrefixes());
			assertEquals(Arrays.asList("III"), f.getSuffixes());
		}

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("John Doe III", f.getValue());
		}

		//NICKNAME
		{
			Nickname f = vcard.getNickname();
			assertEquals(Arrays.asList("Joey"), f.getValues());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("Company, The", "TheDepartment"), f.getValues());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("The Job Title", f.getValue());

			assertFalse(it.hasNext());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("This is the note field!!\r\nSecond line\r\n\r\nThird line is empty\r\n", f.getValue());

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();
			Telephone f = it.next();

			assertEquals("BusinessPhone", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

			f = it.next();
			assertEquals("HomePhone", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME, TelephoneType.VOICE);

			f = it.next();
			assertEquals("MobilePhone", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL, TelephoneType.VOICE);

			f = it.next();
			assertEquals("BusinessFaxPhone", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.FAX, TelephoneType.WORK);

			assertFalse(it.hasNext());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("TheOffice", f.getExtendedAddress());
			assertEquals("123 Main St", f.getStreetAddress());
			assertEquals("Austin", f.getLocality());
			assertEquals("TX", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("United States of America", f.getCountry());
			assertEquals("TheOffice\r\n123 Main St\r\nAustin, TX 12345\r\nUnited States of America", f.getLabel());
			assertSetEquals(f.getTypes(), AddressType.WORK);

			assertFalse(it.hasNext());
		}

		//LABEL
		{
			assertTrue(vcard.getOrphanedLabels().isEmpty());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("http://web-page-address.com", f.getValue());
			assertEquals("WORK", f.getType());

			assertFalse(it.hasNext());
		}

		//ROLE
		{
			Iterator<Role> it = vcard.getRoles().iterator();

			Role f = it.next();
			assertEquals("TheProfession", f.getValue());

			assertFalse(it.hasNext());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1980);
			c.set(Calendar.MONTH, Calendar.MARCH);
			c.set(Calendar.DAY_OF_MONTH, 21);
			assertEquals(c.getTime(), f.getDate());
		}

		//KEY
		{
			Iterator<Key> it = vcard.getKeys().iterator();

			Key f = it.next();
			assertEquals(KeyType.X509, f.getContentType());
			assertEquals(805, f.getData().length);

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("jdoe@hotmail.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.PREF, EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		//FBURL
		{
			Iterator<FreeBusyUrl> it = vcard.getFbUrls().iterator();

			//Outlook 2003 apparently doesn't output FBURL correctly:
			//http://help.lockergnome.com/office/BUG-Outlook-2003-exports-FBURL-vCard-incorrectly--ftopict423660.html
			FreeBusyUrl f = it.next();
			assertEquals("????????????????s????????????" + (char) 12, f.getValue());

			assertFalse(it.hasNext());
		}

		//REV
		{
			Revision f = vcard.getRevision();
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.OCTOBER);
			c.set(Calendar.DAY_OF_MONTH, 12);
			c.set(Calendar.HOUR_OF_DAY, 21);
			c.set(Calendar.MINUTE, 5);
			c.set(Calendar.SECOND, 25);
			assertEquals(c.getTime(), f.getValue());
		}

		assertValidate(vcard).versions(vcard.getVersion()).prop(vcard.getNickname(), 2).prop(vcard.getFbUrls().get(0), 2).run();
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void thunderbird() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("thunderbird-MoreFunctionsForAddressBook-extension.vcf"));
		VCard vcard = reader.readNext();

		//VERSION
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//N
		{
			StructuredName f = vcard.getStructuredName();
			assertEquals("Doe", f.getFamily());
			assertEquals("John", f.getGiven());
			assertTrue(f.getAdditional().isEmpty());
			assertTrue(f.getPrefixes().isEmpty());
			assertTrue(f.getSuffixes().isEmpty());
		}

		//FN
		{
			FormattedName f = vcard.getFormattedName();
			assertEquals("John Doe", f.getValue());
		}

		//ORG
		{
			Organization f = vcard.getOrganization();
			assertEquals(Arrays.asList("TheOrganization", "TheDepartment"), f.getValues());
		}

		//NICKNAME
		{
			Nickname f = vcard.getNickname();
			assertEquals(Arrays.asList("Johnny"), f.getValues());
		}

		//ADR
		{
			Iterator<Address> it = vcard.getAddresses().iterator();

			Address f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("222 Broadway", f.getExtendedAddress());
			assertEquals("Suite 100", f.getStreetAddress());
			assertEquals("New York", f.getLocality());
			assertEquals("NY", f.getRegion());
			assertEquals("98765", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			assertSetEquals(f.getTypes(), AddressType.WORK, AddressType.POSTAL);

			f = it.next();
			assertEquals(null, f.getPoBox());
			assertEquals("123 Main St", f.getExtendedAddress());
			assertEquals("Apt 10", f.getStreetAddress());
			assertEquals("Austin", f.getLocality());
			assertEquals("TX", f.getRegion());
			assertEquals("12345", f.getPostalCode());
			assertEquals("USA", f.getCountry());
			assertSetEquals(f.getTypes(), AddressType.HOME, AddressType.POSTAL);

			assertFalse(it.hasNext());
		}

		//TEL
		{
			Iterator<Telephone> it = vcard.getTelephoneNumbers().iterator();

			Telephone f = it.next();
			assertEquals("555-555-1111", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.WORK, TelephoneType.VOICE);

			f = it.next();
			assertEquals("555-555-2222", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.HOME, TelephoneType.VOICE);

			f = it.next();
			assertEquals("555-555-5555", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.CELL, TelephoneType.VOICE);

			f = it.next();
			assertEquals("555-555-3333", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.FAX);

			f = it.next();
			assertEquals("555-555-4444", f.getText());
			assertSetEquals(f.getTypes(), TelephoneType.PAGER);

			assertFalse(it.hasNext());
		}

		//EMAIL
		{
			Iterator<Email> it = vcard.getEmails().iterator();

			Email f = it.next();
			assertEquals("doe.john@hotmail.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET, EmailType.PREF);

			f = it.next();
			assertEquals("additional-email@company.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			f = it.next();
			assertEquals("additional-email1@company.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			f = it.next();
			assertEquals("additional-email2@company.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			f = it.next();
			assertEquals("additional-email3@company.com", f.getValue());
			assertSetEquals(f.getTypes(), EmailType.INTERNET);

			assertFalse(it.hasNext());
		}

		//URL
		{
			Iterator<Url> it = vcard.getUrls().iterator();

			Url f = it.next();
			assertEquals("http://www.private-webpage.com", f.getValue());
			assertEquals("HOME", f.getType());

			f = it.next();
			assertEquals("http://www.work-webpage.com", f.getValue());
			assertEquals("WORK", f.getType());

			assertFalse(it.hasNext());
		}

		//TITLE
		{
			Iterator<Title> it = vcard.getTitles().iterator();

			Title f = it.next();
			assertEquals("TheTitle", f.getValue());

			assertFalse(it.hasNext());
		}

		//CATEGORIES
		{
			//commas are incorrectly escaped, so there is only 1 item
			Categories f = vcard.getCategories();
			assertEquals(Arrays.asList("category1, category2, category3"), f.getValues());
		}

		//BDAY
		{
			Birthday f = vcard.getBirthday();
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 1970);
			c.set(Calendar.MONTH, Calendar.SEPTEMBER);
			c.set(Calendar.DAY_OF_MONTH, 21);
			assertEquals(c.getTime(), f.getDate());
		}

		//NOTE
		{
			Iterator<Note> it = vcard.getNotes().iterator();

			Note f = it.next();
			assertEquals("This is the notes field." + NEWLINE + "Second Line" + NEWLINE + NEWLINE + "Fourth Line" + NEWLINE + "You can put anything in the \"note\" field; even curse words.", f.getValue());

			assertFalse(it.hasNext());
		}

		//PHOTO
		{
			Iterator<Photo> it = vcard.getPhotos().iterator();

			Photo f = it.next();
			assertEquals(ImageType.JPEG, f.getContentType());
			assertEquals(8940, f.getData().length);

			assertFalse(it.hasNext());
		}

		//extended types
		{
			assertEquals(2, countExtTypes(vcard));

			RawProperty f = vcard.getExtendedProperties("X-SPOUSE").get(0);
			assertEquals("X-SPOUSE", f.getPropertyName());
			assertEquals("TheSpouse", f.getValue());

			f = vcard.getExtendedProperties("X-ANNIVERSARY").get(0);
			assertEquals("X-ANNIVERSARY", f.getPropertyName());
			assertEquals("1990-04-30", f.getValue());
		}

		//@formatter:off
		assertValidate(vcard).versions(vcard.getVersion())
		.prop(vcard.getStructuredName(), 6)
		.prop(vcard.getFormattedName(), 6)
		.prop(vcard.getOrganization(), 6)
		.prop(vcard.getNickname(), 6)
		.prop(vcard.getAddresses().get(0), 6)
		.prop(vcard.getAddresses().get(1), 6)
		.prop(vcard.getTitles().get(0), 6)
		.prop(vcard.getCategories(), 6)
		.prop(vcard.getNotes().get(0), 6)
		.run();
		//@formatter:on
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void rfc6350_example() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("rfc6350-example.vcf"));
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

		PartialDate expectedBday = PartialDate.date(null, 2, 3);
		PartialDate actualBday = vcard.getBirthday().getPartialDate();
		assertEquals(expectedBday, actualBday);

		PartialDate expectedAnniversary = PartialDate.dateTime(2009, 8, 8, 14, 30, null, new UtcOffset(-5, 0));
		PartialDate actualAnniversary = vcard.getAnniversary().getPartialDate();
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
		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void rfc2426_example() throws Throwable {
		VCardReader reader = new VCardReader(getClass().getResourceAsStream("rfc2426-example.vcf"));

		{
			VCard vcard = reader.readNext();

			assertEquals(VCardVersion.V3_0, vcard.getVersion());
			assertEquals(8, vcard.getProperties().size());

			assertEquals("Frank Dawson", vcard.getFormattedName().getValue());

			assertEquals(Arrays.asList("Lotus Development Corporation"), vcard.getOrganization().getValues());

			Address adr = vcard.getAddresses().get(0);
			assertNull(adr.getPoBox());
			assertNull(adr.getExtendedAddress());
			assertEquals("6544 Battleford Drive", adr.getStreetAddress());
			assertEquals("Raleigh", adr.getLocality());
			assertEquals("NC", adr.getRegion());
			assertEquals("27613-3502", adr.getPostalCode());
			assertEquals("U.S.A.", adr.getCountry());
			assertSetEquals(adr.getTypes(), AddressType.WORK, AddressType.POSTAL, AddressType.PARCEL);

			Telephone tel = vcard.getTelephoneNumbers().get(0);
			assertEquals("+1-919-676-9515", tel.getText());
			assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE, TelephoneType.MSG);

			tel = vcard.getTelephoneNumbers().get(1);
			assertEquals("+1-919-676-9564", tel.getText());
			assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.FAX);

			Email email = vcard.getEmails().get(0);
			assertEquals("Frank_Dawson@Lotus.com", email.getValue());
			assertSetEquals(email.getTypes(), EmailType.INTERNET, EmailType.PREF);

			email = vcard.getEmails().get(1);
			assertEquals("fdawson@earthlink.net", email.getValue());
			assertSetEquals(email.getTypes(), EmailType.INTERNET);

			assertEquals("http://home.earthlink.net/~fdawson", vcard.getUrls().get(0).getValue());

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).run();
			assertWarnings(0, reader.getWarnings());
		}

		{
			VCard vcard = reader.readNext();

			assertEquals(VCardVersion.V3_0, vcard.getVersion());
			assertEquals(6, vcard.getProperties().size());

			assertEquals("Tim Howes", vcard.getFormattedName().getValue());

			assertEquals(Arrays.asList("Netscape Communications Corp."), vcard.getOrganization().getValues());

			Address adr = vcard.getAddresses().get(0);
			assertNull(adr.getPoBox());
			assertNull(adr.getExtendedAddress());
			assertEquals("501 E. Middlefield Rd.", adr.getStreetAddress());
			assertEquals("Mountain View", adr.getLocality());
			assertEquals("CA", adr.getRegion());
			assertEquals("94043", adr.getPostalCode());
			assertEquals("U.S.A.", adr.getCountry());
			assertSetEquals(adr.getTypes(), AddressType.WORK);

			Telephone tel = vcard.getTelephoneNumbers().get(0);
			assertEquals("+1-415-937-3419", tel.getText());
			assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.VOICE, TelephoneType.MSG);

			tel = vcard.getTelephoneNumbers().get(1);
			assertEquals("+1-415-528-4164", tel.getText());
			assertSetEquals(tel.getTypes(), TelephoneType.WORK, TelephoneType.FAX);

			Email email = vcard.getEmails().get(0);
			assertEquals("howes@netscape.com", email.getValue());
			assertSetEquals(email.getTypes(), EmailType.INTERNET);

			assertValidate(vcard).versions(vcard.getVersion()).prop(null, 0).run();
			assertWarnings(0, reader.getWarnings());
		}

		assertNull(reader.readNext());
	}

	/**
	 * Counts the number of extended types in a vCard.
	 * @param vcard the vCard
	 * @return the number of extended types
	 */
	private int countExtTypes(VCard vcard) {
		return vcard.getExtendedProperties().size();
	}
}
