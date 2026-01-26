package ezvcard.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.ParseWarning;
import ezvcard.property.Birthday;
import ezvcard.property.RawProperty;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/134"
 */
public class Issue134 {
	@Test
	public void test_2_1() {
		String vcardStr = //@formatter:off
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"BDAY:19879215\r\n" +
		"END:VCARD\r\n"; //@formatter:on

		List<List<ParseWarning>> warnings = new ArrayList<>();

		VCard vcard = Ezvcard.parse(vcardStr).warnings(warnings).first();

		assertNull(vcard.getBirthday());

		RawProperty bday = vcard.getExtendedProperty("BDAY");
		assertEquals("19879215", bday.getValue());

		assertEquals(1, warnings.get(0).size());
		assertEquals(Integer.valueOf(5), warnings.get(0).get(0).getCode());
	}

	@Test
	public void test_3_0() {
		String vcardStr = //@formatter:off
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"BDAY:19879215\r\n" +
		"END:VCARD\r\n"; //@formatter:on

		List<List<ParseWarning>> warnings = new ArrayList<>();

		VCard vcard = Ezvcard.parse(vcardStr).warnings(warnings).first();

		assertNull(vcard.getBirthday());

		RawProperty bday = vcard.getExtendedProperty("BDAY");
		assertEquals("19879215", bday.getValue());

		assertEquals(1, warnings.get(0).size());
		assertEquals(Integer.valueOf(5), warnings.get(0).get(0).getCode());
	}

	@Test
	public void test_4_0() {
		String vcardStr = //@formatter:off
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"BDAY:19879215\r\n" +
		"END:VCARD\r\n"; //@formatter:on

		List<List<ParseWarning>> warnings = new ArrayList<>();

		VCard vcard = Ezvcard.parse(vcardStr).warnings(warnings).first();

		Birthday bday = vcard.getBirthday();
		assertNull(bday.getDate());
		assertEquals("19879215", bday.getText());

		assertEquals(1, warnings.get(0).size());
		assertEquals(Integer.valueOf(6), warnings.get(0).get(0).getCode());
	}
}
