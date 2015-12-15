package ezvcard.io.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ezvcard.VCard;

public class JCardDeserializer extends JsonDeserializer<VCard> {

	@Override
	public VCard deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		JCardReader reader = new JCardReader(parser);
		try {
			return reader.readNext();
		} catch (JCardParseException e) {
			throw new JsonParseException("Error parsing JCard: " + e.getMessage(), parser.getCurrentLocation());
		} finally {
			reader.close();
		}
	}

}
