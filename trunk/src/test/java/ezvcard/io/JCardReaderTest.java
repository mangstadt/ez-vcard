package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.NoteType;
import ezvcard.types.TelephoneType;
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
public class JCardReaderTest {
	@Test
	public void read_single() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void read_multiple() throws Exception {
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

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void ignore_other_json() throws Exception {
		//@formatter:off
		String json =
		"{" +
		  "\"website\": \"example.com\"," +
		  "\"vcard\": " +
		    "[\"vcard\"," +
		      "[" +
		        "[\"version\", {}, \"text\", \"4.0\"]," +
		        "[\"fn\", {}, \"text\", \"John Doe\"]" +
		      "]" +
		    "]," +
		  "\"date\": \"2013-07-04\"" +
		"}";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void no_version() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(2, reader.getWarnings().size()); //no VERSION property

		assertNull(reader.readNext());
	}

	@Test
	public void invalid_version() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"3.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //should still set the version to 4.0
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, reader.getWarnings().size()); //warning added

		assertNull(reader.readNext());
	}

	@Test
	public void version_not_first() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"fn\", {}, \"text\", \"John Doe\"]," +
		      "[\"version\", {}, \"text\", \"4.0\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertEquals(1, reader.getWarnings().size()); //warning added

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertNull(vcard.getFormattedName());
		assertEquals(1, reader.getWarnings().size()); //missing VERSION property

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties_multiple() throws Exception {
		//@formatter:off
		String json =
		  "[" +
		    "[\"vcard\"," +
		      "[" +
		      "]" +
		    "]," +
		    "[\"vcard\"," +
		      "[" +
		      "]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertNull(vcard.getFormattedName());
		assertEquals(1, reader.getWarnings().size()); //missing VERSION property

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertNull(vcard.getFormattedName());
		assertEquals(1, reader.getWarnings().size()); //missing VERSION property

		assertNull(reader.readNext());
	}

	@Test
	public void read_sub_types() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"tel\", {\"type\":[\"work\", \"voice\"], \"x-foo\":\"foo\"}, \"uri\", \"tel:+1 555-555-1234\"]," +
		      "[\"tel\", {\"x-bar\":\"bar\", \"type\":[\"home\", \"voice\"]}, \"uri\", \"tel:+1 555-555-5678\"]," +
		      "[\"note\", {\"language\":\"fr\"}, \"text\", \"Bonjour tout le monde!\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType tel = it.next();
			assertEquals("+1 555-555-1234", tel.getUri().getNumber());
			assertEquals(2, tel.getTypes().size());
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.WORK));
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.VOICE));
			assertEquals("foo", tel.getSubTypes().first("x-foo"));

			tel = it.next();
			assertEquals("+1 555-555-5678", tel.getUri().getNumber());
			assertEquals(2, tel.getTypes().size());
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.HOME));
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.VOICE));
			assertEquals("bar", tel.getSubTypes().first("x-bar"));

			assertFalse(it.hasNext());
		}

		{
			Iterator<NoteType> it = vcard.getNotes().iterator();
			NoteType note = it.next();
			assertEquals("Bonjour tout le monde!", note.getValue());
			assertEquals("fr", note.getLanguage());
			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	@Test
	public void read_group() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"tel\", {\"group\":\"TheGroup1\"}, \"uri\", \"tel:+1 555-555-1234\"]," +					//normal
		      "[\"tel\", {\"Group\":\"TheGroup2\"}, \"uri\", \"tel:+1 555-555-1234\"]," +					//ignore case
		      "[\"tel\", {\"group\":[\"TheGroup3\", \"TheGroup\"]}, \"uri\", \"tel:+1 555-555-5678\"]," +	//array
		      "[\"tel\", {\"group\":null}, \"uri\", \"tel:+1 555-555-9012\"]" +								//null
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());

		{
			Iterator<TelephoneType> it = vcard.getTelephoneNumbers().iterator();

			TelephoneType tel = it.next();
			assertEquals("TheGroup1", tel.getGroup());
			assertTrue(tel.getSubTypes().isEmpty());

			tel = it.next();
			assertEquals("TheGroup2", tel.getGroup());
			assertTrue(tel.getSubTypes().isEmpty());

			tel = it.next();
			assertEquals("TheGroup3", tel.getGroup());
			assertTrue(tel.getSubTypes().isEmpty());

			tel = it.next();
			assertNull(tel.getGroup());
			assertTrue(tel.getSubTypes().isEmpty());

			assertFalse(it.hasNext());
		}

		assertNull(reader.readNext());
	}

	/**
	 * Tests:
	 * <ul>
	 * <li>all JSON data types</li>
	 * <li>null values</li>
	 * <li>empty arrays</li>
	 * <li>multi-valued components</li>
	 * <li>structured/non-structured values</li>
	 * <li>empty structured value</li>
	 * </ul>
	 * @throws Exception
	 */
	@Test
	public void read_property_value() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-type\", {}, \"text\", [ false, true, 1.1, 1, null, \"test\", [], [false, true, 1.1, 1, null, \"test\"] ] ]," +
		      "[\"x-type\", {}, \"text\", false, true, 1.1, 1, null, \"test\", [] ]," +
		      "[\"x-type\", {}, \"text\", [] ]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerExtendedType(TypeForTesting.class);

		VCard vcard = reader.readNext();
		Iterator<TypeForTesting> it = vcard.getProperties(TypeForTesting.class).iterator();

		{
			JCardValue value = it.next().value;

			assertTrue(value.isStructured());
			assertEquals(JCardDataType.TEXT, value.getDataType());

			//@formatter:off
			@SuppressWarnings("unchecked")
			List<List<Object>> expected = Arrays.asList(
				Arrays.asList(new Object[]{ false }),
				Arrays.asList(new Object[]{ true }),
				Arrays.asList(new Object[]{ 1.1 }),
				Arrays.asList(new Object[]{ 1L }),
				Arrays.asList(new Object[]{ "" }),
				Arrays.asList(new Object[]{ "test" }),
				Arrays.asList(new Object[]{ "" }),
				Arrays.asList(new Object[]{ false, true, 1.1, 1L, "", "test" })
			);
			//@formatter:on
			assertEquals(expected, value.getValues());
		}

		{
			JCardValue value = it.next().value;

			assertFalse(value.isStructured());
			assertEquals(JCardDataType.TEXT, value.getDataType());

			//@formatter:off
			@SuppressWarnings("unchecked")
			List<List<Object>> expected = Arrays.asList(
				Arrays.asList(new Object[]{ false }),
				Arrays.asList(new Object[]{ true }),
				Arrays.asList(new Object[]{ 1.1 }),
				Arrays.asList(new Object[]{ 1L }),
				Arrays.asList(new Object[]{ "" }),
				Arrays.asList(new Object[]{ "test" }),
				Arrays.asList(new Object[]{ "" })
			);
			//@formatter:on
			assertEquals(expected, value.getValues());
		}

		{
			JCardValue value = it.next().value;

			assertTrue(value.isStructured());
			assertEquals(JCardDataType.TEXT, value.getDataType());

			//@formatter:off
			@SuppressWarnings("unchecked")
			List<List<Object>> expected = Arrays.asList(
				Arrays.asList(new Object[]{ "" })
			);
			//@formatter:on
			assertEquals(expected, value.getValues());
		}

		assertFalse(it.hasNext());

		assertNull(reader.readNext());
	}

	@Test
	public void unknown_datatype() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-type\", {}, \"name\", \"John Doe\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerExtendedType(TypeForTesting.class);

		VCard vcard = reader.readNext();
		TypeForTesting type = vcard.getProperty(TypeForTesting.class);
		JCardValue value = type.value;

		assertTrue(value.getDataType() == JCardDataType.get("name"));

		assertNull(reader.readNext());
	}

	@Test(expected = RuntimeException.class)
	public void registerExtendedType_no_default_constructor() throws Exception {
		JCardReader reader = new JCardReader("");
		reader.registerExtendedType(BadType.class);
	}

	private static class TypeForTesting extends VCardType {
		public JCardValue value;

		public TypeForTesting() {
			super("X-TYPE");
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
		protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
			this.value = value;
		}
	}
}
