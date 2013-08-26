package ezvcard.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.TypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.types.AddressType;
import ezvcard.types.LabelType;
import ezvcard.types.RawType;
import ezvcard.types.TypeList;
import ezvcard.types.VCardType;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.org.apache.commons.codec.DecoderException;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
 * Parses {@link VCard} objects from a plain-text vCard data stream.
 * @author Michael Angstadt
 */
public class VCardReader implements Closeable, IParser {
	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private List<String> warnings = new ArrayList<String>();
	private Map<String, Class<? extends VCardType>> extendedTypeClasses = new HashMap<String, Class<? extends VCardType>>();
	private VCardRawReader reader;

	/**
	 * Creates a vCard reader.
	 * @param str the string to read the vCards from
	 */
	public VCardReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * Creates a vCard reader.
	 * @param in the input stream to read the vCards from
	 */
	public VCardReader(InputStream in) {
		this(new InputStreamReader(in));
	}

	/**
	 * Creates a vCard reader.
	 * @param file the file to read the vCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public VCardReader(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	/**
	 * Creates a vCard reader.
	 * @param reader the reader to read the vCards from
	 */
	public VCardReader(Reader reader) {
		this.reader = new VCardRawReader(reader);
	}

	/**
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @see VCardRawReader#isCaretDecodingEnabled()
	 */
	public boolean isCaretDecodingEnabled() {
		return reader.isCaretDecodingEnabled();
	}

