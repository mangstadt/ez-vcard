package ezvcard.types;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.JCardValue;
import ezvcard.util.JsonValue;
import ezvcard.util.XCardElement;

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
public class ClientPidMapTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final VCard vcard = new VCard();

	private final int pid = 1;
	private final String uri = "urn:uuid:1234";
	private final ClientPidMapType withValue = new ClientPidMapType(pid, uri);
	private final ClientPidMapType empty = new ClientPidMapType();

	private ClientPidMapType clientPidMapType;

	@Before
	public void before() {
		clientPidMapType = new ClientPidMapType();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void validate() {
		assertWarnings(2, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(2, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		assertWarnings(1, withValue.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, withValue.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = withValue.marshalText(version, compatibilityMode);

		assertEquals(pid + ";" + uri, actual);
	}

	@Test
	public void marshalText_empty() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = empty.marshalText(version, compatibilityMode);

		assertEquals("null;null", actual);
	}

	@Test
	public void marshalXml() {
		assertMarshalXml(withValue, "<uri>" + uri + "</uri><sourceid>" + pid + "</sourceid>");
	}

	@Test
	public void marshalXml_empty() {
		assertMarshalXml(empty, "<sourceid/><uri/>");
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = withValue.marshalJson(version);
		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue(1),
				new JsonValue("urn:uuid:1234")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void marshalJson_empty() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = empty.marshalJson(version);

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue(""),
				new JsonValue("")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void unmarshalText() {
		VCardVersion version = VCardVersion.V4_0;
		clientPidMapType.unmarshalText(subTypes, pid + ";" + uri, version, warnings, compatibilityMode);

		assertEquals(pid, clientPidMapType.getPid().intValue());
		assertEquals(uri, clientPidMapType.getUri());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_semicolon_in_uri() {
		VCardVersion version = VCardVersion.V4_0;
		clientPidMapType.unmarshalText(subTypes, pid + ";" + uri + ";foo", version, warnings, compatibilityMode);

		assertEquals(pid, clientPidMapType.getPid().intValue());
		assertEquals(uri + ";foo", clientPidMapType.getUri());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_no_semicolon() {
		VCardVersion version = VCardVersion.V4_0;
		clientPidMapType.unmarshalText(subTypes, "no semicolon", version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_bad_pid() {
		VCardVersion version = VCardVersion.V4_0;
		clientPidMapType.unmarshalText(subTypes, "foo;bar", version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.append(VCardDataType.URI, uri);
		xe.append("sourceid", pid + "");

		clientPidMapType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(pid, clientPidMapType.getPid().intValue());
		assertEquals(uri, clientPidMapType.getUri());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_bad_pid() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.append(VCardDataType.URI, uri);
		xe.append("sourceid", "foo");

		clientPidMapType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_no_pid() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.append(VCardDataType.URI, uri);

		clientPidMapType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_no_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.append("sourceid", "1");

		clientPidMapType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_no_pid_or_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());

		clientPidMapType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(VCardDataType.TEXT, pid + "", uri);

		clientPidMapType.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(pid, clientPidMapType.getPid().intValue());
		assertEquals(uri, clientPidMapType.getUri());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalJson_bad_pid() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.structured(VCardDataType.TEXT, "foo", uri);

		clientPidMapType.unmarshalJson(subTypes, value, version, warnings);
	}

	@Test
	public void random() {
		ClientPidMapType clientpidmap = ClientPidMapType.random(2);
		assertIntEquals(2, clientpidmap.getPid());
		assertTrue(clientpidmap.getUri().matches("urn:uuid:[-\\da-f]+"));
	}
}
