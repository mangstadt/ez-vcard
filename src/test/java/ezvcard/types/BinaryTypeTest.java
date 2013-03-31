package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.XCardElement;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class BinaryTypeTest {
	private final String url = "http://example.com/image.jpg";
	private final byte[] data = "data".getBytes();
	private final String base64Data = Base64.encodeBase64String(data);
	private final String dataUri = "data:image/jpeg;base64," + base64Data;

	private final BinaryTypeImpl urlType = new BinaryTypeImpl();
	{
		urlType.setUrl(url, ImageTypeParameter.JPEG);
	}

	private final BinaryTypeImpl binaryType = new BinaryTypeImpl();
	{
		binaryType.setData(data, ImageTypeParameter.JPEG);
		binaryType.setType("work");
	}

	@Test
	public void marshalSubTypes_url_2_1() {
		VCardSubTypes subTypes = urlType.marshalSubTypes(VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC, new VCard());
		assertEquals(ValueParameter.URL, subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());
	}

	@Test
	public void marshalSubTypes_url_3_0() {
		VCardSubTypes subTypes = urlType.marshalSubTypes(VCardVersion.V3_0, new ArrayList<String>(), CompatibilityMode.RFC, new VCard());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());
	}

	@Test
	public void marshalSubTypes_url_4_0() {
		VCardSubTypes subTypes = urlType.marshalSubTypes(VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC, new VCard());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertNull(subTypes.getType());
		assertEquals(ImageTypeParameter.JPEG.getMediaType(), subTypes.getMediaType());
	}

	@Test
	public void marshalSubTypes_binary_2_1() {
		VCardSubTypes subTypes = binaryType.marshalSubTypes(VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC, new VCard());
		assertEquals(EncodingParameter.BASE64, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
	}

	@Test
	public void marshalSubTypes_binary_3_0() {
		VCardSubTypes subTypes = binaryType.marshalSubTypes(VCardVersion.V3_0, new ArrayList<String>(), CompatibilityMode.RFC, new VCard());
		assertEquals(EncodingParameter.B, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
	}

	@Test
	public void marshalSubTypes_binary_4_0() {
		VCardSubTypes subTypes = binaryType.marshalSubTypes(VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC, new VCard());
		assertNull(subTypes.getEncoding());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals("work", subTypes.getType());
	}

	@Test
	public void marshalText_url_2_1() {
		String actual = urlType.marshalText(VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(url, actual);
	}

	@Test
	public void marshalText_url_3_0() {
		String actual = urlType.marshalText(VCardVersion.V3_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(url, actual);
	}

	@Test
	public void marshalText_url_4_0() {
		String actual = urlType.marshalText(VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(url, actual);
	}

	@Test
	public void marshalText_binary_2_1() {
		String actual = binaryType.marshalText(VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(base64Data, actual);
	}

	@Test
	public void marshalText_binary_3_0() {
		String actual = binaryType.marshalText(VCardVersion.V3_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(base64Data, actual);
	}

	@Test
	public void marshalText_binary_4_0() {
		String actual = binaryType.marshalText(VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(dataUri, actual);
	}

	@Test
	public void marshalXml_url() {
		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(url);
		Document expected = xe.document();

		xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();

		urlType.marshalXml(xe.element(), VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalXml_binary() {
		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(dataUri);
		Document expected = xe.document();

		xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();

		binaryType.marshalXml(xe.element(), VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalJson_url() {
		JCardValue value = urlType.marshalJson(VCardVersion.V4_0, new ArrayList<String>());

		assertEquals(JCardDataType.URI, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ url })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
	}

	@Test
	public void marshalJson_binary() {
		JCardValue value = binaryType.marshalJson(VCardVersion.V4_0, new ArrayList<String>());

		assertEquals(JCardDataType.URI, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ dataUri })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
	}

	@Test
	public void unmarshalText_url() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setValue(ValueParameter.URL);

		t.unmarshalText(subTypes, url, VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(url, t.getUrl());
		assertNull(t.getData());
		assertNull(t.getContentType());
	}

	@Test
	public void unmarshalText_url_no_value() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();

		t.unmarshalText(subTypes, url, VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(url, t.getUrl());
		assertNull(t.getData());
		assertNull(t.getContentType());
	}

	@Test
	public void unmarshalText_binary_b_encoding() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.B);

		t.unmarshalText(subTypes, base64Data, VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC);
		assertNull(t.getUrl());
		assertArrayEquals(data, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	@Test
	public void unmarshalText_binary_base64_encoding() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.BASE64);

		t.unmarshalText(subTypes, base64Data, VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC);
		assertNull(t.getUrl());
		assertArrayEquals(data, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	@Test
	public void unmarshalText_binary_no_encoding_no_type() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();

		t.unmarshalText(subTypes, base64Data, VCardVersion.V2_1, new ArrayList<String>(), CompatibilityMode.RFC);
		assertNull(t.getUrl());
		assertArrayEquals(data, t.getData());
		assertNull(t.getContentType());
	}

	@Test
	public void unmarshalText_url_4_0() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		t.unmarshalText(subTypes, url, VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(url, t.getUrl());
		assertNull(t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	@Test
	public void unmarshalText_binary_4_0() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();

		t.unmarshalText(subTypes, dataUri, VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertNull(t.getUrl());
		assertArrayEquals(data, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	@Test
	public void unmarshalXml_url() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(url);

		t.unmarshalXml(subTypes, xe.element(), VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertEquals(url, t.getUrl());
		assertNull(t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	@Test
	public void unmarshalXml_binary() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(dataUri);

		t.unmarshalXml(subTypes, xe.element(), VCardVersion.V4_0, new ArrayList<String>(), CompatibilityMode.RFC);
		assertNull(t.getUrl());
		assertArrayEquals(data, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	@Test
	public void unmarshalJson_url() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		JCardValue value = JCardValue.uri(url);

		t.unmarshalJson(subTypes, value, VCardVersion.V4_0, new ArrayList<String>());
		assertEquals(url, t.getUrl());
		assertNull(t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	@Test
	public void unmarshalJson_binary() {
		BinaryTypeImpl t = new BinaryTypeImpl();
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		JCardValue value = JCardValue.uri(dataUri);

		t.unmarshalJson(subTypes, value, VCardVersion.V4_0, new ArrayList<String>());
		assertNull(t.getUrl());
		assertArrayEquals(data, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
	}

	private class BinaryTypeImpl extends BinaryType<ImageTypeParameter> {
		public static final String NAME = "NAME";

		public BinaryTypeImpl() {
			super(NAME);
		}

		@Override
		protected ImageTypeParameter buildTypeObj(String type) {
			return ImageTypeParameter.valueOf(type);
		}

		@Override
		protected ImageTypeParameter buildMediaTypeObj(String mediaType) {
			return ImageTypeParameter.findByMediaType(mediaType);
		}
	}
}
