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
import ezvcard.parameters.KeyTypeParameter;
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
public class KeyTypeTest {
	@Test
	public void marshalTextValue() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		KeyType t;
		String expectedValue, actualValue;
		VCardSubTypes subTypes;

		t = new KeyType();
		t.setText("abc123", KeyTypeParameter.PGP);
		t.setType("work"); //4.0 TYPE parameter

		//2.1
		version = VCardVersion.V2_1;
		expectedValue = "abc123";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals(KeyTypeParameter.PGP.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());
		
		//3.0
		version = VCardVersion.V3_0;
		expectedValue = "abc123";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals(KeyTypeParameter.PGP.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());

		//4.0
		version = VCardVersion.V4_0;
		expectedValue = "abc123";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals("work", subTypes.getType());
		assertEquals(KeyTypeParameter.PGP.getMediaType(), subTypes.getMediaType());
	}

	@Test
	public void unmarshalText() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes;
		KeyType t;

		//2.1
		version = VCardVersion.V2_1;
		t = new KeyType();
		subTypes = new VCardSubTypes();
		subTypes.setType(KeyTypeParameter.PGP.getValue());
		t.unmarshalValue(subTypes, "abc123", version, warnings, compatibilityMode);
		assertEquals("abc123", t.getText());
		assertNull(t.getUrl());
		assertNull(t.getData());
		assertEquals(KeyTypeParameter.PGP, t.getContentType());
		
		//3.0
		version = VCardVersion.V3_0;
		t = new KeyType();
		subTypes = new VCardSubTypes();
		subTypes.setType(KeyTypeParameter.PGP.getValue());
		t.unmarshalValue(subTypes, "abc123", version, warnings, compatibilityMode);
		assertEquals("abc123", t.getText());
		assertNull(t.getUrl());
		assertNull(t.getData());
		assertEquals(KeyTypeParameter.PGP, t.getContentType());
		
		//4.0
		version = VCardVersion.V4_0;
		t = new KeyType();
		subTypes = new VCardSubTypes();
		subTypes.setMediaType(KeyTypeParameter.PGP.getMediaType());
		t.unmarshalValue(subTypes, "abc123", version, warnings, compatibilityMode);
		assertEquals("abc123", t.getText());
		assertNull(t.getUrl());
		assertNull(t.getData());
		assertEquals(KeyTypeParameter.PGP, t.getContentType());
	}
}
