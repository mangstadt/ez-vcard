package ezvcard.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.TypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.LabelType;
import ezvcard.types.MemberType;
import ezvcard.types.ProdIdType;
import ezvcard.types.TextType;
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
 * Converts vCards to string representations.
 * @author Michael Angstadt
 */
public class VCardWriter implements Closeable {
	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private VCardVersion targetVersion = VCardVersion.V3_0;
	private String newline;
	private boolean addProdId = true;
	private FoldingScheme foldingScheme;
	private List<String> warnings = new ArrayList<String>();
	private final Writer writer;

	/**
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param out the output stream to write the vCard to
	 */
	public VCardWriter(OutputStream out) {
		this(new OutputStreamWriter(out));
	}

	/**
	 * Creates a vCard writer (uses the standard folding scheme and newline
	 * sequence).
	 * @param out the output stream to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion) {
		this(new OutputStreamWriter(out), targetVersion);
	}

	/**
	 * Creates a vCard writer.
	 * @param out the output stream to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion, FoldingScheme foldingScheme, String newline) {
		this(new OutputStreamWriter(out), targetVersion, foldingScheme, newline);
	}

	/**
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file) throws IOException {
		this(new FileWriter(file));
	}

	/**
	 * Creates a vCard writer (uses the standard folding scheme and newline
	 * sequence).
	 * @param file the file to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, VCardVersion targetVersion) throws IOException {
		this(new FileWriter(file), targetVersion);
	}

	/**
	 * Creates a vCard writer.
	 * @param file the file to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, VCardVersion targetVersion, FoldingScheme foldingScheme, String newline) throws IOException {
		this(new FileWriter(file), targetVersion, foldingScheme, newline);
	}

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
		this(writer, targetVersion, FoldingScheme.MIME_DIR, "\r\n");
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
			this.writer = new FoldedLineWriter(writer, foldingScheme.getLineLength(), foldingScheme.getIndent(), newline);
		}
		this.targetVersion = targetVersion;
		this.foldingScheme = foldingScheme;
		this.newline = newline;
	}

	/**
	 * Gets the compatibility mode. Used for customizing the marshalling process
	 * to target a particular application.
	 * @return the compatibility mode
	 */
	@Deprecated
	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * Sets the compatibility mode. Used for customizing the marshalling process
	 * to target a particular application.
	 * @param compatibilityMode the compatibility mode
	 */
	@Deprecated
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
	 * Gets whether or not a "PRODID" type will be added to each vCard, saying
	 * that the vCard was generated by this library. For 2.1 vCards, the
	 * extended type "X-PRODID" will be added, since "PRODID" is not supported
	 * by that version.
	 * @return true if it will be added, false if not (defaults to true)
	 */
	public boolean isAddProdId() {
		return addProdId;
	}

	/**
	 * Sets whether or not to add a "PRODID" type to each vCard, saying that the
	 * vCard was generated by this library. For 2.1 vCards, the extended type
	 * "X-PRODID" will be added, since "PRODID" is not supported by that
	 * version.
	 * @param addProdId true to add this type, false not to (defaults to true)
	 */
	public void setAddProdId(boolean addProdId) {
		this.addProdId = addProdId;
	}

	/**
	 * Gets the newline sequence that is used to separate lines.
	 * @return the newline sequence
	 */
	public String getNewline() {
		return newline;
	}

