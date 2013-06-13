package ezvcard.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Pattern;

import ezvcard.util.VCardStringUtils;

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
 * Automatically unfolds lines of text as they are read.
 * @author Michael Angstadt
 */
public class FoldedLineReader extends BufferedReader {
	/**
	 * Regular expression used for the incorrectly folded lines that Outlook can
	 * generate.
	 */
	private static final Pattern outlookQuirk = Pattern.compile("[^:]*?QUOTED-PRINTABLE.*?:.*?=", Pattern.CASE_INSENSITIVE);

	private String lastLine;

	/**
	 * Creates a folded line reader.
	 * @param reader the reader object to wrap
	 */
	public FoldedLineReader(Reader reader) {
		super(reader);
	}

	/**
	 * Creates a folded line reader.
	 * @param text the text to read
	 */
	public FoldedLineReader(String text) {
		this(new StringReader(text));
	}

	/**
	 * Reads the next non-empty line. Empty lines must be ignored because some
	 * vCards (i.e. iPhone) contain empty lines. These empty lines appear in
	 * between folded lines, which, if not ignored, will cause the parser to
	 * incorrectly parse the vCard.
	 * @return the next non-empty line or null of EOF
	 * @throws IOException
	 */
	private String readNonEmptyLine() throws IOException {
		String line;
		do {
			line = super.readLine();
		} while (line != null && line.length() == 0);
		return line;
	}

	/**
	 * Reads the next line, unfolding it if necessary.
	 * @return the next line or null if EOF
	 * @throws IOException if there's a problem reading from the reader
	 */
	@Override
	public String readLine() throws IOException {
		String wholeLine = (lastLine == null) ? readNonEmptyLine() : lastLine;
		lastLine = null;
		if (wholeLine == null) {
			return null;
		}

		//@formatter:off
		/*
		 * Outlook incorrectly folds lines that are QUOTED-PRINTABLE. It puts a
		 * "=" at the end of a line to signal that the line's newline characters
		 * should be ignored and that the vCard parser should continue to read
		 * the next line as if it were part of the current line. It does not
		 * prepend each additional line with whitespace.
		 * 
		 * For example:
		 * 
		 * ------------
		 * BEGIN:VCARD
		 * NOTE;QUOTED-PRINTABLE: This is an=0D=0A=
		 * annoyingly formatted=0D=0A=
		 * note=
		 * 
		 * END:VCARD
		 * ------------
		 * 
		 * In the example above, note how there is an empty line directly above
		 * END. This is still part of the NOTE property value because the 3rd
		 * line of NOTE ends with a "=".
		 */
		//@formatter:on

		boolean foldedQuotedPrintableLine = false;
		if (outlookQuirk.matcher(wholeLine).matches()) {
			foldedQuotedPrintableLine = true;
			wholeLine = wholeLine.substring(0, wholeLine.length() - 1); //chop off the ending "="
		}

		//long lines are folded
		StringBuilder wholeLineSb = new StringBuilder(wholeLine);
		while (true) {
			String line = foldedQuotedPrintableLine ? super.readLine() : readNonEmptyLine();
			if (line == null) {
				break;
			} else if (foldedQuotedPrintableLine) {
				line = VCardStringUtils.ltrim(line);

				boolean endsInEquals = line.endsWith("=");
				if (endsInEquals) {
					line = line.substring(0, line.length() - 1);
				}

				wholeLineSb.append(line);

				if (!endsInEquals) {
					break;
				}
			} else if (line.length() > 0 && Character.isWhitespace(line.charAt(0))) {
				//the line was folded

				int lastWhitespace = 1;
				//Evolution will include real whitespace chars alongside the folding char
				while (lastWhitespace < line.length() && Character.isWhitespace(line.charAt(lastWhitespace))) {
					lastWhitespace++;
				}
				wholeLineSb.append(line.substring(lastWhitespace));
			} else {
				lastLine = line;
				break;
			}
		}
		return wholeLineSb.toString();
	}
}
