package ezvcard.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.TelephoneTypeParameter;
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
public class TelephoneTypeTest {
	@Test
	public void marshal() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		String expectedValue, actualValue;
		VCardSubTypes subTypes;

		TelephoneType t = new TelephoneType("+1 555-555-1234");
		t.addType(TelephoneTypeParameter.HOME);

		//2.1
		version = VCardVersion.V2_1;
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		expectedValue = "+1 555-555-1234";
		assertEquals(expectedValue, actualValue);
		assertNull(subTypes.getValue());
		assertEquals(TelephoneTypeParameter.HOME.getValue(), subTypes.getType());

		//3.0
		version = VCardVersion.V3_0;
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		expectedValue = "+1 555-555-1234";
		assertEquals(expectedValue, actualValue);
		assertNull(subTypes.getValue());
		assertEquals(TelephoneTypeParameter.HOME.getValue(), subTypes.getType());

		//4.0
		version = VCardVersion.V4_0;
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		expectedValue = "tel:+1 555-555-1234";
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(TelephoneTypeParameter.HOME.getValue(), subTypes.getType());
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		TelephoneType t;
		String expectedValue, actualValue;

		//2.1
		version = VCardVersion.V2_1;
		t = new TelephoneType();
		t.unmarshalValue(subTypes, "+1 555-555-1234.", version, warnings, compatibilityMode);
		expectedValue = "+1 555-555-1234.";
		actualValue = t.getValue();
		assertEquals(expectedValue, actualValue);

		//3.0
		version = VCardVersion.V3_0;
		t = new TelephoneType();
		t.unmarshalValue(subTypes, "+1 555-555-1234.", version, warnings, compatibilityMode);
		expectedValue = "+1 555-555-1234.";
		actualValue = t.getValue();
		assertEquals(expectedValue, actualValue);

		//4.0
		version = VCardVersion.V4_0;
		t = new TelephoneType();
		t.unmarshalValue(subTypes, "tel:+1 555-555-1234.", version, warnings, compatibilityMode);
		expectedValue = "+1 555-555-1234.";
		actualValue = t.getValue();
		assertEquals(expectedValue, actualValue);
	}
}
