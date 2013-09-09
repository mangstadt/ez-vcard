package ezvcard.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
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
 * <b>Property name:</b> {@code XML}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 */
public class XmlType extends VCardType implements HasAltId {
	public static final String NAME = "XML";

	private Document document;

	/**
	 * Creates an empty XML property.
	 */
	public XmlType() {
		this((Document) null);
	}

	/**
	 * Creates an XML property.
	 * @param xml the XML to use as the property's value
	 * @throws SAXException if the XML cannot be parsed
	 */
	public XmlType(String xml) throws SAXException {
		this(XmlUtils.toDocument(xml));
	}

	/**
	 * Creates an XML property.
	 * @param element the XML element to use as the property's value (the
	 * element is imported into an empty {@link Document} object)
	 */
	public XmlType(Element element) {
		this(detachElement(element));
	}

	/**
	 * Creates an XML property.
	 * @param document the XML document to use as the property's value
	 */
	public XmlType(Document document) {
		super(NAME);
		this.document = document;
	}

	/**
	 * Gets the value of this property.
	 * @return the XML DOM or null if not set
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the value of this property.
	 * @param document the XML DOM or null to remove
	 */
	public void setDocument(Document document) {
		this.document = document;
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

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		String xml = write();
		sb.append(VCardStringUtils.escape(xml));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		parse(value);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		if (document == null) {
			return;
		}

		//add XML element to marshalled document
		Element root = XmlUtils.getRootElement(document);
		Node imported = parent.element().getOwnerDocument().importNode(root, true);
		parent.element().appendChild(imported);
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		document = detachElement(element.element());
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		parse(element.value());
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		String value = write();
		return JCardValue.single(VCardDataType.TEXT, value);
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		parse(value.getSingleValued());
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (document == null) {
			warnings.add("Property value is null.");
		}
	}

	private void parse(String xml) {
		if (xml == null || xml.length() == 0) {
			return;
		}

		try {
			document = XmlUtils.toDocument(xml);
		} catch (SAXException e) {
			throw new CannotParseException("Cannot parse value as XML.");
		}
	}

	private String write() {
		if (document == null) {
			return "";
		}

		Map<String, String> props = new HashMap<String, String>();
		props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
		return XmlUtils.toString(document, props);
	}

	private static Document detachElement(Element element) {
		Document document = XmlUtils.createDocument();
		Node imported = document.importNode(element, true);
		document.appendChild(imported);
		return document;
	}
}
