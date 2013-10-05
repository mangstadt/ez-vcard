package ezvcard.io.scribe;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

import ezvcard.io.scribe.Sensei.Check;
import ezvcard.types.ImppType;

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
public class ImppScribeTest {
	private final ImppScribe scribe = new ImppScribe();
	private final Sensei<ImppType> sensei = new Sensei<ImppType>(scribe);

	private final String uri = "aim:johndoe";
	private final String badUri = ":::";

	private final ImppType withValue = new ImppType(uri);
	private final ImppType empty = new ImppType((URI) null);

	@Test
	public void marshalText() {
		sensei.assertWriteText(withValue).run(uri);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void marshalXml() {
		sensei.assertWriteXml(withValue).run("<uri>" + uri + "</uri>");
		sensei.assertWriteXml(empty).run("<uri/>");
	}

	@Test
	public void marshalJson() {
		sensei.assertWriteJson(withValue).run(uri);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void unmarshalText() {
		sensei.assertParseText(uri).run(is(withValue));
		sensei.assertParseText(badUri).cannotParse();
		sensei.assertParseText("").run(is(empty));
	}

	@Test
	public void unmarshalXml() {
		sensei.assertParseXml("<uri>" + uri + "</uri>").run(is(withValue));
		sensei.assertParseXml("<uri>" + badUri + "</uri>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void unmarshalHtml() {
		sensei.assertParseHtml("<a href=\"aim:goim?screenname=johndoe\">IM me</a>").run(is(withValue));
		sensei.assertParseHtml("<div>aim:goim?screenname=johndoe</div>").run(is(withValue));
		sensei.assertParseHtml("<div>johndoe</div>").cannotParse();
	}

	@Test
	public void unmarshalJson() {
		sensei.assertParseJson(uri).run(is(withValue));
		sensei.assertParseJson(badUri).cannotParse();
		sensei.assertParseJson("").run(is(empty));
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
		assertWriteHtmlLink(new ImppType((String) null), null);
		assertWriteHtmlLink(ImppType.aim("theuser"), "aim:goim?screenname=theuser");
		assertWriteHtmlLink(ImppType.skype("theuser"), "skype:theuser");
		assertWriteHtmlLink(ImppType.icq("123456789"), "icq:message?uin=123456789");
		assertWriteHtmlLink(ImppType.msn("theuser"), "msnim:chat?contact=theuser");
		assertWriteHtmlLink(ImppType.yahoo("theuser"), "ymsgr:sendim?theuser");
		assertWriteHtmlLink(ImppType.irc("theuser"), "irc:theuser");
		assertWriteHtmlLink(ImppType.sip("theuser"), "sip:theuser");
		assertWriteHtmlLink(ImppType.xmpp("theuser"), "xmpp:theuser?message");
		assertWriteHtmlLink(new ImppType("foo", "bar"), "foo:bar");
	}

	private void assertWriteHtmlLink(ImppType property, String expectedUri) {
		String actualUri = scribe.writeHtmlLink(property);
		assertEquals(expectedUri, actualUri);
	}

	private Check<ImppType> is(final ImppType expected) {
		return new Check<ImppType>() {
			public void check(ImppType actual) {
				assertEquals(expected.getUri(), actual.getUri());
			}
		};
	}
}
