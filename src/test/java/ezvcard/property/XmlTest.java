package ezvcard.property;

import static ezvcard.property.PropertySensei.assertValidate;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ezvcard.VCardVersion;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
public class XmlTest {
	@Test
	public void validate() throws Throwable {
		Xml empty = new Xml((Document) null);
		assertValidate(empty).versions(VCardVersion.V2_1).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V3_0).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		Xml withValue = new Xml("<foo/>");
		assertValidate(withValue).versions(VCardVersion.V2_1).run(2);
		assertValidate(withValue).versions(VCardVersion.V3_0).run(2);
		assertValidate(withValue).versions(VCardVersion.V4_0).run();
	}

	@Test(expected = SAXException.class)
	public void invalid_xml() throws Throwable {
		new Xml("not valid XML");
	}

	@Test
	public void copy() throws Throwable {
		Xml property = new Xml("<root><foo attr=\"value\">text</foo></root>");
		Xml copy = new Xml(property);

		assertNotSame(property.getValue(), copy.getValue());
		assertXMLEqual(property.getValue(), copy.getValue());
		assertXMLNotSame(property.getValue().getDocumentElement(), copy.getValue().getDocumentElement());
	}

	@Test
	public void copy_null_value() throws Throwable {
		Xml property = new Xml((Document) null);
		Xml copy = new Xml(property);

		assertNull(property.getValue());
		assertNull(copy.getValue());
	}

	private static void assertXMLNotSame(Node node1, Node node2) {
		assertNotSame(node1, node2);

		NodeList children1 = node1.getChildNodes();
		NodeList children2 = node2.getChildNodes();
		for (int i = 0; i < children1.getLength(); i++) {
			Node child1 = children1.item(i);
			Node child2 = children2.item(i);
			assertXMLNotSame(child1, child2);
		}
	}
}
