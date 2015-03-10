package ezvcard.property.asserter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import ezvcard.property.StructuredName;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
public class StructuredNameAsserter extends PropertyAsserter<StructuredNameAsserter, StructuredName> {
	private String family, given;
	private String[] prefixes, suffixes, additional;

	public StructuredNameAsserter(List<StructuredName> properties) {
		super(properties);
	}

	public StructuredNameAsserter family(String family) {
		this.family = family;
		return this_;
	}

	public StructuredNameAsserter given(String given) {
		this.given = given;
		return this_;
	}

	public StructuredNameAsserter prefixes(String... prefixes) {
		this.prefixes = prefixes;
		return this_;
	}

	public StructuredNameAsserter suffixes(String... suffixes) {
		this.suffixes = suffixes;
		return this_;
	}

	public StructuredNameAsserter additional(String... additional) {
		this.additional = additional;
		return this_;
	}

	@Override
	protected void _run(StructuredName property) {
		assertEquals(family, property.getFamily());
		assertEquals(given, property.getGiven());
		assertEquals(arrayToList(prefixes), property.getPrefixes());
		assertEquals(arrayToList(suffixes), property.getSuffixes());
		assertEquals(arrayToList(additional), property.getAdditional());
	}

	@Override
	protected void _reset() {
		family = given = null;
		prefixes = suffixes = additional = null;
	}
}