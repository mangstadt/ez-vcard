package ezvcard.property;

import static ezvcard.util.TestUtils.assertSetEquals;
import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;

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
	@SuppressWarnings("unchecked")
	@Test
	public void validate_overrideable_method_called() {
		VCardPropertyImpl property = spy(new VCardPropertyImpl());
		assertValidate(property).versions(VCardVersion.V2_1).run();
		verify(property)._validate(anyList(), eq(VCardVersion.V2_1), any(VCard.class));
	}

	@Test
	public void validate_unsupported_version() {
		Version3Property property = new Version3Property();
		assertValidate(property).versions(VCardVersion.V2_1).run(2);
		assertValidate(property).versions(VCardVersion.V3_0).run();
		assertValidate(property).versions(VCardVersion.V4_0).run(2);
	}

	@Test
	public void validate_valid_group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setGroup("Group-1");
		assertValidate(property).run();
	}

	@Test
	public void validate_invalid_group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setGroup("mr.smith");
		assertValidate(property).run(23);
	}

	@Test
	public void validate_parameters() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		property.setParameter("ALTID", "1");
		assertValidate(property).versions(VCardVersion.V2_1).run(6);
		assertValidate(property).versions(VCardVersion.V3_0).run(6);
		assertValidate(property).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void getSupportedVersions() {
		VCardPropertyImpl withoutSupportedVersions = new VCardPropertyImpl();
		assertSetEquals(withoutSupportedVersions.getSupportedVersions(), VCardVersion.values());

		Version3Property withSupportedVersions = new Version3Property();
		assertSetEquals(withSupportedVersions.getSupportedVersions(), VCardVersion.V3_0);
	}

	@Test
	public void group() {
		VCardPropertyImpl property = new VCardPropertyImpl();
		assertNull(property.getGroup());

		property.setGroup("group");
		assertEquals("group", property.getGroup());
	}

	@Test
	public void compareTo() {
		VCardPropertyImpl one = new VCardPropertyImpl();
		one.getParameters().setPref(1);

		VCardPropertyImpl two = new VCardPropertyImpl();
		one.getParameters().setPref(2);

		VCardPropertyImpl null1 = new VCardPropertyImpl();
		VCardPropertyImpl null2 = new VCardPropertyImpl();

		assertEquals(-1, one.compareTo(two));
		assertEquals(1, two.compareTo(one));
		assertEquals(0, one.compareTo(one));
		assertEquals(-1, one.compareTo(null1));
		assertEquals(1, null1.compareTo(one));
		assertEquals(0, null1.compareTo(null2));
	}

	private class VCardPropertyImpl extends VCardProperty {
		//empty
	}

	private class Version3Property extends VCardProperty {
		@Override
		public Set<VCardVersion> _supportedVersions() {
			return EnumSet.of(VCardVersion.V3_0);
		}
	}
}
