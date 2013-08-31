package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardValue;
import ezvcard.util.XCardElement;

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
public class ImppTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCard vcard = new VCard();
	private final String uri = "aim:john.doe@aol.com";
	private final String badUri = ":::";
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final ImppType impp = new ImppType(uri);

	@After
	public void after() {
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void validate() {
		ImppType empty = new ImppType();
		assertWarnings(2, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		assertWarnings(1, impp.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, impp.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, impp.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void aim() {
		ImppType impp = ImppType.aim("handle");
		assertEquals("aim:handle", impp.getUri().toString());
		assertTrue(impp.isAim());
		assertFalse(impp.isIrc());
		assertFalse(impp.isMsn());
		assertFalse(impp.isSip());
		assertFalse(impp.isXmpp());
		assertFalse(impp.isYahoo());
		assertFalse(impp.isSkype());
		assertFalse(impp.isIcq());
	}

	@Test
	public void irc() {
		ImppType impp = ImppType.irc("handle");
		assertEquals("irc:handle", impp.getUri().toString());
		assertFalse(impp.isAim());
		assertTrue(impp.isIrc());
		assertFalse(impp.isMsn());
		assertFalse(impp.isSip());
		assertFalse(impp.isXmpp());
		assertFalse(impp.isYahoo());
		assertFalse(impp.isSkype());
		assertFalse(impp.isIcq());
	}

	@Test
	public void msn() {
		ImppType impp = ImppType.msn("handle");
		assertEquals("msnim:handle", impp.getUri().toString());
		assertFalse(impp.isAim());
		assertFalse(impp.isIrc());
		assertTrue(impp.isMsn());
		assertFalse(impp.isSip());
		assertFalse(impp.isXmpp());
		assertFalse(impp.isYahoo());
		assertFalse(impp.isSkype());
		assertFalse(impp.isIcq());
	}

	@Test
	public void sip() {
		ImppType impp = ImppType.sip("handle");
		assertEquals("sip:handle", impp.getUri().toString());
		assertFalse(impp.isAim());
		assertFalse(impp.isIrc());
		assertFalse(impp.isMsn());
		assertTrue(impp.isSip());
		assertFalse(impp.isXmpp());
		assertFalse(impp.isYahoo());
		assertFalse(impp.isSkype());
		assertFalse(impp.isIcq());
	}

	@Test
	public void xmpp() {
		ImppType impp = ImppType.xmpp("handle");
		assertEquals("xmpp:handle", impp.getUri().toString());
		assertFalse(impp.isAim());
		assertFalse(impp.isIrc());
		assertFalse(impp.isMsn());
		assertFalse(impp.isSip());
		assertTrue(impp.isXmpp());
		assertFalse(impp.isYahoo());
		assertFalse(impp.isSkype());
		assertFalse(impp.isIcq());
	}

	@Test
	public void yahoo() {
		ImppType impp = ImppType.yahoo("handle");
		assertEquals("ymsgr:handle", impp.getUri().toString());
		assertFalse(impp.isAim());
		assertFalse(impp.isIrc());
		assertFalse(impp.isMsn());
		assertFalse(impp.isSip());
		assertFalse(impp.isXmpp());
		assertTrue(impp.isYahoo());
		assertFalse(impp.isSkype());
		assertFalse(impp.isIcq());
	}

	@Test
	public void skype() {
		ImppType impp = ImppType.skype("handle");
		assertEquals("skype:handle", impp.getUri().toString());
		assertFalse(impp.isAim());
		assertFalse(impp.isIrc());
		assertFalse(impp.isMsn());
		assertFalse(impp.isSip());
		assertFalse(impp.isXmpp());
		assertFalse(impp.isYahoo());
		assertTrue(impp.isSkype());
		assertFalse(impp.isIcq());
	}

	@Test
	public void icq() {
		ImppType impp = ImppType.icq("123456789");
		assertEquals("icq:123456789", impp.getUri().toString());
		assertFalse(impp.isAim());
		assertFalse(impp.isIrc());
		assertFalse(impp.isMsn());
		assertFalse(impp.isSip());
		assertFalse(impp.isXmpp());
		assertFalse(impp.isYahoo());
		assertFalse(impp.isSkype());
		assertTrue(impp.isIcq());
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V4_0;
		String actual = impp.marshalText(version, compatibilityMode);
		assertEquals(uri, actual);
	}

	@Test
	public void marshalText_no_uri() {
		VCardVersion version = VCardVersion.V4_0;
		ImppType impp = new ImppType();
		String value = impp.marshalText(version, compatibilityMode);

		assertEquals("", value);
	}

	@Test
	public void marshalXml() {
		assertMarshalXml(impp, "<uri>" + uri + "</uri>");
	}

	@Test
	public void marshalXml_no_uri() {
		assertMarshalXml(new ImppType(), "<uri/>");
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = impp.marshalJson(version);

		assertJCardValue(VCardDataType.URI, uri, value);
	}

	@Test
	public void marshalJson_no_uri() {	
		VCardVersion version = VCardVersion.V4_0;
		ImppType impp = new ImppType();
		JCardValue value = impp.marshalJson(version);

		assertJCardValue(VCardDataType.URI, "", value);
	}

	@Test
	public void unmarshalText() {
		VCardVersion version = VCardVersion.V4_0;
		ImppType impp = new ImppType();
		impp.unmarshalText(subTypes, uri, version, warnings, compatibilityMode);
		assertEquals(uri, impp.getUri().toString());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		ImppType impp = new ImppType();
		impp.unmarshalText(subTypes, badUri, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ImppType.NAME);
		xe.append(VCardDataType.URI, uri);
		ImppType impp = new ImppType();
		impp.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
		assertEquals(uri, impp.getUri().toString());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ImppType.NAME);
		xe.append(VCardDataType.URI, badUri);
		ImppType impp = new ImppType();
		impp.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_no_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(ImppType.NAME);
		ImppType impp = new ImppType();
		impp.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalHtml() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<a href=\"aim:goim?screenname=theuser\">IM me</a>");
		ImppType impp = new ImppType();
		impp.unmarshalHtml(element, warnings);
		assertEquals("aim:theuser", impp.getUri().toString());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_no_href_attribute() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>aim:goim?screenname=theuser</div>");
		ImppType impp = new ImppType();
		impp.unmarshalHtml(element, warnings);
		assertEquals("aim:theuser", impp.getUri().toString());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalHtml_bad_uri() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>theuser</div>");
		ImppType impp = new ImppType();
		impp.unmarshalHtml(element, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.URI, uri);

		ImppType impp = new ImppType();
		impp.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(uri, impp.getUri().toString());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalJson_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.URI, badUri);

		ImppType impp = new ImppType();
		impp.unmarshalJson(subTypes, value, version, warnings);
	}

	@Test
	public void parseUriFromLink_aim() {
		//@formatter:off
		checkUris("aim:theuser", new String[]{
			"aim:goim?screenname=theuser",
			"aim:goim?screenname=theuser&message=hello",
			"aim:addbuddy?screenname=theuser"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_yahoo() {
		//@formatter:off
		checkUris("ymsgr:theuser", new String[]{
			"ymsgr:sendim?theuser",
			"ymsgr:addfriend?theuser",
			"ymsgr:sendfile?theuser",
			"ymsgr:call?theuser"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_skype() {
		//@formatter:off
		checkUris("skype:theuser", new String[]{
			"skype:theuser",
			"skype:theuser?call",
			"skype:theuser?call&topic=theTopic"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_msn() {
		//@formatter:off
		checkUris("msnim:theuser", new String[]{
			"msnim:chat?contact=theuser",
			"msnim:add?contact=theuser",
			"msnim:voice?contact=theuser",
			"msnim:video?contact=theuser"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_xmpp() {
		//@formatter:off
		checkUris("xmpp:theuser", new String[]{
			"xmpp:theuser",
			"xmpp:theuser?message"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_icq() {
		//@formatter:off
		checkUris("icq:123456789", new String[]{
			"icq:message?uin=123456789"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_sip() {
		//@formatter:off
		checkUris("sip:username:password@host:port", new String[]{
			"sip:username:password@host:port"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_irc() {
		//@formatter:off
		checkUris("irc://foobar.org/theuser,isnick", new String[]{
			"irc://foobar.org/theuser,isnick"
		});
		//@formatter:on
	}

	@Test
	public void parseUriFromLink_unknown_protocol() {
		assertNull(ImppType.parseUriFromLink("foo:theuser"));
		assertNull(ImppType.parseUriFromLink("theuser"));
		assertNull(ImppType.parseUriFromLink("aim:invalid?screenname=theuser"));
	}

	@Test
	public void buildLink_no_uri() {
		ImppType impp = new ImppType();
		assertNull(impp.buildLink());
	}

	@Test
	public void buildLink_aim() {
		ImppType impp = ImppType.aim("theuser");
		assertEquals("aim:goim?screenname=theuser", impp.buildLink());
	}

	@Test
	public void buildLink_skype() {
		ImppType impp = ImppType.skype("theuser");
		assertEquals("skype:theuser", impp.buildLink());
	}

	@Test
	public void buildLink_icq() {
		ImppType impp = ImppType.icq("123456789");
		assertEquals("icq:message?uin=123456789", impp.buildLink());
	}

	@Test
	public void buildLink_msn() {
		ImppType impp = ImppType.msn("theuser");
		assertEquals("msnim:chat?contact=theuser", impp.buildLink());
	}

	@Test
	public void buildLink_yahoo() {
		ImppType impp = ImppType.yahoo("theuser");
		assertEquals("ymsgr:sendim?theuser", impp.buildLink());
	}

	@Test
	public void buildLink_irc() {
		ImppType impp = ImppType.irc("theuser");
		assertEquals("irc:theuser", impp.buildLink());
	}

	@Test
	public void buildLink_sip() {
		ImppType impp = ImppType.sip("theuser");
		assertEquals("sip:theuser", impp.buildLink());
	}

	@Test
	public void buildLink_xmpp() {
		ImppType impp = ImppType.xmpp("theuser");
		assertEquals("xmpp:theuser?message", impp.buildLink());
	}

	@Test
	public void buildLink_unknown_protocol() {
		ImppType impp = new ImppType("foo", "bar");
		assertEquals("foo:bar", impp.buildLink());
	}

	@Test
	public void getProtocol() {
		ImppType impp = ImppType.aim("theuser");
		assertEquals("aim", impp.getProtocol());
	}

	@Test
	public void getProtocol_no_uri() {
		ImppType impp = new ImppType();
		assertNull(impp.getProtocol());
	}

	@Test
	public void getHandle() {
		ImppType impp = ImppType.aim("theuser");
		assertEquals("theuser", impp.getHandle());
	}

	@Test
	public void getHandle_no_uri() {
		ImppType impp = new ImppType();
		assertNull(impp.getHandle());
	}

	private void checkUris(String expectedUri, String linksToTest[]) {
		URI expected = URI.create(expectedUri);
		for (String link : linksToTest) {
			URI actual = ImppType.parseUriFromLink(link);
			assertEquals(expected, actual);
		}
	}
}
