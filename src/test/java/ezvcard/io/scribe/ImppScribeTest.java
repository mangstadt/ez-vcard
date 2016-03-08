package ezvcard.io.scribe;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Impp;

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
public class ImppScribeTest {
	private final ImppScribe scribe = new ImppScribe();
	private final Sensei<Impp> sensei = new Sensei<Impp>(scribe);

	private final String uri = "aim:johndoe";
	private final String badUri = ":::";

	private final Impp withValue = new Impp(uri);
	private final Impp empty = new Impp((URI) null);

	@Test
	public void prepareParameters() {
		Impp withPref = new Impp(uri);
		withPref.setPref(1);
		VCard vcard = new VCard();
		vcard.addImpp(withPref);

		sensei.assertPrepareParams(withPref).vcard(vcard).versions(VCardVersion.V2_1, VCardVersion.V3_0).expected("TYPE", "pref").run();
		sensei.assertPrepareParams(withPref).vcard(vcard).versions(VCardVersion.V4_0).expected("PREF", "1").run();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run(uri);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withValue).run("<uri>" + uri + "</uri>");
		sensei.assertWriteXml(empty).run("<uri/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run(uri);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void escape_special_chars() {
		Impp impp = new Impp("special", "öffentlich");
		String expected = "special:%C3%B6ffentlich";

		sensei.assertWriteText(impp).run(expected);
		sensei.assertWriteXml(impp).run("<uri>" + expected + "</uri>");
		sensei.assertWriteJson(impp).run(expected);
	}

	@Test
	public void parseText() {
		sensei.assertParseText(uri).run(withValue);
		sensei.assertParseText(badUri).cannotParse();
		sensei.assertParseText("").run(empty);
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<uri>" + uri + "</uri>").run(withValue);
		sensei.assertParseXml("<uri>" + badUri + "</uri>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseHtml() {
		sensei.assertParseHtml("<a href=\"aim:goim?screenname=johndoe\">IM me</a>").run(withValue);
		sensei.assertParseHtml("<div>aim:goim?screenname=johndoe</div>").run(withValue);
		sensei.assertParseHtml("<div>johndoe</div>").cannotParse();
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(uri).run(withValue);
		sensei.assertParseJson(badUri).cannotParse();
		sensei.assertParseJson("").run(empty);
	}

	@Test
	public void parseHtmlLink() {
		//@formatter:off
		assertParseHtmlLink("aim:theuser",
			"aim:goim?screenname=theuser",
			"aim:goim?screenname=theuser&message=hello",
			"aim:addbuddy?screenname=theuser"
		);
		
		assertParseHtmlLink("ymsgr:theuser",
			"ymsgr:sendim?theuser",
			"ymsgr:addfriend?theuser",
			"ymsgr:sendfile?theuser",
			"ymsgr:call?theuser"
		);
		
		assertParseHtmlLink("skype:theuser",
			"skype:theuser",
			"skype:theuser?call",
			"skype:theuser?call&topic=theTopic"
		);
		
		assertParseHtmlLink("msnim:theuser",
			"msnim:chat?contact=theuser",
			"msnim:add?contact=theuser",
			"msnim:voice?contact=theuser",
			"msnim:video?contact=theuser"
		);
		
		assertParseHtmlLink("xmpp:theuser",
			"xmpp:theuser",
			"xmpp:theuser?message"
		);
		
		assertParseHtmlLink("icq:123456789",
			"icq:message?uin=123456789"
		);
		
		assertParseHtmlLink("sip:username:password@host:port",
			"sip:username:password@host:port"
		);
		
		assertParseHtmlLink("irc://foobar.org/theuser,isnick",
			"irc://foobar.org/theuser,isnick"
		);
		
		assertParseHtmlLink(null,
			"foo:theuser",
			"theuser",
			"aim:invalid?screenname=theuser"
		);
		//@formatter:on
	}

	private void assertParseHtmlLink(String expectedUri, String... linksToTest) {
		URI expected = (expectedUri == null) ? null : URI.create(expectedUri);
		for (String link : linksToTest) {
			URI actual = scribe.parseHtmlLink(link);
			assertEquals(expected, actual);
		}
	}

	@Test
	public void writeHtmlLink() {
		assertWriteHtmlLink(new Impp((String) null), null);
		assertWriteHtmlLink(Impp.aim("theuser"), "aim:goim?screenname=theuser");
		assertWriteHtmlLink(Impp.skype("theuser"), "skype:theuser");
		assertWriteHtmlLink(Impp.icq("123456789"), "icq:message?uin=123456789");
		assertWriteHtmlLink(Impp.msn("theuser"), "msnim:chat?contact=theuser");
		assertWriteHtmlLink(Impp.yahoo("theuser"), "ymsgr:sendim?theuser");
		assertWriteHtmlLink(Impp.irc("theuser"), "irc:theuser");
		assertWriteHtmlLink(Impp.sip("theuser"), "sip:theuser");
		assertWriteHtmlLink(Impp.xmpp("theuser"), "xmpp:theuser?message");
		assertWriteHtmlLink(new Impp("foo", "bar"), "foo:bar");
	}

	private void assertWriteHtmlLink(Impp property, String expectedUri) {
		String actualUri = scribe.writeHtmlLink(property);
		assertEquals(expectedUri, actualUri);
	}
}
