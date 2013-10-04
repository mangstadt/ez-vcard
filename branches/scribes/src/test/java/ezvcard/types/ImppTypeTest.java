package ezvcard.types;

import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;

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
	@Test
	public void validate() {
		VCard vcard = new VCard();

		ImppType empty = new ImppType((String) null);
		assertWarnings(2, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		ImppType withValue = new ImppType("aim:john.doe@aol.com");
		assertWarnings(1, withValue.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V4_0, vcard));
	}

	@Test(expected = IllegalArgumentException.class)
	public void bad_uri() {
		new ImppType(":::");
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
	public void getProtocol() {
		ImppType impp = ImppType.aim("theuser");
		assertEquals("aim", impp.getProtocol());
	}

	@Test
	public void getProtocol_no_uri() {
		ImppType impp = new ImppType((String) null);
		assertNull(impp.getProtocol());
	}

	@Test
	public void getHandle() {
		ImppType impp = ImppType.aim("theuser");
		assertEquals("theuser", impp.getHandle());
	}

	@Test
	public void getHandle_no_uri() {
		ImppType impp = new ImppType((String) null);
		assertNull(impp.getHandle());
	}
}
