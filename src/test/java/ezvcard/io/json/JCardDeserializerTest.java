package ezvcard.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ezvcard.VCard;
import ezvcard.VCardVersion;

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
public class JCardDeserializerTest {
	//TODO test setScribe()

	private ObjectMapper mapper;

	@Before
	public void before() {
		mapper = new ObjectMapper();
	}

	@Test
	public void deserialize_single() throws Exception {
		//@formatter:off
		String json =
		"[\"vcard\"," +
			"[" +
				"[\"version\", {}, \"text\", \"4.0\"]," +
				"[\"fn\", {}, \"text\", \"John Doe\"]" +
			"]" +
		"]";
		//@formatter:on

		JCardModule module = new JCardModule();
		mapper.registerModule(module);

		VCard expected = new VCard();
		expected.setVersion(VCardVersion.V4_0);
		expected.setFormattedName("John Doe");

		VCard actual = mapper.readValue(json, VCard.class);
		assertEquals(expected, actual);
	}

	@Test
	public void deserialize_multiple() throws Exception {
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

		JCardModule module = new JCardModule();
		mapper.registerModule(module);

		List<VCard> expected = new ArrayList<VCard>();

		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V4_0);
		vcard.setFormattedName("John Doe");
		expected.add(vcard);

		vcard = new VCard();
		vcard.setVersion(VCardVersion.V4_0);
		vcard.setFormattedName("Jane Doe");
		expected.add(vcard);

		List<VCard> actual = mapper.readValue(json, new TypeReference<List<VCard>>() {
		});
		assertEquals(expected, actual);
	}

	@Test
	public void container() throws Exception {
		//@formatter:off
		String json =
		"{" +
			"\"contact\": [\"vcard\"," +
				"[" +
					"[\"version\", {}, \"text\", \"4.0\"]," +
					"[\"fn\", {}, \"text\", \"John Doe\"]" +
				"]" +
			"]" +
		"}";
		//@formatter:on

		Container container = mapper.readValue(json, Container.class);

		VCard expected = new VCard();
		expected.setVersion(VCardVersion.V4_0);
		expected.setFormattedName("John Doe");

		VCard actual = container.contact;
		assertEquals(expected, actual);
	}

	@Test
	public void container_null() throws Exception {
		//@formatter:off
		String json =
		"{" +
			"\"contact\": null" +
		"}";
		//@formatter:on

		Container container = mapper.readValue(json, Container.class);

		VCard actual = container.contact;
		assertNull(actual);
	}

	private static class Container {
		private VCard contact;

		@JsonDeserialize(using = JCardDeserializer.class)
		public void setContact(VCard contact) {
			this.contact = contact;
		}
	}
}
