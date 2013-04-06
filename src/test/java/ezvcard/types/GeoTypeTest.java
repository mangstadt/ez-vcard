package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.GeoUri;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardDataType;
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
	List<String> warnings = new ArrayList<String>();
	CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	VCard vcard = new VCard();
	VCardSubTypes subTypes = new VCardSubTypes();
	GeoType geo = new GeoType(-12.34, 56.7777777777777);

	@After
	public void after() {
		warnings.clear();
	}

	@Test
	public void marshalSubTypes_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		VCardSubTypes subTypes = geo.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		VCardSubTypes subTypes = geo.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalSubTypes_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		VCardSubTypes subTypes = geo.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertEquals(ValueParameter.URI, subTypes.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "-12.34;56.777778";
		String actual = geo.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		String expected = "-12.34;56.777778";
		String actual = geo.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalText_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		String expected = "geo:-12.34,56.777778";
		String actual = geo.marshalText(version, warnings, compatibilityMode);
		assertEquals(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_latitude_missing() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.setLongitude(56.777778);
		geo.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_longitude_missing() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.setLatitude(-12.34);
		geo.marshalText(version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void marshalText_latitude_and_longitude_missing() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.marshalText(version, warnings, compatibilityMode);
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(GeoType.NAME);
		xe.uri("geo:-12.34,56.777778");
		Document expectedDoc = xe.document();
		xe = new XCardElement(GeoType.NAME);
		Document actualDoc = xe.document();
		Element element = xe.element();
		geo.marshalXml(element, version, warnings, compatibilityMode);
		assertXMLEqual(expectedDoc, actualDoc);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = geo.marshalJson(version, new ArrayList<String>());
		assertEquals(JCardDataType.URI, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ "geo:-12.34,56.777778" })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "-12.34;56.7878", version, warnings, compatibilityMode);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "-12.34;56.7878", version, warnings, compatibilityMode);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "geo:-12.34,56.7878", version, warnings, compatibilityMode);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_bad_latitude() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "12.34;not-a-number", version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_bad_longitude() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "not-a-number;12.34", version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_missing_longitude() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "12.34", version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_bad_latitude_and_longitude() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "not a;number", version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_bad_value() {
		VCardVersion version = VCardVersion.V2_1;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "random text", version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalText_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		GeoType geo = new GeoType();
		geo.unmarshalText(subTypes, "bad:uri", version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		GeoType geo = new GeoType();
		XCardElement xe = new XCardElement("geo");
		xe.uri("geo:-12.34,56.7878");
		Element element = xe.element();
		geo.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		GeoType geo = new GeoType();
		XCardElement xe = new XCardElement("geo");
		xe.uri("bad:uri");
		Element element = xe.element();
		geo.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalXml_no_uri() {
		VCardVersion version = VCardVersion.V4_0;
		GeoType geo = new GeoType();
		XCardElement xe = new XCardElement("geo");
		Element element = xe.element();
		geo.unmarshalXml(subTypes, element, version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalHtml() throws Exception {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
			"<span class=\"longitude\">56.7878</span>" +
		"</div>");
		//@formatter:on

		GeoType geo = new GeoType();
		geo.unmarshalHtml(element, warnings);
		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_missing_latitude() throws Exception {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"longitude\">56.7878</span>" +
		"</div>");
		//@formatter:on

		GeoType geo = new GeoType();
		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_missing_longitude() throws Exception {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
		"</div>");
		//@formatter:on

		GeoType geo = new GeoType();
		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_bad_latitude() throws Exception {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">bad</span>" +
			"<span class=\"longitude\">56.7878</span>" +
		"</div>");
		//@formatter:on

		GeoType geo = new GeoType();
		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_bad_longitude() throws Exception {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">-12.34</span>" +
			"<span class=\"longitude\">bad</span>" +
		"</div>");
		//@formatter:on

		GeoType geo = new GeoType();
		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_bad_latitude_and_longitude() throws Exception {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"latitude\">bad</span>" +
			"<span class=\"longitude\">bad</span>" +
		"</div>");
		//@formatter:on

		GeoType geo = new GeoType();
		geo.unmarshalHtml(element, warnings);
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalHtml_missing_latitude_and_longitude() throws Exception {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
		"</div>");
		//@formatter:on

		GeoType geo = new GeoType();
		geo.unmarshalHtml(element, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.uri("geo:-12.34,56.7878");

		GeoType geo = new GeoType();
		geo.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(-12.34, geo.getLatitude(), 0.00001);
		assertEquals(56.7878, geo.getLongitude(), 0.00001);
		assertEquals(0, warnings.size());
	}

	@Test(expected = SkipMeException.class)
	public void unmarshalJson_bad_uri() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = JCardValue.uri("bad:uri");

		GeoType geo = new GeoType();
		geo.unmarshalJson(subTypes, value, version, warnings);
	}

	@Test
	public void latitude() {
		GeoType geo = new GeoType();
		assertNull(geo.getLatitude());
		assertNull(geo.getGeoUri().getCoordA());

		geo.setLatitude(-12.34);
		assertEquals(-12.34, geo.getLatitude(), 0.1);
		assertEquals(-12.34, geo.getGeoUri().getCoordA(), 0.1);
	}

	@Test
	public void longitude() {
		GeoType geo = new GeoType();
		assertNull(geo.getLongitude());
		assertNull(geo.getGeoUri().getCoordB());

		geo.setLongitude(56.7878);
		assertEquals(56.7878, geo.getLongitude(), 0.1);
		assertEquals(56.7878, geo.getGeoUri().getCoordB(), 0.1);
	}

	@Test
	public void getGeoUri() {
		GeoType geo = new GeoType();
		GeoUri uri = geo.getGeoUri();
		assertNull(uri.getCoordA());
		assertNull(uri.getCoordB());

		//user can further customize the geo URI
		uri.setCoordA(-12.34);
		uri.setCoordB(56.7878);
		uri.setCoordC(99.11);
		String actual = geo.marshalText(VCardVersion.V4_0, warnings, compatibilityMode);
		String expected = "geo:-12.34,56.7878,99.11";
		assertEquals(expected, actual);
	}
}
