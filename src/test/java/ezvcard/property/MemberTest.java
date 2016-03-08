package ezvcard.property;

import static ezvcard.property.PropertySensei.assertValidate;

import org.junit.Test;

import ezvcard.VCard;
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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class MemberTest {
	@Test
	public void validate() {
		Member empty = new Member((String) null);
		assertValidate(empty).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2, 8, 17);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8, 17);

		Member withUri = new Member("uri");

		VCard groupKind = new VCard();
		groupKind.setKind(Kind.group());
		assertValidate(withUri).vcard(groupKind).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2);
		assertValidate(withUri).vcard(groupKind).versions(VCardVersion.V4_0).run();

		VCard nonGroupKind = new VCard();
		nonGroupKind.setKind(Kind.application());
		assertValidate(withUri).vcard(nonGroupKind).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2, 17);
		assertValidate(withUri).vcard(nonGroupKind).versions(VCardVersion.V4_0).run(17);

		VCard withoutKind = new VCard();
		assertValidate(withUri).vcard(withoutKind).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(2, 17);
		assertValidate(withUri).vcard(withoutKind).versions(VCardVersion.V4_0).run(17);
	}
}
