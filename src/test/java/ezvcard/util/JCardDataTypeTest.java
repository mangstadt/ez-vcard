package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.After;
import org.junit.Test;

import ezvcard.util.JCardDataType;

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
 * @author Michael Angsadt
 */
public class JCardDataTypeTest {
	@After
	public void after() {
		JCardDataType.custom.clear();
	}

	@Test
	public void find() {
		JCardDataType found = JCardDataType.find("text");
		assertEquals(JCardDataType.TEXT, found);
	}

	@Test
	public void find_create() {
		JCardDataType found = JCardDataType.find("fake");
		assertEquals("fake", found.getName());
	}

	@Test
	public void find_not_found() {
		assertNull(JCardDataType.find("fake", false));
	}

	@Test
	public void all() {
		Collection<JCardDataType> dataTypes = JCardDataType.all();
		assertEquals(10, dataTypes.size());

		assertTrue(dataTypes.contains(JCardDataType.TEXT));
		assertTrue(dataTypes.contains(JCardDataType.INTEGER));
		assertTrue(dataTypes.contains(JCardDataType.BOOLEAN));
		assertTrue(dataTypes.contains(JCardDataType.FLOAT));
		assertTrue(dataTypes.contains(JCardDataType.DATE));
		assertTrue(dataTypes.contains(JCardDataType.TIME));
		assertTrue(dataTypes.contains(JCardDataType.DATE_TIME));
		assertTrue(dataTypes.contains(JCardDataType.UTC_OFFSET));
		assertTrue(dataTypes.contains(JCardDataType.LANGUAGE_TAG));
		assertTrue(dataTypes.contains(JCardDataType.URI));
	}

	@Test
	public void all_with_custom() {
		JCardDataType fake = JCardDataType.find("fake");
		Collection<JCardDataType> dataTypes = JCardDataType.all();
		assertEquals(11, dataTypes.size());

		assertTrue(dataTypes.contains(fake));
		assertTrue(dataTypes.contains(JCardDataType.TEXT));
		assertTrue(dataTypes.contains(JCardDataType.INTEGER));
		assertTrue(dataTypes.contains(JCardDataType.BOOLEAN));
		assertTrue(dataTypes.contains(JCardDataType.FLOAT));
		assertTrue(dataTypes.contains(JCardDataType.DATE));
		assertTrue(dataTypes.contains(JCardDataType.TIME));
		assertTrue(dataTypes.contains(JCardDataType.DATE_TIME));
		assertTrue(dataTypes.contains(JCardDataType.UTC_OFFSET));
		assertTrue(dataTypes.contains(JCardDataType.LANGUAGE_TAG));
		assertTrue(dataTypes.contains(JCardDataType.URI));
	}
}
