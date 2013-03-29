package ezvcard.types;

import static ezvcard.util.HCardUnitTestUtils.toHtmlElement;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.util.XCardUtils;

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
public class ImppTypeTest {
	@Test
	public void aim() {
		ImppType t = ImppType.aim("handle");
		assertEquals("aim:handle", t.getUri().toString());
		assertTrue(t.isAim());
		assertFalse(t.isIrc());
		assertFalse(t.isMsn());
		assertFalse(t.isSip());
		assertFalse(t.isXmpp());
		assertFalse(t.isYahoo());
		assertFalse(t.isSkype());
		assertFalse(t.isIcq());
	}

	@Test
	public void irc() {
		ImppType t = ImppType.irc("handle");
		assertEquals("irc:handle", t.getUri().toString());
		assertFalse(t.isAim());
		assertTrue(t.isIrc());
		assertFalse(t.isMsn());
		assertFalse(t.isSip());
		assertFalse(t.isXmpp());
		assertFalse(t.isYahoo());
		assertFalse(t.isSkype());
		assertFalse(t.isIcq());
	}

	@Test
	public void msn() {
		ImppType t = ImppType.msn("handle");
		assertEquals("msnim:handle", t.getUri().toString());
		assertFalse(t.isAim());
		assertFalse(t.isIrc());
		assertTrue(t.isMsn());
		assertFalse(t.isSip());
		assertFalse(t.isXmpp());
		assertFalse(t.isYahoo());
		assertFalse(t.isSkype());
		assertFalse(t.isIcq());
	}

	@Test
	public void sip() {
		ImppType t = ImppType.sip("handle");
		assertEquals("sip:handle", t.getUri().toString());
		assertFalse(t.isAim());
		assertFalse(t.isIrc());
		assertFalse(t.isMsn());
		assertTrue(t.isSip());
		assertFalse(t.isXmpp());
		assertFalse(t.isYahoo());
		assertFalse(t.isSkype());
		assertFalse(t.isIcq());
	}

	@Test
	public void xmpp() {
		ImppType t = ImppType.xmpp("handle");
		assertEquals("xmpp:handle", t.getUri().toString());
		assertFalse(t.isAim());
		assertFalse(t.isIrc());
		assertFalse(t.isMsn());
		assertFalse(t.isSip());
		assertTrue(t.isXmpp());
		assertFalse(t.isYahoo());
		assertFalse(t.isSkype());
		assertFalse(t.isIcq());
	}

	@Test
	public void yahoo() {
		ImppType t = ImppType.yahoo("handle");
		assertEquals("ymsgr:handle", t.getUri().toString());
		assertFalse(t.isAim());
		assertFalse(t.isIrc());
		assertFalse(t.isMsn());
		assertFalse(t.isSip());
		assertFalse(t.isXmpp());
		assertTrue(t.isYahoo());
		assertFalse(t.isSkype());
		assertFalse(t.isIcq());
	}

	@Test
	public void skype() {
		ImppType t = ImppType.skype("handle");
		assertEquals("skype:handle", t.getUri().toString());
		assertFalse(t.isAim());
		assertFalse(t.isIrc());
		assertFalse(t.isMsn());
		assertFalse(t.isSip());
		assertFalse(t.isXmpp());
		assertFalse(t.isYahoo());
		assertTrue(t.isSkype());
		assertFalse(t.isIcq());
	}

	@Test
	public void icq() {
		ImppType t = ImppType.icq("123456789");
		assertEquals("icq:123456789", t.getUri().toString());
		assertFalse(t.isAim());
		assertFalse(t.isIrc());
		assertFalse(t.isMsn());
		assertFalse(t.isSip());
		assertFalse(t.isXmpp());
		assertFalse(t.isYahoo());
		assertFalse(t.isSkype());
		assertTrue(t.isIcq());
	}

	@Test
	public void marshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		ImppType t;
		String expected, actual;

