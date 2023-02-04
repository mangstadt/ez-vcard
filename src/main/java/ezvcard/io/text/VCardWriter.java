package ezvcard.io.text;

import static com.github.mangstadt.vinnie.Utils.escapeNewlines;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.github.mangstadt.vinnie.VObjectParameters;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues;
import com.github.mangstadt.vinnie.io.VObjectWriter;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamWriter;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.BinaryProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.VCardProperty;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * Path file = Paths.get("vcard.vcf");
 * try (VCardWriter writer = new VCardWriter(file, VCardVersion.V3_0)) {
 *   writer.write(vcard1);
 *   writer.write(vcard2);
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
 * writer.getVObjectWriter().getFoldedLineWriter().setLineLength(null);
 * 
 * //change line length
 * writer.getVObjectWriter().getFoldedLineWriter().setLineLength(50);
 * 
 * //change folded line indent string
 * writer.getVObjectWriter().getFoldedLineWriter().setIndent("\t");
 * 
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
 */
public class VCardWriter extends StreamWriter implements Flushable {
	private final VObjectWriter writer;
	private final List<Boolean> prodIdStack = new ArrayList<>();
	private VCardVersion targetVersion;
	private TargetApplication targetApplication;
	private Boolean includeTrailingSemicolons;

	/**
	 * @param out the output stream to write to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 */
	public VCardWriter(OutputStream out, VCardVersion targetVersion) {
		this(new OutputStreamWriter(out, (targetVersion == VCardVersion.V4_0) ? StandardCharsets.UTF_8 : Charset.defaultCharset()), targetVersion);
	}

	/**
	 * @param file the file to write to
	 * @param targetVersion the version that the vCards should conform to (if
	 * set to "4.0", vCards will be written in UTF-8 encoding)
	 * @throws IOException if there's a problem opening the file
	 */
	public VCardWriter(Path file, VCardVersion targetVersion) throws IOException {
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
	public VCardWriter(Path file, boolean append, VCardVersion targetVersion) throws IOException {
		//@formatter:off
		this(
			Files.newBufferedWriter(
				file,
				(targetVersion == VCardVersion.V4_0) ? StandardCharsets.UTF_8 : Charset.defaultCharset(),
				append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING
			),
			targetVersion
		);
		//@formatter:on
	}

	/**
	 * @param writer the writer to write to
	 * @param targetVersion the version that the vCards should conform to
	 */
	public VCardWriter(Writer writer, VCardVersion targetVersion) {
		this.writer = new VObjectWriter(writer, targetVersion.getSyntaxStyle());
		this.targetVersion = targetVersion;
	}

	/**
	 * Gets the writer that this object uses to write data to the output stream.
	 * @return the writer
	 */
	public VObjectWriter getVObjectWriter() {
		return writer;
	}

	/**
	 * Gets the version that the vCards should adhere to.
	 * @return the vCard version
	 */
	@Override
	public VCardVersion getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Sets the version that the vCards should adhere to.
	 * @param targetVersion the vCard version
	 */
	public void setTargetVersion(VCardVersion targetVersion) {
		writer.setSyntaxStyle(targetVersion.getSyntaxStyle());
		this.targetVersion = targetVersion;
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
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values. It is
	 * only supported by vCard versions 3.0 and 4.0.
	 * </p>
	 * 
	 * <p>
	 * Note that this encoding mechanism is defined separately from the vCard
	 * specification and may not be supported by the consumer of the vCard.
	 * </p>
	 * @return true if circumflex accent encoding is enabled, false if not
	 * @see VObjectWriter#isCaretEncodingEnabled()
	 */
	public boolean isCaretEncodingEnabled() {
		return writer.isCaretEncodingEnabled();
	}

	/**
	 * <p>
	 * Sets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values. It is
	 * only supported by vCard versions 3.0 and 4.0.
	 * </p>
	 * 
	 * <p>
	 * Note that this encoding mechanism is defined separately from the vCard
	 * specification and may not be supported by the consumer of the vCard.
	 * </p>
	 * @param enable true to use circumflex accent encoding, false not to
	 * @see VObjectWriter#setCaretEncodingEnabled(boolean)
	 */
	public void setCaretEncodingEnabled(boolean enable) {
		writer.setCaretEncodingEnabled(enable);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void _write(VCard vcard, List<VCardProperty> propertiesToAdd) throws IOException {
		VCardVersion targetVersion = getTargetVersion();
		TargetApplication targetApplication = getTargetApplication();

		Boolean includeTrailingSemicolons = this.includeTrailingSemicolons;
		if (includeTrailingSemicolons == null) {
			includeTrailingSemicolons = (targetVersion == VCardVersion.V4_0);
		}

		WriteContext context = new WriteContext(targetVersion, targetApplication, includeTrailingSemicolons);

		writer.writeBeginComponent("VCARD");
		writer.writeVersion(targetVersion.getVersion());

		for (VCardProperty property : propertiesToAdd) {
			VCardPropertyScribe scribe = index.getPropertyScribe(property);

			String value = null;
			VCard nestedVCard = null;
			try {
				value = scribe.writeText(property, context);
			} catch (SkipMeException e) {
				continue;
			} catch (EmbeddedVCardException e) {
				nestedVCard = e.getVCard();
			}

			VCardParameters parameters = scribe.prepareParameters(property, targetVersion, vcard);

			if (nestedVCard != null) {
				writeNestedVCard(nestedVCard, property, scribe, parameters, value);
				continue;
			}

			handleValueParameter(property, scribe, parameters);
			handleLabelParameter(property, parameters);
			handleQuotedPrintableEncodingParameter(property, parameters);

			writer.writeProperty(property.getGroup(), scribe.getPropertyName(), new VObjectParameters(parameters.getMap()), value);

			fixBinaryPropertyForOutlook(property);
		}

		writer.writeEndComponent("VCARD");
	}

	@SuppressWarnings("rawtypes")
	private void writeNestedVCard(VCard nestedVCard, VCardProperty property, VCardPropertyScribe scribe, VCardParameters parameters, String value) throws IOException {
		if (targetVersion == VCardVersion.V2_1) {
			//write a nested vCard (2.1 style)
			writer.writeProperty(property.getGroup(), scribe.getPropertyName(), new VObjectParameters(parameters.getMap()), value);
			prodIdStack.add(addProdId);
			addProdId = false;
			write(nestedVCard);
			addProdId = prodIdStack.remove(prodIdStack.size() - 1);
		} else {
			//write an embedded vCard (3.0 style)
			StringWriter sw = new StringWriter();
			try (VCardWriter agentWriter = new VCardWriter(sw, targetVersion)) {
				agentWriter.getVObjectWriter().getFoldedLineWriter().setLineLength(null);
				agentWriter.setAddProdId(false);
				agentWriter.setCaretEncodingEnabled(isCaretEncodingEnabled());
				agentWriter.setIncludeTrailingSemicolons(this.includeTrailingSemicolons);
				agentWriter.setScribeIndex(index);
				agentWriter.setTargetApplication(targetApplication);
				agentWriter.setVersionStrict(versionStrict);
				agentWriter.write(nestedVCard);
			} catch (IOException ignore) {
				//should never be thrown because we're writing to a string
			}

			String vcardStr = sw.toString();
			vcardStr = VObjectPropertyValues.escape(vcardStr);
			writer.writeProperty(property.getGroup(), scribe.getPropertyName(), new VObjectParameters(parameters.getMap()), vcardStr);
		}
	}

	/**
	 * <p>
	 * Sets the property's VALUE parameter. This method only adds a VALUE
	 * parameter if all the following conditions are met:
	 * </p>
	 * <ol>
	 * <li>The data type is NOT "unknown"</li>
	 * <li>The data type is different from the property's default data type</li>
	 * <li>The data type does not fall under the "date/time special case" (see
	 * {@link #isDateTimeValueParameterSpecialCase})</li>
	 * </ol>
	 * @param property the property
	 * @param scribe the property scribe
	 * @param parameters the property parameters
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleValueParameter(VCardProperty property, VCardPropertyScribe scribe, VCardParameters parameters) {
		VCardDataType dataType = scribe.dataType(property, targetVersion);
		if (dataType == null) {
			return;
		}

		VCardDataType defaultDataType = scribe.defaultDataType(targetVersion);
		if (dataType == defaultDataType) {
			return;
		}

		if (isDateTimeValueParameterSpecialCase(defaultDataType, dataType)) {
			return;
		}

		parameters.setValue(dataType);
	}

	/**
	 * <p>
	 * Escapes newline sequences in the LABEL parameter of {@link Address}
	 * properties. Newlines cannot normally be escaped in parameter values.
	 * </p>
	 * <p>
	 * Only version 4.0 allows this (and only version 4.0 defines a LABEL
	 * parameter), but this method does this for all versions for compatibility.
	 * </p>
	 * @param property the property
	 * @param parameters the property parameters
	 */
	private void handleLabelParameter(VCardProperty property, VCardParameters parameters) {
		if (!(property instanceof Address)) {
			return;
		}

		String label = parameters.getLabel();
		if (label == null) {
			return;
		}

		label = escapeNewlines(label);
		parameters.setLabel(label);
	}

	/**
	 * Disables quoted-printable encoding on the given property if the target
	 * vCard version does not support this encoding scheme.
	 * @param property the property
	 * @param parameters the property parameters
	 */
	private void handleQuotedPrintableEncodingParameter(VCardProperty property, VCardParameters parameters) {
		if (targetVersion == VCardVersion.V2_1) {
			return;
		}

		if (parameters.getEncoding() == Encoding.QUOTED_PRINTABLE) {
			parameters.setEncoding(null);
			parameters.setCharset(null);
		}
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

		writer.getFoldedLineWriter().writeln();
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
	 * Flushes the output stream.
	 * @throws IOException if there's a problem flushing the output stream
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Closes the output stream.
	 * @throws IOException if there's a problem closing the output stream
	 */
	public void close() throws IOException {
		writer.close();
	}
}
