package ezvcard.io.json;

import static ezvcard.util.IOUtils.utf8Writer;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamWriter;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;

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
 * Writes {@link VCard} objects to a JSON data stream (jCard format).
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * VCard vcard1 = ...
 * VCard vcard2 = ...
 * File file = new File("vcard.json");
 * JCardWriter writer = null;
 * try {
 *   writer = new JCardWriter(file);
 *   writer.write(vcard1);
 *   writer.write(vcard2);
 * } finally {
 *   if (writer != null) writer.close();
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
 */
public class JCardWriter extends StreamWriter implements Flushable {
	private final JCardRawWriter writer;
	private final VCardVersion targetVersion = VCardVersion.V4_0;

	/**
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 */
	public JCardWriter(OutputStream out) {
		this(utf8Writer(out));
	}

	/**
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 * @param wrapInArray true to enclose all written vCards in a JSON array,
	 * false not to
	 */
	public JCardWriter(OutputStream out, boolean wrapInArray) {
		this(utf8Writer(out), wrapInArray);
	}

	/**
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @throws IOException if there's a problem opening the file
	 */
	public JCardWriter(File file) throws IOException {
		this(utf8Writer(file));
	}

	/**
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param wrapInArray true to enclose all written vCards in a JSON array,
	 * false not to
	 * @throws IOException if there's a problem opening the file
	 */
	public JCardWriter(File file, boolean wrapInArray) throws IOException {
		this(utf8Writer(file), wrapInArray);
	}

	/**
	 * @param writer the writer to write to
	 */
	public JCardWriter(Writer writer) {
		this(writer, false);
	}

	/**
	 * @param writer the writer to write to
	 * @param wrapInArray true to enclose all written vCards in a JSON array,
	 * false not to
	 */
	public JCardWriter(Writer writer, boolean wrapInArray) {
		this.writer = new JCardRawWriter(writer, wrapInArray);
	}

	/**
	 * Writes a vCard to the stream.
	 * @param vcard the vCard that is being written
	 * @param properties the properties to write
	 * @throws IOException if there's a problem writing to the output stream
	 * @throws IllegalArgumentException if a scribe hasn't been registered for a
	 * custom property class (see: {@link #registerScribe registerScribe})
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void _write(VCard vcard, List<VCardProperty> properties) throws IOException {
		writer.writeStartVCard();
		writer.writeProperty("version", VCardDataType.TEXT, JCardValue.single(targetVersion.getVersion()));

		for (VCardProperty property : properties) {
			VCardPropertyScribe scribe = index.getPropertyScribe(property);

			//marshal the value
			JCardValue value;
			try {
				value = scribe.writeJson(property);
			} catch (SkipMeException e) {
				//property has requested not to be written
				continue;
			} catch (EmbeddedVCardException e) {
				//don't write because jCard does not support embedded vCards
				continue;
			}

			String group = property.getGroup();
			String name = scribe.getPropertyName().toLowerCase();
			VCardParameters parameters = scribe.prepareParameters(property, targetVersion, vcard);
			VCardDataType dataType = scribe.dataType(property, targetVersion);

			writer.writeProperty(group, name, parameters, dataType, value);
		}

		writer.writeEndVCard();
	}

	@Override
	protected VCardVersion getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Gets whether or not the JSON will be pretty-printed.
	 * @return true if it will be pretty-printed, false if not (defaults to
	 * false)
	 */
	public boolean isIndent() {
		return writer.isIndent();
	}

	/**
	 * Sets whether or not to pretty-print the JSON.
	 * @param indent true to pretty-print it, false not to (defaults to false)
	 */
	public void setIndent(boolean indent) {
		writer.setIndent(indent);
	}

	/**
	 * Flushes the jCard data stream.
	 * @throws IOException if there's a problem flushing the stream
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Ends the jCard data stream, but does not close the underlying writer.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void closeJsonStream() throws IOException {
		writer.closeJsonStream();
	}

	/**
	 * Ends the jCard data stream and closes the underlying writer.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void close() throws IOException {
		writer.close();
	}
}
