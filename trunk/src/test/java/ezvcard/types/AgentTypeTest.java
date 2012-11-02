package ezvcard.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.parameters.ValueParameter;

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
		VCardVersion version = VCardVersion.V3_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		AgentType t;
		String expected, actual;

		//with URL
		t = new AgentType("http://mi5.co.uk/007");
		expected = "http://mi5.co.uk/007";
		actual = t.marshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//with vCard
		VCard vcard = new VCard();
		t = new AgentType(vcard);
		try {
			t.marshalValue(version, warnings, compatibilityMode);
			fail();
		} catch (EmbeddedVCardException e) {
			//should be thrown
			assertEquals(vcard, e.getVCard());
		}
	}

	@Test
	public void doUnmarshalValue() throws Exception {
		VCardVersion version = VCardVersion.V3_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes;
		AgentType t;

		//with URL
		t = new AgentType();
		subTypes = new VCardSubTypes();
		subTypes.setValue(ValueParameter.URL);
		t.unmarshalValue(subTypes, "http://mi5.co.uk/007", version, warnings, compatibilityMode);
		assertEquals("http://mi5.co.uk/007", t.getUrl());
		assertNull(t.getVcard());

		//with vCard
		VCard vcard = new VCard();
		t = new AgentType();
		subTypes = new VCardSubTypes();
		try {
			t.unmarshalValue(subTypes, "BEGIN:VCARD\\nEND:VCARD", version, warnings, compatibilityMode);
			fail();
		} catch (EmbeddedVCardException e) {
			//should be thrown
			e.injectVCard(vcard);
		}
		assertNull(t.getUrl());
		assertEquals(vcard, t.getVcard());
	}
}
