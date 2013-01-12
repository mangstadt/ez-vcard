package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.XmlUtils;

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
public class TimezoneTypeTest {
	@Test
	public void setMinuteOffset() {
		try {
			new TimezoneType().setMinuteOffset(-1);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		try {
			new TimezoneType().setMinuteOffset(60);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		//nulls allowed
		new TimezoneType().setMinuteOffset(null);

		try {
			new TimezoneType(0, -1);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		try {
			new TimezoneType(0, 60);
		} catch (IllegalArgumentException e) {
			//should be thrown
		}
	}

	@Test
	public void toTimeZone() {
		TimezoneType t = new TimezoneType(-5, 30);
		TimeZone tz = t.toTimeZone();
		assertEquals(-(5 * 1000 * 60 * 60 + 30 * 1000 * 60), tz.getRawOffset());

		t = new TimezoneType("America/New_York");
		tz = t.toTimeZone();
		assertNull(tz);
	}

	@Test
	public void marshal() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		TimezoneType t;
		String expectedValue, actualValue;
		VCardSubTypes subTypes;

		//just offset=======
		t = new TimezoneType(-5, 30);

		//2.1
		version = VCardVersion.V2_1;
		expectedValue = "-05:30";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertNull(subTypes.getValue());

		//3.0
		version = VCardVersion.V3_0;
		expectedValue = "-05:30";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertNull(subTypes.getValue());

		//4.0
		version = VCardVersion.V4_0;
		expectedValue = "-05:30";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.UTC_OFFSET, subTypes.getValue());

		//xCard
		version = VCardVersion.V4_0;
		String expectedXml = "<tz xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		expectedXml += "<utc-offset>-05:30</utc-offset>";
		expectedXml += "</tz>";
		Document expectedDoc = XmlUtils.toDocument(expectedXml);
		Document actualDoc = XmlUtils.toDocument("<tz xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		Element element = XmlUtils.getRootElement(actualDoc);
		t.marshalValue(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);

		//just text======
		t = new TimezoneType("America/New_York");

		//2.1
		version = VCardVersion.V2_1;
		expectedValue = "America/New_York";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());

		//3.0
		version = VCardVersion.V3_0;
		expectedValue = "America/New_York";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());

		//4.0
		version = VCardVersion.V4_0;
		expectedValue = "America/New_York";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());

		//xCard
		version = VCardVersion.V4_0;
		expectedXml = "<tz xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		expectedXml += "<text>America/New_York</text>";
		expectedXml += "</tz>";
		expectedDoc = XmlUtils.toDocument(expectedXml);
		actualDoc = XmlUtils.toDocument("<tz xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		element = XmlUtils.getRootElement(actualDoc);
		t.marshalValue(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);

		//offset and text=====
		t = new TimezoneType(-5, 30, "America/New_York");

		//2.1
		version = VCardVersion.V2_1;
		expectedValue = "-05:30;America/New_York";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());

		//3.0
		version = VCardVersion.V3_0;
		expectedValue = "-05:30;America/New_York";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());

		//4.0
		version = VCardVersion.V4_0;
		expectedValue = "America/New_York";
		actualValue = t.marshalValue(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());

		//xCard
		version = VCardVersion.V4_0;
		expectedXml = "<tz xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		expectedXml += "<text>America/New_York</text>";
		expectedXml += "</tz>";
		expectedDoc = XmlUtils.toDocument(expectedXml);
		actualDoc = XmlUtils.toDocument("<tz xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		element = XmlUtils.getRootElement(actualDoc);
		t.marshalValue(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);

		//no offset, no text====
		t = new TimezoneType();

		//2.1
		version = VCardVersion.V2_1;
		try {
			actualValue = t.marshalValue(version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
		}

		//3.0
		version = VCardVersion.V3_0;
		try {
			actualValue = t.marshalValue(version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
		}

		//4.0
		version = VCardVersion.V4_0;
		try {
			actualValue = t.marshalValue(version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
		}

		//xCard
		version = VCardVersion.V4_0;
		actualDoc = XmlUtils.toDocument("<tz xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		element = XmlUtils.getRootElement(actualDoc);
		try {
			t.marshalValue(element, version, warnings, compatibilityMode);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
		}
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1; //unmarshalled the same in all versions
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		TimezoneType t;

		//offset
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "-05:30", version, warnings, compatibilityMode);
		assertEquals(Integer.valueOf(-5), t.getHourOffset());
		assertEquals(Integer.valueOf(30), t.getMinuteOffset());
		assertNull(t.getText());

		//text
		version = VCardVersion.V2_1;
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "America/New_York", version, warnings, compatibilityMode);
		assertNull(t.getHourOffset());
		assertNull(t.getMinuteOffset());
		assertEquals("America/New_York", t.getText());

		//text that starts with offset
		version = VCardVersion.V2_1;
		t = new TimezoneType();
		t.unmarshalValue(subTypes, "-05:30; EST; America/New_York", version, warnings, compatibilityMode);
		assertEquals(Integer.valueOf(-5), t.getHourOffset());
		assertEquals(Integer.valueOf(30), t.getMinuteOffset());
		assertEquals("; EST; America/New_York", t.getText());
	}
}
