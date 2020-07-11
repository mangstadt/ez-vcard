package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;

import org.junit.Test;

import ezvcard.io.json.JCardValue;
import ezvcard.property.Organization;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
public class OrganizationScribeTest {
	private final OrganizationScribe scribe = new OrganizationScribe();
	private final Sensei<Organization> sensei = new Sensei<Organization>(scribe);

	private final Organization withOneValue = new Organization();
	{
		withOneValue.getValues().add("one,two;three");
	}

	private final Organization withMultipleValues = new Organization(withOneValue);
	{
		withMultipleValues.getValues().add("four");
	}

	private final Organization empty = new Organization();

	@Test
	public void writeText() {
		sensei.assertWriteText(withOneValue).versions(V2_1).run("one,two\\;three");
		sensei.assertWriteText(withOneValue).versions(V3_0, V4_0).run("one\\,two\\;three");
		sensei.assertWriteText(withMultipleValues).versions(V2_1).run("one,two\\;three;four");
		sensei.assertWriteText(withMultipleValues).versions(V3_0, V4_0).run("one\\,two\\;three;four");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withOneValue).run("<text>one,two;three</text>");
		sensei.assertWriteXml(withMultipleValues).run("<text>one,two;three</text><text>four</text>");
		sensei.assertWriteXml(empty).run("<text/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withOneValue).run("one,two;three");
		sensei.assertWriteJson(withMultipleValues).run(JCardValue.structured("one,two;three", "four"));
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("one\\,two\\;three").run(withOneValue);
		sensei.assertParseText("one\\,two\\;three;four").run(withMultipleValues);
		sensei.assertParseText("").run(empty);
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>one,two;three</text>").run(withOneValue);
		sensei.assertParseXml("<text>one,two;three</text><text>four</text>").run(withMultipleValues);
		sensei.assertParseXml("").cannotParse(0);
	}

	@Test
	public void parseHtml() {
		//@formatter:off
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"organization-name\">one,two;three</span>" +
			"<span class=\"organization-unit\">four</span>" +
		"</div>"
		).run(withMultipleValues);

		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"organization-name\">one,two;three</span>" +
		"</div>"
		).run(withOneValue);
		
		Organization withUnit = new Organization(withMultipleValues);
		withUnit.getValues().remove(0);
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"organization-unit\">four</span>" +
		"</div>"
		).run(withUnit);
		
		Organization withTextContent = new Organization();
		withTextContent.getValues().add("name");
		sensei.assertParseHtml(
		"<div>name</div>"
		).run(withTextContent);
		
		sensei.assertParseHtml(
		"<div>" +
		"</div>"
		).run(empty);
		//@formatter:on
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson("one,two;three").run(withOneValue);
		sensei.assertParseJson(JCardValue.structured("one,two;three")).run(withOneValue);
		sensei.assertParseJson(JCardValue.structured("one,two;three", "four")).run(withMultipleValues);
		sensei.assertParseJson("").run(empty);
	}
}
