package ezvcard.issue;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/114"
 */
public class Issue114 {
	@Test
	public void test() throws Exception {
		VCard vcard;
		try (InputStream in = getClass().getResourceAsStream("issue114.vcf")) {
			vcard = Ezvcard.parse(in).first();
		}

		try {
			Ezvcard.write(vcard).go();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Property \"ADR\" has a parameter named \"LABEL\" whose value contains one or more invalid characters.  The following characters are not permitted: [ \\n \\r \" ]", e.getMessage());
		}

		Ezvcard.write(vcard).caretEncoding(true).go(); //should not throw any exceptions
	}
}
