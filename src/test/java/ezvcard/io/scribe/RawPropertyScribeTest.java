package ezvcard.io.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.property.RawProperty;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
public class RawPropertyScribeTest {
	private final RawPropertyScribe scribe = new RawPropertyScribe("RAW");
	private final Sensei<RawProperty> sensei = new Sensei<RawProperty>(scribe);

	private final RawProperty withValue = new RawProperty("RAW", "value");
	private final RawProperty empty = new RawProperty("RAW", null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run("value");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("value").run(has("RAW", "value", null));
		sensei.assertParseText("value").dataType(VCardDataType.TEXT).run(has("RAW", "value", VCardDataType.TEXT));
		sensei.assertParseText("").run(has("RAW", "", null));
		sensei.assertParseText("").dataType(VCardDataType.TEXT).run(has("RAW", "", VCardDataType.TEXT));
	}

	private Check<RawProperty> has(final String name, final String value, final VCardDataType dataType) {
		return new Check<RawProperty>() {
			public void check(RawProperty property) {
				assertEquals(name, property.getPropertyName());
				assertEquals(value, property.getValue());
				assertEquals(dataType, property.getDataType());
			}
		};
	}
}
