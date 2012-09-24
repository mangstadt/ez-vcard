package ezvcard.types;

import java.util.List;

import org.w3c.dom.Element;

import ezvcard.VCardException;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
 * Holds the type value as-is. No escaping or unescaping is done on the value.
 * @author Michael Angstadt
 */
public class RawType extends VCardType {
	private String value;

	/**
	 * @param name the type name (e.g. "NOTE")
	 */
	public RawType(String name) {
		this(name, null);
	}

	/**
	 * @param name the type name (e.g. "NOTE")
	 * @param value the type value
	 */
	public RawType(String name, String value) {
		super(name);
		this.value = value;
	}

	/**
	 * Gets the raw value of the property.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the raw value of the property.
	 * @param value the value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		sb.append(value);
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		this.value = value;
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		Element value = XCardUtils.getFirstElement(element.getChildNodes());
		if (value == null) {
			this.value = element.getTextContent();
		} else {
			this.value = value.getTextContent();
		}
	}
}
