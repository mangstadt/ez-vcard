package ezvcard;

import static ezvcard.util.TestUtils.assertCollectionContains;
import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertValidate;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import ezvcard.property.Gender;
import ezvcard.property.HasAltId;
import ezvcard.property.Note;
import ezvcard.property.RawProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.VCardProperty;
import ezvcard.util.StringUtils;

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
public class VCardTest {
	@Test
	public void getProperties() {
		VCard vcard = new VCard();
		assertCollectionContains(vcard.getProperties());
		assertEquals(asList(), vcard.getProperties(Note.class));
		assertEquals(asList(), vcard.getProperties(RawProperty.class));

		Note property1 = new Note("value");
		vcard.addProperty(property1);
		assertCollectionContains(vcard.getProperties(), property1);
		assertEquals(asList(property1), vcard.getProperties(Note.class));
		assertEquals(asList(), vcard.getProperties(RawProperty.class));

		RawProperty property2 = vcard.addExtendedProperty("X-GENDER", "male");
		RawProperty property3 = vcard.addExtendedProperty("X-MANAGER", "Michael Scott");
		assertCollectionContains(vcard.getProperties(), property1, property2, property3);
		assertEquals(asList(property1), vcard.getProperties(Note.class));
		assertEquals(asList(property2, property3), vcard.getProperties(RawProperty.class));

	}

	@Test
	public void addProperty() {
		VCard vcard = new VCard();
		assertEquals(asList(), vcard.getProperties(Note.class));

		Note property1 = new Note("value");
		vcard.addProperty(property1);
		assertEquals(asList(property1), vcard.getProperties(Note.class));

		Note property2 = new Note("value2");
		vcard.addProperty(property2);
		assertEquals(asList(property1, property2), vcard.getProperties(Note.class));
	}

	@Test
	public void setProperty() {
		VCard vcard = new VCard();

		Note property1 = new Note("value");
		assertEquals(asList(), vcard.setProperty(property1));
		assertEquals(asList(property1), vcard.getProperties(Note.class));

		Note property2 = new Note("value2");
		assertEquals(asList(property1), vcard.setProperty(property2));
		assertEquals(asList(property2), vcard.getProperties(Note.class));

		Note property3 = new Note("value3");
		vcard.addProperty(property3);
		assertEquals(asList(property2, property3), vcard.getProperties(Note.class));

		Note property4 = new Note("value4");
		assertEquals(asList(property2, property3), vcard.setProperty(property4));
		assertEquals(asList(property4), vcard.getProperties(Note.class));
	}

	@Test
	public void removeProperty() {
		VCard vcard = new VCard();

		Note property1 = new Note("value");
		assertFalse(vcard.removeProperty(property1));

		vcard.addProperty(property1);
		assertEquals(asList(property1), vcard.getProperties(Note.class));
		assertTrue(vcard.removeProperty(property1));
		assertEquals(asList(), vcard.getProperties(Note.class));
	}

	@Test
	public void removeProperties() {
		VCard vcard = new VCard();

		assertEquals(asList(), vcard.removeProperties(Note.class));

		Note property1 = new Note("value");
		Note property2 = new Note("value2");
		vcard.addProperty(property1);
		vcard.addProperty(property2);
		assertEquals(asList(property1, property2), vcard.getProperties(Note.class));

		assertEquals(asList(property1, property2), vcard.removeProperties(Note.class));
		assertEquals(asList(), vcard.getProperties(Note.class));
	}

	@Test
	public void getExtendedProperty() {
		VCard vcard = new VCard();
		assertNull(vcard.getExtendedProperty("NAME"));

		vcard.addExtendedProperty("NAME2", "value");
		assertNull(vcard.getExtendedProperty("NAME"));

		RawProperty property = vcard.addExtendedProperty("NAME", "value");
		assertEquals(property, vcard.getExtendedProperty("NAME"));
		assertEquals(property, vcard.getExtendedProperty("name"));

		vcard.addExtendedProperty("NAME", "value2");
		assertEquals(property, vcard.getExtendedProperty("NAME"));
	}

	@Test
	public void addExtendedProperty() {
		VCard vcard = new VCard();
		assertEquals(asList(), vcard.getExtendedProperties());

		RawProperty property = vcard.addExtendedProperty("NAME", "value");
		assertEquals("NAME", property.getPropertyName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());
		assertEquals(asList(property), vcard.getExtendedProperties());

		RawProperty property2 = vcard.addExtendedProperty("NAME", "value", VCardDataType.TEXT);
		assertEquals("NAME", property2.getPropertyName());
		assertEquals("value", property2.getValue());
		assertEquals(VCardDataType.TEXT, property2.getDataType());
		assertEquals(asList(property, property2), vcard.getExtendedProperties());
	}

