package ezvcard.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.TargetApplication;
import ezvcard.io.text.VCardWriter;
import ezvcard.property.Address;
import ezvcard.property.StructuredName;
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
 */

/**
 * Chainer class for writing traditional, plain-text vCards.
 * @see Ezvcard#write(Collection)
 * @see Ezvcard#write(VCard...)
 * @author Michael Angstadt
 */
public class ChainingTextWriter extends ChainingWriter<ChainingTextWriter> {
	private VCardVersion version;
	private boolean caretEncoding = false;
	private Boolean includeTrailingSemicolons;
	private TargetApplication targetApplication;

	/**
	 * @param vcards the vCards to write
	 */
	public ChainingTextWriter(Collection<VCard> vcards) {
		super(vcards);
	}

	/**
	 * <p>
	 * Sets the version that all the vCards will be marshalled to. The version
	 * that is attached to each individual {@link VCard} object will be ignored.
	 * </p>
	 * <p>
	 * If no version is passed into this method, the writer will look at the
	 * version attached to each individual {@link VCard} object and marshal it
	 * to that version. And if a {@link VCard} object has no version attached to
	 * it, then it will be marshalled to version 3.0.
	 * </p>
	 * @param version the version to marshal the vCards to
	 * @return this
	 */
	public ChainingTextWriter version(VCardVersion version) {
		this.version = version;
		return this;
	}

	/**
	 * Sets whether the writer will use circumflex accent encoding for vCard 3.0
	 * and 4.0 parameter values (disabled by default).
	 * @param enable true to use circumflex accent encoding, false not to
	 * @return this
	 * @see VCardWriter#setCaretEncodingEnabled(boolean)
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public ChainingTextWriter caretEncoding(boolean enable) {
		this.caretEncoding = enable;
		return this;
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
	public ChainingTextWriter includeTrailingSemicolons(Boolean include) {
		this.includeTrailingSemicolons = include;
		return this;
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
	 * @see VCardWriter#setTargetApplication(TargetApplication)
	 */
	public ChainingTextWriter targetApplication(TargetApplication targetApplication) {
		this.targetApplication = targetApplication;
		return this;
	}

	@Override
	public ChainingTextWriter prodId(boolean include) {
		return super.prodId(include);
	}

	@Override
	public ChainingTextWriter versionStrict(boolean versionStrict) {
		return super.versionStrict(versionStrict);
	}

	@Override
	public ChainingTextWriter register(VCardPropertyScribe<? extends VCardProperty> scribe) {
		return super.register(scribe);
	}

	/**
	 * Writes the vCards to a string.
	 * @return the vCard string
	 */
	public String go() {
		StringWriter sw = new StringWriter();
		try {
			go(sw);
		} catch (IOException e) {
			//should never be thrown because we're writing to a string
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

	/**
	 * Writes the vCards to an output stream.
	 * @param out the output stream to write to
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void go(OutputStream out) throws IOException {
		go(new VCardWriter(out, version));
	}

	/**
	 * Writes the vCards to a file. If the file exists, it will be overwritten.
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 */
	public void go(File file) throws IOException {
		go(file, false);
	}

	/**
	 * Writes the vCards to a file.
	 * @param file the file to write to
	 * @param append true to append onto the end of the file, false to overwrite
	 * it
	 * @throws IOException if there's a problem writing to the file
	 */
	public void go(File file, boolean append) throws IOException {
		VCardWriter writer = new VCardWriter(file, append, version);
		try {
			go(writer);
		} finally {
			writer.close();
		}
	}

	/**
	 * Writes the vCards to a writer.
	 * @param writer the writer to write to
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void go(Writer writer) throws IOException {
		go(new VCardWriter(writer, version));
	}

	private void go(VCardWriter writer) throws IOException {
		writer.setAddProdId(prodId);
		writer.setCaretEncodingEnabled(caretEncoding);
		writer.setVersionStrict(versionStrict);
		writer.setIncludeTrailingSemicolons(includeTrailingSemicolons);
		writer.setTargetApplication(targetApplication);
		if (index != null) {
			writer.setScribeIndex(index);
		}

		for (VCard vcard : vcards) {
			if (version == null) {
				//use the version that's assigned to each individual vCard
				VCardVersion vcardVersion = vcard.getVersion();
				if (vcardVersion == null) {
					vcardVersion = VCardVersion.V3_0;
				}
				writer.setTargetVersion(vcardVersion);
			}
			writer.write(vcard);
			writer.flush();
		}
	}
}
