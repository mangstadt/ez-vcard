package ezvcard.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.util.XCardElement;
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
 * Any XML data attached to the vCard. This is used if the vCard was encoded in
 * XML (xCard standard) and it contained some non-standard elements.
 * 
 * <pre>
 * VCard vcard = new VCard();
 * XmlType xml = new XmlType(&quot;&lt;b&gt;Some xml&lt;/b&gt;&quot;);
 * vcard.addXml(xml);
 * </pre>
 * 
 * <p>
 * vCard property name: XML
 * </p>
 * <p>
 * vCard versions: 4.0
 * </p>
 * @author Michael Angstadt
 */
public class XmlType extends TextType {
	public static final String NAME = "XML";

	public XmlType() {
		super(NAME);
	}

	/**
	 * @param xml the XML element
	 */
	public XmlType(String xml) {
		super(NAME, xml);
	}

	/**
	 * Gets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the ALTID or null if it doesn't exist
	 * @see VCardSubTypes#getAltId
	 */
	public String getAltId() {
		return subTypes.getAltId();
	}

	/**
	 * Sets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param altId the ALTID or null to remove
	 * @see VCardSubTypes#setAltId
	 */
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	/**
	 * Converts the text value of this property to an XML {@link Element}
	 * object.
	 * @return the element object or null if this property's value is null
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public Element asElement() throws SAXException {
		if (value == null) {
			return null;
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(new InputSource(new StringReader(value)));
			return document.getDocumentElement();
		} catch (IOException e) {
			//never thrown because we're reading from a string
			return null;
		} catch (ParserConfigurationException e) {
			//never thrown because we're not doing anything fancy with the configuration
			return null;
		}
	}

	@Override
	protected void doMarshalValue(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (value == null) {
			throw new SkipMeException("Property does not have a value associated with it.");
		}

		//parse the XML string
		Element root = null;
		try {
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			DocumentBuilder builder = fact.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(value.getBytes()));
			root = XmlUtils.getRootElement(document);
		} catch (IOException e) {
			//never thrown because reading from a string
		} catch (ParserConfigurationException e) {
			//should never be thrown
		} catch (SAXException e) {
			throw new SkipMeException("Property value is not valid XML.");
		}

		//add XML element to marshalled document
		Node imported = parent.getElement().getOwnerDocument().importNode(root, true);
		parent.getElement().appendChild(imported);
	}

	@Override
	protected void doUnmarshalValue(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		try {
			StringWriter writer = new StringWriter();
			DOMSource source = new DOMSource(element.getElement());
			StreamResult result = new StreamResult(writer);
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(source, result);
			value = writer.toString();
		} catch (TransformerException e) {
			warnings.add("Problem transforming XML element to string for " + NAME + " property: " + e.getMessage());
		}
	}
}
