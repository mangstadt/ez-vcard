package ezvcard.io.scribe;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.io.scribe.Sensei.Check;
import ezvcard.parameter.ImageType;
import ezvcard.property.ImageProperty;
import ezvcard.util.DataUri;

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
public class ImagePropertyScribeTest {
	private final ImagePropertyScribeImpl scribe = new ImagePropertyScribeImpl();
	private final Sensei<ImagePropertyImpl> sensei = new Sensei<ImagePropertyImpl>(scribe);

	private final String url = "http://example.com/image.jpg";
	private final String urlWithoutExtension = "http://example.com/image";
	private final byte[] data = "data".getBytes();
	private final String dataUri = new DataUri("image/jpeg", data).toString();

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<img />").cannotParse();
		sensei.assertParseHtml("<img src=\"\" />").cannotParse();
		sensei.assertParseHtml("<img src=\"" + url + "\" />").run(hasUrl(url, ImageType.JPEG));
		sensei.assertParseHtml("<img src=\"" + urlWithoutExtension + "\" />").run(hasUrl(urlWithoutExtension, null));
		sensei.assertParseHtml("<img src=\"" + dataUri + "\" />").run(hasData(data, ImageType.JPEG));

		//call super.parseHtml() if it's not an <img> tag
		sensei.assertParseHtml("<object type=\"image/gif\" data=\"" + url + "\" />").run(hasUrl(url, ImageType.GIF));
	}

	private static class ImagePropertyScribeImpl extends ImagePropertyScribe<ImagePropertyImpl> {
		public ImagePropertyScribeImpl() {
			super(ImagePropertyImpl.class, "IMAGE");
		}

		@Override
		protected ImagePropertyImpl _newInstance(String uri, ImageType contentType) {
			return new ImagePropertyImpl(uri, contentType);
		}

		@Override
		protected ImagePropertyImpl _newInstance(byte[] data, ImageType contentType) {
			return new ImagePropertyImpl(data, contentType);
		}
	}

	private static class ImagePropertyImpl extends ImageProperty {
		public ImagePropertyImpl(byte[] data, ImageType type) {
			super(data, type);
		}

		public ImagePropertyImpl(String uri, ImageType type) {
			super(uri, type);
		}
	}

	private Check<ImagePropertyImpl> hasUrl(final String url, final ImageType contentType) {
		return new Check<ImagePropertyImpl>() {
			public void check(ImagePropertyImpl actual) {
				assertEquals(url, actual.getUrl());
				assertNull(actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}

	private Check<ImagePropertyImpl> hasData(final byte[] data, final ImageType contentType) {
		return new Check<ImagePropertyImpl>() {
			public void check(ImagePropertyImpl actual) {
				assertNull(actual.getUrl());
				assertArrayEquals(data, actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}
}
