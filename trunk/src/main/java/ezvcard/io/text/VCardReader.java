package ezvcard.io.text;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ezvcard.Messages;
import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Label;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;
import ezvcard.util.org.apache.commons.codec.DecoderException;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
 * Parses {@link VCard} objects from a plain-text vCard data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * File file = new File("vcards.vcf");
 * VCardReader vcardReader = new VCardReader(file);
 * VCard vcard;
 * while ((vcard = vcardReader.readNext()) != null){
 *   ...
 * }
 * vcardReader.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public class VCardReader implements Closeable {
	private final List<String> warnings = new ArrayList<String>();
	private ScribeIndex index = new ScribeIndex();
	private Charset defaultQuotedPrintableCharset;
	private final VCardRawReader reader;

	/**
	 * Creates a reader that parses vCards from a string.
	 * @param str the string to read the vCards from
	 */
	public VCardReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * Creates a reader that parses vCards from an input stream.
	 * @param in the input stream to read the vCards from
	 */
	public VCardReader(InputStream in) {
		this(new InputStreamReader(in));
	}

	/**
	 * Creates a reader that parses vCards from a file.
	 * @param file the file to read the vCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public VCardReader(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	/**
	 * Creates a reader that parses vCards from a reader.
	 * @param reader the reader to read the vCards from
	 */
	public VCardReader(Reader reader) {
		this.reader = new VCardRawReader(reader);
		defaultQuotedPrintableCharset = this.reader.getEncoding();
		if (defaultQuotedPrintableCharset == null) {
			defaultQuotedPrintableCharset = Charset.defaultCharset();
		}
	}

	/**
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @see VCardRawReader#isCaretDecodingEnabled()
	 */
	public boolean isCaretDecodingEnabled() {
		return reader.isCaretDecodingEnabled();
	}

	/**
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see VCardRawReader#setCaretDecodingEnabled(boolean)
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		reader.setCaretDecodingEnabled(enable);
	}

	/**
	 * <p>
	 * Gets the character set to use when decoding quoted-printable values if
	 * the property has no CHARSET parameter, or if the CHARSET parameter is not
	 * a valid character set.
	 * </p>
	 * <p>
	 * By default, the Reader's character encoding will be used. If the Reader
	 * has no character encoding, then the system's default character encoding
	 * will be used.
	 * </p>
	 * @return the character set
	 */
	public Charset getDefaultQuotedPrintableCharset() {
		return defaultQuotedPrintableCharset;
	}

	/**
	 * <p>
	 * Sets the character set to use when decoding quoted-printable values if
	 * the property has no CHARSET parameter, or if the CHARSET parameter is not
	 * a valid character set.
	 * </p>
	 * <p>
	 * By default, the Reader's character encoding will be used. If the Reader
	 * has no character encoding, then the system's default character encoding
	 * will be used.
	 * </p>
	 * @param charset the character set
	 */
	public void setDefaultQuotedPrintableCharset(Charset charset) {
		defaultQuotedPrintableCharset = charset;
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

		VCardDataStreamListenerImpl listener = new VCardDataStreamListenerImpl();
		reader.start(listener);

		return listener.root;
	}

	/**
	 * Assigns names to all nameless parameters. v3.0 and v4.0 requires all
	 * parameters to have names, but v2.1 does not.
	 * @param parameters the parameters
	 */
	private void handleNamelessParameters(VCardParameters parameters) {
		List<String> namelessParamValues = parameters.get(null);
		for (String paramValue : namelessParamValues) {
			String paramName;
			if (VCardDataType.find(paramValue) != null) {
				paramName = VCardParameters.VALUE;
			} else if (Encoding.find(paramValue) != null) {
				paramName = VCardParameters.ENCODING;
			} else {
				//otherwise, assume it's a TYPE
				paramName = VCardParameters.TYPE;
			}
			parameters.put(paramName, paramValue);
		}
		parameters.removeAll(null);
	}

	/**
	 * <p>
	 * Accounts for multi-valued TYPE parameters being enclosed entirely in
	 * double quotes (for example: ADR;TYPE="home,work").
	 * </p>
	 * <p>
	 * Many examples throughout the 4.0 specs show TYPE parameters being encoded
	 * in this way. This conflicts with the ABNF and is noted in the errata.
	 * This method will split the value by comma incase the vendor implemented
	 * it this way.
	 * </p>
	 * @param parameters the parameters
	 */
	private void handleQuotedMultivaluedTypeParams(VCardParameters parameters) {
		//account for multi-valued TYPE parameters being enclosed entirely in double quotes
		//e.g. ADR;TYPE="home,work"
		for (String typeParameter : parameters.getTypes()) {
			if (!typeParameter.contains(",")) {
				continue;
			}

			parameters.removeTypes();
			for (String splitValue : typeParameter.split(",")) {
				parameters.addType(splitValue);
			}
		}
	}

	/**
	 * Decodes the property value if it's encoded in quoted-printable encoding.
	 * Quoted-printable encoding is only supported in v2.1.
	 * @param name the property name
	 * @param parameters the parameters
	 * @param value the property value
	 * @return the decoded property value
	 */
	private String decodeQuotedPrintable(String name, VCardParameters parameters, String value) {
		if (parameters.getEncoding() != Encoding.QUOTED_PRINTABLE) {
			return value;
		}

		//remove encoding parameter
		parameters.setEncoding(null);

		//determine the character set
		Charset charset = null;
		String charsetStr = parameters.getCharset();
		if (charsetStr == null) {
			charset = defaultQuotedPrintableCharset;
		} else {
			try {
				charset = Charset.forName(charsetStr);
			} catch (Throwable t) {
				charset = defaultQuotedPrintableCharset;

				//the given charset was invalid, so add a warning
				addWarning(name, 23, charsetStr, charset.name());
			}
		}

		QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
		try {
			return codec.decode(value);
		} catch (DecoderException e) {
			//only thrown if the charset is invalid
			//we know this will never happen because we're using a Charset object
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}

	private void addWarning(String propertyName, int code, Object... args) {
		String message = Messages.INSTANCE.getParseMessage(code, args);
		addWarning(propertyName, message);
	}

	private void addWarning(String propertyName, String message) {
		int code = (propertyName == null) ? 37 : 36;
		int line = reader.getLineNum();

		String warning = Messages.INSTANCE.getParseMessage(code, line, propertyName, message);
		warnings.add(warning);
	}

	private class VCardDataStreamListenerImpl implements VCardRawReader.VCardDataStreamListener {
		private VCard root;
		private final List<Label> labels = new ArrayList<Label>();
		private final LinkedList<VCard> vcardStack = new LinkedList<VCard>();
		private EmbeddedVCardException embeddedVCardException;

		public void beginComponent(String name) {
			if (!"VCARD".equalsIgnoreCase(name)) {
				return;
			}

			VCard vcard = new VCard();

			//initialize version to 2.1, since the VERSION property can exist anywhere in a 2.1 vCard
			vcard.setVersion(VCardVersion.V2_1);

			vcardStack.add(vcard);

			if (root == null) {
				root = vcard;
			}

			if (embeddedVCardException != null) {
				embeddedVCardException.injectVCard(vcard);
				embeddedVCardException = null;
			}
		}

		public void readVersion(VCardVersion version) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			vcardStack.getLast().setVersion(version);
		}

		public void readProperty(String group, String name, VCardParameters parameters, String value) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			if (embeddedVCardException != null) {
				//the next property was supposed to be the start of a nested vCard, but it wasn't
				embeddedVCardException.injectVCard(null);
				embeddedVCardException = null;
			}

			VCard curVCard = vcardStack.getLast();
			VCardVersion version = curVCard.getVersion();

			//massage the parameters
			handleNamelessParameters(parameters);
			handleQuotedMultivaluedTypeParams(parameters);

			//decode property value from quoted-printable
			value = decodeQuotedPrintable(name, parameters, value);

			//get the scribe
			VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(name);
			if (scribe == null) {
				scribe = new RawPropertyScribe(name);
			}

			//get the data type
			VCardDataType dataType = parameters.getValue();
			if (dataType == null) {
				//use the default data type if there is no VALUE parameter
				dataType = scribe.defaultDataType(version);
			} else {
				//remove VALUE parameter if it is set
				parameters.setValue(null);
			}

			VCardProperty property;
			try {
				Result<? extends VCardProperty> result = scribe.parseText(value, dataType, version, parameters);

				for (String warning : result.getWarnings()) {
					addWarning(name, warning);
				}

				property = result.getProperty();
				property.setGroup(group);

				if (property instanceof Label) {
					//LABELs must be treated specially so they can be matched up with their ADRs
					labels.add((Label) property);
					return;
				}
			} catch (SkipMeException e) {
				addWarning(name, 22, e.getMessage());
				return;
			} catch (CannotParseException e) {
				addWarning(name, 25, value, e.getMessage());
				property = new RawProperty(name, value);
				property.setGroup(group);
			} catch (EmbeddedVCardException e) {
				//parse an embedded vCard (i.e. the AGENT type)
				property = e.getProperty();

				if (value.length() == 0 || version == VCardVersion.V2_1) {
					//a nested vCard is expected to be next (2.1 style)
					embeddedVCardException = e;
				} else {
					//the property value should be an embedded vCard (3.0 style)
					value = VCardPropertyScribe.unescape(value);

					VCardReader agentReader = new VCardReader(value);
					try {
						VCard nestedVCard = agentReader.readNext();
						if (nestedVCard != null) {
							e.injectVCard(nestedVCard);
						}
					} catch (IOException e2) {
						//shouldn't be thrown because we're reading from a string
					} finally {
						for (String w : agentReader.getWarnings()) {
							addWarning(name, 26, w);
						}
						IOUtils.closeQuietly(agentReader);
					}
				}
			}

			curVCard.addProperty(property);
		}

		public void endComponent(String name) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			if (!"VCARD".equalsIgnoreCase(name)) {
				//not a "VCARD" component
				return;
			}

			VCard curVCard = vcardStack.removeLast();

			//assign labels to their addresses
			for (Label label : labels) {
				boolean orphaned = true;
				for (Address adr : curVCard.getAddresses()) {
					if (adr.getLabel() == null && adr.getTypes().equals(label.getTypes())) {
						adr.setLabel(label.getValue());
						orphaned = false;
						break;
					}
				}
				if (orphaned) {
					curVCard.addOrphanedLabel(label);
				}
			}

			if (vcardStack.isEmpty()) {
				throw new VCardRawReader.StopReadingException();
			}
		}

		public void invalidLine(String line) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			addWarning(null, 27, line);
		}

		public void invalidVersion(String version) {
			if (vcardStack.isEmpty()) {
				//not in a "VCARD" component
				return;
			}

			addWarning("VERSION", 28, version);
		}
	}
}
