package ezvcard.io.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ezvcard.VCard;

public class JCardDeserializer extends JsonDeserializer<VCard> {

	@Override
	public VCard deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		return new JCardReader(parser).readNext();
	}

}
