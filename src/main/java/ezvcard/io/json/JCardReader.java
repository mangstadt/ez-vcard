package ezvcard.io.json;

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
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.json.JCardRawReader.JCardDataStreamListener;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.VCardSubTypes;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;

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
	private ScribeIndex index = new ScribeIndex();
	private final List<String> warnings = new ArrayList<String>();
	private final JCardRawReader reader;

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
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(VCardPropertyScribe<? extends VCardProperty> scribe) {
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
		private VCard vcard = null;
		private boolean versionFound = false;

		public void beginVCard() {
			vcard = new VCard();
			vcard.setVersion(VCardVersion.V4_0);
		}

		public void readProperty(String group, String propertyName, VCardSubTypes parameters, VCardDataType dataType, JCardValue value) {
			if ("version".equalsIgnoreCase(propertyName)) {
				//don't unmarshal "version" because we don't treat it as a property
				versionFound = true;

				VCardVersion version = VCardVersion.valueOfByStr(value.asSingle());
				if (version != VCardVersion.V4_0) {
					addWarning("Version must be \"" + VCardVersion.V4_0.getVersion() + "\"", propertyName);
				}
				return;
			}

			VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(propertyName);
			if (scribe == null) {
				scribe = new RawPropertyScribe(propertyName);
			}

			VCardProperty property;
			try {
				Result<? extends VCardProperty> result = scribe.parseJson(value, dataType, parameters);

				for (String warning : result.getWarnings()) {
					addWarning(warning, propertyName);
				}

				property = result.getProperty();
				property.setGroup(group);
			} catch (SkipMeException e) {
				addWarning("Property has requested that it be skipped: " + e.getMessage(), propertyName);
				return;
			} catch (CannotParseException e) {
				scribe = new RawPropertyScribe(propertyName);
				Result<? extends VCardProperty> result = scribe.parseJson(value, dataType, parameters);

				property = result.getProperty();
				property.setGroup(group);

				String valueStr = ((RawProperty) property).getValue();
				addWarning("Property value could not be parsed.  Property will be saved as an extended type instead." + NEWLINE + "  Value: " + valueStr + NEWLINE + "  Reason: " + e.getMessage(), propertyName);
			} catch (EmbeddedVCardException e) {
				addWarning("Property will not be unmarshalled because jCard does not supported embedded vCards.", propertyName);
				return;
			}

			vcard.addType(property);
		}
	}
}
