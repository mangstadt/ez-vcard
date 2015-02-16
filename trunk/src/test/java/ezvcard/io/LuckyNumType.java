package ezvcard.io;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;

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
 * An extended type class used for testing that contains XML marshalling methods
 * and a QName.
 * @author Michael Angstadt
 */
public class LuckyNumType extends VCardProperty {
	public int luckyNum;

	public LuckyNumType(int luckyNum) {
		this.luckyNum = luckyNum;
	}

	public static class LuckyNumScribe extends VCardPropertyScribe<LuckyNumType> {
		public LuckyNumScribe() {
			super(LuckyNumType.class, "X-LUCKY-NUM", new QName("http://luckynum.com", "lucky-num"));
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return VCardDataType.INTEGER;
		}

		@Override
		protected String _writeText(LuckyNumType property, VCardVersion version) {
			return property.luckyNum + "";
		}

		@Override
		protected LuckyNumType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			int luckyNum = Integer.parseInt(value);
			return new LuckyNumType(luckyNum);
		}

		@Override
		protected void _writeXml(LuckyNumType property, XCardElement parent) {
			int luckyNum = property.luckyNum;
			parent.element().setTextContent(luckyNum + "");
		}

		@Override
		protected LuckyNumType _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
			NodeList nodeList = element.element().getElementsByTagNameNS(qname.getNamespaceURI(), "num");
			if (nodeList.getLength() == 0) {
				return new LuckyNumType(0);
			}

			Element num = (Element) nodeList.item(0);
			int luckyNum = Integer.parseInt(num.getTextContent());

			return new LuckyNumType(luckyNum);
		}

		@Override
		protected LuckyNumType _parseHtml(HCardElement element, List<String> warnings) {
			return new LuckyNumType(Integer.parseInt(element.value()));
		}
	}
}