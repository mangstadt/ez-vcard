package ezvcard.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
public class KindTest {
	@Test
	public void isIndividual() {
		Kind kind = new Kind("individual");
		assertTrue(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	public void isGroup() {
		Kind kind = new Kind("group");
		assertFalse(kind.isIndividual());
		assertTrue(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	public void isOrg() {
		Kind kind = new Kind("org");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertTrue(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	public void isLocation() {
		Kind kind = new Kind("location");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertTrue(kind.isLocation());
		assertFalse(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	public void isApplication() {
		Kind kind = new Kind("application");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertTrue(kind.isApplication());
		assertFalse(kind.isDevice());
	}

	@Test
	public void isDevice() {
		Kind kind = new Kind("device");
		assertFalse(kind.isIndividual());
		assertFalse(kind.isGroup());
		assertFalse(kind.isOrg());
		assertFalse(kind.isLocation());
		assertFalse(kind.isApplication());
		assertTrue(kind.isDevice());
	}

	@Test
	public void individual() {
		Kind kind = Kind.individual();
		assertEquals("individual", kind.getValue());
	}

	@Test
	public void group() {
		Kind kind = Kind.group();
		assertEquals("group", kind.getValue());
	}

	@Test
	public void org() {
		Kind kind = Kind.org();
		assertEquals("org", kind.getValue());
	}

	@Test
	public void location() {
		Kind kind = Kind.location();
		assertEquals("location", kind.getValue());
	}

	@Test
	public void application() {
		Kind kind = Kind.application();
		assertEquals("application", kind.getValue());
	}

	@Test
	public void device() {
		Kind kind = Kind.device();
		assertEquals("device", kind.getValue());
	}
}
