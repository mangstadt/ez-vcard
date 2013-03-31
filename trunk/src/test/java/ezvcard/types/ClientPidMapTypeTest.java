package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
	private final ClientPidMapType clientpidmap = new ClientPidMapType(1, "urn:uuid:1234");

	@Test
	public void marshalText() {
		String actual = clientpidmap.marshalText(VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		String expected = "1;urn:uuid:1234";
		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml() {
		XCardElement xe = new XCardElement("clientpidmap");
		xe.uri("urn:uuid:1234");
		xe.append("sourceid", "1");
		Document expected = xe.document();

		xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		Document actual = xe.document();

		clientpidmap.marshalXml(xe.element(), VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);

		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalJson() {
		JCardValue value = clientpidmap.marshalJson(VCardVersion.V4_0, new ArrayList<String>());
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(new Object[]{ "1" }),
			Arrays.asList(new Object[]{ "urn:uuid:1234" })
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void unmarshalText() {
		ClientPidMapType clientpidmap = new ClientPidMapType();
		clientpidmap.unmarshalText(new VCardSubTypes(), "1;urn:uuid:1234", VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);

		assertEquals(1, clientpidmap.getPid().intValue());
		assertEquals("urn:uuid:1234", clientpidmap.getUri());
	}

	@Test
	public void unmarshalXml() {
		XCardElement xe = new XCardElement(ClientPidMapType.NAME.toLowerCase());
		xe.uri("urn:uuid:1234");
		xe.append("sourceid", "1");

		ClientPidMapType clientpidmap = new ClientPidMapType();
		clientpidmap.unmarshalXml(new VCardSubTypes(), xe.element(), VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);

		assertEquals(1, clientpidmap.getPid().intValue());
		assertEquals("urn:uuid:1234", clientpidmap.getUri());
	}

	@Test
	public void unmarshalJson() {
		JCardValue value = JCardValue.text("1", "urn:uuid:1234");

		ClientPidMapType clientpidmap = new ClientPidMapType();
		clientpidmap.unmarshalJson(new VCardSubTypes(), value, VCardVersion.V4_0, new ArrayList<String>());

		assertEquals(1, clientpidmap.getPid().intValue());
		assertEquals("urn:uuid:1234", clientpidmap.getUri());
	}

	@Test
	public void random() {
		ClientPidMapType clientpidmap = ClientPidMapType.random(2);
		assertEquals(Integer.valueOf(2), clientpidmap.getPid());
		assertTrue(clientpidmap.getUri().matches("urn:uuid:[-\\da-f]+"));
	}
}
