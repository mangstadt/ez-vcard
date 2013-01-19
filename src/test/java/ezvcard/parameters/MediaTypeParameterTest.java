package ezvcard.parameters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
public class MediaTypeParameterTest {
	@Test
	public void findByMediaType() {
		TextMediaType param = MediaTypeParameter.findByMediaType("text/xml", TextMediaType.class);
		assertTrue(TextMediaType.XML == param);
	}

	@Test
	public void findByMediaType_not_found() {
		TextMediaType param = MediaTypeParameter.findByMediaType("text/rtf", TextMediaType.class);
		assertNull(param);
	}

	@Test
	public void findByMediaType_case_insensitive() {
		TextMediaType param = MediaTypeParameter.findByMediaType("tExT/xml", TextMediaType.class);
		assertTrue(TextMediaType.XML == param);
	}

	@Test
	public void equals() {
		TextMediaType html = new TextMediaType("html", "text/html", "html");
		assertTrue(html.equals(TextMediaType.HTML));
	}

	@Test
	public void equals_same_values_different_class() {
		assertFalse(TextMediaType.PLAIN.equals(OtherTextMediaType.PLAIN));
	}

	private static class TextMediaType extends MediaTypeParameter {
		public static final TextMediaType PLAIN = new TextMediaType("plain", "text/plain", "txt");
		public static final TextMediaType XML = new TextMediaType("xml", "text/xml", "xml");
		public static final TextMediaType HTML = new TextMediaType("html", "text/html", "html");

		public TextMediaType(String value, String mediaType, String extension) {
			super(value, mediaType, extension);
		}
	}

	private static class OtherTextMediaType extends MediaTypeParameter {
		public static final OtherTextMediaType PLAIN = new OtherTextMediaType("plain", "text/plain", "txt");

		public OtherTextMediaType(String value, String mediaType, String extension) {
			super(value, mediaType, extension);
		}
	}
}
