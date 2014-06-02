package ezvcard.io.text;

import static ezvcard.util.IOUtils.utf8Writer;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.AbstractVCardWriter;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;

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
public class VCardWriter extends AbstractVCardWriter implements Closeable, Flushable {
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
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion) {
		this((targetVersion == VCardVersion.V4_0) ? utf8Writer(out) : new OutputStreamWriter(out), targetVersion);
	}

	/**
	 * Creates a vCard writer.
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
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file) throws IOException {
		this(new FileWriter(file, false));
	}

	/**
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param file the file to write the vCard to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, boolean append) throws IOException {
		this(new FileWriter(file, append));
	}

	/**
	 * Creates a vCard writer (uses the standard folding scheme and newline
	 * sequence).
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
	 * Creates a vCard writer.
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
	 * @throws IllegalArgumentException if a scribe hasn't been registered for a
	 * custom property class (see: {@link #registerScribe registerScribe})
	 */
	public void write(VCard vcard) throws IOException {
		write(vcard, addProdId);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void write(VCard vcard, boolean addProdId) throws IOException {
		VCardVersion targetVersion = writer.getVersion();
		List<VCardProperty> propertiesToAdd = prepare(vcard, targetVersion, addProdId);

		writer.writeBeginComponent("VCARD");
		writer.writeVersion();

		for (VCardProperty property : propertiesToAdd) {
			VCardPropertyScribe scribe = index.getPropertyScribe(property);

			//marshal the value
			String value = null;
			VCard nestedVCard = null;
			try {
				value = scribe.writeText(property, targetVersion);
			} catch (SkipMeException e) {
				continue;
			} catch (EmbeddedVCardException e) {
				nestedVCard = e.getVCard();
			}

			//marshal the parameters
			VCardParameters parameters = scribe.prepareParameters(property, targetVersion, vcard);

			//is the value a nested vCard?
			if (nestedVCard != null) {
				if (targetVersion == VCardVersion.V2_1) {
					//write a nested vCard (2.1 style)
					writer.writeProperty(property.getGroup(), scribe.getPropertyName(), parameters, value);
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
					vCardStr = VCardPropertyScribe.escape(vCardStr);
					writer.writeProperty(property.getGroup(), scribe.getPropertyName(), parameters, vCardStr);
				}
				continue;
			}

			if (value != null) {
				//set the data type
				//only add a VALUE parameter if the data type is (1) not "unknown" and (2) different from the property's default data type
				VCardDataType dataType = scribe.dataType(property, targetVersion);
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

				writer.writeProperty(property.getGroup(), scribe.getPropertyName(), parameters, value);
				continue;
			}
		}

		writer.writeEndComponent("VCARD");
	}

	/**
	 * Flushes the underlying {@link Writer} object.
	 * @throws IOException if there's a problem flushing the writer
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 * @throws IOException if there's a problem closing the writer
	 */
	public void close() throws IOException {
		writer.close();
	}
}
