package ezvcard.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import ezvcard.Ezvcard;
import ezvcard.io.StreamReader;
import ezvcard.io.text.VCardReader;

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
 * Chainer class for parsing traditional, plain-text vCards.
 * @see Ezvcard#parse(InputStream)
 * @see Ezvcard#parse(File)
 * @see Ezvcard#parse(Reader)
 * @author Michael Angstadt
 */
public class ChainingTextParser<T extends ChainingTextParser<?>> extends ChainingParser<T> {
	private boolean caretDecoding = true;

	public ChainingTextParser(String string) {
		super(string);
	}

	public ChainingTextParser(InputStream in) {
		super(in);
	}

	public ChainingTextParser(Reader reader) {
		super(reader);
	}

	public ChainingTextParser(File file) {
		super(file);
	}

	/**
	 * Sets whether the reader will decode characters in parameter values that
	 * use circumflex accent encoding (enabled by default).
	 * 
	 * @param enable true to use circumflex accent decoding, false not to
	 * @return this
	 * @see VCardReader#setCaretDecodingEnabled(boolean)
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public T caretDecoding(boolean enable) {
		caretDecoding = enable;
		return this_;
	}

	@Override
	StreamReader constructReader() throws IOException {
		VCardReader reader = newReader();
		reader.setCaretDecodingEnabled(caretDecoding);
		return reader;
	}

	private VCardReader newReader() throws IOException {
		if (string != null) {
			return new VCardReader(string);
		}
		if (in != null) {
			return new VCardReader(in);
		}
		if (reader != null) {
			return new VCardReader(reader);
		}
		return new VCardReader(file);
	}
}
