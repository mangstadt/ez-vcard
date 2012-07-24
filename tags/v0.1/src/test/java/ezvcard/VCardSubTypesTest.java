package ezvcard;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

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
public class VCardSubTypesTest {
	@Test
	public void getFirst() {
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.put("NUMBERS", "1");
		subTypes.put("NUMBERS", "2");
		subTypes.put("NUMBERS", "3");

		//"getFirst()" will return one of the values
		//it's not sure which one because a Set is returned
		List<String> expected = Arrays.asList("1", "2", "3");
		assertTrue(expected.contains(subTypes.getFirst("NUMBERS")));
	}

	@Test
	public void put() {
		//names are case insensitive
		//values should retain their case
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.put("test1", "OnE");
		subTypes.put("TEST2", "TwO");
		subTypes.put("test3", "three");
		subTypes.put("tESt3", "trois");
		subTypes.put("tesT3", "three"); //value should not be added because it already exists

		assertEquals("OnE", subTypes.getFirst("tESt1"));
		assertEquals("TwO", subTypes.getFirst("test2"));
		Set<String> s = new HashSet<String>(Arrays.asList("three", "trois"));
		assertEquals(s, subTypes.get("TEST3"));
	}

	@Test
	public void remove() {
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.put("NUMBERS", "1");
		subTypes.put("NUMBERS", "2");
		subTypes.put("NUMBERS", "3");
		subTypes.remove("NUMBERS", "2");

		Set<String> expected = new HashSet<String>(Arrays.asList("1", "3"));
		assertEquals(expected, subTypes.get("NUMBERS"));
	}

	@Test
	public void removeAll() {
		VCardSubTypes subTypes = new VCardSubTypes();
		subTypes.put("NUMBERS", "1");
		subTypes.put("NUMBERS", "2");
		subTypes.put("NUMBERS", "3");
		subTypes.removeAll("NuMBERs");

		assertTrue(subTypes.get("NUMBERS").isEmpty());
	}
}
