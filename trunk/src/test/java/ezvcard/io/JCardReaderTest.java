package ezvcard.io;

import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.types.RawType;
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
public class JCardReaderTest {
	@Test
	public void read_single() throws Throwable {
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
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

		assertNull(reader.readNext());
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

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		assertEquals("Jane Doe", vcard.getFormattedName().getValue());
		assertWarnings(0, reader.getWarnings());

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
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(1, reader.getWarnings());

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
		assertEquals(1, vcard.getProperties().size());
		assertEquals("John Doe", vcard.getFormattedName().getValue());
		assertWarnings(1, reader.getWarnings());

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties() throws Throwable {
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
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader.getWarnings()); //missing VERSION property

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties_multiple() throws Throwable {
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
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader.getWarnings()); //missing VERSION property

		vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion()); //default to 4.0
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader.getWarnings()); //missing VERSION property

		assertNull(reader.readNext());
	}

	@Test
	public void extendedType() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-type\", {}, \"text\", \"value\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		RawType prop = vcard.getExtendedProperty("x-type");
		assertEquals("value", prop.getValue());
		assertWarnings(0, reader.getWarnings());
	}

	@Test
	public void registerExtendedType() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-type\", {}, \"text\", \"value\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerExtendedType(TypeForTesting.class);

		VCard vcard = reader.readNext();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());
		TypeForTesting prop = vcard.getProperty(TypeForTesting.class);
		assertEquals("value", prop.value.getSingleValued());
		assertWarnings(0, reader.getWarnings());
	}

	@Test(expected = RuntimeException.class)
	public void registerExtendedType_no_default_constructor() throws Throwable {
		JCardReader reader = new JCardReader("");
		reader.registerExtendedType(BadType.class);
	}

	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String json =
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"x-lucky-num\", {}, \"text\", \"13\"]" +
		    "]" +
		  "]";
		//@formatter:on

		JCardReader reader = new JCardReader(json);
		reader.registerExtendedType(LuckyNumType.class);

		VCard vcard = reader.readNext();
		assertEquals(0, vcard.getProperties().size());
		assertWarnings(1, reader.getWarnings());
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