	@Test
	public void setExtendedProperty() {
		VCard vcard = new VCard();
		assertEquals(asList(), vcard.getExtendedProperties());

		RawProperty property = vcard.setExtendedProperty("NAME", "value");
		assertEquals("NAME", property.getPropertyName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());
		assertEquals(asList(property), vcard.getExtendedProperties());

		RawProperty property2 = vcard.setExtendedProperty("NAME", "value", VCardDataType.TEXT);
		assertEquals("NAME", property2.getPropertyName());
		assertEquals("value", property2.getValue());
		assertEquals(VCardDataType.TEXT, property2.getDataType());
		assertEquals(asList(property2), vcard.getExtendedProperties());
	}

	@Test
	public void removeExtendedProperty() {
		VCard vcard = new VCard();
		assertEquals(asList(), vcard.removeExtendedProperty("NAME"));
		assertEquals(asList(), vcard.getExtendedProperties());

		RawProperty property = vcard.addExtendedProperty("NAME", "value");
		RawProperty property2 = vcard.addExtendedProperty("NAME", "value2");
		RawProperty property3 = vcard.addExtendedProperty("NAME2", "value");
		assertEquals(asList(property, property2, property3), vcard.getExtendedProperties());

		assertEquals(asList(property, property2), vcard.removeExtendedProperty("NAME"));
		assertEquals(asList(property3), vcard.getExtendedProperties());
	}

	@Test
	public void addPropertyAlt() {
		VCard vcard = new VCard();

		HasAltIdImpl one1 = new HasAltIdImpl("1");
		vcard.addProperty(one1);
		HasAltIdImpl null1 = new HasAltIdImpl(null);
		vcard.addProperty(null1);

		HasAltIdImpl two1 = new HasAltIdImpl("3");
		HasAltIdImpl two2 = new HasAltIdImpl(null);

		vcard.addPropertyAlt(HasAltIdImpl.class, two1, two2);

		assertEquals(asList(one1, null1, two1, two2), vcard.getProperties(HasAltIdImpl.class));

		assertEquals("1", one1.getAltId());
		assertEquals(null, null1.getAltId());
		assertEquals("2", two1.getAltId());
		assertEquals("2", two2.getAltId());
	}

	@Test
	public void setPropertyAlt() {
		VCard vcard = new VCard();

		HasAltIdImpl property = new HasAltIdImpl(null);
		HasAltIdImpl property2 = new HasAltIdImpl(null);
		assertEquals(asList(), vcard.setPropertyAlt(HasAltIdImpl.class, property, property2));

		HasAltIdImpl property3 = new HasAltIdImpl(null);
		HasAltIdImpl property4 = new HasAltIdImpl(null);
		assertEquals(asList(property, property2), vcard.setPropertyAlt(HasAltIdImpl.class, property3, property4));
	}

	@Test
	public void generateAltId() {
		Collection<HasAltIdImpl> list = asList(new HasAltIdImpl("1"), new HasAltIdImpl("1"), new HasAltIdImpl("2"));
		assertEquals("3", VCard.generateAltId(list));

		list = asList(new HasAltIdImpl("1"), new HasAltIdImpl("1"), new HasAltIdImpl("3"));
		assertEquals("2", VCard.generateAltId(list));

		list = asList(new HasAltIdImpl("2"), new HasAltIdImpl("2"), new HasAltIdImpl("3"));
		assertEquals("1", VCard.generateAltId(list));

		list = asList();
		assertEquals("1", VCard.generateAltId(list));

		list = asList(new HasAltIdImpl("one"), new HasAltIdImpl("one"), new HasAltIdImpl("three"));
		assertEquals("1", VCard.generateAltId(list));
	}

	@Test
	public void getPropertiesAlt() {
		VCard vcard = new VCard();
		assertEquals(asList(), vcard.getPropertiesAlt(HasAltIdImpl.class));

		HasAltIdImpl one1 = new HasAltIdImpl("1");
		vcard.addProperty(one1);
		HasAltIdImpl null1 = new HasAltIdImpl(null);
		vcard.addProperty(null1);
		HasAltIdImpl two1 = new HasAltIdImpl("2");
		vcard.addProperty(two1);
		HasAltIdImpl one2 = new HasAltIdImpl("1");
		vcard.addProperty(one2);
		HasAltIdImpl null2 = new HasAltIdImpl(null);
		vcard.addProperty(null2);

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<HasAltIdImpl>> expected = asList(
			asList(one1, one2),
			asList(two1),
			asList(null1),
			asList(null2)
		);
		//@formatter:on
		assertEquals(expected, vcard.getPropertiesAlt(HasAltIdImpl.class));
	}

