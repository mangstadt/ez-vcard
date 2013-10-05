package ezvcard.parameter;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ezvcard.util.TestUtils.Tests;

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
		assertTrue(TextMediaType.XML == caseClasses.get(new String[] { "xml", null, null }));

		TextMediaType rtf = caseClasses.get(new String[] { "rtf", "text/rtf", "rtf" });
		assertTrue(rtf == caseClasses.get(new String[] { "rtf", "text/rtf", "rtf" }));
		assertTrue(rtf == caseClasses.get(new String[] { "rtf", null, null }));
		assertTrue(rtf == caseClasses.get(new String[] { null, "text/rtf", null }));
		assertTrue(rtf == caseClasses.get(new String[] { null, null, "rtf" }));
	}

	@Test
	public void find() {
		//find() does not include runtime-created objects
		caseClasses.get(new String[] { "rtf", "text/rtf", "rtf" });

		Tests tests = new Tests();
		tests.add("XmL", null, null, TextMediaType.XML);
		tests.add("rtf", null, null, null); //not public
		tests.add(null, "TEXT/xml", null, TextMediaType.XML);
		tests.add(null, "foo/bar", null, null);
		tests.add(null, null, "xML", TextMediaType.XML);
		tests.add(null, null, "foo", null);
		tests.add("xml", "TEXT/xml", null, TextMediaType.XML);
		tests.add("xml", "foo/bar", null, null);
		tests.add("xml", null, "xML", TextMediaType.XML);
		tests.add("xml", null, "foo", null);
		tests.add(null, "text/xml", "xML", TextMediaType.XML);
		tests.add(null, "text/xml", "foo", null);
		tests.add("xml", "text/xml", "xML", TextMediaType.XML);
		tests.add("xml", "text/xml", "foo", null);

		int i = 0;
		for (Object[] test : tests) {
			String type = (String) test[0];
			String mediaType = (String) test[1];
			String extension = (String) test[2];
			TextMediaType expected = (TextMediaType) test[3];

			TextMediaType actual = caseClasses.find(new String[] { type, mediaType, extension });
			assertTrue("Test " + i + ": Expected = " + expected + ", Actual = " + actual, expected == actual);
			i++;
		}
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
