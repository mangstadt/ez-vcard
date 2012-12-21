package ezvcard.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.transform.TransformerException;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.SoundTypeParameter;
import ezvcard.types.CategoriesType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GenderType;
import ezvcard.types.GeoType;
import ezvcard.types.KindType;
import ezvcard.types.LanguageType;
import ezvcard.types.NicknameType;
import ezvcard.types.PhotoType;
import ezvcard.types.RevisionType;
import ezvcard.types.SoundType;
import ezvcard.types.SourceType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TimezoneType;
import ezvcard.types.TitleType;
import ezvcard.types.UidType;
import ezvcard.types.UrlType;

/**
 * Generates my own vCard.
 * @author Michael Angstadt
 */
public class MyVCard {
	public static void main(String[] args) throws Exception {
		VCard vcard = new VCard();

		vcard.setKind(KindType.individual());

		vcard.setGender(GenderType.male());

		vcard.addLanguage(new LanguageType("en-US"));

		StructuredNameType n = new StructuredNameType();
		n.setFamily("Angstadt");
		n.setGiven("Michael");
		n.addPrefix("Mr");
		vcard.setStructuredName(n);

		vcard.setFormattedName(new FormattedNameType("Michael Angstadt"));

		NicknameType nickname = new NicknameType();
		nickname.addValue("Mike");
		vcard.setNickname(nickname);

		vcard.addTitle(new TitleType("Software Engineer"));

		vcard.addEmail(new EmailType("mike.angstadt@gmail.com"));

		vcard.addUrl(new UrlType("http://mikeangstadt.name"));
		vcard.addUrl(new UrlType("http://code.google.com/p/ez-vcard"));

		CategoriesType categories = new CategoriesType();
		categories.addValue("Java software engineer");
		categories.addValue("vCard expert");
		categories.addValue("Nice guy!!");
		vcard.setCategories(categories);

		vcard.setGeo(new GeoType(39.95, -75.1667));

		vcard.setTimezone(new TimezoneType(-5, 0, "America/New_York"));

		PhotoType photo = new PhotoType(new File("portrait.jpg"), ImageTypeParameter.JPEG);
		vcard.addPhoto(photo);

		SoundType sound = new SoundType(new File("pronunciation.ogg"), SoundTypeParameter.OGG);
		vcard.addSound(sound);

		//vcard.setUid(UidType.random());
		vcard.setUid(new UidType("urn:uuid:dd418720-c754-4631-a869-db89d02b831b"));

		SourceType source = new SourceType();
		vcard.addSource(source);

		vcard.setRevision(RevisionType.now());

		File file = new File("mike-angstadt.vcf");
		writeVCard(vcard, file, VCardVersion.V3_0);

		System.out.println();

		file = new File("mike-angstadt.xml");
		writeXCard(vcard, file);
	}

	private static void writeVCard(VCard vcard, File file, VCardVersion version) throws IOException {
		vcard.getSources().get(0).setValue("http://mikeangstadt.name/" + file.getName());

		System.out.println("Writing " + file.getName() + "...");
		Writer writer = new FileWriter(file);
		VCardWriter vcw = new VCardWriter(writer, version);
		vcw.write(vcard);

		List<String> warnings = vcw.getWarnings();
		System.out.println("Completed with " + warnings.size() + " warnings.");
		for (String warning : warnings) {
			System.out.println("* " + warning);
		}
		writer.close();
	}

	private static void writeXCard(VCard vcard, File file) throws IOException, TransformerException {
		vcard.getSources().get(0).setValue("http://mikeangstadt.name/" + file.getName());

		System.out.println("Writing " + file.getName() + "...");
		Writer writer = new FileWriter(file);
		XCardDocument xcardDoc = new XCardDocument();
		xcardDoc.addVCard(vcard);
		xcardDoc.write(writer);

		List<String> warnings = xcardDoc.getWarnings();
		System.out.println("Completed with " + warnings.size() + " warnings.");
		for (String warning : warnings) {
			System.out.println("* " + warning);
		}
		writer.close();
	}
}