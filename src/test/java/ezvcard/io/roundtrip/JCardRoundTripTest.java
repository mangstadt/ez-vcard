package ezvcard.io.roundtrip;

import java.io.*;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.io.StreamReader;
import ezvcard.io.StreamWriter;
import ezvcard.io.json.JCardReader;
import ezvcard.io.json.JCardWriter;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 * @author Buddy Gorven
 */
public class JCardRoundTripTest extends RoundTripTestBase {

	public JCardRoundTripTest() throws Throwable {
		updateSamples(VCardVersion.V4_0);
	}

	@Test
	public void convert_to_jcard() throws Throwable {
		convertAllFromVCard(VCardVersion.V4_0, true, true,
				"outlook" // newline conversion on linux
		);
	}

	@Test
	public void convert_from_jcard() throws Throwable {
		convertAllToVCard(VCardVersion.V4_0, true, true,
				"outlook" // newline conversion on linux
		);
	}

	@Override
	protected StreamWriter getTargetWriter(Writer sw) {
		JCardWriter writer = new JCardWriter(sw);
		writer.setPrettyPrint(true);
		return writer;
	}

	@Override
	protected StreamReader getTargetReader(File file) throws FileNotFoundException {
		return new JCardReader(file);
	}

	@Override
	protected String getTargetExtension() {
		return "json";
	}
}
