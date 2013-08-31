package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.GeoUri;
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
public class GeoTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCard vcard = new VCard();
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final GeoType withValue = new GeoType(-12.34, 56.7777777777777);
	private GeoType geo;

	@Before
	public void before() {
		warnings.clear();
		subTypes.clear();
		geo = new GeoType();
	}

	@Test
	public void validate() {
		assertWarnings(2, geo.validate(VCardVersion.V2_1, vcard));
		assertWarnings(2, geo.validate(VCardVersion.V3_0, vcard));
		assertWarnings(2, geo.validate(VCardVersion.V4_0, vcard));

		assertWarnings(0, withValue.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withValue.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void marshalText_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "-12.34;56.777778";
		String actual = withValue.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String expected = "-12.34;56.777778";
		String actual = withValue.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String expected = "geo:-12.34,56.777778";
		String actual = withValue.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_latitude_missing() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.setLongitude(56.777778);
		String value = geo.marshalText(version, compatibilityMode);

		assertEquals(";56.777778", value);
	}

	@Test
	public void marshalText_longitude_missing() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.setLatitude(-12.34);
		String value = geo.marshalText(version, compatibilityMode);

		assertEquals("-12.34;", value);
	}

	@Test
	public void marshalText_latitude_and_longitude_missing() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		String value = geo.marshalText(version, compatibilityMode);

		assertEquals(";", value);
	}

	@Test
	public void marshalXml() {
		assertMarshalXml(withValue, "<uri>geo:-12.34,56.777778</uri>");
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = withValue.marshalJson(version);

		assertJCardValue(VCardDataType.URI, "geo:-12.34,56.777778", value);
	}

	@Test
	public void unmarshalText_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		geo.unmarshalText(subTypes, "-12.34;56.7878", version, warnings, compatibilityMode);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		geo.unmarshalText(subTypes, "-12.34;56.7878", version, warnings, compatibilityMode);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		geo.unmarshalText(subTypes, "geo:-12.34,56.7878", version, warnings, compatibilityMode);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_bad_latitude() {
		VCardVersion version = VCardVersion.V2_1;
		geo.unmarshalText(subTypes, "12.34;not-a-number", version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_bad_longitude() {
		VCardVersion version = VCardVersion.V2_1;
		geo.unmarshalText(subTypes, "not-a-number;12.34", version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_missing_longitude() {
		VCardVersion version = VCardVersion.V2_1;
		geo.unmarshalText(subTypes, "12.34", version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_bad_latitude_and_longitude() {
		VCardVersion version = VCardVersion.V2_1;
		geo.unmarshalText(subTypes, "not a;number", version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_bad_value() {
		VCardVersion version = VCardVersion.V2_1;
		geo.unmarshalText(subTypes, "random text", version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalText_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		geo.unmarshalText(subTypes, "bad:uri", version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;

		XCardElement xe = new XCardElement(GeoType.NAME.toLowerCase());
		xe.append(VCardDataType.URI, "geo:-12.34,56.7878");
		Element element = xe.element();
		geo.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(GeoType.NAME.toLowerCase());
		xe.append(VCardDataType.URI, "bad:uri");
		Element element = xe.element();
		geo.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_no_uri() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(GeoType.NAME.toLowerCase());
		Element element = xe.element();
		geo.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalHtml() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
			"<span class=\"longitude\">56.7878</span>" +
		"</div>");
		//@formatter:on

		geo.unmarshalHtml(element, warnings);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalHtml_missing_latitude() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"longitude\">56.7878</span>" +
		"</div>");
		//@formatter:on

		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalHtml_missing_longitude() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
		"</div>");
		//@formatter:on

		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalHtml_bad_latitude() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">bad</span>" +
			"<span class=\"longitude\">56.7878</span>" +
		"</div>");
		//@formatter:on

		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalHtml_bad_longitude() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
			"<span class=\"longitude\">bad</span>" +
		"</div>");
		//@formatter:on

		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalHtml_bad_latitude_and_longitude() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">bad</span>" +
			"<span class=\"longitude\">bad</span>" +
		"</div>");
		//@formatter:on

		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalHtml_missing_latitude_and_longitude() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
		"</div>");
		//@formatter:on

		geo.unmarshalHtml(element, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.URI, "geo:-12.34,56.7878");

		geo.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalJson_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.single(VCardDataType.URI, "bad:uri");

		geo.unmarshalJson(subTypes, value, version, warnings);
	}

	@Test
	public void latitude() {
		assertNull(geo.getLatitude());
		assertNull(geo.getGeoUri().getCoordA());

		geo.setLatitude(-12.34);
		assertEquals(-12.34, geo.getLatitude(), 0.1);
		assertEquals(-12.34, geo.getGeoUri().getCoordA(), 0.1);
	}

	@Test
	public void longitude() {
		assertNull(geo.getLongitude());
		assertNull(geo.getGeoUri().getCoordB());

		geo.setLongitude(56.7878);
		assertEquals(56.7878, geo.getLongitude(), 0.1);
		assertEquals(56.7878, geo.getGeoUri().getCoordB(), 0.1);
	}

	@Test
	public void getGeoUri() {
		GeoUri uri = geo.getGeoUri();
		assertNull(uri.getCoordA());
		assertNull(uri.getCoordB());

		//user can further customize the geo URI
		uri.setCoordA(-12.34);
		uri.setCoordB(56.7878);
		uri.setCoordC(99.11);
		String actual = geo.marshalText(VCardVersion.V4_0, compatibilityMode);
		String expected = "geo:-12.34,56.7878,99.11";
		assertEquals(expected, actual);
	}
}
