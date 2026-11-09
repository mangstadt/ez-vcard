package ezvcard.issue;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.ClassRule;
import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Title;
import ezvcard.util.DefaultLocaleRule;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/pull/166"
 */
public class PR166 {
	@ClassRule
	public static final DefaultLocaleRule tzRule = new DefaultLocaleRule(new Locale("tr", "TR"));

	@Test
	public void converting_letter_i_to_lower_case_under_turkish_locale() {
		VCard vcard = new VCard();
		vcard.getTitles().add(new Title("Director"));
		String actual = Ezvcard.writeJson(vcard).prodId(false).go();

		//                                              should be an English i
		//                                                                   v
		String expected = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"title\",{},\"text\",\"Director\"]]]";

		assertEquals(expected, actual);
	}
}
