package ezvcard.io;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.types.AgentType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.NoteType;
import ezvcard.types.PhotoType;
import ezvcard.types.StructuredNameType;

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
public class VCardWriterTest {
	@Test
	public void test() throws Exception {
		VCard vcard = new VCard();

		StructuredNameType n = new StructuredNameType();
		n.setGiven("Michael");
		n.setFamily("Angstadt");
		vcard.setStructuredName(n);

		FormattedNameType fn = new FormattedNameType("Michael Angstadt");
		vcard.setFormattedName(fn);

		PhotoType photo = new PhotoType();
		photo.setUrl("http://example.com/image.jpg");
		vcard.getPhotos().add(photo);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.write(vcard);
		System.out.println(sw.toString());
	}

	/**
	 * AGENT types for 2.1 vCards are nested.
	 */
	@Test
	public void nestedAgent() throws Exception {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("Michael Angstadt");
		vcard.setFormattedName(fn);

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName(new FormattedNameType("Agent 007"));
		agentVcard.getNotes().add(new NoteType("Make sure that it properly folds long lines which are part of a nested AGENT type in a version 2.1 vCard."));
		AgentType agent = new AgentType(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName(new FormattedNameType("Agent 009"));
		secondAgentVCard.getNotes().add(new NoteType("Make sure that it ALSO properly folds THIS long line because it's part of an AGENT that's inside of an AGENT."));
		AgentType secondAgent = new AgentType(secondAgentVCard);
		agentVcard.setAgent(secondAgent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.write(vcard);
		String actual = sw.toString();

		//FIXME this test may fail on other machines because Class.getDeclaredFields() returns the fields in no particular order
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("FN: Michael Angstadt\r\n");
		sb.append("AGENT: \r\n"); //nested types should not have X-GENERATOR
			sb.append("BEGIN: vcard\r\n");
			sb.append("VERSION: 2.1\r\n");
			sb.append("FN: Agent 007\r\n");
			sb.append("AGENT: \r\n");
				sb.append("BEGIN: vcard\r\n");
				sb.append("VERSION: 2.1\r\n");
				sb.append("FN: Agent 009\r\n");
				sb.append("NOTE: Make sure that it ALSO properly folds THIS long line because it's par\r\n");
				sb.append(" t of an AGENT that's inside of an AGENT.\r\n");
				sb.append("END: vcard\r\n");
			sb.append("NOTE: Make sure that it properly folds long lines which are part of a neste\r\n");
			sb.append(" d AGENT type in a version 2.1 vCard.\r\n");
			sb.append("END: vcard\r\n");
		sb.append("X-GENERATOR: EZ vCard v0.1 http://code.google.com/p/ez-vcard\r\n");
		sb.append("END: vcard\r\n");
		String expected = sb.toString();
		//@formatter:on

		assertEquals(expected, actual);
	}
}
