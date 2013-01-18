package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
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
import ezvcard.parameters.RelatedTypeParameter;
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
public class RelatedTypeTest {
	@Test
	public void marshal() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		String actual;
		VCardSubTypes subTypes;

		//text
		RelatedType t = new RelatedType();
		t.addType(RelatedTypeParameter.SPOUSE);
		t.setText("Edna Smith");
		actual = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals("Edna Smith", actual);
		assertEquals(ValueParameter.TEXT, subTypes.getValue());
		assertEquals(RelatedTypeParameter.SPOUSE.getValue(), subTypes.getType());

		//URI
		t = new RelatedType();
		t.addType(RelatedTypeParameter.SPOUSE);
		t.setUri("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af");
		actual = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af", actual);
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(RelatedTypeParameter.SPOUSE.getValue(), subTypes.getType());
	}

	@Test
	public void marshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		//text
		RelatedType t = new RelatedType();
		t.setText("Edna Smith");
		String expectedXml = "<related xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		expectedXml += "<text>Edna Smith</text>";
		expectedXml += "</related>";
		Document expectedDoc = XmlUtils.toDocument(expectedXml);
		Document actualDoc = XmlUtils.toDocument("<related xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		Element element = XmlUtils.getRootElement(actualDoc);
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);

		//URI
		t = new RelatedType();
		t.setUri("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af");
		expectedXml = "<related xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		expectedXml += "<uri>urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af</uri>";
		expectedXml += "</related>";
		expectedDoc = XmlUtils.toDocument(expectedXml);
		actualDoc = XmlUtils.toDocument("<related xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		element = XmlUtils.getRootElement(actualDoc);
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		//text
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setType(RelatedTypeParameter.SPOUSE.getValue());
		t.unmarshalText(subTypes, "Edna Smith", version, warnings, compatibilityMode);
		assertEquals("Edna Smith", t.getText());
		assertNull(t.getUri());
		assertTrue(t.getTypes().contains(RelatedTypeParameter.SPOUSE));

		//URI
		t = new RelatedType();
		subTypes = new VCardSubTypes();
		subTypes.setType(RelatedTypeParameter.SPOUSE.getValue());
		subTypes.setValue(ValueParameter.URI);
		t.unmarshalText(subTypes, "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af", version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af", t.getUri());
		assertTrue(t.getTypes().contains(RelatedTypeParameter.SPOUSE));
	}

	@Test
	public void unmarshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		//text
		RelatedType t = new RelatedType();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setType(RelatedTypeParameter.SPOUSE.getValue());
		String xml = "<related xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		xml += "<text>Edna Smith</text>";
		xml += "</related>";
		Element element = XmlUtils.getRootElement(XmlUtils.toDocument(xml));
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("Edna Smith", t.getText());
		assertNull(t.getUri());
		assertTrue(t.getTypes().contains(RelatedTypeParameter.SPOUSE));

		//URI
		t = new RelatedType();
		subTypes = new VCardSubTypes();
		subTypes.setType(RelatedTypeParameter.SPOUSE.getValue());
		xml = "<related xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		xml += "<uri>urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af</uri>";
		xml += "</related>";
		element = XmlUtils.getRootElement(XmlUtils.toDocument(xml));
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertNull(t.getText());
		assertEquals("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af", t.getUri());
		assertTrue(t.getTypes().contains(RelatedTypeParameter.SPOUSE));
	}
}
