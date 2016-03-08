package ezvcard.util;

import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static ezvcard.util.TestUtils.assertEqualsMethodEssentials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

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
public class CharacterBitSetTest {
	@Test
	public void expandCharacterList() {
		BitSet actual = new CharacterBitSet("abc123").bitSet();
		BitSet expected = new BitSet();
		expected.set('a', 'd');
		expected.set('1', '4');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("a-f").bitSet();
		expected = new BitSet();
		expected.set('a', 'g');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("a-fa-f").bitSet();
		expected = new BitSet();
		expected.set('a', 'g');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("f-a").bitSet();
		expected = new BitSet();
		expected.set('a', 'g');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("a-fz").bitSet();
		expected = new BitSet();
		expected.set('a', 'g');
		expected.set('z');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("-a-f").bitSet();
		expected = new BitSet();
		expected.set('a', 'g');
		expected.set('-');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("a-f-").bitSet();
		expected = new BitSet();
		expected.set('a', 'g');
		expected.set('-');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("-a-f0-9xyz*").bitSet();
		expected = new BitSet();
		expected.set('a', 'g');
		expected.set('-');
		expected.set('0', '9' + 1);
		expected.set('x');
		expected.set('y');
		expected.set('z');
		expected.set('*');
		assertEquals(expected, actual);

		actual = new CharacterBitSet("").bitSet();
		expected = new BitSet();
		assertEquals(expected, actual);
	}

	@Test
	public void characters() {
		CharacterBitSet bitSet = new CharacterBitSet("a-z");
		assertEquals("a-z", bitSet.characters());
	}

	@Test
	public void containsOnly_empty() {
		CharacterBitSet bitSet = new CharacterBitSet("");
		assertFalse(bitSet.containsOnly("anything"));
		assertTrue(bitSet.containsOnly(""));
	}

	@Test
	public void containsOnly() {
		CharacterBitSet bitSet = new CharacterBitSet("b-f0-9");
		assertTrue(bitSet.containsOnly("bf3"));
		assertFalse(bitSet.containsOnly("af3"));
		assertTrue(bitSet.containsOnly("bf3", 1));
		assertTrue(bitSet.containsOnly("af3", 1));
		assertTrue(bitSet.containsOnly(""));
	}

	@Test
	public void containsAny_empty() {
		CharacterBitSet bitSet = new CharacterBitSet("");
		assertFalse(bitSet.containsAny("anything"));
		assertFalse(bitSet.containsAny(""));
	}

	@Test
	public void containsAny() {
		CharacterBitSet bitSet = new CharacterBitSet("b-f0-9");
		assertTrue(bitSet.containsAny("bxx"));
		assertFalse(bitSet.containsAny("aaa"));
		assertFalse(bitSet.containsAny("bxx", 1));
		assertFalse(bitSet.containsAny(""));
	}

	@Test
	public void equals_contract() {
		CharacterBitSet one = new CharacterBitSet("b-f0-9");
		CharacterBitSet two = new CharacterBitSet("0-9f-b");
		assertEqualsMethodEssentials(one);
		assertEqualsAndHash(one, two);

		one = new CharacterBitSet("b-f0-9");
		two = new CharacterBitSet("bcfed9786452310");
		assertEqualsAndHash(one, two);

		one = new CharacterBitSet("a-f0-9");
		two = new CharacterBitSet("0-9f-b");
		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}
}
