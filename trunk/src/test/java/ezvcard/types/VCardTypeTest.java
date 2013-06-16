package ezvcard.types;

import static ezvcard.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ValueParameter;
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
public class VCardTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();
	final VCardTypeImpl type = new VCardTypeImpl();
	{
		type.value = "value";
	}
	VCardTypeImpl t;

	@Before
	public void before() {
		t = new VCardTypeImpl();
		subTypes.clear();
		warnings.clear();
	}

	@Test
	public void marshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(VCardTypeImpl.NAME.toLowerCase());
		xe.append("unknown", type.value);
		Document expectedDoc = xe.document();
		xe = new XCardElement(VCardTypeImpl.NAME.toLowerCase());
		Document actualDoc = xe.document();
		type.marshalXml(xe.element(), version, warnings, compatibilityMode);

		assertXMLEqual(expectedDoc, actualDoc);
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson() {
		VCardVersion version = VCardVersion.V4_0;
		JCardValue value = type.marshalJson(version, warnings);
		assertEquals(JCardDataType.UNKNOWN, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ type.value })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test
	public void marshalJson_value_parameter() {
		VCardVersion version = VCardVersion.V4_0;
		VCardTypeImpl type = new VCardTypeImpl();
		type.value = "value";
		type.getSubTypes().setValue(ValueParameter.BOOLEAN);

		JCardValue value = type.marshalJson(version, warnings);
		assertEquals(JCardDataType.BOOLEAN, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ type.value })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertWarnings(0, warnings);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void unmarshalXml() {
		VCardVersion version = VCardVersion.V4_0;
		XCardElement xe = new XCardElement(VCardTypeImpl.NAME.toLowerCase());
		t.unmarshalXml(subTypes, xe.element(), version, warnings, compatibilityMode);
	}

	@Test
	public void unmarshalHtml() {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + type.value + "</div>");

		t.unmarshalHtml(element, warnings);

		assertEquals(type.value, t.value);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.text(type.value);

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals(type.value, t.value);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_special_chars() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.text("va,l;ue\\");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("va\\,l\\;ue\\\\", t.value);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_list() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.text("one", "two", "three");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("one,two,three", t.value);
		assertWarnings(0, warnings);
	}

	@Test
	public void unmarshalJson_structured() {
		VCardVersion version = VCardVersion.V4_0;

		JCardValue value = JCardValue.text();
		value.setStructured(true);
		value.addValues("one", Arrays.asList("two", "three"), "four");

		t.unmarshalJson(subTypes, value, version, warnings);

		assertEquals("one;two,three;four", t.value);
		assertWarnings(0, warnings);
	}

	@Test
	public void getSupportedVersions() {
		assertArrayEquals(VCardVersion.values(), t.getSupportedVersions());
	}

	@Test
	public void getTypeName() {
		assertEquals(VCardTypeImpl.NAME, t.getTypeName());
	}

	@Test
	public void group() {
		assertNull(t.getGroup());

		t.setGroup("group");
		assertEquals("group", t.getGroup());
	}

	@Test
	public void getQName() {
		assertNull(t.getQName());
	}

	@Test
	public void marshalSubTypes() {
		assertFalse(t.getSubTypes() == t.marshalSubTypes(VCardVersion.V2_1, warnings, compatibilityMode, vcard)); //should create a copy
	}

	@Test
	public void compareTo() {
		VCardTypeImpl one = new VCardTypeImpl();
		one.getSubTypes().setPref(1);

		VCardTypeImpl two = new VCardTypeImpl();
		one.getSubTypes().setPref(2);

		VCardTypeImpl null1 = new VCardTypeImpl();
		VCardTypeImpl null2 = new VCardTypeImpl();

		assertEquals(-1, one.compareTo(two));
		assertEquals(1, two.compareTo(one));
		assertEquals(0, one.compareTo(one));
		assertEquals(-1, one.compareTo(null1));
		assertEquals(1, null1.compareTo(one));
		assertEquals(0, null1.compareTo(null2));
	}

	private class VCardTypeImpl extends VCardType {
		public static final String NAME = "NAME";
		public String value;

		public VCardTypeImpl() {
			super(NAME);
		}

		@Override
		protected void doMarshalText(StringBuilder value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			value.append(this.value);
		}

		@Override
		protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			this.value = value;
		}
	}
}
