package ezvcard.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import ezvcard.EZVCard;
import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.TypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.AgentType;
import ezvcard.types.LabelType;
import ezvcard.types.MemberType;
import ezvcard.types.TextType;
import ezvcard.types.VCardType;

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
 * Converts vCards to string representations.
 * @author Michael Angstadt
 */
public class VCardWriter implements Closeable {
	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private VCardVersion targetVersion = VCardVersion.V3_0;
	private String newline = "\r\n";
	private boolean addGenerator = true;
	private List<String> warnings = new ArrayList<String>();
	private final Writer writer;

	/**
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param writer the writer to write the vCard to
	 */
	public VCardWriter(Writer writer) {
		this(writer, VCardVersion.V3_0);
	}

	/**
	 * Creates a vCard writer (uses the standard folding scheme and newline
	 * sequence).
	 * @param writer the writer to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 */
	public VCardWriter(Writer writer, VCardVersion targetVersion) {
		this(writer, targetVersion, FoldingScheme.MIME_DIR);
	}

	/**
	 * Creates a vCard writer (uses the standard newline sequence).
	 * @param writer the writer to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 */
	public VCardWriter(Writer writer, VCardVersion targetVersion, FoldingScheme foldingScheme) {
		this(writer, targetVersion, foldingScheme, "\r\n");
	}

	/**
	 * Creates a vCard writer.
	 * @param writer the writer to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public VCardWriter(Writer writer, VCardVersion targetVersion, FoldingScheme foldingScheme, String newline) {
		if (writer instanceof FoldedLineWriter || foldingScheme == null) {
			//the check for FoldedLineWriter is for writing nested 2.1 vCards (i.e. the AGENT type)
			this.writer = writer;
		} else {
			this.writer = new FoldedLineWriter(writer, foldingScheme.getMaxChars(), foldingScheme.getIndent(), newline);
		}
		this.targetVersion = targetVersion;
		this.newline = newline;
	}

	/**
	 * Gets the compatibility mode.
	 * @return the compatibility mode
	 */
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Used for customizing the marshalling process based on the mail client
	 * that the vCard is being generated for.
	 * @param compatibilityMode the compatiblity mode
	 */
	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	/**
	 * Gets the version that the vCards should adhere to.
	 * @return the vCard version
	 */
	public VCardVersion getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Sets the version that the vCards should adhere to.
	 * @param targetVersion the vCard version
	 */
	public void setTargetVersion(VCardVersion targetVersion) {
		this.targetVersion = targetVersion;
	}

