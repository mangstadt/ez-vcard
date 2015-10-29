package ezvcard.io.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamReader;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe.Result;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Label;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;
import ezvcard.util.org.apache.commons.codec.DecoderException;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
 * Parses {@link VCard} objects from a plain-text vCard data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * File file = new File("vcards.vcf");
 * VCardReader reader = null;
 * try {
 *   reader = new VCardReader(file);
 *   VCard vcard;
 *   while ((vcard = reader.readNext()) != null){
 *     ...
 *   }
 * } finally {
 *   if (reader != null) reader.close();
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
 */
public class VCardReader extends StreamReader {
	private final VCardRawReader reader;
	private Charset defaultQuotedPrintableCharset;

	/**
	 * @param str the string to read from
	 */
	public VCardReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * @param in the input stream to read from
	 */
	public VCardReader(InputStream in) {
		this(new InputStreamReader(in));
	}

	/**
	 * @param file the file to read from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public VCardReader(File file) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(file)));
	}

	/**
	 * @param reader the reader to read from
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

	@Override
	protected VCard _readNext() throws IOException {
		VCard root = null;
		LinkedList<VCard> vcardStack = new LinkedList<VCard>();
		LinkedList<List<Label>> labelStack = new LinkedList<List<Label>>();
		EmbeddedVCardException embeddedVCardException = null;
		while (true) {
			//read next line
			VCardRawLine line;
			try {
				line = reader.readLine();
			} catch (VCardParseException e) {
				if (!vcardStack.isEmpty()) {
					warnings.add(e.getLineNumber(), null, 27, e.getLine());
				}
				continue;
			}

			//EOF
			if (line == null) {
				break;
			}

			//handle BEGIN:VCARD
			if ("BEGIN".equalsIgnoreCase(line.getName()) && "VCARD".equalsIgnoreCase(line.getValue())) {
				VCard vcard = new VCard();
				vcard.setVersion(reader.getVersion());
				vcardStack.add(vcard);
				labelStack.add(new ArrayList<Label>());

				if (root == null) {
					root = vcard;
				}

				if (embeddedVCardException != null) {
					embeddedVCardException.injectVCard(vcard);
					embeddedVCardException = null;
				}

				continue;
			}

			if (vcardStack.isEmpty()) {
				//BEGIN component hasn't been encountered yet, so skip this line
				continue;
			}

			//handle VERSION property
			if ("VERSION".equalsIgnoreCase(line.getName())) {
				vcardStack.getLast().setVersion(reader.getVersion());
				continue;
			}

			//handle END:VCARD
			if ("END".equalsIgnoreCase(line.getName()) && "VCARD".equalsIgnoreCase(line.getValue())) {
				VCard curVCard = vcardStack.removeLast();
				List<Label> labels = labelStack.removeLast();
				assignLabels(curVCard, labels);

				if (vcardStack.isEmpty()) {
					//done reading the vCard
					break;
				}
				continue;
			}

			//handle property
			{
				String group = line.getGroup();
				VCardParameters parameters = line.getParameters();
				String name = line.getName();
				String value = line.getValue();

				if (embeddedVCardException != null) {
					//the next property was supposed to be the start of a nested vCard, but it wasn't
					embeddedVCardException.injectVCard(null);
					embeddedVCardException = null;
				}

				VCard curVCard = vcardStack.getLast();
				VCardVersion version = curVCard.getVersion();

				//sanitize the parameters
				processNamelessParameters(parameters);
				processQuotedMultivaluedTypeParams(parameters);

				//decode property value from quoted-printable
				try {
					value = decodeQuotedPrintableValue(name, parameters, value);
				} catch (DecoderException e) {
					warnings.add(reader.getLineNumber(), name, 38, e.getMessage());
				}

				//get the scribe
				VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(name);
				if (scribe == null) {
					scribe = new RawPropertyScribe(name);
				}

				//get the data type (VALUE parameter)
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
						warnings.add(reader.getLineNumber(), name, warning);
					}

					property = result.getProperty();
					property.setGroup(group);

					if (property instanceof Label) {
						/*
						 * LABEL properties must be treated specially so they
						 * can be matched up with the ADR properties that they
						 * belong to.
						 */
						Label label = (Label) property;
						labelStack.getLast().add(label);
					} else {
						curVCard.addProperty(property);
					}
				} catch (SkipMeException e) {
					warnings.add(reader.getLineNumber(), name, 22, e.getMessage());
				} catch (CannotParseException e) {
					warnings.add(reader.getLineNumber(), name, 25, value, e.getMessage());
					property = new RawProperty(name, value);
					property.setGroup(group);
					curVCard.addProperty(property);
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
						agentReader.setScribeIndex(index);
						try {
							VCard nestedVCard = agentReader.readNext();
							if (nestedVCard != null) {
								e.injectVCard(nestedVCard);
							}
						} catch (IOException e2) {
							//shouldn't be thrown because we're reading from a string
						} finally {
							for (String w : agentReader.getWarnings()) {
								warnings.add(reader.getLineNumber(), name, 26, w);
							}
							IOUtils.closeQuietly(agentReader);
						}
					}

					curVCard.addProperty(property);
				}
			}
		}

		return root;
	}

	/**
	 * Assigns names to all nameless parameters. v3.0 and v4.0 requires all
	 * parameters to have names, but v2.1 does not.
	 * @param parameters the parameters
	 */
	private void processNamelessParameters(VCardParameters parameters) {
		List<String> namelessParamValues = parameters.removeAll(null);
		for (String paramValue : namelessParamValues) {
			String paramName = guessParameterName(paramValue);
			parameters.put(paramName, paramValue);
		}
	}

	/**
	 * Makes a guess as to what a parameter value's name should be.
	 * @param value the parameter value
	 * @return the guessed name
	 */
	private String guessParameterName(String value) {
		if (VCardDataType.find(value) != null) {
			return VCardParameters.VALUE;
		}

		if (Encoding.find(value) != null) {
			return VCardParameters.ENCODING;
		}

		//otherwise, assume it's a TYPE
		return VCardParameters.TYPE;
	}

	/**
	 * <p>
	 * Accounts for multi-valued TYPE parameters being enclosed entirely in
	 * double quotes (for example: ADR;TYPE="home,work").
	 * </p>
	 * <p>
	 * Many examples throughout the 4.0 specs show TYPE parameters being encoded
	 * in this way. This conflicts with the ABNF and is noted in the errata.
	 * This method will parse these incorrectly-formatted TYPE parameters as if
	 * they were multi-valued, even though, technically, they are not.
	 * </p>
	 * @param parameters the parameters
	 */
	private void processQuotedMultivaluedTypeParams(VCardParameters parameters) {
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
	 * Checks to see if a property's value is encoded in quoted-printable
	 * encoding and decodes it if it is.
	 * @param name the property name
	 * @param parameters the property parameters
	 * @param value the property value (may or may not be encoded in
	 * quoted-printable
	 * @return the decoded property value or the untouched property value if it
	 * is not encoded in quoted-printable encoding
	 * @throws DecoderException if the value couldn't be decoded
	 */
	private String decodeQuotedPrintableValue(String name, VCardParameters parameters, String value) throws DecoderException {
		if (parameters.getEncoding() != Encoding.QUOTED_PRINTABLE) {
			//the property value is not encoded in quoted-printable encoding
			return value;
		}

		//remove the encoding parameter
		parameters.setEncoding(null);

		//determine the character set
		Charset charset = null;
		String charsetStr = parameters.getCharset();
		if (charsetStr == null) {
			charset = defaultQuotedPrintableCharset;
		} else {
			try {
				charset = Charset.forName(charsetStr);
			} catch (IllegalCharsetNameException e) {
				//bad charset name
			} catch (UnsupportedCharsetException e) {
				//bad charset name
			}

			if (charset == null) {
				charset = defaultQuotedPrintableCharset;

				//the given charset was invalid, so add a warning
				warnings.add(reader.getLineNumber(), name, 23, charsetStr, charset.name());
			}
		}

		QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
		return codec.decode(value);
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
