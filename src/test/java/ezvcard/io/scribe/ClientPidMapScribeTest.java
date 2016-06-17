package ezvcard.io.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ezvcard.io.json.JCardValue;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.property.ClientPidMap;

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
public class ClientPidMapScribeTest {
	private final ClientPidMapScribe scribe = new ClientPidMapScribe();
	private final Sensei<ClientPidMap> sensei = new Sensei<ClientPidMap>(scribe);

	private final int pid = 1;
	private final String uri = "urn:uuid:1234";

	private final ClientPidMap withValue = new ClientPidMap(pid, uri);
	private final ClientPidMap empty = new ClientPidMap(null, null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run(pid + ";" + uri);
		sensei.assertWriteText(empty).run("");
		sensei.assertWriteText(empty).includeTrailingSemicolons(true).run(";");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withValue).run("<uri>" + uri + "</uri><sourceid>" + pid + "</sourceid>");
		sensei.assertWriteXml(empty).run("<sourceid/><uri/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run(JCardValue.structured(1, "urn:uuid:1234"));
		sensei.assertWriteJson(empty).run(JCardValue.structured("", ""));
	}

	@Test
	public void parseText() {
		sensei.assertParseText(pid + ";" + uri).run(withValue);
		sensei.assertParseText(pid + ";" + uri + ";foo").run(has(pid, uri + ";foo"));
		sensei.assertParseText("no semicolon").cannotParse();
		sensei.assertParseText("not-a-number;bar").cannotParse();
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<uri>" + uri + "</uri><sourceid>" + pid + "</sourceid>").run(withValue);

		sensei.assertParseXml("<uri>" + uri + "</uri><sourceid>not-a-number</sourceid>").cannotParse();
		sensei.assertParseXml("<uri>" + uri + "</uri>").cannotParse();
		sensei.assertParseXml("<sourceid>" + pid + "</sourceid>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseJson() {
		JCardValue value = JCardValue.structured(pid + "", uri);
		sensei.assertParseJson(value).run(withValue);

		value = JCardValue.structured("not-a-number", uri);
		sensei.assertParseJson(value).cannotParse();

		value = JCardValue.structured(pid + "");
		sensei.assertParseJson(value).run(has(pid, null));
	}

	private Check<ClientPidMap> has(final Integer pid, final String uri) {
		return new Check<ClientPidMap>() {
			public void check(ClientPidMap actual) {
				assertEquals(pid, actual.getPid());
				assertEquals(uri, actual.getUri());
			}
		};
	}
}
