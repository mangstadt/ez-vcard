package ezvcard.io.text;

import java.io.IOException;

/**
 * Thrown when there's a problem parsing a line in a vCard file.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class VCardParseException extends IOException {
	private final String line;

	/**
	 * @param line the line that couldn't be parsed
	 */
	public VCardParseException(String line) {
		super("Problem parsing vCard line: " + line);
		this.line = line;
	}

	public String getLine() {
		return line;
	}
}
