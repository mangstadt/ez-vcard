package ezvcard.issue;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Geo;
import ezvcard.util.DefaultLocaleRule;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import java.util.Locale;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/113"
 */
public class Issue113 {
	private final Locale nonWesternLocale = new Locale("fa", "ir", "u-un-arabext");

	@Rule
	public final DefaultLocaleRule defaultLocaleRule = new DefaultLocaleRule(nonWesternLocale);

	@Test
	public void issue113() {
		VCard vcard = new VCard(VCardVersion.V4_0);
		vcard.setGeo(new Geo(1.0, 2.0));

		String actual = Ezvcard.write(vcard).prodId(false).go();
		String expected = //@formatter:off
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"GEO:geo:1.0,2.0\r\n" +
		"END:VCARD\r\n"; //@formatter:on
		assertEquals(expected, actual);
	}

	/**
	 * ez-vcard uses {@link Double#parseDouble} to parse floating-point numbers.
	 */
	@Test
	public void parse_western_digits_in_non_western_locales() {
		for (Locale locale : Locale.getAvailableLocales()) {
			Locale.setDefault(locale);
			double actual = Double.parseDouble("1.0");
			double expected = 1.0;
			assertEquals(expected, actual, 0);
		}
	}

	@Test
	public void can_root_locale_be_used() {
		assertEquals("۱٫۲۳۰۰۰۰", String.format("%f", 1.23));
		assertEquals("1.230000", String.format(Locale.ROOT, "%f", 1.23));
	}
}
