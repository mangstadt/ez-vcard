package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * Property and parameter names must not depend on the JVM default locale. In
 * Turkish, "I".toLowerCase() is a dotless "\u0131", which produced names such
 * as "t\u0131tle" in jCard and xCard output.
 */
public class DefaultLocaleTest {
	private Locale defaultLocale;

	@Before
	public void before() {
		defaultLocale = Locale.getDefault();
		Locale.setDefault(new Locale("tr", "TR"));
	}

	@After
	public void after() {
		Locale.setDefault(defaultLocale);
	}

	private static VCard vcard() {
		VCard vcard = new VCard();
		vcard.setFormattedName("Ali");
		vcard.addTitle("Engineer");
		vcard.addEmail("ali@example.com");
		return vcard;
	}

	@Test
	public void jcard() {
		String json = Ezvcard.writeJson(vcard()).prodId(false).go();
		assertFalse(json, json.contains("\u0131"));
		assertTrue(json, json.contains("[\"title\",{},\"text\",\"Engineer\"]"));
		assertTrue(json, json.contains("[\"email\",{},\"text\",\"ali@example.com\"]"));
	}

	@Test
	public void xcard() {
		String xml = Ezvcard.writeXml(vcard()).prodId(false).go();
		assertFalse(xml, xml.contains("\u0131"));
		assertTrue(xml, xml.contains("<title>"));
		assertTrue(xml, xml.contains("<email>"));
	}

	@Test
	public void jcard_round_trip() {
		String json = Ezvcard.writeJson(vcard()).prodId(false).go();
		Locale.setDefault(Locale.ROOT);
		VCard parsed = Ezvcard.parseJson(json).first();
		assertEquals(1, parsed.getTitles().size());
		assertEquals(1, parsed.getEmails().size());
	}
}
