package ezvcard.io.text;

import static ezvcard.util.IOUtils.utf8Writer;

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

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.ScribeIndex;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.LabelType;
import ezvcard.types.ProdIdType;
import ezvcard.types.RawType;
import ezvcard.types.VCardType;
import ezvcard.util.IOUtils;
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
 * <p>
 * Writes {@link VCard} objects to a plain-text vCard data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * VCard vcard1 = ...
 * VCard vcard2 = ...
 * 
 * File file = new File("vcard.vcf");
 * VCardWriter vcardWriter = new VCardWriter(file);
 * vcardWriter.write(vcard1);
 * vcardWriter.write(vcard2);
 * vcardWriter.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public class VCardWriter implements Closeable {
	private ScribeIndex index = new ScribeIndex();
	private boolean addProdId = true;
	private boolean versionStrict = true;
	private final VCardRawWriter writer;

	/**
	 * Creates a writer that writes vCards to an output stream (writes v3.0
	 * vCards and uses the standard folding scheme and newline sequence).
	 * @param out the output stream to write the vCard to
	 */
	public VCardWriter(OutputStream out) {
		this(new OutputStreamWriter(out));
	}

	/**
	 * Creates a writer that writes vCards to an output stream (uses the
	 * standard folding scheme and newline sequence).
	 * @param out the output stream to write the vCard to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion) {
		this((targetVersion == VCardVersion.V4_0) ? utf8Writer(out) : new OutputStreamWriter(out), targetVersion);
	}

	/**
	 * Creates a writer that writes vCards to an output stream.
	 * @param out the output stream to write the vCard to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion, FoldingScheme foldingScheme, String newline) {
		this((targetVersion == VCardVersion.V4_0) ? utf8Writer(out) : new OutputStreamWriter(out), targetVersion, foldingScheme, newline);
	}

	/**
	 * Creates a writer that writes vCards to a file (writes v3.0 vCards and
	 * uses the standard folding scheme and newline sequence).
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file) throws IOException {
		this(new FileWriter(file, false));
	}

	/**
	 * Creates a writer that writes vCards to a file (writes v3.0 vCards and
	 * uses the standard folding scheme and newline sequence).
	 * @param file the file to write the vCard to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, boolean append) throws IOException {
		this(new FileWriter(file, append));
	}

	/**
	 * Creates a writer that writes vCards to a file (uses the standard folding
	 * scheme and newline sequence).
	 * @param file the file to write the vCard to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, boolean append, VCardVersion targetVersion) throws IOException {
		this((targetVersion == VCardVersion.V4_0) ? utf8Writer(file, append) : new FileWriter(file, append), targetVersion);
	}

	/**
	 * Creates a writer that writes vCards to a file.
	 * @param file the file to write the vCard to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, boolean append, VCardVersion targetVersion, FoldingScheme foldingScheme, String newline) throws IOException {
		this((targetVersion == VCardVersion.V4_0) ? utf8Writer(file, append) : new FileWriter(file, append), targetVersion, foldingScheme, newline);
	}

	/**
	 * Creates a writer that writes vCards to a writer (writes v3.0 vCards and
	 * uses the standard folding scheme and newline sequence).
	 * @param writer the writer to write the vCard to
	 */
	public VCardWriter(Writer writer) {
		this(writer, VCardVersion.V3_0);
	}

	/**
	 * Creates a writer that writes vCards to a writer (uses the standard
	 * folding scheme and newline sequence).
	 * @param writer the writer to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 */
	public VCardWriter(Writer writer, VCardVersion targetVersion) {
		this(writer, targetVersion, FoldingScheme.MIME_DIR, "\r\n");
	}

	/**
	 * Creates a writer that writes vCards to a writer.
	 * @param writer the writer to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public VCardWriter(Writer writer, VCardVersion targetVersion, FoldingScheme foldingScheme, String newline) {
		this.writer = new VCardRawWriter(writer, targetVersion, foldingScheme, newline);
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
	 * Gets whether properties that do not support the target version will be
	 * excluded from the written vCard.
	 * @return true to exclude properties that do not support the target
	 * version, false to include them anyway (defaults to true)
	 */
	public boolean isVersionStrict() {
		return versionStrict;
	}

	/**
	 * Sets whether properties that do not support the target version will be
	 * excluded from the written vCard.
	 * @param versionStrict true to exclude properties that do not support the
	 * target version, false to include them anyway (defaults to true)
	 */
	public void setVersionStrict(boolean versionStrict) {
		this.versionStrict = versionStrict;
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
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(VCardPropertyScribe<? extends VCardType> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the scribe index.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

	/**
	 * Writes a vCard to the stream.
	 * @param vcard the vCard to write
	 * @throws IOException if there's a problem writing to the output stream
	 * @throws IllegalArgumentException if a scribe hasn't been registered for a
	 * custom property class (see: {@link #registerScribe})
	 */
	public void write(VCard vcard) throws IOException {
		write(vcard, addProdId);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void write(VCard vcard, boolean addProdId) throws IOException {
		VCardVersion targetVersion = writer.getVersion();

		List<VCardType> typesToAdd = new ArrayList<VCardType>();
		for (VCardType type : vcard) {
			if (addProdId && type instanceof ProdIdType) {
				//do not add the PRODID in the vCard if "addProdId" is true
				continue;
			}

			if (versionStrict && !type.getSupportedVersions().contains(targetVersion)) {
				//do not add the property to the vCard if it is not supported by the target version
				continue;
			}

			//check for scribes before writing anything to the stream
			if (index.getPropertyScribe(type) == null) {
				throw new IllegalArgumentException("No scribe found for property class \"" + type.getClass().getName() + "\".");
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
			VCardType property;
			if (targetVersion == VCardVersion.V2_1) {
				property = new RawType("X-PRODID", "ezvcard " + Ezvcard.VERSION);
			} else {
				property = new ProdIdType("ezvcard " + Ezvcard.VERSION);
			}
			typesToAdd.add(property);
		}

		writer.writeBeginComponent("VCARD");
		writer.writeVersion();

		for (VCardType type : typesToAdd) {
			VCardPropertyScribe scribe = index.getPropertyScribe(type);

			//marshal the value
			String value = null;
			VCard nestedVCard = null;
			try {
				value = scribe.writeText(type, targetVersion);
			} catch (SkipMeException e) {
				continue;
			} catch (EmbeddedVCardException e) {
				nestedVCard = e.getVCard();
			}

			//marshal the sub types
			VCardSubTypes parameters = scribe.prepareParameters(type, targetVersion, vcard);

			if (nestedVCard == null) {
				//set the data type
				//only add a VALUE parameter if the data type is (1) not "unknown" and (2) different from the property's default data type
				VCardDataType dataType = scribe.dataType(type, targetVersion);
				if (dataType != null) {
					VCardDataType defaultDataType = scribe.defaultDataType(targetVersion);
					if (dataType != defaultDataType) {
						if (defaultDataType == VCardDataType.DATE_AND_OR_TIME && (dataType == VCardDataType.DATE || dataType == VCardDataType.DATE_TIME || dataType == VCardDataType.TIME)) {
							//do not write VALUE if the default data type is "date-and-or-time" and the property's data type is time-based
						} else {
							parameters.setValue(dataType);
						}
					}
				}

				writer.writeProperty(type.getGroup(), scribe.getPropertyName(), parameters, value);
			} else {
				if (targetVersion == VCardVersion.V2_1) {
					//write a nested vCard (2.1 style)
					writer.writeProperty(type.getGroup(), scribe.getPropertyName(), parameters, value);
					write(nestedVCard, false);
				} else {
					//write an embedded vCard (3.0 style)
					StringWriter sw = new StringWriter();
					VCardWriter agentWriter = new VCardWriter(sw, targetVersion, null, "\n");
					agentWriter.setAddProdId(false);
					agentWriter.setVersionStrict(versionStrict);
					try {
						agentWriter.write(nestedVCard);
					} catch (IOException e) {
						//writing to a string
					} finally {
						IOUtils.closeQuietly(agentWriter);
					}

					String vCardStr = sw.toString();
					vCardStr = VCardStringUtils.escape(vCardStr);
					writer.writeProperty(type.getGroup(), scribe.getPropertyName(), parameters, vCardStr);
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
