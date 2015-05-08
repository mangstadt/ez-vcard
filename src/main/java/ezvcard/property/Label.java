package ezvcard.property;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
 * <p>
 * Defines the exact text to put on the mailing label when sending snail mail to
 * the person. Note that instances of this class should NEVER be added to a
 * vCard! Instead, use the {@link Address#setLabel} method to assign a mailing
 * label to an {@link Address} property.
 * </p>
 * 
 * <p>
 * <b>Version interoperability</b>
 * </p>
 * 
 * <p>
 * The label property is not supported in vCard version 4.0. Instead, labels are
 * included as <i>parameters</i> to their corresponding {@link Address}
 * properties. When marshalling a vCard, ez-vcard will use either the label
 * property or the LABEL parameter, depending on the requested vCard version.
 * </p>
 * 
 * <p>
 * <b>Orphaned labels</b>
 * </p>
 * 
 * <p>
 * ez-vcard defines an "orphaned label" as a label property that could not be
 * assigned to an address (a label is assigned to an address if its list of TYPE
 * parameters is identical to the address's list of TYPE parameters). The
 * {@link VCard#addOrphanedLabel} method can be used to add such labels to a
 * vCard, but its use is strongly discouraged. The
 * {@link VCard#getOrphanedLabels} method can be useful when parsing version 2.1
 * or 3.0 vCards in order to retrieve any label properties that the parser could
 * not assign to an address.
 * </p>
 * 
 * <p>
 * <b>Property name:</b> {@code LABEL}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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
	public Set<AddressType> getTypes() {
		Set<String> values = parameters.getTypes();
		Set<AddressType> types = new HashSet<AddressType>(values.size());
		for (String value : values) {
			types.add(AddressType.get(value));
		}
		return types;
	}

	/**
	 * Adds a TYPE parameter.
	 * @param type the TYPE parameter to add
	 */
	public void addType(AddressType type) {
		parameters.addType(type.getValue());
	}

	/**
	 * Removes a TYPE parameter.
	 * @param type the TYPE parameter to remove
	 */
	public void removeType(AddressType type) {
		parameters.removeType(type.getValue());
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
