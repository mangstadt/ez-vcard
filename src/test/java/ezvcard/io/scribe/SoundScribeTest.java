package ezvcard.io.scribe;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.io.scribe.Sensei.Check;
import ezvcard.parameter.SoundType;
import ezvcard.property.Sound;
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
public class SoundScribeTest {
	private final SoundScribe scribe = new SoundScribe();
	private final Sensei<Sound> sensei = new Sensei<Sound>(scribe);

	private final String url = "http://example.com/song.mp3";
	private final String urlUnknownExtension = "http://example.com/song.abc";
	private final String urlWithoutExtension = "http://example.com/song";
	private final byte[] data = "data".getBytes();
	private final String dataUri = new DataUri("audio/mp3", data).toString();

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<audio />").cannotParse();
		sensei.assertParseHtml("<audio><source /></audio>").cannotParse();
		sensei.assertParseHtml("<audio><source src=\"" + url + "\" /></audio>").run(hasUrl(url, SoundType.MP3));
		sensei.assertParseHtml("<audio><source src=\"" + urlUnknownExtension + "\" /></audio>").run(hasUrl(urlUnknownExtension, null));
		sensei.assertParseHtml("<audio><source src=\"" + urlWithoutExtension + "\" /></audio>").run(hasUrl(urlWithoutExtension, null));
		sensei.assertParseHtml("<audio><source src=\"" + dataUri + "\" /></audio>").run(hasData(data, SoundType.MP3));

		//without <audio> parent tag
		sensei.assertParseHtml("<source src=\"" + url + "\" />").run(hasUrl(url, SoundType.MP3));

		//"type" attribute overrides file extension
		sensei.assertParseHtml("<source type=\"audio/wav\" src=\"" + url + "\" />").run(hasUrl(url, SoundType.WAV));

		//call super.parseHtml() if it's not an <img> tag
		sensei.assertParseHtml("<object type=\"audio/wav\" data=\"" + url + "\" />").run(hasUrl(url, SoundType.WAV));
	}

	private Check<Sound> hasUrl(final String url, final SoundType contentType) {
		return new Check<Sound>() {
			public void check(Sound actual) {
				assertEquals(url, actual.getUrl());
				assertNull(actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}

	private Check<Sound> hasData(final byte[] data, final SoundType contentType) {
		return new Check<Sound>() {
			public void check(Sound actual) {
				assertNull(actual.getUrl());
				assertArrayEquals(data, actual.getData());
				assertEquals(contentType, actual.getContentType());
			}
		};
	}
}
