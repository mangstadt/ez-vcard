package ezvcard.property;

import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
public class VCardPropertyTest {
	@Test
	public void validate() {
		ValidateType property = new ValidateType();
		assertValidate(property).versions(VCardVersion.V2_1).run(2);
		assertValidate(property).versions(VCardVersion.V3_0).run();
		assertValidate(property).versions(VCardVersion.V4_0).run(2);
		assertTrue(property.validateCalled);
	}

	@Test
	public void getSupportedVersions() {
		VCardTypeImpl withoutSupportedVersions = new VCardTypeImpl();
		assertSetEquals(withoutSupportedVersions.getSupportedVersions(), VCardVersion.values());

		ValidateType withSupportedVersions = new ValidateType();
		assertSetEquals(withSupportedVersions.getSupportedVersions(), VCardVersion.V3_0);
	}

	@Test
	public void group() {
		VCardTypeImpl property = new VCardTypeImpl();
		assertNull(property.getGroup());

		property.setGroup("group");
		assertEquals("group", property.getGroup());
	}

	@Test
	public void compareTo() {
		VCardTypeImpl one = new VCardTypeImpl();
		one.getParameters().setPref(1);

		VCardTypeImpl two = new VCardTypeImpl();
		one.getParameters().setPref(2);

		VCardTypeImpl null1 = new VCardTypeImpl();
		VCardTypeImpl null2 = new VCardTypeImpl();

		assertEquals(-1, one.compareTo(two));
		assertEquals(1, two.compareTo(one));
		assertEquals(0, one.compareTo(one));
		assertEquals(-1, one.compareTo(null1));
		assertEquals(1, null1.compareTo(one));
		assertEquals(0, null1.compareTo(null2));
	}

	private class VCardTypeImpl extends VCardProperty {
		//empty
	}

	private class ValidateType extends VCardProperty {
		public boolean validateCalled = false;

		@Override
		public Set<VCardVersion> _supportedVersions() {
			return EnumSet.of(VCardVersion.V3_0);
		}

		@Override
		public void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
			validateCalled = true;
		}
	}
}
