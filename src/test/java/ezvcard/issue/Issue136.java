package ezvcard.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.ParseContext;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.WriteContext;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/136"
 */
public class Issue136 {
	@Test
	public void test() throws Exception {
		String vcardStr = //@formatter:off
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"ADR:foo;bar\r\n" +
		"END:VCARD"; //@formatter:on

		VCard vcard;
		try (VCardReader reader = new VCardReader(vcardStr)) {
			reader.registerScribe(new MarioAddressScribe());
			vcard = reader.readNext();
		}

		assertTrue(vcard.getAddresses().isEmpty());
		assertEquals("foo;bar", vcard.getProperty(MarioAddress.class).value);
	}

	private static class MarioAddressScribe extends VCardPropertyScribe<MarioAddress> {
		public MarioAddressScribe() {
			super(MarioAddress.class, "ADR");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return null;
		}

		@Override
		protected String _writeText(MarioAddress property, WriteContext context) {
			return property.value;
		}

		@Override
		protected MarioAddress _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
			return new MarioAddress(value);
		}

	}

	private static class MarioAddress extends VCardProperty {
		public String value;

		public MarioAddress(String value) {
			this.value = value;
		}
	}
}
