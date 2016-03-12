package ezvcard.io.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ezvcard.VCard;

public class NestedVCard {
	private VCard contact;

	@JsonSerialize(using = JCardSerializer.class)
	@JCardFormat(addProdId = false)
	public VCard getContact() {
		return contact;
	}

	@JsonDeserialize(using = JCardDeserializer.class)
	public void setContact(VCard vcard) {
		this.contact = vcard;
	}
}
