package ezvcard.property.asserter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.BinaryProperty;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.property.ListProperty;
import ezvcard.property.SimpleProperty;
import ezvcard.property.VCardProperty;

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
public abstract class PropertyAsserter<T, P extends VCardProperty> {
	@SuppressWarnings("unchecked")
	protected final T this_ = (T) this;
	private final Iterator<P> it;

	private String group;
	private VCardParameters parameters;

	public PropertyAsserter(List<P> list) {
		it = list.iterator();
		reset();
	}

	public T group(String group) {
		this.group = group;
		return this_;
	}

	public T param(String name, String value) {
		parameters.put(name, value);
		return this_;
	}

	public T next() {
		P property = it.next();

		assertEquals(group, property.getGroup());
		for (Map.Entry<String, List<String>> entry : parameters) {
			assertEquals(entry.getValue(), property.getParameters(entry.getKey()));
		}

		_run(property);

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

	protected abstract void _run(P property);

	protected abstract void _reset();

	protected static <T> List<T> arrayToList(T[] array) {
		return (array == null) ? Collections.<T> emptyList() : Arrays.asList(array);
	}

	public static RawPropertyAsserter assertRawProperty(String name, VCard vcard) {
		return new RawPropertyAsserter(vcard.getExtendedProperties(name), name);
	}

	@SuppressWarnings("rawtypes")
	public static <T extends SimpleProperty> SimplePropertyAsserter<T> assertSimpleProperty(List<T> list) {
		return new SimplePropertyAsserter<T>(list);
	}

	public static <T extends ListProperty<String>> ListPropertyAsserter<T> assertListProperty(List<T> list) {
		return new ListPropertyAsserter<T>(list);
	}

	public static <T extends DateOrTimeProperty> DateOrTimePropertyAsserter<T> assertDateProperty(List<T> list) {
		return new DateOrTimePropertyAsserter<T>(list);
	}

	@SuppressWarnings("rawtypes")
	public static <T extends BinaryProperty> BinaryPropertyAsserter<T> assertBinaryProperty(List<T> list) {
		return new BinaryPropertyAsserter<T>(list);
	}

	public static GeoAsserter assertGeo(VCard vcard) {
		return new GeoAsserter(vcard.getGeos());
	}

	public static TimezoneAsserter assertTimezone(VCard vcard) {
		return new TimezoneAsserter(vcard.getTimezones());
	}

	public static EmailAsserter assertEmail(VCard vcard) {
		return new EmailAsserter(vcard.getEmails());
	}

	public static ImppAsserter assertImpp(VCard vcard) {
		return new ImppAsserter(vcard.getImpps());
	}

	public static AddressAsserter assertAddress(VCard vcard) {
		return new AddressAsserter(vcard.getAddresses());
	}

	public static TelephoneAsserter assertTelephone(VCard vcard) {
		return new TelephoneAsserter(vcard.getTelephoneNumbers());
	}

	public static StructuredNameAsserter assertStructuredName(VCard vcard) {
		return new StructuredNameAsserter(vcard.getStructuredNames());
	}
}
