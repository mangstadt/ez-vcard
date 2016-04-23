package ezvcard.io.roundtrip;

import java.io.*;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.io.StreamReader;
import ezvcard.io.StreamWriter;
import ezvcard.io.xml.XCardReader;
import ezvcard.io.xml.XCardWriter;

public class XCardRoundTripTest extends RoundTripTestBase {

	public XCardRoundTripTest() throws Exception {
		updateSamples(VCardVersion.V4_0,
				"outlook-2003", // &#12; in fburl is not valid xml
				"android", // empty <type/> tag added to email
				"ms_outlook", // empty <street/> tag added to home adr
				"evolution", "mac_address_book", // string escape issue
				"iphone", "lotus_notes" // groups are reordered
		);
		updateSamples(VCardVersion.V3_0,
				"android", // empty <type/> tag added to email
				"ms_outlook", // empty <street/> tag added to home adr
				"evolution", "mac_address_book", // string escape issue
				"iphone", "lotus_notes", // groups are reordered
				"outlook-2007", // label got lost
				"rfc6350", // tel uri got lost
				"outlook-2003", "thunderbird" // uppercase text got converted to lowercase
		);
	}

	@Test
	public void convert_vcard_4_to_xcard() throws Exception {
		convertAllFromVCard(VCardVersion.V4_0);
	}

	@Test
	public void convert_vcard_4_from_xcard() throws Exception {
		convertAllToVCard(VCardVersion.V4_0);
	}

	@Test
	public void convert_vcard_3_to_xcard() throws Exception {
		convertAllFromVCard(VCardVersion.V3_0);
	}

	@Test
	public void convert_vcard_3_from_xcard() throws Exception {
		convertAllToVCard(VCardVersion.V3_0);
	}

	@Override
	protected String getTargetExtension() {
		return "xml";
	}

	@Override
	protected StreamWriter getTargetWriter(Writer sw) {
		return new XCardWriter(sw);
	}

	@Override
	protected StreamReader getTargetReader(File file) throws FileNotFoundException {
		return new XCardReader(file);
	}

}
