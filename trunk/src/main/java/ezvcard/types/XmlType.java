package ezvcard.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.util.XCardElement;
import ezvcard.util.XmlUtils;

/*
 Copyright (c) 2013, Michael Angstadt
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
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * XmlType xml = new XmlType(&quot;&lt;b&gt;Some xml&lt;/b&gt;&quot;);
 * vcard.addXml(xml);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>XML</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class XmlType extends TextType implements HasAltId {
	public static final String NAME = "XML";

	/**
	 * Creates an empty XML property.
	 */
	public XmlType() {
		super(NAME);
	}

	/**
	 * Creates an XML property.
	 * @param xml the XML element
	 */
	public XmlType(String xml) {
		super(NAME, xml);
	}

	//@Override
	public String getAltId() {
		return subTypes.getAltId();
	}

	//@Override
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

		Document document = XmlUtils.toDocument(value);
		return document.getDocumentElement();
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (value == null) {
			throw new SkipMeException("Property does not have a value associated with it.");
		}

		//parse the XML string
		Element root = null;
		try {
			Document document = XmlUtils.toDocument(value);
			root = XmlUtils.getRootElement(document);
		} catch (SAXException e) {
			throw new SkipMeException("Property value is not valid XML.");
		}

		//add XML element to marshalled document
		Node imported = parent.element().getOwnerDocument().importNode(root, true);
		parent.element().appendChild(imported);
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		Map<String, String> outputProperties = new HashMap<String, String>();
		outputProperties.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
		value = XmlUtils.toString(element.element(), outputProperties);
	}
}
