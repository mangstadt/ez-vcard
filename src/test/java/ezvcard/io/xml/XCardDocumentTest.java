package ezvcard.io.xml;

import static ezvcard.util.StringUtils.NEWLINE;
import static ezvcard.util.TestUtils.assertWarningsLists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.scribe.SkipMeScribe;
import ezvcard.util.IOUtils;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class XCardDocumentTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void parseAll() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);
		Iterator<VCard> it = xcard.parseAll().iterator();

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			assertEquals("Dr. Gregory House M.D.", vcard.getFormattedName().getValue());
		}

		{
			VCard vcard = it.next();
			assertEquals(VCardVersion.V4_0, vcard.getVersion());
			assertEquals(1, vcard.getProperties().size());

			assertEquals("Dr. Lisa Cuddy M.D.", vcard.getFormattedName().getValue());
		}

		assertFalse(it.hasNext());
		assertWarningsLists(xcard.getParseWarnings(), 0, 0);
	}

	@Test
	public void parseAll_empty() throws Exception {
		XCardDocument xcard = new XCardDocument();
		Iterator<VCard> it = xcard.parseAll().iterator();

		assertFalse(it.hasNext());
		assertWarningsLists(xcard.getParseWarnings());
	}

	@Test
	public void parseFirst() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<fn><text>Dr. Gregory House M.D.</text></fn>" +
			"</vcard>" +
			"<vcard>" +
				"<fn><text>Dr. Lisa Cuddy M.D.</text></fn>" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcard = new XCardDocument(xml);

		VCard vcard = xcard.parseFirst();
		assertEquals(VCardVersion.V4_0, vcard.getVersion());
		assertEquals(1, vcard.getProperties().size());

		assertEquals("Dr. Gregory House M.D.", vcard.getFormattedName().getValue());

		assertWarningsLists(xcard.getParseWarnings(), 0);
	}

	@Test
	public void parseFirst_empty() throws Exception {
		XCardDocument xcard = new XCardDocument();
		VCard vcard = xcard.parseFirst();

		assertNull(vcard);
		assertWarningsLists(xcard.getParseWarnings());
	}

	@Test
	public void parse_clear_warnings() throws Exception {
		//@formatter:off
		String xml =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" +
			"<vcard>" +
				"<skipme />" +
			"</vcard>" +
			"<vcard>" +
				"<x-foo />" +
			"</vcard>" +
		"</vcards>";
		//@formatter:on

		XCardDocument xcr = new XCardDocument(xml);
		xcr.registerScribe(new SkipMeScribe());

		xcr.parseAll();
		assertWarningsLists(xcr.getParseWarnings(), 1, 0);

		xcr.parseAll();
		assertWarningsLists(xcr.getParseWarnings(), 1, 0);
	}

	@Test
	public void write_prettyPrint() throws Exception {
		XCardDocument xcard = new XCardDocument();
		xcard.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		xcard.add(vcard);

		String actual = xcard.write(2);

		//@formatter:off
		String expected =
		"<vcards xmlns=\"" + VCardVersion.V4_0.getXmlNamespace() + "\">" + NEWLINE +
		"  <vcard>" + NEWLINE +
		"    <fn>" + NEWLINE +
		"      <text>John Doe</text>" + NEWLINE +
		"    </fn>" + NEWLINE +
		"  </vcard>" + NEWLINE +
		"</vcards>";
		//@formatter:on

		//use "String.contains()" to ignore the XML declaration at the top
		assertTrue("Expected:" + NEWLINE + expected + NEWLINE + NEWLINE + "Actual:" + NEWLINE + actual, actual.contains(expected));
	}

	@Test
	public void write_utf8() throws Exception {
		XCardDocument xcard = new XCardDocument();

		VCard vcard = new VCard();
		vcard.addNote("\u019dote");
		xcard.add(vcard);

		File file = tempFolder.newFile();
		xcard.write(file);

		String xml = IOUtils.getFileContents(file, "UTF-8");
		assertTrue(xml.matches("(?i)<\\?xml.*?encoding=\"utf-8\".*?\\?>.*"));
		assertTrue(xml.matches(".*?<note><text>\u019dote</text></note>.*"));
	}
}
