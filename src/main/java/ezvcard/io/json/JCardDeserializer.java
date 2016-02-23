package ezvcard.io.json;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ezvcard.VCard;

public class JCardDeserializer extends JsonDeserializer<VCard> {
	
	public static void configureObjectMapper(ObjectMapper mapper) {
		Map<Class<?>, JsonDeserializer<?>> map = new HashMap<Class<?>, JsonDeserializer<?>>();
		map.put(VCard.class, new JCardDeserializer());
		mapper.registerModule(new SimpleModule("jcarddeserializer", Version.unknownVersion(), map));
	}

	@Override
	public VCard deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		JCardReader reader = new JCardReader(parser);
		try {
			return reader.readNext();
		} catch (JCardParseException e) {
			throw new JsonParseException("Error parsing JCard: " + e.getMessage(), parser.getCurrentLocation());
		}
	}

}
