package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.XmlUtils;

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
	public void marshalUrl() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		String expectedValue, actualValue;
		VCardSubTypes subTypes;

		BinaryTypeImpl t = new BinaryTypeImpl();
		t.setUrl("http://example.com/image.jpg", ImageTypeParameter.JPEG);

		//2.1
		version = VCardVersion.V2_1;
		expectedValue = "http://example.com/image.jpg";
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URL, subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());

		//3.0
		version = VCardVersion.V3_0;
		expectedValue = "http://example.com/image.jpg";
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());

		//4.0
		version = VCardVersion.V4_0;
		expectedValue = "http://example.com/image.jpg";
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertNull(subTypes.getType());
		assertEquals("image/jpeg", subTypes.getMediaType());

		//xCard
		version = VCardVersion.V4_0;
		String expectedXml = "<name xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		expectedXml += "<uri>http://example.com/image.jpg</uri>";
		expectedXml += "</name>";
		Document expected = XmlUtils.toDocument(expectedXml);
		Document actual = XmlUtils.toDocument("<name xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		Element element = XmlUtils.getRootElement(actual);
		t.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void marshalBinary() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		String expectedValue, actualValue;
		VCardSubTypes subTypes;

		BinaryTypeImpl t = new BinaryTypeImpl();
		t.setData(dummyData, ImageTypeParameter.JPEG);
		t.setType("work");

		//2.1
		version = VCardVersion.V2_1;
		expectedValue = Base64.encodeBase64String(dummyData);
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(EncodingParameter.BASE64, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());

		//3.0
		version = VCardVersion.V3_0;
		expectedValue = Base64.encodeBase64String(dummyData);
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(EncodingParameter.B, subTypes.getEncoding());
		assertNull(subTypes.getValue());
		assertEquals(ImageTypeParameter.JPEG.getValue(), subTypes.getType());

		//4.0
		version = VCardVersion.V4_0;
		expectedValue = "data:image/jpeg;base64," + Base64.encodeBase64String(dummyData);
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertNull(subTypes.getEncoding());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals("work", subTypes.getType());

		//xCard
		version = VCardVersion.V4_0;
		String expectedXml = "<name xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		expectedXml += "<uri>data:image/jpeg;base64," + Base64.encodeBase64String(dummyData) + "</uri>";
		expectedXml += "</name>";
		Document expected = XmlUtils.toDocument(expectedXml);

		Document actual = XmlUtils.toDocument("<name xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\" />");
		Element element = XmlUtils.getRootElement(actual);
		t.marshalXml(element, version, warnings, compatibilityMode);

		assertXMLEqual(expected, actual);
	}

	/**
	 * Tests to make sure if can handle types that are not a pre-defined
	 * constant.
	 */
	@Test
	public void marshalCustomType() throws Exception {
		VCardVersion version;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		String expectedValue, actualValue;
		VCardSubTypes subTypes;

		BinaryTypeImpl t = new BinaryTypeImpl();
		ImageTypeParameter param = new ImageTypeParameter("aaa", "image/aaa", "aaa");
		t.setUrl("http://example.com/image.aaa", param);

		//2.1
		version = VCardVersion.V2_1;
		expectedValue = "http://example.com/image.aaa";
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URL, subTypes.getValue());
		assertEquals(param.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());

		//3.0
		version = VCardVersion.V3_0;
		expectedValue = "http://example.com/image.aaa";
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(param.getValue(), subTypes.getType());
		assertNull(subTypes.getMediaType());

		//4.0
		version = VCardVersion.V4_0;
		expectedValue = "http://example.com/image.aaa";
		actualValue = t.marshalText(version, warnings, compatibilityMode);
		subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());
		assertEquals(expectedValue, actualValue);
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertNull(subTypes.getType());
		assertEquals(param.getMediaType(), subTypes.getMediaType());

		//xCard (N/A -- "<parameters>" element is added by the "XCardMarshaller" class)
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes;
		BinaryTypeImpl t;

		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setValue(ValueParameter.URL);
		t.unmarshalText(subTypes, "http://example.com/image.jpg", version, warnings, compatibilityMode);
		assertEquals("http://example.com/image.jpg", t.getUrl());
		assertNull(t.getData());
		assertNull(t.getContentType());

		//no VALUE
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		t.unmarshalText(subTypes, "http://example.com/image.jpg", version, warnings, compatibilityMode);
		assertEquals("http://example.com/image.jpg", t.getUrl());
		assertNull(t.getData());
		assertNull(t.getContentType());

		//"B" encoding
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.B);
		t.unmarshalText(subTypes, Base64.encodeBase64String(dummyData), version, warnings, compatibilityMode);
		assertNull(t.getUrl());
		assertArrayEquals(dummyData, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());

		//"BASE64" encoding
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setType(ImageTypeParameter.JPEG.getValue());
		subTypes.setEncoding(EncodingParameter.BASE64);
		t.unmarshalText(subTypes, Base64.encodeBase64String(dummyData), version, warnings, compatibilityMode);
		assertNull(t.getUrl());
		assertArrayEquals(dummyData, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());

		//no encoding
		//no type
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		t.unmarshalText(subTypes, Base64.encodeBase64String(dummyData), version, warnings, compatibilityMode);
		assertNull(t.getUrl());
		assertArrayEquals(dummyData, t.getData());
		assertNull(t.getContentType());

		//4.0 URL
		version = VCardVersion.V4_0;
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setMediaType("image/jpeg");
		t.unmarshalText(subTypes, "http://example.com/image.jpg", version, warnings, compatibilityMode);
		assertEquals("http://example.com/image.jpg", t.getUrl());
		assertNull(t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());

		//4.0 data URI
		version = VCardVersion.V4_0;
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		t.unmarshalText(subTypes, "data:image/jpeg;base64," + Base64.encodeBase64String(dummyData), version, warnings, compatibilityMode);
		assertNull(t.getUrl());
		assertArrayEquals(dummyData, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());

		//xCard URL
		version = VCardVersion.V4_0;
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setMediaType("image/jpeg");

		String xml = "<name xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		xml += "<uri>http://example.com/image.jpg</uri>";
		xml += "</name>";
		Element element = XmlUtils.getRootElement(XmlUtils.toDocument(xml));

		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("http://example.com/image.jpg", t.getUrl());
		assertNull(t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());

		//xCard data URI
		version = VCardVersion.V4_0;
		t = new BinaryTypeImpl();
		subTypes = new VCardSubTypes();
		subTypes.setMediaType("image/jpeg");

		xml = "<name xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">";
		xml += "<uri>data:image/jpeg;base64," + Base64.encodeBase64String(dummyData) + "</uri>";
		xml += "</name>";
		element = XmlUtils.getRootElement(XmlUtils.toDocument(xml));

		t.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertNull(t.getUrl());
		assertArrayEquals(dummyData, t.getData());
		assertEquals(ImageTypeParameter.JPEG, t.getContentType());
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
				param = new ImageTypeParameter(type, null, null);
			}
			return param;
		}

		@Override
		protected ImageTypeParameter buildMediaTypeObj(String mediaType) {
			ImageTypeParameter p = ImageTypeParameter.findByMediaType(mediaType);
			if (p == null) {
				int slashPos = mediaType.indexOf('/');
				String type;
				if (slashPos == -1 || slashPos < mediaType.length() - 1) {
					type = "";
				} else {
					type = mediaType.substring(slashPos + 1);
				}
				p = new ImageTypeParameter(type, mediaType, null);
			}
			return p;
		}
	}
}
