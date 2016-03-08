package ezvcard.io;

import ezvcard.VCard;
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
 * Thrown during the marshalling/unmarshalling of a property to signal to the
 * marshaller that the property's value is a nested (2.1 style) or embedded (3.0
 * style) vCard.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class EmbeddedVCardException extends RuntimeException {
	/*
	 * Serialization note: Not going to bother adding a serialVersionUID, since
	 * VCard is not serializable.
	 */
	private final VCard vcard;
	private final InjectionCallback callback;

	/**
	 * Thrown to unmarshal a nested or embedded vCard.
	 * @param callback injects the unmarshalled vCard into the property object
	 */
	public EmbeddedVCardException(InjectionCallback callback) {
		this.callback = callback;
		this.vcard = null;
	}

	/**
	 * Thrown to marshal a nested or embedded vCard.
	 * @param vcard the vCard to marshal
	 */
	public EmbeddedVCardException(VCard vcard) {
		this.callback = null;
		this.vcard = vcard;
	}

	/**
	 * Gets the vCard to marshal.
	 * @return the vCard to marshal
	 */
	public VCard getVCard() {
		return vcard;
	}

	/**
	 * Injects the unmarshalled vCard into the property object that threw this
	 * exception.
	 * @param vcard the vCard to inject
	 */
	public void injectVCard(VCard vcard) {
		if (callback == null) {
			return;
		}

		callback.injectVCard(vcard);
	}

	/**
	 * Gets the property object that threw the exception.
	 * @return the property object
	 */
	public VCardProperty getProperty() {
		if (callback == null) {
			return null;
		}

		return callback.getProperty();
	}

	/**
	 * Injects an unmarshalled vCard into the property object.
	 */
	public interface InjectionCallback {
		/**
		 * Injects an unmarshalled vCard into the property object.
		 * @param vcard the vCard to inject
		 */
		void injectVCard(VCard vcard);

		/**
		 * Gets the property object that threw the
		 * {@link EmbeddedVCardException}.
		 * @return the property object
		 */
		VCardProperty getProperty();
	}
}
