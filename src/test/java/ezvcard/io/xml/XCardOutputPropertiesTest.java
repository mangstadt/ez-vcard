package ezvcard.io.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.junit.Test;

public class XCardOutputPropertiesTest {
	private static final String INDENT_AMT = "{http://xml.apache.org/xslt}indent-amount";

	@Test
	public void constructor_empty() {
		XCardOutputProperties properties = new XCardOutputProperties();

		Map<String, String> expected = new HashMap<String, String>();
		expected.put(OutputKeys.METHOD, "xml");

		assertEquals(expected, properties);
	}

	@Test
	public void constructor() {
		XCardOutputProperties properties = new XCardOutputProperties(1, "1.1");

		Map<String, String> expected = new HashMap<String, String>();
		expected.put(OutputKeys.METHOD, "xml");
		expected.put(OutputKeys.INDENT, "yes");
		expected.put(INDENT_AMT, "1");
		expected.put(OutputKeys.VERSION, "1.1");

		assertEquals(expected, properties);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_negative_indent() {
		new XCardOutputProperties(-1, "1.1");
	}

	@Test
	public void setIndent() {
		XCardOutputProperties properties = new XCardOutputProperties();
		properties.setIndent(1);

		Map<String, String> expected = new HashMap<String, String>();
		expected.put(OutputKeys.METHOD, "xml");
		expected.put(OutputKeys.INDENT, "yes");
		expected.put(INDENT_AMT, "1");

		assertEquals(expected, properties);
	}

	@Test
	public void setIndent_remove() {
		XCardOutputProperties properties = new XCardOutputProperties();
		properties.setIndent(1);
		properties.setIndent(null);

		Map<String, String> expected = new HashMap<String, String>();
		expected.put(OutputKeys.METHOD, "xml");

		assertEquals(expected, properties);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIndent_negative() {
		XCardOutputProperties properties = new XCardOutputProperties();
		properties.setIndent(-1);
	}

	@Test
	public void getIndent() {
		XCardOutputProperties properties = new XCardOutputProperties();

		assertNull(properties.getIndent());

		properties.put(INDENT_AMT, "1");
		assertNull(properties.getIndent());

		properties.put(OutputKeys.INDENT, "no");
		assertNull(properties.getIndent());

		properties.put(OutputKeys.INDENT, "YES");
		assertNull(properties.getIndent());

		properties.put(OutputKeys.INDENT, "FOO");
		assertNull(properties.getIndent());

		properties.put(OutputKeys.INDENT, "yes");
		assertEquals(Integer.valueOf(1), properties.getIndent());

		properties.remove(INDENT_AMT);
		assertNull(properties.getIndent());
	}

	@Test(expected = NumberFormatException.class)
	public void getIndent_not_an_integer() {
		XCardOutputProperties properties = new XCardOutputProperties();
		properties.put(OutputKeys.INDENT, "yes");
		properties.put(INDENT_AMT, "foo");
		properties.getIndent();
	}

	@Test
	public void setXmlVersion() {
		XCardOutputProperties properties = new XCardOutputProperties();
		properties.setXmlVersion("1.1");

		Map<String, String> expected = new HashMap<String, String>();
		expected.put(OutputKeys.METHOD, "xml");
		expected.put(OutputKeys.VERSION, "1.1");

		assertEquals(expected, properties);
	}

	@Test
	public void setXmlVersion_remove() {
		XCardOutputProperties properties = new XCardOutputProperties();
		properties.setXmlVersion("1.1");
		properties.setXmlVersion(null);

		Map<String, String> expected = new HashMap<String, String>();
		expected.put(OutputKeys.METHOD, "xml");

		assertEquals(expected, properties);
	}

	@Test
	public void getXmlVersion() {
		XCardOutputProperties properties = new XCardOutputProperties();
		assertNull(properties.getXmlVersion());

		properties.setXmlVersion("1.1");
		assertEquals("1.1", properties.getXmlVersion());
	}
}
