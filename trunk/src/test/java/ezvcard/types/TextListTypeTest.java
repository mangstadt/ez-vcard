package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
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
public class TextListTypeTest {
	@Test
	public void marshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		TextListType t;
		String expected, actual;

		//three items
		t = new TextListType("NAME", ',');
		t.addValue("One");
		t.addValue("One and a half");
		t.addValue("T,wo");
		t.removeValue("One and a half"); //test "removeValue"
		t.addValue("Thr;ee");
		expected = "One,T\\,wo,Thr\\;ee";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//two items
		t = new TextListType("NAME", ',');
		t.addValue("One");
		t.addValue("Two");
		expected = "One,Two";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//one item
		t = new TextListType("NAME", ',');
		t.addValue("One");
		expected = "One";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);

		//zero items
		t = new TextListType("NAME", ',');
		expected = "";
		actual = t.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;

		//three items
		TextListType t = new TextListType("NAME", ',');
		t.addValue("One");
		t.addValue("One and a half");
		t.addValue("Two");
		t.removeValue("One and a half"); //test "removeValue"
		t.addValue("Three");
		XCardElement xe = new XCardElement("name");
		xe.appendText("One");
		xe.appendText("Two");
		xe.appendText("Three");
		Document expectedDoc = xe.getDocument();
		xe = new XCardElement("name");
		Document actualDoc = xe.getDocument();
		Element element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);

		//two items
		t = new TextListType("NAME", ',');
		t.addValue("One");
		t.addValue("Two");
		xe = new XCardElement("name");
		xe.appendText("One");
		xe.appendText("Two");
		expectedDoc = xe.getDocument();
		xe = new XCardElement("name");
		actualDoc = xe.getDocument();
		element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);

		//one item
		t = new TextListType("NAME", ',');
		t.addValue("One");
		xe = new XCardElement("name");
		xe.appendText("One");
		expectedDoc = xe.getDocument();
		xe = new XCardElement("name");
		actualDoc = xe.getDocument();
		element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);

		//zero items
		t = new TextListType("NAME", ',');
		xe = new XCardElement("name");
		expectedDoc = xe.getDocument();
		xe = new XCardElement("name");
		actualDoc = xe.getDocument();
		element = xe.getElement();
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		TextListType t;
		List<String> expected, actual;

		//three values
		t = new TextListType("NAME", ',');
		t.unmarshalText(subTypes, "One,T\\,wo,Thr\\;ee", version, warnings, compatibilityMode);
		expected = Arrays.asList("One", "T,wo", "Thr;ee");
		actual = t.getValues();
		assertEquals(expected, actual);

		//two values
		t = new TextListType("NAME", ',');
		t.unmarshalText(subTypes, "One,Two", version, warnings, compatibilityMode);
		expected = Arrays.asList("One", "Two");
		actual = t.getValues();
		assertEquals(expected, actual);

		//one value
		t = new TextListType("NAME", ',');
		t.unmarshalText(subTypes, "One", version, warnings, compatibilityMode);
		expected = Arrays.asList("One");
		actual = t.getValues();
		assertEquals(expected, actual);

		//zero values
		t = new TextListType("NAME", ',');
		t.unmarshalText(subTypes, "", version, warnings, compatibilityMode);
		expected = Arrays.asList();
		actual = t.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void unmarshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		TextListType t;
		List<String> expected, actual;
		Element element;

		//three values
		XCardElement xe = new XCardElement("name");
		xe.appendText("One");
		xe.appendText("Two");
		xe.appendText("Three");
		element = xe.getElement();
		t = new TextListType("NAME", ',');
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		expected = Arrays.asList("One", "Two", "Three");
		actual = t.getValues();
		assertEquals(expected, actual);

		//two values
		xe = new XCardElement("name");
		xe.appendText("One");
		xe.appendText("Two");
		element = xe.getElement();
		t = new TextListType("NAME", ',');
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		expected = Arrays.asList("One", "Two");
		actual = t.getValues();
		assertEquals(expected, actual);

		//one value
		xe = new XCardElement("name");
		xe.appendText("One");
		element = xe.getElement();
		t = new TextListType("NAME", ',');
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		expected = Arrays.asList("One");
		actual = t.getValues();
		assertEquals(expected, actual);

		//zero values
		xe = new XCardElement("name");
		element = xe.getElement();
		t = new TextListType("NAME", ',');
		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		expected = Arrays.asList();
		actual = t.getValues();
		assertEquals(expected, actual);
	}
}
