package ezvcard.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.json.JCardWriter;
import ezvcard.io.scribe.VCardPropertyScribe;
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
 * Chainer class for writing jCards (JSON-encoded vCards).
 * @see Ezvcard#writeJson(Collection)
 * @see Ezvcard#writeJson(VCard...)
 * @author Michael Angstadt
 */
public class ChainingJsonWriter extends ChainingWriter<ChainingJsonWriter> {
	private boolean prettyPrint = false;

	/**
	 * @param vcards the vCards to write
	 */
	public ChainingJsonWriter(Collection<VCard> vcards) {
		super(vcards);
	}

	/**
	 * Sets whether or not to pretty-print the JSON.
	 * @param prettyPrint true to pretty-print it, false not to (defaults to
	 * false)
	 * @return this
	 */
	public ChainingJsonWriter prettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
		return this;
	}

	@Override
	public ChainingJsonWriter prodId(boolean include) {
		return super.prodId(include);
	}

	@Override
	public ChainingJsonWriter versionStrict(boolean versionStrict) {
		return super.versionStrict(versionStrict);
	}

	@Override
	public ChainingJsonWriter register(VCardPropertyScribe<? extends VCardProperty> scribe) {
		return super.register(scribe);
	}

	/**
	 * Writes the jCards to a string.
	 * @return the JSON string
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
	 * Writes the jCards to an output stream.
	 * @param out the output stream to write to
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void go(OutputStream out) throws IOException {
		go(new JCardWriter(out, wrapInArray()));
	}

	/**
	 * Writes the jCards to a file.
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 */
	public void go(File file) throws IOException {
		JCardWriter writer = new JCardWriter(file, wrapInArray());
		try {
			go(writer);
		} finally {
			writer.close();
		}
	}

	/**
	 * Writes the jCards to a writer.
	 * @param writer the writer to write to
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void go(Writer writer) throws IOException {
		go(new JCardWriter(writer, wrapInArray()));
	}

	private void go(JCardWriter writer) throws IOException {
		writer.setAddProdId(prodId);
		writer.setPrettyPrint(prettyPrint);
		writer.setVersionStrict(versionStrict);
		if (index != null) {
			writer.setScribeIndex(index);
		}
		try {
			for (VCard vcard : vcards) {
				writer.write(vcard);
				writer.flush();
			}
		} finally {
			writer.closeJsonStream();
		}
	}

	private boolean wrapInArray() {
		return vcards.size() > 1;
	}
}
