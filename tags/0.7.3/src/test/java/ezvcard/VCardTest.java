package ezvcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import ezvcard.types.NoteType;
import ezvcard.types.RawType;
import ezvcard.types.RevisionType;
import ezvcard.types.VCardType;

/*
 Copyright (c) 2012, Michael Angstadt
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
	public void getAllTypes() {
		VCard vcard = new VCard();

		//type stored in VCardType variable
		RevisionType rev = RevisionType.now();
		vcard.setRevision(rev);

		//type stored in a List
		NoteType note = new NoteType("A note.");
		vcard.addNote(note);

		//extended type with unique name
		RawType xGender = vcard.addExtendedType("X-GENDER", "male");

		//extended types with same name
		RawType xManager1 = vcard.addExtendedType("X-MANAGER", "Michael Scott");
		RawType xManager2 = vcard.addExtendedType("X-MANAGER", "Pointy Haired Boss");

		Collection<VCardType> types = vcard.getAllTypes();
		assertEquals(5, types.size());
		assertTrue(types.contains(rev));
		assertTrue(types.contains(note));
		assertTrue(types.contains(xGender));
		assertTrue(types.contains(xManager1));
		assertTrue(types.contains(xManager2));
	}

	@Test
	public void getAllTypes_none() {
		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V2_1); //no type class is returned for VERSION
		assertTrue(vcard.getAllTypes().isEmpty());
	}
}
