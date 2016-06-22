package ezvcard.io.scribe;

import static ezvcard.VCardDataType.URI;
import static ezvcard.VCardDataType.URL;
import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.ImageType;
import ezvcard.property.BinaryProperty;
import ezvcard.util.DataUri;
import ezvcard.util.org.apache.commons.codec.binary.Base64;

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
public class BinaryPropertyScribeTest {
	private final BinaryPropertyScribeImpl scribe = new BinaryPropertyScribeImpl();
	private final Sensei<BinaryTypeImpl> sensei = new Sensei<BinaryTypeImpl>(scribe);

	private final String url = "http://example.com/image.jpg";
	private final String urlUnknownExtension = "http://example.com/image.aaa";
	private final String urlWithoutExtension = "http://example.com/image";
	private final byte[] data = "data".getBytes();
	private final String base64Data = Base64.encodeBase64String(data);
	private final String dataUri = new DataUri("image/jpeg", data).toString();
	private final String dataUriNoContentType = new DataUri("application/octet-stream", data).toString();

	private final BinaryTypeImpl withUrl = new BinaryTypeImpl();
	{
		withUrl.setUrl(url, ImageType.JPEG);
		withUrl.getParameters().setEncoding(Encoding._8BIT); //ENCODING parameter (if one exists) should be removed when written
	}
	private final BinaryTypeImpl withDataNoContentType = new BinaryTypeImpl();
	{
		withDataNoContentType.setData(data, null);
	}
	private final BinaryTypeImpl withData = new BinaryTypeImpl();
	{
		withData.setData(data, ImageType.JPEG);
		withData.setType("work");
		withData.getParameters().setEncoding(Encoding._8BIT); //ENCODING parameter (if one exists) should be removed/overwritten when written
		withData.getParameters().setMediaType("foo"); //MEDIATYPE parameter (if one exists) should be removed when written
	}
	private final BinaryTypeImpl empty = new BinaryTypeImpl();

	@Test
	public void dataType() {
		sensei.assertDataType(withUrl).versions(V2_1).run(URL);
		sensei.assertDataType(withUrl).versions(V3_0, V4_0).run(URI);

		sensei.assertDataType(withData).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withData).versions(V4_0).run(URI);

