package ezvcard;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

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
public class VCardDataTypeTest {
	@Test
	public void get() {
		assertSame(VCardDataType.TEXT, VCardDataType.get("tExT"));

		VCardDataType test = VCardDataType.get("test");
		VCardDataType test2 = VCardDataType.get("tEsT");
		assertSame(test, test2);
	}

	@Test
	public void find() {
		assertSame(VCardDataType.TEXT, VCardDataType.find("tExT"));

		//find() ignores runtime-defined objects
		VCardDataType.get("test");
		assertNull(VCardDataType.find("test"));
	}

	@Test
	public void all() {
		VCardDataType.get("test"); //all() ignores runtime-defined objects
		Collection<VCardDataType> all = VCardDataType.all();

		assertEquals(15, all.size());
		assertTrue(all.contains(VCardDataType.BINARY));
		assertTrue(all.contains(VCardDataType.BOOLEAN));
		assertTrue(all.contains(VCardDataType.CONTENT_ID));
		assertTrue(all.contains(VCardDataType.DATE));
		assertTrue(all.contains(VCardDataType.DATE_TIME));
		assertTrue(all.contains(VCardDataType.DATE_AND_OR_TIME));
		assertTrue(all.contains(VCardDataType.FLOAT));
		assertTrue(all.contains(VCardDataType.INTEGER));
		assertTrue(all.contains(VCardDataType.LANGUAGE_TAG));
		assertTrue(all.contains(VCardDataType.TEXT));
		assertTrue(all.contains(VCardDataType.TIME));
		assertTrue(all.contains(VCardDataType.TIMESTAMP));
		assertTrue(all.contains(VCardDataType.URI));
		assertTrue(all.contains(VCardDataType.URL));
		assertTrue(all.contains(VCardDataType.UTC_OFFSET));
	}

	@Test
	public void getSupportedVersions() {
		assertArrayEquals(new VCardVersion[] { VCardVersion.V2_1 }, VCardDataType.CONTENT_ID.getSupportedVersions());
		assertArrayEquals(VCardVersion.values(), VCardDataType.TEXT.getSupportedVersions());
		assertArrayEquals(VCardVersion.values(), VCardDataType.get("test").getSupportedVersions());
	}

	@Test
	public void isSupportedBy() {
		assertTrue(VCardDataType.CONTENT_ID.isSupportedBy(VCardVersion.V2_1));
		assertFalse(VCardDataType.CONTENT_ID.isSupportedBy(VCardVersion.V3_0));
		assertFalse(VCardDataType.CONTENT_ID.isSupportedBy(VCardVersion.V4_0));

		for (VCardVersion version : VCardVersion.values()) {
			assertTrue(VCardDataType.TEXT.isSupportedBy(version));
		}

		VCardDataType test = VCardDataType.get("test");
		for (VCardVersion version : VCardVersion.values()) {
			assertTrue(test.isSupportedBy(version));
		}
	}
}
