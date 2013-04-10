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
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.DataUri;
import ezvcard.util.HtmlUtils;
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
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();

	final String url = "http://example.com/image.jpg";
	final byte[] data = "data".getBytes();
	final String base64Data = Base64.encodeBase64String(data);
	final String dataUri = new DataUri("image/jpeg", data).toString();

	final BinaryTypeImpl withUrl = new BinaryTypeImpl();
	{
		withUrl.setUrl(url, ImageTypeParameter.JPEG);
	}
	final BinaryTypeImpl withDataNoContentType = new BinaryTypeImpl();
	{
		withDataNoContentType.setData(data, null);
	}
	final BinaryTypeImpl withData = new BinaryTypeImpl();
	{
		withData.setData(data, ImageTypeParameter.JPEG);
		withData.setType("work");
	}
	final BinaryTypeImpl empty = new BinaryTypeImpl();
	BinaryTypeImpl binaryType;

	@Before
	public void before() {
		binaryType = new BinaryTypeImpl();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void marshalSubTypes_url_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = withUrl.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(2, subTypes.size());
		assertEquals(ValueParameter.URL, subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_url_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = withUrl.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(2, subTypes.size());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_url_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = withUrl.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getValue());
		assertNull(subTypes.getType());
		assertEquals(ImageTypeParameter.JPEG.getMediaType(), subTypes.getMediaType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_no_content_type_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = withDataNoContentType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertEquals(EncodingParameter.BASE64, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_no_content_type_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = withDataNoContentType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertEquals(EncodingParameter.B, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_no_content_type_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = withDataNoContentType.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getEncoding());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_binary_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = withData.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(2, subTypes.size());
		assertEquals(EncodingParameter.BASE64, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_binary_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = withData.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(2, subTypes.size());
		assertEquals(EncodingParameter.B, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_binary_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = withData.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(2, subTypes.size());
		assertNull(subTypes.getEncoding());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals("work", subTypes.getType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_url_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = withUrl.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_url_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = withUrl.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_url_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = withUrl.marshalText(version, warnings, compatibilityMode);

		assertEquals(url, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_binary_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String actual = withData.marshalText(version, warnings, compatibilityMode);

		assertEquals(base64Data, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_binary_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String actual = withData.marshalText(version, warnings, compatibilityMode);

		assertEquals(base64Data, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_binary_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = withData.marshalText(version, warnings, compatibilityMode);

		assertEquals(dataUri, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_binary_no_content_type_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = withDataNoContentType.marshalText(version, warnings, compatibilityMode);

		assertEquals(new DataUri("application/octet-stream", data).toString(), actual);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_empty_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		empty.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_empty_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		empty.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_empty_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		empty.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalXml_url() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(url);
		Document expected = xe.document();

		xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();

		withUrl.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalXml_binary() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(dataUri);
		Document expected = xe.document();

		xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();

		withData.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalXml_empty() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());

		empty.marshalXml(xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void marshalJson_url() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = withUrl.marshalJson(version, warnings);

		assertEquals(JCardDataType.URI, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ url })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalJson_binary() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = withData.marshalJson(version, warnings);

		assertEquals(JCardDataType.URI, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ dataUri })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalJson_empty() {
		VCardVersion version = VCardVersion.V4_0;
		empty.marshalJson(version, warnings);
	}

	@Test
	public void unmarshalText_url_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setValue(ValueParameter.URL);
		binaryType.unmarshalText(subTypes, url, version, warnings, compatibilityMode);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_url_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		subTypes.setValue(ValueParameter.URI);
		binaryType.unmarshalText(subTypes, url, version, warnings, compatibilityMode);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_url_no_value_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		binaryType.unmarshalText(subTypes, url, version, warnings, compatibilityMode);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_url_no_value_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		binaryType.unmarshalText(subTypes, url, version, warnings, compatibilityMode);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_binary_b_encoding_with_type() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.B);
		binaryType.unmarshalText(subTypes, base64Data, version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_binary_base64_encoding_with_type() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.BASE64);
		binaryType.unmarshalText(subTypes, base64Data, version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_binary_b_encoding_without_type() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setEncoding(EncodingParameter.B);
		binaryType.unmarshalText(subTypes, base64Data, version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_binary_base64_encoding_without_type() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setEncoding(EncodingParameter.BASE64);
		binaryType.unmarshalText(subTypes, base64Data, version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_binary_without_encoding_with_type() {
		VCardVersion version = VCardVersion.V2_1;
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		binaryType.unmarshalText(subTypes, base64Data, version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_binary_no_encoding_no_type() {
		VCardVersion version = VCardVersion.V2_1;
		binaryType.unmarshalText(subTypes, base64Data, version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_url_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());
		binaryType.unmarshalText(subTypes, url, version, warnings, compatibilityMode);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_binary_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		binaryType.unmarshalText(subTypes, dataUri, version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml_url() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(url);

		binaryType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml_binary() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		xe.uri(dataUri);

		binaryType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_empty() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(BinaryTypeImpl.NAME.toLowerCase());
		binaryType.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalHtml_url() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<object type=\"image/gif\" data=\"" + url + "\" />");

		binaryType.unmarshalHtml(element, warnings);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertEquals(ImageTypeParameter.GIF, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml_url_no_type() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<object data=\"" + url + "\" />");

		binaryType.unmarshalHtml(element, warnings);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertNull(binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml_binary() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<object type=\"image/gif\" data=\"" + dataUri + "\" />");

		binaryType.unmarshalHtml(element, warnings);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml_binary_no_type() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<object data=\"" + dataUri + "\" />");

		binaryType.unmarshalHtml(element, warnings);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_no_data_attribute() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<object type=\"image/gif\" />");

		binaryType.unmarshalHtml(element, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_no_object_tag() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div />");

		binaryType.unmarshalHtml(element, warnings);
	}

	@Test
	public void unmarshalJson_url() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		JCardValue value = JCardValue.uri(url);

		binaryType.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(url, binaryType.getUrl());
		assertNull(binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalJson_binary() {
		VCardVersion version = VCardVersion.V4_0;
		subTypes.setMediaType(ImageTypeParameter.JPEG.getMediaType());

		JCardValue value = JCardValue.uri(dataUri);

		binaryType.unmarshalJson(subTypes, value, version, warnings);

		assertNull(binaryType.getUrl());
		assertArrayEquals(data, binaryType.getData());
		assertEquals(ImageTypeParameter.JPEG, binaryType.getContentType());
		assertEquals(0, warnings.size());
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
