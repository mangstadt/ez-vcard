package ezvcard.io;

import static ezvcard.util.IOUtils.utf8Reader;
import static ezvcard.util.VCardStringUtils.NEWLINE;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.JCardRawReader.JCardDataStreamListener;
import ezvcard.types.RawType;
import ezvcard.types.TypeList;
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
 * <p>
 * Parses {@link VCard} objects from a JSON data stream (jCard format).
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * File file = new File("vcards.json");
 * JCardReader jcardReader = new JCardReader(file);
 * VCard vcard;
 * while ((vcard = jcardReader.readNext()) != null){
 *   ...
 * }
 * jcardReader.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-kewisch-vcard-in-json-04">jCard
 * draft</a>
 */
public class JCardReader implements Closeable {
	private final List<String> warnings = new ArrayList<String>();
	private final JCardRawReader reader;
	private final Map<String, Class<? extends VCardType>> extendedTypeClasses = new HashMap<String, Class<? extends VCardType>>();

	/**
	 * Creates a reader that parses jCards from a JSON string.
	 * @param json the JSON string
	 */
	public JCardReader(String json) {
		this(new StringReader(json));
	}

	/**
	 * Creates a reader that parses jCards from an input stream.
	 * @param in the input stream to read the vCards from
	 */
	public JCardReader(InputStream in) {
		this(utf8Reader(in));
	}

	/**
	 * Creates a reader that parses jCards from a file.
	 * @param file the file to read the vCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public JCardReader(File file) throws FileNotFoundException {
		this(utf8Reader(file));
	}

	/**
	 * Creates a reader that parses jCards from a reader.
	 * @param reader the reader to read the vCards from
	 */
	public JCardReader(Reader reader) {
		this.reader = new JCardRawReader(reader);
	}

	/**
	 * Reads the next vCard from the data stream.
	 * @return the next vCard or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	public VCard readNext() throws IOException {
		if (reader.eof()) {
			return null;
		}

		warnings.clear();

		JCardDataStreamListenerImpl listener = new JCardDataStreamListenerImpl();
		reader.readNext(listener);
		VCard vcard = listener.vcard;
		if (vcard != null && !listener.versionFound) {
			addWarning("No \"version\" property found.");
		}
		return vcard;
	}

	/**
	 * Creates the appropriate {@link VCardType} instance, given the type name.
	 * This method does not unmarshal the type, it just creates the type object.
	 * @param name the type name (e.g. "FN")
	 * @return the Type that was created
	 */
	private VCardType createTypeObject(String name) {
		//parse as a registered extended type class (extended type classes should override standard ones)
		Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(name);
		if (extendedTypeClass != null) {
			try {
				return extendedTypeClass.newInstance();
			} catch (Exception e) {
				//should never be thrown
				//the type class is checked to see if it has a public, no-arg constructor in the "registerExtendedType" method
				throw new RuntimeException("Extended property class \"" + extendedTypeClass.getName() + "\" must have a public, no-arg constructor.");
			}
		}

		//parse as a standard property
		Class<? extends VCardType> clazz = TypeList.getTypeClass(name);
		if (clazz != null) {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				//should never be thrown
				//all type classes must have public, no-arg constructors
				throw new RuntimeException(e);
			}
		}

		//parse as a RawType
		if (!name.toUpperCase().startsWith("X-")) {
			addWarning("Non-standard property \"" + name + "\" found.  Treating it as an extended property.");
		}
		return new RawType(name); //use RawType instead of TextType because we don't want to unescape any characters that might be meaningful to this type
	}

	/**
	 * Gets the type name from a type class.
	 * @param clazz the type class
	 * @return the type name
	 */
	private String getTypeNameFromTypeClass(Class<? extends VCardType> clazz) {
		try {
			VCardType t = clazz.newInstance();
			return t.getTypeName().toLowerCase();
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers an extended type class.
	 * @param clazz the extended type class to register (MUST have a public,
	 * no-arg constructor)
	 * @throws RuntimeException if the class doesn't have a public, no-arg
	 * constructor
	 */
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.put(getTypeNameFromTypeClass(clazz), clazz);
	}

	/**
	 * Removes an extended type class that was previously registered.
	 * @param clazz the extended type class to remove
	 */
	public void unregisterExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.remove(getTypeNameFromTypeClass(clazz));
	}

	/**
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	private void addWarning(String message) {
		addWarning(message, null);
	}

	private void addWarning(String message, String propertyName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(reader.getLineNum());
		if (propertyName != null) {
			sb.append(" (").append(propertyName).append(" property)");
		}
		sb.append(": ").append(message);

		warnings.add(sb.toString());
	}

	public void close() throws IOException {
		reader.close();
	}

	private class JCardDataStreamListenerImpl implements JCardDataStreamListener {
		private final List<String> warningsBuf = new ArrayList<String>();
		private VCard vcard = null;
		private boolean versionFound = false;

		public void beginVCard() {
			vcard = new VCard();
			vcard.setVersion(VCardVersion.V4_0);
		}

		public void readProperty(String group, String propertyName, VCardSubTypes parameters, JCardValue value) {
			if ("version".equalsIgnoreCase(propertyName)) {
				//don't unmarshal "version" because we don't treat it as a property
				versionFound = true;

				VCardVersion version = VCardVersion.valueOfByStr(value.asSingle());
				if (version != VCardVersion.V4_0) {
					addWarning("Version must be \"" + VCardVersion.V4_0.getVersion() + "\"", "version");
				}
				return;
			}

			VCardType type = createTypeObject(propertyName);
			type.setGroup(group);

			//unmarshal the text string into the object
			warningsBuf.clear();
			try {
				type.unmarshalJson(parameters, value, VCardVersion.V4_0, warningsBuf);
			} catch (SkipMeException e) {
				warningsBuf.add("Property has requested that it be skipped: " + e.getMessage());
				return;
			} catch (CannotParseException e) {
				type = new RawType(propertyName);
				type.setGroup(group);
				type.unmarshalJson(parameters, value, vcard.getVersion(), warningsBuf);

				String valueStr = ((RawType) type).getValue();
				warningsBuf.add("Property value could not be parsed.  Property will be saved as an extended type instead." + NEWLINE + "  Value: " + valueStr + NEWLINE + "  Reason: " + e.getMessage());
			} catch (EmbeddedVCardException e) {
				warningsBuf.add("Property will not be unmarshalled because jCard does not supported embedded vCards.");
				return;
			} finally {
				for (String warning : warningsBuf) {
					addWarning(warning, type.getTypeName());
				}
			}

			vcard.addType(type);
		}
	}
}
