package ezvcard.property;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;

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
 * Represents a property whose value is a list of textual values.
 * @author Michael Angstadt
 * @param <T> the type of values stored in the list
 */
public class ListProperty<T> extends VCardProperty {
	protected final List<T> values;

	public ListProperty() {
		values = new ArrayList<T>();
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public ListProperty(ListProperty<T> original) {
		super(original);
		values = new ArrayList<T>(original.values);
	}

	/**
	 * Gets the list that stores this property's values.
	 * @return the list of values (this list is mutable)
	 */
	public List<T> getValues() {
		return values;
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (values.isEmpty()) {
			warnings.add(new Warning(8));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("values", this.values);
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + values.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		ListProperty<?> other = (ListProperty<?>) obj;
		if (!values.equals(other.values)) return false;
		return true;
	}
}