	/**
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see VCardRawReader#setCaretDecodingEnabled(boolean)
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		reader.setCaretDecodingEnabled(enable);
	}

	/**
	 * Gets the compatibility mode. Used for customizing the unmarshalling
	 * process based on the application that generated the vCard.
	 * @return the compatibility mode
	 */
	@Deprecated
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Sets the compatibility mode. Used for customizing the unmarshalling
	 * process based on the application that generated the vCard.
	 * @param compatibilityMode the compatibility mode
	 */
	@Deprecated
	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	//@Override
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.put(getTypeNameFromTypeClass(clazz), clazz);
	}

	//@Override
	public void unregisterExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.remove(getTypeNameFromTypeClass(clazz));
	}

	//@Override
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	//@Override
	public VCard readNext() throws IOException {
		if (reader.eof()) {
			return null;
		}

		warnings.clear();

		VCardDataStreamListenerImpl listener = new VCardDataStreamListenerImpl();
		reader.start(listener);

		return listener.root;
	}

	/**
	 * Creates the appropriate {@link VCardType} instance, given the type name.
	 * This method does not unmarshal the type, it just creates the type object.
	 * @param name the type name (e.g. "FN")
	 * @return the Type that was created
	 */
	private VCardType createTypeObject(String name) {
		Class<? extends VCardType> clazz = TypeList.getTypeClass(name);
		VCardType t;
		if (clazz != null) {
			try {
				//create a new instance of the class
				t = clazz.newInstance();
			} catch (Exception e) {
				//it is the responsibility of the ez-vcard developer to ensure that this exception is never thrown
				//all type classes defined in the ez-vcard library MUST have public, no-arg constructors
				throw new RuntimeException(e);
			}
		} else {
			Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(name);
			if (extendedTypeClass != null) {
				try {
					t = extendedTypeClass.newInstance();
				} catch (Exception e) {
					//this should never happen because the type class is checked to see if it has a public, no-arg constructor in the "registerExtendedType" method
					throw new RuntimeException("Extended type class \"" + extendedTypeClass.getName() + "\" must have a public, no-arg constructor.");
				}
			} else {
				t = new RawType(name); //use RawType instead of TextType because we don't want to unescape any characters that might be meaningful to this type
				if (!name.startsWith("X-")) {
					addWarning("Non-standard property found.  Treating it as an extended property.", name);
				}
			}
		}
		return t;
	}

	/**
	 * Gets the type name from a type class.
	 * @param clazz the type class
	 * @return the type name
	 */
	private String getTypeNameFromTypeClass(Class<? extends VCardType> clazz) {
		try {
			VCardType t = clazz.newInstance();
			return t.getTypeName().toUpperCase();
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	/**
	 * Assigns names to all nameless parameters. v3.0 and v4.0 requires all
	 * parameters to have names, but v2.1 does not.
	 * @param parameters the parameters
	 */
	private void handleNamelessParameters(VCardSubTypes parameters) {
		List<String> namelessParamValues = parameters.get(null);
		for (String paramValue : namelessParamValues) {
			String paramName;
			if (ValueParameter.find(paramValue) != null) {
				paramName = ValueParameter.NAME;
			} else if (EncodingParameter.find(paramValue) != null) {
				paramName = EncodingParameter.NAME;
			} else {
				//otherwise, assume it's a TYPE
				paramName = TypeParameter.NAME;
			}
			parameters.put(paramName, paramValue);
		}
		parameters.removeAll(null);
	}

	/**
	 * <p>
	 * Accounts for multi-valued TYPE parameters being enclosed entirely in
	 * double quotes (for example: ADR;TYPE="home,work").
	 * </p>
	 * <p>
	 * Many examples throughout the 4.0 specs show TYPE parameters being encoded
	 * in this way. This conflicts with the ABNF and is noted in the errata.
	 * This method will split the value by comma incase the vendor implemented
	 * it this way.
	 * </p>
	 * @param parameters the parameters
	 */
	private void handleQuotedMultivaluedTypeParams(VCardSubTypes parameters) {
		//account for multi-valued TYPE parameters being enclosed entirely in double quotes
		//e.g. ADR;TYPE="home,work"
		List<String> typeParams = new ArrayList<String>(parameters.get(TypeParameter.NAME));
		for (String typeParam : typeParams) {
			if (!typeParam.contains(",")) {
				continue;
			}

			parameters.remove(TypeParameter.NAME, typeParam);
			for (String splitValue : typeParam.split(",")) {
				parameters.put(TypeParameter.NAME, splitValue);
			}
		}
	}

	/**
	 * Decodes the property value if it's encoded in quoted-printable encoding.
	 * Quoted-printable encoding is only supported in v2.1.
	 * @param name the property name
	 * @param parameters the parameters
	 * @param value the property value
	 * @return the decoded property value
	 */
	private String decodeQuotedPrintable(String name, VCardSubTypes parameters, String value) {
		if (parameters.getEncoding() != EncodingParameter.QUOTED_PRINTABLE) {
			return value;
		}

		parameters.setEncoding(null); //remove encoding sub type

		QuotedPrintableCodec codec = new QuotedPrintableCodec();
		String charset = parameters.getCharset();
		try {
			if (charset == null) {
				return codec.decode(value);
			} else {
				try {
					return codec.decode(value, charset);
				} catch (UnsupportedEncodingException e) {
					addWarning("The specified charset is not supported.  Using default charset instead: " + charset, name);
					return codec.decode(value);
				}
			}
		} catch (DecoderException e) {
			addWarning("Property value was marked as \"quoted-printable\", but it could not be decoded.  Treating the value as plain text.", name);
		}

		return value;
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}

	private void addWarning(String message) {
		addWarning(message, null);
	}

	private void addWarning(String message, String propertyName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(reader.getLineNum());
		if (propertyName != null) {
			sb.append(" (").append(propertyName).append(" property)");
		}
		sb.append(": ").append(message);

		warnings.add(sb.toString());
	}

	private class VCardDataStreamListenerImpl implements VCardRawReader.VCardDataStreamListener {
		private VCard root;
		private final List<LabelType> labels = new ArrayList<LabelType>();
		private final List<String> warningsBuf = new ArrayList<String>();
		private final LinkedList<VCard> vcardStack = new LinkedList<VCard>();
		private EmbeddedVCardException embeddedVCardException;

		public void beginComponent(String name) {
			if (!"VCARD".equalsIgnoreCase(name)) {
				return;
			}

			VCard vcard = new VCard();

			//initialize version to 2.1, since the VERSION property can exist anywhere in a 2.1 vCard
			vcard.setVersion(VCardVersion.V2_1);

			vcardStack.add(vcard);

			if (root == null) {
				root = vcard;
			}

			if (embeddedVCardException != null) {
				embeddedVCardException.injectVCard(vcard);
				embeddedVCardException = null;
			}
		}

		public void readVersion(VCardVersion version) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			vcardStack.getLast().setVersion(version);
		}

		public void readProperty(String group, String name, VCardSubTypes parameters, String value) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			if (embeddedVCardException != null) {
				//the next property was supposed to be the start of a nested vCard, but it wasn't
				embeddedVCardException.injectVCard(null);
				embeddedVCardException = null;
			}

			handleNamelessParameters(parameters);

			handleQuotedMultivaluedTypeParams(parameters);

			value = decodeQuotedPrintable(name, parameters, value);

			//create the type object
			VCardType type = createTypeObject(name);
			type.setGroup(group);

			//unmarshal the text string into the object
			VCard curVCard = vcardStack.getLast();
			VCardVersion version = curVCard.getVersion();
			warningsBuf.clear();
			try {
				type.unmarshalText(parameters, value, version, warningsBuf, compatibilityMode);

				//add to vcard
				if (type instanceof LabelType) {
					//LABELs must be treated specially so they can be matched up with their ADRs
					labels.add((LabelType) type);
				} else {
					curVCard.addType(type);
				}
			} catch (SkipMeException e) {
				warningsBuf.add("Property has requested that it be skipped: " + e.getMessage());
			} catch (EmbeddedVCardException e) {
				//parse an embedded vCard (i.e. the AGENT type)

				if (value.length() == 0 || version == VCardVersion.V2_1) {
					//a nested vCard is expected to be next (2.1 style)
					embeddedVCardException = e;
				} else {
					//the property value should be an embedded vCard (3.0 style)
					value = VCardStringUtils.unescape(value);
					VCardReader agentReader = new VCardReader(value);

					agentReader.setCompatibilityMode(compatibilityMode);
					try {
						VCard nestedVCard = agentReader.readNext();
						if (nestedVCard != null) {
							e.injectVCard(nestedVCard);
						}
					} catch (IOException e2) {
						//reading from a string
					} finally {
						for (String w : agentReader.getWarnings()) {
							addWarning("Problem unmarshalling nested vCard value: " + w, type.getTypeName());
						}
					}
				}

				curVCard.addType(type);
			} finally {
				for (String warning : warningsBuf) {
					addWarning(warning, name);
				}
			}
		}

		public void endComponent(String name) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			if (!"VCARD".equalsIgnoreCase(name)) {
				//not a "VCARD" component
				return;
			}

			VCard curVCard = vcardStack.removeLast();

			//assign labels to their addresses
			for (LabelType label : labels) {
				boolean orphaned = true;
				for (AddressType adr : curVCard.getAddresses()) {
					if (adr.getLabel() == null && adr.getTypes().equals(label.getTypes())) {
						adr.setLabel(label.getValue());
						orphaned = false;
						break;
					}
				}
				if (orphaned) {
					curVCard.addOrphanedLabel(label);
				}
			}

			if (vcardStack.isEmpty()) {
				throw new VCardRawReader.StopReadingException();
			}
		}

		public void invalidLine(String line) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			addWarning("Skipping malformed line: \"" + line + "\"");
		}

		public void invalidVersion(String version) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			addWarning("Ignoring invalid version value: " + version, "VERSION");
		}
	}
}
