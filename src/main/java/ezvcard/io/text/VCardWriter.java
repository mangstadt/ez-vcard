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
import ezvcard.property.Address;
import ezvcard.property.BinaryProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 * </p>
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
 * <p>
 * <b>Changing the line folding settings:</b>
 * </p>
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
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
 */
public class VCardWriter extends StreamWriter implements Flushable {
	private final VCardRawWriter writer;
	private final LinkedList<Boolean> prodIdStack = new LinkedList<Boolean>();
	private TargetApplication targetApplication;
	private Boolean includeTrailingSemicolons;

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
	 * Gets the application that the vCards will be targeted for.
	 * </p>
	 * <p>
	 * Some vCard consumers do not completely adhere to the vCard specifications
	 * and require their vCards to be formatted in a specific way. See the
	 * {@link TargetApplication} class for a list of these applications.
	 * </p>
	 * @return the target application or null if the vCards do not be given any
	 * special processing (defaults to null)
	 */
	public TargetApplication getTargetApplication() {
		return targetApplication;
	}

	/**
	 * <p>
	 * Sets the application that the vCards will be targeted for.
	 * </p>
	 * <p>
	 * Some vCard consumers do not completely adhere to the vCard specifications
	 * and require their vCards to be formatted in a specific way. See the
	 * {@link TargetApplication} class for a list of these applications.
	 * </p>
	 * @param targetApplication the target application or null if the vCards do
	 * not require any special processing (defaults to null)
	 */
	public void setTargetApplication(TargetApplication targetApplication) {
		this.targetApplication = targetApplication;
	}

	/**
	 * <p>
	 * Gets whether this writer will include trailing semicolon delimiters for
	 * structured property values whose list of values end with null or empty
	 * values. Examples of properties that use structured values are
	 * {@link StructuredName} and {@link Address}.
	 * </p>
	 * <p>
	 * This setting exists for compatibility reasons and should not make a
	 * difference to consumers that correctly implement the vCard grammar.
	 * </p>
	 * @return true to include the trailing semicolons, false not to, null to
	 * use the default behavior (defaults to false for vCard versions 2.1 and
	 * 3.0 and true for vCard version 4.0)
	 * @see <a href="https://github.com/mangstadt/ez-vcard/issues/57">Issue
	 * 57</a>
	 */
	public Boolean isIncludeTrailingSemicolons() {
		return includeTrailingSemicolons;
	}

	/**
	 * <p>
	 * Sets whether to include trailing semicolon delimiters for structured
	 * property values whose list of values end with null or empty values.
	 * Examples of properties that use structured values are
	 * {@link StructuredName} and {@link Address}.
	 * </p>
	 * <p>
	 * This setting exists for compatibility reasons and should not make a
	 * difference to consumers that correctly implement the vCard grammar.
	 * </p>
	 * @param include true to include the trailing semicolons, false not to,
	 * null to use the default behavior (defaults to false for vCard versions
	 * 2.1 and 3.0 and true for vCard version 4.0)
	 * @see <a href="https://github.com/mangstadt/ez-vcard/issues/57">Issue
	 * 57</a>
	 */
	public void setIncludeTrailingSemicolons(Boolean include) {
		includeTrailingSemicolons = include;
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
	 * Note that this encoding mechanism is defined separately from the
	 * iCalendar specification and may not be supported by the vCard consumer.
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
	 * Note that this encoding mechanism is defined separately from the
	 * iCalendar specification and may not be supported by the vCard consumer.
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

		Boolean includeTrailingSemicolons = this.includeTrailingSemicolons;
		if (includeTrailingSemicolons == null) {
			includeTrailingSemicolons = (targetVersion == VCardVersion.V4_0);
		}

		WriteContext context = new WriteContext(targetVersion, getTargetApplication(), includeTrailingSemicolons);

		writer.writeBeginComponent("VCARD");
		writer.writeVersion();

		for (VCardProperty property : propertiesToAdd) {
			VCardPropertyScribe scribe = index.getPropertyScribe(property);

			//marshal the value
			String value = null;
			VCard nestedVCard = null;
			try {
				value = scribe.writeText(property, context);
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
					agentWriter.setIncludeTrailingSemicolons(includeTrailingSemicolons);
					agentWriter.setTargetApplication(getTargetApplication());
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

			fixBinaryPropertyForOutlook(property);
		}

		writer.writeEndComponent("VCARD");
	}

	/**
	 * @see TargetApplication#OUTLOOK
	 */
	private void fixBinaryPropertyForOutlook(VCardProperty property) throws IOException {
		if (targetApplication != TargetApplication.OUTLOOK) {
			return;
		}

		if (getTargetVersion() == VCardVersion.V4_0) {
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
