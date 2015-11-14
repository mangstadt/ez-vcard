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
 * File file = new File("vcard.vcf");
 * VCardWriter writer = null;
 * try {
 *   writer = new VCardWriter(file, VCardVersion.V3_0);
 *   writer.write(vcard1);
 *   writer.write(vcard2);
 * } finally {
 *   if (writer != null) writer.close();
 * }
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
 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
 */
public class VCardWriter extends StreamWriter implements Flushable {
	private final VCardRawWriter writer;
	private final LinkedList<Boolean> prodIdStack = new LinkedList<Boolean>();
	private boolean outlookCompatibility = false;

	/**
	 * @param out the output stream to write to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion) {
		this((targetVersion == VCardVersion.V4_0) ? utf8Writer(out) : new OutputStreamWriter(out), targetVersion);
	}

	/**
	 * @param file the file to write to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(File file, VCardVersion targetVersion) throws IOException {
		this(file, false, targetVersion);
	}

	/**
	 * @param file the file to write to
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
	 * @param writer the writer to write to
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

	/**
	 * <p>
	 * Gets whether vCards generated by this writer will be compatible with
	 * Microsoft Outlook mail clients. This setting is disabled by default.
	 * </p>
	 * <p>
	 * Enabling this setting adds an empty line after all base64-encoded
	 * property values for vCard versions 2.1 and 3.0.
	 * </p>
	 * @return true if enabled, false if disabled (defaults to false).
	 */
	public boolean isOutlookCompatibility() {
		return outlookCompatibility;
	}

	/**
	 * <p>
	 * Sets whether vCards generated by this writer should be fully compatible
	 * with Microsoft Outlook mail clients. This setting is disabled by default.
	 * </p>
	 * <p>
	 * Enabling this setting may make the vCards incompatible with other vCard
	 * consumers.
	 * </p>
	 * <p>
	 * Enabling this setting adds an empty line after all base64-encoded
	 * property values for vCards with versions 2.1 and 3.0. This setting has no
	 * effect on 4.0 vCards, or on vCards that do not have any properties with
	 * base64-encoded values.
	 * </p>
	 * @param enable true to enable, false to disable (defaults to false).
	 */
	public void setOutlookCompatibility(boolean enable) {
		outlookCompatibility = enable;
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
						//should never be thrown because we're writing to a string
					} finally {
						IOUtils.closeQuietly(agentWriter);
					}

					String vcardStr = sw.toString();
					vcardStr = VCardPropertyScribe.escape(vcardStr);
					writer.writeProperty(property.getGroup(), scribe.getPropertyName(), parameters, vcardStr);
				}
				continue;
			}

			/*
			 * Set the property's data type.
			 * 
			 * Only add a VALUE parameter if the data type is: (1) not "unknown"
			 * (2) different from the property's default data type (3) not the
			 * date/time special case (see code)
			 */
			VCardDataType dataType = scribe.dataType(property, targetVersion);
			if (dataType != null) {
				VCardDataType defaultDataType = scribe.defaultDataType(targetVersion);
				if (dataType != defaultDataType && !isDateTimeValueParameterSpecialCase(defaultDataType, dataType)) {
					parameters.setValue(dataType);
				}
			}

			//write the property
			writer.writeProperty(property.getGroup(), scribe.getPropertyName(), parameters, value);

			insertEmptyLineForOutlook(targetVersion, property);
		}

		writer.writeEndComponent("VCARD");
	}

	/**
	 * Outlook 2010 requires an empty line after base64 values (at least, some
	 * of the time).
	 * @param targetVersion the vCard version
	 * @param property the property being written
	 * @see <a href="https://github.com/mangstadt/ez-vcard/issues/21">Issue
	 * 21</a>
	 */
	private void insertEmptyLineForOutlook(VCardVersion targetVersion, VCardProperty property) throws IOException {
		if (!outlookCompatibility) {
			//setting not enabled
			return;
		}

		if (targetVersion == VCardVersion.V4_0) {
			//only do this for 2.1 and 3.0 vCards
			return;
		}

		if (!(property instanceof BinaryProperty)) {
			//property does not have binary data
			return;
		}

		BinaryProperty<?> binaryProperty = (BinaryProperty<?>) property;
		if (binaryProperty.getData() == null) {
			//property value is not base64-encoded
			return;
		}

		writer.getFoldedLineWriter().writeln("");
	}

	/**
	 * Determines if the given default data type is "date-and-or-time" and the
	 * given data type is time-based. Properties that meet this criteria should
	 * NOT be given a VALUE parameter.
	 * @param defaultDataType the property's default data type
	 * @param dataType the current property instance's data type
	 * @return true if the default data type is "date-and-or-time" and the data
	 * type is time-based, false otherwise
	 */
	private boolean isDateTimeValueParameterSpecialCase(VCardDataType defaultDataType, VCardDataType dataType) {
		//@formatter:off
		return
		defaultDataType == VCardDataType.DATE_AND_OR_TIME &&
		(
			dataType == VCardDataType.DATE ||
			dataType == VCardDataType.DATE_TIME ||
			dataType == VCardDataType.TIME
		);
		//@formatter:on
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
