package ezvcard.io.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.junit.Test;

/*
 Copyright (c) 2012-2016, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
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
