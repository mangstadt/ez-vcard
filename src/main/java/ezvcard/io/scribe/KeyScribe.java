package ezvcard.io.scribe;

import ezvcard.parameter.KeyType;
import ezvcard.property.Key;

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
 */

/**
 * Marshals {@link Key} properties.
 * @author Michael Angstadt
 */
public class KeyScribe extends BinaryPropertyScribe<Key, KeyType> {
	public KeyScribe() {
		super(Key.class, "KEY");
	}

	@Override
	protected KeyType _mediaTypeFromTypeParameter(String type) {
		return KeyType.get(type, null, null);
	}

	@Override
	protected KeyType _mediaTypeFromMediaTypeParameter(String mediaType) {
		return KeyType.get(null, mediaType, null);
	}

	@Override
	protected KeyType _mediaTypeFromFileExtension(String extension) {
		return KeyType.find(null, null, extension);
	}

	@Override
	protected Key _newInstance(String uri, KeyType contentType) {
		return new Key(uri, contentType);
	}

	@Override
	protected Key _newInstance(byte[] data, KeyType contentType) {
		return new Key(data, contentType);
	}
}
