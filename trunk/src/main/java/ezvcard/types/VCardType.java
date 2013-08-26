package ezvcard.types;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.VCardStringUtils.JoinCallback;
import ezvcard.util.XCardElement;

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
	 * The name of the type.
	 */
	protected final String typeName;

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
	 * Creates a vCard property.
	 * @param typeName the type name (e.g. "ADR")
	 */
	public VCardType(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * Gets the name of this type.
	 * @return the type name (e.g. "ADR")
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
	 * Converts this type object to a string for sending over the wire. It is
	 * NOT responsible for folding.
	 * @param version the version vCard that is being generated
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * marshalling process depending on the expected consumer of the vCard
	 * @return the string for sending over the wire
	 * @throws SkipMeException if this type should NOT be marshalled into the
	 * vCard
	 * @throws EmbeddedVCardException if the value of this type is an embedded
	 * vCard (i.e. the AGENT type)
	 */
	public final String marshalText(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder sb = new StringBuilder();
		doMarshalText(sb, version, warnings, compatibilityMode);
		return sb.toString();
	}

	/**
	 * Converts this type object to a string for sending over the wire. It is
	 * NOT responsible for folding.
	 * @param value the buffer to add the marshalled value to
	 * @param version the version vCard that is being generated
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * marshalling process depending on the expected consumer of the vCard
	 * @throws SkipMeException if this type should NOT be marshalled into the
	 * vCard
	 * @throws EmbeddedVCardException if the value of this type is an embedded
	 * vCard (i.e. the AGENT type)
	 */
	protected abstract void doMarshalText(StringBuilder value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode);

	/**
	 * Marshals this type for inclusion in an xCard (XML document).
	 * @param parent the XML element that the type's value will be inserted
	 * into. For example, this would be the "&lt;fn&gt;" element for the "FN"
	 * type.
	 * @param version the version vCard that is being generated
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * marshalling process depending on the expected consumer of the vCard
	 * @throws SkipMeException if this type should NOT be marshalled into the
	 * vCard
	 */
	public final void marshalXml(Element parent, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		XCardElement wrapper = new XCardElement(parent, version);
		doMarshalXml(wrapper, warnings, compatibilityMode);
	}

	/**
	 * Marshals this type for inclusion in an xCard (XML document). All child
	 * classes SHOULD override this, but are not required to.
	 * @param parent the XML element that the type's value will be inserted
	 * into. For example, this would be the "&lt;fn&gt;" element for the "FN"
	 * type.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * marshalling process depending on the expected consumer of the vCard
	 * @throws SkipMeException if this type should NOT be marshalled into the
	 * vCard
	 */
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = marshalText(parent.version(), warnings, compatibilityMode);
		parent.append("unknown", value);
	}

	/**
	 * Marshals this type for inclusion in a jCard (JSON document).
	 * @param version the version vCard that is being generated
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @return the marshalled jCard value
	 * @throws SkipMeException if this type should NOT be marshalled into the
	 * vCard
	 */
	public final JCardValue marshalJson(VCardVersion version, List<String> warnings) {
		return doMarshalJson(version, warnings);
	}

	/**
	 * Marshals this type for inclusion in a jCard (JSON document).
	 * @param version the version vCard that is being generated
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @return the marshalled jCard value
	 * @throws SkipMeException if this type should NOT be marshalled into the
	 * vCard
	 */
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		String valueStr = marshalText(version, warnings, CompatibilityMode.RFC);

		//determine the data type based on the VALUE parameter
		ValueParameter valueParam = subTypes.getValue();
		JCardDataType dataType = (valueParam == null) ? null : JCardDataType.get(valueParam.getValue());
		return JCardValue.single(dataType, valueStr);
	}

	/**
	 * Gets the Sub Types to send over the wire.
	 * @param version the version vCard that is being generated
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * marshalling process depending on the expected consumer of the vCard
	 * @param vcard the vCard that is being marshalled
	 * @return the sub types that will be sent
	 */
	public final VCardSubTypes marshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		VCardSubTypes copy = new VCardSubTypes(subTypes);
		doMarshalSubTypes(copy, version, warnings, compatibilityMode, vcard);
		return copy;
	}

	/**
	 * Gets the sub types that will be sent over the wire.
	 * 
	 * <p>
	 * If this method is NOT overridden, then the type's sub types will be sent
	 * over the wire as-is. In other words, whatever is in the
	 * {@link VCardType#subTypes} field will be sent. Child classes can override
	 * this method in order to modify the sub types before they are marshalled.
	 * </p>
	 * @param subTypes the sub types that will be marshalled into the vCard.
	 * This object is a copy of the {@link VCardType#subTypes} field, so any
	 * modifications done to this object will not effect the state of the field.
	 * @param version the version vCard that is being generated
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * marshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * marshalling process depending on the expected consumer of the vCard
	 * @param vcard the {@link VCard} object that is being marshalled
	 */
	protected void doMarshalSubTypes(VCardSubTypes subTypes, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		//do nothing
	}

	/**
	 * Unmarshals the type value from off the wire.
	 * @param subTypes the sub types that were parsed
	 * @param value the unfolded value from off the wire. If the wire value is
	 * in "quoted-printable" encoding, it will be decoded.
	 * @param version the version of the vCard that is being read or null if the
	 * VERSION type has not been parsed yet (v3.0 and v4.0 require that the
	 * VERSION type be at the top of the vCard, but v2.1 has no such
	 * requirement)
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * unmarshalling process depending on where the vCard came from
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the value of this type is an embedded
	 * vCard (i.e. the AGENT type)
	 */
	public final void unmarshalText(VCardSubTypes subTypes, String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		this.subTypes = subTypes;
		doUnmarshalText(value, version, warnings, compatibilityMode);
	}

	/**
	 * Unmarshals the type value from off the wire.
	 * @param value the unfolded value from off the wire. If the wire value is
	 * in the "quoted-printable" encoding, it will be decoded.
	 * @param version the version of the vCard that is being read or null if the
	 * VERSION type has not been parsed yet (v3.0 and v4.0 require that the
	 * VERSION type be at the top of the vCard, but v2.1 has no such
	 * requirement)
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @param compatibilityMode allows you to customize the unmarshalling
	 * process depending on where the vCard came from
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the value of this type is an embedded
	 * vCard (i.e. the AGENT type)
	 */
	protected abstract void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode);

	/**
	 * Unmarshals the type from an xCard (XML document).
	 * @param subTypes the sub types that were parsed
	 * @param element the XML element that contains the type data. For example,
	 * this would be the "&lt;fn&gt;" element for the "FN" type. This object
	 * will NOT include the "&lt;parameters&gt;" child element (it is removed
	 * after being unmarshalled into a {@link VCardSubTypes} object).
	 * @param version the version of the xCard
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * unmarshalling process depending on where the vCard came from
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws UnsupportedOperationException if the type class does not support
	 * xCard parsing
	 */
	public final void unmarshalXml(VCardSubTypes subTypes, Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		this.subTypes = subTypes;
		XCardElement wrapper = new XCardElement(element, version);
		doUnmarshalXml(wrapper, warnings, compatibilityMode);
	}

	/**
	 * Unmarshals the type from an xCard (XML document).
	 * @param element the XML element that contains the type data. For example,
	 * this would be the "&lt;fn&gt;" element for the "FN" type. This object
	 * will NOT include the "&lt;parameters&gt;" child element (it is removed
	 * after being unmarshalled into a {@link VCardSubTypes} object).
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @param compatibilityMode allows the programmer to customize the
	 * unmarshalling process depending on where the vCard came from
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws UnsupportedOperationException if the type class does not support
	 * xCard parsing
	 */
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		throw new UnsupportedOperationException("This type class does not support the parsing of xCards.");
	}

	/**
	 * Unmarshals the type from an hCard (HTML document).
	 * @param element the HTML element that contains the type data.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the value of this type is an embedded
	 * vCard (i.e. the AGENT type)
	 * @throws UnsupportedOperationException if the type class does not support
	 * hCard parsing
	 */
	public final void unmarshalHtml(org.jsoup.nodes.Element element, List<String> warnings) {
		HCardElement hcardElement = new HCardElement(element);
		doUnmarshalHtml(hcardElement, warnings);
	}

	/**
	 * Unmarshals the type from an hCard (HTML document).
	 * @param element the HTML element that contains the type data.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 * @throws EmbeddedVCardException if the value of this type is an embedded
	 * vCard (i.e. the AGENT type)
	 */
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String value = element.value();
		doUnmarshalText(value, VCardVersion.V3_0, warnings, CompatibilityMode.RFC);
	}

	/**
	 * Unmarshals the type from a jCard (JSON).
	 * @param subTypes the sub types that were parsed
	 * @param value includes the data type and property value(s)
	 * @param version the version of the jCard that is being read
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 */
	public final void unmarshalJson(VCardSubTypes subTypes, JCardValue value, VCardVersion version, List<String> warnings) {
		this.subTypes = subTypes;
		doUnmarshalJson(value, version, warnings);
	}

	/**
	 * Unmarshals the type from a jCard (JSON).
	 * @param value includes the data type and property value(s)
	 * @param version the version of the jCard that is being read
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @throws SkipMeException if this type should NOT be added to the
	 * {@link VCard} object
	 */
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		doUnmarshalText(jcardValueToString(value), version, warnings, CompatibilityMode.RFC);
	}

	private String jcardValueToString(JCardValue value) {
		if (value.getValues().size() > 1) {
			List<String> multi = value.getMultivalued();
			if (multi != null) {
				return VCardStringUtils.join(multi, ",", new JoinCallback<String>() {
					public void handle(StringBuilder sb, String value) {
						sb.append(VCardStringUtils.escape(value));
					}
				});
			}
		}

		if (!value.getValues().isEmpty() && value.getValues().get(0).getArray() != null) {
			List<List<String>> structured = value.getStructured();
			if (structured != null) {
				return VCardStringUtils.join(structured, ";", new JoinCallback<List<String>>() {
					public void handle(StringBuilder sb, List<String> value) {
						VCardStringUtils.join(value, ",", sb, new JoinCallback<String>() {
							public void handle(StringBuilder sb, String value) {
								sb.append(VCardStringUtils.escape(value));
							}
						});
					}
				});
			}
		}

		return value.getSingleValued();
	}

	/**
	 * <p>
	 * Gets the qualified name (XML namespace and local part) for marshalling
	 * the type to an XML document (xCard).
	 * </p>
	 * <p>
	 * Extended type classes should override this method. By default, this
	 * method returns <code>null</code>, which instructs the marshallers to
	 * assign the following qualified name to the type:<br>
	 * <br>
	 * Namespace: xCard namespace<br>
	 * Local part: a lower-cased version of the type name
	 * </p>
	 * @return the XML qualified name or null to use the default qualified name
	 */
	public QName getQName() {
		return null;
	}

	/**
	 * Gets all parameters associated with this type.
	 * @return the type's parameters
	 */
	public VCardSubTypes getSubTypes() {
		return subTypes;
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
	 * @param values the parameter value
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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