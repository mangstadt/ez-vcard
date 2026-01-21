package ezvcard.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.InputStream;

import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.chain.ChainingTextWriter;

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

		ChainingTextWriter writer = Ezvcard.write(vcard);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, writer::go);
		assertEquals("Property \"ADR\" has a parameter named \"LABEL\" whose value contains one or more invalid characters.  The following characters are not permitted: [ \\n \\r \" ]", e.getMessage());

		Ezvcard.write(vcard).caretEncoding(true).go(); //should not throw any exceptions
	}
}
