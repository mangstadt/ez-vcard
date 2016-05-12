package ezvcard.property.asserter;

import static org.junit.Assert.assertEquals;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Address;
import ezvcard.property.BinaryProperty;
import ezvcard.property.DateOrTimeProperty;
import ezvcard.property.Email;
import ezvcard.property.Geo;
import ezvcard.property.Impp;
import ezvcard.property.ListProperty;
import ezvcard.property.RawProperty;
import ezvcard.property.SimpleProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;

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
	private int propertiesChecked = 0;

	public VCardAsserter(VCard vcard) {
		this.vcard = vcard;
	}

	/**
	 * Asserts the version of the vCard.
	 * @param expected the expected version
	 */
	public void version(VCardVersion expected) {
		VCardVersion actual = vcard.getVersion();
		assertEquals(expected, actual);
	}

	/**
	 * Asserts the contents of the vCard's properties that match the given
	 * class.
	 * @param clazz the property class
	 * @return the asserter object
	 */
	public <T extends VCardProperty> PropertyImplAsserterImpl<T> property(Class<T> clazz) {
		return new PropertyImplAsserterImpl<T>(vcard.getProperties(clazz), this);
	}

	/**
	 * Asserts the contents of the vCard's properties that match the given
	 * class.
	 * @param clazz the property class (must extend {@link SimpleProperty})
	 * @return the asserter object
	 */
	@SuppressWarnings("rawtypes")
	public <T extends SimpleProperty> SimplePropertyAsserter<T> simpleProperty(Class<T> clazz) {
		return new SimplePropertyAsserter<T>(vcard.getProperties(clazz), this);
	}

	/**
	 * Asserts the contents of the vCard's properties that match the given
	 * class.
	 * @param clazz the property class (must extend {@link ListProperty})
	 * @return the asserter object
	 */
	public <T extends ListProperty<String>> ListPropertyAsserter<T> listProperty(Class<T> clazz) {
		return new ListPropertyAsserter<T>(vcard.getProperties(clazz), this);
	}

	/**
	 * Asserts the contents of the vCard's properties that match the given
	 * class.
	 * @param clazz the property class (must extend {@link DateOrTimeProperty})
	 * @return the asserter object
	 */
	public <T extends DateOrTimeProperty> DateOrTimePropertyAsserter<T> dateProperty(Class<T> clazz) {
		return new DateOrTimePropertyAsserter<T>(vcard.getProperties(clazz), this);
	}

	/**
	 * Asserts the contents of the vCard's properties that match the given
	 * class.
	 * @param clazz the property class (must extend {@link BinaryProperty})
	 * @return the asserter object
	 */
	@SuppressWarnings("rawtypes")
	public <T extends BinaryProperty> BinaryPropertyAsserter<T> binaryProperty(Class<T> clazz) {
		return new BinaryPropertyAsserter<T>(vcard.getProperties(clazz), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link Geo} properties.
	 * @return the asserter object
	 */
	public GeoAsserter geo() {
		return new GeoAsserter(vcard.getGeos(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link Timezone} properties.
	 * @return the asserter object
	 */
	public TimezoneAsserter timezone() {
		return new TimezoneAsserter(vcard.getTimezones(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link Email} properties.
	 * @return the asserter object
	 */
	public EmailAsserter email() {
		return new EmailAsserter(vcard.getEmails(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link Impp} properties.
	 * @return the asserter object
	 */
	public ImppAsserter impp() {
		return new ImppAsserter(vcard.getImpps(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link Address} properties.
	 * @return the asserter object
	 */
	public AddressAsserter address() {
		return new AddressAsserter(vcard.getAddresses(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link Telephone} properties.
	 * @return the asserter object
	 */
	public TelephoneAsserter telephone() {
		return new TelephoneAsserter(vcard.getTelephoneNumbers(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link StructuredName} properties.
	 * @return the asserter object
	 */
	public StructuredNameAsserter structuredName() {
		return new StructuredNameAsserter(vcard.getStructuredNames(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link Xml} properties.
	 * @return the asserter object
	 */
	public XmlAsserter xml() {
		return new XmlAsserter(vcard.getXmls(), this);
	}

	/**
	 * Asserts the contents of the vCard's {@link RawProperty} properties.
	 * @param name the property name
	 * @return the asserter object
	 */
	public RawPropertyAsserter rawProperty(String name) {
		return new RawPropertyAsserter(vcard.getExtendedProperties(name), name, this);
	}

	/**
	 * This should be called when you are done asserting the contents of the
	 * vCard. It makes sure that no properties were skipped.
	 */
	public void done() {
		int total = vcard.getProperties().size();
		assertEquals("The vCard has " + total + " properties, but only " + propertiesChecked + " were checked.", propertiesChecked, total);
	}

	void incPropertiesChecked() {
		propertiesChecked++;
	}
}
