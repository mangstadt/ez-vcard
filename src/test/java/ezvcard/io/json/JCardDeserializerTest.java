package ezvcard.io.json;

import static ezvcard.VCardVersion.V4_0;
import static ezvcard.property.asserter.PropertyAsserter.assertSimpleProperty;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertVersion;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 */
public class JCardDeserializerTest {
	@Test
	public void deserialize_single_vcard() throws Throwable {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JCardModule());
		VCard result = mapper.readValue(getClass().getResourceAsStream("jcard-example.json"), VCard.class);

		JCardReaderTest.validateExampleJCard(result);
	}

	@Test
	public void read_nested() throws Throwable {
		//@formatter:off
		String json =
		"{" +
		  "\"contact\": "+
		  "[" +
		    "\"vcard\"," +
			  "[" +
			    "[\"version\", {}, \"text\", \"4.0\"]," +
			    "[\"fn\", {}, \"text\", \"John Doe\"]" +
			  "]" +
		  "]" +
		"}";
		//@formatter:on

		NestedVCard nested = new ObjectMapper().readValue(json, NestedVCard.class);

		VCard vcard = nested.getContact();
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();
		//@formatter:on
	}

	@Test
	public void read_nested_null() throws Throwable {
		//@formatter:off
		String json =
		"{" +
		  "\"contact\": null" +
		"}";
		//@formatter:on

		NestedVCard nested = new ObjectMapper().readValue(json, NestedVCard.class);

		VCard vcard = nested.getContact();
		assertEquals(null, vcard);
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		String json =
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
		"]";
		//@formatter:on

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JCardModule());
		List<VCard> cards = mapper.readValue(json, new TypeReference<List<VCard>>() {
		});

		assertEquals(2, cards.size());

		VCard vcard = cards.get(0);
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();
		//@formatter:on

		vcard = cards.get(1);
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Jane Doe")
		.noMore();
		//@formatter:on
	}
}
