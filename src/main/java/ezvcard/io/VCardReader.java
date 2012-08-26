package ezvcard.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.TypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.types.AddressType;
import ezvcard.types.AgentType;
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
public class VCardReader implements Closeable {
	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private List<String> warnings = new ArrayList<String>();
	private VCardVersion version;
	private boolean endFound = false;
	private Map<String, Class<? extends VCardType>> extendedTypeClasses = new HashMap<String, Class<? extends VCardType>>();
	private FoldedLineReader reader;

	private List<LabelType> labels = new ArrayList<LabelType>();

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
	 * Gets the compatibility mode.
	 * @return the compatibility mode
	 */
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Used for customizing the unmarshalling process based on the mail client
	 * that generated the vCard.
	 * @param compatibilityMode the compatiblity mode
	 */
	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	/**
	 * Registers a extended type class. These types will be unmarshalled into
	 * instances of this class.
	 * @param clazz the extended type class. It MUST contain a public, no-arg
	 * constructor.
	 */
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		try {
			VCardType t = clazz.newInstance();
			extendedTypeClasses.put(t.getTypeName().toUpperCase(), clazz);
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	/**
	 * Reads the next vCard.
	 * @return the next vCard or null if there are no more
	 * @throws VCardException if there's a problem parsing the vCard
	 * @throws IOException if there's a problem reading from the stream
	 */
	public VCard readNext() throws VCardException, IOException {
		warnings.clear();
		version = null;
		endFound = false;
		labels.clear();

		VCard vcard = new VCard();

		int typesRead = 0;
		String line;
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

			//parse a v2.1 AGENT type
			if ((version == null || version == VCardVersion.V2_1) && AgentType.NAME.equals(typeName) && value.length() == 0) {
				VCardReader agentReader = new VCardReader(reader);
				agentReader.setCompatibilityMode(compatibilityMode);
				try {
					VCard agentVcard = agentReader.readNext();
					AgentType agent = new AgentType(agentVcard);
					agent.setGroup(groupName);
					vcard.setAgent(agent);
				} finally {
					for (String w : agentReader.getWarnings()) {
						warnings.add("AGENT unmarshal warning: " + w);
					}
				}

				continue;
			}

			//create the Type object
			VCardType type = createAndAddToVCard(vcard, typeName, subTypes, value);

			if (type != null) {
				//set the group
				type.setGroup(groupName);

				//unmarshal the text string into the object
				List<String> warnings = new ArrayList<String>();
				try {
					type.unmarshalValue(subTypes, value, version, warnings, compatibilityMode);
				} finally {
					this.warnings.addAll(warnings);
				}
			}

//			if (endFound) {
//				return vcard;
//			}
		}

		if (typesRead == 0) {
			//end of stream reached
			return null;
		}
		
		//assign labels to their addresses
		for (LabelType label : labels){
			boolean orphaned = true;
			for (AddressType adr : vcard.getAddresses()){
				if (adr.getLabel() == null && adr.getTypes().equals(label.getTypes())){
					adr.setLabel(label.getValue());
					orphaned = false;
					break;
				}
			}
			if (orphaned){
				vcard.addOrphanedLabel(label);
			}
		}

		if (!endFound) {
			warnings.add("vCard does not end with the END type.");
		}

		return vcard;
	}

	/**
	 * Creates the appropriate VCardType instance and adds it to the vCard. This
	 * method does not unmarshal the type, it just creates the type object and
	 * adds it to the VCard bean.
	 * @param vcard the vCard
	 * @param name the Type's name
	 * @param subTypes the Type's Sub Types
	 * @param value the Type's value
	 * @return the Type that was created or null if a Type object wasn't created
	 * @throws VCardException
	 */
	//Note: If this method has a compile error, make sure that you're returning the
	//      Type that you created in its "if" block
	private VCardType createAndAddToVCard(VCard vcard, String name, VCardSubTypes subTypes, String value) throws VCardException {
		if ("BEGIN".equals(name)) {
			if (!"vcard".equalsIgnoreCase(value)) {
				warnings.add("The value of the BEGIN type should be \"vcard\", but it is \"" + value + "\".");
			}
			return null;
		} else if ("END".equals(name)) {
			endFound = true;
			if (!"vcard".equalsIgnoreCase(value)) {
				warnings.add("The value of the END type should be \"vcard\", but it is \"" + value + "\".");
			}
			return null;
		} else if ("VERSION".equals(name)) {
			if (version == null) {
				version = VCardVersion.valueOfByStr(value);
				if (version == null) {
					warnings.add("Invalid VERSION: " + value);
				}
			} else {
				warnings.add("Additional VERSION type encountered: \"" + value + "\".  It will be ignored.");
			}
			vcard.setVersion(version);
			return null;
		} else if (LabelType.NAME.equals(name)) {
			//LABELs must be matched up with their ADRs
			LabelType t = new LabelType();
			t.unmarshalValue(subTypes, value, version, warnings, compatibilityMode);
			labels.add(t);
			return null;
		} else {
			Class<? extends VCardType> clazz = TypeList.nameToTypeClass.get(name);
			VCardType t;
			if (clazz != null) {
				try {
					//create a new instance of the class
					t = clazz.newInstance();
					
					//add the type object to the "VCard" object
					Method method = TypeList.typeClassToAddMethod.get(clazz);
					method.invoke(vcard, t);
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
						vcard.addExtendedType(t);
					} catch (Exception e) {
						throw new VCardException("Extended type class \"" + extendedTypeClass.getName() + "\" must have a public, no-arg constructor.");
					}
				} else {
					t = new RawType(name); //use RawType instead of TextType because we don't want to unescape any characters that might be meaningful to this type
					vcard.addExtendedType(t);

					if (!name.startsWith("X-")) {
						warnings.add("Non-standard type \"" + name + "\" found.  Treating it as an extended type.");
					}
				}
			}
			return t;
		}
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
