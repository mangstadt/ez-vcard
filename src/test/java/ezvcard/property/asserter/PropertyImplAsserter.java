package ezvcard.property.asserter;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;

import ezvcard.property.VCardProperty;

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
public abstract class PropertyImplAsserter<T, P extends VCardProperty> {
	@SuppressWarnings("unchecked")
	protected final T this_ = (T) this;
	private final Iterator<P> it;
	private final VCardAsserter asserter;

	protected P expected;

	public PropertyImplAsserter(List<P> list, VCardAsserter asserter) {
		it = list.iterator();
		this.asserter = asserter;
		expected = _newInstance();
	}

	public T expected(P expected) {
		this.expected = expected;
		return this_;
	}

	public T group(String group) {
		expected.setGroup(group);
		return this_;
	}

	public T param(String name, String... values) {
		expected.getParameters().putAll(name, asList(values));
		return this_;
	}

	public T next() {
		P actual = it.next();
		assertEquals(expected, actual);
		asserter.incPropertiesChecked();

		expected = _newInstance();
		return this_;
	}

	public void noMore() {
		next();
		assertFalse(it.hasNext());
	}

	protected abstract P _newInstance();
}
