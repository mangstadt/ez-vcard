package ezvcard.io.json;

import static ezvcard.VCardVersion.V4_0;
import static ezvcard.property.asserter.PropertyAsserter.assertSimpleProperty;
import static ezvcard.util.TestUtils.assertPropertyCount;
import static ezvcard.util.TestUtils.assertVersion;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ezvcard.VCard;


/**
 * @author Buddy Gorven
 */
public class JCardDeserializerTest {
	@Test
	public void deserialize_single_vcard() throws Throwable {

		ObjectMapper mapper = new ObjectMapper();
		VCard result = mapper.readValue(getClass().getResourceAsStream("jcard-example.json"), VCard.class);
		
		JCardReaderTest.validateExampleJCard(result);
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		String json =
		"[" +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"John Doe\"]" +
		    "]" +
		  "]," +
		  "[\"vcard\"," +
		    "[" +
		      "[\"version\", {}, \"text\", \"4.0\"]," +
		      "[\"fn\", {}, \"text\", \"Jane Doe\"]" +
		    "]" +
		  "]" +
		"]";
		//@formatter:on
		
		List<VCard> cards = new ObjectMapper().readValue(json, new TypeReference<List<VCard>>() {});

		assertEquals(2, cards.size());
		
		VCard vcard = cards.get(0);
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();
		//@formatter:on

		vcard = cards.get(1);
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("Jane Doe")
		.noMore();
		//@formatter:on
	}
}
