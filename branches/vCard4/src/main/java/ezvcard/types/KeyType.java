package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.KeyTypeParameter;
import ezvcard.parameters.MediaTypeParameter;
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
 * Represents the KEY type.
 * @author Michael Angstadt
 */
public class KeyType extends BinaryType<KeyTypeParameter> {
	public static final String NAME = "KEY";

	private String text;

	public KeyType() {
		super(NAME);
	}

	/**
	 * @param data the binary data
	 * @param type the type of key
	 */
	public KeyType(byte data[], KeyTypeParameter type) {
		super(NAME, data, type);
	}

	/**
	 * @param url the URL to the key (vCard 4.0 only)
	 * @param type the type of key
	 */
	public KeyType(String url, KeyTypeParameter type) {
		super(NAME, url, type);
	}

	/**
	 * Sets a plain text representation of the key.
	 * @param key the key in plain text
	 * @param type the key type
	 */
	public void setText(String text, KeyTypeParameter type) {
		this.text = text;
		data = null;
		url = null;
		setContentType(type);
	}

	/**
	 * Gets the plain text representation of the key.
	 * @return the key in plain text
	 */
	public String getText() {
		return text;
	}

	@Override
	protected KeyTypeParameter buildTypeObj(String type) {
		KeyTypeParameter param = KeyTypeParameter.valueOf(type);
		if (param == null) {
			param = new KeyTypeParameter(type, "application/" + type, null);
		}
		return param;
	}

	@Override
	protected KeyTypeParameter buildMediaTypeObj(String mediaType) {
		KeyTypeParameter p = KeyTypeParameter.findByMediaType(mediaType);
		if (p == null) {
			int slashPos = mediaType.indexOf('/');
			String type;
			if (slashPos == -1 || slashPos < mediaType.length() - 1) {
				type = "";
			} else {
				type = mediaType.substring(slashPos + 1);
			}
			p = new KeyTypeParameter(type, mediaType, null);
		}
		return p;
	}

	@Override
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		if (text != null) {
			VCardSubTypes copy = new VCardSubTypes(subTypes);
			
			MediaTypeParameter contentType = getContentType();
			if (contentType == null) {
				contentType = new MediaTypeParameter(null, null, null);
			}
			
			copy.setValue(ValueParameter.TEXT);
			copy.setEncoding(null);
			if (version == VCardVersion.V4_0) {
				//don't null out TYPE, it could be set to "home" or "work"
				copy.setMediaType(contentType.getMediaType());
			} else {
				copy.setType(contentType.getValue());
				copy.setMediaType(null);
			}
			return copy;
		}
		return super.doMarshalSubTypes(version, warnings, compatibilityMode, vcard);
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (text != null) {
			return VCardStringUtils.escapeText(text);
		}

		if ((version == VCardVersion.V2_1 || version == VCardVersion.V3_0) && getUrl() != null) {
			warnings.add("vCard version " + version + " does not allow URLs to be used in the " + NAME + " type.");
		}
		return super.doMarshalValue(version, warnings, compatibilityMode);
	}

	@Override
	protected void cannotUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, KeyTypeParameter contentType) {
		//unmarshal it as a plain text key
		setText(VCardStringUtils.unescape(value), contentType);
	}
}
