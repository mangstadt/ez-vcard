package ezvcard.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.KindType;
import ezvcard.types.MemberType;
import ezvcard.types.ProdIdType;
import ezvcard.types.TextType;
import ezvcard.types.VCardType;
import ezvcard.util.JCardValue;

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
 * Writes {@link VCard} objects to a JSON data stream (jCard format).
 * @author Michael Angstadt
 */
public class JCardWriter implements Closeable {
	private final JCardRawWriter writer;
	private final List<String> warnings = new ArrayList<String>();
	private final VCardVersion targetVersion = VCardVersion.V4_0;
	private boolean addProdId = true;

	/**
	 * Creates a jCard writer.
	 * @param out the output stream to write the vCard to
	 */
	public JCardWriter(OutputStream out) {
		this(new OutputStreamWriter(out));
	}

	/**
	 * Creates a jCard writer.
	 * @param out the output stream to write the vCard to
	 * @param wrapInArray true to enclose all written vCards in a JSON array,
	 * false not to
	 */
	public JCardWriter(OutputStream out, boolean wrapInArray) {
		this(new OutputStreamWriter(out), wrapInArray);
	}

	/**
	 * Creates a jCard writer.
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem opening the file
	 */
	public JCardWriter(File file) throws IOException {
		this(new FileWriter(file));
	}

	/**
	 * Creates a jCard writer.
	 * @param file the file to write the vCard to
	 * @param wrapInArray true to enclose all written vCards in a JSON array,
	 * false not to
	 * @throws IOException if there's a problem opening the file
	 */
	public JCardWriter(File file, boolean wrapInArray) throws IOException {
		this(new FileWriter(file), wrapInArray);
	}

	/**
	 * Creates a jCard writer.
	 * @param writer the writer to write the vCard to
	 */
	public JCardWriter(Writer writer) {
		this(writer, false);
	}

	/**
	 * Creates a jCard writer.
	 * @param writer the writer to write the vCard to
	 * @param wrapInArray true to enclose all written vCards in a JSON array,
	 * false not to
	 */
	public JCardWriter(Writer writer, boolean wrapInArray) {
		this.writer = new JCardRawWriter(writer, wrapInArray);
	}

	/**
	 * Writes a vCard to the stream.
	 * @param vcard the vCard to write
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(final VCard vcard) throws IOException {
		warnings.clear();

		if (vcard.getFormattedName() == null) {
			warnings.add("vCard version " + targetVersion + " requires that a formatted name property be defined.");
		}

		List<VCardType> typesToAdd = new ArrayList<VCardType>();
		typesToAdd.add(new TextType("version", targetVersion.getVersion()));

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
		}

		//add an extended type saying it was generated by this library
		if (addProdId) {
			EzvcardProdIdType prodId = new EzvcardProdIdType(targetVersion);
			typesToAdd.add(prodId);
		}

		writer.writeStartVCard();

		List<String> warningsBuf = new ArrayList<String>();
		for (VCardType type : typesToAdd) {
			//marshal the value
			warningsBuf.clear();
			JCardValue value = null;
			try {
				value = type.marshalJson(targetVersion, warningsBuf);
			} catch (SkipMeException e) {
				warningsBuf.add("Property has requested that it be skipped: " + e.getMessage());
				continue;
			} catch (EmbeddedVCardException e) {
				warningsBuf.add("Property will not be marshalled because jCard does not supported embedded vCards.");
			} finally {
				for (String warning : warningsBuf) {
					addWarning(warning, type.getTypeName());
				}
			}

			//marshal the sub types
			warningsBuf.clear();
			VCardSubTypes subTypes;
			try {
				subTypes = type.marshalSubTypes(targetVersion, warningsBuf, CompatibilityMode.RFC, vcard);
			} finally {
				for (String warning : warningsBuf) {
					addWarning(warning, type.getTypeName());
				}
			}
			subTypes.removeValue(); //remove all VALUE parameters

			writer.writeProperty(type.getGroup(), type.getTypeName().toLowerCase(), subTypes, value);
		}

		writer.writeEndVCard();
	}

	/**
	 * Determines if a property is supported by the target version.
	 * @param type the property
	 * @return true if it is supported, false if not
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
	 * Gets whether or not a "PRODID" type will be added to each vCard, saying
	 * that the vCard was generated by this library.
	 * @return true if it will be added, false if not (defaults to true)
	 */
	public boolean isAddProdId() {
		return addProdId;
	}

	/**
	 * Sets whether or not to add a "PRODID" type to each vCard, saying that the
	 * vCard was generated by this library.
	 * @param addProdId true to add this type, false not to (defaults to true)
	 */
	public void setAddProdId(boolean addProdId) {
		this.addProdId = addProdId;
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
	 * Gets the warnings from the last vCard that was marshalled. This list is
	 * reset every time a new vCard is written.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
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

	private void addWarning(String message, String propertyName) {
		warnings.add(propertyName + " property: " + message);
	}
}
