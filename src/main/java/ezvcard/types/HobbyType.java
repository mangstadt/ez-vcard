package ezvcard.types;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.HobbyLevelParameter;

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
 * Defines a recreational activity that the person actively engages in. For
 * example, if a person has a HOBBY of "hockey", it would mean that he likes to
 * play hockey. Someone who just likes to <i>watch</i> hockey would list
 * "hockey" as an INTEREST (see {@link InterestType}) instead.
 * 
 * <pre>
 * //someone who likes to wind surf during the summer, but rents a boat instead of owning one
 * VCard vcard = new VCard();
 * HobbyType hobby = new HobbyType(&quot;wind surfing&quot;);
 * hobby.setLevel(HobbyLevelParameter.LOW);
 * vcard.addHobby(hobby);
 * </pre>
 * 
 * <p>
 * vCard property name: HOBBY
 * </p>
 * <p>
 * vCard versions: 4.0
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
 */
public class HobbyType extends TextType implements HasAltId {
	public static final String NAME = "HOBBY";

	public HobbyType() {
		super(NAME);
	}

	/**
	 * @param hobby the hobby (e.g. "wind surfing")
	 */
	public HobbyType(String hobby) {
		super(NAME, hobby);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	/**
	 * Gets the level at which the person practices the hobby.
	 * @return the skill level (e.g. "low") or null if not set
	 * @see VCardSubTypes#getLevel
	 */
	public HobbyLevelParameter getLevel() {
		String value = subTypes.getLevel();
		if (value == null) {
			return null;
		}
		HobbyLevelParameter p = HobbyLevelParameter.valueOf(value);
		if (p == null) {
			p = new HobbyLevelParameter(value);
		}
		return p;
	}

	/**
	 * Sets the level at which the person practices the hobby.
	 * @param level the level (e.g. "low") or null to remove
	 * @see VCardSubTypes#setLevel
	 */
	public void setLevel(HobbyLevelParameter level) {
		subTypes.setLevel(level.getValue());
	}

	/**
	 * Gets the INDEX parameter.
	 * @return the INDEX or null if not set
	 * @see VCardSubTypes#getIndex
	 */
	public Integer getIndex() {
		return subTypes.getIndex();
	}

	/**
	 * Sets the INDEX parameter.
	 * @param index the INDEX or null to remove
	 * @see VCardSubTypes#setIndex
	 */
	public void setIndex(Integer index) {
		subTypes.setIndex(index);
	}

	/**
	 * Gets the TYPE parameter.
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return subTypes.getType();
	}

	/**
	 * Sets the TYPE parameter.
	 * @param type the TYPE value (this should be either "work" or "home") or
	 * null to remove
	 */
	public void setType(String type) {
		subTypes.setType(type);
	}

	/**
	 * Gets the language that the skill description is written in.
	 * @return the language or null if not set
	 * @see VCardSubTypes#getLanguage
	 */
	public String getLanguage() {
		return subTypes.getLanguage();
	}

	/**
	 * Sets the language that the skill description is written in.
	 * @param language the language or null to remove
	 * @see VCardSubTypes#setLanguage
	 */
	public void setLanguage(String language) {
		subTypes.setLanguage(language);
	}

	/**
	 * Gets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the preference value or null if it doesn't exist
	 * @see VCardSubTypes#getPref
	 */
	public Integer getPref() {
		return subTypes.getPref();
	}

	/**
	 * Sets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see VCardSubTypes#setPref
	 */
	public void setPref(Integer pref) {
		subTypes.setPref(pref);
	}

	//@Override
	public String getAltId() {
		return subTypes.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}
}
