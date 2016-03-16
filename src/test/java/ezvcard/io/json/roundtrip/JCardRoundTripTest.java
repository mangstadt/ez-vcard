package ezvcard.io.json.roundtrip;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.json.JCardReader;
import ezvcard.io.json.JCardWriter;
import ezvcard.io.text.*;
import ezvcard.util.IOUtils;

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
public class JCardRoundTripTest {
	private final class ExtensionFilter implements FilenameFilter {
		private final String ext;

		private ExtensionFilter(String extension) {
			ext = extension;
		}

		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(ext);
		}
	}

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void convert_to_jcard() throws Throwable {
		File input = new File(JCardRoundTripTest.class.getResource("").toURI());
		File[] dir = input.listFiles(new ExtensionFilter(".vcf"));
		for (File vcf : dir) {
			String file = vcf.getName().toString();
			if (file.startsWith("John_Doe_EVOLUTION") || file.startsWith("John_Doe_IPHONE")
					|| file.startsWith("John_Doe_MAC_ADDRESS_BOOK") || file.toLowerCase().contains("outlook")) {
				continue;
			}
			assertEquals(file, read(file.replace(".vcf", ".json")), convertVCard(vcf));
		}
	}

	@Test
	public void convert_from_jcard() throws Throwable {
		File input = new File(JCardRoundTripTest.class.getResource("").toURI());
		File[] dir = input.listFiles(new ExtensionFilter(".json"));
		for (File jcard : dir) {
			String file = jcard.getName().toString();
			assertEquals(file, read(file.replace(".json", ".vcf")), convertJCard(jcard));
		}
	}

	/**
	 * Run this if there are new vcard samples added to
	 * src/test/resources/ezvcard/io/text/, or if the output format changes.
	 * 
	 * @throws Throwable
	 */
	public void update_samples() throws Throwable {
		File input = new File(SampleVCardsTest.class.getResource("").toURI());
		File[] dir = input.listFiles(new ExtensionFilter(".vcf"));
		for (File vcf : dir) {
			String file = vcf.getName().toString();
			write(file.replace(".vcf", ".json"), convertVCard(vcf));
		}

		input = new File(JCardRoundTripTest.class.getResource("").toURI());
		dir = input.listFiles(new ExtensionFilter(".json"));
		for (File jcard : dir) {
			String file = jcard.getName().toString();
			write(file.replace(".json", ".vcf"), convertJCard(jcard));
		}
	}

	private static String convertJCard(File jcard) throws IOException {
		StringWriter sw = new StringWriter();
		VCardWriter writer = new VCardWriter(sw, VCardVersion.V4_0);

		try {
			for (VCard vcard : new JCardReader(jcard).readAll()) {
				writer.write(vcard);
			}
		} finally {
			writer.close();
		}
		return sw.toString();
	}

	public static String convertVCard(File vcf) throws IOException, FileNotFoundException {
		StringWriter sw = new StringWriter();
		JCardWriter writer = new JCardWriter(sw);
		writer.setIndent(true);

		try {
			for (VCard vcard : new VCardReader(vcf).readAll()) {
				writer.write(vcard);
			}
		} finally {
			writer.close();
		}
		return sw.toString();
	}

	private static String read(String fileName) throws IOException {
		return IOUtils.toString(IOUtils.utf8Reader(JCardRoundTripTest.class.getResourceAsStream(fileName)));
	}

	private static void write(String fileName, String converted) throws IOException {
		File file = new File("src/test/resources/ezvcard/io/json/roundtrip/" + fileName);
		Writer writer = new FileWriter(file);
		try {
			writer.write(converted);
			writer.flush();
		} finally {
			writer.close();
		}
	}
}
