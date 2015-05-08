package ezvcard.property;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
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
 * Defines the person's sex.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Gender gender = Gender.male();
 * vcard.setGender(gender);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Gender gender = vcard.getGender();
 * if (gender.isMale()){
 *   //gender is male
 * } else if (gender.isFemale()){
 *   //gender is female
 * }
 * //etc
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code GENDER}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Gender extends VCardProperty {
	public static final String MALE = "M";
	public static final String FEMALE = "F";
	public static final String OTHER = "O";
	public static final String NONE = "N";
	public static final String UNKNOWN = "U";

	private String gender;
	private String text;

	/**
	 * Creates a gender property. Use of this constructor is discouraged. Please
	 * use one of the static factory methods to create a new GENDER property.
	 * @param gender the gender value (e.g. "F")
	 */
	public Gender(String gender) {
		this.gender = gender;
	}

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V4_0);
	}

	/**
	 * Gets the additional text associated with this property.
	 * @return the additional text or null if there is no text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the additional text associated with this property.
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
	 * location may have this gender property.
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
	 * Creates a gender property whose value is set to "male".
	 * @return a "male" gender property
	 */
	public static Gender male() {
		return new Gender(MALE);
	}

	/**
	 * Creates a gender property whose value is set to "female".
	 * @return a "female" gender property
	 */
	public static Gender female() {
		return new Gender(FEMALE);
	}

	/**
	 * Creates a gender property whose value is set to "other".
	 * @return an "other" gender property
	 */
	public static Gender other() {
		return new Gender(OTHER);
	}

	/**
	 * Creates a gender property whose value is set to "none". Groups,
	 * organizations, and locations should be given this gender property.
	 * @return a "none" gender property
	 */
	public static Gender none() {
		return new Gender(NONE);
	}

	/**
	 * Creates a gender property whose value is set to "unknown".
	 * @return a "unknown" gender property
	 */
	public static Gender unknown() {
		return new Gender(UNKNOWN);
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (gender == null) {
			warnings.add(new Warning(8));
		}
	}
}
