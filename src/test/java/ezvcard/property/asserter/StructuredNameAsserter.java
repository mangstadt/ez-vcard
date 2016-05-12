package ezvcard.property.asserter;

import static java.util.Arrays.asList;

import java.util.List;

import ezvcard.property.StructuredName;

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
public class StructuredNameAsserter extends PropertyImplAsserter<StructuredNameAsserter, StructuredName> {
	public StructuredNameAsserter(List<StructuredName> properties, VCardAsserter asserter) {
		super(properties, asserter);
	}

	public StructuredNameAsserter family(String family) {
		expected.setFamily(family);
		return this_;
	}

	public StructuredNameAsserter given(String given) {
		expected.setGiven(given);
		return this_;
	}

	public StructuredNameAsserter prefixes(String... prefixes) {
		expected.getPrefixes().addAll(asList(prefixes));
		return this_;
	}

	public StructuredNameAsserter suffixes(String... suffixes) {
		expected.getSuffixes().addAll(asList(suffixes));
		return this_;
	}

	public StructuredNameAsserter additional(String... additional) {
		expected.getAdditionalNames().addAll(asList(additional));
		return this_;
	}

	@Override
	protected StructuredName _newInstance() {
		return new StructuredName();
	}
}