		t = new ImppType("aim:john.doe@aol.com");
		expected = "aim:john.doe@aol.com";
		actual = t.marshalValue(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		ImppType t;
		Document expected, actual;
		Element element;
		String expectedXml;

		t = new ImppType("aim:john.doe@aol.com");
		expectedXml = "<impp xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		expectedXml += "<uri>aim:john.doe@aol.com</uri>";
		expectedXml += "</impp>";
		expected = XCardUtils.toDocument(expectedXml);
		actual = XCardUtils.toDocument("<impp xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\" />");
		element = XCardUtils.getFirstElement(actual.getChildNodes());
		t.marshalValue(element, version, warnings, compatibilityMode);
		assertXMLEqual(expected, actual);
	}

	@Test
	public void unmarshal() throws Exception {
		VCardVersion version = VCardVersion.V2_1;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		ImppType t;

		t = new ImppType();
		t.unmarshalValue(subTypes, "aim:john.doe@aol.com", version, warnings, compatibilityMode);
		assertEquals("aim:john.doe@aol.com", t.getUri().toString());
	}

	@Test
	public void unmarshalXml() throws Exception {
		VCardVersion version = VCardVersion.V4_0;
		List<String> warnings = new ArrayList<String>();
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		VCardSubTypes subTypes = new VCardSubTypes();
		ImppType t;
		String xml;
		Element element;

		xml = "<impp xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">";
		xml += "<uri>aim:john.doe@aol.com</uri>";
		xml += "</impp>";
		element = XCardUtils.getFirstElement(XCardUtils.toDocument(xml).getChildNodes());
		t = new ImppType();
		t.unmarshalValue(subTypes, element, version, warnings, compatibilityMode);
		assertEquals("aim:john.doe@aol.com", t.getUri().toString());
	}

	@Test
	public void unmarshalHtml() throws Exception {
		List<String> warnings = new ArrayList<String>();

		org.jsoup.nodes.Element element = toHtmlElement("<a href=\"aim:goim?screenname=theuser\">IM me</a>");
		ImppType t = new ImppType();
		t.unmarshalHtml(element, warnings);
		assertEquals("aim:theuser", t.getUri().toString());

		//no "href" attribute
		element = toHtmlElement("<div>aim:goim?screenname=theuser</div>");
		t = new ImppType();
		t.unmarshalHtml(element, warnings);
		assertEquals("aim:theuser", t.getUri().toString());

		//not a valid URI
		element = toHtmlElement("<div>theuser</div>");
		t = new ImppType();
		try {
			t.unmarshalHtml(element, warnings);
			fail();
		} catch (SkipMeException e) {
			//should be thrown
		}
	}

