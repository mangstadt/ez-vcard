package ezvcard.io;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.util.TestUtils.each;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Gender;
import ezvcard.property.Label;
import ezvcard.property.Mailer;
import ezvcard.property.ProductId;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.util.ListMultimap;

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
public class StreamWriterTest {
	private StreamWriterStub writer;
	private VCard vcard;

	@Before
	public void before() {
		writer = new StreamWriterStub();
		vcard = new VCard();
	}

	@Test
	public void unregistered_property() throws Exception {
		vcard.addProperty(new TestProperty());

		try {
			writer.write(vcard);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//expected
		}

		assertNull(writer.properties); //vCard was not written
	}

	@Test
	public void productId() throws IOException {
		assertNull(vcard.getProductId());

		//default value
		{
			vcard.setProductId((String) null);
			{
				writer.write(vcard, V2_1);
				assertEquals(1, writer.count());
				assertEquals(1, writer.count(RawProperty.class));

				for (VCardVersion version : each(V3_0, V4_0)) {
					writer.write(vcard, version);
					assertEquals(1, writer.count());
					assertEquals(1, writer.count(ProductId.class));
				}
			}

			vcard.setProductId("value"); //should be ignored
			{
				writer.write(vcard, V2_1);
				assertEquals(1, writer.count());
				assertEquals(1, writer.count(RawProperty.class));

				for (VCardVersion version : each(V3_0, V4_0)) {
					writer.write(vcard, version);
					assertEquals(1, writer.count());
					ProductId prodid = writer.first(ProductId.class);
					assertNotEquals("value", prodid.getValue());
				}
			}
		}

		writer.setAddProdId(false);
		{
			vcard.setProductId((String) null);
			{
				for (VCardVersion version : VCardVersion.values()) {
					writer.write(vcard, version);
					assertEquals(0, writer.count());
				}
			}

			vcard.setProductId("value");
			{
				writer.write(vcard, V2_1);
				assertEquals(0, writer.count());

				for (VCardVersion version : each(V3_0, V4_0)) {
					writer.write(vcard, version);
					ProductId prodId = writer.first(ProductId.class);
					assertEquals("value", prodId.getValue());
				}
			}
		}

		writer.setAddProdId(true);
		{
			vcard.setProductId((String) null);
			{
				writer.write(vcard, V2_1);
				assertEquals(1, writer.count());
				assertEquals(1, writer.count(RawProperty.class));

				for (VCardVersion version : each(V3_0, V4_0)) {
					writer.write(vcard, version);
					assertEquals(1, writer.count());
					assertEquals(1, writer.count(ProductId.class));
				}
			}

			vcard.setProductId("value"); //should be ignored
			{
				writer.write(vcard, V2_1);
				assertEquals(1, writer.count());
				assertEquals(1, writer.count(RawProperty.class));

				for (VCardVersion version : each(V3_0, V4_0)) {
					writer.write(vcard, version);
					assertEquals(1, writer.count());
					ProductId prodid = writer.first(ProductId.class);
					assertNotEquals("value", prodid.getValue());
				}
			}
		}
	}

	/**
	 * Asserts that the PRODID property is always put at the front of the vCard.
	 */
	@Test
	public void productId_order() throws IOException {
		vcard.setFormattedName("Name");

		{
			writer.write(vcard, V2_1);

			Iterator<VCardProperty> it = writer.propertiesList.iterator();

			VCardProperty property = it.next();
			assertTrue(property instanceof RawProperty);
			property = it.next();
			assertTrue(property instanceof FormattedName);

			assertFalse(it.hasNext());
		}

		for (VCardVersion version : each(V3_0, V4_0)) {
			writer.write(vcard, version);

			Iterator<VCardProperty> it = writer.propertiesList.iterator();

			VCardProperty property = it.next();
			assertTrue(property instanceof ProductId);
			property = it.next();
			assertTrue(property instanceof FormattedName);

			assertFalse(it.hasNext());
		}

		vcard.setProductId("value");
		writer.setAddProdId(false);
		for (VCardVersion version : each(V3_0, V4_0)) {
			writer.write(vcard, version);

			Iterator<VCardProperty> it = writer.propertiesList.iterator();

			VCardProperty property = it.next();
			assertTrue(property instanceof ProductId);
			property = it.next();
			assertTrue(property instanceof FormattedName);

			assertFalse(it.hasNext());
		}
	}

	@Test
	public void versionStrict() throws Exception {
		writer.setAddProdId(false);
		vcard.setGender(Gender.male());
		vcard.setMailer("value");

		//default value
		{
			for (VCardVersion version : each(V2_1, V3_0)) {
				writer.write(vcard, version);
				assertEquals(1, writer.count());
				assertEquals(1, writer.count(Mailer.class));
			}

			writer.write(vcard, V4_0);
			assertEquals(1, writer.count());
			assertEquals(1, writer.count(Gender.class));
		}

		writer.setVersionStrict(false);
		{
			for (VCardVersion version : VCardVersion.values()) {
				writer.write(vcard, version);
				assertEquals(2, writer.count());
				assertEquals(1, writer.count(Mailer.class));
				assertEquals(1, writer.count(Gender.class));
			}
		}

		writer.setVersionStrict(true);
		{
			for (VCardVersion version : each(V2_1, V3_0)) {
				writer.write(vcard, version);
				assertEquals(1, writer.count());
				assertEquals(1, writer.count(Mailer.class));
			}

			writer.write(vcard, V4_0);
			assertEquals(1, writer.count());
			assertEquals(1, writer.count(Gender.class));
		}
	}

	@Test
	public void labels() throws Throwable {
		writer.setAddProdId(false);

		//address with label and type
		Address adr = new Address();
		adr.setLabel("value1");
		adr.getTypes().add(AddressType.HOME);
		vcard.addAddress(adr);

		//address with label
		adr = new Address();
		adr.setLabel("value2");
		vcard.addAddress(adr);

		//address
		adr = new Address();
		vcard.addAddress(adr);

		for (VCardVersion version : each(V2_1, V3_0)) {
			writer.write(vcard, version);
			assertEquals(5, writer.count());
			assertEquals(3, writer.count(Address.class));
			assertEquals(2, writer.count(Label.class));

			Label label = writer.get(Label.class).get(0);
			assertEquals("value1", label.getValue());
			assertEquals(Arrays.asList(AddressType.HOME), label.getTypes());

			label = writer.get(Label.class).get(1);
			assertEquals("value2", label.getValue());
			assertEquals(Arrays.asList(), label.getTypes());
		}

		writer.write(vcard, V4_0);
		assertEquals(3, writer.count());
		assertEquals(3, writer.count(Address.class));
	}

	private class StreamWriterStub extends StreamWriter {
		private VCardVersion targetVersion;
		private List<VCardProperty> propertiesList;
		private ListMultimap<Class<? extends VCardProperty>, VCardProperty> properties = null;

		public StreamWriterStub() {
			this(V4_0);
		}

		public StreamWriterStub(VCardVersion targetVersion) {
			this.targetVersion = targetVersion;
		}

		@Override
		protected VCardVersion getTargetVersion() {
			return targetVersion;
		}

		public void write(VCard vcard, VCardVersion targetVersion) throws IOException {
			this.targetVersion = targetVersion;
			super.write(vcard);
		}

		@Override
		protected void _write(VCard vcard, List<VCardProperty> properties) throws IOException {
			propertiesList = properties;
			this.properties = new ListMultimap<Class<? extends VCardProperty>, VCardProperty>();
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

		public void close() throws IOException {
			//empty
		}
	}

	private class TestProperty extends VCardProperty {
		//empty
	}
}
