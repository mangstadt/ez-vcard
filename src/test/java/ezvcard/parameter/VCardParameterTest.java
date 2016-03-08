package ezvcard.parameter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

import ezvcard.SupportedVersions;
import ezvcard.VCardVersion;

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
 */

/**
 * @author Michael Angstadt
 */
public class VCardParameterTest {
	@Test
	public void getSupportedVersions() {
		assertArrayEquals(new VCardVersion[] { VCardVersion.V2_1 }, VCardParameterImpl.ONE.getSupportedVersions());
		assertArrayEquals(VCardVersion.values(), VCardParameterImpl.TWO.getSupportedVersions());
		assertArrayEquals(VCardVersion.values(), new VCardParameterImpl("three").getSupportedVersions());
	}

	@Test
	public void isSupportedBy() {
		assertTrue(VCardParameterImpl.ONE.isSupportedBy(VCardVersion.V2_1));
		assertFalse(VCardParameterImpl.ONE.isSupportedBy(VCardVersion.V3_0));
		assertFalse(VCardParameterImpl.ONE.isSupportedBy(VCardVersion.V4_0));

		for (VCardVersion version : VCardVersion.values()) {
			assertTrue(VCardParameterImpl.TWO.isSupportedBy(version));
		}

		VCardParameterImpl three = new VCardParameterImpl("three");
		for (VCardVersion version : VCardVersion.values()) {
			assertTrue(three.isSupportedBy(version));
		}
	}

	@Test
	public void value_to_lowercase() {
		VCardParameterImpl three = new VCardParameterImpl("THREE");
		assertEquals("three", three.getValue());
	}

	@Test
	public void value_null() {
		VCardParameterImpl parameter = new VCardParameterImpl(null);
		assertNull(parameter.getValue());
	}

	@Test
	public void equals_contract() {
		EqualsVerifier.forClass(VCardParameter.class).usingGetClass().verify();
	}

	private static class VCardParameterImpl extends VCardParameter {
		@SupportedVersions(VCardVersion.V2_1)
		public static VCardParameterImpl ONE = new VCardParameterImpl("one");

		@SuppressWarnings("unused")
		public String instanceField = "value";

		public static VCardParameterImpl TWO = new VCardParameterImpl("two");

		public VCardParameterImpl() {
			super("value");
		}

		public VCardParameterImpl(String value) {
			super(value);
		}
	}
}
