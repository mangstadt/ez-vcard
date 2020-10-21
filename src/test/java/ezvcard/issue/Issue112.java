package ezvcard.issue;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/112"
 */
public class Issue112 {
	@Test
	public void test() {
		String vcardStr = //@formatter:off
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=96=64=65;=C3=96=6D=C3=BC=72;;;\r\n" +
		"FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=96=6D=C3=BC=72=20=C3=96=64=65\r\n" +
		"END:VCARD\r\n"; //@formatter:on

		VCard vcard = Ezvcard.parse(vcardStr).first();

		VCardVersion version = VCardVersion.V2_1;
		{
			String actual = Ezvcard.write(vcard).version(version).prodId(false).go();
			String expected = //@formatter:off
			"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=96de;=C3=96m=C3=BCr\r\n" +
			"FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=C3=96m=C3=BCr =C3=96de\r\n" +
			"END:VCARD\r\n"; //@formatter:on

			assertEquals(expected, actual);
		}

		version = VCardVersion.V3_0;
		{
			String actual = Ezvcard.write(vcard).version(version).prodId(false).go();
			String expected = //@formatter:off
			"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"N:Öde;Ömür\r\n" +
			"FN:Ömür Öde\r\n" +
			"END:VCARD\r\n"; //@formatter:on

			assertEquals(expected, actual);
		}

		version = VCardVersion.V4_0;
		{
			String actual = Ezvcard.write(vcard).version(version).prodId(false).go();
			String expected = //@formatter:off
			"BEGIN:VCARD\r\n" +
			"VERSION:4.0\r\n" +
			"N:Öde;Ömür;;;\r\n" +
			"FN:Ömür Öde\r\n" +
			"END:VCARD\r\n"; //@formatter:on

			assertEquals(expected, actual);
		}
	}
}
