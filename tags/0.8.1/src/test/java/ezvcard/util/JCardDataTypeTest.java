package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

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
	@BeforeClass
	public static void beforeClass() {
		JCardDataType.custom.clear();
	}

	@After
	public void after() {
		JCardDataType.custom.clear();
	}

	@Test
	public void find() {
		JCardDataType found = JCardDataType.find("text");
		assertTrue(JCardDataType.TEXT == found);

		assertNull(JCardDataType.find("fake"));
	}

	@Test
	public void get() {
		JCardDataType found = JCardDataType.get("text");
		assertTrue(JCardDataType.TEXT == found);

		JCardDataType fake1 = JCardDataType.get("fake");
		assertEquals("fake", fake1.getName());

		JCardDataType fake2 = JCardDataType.get("fake");
		assertTrue(fake1 == fake2);
	}

	@Test
	public void all() {
		Collection<JCardDataType> dataTypes = JCardDataType.all();
		assertEquals(13, dataTypes.size());

		assertTrue(dataTypes.contains(JCardDataType.TEXT));
		assertTrue(dataTypes.contains(JCardDataType.INTEGER));
		assertTrue(dataTypes.contains(JCardDataType.BOOLEAN));
		assertTrue(dataTypes.contains(JCardDataType.FLOAT));
		assertTrue(dataTypes.contains(JCardDataType.DATE));
		assertTrue(dataTypes.contains(JCardDataType.TIME));
		assertTrue(dataTypes.contains(JCardDataType.DATE_TIME));
		assertTrue(dataTypes.contains(JCardDataType.DATE_AND_OR_TIME));
		assertTrue(dataTypes.contains(JCardDataType.TIMESTAMP));
		assertTrue(dataTypes.contains(JCardDataType.UTC_OFFSET));
		assertTrue(dataTypes.contains(JCardDataType.LANGUAGE_TAG));
		assertTrue(dataTypes.contains(JCardDataType.URI));
		assertTrue(dataTypes.contains(JCardDataType.UNKNOWN));
	}

	@Test
	public void all_with_custom() {
		JCardDataType fake = JCardDataType.get("fake");
		Collection<JCardDataType> dataTypes = JCardDataType.all();
		assertEquals(14, dataTypes.size());

		assertTrue(dataTypes.contains(fake));
		assertTrue(dataTypes.contains(JCardDataType.TEXT));
		assertTrue(dataTypes.contains(JCardDataType.INTEGER));
		assertTrue(dataTypes.contains(JCardDataType.BOOLEAN));
		assertTrue(dataTypes.contains(JCardDataType.FLOAT));
		assertTrue(dataTypes.contains(JCardDataType.DATE));
		assertTrue(dataTypes.contains(JCardDataType.TIME));
		assertTrue(dataTypes.contains(JCardDataType.DATE_TIME));
		assertTrue(dataTypes.contains(JCardDataType.DATE_AND_OR_TIME));
		assertTrue(dataTypes.contains(JCardDataType.TIMESTAMP));
		assertTrue(dataTypes.contains(JCardDataType.UTC_OFFSET));
		assertTrue(dataTypes.contains(JCardDataType.LANGUAGE_TAG));
		assertTrue(dataTypes.contains(JCardDataType.URI));
		assertTrue(dataTypes.contains(JCardDataType.UNKNOWN));
	}
}
