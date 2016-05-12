package ezvcard.property.asserter;

import static ezvcard.util.TestUtils.assertIntEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import ezvcard.parameter.MediaTypeParameter;
import ezvcard.property.BinaryProperty;

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
 * @author Michael Angstadt
 */
@SuppressWarnings("rawtypes")
public class BinaryPropertyAsserter<T extends BinaryProperty> extends PropertyAsserter<BinaryPropertyAsserter<T>, T> {
	private MediaTypeParameter contentType;
	private Integer dataLength;
	private String url;

	public BinaryPropertyAsserter(List<T> properties, VCardAsserter asserter) {
		super(properties, asserter);
	}

	public BinaryPropertyAsserter<T> contentType(MediaTypeParameter contentType) {
		this.contentType = contentType;
		return this_;
	}

	public BinaryPropertyAsserter<T> dataLength(int dataLength) {
		this.dataLength = dataLength;
		return this_;
	}

	public BinaryPropertyAsserter<T> url(String url) {
		this.url = url;
		return this_;
	}

	@Override
	protected void _run(T property) {
		assertEquals(contentType, property.getContentType());

		if (dataLength == null) {
			assertNull(property.getData());
		} else {
			assertIntEquals(dataLength, property.getData().length);
		}

		assertEquals(url, property.getUrl());
	}

	@Override
	protected void _reset() {
		contentType = null;
		dataLength = null;
		url = null;
	}
}