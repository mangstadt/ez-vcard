package ezvcard.util;

import ezvcard.VCardVersion;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Basic class for building an xCard string.
 * @author Michael Angstadt
 */
public class XCardBuilder {
	private final StringBuilder sb = new StringBuilder();
	private int count = 0;

	public XCardBuilder() {
		sb.append("<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">");
		begin();
	}

	public XCardBuilder begin() {
		if (count > 0) {
			sb.append("</vcard>");
		}
		sb.append("<vcard>");
		count++;
		return this;
	}

	public XCardBuilder prop(String name, String xml) {
		return prop(null, name, xml);
	}

	public XCardBuilder prop(String ns, String name, String xml) {
		sb.append('<').append(name);
		if (ns != null) {
			sb.append(" xmlns=\"").append(ns).append("\"");
		}
		sb.append('>');
		sb.append(xml);
		sb.append("</").append(name).append('>');
		return this;
	}

	public int size() {
		return count;
	}

	@Override
	public String toString() {
		return sb.toString() + "</vcard></vcards>";
	}
}