package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.NoteType;
import ezvcard.types.TelephoneType;

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
	public void read_single_with_vcardstream() throws Exception {
		//@formatter:off
		String json =
		"[\"vcardstream\",\n" +
		  "[\"vcard\",\n" +
		    "[\n" +
		      "[\"version\", {}, \"text\", \"4.0\"],\n" +
		      "[\"fn\", {}, \"text\", \"Simon Perreault\"]\n" +
		    "]\n" +
		  "]\n" +
		"]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void read_single_without_vcardstream() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"Simon Perreault\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	@Test
	public void read_multiple() throws Exception {
		//@formatter:off
		String json =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"Simon Perreault\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("John Doe", vcard.getFormattedName().getValue());

		assertNull(reader.readNext());
	}

	//	@Test
	//	public void data_type_does_not_match_JSON_data_type() throws Exception {
//		//@formatter:off
//		String json =
//		  "[\"vcard\"," +
//		    "[" +
//		      "[\"version\", {}, \"text\", \"4.0\"]," +
//		      "[\"fn\", {}, \"text\", \"Simon Perreault\"]," +
//		      "[\"x-int-wrong\", {}, \"text\", 12]," +
//		      "[\"x-int-correct\", {}, \"integer\", 12]," +
//		      "[\"x-int-correct\", {}, \"integer\", \"12\"]," +
//		      "[\"x-float-wrong\", {}, \"text\", 12.1]," +
//		      "[\"x-float-correct\", {}, \"float\", 12.1]," +
//		      "[\"x-float-correct\", {}, \"float\", \"12.1\"]," +
//		      "[\"x-true-wrong\", {}, \"text\", true]," +
//		      "[\"x-true-correct\", {}, \"boolean\", true]," +
//		      "[\"x-true-correct\", {}, \"boolean\", \"true\"]," +
//		      "[\"x-false-wrong\", {}, \"text\", false]," +
//		      "[\"x-false-correct\", {}, \"boolean\", false]" +
//		      "[\"x-false-correct\", {}, \"boolean\", \"false\"]" +
//		    "]" +
//		  "]";
//		//@formatter:on
	//
	//		JCardReader reader = new JCardReader(json);
	//
	//		VCard vcard = reader.readNext();
	//		assertEquals(VCardVersion.V4_0, vcard.getVersion());
	//		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());
	//		assertEquals(4, reader.getWarnings().size()); //warnings added
	//
	//		assertNull(reader.readNext());
	//	}

	@Test
	public void no_version() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"fn\", {}, \"text\", \"Simon Perreault\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());
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
		      "[\"fn\", {}, \"text\", \"Simon Perreault\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //should still set the version to 4.0
		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());
		assertEquals(1, reader.getWarnings().size()); //warning added

		assertNull(reader.readNext());
	}

	@Test
	public void version_not_first() throws Exception {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"fn\", {}, \"text\", \"Simon Perreault\"]," +
		      "[\"version\", {}, \"text\", \"4.0\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals("Simon Perreault", vcard.getFormattedName().getValue());
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
			assertEquals("+1 555-555-1234", tel.getValue());
			assertEquals(2, tel.getTypes().size());
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.WORK));
			assertTrue(tel.getTypes().contains(TelephoneTypeParameter.VOICE));
			assertEquals("foo", tel.getSubTypes().first("x-foo"));

			tel = it.next();
			assertEquals("+1 555-555-5678", tel.getValue());
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
}
