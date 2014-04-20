package ezvcard.io.text;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VCardRawLineTest {
	@Test
	public void isBegin() {
		VCardRawLine line = new VCardRawLine.Builder().name("BeGiN").build();
		assertTrue(line.isBegin());
		assertFalse(line.isVersion());
		assertFalse(line.isEnd());
	}

	@Test
	public void isVersion() {
		VCardRawLine line = new VCardRawLine.Builder().name("VeRsIoN").build();
		assertFalse(line.isBegin());
		assertTrue(line.isVersion());
		assertFalse(line.isEnd());
	}

	@Test
	public void isEnd() {
		VCardRawLine line = new VCardRawLine.Builder().name("EnD").build();
		assertFalse(line.isBegin());
		assertFalse(line.isVersion());
		assertTrue(line.isEnd());
	}

	@Test(expected = IllegalArgumentException.class)
	public void no_name() {
		new VCardRawLine.Builder().build();
	}
}
