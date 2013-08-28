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
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.LabelType;
import ezvcard.types.ProdIdType;
import ezvcard.types.VCardType;
import ezvcard.util.VCardStringUtils;

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
	 * Writes a vCard to the stream.
	 * @param vcard the vCard to write
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(VCard vcard) throws IOException {
		write(vcard, addProdId);
	}

	private void write(VCard vcard, boolean addProdId) throws IOException {
		VCardVersion targetVersion = writer.getVersion();

		List<VCardType> typesToAdd = new ArrayList<VCardType>();
		for (VCardType type : vcard) {
			if (addProdId && type instanceof ProdIdType) {
				//do not add the PRODID in the vCard if "addProdId" is true
				continue;
			}

			typesToAdd.add(type);

			//add LABEL types for each ADR type if the target version is 2.1 or 3.0
			if (type instanceof AddressType && (targetVersion == VCardVersion.V2_1 || targetVersion == VCardVersion.V3_0)) {
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

		for (VCardType type : typesToAdd) {
			//marshal the value
			String value = null;
			VCard nestedVCard = null;
			try {
				value = type.marshalText(targetVersion, compatibilityMode);
			} catch (SkipMeException e) {
				continue;
			} catch (EmbeddedVCardException e) {
				nestedVCard = e.getVCard();
			}

			//marshal the sub types
			VCardSubTypes parameters = type.marshalSubTypes(targetVersion, compatibilityMode, vcard);

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
					}

					String vCardStr = sw.toString();
					vCardStr = VCardStringUtils.escape(vCardStr);
					writer.writeProperty(type.getGroup(), type.getTypeName(), parameters, vCardStr);
				}
			}
		}

		writer.writeEndComponent("VCARD");
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}
}
