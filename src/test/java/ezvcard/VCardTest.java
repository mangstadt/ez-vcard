package ezvcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import ezvcard.io.CompatibilityMode;
import ezvcard.types.HasAltId;
import ezvcard.types.NoteType;
import ezvcard.types.RawType;
import ezvcard.types.RevisionType;
import ezvcard.types.VCardType;

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
public class VCardTest {
	@Test
	public void getProperties() {
		VCard vcard = new VCard();

		//type stored in VCardType variable
		RevisionType rev = RevisionType.now();
		vcard.setRevision(rev);

		//type stored in a List
		NoteType note = new NoteType("A note.");
		vcard.addNote(note);

		//extended type with unique name
		RawType xGender = vcard.addExtendedProperty("X-GENDER", "male");

		//extended types with same name
		RawType xManager1 = vcard.addExtendedProperty("X-MANAGER", "Michael Scott");
		RawType xManager2 = vcard.addExtendedProperty("X-MANAGER", "Pointy Haired Boss");

		Collection<VCardType> types = vcard.getProperties().values();
		assertEquals(5, types.size());
		assertTrue(types.contains(rev));
		assertTrue(types.contains(note));
		assertTrue(types.contains(xGender));
		assertTrue(types.contains(xManager1));
		assertTrue(types.contains(xManager2));
	}

	@Test
	public void getProperties_none() {
		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V2_1); //no type class is returned for VERSION
		assertTrue(vcard.getProperties().isEmpty());
	}

	@Test
	public void addExtendedProperty() {
		VCard vcard = new VCard();
		RawType type = vcard.addExtendedProperty("NAME", "value");
		assertEquals("NAME", type.getTypeName());
		assertEquals("value", type.getValue());
		assertEquals(Arrays.asList(type), vcard.getExtendedProperties("NAME"));
	}

	@Test
	public void addProperty() {
		VCard vcard = new VCard();
		VCardTypeImpl type = new VCardTypeImpl("NAME");
		vcard.addProperty(type);
		assertEquals(Arrays.asList(type), vcard.getProperties(type.getClass()));
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

		List<HasAltIdImpl> props = vcard.getProperties(HasAltIdImpl.class);

		Collection<HasAltIdImpl> expected = Arrays.asList(one1, null1, two1, two2);
		assertEquals(expected, props);

		assertEquals("1", one1.altId);
		assertEquals(null, null1.altId);
		assertEquals("2", two1.altId);
		assertEquals("2", two2.altId);
	}

	@Test
	public void generateAltId() {
		Collection<HasAltId> list = new ArrayList<HasAltId>();
		list.add(new HasAltIdImpl("1"));
		list.add(new HasAltIdImpl("1"));
		list.add(new HasAltIdImpl("2"));
		assertEquals("3", VCard.generateAltId(list));

		list = new ArrayList<HasAltId>();
		list.add(new HasAltIdImpl("1"));
		list.add(new HasAltIdImpl("1"));
		list.add(new HasAltIdImpl("3"));
		assertEquals("2", VCard.generateAltId(list));

		list = new ArrayList<HasAltId>();
		list.add(new HasAltIdImpl("2"));
		list.add(new HasAltIdImpl("2"));
		list.add(new HasAltIdImpl("3"));
		assertEquals("1", VCard.generateAltId(list));

		list = new ArrayList<HasAltId>();
		assertEquals("1", VCard.generateAltId(list));

		list = new ArrayList<HasAltId>();
		list.add(new HasAltIdImpl("one"));
		list.add(new HasAltIdImpl("one"));
		list.add(new HasAltIdImpl("three"));
		assertEquals("1", VCard.generateAltId(list));
	}

	@Test
	public void getPropertiesAlt() {
		VCard vcard = new VCard();

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
		List<List<HasAltIdImpl>> expected = Arrays.asList(
			Arrays.asList(one1, one2),
			Arrays.asList(two1),
			Arrays.asList(null1),
			Arrays.asList(null2)
		);
		//@formatter:on
		assertEquals(expected, vcard.getPropertiesAlt(HasAltIdImpl.class));
	}

	@Test
	public void getPropertiesAlt_empty() {
		VCard vcard = new VCard();

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<HasAltIdImpl>> expected = Arrays.asList(
		);
		//@formatter:on
		assertEquals(expected, vcard.getPropertiesAlt(HasAltIdImpl.class));
	}

	private class HasAltIdImpl extends VCardType implements HasAltId {
		private String altId;

		public HasAltIdImpl(String altId) {
			super("NAME");
			this.altId = altId;
		}

		//@Overrides
		public String getAltId() {
			return altId;
		}

		//@Overrides
		public void setAltId(String altId) {
			this.altId = altId;
		}

		@Override
		protected void doMarshalText(StringBuilder value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			//empty
		}

		@Override
		protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			//empty
		}
	}

	private class VCardTypeImpl extends VCardType {
		public VCardTypeImpl(String typeName) {
			super(typeName);
		}

		@Override
		protected void doMarshalText(StringBuilder value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			//empty
		}

		@Override
		protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
			//empty
		}
	}
}
