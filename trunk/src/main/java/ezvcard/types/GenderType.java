package ezvcard.types;

import java.util.List;

import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * Defines the person's sex.
 * 
 * <p>
 * <b>Setting the gender</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * GenderType gender = GenderType.male();
 * vcard.setGender(gender);
 * </pre>
 * 
 * <p>
 * <b>Getting the gender</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = ...
 * GenderType gender = vcard.getGender();
 * if (gender != null){
 *   if (gender.isMale()){
 *     ...
 *   } else if (gender.isFemale()){
 *     ...
 *   }
 *   ...
 * }
 * </pre>
 * 
 * <p>
 * vCard property name: GENDER
 * </p>
 * <p>
 * vCard versions: 4.0
 * </p>
 * @author Michael Angstadt
 */
public class GenderType extends VCardType {
	public static final String NAME = "GENDER";

	public static final String MALE = "M";
	public static final String FEMALE = "F";
	public static final String OTHER = "O";
	public static final String NONE = "N";
	public static final String UNKNOWN = "U";

	private String gender;
	private String text;

	public GenderType() {
		super(NAME);
	}

	/**
	 * Use of this constructor is discouraged. Please use one of the static
	 * methods to create a new GENDER type.
	 * @param gender the gender value (e.g. "F")
	 */
	public GenderType(String gender) {
		super(NAME);
		this.gender = gender;
	}

	/**
	 * Gets the additional text associated with this gender type.
	 * @return the additional text or null if there is no text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the additional text associated with this gender type.
	 * @param text additional text or null to remove
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the gender value.
	 * @return the gender value (see static strings for the possible values)
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * Sets the gender value.
	 * @param gender the gender value (see static strings for the possible
	 * values)
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * Determines if the gender is "male" or not.
	 * @return true if the gender is "male", false if not
	 */
	public boolean isMale() {
		return MALE.equals(gender);
	}

	/**
	 * Determines if the gender is "female" or not.
	 * @return true if the gender is "female", false if not
	 */
	public boolean isFemale() {
		return FEMALE.equals(gender);
	}

	/**
	 * Determines if the gender is "other" or not.
	 * @return true if the gender is "other", false if not
	 */
	public boolean isOther() {
		return OTHER.equals(gender);
	}

	/**
	 * Determines if the gender is "none" or not. A group, organization, or
	 * location may have this gender type.
	 * @return true if the gender is "none", false if not
	 */
	public boolean isNone() {
		return NONE.equals(gender);
	}

	/**
	 * Determines if the gender is "unknown" or not.
	 * @return true if the gender is "unknown", false if not
	 */
	public boolean isUnknown() {
		return UNKNOWN.equals(gender);
	}

	/**
	 * Creates a gender type whose value is set to "male".
	 * @return a "male" gender type
	 */
	public static GenderType male() {
		return new GenderType(MALE);
	}

	/**
	 * Creates a gender type whose value is set to "female".
	 * @return a "female" gender type
	 */
	public static GenderType female() {
		return new GenderType(FEMALE);
	}

	/**
	 * Creates a gender type whose value is set to "other".
	 * @return an "other" gender type
	 */
	public static GenderType other() {
		return new GenderType(OTHER);
	}

	/**
	 * Creates a gender type whose value is set to "none". Groups,
	 * organizations, and locations should be given this gender type.
	 * @return a "none" gender type
	 */
	public static GenderType none() {
		return new GenderType(NONE);
	}

	/**
	 * Creates a gender type whose value is set to "unknown".
	 * @return a "unknown" gender type
	 */
	public static GenderType unknown() {
		return new GenderType(UNKNOWN);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (gender != null) {
			sb.append(gender);
		}
		if (text != null) {
			sb.append(';');
			sb.append(VCardStringUtils.escape(text));
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String split[] = value.split(";", 2);
		setGender(split[0].toUpperCase());
		if (split.length > 1) {
			setText(VCardStringUtils.unescape(split[1]));
		}
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		parent.append("sex", (gender == null) ? "" : gender);
		if (text != null) {
			parent.append("identity", text);
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		setGender(element.get("sex"));
		setText(element.get("identity"));
	}
}
