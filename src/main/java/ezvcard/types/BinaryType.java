package ezvcard.types;

import java.util.List;

import org.apache.commons.codec.binary.Base64;

import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.TypeParameter;
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
public abstract class BinaryType<T extends TypeParameter> extends SingleValuedTypeParameterType<T> {
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
	
	public BinaryType(String name, String url){
		this(name);
		setUrl(url);
	}
	
	public BinaryType(String name, byte[] data, T type){
		this(name);
		setData(data, type);
	}

	public byte[] getData() {
		return data;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		this.data = null;
	}

	public void setData(byte[] data, T type) {
		this.url = null;
		this.data = data;
		setType(type);
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (url != null) {
			subTypes.setValue(ValueParameter.URI);
			subTypes.removeAll(EncodingParameter.NAME);
			return url;
		}
		if (data != null) {
			subTypes.removeAll(ValueParameter.NAME);
			subTypes.setEncoding(EncodingParameter.B);
			return new String(Base64.encodeBase64(data));
		}
		return null;
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		ValueParameter valueSubType = subTypes.getValue();
		if (valueSubType == ValueParameter.URI || valueSubType == ValueParameter.CONTENT_ID) {
			//the value is a URI, not binary data
			url = value;
		} else {
			EncodingParameter encodingSubType = subTypes.getEncoding();
			if (encodingSubType != null) {
				if (encodingSubType != EncodingParameter.B && encodingSubType != EncodingParameter.BASE64) {
					warnings.add("Unrecognized ENCODING type \"" + encodingSubType + "\".  Attempting to decode as base64.");
				}
				data = Base64.decodeBase64(value);
			} else {
				//the required Sub Types weren't included, make a guess 
				if (value.startsWith("http")) {
					warnings.add("No VALUE or ENCODING type given.  Assuming it's a URL.");
					url = value;
				} else {
					warnings.add("No VALUE or ENCODING type given.  Attempting to decode as base64.");
					data = Base64.decodeBase64(value);
				}
			}
		}
	}
}
