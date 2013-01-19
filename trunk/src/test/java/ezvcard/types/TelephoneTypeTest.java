package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.XCardElement;

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
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		expectedValue = "+1 555-555-1234";
		assertEquals(expectedValue, actualValue);
		assertNull(subTypes.getValue());
		assertEquals(TelephoneTypeParameter.HOME.getValue(), subTypes.getType());

		//3.0
		version = VCardVersion.V3_0;
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		expectedValue = "+1 555-555-1234";
		assertEquals(expectedValue, actualValue);
		assertNull(subTypes.getValue());
		assertEquals(TelephoneTypeParameter.HOME.getValue(), subTypes.getType());

		//4.0
		version = VCardVersion.V4_0;
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		expectedValue = "tel:+1 555-555-1234";
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(TelephoneTypeParameter.HOME.getValue(), subTypes.getType());

		//xCard
		version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement("tel");
		xe.appendUri("tel:+1 555-555-1234");
		Document expectedDoc = xe.getDocument();
		xe = new XCardElement("tel");
		Document actualDoc = xe.getDocument();
		Element element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
	}

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
		TelephoneType t = new TelephoneType();
		t.addType(TelephoneTypeParameter.PREF);

		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));

		version = VCardVersion.V4_0;
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(1), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));

		//EMAIL has PREF parameter=======
		VCard vcard = new VCard();
		TelephoneType t1 = new TelephoneType();
		t1.setPref(1);
		vcard.addTelephoneNumber(t1);
		TelephoneType t2 = new TelephoneType();
		t2.setPref(2);
		vcard.addTelephoneNumber(t2);

		version = VCardVersion.V2_1;
		subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertNull(subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));

		version = VCardVersion.V4_0;
		subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(1), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(Integer.valueOf(2), subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(TelephoneTypeParameter.PREF.getValue()));
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		TelephoneType t;

		//2.1
		version = VCardVersion.V2_1;
		t = new TelephoneType();
		t.unmarshalText(subTypes, "+1 555-555-1234.", version, warnings, compatibilityMode);
		assertEquals("+1 555-555-1234.", t.getValue());

		//3.0
		version = VCardVersion.V3_0;
		t = new TelephoneType();
		t.unmarshalText(subTypes, "+1 555-555-1234.", version, warnings, compatibilityMode);
		assertEquals("+1 555-555-1234.", t.getValue());

		//4.0
		version = VCardVersion.V4_0;
		t = new TelephoneType();
		t.unmarshalText(subTypes, "tel:+1 555-555-1234.", version, warnings, compatibilityMode);
		assertEquals("+1 555-555-1234.", t.getValue());

		//xCard
		version = VCardVersion.V4_0;
		t = new TelephoneType();
		XCardElement xe = new XCardElement("tel");
		xe.appendUri("tel:+1 555-555-1234.");
		Element element = xe.getElement();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("+1 555-555-1234.", t.getValue());

		version = VCardVersion.V4_0;
		t = new TelephoneType();
		xe = new XCardElement("tel");
		xe.appendText("+1 555-555-1234.");
		element = xe.getElement();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("+1 555-555-1234.", t.getValue());
	}
}
