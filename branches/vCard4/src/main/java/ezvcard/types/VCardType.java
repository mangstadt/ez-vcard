package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;

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
 * Represents a vCard key/value pair entry (called a "Type").
 * @author Michael Angstadt
 */
public abstract class VCardType implements Comparable<VCardType> {
	/**
	 * The name of the Type.
	 */
	protected final String typeName;

	/**
	 * The group that this type belongs to or null if it doesn't belong to a
	 * group.
	 */
	protected String group;

	/**
	 * The list of name/value pairs that are associated with this Type (called
	 * "Sub Types").
	 */
	protected VCardSubTypes subTypes = new VCardSubTypes();

	/**
	 * @param typeName the type name (e.g. "ADR")
	 */
	public VCardType(String typeName) {
		this.typeName = typeName;

	}

	/**
	 * Gets the name of this Type.
	 * @return the Type name
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Gets the vCard versions that support this type.
	 * @return the vCard versions that support this type.
	 */
	public VCardVersion[] getSupportedVersions() {
		return VCardVersion.values();
	}

	/**
	 * Converts this Type object to a string for sending over the wire. This
	 * method is responsible for escaping all the necessary characters (such as
	 * commas and semi-colons). It is NOT responsible for folding.
	 * @param version the version vCard that is being generated
	 * @param warnings if you want to alert the user to any possible problems,
	 * add the warnings to this list
	 * @param compatibilityMode allows you to customize the marshalling process
	 * depending on the expected consumer of the vCard
	 * @return the string for sending over the wire or null to NOT marshal the
	 * type
	 * @throws VCardException if there's a problem marshalling the value
	 */
	public final String marshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		return doMarshalValue(version, warnings, compatibilityMode);
	}

	/**
	 * Converts this Type object to a string for sending over the wire. This
	 * method is responsible for escaping all the necessary characters (such as
	 * commas and semi-colons). It is NOT responsible for folding.
	 * @param version the version vCard that is being generated
	 * @param warnings if you want to alert the user to any possible problems,
	 * add the warnings to this list
	 * @param compatibilityMode allows you to customize the marshalling process
	 * depending on the expected consumer of the vCard
	 * @return the string for sending over the wire or null to NOT marshal the
	 * type
	 * @throws VCardException if there's a problem marshalling the value
	 */
	protected abstract String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException;

	/**
	 * Gets the Sub Types to send over the wire.
	 * @param version the version vCard that is being generated
	 * @param warnings if you want to alert the user to any possible problems,
	 * add the warnings to this list
	 * @param compatibilityMode allows you to customize the marshalling process
	 * depending on the expected consumer of the vCard
	 * @param vcard the vCard that is being marshalled
	 * @return the sub types for sending over the wire
	 * @throws VCardException if there's a problem marshalling a sub type
	 */
	public final VCardSubTypes marshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) throws VCardException {
		return doMarshalSubTypes(version, warnings, compatibilityMode, vcard);
	}

	/**
	 * Gets the Sub Types to send over the wire. Child classes should override
	 * this method if the sub types need to be modified in any way. Child
	 * classes may also choose to return a <i>copy</i> of the object's
	 * {@link VCardSubTypes} object if they wish to preserve the original data.
	 * @param version the version vCard that is being generated
	 * @param warnings if you want to alert the user to any possible problems,
	 * add the warnings to this list
	 * @param compatibilityMode allows you to customize the marshalling process
	 * depending on the expected consumer of the vCard
	 * @param vcard the vCard that is being marshalled
	 * @return the sub types for sending over the wire
	 * @throws VCardException if there's a problem marshalling a sub type
	 */
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) throws VCardException {
		return subTypes;
	}

	/**
	 * Unmarshals the Type value from off the wire.
	 * @param subTypes the Sub Types that were parsed
	 * @param value the unfolded value from off the wire. If the wire value is
	 * in the "quoted-printable" encoding, it will be decoded.
	 * @param version the version of the vCard that is being read or null if the
	 * VERSION type has not been parsed yet (v3.0 and v4.0 require that the
	 * VERSION type come right after the BEGIN type, but v2.1 allows it to be
	 * anywhere)
	 * @param warnings if you want to alert the user to any possible problems,
	 * add the warnings to this list
	 * @param compatibilityMode allows you to customize the unmarshalling
	 * process depending on where the vCard came from
	 * @throws VCardException if there's a problem unmarshalling the value
	 */
	public final void unmarshalValue(VCardSubTypes subTypes, String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		this.subTypes = subTypes;
		doUnmarshalValue(value, version, warnings, compatibilityMode);
	}

	/**
	 * Unmarshals the Type value from off the wire.
	 * @param value the unfolded value from off the wire. If the wire value is
	 * in the "quoted-printable" encoding, it will be decoded.
	 * @param version the version of the vCard that is being read or null if the
	 * VERSION type has not been parsed yet (v3.0 and v4.0 require that the
	 * VERSION type come right after the BEGIN type, but v2.1 allows it to be
	 * anywhere)
	 * @param warnings if you want to alert the user to any possible problems,
	 * add the warnings to this list
	 * @param compatibilityMode allows you to customize the unmarshalling
	 * process depending on where the vCard came from
	 * @throws VCardException if there's a problem unmarshalling the value
	 */
	protected abstract void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException;

	/**
	 * Gets all Sub Types associated with this Type. This method can be used to
	 * retrieve any extended, standard, or non-standard Sub Type. It should not
	 * be used for standard Sub Types--the Type's class <i>should</i> contain
	 * getter methods for the standard/expected Sub Types (e.g.
	 * {@link NoteType#getLanguage()} retrieves a note's "LANGUAGE" Sub Type).
	 * @return all of the Type's Sub Types
	 */
	public VCardSubTypes getSubTypes() {
		return subTypes;
	}

	/**
	 * Gets this type's group.
	 * @return the group or null if it does not belong to a group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets this type's group.
	 * @param group the group or null to remove the type's group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Sorts a list of types by PREF parameter (if present).
	 */
	public int compareTo(VCardType that) {
		Integer pref0 = this.getSubTypes().getPref();
		Integer pref1 = that.getSubTypes().getPref();
		if (pref0 == null && pref1 == null) {
			return 0;
		}
		if (pref0 == null) {
			return 1;
		}
		if (pref1 == null) {
			return -1;
		}
		return pref1.compareTo(pref0);
	}
}