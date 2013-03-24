package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import ezvcard.VCard;

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
public class JCardWriterTest {
	@Test
	public void write_single_vcard() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"Simon Perreault\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void write_multiple_vcards() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault");
		writer.write(vcard);

		vcard = new VCard();
		vcard.setFormattedName("John Doe");
		writer.write(vcard);

		writer.close();

		//@formatter:off
		String expected =
		"[\"vcardstream\"," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"Simon Perreault\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\",{},\"text\",\"4.0\"]," +
		      "[\"fn\",{},\"text\",\"John Doe\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		assertEquals(expected, sw.toString());
	}

	@Test
	public void setAddProdid() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(true);

		VCard vcard = new VCard();
		vcard.setFormattedName("Simon Perreault");
		writer.write(vcard);

		writer.close();

		String regex = Pattern.quote("[\"prodid\",{},\"text\",") + "\".*?\"" + Pattern.quote("]");
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sw.toString());
		assertTrue(m.find());
		assertFalse(m.find());
	}

	@Test
	public void write_no_vcards() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.close();
		assertEquals("", sw.toString());
	}

	@Test
	public void check_required_properties() throws Exception {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setAddProdId(false);

		VCard vcard = new VCard();
		writer.write(vcard);
		assertEquals(1, writer.getWarnings().size()); //FN is required

		writer.close();
	}
}
