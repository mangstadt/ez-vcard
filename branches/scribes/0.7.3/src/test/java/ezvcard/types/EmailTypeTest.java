package ezvcard.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
public class EmailTypeTest {
	/**
	 * If a type contains a "TYPE=pref" parameter and it's being marshalled to
	 * 4.0, it should replace "TYPE=pref" with "PREF=1". <br>
	 * <br>
	 * Conversely, if types contain "PREF" parameters and they're being
	 * marshalled to 2.1/3.0, then it should find the type with the lowest PREF
	 * value and add "TYPE=pref" to it.
	 */
	@Test
	public void marshalPref() throws Exception {
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		//EMAIL has "TYPE=pref"==========
		EmailType t = new EmailType();
		t.addType(EmailTypeParameter.PREF);

		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));

		version = VCardVersion.V4_0;
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(1), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));

		//EMAIL has PREF parameter=======
		VCard vcard = new VCard();
		EmailType t1 = new EmailType();
		t1.setPref(1);
		vcard.addEmail(t1);
		EmailType t2 = new EmailType();
		t2.setPref(2);
		vcard.addEmail(t2);

		version = VCardVersion.V2_1;
		subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));

		version = VCardVersion.V4_0;
		subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(1), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(2), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
	}
}
