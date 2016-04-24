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
				"outlook-2003" // &#12; in fburl is not valid xml
		);
		updateSamples(VCardVersion.V3_0);
	}

	@Test
	public void equals_compare_vcard_4_to_xcard() throws Exception {
		convertAllFromVCard(VCardVersion.V4_0, true, false,
				"outlook", // newlines not preserved on linux
				"black_berry", // encoding parameter for image?
				"rfc2426" // {TYPE=[INTERNET,pref]} != {TYPE=[INTERNET],PREF=[1]} ?
		);
	}

	@Test
	public void content_compare_vcard_4_to_xcard() throws Exception {
		convertAllFromVCard(VCardVersion.V4_0, false, true,
				"outlook-2007", // newlines not preserved on linux
				"android", // empty <type/> tag added to email
				"ms_outlook", // empty <street/> tag added to home adr
				"evolution", "mac_address_book", // string escape issue
				"iphone", "lotus_notes" // groups are reordered
		);
	}

	@Test
	public void compare_xcard_to_vcard_4() throws Exception {
		convertAllToVCard(VCardVersion.V4_0, true, true, 
				"outlook" // newline conversion on linux
		);
	}

	@Test
	public void compare_vcard_3_to_xcard() throws Exception {
		convertAllFromVCard(VCardVersion.V3_0, false, true,
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
	public void content_compare_xcard_to_vcard_3() throws Exception {
		convertAllToVCard(VCardVersion.V3_0, false, true);
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
