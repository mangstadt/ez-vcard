package ezvcard.io.json;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.VCardProperty;

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
 */
public class JCardSerializerTest {
	@Test
	public void serialize_indented() throws Throwable {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName("John Doe");

		VCard vcard2 = new VCard();
		vcard2.setFormattedName("John Doe");

		ObjectMapper mapper = getMapper();

		final DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		mapper.setDefaultPrettyPrinter(pp);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		String result = mapper.writeValueAsString(Arrays.asList(vcard1, vcard2));

		//@formatter:off
		String expected =
		"[" + NEWLINE + 
		"  [" + NEWLINE + 
		"    \"vcard\"," + NEWLINE + 
		"    [" + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"version\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"4.0\"" + NEWLINE + 
		"      ]," + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"fn\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"John Doe\"" + NEWLINE + 
		"      ]" + NEWLINE + 
		"    ]" + NEWLINE + 
		"  ]," + NEWLINE + 
		"  [" + NEWLINE + 
		"    \"vcard\"," + NEWLINE + 
		"    [" + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"version\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"4.0\"" + NEWLINE + 
		"      ]," + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"fn\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"John Doe\"" + NEWLINE + 
		"      ]" + NEWLINE + 
		"    ]" + NEWLINE + 
		"  ]" + NEWLINE + 
		"]";
		//@formatter:on
		assertEquals(expected, result);
	}

	@Test
	public void serialize_single_vcard() throws Throwable {
		VCard example = JCardWriterTest.createExample();
		String actual = getMapper().writeValueAsString(example);
		JCardWriterTest.assertExample(actual, "jcard-example.json");
	}

	@Test
	public void check_context_set() throws Throwable {
		VCard example = JCardWriterTest.createExample();
		ObjectMapper mapper = getMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setDefaultPrettyPrinter(new MinimalPrettyPrinter() {
			private static final long serialVersionUID = 1L;

			private boolean find(JsonStreamContext context, Class<?>... classes) {
				if (context == null) {
					return false;
				} else {
					for (Class<?> type : classes) {
						if (type.isInstance(context.getCurrentValue())) {
							return true;
						}
					}
					return find(context.getParent(), classes);
				}
			}

			public void writeStartObject(JsonGenerator gen) throws JsonGenerationException, IOException {
				Assert.assertTrue("Written VCard parameter was not within a VCardProperty context",
						find(gen.getOutputContext(), VCardProperty.class, VCardVersion.class));
				super.writeStartObject(gen);
			}

			public void writeStartArray(JsonGenerator gen) throws JsonGenerationException, IOException {
				Assert.assertTrue("Written value was not within a VCard context",
						find(gen.getOutputContext(), VCard.class));
				super.writeStartArray(gen);
			}
		});
		mapper.writeValueAsString(example);
	}

	@Test
	public void serialize_nested() throws Throwable {
		NestedVCard nested = new NestedVCard();
		nested.setContact(JCardWriterTest.createExample());

		StringWriter result = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(result, nested);
		String nestedJson = result.toString();

		String actual = nestedJson.substring(nestedJson.indexOf('['), nestedJson.lastIndexOf(']') + 1);
		JCardWriterTest.assertExample(actual, "jcard-example.json");
	}

	@Test
	public void write_nested_null() throws Throwable {
		NestedVCard nested = new NestedVCard();

		StringWriter result = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(result, nested);
		String nestedJson = result.toString();

		assertEquals("{\"contact\":null}", nestedJson.replaceAll("\\s", ""));
	}

	@Test
	public void serialize_multiple_vcards() throws Throwable {
		List<VCard> cards = new ArrayList<VCard>();
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		cards.add(vcard);

		vcard = new VCard();
		vcard.setFormattedName("Jane Doe");
		cards.add(vcard);

		String actual = getMapper().writeValueAsString(cards);

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

	private ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		final JCardModule module = new JCardModule();
		module.setAddProdId(false);
		mapper.registerModule(module);
		return mapper;
	}
}
