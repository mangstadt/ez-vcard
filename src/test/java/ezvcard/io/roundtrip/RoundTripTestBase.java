package ezvcard.io.roundtrip;

import static org.junit.Assert.*;

import java.io.*;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.StreamReader;
import ezvcard.io.StreamWriter;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import ezvcard.property.VCardProperty;
import ezvcard.util.IOUtils;

public abstract class RoundTripTestBase {

	static final class Filter implements FilenameFilter {
		private final String extension;
		private final String[] excludes;

		private Filter(String extension, String... excludes) {
			this.extension = "." + extension;
			for (int i = 0; i < excludes.length; i++) {
				excludes[i] = excludes[i].toLowerCase();
			}
			this.excludes = excludes;
		}

		public boolean accept(File dir, String name) {
			name = name.toLowerCase();
			if (!name.endsWith(extension)) {
				return false;
			} else {
				for (String exclude : excludes) {
					if (name.contains(exclude)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	private static final String VCF_EXTENSION = "vcf";

	protected RoundTripTestBase() {
		getSampleDir().mkdirs();
		for (File existing : getSampleDir().listFiles()) {
			existing.delete();
		}
	}

	/**
	 * Run this if there are new vcard samples added to
	 * src/test/resources/ezvcard/io/text/, or if the output format changes.
	 * 
	 * @param version the version of VCard to roundtrip to
	 * @param excludes files to exclude (i.e. files that are known to fail
	 * roundtrip testing). Files will be excluded if the exclude string is found
	 * anywhere in the file name (case-insensitive)
	 */
	public void updateSamples(VCardVersion version, String... excludes) throws Exception {
		// Convert source files to target type
		File[] sourceFiles = listFiles(new File("src/test/resources/ezvcard/io/text"), VCF_EXTENSION, excludes);
		for (File sourceFile : sourceFiles) {
			String file = sourceFile.getName().toString();
			StringWriter sw = new StringWriter();
			try {
				convert(getVCardReader(sourceFile), getTargetWriter(sw), version);
			} catch (Exception e) {
				throw new Exception("Error converting " + file, e);
			}
			write(file.replaceAll("vcf$", "v" + version.getVersion() + "." + getTargetExtension()), sw.toString());
		}

		// Convert files from target type back to vcard
		for (File targetFile : listFiles(version, getTargetExtension())) {
			String file = targetFile.getName().toString();
			StringWriter sw = new StringWriter();
			try {
				convert(getTargetReader(targetFile), getVCardWriter(sw, version), version);
			} catch (Exception e) {
				throw new Exception("Error converting " + file, e);
			}
			write(file.replace(getTargetExtension(), VCF_EXTENSION), sw.toString());
		}
	}

	/**
	 * Converts all files in {@link #getSampleDir()} with names ending in
	 * {@link #VCF_EXTENSION} using {@link #getTargetWriter(StringWriter)} , and
	 * asserts that the content matches the file with the corresponding target
	 * extension.
	 * 
	 * @param excludes source files to exclude from this test
	 * @throws IOException
	 */
	public void convertAllFromVCard(VCardVersion version, boolean equals, boolean content, String... excludes) throws Exception {
		for (File vcf : listFiles(version, VCF_EXTENSION, excludes)) {
			String vcardFile = vcf.getName().toString();
			String targetFile = vcardFile.replace(VCF_EXTENSION, getTargetExtension());
			StringWriter vcardSW = new StringWriter();
			try {
				List<VCard> vcard = convert(getVCardReader(vcf), getTargetWriter(vcardSW), version);
				List<VCard> card = convert(getTargetReader(new File(getSampleDir(), targetFile)), null, version);
				if (equals) {
					assertEquals(vcardFile, card, vcard);
				}
			} catch (Exception e) {
				throw new Exception("Error converting " + vcardFile, e);
			}
			if (content) {
				assertEquals(vcardFile, read(targetFile), vcardSW.toString());
			}
		}
	}

	/**
	 * Converts all files in {@link #getSampleDir()} with names ending in
	 * {@link #getTargetExtension()} using
	 * {@link #getVCardWriter(StringWriter, VCardVersion)}, and asserts that the
	 * content matches the file with the corresponding vcard extension.
	 */
	public void convertAllToVCard(VCardVersion version, boolean equals, boolean content, String... excludes) throws Exception {
		for (File target : listFiles(version, getTargetExtension(), excludes)) {
			String targetFile = target.getName().toString();
			String vcardFile = targetFile.replace(getTargetExtension(), VCF_EXTENSION);
			StringWriter targetSW = new StringWriter();
			try {
				List<VCard> card = convert(getTargetReader(target), getVCardWriter(targetSW, version), version);
				List<VCard> vcard = convert(getVCardReader(new File(getSampleDir(), vcardFile)), null, version);
				if (equals) {
					assertEquals(vcardFile, vcard, card);
				}
			} catch (Exception e) {
				throw new Exception("Error converting " + targetFile, e);
			}
			if (content) {
				assertEquals(targetFile, read(vcardFile), targetSW.toString());
			}
		}
	}

	/**
	 * @return The extension of the file type under test, not including the "."
	 */
	protected abstract String getTargetExtension();

	/**
	 * @return A writer for the file type under test
	 */
	protected abstract StreamWriter getTargetWriter(Writer sw);

	/**
	 * @return A reader for the file type under test
	 */
	protected abstract StreamReader getTargetReader(File file) throws FileNotFoundException;

	protected File getSampleDir() {
		return new File("src/test/resources/ezvcard/io/" + getTargetExtension() + "/roundtrip/");
	}

	protected StreamWriter getVCardWriter(Writer sw, VCardVersion version) {
		return new VCardWriter(sw, version);
	}

	protected StreamReader getVCardReader(File file) throws FileNotFoundException {
		return new VCardReader(file);
	}

	public File[] listFiles(VCardVersion version, String extension, String... excludes) {
		return listFiles(getSampleDir(), "v" + version + "." + extension, excludes);
	}

	public static File[] listFiles(File dir, String extension, String... excludes) {
		return dir.listFiles(new Filter(extension, excludes));
	}

	private static List<VCard> convert(StreamReader reader, StreamWriter writer, VCardVersion version) throws IOException {
		try {
			List<VCard> result = reader.readAll();
			for (VCard vcard : result) {
				// Remove any property that is not supported by the version of VCard to be round-tripped to
				for (VCardProperty prop : vcard.getProperties()) {
					if (!prop.isSupportedBy(version)) {
						vcard.removeProperty(prop);
					}
				}
				vcard.setVersion(version);
				if (writer != null) {
					writer.write(vcard);
				}
			}
			return result;
		} finally {
			if (writer != null) {
				writer.close();
			}
			reader.close();
		}
	}

	private String read(String fileName) throws IOException {
		return IOUtils.getFileContents(new File(getSampleDir(), fileName));
	}

	private void write(String fileName, String converted) throws IOException {
		Writer writer = IOUtils.utf8Writer(new File(getSampleDir(), fileName));
		try {
			writer.write(converted);
		} finally {
			writer.close();
		}
	}
}