package ezvcard.property.asserter;

import ezvcard.VCard;
import ezvcard.property.BinaryProperty;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.property.ListProperty;
import ezvcard.property.SimpleProperty;

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
 * Helper class used to assert the contents of a {@link VCard} object.
 * @author Michael Angstadt
 */
public class VCardAsserter {
	private final VCard vcard;

	public VCardAsserter(VCard vcard) {
		this.vcard = vcard;
	}

	@SuppressWarnings("rawtypes")
	public <T extends SimpleProperty> SimplePropertyAsserter<T> simpleProperty(Class<T> clazz) {
		return new SimplePropertyAsserter<T>(vcard.getProperties(clazz));
	}

	public <T extends ListProperty<String>> ListPropertyAsserter<T> listProperty(Class<T> clazz) {
		return new ListPropertyAsserter<T>(vcard.getProperties(clazz));
	}

	public <T extends DateOrTimeProperty> DateOrTimePropertyAsserter<T> dateProperty(Class<T> clazz) {
		return new DateOrTimePropertyAsserter<T>(vcard.getProperties(clazz));
	}

	@SuppressWarnings("rawtypes")
	public <T extends BinaryProperty> BinaryPropertyAsserter<T> binaryProperty(Class<T> clazz) {
		return new BinaryPropertyAsserter<T>(vcard.getProperties(clazz));
	}

	public GeoAsserter geo() {
		return new GeoAsserter(vcard.getGeos());
	}

	public TimezoneAsserter timezone() {
		return new TimezoneAsserter(vcard.getTimezones());
	}

	public EmailAsserter email() {
		return new EmailAsserter(vcard.getEmails());
	}

	public ImppAsserter impp() {
		return new ImppAsserter(vcard.getImpps());
	}

	public AddressAsserter address() {
		return new AddressAsserter(vcard.getAddresses());
	}

	public TelephoneAsserter telephone() {
		return new TelephoneAsserter(vcard.getTelephoneNumbers());
	}

	public StructuredNameAsserter structuredName() {
		return new StructuredNameAsserter(vcard.getStructuredNames());
	}

	public RawPropertyAsserter rawProperty(String name) {
		return new RawPropertyAsserter(vcard.getExtendedProperties(name), name);
	}
}