		sensei.assertDataType(withDataNoContentType).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withDataNoContentType).versions(V4_0).run(URI);

		sensei.assertDataType(empty).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(empty).versions(V4_0).run(URI);
	}

	@Test
	public void prepareParameters() {
		sensei.assertPrepareParams(withUrl).versions(V2_1, V3_0).expected("TYPE", "jpeg").run();
		sensei.assertPrepareParams(withUrl).versions(V4_0).expected("MEDIATYPE", "image/jpeg").run();

		sensei.assertPrepareParams(withData).versions(V2_1).expected("TYPE", "jpeg").expected("ENCODING", "BASE64").run();
		sensei.assertPrepareParams(withData).versions(V3_0).expected("TYPE", "jpeg").expected("ENCODING", "b").run();
		sensei.assertPrepareParams(withData).versions(V4_0).expected("TYPE", "work").run();

		sensei.assertPrepareParams(withDataNoContentType).versions(V2_1).expected("ENCODING", "BASE64").run();
		sensei.assertPrepareParams(withDataNoContentType).versions(V3_0).expected("ENCODING", "b").run();
		sensei.assertPrepareParams(withDataNoContentType).versions(V4_0).run();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withUrl).run(url);

		sensei.assertWriteText(withData).versions(V2_1, V3_0).run(base64Data);
		sensei.assertWriteText(withData).versions(V4_0).run(dataUri);

		sensei.assertWriteText(withDataNoContentType).versions(V2_1, V3_0).run(base64Data);
		sensei.assertWriteText(withDataNoContentType).versions(V4_0).run(dataUriNoContentType);

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
			VCardVersion versions[] = { V2_1, V3_0 };

			//without TYPE parameter
			sensei.assertParseText(url).dataType(URL).versions(versions).run(hasUrl(url, ImageType.JPEG));
			sensei.assertParseText(url).versions(versions).run(hasUrl(url, ImageType.JPEG));
			sensei.assertParseText(urlUnknownExtension).dataType(URL).versions(versions).run(hasUrl(urlUnknownExtension, null));
			sensei.assertParseText(urlUnknownExtension).versions(versions).run(hasUrl(urlUnknownExtension, null));
			sensei.assertParseText(urlWithoutExtension).dataType(URL).versions(versions).run(hasUrl(urlWithoutExtension, null));
			sensei.assertParseText(urlWithoutExtension).versions(versions).run(hasUrl(urlWithoutExtension, null));

			//with TYPE parameter
			sensei.assertParseText(url).dataType(URL).versions(versions).param("TYPE", "JPEG").run(hasUrl(url, ImageType.JPEG));
			sensei.assertParseText(url).versions(versions).param("TYPE", "JPEG").run(hasUrl(url, ImageType.JPEG));
		}

		{
			VCardVersion version = V4_0;

			//without MEDIATYPE parameter
			sensei.assertParseText(url).dataType(URI).versions(version).run(hasUrl(url, ImageType.JPEG));
			sensei.assertParseText(url).versions(version).run(hasUrl(url, ImageType.JPEG));
			sensei.assertParseText(urlUnknownExtension).dataType(URL).versions(version).run(hasUrl(urlUnknownExtension, null));
			sensei.assertParseText(urlUnknownExtension).versions(version).run(hasUrl(urlUnknownExtension, null));
			sensei.assertParseText(urlWithoutExtension).dataType(URI).versions(version).run(hasUrl(urlWithoutExtension, null));
			sensei.assertParseText(urlWithoutExtension).versions(version).run(hasUrl(urlWithoutExtension, null));

			//with MEDIATYPE parameter
			sensei.assertParseText(url).dataType(URI).versions(version).param("MEDIATYPE", "image/jpeg").run(hasUrl(url, ImageType.JPEG));
			sensei.assertParseText(url).versions(version).param("MEDIATYPE", "image/jpeg").run(hasUrl(url, ImageType.JPEG));
		}
	}

	@Test
	public void parseText_binary() {
		//2.1, 3.0
		{
			VCardVersion versions[] = { V2_1, V3_0 };

			//with TYPE
			{
				//B encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("TYPE", "JPEG").param("ENCODING", "b").run(hasData(data, ImageType.JPEG));

				//BASE64 encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("TYPE", "JPEG").param("ENCODING", "base64").run(hasData(data, ImageType.JPEG));

				//without encoding
				sensei.assertParseText(base64Data).dataType(null).versions(versions).param("TYPE", "JPEG").run(hasData(data, ImageType.JPEG));
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
			VCardVersion version = V4_0;

			//without MEDIATYPE
			sensei.assertParseText(dataUri).versions(version).run(hasData(data, ImageType.JPEG));

			//with MEDIATYPE (parameter should be ignored)
			sensei.assertParseText(dataUri).versions(version).param("MEDIATYPE", "image/png").run(hasData(data, ImageType.JPEG));
		}
	}

	@Test
	public void parseXml_url() {
		//with MEDIATYPE
		sensei.assertParseXml("<uri>" + url + "</uri>").param("MEDIATYPE", "image/jpeg").run(hasUrl(url, ImageType.JPEG));

		//without MEDIATYPE
		sensei.assertParseXml("<uri>" + url + "</uri>").run(hasUrl(url, ImageType.JPEG));
		sensei.assertParseXml("<uri>" + urlUnknownExtension + "</uri>").run(hasUrl(urlUnknownExtension, null));
		sensei.assertParseXml("<uri>" + urlWithoutExtension + "</uri>").run(hasUrl(urlWithoutExtension, null));

	}

	@Test
	public void parseXml_binary() {
		//with MEDIATYPE (parameter should be ignored
		sensei.assertParseXml("<uri>" + dataUri + "</uri>").param("MEDIATYPE", "image/png").run(hasData(data, ImageType.JPEG));

		//without MEDIATYPE
		sensei.assertParseXml("<uri>" + dataUri + "</uri>").run(hasData(data, ImageType.JPEG));
	}

	@Test
	public void parseXml_empty() {
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseHtml_url() {
		//with type
		sensei.assertParseHtml("<object type=\"image/gif\" data=\"" + url + "\" />").run(hasUrl(url, ImageType.GIF));

		//without type
		sensei.assertParseHtml("<object data=\"" + url + "\" />").run(hasUrl(url, ImageType.JPEG));
		sensei.assertParseHtml("<object data=\"" + urlUnknownExtension + "\" />").run(hasUrl(urlUnknownExtension, null));
		sensei.assertParseHtml("<object data=\"" + urlWithoutExtension + "\" />").run(hasUrl(urlWithoutExtension, null));
	}

	@Test
	public void parseHtml_binary() {
		//with type (should be ignored)
		sensei.assertParseHtml("<object type=\"image/png\" data=\"" + dataUri + "\" />").run(hasData(data, ImageType.JPEG));

		//without type
		sensei.assertParseHtml("<object data=\"" + dataUri + "\" />").run(hasData(data, ImageType.JPEG));
	}

	@Test
	public void parseHtml_invalid() {
		sensei.assertParseHtml("<object type=\"image/gif\" />").cannotParse();
		sensei.assertParseHtml("<div />").cannotParse();
	}

	@Test
	public void parseJson_url() {
		//with MEDIATYPE
		sensei.assertParseJson(url).param("MEDIATYPE", "image/jpeg").run(hasUrl(url, ImageType.JPEG));

		//without MEDIATYPE
		sensei.assertParseJson(url).run(hasUrl(url, ImageType.JPEG));
		sensei.assertParseJson(urlUnknownExtension).run(hasUrl(urlUnknownExtension, null));
		sensei.assertParseJson(urlWithoutExtension).run(hasUrl(urlWithoutExtension, null));

		sensei.assertParseJson("").run(hasUrl("", null));
	}

	@Test
	public void parseJson_binary() {
		//with MEDIATYPE (parameter should be ignored)
		sensei.assertParseJson(dataUri).param("MEDIATYPE", "image/png").run(hasData(data, ImageType.JPEG));

		//without MEDIATYPE
		sensei.assertParseJson(dataUri).run(hasData(data, ImageType.JPEG));
	}

	private static class BinaryPropertyScribeImpl extends BinaryPropertyScribe<BinaryTypeImpl, ImageType> {
		public BinaryPropertyScribeImpl() {
			super(BinaryTypeImpl.class, "BINARY");
		}

		@Override
		protected ImageType _mediaTypeFromTypeParameter(String type) {
			return ImageType.get(type, null, null);
		}

		@Override
		protected ImageType _mediaTypeFromMediaTypeParameter(String mediaType) {
			return ImageType.get(null, mediaType, null);
		}

		@Override
		protected ImageType _mediaTypeFromFileExtension(String extension) {
			return ImageType.find(null, null, extension);
		}

		@Override
		protected BinaryTypeImpl _newInstance(String uri, ImageType contentType) {
			BinaryTypeImpl property = new BinaryTypeImpl();
			property.setUrl(uri, contentType);
			return property;
		}

		@Override
		protected BinaryTypeImpl _newInstance(byte[] data, ImageType contentType) {
			BinaryTypeImpl property = new BinaryTypeImpl();
			property.setData(data, contentType);
			return property;
		}

	}

	private static class BinaryTypeImpl extends BinaryProperty<ImageType> {
		public BinaryTypeImpl() {
			super((String) null, null);
		}
	}

	private Check<BinaryTypeImpl> hasUrl(final String url, final ImageType contentType) {
		return new Check<BinaryTypeImpl>() {
			public void check(BinaryTypeImpl actual) {
				assertEquals(url, actual.getUrl());
				assertNull(actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}

	private Check<BinaryTypeImpl> hasData(final byte[] data, final ImageType contentType) {
		return new Check<BinaryTypeImpl>() {
			public void check(BinaryTypeImpl actual) {
				assertNull(actual.getUrl());
				assertArrayEquals(data, actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}
}
