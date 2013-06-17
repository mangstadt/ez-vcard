package ezvcard.types;

import static ezvcard.util.TestUtils.assertIntEquals;
import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.EmailTypeParameter;

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
public class EmailTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final VCard vcard = new VCard();

	@After
	public void after() {
		warnings.clear();
		subTypes.clear();
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_2_1() {
		VCardVersion version = VCardVersion.V2_1;
		EmailType t = new EmailType();
		t.addType(EmailTypeParameter.PREF);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_3_0() {
		VCardVersion version = VCardVersion.V3_0;
		EmailType t = new EmailType();
		t.addType(EmailTypeParameter.PREF);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, vcard);

		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	/**
	 * If a property contains a "TYPE=pref" parameter and it's being marshalled
	 * to 4.0, it should replace "TYPE=pref" with "PREF=1".
	 */
	@Test
	public void marshalSubTypes_type_pref_4_0() {
		VCardVersion version = VCardVersion.V4_0;
		EmailType t = new EmailType();
		t.addType(EmailTypeParameter.PREF);
		VCardSubTypes subTypes = t.marshalSubTypes(version, warnings, compatibilityMode, new VCard());

		assertEquals(1, subTypes.size());
		assertIntEquals(1, subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void marshalSubTypes_pref_parameter_2_1() {
		VCardVersion version = VCardVersion.V2_1;

		VCard vcard = new VCard();
		EmailType t1 = new EmailType();
		t1.setPref(1);
		vcard.addEmail(t1);
		EmailType t2 = new EmailType();
		t2.setPref(2);
		vcard.addEmail(t2);

		VCardSubTypes subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));

		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertNull(subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void marshalSubTypes_pref_parameter_3_0() {
		VCardVersion version = VCardVersion.V3_0;

		VCard vcard = new VCard();
		EmailType t1 = new EmailType();
		t1.setPref(1);
		vcard.addEmail(t1);
		EmailType t2 = new EmailType();
		t2.setPref(2);
		vcard.addEmail(t2);

		VCardSubTypes subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertNull(subTypes.getPref());
		assertTrue(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);

		warnings.clear();

		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(0, subTypes.size());
		assertNull(subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}

	/**
	 * If properties contain "PREF" parameters and they're being marshalled to
	 * 2.1/3.0, then it should find the type with the lowest PREF value and add
	 * "TYPE=pref" to it.
	 */
	@Test
	public void marshalSubTypes_pref_parameter_4_0() {
		VCardVersion version = VCardVersion.V4_0;

		VCard vcard = new VCard();
		EmailType t1 = new EmailType();
		t1.setPref(1);
		vcard.addEmail(t1);
		EmailType t2 = new EmailType();
		t2.setPref(2);
		vcard.addEmail(t2);

		version = VCardVersion.V4_0;
		VCardSubTypes subTypes = t1.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertIntEquals(1, subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);

		warnings.clear();

		subTypes = t2.marshalSubTypes(version, warnings, compatibilityMode, vcard);
		assertEquals(1, subTypes.size());
		assertIntEquals(2, subTypes.getPref());
		assertFalse(subTypes.getTypes().contains(EmailTypeParameter.PREF.getValue()));
		assertWarnings(0, warnings);
	}
}
