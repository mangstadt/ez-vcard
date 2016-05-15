package ezvcard.io.json;

import static ezvcard.util.IOUtils.utf8Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import com.fasterxml.jackson.core.JsonParser;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamReader;
import ezvcard.io.json.JCardRawReader.JCardDataStreamListener;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;

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
 * Parses {@link VCard} objects from a JSON data stream (jCard format).
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * File file = new File("vcards.json");
 * JCardReader reader = null;
 * try {
 *   reader = new JCardReader(file);
 *   VCard vcard;
 *   while ((vcard = reader.readNext()) != null){
 *     ...
 *   }
 * } finally {
 *   if (reader != null) reader.close();
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
 */
public class JCardReader extends StreamReader {
	private final JCardRawReader reader;

	/**
	 * @param json the JSON string to read from
	 */
	public JCardReader(String json) {
		this(new StringReader(json));
	}

	/**
	 * @param in the input stream to read from
	 */
	public JCardReader(InputStream in) {
		this(utf8Reader(in));
	}

	/**
	 * @param file the file to read from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public JCardReader(File file) throws FileNotFoundException {
		this(new BufferedReader(utf8Reader(file)));
	}

	/**
	 * @param reader the reader to read from
	 */
	public JCardReader(Reader reader) {
		this.reader = new JCardRawReader(reader);
	}

	/**
	 * @param parser the parser to read from
	 */
	public JCardReader(JsonParser parser) {
		this.reader = new JCardRawReader(parser, true);
	}

	@Override
	protected VCard _readNext() throws IOException {
		if (reader.eof()) {
			return null;
		}

		warnings.clear();

		JCardDataStreamListenerImpl listener = new JCardDataStreamListenerImpl();
		reader.readNext(listener);
		VCard vcard = listener.vcard;
		if (vcard != null && !listener.versionFound) {
			warnings.add(reader.getLineNum(), null, 29);
		}
		return vcard;
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

		public void readProperty(String group, String propertyName, VCardParameters parameters, VCardDataType dataType, JCardValue value) {
			if ("version".equalsIgnoreCase(propertyName)) {
				//don't unmarshal "version" because we don't treat it as a property
				versionFound = true;

				VCardVersion version = VCardVersion.valueOfByStr(value.asSingle());
				if (version != VCardVersion.V4_0) {
					warnings.add(reader.getLineNum(), propertyName, 30);
				}
				return;
			}

			VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(propertyName);
			if (scribe == null) {
				scribe = new RawPropertyScribe(propertyName);
			}

			Result<? extends VCardProperty> result;
			try {
				result = scribe.parseJson(value, dataType, parameters);
				for (String warning : result.getWarnings()) {
					warnings.add(reader.getLineNum(), warning, propertyName);
				}
			} catch (SkipMeException e) {
				warnings.add(reader.getLineNum(), propertyName, 22, e.getMessage());
				return;
			} catch (CannotParseException e) {
				scribe = new RawPropertyScribe(propertyName);
				result = scribe.parseJson(value, dataType, parameters);

				VCardProperty property = result.getProperty();
				String valueStr = ((RawProperty) property).getValue();
				warnings.add(reader.getLineNum(), propertyName, 25, valueStr, e.getMessage());
			} catch (EmbeddedVCardException e) {
				warnings.add(reader.getLineNum(), propertyName, 31);
				return;
			}

			VCardProperty property = result.getProperty();
			property.setGroup(group);
			vcard.addProperty(property);
		}
	}
}
