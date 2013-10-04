package ezvcard.types.scribes;

import static ezvcard.util.TestUtils.each;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.types.BinaryType;
import ezvcard.types.scribes.Sensei.Check;
import ezvcard.util.DataUri;
import ezvcard.util.org.apache.commons.codec.binary.Base64;

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
public class BinaryPropertyScribeTest {
	private final BinaryPropertyMarshallerImpl scribe = new BinaryPropertyMarshallerImpl();
	private final Sensei<BinaryTypeImpl> sensei = new Sensei<BinaryTypeImpl>(scribe);

	private final String url = "http://example.com/image.jpg";
	private final byte[] data = "data".getBytes();
	private final String base64Data = Base64.encodeBase64String(data);
	private final String dataUri = new DataUri("image/jpeg", data).toString();
	private final String dataUriNoContentType = new DataUri("application/octet-stream", data).toString();

	private final BinaryTypeImpl withUrl = new BinaryTypeImpl();
	{
		withUrl.setUrl(url, ImageTypeParameter.JPEG);
	}
	private final BinaryTypeImpl withDataNoContentType = new BinaryTypeImpl();
	{
		withDataNoContentType.setData(data, null);
	}
	private final BinaryTypeImpl withData = new BinaryTypeImpl();
	{
		withData.setData(data, ImageTypeParameter.JPEG);
		withData.setType("work");
	}
	private final BinaryTypeImpl empty = new BinaryTypeImpl();

	@Test
	public void dataType() {
		sensei.assertDataType(withUrl).versions(VCardVersion.V2_1).run(VCardDataType.URL);
		sensei.assertDataType(withUrl).versions(VCardVersion.V3_0, VCardVersion.V4_0).run(VCardDataType.URI);

		sensei.assertDataType(withData).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(null);
		sensei.assertDataType(withData).versions(VCardVersion.V4_0).run(VCardDataType.URI);

		sensei.assertDataType(withDataNoContentType).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(null);
		sensei.assertDataType(withDataNoContentType).versions(VCardVersion.V4_0).run(VCardDataType.URI);

		sensei.assertDataType(empty).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(null);
		sensei.assertDataType(empty).versions(VCardVersion.V4_0).run(VCardDataType.URI);
	}

	@Test
	public void prepareParameters() {
		sensei.assertPrepareParams(withUrl).versions(VCardVersion.V2_1, VCardVersion.V3_0).expected("TYPE", "jpeg").run();
		sensei.assertPrepareParams(withUrl).versions(VCardVersion.V4_0).expected("MEDIATYPE", "image/jpeg").run();

		sensei.assertPrepareParams(withData).versions(VCardVersion.V2_1).expected("TYPE", "jpeg").expected("ENCODING", "base64").run();
		sensei.assertPrepareParams(withData).versions(VCardVersion.V3_0).expected("TYPE", "jpeg").expected("ENCODING", "b").run();
		sensei.assertPrepareParams(withData).versions(VCardVersion.V4_0).expected("TYPE", "work").run();

		sensei.assertPrepareParams(withDataNoContentType).versions(VCardVersion.V2_1).expected("ENCODING", "base64").run();
		sensei.assertPrepareParams(withDataNoContentType).versions(VCardVersion.V3_0).expected("ENCODING", "b").run();
		sensei.assertPrepareParams(withDataNoContentType).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withUrl).run(url);

		sensei.assertWriteText(withData).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(base64Data);
		sensei.assertWriteText(withData).versions(VCardVersion.V4_0).run(dataUri);

