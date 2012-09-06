package ezvcard.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EmailTypeParameter;

/*
 Copyright (c) 2012, Michael Angstadt
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
	@Test
	public void doMarshalValue() throws Exception {
		//Note: Marshalling of 2.1 AGENT types is tested in VCardWriterTest
		VCardVersion version = VCardVersion.V3_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		AgentType t;
		String expected, actual;

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName(new FormattedNameType("Agent 007"));
		EmailType agentEmail = new EmailType("007@mi5.co.uk");
		agentEmail.addType(EmailTypeParameter.INTERNET);
		agentVcard.getEmails().add(agentEmail);
		t = new AgentType(agentVcard);

		VCard agentVcard2 = new VCard();
		agentVcard2.setFormattedName(new FormattedNameType("Agent 009"));
		EmailType agentEmail2 = new EmailType("009@mi5.co.uk");
		agentEmail2.addType(EmailTypeParameter.INTERNET);
		agentVcard2.getEmails().add(agentEmail2);
		agentVcard.setAgent(new AgentType(agentVcard2));

		//FIXME this test may fail on other machines because Class.getDeclaredFields() returns the fields in no particular order
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN:VCARD\n");
		sb.append("VERSION:3.0\n");
		sb.append("FN:Agent 007\n");
		sb.append("EMAIL\\;TYPE=internet:007@mi5.co.uk\n");
		sb.append("AGENT:");
			sb.append("BEGIN:VCARD\\\\n");
			sb.append("VERSION:3.0\\\\n");
			sb.append("FN:Agent 009\\\\n");
			sb.append("EMAIL\\\\\\;TYPE=internet:009@mi5.co.uk\\\\n");
			sb.append("END:VCARD\\\\n\n");
		sb.append("END:VCARD\n");
		//@formatter:on

		expected = sb.toString();
		actual = t.marshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void doUnmarshalValue() throws Exception {
		//Note: Marshalling of 2.1 AGENT types is tested in VCardWriterTest

		VCardVersion version = VCardVersion.V3_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		AgentType t;

		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: VCARD\\n");
		sb.append("VERSION: 3.0\\n");
		sb.append("FN: Agent 007\\n");
		sb.append("EMAIL\\;TYPE=internet: 007@mi5.co.uk\\n");
		sb.append("AGENT: ");
			sb.append("BEGIN: VCARD\\\\n");
			sb.append("VERSION: 3.0\\\\n");
			sb.append("FN: Agent 009\\\\n");
			sb.append("EMAIL\\\\\\;TYPE=internet: 009@mi5.co.uk\\\\n");
			sb.append("END: VCARD\\\\n\\n");
		sb.append("END: VCARD\\n");
		//@formatter:on

		t = new AgentType();
		t.unmarshalValue(subTypes, sb.toString(), version, warnings, compatibilityMode);
		VCard agent1 = t.getVcard();
		assertEquals("Agent 007", agent1.getFormattedName().getValue());
		assertEquals("007@mi5.co.uk", agent1.getEmails().get(0).getValue());
		assertTrue(agent1.getEmails().get(0).getTypes().contains(EmailTypeParameter.INTERNET));
		VCard agent2 = agent1.getAgent().getVcard();
		assertEquals("Agent 009", agent2.getFormattedName().getValue());
		assertEquals("009@mi5.co.uk", agent2.getEmails().get(0).getValue());
		assertTrue(agent2.getEmails().get(0).getTypes().contains(EmailTypeParameter.INTERNET));
	}
}
