package ezvcard.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.html.HCardPage;

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
 * Chainer class for writing hCards (HTML-encoded vCards).
 * @see Ezvcard#writeHtml(Collection)
 * @see Ezvcard#writeHtml(VCard...)
 * @author Michael Angstadt
 */
public class ChainingHtmlWriter extends ChainingWriter<ChainingHtmlWriter> {
	/**
	 * @param vcards the vCards to write
	 */
	public ChainingHtmlWriter(Collection<VCard> vcards) {
		super(vcards);
	}

	/**
	 * Writes the hCards to a string.
	 * @return the HTML page
	 */
	public String go() {
		return buildPage().write();
	}

	/**
	 * Writes the hCards to an output stream.
	 * @param out the output stream to write to
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void go(OutputStream out) throws IOException {
		buildPage().write(out);
	}

	/**
	 * Writes the hCards to a file.
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 */
	public void go(File file) throws IOException {
		buildPage().write(file);
	}

	/**
	 * Writes the hCards to a writer.
	 * @param writer the writer to write to
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void go(Writer writer) throws IOException {
		buildPage().write(writer);
	}

	private HCardPage buildPage() {
		HCardPage page = new HCardPage();
		for (VCard vcard : vcards) {
			page.add(vcard);
		}
		return page;
	}
}
