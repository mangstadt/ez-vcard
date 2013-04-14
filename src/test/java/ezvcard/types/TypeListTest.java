package ezvcard.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCard;

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
public class TypeListTest {
	@Test
	public void getTypeClass() {
		assertEquals(AddressType.class, TypeList.getTypeClass("ADR"));
	}

	@Test
	public void getTypeClass_case_insensitive() {
		assertEquals(AddressType.class, TypeList.getTypeClass("aDr"));
	}

	@Test
	public void getTypeClass_not_found() {
		assertNull(TypeList.getTypeClass("non-existant"));
	}

	@Test
	public void getTypeClassByHCardTypeName() {
		assertEquals(AddressType.class, TypeList.getTypeClassByHCardTypeName("adr"));
	}

	@Test
	public void getTypeClassByHCardTypeName_case_insensitive() {
		assertEquals(AddressType.class, TypeList.getTypeClassByHCardTypeName("AdR"));
	}

	@Test
	public void getTypeClassByHCardTypeName_not_found() {
		assertNull(TypeList.getTypeClassByHCardTypeName("non-existant"));
	}

	/**
	 * hCard uses a different name for the CATEGORIES property.
	 */
	@Test
	public void getTypeClassByHCardTypeName_categories() {
		assertEquals(CategoriesType.class, TypeList.getTypeClassByHCardTypeName("categories"));
		assertEquals(CategoriesType.class, TypeList.getTypeClassByHCardTypeName("category"));
	}

	@Test
	public void getAddMethod() throws Exception {
		assertEquals(VCard.class.getMethod("addAddress", AddressType.class), TypeList.getAddMethod(AddressType.class));
		assertEquals(VCard.class.getMethod("addOrphanedLabel", LabelType.class), TypeList.getAddMethod(LabelType.class));
		assertEquals(VCard.class.getMethod("addTelephoneNumber", TelephoneType.class), TypeList.getAddMethod(TelephoneType.class));
	}
}
