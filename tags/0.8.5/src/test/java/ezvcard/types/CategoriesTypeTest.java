package ezvcard.types;

import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;

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
public class CategoriesTypeTest {
	private final List<String> warnings = new ArrayList<String>();
	private final VCardSubTypes subTypes = new VCardSubTypes();
	private final VCard vcard = new VCard();

	private final CategoriesType withValues = new CategoriesType();
	{
		withValues.addValue("One");
		withValues.addValue("T,wo");
		withValues.addValue("Thr;ee");
	}
	private CategoriesType t;

	@Before
	public void before() {
		t = new CategoriesType();
		warnings.clear();
		subTypes.clear();
	}

	@Test
	public void validate() {
		assertWarnings(1, t.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, t.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, t.validate(VCardVersion.V4_0, vcard));

		assertWarnings(0, withValues.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, withValues.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withValues.validate(VCardVersion.V4_0, vcard));
	}

	@Test
	public void marshalText_kde() {
		//comma delimiters are escaped for KDE
		VCardVersion version = VCardVersion.V2_1;
		CompatibilityMode compatibilityMode = CompatibilityMode.KDE_ADDRESS_BOOK;
		String expected = "One\\,T\\,wo\\,Thr\\;ee";
		String actual = withValues.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void marshalText_rfc() {
		VCardVersion version = VCardVersion.V2_1;
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		String expected = "One,T\\,wo,Thr\\;ee";
		String actual = withValues.marshalText(version, compatibilityMode);

		assertEquals(expected, actual);
	}

	@Test
	public void doUnmarshalText_kde() {
		//comma delimiters are escaped for KDE
		VCardVersion version = VCardVersion.V2_1;
		CompatibilityMode compatibilityMode = CompatibilityMode.KDE_ADDRESS_BOOK;
		t.unmarshalText(subTypes, "One\\,T\\,wo\\,Thr\\;ee", version, warnings, compatibilityMode);
		List<String> expected = Arrays.asList("One", "T", "wo", "Thr;ee");
		List<String> actual = t.getValues();

		assertEquals(expected, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void doUnmarshalText_rfc() {
		VCardVersion version = VCardVersion.V2_1;
		CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
		t.unmarshalText(subTypes, "One\\,T\\,wo\\,Thr\\;ee", version, warnings, compatibilityMode);
		List<String> expected = Arrays.asList("One,T,wo,Thr;ee");
		List<String> actual = t.getValues();

		assertEquals(expected, actual);
		assertWarnings(0, warnings);
	}
}