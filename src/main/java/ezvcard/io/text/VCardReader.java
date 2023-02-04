package ezvcard.io.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.mangstadt.vinnie.VObjectProperty;
import com.github.mangstadt.vinnie.io.Context;
import com.github.mangstadt.vinnie.io.SyntaxRules;
import com.github.mangstadt.vinnie.io.VObjectDataListener;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues;
import com.github.mangstadt.vinnie.io.VObjectReader;
import com.github.mangstadt.vinnie.io.Warning;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.EmbeddedVCardException;
import ezvcard.io.ParseWarning;
import ezvcard.io.SkipMeException;
import ezvcard.io.StreamReader;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Label;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;
import ezvcard.util.StringUtils;

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
 * Parses {@link VCard} objects from a plain-text vCard data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * Path file = Paths.get("vcards.vcf");
 * try (VCardReader reader = new VCardReader(file)) {
 *   VCard vcard;
 *   while ((vcard = reader.readNext()) != null) {
 *     //...
 *   }
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
 */
public class VCardReader extends StreamReader {
	private final VObjectReader reader;
	private final VCardVersion defaultVersion;

	/**
	 * Creates a new vCard reader.
	 * @param str the string to read from
	 */
	public VCardReader(String str) {
		this(str, VCardVersion.V2_1);
	}

	/**
	 * Creates a new vCard reader.
	 * @param str the string to read from
	 * @param defaultVersion the version to assume the vCard is in until a
	 * VERSION property is encountered (defaults to 2.1)
	 */
	public VCardReader(String str, VCardVersion defaultVersion) {
		this(new StringReader(str), defaultVersion);
	}

	/**
	 * Creates a new vCard reader.
	 * @param in the input stream to read from
	 */
	public VCardReader(InputStream in) {
		this(in, VCardVersion.V2_1);
	}

	/**
	 * Creates a new vCard reader.
	 * @param in the input stream to read from
	 * @param defaultVersion the version to assume the vCard is in until a
	 * VERSION property is encountered (defaults to 2.1)
	 */
	public VCardReader(InputStream in, VCardVersion defaultVersion) {
		this(new InputStreamReader(in), defaultVersion);
	}

	/**
	 * Creates a new vCard reader.
	 * @param file the file to read from
	 * @throws IOException if there is a problem opening the file
	 */
	public VCardReader(Path file) throws IOException {
		this(file, VCardVersion.V2_1);
	}

	/**
	 * Creates a new vCard reader.
	 * @param file the file to read from
	 * @param defaultVersion the version to assume the vCard is in until a
	 * VERSION property is encountered (defaults to 2.1)
	 * @throws IOException if there is a problem opening the file
	 */
	public VCardReader(Path file, VCardVersion defaultVersion) throws IOException {
		this(Files.newBufferedReader(file), defaultVersion);
	}

	/**
	 * Creates a new vCard reader.
	 * @param reader the reader to read from
	 */
	public VCardReader(Reader reader) {
		this(reader, VCardVersion.V2_1);
	}

	/**
	 * Creates a new vCard reader.
	 * @param reader the reader to read from
	 * @param defaultVersion the version to assume the vCard is in until a
	 * VERSION property is encountered (defaults to 2.1)
	 */
	public VCardReader(Reader reader, VCardVersion defaultVersion) {
		SyntaxRules rules = SyntaxRules.vcard();
		rules.setDefaultSyntaxStyle(defaultVersion.getSyntaxStyle());
		this.reader = new VObjectReader(reader, rules);
		this.defaultVersion = defaultVersion;
	}

	/**
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @see VObjectReader#isCaretDecodingEnabled()
	 */
	public boolean isCaretDecodingEnabled() {
		return reader.isCaretDecodingEnabled();
	}

	/**
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see VObjectReader#setCaretDecodingEnabled(boolean)
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		reader.setCaretDecodingEnabled(enable);
	}

	/**
	 * Gets the character set to use when the parser cannot determine what
	 * character set to use to decode a quoted-printable property value.
	 * @return the character set
	 * @see VObjectReader#getDefaultQuotedPrintableCharset()
	 */
	public Charset getDefaultQuotedPrintableCharset() {
		return reader.getDefaultQuotedPrintableCharset();
	}

	/**
	 * Sets the character set to use when the parser cannot determine what
	 * character set to use to decode a quoted-printable property value.
	 * @param charset the character set (cannot be null)
	 * @see VObjectReader#setDefaultQuotedPrintableCharset
	 */
	public void setDefaultQuotedPrintableCharset(Charset charset) {
		reader.setDefaultQuotedPrintableCharset(charset);
	}

	@Override
	protected VCard _readNext() throws IOException {
		VObjectDataListenerImpl listener = new VObjectDataListenerImpl();
		reader.parse(listener);
		return listener.root;
	}

	private class VObjectDataListenerImpl implements VObjectDataListener {
		private VCard root;
		private final VCardStack stack = new VCardStack();
		private EmbeddedVCardException embeddedVCardException;

		public void onComponentBegin(String name, Context context) {
			if (!isVCardComponent(name)) {
				//ignore non-VCARD components
				return;
			}

			VCard vcard = new VCard(defaultVersion);
			if (stack.isEmpty()) {
				root = vcard;
			}
			stack.push(vcard);

			if (embeddedVCardException != null) {
				embeddedVCardException.injectVCard(vcard);
				embeddedVCardException = null;
			}
		}

		public void onComponentEnd(String name, Context context) {
			if (!isVCardComponent(name)) {
				//ignore non-VCARD components
				return;
			}

			VCardStack.Item item = stack.pop();
			assignLabels(item.vcard, item.labels);

			if (stack.isEmpty()) {
				context.stop();
			}
		}

		public void onProperty(VObjectProperty vobjectProperty, Context vobjectContext) {
			if (!inVCardComponent(vobjectContext.getParentComponents())) {
				//ignore properties that are not directly inside a VCARD component
				return;
			}

			if (embeddedVCardException != null) {
				//the next property was supposed to be the start of a nested vCard, but it wasn't
				embeddedVCardException.injectVCard(null);
				embeddedVCardException = null;
			}

			VCard curVCard = stack.peek().vcard;
			VCardVersion version = curVCard.getVersion();

			VCardProperty property = parseProperty(vobjectProperty, version, vobjectContext.getLineNumber());
			if (property != null) {
				curVCard.addProperty(property);
			}
		}

		private VCardProperty parseProperty(VObjectProperty vobjectProperty, VCardVersion version, int lineNumber) {
			String group = vobjectProperty.getGroup();
			String name = vobjectProperty.getName();
			VCardParameters parameters = new VCardParameters(vobjectProperty.getParameters().getMap());
			String value = vobjectProperty.getValue();

			context.getWarnings().clear();
			context.setVersion(version);
			context.setLineNumber(lineNumber);
			context.setPropertyName(name);

			//sanitize the parameters
			processNamelessParameters(parameters);
			processQuotedMultivaluedTypeParams(parameters, version);

			//get the scribe
			VCardPropertyScribe<? extends VCardProperty> scribe = index.getPropertyScribe(name);
			if (scribe == null) {
				scribe = new RawPropertyScribe(name);
			}

			//get the data type (VALUE parameter)
			VCardDataType dataType = parameters.getValue();
			parameters.setValue(null);
			if (dataType == null) {
				//use the default data type if there is no VALUE parameter
				dataType = scribe.defaultDataType(version);
			}

			VCardProperty property;
			try {
				property = scribe.parseText(value, dataType, parameters, context);
				warnings.addAll(context.getWarnings());
			} catch (SkipMeException e) {
				handleSkippedProperty(name, lineNumber, e);
				return null;
			} catch (CannotParseException e) {
				property = handleUnparseableProperty(name, parameters, value, dataType, lineNumber, version, e);
			} catch (EmbeddedVCardException e) {
				handledEmbeddedVCard(name, value, lineNumber, e);
				property = e.getProperty();
			}

			property.setGroup(group);

			/*
			 * LABEL properties must be treated specially so they can be matched
			 * up with the ADR properties that they belong to. LABELs are not
			 * added to the vCard as properties, they are added to the ADR
			 * properties they belong to (unless they cannot be matched up with
			 * an ADR).
			 */
			if (property instanceof Label) {
				Label label = (Label) property;
				stack.peek().labels.add(label);
				return null;
			}

			handleLabelParameter(property);

			return property;
		}

		private void handleSkippedProperty(String propertyName, int lineNumber, SkipMeException e) {
			//@formatter:off
			warnings.add(new ParseWarning.Builder(context)
				.message(22, e.getMessage())
				.build()
			);
			//@formatter:on
		}

		private VCardProperty handleUnparseableProperty(String name, VCardParameters parameters, String value, VCardDataType dataType, int lineNumber, VCardVersion version, CannotParseException e) {
			//@formatter:off
			warnings.add(new ParseWarning.Builder(context)
				.message(e)
				.build()
			);
			//@formatter:on

			RawPropertyScribe scribe = new RawPropertyScribe(name);
			return scribe.parseText(value, dataType, parameters, null);
		}

		private void handledEmbeddedVCard(String name, String value, int lineNumber, EmbeddedVCardException exception) {
			/*
			 * If the property does not have a value, a nested vCard is expected
			 * to be next (2.1 style).
			 */
			if (value.trim().isEmpty()) {
				embeddedVCardException = exception;
				return;
			}

			/*
			 * If the property does have a value, the property value should be
			 * an embedded vCard (3.0 style).
			 */
			value = VObjectPropertyValues.unescape(value);

			VCardReader agentReader = new VCardReader(value);
			agentReader.setCaretDecodingEnabled(isCaretDecodingEnabled());
			agentReader.setDefaultQuotedPrintableCharset(getDefaultQuotedPrintableCharset());
			agentReader.setScribeIndex(index);

			try {
				VCard nestedVCard = agentReader.readNext();
				if (nestedVCard != null) {
					exception.injectVCard(nestedVCard);
				}
			} catch (IOException ignore) {
				//shouldn't be thrown because we're reading from a string
			} finally {
				warnings.addAll(agentReader.getWarnings());
				IOUtils.closeQuietly(agentReader);
			}
		}

		/**
		 * <p>
		 * Unescapes newline sequences in the LABEL parameter of {@link Address}
		 * properties. Newlines cannot normally be escaped in parameter values.
		 * </p>
		 * <p>
		 * Only version 4.0 allows this (and only version 4.0 defines a LABEL
		 * parameter), but do this for all versions for compatibility.
		 * </p>
		 * @param property the property
		 */
		private void handleLabelParameter(VCardProperty property) {
			if (!(property instanceof Address)) {
				return;
			}

			Address adr = (Address) property;
			String label = adr.getLabel();
			if (label == null) {
				return;
			}

			label = label.replace("\\n", StringUtils.NEWLINE);
			adr.setLabel(label);
		}

		public void onVersion(String value, Context vobjectContext) {
			VCardVersion version = VCardVersion.valueOfByStr(value);
			context.setVersion(version);
			stack.peek().vcard.setVersion(version);
		}

		public void onWarning(Warning warning, VObjectProperty property, Exception thrown, Context vobjectContext) {
			if (!inVCardComponent(vobjectContext.getParentComponents())) {
				//ignore warnings that are not directly inside a VCARD component
				return;
			}

			//@formatter:off
			warnings.add(new ParseWarning.Builder(context)
				.lineNumber(vobjectContext.getLineNumber())
				.propertyName((property == null) ? null : property.getName())
				.message(27, warning.getMessage(), vobjectContext.getUnfoldedLine())
				.build()
			);
			//@formatter:on
		}

		private boolean inVCardComponent(List<String> parentComponents) {
			if (parentComponents.isEmpty()) {
				return false;
			}
			String last = parentComponents.get(parentComponents.size() - 1);
			return isVCardComponent(last);
		}

		private boolean isVCardComponent(String componentName) {
			return "VCARD".equals(componentName);
		}

		/**
		 * Assigns names to all nameless parameters. v3.0 and v4.0 require all
		 * parameters to have names, but v2.1 does not.
		 * @param parameters the parameters
		 */
		private void processNamelessParameters(VCardParameters parameters) {
			List<String> namelessValues = parameters.removeAll(null);
			for (String value : namelessValues) {
				String name = guessParameterName(value);
				parameters.put(name, value);
			}
		}

		/**
		 * Makes a guess as to what a parameter value's name should be.
		 * @param value the parameter value (e.g. "HOME")
		 * @return the guessed name (e.g. "TYPE")
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
		 * Many examples throughout the 4.0 specs show TYPE parameters being
		 * encoded in this way. This conflicts with the ABNF and is noted in the
		 * errata. This method will parse these incorrectly-formatted TYPE
		 * parameters as if they were multi-valued, even though, technically,
		 * they are not.
		 * </p>
		 * @param parameters the parameters
		 * @param version the version
		 */
		private void processQuotedMultivaluedTypeParams(VCardParameters parameters, VCardVersion version) {
			if (version == VCardVersion.V2_1) {
				return;
			}

			List<String> types = parameters.getTypes();
			if (types.isEmpty()) {
				return;
			}

			String valueWithComma = null;
			for (String value : types) {
				if (value.indexOf(',') >= 0) {
					valueWithComma = value;
					break;
				}
			}
			if (valueWithComma == null) {
				return;
			}

			types.clear();
			int prev = -1, cur;
			while ((cur = valueWithComma.indexOf(',', prev + 1)) >= 0) {
				types.add(valueWithComma.substring(prev + 1, cur));
				prev = cur;
			}
			types.add(valueWithComma.substring(prev + 1));
		}
	}

	/**
	 * Keeps track of the hierarchy of nested vCards.
	 */
	private static class VCardStack {
		private final List<Item> stack = new ArrayList<>();

		/**
		 * Adds a vCard to the stack.
		 * @param vcard the vcard to add
		 */
		public void push(VCard vcard) {
			stack.add(new Item(vcard, new ArrayList<>()));
		}

		/**
		 * Removes the top item from the stack and returns it.
		 * @return the last item or null if the stack is empty
		 */
		public Item pop() {
			return isEmpty() ? null : stack.remove(stack.size() - 1);
		}

		/**
		 * Gets the top item of the stack.
		 * @return the top item
		 */
		public Item peek() {
			return isEmpty() ? null : stack.get(stack.size() - 1);
		}

		/**
		 * Determines if the stack is empty.
		 * @return true if it's empty, false if not
		 */
		public boolean isEmpty() {
			return stack.isEmpty();
		}

		private static class Item {
			public final VCard vcard;
			public final List<Label> labels;

			public Item(VCard vcard, List<Label> labels) {
				this.vcard = vcard;
				this.labels = labels;
			}
		}
	}

	/**
	 * Closes the input stream.
	 * @throws IOException if there's a problem closing the input stream
	 */
	public void close() throws IOException {
		reader.close();
	}
}
