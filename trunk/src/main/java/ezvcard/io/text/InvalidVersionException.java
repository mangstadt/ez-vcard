package ezvcard.io.text;

/**
 * Thrown when an invalid VERSION property is encountered in a vCard.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class InvalidVersionException extends VCardParseException {
	private final String version;

	/**
	 * @param version the value of the invalid VERSION property
	 * @param line the line that couldn't be parsed
	 */
	public InvalidVersionException(String version, String line) {
		super(line);
		this.version = version;
	}

	public String getVersion() {
		return version;
	}
}
