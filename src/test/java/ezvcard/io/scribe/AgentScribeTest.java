package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.VCardVersion.values;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.property.Agent;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
public class AgentScribeTest {
	private final AgentScribe scribe = new AgentScribe();
	private final Sensei<Agent> sensei = new Sensei<Agent>(scribe);

	private final VCard vcard = new VCard();
	private final String url = "http://mi5.co.uk/007";
	private final Agent withUrl = new Agent(url);
	private final Agent withVCard = new Agent(vcard);
	private final Agent empty = new Agent();

	@Test
	public void dataType() {
		sensei.assertDataType(withUrl).versions(V2_1).run(VCardDataType.URL);
		sensei.assertDataType(withUrl).versions(V3_0, V4_0).run(VCardDataType.URI);

		sensei.assertDataType(withVCard).run(null);
		sensei.assertDataType(empty).run(null);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withUrl).run(url);

		for (VCardVersion version : values()) {
			try {
				scribe.writeText(withVCard, version);
				fail();
			} catch (EmbeddedVCardException e) {
				assertEquals(vcard, e.getVCard());
			}
		}

		sensei.assertWriteText(empty).skipMe();
	}

	@Test
	public void parseText_url() {
		sensei.assertParseText(url).dataType(VCardDataType.URL).run(hasUrl);
		sensei.assertParseText(url).dataType(VCardDataType.URI).run(hasUrl);
	}

	@Test
	public void parseText_vcard_2_1() {
		try {
			sensei.assertParseText("").versions(V2_1).run();
			fail();
		} catch (EmbeddedVCardException e) {
			Agent property = (Agent) e.getProperty();
			assertNull(property.getUrl());
			assertNull(property.getVCard());

			e.injectVCard(vcard);
			assertNull(property.getUrl());
			assertEquals(vcard, property.getVCard());
		}
	}

	@Test
	public void parseText_vcard_3_0() {
		try {
			sensei.assertParseText("BEGIN:VCARD\\nEND:VCARD").versions(V3_0).run();
			fail();
		} catch (EmbeddedVCardException e) {
			Agent property = (Agent) e.getProperty();
			assertNull(property.getUrl());
			assertNull(property.getVCard());

			e.injectVCard(vcard);
			assertNull(property.getUrl());
			assertEquals(vcard, property.getVCard());
		}
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<a href=\"" + url + "\"></a>").run(hasUrl);
		sensei.assertParseHtml("<div>" + url + "</div>").run(hasUrl);

		try {
			//@formatter:off
			sensei.assertParseHtml(
			"<div class=\"agent vcard\">" +
				"<span class=\"fn\">Jane Doe</span>" +
				"<div class=\"agent vcard\">" +
					"<span class=\"fn\">Joseph Doe</span>" +
				"</div>" +
			"</div>").run();
			//@formatter:on

			fail();
		} catch (EmbeddedVCardException e) {
			Agent property = (Agent) e.getProperty();
			assertNull(property.getUrl());
			assertNull(property.getVCard());

			e.injectVCard(vcard);
			assertNull(property.getUrl());
			assertEquals(vcard, property.getVCard());
		}
	}

	private final Check<Agent> hasUrl = new Check<Agent>() {
		public void check(Agent property) {
			assertEquals(url, property.getUrl());
			assertNull(property.getVCard());
		}
	};
}
