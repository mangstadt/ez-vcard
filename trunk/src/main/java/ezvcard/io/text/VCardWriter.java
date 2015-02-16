package ezvcard.io.text;

import static ezvcard.util.IOUtils.utf8Writer;

import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamWriter;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.BinaryProperty;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * VCardWriter vcardWriter = new VCardWriter(file, VCardVersion.V3_0);
 * vcardWriter.write(vcard1);
 * vcardWriter.write(vcard2);
 * vcardWriter.close();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Changing the line folding settings:</b>
 * 
 * <pre class="brush:java">
 * VCardWriter writer = new VCardWriter(...);
 * 
 * //disable line folding
 * writer.getRawWriter().getFoldedLineWriter().setLineLength(null);
 * 
 * //change line length
 * writer.getRawWriter().getFoldedLineWriter().setLineLength(50);
 * 
 * //change folded line indent string
 * writer.getRawWriter().getFoldedLineWriter().setIndent("\t");
 * 
 * //change newline character
 * writer.getRawWriter().getFoldedLineWriter().setNewline("**");
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public class VCardWriter extends StreamWriter implements Flushable {
	private final VCardRawWriter writer;
	private final LinkedList<Boolean> prodIdStack = new LinkedList<Boolean>();

	/**
	 * Creates a vCard writer.
	 * @param out the output stream to write the vCard to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion) {
		this((targetVersion == VCardVersion.V4_0) ? utf8Writer(out) : new OutputStreamWriter(out), targetVersion);
	}

	/**
	 * Creates a vCard writer.
	 * @param file the file to write the vCard to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, VCardVersion targetVersion) throws IOException {
		this(file, false, targetVersion);
	}

	/**
	 * Creates a vCard writer.
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
	 * @param writer the writer to write the vCard to
	 * @param targetVersion the version that the vCards should conform to
	 */
	public VCardWriter(Writer writer, VCardVersion targetVersion) {
		this.writer = new VCardRawWriter(writer, targetVersion);
	}

	/**
	 * Gets the writer that this object wraps.
	 * @return the raw writer
	 */
	public VCardRawWriter getRawWriter() {
		return writer;
	}

	/**
	 * Gets the version that the vCards should adhere to.
	 * @return the vCard version
	 */
	@Override
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

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void _write(VCard vcard, List<VCardProperty> propertiesToAdd) throws IOException {
		VCardVersion targetVersion = getTargetVersion();
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
					prodIdStack.add(addProdId);
					addProdId = false;
					write(nestedVCard);
					addProdId = prodIdStack.removeLast();
				} else {
					//write an embedded vCard (3.0 style)
					StringWriter sw = new StringWriter();
					VCardWriter agentWriter = new VCardWriter(sw, targetVersion);
					agentWriter.getRawWriter().getFoldedLineWriter().setLineLength(null);
					agentWriter.getRawWriter().getFoldedLineWriter().setNewline("\n");
					agentWriter.setAddProdId(false);
					agentWriter.setVersionStrict(versionStrict);
					try {
						agentWriter.write(nestedVCard);
					} catch (IOException e) {
						//writing to a string
					} finally {
						IOUtils.closeQuietly(agentWriter);
					}

					String vcardStr = sw.toString();
					vcardStr = VCardPropertyScribe.escape(vcardStr);
					writer.writeProperty(property.getGroup(), scribe.getPropertyName(), parameters, vcardStr);
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

				//Outlook 2010 requires an empty line after base64 values (at least, some of the time)
				//https://code.google.com/p/ez-vcard/issues/detail?id=21
				if (getTargetVersion() != VCardVersion.V4_0 && property instanceof BinaryProperty) {
					BinaryProperty binaryProperty = (BinaryProperty) property;
					if (binaryProperty.getData() != null) {
						writer.getFoldedLineWriter().writeln("");
					}
				}
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
