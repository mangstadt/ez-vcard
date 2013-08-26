package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.VCardStringUtils;

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
 * parameter to their corresponding {@link AddressType}. When marshalling a
 * vCard, ez-vcard will use either the LABEL type or the LABEL parameter,
 * depending on the requested vCard version.
 * </p>
 * 
 * <p>
 * To add a label to a vCard, the {@link AddressType#setLabel} method should be
 * used.
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * AddressType adr = new AddressType();
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
 * <pre>
 * VCard vcard = ...
 * for (LabelType label : vcard.getOrphanedLabels()) {
 * 	System.out.println(label.getValue());
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>LABEL</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class LabelType extends MultiValuedTypeParameterType<AddressTypeParameter> {
	public static final String NAME = "LABEL";

	private String value;

	/**
	 * Creates an empty label property.
	 */
	public LabelType() {
		this(null);
	}

	/**
	 * Creates a label property.
	 * @param label the label value
	 */
	public LabelType(String label) {
		super(NAME);
		setValue(label);
	}

	@Override
	protected AddressTypeParameter buildTypeObj(String type) {
		return AddressTypeParameter.get(type);
	}

	/**
	 * Gets the label value.
	 * @return the label value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the label value.
	 * @param value the label value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V2_1, VCardVersion.V3_0 };
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		sb.append(VCardStringUtils.escape(value));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		setValue(VCardStringUtils.unescape(value));
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		List<String> types = element.types();
		for (String type : types) {
			subTypes.addType(type);
		}

		setValue(element.value());
	}
}