	/**
	 * Sets whether or not to add a "X-GENERATOR" type to the vCard, saying that
	 * it was generated by this library.
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
	 * Writes a vCard
	 * @param vcard the vCard to write
	 * @throws VCardException if there's a problem writing the vCard
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(final VCard vcard) throws VCardException, IOException {
		warnings.clear();

		if (targetVersion == VCardVersion.V2_1 || targetVersion == VCardVersion.V3_0) {
			if (vcard.getStructuredName() == null) {
				warnings.add("vCard version " + targetVersion + " requires that a structured name be defined.");
			}
		}

		if (targetVersion == VCardVersion.V3_0 || targetVersion == VCardVersion.V4_0) {
			if (vcard.getFormattedName() == null) {
				warnings.add("vCard version " + targetVersion + " requires that a formatted name be defined.");
			}
		}

		@SuppressWarnings("serial")
		List<VCardType> types = new ArrayList<VCardType>() {
			@Override
			public boolean add(VCardType type) {
				if (type == null) {
					return true;
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
						return true;
					}

					super.add(type);

					//add LABEL types for each ADR type if the vCard version is not 4.0
					if (targetVersion != VCardVersion.V4_0 && type instanceof AddressType) {
						AddressType adr = (AddressType) type;
						String labelStr = adr.getLabel();
						if (labelStr != null) {
							LabelType label = new LabelType(labelStr);
							for (AddressTypeParameter t : adr.getTypes()) {
								label.addType(t);
							}
							super.add(label);
						}
					}

				} else {
					warnings.add("The " + type.getTypeName() + " type is not supported by vCard version " + targetVersion + ".  The supported versions are " + Arrays.toString(type.getSupportedVersions()) + ".  This type will not be added to the vCard.");
				}
				return true;
			}
		};
		types.add(new TextType("BEGIN", "VCARD"));
		types.add(new TextType("VERSION", targetVersion.getVersion()));

		//use reflection to get all VCardType fields in the VCard class
		//the order that the Types are in doesn't matter (except for BEGIN, END, and VERSION)
		for (Field f : vcard.getClass().getDeclaredFields()) {
			try {
				f.setAccessible(true);
				Object value = f.get(vcard);
				if (value instanceof VCardType) {
					VCardType type = (VCardType) value;
					types.add(type);
				} else if (value instanceof Collection) {
					Collection<?> collection = (Collection<?>) value;
					for (Object obj : collection) {
						if (obj instanceof VCardType) {
							VCardType type = (VCardType) obj;
							types.add(type);
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
		for (VCardType extendedType : vcard.getExtendedTypes().values()) {
			types.add(extendedType);
		}

		//add an extended type saying it was generated by EZ vCard
		if (addGenerator) {
			types.add(new TextType("X-GENERATOR", "EZ vCard v" + EZVCard.VERSION + " " + EZVCard.URL));
		}

		types.add(new TextType("END", "VCARD"));

		for (VCardType type : types) {
			//determine if this type has a nested vCard
			VCard nested = null;
			if (targetVersion == VCardVersion.V2_1 && type instanceof AgentType) {
				//AGENT types for for 2.1 vCards are nested (see 2.1 docs, p.19)
				AgentType at = (AgentType) type;
				nested = at.getVcard();
			}

			//marshal the value
			String value = null;
			if (nested == null) {
				List<String> warnings = new ArrayList<String>();
				try {
					value = type.marshalValue(targetVersion, warnings, compatibilityMode);
				} finally {
					this.warnings.addAll(warnings);
				}
				if (value == null) {
					warnings.add(type.getTypeName() + " type has requested that it not be marshalled.");
					continue;
				}
			}

			//marshal the sub types
			VCardSubTypes subTypes;
			List<String> warnings = new ArrayList<String>();
			try {
				subTypes = type.marshalSubTypes(targetVersion, warnings, compatibilityMode, vcard);
			} finally {
				this.warnings.addAll(warnings);
			}

			StringBuilder sb = new StringBuilder();

			//write the group
			if (type.getGroup() != null) {
				sb.append(type.getGroup());
				sb.append('.');
			}

			//write the type name
			sb.append(type.getTypeName());

			//write the Sub Types
			for (String subTypeName : subTypes.getNames()) {
				Set<String> subTypeValues = subTypes.get(subTypeName);
				if (!subTypeValues.isEmpty()) {
					if (targetVersion == VCardVersion.V2_1) {
						if (TypeParameter.NAME.equalsIgnoreCase(subTypeName)) {
							//example: ADR;HOME;WORK:
							for (String subTypeValue : subTypeValues) {
								sb.append(';').append(subTypeValue.toUpperCase());
							}
						} else {
							//example: ADR;FOO=bar;FOO=car:
							for (String subTypeValue : subTypeValues) {
								sb.append(';').append(subTypeName).append('=');
								if (subTypeValueNeedsEscaping(subTypeValue)) {
									subTypeValue = escapeSubTypeValue(subTypeValue);
									sb.append('"').append(subTypeValue).append('"');
								} else {
									sb.append(subTypeValue);
								}
							}
						}
					} else {
						//example: ADR;TYPE=home,work:

						//check all the values to see if any have special chars in them
						boolean needsEscaping = false;
						for (String subTypeValue : subTypeValues) {
							if (subTypeValueNeedsEscaping(subTypeValue)) {
								needsEscaping = true;
								break;
							}
						}

						sb.append(';').append(subTypeName).append('=');

						if (needsEscaping) {
							sb.append('"');
							for (String subTypeValue : subTypeValues) {
								subTypeValue = escapeSubTypeValue(subTypeValue);
								sb.append(subTypeValue).append(',');
							}
							sb.deleteCharAt(sb.length() - 1); //chomp last comma
							sb.append('"');
						} else {
							for (String subTypeValue : subTypeValues) {
								sb.append(subTypeValue).append(',');
							}
							sb.deleteCharAt(sb.length() - 1); //chomp last comma
						}
					}
				}
			}

			sb.append(':');

			//write the value
			if (nested == null) {
				sb.append(value);
			}
			writer.write(sb.toString());
			writer.write(newline);
			if (nested != null) {
				//write the nested vCard
				VCardWriter agentWriter = new VCardWriter(writer, targetVersion);
				agentWriter.setAddGenerator(false);
				try {
					agentWriter.write(nested);
				} finally {
					for (String w : agentWriter.getWarnings()) {
						warnings.add("AGENT marshal warning: " + w);
					}
				}
			}
		}
	}

	/**
	 * Determines if a sub type value needs to be escaped.
	 * @param value the sub type value
	 * @return true if it needs to be escaped, false if not
	 */
	private boolean subTypeValueNeedsEscaping(String value) {
		String specialChars = "\",;:\\\n\r";
		for (int i = 0; i < specialChars.length(); i++) {
			char c = specialChars.charAt(i);
			if (value.contains(c + "")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Escapes a sub type value for safe inclusion in a vCard string.
	 * @param value the sub type value
	 * @return the safely escaped value. This method does NOT enclose the value
	 * in double quotes
	 */
	private String escapeSubTypeValue(String value) {
		value = value.replace("\\", "\\\\"); //escape backslashes
		value = value.replace("\"", "\\\""); //escape double quotes
		value = value.replaceAll("\\r\\n|\\r|\\n", "\\\\\\n"); //escape newlines
		return value;
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}
}
