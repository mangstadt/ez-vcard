package ezvcard.io.scribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.io.scribe.Sensei.Check;
import ezvcard.property.PlaceProperty;
import ezvcard.util.GeoUri;

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
public class PlacePropertyScribeTest {
	private final PlacePropertyScribeImpl scribe = new PlacePropertyScribeImpl();
	private final Sensei<PlaceProperty> sensei = new Sensei<PlaceProperty>(scribe);

	private final String text = "New York, NY";
	private final String textEscaped = "New York\\, NY";
	private final String uri = "http://newyork.gov";
	private final GeoUri geoUriObj = new GeoUri.Builder(40.71448, -74.00598).build();
	private final String geoUri = geoUriObj.toString();
	private final String badGeoUri = "geo:foo,bar";

	private final PlaceProperty withText = new PlaceProperty(text);
	private final PlaceProperty withUri = new PlaceProperty();
	{
		withUri.setUri(uri);
	}
	private final PlaceProperty withGeoUri = new PlaceProperty(geoUriObj.getCoordA(), geoUriObj.getCoordB());
	private final PlaceProperty empty = new PlaceProperty();

	@Test
	public void dataType() {
		sensei.assertDataType(withText).run(VCardDataType.TEXT);
		sensei.assertDataType(withUri).run(VCardDataType.URI);
		sensei.assertDataType(withGeoUri).run(VCardDataType.URI);
		sensei.assertDataType(empty).run(VCardDataType.TEXT);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withText).run(textEscaped);
		sensei.assertWriteText(withUri).run(uri);
		sensei.assertWriteText(withGeoUri).run(geoUri);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withText).run("<text>" + text + "</text>");
		sensei.assertWriteXml(withUri).run("<uri>" + uri + "</uri>");
		sensei.assertWriteXml(withGeoUri).run("<uri>" + geoUri + "</uri>");
		sensei.assertWriteXml(empty).run("<text/>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withText).run(text);
		sensei.assertWriteJson(withUri).run(uri);
		sensei.assertWriteJson(withGeoUri).run(geoUri);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(uri).run(hasText(uri));
		sensei.assertParseText(textEscaped).dataType(VCardDataType.TEXT).run(hasText(text));
		sensei.assertParseText(uri).dataType(VCardDataType.URI).run(hasUri(uri));
		sensei.assertParseText(geoUri).dataType(VCardDataType.URI).run(hasGeoUri(geoUriObj));
		sensei.assertParseText(badGeoUri).dataType(VCardDataType.URI).run(hasUri(badGeoUri));
		sensei.assertParseText("").run(hasText(""));
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>" + text + "</text>").run(hasText(text));
		sensei.assertParseXml("<uri>" + uri + "</uri>").run(hasUri(uri));
		sensei.assertParseXml("<uri>" + geoUri + "</uri>").run(hasGeoUri(geoUriObj));
		sensei.assertParseXml("<uri>" + badGeoUri + "</uri>").run(hasUri(badGeoUri));
		sensei.assertParseXml("<text>" + text + "</text><uri>" + text + "</uri>").run(hasText(text));
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(text).dataType(VCardDataType.TEXT).run(hasText(text));
		sensei.assertParseJson(uri).dataType(VCardDataType.URI).run(hasUri(uri));
		sensei.assertParseJson(geoUri).dataType(VCardDataType.URI).run(hasGeoUri(geoUriObj));
		sensei.assertParseJson(badGeoUri).dataType(VCardDataType.URI).run(hasUri(badGeoUri));

		//use text data type if set to something other than text or uri
		sensei.assertParseJson(text).dataType(VCardDataType.LANGUAGE_TAG).run(hasText(text));
	}

	private Check<PlaceProperty> hasText(final String text) {
		return new Check<PlaceProperty>() {
			public void check(PlaceProperty actual) {
				assertEquals(text, actual.getText());
				assertNull(actual.getUri());
				assertNull(actual.getGeoUri());
			}
		};
	}

	private Check<PlaceProperty> hasUri(final String uri) {
		return new Check<PlaceProperty>() {
			public void check(PlaceProperty actual) {
				assertNull(actual.getText());
				assertEquals(uri, actual.getUri());
				assertNull(actual.getGeoUri());
			}
		};
	}

	private Check<PlaceProperty> hasGeoUri(final GeoUri uri) {
		return new Check<PlaceProperty>() {
			public void check(PlaceProperty actual) {
				assertNull(actual.getText());
				assertNull(actual.getUri());
				assertEquals(uri.getCoordA(), actual.getGeoUri().getCoordA());
				assertEquals(uri.getCoordB(), actual.getGeoUri().getCoordB());
			}
		};
	}

	private class PlacePropertyScribeImpl extends PlacePropertyScribe<PlaceProperty> {
		public PlacePropertyScribeImpl() {
			super(PlaceProperty.class, "PLACE");
		}

		@Override
		protected PlaceProperty newInstance() {
			return new PlaceProperty();
		}
	}
}
