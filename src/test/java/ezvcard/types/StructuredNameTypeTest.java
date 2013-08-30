package ezvcard.types;

import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardValue;
import ezvcard.util.JsonValue;
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
public class StructuredNameTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCardSubTypes subTypes = new VCardSubTypes();

	private final StructuredNameType allValues = new StructuredNameType();
	{
		allValues.setGiven("Jonathan");
		allValues.setFamily("Doe");
		allValues.addAdditional("Joh;nny,");
		allValues.addAdditional("John");
		allValues.addPrefix("Mr.");
		allValues.addSuffix("III");
	}

	private final StructuredNameType emptyValues = new StructuredNameType();
	{
		emptyValues.setGiven("Jonathan");
		emptyValues.setFamily(null);
		emptyValues.addAdditional("Joh;nny,");
		emptyValues.addAdditional("John");
	}

	private final StructuredNameType allEmptyValues = new StructuredNameType();

	@After
	public void after() {
		warnings.clear();
	}

	@Test
	public void marshalText() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "Doe;Jonathan;Joh\\;nny\\,,John;Mr.;III";
		String actual = allValues.marshalText(version, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_empty_values() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = ";Jonathan;Joh\\;nny\\,,John;;";
		String actual = emptyValues.marshalText(version, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_all_empty_values() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = ";;;;";
		String actual = allEmptyValues.marshalText(version, compatibilityMode);
		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml() {
		//@formatter:off
		assertMarshalXml(allValues,
		"<surname>Doe</surname>" +
		"<given>Jonathan</given>" +
		"<additional>Joh;nny,</additional>" +
		"<additional>John</additional>" +
		"<prefix>Mr.</prefix>" +
		"<suffix>III</suffix>"
		);
		//@formatter:on
	}

	@Test
	public void marshalXml_empty_values() {
		//@formatter:off
		assertMarshalXml(emptyValues,
		"<surname/>" +
		"<given>Jonathan</given>" +
		"<additional>Joh;nny,</additional>" +
		"<additional>John</additional>" +
		"<prefix/>" +
		"<suffix/>"
		);
		//@formatter:on
	}

	@Test
	public void marshalXml_all_empty_values() {
		//@formatter:off
		assertMarshalXml(allEmptyValues,
		"<surname/>" +
		"<given/>" +
		"<additional/>" +
		"<prefix/>" +
		"<suffix/>"
		);
		//@formatter:on
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = allValues.marshalJson(version);

		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue("Doe"),
				new JsonValue("Jonathan"),
				new JsonValue(Arrays.asList(new JsonValue("Joh;nny,"), new JsonValue("John"))),
				new JsonValue("Mr."),
				new JsonValue("III")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void marshalJson_empty_values() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = emptyValues.marshalJson(version);

		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue(""),
				new JsonValue("Jonathan"),
				new JsonValue(Arrays.asList(new JsonValue("Joh;nny,"), new JsonValue("John"))),
				new JsonValue(""),
				new JsonValue("")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void marshalJson_all_empty_values() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = allEmptyValues.marshalJson(version);

		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue(""),
				new JsonValue("")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void unmarshalText() {
		VCardVersion version = VCardVersion.V2_1;
		StructuredNameType t = new StructuredNameType();
		t.unmarshalText(subTypes, "Doe;Jonathan;Joh\\;nny\\,,John;Mr.;III", version, warnings, compatibilityMode);

		assertEquals("Doe", t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertEquals(Arrays.asList("Mr."), t.getPrefixes());
		assertEquals(Arrays.asList("III"), t.getSuffixes());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_empty_values() {
		VCardVersion version = VCardVersion.V2_1;
		StructuredNameType t = new StructuredNameType();
		t.unmarshalText(subTypes, ";Jonathan;Joh\\;nny\\,,John;;", version, warnings, compatibilityMode);

		assertNull(t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_all_empty_values() {
		VCardVersion version = VCardVersion.V2_1;
		StructuredNameType t = new StructuredNameType();
		t.unmarshalText(subTypes, ";;;;", version, warnings, compatibilityMode);

		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertTrue(t.getAdditional().isEmpty());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_empty_string() {
		VCardVersion version = VCardVersion.V2_1;
		StructuredNameType t = new StructuredNameType();
		t.unmarshalText(subTypes, "", version, warnings, compatibilityMode);

		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertTrue(t.getAdditional().isEmpty());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(StructuredNameType.NAME.toLowerCase());
		xe.append("surname", "Doe");
		xe.append("given", "Jonathan");
		xe.append("additional", "Joh;nny,");
		xe.append("additional", "John");
		xe.append("prefix", "Mr.");
		xe.append("suffix", "III");
		StructuredNameType t = new StructuredNameType();
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals("Doe", t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertEquals(Arrays.asList("Mr."), t.getPrefixes());
		assertEquals(Arrays.asList("III"), t.getSuffixes());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_empty_values() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(StructuredNameType.NAME.toLowerCase());
		xe.append("given", ""); //should convert empty strings to null
		xe.append("additional", "Joh;nny,");
		xe.append("additional", "John");
		StructuredNameType t = new StructuredNameType();
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_all_empty_values() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(StructuredNameType.NAME.toLowerCase());
		StructuredNameType t = new StructuredNameType();
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertTrue(t.getAdditional().isEmpty());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"family-name\">Doe</span>" +
			"<span class=\"given-name\">Jonathan</span>" +
			"<span class=\"additional-name\">Joh;nny,</span>" +
			"<span class=\"additional-name\">John</span>" +
			"<span class=\"honorific-prefix\">Mr.</span>" +
			"<span class=\"honorific-suffix\">III</span>" +
		"</div>");
		//@formatter:on

		StructuredNameType t = new StructuredNameType();
		t.unmarshalHtml(element, warnings);

		assertEquals("Doe", t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertEquals(Arrays.asList("Mr."), t.getPrefixes());
		assertEquals(Arrays.asList("III"), t.getSuffixes());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_empty_values() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
			"<span class=\"given-name\">Jonathan</span>" +
			"<span class=\"additional-name\">Joh;nny,</span>" +
			"<span class=\"additional-name\">John</span>" +
		"</div>");
		//@formatter:on

		StructuredNameType t = new StructuredNameType();
		t.unmarshalHtml(element, warnings);

		assertNull(t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalHtml_all_empty_values() {
		//@formatter:off
		org.jsoup.nodes.Element element = HtmlUtils.toElement(
		"<div>" +
		"</div>");
		//@formatter:on

		StructuredNameType t = new StructuredNameType();
		t.unmarshalHtml(element, warnings);

		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertTrue(t.getAdditional().isEmpty());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;

		List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList("Doe"));
		values.add(Arrays.asList("Jonathan"));
		values.add(Arrays.asList("Joh;nny,", "John"));
		values.add(Arrays.asList("Mr."));
		values.add(Arrays.asList("III"));
		JCardValue value = JCardValue.structured(VCardDataType.TEXT, values);

		StructuredNameType t = new StructuredNameType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("Doe", t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertEquals(Arrays.asList("Mr."), t.getPrefixes());
		assertEquals(Arrays.asList("III"), t.getSuffixes());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_empty_values() {
		VCardVersion version = VCardVersion.V4_0;

		List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList(""));
		values.add(Arrays.asList("Jonathan"));
		values.add(Arrays.asList("Joh;nny,", "John"));
		values.add(Arrays.asList(""));
		values.add(Arrays.asList(""));
		JCardValue value = JCardValue.structured(VCardDataType.TEXT, values);

		StructuredNameType t = new StructuredNameType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getFamily());
		assertEquals("Jonathan", t.getGiven());
		assertEquals(Arrays.asList("Joh;nny,", "John"), t.getAdditional());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_all_empty_values() {
		VCardVersion version = VCardVersion.V4_0;

		List<List<?>> values = new ArrayList<List<?>>();
		values.add(Arrays.asList(""));
		values.add(Arrays.asList(""));
		values.add(Arrays.asList(""));
		values.add(Arrays.asList(""));
		values.add(Arrays.asList(""));
		JCardValue value = JCardValue.structured(VCardDataType.TEXT, values);

		StructuredNameType t = new StructuredNameType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertTrue(t.getAdditional().isEmpty());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_one_empty_string() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.TEXT, "");

		StructuredNameType t = new StructuredNameType();
		t.unmarshalJson(subTypes, value, version, warnings);

		assertNull(t.getFamily());
		assertNull(t.getGiven());
		assertTrue(t.getAdditional().isEmpty());
		assertTrue(t.getPrefixes().isEmpty());
		assertTrue(t.getSuffixes().isEmpty());
		assertWarnings(0, warnings);
	}
}
