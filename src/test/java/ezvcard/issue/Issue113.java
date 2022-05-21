package ezvcard.issue;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Geo;
import ezvcard.util.DefaultLocaleRule;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	public void submitter_unit_test() {
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
	 * Will Java parse Western numerals if the default locale is non-Western?
	 */
	@Test
	public void parseDouble_other_locales() {
		for (Locale locale : Locale.getAvailableLocales()) {
			Locale.setDefault(locale);
			double actual = Double.parseDouble("1.0"); //ez-vcard uses this method when parsing numbers
			double expected = 1.0;
			assertEquals(expected, actual, 0);
		}
	}

	@Test
	public void root_locale_numbers() {
		double number = 1.23;
		assertEquals("۱٫۲۳۰۰۰۰", String.format("%f", number));
		assertEquals("1.230000", String.format(Locale.ROOT, "%f", number));
	}

	@Test
	public void root_locale_dates() throws Exception {
		String pattern = "yyyy-MM-dd HH:mm";
		Date date = new SimpleDateFormat(pattern).parse("2020-10-28 12:00");

		DateFormat df = new SimpleDateFormat(pattern);
		assertEquals("۲۰۲۰-۱۰-۲۸ ۱۲:۰۰", df.format(date));

		df = new SimpleDateFormat(pattern, Locale.ROOT);
		assertEquals("2020-10-28 12:00", df.format(date));
	}

	@Test
	public void decimal_format() {
		NumberFormat nf = new DecimalFormat("00");
		assertEquals("۰۸", nf.format(8));

		nf = new DecimalFormat("00", DecimalFormatSymbols.getInstance(Locale.ROOT));
		assertEquals("08", nf.format(8));
	}
}
