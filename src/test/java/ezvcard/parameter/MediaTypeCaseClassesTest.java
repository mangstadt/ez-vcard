package ezvcard.parameter;

import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

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
 */

/**
 * @author Michael Angstadt
 */
public class MediaTypeCaseClassesTest {
	private MediaTypeCaseClasses<TextMediaType> caseClasses;

	@Before
	public void before() {
		caseClasses = new MediaTypeCaseClasses<TextMediaType>(TextMediaType.class);
	}

	@Test
	public void get() {
		assertSame(TextMediaType.XML, caseClasses.get(new String[] { "xml", null, null }));

		TextMediaType rtf = caseClasses.get(new String[] { "rtf", "text/rtf", "rtf" });
		assertSame(rtf, caseClasses.get(new String[] { "rtf", "text/rtf", "rtf" }));
		assertSame(rtf, caseClasses.get(new String[] { "rtf", null, null }));
		assertSame(rtf, caseClasses.get(new String[] { null, "text/rtf", null }));
		assertSame(rtf, caseClasses.get(new String[] { null, null, "rtf" }));
	}

	@Test
	public void find() {
		//find() does not include runtime-created objects
		caseClasses.get(new String[] { "rtf", "text/rtf", "rtf" });

		assertFind("XmL", null, null, TextMediaType.XML);
		assertFind("rtf", null, null, null); //not public
		assertFind(null, "TEXT/xml", null, TextMediaType.XML);
		assertFind(null, "foo/bar", null, null);
		assertFind(null, null, "xML", TextMediaType.XML);
		assertFind(null, null, "foo", null);
		assertFind("xml", "TEXT/xml", null, TextMediaType.XML);
		assertFind("xml", "foo/bar", null, null);
		assertFind("xml", null, "xML", TextMediaType.XML);
		assertFind("xml", null, "foo", null);
		assertFind(null, "text/xml", "xML", TextMediaType.XML);
		assertFind(null, "text/xml", "foo", null);
		assertFind("xml", "text/xml", "xML", TextMediaType.XML);
		assertFind("xml", "text/xml", "foo", null);
	}

	private void assertFind(String type, String mediaType, String extension, TextMediaType expected) {
		TextMediaType actual = caseClasses.find(new String[] { type, mediaType, extension });
		assertSame("Expected = " + expected + ", Actual = " + actual, expected, actual);
	}

	public static class TextMediaType extends MediaTypeParameter {
		public static final TextMediaType PLAIN = new TextMediaType("plain", "text/plain", "txt");
		public static final TextMediaType XML = new TextMediaType("xml", "text/xml", "xml");
		public static final TextMediaType HTML = new TextMediaType("html", "text/html", "html");

		private TextMediaType(String value, String mediaType, String extension) {
			super(value, mediaType, extension);
		}
	}
}
