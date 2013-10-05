package ezvcard.io.scribe;

import ezvcard.parameters.KeyTypeParameter;
import ezvcard.property.KeyType;

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
 */

/**
 * Marshals {@link KeyType} properties.
 * @author Michael Angstadt
 */
public class KeyScribe extends BinaryPropertyScribe<KeyType, KeyTypeParameter> {
	public KeyScribe() {
		super(KeyType.class, "KEY");
	}

	@Override
	protected KeyTypeParameter _buildTypeObj(String type) {
		return KeyTypeParameter.get(type, null, null);
	}

	@Override
	protected KeyTypeParameter _buildMediaTypeObj(String mediaType) {
		return KeyTypeParameter.get(null, mediaType, null);
	}

	@Override
	protected KeyType _newInstance(String uri, KeyTypeParameter contentType) {
		return new KeyType(uri, contentType);
	}

	@Override
	protected KeyType _newInstance(byte[] data, KeyTypeParameter contentType) {
		return new KeyType(data, contentType);
	}
}
