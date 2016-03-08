package ezvcard.io.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.property.VCardProperty;

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
public class SimplePropertyScribeTest {
	private final SimplePropertyScribeImpl scribe = new SimplePropertyScribeImpl();
	private final Sensei<TestProperty> sensei = new Sensei<TestProperty>(scribe);

	private final String value = "One, two\nthree; four\\.";
	private final String valueEscaped = "One\\, two\nthree\\; four\\\\.";
	private final String valueHtml = "One, two three; four\\.";

	private final TestProperty withValue = new TestProperty(value);
	private final TestProperty empty = new TestProperty(null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run(valueEscaped);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withValue).run("<text>" + value + "</text>");
		sensei.assertWriteXml(empty).run("<text/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run(value);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(valueEscaped).run(withValue);
		sensei.assertParseText("").run(hasText(""));
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>" + value + "</text>").run(withValue);
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<div>" + value + "</div>").run(hasText(valueHtml));
		sensei.assertParseHtml("<div></div>").run(hasText(""));
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(value).run(withValue);
		sensei.assertParseJson("").run(hasText(""));
	}

	private static class SimplePropertyScribeImpl extends SimplePropertyScribe<TestProperty> {
		public SimplePropertyScribeImpl() {
			super(TestProperty.class, "TEST", VCardDataType.TEXT);
		}

		@Override
		protected String _writeValue(TestProperty property) {
			return property.value;
		}

		@Override
		protected TestProperty _parseValue(String value) {
			return new TestProperty(value);
		}
	}

	private static class TestProperty extends VCardProperty {
		public String value;

		public TestProperty(String value) {
			this.value = value;
		}
	}

	private Check<TestProperty> hasText(final String text) {
		return new Check<TestProperty>() {
			public void check(TestProperty actual) {
				assertEquals(text, actual.value);
			}
		};
	}
}
