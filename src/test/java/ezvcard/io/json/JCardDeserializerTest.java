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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ezvcard.VCard;


/**
 * @author Buddy Gorven
 */
public class JCardDeserializerTest {
	@Test
	public void deserialize_single_vcard() throws Throwable {

		ObjectMapper mapper = new ObjectMapper();
		JCardDeserializer.configureObjectMapper(mapper);
		VCard result = mapper.readValue(getClass().getResourceAsStream("jcard-example.json"), VCard.class);
		
		JCardReaderTest.validateExampleJCard(result);
	}
	
	public static class NestedVCard {
		private VCard contact;
		
		@JsonDeserialize(using=JCardDeserializer.class)
		public VCard getContact() {
			return contact;
		}

		@JsonSerialize(using=JCardSerializer.class)
		public void setContact(VCard vcard) {
			this.contact = vcard;
		}
	}
	
	@Test
	public void read_nested() throws Throwable {
		//@formatter:off
		String json =
		"{" +
		  "\"contact\": "+
		  "[" +
		    "\"vcard\"," +
			  "[" +
			    "[\"version\", {}, \"text\", \"4.0\"]," +
			    "[\"fn\", {}, \"text\", \"John Doe\"]" +
			  "]" +
		  "]" +
		"}";
		//@formatter:on
		
		NestedVCard nested = new ObjectMapper().readValue(json, NestedVCard.class);
		
		VCard vcard = nested.getContact();
		assertVersion(V4_0, vcard);
		assertPropertyCount(1, vcard);

		//@formatter:off
		assertSimpleProperty(vcard.getFormattedNames())
			.value("John Doe")
		.noMore();
		//@formatter:on
	}
	
	@Test
	public void read_nested_null() throws Throwable {
		//@formatter:off
		String json =
		"{" +
		  "\"contact\": null" +
		"}";
		//@formatter:on
		
		NestedVCard nested = new ObjectMapper().readValue(json, NestedVCard.class);
		
		VCard vcard = nested.getContact();
		assertEquals(null, vcard);
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
		
		ObjectMapper mapper = new ObjectMapper();
		JCardDeserializer.configureObjectMapper(mapper);
		List<VCard> cards = mapper.readValue(json, new TypeReference<List<VCard>>() {});

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
