package ezvcard.io.scribe;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;

import org.junit.Test;

import ezvcard.property.Geo;
import ezvcard.util.GeoUri;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
public class GeoScribeTest {
	private final GeoScribe scribe = new GeoScribe();
	private final Sensei<Geo> sensei = new Sensei<Geo>(scribe);

	private final Geo withBoth = new Geo(-12.34, 56.78);
	private final Geo withLatitude = new Geo(-12.34, null);
	private final Geo withLongitude = new Geo(null, 56.78);
	private final Geo withManyDecimals = new Geo(-12.3444444444, 56.7777777777);
	private final Geo empty = new Geo((GeoUri) null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withBoth).versions(V2_1, V3_0).run("-12.34;56.78");
		sensei.assertWriteText(withBoth).versions(V4_0).run("geo:-12.34,56.78");

		sensei.assertWriteText(withLatitude).versions(V2_1, V3_0).run("-12.34;0.0");
		sensei.assertWriteText(withLatitude).versions(V4_0).run("geo:-12.34,0.0");

		sensei.assertWriteText(withLongitude).versions(V2_1, V3_0).run("0.0;56.78");
		sensei.assertWriteText(withLongitude).versions(V4_0).run("geo:0.0,56.78");

		sensei.assertWriteText(withManyDecimals).versions(V2_1, V3_0).run("-12.344444;56.777778");
		sensei.assertWriteText(withManyDecimals).versions(V4_0).run("geo:-12.344444,56.777778");

		sensei.assertWriteText(empty).versions(V2_1, V3_0).run("");
		sensei.assertWriteText(empty).versions(V4_0).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withBoth).run("<uri>geo:-12.34,56.78</uri>");
		sensei.assertWriteXml(withLatitude).run("<uri>geo:-12.34,0.0</uri>");
		sensei.assertWriteXml(withLongitude).run("<uri>geo:0.0,56.78</uri>");
		sensei.assertWriteXml(withManyDecimals).run("<uri>geo:-12.344444,56.777778</uri>");
		sensei.assertWriteXml(empty).run("<uri/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withBoth).run("geo:-12.34,56.78");
		sensei.assertWriteJson(withLatitude).run("geo:-12.34,0.0");
		sensei.assertWriteJson(withLongitude).run("geo:0.0,56.78");
		sensei.assertWriteJson(withManyDecimals).run("geo:-12.344444,56.777778");
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("-12.34;56.78").versions(V2_1, V3_0).run(withBoth);
		sensei.assertParseText("-12.34;56.78").versions(V4_0).cannotParse(12);

		sensei.assertParseText("geo:-12.34,56.78").versions(V2_1, V3_0).cannotParse(11);
		sensei.assertParseText("geo:-12.34,56.78").versions(V4_0).run(withBoth);

		sensei.assertParseText("invalid;56.78").versions(V2_1, V3_0).cannotParse(8);
		sensei.assertParseText("-12.34;invalid").versions(V2_1, V3_0).cannotParse(10);
		sensei.assertParseText("invalid;invalid").versions(V2_1, V3_0).cannotParse(8);
		sensei.assertParseText(";56.78").versions(V2_1, V3_0).cannotParse(8);
		sensei.assertParseText("-12.34;").versions(V2_1, V3_0).cannotParse(10);
		sensei.assertParseText("-12.34").versions(V2_1, V3_0).cannotParse(11);
		sensei.assertParseText(";").versions(V2_1, V3_0).cannotParse(8);

		sensei.assertParseText("geo:invalid,56.78").versions(V4_0).cannotParse(12);
		sensei.assertParseText("geo:-12.34,invalid").versions(V4_0).cannotParse(12);
		sensei.assertParseText("geo:invalid,invalid").versions(V4_0).cannotParse(12);
		sensei.assertParseText("geo:,56.78").versions(V4_0).cannotParse(12);
		sensei.assertParseText("geo:-12.34,").versions(V4_0).cannotParse(12);
		sensei.assertParseText("geo:-12.34").versions(V4_0).cannotParse(12);

		sensei.assertParseText("").run(empty);
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<uri>geo:-12.34,56.78</uri>").run(withBoth);
		sensei.assertParseXml("<uri>geo:invalid,56.78</uri>").cannotParse(12);
		sensei.assertParseXml("<uri>geo:-12.34,invalid</uri>").cannotParse(12);
		sensei.assertParseXml("<uri>geo:invalid,invalid</uri>").cannotParse(12);
		sensei.assertParseXml("<uri>geo:,56.78</uri>").cannotParse(12);
		sensei.assertParseXml("<uri>geo:-12.34,</uri>").cannotParse(12);
		sensei.assertParseXml("<uri>geo:-12.34</uri>").cannotParse(12);
		sensei.assertParseXml("<uri>invalid</uri>").cannotParse(12);
		sensei.assertParseXml("").cannotParse(0);
	}

	@Test
	public void parseHtml() {
		//@formatter:off
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
			"<span class=\"longitude\">56.78</span>" +
		"</div>"		
		).run(withBoth);
		
		sensei.assertParseHtml(
		"<div>" +
			"<abbr class=\"latitude\" title=\"-12.34\">S 12° 34</span>" +
			"<abbr class=\"longitude\" title=\"56.78\">E 56° 78</span>" +
		"</div>"		
		).run(withBoth);
		
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"latitude\">" +
				"<span class=\"value-title\" title=\"-12.34\"> </span>" +
			"</span>" +
			"<span class=\"longitude\">" +
				"<span class=\"value-title\" title=\"56.78\"> </span>" +
			"</span>" +
		"</div>"		
		).run(withBoth);
		
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"latitude\">invalid</span>" +
			"<span class=\"longitude\">56.78</span>" +
		"</div>"		
		).cannotParse(8);
		
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
			"<span class=\"longitude\">invalid</span>" +
		"</div>"		
		).cannotParse(10);
		
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"latitude\">invalid</span>" +
			"<span class=\"longitude\">invalid</span>" +
		"</div>"		
		).cannotParse(8);
		
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
		"</div>"		
		).cannotParse(9);
		
		sensei.assertParseHtml(
		"<div>" +
			"<span class=\"longitude\">56.78</span>" +
		"</div>"		
		).cannotParse(7);
		
		sensei.assertParseHtml(
		"<div>" +
		"</div>"		
		).cannotParse(7);
		//@formatter:on
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson("geo:-12.34,56.78").run(withBoth);
		sensei.assertParseJson("geo:invalid,56.78").cannotParse(12);
		sensei.assertParseJson("geo:-12.34,invalid").cannotParse(12);
		sensei.assertParseJson("geo:invalid,invalid").cannotParse(12);
		sensei.assertParseJson("geo:,56.78").cannotParse(12);
		sensei.assertParseJson("geo:-12.34,").cannotParse(12);
		sensei.assertParseJson("geo:-12.34").cannotParse(12);
		sensei.assertParseJson("invalid").cannotParse(12);
		sensei.assertParseJson("").run(empty);
	}
}
