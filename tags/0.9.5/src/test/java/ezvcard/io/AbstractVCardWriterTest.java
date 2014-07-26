package ezvcard.io;

import static ezvcard.util.TestUtils.assertSetEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import ezvcard.property.Address;
import ezvcard.property.Gender;
import ezvcard.property.Label;
import ezvcard.property.Mailer;
import ezvcard.property.ProductId;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.util.ListMultimap;

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
public class AbstractVCardWriterTest {
	private VCard vcard;
	private AbstractVCardWriter writer;

	@Before
	public void before() {
		vcard = new VCard();
		writer = new AbstractVCardWriter() {
			//empty
		};
	}

	@Test(expected = IllegalArgumentException.class)
	public void property_without_scribe() {
		vcard.addProperty(new VCardProperty() {
			//empty
		});
		writer.prepare(vcard, VCardVersion.V2_1);
	}

	@Test
	public void productId() {
		assertNull(vcard.getProductId());

		//default value
		{
			vcard.setProductId((String) null);
			{
				Prepared properties = new Prepared(VCardVersion.V2_1);
				assertEquals(1, properties.count());
				assertEquals(1, properties.count(RawProperty.class));

				for (VCardVersion version : each(VCardVersion.V3_0, VCardVersion.V4_0)) {
					properties = new Prepared(version);
					assertEquals(1, properties.count());
					assertEquals(1, properties.count(ProductId.class));
				}
			}

			vcard.setProductId("value"); //should be ignored
			{
				Prepared properties = new Prepared(VCardVersion.V2_1);
				assertEquals(1, properties.count());
				assertEquals(1, properties.count(RawProperty.class));

				for (VCardVersion version : each(VCardVersion.V3_0, VCardVersion.V4_0)) {
					properties = new Prepared(version);
					assertEquals(1, properties.count());
					ProductId prodId = properties.first(ProductId.class);
					assertNotEquals("value", prodId.getValue());
				}
			}
		}

		writer.setAddProdId(false);
		{
			vcard.setProductId((String) null);
			{
				for (VCardVersion version : VCardVersion.values()) {
					Prepared properties = new Prepared(version);
					assertEquals(0, properties.count());
				}
			}

			vcard.setProductId("value");
			{
				Prepared properties = new Prepared(VCardVersion.V2_1);
				assertEquals(0, properties.count());

				for (VCardVersion version : each(VCardVersion.V3_0, VCardVersion.V4_0)) {
					properties = new Prepared(version);
					assertEquals(1, properties.count());
					ProductId prodId = properties.first(ProductId.class);
					assertEquals("value", prodId.getValue());
				}
			}
		}

		writer.setAddProdId(true);
		{
			vcard.setProductId((String) null);
			{
				Prepared properties = new Prepared(VCardVersion.V2_1);
				assertEquals(1, properties.count());
				assertEquals(1, properties.count(RawProperty.class));

				for (VCardVersion version : each(VCardVersion.V3_0, VCardVersion.V4_0)) {
					properties = new Prepared(version);
					assertEquals(1, properties.count());
					assertEquals(1, properties.count(ProductId.class));
				}
			}

			vcard.setProductId("value"); //should be ignored
			{
				Prepared properties = new Prepared(VCardVersion.V2_1);
				assertEquals(1, properties.count());
				assertEquals(1, properties.count(RawProperty.class));

				for (VCardVersion version : each(VCardVersion.V3_0, VCardVersion.V4_0)) {
					properties = new Prepared(version);
					assertEquals(1, properties.count());
					ProductId prodId = properties.first(ProductId.class);
					assertNotEquals("value", prodId.getValue());
				}
			}
		}
	}

	@Test
	public void versionStrict() {
		writer.setAddProdId(false);
		vcard.setGender(Gender.male());
		vcard.setMailer("value");

		//default value
		{
			for (VCardVersion version : each(VCardVersion.V2_1, VCardVersion.V3_0)) {
				Prepared properties = new Prepared(version);
				assertEquals(1, properties.count());
				assertEquals(1, properties.count(Mailer.class));
			}

			Prepared properties = new Prepared(VCardVersion.V4_0);
			assertEquals(1, properties.count());
			assertEquals(1, properties.count(Gender.class));
		}

		writer.setVersionStrict(false);
		{
			for (VCardVersion version : VCardVersion.values()) {
				Prepared properties = new Prepared(version);
				assertEquals(2, properties.count());
				assertEquals(1, properties.count(Mailer.class));
				assertEquals(1, properties.count(Gender.class));
			}
		}

		writer.setVersionStrict(true);
		{
			for (VCardVersion version : each(VCardVersion.V2_1, VCardVersion.V3_0)) {
				Prepared properties = new Prepared(version);
				assertEquals(1, properties.count());
				assertEquals(1, properties.count(Mailer.class));
			}

			Prepared properties = new Prepared(VCardVersion.V4_0);
			assertEquals(1, properties.count());
			assertEquals(1, properties.count(Gender.class));
		}
	}

	@Test
	public void labels() throws Throwable {
		writer.setAddProdId(false);

		//address with label and type
		Address adr = new Address();
		adr.setLabel("value1");
		adr.addType(AddressType.HOME);
		vcard.addAddress(adr);

		//address with label
		adr = new Address();
		adr.setLabel("value2");
		vcard.addAddress(adr);

		//address
		adr = new Address();
		vcard.addAddress(adr);

		for (VCardVersion version : each(VCardVersion.V2_1, VCardVersion.V3_0)) {
			Prepared properties = new Prepared(version);
			assertEquals(5, properties.count());
			assertEquals(3, properties.count(Address.class));

			Label label = properties.get(Label.class).get(0);
			assertEquals("value1", label.getValue());
			assertSetEquals(label.getTypes(), AddressType.HOME);

			label = properties.get(Label.class).get(1);
			assertEquals("value2", label.getValue());
			assertSetEquals(label.getTypes());
		}

		Prepared properties = new Prepared(VCardVersion.V4_0);
		assertEquals(3, properties.count());
		assertEquals(3, properties.count(Address.class));
	}

	private <T> T[] each(T... t) {
		return t;
	}

	private class Prepared {
		private final ListMultimap<Class<? extends VCardProperty>, VCardProperty> properties = new ListMultimap<Class<? extends VCardProperty>, VCardProperty>();

		public Prepared(VCardVersion version) {
			List<VCardProperty> properties = writer.prepare(vcard, version);
			for (VCardProperty property : properties) {
				this.properties.put(property.getClass(), property);
			}
		}

		public int count() {
			return properties.size();
		}

		public int count(Class<? extends VCardProperty> clazz) {
			return properties.get(clazz).size();
		}

		public <T extends VCardProperty> List<T> get(Class<T> clazz) {
			List<VCardProperty> props = properties.get(clazz);
			List<T> casted = new ArrayList<T>(props.size());
			for (VCardProperty property : props) {
				casted.add(clazz.cast(property));
			}
			return casted;
		}

		public <T extends VCardProperty> T first(Class<T> clazz) {
			List<VCardProperty> props = properties.get(clazz);
			return props.isEmpty() ? null : clazz.cast(props.get(0));
		}
	}
}