	/**
	 * Gets the rules for how each line is folded.
	 * @return the folding scheme or null if the lines are not folded
	 */
	public FoldingScheme getFoldingScheme() {
		return foldingScheme;
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
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(final VCard vcard) throws IOException {
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

		List<VCardType> typesToAdd = new ArrayList<VCardType>();
		typesToAdd.add(new TextType("BEGIN", "VCARD"));
		typesToAdd.add(new TextType("VERSION", targetVersion.getVersion()));

		for (VCardType type : vcard.getAllTypes()) {
			if (addProdId && type instanceof ProdIdType) {
				//do not add the PRODID in the vCard if "addProdId" is true
				continue;
			}

			//determine if this type is supported by the target version
			if (!supportsTargetVersion(type)) {
				warnings.add(type.getTypeName() + " is not supported by vCard version " + targetVersion + " and will not be added to the vCard.  Supported versions are: " + Arrays.toString(type.getSupportedVersions()));
				continue;
			}

			//check for correct KIND value if there are MEMBER types
			if (type instanceof MemberType && (vcard.getKind() == null || !vcard.getKind().isGroup())) {
				warnings.add("KIND must be set to \"group\" in order to add MEMBER properties to the vCard.");
				continue;
			}

			typesToAdd.add(type);

			//add LABEL types for each ADR type if the target version is 2.1 or 3.0
			if (type instanceof AddressType && targetVersion != VCardVersion.V4_0) {
				AddressType adr = (AddressType) type;
				String labelStr = adr.getLabel();
				if (labelStr != null) {
					LabelType label = new LabelType(labelStr);
					for (AddressTypeParameter t : adr.getTypes()) {
						label.addType(t);
					}
					typesToAdd.add(label);
				}
			}
		}

		//add an extended type saying it was generated by this library
		if (addProdId) {
			VCardType type;
			String text = "ez-vcard " + Ezvcard.VERSION;
			if (targetVersion == VCardVersion.V2_1) {
				type = new TextType("X-" + ProdIdType.NAME, text);
			} else {
				type = new ProdIdType(text);
			}
			typesToAdd.add(type);
		}

		typesToAdd.add(new TextType("END", "VCARD"));

		List<String> warningsBuf = new ArrayList<String>();
		for (VCardType type : typesToAdd) {
			//marshal the value
			warningsBuf.clear();
			String value = null;
			VCard nested = null;
			try {
				value = type.marshalValue(targetVersion, warningsBuf, compatibilityMode);
			} catch (SkipMeException e) {
				warningsBuf.add(type.getTypeName() + " property will not be marshalled: " + e.getMessage());
				continue;
			} catch (EmbeddedVCardException e) {
				nested = e.getVCard();
			} finally {
				warnings.addAll(warningsBuf);
			}

			//marshal the sub types
			warningsBuf.clear();
			VCardSubTypes subTypes;
			try {
				subTypes = type.marshalSubTypes(targetVersion, warningsBuf, compatibilityMode, vcard);
			} finally {
				warnings.addAll(warningsBuf);
			}

			//sanitize value for safe inclusion in the vCard
			if (value != null) {
				if (targetVersion == VCardVersion.V2_1) {
					if (VCardStringUtils.containsNewlines(value)) {
						//2.1 does not support the "\n" escape sequence (see "Delimiters" sub-section in section 2 of the specs)
						QuotedPrintableCodec codec = new QuotedPrintableCodec();
						try {
							value = codec.encode(value);
							subTypes.setEncoding(EncodingParameter.QUOTED_PRINTABLE);
						} catch (EncoderException e) {
							warnings.add("A unexpected error occurred while encoding the value of the " + type.getTypeName() + " property in \"quoted-printable\" encoding.  Value will not be encoded.\n" + e.getMessage());
							value = VCardStringUtils.escapeNewlines(value);
						}
					}
				} else {
					value = VCardStringUtils.escapeNewlines(value);
				}
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

			writer.write(sb.toString());

			//write the value
			if (nested == null) {
				writer.write(value);
				writer.write(newline);
			} else {
				if (targetVersion == VCardVersion.V2_1) {
					writer.write(newline);

					//write a nested vCard (2.1 style)
					VCardWriter agentWriter = new VCardWriter(writer, targetVersion);
					agentWriter.setAddProdId(false);
					agentWriter.setCompatibilityMode(compatibilityMode);
					try {
						agentWriter.write(nested);
					} finally {
						for (String w : agentWriter.getWarnings()) {
							warnings.add(type.getTypeName() + " marshal warning: " + w);
						}
					}
				} else {
					//write an embedded vCard (3.0 style)
					StringWriter sw = new StringWriter();
					VCardWriter agentWriter = new VCardWriter(sw, targetVersion, null, "\n");
					agentWriter.setAddProdId(false);
					agentWriter.setCompatibilityMode(compatibilityMode);
					try {
						agentWriter.write(nested);
					} finally {
						for (String w : agentWriter.getWarnings()) {
							warnings.add("Problem marshalling nested vCard for " + type.getTypeName() + ": " + w);
						}
					}

					String vCardStr = sw.toString();
					vCardStr = VCardStringUtils.escape(vCardStr);
					vCardStr = VCardStringUtils.escapeNewlines(vCardStr);
					writer.write(vCardStr);
					writer.write(newline);
				}
			}
		}
	}

	/**
	 * Determines if a type supports the target version.
	 * @param type the type
	 * @return true if it supports the target version, false if not
	 */
	private boolean supportsTargetVersion(VCardType type) {
		for (VCardVersion version : type.getSupportedVersions()) {
			if (version == targetVersion) {
				return true;
			}
		}
		return false;
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
