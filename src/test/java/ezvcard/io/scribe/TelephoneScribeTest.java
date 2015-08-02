package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.assertSetEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Telephone;
import ezvcard.util.TelUri;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class TelephoneScribeTest {
	private final TelephoneScribe scribe = new TelephoneScribe();
	private final Sensei<Telephone> sensei = new Sensei<Telephone>(scribe);

	private final String text = "(555) 555-1234";
	private final String textWithExt = "+1-555-555-1234 x101";
	private final String uri = "tel:+1-555-555-1234;ext=101";

	private final Telephone withText = new Telephone(text);
	private final Telephone withUri = new Telephone(new TelUri.Builder("+1-555-555-1234").extension("101").build());
	private final Telephone empty = new Telephone((String) null);

	@Test
	public void dataType() {
		sensei.assertDataType(withText).run(VCardDataType.TEXT);
		sensei.assertDataType(withUri).versions(V2_1, V3_0).run(VCardDataType.TEXT);
		sensei.assertDataType(withUri).versions(V4_0).run(VCardDataType.URI);
		sensei.assertDataType(empty).run(VCardDataType.TEXT);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void prepareParameters_type_pref() {
		//TODO move test to VCardPropertyScribeTest

		Telephone property = new Telephone((String) null);
		property.addType(TelephoneType.PREF);

		//2.1 and 3.0 keep it
		sensei.assertPrepareParams(property).versions(V2_1, V3_0).expected("TYPE", "pref").run();

		//4.0 converts it to "PREF=1"
		sensei.assertPrepareParams(property).versions(V4_0).expected("PREF", "1").run();
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void prepareParameters_pref_parameter() {
		VCard vcard = new VCard();

		Telephone tel2 = new Telephone((String) null);
		tel2.setPref(2);
		vcard.addTelephoneNumber(tel2);

		Telephone tel1 = new Telephone((String) null);
		tel1.setPref(1);
		vcard.addTelephoneNumber(tel1);

		Telephone tel3 = new Telephone((String) null);
		vcard.addTelephoneNumber(tel3);

		//2.1 and 3.0 converts the lowest PREF parameter to "TYPE=pref", and removes all the rest
		sensei.assertPrepareParams(tel1).versions(V2_1, V3_0).vcard(vcard).expected("TYPE", "pref").run();
		sensei.assertPrepareParams(tel2).versions(V2_1, V3_0).vcard(vcard).run();
		sensei.assertPrepareParams(tel3).versions(V2_1, V3_0).vcard(vcard).run();

		//4.0 keeps it
		sensei.assertPrepareParams(tel1).versions(V4_0).vcard(vcard).expected("PREF", "1").run();
		sensei.assertPrepareParams(tel2).versions(V4_0).vcard(vcard).expected("PREF", "2").run();
		sensei.assertPrepareParams(tel3).versions(V4_0).vcard(vcard).run();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withText).run(text);
		sensei.assertWriteText(withUri).versions(V2_1, V3_0).run(textWithExt);
		sensei.assertWriteText(withUri).versions(V4_0).run(uri);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withText).run("<text>" + text + "</text>");
		sensei.assertWriteXml(withUri).run("<uri>" + uri + "</uri>");
		sensei.assertWriteXml(empty).run("<text/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withText).run(text);
		sensei.assertWriteJson(withUri).run(uri);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(text).run(is(withText));
		sensei.assertParseText(text).dataType(VCardDataType.TEXT).run(is(withText));
		sensei.assertParseText(text).dataType(VCardDataType.URI).warnings(1).run(is(withText));

		sensei.assertParseText(uri).run(is(withUri));
		sensei.assertParseText(uri).dataType(VCardDataType.TEXT).run(is(withUri));
		sensei.assertParseText(uri).dataType(VCardDataType.URI).run(is(withUri));

		sensei.assertParseText("invalid").run(hasText("invalid"));
		sensei.assertParseText("invalid").dataType(VCardDataType.TEXT).run(hasText("invalid"));
		sensei.assertParseText("invalid").dataType(VCardDataType.URI).warnings(1).run(hasText("invalid"));

		sensei.assertParseText("").run(hasText(""));
		sensei.assertParseText("").dataType(VCardDataType.TEXT).run(hasText(""));
		sensei.assertParseText("").dataType(VCardDataType.URI).warnings(1).run(hasText(""));
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>" + text + "</text>").run(is(withText));
		sensei.assertParseXml("<uri>" + uri + "</uri>").run(is(withUri));

		//prefer <text> to <uri>
		sensei.assertParseXml("<uri>" + uri + "</uri><text>" + text + "</text>").run(is(withText));

		sensei.assertParseXml("<uri>invalid</uri>").warnings(1).run(hasText("invalid"));
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseHtml() {
		//@formatter:off
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"type\">home</span>" +
			"<span class=\"type\">cell</span>" +
			"<span class=\"type\">foo</span>" +
			"<span class=\"value\">" + text + "</span>" +
		"</div>").run(new Check<Telephone>(){
			public void check(Telephone property) {
				is(withText).check(property);
				assertSetEquals(property.getTypes(), TelephoneType.HOME, TelephoneType.CELL, TelephoneType.get("foo"));
			}
		});

		sensei.assertParseHtml("<a href=\"" + uri + "\">Call me</a>").run(is(withUri));
		sensei.assertParseHtml("<a href=\"foo\">" + text + "</a>").run(is(withText));
		//@formatter:on
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(text).dataType(VCardDataType.TEXT).run(is(withText));
		sensei.assertParseJson(text).dataType(VCardDataType.URI).warnings(1).run(is(withText));

		sensei.assertParseJson(uri).dataType(VCardDataType.TEXT).run(is(withUri));
		sensei.assertParseJson(uri).dataType(VCardDataType.URI).run(is(withUri));

		sensei.assertParseJson("invalid").dataType(VCardDataType.TEXT).run(hasText("invalid"));
		sensei.assertParseJson("invalid").dataType(VCardDataType.URI).warnings(1).run(hasText("invalid"));

		sensei.assertParseJson("").dataType(VCardDataType.TEXT).run(hasText(""));
		sensei.assertParseJson("").dataType(VCardDataType.URI).warnings(1).run(hasText(""));
	}

	private Check<Telephone> hasText(final String text) {
		return new Check<Telephone>() {
			public void check(Telephone actual) {
				assertEquals(text, actual.getText());
				assertNull(actual.getUri());
			}
		};
	}

	private Check<Telephone> is(final Telephone expected) {
		return new Check<Telephone>() {
			public void check(Telephone actual) {
				assertEquals(expected.getText(), actual.getText());
				assertEquals(expected.getUri(), actual.getUri());
			}
		};
	}
}
