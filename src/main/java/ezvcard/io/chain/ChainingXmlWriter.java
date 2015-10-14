package ezvcard.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.xml.XCardDocument;
import ezvcard.io.xml.XCardDocument.XCardDocumentStreamWriter;
import ezvcard.property.VCardProperty;

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
 */

/**
 * Chainer class for writing xCards (XML-encoded vCards).
 * @see Ezvcard#writeXml(Collection)
 * @see Ezvcard#writeXml(VCard...)
 * @author Michael Angstadt
 */
public class ChainingXmlWriter extends ChainingWriter<ChainingXmlWriter> {
	private int indent = -1;
	private String xmlVersion;

	/**
	 * @param vcards the vCards to write
	 */
	public ChainingXmlWriter(Collection<VCard> vcards) {
		super(vcards);
	}

	/**
	 * Sets the number of indent spaces to use for pretty-printing. If not set,
	 * then the XML will not be pretty-printed.
	 * @param indent the number of spaces in the indent string (pretty-printing
	 * disabled by default)
	 * @return this
	 */
	public ChainingXmlWriter indent(int indent) {
		this.indent = indent;
		return this;
	}

	/**
	 * Sets the XML version to use. Note that many JDKs only support 1.0
	 * natively. For XML 1.1 support, add a JAXP library like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project.
	 * @param xmlVersion the XML version (defaults to "1.0")
	 * @return this
	 */
	public ChainingXmlWriter xmlVersion(String xmlVersion) {
		this.xmlVersion = xmlVersion;
		return this;
	}

	@Override
	public ChainingXmlWriter prodId(boolean include) {
		return super.prodId(include);
	}

	@Override
	public ChainingXmlWriter versionStrict(boolean versionStrict) {
		return super.versionStrict(versionStrict);
	}

	@Override
	public ChainingXmlWriter register(VCardPropertyScribe<? extends VCardProperty> scribe) {
		return super.register(scribe);
	}

	/**
	 * Writes the xCards to a string.
	 * @return the XML document
	 */
	public String go() {
		return createXCardDocument().write(indent, xmlVersion);
	}

	/**
	 * Writes the xCards to an output stream.
	 * @param out the output stream to write to
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void go(OutputStream out) throws TransformerException {
		createXCardDocument().write(out, indent, xmlVersion);
	}

	/**
	 * Writes the xCards to a file.
	 * @param file the file to write to
	 * @throws IOException if the file can't be opened
	 * @throws TransformerException if there's a problem writing to the file
	 */
	public void go(File file) throws IOException, TransformerException {
		createXCardDocument().write(file, indent, xmlVersion);
	}

	/**
	 * Writes the xCards to a writer.
	 * @param writer the writer to write to
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void go(Writer writer) throws TransformerException {
		createXCardDocument().write(writer, indent, xmlVersion);
	}

	/**
	 * Generates an XML document object model (DOM) containing the xCards.
	 * @return the DOM
	 */
	public Document dom() {
		return createXCardDocument().getDocument();
	}

	private XCardDocument createXCardDocument() {
		XCardDocument document = new XCardDocument();

		XCardDocumentStreamWriter writer = document.writer();
		writer.setAddProdId(prodId);
		writer.setVersionStrict(versionStrict);
		if (index != null) {
			writer.setScribeIndex(index);
		}

		for (VCard vcard : vcards) {
			writer.write(vcard);
		}

		return document;
	}
}
