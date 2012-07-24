package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.AgentType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.NoteType;

/*
 Copyright (c) 2012, Michael Angstadt
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
public class VCardWriterTest {
	/**
	 * Tests to make sure it contains the BEGIN, VERSION, and END types.
	 */
	@Test
	public void generalStructure() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		String actual = sw.toString();

		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("FN: John Doe\r\n");
		sb.append("END: vcard\r\n");
		String expected = sb.toString();

		assertEquals(actual, expected);
	}

	/**
	 * Make sure it does not print out the X-GENERATOR type if you don't want it
	 * to.
	 */
	@Test
	public void setAddGenerated() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		//with X-GENERATOR
		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("X-GENERATOR:"));

		//without X-GENERATOR
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("X-GENERATOR:"));
	}

	/**
	 * The TYPE sub types for 2.1 vCards should look like this:
	 * <p>
	 * <code>ADR;WORK;DOM:</code>
	 * </p>
	 * 
	 * The TYPE sub types for 3.0 vCards should look like this:
	 * <p>
	 * <code>ADR;TYPE=work,dom:</code>
	 * </p>
	 */
	@Test
	public void typeParameter() throws Exception {
		VCard vcard = new VCard();

		//one type
		AddressType adr = new AddressType();
		adr.addType(AddressTypeParameter.WORK);
		vcard.addAddress(adr);

		//two types
		adr = new AddressType();
		adr.addType(AddressTypeParameter.WORK);
		adr.addType(AddressTypeParameter.DOM);
		vcard.addAddress(adr);

		//three types
		adr = new AddressType();
		adr.addType(AddressTypeParameter.WORK);
		adr.addType(AddressTypeParameter.DOM);
		adr.addType(AddressTypeParameter.PARCEL);
		vcard.addAddress(adr);

		//2.1
		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		String actual = sw.toString();

		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("ADR;WORK: ;;;;;;\r\n");
		sb.append("ADR;WORK;DOM: ;;;;;;\r\n");
		sb.append("ADR;WORK;PARCEL;DOM: ;;;;;;\r\n");
		sb.append("END: vcard\r\n");
		String expected = sb.toString();

		assertEquals(expected, actual);

		//3.0
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		actual = sw.toString();

		sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 3.0\r\n");
		sb.append("ADR;TYPE=work: ;;;;;;\r\n");
		sb.append("ADR;TYPE=work,dom: ;;;;;;\r\n");
		sb.append("ADR;TYPE=work,parcel,dom: ;;;;;;\r\n");
		sb.append("END: vcard\r\n");
		expected = sb.toString();

		assertEquals(expected, actual);
	}

	/**
	 * Test to make sure it marshals sub types correctly.
	 */
	@Test
	public void subTypes() throws Exception {
		VCard vcard = new VCard();

		//one sub type
		AddressType adr = new AddressType();
		adr.getSubTypes().put("X-DOORMAN", "true");
		vcard.addAddress(adr);

		//two types
		adr = new AddressType();
		adr.getSubTypes().put("X-DOORMAN", "true");
		adr.getSubTypes().put("LANGUAGE", "FR");
		adr.getSubTypes().put("LANGUAGE", "es");
		vcard.addAddress(adr);

		//three types
		adr = new AddressType();
		adr.getSubTypes().put("X-DOORMAN", "true");
		adr.getSubTypes().put("LANGUAGE", "FR");
		adr.getSubTypes().put("LANGUAGE", "es");
		adr.getSubTypes().put("X-PARKING", "10");
		vcard.addAddress(adr);

		//2.1
		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		String actual = sw.toString();

		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("ADR;X-DOORMAN=true: ;;;;;;\r\n");
		sb.append("ADR;X-DOORMAN=true;LANGUAGE=es;LANGUAGE=FR: ;;;;;;\r\n");
		sb.append("ADR;X-DOORMAN=true;LANGUAGE=es;LANGUAGE=FR;X-PARKING=10: ;;;;;;\r\n");
		sb.append("END: vcard\r\n");
		String expected = sb.toString();

		assertEquals(expected, actual);

		//3.0
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		actual = sw.toString();

		sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 3.0\r\n");
		sb.append("ADR;X-DOORMAN=true: ;;;;;;\r\n");
		sb.append("ADR;X-DOORMAN=true;LANGUAGE=es,FR: ;;;;;;\r\n");
		sb.append("ADR;X-DOORMAN=true;LANGUAGE=es,FR;X-PARKING=10: ;;;;;;\r\n");
		sb.append("END: vcard\r\n");
		expected = sb.toString();

		assertEquals(expected, actual);
	}

	/**
	 * Test to make sure the folding scheme functionality works.
	 */
	@Test
	public void foldingScheme() throws Exception {
		VCard vcard = new VCard();
		NoteType note = new NoteType("The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.");
		vcard.addNote(note);
		FoldingScheme fs = new FoldingScheme(50, "  ");

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1, fs);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		String actual = sw.toString();

		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("NOTE: The vCard MIME Directory Profile also provid\r\n");
		sb.append("  es support for representing other important info\r\n");
		sb.append("  rmation about the person associated with the dir\r\n");
		sb.append("  ectory entry. For instance\\, the date of birth o\r\n");
		sb.append("  f the person\\; an audio clip describing the pron\r\n");
		sb.append("  unciation of the name associated with the direct\r\n");
		sb.append("  ory entry\\, or some other application of the dig\r\n");
		sb.append("  ital sound\\; longitude and latitude geo-position\r\n");
		sb.append("  ing information related to the person associated \r\n");
		sb.append("  with the directory entry\\; date and time that th\r\n");
		sb.append("  e directory information was last updated\\; annot\r\n");
		sb.append("  ations often written on a business card\\; Unifor\r\n");
		sb.append("  m Resource Locators (URL) for a website\\; public \r\n");
		sb.append("  key information.\r\n");
		sb.append("END: vcard\r\n");
		String expected = sb.toString();

		assertEquals(actual, expected);
	}

	/**
	 * Test to make sure it uses whatever newline string you want it to.
	 */
	@Test
	public void newline() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1, FoldingScheme.MIME_DIR, "*");
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		String actual = sw.toString();

		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard*");
		sb.append("VERSION: 2.1*");
		sb.append("FN: John Doe*");
		sb.append("END: vcard*");
		String expected = sb.toString();

		assertEquals(actual, expected);
	}

	/**
	 * Tests to make sure it marshals groups correctly.
	 */
	@Test
	public void groups() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		fn.setGroup("group1");
		vcard.setFormattedName(fn);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddGenerator(false);
		vcw.write(vcard);
		String actual = sw.toString();

		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("group1.FN: John Doe\r\n");
		sb.append("END: vcard\r\n");
		String expected = sb.toString();

		assertEquals(actual, expected);
	}

	/**
	 * Tests the AGENT types for 2.1 vCards (they are nested).
	 */
	@Test
	public void nestedAgent() throws Exception {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("Michael Angstadt");
		vcard.setFormattedName(fn);

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName(new FormattedNameType("Agent 007"));
		agentVcard.getNotes().add(new NoteType("Make sure that it properly folds long lines which are part of a nested AGENT type in a version 2.1 vCard."));
		AgentType agent = new AgentType(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName(new FormattedNameType("Agent 009"));
		secondAgentVCard.getNotes().add(new NoteType("Make sure that it ALSO properly folds THIS long line because it's part of an AGENT that's inside of an AGENT."));
		AgentType secondAgent = new AgentType(secondAgentVCard);
		agentVcard.setAgent(secondAgent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.write(vcard);
		String actual = sw.toString();

		//FIXME this test may fail on other machines because Class.getDeclaredFields() returns the fields in no particular order
		//@formatter:off
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN: vcard\r\n");
		sb.append("VERSION: 2.1\r\n");
		sb.append("FN: Michael Angstadt\r\n");
		sb.append("AGENT: \r\n"); //nested types should not have X-GENERATOR
			sb.append("BEGIN: vcard\r\n");
			sb.append("VERSION: 2.1\r\n");
			sb.append("FN: Agent 007\r\n");
			sb.append("AGENT: \r\n");
				sb.append("BEGIN: vcard\r\n");
				sb.append("VERSION: 2.1\r\n");
				sb.append("FN: Agent 009\r\n");
				sb.append("NOTE: Make sure that it ALSO properly folds THIS long line because it's par\r\n");
				sb.append(" t of an AGENT that's inside of an AGENT.\r\n");
				sb.append("END: vcard\r\n");
			sb.append("NOTE: Make sure that it properly folds long lines which are part of a neste\r\n");
			sb.append(" d AGENT type in a version 2.1 vCard.\r\n");
			sb.append("END: vcard\r\n");
		sb.append("X-GENERATOR: EZ vCard v0.1 http://code.google.com/p/ez-vcard\r\n");
		sb.append("END: vcard\r\n");
		String expected = sb.toString();
		//@formatter:on

		assertEquals(expected, actual);
	}
}
