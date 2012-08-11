package ezvcard.parameters;

import org.junit.Test;
import static org.junit.Assert.*;

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
public class MediaTypeParameterTest {
	@Test
	public void findByMediaType() {
		TextMediaType expected = TextMediaType.xml;
		TextMediaType actual = TextMediaType.findByMediaType("TEXT/xml", TextMediaType.class);
		assertEquals(expected, actual);

		actual = TextMediaType.findByMediaType("text/rtf", TextMediaType.class);
		assertNull(actual);
	}

	@Test
	public void equals() {
		TextMediaType html = new TextMediaType("html", "text/html", "html");
		assertTrue(html.equals(TextMediaType.html));

		assertFalse(TextMediaType.plain.equals(OtherTextMediaType.plain));
	}

	public static class TextMediaType extends MediaTypeParameter {
		public static final TextMediaType plain = new TextMediaType("plain", "text/plain", "txt");
		public static final TextMediaType xml = new TextMediaType("xml", "text/xml", "xml");
		public static final TextMediaType html = new TextMediaType("html", "text/html", "html");

		public TextMediaType(String value, String mediaType, String extension) {
			super(value, mediaType, extension);
		}
	}

	public static class OtherTextMediaType extends MediaTypeParameter {
		public static final OtherTextMediaType plain = new OtherTextMediaType("plain", "text/plain", "txt");

		public OtherTextMediaType(String value, String mediaType, String extension) {
			super(value, mediaType, extension);
		}
	}
}
