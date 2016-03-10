package ezvcard.property;

import java.lang.reflect.Constructor;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ezvcard.Messages;
import ezvcard.SupportedVersions;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.parameter.Pid;
import ezvcard.parameter.VCardParameter;
import ezvcard.parameter.VCardParameters;
import ezvcard.util.CharacterBitSet;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 * Base class for all vCard property classes.
 * @author Michael Angstadt
 */
public abstract class VCardProperty implements Comparable<VCardProperty> {
	/**
	 * The group that this property belongs to or null if it doesn't belong to a
	 * group.
	 */
	protected String group;

	/**
	 * The property's parameters.
	 */
	protected VCardParameters parameters;

	public VCardProperty() {
		parameters = new VCardParameters();
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	protected VCardProperty(VCardProperty original) {
		group = original.group;
		parameters = new VCardParameters(original.parameters);
	}

	/**
	 * <p>
	 * Gets the vCard versions that support this property.
	 * </p>
	 * <p>
	 * The supported versions are defined by assigning a
	 * {@link SupportedVersions @SupportedVersions} annotation to the property
	 * class. Property classes without this annotation are considered to be
	 * supported by all versions.
	 * </p>
	 * @return the vCard versions that support this property.
	 */
	public final VCardVersion[] getSupportedVersions() {
		SupportedVersions supportedVersionsAnnotation = getClass().getAnnotation(SupportedVersions.class);
		return (supportedVersionsAnnotation == null) ? VCardVersion.values() : supportedVersionsAnnotation.value();
	}

	/**
	 * <p>
	 * Determines if this property is supported by the given vCard version.
	 * </p>
	 * <p>
	 * The supported versions are defined by assigning a
	 * {@link SupportedVersions} annotation to the property class. Property
	 * classes without this annotation are considered to be supported by all
	 * versions.
	 * </p>
	 * @param version the vCard version
	 * @return true if it is supported, false if not
	 */
	public final boolean isSupportedBy(VCardVersion version) {
		VCardVersion supportedVersions[] = getSupportedVersions();
		for (VCardVersion supportedVersion : supportedVersions) {
			if (supportedVersion == version) {
				return true;
			}
		}
		return false;
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
	public final List<Warning> validate(VCardVersion version, VCard vcard) {
		List<Warning> warnings = new ArrayList<Warning>(0);

		//check the supported versions
		if (!isSupportedBy(version)) {
			warnings.add(new Warning(2, Arrays.toString(getSupportedVersions())));
		}

		//check parameters
		warnings.addAll(parameters.validate(version));

		//check group
		if (group != null) {
			CharacterBitSet validCharacters = new CharacterBitSet("-a-zA-Z0-9");
			if (!validCharacters.containsOnly(group)) {
				warnings.add(new Warning(23, group));
			}
		}

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
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		//empty
	}

	/**
	 * Gets all of the property's parameters.
	 * @return the property's parameters
	 */
	public VCardParameters getParameters() {
		return parameters;
	}

	/**
	 * Sets the property's parameters.
	 * @param parameters the parameters (cannot be null)
	 */
	public void setParameters(VCardParameters parameters) {
		if (parameters == null) {
			throw new NullPointerException(Messages.INSTANCE.getExceptionMessage(42));
		}
		this.parameters = parameters;
	}

	/**
	 * Gets the first value of a parameter.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @return the parameter value or null if not found
	 */
	public String getParameter(String name) {
		return parameters.first(name);
	}

	/**
	 * Gets all values of a parameter.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @return the parameter values
	 */
	public List<String> getParameters(String name) {
		return parameters.get(name);
	}

	/**
	 * Replaces all existing values of a parameter with the given value.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param value the parameter value
	 */
	public void setParameter(String name, String value) {
		parameters.replace(name, value);
	}

	/**
	 * Adds a value to a parameter.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param value the parameter value
	 */
	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	/**
	 * Removes a parameter from the property.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 */
	public void removeParameter(String name) {
		parameters.removeAll(name);
	}

	/**
	 * Gets this property's group.
	 * @return the group or null if it does not belong to a group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets this property's group.
	 * @param group the group or null to remove the property's group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Sorts by PREF parameter ascending. Properties that do not have a PREF
	 * parameter are pushed to the end of the list.
	 */
	public int compareTo(VCardProperty that) {
		Integer pref0 = this.getParameters().getPref();
		Integer pref1 = that.getParameters().getPref();
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

	/**
	 * <p>
	 * Gets string representations of the class's fields for the
	 * {@link #toString} method.
	 * </p>
	 * <p>
	 * Meant to be overridden by child classes. The default implementation
	 * returns an empty map.
	 * </p>
	 * @return the values of the class's fields (key = field name, value = field
	 * value)
	 */
	protected Map<String, Object> toStringValues() {
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		sb.append(" [ group=").append(group);
		sb.append(" | parameters=").append(parameters);
		for (Map.Entry<String, Object> field : toStringValues().entrySet()) {
			String fieldName = field.getKey();
			Object fieldValue = field.getValue();
			sb.append(" | ").append(fieldName).append('=').append(fieldValue);
		}
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * <p>
	 * Creates a copy of this property object.
	 * </p>
	 * <p>
	 * The default implementation of this method uses reflection to look for a
	 * copy constructor. Child classes SHOULD override this method to avoid the
	 * performance overhead involved in using reflection.
	 * </p>
	 * <p>
	 * The child class's copy constructor, if present, MUST invoke the
	 * {@link #VCardProperty(VCardProperty)} super constructor to ensure that
	 * the group name and parameters are also copied.
	 * </p>
	 * <p>
	 * This method MUST be overridden by the child class if the child class does
	 * not have a copy constructor. Otherwise, an
	 * {@link UnsupportedOperationException} will be thrown when an attempt is
	 * made to copy the property (such as in the {@link VCard#VCard(VCard) VCard
	 * class's copy constructor}).
	 * </p>
	 * @return the copy
	 * @throws UnsupportedOperationException if the class does not have a copy
	 * constructor or there is a problem invoking it
	 */
	public VCardProperty copy() {
		Class<? extends VCardProperty> clazz = getClass();

		try {
			Constructor<? extends VCardProperty> copyConstructor = clazz.getConstructor(clazz);
			return copyConstructor.newInstance(this);
		} catch (Exception e) {
			throw new UnsupportedOperationException(Messages.INSTANCE.getExceptionMessage(31, clazz.getName()), e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.toLowerCase().hashCode());
		result = prime * result + parameters.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VCardProperty other = (VCardProperty) obj;
		if (group == null) {
			if (other.group != null) return false;
		} else if (!group.equalsIgnoreCase(other.group)) return false;
		if (!parameters.equals(other.parameters)) return false;
		return true;
	}

	/*
	 * Note: The following parameter helper methods are package-scoped so they
	 * don't clutter up the Javadocs for the VCardProperty class. They are
	 * defined here instead of in the child classes that use them, so that their
	 * Javadocs don't have to be repeated.
	 */

	/**
	 * <p>
	 * Gets the list that stores this property's PID parameter values. PIDs can
	 * exist on any property where multiple instances are allowed (such as
	 * {@link Email} or {@link Address}, but not {@link StructuredName} because
	 * only 1 instance of this property is allowed per vCard).
	 * </p>
	 * <p>
	 * When used in conjunction with the {@link ClientPidMap} property, it
	 * allows an individual property instance to be uniquely identifiable. This
	 * feature is made use of when two different versions of the same vCard have
	 * to be merged together (called "synchronizing").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the PID parameter values
	 * @see <a href="http://tools.ietf.org/html/rfc6350#section-5.5">RFC 6350
	 * Section 5.5</a>
	 */
	List<Pid> getPids() {
		if (pidParameterList == null) {
			pidParameterList = new PidParameterList();
		}
		return pidParameterList;
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
	 * @see VCardParameters#getPref
	 */
	Integer getPref() {
		return parameters.getPref();
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
	 * @see VCardParameters#setPref
	 */
	void setPref(Integer pref) {
		parameters.setPref(pref);
	}

	/**
	 * Gets the language that the property value is written in.
	 * @return the language or null if not set
	 * @see VCardParameters#getLanguage
	 */
	String getLanguage() {
		return parameters.getLanguage();
	}

	/**
	 * Sets the language that the property value is written in.
	 * @param language the language or null to remove
	 * @see VCardParameters#setLanguage
	 */
	void setLanguage(String language) {
		parameters.setLanguage(language);
	}

	/**
	 * Gets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list and properties with high
	 * index values are put at the end of the list.
	 * @return the index or null if not set
	 * @see VCardParameters#setIndex
	 */
	Integer getIndex() {
		return parameters.getIndex();
	}

	/**
	 * Sets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list and properties with high
	 * index values are put at the end of the list.
	 * @param index the index or null to remove
	 * @see VCardParameters#setIndex
	 */
	void setIndex(Integer index) {
		parameters.setIndex(index);
	}

	private PidParameterList pidParameterList;

	protected class PidParameterList extends VCardParameterList<Pid> {
		public PidParameterList() {
			super(VCardParameters.PID);
		}

		@Override
		protected String _asString(Pid value) {
			Integer localId = value.getLocalId();
			Integer clientPidMapReference = value.getClientPidMapReference();
			return (clientPidMapReference == null) ? Integer.toString(localId) : localId + "." + clientPidMapReference;
		}

		@Override
		protected Pid _asObject(String value) {
			int dot = value.indexOf('.');
			String localIdStr, clientPidMapReferenceStr;
			if (dot < 0) {
				localIdStr = value;
				clientPidMapReferenceStr = null;
			} else {
				localIdStr = value.substring(0, dot);
				clientPidMapReferenceStr = (dot == value.length() - 1) ? null : value.substring(dot + 1);
			}

			Integer localId = Integer.valueOf(localIdStr);
			Integer clientPidMapReference = (clientPidMapReferenceStr == null) ? null : Integer.valueOf(clientPidMapReferenceStr);
			return new Pid(localId, clientPidMapReference);
		}
	};

	/**
	 * <p>
	 * A list that holds the values of a particular parameter. It automatically
	 * converts parameter value Strings from the property's
	 * {@link VCardParameters} object to the appropriate {@link VCardParameter}
	 * object that some parameters use.
	 * </p>
	 * <p>
	 * The list is backed by the property's {@link VCardParameters} object, so
	 * any changes made to the list will affect the property's
	 * {@link VCardParameters} object and vice versa.
	 * </p>
	 * @param <T> the {@link VCardParameter} class
	 */
	protected abstract class TypeParameterEnumList<T extends VCardParameter> extends VCardParameterList<T> {
		public TypeParameterEnumList() {
			super(VCardParameters.TYPE);
		}

		@Override
		protected String _asString(T value) {
			return value.getValue();
		}
	}

	/**
	 * <p>
	 * A list that holds the values of a particular parameter.
	 * </p>
	 * <p>
	 * This list is backed by the property's {@link VCardParameters} object. Any
	 * changes made to the list will affect the property's
	 * {@link VCardParameters} object and vice versa.
	 * </p>
	 */
	protected abstract class VCardParameterList<T> extends AbstractList<T> {
		protected final String parameterName;

		/**
		 * Creates a new list.
		 * @param parameterName the name of the parameter
		 */
		public VCardParameterList(String parameterName) {
			this.parameterName = parameterName;
		}

		@Override
		public void add(int index, T value) {
			String valueStr = _asString(value);

			/*
			 * Note: If a property name does not exist, then the parameters
			 * object will return an empty list. Any objects added to this empty
			 * list will NOT be added to the parameters object.
			 */
			List<String> values = values();
			if (values.isEmpty()) {
				parameters.put(parameterName, valueStr);
			} else {
				values.add(index, valueStr);
			}
		}

		@Override
		public T remove(int index) {
			String removed = values().remove(index);
			return asObject(removed);
		}

		@Override
		public T get(int index) {
			String value = values().get(index);
			return asObject(value);
		}

		@Override
		public T set(int index, T value) {
			String valueStr = _asString(value);
			String replaced = values().set(index, valueStr);
			return asObject(replaced);
		}

		@Override
		public int size() {
			return values().size();
		}

		private T asObject(String value) {
			try {
				return _asObject(value);
			} catch (Exception e) {
				throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(15, parameterName), e);
			}
		}

		/**
		 * Converts the object to a String value for storing in the
		 * {@link VCardParameters} object.
		 * @param value the value
		 * @return the string value
		 */
		protected abstract String _asString(T value);

		/**
		 * Converts a String value to its object form.
		 * @param value the string value
		 * @return the object
		 * @throws Exception if there is a problem parsing the string
		 */
		protected abstract T _asObject(String value) throws Exception;

		private List<String> values() {
			return parameters.get(parameterName);
		}
	}
}