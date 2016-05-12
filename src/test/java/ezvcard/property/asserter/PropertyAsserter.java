package ezvcard.property.asserter;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ezvcard.parameter.VCardParameters;
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
public abstract class PropertyAsserter<T, P extends VCardProperty> {
	@SuppressWarnings("unchecked")
	protected final T this_ = (T) this;
	private final Iterator<P> it;
	private final VCardAsserter asserter;

	private String group;
	private VCardParameters parameters;

	public PropertyAsserter(List<P> list, VCardAsserter asserter) {
		it = list.iterator();
		this.asserter = asserter;
		reset();
	}

	public T group(String group) {
		this.group = group;
		return this_;
	}

	public T param(String name, String... values) {
		parameters.putAll(name, asList(values));
		return this_;
	}

	public T next() {
		P actual = it.next();

		assertEquals(group, actual.getGroup());
		assertEquals(parameters, actual.getParameters());

		_run(actual);
		asserter.incPropertiesChecked();

		reset();
		return this_;
	}

	public void noMore() {
		next();
		assertFalse(it.hasNext());
	}

	private void reset() {
		group = null;
		parameters = new VCardParameters();
		_reset();
	}

	protected abstract void _run(P actual);

	protected abstract void _reset();

	protected static <T> List<T> arrayToList(T[] array) {
		return (array == null) ? Collections.<T> emptyList() : asList(array);
	}
}
