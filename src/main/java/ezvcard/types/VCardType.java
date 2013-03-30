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
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
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
		return JCardValue.text(valueStr);
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
		Object first = value.getFirstValue();
		String valueStr = (first == null) ? "" : first.toString();
		doUnmarshalText(valueStr, version, warnings, CompatibilityMode.RFC);
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
	 * Gets all sub types (a.k.a "parameters") associated with this type. This
	 * method can be used to retrieve any extended, standard, or non-standard
	 * sub type.
	 * 
	 * <p>
	 * Ideally, this method should NOT be used to retrieve the values of
	 * standard sub types because the type class should contain getter/setter
	 * methods for each standard sub type. For example, instead of calling
	 * <code>NoteType.getSubTypes().getLanguage()</code> to retrieve the
	 * "LANGUAGE" sub type of a NOTE, the {@link NoteType#getLanguage()} method
	 * should be called instead.
	 * </p>
	 * @return the type's sub types
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
		//TODO test for valid chars
		//		if (group != null && !group.matches("(?i)[-a-z0-9]+")) {
		//			throw new IllegalArgumentException("");
		//		}
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
}