package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.types.FormattedNameType;
import ezvcard.types.VCardType;
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
	final String newline = System.getProperty("line.separator");

	@Test
	public void write_single_vcard() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"Simon Perreault\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_multiple_vcards() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"Simon Perreault\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"John Doe\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_group() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault").setGroup("TheGroup");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{\"group\":\"TheGroup\"},\"text\",\"Simon Perreault\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_parameters() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		FormattedNameType fn = vcard.setFormattedName("Simon Perreault");
		fn.getSubTypes().put("x-one", "1");
		fn.getSubTypes().put("x-two", "2");
		fn.getSubTypes().put("x-two", "22");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{\"x-one\":\"1\",\"x-two\":[\"2\",\"22\"]},\"text\",\"Simon Perreault\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void setAddProdid() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(true);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault");
		writer.write(vcard);

		writer.close();

		String regex = Pattern.quote("[\"prodid\",{},\"text\",") + "\".*?\"" + Pattern.quote("]");
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sw.toString());
		assertTrue(m.find());
		assertFalse(m.find());
	}

	@Test
	public void setIndent() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);
		writer.setIndent(true);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\",[" + newline +
		"  \"vcard\",[[" + newline +
		"    \"version\",{},\"text\",\"4.0\"],[" + newline +
		"    \"fn\",{},\"text\",\"Simon Perreault\"]" + newline +
		"  ]],[" + newline +
		"  \"vcard\",[[" + newline +
		"    \"version\",{},\"text\",\"4.0\"],[" + newline +
		"    \"fn\",{},\"text\",\"John Doe\"]" + newline +
		"  ]]" + newline +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_no_vcards() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.close();
		assertEquals("", sw.toString());
	}

	@Test
	public void check_required_properties() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		writer.write(vcard);
		assertEquals(1, writer.getWarnings().size()); //FN is required

		writer.close();
	}

	@Test
	public void write_null_value() throws Exception {
		JCardValue value = JCardValue.text((String) null);
		VCard vcard = new VCard();
		vcard.addExtendedType(new TypeForTesting(value));

		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"x-type\",{},\"text\",\"\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_null_structured_value() throws Exception {
		JCardValue value = JCardValue.text((String) null);
		value.setStructured(true);
		VCard vcard = new VCard();
		vcard.addExtendedType(new TypeForTesting(value));

		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"x-type\",{},\"text\",[\"\"]]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_no_value() throws Exception {
		JCardValue value = JCardValue.text();
		VCard vcard = new VCard();
		vcard.addExtendedType(new TypeForTesting(value));

		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"x-type\",{},\"text\",\"\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_empty_value() throws Exception {
		JCardValue value = JCardValue.text();
		value.addValues(new ArrayList<Object>(0));
		VCard vcard = new VCard();
		vcard.addExtendedType(new TypeForTesting(value));

		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);
		writer.write(vcard);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"x-type\",{},\"text\",\"\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
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
