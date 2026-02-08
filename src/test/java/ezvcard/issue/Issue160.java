package ezvcard.issue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Photo;
import ezvcard.util.org.apache.commons.codec.binary.Base64;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/160"
 */
public class Issue160 {
	@Test
	public void test() {
		String vcardStr = //@formatter:off
		"BEGIN:VCARD\r\n" +
        "VERSION:3.0\r\n" +
        "PHOTO;VALUE=uri:data:image/png;base64,dGVzdA==\r\n" +
        "END:VCARD"; //@formatter:on

		VCard vcard = Ezvcard.parse(vcardStr).first();
		Photo photo = vcard.getPhotos().get(0);

		assertEquals("image/png", photo.getContentType().getMediaType());
		assertArrayEquals(Base64.decodeBase64("dGVzdA=="), photo.getData());
	}
}
