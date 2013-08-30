package ezvcard.types;

import static ezvcard.util.TestUtils.assertJCardValue;
import static ezvcard.util.TestUtils.assertMarshalXml;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
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
public class TextListTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private final VCard vcard = new VCard();
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final TextListTypeImpl zeroItems = new TextListTypeImpl();
	private final TextListTypeImpl oneItem = new TextListTypeImpl();
	{
		oneItem.addValue("one");
	}
	private final TextListTypeImpl multipleItems = new TextListTypeImpl();
	{
		multipleItems.addValue("one");
		multipleItems.addValue("two");
		multipleItems.addValue("three");
	}
	private final TextListTypeImpl multipleItemsStructured = new TextListTypeImpl(';');
	{
		multipleItemsStructured.addValue("one");
		multipleItemsStructured.addValue("two");
		multipleItemsStructured.addValue("three");
	}
	private final TextListTypeImpl oneItemStructured = new TextListTypeImpl(';');
	{
		oneItemStructured.addValue("one");
	}
	private final TextListTypeImpl specialChars = new TextListTypeImpl();
	{
		specialChars.addValue("on,e");
		specialChars.addValue("tw;o");
		specialChars.addValue("three");
	}
	private TextListTypeImpl testObj;

	@Before
	public void before() {
		warnings.clear();
		testObj = new TextListTypeImpl();
	}

	@Test
	public void validate() {
		assertWarnings(1, zeroItems.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, zeroItems.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, zeroItems.validate(VCardVersion.V4_0, vcard));

		assertWarnings(0, oneItem.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, oneItem.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, oneItem.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void marshalText_zero_items() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "";
		String actual = zeroItems.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_one_item() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "one";
		String actual = oneItem.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_multiple_items() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "one,two,three";
		String actual = multipleItems.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_escape_special_chars() {
		VCardVersion version = VCardVersion.V2_1;
		String expected = "on\\,e,tw\\;o,three";
		String actual = specialChars.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_delimiter() {
		VCardVersion version = VCardVersion.V2_1;
		TextListType t = new TextListType("NAME", '*');
		t.addValue("one");
		t.addValue("two");
		t.addValue("three");
		String expected = "one*two*three";
		String actual = t.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalXml_zero_items() {
		assertMarshalXml(zeroItems, "<text/>");
	}

	@Test
	public void marshalXml_one_item() {
		assertMarshalXml(oneItem, "<text>one</text>");
	}

	@Test
	public void marshalXml_multiple_items() {
		assertMarshalXml(multipleItems, "<text>one</text><text>two</text><text>three</text>");
	}

	@Test
	public void marshalJson_zero_items() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = zeroItems.marshalJson(version);

		assertEquals(VCardDataType.TEXT, value.getDataType());
		assertTrue(value.getValues().isEmpty());
	}

	@Test
	public void marshalJson_one_item() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = oneItem.marshalJson(version);

		assertJCardValue(VCardDataType.TEXT, "one", value);
	}

	@Test
	public void marshalJson_multiple_items() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = multipleItems.marshalJson(version);

		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue("one"),
			new JsonValue("two"),
			new JsonValue("three")
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void marshalJson_structured() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = multipleItemsStructured.marshalJson(version);

		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue("one"),
				new JsonValue("two"),
				new JsonValue("three")
			))
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void marshalJson_structured_one_item() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = oneItemStructured.marshalJson(version);

		assertEquals(VCardDataType.TEXT, value.getDataType());

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue("one")
		);
		//@formatter:on
		assertEquals(expected, value.getValues());
	}

	@Test
	public void unmarshalText_zero_items() {
		VCardVersion version = VCardVersion.V2_1;
		testObj.unmarshalText(subTypes, "", version, warnings, compatibilityMode);

		assertEquals(Arrays.asList(), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_one_item() {
		VCardVersion version = VCardVersion.V2_1;
		testObj.unmarshalText(subTypes, "one", version, warnings, compatibilityMode);

		assertEquals(Arrays.asList("one"), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_multiple_items() {
		VCardVersion version = VCardVersion.V2_1;
		testObj.unmarshalText(subTypes, "one,two,three", version, warnings, compatibilityMode);

		assertEquals(Arrays.asList("one", "two", "three"), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalText_unescape_special_chars() {
		VCardVersion version = VCardVersion.V2_1;
		testObj.unmarshalText(subTypes, "on\\,e,tw\\;o,three", version, warnings, compatibilityMode);

		assertEquals(Arrays.asList("on,e", "tw;o", "three"), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test(expected = CannotParseException.class)
	public void unmarshalXml_zero_values() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TextListTypeImpl.NAME.toLowerCase());
		testObj.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalXml_one_value() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TextListTypeImpl.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, "one");
		testObj.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(Arrays.asList("one"), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalXml_multiple_values() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(TextListTypeImpl.NAME.toLowerCase());
		xe.append(VCardDataType.TEXT, "one");
		xe.append(VCardDataType.TEXT, "two");
		xe.append(VCardDataType.TEXT, "three");
		testObj.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);

		assertEquals(Arrays.asList("one", "two", "three"), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_zero_items() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = new JCardValue(VCardDataType.TEXT, Arrays.<JsonValue> asList());

		testObj.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(Arrays.asList(), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_one_item() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.single(VCardDataType.TEXT, "one");

		testObj.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(Arrays.asList("one"), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_multiple_items() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.multi(VCardDataType.TEXT, "one", "two", "three");

		testObj.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(Arrays.asList("one", "two", "three"), testObj.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void removeValue() {
		testObj.addValue("one");
		testObj.addValue("two");
		testObj.addValue("three");
		testObj.removeValue("two");
		testObj.removeValue("four");
		assertEquals(Arrays.asList("one", "three"), testObj.getValues());
	}

	private class TextListTypeImpl extends TextListType {
		public static final String NAME = "NAME";

		public TextListTypeImpl() {
			this(',');
		}

		public TextListTypeImpl(char separator) {
			super(NAME, separator);
		}
	}
}