	@Test
	public void parseUriFromLink_aim() throws Exception {
		URI expected = URI.create("aim:theuser");
		URI actual = ImppType.parseUriFromLink("aim:goim?screenname=theuser");
		assertEquals(expected, actual);

		expected = URI.create("aim:theuser");
		actual = ImppType.parseUriFromLink("aim:goim?screenname=theuser&message=hello");
		assertEquals(expected, actual);

		expected = URI.create("aim:theuser");
		actual = ImppType.parseUriFromLink("aim:addbuddy?screenname=theuser");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_yahoo() throws Exception {
		URI expected = URI.create("ymsgr:theuser");
		URI actual = ImppType.parseUriFromLink("ymsgr:sendim?theuser");
		assertEquals(expected, actual);

		expected = URI.create("ymsgr:theuser");
		actual = ImppType.parseUriFromLink("ymsgr:addfriend?theuser");
		assertEquals(expected, actual);

		expected = URI.create("ymsgr:theuser");
		actual = ImppType.parseUriFromLink("ymsgr:sendfile?theuser");
		assertEquals(expected, actual);

		expected = URI.create("ymsgr:theuser");
		actual = ImppType.parseUriFromLink("ymsgr:call?theuser");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_skype() throws Exception {
		URI expected = URI.create("skype:theuser");
		URI actual = ImppType.parseUriFromLink("skype:theuser");
		assertEquals(expected, actual);

		expected = URI.create("skype:theuser");
		actual = ImppType.parseUriFromLink("skype:theuser?call");
		assertEquals(expected, actual);

		expected = URI.create("skype:theuser");
		actual = ImppType.parseUriFromLink("skype:theuser?call&topic=theTopic");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_msn() throws Exception {
		URI expected = URI.create("msnim:theuser");
		URI actual = ImppType.parseUriFromLink("msnim:chat?contact=theuser");
		assertEquals(expected, actual);

		expected = URI.create("msnim:theuser");
		actual = ImppType.parseUriFromLink("msnim:add?contact=theuser");
		assertEquals(expected, actual);

		expected = URI.create("msnim:theuser");
		actual = ImppType.parseUriFromLink("msnim:voice?contact=theuser");
		assertEquals(expected, actual);

		expected = URI.create("msnim:theuser");
		actual = ImppType.parseUriFromLink("msnim:video?contact=theuser");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_xmpp() throws Exception {
		URI expected = URI.create("xmpp:theuser");
		URI actual = ImppType.parseUriFromLink("xmpp:theuser");
		assertEquals(expected, actual);

		expected = URI.create("xmpp:theuser");
		actual = ImppType.parseUriFromLink("xmpp:theuser?message");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_icq() throws Exception {
		URI expected = URI.create("icq:123456789");
		URI actual = ImppType.parseUriFromLink("icq:message?uin=123456789");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_sip() throws Exception {
		URI expected = URI.create("sip:username:password@host:port");
		URI actual = ImppType.parseUriFromLink("sip:username:password@host:port");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_irc() throws Exception {
		URI expected = URI.create("irc://foobar.org/theuser,isnick");
		URI actual = ImppType.parseUriFromLink("irc://foobar.org/theuser,isnick");
		assertEquals(expected, actual);
	}

	@Test
	public void parseUriFromLink_unknown_protocol() throws Exception {
		assertNull(ImppType.parseUriFromLink("foo:theuser"));
		assertNull(ImppType.parseUriFromLink("theuser"));
		assertNull(ImppType.parseUriFromLink("aim:invalid?screenname=theuser"));
	}

	@Test
	public void buildLink_aim() throws Exception {
		ImppType t = ImppType.aim("theuser");
		assertEquals("aim:goim?screenname=theuser", t.buildLink());
	}

	@Test
	public void buildLink_skype() throws Exception {
		ImppType t = ImppType.skype("theuser");
		assertEquals("skype:theuser", t.buildLink());
	}

	@Test
	public void buildLink_icq() throws Exception {
		ImppType t = ImppType.icq("123456789");
		assertEquals("icq:message?uin=123456789", t.buildLink());
	}

	@Test
	public void buildLink_msn() throws Exception {
		ImppType t = ImppType.msn("theuser");
		assertEquals("msnim:chat?contact=theuser", t.buildLink());
	}

	@Test
	public void buildLink_yahoo() throws Exception {
		ImppType t = ImppType.yahoo("theuser");
		assertEquals("ymsgr:sendim?theuser", t.buildLink());
	}

	@Test
	public void buildLink_irc() throws Exception {
		ImppType t = ImppType.irc("theuser");
		assertEquals("irc:theuser", t.buildLink());
	}

	@Test
	public void buildLink_sip() throws Exception {
		ImppType t = ImppType.sip("theuser");
		assertEquals("sip:theuser", t.buildLink());
	}

	@Test
	public void buildLink_xmpp() throws Exception {
		ImppType t = ImppType.xmpp("theuser");
		assertEquals("xmpp:theuser?message", t.buildLink());
	}

	@Test
	public void buildLink_unknown_protocol() throws Exception {
		ImppType t = new ImppType("foo", "bar");
		assertEquals("foo:bar", t.buildLink());
	}
}
