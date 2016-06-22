package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.date;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import ezvcard.property.Revision;
import ezvcard.util.DefaultTimezoneRule;

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
public class RevisionScribeTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final RevisionScribe scribe = new RevisionScribe();
	private final Sensei<Revision> sensei = new Sensei<Revision>(scribe);

	private final Date datetime = date("1980-06-05 13:10:20");
	private final String datetimeStr = "19800605T121020Z";
	private final String datetimeStrExt = "1980-06-05T12:10:20Z";

	private final Revision withValue = new Revision(datetime);
	private final Revision empty = new Revision((Date) null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).versions(V2_1, V4_0).run(datetimeStr);
		sensei.assertWriteText(withValue).versions(V3_0).run(datetimeStrExt);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withValue).run("<timestamp>" + datetimeStr + "</timestamp>");
		sensei.assertWriteXml(empty).run("<timestamp/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run(datetimeStrExt);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(datetimeStr).run(withValue);
		sensei.assertParseText("invalid").cannotParse();
		sensei.assertParseText("").run(empty);
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<timestamp>" + datetimeStr + "</timestamp>").run(withValue);
		sensei.assertParseXml("<timestamp>invalid</timestamp>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<time datetime=\"" + datetimeStrExt + "\">June 5, 1980</time>").run(withValue);
		sensei.assertParseHtml("<time datetime=\"invalid\">June 5, 1980</time>").cannotParse();

		sensei.assertParseHtml("<time>" + datetimeStrExt + "</time>").run(withValue);
		sensei.assertParseHtml("<time>invalid</time>").cannotParse();

		sensei.assertParseHtml("<div>" + datetimeStrExt + "</div>").run(withValue);
		sensei.assertParseHtml("<div>invalid</div>").cannotParse();
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(datetimeStrExt).run(withValue);
		sensei.assertParseJson("invalid").cannotParse();
		sensei.assertParseJson("").run(empty);
	}
}
