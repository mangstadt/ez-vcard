package ezvcard.property;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.validate.AllowedCharacters;
import com.github.mangstadt.vinnie.validate.VObjectValidator;

import ezvcard.Messages;
import ezvcard.SupportedVersions;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;
import ezvcard.parameter.Pid;
import ezvcard.parameter.VCardParameters;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
	public final List<ValidationWarning> validate(VCardVersion version, VCard vcard) {
		List<ValidationWarning> warnings = new ArrayList<ValidationWarning>(0);

		//check the supported versions
		if (!isSupportedBy(version)) {
			warnings.add(new ValidationWarning(2, Arrays.toString(getSupportedVersions())));
		}

		//check parameters
		warnings.addAll(parameters.validate(version));

		//check group
		if (group != null) {
			SyntaxStyle syntax = version.getSyntaxStyle();
			AllowedCharacters allowed = VObjectValidator.allowedCharactersGroup(syntax, true);
			if (!allowed.check(group)) {
				if (syntax == SyntaxStyle.OLD) {
					AllowedCharacters notAllowed = allowed.flip();
					warnings.add(new ValidationWarning(32, group, notAllowed.toString(true)));
				} else {
					warnings.add(new ValidationWarning(23, group));
				}
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
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
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
	 * @return the parameter values (this list is immutable)
	 */
	public List<String> getParameters(String name) {
		return Collections.unmodifiableList(parameters.get(name));
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
	 * Gets the list that stores this property's PID (property ID) parameter
	 * values.
	 * </p>
	 * <p>
	 * PIDs can exist on any property where multiple instances are allowed (such
	 * as {@link Email} or {@link Address}, but not {@link StructuredName}
	 * because only 1 instance of this property is allowed per vCard).
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
	 * @return the PID parameter values (this list is mutable)
	 * @throws IllegalStateException if one or more parameter values cannot be
	 * parsed as PIDs. If this happens, you may use the
	 * {@link #getParameters(String)} method to retrieve the raw values.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-19">RFC 6350
	 * p.19</a>
	 */
	List<Pid> getPids() {
		return parameters.getPids();
	}

	/**
	 * <p>
	 * Gets this property's preference value. The lower this number is, the more
	 * "preferred" the property instance is compared with other properties of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * <p>
	 * In the vCard below, the {@link Address} on the second row is the most
	 * preferred because it has the lowest PREF value.
	 * </p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:;;1600 Amphitheatre Parkway;Mountain View;CA;94043
	 * ADR;TYPE=work;PREF=1:;;One Microsoft Way;Redmond;WA;98052
	 * ADR;TYPE=home:;;123 Maple St;Hometown;KS;12345
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the preference value or null if not set
	 * @throws IllegalStateException if the parameter value cannot be parsed as
	 * an integer. If this happens, you may use the
	 * {@link #getParameter(String)} method to retrieve its raw value.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-17">RFC 6350
	 * p.17</a>
	 */
	Integer getPref() {
		return parameters.getPref();
	}

	/**
	 * <p>
	 * Sets this property's preference value. The lower this number is, the more
	 * "preferred" the property instance is compared with other properties of
	 * the same type. If a property doesn't have a preference value, then it is
	 * considered the least preferred.
	 * </p>
	 * <p>
	 * In the vCard below, the {@link Address} on the second row is the most
	 * preferred because it has the lowest PREF value.
	 * </p>
	 * 
	 * <pre>
	 * ADR;TYPE=work;PREF=2:;;1600 Amphitheatre Parkway;Mountain View;CA;94043
	 * ADR;TYPE=work;PREF=1:;;One Microsoft Way;Redmond;WA;98052
	 * ADR;TYPE=home:;;123 Maple St;Hometown;KS;12345
	 * </pre>
	 * 
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-17">RFC 6350
	 * p.17</a>
	 */
	void setPref(Integer pref) {
		parameters.setPref(pref);
	}

	/**
	 * Gets the language that the property value is written in.
	 * @return the language or null if not set
	 */
	String getLanguage() {
		return parameters.getLanguage();
	}

	/**
	 * Sets the language that the property value is written in.
	 * @param language the language or null to remove
	 */
	void setLanguage(String language) {
		parameters.setLanguage(language);
	}

	/**
	 * <p>
	 * Gets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list. Properties with high index
	 * values are put at the end of the list.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the index or null if not set
	 * @throws IllegalStateException if the parameter value cannot be parsed as
	 * an integer. If this happens, you may use the
	 * {@link #getParameter(String)} method to retrieve its raw value.
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-7">RFC 6715
	 * p.7</a>
	 */
	Integer getIndex() {
		return parameters.getIndex();
	}

	/**
	 * <p>
	 * Sets the sorted position of this property when it is grouped together
	 * with other properties of the same type. Properties with low index values
	 * are put at the beginning of the sorted list. Properties with high index
	 * values are put at the end of the list.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param index the index or null to remove
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed. If this happens, you may use the
	 * {@link #getParameter(String)} method to retrieve its raw value.
	 * @see <a href="https://tools.ietf.org/html/rfc6715#page-7">RFC 6715
	 * p.7</a>
	 */
	void setIndex(Integer index) {
		parameters.setIndex(index);
	}
}