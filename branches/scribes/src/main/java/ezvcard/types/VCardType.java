package ezvcard.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;

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
 * Represents a vCard key/value pair entry (called a "type" or "property").
 * @author Michael Angstadt
 */
public abstract class VCardType implements Comparable<VCardType> {
	/**
	 * The group that this type belongs to or null if it doesn't belong to a
	 * group.
	 */
	protected String group;

	/**
	 * The list of attributes that are associated with this type (called
	 * "sub types" or "parameters").
	 */
	protected VCardSubTypes subTypes = new VCardSubTypes();

	/**
	 * Gets the vCard versions that support this property.
	 * @return the vCard versions that support this property.
	 */
	public final Set<VCardVersion> getSupportedVersions() {
		return _supportedVersions();
	}

	/**
	 * <p>
	 * Gets the vCard versions that support this property.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes if the property does
	 * not support all vCard versions. The default implementation of this method
	 * returns all vCard versions.
	 * </p>
	 * @return the vCard versions that support this type.
	 */
	protected Set<VCardVersion> _supportedVersions() {
		return EnumSet.copyOf(Arrays.asList(VCardVersion.values()));
	}

	/**
	 * Checks the property for data consistency problems or deviations from the
	 * spec. These problems will not prevent the property from being written to
	 * a data stream, but may prevent it from being parsed correctly by the
	 * consuming application. These problems can largely be avoided by reading
	 * the Javadocs of the property class, or by being familiar with the vCard
	 * standard.
	 * @param version the version to check the property against (use 4.0 for
	 * xCard and jCard)
	 * @param vcard the vCard this property belongs to
	 * @see VCard#validate
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public final List<String> validate(VCardVersion version, VCard vcard) {
		List<String> warnings = new ArrayList<String>(0);

		//check the supported versions
		Set<VCardVersion> supportedVersions = getSupportedVersions();
		if (!supportedVersions.contains(version)) {
			warnings.add("Property is not supported by version " + version.getVersion() + ".  Supported versions are: " + supportedVersions);
		}

		//check parameters
		warnings.addAll(subTypes.validate(version));

		_validate(warnings, version, vcard);

		return warnings;
	}

	/**
	 * Checks the property for data consistency problems or deviations from the
	 * spec. Meant to be overridden by child classes that wish to provide
	 * validation logic.
	 * @param warnings the list to add the warnings to
	 * @param version the version to check the property against
	 * @param vcard the vCard this property belongs to
	 */
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		//empty
	}

	/**
	 * Gets all of the property's parameters.
	 * @return the property's parameters
	 */
	public VCardSubTypes getSubTypes() {
		return subTypes;
	}

	/**
	 * Sets the property's parameters.
	 * @param subTypes the parameters
	 */
	public void setSubTypes(VCardSubTypes subTypes) {
		this.subTypes = subTypes;
	}

	/**
	 * Gets the first value of a parameter.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @return the parameter value or null if not found
	 */
	public String getSubType(String name) {
		return subTypes.first(name);
	}

	/**
	 * Gets all values of a parameter.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @return the parameter values
	 */
	public List<String> getSubTypes(String name) {
		return subTypes.get(name);
	}

	/**
	 * Replaces all existing values of a parameter with the given value.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param value the parameter value
	 */
	public void setSubType(String name, String value) {
		subTypes.replace(name, value);
	}

	/**
	 * Adds a value to a parameter.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param value the parameter value
	 */
	public void addSubType(String name, String value) {
		subTypes.put(name, value);
	}

	/**
	 * Removes a parameter from the property.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 */
	public void removeSubType(String name) {
		subTypes.removeAll(name);
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
	 * Sorts by PREF parameter ascending. Types that do not have a PREF
	 * parameter are pushed to the end of the list.
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

	//Note: The following parameter helper methods are package-scoped to prevent them from cluttering up the Javadocs

	/**
	 * <p>
	 * Gets all PID values.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the PID values or empty set if there are none
	 * @see VCardSubTypes#getPids
	 */
	List<Integer[]> getPids() {
		return subTypes.getPids();
	}

	/**
	 * <p>
	 * Adds a PID value.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param localId the local ID
	 * @param clientPidMapRef the ID used to reference the property's globally
	 * unique identifier in the CLIENTPIDMAP property.
	 * @see VCardSubTypes#addPid(int, int)
	 */
	void addPid(int localId, int clientPidMapRef) {
		subTypes.addPid(localId, clientPidMapRef);
	}

	/**
	 * <p>
	 * Removes all PID values.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @see VCardSubTypes#removePids
	 */
	void removePids() {
		subTypes.removePids();
	}

	/**
	 * <p>
	 * Gets the preference value. The lower the number, the more preferred this
	 * property instance is compared with other properties in the same vCard of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the preference value or null if it doesn't exist
	 * @see VCardSubTypes#getPref
	 */
	Integer getPref() {
		return subTypes.getPref();
	}

	/**
	 * <p>
	 * Sets the preference value. The lower the number, the more preferred this
	 * property instance is compared with other properties in the same vCard of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see VCardSubTypes#setPref
	 */
	void setPref(Integer pref) {
		subTypes.setPref(pref);
	}

	/**
	 * Gets the language that the property value is written in.
	 * @return the language or null if not set
	 * @see VCardSubTypes#getLanguage
	 */
	String getLanguage() {
		return subTypes.getLanguage();
	}

	/**
	 * Sets the language that the property value is written in.
	 * @param language the language or null to remove
	 * @see VCardSubTypes#setLanguage
	 */
	void setLanguage(String language) {
		subTypes.setLanguage(language);
	}

	/**
	 * Gets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list and properties with high
	 * index values are put at the end of the list.
	 * @return the index or null if not set
	 * @see VCardSubTypes#setIndex
	 */
	Integer getIndex() {
		return subTypes.getIndex();
	}

	/**
	 * Sets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list and properties with high
	 * index values are put at the end of the list.
	 * @param index the index or null to remove
	 * @see VCardSubTypes#setIndex
	 */
	void setIndex(Integer index) {
		subTypes.setIndex(index);
	}
}