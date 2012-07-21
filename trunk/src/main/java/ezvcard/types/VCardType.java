package ezvcard.types;

import java.util.List;

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
public abstract class VCardType {
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
	 * Converts this Type object to a string for sending over the wire. This
	 * method is responsible for escaping all the necessary characters (such as
	 * commas and semi-colons), as well as setting any sub types. It is NOT
	 * responsible for folding.
	 * @param version the version vCard that is being generated
	 * @param warnings if you want to alert the user to any possible problems,
	 * add the warnings to this list
	 * @param compatibilityMode allows you to customize the marshalling process
	 * depending on the expected consumer of the vCard
	 * @return the string for sending over the wire or null to NOT marshal the
	 * type
	 * @throws VCardException if there's a problem marshalling the value
	 */
	public String marshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		return doMarshalValue(version, warnings, compatibilityMode);
	}

	/**
	 * Converts this Type object to a string for sending over the wire. This
	 * method is responsible for escaping all the necessary characters (such as
	 * commas and semi-colons), as well as setting any sub types. It is NOT
	 * responsible for folding.
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
	public void unmarshalValue(VCardSubTypes subTypes, String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
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
	 * retrieve any custom, standard, or non-standard Sub Type. It should not be
	 * used for standard Sub Types--the Type's class should contain getter
	 * methods for the standard/expected Sub Types (e.g.
	 * {@link AddressType#getTypes()} retrieves all of the address's "TYPE" Sub
	 * Types).
	 * @return all of the Type's Sub Types
	 */
	public VCardSubTypes getSubTypes() {
		return subTypes;
	}

	/**
	 * Gets this type's group.
	 * @return the group of null if does not belong to a group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets this type's group.
	 * @param group the group or null to remove the group
	 */
	public void setGroup(String group) {
		this.group = group;
	}
}