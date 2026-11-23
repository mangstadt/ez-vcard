package ezvcard.issue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Photo;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/160"
 */
public class Issue160 {
	@Test
	void test() {
		String vcardStr = //@formatter:off
		"BEGIN:VCARD\r\n" +
        "VERSION:3.0\r\n" +
        "PHOTO;VALUE=uri:data:image/png;base64,dGVzdA==\r\n" +
        "END:VCARD"; //@formatter:on

		VCard vcard = Ezvcard.parse(vcardStr).first();
		Photo photo = vcard.getPhotos().get(0);

		assertEquals("image/png", photo.getContentType().getMediaType());
		assertArrayEquals(Base64.getDecoder().decode("dGVzdA=="), photo.getData());
	}
}
