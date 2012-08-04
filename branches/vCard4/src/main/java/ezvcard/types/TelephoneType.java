package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.VCardStringUtils;

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
 * Represents the TEL type.
 * @author Michael Angstadt
 */
public class TelephoneType extends MultiValuedTypeParameterType<TelephoneTypeParameter> {
	public static final String NAME = "TEL";

	private String value;

	public TelephoneType() {
		this(null);
	}

	public TelephoneType(String value) {
		super(NAME);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	protected TelephoneTypeParameter buildTypeObj(String type) {
		TelephoneTypeParameter param = TelephoneTypeParameter.valueOf(type);
		if (param == null) {
			param = new TelephoneTypeParameter(type);
		}
		return param;
	}

	@Override
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		VCardSubTypes copy = new VCardSubTypes(subTypes);
		if (version == VCardVersion.V4_0) {
			copy.setValue(ValueParameter.URI);
		} else {
			copy.setValue(null);
		}
		return copy;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = VCardStringUtils.escapeText(this.value);
		if (version == VCardVersion.V4_0) {
			value = "tel:" + value;
		}
		return value;
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (value.matches("(?i)tel:.*")) {
			//remove "tel:"
			value = (value.length() > 4) ? value.substring(4) : "";
		}
		this.value = VCardStringUtils.unescape(value.trim());
	}

}