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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

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
 * Unmarshals vCards into {@link VCard} objects.
 * @author Michael Angstadt
 */
public class VCardReader implements Closeable, IParser {
	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private List<String> warnings = new ArrayList<String>();
	private Map<String, Class<? extends VCardType>> extendedTypeClasses = new HashMap<String, Class<? extends VCardType>>();
	private FoldedLineReader reader;

	/**
	 * @param str the string to read the vCards from
	 */
	public VCardReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * @param in the input stream to read the vCards from
	 */
	public VCardReader(InputStream in) {
		this(new InputStreamReader(in));
	}

	/**
	 * @param file the file to read the vCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public VCardReader(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	/**
	 * @param reader the reader to read the vCards from
	 */
	public VCardReader(Reader reader) {
		this.reader = new FoldedLineReader(reader);
	}

	/**
	 * This constructor is used for reading embedded 2.1 vCards (see 2.1 docs,
	 * p.19)
	 * @param reader the reader to read the vCards from
	 */
	private VCardReader(FoldedLineReader reader) {
		this.reader = reader;
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
		warnings.clear();

		VCard vcard = new VCard();

		List<LabelType> labels = new ArrayList<LabelType>();
		VCardVersion version = null;
		boolean endFound = false;
		int typesRead = 0;
		String line;
		List<String> warningsBuf = new ArrayList<String>();

		while (!endFound && (line = reader.readLine()) != null) {
			//parse the components out of the line
			VCardLine parsedLine = VCardLine.parse(line);
			if (parsedLine == null) {
				warnings.add("Skipping malformed vCard line: \"" + line + "\"");
				continue;
			}

			//build the sub types 
			VCardSubTypes subTypes = new VCardSubTypes();
			for (String[] subTypeStr : parsedLine.getSubTypes()) {
				//if the parameter is name-less, make a guess at what the name is
				//v3.0 and v4.0 requires all sub types to have names, but v2.1 does not
				String subTypeName = subTypeStr[0];
				String subTypeValue = subTypeStr[1];
				if (subTypeName == null) {
					if (ValueParameter.valueOf(subTypeValue) != null) {
						subTypeName = ValueParameter.NAME;
					} else if (EncodingParameter.valueOf(subTypeValue) != null) {
						subTypeName = EncodingParameter.NAME;
					} else {
						//otherwise, assume it's a TYPE
						subTypeName = TypeParameter.NAME;
					}

					subTypes.put(subTypeName, subTypeValue);
				} else {
					subTypeName = subTypeName.toUpperCase();

					//split the value up if it's a comma-delimited list (i.e. the "TYPE" sub type)
					String commaSplit[] = VCardStringUtils.splitBy(subTypeValue, ',', true, true);
					for (String s : commaSplit) {
						subTypes.put(subTypeName, s);
					}
				}
			}

			String typeName = parsedLine.getTypeName().toUpperCase();
			String value = VCardStringUtils.ltrim(parsedLine.getValue());
			String groupName = parsedLine.getGroup();

			//if the value is encoded in "quoted-printable", decode it
			//"quoted-printable" encoding is only supported in v2.1
			if (subTypes.getEncoding() == EncodingParameter.QUOTED_PRINTABLE) {
				QuotedPrintableCodec codec = new QuotedPrintableCodec();
				String charset = subTypes.getCharset();
				try {
					value = (charset == null) ? codec.decode(value) : codec.decode(value, charset);
				} catch (DecoderException e) {
					warnings.add("The value of the " + typeName + " type was marked as \"quoted-printable\", but it could not be decoded.  Assuming that the value is plain text.");
				}
				subTypes.setEncoding(null); //remove encoding sub type
			}

			//vCard should start with "BEGIN"
			if (typesRead == 0 && !"BEGIN".equals(typeName)) {
				warnings.add("vCard does not start with \"BEGIN\".");
			}

			typesRead++;

			if ("BEGIN".equals(typeName)) {
				if (!"vcard".equalsIgnoreCase(value)) {
					warnings.add("The value of the BEGIN property should be \"vcard\", but it is \"" + value + "\".");
				}
			} else if ("END".equals(typeName)) {
				endFound = true;
				if (!"vcard".equalsIgnoreCase(value)) {
					warnings.add("The value of the END property should be \"vcard\", but it is \"" + value + "\".");
				}
			} else if ("VERSION".equals(typeName)) {
				if (version == null) {
					version = VCardVersion.valueOfByStr(value);
					if (version == null) {
						warnings.add("Invalid value of VERSION property: " + value);
					}
				} else {
					warnings.add("Additional VERSION property encountered: \"" + value + "\".  It will be ignored.");
				}
				vcard.setVersion(version);
			} else {
				//create the type object
				VCardType type = createTypeObject(typeName);
				type.setGroup(groupName);

				//unmarshal the text string into the object
				warningsBuf.clear();
				try {
					type.unmarshalText(subTypes, value, version, warningsBuf, compatibilityMode);

					//add to vcard
					if (type instanceof LabelType) {
						//LABELs must be treated specially so they can be matched up with their ADRs
						labels.add((LabelType) type);
					} else {
						addToVCard(type, vcard);
					}
				} catch (SkipMeException e) {
					warningsBuf.add(type.getTypeName() + " property will not be unmarshalled: " + e.getMessage());
				} catch (EmbeddedVCardException e) {
					//parse an embedded vCard (i.e. the AGENT type)

					VCardReader agentReader;
					if (value.length() == 0 || version == null || version == VCardVersion.V2_1) {
						//vCard will be added as a nested vCard (2.1 style)
						agentReader = new VCardReader(reader);
					} else {
						//vCard will be contained within the type value (3.0 style)
						value = VCardStringUtils.unescape(value);
						agentReader = new VCardReader(new StringReader(value));
					}

					agentReader.setCompatibilityMode(compatibilityMode);
					try {
						VCard agentVcard = agentReader.readNext();
						e.injectVCard(agentVcard);
					} finally {
						for (String w : agentReader.getWarnings()) {
							warnings.add("Problem unmarshalling nested vCard value from " + type.getTypeName() + ": " + w);
						}
					}

					addToVCard(type, vcard);
				} finally {
					warnings.addAll(warningsBuf);
				}
			}
		}

		if (typesRead == 0) {
			//end of stream reached
			return null;
		}

		//assign labels to their addresses
		for (LabelType label : labels) {
			boolean orphaned = true;
			for (AddressType adr : vcard.getAddresses()) {
				if (adr.getLabel() == null && adr.getTypes().equals(label.getTypes())) {
					adr.setLabel(label.getValue());
					orphaned = false;
					break;
				}
			}
			if (orphaned) {
				vcard.addOrphanedLabel(label);
			}
		}

		if (!endFound) {
			warnings.add("vCard does not terminate with the END property.");
		}

		return vcard;
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
				//it is the responsibility of the EZ-vCard developer to ensure that this exception is never thrown
				//all type classes defined in the EZ-vCard library MUST have public, no-arg constructors
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
					warnings.add("Non-standard type \"" + name + "\" found.  Treating it as an extended type.");
				}
			}
		}
		return t;
	}

	/**
	 * Adds a type object to the vCard.
	 * @param t the type object
	 * @param vcard the vCard
	 */
	private void addToVCard(VCardType t, VCard vcard) {
		Method method = TypeList.getAddMethod(t.getClass());
		if (method != null) {
			try {
				method.invoke(vcard, t);
			} catch (Exception e) {
				//this should NEVER be thrown because the method MUST be public
				throw new RuntimeException(e);
			}
		} else {
			vcard.addExtendedType(t);
		}
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
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
