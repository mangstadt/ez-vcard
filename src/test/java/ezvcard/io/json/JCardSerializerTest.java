package ezvcard.io.json;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ezvcard.Ezvcard;
import ezvcard.VCard;

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
 * @author Buddy Gorven
 * @author Michael Angstadt
 */
public class JCardSerializerTest {
	//TODO test setScribe()

	private ObjectMapper mapper;

	@Before
	public void before() {
		mapper = new ObjectMapper();
	}

	@Test
	public void serialize_single() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		JCardModule module = new JCardModule();
		module.setAddProdId(false);
		mapper.registerModule(module);

		String actual = mapper.writeValueAsString(vcard);

		//@formatter:off
		String expected =
		"[\"vcard\"," +
			"[" +
				"[\"version\",{},\"text\",\"4.0\"]," +
				"[\"fn\",{},\"text\",\"John Doe\"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void serialize_single_prettyPrint() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");

		JCardModule module = new JCardModule();
		module.setAddProdId(false);
		mapper.registerModule(module);
		mapper.setDefaultPrettyPrinter(new JCardPrettyPrinter());
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		String actual = mapper.writeValueAsString(vcard);

		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"  \"vcard\"," + NEWLINE +
		"  [" + NEWLINE +
		"    [ \"version\", { }, \"text\", \"4.0\" ]," + NEWLINE +
		"    [ \"fn\", { }, \"text\", \"John Doe\" ]" + NEWLINE +
		"  ]" + NEWLINE +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void serialize_multiple() throws Exception {
		List<VCard> vcards = new ArrayList<VCard>();
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcards.add(vcard);

		vcard = new VCard();
		vcard.setFormattedName("Jane Doe");
		vcards.add(vcard);

		JCardModule module = new JCardModule();
		module.setAddProdId(false);
		mapper.registerModule(module);

		String actual = mapper.writeValueAsString(vcards);

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
		assertEquals(expected, actual);
	}

	@Test
	public void serialize_multiple_prettyPrint() throws Exception {
		List<VCard> vcards = new ArrayList<VCard>();
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcards.add(vcard);

		vcard = new VCard();
		vcard.setFormattedName("Jane Doe");
		vcards.add(vcard);

		JCardModule module = new JCardModule();
		module.setAddProdId(false);
		mapper.registerModule(module);
		mapper.setDefaultPrettyPrinter(new JCardPrettyPrinter());
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		String actual = mapper.writeValueAsString(vcards);

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
		"      [ \"fn\", { }, \"text\", \"Jane Doe\" ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]" + NEWLINE +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void container() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.setMailer("mailer");
		Container container = new Container(vcard);

		StringWriter result = new StringWriter();
		mapper.writeValue(result, container);
		String actual = result.toString();

		//@formatter:off
		String expected =
		"{" +
			"\"contact\":[\"vcard\"," +
				"[" +
					"[\"version\",{},\"text\",\"4.0\"]," +
					"[\"prodid\",{},\"text\",\"ez-vcard " + Ezvcard.VERSION + "\"]," +
					"[\"fn\",{},\"text\",\"John Doe\"]" +
				"]" +
			"]" +
		"}";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void container_null() throws Exception {
		Container container = new Container(null);

		StringWriter result = new StringWriter();
		mapper.writeValue(result, container);
		String actual = result.toString();

		//@formatter:off
		String expected =
		"{" +
			"\"contact\":null"+
		"}";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void container_annotation() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.setMailer("mailer");
		ContainerAnnotation container = new ContainerAnnotation(vcard);

		StringWriter result = new StringWriter();
		mapper.writeValue(result, container);
		String actual = result.toString();

		//@formatter:off
		String expected =
		"{" +
			"\"contact\":[\"vcard\"," +
				"[" +
					"[\"version\",{},\"text\",\"4.0\"]," +
					"[\"fn\",{},\"text\",\"John Doe\"]," +
					"[\"mailer\",{},\"text\",\"mailer\"]" +
				"]" +
			"]" +
		"}";
		//@formatter:on
		assertEquals(expected, actual);
	}

	private static class Container {
		private final VCard contact;

		public Container(VCard contact) {
			this.contact = contact;
		}

		@JsonSerialize(using = JCardSerializer.class)
		public VCard getContact() {
			return contact;
		}
	}

	private static class ContainerAnnotation {
		private final VCard contact;

		public ContainerAnnotation(VCard contact) {
			this.contact = contact;
		}

		@JCardFormat(addProdId = false, versionStrict = false)
		@JsonSerialize(using = JCardSerializer.class)
		public VCard getContact() {
			return contact;
		}
	}
}
