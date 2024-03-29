package ezvcard.io.chain;

import java.util.Collection;

import ezvcard.VCard;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.property.ProductId;
import ezvcard.property.VCardProperty;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 */

/**
 * Parent class for all chaining writers. This class is package-private in order
 * to hide it from the generated Javadocs.
 * @author Michael Angstadt
 * @param <T> the object instance's type (for method chaining)
 */
class ChainingWriter<T extends ChainingWriter<?>> {
	final Collection<VCard> vcards;
	ScribeIndex index;
	boolean prodId = true;
	boolean versionStrict = true;

	@SuppressWarnings("unchecked")
	private final T this_ = (T) this;

	/**
	 * @param vcards the vCards to write
	 */
	ChainingWriter(Collection<VCard> vcards) {
		this.vcards = vcards;
	}

	/**
	 * Sets whether to exclude properties that do not support the target version
	 * from the written vCard.
	 * @param versionStrict true to exclude such properties, false not to
	 * (defaults to true)
	 * @return this
	 */
	T versionStrict(boolean versionStrict) {
		this.versionStrict = versionStrict;
		return this_;
	}

	/**
	 * Sets whether to add a {@link ProductId} property to each vCard that marks
	 * it as having been generated by this library. For 2.1 vCards, the extended
	 * property "X-PRODID" will be added, since {@link ProductId} is not
	 * supported by that version.
	 * @param include true to add the property, false not to (defaults to true)
	 * @return this
	 */
	T prodId(boolean include) {
		this.prodId = include;
		return this_;
	}

	/**
	 * Registers a property scribe.
	 * @param scribe the scribe to register
	 * @return this
	 */
	T register(VCardPropertyScribe<? extends VCardProperty> scribe) {
		if (index == null) {
			index = new ScribeIndex();
		}
		index.register(scribe);
		return this_;
	}
}
