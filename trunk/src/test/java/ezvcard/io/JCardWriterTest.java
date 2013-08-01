package ezvcard.io;

import static ezvcard.util.TestUtils.assertWarnings;
import static ezvcard.util.VCardStringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.types.KindType;
import ezvcard.types.MemberType;
import ezvcard.types.VCardType;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;

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
		assertWarnings(0, writer.getWarnings());
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
		assertWarnings(0, writer.getWarnings());
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
		assertWarnings(0, writer.getWarnings());
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
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void write_no_vcards() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.close();
		assertEquals("", sw.toString());
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void check_required_properties() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		writer.write(vcard);
		assertWarnings(1, writer.getWarnings()); //FN is required

		writer.close();
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
		assertWarnings(0, writer.getWarnings());

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
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addProperty(new TypeForTesting(JCardValue.single(JCardDataType.TEXT, "value")));
		writer.write(vcard);
		assertWarnings(0, writer.getWarnings());

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
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		LuckyNumType prop = new LuckyNumType();
		prop.luckyNum = 13;
		vcard.addProperty(prop);
		writer.write(vcard);
		assertWarnings(1, writer.getWarnings());

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
	public void unsupported_version() throws Throwable {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.setMailer("mailer");
		writer.write(vcard);
		assertWarnings(1, writer.getWarnings());

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
	public void kind_and_member_combination() throws Throwable {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addMember(new MemberType("http://uri.com"));

		//correct KIND
		{
			vcard.setKind(KindType.group());

			StringWriter sw = new StringWriter();
			JCardWriter writer = new JCardWriter(sw);
			writer.setAddProdId(false);
			writer.write(vcard);
			writer.close();

			//@formatter:off
			String expected =
			"[\"vcard\"," +
			  "[" +
			    "[\"version\",{},\"text\",\"4.0\"]," +
			    "[\"fn\",{},\"text\",\"John Doe\"]," +
			    "[\"member\",{},\"uri\",\"http://uri.com\"]," +
			    "[\"kind\",{},\"text\",\"group\"]" +
			  "]" +
			"]";
			//@formatter:on
			assertEquals(expected, sw.toString());
			assertWarnings(0, writer.getWarnings());
		}

		//wrong KIND
		{
			vcard.setKind(KindType.individual());

			StringWriter sw = new StringWriter();
			JCardWriter writer = new JCardWriter(sw);
			writer.setAddProdId(false);
			writer.write(vcard);
			writer.close();

			//@formatter:off
			String expected =
			"[\"vcard\"," +
			  "[" +
			    "[\"version\",{},\"text\",\"4.0\"]," +
			    "[\"fn\",{},\"text\",\"John Doe\"]," +
			    "[\"kind\",{},\"text\",\"individual\"]" +
			  "]" +
			"]";
			//@formatter:on
			assertEquals(expected, sw.toString());
			assertWarnings(1, writer.getWarnings());
		}
	}

	private static class TypeForTesting extends VCardType {
		public JCardValue value;

		public TypeForTesting(JCardValue value) {
			super("X-TYPE");
			this.value = value;
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
		protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
			return value;
		}
	}
}
