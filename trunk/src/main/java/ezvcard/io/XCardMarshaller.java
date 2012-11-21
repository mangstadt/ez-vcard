package ezvcard.io;

import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.EZVCard;
import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.MemberType;
import ezvcard.types.TextType;
import ezvcard.types.VCardType;
import ezvcard.util.ListMultimap;

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
 * Converts vCards to their XML representation.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCardMarshaller {
	/**
	 * Defines the names of the XML elements that are used to hold each
	 * parameter's value.
	 */
	private static final Map<String, String> parameterChildElementNames;
	static {
		Map<String, String> m = new HashMap<String, String>();
		m.put("altid", "text");
		m.put("calscale", "text");
		m.put("geo", "uri");
		m.put("label", "text");
		m.put("language", "language-tag");
		m.put("mediatype", "text");
		m.put("pid", "text");
		m.put("pref", "integer");
		m.put("sort-as", "text");
		m.put("type", "text");
		m.put("tz", "uri");
		parameterChildElementNames = Collections.unmodifiableMap(m);
	}

	private CompatibilityMode compatibilityMode;
	private boolean addGenerator = true;
	private VCardVersion targetVersion = VCardVersion.V4_0; //xCard standard only supports 4.0
	private List<String> warnings = new ArrayList<String>();
	private final Document document;
	private final Element root;

	public XCardMarshaller() {
		this(CompatibilityMode.RFC);
	}

	/**
	 * @param compatibilityMode the compatibility mode
	 */
	public XCardMarshaller(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;

		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			//should never be thrown
		}
		document = builder.newDocument();
		root = createElement("vcards");
		document.appendChild(root);
	}

	/**
	 * Gets the compatibility mode. Used for customizing the marshalling process
	 * to target a particular application.
	 * @return the compatibility mode
	 */
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Sets the compatibility mode. Used for customizing the marshalling process
	 * to target a particular application.
	 * @param compatibilityMode the compatibility mode
	 */
	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	/**
	 * Gets whether or not a "X-GENERATOR" extended type will be added to each
	 * vCard. The type includes the version number and URL of this library.
	 * @return true if it will be added, false if not (defaults to true)
	 */
	public boolean isAddGenerator() {
		return addGenerator;
	}

	/**
	 * Sets whether or not to add a "X-GENERATOR" extended type to the vCard.
	 * The type includes the version number and URL of this library.
	 * @param addGenerator true to add this extended type, false not to
	 * (defaults to true)
	 */
	public void setAddGenerator(boolean addGenerator) {
		this.addGenerator = addGenerator;
	}

	/**
	 * Gets the warnings from the last vCard that was marshalled. This list is
	 * reset every time a new vCard is written.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	/**
	 * Gets the XML document that was generated.
	 * @return the XML document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Writes the XML document to an output stream.
	 * @param writer the output stream
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(Writer writer) throws TransformerException {
		Transformer t = TransformerFactory.newInstance().newTransformer();
		Source source = new DOMSource(document);
		Result result = new StreamResult(writer);
		t.transform(source, result);
	}

	/**
	 * Adds a vCard to the XML document
	 * @param vcard the vCard to add
	 */
	public void addVCard(VCard vcard) {
		warnings.clear();

		if (vcard.getFormattedName() == null) {
			warnings.add("vCard version " + targetVersion + " requires that a formatted name be defined.");
		}

		//use reflection to get all VCardType fields in the VCard class
		//the order that the Types are in doesn't matter
		ListMultimap<String, VCardType> types = new ListMultimap<String, VCardType>(); //group the types by group
		for (Field f : vcard.getClass().getDeclaredFields()) {
			try {
				f.setAccessible(true);
				Object value = f.get(vcard);
				if (value instanceof VCardType) {
					VCardType type = (VCardType) value;
					addToTypeList(type, vcard, types);
				} else if (value instanceof Collection) {
					Collection<?> collection = (Collection<?>) value;
					for (Object obj : collection) {
						if (obj instanceof VCardType) {
							VCardType type = (VCardType) obj;
							addToTypeList(type, vcard, types);
						}
					}
				}
			} catch (IllegalArgumentException e) {
				//shouldn't be thrown because we're passing the correct object into Field.get()
			} catch (IllegalAccessException e) {
				//shouldn't be thrown because we're calling Field.setAccessible(true)
			}
		}

		//add extended types
		for (List<VCardType> list : vcard.getExtendedTypes().values()) {
			for (VCardType extendedType : list) {
				addToTypeList(extendedType, vcard, types);
			}
		}

		//add an extended type saying it was generated by EZ vCard
		if (addGenerator) {
			addToTypeList(new TextType("X-GENERATOR", "EZ vCard v" + EZVCard.VERSION + " " + EZVCard.URL), vcard, types);
		}

		//marshal each type object
		Element vcardElement = createElement("vcard");
		for (String groupName : types.keySet()) {
			Element parent;
			if (groupName != null) {
				Element groupElement = createElement("group");
				groupElement.setAttribute("name", groupName);
				vcardElement.appendChild(groupElement);
				parent = groupElement;
			} else {
				parent = vcardElement;
			}

			List<String> warningsBuf = new ArrayList<String>();
			for (VCardType type : types.get(groupName)) {
				warningsBuf.clear();
				try {
					Element typeElement = marshalType(type, vcard, warningsBuf);
					parent.appendChild(typeElement);
				} catch (SkipMeException e) {
					warningsBuf.add(type.getTypeName() + " property will not be marshalled: " + e.getMessage());
				} catch (EmbeddedVCardException e) {
					warningsBuf.add(type.getTypeName() + " property will not be marshalled: xCard does not supported embedded vCards.");
				} finally {
					warnings.addAll(warningsBuf);
				}
			}
		}
		root.appendChild(vcardElement);
	}

	/**
	 * Marshals a type object to an XML element.
	 * @param type the type object to marshal
	 * @param vcard the vcard the type belongs to
	 * @param warningsBuf the list to add the warnings to
	 * @return the XML element or null not to add anything to the final XML
	 * document
	 */
	private Element marshalType(VCardType type, VCard vcard, List<String> warningsBuf) {
		QName qname = type.getQName();
		String ns, localPart;
		if (qname == null) {
			localPart = type.getTypeName().toLowerCase();
			ns = targetVersion.getXmlNamespace();
		} else {
			localPart = qname.getLocalPart();
			ns = qname.getNamespaceURI();
		}
		Element typeElement = createElement(localPart, ns);

		//marshal the sub types
		VCardSubTypes subTypes = type.marshalSubTypes(targetVersion, warningsBuf, compatibilityMode, vcard);
		subTypes.setValue(null); //don't include the VALUE parameter (modification of the "VCardSubTypes" object is safe because it's a copy)
		if (!subTypes.getMultimap().isEmpty()) {
			Element parametersElement = createElement("parameters");
			for (String paramName : subTypes.getNames()) {
				Element parameterElement = createElement(paramName.toLowerCase());
				for (String paramValue : subTypes.get(paramName)) {
					String valueElementName = parameterChildElementNames.get(paramName.toLowerCase());
					if (valueElementName == null) {
						valueElementName = "unknown";
					}
					Element parameterValueElement = createElement(valueElementName);
					parameterValueElement.setTextContent(paramValue);
					parameterElement.appendChild(parameterValueElement);
				}
				parametersElement.appendChild(parameterElement);
			}
			typeElement.appendChild(parametersElement);
		}

		//marshal the value
		type.marshalValue(typeElement, targetVersion, warningsBuf, compatibilityMode);

		return typeElement;
	}

	/**
	 * Adds a type object to the "will-be-marshalled" list if it determines that
	 * the type should be added to the final XML document.
	 * @param type the type to consider for addition
	 * @param vcard the vcard that the type belongs to
	 * @param list the "will-be-marshalled" list
	 */
	private void addToTypeList(VCardType type, VCard vcard, ListMultimap<String, VCardType> list) {
		if (type == null) {
			return;
		}

		//determine if this type is supported by the target version
		boolean supported = false;
		for (VCardVersion v : type.getSupportedVersions()) {
			if (v == targetVersion) {
				supported = true;
				break;
			}
		}

		if (supported) {
			if (type instanceof MemberType && (vcard.getKind() == null || !vcard.getKind().isGroup())) {
				warnings.add("The value of KIND must be set to \"group\" in order to add MEMBERs to the vCard.");
				return;
			}
			list.put(type.getGroup(), type);
		} else {
			warnings.add("The " + type.getTypeName() + " type is not supported by vCard version " + targetVersion + ".  The supported versions are " + Arrays.toString(type.getSupportedVersions()) + ".  This type will not be added to the vCard.");
			return;
		}
	}

	/**
	 * Creates a new XML element.
	 * @param name the name of the XML element
	 * @return the new XML element
	 */
	private Element createElement(String name) {
		return createElement(name, targetVersion.getXmlNamespace());
	}

	/**
	 * Creates a new XML element.
	 * @param name the name of the XML element
	 * @param ns the namespace of the XML element
	 * @return the new XML element
	 */
	private Element createElement(String name, String ns) {
		return document.createElementNS(ns, name);
	}
}
