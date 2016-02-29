package ezvcard.io.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ezvcard.VCard;

public class JCardDeserializer extends JsonDeserializer<VCard> {

	/**
	 * Configures an {@link ObjectMapper} with the abillity to deserialize JCard
	 * formatted data into {@link VCard}s, e.g:<br/>
	 * 
	 * <pre>
	 * ObjectMapper mapper = new ObjectMapper();
	 * JCardDeserializer.configureObjectMapper(mapper);
	 * VCard vcard = mapper.readValue("[\"vcard\",[[\"fn\", {}, \"text\", \"John Doe\"]]]", VCard.class);
	 * </pre>
	 * 
	 * @param mapper
	 *            the mapper to add the jcard deserialization module to
	 */
	public static void configureObjectMapper(ObjectMapper mapper) {
		Map<Class<?>, JsonDeserializer<?>> map = new HashMap<Class<?>, JsonDeserializer<?>>();
		map.put(VCard.class, new JCardDeserializer());
		mapper.registerModule(new SimpleModule("jcarddeserializer", Version.unknownVersion(), map));
	}

	@Override
	public VCard deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		return new JCardReader(parser).readNext();
	}

}
