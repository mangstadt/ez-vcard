package ezvcard.property;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressTypeParameter;

/*
 Copyright (c) 2013, Michael Angstadt
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
 * <p>
 * Defines the exact text to put on the mailing label when mailing a package or
 * letter to the person.
 * </p>
 * 
 * <p>
 * The LABEL type is not supported in 4.0. Instead, labels are included as a
 * parameter to their corresponding {@link Address}. When marshalling a
 * vCard, ez-vcard will use either the LABEL type or the LABEL parameter,
 * depending on the requested vCard version.
 * </p>
 * 
 * <p>
 * To add a label to a vCard, the {@link Address#setLabel} method should be
 * used.
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * Address adr = new Address();
 * adr.setStreetAddress(&quot;123 Main St.&quot;);
 * adr.setLocality(&quot;Austin&quot;);
 * adr.setRegion(&quot;TX&quot;);
 * adr.setPostalCode(&quot;12345&quot;);
 * adr.setLabel(&quot;123 Main St.\nAustin, TX 12345&quot;); //newlines are allowed
 * vcard.addAddress(adr);
 * </pre>
 * 
 * <p>
 * The {@link VCard#addOrphanedLabel} method adds a LABEL type to the vCard.
 * However, use of this method is discouraged because it creates a LABEL type
 * that's not associated with an address. Also, orphaned LABEL types are ignored
 * when creating version 4.0 vCards because the LABEL type is not supported by
 * vCard 4.0.
 * </p>
 * 
 * <p>
 * The {@link VCard#getOrphanedLabels} method can be used after parsing a
 * version 2.1 or 3.0 vCard to retrieve any LABEL types which the parser could
 * not assign to an address. A LABEL is assigned to an address if the LABEL's
 * list of TYPE parameters is identical to the address's list of TYPE
 * parameters.
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * for (Label label : vcard.getOrphanedLabels()) {
 * 	System.out.println(label.getValue());
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code LABEL}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0}
 * </p>
 * @author Michael Angstadt
 */
public class Label extends TextProperty {
	/**
	 * Creates a label property.
	 * @param label the label value
	 */
	public Label(String label) {
		super(label);
	}

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V2_1, VCardVersion.V3_0);
	}

	/**
	 * Gets all the TYPE parameters.
	 * @return the TYPE parameters or empty set if there are none
	 */
	public Set<AddressTypeParameter> getTypes() {
		Set<String> values = subTypes.getTypes();
		Set<AddressTypeParameter> types = new HashSet<AddressTypeParameter>(values.size());
		for (String value : values) {
			types.add(AddressTypeParameter.get(value));
		}
		return types;
	}

	/**
	 * Adds a TYPE parameter.
	 * @param type the TYPE parameter to add
	 */
	public void addType(AddressTypeParameter type) {
		subTypes.addType(type.getValue());
	}

	/**
	 * Removes a TYPE parameter.
	 * @param type the TYPE parameter to remove
	 */
	public void removeType(AddressTypeParameter type) {
		subTypes.removeType(type.getValue());
	}

	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}
}