	@Test
	public void validate_vcard() {
		VCard vcard = new VCard();

		assertValidate(vcard).versions(VCardVersion.V2_1).prop(null, 0).run();
		assertValidate(vcard).versions(VCardVersion.V3_0).prop(null, 0, 1).run();
		assertValidate(vcard).versions(VCardVersion.V4_0).prop(null, 1).run();

		vcard.setFormattedName("John Doe");
		assertValidate(vcard).versions(VCardVersion.V2_1, VCardVersion.V3_0).prop(null, 0).run();
		assertValidate(vcard).versions(VCardVersion.V4_0).run();

		vcard.setStructuredName(new StructuredName());
		assertValidate(vcard).run();
	}

	@Test
	public void validate_properties() {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		VCardPropertyImpl prop = new VCardPropertyImpl();
		vcard.addProperty(prop);

		assertValidate(vcard).versions(VCardVersion.V4_0).prop(prop, 0).run();
		assertEquals(VCardVersion.V4_0, prop.validateVersion);
		assertSame(vcard, prop.validateVCard);
	}

	@Test
	public void copy() {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.setVersion(VCardVersion.V2_1);

		VCard copy = new VCard(vcard);

		assertEquals(vcard.getVersion(), copy.getVersion());
		assertPropertyCount(1, copy);
		assertNotSame(vcard.getFormattedName(), copy.getFormattedName());
		assertEquals(vcard.getFormattedName().getValue(), copy.getFormattedName().getValue());
		assertEquals(vcard, copy);
	}

	@Test
	public void equals_essentials() {
		VCard one = new VCard();
		one.setFormattedName("Name");
		assertEqualsMethodEssentials(one);
	}

	@Test
	public void equals_different_version() {
		VCard one = new VCard();
		one.setFormattedName("Name");
		one.setVersion(VCardVersion.V2_1);

		VCard two = new VCard();
		two.setFormattedName("Name");
		two.setVersion(VCardVersion.V3_0);

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_different_number_of_properties() {
		VCard one = new VCard();
		one.setFormattedName("Name");

		VCard two = new VCard();
		two.setGender(Gender.male());
		two.setFormattedName("Name");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_properties_not_equal() {
		VCard one = new VCard();
		one.setFormattedName("John");

		VCard two = new VCard();
		two.setFormattedName("Jane");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_ignore_property_order() {
		VCard one = new VCard();
		one.setFormattedName("Name");
		one.setGender(Gender.male());
		one.addNote("Note 1");
		one.addNote("Note 2");

		VCard two = new VCard();
		two.setGender(Gender.male());
		two.setFormattedName("Name");
		two.addNote("Note 2");
		two.addNote("Note 1");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_multiple_identical_properties() {
		VCard one = new VCard();
		one.setFormattedName("Name");
		one.setGender(Gender.male());
		one.addNote("Note 1");
		one.addNote("Note 1");

		VCard two = new VCard();
		two.setFormattedName("Name");
		two.setGender(Gender.male());
		two.addNote("Note 1");
		two.addNote("Note 1");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void toString_() {
		VCard vcard = new VCard();
		assertEquals("version=3.0", vcard.toString());

		vcard.setVersion(null);
		assertEquals("version=null", vcard.toString());

		vcard.addNote("value");
		assertEquals("version=null" + StringUtils.NEWLINE + "ezvcard.property.Note [ group=null | parameters={} | value=value ]", vcard.toString());
	}

	/**
	 * This tests to make sure that, if some hashing mechanism is used to
	 * determine equality, identical properties in the same vCard are not
	 * treated as a single property when they are put in a HashSet.
	 */
	@Test
	public void equals_multiple_identical_properties_not_equal() {
		VCard one = new VCard();
		one.setFormattedName("Name");
		one.setGender(Gender.male());
		one.addNote("Note 1");
		one.addNote("Note 1");
		one.addNote("Note 2");

		VCard two = new VCard();
		two.setFormattedName("Name");
		two.setGender(Gender.male());
		two.addNote("Note 1");
		two.addNote("Note 2");
		two.addNote("Note 2");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	private class HasAltIdImpl extends VCardProperty implements HasAltId {
		public HasAltIdImpl(String altId) {
			setAltId(altId);
		}

		//@Override
		public String getAltId() {
			return parameters.getAltId();
		}

		//@Override
		public void setAltId(String altId) {
			parameters.setAltId(altId);
		}
	}

	private class VCardPropertyImpl extends VCardProperty {
		public VCardVersion validateVersion;
		public VCard validateVCard;

		@Override
		public void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
			validateVersion = version;
			validateVCard = vcard;
			warnings.add(new ValidationWarning(0));
		}
	}
}
