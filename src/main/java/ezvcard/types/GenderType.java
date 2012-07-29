package ezvcard.types;

import java.util.List;

import ezvcard.VCardException;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.VCardStringUtils;

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
 * Represents the GENDER type.
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
	 * @param gender the gender value
	 */
	public GenderType(String gender) {
		super(NAME);
		this.gender = gender;
	}

	/**
	 * Gets the free-form text.
	 * @return the additional text or null if there is no text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the free-form text. This is added alongside the gender value.
	 * @param text free-form text
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

	public boolean isMale() {
		return MALE.equals(gender);
	}

	public boolean isFemale() {
		return FEMALE.equals(gender);
	}

	public boolean isOther() {
		return OTHER.equals(gender);
	}

	public boolean isNone() {
		return NONE.equals(gender);
	}

	public boolean isUnknown() {
		return UNKNOWN.equals(gender);
	}

	public static GenderType male() {
		return new GenderType(MALE);
	}

	public static GenderType female() {
		return new GenderType(FEMALE);
	}

	public static GenderType other() {
		return new GenderType(OTHER);
	}

	public static GenderType none() {
		return new GenderType(NONE);
	}

	public static GenderType unknown() {
		return new GenderType(UNKNOWN);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		StringBuilder sb = new StringBuilder();

		if (gender != null) {
			sb.append(gender);
		}
		if (text != null) {
			sb.append(';');
			sb.append(VCardStringUtils.escapeText(text));
		}

		return sb.toString();
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		String split[] = value.split(";", 2);
		setGender(split[0].toUpperCase());
		if (split.length > 1) {
			setText(VCardStringUtils.unescape(split[1]));
		}
	}
}
