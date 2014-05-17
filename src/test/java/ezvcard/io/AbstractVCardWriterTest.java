package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.ProductId;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;

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
	private AbstractVCardWriterImpl writer;

	@Before
	public void before() {
		vcard = new VCard();
		writer = new AbstractVCardWriterImpl();
	}

	@Test(expected = IllegalArgumentException.class)
	public void property_has_no_scribe() {
		vcard.addProperty(new TestProperty());
		writer.prepare(vcard, VCardVersion.V2_1);
	}

	@Test
	public void setAddProdId() {
		assertNull(vcard.getProductId());

		//default value of writer.prodId
		{
			vcard.setProductId((String) null);
			assertProdId(VCardVersion.V2_1, RawProperty.class);
			assertProdId(VCardVersion.V3_0, ProductId.class);
			assertProdId(VCardVersion.V4_0, ProductId.class);

			vcard.setProductId("value"); //should be ignored
			assertProdId(VCardVersion.V2_1, RawProperty.class);
			assertProdId(VCardVersion.V3_0, "!value");
			assertProdId(VCardVersion.V4_0, "!value");
		}

		writer.setAddProdId(false);
		{
			vcard.setProductId((String) null);
			assertProdId(VCardVersion.V2_1);
			assertProdId(VCardVersion.V3_0);
			assertProdId(VCardVersion.V4_0);

			vcard.setProductId("value");
			assertProdId(VCardVersion.V2_1);
			assertProdId(VCardVersion.V3_0, "value");
			assertProdId(VCardVersion.V4_0, "value");
		}

		writer.setAddProdId(true);
		{
			vcard.setProductId((String) null);
			assertProdId(VCardVersion.V2_1, RawProperty.class);
			assertProdId(VCardVersion.V3_0, ProductId.class);
			assertProdId(VCardVersion.V4_0, ProductId.class);

			vcard.setProductId("value"); //should be ignored
			assertProdId(VCardVersion.V2_1, RawProperty.class);
			assertProdId(VCardVersion.V3_0, "!value");
			assertProdId(VCardVersion.V4_0, "!value");
		}
	}

	private void assertProdId(VCardVersion version, Class<? extends VCardProperty> expectedProperty) {
		List<VCardProperty> properties = writer.prepare(vcard, version);

		int expectedSize = (expectedProperty == null) ? 0 : 1;
		assertEquals(expectedSize, properties.size());

		if (expectedProperty == null) {
			return;
		}

		assertEquals(expectedProperty, properties.get(0).getClass());
	}

	private void assertProdId(VCardVersion version, String expectedValue) {
		List<VCardProperty> properties = writer.prepare(vcard, version);

		int expectedSize = (expectedValue == null) ? 0 : 1;
		assertEquals(expectedSize, properties.size());

		if (expectedValue == null) {
			return;
		}

		ProductId prodId = (ProductId) properties.get(0);
		String actualValue = prodId.getValue();
		if (expectedValue.startsWith("!")) {
			expectedValue = expectedValue.substring(1);
			assertNotEquals(expectedValue, actualValue);
		} else {
			assertEquals(expectedValue, actualValue);
		}
	}

	private void assertProdId(VCardVersion version) {
		assertProdId(version, (String) null);
	}

	private class TestProperty extends VCardProperty {
		//empty
	}

	private class AbstractVCardWriterImpl extends AbstractVCardWriter {
		//empty
	}
}
