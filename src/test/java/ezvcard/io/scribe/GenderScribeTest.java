package ezvcard.io.scribe;

import org.junit.Test;

import ezvcard.io.json.JCardValue;
import ezvcard.property.Gender;

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
public class GenderScribeTest {
	private final GenderScribe scribe = new GenderScribe();
	private final Sensei<Gender> sensei = new Sensei<Gender>(scribe);

	private final String gender = "M";
	private final String text = "te;xt";
	private final String textEscaped = "te\\;xt";

	private final Gender withGender = Gender.male();
	private final Gender withText = new Gender((String) null);
	{
		withText.setText(text);
	}
	private final Gender withGenderAndText = Gender.male();
	{
		withGenderAndText.setText(text);
	}
	private final Gender empty = new Gender((String) null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withGenderAndText).run(gender + ";" + textEscaped);
		sensei.assertWriteText(withGender).run(gender);
		sensei.assertWriteText(withText).run(";" + textEscaped);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withGenderAndText).run("<sex>" + gender + "</sex><identity>" + text + "</identity>");
		sensei.assertWriteXml(withGender).run("<sex>" + gender + "</sex>");
		sensei.assertWriteXml(withText).run("<sex/><identity>" + text + "</identity>");
		sensei.assertWriteXml(empty).run("<sex/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withGenderAndText).run(JCardValue.structured(gender, text));
		sensei.assertWriteJson(withGender).run(gender);
		sensei.assertWriteJson(withText).run(JCardValue.structured(null, text));
		sensei.assertWriteJson(empty).run((String) null);
	}

	@Test
	public void parseText() {
		sensei.assertParseText(gender + ";" + textEscaped).run(withGenderAndText);
		sensei.assertParseText(gender).run(withGender);
		sensei.assertParseText(";" + textEscaped).run(withText);
		sensei.assertParseText("").run(empty);
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<sex>" + gender + "</sex><identity>" + text + "</identity>").run(withGenderAndText);
		sensei.assertParseXml("<sex>" + gender + "</sex>").run(withGender);
		sensei.assertParseXml("<identity>" + text + "</identity>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCardValue.structured(gender, text)).run(withGenderAndText);

		//single-valued array
		sensei.assertParseJson(JCardValue.structured(gender)).run(withGender);

		sensei.assertParseJson(gender).run(withGender);

		sensei.assertParseJson(JCardValue.structured(null, text)).run(withText);
		sensei.assertParseJson("").run(empty);
	}
}
