package ezvcard.issue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.WriteContext;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.VCardProperty;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/ez-vcard/issues/119"
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Issue119 {
	@Test
	public void faq_code_sample() throws Exception {
		VCard vcard = new VCard();
		vcard.setFormattedName("value");
		
		ScribeIndex index = new ScribeIndex();
		WriteContext context = new WriteContext(VCardVersion.V3_0, null, true);
		
		List<String> actual = new ArrayList<>();
		for (VCardProperty property : vcard) {
			VCardPropertyScribe scribe = index.getPropertyScribe(property); //compiler: raw type warning
			String name = scribe.getPropertyName();
			String value = scribe.writeText(property, context); //compiler: type safety warning
			actual.add(name + " = " + value);
		}

		List<String> expected = Arrays.asList("FN = value");
		assertEquals(expected, actual);
	}

	@Test(expected = ClassCastException.class)
	public void class_cast_exception() {
		ScribeIndex index = new ScribeIndex();
		WriteContext context = new WriteContext(VCardVersion.V3_0, null, true);
		VCardProperty property = new FormattedName("value");
		VCardPropertyScribe scribe = index.getPropertyScribe(Address.class); //compiler: raw type warning
		scribe.writeText(property, context); //compiler: type safety warning; ClassCastException thrown at runtime
	}
}
