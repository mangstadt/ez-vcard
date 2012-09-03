package ezvcard.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.SoundTypeParameter;
import ezvcard.types.CategoriesType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GeoType;
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

		EmailType email = new EmailType("mike.angstadt@gmail.com");
		vcard.addEmail(email);

		UrlType url = new UrlType("http://mikeangstadt.name");
		vcard.addUrl(url);

		CategoriesType categories = new CategoriesType();
		categories.addValue("Java software engineer");
		categories.addValue("vCard expert");
		categories.addValue("Nice guy!!");
		vcard.setCategories(categories);

		vcard.setGeo(new GeoType(39.95, -75.1667));

		vcard.setTimezone(new TimezoneType(-5, 0, "America/New_York"));

		byte portrait[] = getFileBytes("portrait.jpg");
		PhotoType photo = new PhotoType(portrait, ImageTypeParameter.JPEG);
		vcard.addPhoto(photo);

		byte pronunciation[] = getFileBytes("pronunciation.ogg");
		SoundType sound = new SoundType(pronunciation, SoundTypeParameter.OGG);
		vcard.addSound(sound);

		//vcard.setUid(UidType.random());
		vcard.setUid(new UidType("urn:uuid:dd418720-c754-4631-a869-db89d02b831b"));

		vcard.addSource(new SourceType("http://mikeangstadt.name/mike-angstadt.vcf"));

		vcard.setRevision(new RevisionType(new Date()));

		//write vCard to file
		File file = new File("mike-angstadt.vcf");
		System.out.println("Writing " + file.getName() + "...");
		Writer writer = new FileWriter(file);
		VCardWriter vcw = new VCardWriter(writer, VCardVersion.V3_0);
		vcw.write(vcard);
		List<String> warnings = vcw.getWarnings();
		System.out.println("Completed with " + warnings.size() + " warnings.");
		for (String warning : warnings) {
			System.out.println("* " + warning);
		}
		writer.close();
		
		System.out.println();
		
		file = new File("mike-angstadt.xml");
		System.out.println("Writing " + file.getName() + "...");
		writer = new FileWriter(file);
		XCardMarshaller xcm = new XCardMarshaller();
		xcm.addVCard(vcard);
		xcm.write(writer);
		warnings = xcm.getWarnings();
		System.out.println("Completed with " + warnings.size() + " warnings.");
		for (String warning : warnings) {
			System.out.println("* " + warning);
		}
		writer.close();
	}

	private static byte[] getFileBytes(String path) throws IOException {
		File file = new File(path);
		byte data[] = new byte[(int) file.length()];
		InputStream in = new FileInputStream(file);
		in.read(data);
		in.close();
		return data;
	}
}
