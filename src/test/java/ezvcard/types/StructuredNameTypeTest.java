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

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
public class StructuredNameTypeTest {
	@Test
	public void marshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		StructuredNameType t;
		String expected, actual;

		t = new StructuredNameType();
		t.setGiven("Jonathan");
		t.setFamily("Doe");
		t.addAdditional("Joh;nny,");
		t.addAdditional("John");
		t.addPrefix("Mr.");
		t.addSuffix("III");
		expected = "Doe;Jonathan;Joh\\;nny\\,,John;Mr.;III";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//some empty values
		t = new StructuredNameType();
		t.setGiven("Jonathan");
		t.setFamily(null);
		t.addAdditional("Joh;nny,");
		t.addAdditional("John");
		t.addSuffix("III");
		expected = ";Jonathan;Joh\\;nny\\,,John;;III";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//all empty values
		t = new StructuredNameType();
		expected = ";;;;";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		StructuredNameType t;
		Document expected, actual;
		Element element;

		t = new StructuredNameType();
		t.setGiven("Jonathan");
		t.setFamily("Doe");
		t.addAdditional("Joh;nny,");
		t.addAdditional("John");
		t.addPrefix("Mr.");
		t.addSuffix("III");

		XCardElement xe = new XCardElement("n");
		xe.append("surname", "Doe");
		xe.append("given", "Jonathan");
		xe.append("additional", "Joh;nny,");
		xe.append("additional", "John");
		xe.append("prefix", "Mr.");
		xe.append("suffix", "III");
		expected = xe.getDocument();

		xe = new XCardElement("n");
		actual = xe.getDocument();
		element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);

		//some empty values
		t = new StructuredNameType();
		t.setGiven("Jonathan");
		t.setFamily(null);
		t.addAdditional("Joh;nny,");
		t.addAdditional("John");
		t.addSuffix("III");

		xe = new XCardElement("n");
		xe.append("given", "Jonathan");
		xe.append("additional", "Joh;nny,");
		xe.append("additional", "John");
		xe.append("suffix", "III");
		expected = xe.getDocument();

		xe = new XCardElement("n");
		actual = xe.getDocument();
		element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		StructuredNameType t;

		t = new StructuredNameType();
		t.unmarshalText(subTypes, "Doe;Jonathan;Joh\\;nny\\,,John;Mr.;III", version, warnings, compatibilityMode);
		assertEquals("Doe", t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(2, t.getAdditional().size());
		assertTrue(t.getAdditional().contains("Joh;nny,"));
		assertTrue(t.getAdditional().contains("John"));
		assertEquals(1, t.getPrefixes().size());
		assertTrue(t.getPrefixes().contains("Mr."));
		assertEquals(1, t.getSuffixes().size());
		assertTrue(t.getSuffixes().contains("III"));

		//some empty values
		t = new StructuredNameType();
		t.unmarshalText(subTypes, ";Jonathan;Joh\\;nny\\,,John;;III", version, warnings, compatibilityMode);
		assertNull(t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(2, t.getAdditional().size());
		assertTrue(t.getAdditional().contains("Joh;nny,"));
		assertTrue(t.getAdditional().contains("John"));
		assertTrue(t.getPrefixes().isEmpty());
		assertEquals(1, t.getSuffixes().size());
		assertTrue(t.getSuffixes().contains("III"));

		//values missing off the end
		t = new StructuredNameType();
		t.unmarshalText(subTypes, "Doe;Jonathan;Joh\\;nny\\,,John", version, warnings, compatibilityMode);
		assertEquals("Doe", t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(2, t.getAdditional().size());
		assertTrue(t.getAdditional().contains("Joh;nny,"));
		assertTrue(t.getAdditional().contains("John"));
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());

		//all empty values
		t = new StructuredNameType();
		t.unmarshalText(subTypes, ";;;;", version, warnings, compatibilityMode);
		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertTrue(t.getAdditional().isEmpty());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
	}

	@Test
	public void unmarshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		StructuredNameType t;
		Element element;

		XCardElement xe = new XCardElement("n");
		xe.append("surname", "Doe");
		xe.append("given", "Jonathan");
		xe.append("additional", "Joh;nny,");
		xe.append("additional", "John");
		xe.append("prefix", "Mr.");
		xe.append("suffix", "III");
		element = xe.getElement();
		t = new StructuredNameType();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("Doe", t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(2, t.getAdditional().size());
		assertTrue(t.getAdditional().contains("Joh;nny,"));
		assertTrue(t.getAdditional().contains("John"));
		assertEquals(1, t.getPrefixes().size());
		assertTrue(t.getPrefixes().contains("Mr."));
		assertEquals(1, t.getSuffixes().size());
		assertTrue(t.getSuffixes().contains("III"));

		//some empty values
		xe = new XCardElement("n");
		xe.append("given", "Jonathan");
		xe.append("additional", "Joh;nny,");
		xe.append("additional", "John");
		xe.append("suffix", "III");
		element = xe.getElement();
		t = new StructuredNameType();
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertNull(t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(2, t.getAdditional().size());
		assertTrue(t.getAdditional().contains("Joh;nny,"));
		assertTrue(t.getAdditional().contains("John"));
		assertTrue(t.getPrefixes().isEmpty());
		assertEquals(1, t.getSuffixes().size());
		assertTrue(t.getSuffixes().contains("III"));
	}
}
