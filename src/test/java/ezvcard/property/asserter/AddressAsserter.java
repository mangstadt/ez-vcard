package ezvcard.property.asserter;

import static java.util.Arrays.asList;

import java.util.List;

import ezvcard.parameter.AddressType;
import ezvcard.property.Address;

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
public class AddressAsserter extends PropertyImplAsserter<AddressAsserter, Address> {
	public AddressAsserter(List<Address> properties, VCardAsserter asserter) {
		super(properties, asserter);
	}

	public AddressAsserter poBox(String poBox) {
		expected.setPoBox(poBox);
		return this_;
	}

	public AddressAsserter extendedAddress(String extendedAddress) {
		expected.setExtendedAddress(extendedAddress);
		return this_;
	}

	public AddressAsserter streetAddress(String... streetAddress) {
		expected.getStreetAddresses().addAll(asList(streetAddress));
		return this_;
	}

	public AddressAsserter locality(String locality) {
		expected.setLocality(locality);
		return this_;
	}

	public AddressAsserter region(String region) {
		expected.setRegion(region);
		return this_;
	}

	public AddressAsserter postalCode(String postalCode) {
		expected.setPostalCode(postalCode);
		return this_;
	}

	public AddressAsserter country(String country) {
		expected.setCountry(country);
		return this_;
	}

	public AddressAsserter label(String label) {
		expected.setLabel(label);
		return this_;
	}

	public AddressAsserter types(AddressType... types) {
		expected.getTypes().addAll(asList(types));
		return this_;
	}

	@Override
	protected Address _newInstance() {
		return new Address();
	}
}