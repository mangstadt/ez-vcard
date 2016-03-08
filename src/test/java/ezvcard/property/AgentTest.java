package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
 * @author Michael Angstadt
 */
public class AgentTest {
	@Test
	public void set_value() {
		VCard vcard = new VCard();
		Agent agent = new Agent();

		agent.setUrl("one");
		assertEquals("one", agent.getUrl());
		assertNull(agent.getVCard());

		agent.setVCard(vcard);
		assertNull(agent.getUrl());
		assertEquals(vcard, agent.getVCard());

		agent.setUrl("one");
		assertEquals("one", agent.getUrl());
		assertNull(agent.getVCard());
	}

	@Test
	public void constructors() {
		VCard vcard = new VCard();
		Agent agent = new Agent();
		assertNull(agent.getUrl());
		assertNull(agent.getVCard());

		agent = new Agent("one");
		assertEquals("one", agent.getUrl());
		assertNull(agent.getVCard());

		agent = new Agent(vcard);
		assertNull(agent.getUrl());
		assertEquals(vcard, agent.getVCard());
	}

	@Test
	public void validate() {
		Agent property = new Agent();
		assertValidate(property).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(8);
		assertValidate(property).versions(VCardVersion.V4_0).run(8, 2);

		VCard agentVCard = new VCard();
		property.setVCard(agentVCard);
		assertValidate(property).versions(VCardVersion.V2_1).run(10);
		assertValidate(property).versions(VCardVersion.V3_0).run(10, 10);
		assertValidate(property).versions(VCardVersion.V4_0).run(10, 2);

		property.setUrl("http://example.com");
		assertValidate(property).versions(VCardVersion.V2_1, VCardVersion.V3_0).run();
		assertValidate(property).versions(VCardVersion.V4_0).run(2);
	}

	@Test
	public void toStringValues() {
		Agent property = new Agent();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Agent original = new Agent();
		assertCopy(original);

		original = new Agent("url");
		assertCopy(original);

		original = new Agent(new VCard());
		assertCopy(original).notSame("getVCard");
	}

	@Test
	public void equals() {
		VCard vcard = new VCard();
		VCard vcard2 = new VCard();
		vcard2.setVersion(VCardVersion.V4_0);

		//@formatter:off
		assertNothingIsEqual(
			new Agent(),
			new Agent("url"),
			new Agent("url2"),
			new Agent(vcard),
			new Agent(vcard2)
		);

		assertEqualsMethod(Agent.class)
		.constructor().test()
		.constructor("url")
			.test()
			.test(new Agent(new VCard()), new Agent(new VCard()));
		//@formatter:on
	}
}
