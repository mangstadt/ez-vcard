package ezvcard.types;

import ezvcard.parameters.KeyTypeParameter;

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

	public KeyType() {
		super(NAME);
	}

	public KeyType(byte data[], KeyTypeParameter type) {
		super(NAME, data, type);
	}

	@Override
	protected KeyTypeParameter buildTypeObj(String type) {
		KeyTypeParameter param = KeyTypeParameter.valueOf(type);
		if (param == null) {
			param = new KeyTypeParameter(type, null, null);
		}
		return param;
	}

	/**
	 * URL values are not supported by the KEY type.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public String getUrl() {
		throw new UnsupportedOperationException("URL values are not supported by the KEY type.");
	}

	/**
	 * URL values are not supported by the KEY type.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void setUrl(String url, KeyTypeParameter type) {
		throw new UnsupportedOperationException("URL values are not supported by the KEY type.");
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
}
