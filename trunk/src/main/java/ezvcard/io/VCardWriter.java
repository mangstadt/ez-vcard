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

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.VCardRawWriter.ProblemsListener;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.KindType;
import ezvcard.types.LabelType;
import ezvcard.types.MemberType;
import ezvcard.types.ProdIdType;
import ezvcard.types.VCardType;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.org.apache.commons.codec.EncoderException;

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
 * Writes {@link VCard} objects to a plain-text vCard data stream.
 * @author Michael Angstadt
 */
public class VCardWriter implements Closeable {
	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	private boolean addProdId = true;
	private List<String> warnings = new ArrayList<String>();
	private final VCardRawWriter writer;

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
		this.writer = new VCardRawWriter(writer, targetVersion, foldingScheme, newline);
		this.writer.setProblemsListener(new ProblemsListener() {
			public void onParameterValueChanged(String propertyName, String parameterName, String originalValue, String modifiedValue) {
				addWarning("\"" + parameterName + "\" parameter contained one or more characters which are not allowed in vCard " + VCardWriter.this.writer.getVersion() + " parameter values.  These characters were removed.", propertyName);
			}

			public void onQuotedPrintableEncodingFailed(String propertyName, String propertyValue, EncoderException thrown) {
				addWarning("A unexpected error occurred while encoding the property's value into \"quoted-printable\" encoding.  Value will not be encoded: " + thrown.getMessage(), propertyName);
			}
		});
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
		return writer.getVersion();
	}

	/**
	 * Sets the version that the vCards should adhere to.
	 * @param targetVersion the vCard version
	 */
	public void setTargetVersion(VCardVersion targetVersion) {
		writer.setVersion(targetVersion);
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
	 * <p>
	 * Gets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default, only applies to 3.0 and 4.0
	 * vCards). This escaping mechanism allows for newlines and double quotes to
	 * be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * @return true if circumflex accent encoding is enabled, false if not
	 * @see VCardRawWriter#isCaretEncodingEnabled()
	 */
	public boolean isCaretEncodingEnabled() {
		return writer.isCaretEncodingEnabled();
	}

	/**
	 * <p>
	 * Sets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default, only applies to 3.0 and 4.0
	 * vCards). This escaping mechanism allows for newlines and double quotes to
	 * be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * @param enable true to use circumflex accent encoding, false not to
	 * @see VCardRawWriter#setCaretEncodingEnabled(boolean)
	 */
	public void setCaretEncodingEnabled(boolean enable) {
		writer.setCaretEncodingEnabled(enable);
	}

	/**
	 * Gets the newline sequence that is used to separate lines.
	 * @return the newline sequence
	 */
	public String getNewline() {
		return writer.getNewline();
	}

	/**
	 * Gets the rules for how each line is folded.
	 * @return the folding scheme or null if the lines are not folded
	 */
	public FoldingScheme getFoldingScheme() {
		return writer.getFoldingScheme();
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
	 * Writes a vCard to the stream.
	 * @param vcard the vCard to write
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(VCard vcard) throws IOException {
		warnings.clear();
		write(vcard, addProdId);
	}

	private void write(VCard vcard, boolean addProdId) throws IOException {
		VCardVersion targetVersion = writer.getVersion();

		if (targetVersion == VCardVersion.V2_1 || targetVersion == VCardVersion.V3_0) {
			if (vcard.getStructuredName() == null) {
				warnings.add("vCard version " + targetVersion + " requires that a structured name property be defined.");
			}
		}

		if (targetVersion == VCardVersion.V3_0 || targetVersion == VCardVersion.V4_0) {
			if (vcard.getFormattedName() == null) {
				warnings.add("vCard version " + targetVersion + " requires that a formatted name property be defined.");
			}
		}

		List<VCardType> typesToAdd = new ArrayList<VCardType>();
		for (VCardType type : vcard) {
			if (addProdId && type instanceof ProdIdType) {
				//do not add the PRODID in the vCard if "addProdId" is true
				continue;
			}

			//determine if this type is supported by the target version
			if (!supportsTargetVersion(type)) {
				addWarning("This property is not supported by vCard version " + targetVersion + " and will not be added to the vCard.  Supported versions are: " + Arrays.toString(type.getSupportedVersions()), type.getTypeName());
				continue;
			}

			//check for correct KIND value if there are MEMBER types
			if (type instanceof MemberType && (vcard.getKind() == null || !vcard.getKind().isGroup())) {
				addWarning("Value must be set to \"group\" if the vCard contains " + MemberType.NAME + " properties.", KindType.NAME);
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
			EzvcardProdIdType prodId = new EzvcardProdIdType(targetVersion);
			typesToAdd.add(prodId);
		}

		writer.writeBeginComponent("VCARD");
		writer.writeVersion();

		List<String> warningsBuf = new ArrayList<String>();
		for (VCardType type : typesToAdd) {
			//marshal the value
			warningsBuf.clear();
			String value = null;
			VCard nestedVCard = null;
			try {
				value = type.marshalText(targetVersion, warningsBuf, compatibilityMode);
			} catch (SkipMeException e) {
				warningsBuf.add("Property has requested that it be skipped: " + e.getMessage());
				continue;
			} catch (EmbeddedVCardException e) {
				nestedVCard = e.getVCard();
			} finally {
				for (String warning : warningsBuf) {
					addWarning(warning, type.getTypeName());
				}
			}

			//marshal the sub types
			warningsBuf.clear();
			VCardSubTypes parameters;
			try {
				parameters = type.marshalSubTypes(targetVersion, warningsBuf, compatibilityMode, vcard);
			} finally {
				for (String warning : warningsBuf) {
					addWarning(warning, type.getTypeName());
				}
			}

			if (nestedVCard == null) {
				writer.writeProperty(type.getGroup(), type.getTypeName(), parameters, value);
			} else {
				if (targetVersion == VCardVersion.V2_1) {
					//write a nested vCard (2.1 style)
					writer.writeProperty(type.getGroup(), type.getTypeName(), parameters, value);
					write(nestedVCard, false);
				} else {
					//write an embedded vCard (3.0 style)
					StringWriter sw = new StringWriter();
					VCardWriter agentWriter = new VCardWriter(sw, targetVersion, null, "\n");
					agentWriter.setAddProdId(false);
					agentWriter.setCompatibilityMode(compatibilityMode);
					try {
						agentWriter.write(nestedVCard);
					} catch (IOException e) {
						//writing to a string
					} finally {
						for (String w : agentWriter.getWarnings()) {
							addWarning("Problem marshalling embedded vCard value: " + w, type.getTypeName());
						}
					}

					String vCardStr = sw.toString();
					vCardStr = VCardStringUtils.escape(vCardStr);
					writer.writeProperty(type.getGroup(), type.getTypeName(), parameters, vCardStr);
				}
			}
		}

		writer.writeEndComponent("VCARD");
	}

	private boolean supportsTargetVersion(VCardType type) {
		for (VCardVersion version : type.getSupportedVersions()) {
			if (version == writer.getVersion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}

	private void addWarning(String message, String propertyName) {
		warnings.add(propertyName + " property: " + message);
	}
}
