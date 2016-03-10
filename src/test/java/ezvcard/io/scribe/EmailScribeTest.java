package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.property.Email;

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
public class EmailScribeTest {
	private final EmailScribe scribe = new EmailScribe();
	private final Sensei<Email> sensei = new Sensei<Email>(scribe);

	@Test
	public void prepareParameters_type_pref() {
		Email property = new Email("johndoe@example.com");
		property.getTypes().add(EmailType.PREF);

		sensei.assertPrepareParams(property).versions(V2_1, V3_0).expected("TYPE", "pref").run();
		sensei.assertPrepareParams(property).versions(V4_0).expected("PREF", "1").run();
	}

	@Test
	public void prepareParameters_pref_parameter() {
		Email property = new Email("johndoe@example.com");
		property.setPref(1);

		VCard vcard = new VCard();
		vcard.addEmail(property);

		sensei.assertPrepareParams(property).versions(V2_1, V3_0).vcard(vcard).expected("TYPE", "pref").run();
		sensei.assertPrepareParams(property).versions(V4_0).vcard(vcard).expected("PREF", "1").run();
	}

	@Test
	public void parseText() {
		Email expected = new Email("johndoe@example.com");
		sensei.assertParseText("johndoe@example.com").run(expected);
	}

	@Test
	public void parseHtml() {
		Email expected = new Email("johndoe@example.com");

		//@formatter:off
		sensei.assertParseHtml(
		"<a href=\"mailto:johndoe@example.com\">Email Me</a>"	
		).run(expected);
		
		sensei.assertParseHtml(
		"<a href=\"MAILTO:johndoe@example.com\">Email Me</a>"	
		).run(expected);
		
		sensei.assertParseHtml(
		"<a href=\"http://www.example.com\">johndoe@example.com</a>"	
		).run(expected);
		
		sensei.assertParseHtml(
		"<div>johndoe@example.com</div>"	
		).run(expected);
		//@formatter:on
	}

	@Test
	public void parseHtml_types() {
		Email expected = new Email("johndoe@example.com");
		expected.getTypes().add(EmailType.HOME);

		//@formatter:off
		sensei.assertParseHtml(
		"<a href=\"mailto:johndoe@example.com\">" +
			"<span class=\"type\">Home</span> Email" +
		"</a>"	
		).run(expected);
		//@formatter:on
	}
}
