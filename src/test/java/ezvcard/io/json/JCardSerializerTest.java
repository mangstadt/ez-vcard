package ezvcard.io.json;

import static ezvcard.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ezvcard.VCard;

/**
 * @author Buddy Gorven
 */
public class JCardSerializerTest {

	@Test
	public void serialize_indented() throws Throwable {
		VCard vcard1 = new VCard();
		vcard1.setFormattedName("John Doe");

		VCard vcard2 = new VCard();
		vcard2.setFormattedName("John Doe");

		ObjectMapper mapper = getMapper();

		final DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		mapper.setDefaultPrettyPrinter(pp);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		String result = mapper.writeValueAsString(Arrays.asList(vcard1, vcard2));

		//@formatter:off
		String expected =
		"[" + NEWLINE + 
		"  [" + NEWLINE + 
		"    \"vcard\"," + NEWLINE + 
		"    [" + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"version\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"4.0\"" + NEWLINE + 
		"      ]," + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"fn\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"John Doe\"" + NEWLINE + 
		"      ]" + NEWLINE + 
		"    ]" + NEWLINE + 
		"  ]," + NEWLINE + 
		"  [" + NEWLINE + 
		"    \"vcard\"," + NEWLINE + 
		"    [" + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"version\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"4.0\"" + NEWLINE + 
		"      ]," + NEWLINE + 
		"      [" + NEWLINE + 
		"        \"fn\"," + NEWLINE + 
		"        { }," + NEWLINE + 
		"        \"text\"," + NEWLINE + 
		"        \"John Doe\"" + NEWLINE + 
		"      ]" + NEWLINE + 
		"    ]" + NEWLINE + 
		"  ]" + NEWLINE + 
		"]";
		//@formatter:on
		assertEquals(expected, result);
	}

	@Test
	public void serialize_single_vcard() throws Throwable {

		VCard example = JCardWriterTest.createExample();
		String actual = getMapper().writeValueAsString(example);
		JCardWriterTest.assertExample(actual, "jcard-example.json");
	}

	@Test
	public void serialize_nested() throws Throwable {
		NestedVCard nested = new NestedVCard();
		nested.setContact(JCardWriterTest.createExample());

		StringWriter result = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(result, nested);
		String nestedJson = result.toString();

		String actual = nestedJson.substring(nestedJson.indexOf('['), nestedJson.lastIndexOf(']') + 1);
		JCardWriterTest.assertExample(actual, "jcard-example.json");
	}

	@Test
	public void write_nested_null() throws Throwable {
		NestedVCard nested = new NestedVCard();

		StringWriter result = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(result, nested);
		String nestedJson = result.toString();

		assertEquals("{\"contact\":null}", nestedJson.replaceAll("\\s", ""));
	}

	@Test
	public void serialize_multiple_vcards() throws Throwable {

		List<VCard> cards = new ArrayList<VCard>();
		VCard vcard = new VCard();
		vcard.setFormattedName("John Doe");
		cards.add(vcard);

		vcard = new VCard();
		vcard.setFormattedName("Jane Doe");
		cards.add(vcard);

		String actual = getMapper().writeValueAsString(cards);

		//@formatter:off
		String expected =
		"[" +
			"[\"vcard\"," +
				"[" +
					"[\"version\",{},\"text\",\"4.0\"]," +
					"[\"fn\",{},\"text\",\"John Doe\"]" +
				"]" +
			"]," +
			"[\"vcard\"," +
				"[" +
					"[\"version\",{},\"text\",\"4.0\"]," +
					"[\"fn\",{},\"text\",\"Jane Doe\"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	private ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		final JCardModule module = new JCardModule();
		module.setAddProdId(false);
		mapper.registerModule(module);
		return mapper;
	}
}
