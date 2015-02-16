package ezvcard.io.text;

import static ezvcard.util.StringUtils.ltrim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

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
 * Automatically unfolds lines of text as they are read.
 * @author Michael Angstadt
 */
public class FoldedLineReader extends BufferedReader {
	/**
	 * Regular expression used to detect the first line of folded,
	 * "quoted-printable" property values.
	 */
	private static final Pattern foldedQuotedPrintableValueRegex = Pattern.compile("[^:]*?QUOTED-PRINTABLE.*?:.*?=", Pattern.CASE_INSENSITIVE);

	private String lastLine;
	private int lastLineNum = 0, lineCount = 0;
	private final Charset charset;

	/**
	 * Creates a folded line reader.
	 * @param reader the reader object to wrap
	 */
	public FoldedLineReader(Reader reader) {
		super(reader);

		if (reader instanceof InputStreamReader) {
			InputStreamReader isr = (InputStreamReader) reader;
			String charsetStr = isr.getEncoding();
			charset = (charsetStr == null) ? null : Charset.forName(charsetStr);
		} else {
			charset = null;
		}
	}

	/**
	 * Creates a folded line reader.
	 * @param text the text to read
	 */
	public FoldedLineReader(String text) {
		this(new StringReader(text));
	}

	/**
	 * Gets the starting line number of the last unfolded line that was read.
	 * @return the line number
	 */
	public int getLineNum() {
		return lastLineNum;
	}

	/**
	 * Gets the character encoding of the reader.
	 * @return the character encoding or null if none is defined
	 */
	public Charset getEncoding() {
		return charset;
	}

	/**
	 * Reads the next non-empty line. Empty lines must be ignored because some
	 * vCards (i.e. iPhone) contain empty lines. These empty lines appear in
	 * between folded lines, which, if not ignored, will cause the parser to
	 * incorrectly parse the vCard.
	 * @return the next non-empty line or null of EOF
	 * @throws IOException if there's a problem reading from the reader
	 */
	private String readNonEmptyLine() throws IOException {
		String line;
		do {
			line = super.readLine();
			if (line != null) {
				lineCount++;
			}
		} while (line != null && line.length() == 0);
		return line;
	}

	/**
	 * Reads the next unfolded line.
	 * @return the next unfolded line or null if the end of the stream has been
	 * reached
	 * @throws IOException if there's a problem reading from the reader
	 */
	@Override
	public String readLine() throws IOException {
		String wholeLine = (lastLine == null) ? readNonEmptyLine() : lastLine;
		lastLine = null;
		if (wholeLine == null) {
			//end of stream
			return null;
		}

		//@formatter:off
		/*
		 * Lines that are QUOTED-PRINTABLE are folded in a strange way. A "=" is
		 * appended to the end of a line to signal that the next line is folded.
		 * Also, each folded line is *not* prepend with whitespace (which it should
		 * be, according to the 2.1 specs).
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
		 * 
		 * This behavior has only been observed in Outlook vCards.
		 */
		//@formatter:on

		boolean foldedQuotedPrintableLine = foldedQuotedPrintableValueRegex.matcher(wholeLine).matches();
		if (foldedQuotedPrintableLine) {
			//chop off the trailing "="
			wholeLine = chop(wholeLine);
		}

		lastLineNum = lineCount;
		StringBuilder unfoldedLine = new StringBuilder(wholeLine);
		while (true) {
			String line = foldedQuotedPrintableLine ? super.readLine() : readNonEmptyLine();
			if (line == null) {
				//end of stream
				break;
			}

			if (foldedQuotedPrintableLine) {
				//remove any folding whitespace
				line = ltrim(line);

				boolean endsInEquals = line.endsWith("=");
				if (endsInEquals) {
					//chop off the trailing "="
					line = chop(line);
				}

				unfoldedLine.append(line);

				if (endsInEquals) {
					//there are more folded lines
					continue;
				}

				//end of the folded line
				break;
			}

			if (line.length() > 0 && Character.isWhitespace(line.charAt(0))) {
				//the line is folded
				line = ltrim(line);
				unfoldedLine.append(line);
				continue;
			}

			lastLine = line;
			break;
		}

		return unfoldedLine.toString();
	}

	/**
	 * Removes the last character from a string.
	 * @param string the string
	 * @return the modified string
	 */
	private static String chop(String string) {
		return string.substring(0, string.length() - 1);
	}
}
