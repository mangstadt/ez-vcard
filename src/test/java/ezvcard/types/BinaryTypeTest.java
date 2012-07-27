package ezvcard.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.ImageTypeParameter;
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
 * @author Michael Angstadt
 */
public class BinaryTypeTest {
	private byte[] dummyData = "dummy data".getBytes();

	@Test
	public void doMarshalValue() {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC2426;
		BinaryTypeImpl t;
		String expected, actual;

		//test URL v2.1
		version = VCardVersion.V2_1;
		t = new BinaryTypeImpl();
		t.setUrl("http://example.com/image.jpg");
		expected = "http://example.com/image.jpg";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(ValueParameter.URL, t.getSubTypes().getValue());

		//test URL v3.0
		version = VCardVersion.V3_0;
		t = new BinaryTypeImpl();
		t.setUrl("http://example.com/image.jpg");
		expected = "http://example.com/image.jpg";
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(ValueParameter.URI, t.getSubTypes().getValue());

		//test base64 data v2.1
		version = VCardVersion.V2_1;
		t = new BinaryTypeImpl();
		t.setData(dummyData, ImageTypeParameter.JPEG);
		expected = Base64.encodeBase64String(dummyData);
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(EncodingParameter.BASE64, t.getSubTypes().getEncoding());
		assertEquals(ImageTypeParameter.JPEG.getValue(), t.getSubTypes().getType());

		//test base64 data v3.0
		version = VCardVersion.V3_0;
		t = new BinaryTypeImpl();
		t.setData(dummyData, ImageTypeParameter.JPEG);
		expected = Base64.encodeBase64String(dummyData);
		actual = t.doMarshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(EncodingParameter.B, t.getSubTypes().getEncoding());
		assertEquals(ImageTypeParameter.JPEG.getValue(), t.getSubTypes().getType());
	}

	@Test
	public void doUnmarshalValue() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC2426;
		VCardSubTypes subTypes;
		BinaryTypeImpl t;

		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setValue(ValueParameter.URL);
		t.unmarshalValue(subTypes, "http://example.com/image.jpg", version, warnings, compatibilityMode);
		assertEquals("http://example.com/image.jpg", t.getUrl());

		//no VALUE
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		t.unmarshalValue(subTypes, "http://example.com/image.jpg", version, warnings, compatibilityMode);
		assertEquals("http://example.com/image.jpg", t.getUrl());

		//"B" encoding
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.B);
		t.unmarshalValue(subTypes, Base64.encodeBase64String(dummyData), version, warnings, compatibilityMode);
		assertArrayEquals(dummyData, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getType());

		//"BASE64" encoding
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.BASE64);
		t.unmarshalValue(subTypes, Base64.encodeBase64String(dummyData), version, warnings, compatibilityMode);
		assertArrayEquals(dummyData, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getType());

		//no encoding
		//no type
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		t.unmarshalValue(subTypes, Base64.encodeBase64String(dummyData), version, warnings, compatibilityMode);
		assertArrayEquals(dummyData, t.getData());
	}

	/**
	 * Test implementation.
	 */
	private class BinaryTypeImpl extends BinaryType<ImageTypeParameter> {
		public BinaryTypeImpl() {
			super("NAME");
		}

		@Override
		protected ImageTypeParameter buildTypeObj(String type) {
			ImageTypeParameter param = ImageTypeParameter.valueOf(type);
			if (param == null) {
				param = new ImageTypeParameter(type);
			}
			return param;
		}
	}
}
