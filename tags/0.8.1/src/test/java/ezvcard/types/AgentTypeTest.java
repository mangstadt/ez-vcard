package ezvcard.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HtmlUtils;

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
public class AgentTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

	final VCard vcard = new VCard();
	final String url = "http://mi5.co.uk/007";
	final AgentType urlType = new AgentType(url);
	final AgentType vcardType = new AgentType(vcard);
	final AgentType emptyType = new AgentType();

	final VCardSubTypes subTypes = new VCardSubTypes();
	AgentType agentType;

	@Before
	public void before() {
		agentType = new AgentType();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void marshalSubTypes_url_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = urlType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertEquals(ValueParameter.URL, subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_url_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = urlType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_vcard_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = vcardType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_vcard_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = vcardType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(0, subTypes.size());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_url_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = urlType.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_url_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = urlType.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_vcard_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		try {
			vcardType.marshalText(version, warnings, compatibilityMode);
			fail();
		} catch (EmbeddedVCardException e) {
			assertEquals(vcard, e.getVCard());
		}
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_vcard_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		try {
			vcardType.marshalText(version, warnings, compatibilityMode);
			fail();
		} catch (EmbeddedVCardException e) {
			assertEquals(vcard, e.getVCard());
		}
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_no_url_or_vcard_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		emptyType.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_no_url_or_vcard_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		emptyType.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalText_url_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setValue(ValueParameter.URL);
		agentType.unmarshalText(subTypes, url, version, warnings, compatibilityMode);

		assertEquals(url, agentType.getUrl());
		assertNull(agentType.getVCard());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_url_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		subTypes.setValue(ValueParameter.URI);
		agentType.unmarshalText(subTypes, url, version, warnings, compatibilityMode);

		assertEquals(url, agentType.getUrl());
		assertNull(agentType.getVCard());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_vcard_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		try {
			agentType.unmarshalText(subTypes, "", version, warnings, compatibilityMode);
			fail();
		} catch (EmbeddedVCardException e) {
			e.injectVCard(vcard);
		}
		assertNull(agentType.getUrl());
		assertEquals(vcard, agentType.getVCard());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_vcard_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		try {
			agentType.unmarshalText(subTypes, "BEGIN:VCARD\\nEND:VCARD", version, warnings, compatibilityMode);
			fail();
		} catch (EmbeddedVCardException e) {
			e.injectVCard(vcard);
		}
		assertNull(agentType.getUrl());
		assertEquals(vcard, agentType.getVCard());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml_url_link() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<a href=\"" + url + "\"></a>");
		agentType.unmarshalHtml(element, warnings);

		assertEquals(url, agentType.getUrl());
		assertNull(agentType.getVCard());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml_url_text() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + url + "</div>");
		agentType.unmarshalHtml(element, warnings);

		assertEquals(url, agentType.getUrl());
		assertNull(agentType.getVCard());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml_vcard() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div class=\"agent vcard\">" +
			"<span class=\"fn\">Jane Doe</span>" +
			"<div class=\"agent vcard\">" +
				"<span class=\"fn\">Joseph Doe</span>" +
			"</div>" +
		"</div>"
		);
		//@formatter:on

		try {
			agentType.unmarshalHtml(element, warnings);
			fail();
		} catch (EmbeddedVCardException e) {
			e.injectVCard(vcard);
		}

		assertNull(agentType.getUrl());
		assertEquals(vcard, agentType.getVCard());
		assertEquals(0, warnings.size());
	}
}
