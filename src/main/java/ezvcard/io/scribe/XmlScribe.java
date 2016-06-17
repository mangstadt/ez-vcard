package ezvcard.io.scribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Xml;
import ezvcard.util.XmlUtils;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 */

/**
 * Marshals {@link Xml} properties.
 * @author Michael Angstadt
 */
public class XmlScribe extends VCardPropertyScribe<Xml> {
	public XmlScribe() {
		super(Xml.class, "XML");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(Xml property, WriteContext context) {
		Document value = property.getValue();
		if (value == null) {
			return "";
		}

		String xml = valueToString(value);
		return escape(xml);
	}

	@Override
	protected Xml _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		value = unescape(value);
		try {
			return new Xml(value);
		} catch (SAXException e) {
			throw new CannotParseException(21);
		}
	}

	@Override
	protected void _writeXml(Xml property, XCardElement element) {
		//Xml properties are handled as a special case when writing xCard documents, so this method should never get called (see: "XCardDocument" class)
		super._writeXml(property, element);
	}

	@Override
	protected Xml _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		Xml xml = new Xml(element.element());

		//remove the <parameters> element
		Element root = xml.getValue().getDocumentElement();
		for (Element child : XmlUtils.toElementList(root.getChildNodes())) {
			if ("parameters".equals(child.getLocalName()) && VCardVersion.V4_0.getXmlNamespace().equals(child.getNamespaceURI())) {
				root.removeChild(child);
			}
		}

		return xml;
	}

	@Override
	protected JCardValue _writeJson(Xml property) {
		String xml = null;
		Document value = property.getValue();
		if (value != null) {
			xml = valueToString(value);
		}

		return JCardValue.single(xml);
	}

	@Override
	protected Xml _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		try {
			String xml = value.asSingle();
			return (xml.length() == 0) ? new Xml((Document) null) : new Xml(xml);
		} catch (SAXException e) {
			throw new CannotParseException(22);
		}
	}

	private String valueToString(Document document) {
		Map<String, String> props = new HashMap<String, String>();
		props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
		return XmlUtils.toString(document, props);
	}
}