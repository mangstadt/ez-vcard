package ezvcard.io.scribe;

import static ezvcard.VCardDataType.TEXT;
import static ezvcard.VCardDataType.URI;
import static ezvcard.VCardDataType.URL;
import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.io.scribe.Sensei.Check;
import ezvcard.parameter.KeyType;
import ezvcard.property.Key;
import ezvcard.util.DataUri;
import ezvcard.util.org.apache.commons.codec.binary.Base64;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
public class KeyScribeTest {
	private final KeyScribe scribe = new KeyScribe();
	private final Sensei<Key> sensei = new Sensei<Key>(scribe);

	private final String url = "http://example.com/key.pgp";
	private final byte[] data = "data".getBytes();
	private final String base64Data = Base64.encodeBase64String(data);
	private final String dataUri = new DataUri(KeyType.GPG.getMediaType(), data).toString();
	private final String text = "text";

	private final Key withUrl = new Key(url, KeyType.GPG);
	private final Key withData = new Key(data, KeyType.GPG);
	private final Key withText = new Key();
	{
		withText.setText(text, KeyType.GPG);
	}
	private final Key empty = new Key();

	@Test
	public void dataType() {
		sensei.assertDataType(withUrl).run(URI);

		sensei.assertDataType(withData).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withData).versions(V4_0).run(URI);

		sensei.assertDataType(withText).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(withText).versions(V4_0).run(TEXT);

		sensei.assertDataType(empty).versions(V2_1, V3_0).run(null);
		sensei.assertDataType(empty).versions(V4_0).run(URI);
	}

	@Test
	public void prepareParameters() {
		sensei.assertPrepareParams(withUrl).versions(V2_1, V3_0).expected("TYPE", "gpg").run();
		sensei.assertPrepareParams(withUrl).versions(V4_0).expected("MEDIATYPE", "application/gpg").run();

		sensei.assertPrepareParams(withData).versions(V2_1).expected("TYPE", "gpg").expected("ENCODING", "BASE64").run();
		sensei.assertPrepareParams(withData).versions(V3_0).expected("TYPE", "gpg").expected("ENCODING", "b").run();
		sensei.assertPrepareParams(withData).versions(V4_0).run();

		sensei.assertPrepareParams(withText).versions(V2_1, V3_0).expected("TYPE", "gpg").run();
		sensei.assertPrepareParams(withText).versions(V4_0).expected("MEDIATYPE", "application/gpg").run();

		sensei.assertPrepareParams(empty).run();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withUrl).run(url);

		sensei.assertWriteText(withData).versions(V2_1, V3_0).run(base64Data);
		sensei.assertWriteText(withData).versions(V4_0).run(dataUri);

		sensei.assertWriteText(withText).run(text);

		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withUrl).run("<uri>" + url + "</uri>");
		sensei.assertWriteXml(withData).run("<uri>" + dataUri + "</uri>");
		sensei.assertWriteXml(withText).run("<text>" + text + "</text>");
		sensei.assertWriteXml(empty).run("<uri/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withUrl).run(url);
		sensei.assertWriteJson(withData).run(dataUri);
		sensei.assertWriteJson(withText).run(text);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(url).dataType(URL).versions(V2_1, V3_0).param("TYPE", "gpg").run(hasUrl(url, KeyType.GPG));
		sensei.assertParseText(url).dataType(URL).versions(V2_1, V3_0).run(hasUrl(url, KeyType.PGP));
		sensei.assertParseText(url).dataType(null).versions(V2_1, V3_0).param("TYPE", "gpg").run(hasText(url, KeyType.GPG));
		sensei.assertParseText(url).dataType(TEXT).versions(V4_0).param("MEDIATYPE", "application/gpg").run(hasText(url, KeyType.GPG));
		sensei.assertParseText(url).dataType(null).versions(V4_0).param("MEDIATYPE", "application/gpg").run(hasUrl(url, KeyType.GPG));
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<uri>" + url + "</uri>").param("MEDIATYPE", "application/gpg").run(hasUrl(url, KeyType.GPG));
		sensei.assertParseXml("<uri>" + url + "</uri>").run(hasUrl(url, KeyType.PGP));
		sensei.assertParseXml("<uri>" + dataUri + "</uri>").param("MEDIATYPE", "application/gpg").run(hasData(data, KeyType.GPG));
		sensei.assertParseXml("<text>" + text + "</text>").param("MEDIATYPE", "application/gpg").run(hasText(text, KeyType.GPG));
		sensei.assertParseXml("").param("MEDIATYPE", "application/gpg").cannotParse(0);
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(url).param("MEDIATYPE", "application/gpg").run(hasUrl(url, KeyType.GPG));
		sensei.assertParseJson(url).run(hasUrl(url, KeyType.PGP));
		sensei.assertParseJson(dataUri).run(hasData(data, KeyType.GPG));
		sensei.assertParseJson(text).dataType(TEXT).param("MEDIATYPE", "application/gpg").run(hasText(text, KeyType.GPG));
		sensei.assertParseJson("").run(hasUrl("", null));
	}

	private static Check<Key> hasUrl(final String url, final KeyType contentType) {
		return new Check<Key>() {
			public void check(Key actual) {
				assertEquals(url, actual.getUrl());
				assertNull(actual.getData());
				assertNull(actual.getText());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}

	private static Check<Key> hasData(final byte[] data, final KeyType contentType) {
		return new Check<Key>() {
			public void check(Key actual) {
				assertNull(actual.getUrl());
				assertArrayEquals(data, actual.getData());
				assertNull(actual.getText());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}

	private static Check<Key> hasText(final String text, final KeyType contentType) {
		return new Check<Key>() {
			public void check(Key actual) {
				assertNull(actual.getUrl());
				assertNull(actual.getData());
				assertEquals(text, actual.getText());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}
}
