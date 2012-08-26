package ezvcard.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.CategoriesType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GeoType;
import ezvcard.types.NicknameType;
import ezvcard.types.PhotoType;
import ezvcard.types.RevisionType;
import ezvcard.types.SourceType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TimezoneType;
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

		EmailType email = new EmailType("mike.angstadt@gmail.com");
		email.addType(EmailTypeParameter.HOME);
		vcard.addEmail(email);

		TelephoneType tel = new TelephoneType("+1 555-555-1234");
		tel.addType(TelephoneTypeParameter.CELL);
		vcard.addTelephoneNumber(tel);

		tel = new TelephoneType("+1 555-555-9876");
		tel.addType(TelephoneTypeParameter.HOME);
		vcard.addTelephoneNumber(tel);

		UrlType url = new UrlType("http://mikeangstadt.name");
		url.setType("home");
		vcard.addUrl(url);

		url = new UrlType("http://code.google.com/p/ez-vcard");
		url.setType("work");
		vcard.addUrl(url);

		CategoriesType categories = new CategoriesType();
		categories.addValue("Java software engineer");
		categories.addValue("vCard expert");
		categories.addValue("Nice guy");
		vcard.setCategories(categories);

		vcard.setGeo(new GeoType(39.95, 75.1667));

		NicknameType nickname = new NicknameType();
		nickname.addValue("Mike");
		vcard.setNickname(nickname);

		vcard.setTimezone(new TimezoneType(-5, 0, "America/New_York"));

		File profile = new File("portrait.jpg");
		byte data[] = new byte[(int) profile.length()];
		InputStream in = new FileInputStream(profile);
		in.read(data);
		in.close();
		PhotoType photo = new PhotoType(data, ImageTypeParameter.JPEG);
		vcard.addPhoto(photo);

		//vcard.setUid(UidType.random());
		vcard.setUid(new UidType("urn:uuid:dd418720-c754-4631-a869-db89d02b831b"));

		vcard.addSource(new SourceType("http://mikeangstadt.name/mike-angstadt.vcf"));

		vcard.setRevision(new RevisionType(new Date()));

		vcard.setVersion(VCardVersion.V3_0);
		vcard.write(new File("mike-angstadt.vcf"));
	}

}
