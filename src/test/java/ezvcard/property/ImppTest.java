package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ezvcard.VCardVersion;

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
public class ImppTest {
	@Test
	public void validate() {
		Impp empty = new Impp((String) null);
		assertValidate(empty).versions(VCardVersion.V2_1).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V3_0).run(8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		Impp withValue = new Impp("aim:john.doe@aol.com");
		assertValidate(withValue).versions(VCardVersion.V2_1).run(2);
		assertValidate(withValue).versions(VCardVersion.V3_0).run();
		assertValidate(withValue).versions(VCardVersion.V4_0).run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void bad_uri() {
		new Impp(":::");
	}

	@Test
	public void aim() {
		Impp impp = Impp.aim("handle");
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
		Impp impp = Impp.irc("handle");
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
		Impp impp = Impp.msn("handle");
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
		Impp impp = Impp.sip("handle");
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
		Impp impp = Impp.xmpp("handle");
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
		Impp impp = Impp.yahoo("handle");
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
		Impp impp = Impp.skype("handle");
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
		Impp impp = Impp.icq("123456789");
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
		Impp impp = Impp.aim("theuser");
		assertEquals("aim", impp.getProtocol());
	}

	@Test
	public void getProtocol_no_uri() {
		Impp impp = new Impp((String) null);
		assertNull(impp.getProtocol());
	}

	@Test
	public void getHandle() {
		Impp impp = Impp.aim("theuser");
		assertEquals("theuser", impp.getHandle());
	}

	@Test
	public void getHandle_no_uri() {
		Impp impp = new Impp((String) null);
		assertNull(impp.getHandle());
	}

	@Test
	public void toStringValues() {
		Impp property = new Impp("protocol", "uri");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Impp original = new Impp((String) null);
		assertCopy(original);

		original = new Impp("aim:username");
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new Impp((String) null),
			new Impp("aim:username"),
			new Impp("aim:username2")
		);
		
		assertEqualsMethod(Impp.class, "aim:username")
		.constructor(new Class<?>[]{String.class}, (String)null).test()
		.constructor("aim:username").test();
		//@formatter:on
	}
}
