package ezvcard.io;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ezvcard.VCardVersion;
import ezvcard.types.VCardType;
import ezvcard.util.HCardElement;
import ezvcard.util.XCardElement;

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
 * An extended type class used for testing that contains XML marshalling methods
 * and a QName.
 * @author Michael Angstadt
 */
public class LuckyNumType extends VCardType {
	private static final QName qname = new QName("http://luckynum.com", "lucky-num");
	public int luckyNum;

	public LuckyNumType() {
		super("X-LUCKY-NUM");
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		if (luckyNum == 13) {
			throw new SkipMeException("Invalid lucky number.");
		}
		sb.append(luckyNum);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		if (luckyNum == 13) {
			throw new SkipMeException("Invalid lucky number.");
		}
		parent.element().setTextContent(luckyNum + "");
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		luckyNum = Integer.parseInt(value);
		if (luckyNum == 13) {
			throw new SkipMeException("Invalid lucky number.");
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		NodeList nodeList = element.element().getElementsByTagNameNS(qname.getNamespaceURI(), "num");
		if (nodeList.getLength() > 0) {
			Element num = (Element) nodeList.item(0);
			luckyNum = Integer.parseInt(num.getTextContent());
			if (luckyNum == 13) {
				throw new SkipMeException("Invalid lucky number.");
			}
		}
	}

	@Override
	public QName getQName() {
		return qname;
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		luckyNum = Integer.parseInt(element.value());
	}
}