		sensei.assertWriteText(withDataNoContentType).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(base64Data);
		sensei.assertWriteText(withDataNoContentType).versions(VCardVersion.V4_0).run(dataUriNoContentType);

		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withUrl).run("<uri>" + url + "</uri>");
		sensei.assertWriteXml(withData).run("<uri>" + dataUri + "</uri>");
		sensei.assertWriteXml(withDataNoContentType).run("<uri>" + dataUriNoContentType + "</uri>");
		sensei.assertWriteXml(empty).run("<uri/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withUrl).run(url);
		sensei.assertWriteJson(withData).run(dataUri);
		sensei.assertWriteJson(withDataNoContentType).run(dataUriNoContentType);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText_url() {
		{
			VCardVersion version = VCardVersion.V2_1;

			//without TYPE parameter
			sensei.assertParseText(url).dataType(VCardDataType.URL).versions(version).run(hasUrl(url, null));

			//with TYPE parameter
			sensei.assertParseText(url).dataType(VCardDataType.URL).versions(version).param("TYPE", "JPEG").run(hasUrl(url, ImageTypeParameter.JPEG));
		}

		{
			VCardVersion version = VCardVersion.V3_0;

			//without TYPE parameter
			sensei.assertParseText(url).dataType(VCardDataType.URI).versions(version).run(hasUrl(url, null));

			//with TYPE parameter
			sensei.assertParseText(url).dataType(VCardDataType.URI).versions(version).param("TYPE", "JPEG").run(hasUrl(url, ImageTypeParameter.JPEG));
		}

		{
			VCardVersion version = VCardVersion.V4_0;

			//without MEDIATYPE parameter
			sensei.assertParseText(url).dataType(VCardDataType.URI).versions(version).run(hasUrl(url, null));

			//with MEDIATYPE parameter
			sensei.assertParseText(url).dataType(VCardDataType.URI).versions(version).param("MEDIATYPE", "image/jpeg").run(hasUrl(url, ImageTypeParameter.JPEG));
		}
	}

	@Test
	public void parseText_binary() {
		//2.1, 3.0
		{
			VCardVersion versions[] = each(VCardVersion.V2_1, VCardVersion.V3_0);

			//with TYPE
			{
				//B encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("TYPE", "JPEG").param("ENCODING", "b").run(hasData(data, ImageTypeParameter.JPEG));

				//BASE64 encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("TYPE", "JPEG").param("ENCODING", "base64").run(hasData(data, ImageTypeParameter.JPEG));

				//without encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("TYPE", "JPEG").run(hasData(data, ImageTypeParameter.JPEG));
			}

			//without TYPE
			{
				//B encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("ENCODING", "b").run(hasData(data, null));

				//BASE64 encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("ENCODING", "base64").run(hasData(data, null));

				//without encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).run(hasData(data, null));
			}
		}

		//4.0
		{
			VCardVersion version = VCardVersion.V4_0;

			//without MEDIATYPE
			sensei.assertParseText(dataUri).versions(version).run(hasData(data, ImageTypeParameter.JPEG));

			//with MEDIATYPE (parameter should be ignored)
			sensei.assertParseText(dataUri).versions(version).param("MEDIATYPE", "image/png").run(hasData(data, ImageTypeParameter.JPEG));
		}
	}

	@Test
	public void parseXml_url() {
		//with MEDIATYPE
		sensei.assertParseXml("<uri>" + url + "</uri>").param("MEDIATYPE", "image/jpeg").run(hasUrl(url, ImageTypeParameter.JPEG));

		//without MEDIATYPE
		sensei.assertParseXml("<uri>" + url + "</uri>").run(hasUrl(url, null));
	}

	@Test
	public void parseXml_binary() {
		//with MEDIATYPE (parameter should be ignored
		sensei.assertParseXml("<uri>" + dataUri + "</uri>").param("MEDIATYPE", "image/png").run(hasData(data, ImageTypeParameter.JPEG));

		//without MEDIATYPE
		sensei.assertParseXml("<uri>" + dataUri + "</uri>").run(hasData(data, ImageTypeParameter.JPEG));

	}

	@Test
	public void parseXml_empty() {
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseHtml_url() {
		//with type
		sensei.assertParseHtml("<object type=\"image/gif\" data=\"" + url + "\" />").run(hasUrl(url, ImageTypeParameter.GIF));

		//without type
		sensei.assertParseHtml("<object data=\"" + url + "\" />").run(hasUrl(url, null));
	}

	@Test
	public void parseHtml_binary() {
		//with type (should be ignored)
		sensei.assertParseHtml("<object type=\"image/png\" data=\"" + dataUri + "\" />").run(hasData(data, ImageTypeParameter.JPEG));

		//without type
		sensei.assertParseHtml("<object data=\"" + dataUri + "\" />").run(hasData(data, ImageTypeParameter.JPEG));
	}

	@Test
	public void parseHtml_invalid() {
		sensei.assertParseHtml("<object type=\"image/gif\" />").cannotParse();
		sensei.assertParseHtml("<div />").cannotParse();
	}

	@Test
	public void parseJson_url() {
		//with MEDIATYPE
		sensei.assertParseJson(url).param("MEDIATYPE", "image/jpeg").run(hasUrl(url, ImageTypeParameter.JPEG));

		//without MEDIATYPE
		sensei.assertParseJson(url).run(hasUrl(url, null));

		sensei.assertParseJson("").run(hasUrl("", null));
	}

	@Test
	public void parseJson_binary() {
		//with MEDIATYPE (parameter should be ignored)
		sensei.assertParseJson(dataUri).param("MEDIATYPE", "image/png").run(hasData(data, ImageTypeParameter.JPEG));

		//without MEDIATYPE
		sensei.assertParseJson(dataUri).run(hasData(data, ImageTypeParameter.JPEG));
	}

	private static class BinaryPropertyMarshallerImpl extends BinaryPropertyScribe<BinaryTypeImpl, ImageTypeParameter> {
		public BinaryPropertyMarshallerImpl() {
			super(BinaryTypeImpl.class, "BINARY");
		}

		@Override
		protected ImageTypeParameter _buildTypeObj(String type) {
			return ImageTypeParameter.get(type, null, null);
		}

		@Override
		protected ImageTypeParameter _buildMediaTypeObj(String mediaType) {
			return ImageTypeParameter.get(null, mediaType, null);
		}

		@Override
		protected BinaryTypeImpl _newInstance(String uri, ImageTypeParameter contentType) {
			BinaryTypeImpl property = new BinaryTypeImpl();
			property.setUrl(uri, contentType);
			return property;
		}

		@Override
		protected BinaryTypeImpl _newInstance(byte[] data, ImageTypeParameter contentType) {
			BinaryTypeImpl property = new BinaryTypeImpl();
			property.setData(data, contentType);
			return property;
		}
	}

	private static class BinaryTypeImpl extends BinaryType<ImageTypeParameter> {
		public BinaryTypeImpl() {
			super((String) null, null);
		}
	}

	private Check<BinaryTypeImpl> hasUrl(final String url, final ImageTypeParameter contentType) {
		return new Check<BinaryTypeImpl>() {
			public void check(BinaryTypeImpl actual) {
				assertEquals(url, actual.getUrl());
				assertNull(actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}

	private Check<BinaryTypeImpl> hasData(final byte[] data, final ImageTypeParameter contentType) {
		return new Check<BinaryTypeImpl>() {
			public void check(BinaryTypeImpl actual) {
				assertNull(actual.getUrl());
				assertArrayEquals(data, actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}
}
