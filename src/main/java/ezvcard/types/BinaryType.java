package ezvcard.types;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.MediaTypeParameter;
import ezvcard.parameters.ValueParameter;

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
 * Represents a Type that contains binary data (for example, the "PHOTO" type).
 * @author Michael Angstadt
 * 
 * @param <T> the class used for wrapping the value of the TYPE parameter
 */
public abstract class BinaryType<T extends MediaTypeParameter> extends SingleValuedTypeParameterType<T> {
	/**
	 * The decoded data.
	 */
	private byte[] data;

	/**
	 * The URL to the resource.
	 */
	private String url;

	public BinaryType(String name) {
		super(name);
	}

	public BinaryType(String name, String url, T type) {
		this(name);
		setUrl(url, type);
	}

	public BinaryType(String name, byte[] data, T type) {
		this(name);
		setData(data, type);
	}

	public byte[] getData() {
		return data;
	}

	public String getUrl() {
		return url;
	}

	private void setUrl(String url) {
		this.url = url;
		this.data = null;
	}

	public void setUrl(String url, T type) {
		setUrl(url);
		setType(type);
	}

	private void setData(byte[] data) {
		this.url = null;
		this.data = data;
	}

	public void setData(byte[] data, T type) {
		setData(data);
		setType(type);
	}

	@Override
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		VCardSubTypes copy = new VCardSubTypes(subTypes);

		if (url != null) {
			ValueParameter vp = null;
			switch (version) {
			case V2_1:
				vp = ValueParameter.URL;
				break;
			case V3_0:
			case V4_0:
				vp = ValueParameter.URI;
				break;
			}
			copy.setValue(vp);
			copy.setEncoding(null);

			if (version == VCardVersion.V4_0) {
				MediaTypeParameter p = getType();
				if (p != null) {
					copy.setMediaType(p.getMediaType());
				}
			}
		}
		if (data != null) {
			if (version == VCardVersion.V2_1) {
				copy.setEncoding(EncodingParameter.BASE64);
				copy.setValue(null);
			} else if (version == VCardVersion.V3_0) {
				copy.setEncoding(EncodingParameter.B);
				copy.setValue(null);
			} else {
				copy.setEncoding(null);
				copy.setValue(ValueParameter.URI);
				copy.setType(null);
			}
		}

		return copy;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (url != null) {
			return url;
		}
		if (data != null) {
			String base64 = new String(Base64.encodeBase64(data));
			if (version == VCardVersion.V4_0) {
				MediaTypeParameter p = getType();
				String mediaType = (p == null) ? "application/octet-stream" : p.getMediaType();
				return "data:" + mediaType + ";base64," + base64;
			} else {
				return base64;
			}
		}
		return null;
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		//check for a data URI (v4.0)
		Pattern p = Pattern.compile("^data:(.*?);base64,(.*)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(value);
		if (m.find()) {
			String mediaType = m.group(1);
			String base64 = m.group(2);

			T param = buildMediaTypeObj(mediaType);
			setData(Base64.decodeBase64(base64), param);
			return;
		}

		//check for a URL
		ValueParameter valueSubType = subTypes.getValue();
		if (valueSubType == ValueParameter.URL || valueSubType == ValueParameter.URI) {
			String mediaType = subTypes.getMediaType();
			if (mediaType != null) {
				//4.0 URLs should have a MEDIATYPE parameter
				T param = buildMediaTypeObj(mediaType);
				setUrl(value, param);
			} else {
				setUrl(value);
			}
			return;
		}

		//check for base64 data
		EncodingParameter encodingSubType = subTypes.getEncoding();
		if (encodingSubType != null) {
			if (encodingSubType != EncodingParameter.B && encodingSubType != EncodingParameter.BASE64) {
				warnings.add("Unrecognized ENCODING type \"" + encodingSubType + "\".  Attempting to decode as base64.");
			}
			setData(Base64.decodeBase64(value));
			return;
		}

		//the required parameters weren't defined, make a guess 
		if (value.matches("(?i)http.*")) {
			warnings.add("No VALUE or ENCODING type given.  Assuming it's a URL.");

			String mediaType = subTypes.getMediaType();
			if (mediaType != null) {
				//4.0 URLs should have a MEDIATYPE parameter
				T param = buildMediaTypeObj(mediaType);
				setUrl(value, param);
			} else {
				setUrl(value);
			}
		} else {
			warnings.add("No VALUE or ENCODING type given.  Attempting to decode as base64.");
			setData(Base64.decodeBase64(value));
		}
	}

	/**
	 * Gets a {@link MediaTypeParameter} object based on the media type string
	 * in the data URI of 4.0 vCards.
	 * @param mediaType the media type string (e.g. "image/jpeg")
	 * @return the parameter object
	 */
	protected abstract T buildMediaTypeObj(String mediaType);
}
