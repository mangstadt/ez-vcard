package ezvcard.io;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import ezvcard.VCardVersion;
import ezvcard.types.VCardType;
import ezvcard.util.XCardUtils;

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
 * An extended type class used for testing.
 * @author Michael Angstadt
 */
public class LuckyNumType extends VCardType {
	public int luckyNum;

	public LuckyNumType() {
		super("X-LUCKY-NUM");
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (luckyNum == 13) {
			throw new SkipMeException("Invalid lucky number.");
		}
		sb.append(luckyNum);
	}

	@Override
	protected void doMarshalValue(Element parent, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (luckyNum == 13) {
			throw new SkipMeException("Invalid lucky number.");
		}
		parent.setTextContent(luckyNum + "");
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		luckyNum = Integer.parseInt(value);
		if (luckyNum == 13) {
			throw new SkipMeException("Invalid lucky number.");
		}
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		Element ele = XCardUtils.getFirstElement(element.getElementsByTagName("integer"));
		luckyNum = Integer.parseInt(ele.getTextContent());
		if (luckyNum == 13) {
			throw new SkipMeException("Invalid lucky number.");
		}
	}

	@Override
	public QName getQName() {
		return new QName("http://luckynum.com", "lucky-num");
	}
}