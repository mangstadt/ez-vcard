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
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.ValueParameter;
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
 * Converts vCards to string representations.
 * @author Michael Angstadt
 */
public class JCardWriter implements Closeable {
	private static final String newline = System.getProperty("line.separator");
	private final Writer writer;
	private JsonGenerator jg;
	private VCardVersion targetVersion = VCardVersion.V4_0;
	private final List<String> warnings = new ArrayList<String>();
	private boolean addProdId = true;
	private boolean indent = false;

	/**
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param out the output stream to write the vCard to
	 */
	public JCardWriter(OutputStream out) {
		this(new OutputStreamWriter(out));
	}

	/**
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem opening the file
	 */
	public JCardWriter(File file) throws IOException {
		this(new FileWriter(file));
	}

	/**
	 * Creates a vCard writer (writes v3.0 vCards and uses the standard folding
	 * scheme and newline sequence).
	 * @param writer the writer to write the vCard to
	 */
	public JCardWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Writes a vCard
	 * @param vcard the vCard to write
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(final VCard vcard) throws IOException {
		warnings.clear();

		if (jg == null) {
			JsonFactory factory = new JsonFactory();
			factory.configure(Feature.AUTO_CLOSE_TARGET, false);
			jg = factory.createJsonGenerator(writer);

			jg.writeStartArray();
			jg.writeString("vcardstream");
		}

		jg.writeStartArray();
		indent(2);
		jg.writeString("vcard");
		jg.writeStartArray();

		if (vcard.getFormattedName() == null) {
			warnings.add("vCard version " + targetVersion + " requires that a formatted name be defined.");
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
				warnings.add(type.getTypeName() + " is not supported by vCard version " + targetVersion + " and will not be added to the vCard.  Supported versions are: " + Arrays.toString(type.getSupportedVersions()));
				continue;
			}

			//check for correct KIND value if there are MEMBER types
			if (type instanceof MemberType && (vcard.getKind() == null || !vcard.getKind().isGroup())) {
				warnings.add("KIND must be set to \"group\" in order to add MEMBER properties to the vCard.");
				continue;
			}

			typesToAdd.add(type);
		}

		//add an extended type saying it was generated by this library
		if (addProdId) {
			EzvcardProdIdType prodId = new EzvcardProdIdType(targetVersion);
			typesToAdd.add(prodId);
		}

		List<String> warningsBuf = new ArrayList<String>();
		for (VCardType type : typesToAdd) {
			//marshal the value
			warningsBuf.clear();
			JCardValue value = null;
			try {
				value = type.marshalJson(targetVersion, warningsBuf);
			} catch (SkipMeException e) {
				warningsBuf.add(type.getTypeName() + " property will not be marshalled: " + e.getMessage());
				continue;
			} catch (EmbeddedVCardException e) {
				warningsBuf.add(type.getTypeName() + " property will not be marshalled: jCard does not supported embedded vCards.");
			} finally {
				warnings.addAll(warningsBuf);
			}

			//marshal the sub types
			warningsBuf.clear();
			VCardSubTypes subTypes;
			try {
				subTypes = type.marshalSubTypes(targetVersion, warningsBuf, CompatibilityMode.RFC, vcard);
			} finally {
				warnings.addAll(warningsBuf);
			}
			subTypes.removeAll(ValueParameter.NAME); //remove all VALUE parameters

			//add the group as a parameter
			if (type.getGroup() != null) {
				subTypes.put("group", type.getGroup());
			}

			jg.writeStartArray(); //start property
			indent(4);

			//write property name
			jg.writeString(type.getTypeName().toLowerCase());

			//write parameters
			jg.writeStartObject();
			for (Map.Entry<String, List<String>> entry : subTypes) {
				String name = entry.getKey().toLowerCase();
				List<String> values = entry.getValue();
				if (values.isEmpty()) {
					continue;
				}

				if (values.size() == 1) {
					jg.writeStringField(name, values.get(0));
				} else {
					jg.writeArrayFieldStart(name);
					for (String paramValue : values) {
						jg.writeString(paramValue);
					}
					jg.writeEndArray();
				}
			}
			jg.writeEndObject();

			//write data type
			jg.writeString(value.getDataType().toString());

			//write value
			if (value.isStructured()) {
				jg.writeStartArray();
			}
			if (value.getValues().isEmpty()) {
				jg.writeString("");
			} else {
				for (List<Object> theValue : value.getValues()) {
					if (theValue.isEmpty()) {
						jg.writeString("");
						continue;
					}

					if (theValue.size() > 1) {
						jg.writeStartArray();
					}
					for (Object v : theValue) {
						if (v == null) {
							jg.writeString("");
						} else if (v instanceof Byte) {
							jg.writeNumber((Byte) v);
						} else if (v instanceof Short) {
							jg.writeNumber((Short) v);
						} else if (v instanceof Integer) {
							jg.writeNumber((Integer) v);
						} else if (v instanceof Long) {
							jg.writeNumber((Long) v);
						} else if (v instanceof Float) {
							jg.writeNumber((Float) v);
						} else if (v instanceof Double) {
							jg.writeNumber((Double) v);
						} else if (v instanceof Boolean) {
							jg.writeBoolean((Boolean) v);
						} else {
							jg.writeString(v.toString());
						}
					}
					if (theValue.size() > 1) {
						jg.writeEndArray();
					}
				}
			}
			if (value.isStructured()) {
				jg.writeEndArray();
			}

			jg.writeEndArray(); //end property
		}

		indent(2);
		jg.writeEndArray();
		jg.writeEndArray();
	}

	private boolean supportsTargetVersion(VCardType type) {
		for (VCardVersion version : type.getSupportedVersions()) {
			if (version == targetVersion) {
				return true;
			}
		}
		return false;
	}

	private void indent(int spaces) throws IOException {
		if (indent) {
			jg.writeRaw(newline);
			for (int i = 0; i < spaces; i++) {
				jg.writeRaw(' ');
			}
		}
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
	 * Gets whether the JSON will be pretty-printed or not.
	 * @return true if it will be pretty-printed, false if not (defaults to
	 * false)
	 */
	public boolean isIndent() {
		return indent;
	}

	/**
	 * Sets whether to pretty-print the JSON or not.
	 * @param indent true to pretty-print it, false not to (defaults to false)
	 */
	public void setIndent(boolean indent) {
		this.indent = indent;
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
	 * Ends the jCard data stream, but does <b>not</b> close the underlying
	 * writer.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void endJsonStream() throws IOException {
		if (jg != null) {
			indent(0);
			jg.writeEndArray();
			jg.close();
		}
	}

	/**
	 * Ends the jCard data stream and closes the underlying writer.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void close() throws IOException {
		endJsonStream();
		writer.close();
	}
}
