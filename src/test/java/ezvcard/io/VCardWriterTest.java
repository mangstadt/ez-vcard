package ezvcard.io;

import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.AgentType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.KindType;
import ezvcard.types.LabelType;
import ezvcard.types.MemberType;
import ezvcard.types.NoteType;
import ezvcard.types.ProdIdType;
import ezvcard.types.StructuredNameType;

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
		vcw.setAddProdId(false);
		vcw.write(vcard);
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"FN:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void check_supported_versions() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		StructuredNameType n = new StructuredNameType();
		n.setGiven("John");
		n.setFamily("Doe");
		vcard.setStructuredName(n);
		vcard.setMailer("Thunderbird");

		//all properties support the version
		{
			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertTrue(warnings.isEmpty());

			VCard parsedVCard = Ezvcard.parse(sw.toString()).first();
			assertEquals("Thunderbird", parsedVCard.getMailer().getValue());
		}

		//one property does not support the version
		{
			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, VCardVersion.V4_0);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertWarnings(1, warnings);

			//property not written to vCard
			VCard parsedVCard = Ezvcard.parse(sw.toString()).first();
			assertNull(parsedVCard.getMailer());
		}
	}

	@Test
	public void required_properties_21() throws Exception {
		VCardVersion version = VCardVersion.V2_1;

		//without N
		{
			VCard vcard = new VCard();
			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertWarnings(1, warnings);
		}

		//with N
		{
			VCard vcard = new VCard();
			StructuredNameType n = new StructuredNameType();
			n.setFamily("Joe");
			n.setGiven("John");
			vcard.setStructuredName(n);

			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertTrue(warnings.isEmpty());
		}
	}

	@Test
	public void required_properties_30() throws Exception {
		VCardVersion version = VCardVersion.V3_0;

		//without N or FN
		{
			VCard vcard = new VCard();
			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertWarnings(2, warnings);
		}

		//with N
		{
			VCard vcard = new VCard();
			StructuredNameType n = new StructuredNameType();
			n.setFamily("Joe");
			n.setGiven("John");
			vcard.setStructuredName(n);

			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertWarnings(1, warnings);
		}

		//with FN
		{
			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");

			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertWarnings(1, warnings);
		}

		//with both
		{
			VCard vcard = new VCard();
			StructuredNameType n = new StructuredNameType();
			n.setFamily("Joe");
			n.setGiven("John");
			vcard.setStructuredName(n);
			vcard.setFormattedName("John Doe");

			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertTrue(warnings.isEmpty());
		}
	}

	@Test
	public void required_properties_40() throws Exception {
		VCardVersion version = VCardVersion.V4_0;

		//without FN
		{
			VCard vcard = new VCard();
			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertWarnings(1, warnings);
		}

		//with FN
		{
			VCard vcard = new VCard();
			vcard.setFormattedName("John Doe");

			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, version);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertTrue(warnings.isEmpty());
		}
	}

	@Test
	public void setAddProdId() throws Exception {
		VCard vcard = new VCard();
		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		//with X-PRODID (2.1)
		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nX-PRODID:"));

		//with PRODID (3.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//with PRODID (4.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//with X-PRODID (2.1)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nX-PRODID:"));

		//with PRODID (3.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//with PRODID (4.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:"));

		//without X-PRODID (2.1)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nX-PRODID:"));

		//without PRODID (3.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nPRODID:"));

		//without PRODID (4.0)
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nPRODID:"));
	}

	@Test
	public void setAddProdId_overwrites_existing_prodId() throws Exception {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("John Doe");
		vcard.setFormattedName(fn);

		ProdIdType prodId = new ProdIdType("Acme Co.");
		vcard.setProdId(prodId);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertTrue(sw.toString().contains("\r\nPRODID:Acme Co."));

		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V3_0);
		vcw.setAddProdId(true);
		vcw.write(vcard);
		assertFalse(sw.toString().contains("\r\nPRODID:Acme Co."));
	}

	/**
	 * Tests types with nested vCards (i.e AGENT type) in version 2.1.
	 */
	@Test
	public void nestedVCard() throws Exception {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("Michael Angstadt");
		vcard.setFormattedName(fn);

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName(new FormattedNameType("Agent 007"));
		agentVcard.addNote(new NoteType("Make sure that it properly folds long lines which are part of a nested AGENT type in a version 2.1 vCard."));
		AgentType agent = new AgentType(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName(new FormattedNameType("Agent 009"));
		secondAgentVCard.addNote(new NoteType("Make sure that it ALSO properly folds THIS long line because it's part of an AGENT that's inside of an AGENT."));
		AgentType secondAgent = new AgentType(secondAgentVCard);
		agentVcard.setAgent(secondAgent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertWarnings(3, vcw.getWarnings()); //each vCard is missing the N property, which is required for 2.1
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:2.1\r\n" +
			"FN:Michael Angstadt\r\n" +
			"AGENT:\r\n" + //nested types should not have X-GENERATOR
			"BEGIN:VCARD\r\n" +
				"VERSION:2.1\r\n" +
				"FN:Agent 007\r\n" +
				"NOTE:Make sure that it properly folds long lines which are part of a nested \r\n" +
				" AGENT type in a version 2.1 vCard.\r\n" +
				"AGENT:\r\n" +
				"BEGIN:VCARD\r\n" +
					"VERSION:2.1\r\n" +
					"FN:Agent 009\r\n" +
					"NOTE:Make sure that it ALSO properly folds THIS long line because it's part \r\n" +
					" of an AGENT that's inside of an AGENT.\r\n" +
				"END:VCARD\r\n" +
			"END:VCARD\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Tests types with embedded vCards (i.e AGENT type) in version 3.0.
	 */
	@Test
	public void embeddedVCard() throws Exception {
		VCard vcard = new VCard();

		FormattedNameType fn = new FormattedNameType("Michael Angstadt");
		vcard.setFormattedName(fn);

		VCard agentVcard = new VCard();
		agentVcard.setFormattedName(new FormattedNameType("Agent 007"));
		NoteType note = new NoteType("Bonne soir�e, 007.");
		note.setLanguage("fr");
		agentVcard.addNote(note);
		AgentType agent = new AgentType(agentVcard);
		vcard.setAgent(agent);

		//i herd u liek AGENTs...
		VCard secondAgentVCard = new VCard();
		secondAgentVCard.setFormattedName(new FormattedNameType("Agent 009"));
		note = new NoteType("Good evening, 009.");
		note.setLanguage("en");
		secondAgentVCard.addNote(note);
		AgentType secondAgent = new AgentType(secondAgentVCard);
		agentVcard.setAgent(secondAgent);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0, null, "\r\n");
		vcw.setAddProdId(false);
		vcw.write(vcard);
		assertWarnings(3, vcw.getWarnings()); //each vCard is missing the N property, which is required for 3.0
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
			"VERSION:3.0\r\n" +
			"FN:Michael Angstadt\r\n" +
			"AGENT:" + //nested types should not have PRODID
			"BEGIN:VCARD\\n" +
				"VERSION:3.0\\n" +
				"FN:Agent 007\\n" +
				"NOTE\\;LANGUAGE=fr:Bonne soir�e\\\\\\, 007.\\n" +
				"AGENT:" +
				"BEGIN:VCARD\\\\n" +
					"VERSION:3.0\\\\n" +
					"FN:Agent 009\\\\n" +
					"NOTE\\\\\\;LANGUAGE=en:Good evening\\\\\\\\\\\\\\, 009.\\\\n" +
				"END:VCARD\\\\n\\n" +
			"END:VCARD\\n\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * Test to make sure it marshals LABELs correctly.
	 */
	@Test
	public void labels() throws Exception {
		VCard vcard = new VCard();

		//address with label
		AddressType adr = new AddressType();
		adr.setStreetAddress("123 Main St.");
		adr.setLocality("Austin");
		adr.setRegion("TX");
		adr.setPostalCode("12345");
		adr.setLabel("123 Main St.\r\nAustin, TX 12345");
		adr.addType(AddressTypeParameter.HOME);
		vcard.addAddress(adr);

		//address without label
		adr = new AddressType();
		adr.setStreetAddress("222 Broadway");
		adr.setLocality("New York");
		adr.setRegion("NY");
		adr.setPostalCode("99999");
		adr.addType(AddressTypeParameter.WORK);
		vcard.addAddress(adr);

		//orphaned label
		LabelType label = new LabelType("22 Spruce Ln.\r\nChicago, IL 11111");
		label.addType(AddressTypeParameter.PARCEL);
		vcard.addOrphanedLabel(label);

		//3.0
		//LABEL types should be used
		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V3_0, null, "\r\n");
		vcw.setAddProdId(false);
		vcw.write(vcard);
		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:3.0\r\n" +
		"ADR;TYPE=home:;;123 Main St.;Austin;TX;12345;\r\n" +
		"LABEL;TYPE=home:123 Main St.\\nAustin\\, TX 12345\r\n" +
		"ADR;TYPE=work:;;222 Broadway;New York;NY;99999;\r\n" +
		"LABEL;TYPE=parcel:22 Spruce Ln.\\nChicago\\, IL 11111\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);

		//4.0
		//LABEL parameters should be used
		sw = new StringWriter();
		vcw = new VCardWriter(sw, VCardVersion.V4_0, null, "\r\n");
		vcw.setAddProdId(false);
		vcw.write(vcard);
		actual = sw.toString();

		//@formatter:off
		expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"ADR;LABEL=\"123 Main St.\\nAustin, TX 12345\";TYPE=home:;;123 Main St.;Austin;TX;12345;\r\n" +
		"ADR;TYPE=work:;;222 Broadway;New York;NY;99999;\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * If the type's marshal method throws a {@link SkipMeException}, then a
	 * warning should be added to the warnings list and the type object should
	 * NOT be marshalled.
	 */
	@Test
	public void skipMeException() throws Exception {
		VCard vcard = new VCard();

		//add N property so a warning isn't generated (2.1 requires N to be present)
		StructuredNameType n = new StructuredNameType();
		vcard.setStructuredName(n);

		LuckyNumType num = new LuckyNumType();
		num.luckyNum = 24;
		vcard.addProperty(num);

		//should be skipped
		num = new LuckyNumType();
		num.luckyNum = 13;
		vcard.addProperty(num);

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V2_1);
		vcw.setAddProdId(false);
		vcw.write(vcard);

		assertWarnings(1, vcw.getWarnings());

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:2.1\r\n" +
		"N:;;;;\r\n" +
		"X-LUCKY-NUM:24\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void kind_and_member_combination() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		vcard.addMember(new MemberType("http://uri.com"));

		//correct KIND
		{
			vcard.setKind(KindType.group());

			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, VCardVersion.V4_0);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertTrue(warnings.isEmpty());

			VCard parsedVCard = Ezvcard.parse(sw.toString()).first();
			assertEquals("group", parsedVCard.getKind().getValue());
			assertEquals(1, parsedVCard.getMembers().size());
			assertEquals("http://uri.com", parsedVCard.getMembers().get(0).getUri());
		}

		//wrong KIND
		{
			vcard.setKind(KindType.individual());

			StringWriter sw = new StringWriter();
			VCardWriter vcw = new VCardWriter(sw, VCardVersion.V4_0);
			vcw.write(vcard);

			List<String> warnings = vcw.getWarnings();
			assertWarnings(1, warnings);

			VCard parsedVCard = Ezvcard.parse(sw.toString()).first();
			assertEquals("individual", parsedVCard.getKind().getValue());
			assertTrue(parsedVCard.getMembers().isEmpty());
		}
	}

	@Test
	public void invalid_parameter_chars() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe").getSubTypes().put("X-TEST", "one" + ((char) 28) + "two");

		StringWriter sw = new StringWriter();
		VCardWriter vcw = new VCardWriter(sw, VCardVersion.V4_0);
		vcw.setAddProdId(false);
		vcw.write(vcard);

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:VCARD\r\n" +
		"VERSION:4.0\r\n" +
		"FN;X-TEST=onetwo:John Doe\r\n" +
		"END:VCARD\r\n";
		//@formatter:on

		assertEquals(actual, expected);
		assertWarnings(1, vcw.getWarnings());
	}
}
