package ezvcard.types;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;

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
public class AddressTypeTest {
	@Test
	public void doMarshalValue() {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC2426;
		AddressType t;
		String expected, actual;

		//all fields present
		t = new AddressType();
		t.setPoBox("P.O. Box 1234;");
		t.setExtendedAddress("Apt, 11");
		t.setStreetAddress("123 Main St");
		t.setLocality("Austin");
		t.setRegion("TX");
		t.setPostalCode("12345");
		t.setCountry("USA");
		expected = "P.O. Box 1234\\;;Apt\\, 11;123 Main St;Austin;TX;12345;USA";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//some nulls
		t = new AddressType();
		t.setPoBox("P.O. Box 1234;");
		t.setExtendedAddress(null);
		t.setStreetAddress(null);
		t.setLocality("Austin");
		t.setRegion("TX");
		t.setPostalCode("12345");
		t.setCountry(null);
		expected = "P.O. Box 1234\\;;;;Austin;TX;12345;";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//all nulls
		t = new AddressType();
		expected = ";;;;;;";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void doUnmarshalValue() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC2426;
		VCardSubTypes subTypes = new VCardSubTypes();
		AddressType t;

		//all fields present
		t = new AddressType();
		t.unmarshalValue(subTypes, "P.O. Box 1234\\;;Apt 11;123 Main St;Austin;TX;12345;USA", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals("Apt 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some empty fields
		t = new AddressType();
		t.unmarshalValue(subTypes, "P.O. Box 1234\\;;;;Austin;TX;12345;USA", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;", t.getPoBox());
		assertEquals(null, t.getExtendedAddress());
		assertEquals(null, t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals("12345", t.getPostalCode());
		assertEquals("USA", t.getCountry());

		//some fields missing at the end
		t = new AddressType();
		t.unmarshalValue(subTypes, "P.O. Box 1234\\;56;Apt 11;123 Main St;Austin;TX", version, warnings, compatibilityMode);
		assertEquals("P.O. Box 1234;56", t.getPoBox());
		assertEquals("Apt 11", t.getExtendedAddress());
		assertEquals("123 Main St", t.getStreetAddress());
		assertEquals("Austin", t.getLocality());
		assertEquals("TX", t.getRegion());
		assertEquals(null, t.getPostalCode());
		assertEquals(null, t.getCountry());
	}
}
