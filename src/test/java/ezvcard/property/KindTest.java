package ezvcard.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 Copyright (c) 2012-2026, Michael Angstadt
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
class KindTest {
	@Test
	void isIndividual() {
		Kind kind = new Kind("Individual");
		assertTrue(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	void isGroup() {
		Kind kind = new Kind("Group");
		assertFalse(kind.isIndividual());
		assertTrue(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	void isOrg() {
		Kind kind = new Kind("Org");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertTrue(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	void isLocation() {
		Kind kind = new Kind("Location");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertTrue(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	void isApplication() {
		Kind kind = new Kind("Application");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertTrue(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	void isDevice() {
		Kind kind = new Kind("Device");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertTrue(kind.isDevice());
	}

	@Test
	void individual() {
		Kind kind = Kind.individual();
		assertEquals("individual", kind.getValue());
	}

	@Test
	void group() {
		Kind kind = Kind.group();
		assertEquals("group", kind.getValue());
	}

	@Test
	void org() {
		Kind kind = Kind.org();
		assertEquals("org", kind.getValue());
	}

	@Test
	void location() {
		Kind kind = Kind.location();
		assertEquals("location", kind.getValue());
	}

	@Test
	void application() {
		Kind kind = Kind.application();
		assertEquals("application", kind.getValue());
	}

	@Test
	void device() {
		Kind kind = Kind.device();
		assertEquals("device", kind.getValue());
	}
}
