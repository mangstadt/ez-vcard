package ezvcard.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import ezvcard.Ezvcard;
import ezvcard.io.StreamReader;
import ezvcard.io.html.HCardParser;

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
 * Chainer class for parsing hCards (HTML-encoded vCards).
 * @see Ezvcard#parseHtml(InputStream)
 * @see Ezvcard#parseHtml(File)
 * @see Ezvcard#parseHtml(Reader)
 * @author Michael Angstadt
 */
public class ChainingHtmlParser<T extends ChainingHtmlParser<?>> extends ChainingParser<T> {
	private String pageUrl;
	private URL url;

	public ChainingHtmlParser(String string) {
		super(string);
	}

	public ChainingHtmlParser(InputStream in) {
		super(in);
	}

	public ChainingHtmlParser(Reader reader) {
		super(reader);
	}

	public ChainingHtmlParser(File file) {
		super(file);
	}

	public ChainingHtmlParser(URL url) {
		this.url = url;
	}

	/**
	 * Sets the original URL of the webpage. This is used to resolve relative
	 * links and to set the SOURCE property on the vCard. Setting this property
	 * has no effect if reading from a {@link URL}.
	 * @param pageUrl the webpage URL
	 * @return this
	 */
	public T pageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
		return this_;
	}

	@Override
	StreamReader constructReader() throws IOException {
		if (string != null) {
			return new HCardParser(string, pageUrl);
		}
		if (in != null) {
			return new HCardParser(in, pageUrl);
		}
		if (reader != null) {
			return new HCardParser(reader, pageUrl);
		}
		if (file != null) {
			return new HCardParser(file, pageUrl);
		}
		return new HCardParser(url);
	}
}
