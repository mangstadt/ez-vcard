package ezvcard.types;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
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
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();

	final int pid = 1;
	final String uri = "urn:uuid:1234";
	final ClientPidMapType withValue = new ClientPidMapType(pid, uri);
	final ClientPidMapType empty = new ClientPidMapType();

	ClientPidMapType clientPidMapType;

	@Before
	public void before() {
		clientPidMapType = new ClientPidMapType();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = withValue.marshalText(version, warnings, compatibilityMode);

		assertEquals(pid + ";" + uri, actual);
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_empty() {
		VCardVersion version = VCardVersion.V4_0;
		empty.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.uri(uri);
		xe.append("sourceid", pid + "");
		Document expected = xe.document();

		xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		Document actual = xe.document();

		withValue.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void marshalXml_empty() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		empty.marshalXml(xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = withValue.marshalJson(version, warnings);
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertTrue(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "1" }),
			Arrays.asList(new Object[]{ "urn:uuid:1234" })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void marshalJson_empty() {
		VCardVersion version = VCardVersion.V4_0;
		empty.marshalJson(version, warnings);
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

	@Test(expected = SkipMeException.class)
	public void unmarshalText_no_semicolon() {
		VCardVersion version = VCardVersion.V4_0;
		clientPidMapType.unmarshalText(subTypes, "no semicolon", version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_bad_pid() {
		VCardVersion version = VCardVersion.V4_0;
		clientPidMapType.unmarshalText(subTypes, "foo;bar", version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.uri(uri);
		xe.append("sourceid", pid + "");

		clientPidMapType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(pid, clientPidMapType.getPid().intValue());
		assertEquals(uri, clientPidMapType.getUri());
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_bad_pid() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.uri(uri);
		xe.append("sourceid", "foo");

		clientPidMapType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.text(pid + "", uri);

		clientPidMapType.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(pid, clientPidMapType.getPid().intValue());
		assertEquals(uri, clientPidMapType.getUri());
		assertWarnings(0, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalJson_bad_pid() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.text("foo", uri);

		clientPidMapType.unmarshalJson(subTypes, value, version, warnings);
	}

	@Test
	public void random() {
		ClientPidMapType clientpidmap = ClientPidMapType.random(2);
		assertIntEquals(2, clientpidmap.getPid());
		assertTrue(clientpidmap.getUri().matches("urn:uuid:[-\\da-f]+"));
	}
}
