package ezvcard.types;

import static ezvcard.util.TestUtils.assertWarnings;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;

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
public class MemberTypeTest {
	@Test
	public void validate() {
		VCard vcard = new VCard();

		MemberType property = new MemberType(null);
		assertWarnings(3, property.validate(VCardVersion.V2_1, vcard));
		assertWarnings(3, property.validate(VCardVersion.V3_0, vcard));
		assertWarnings(2, property.validate(VCardVersion.V4_0, vcard));

		property.setUri("uri");
		VCard group = new VCard();
		group.setKind(KindType.group());
		group.addMember(property);
		assertWarnings(1, property.validate(VCardVersion.V2_1, group));
		assertWarnings(1, property.validate(VCardVersion.V3_0, group));
		assertWarnings(0, property.validate(VCardVersion.V4_0, group));

		VCard notGroup = new VCard();
		notGroup.setKind(KindType.application());
		group.addMember(property);
		assertWarnings(2, property.validate(VCardVersion.V2_1, notGroup));
		assertWarnings(2, property.validate(VCardVersion.V3_0, notGroup));
		assertWarnings(1, property.validate(VCardVersion.V4_0, notGroup));

		VCard noKind = new VCard();
		group.addMember(property);
		assertWarnings(2, property.validate(VCardVersion.V2_1, noKind));
		assertWarnings(2, property.validate(VCardVersion.V3_0, noKind));
		assertWarnings(1, property.validate(VCardVersion.V4_0, noKind));
	}
}
