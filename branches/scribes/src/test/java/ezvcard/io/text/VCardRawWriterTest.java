package ezvcard.io.text;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardRawWriter.ProblemsListener;
import ezvcard.parameters.EncodingParameter;

/*
 Copyright (c) 2013, Michael Angstadt
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
@SuppressWarnings("resource")
public class VCardRawWriterTest {
	@Test
	public void writeBeginComponent() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeBeginComponent("COMP");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:COMP\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void writeEndComponent() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeEndComponent("COMP");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"END:COMP\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void writeVersion() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeVersion();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"VERSION:2.1\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/**
	 * The TYPE sub types for 2.1 vCards should look like this:
	 * <p>
	 * <code>ADR;WORK;DOM:</code>
	 * </p>
	 * 
	 * The TYPE sub types for 3.0 vCards should look like this:
	 * <p>
	 * <code>ADR;TYPE=work,dom:</code>
	 * </p>
	 */
	@Test
	public void type_parameter() throws Exception {
		Tests tests = new Tests();

		//@formatter:off
		tests.add(
		VCardVersion.V2_1,
		"PROP;ONE:\r\n" +
		"PROP;ONE;TWO:\r\n" +
		"PROP;ONE;TWO;THREE:\r\n"
		);
		
		tests.add(
		VCardVersion.V3_0,
		"PROP;TYPE=one:\r\n" +
		"PROP;TYPE=one,two:\r\n" +
		"PROP;TYPE=one,two,three:\r\n"
		);
		
		tests.add(
		VCardVersion.V4_0,
		"PROP;TYPE=one:\r\n" +
		"PROP;TYPE=one,two:\r\n" +
		"PROP;TYPE=one,two,three:\r\n"
		);
		//@formatter:on

		for (Object[] test : tests) {
			VCardVersion version = (VCardVersion) test[0];
			String expected = (String) test[1];

			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);

			VCardSubTypes parameters = new VCardSubTypes();
			parameters.addType("one");
			writer.writeProperty(null, "PROP", parameters, "");

			parameters = new VCardSubTypes();
			parameters.addType("one");
			parameters.addType("two");
			writer.writeProperty(null, "PROP", parameters, "");

			parameters = new VCardSubTypes();
			parameters.addType("one");
			parameters.addType("two");
			parameters.addType("three");
			writer.writeProperty(null, "PROP", parameters, "");

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}

	@Test
	public void parameters() throws Exception {
		Tests tests = new Tests();

		//@formatter:off
		tests.add(
		VCardVersion.V2_1,
		"PROP;SINGLE=one:\r\n" +
		"PROP;MULTIPLE=one;MULTIPLE=two:\r\n" +
		"PROP;SINGLE=one;MULTIPLE=one;MULTIPLE=two:\r\n"
		);
		
		tests.add(
		VCardVersion.V3_0,
		"PROP;SINGLE=one:\r\n" +
		"PROP;MULTIPLE=one,two:\r\n" +
		"PROP;SINGLE=one;MULTIPLE=one,two:\r\n"
		);
		
		tests.add(
		VCardVersion.V4_0,
		"PROP;SINGLE=one:\r\n" +
		"PROP;MULTIPLE=one,two:\r\n" +
		"PROP;SINGLE=one;MULTIPLE=one,two:\r\n"
		);
		//@formatter:on

		for (Object[] test : tests) {
			VCardVersion version = (VCardVersion) test[0];
			String expected = (String) test[1];

			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);

			VCardSubTypes parameters = new VCardSubTypes();
			parameters.put("SINGLE", "one");
			writer.writeProperty(null, "PROP", parameters, "");

			parameters = new VCardSubTypes();
			parameters.put("MULTIPLE", "one");
			parameters.put("MULTIPLE", "two");
			writer.writeProperty(null, "PROP", parameters, "");

			parameters = new VCardSubTypes();
			parameters.put("SINGLE", "one");
			parameters.put("MULTIPLE", "one");
			parameters.put("MULTIPLE", "two");
			writer.writeProperty(null, "PROP", parameters, "");

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}

	@Test
	public void parameters_special_chars() throws Exception {
		Tests tests = new Tests();

		//2.1 without caret escaping
		//removes , : = [ ] FS
		//replaces \ with \\
		//replaces ; with \;
		//replaces newline with space
		tests.add(VCardVersion.V2_1, false, "PROP;X-TEST=^�\\\\\\;\"\t ;X-TEST=normal:\r\n");

		//2.1 with caret escaping (ignored)
		//removes , : = [ ] FS
		//replaces \ with \\
		//replaces ; with \;
		//replaces newline with space
		tests.add(VCardVersion.V2_1, true, "PROP;X-TEST=^�\\\\\\;\"\t ;X-TEST=normal:\r\n");

		//3.0 without caret escaping
		//removes FS
		//replaces \ with \\
		//replaces newline with space
		//replaces " with '
		//surrounds in double quotes, since it contains , ; or :
		tests.add(VCardVersion.V3_0, false, "PROP;X-TEST=\"^�\\,;:=[]'\t \",normal:\r\n");

		//3.0 with caret escaping (same as 4.0)
		//removes FS
		//replaces ^ with ^^
		//replaces newline with ^n
		//replaces " with ^'
		//surrounds in double quotes, since it contains , ; or :
		tests.add(VCardVersion.V3_0, true, "PROP;X-TEST=\"^^�\\,;:=[]^'\t^n\",normal:\r\n");

		//4.0 without caret escaping
		//removes FS
		//replaces \ with \\
		//replaces newline with \n
		//replaces " with '
		//surrounds in double quotes, since it contains , ; or :
		tests.add(VCardVersion.V4_0, false, "PROP;X-TEST=\"^�\\,;:=[]'\t\\n\",normal:\r\n");

		//4.0 with caret escaping
		//removes FS
		//replaces ^ with ^^
		//replaces newline with ^n
		//replaces " with ^'
		//surrounds in double quotes, since it contains , ; or :
		tests.add(VCardVersion.V4_0, true, "PROP;X-TEST=\"^^�\\,;:=[]^'\t^n\",normal:\r\n");

		for (Object[] test : tests) {
			VCardVersion version = (VCardVersion) test[0];
			boolean caretEncodingEnabled = (Boolean) test[1];
			String expected = (String) test[2];

			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);
			writer.setCaretEncodingEnabled(caretEncodingEnabled);
			ProblemsListenerImpl listener = new ProblemsListenerImpl();
			writer.setProblemsListener(listener);

			VCardSubTypes parameters = new VCardSubTypes();
			parameters.put("X-TEST", "^�\\,;:=[]\"\t\n" + ((char) 28));
			parameters.put("X-TEST", "normal");
			writer.writeProperty(null, "PROP", parameters, "");

			assertEquals(1, listener.onParameterValueChanged);

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}

	@Test
	public void foldingScheme() throws Exception {
		StringWriter sw = new StringWriter();
		FoldingScheme fs = new FoldingScheme(50, "  ");
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1, fs);

		writer.writeProperty("PROP", "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"PROP:The vCard MIME Directory Profile also provide\r\n" +
		"  s support for representing other important infor\r\n" +
		"  mation about the person associated with the dire\r\n" +
		"  ctory entry. For instance, the date of birth of \r\n" +
		"  the person; an audio clip describing the pronunc\r\n" +
		"  iation of the name associated with the directory \r\n" +
		"  entry, or some other application of the digital \r\n" +
		"  sound; longitude and latitude geo-positioning in\r\n" +
		"  formation related to the person associated with \r\n" +
		"  the directory entry; date and time that the dire\r\n" +
		"  ctory information was last updated; annotations \r\n" +
		"  often written on a business card; Uniform Resour\r\n" +
		"  ce Locators (URL) for a website; public key info\r\n" +
		"  rmation.\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void no_foldingScheme() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1, null);

		writer.writeProperty("PROP", "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"PROP:The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void newline() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1, FoldingScheme.MIME_DIR, "*");

		writer.writeProperty("PROP", "one");
		writer.writeProperty("PROP", "two");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"PROP:one*" +
		"PROP:two*";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void groups() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeProperty("group1", "PROP", new VCardSubTypes(), "");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"group1.PROP:\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalid_group_name() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);
		writer.writeProperty("invalid*name", "PROP", new VCardSubTypes(), "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalid_property_name() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);
		writer.writeProperty("invalid*name", "");
	}

	/**
	 * If newline characters exist in a property value in 2.1, then that
	 * property value should be "quoted-printable" encoded. The escape sequence
	 * "\n" should ONLY be used for 3.0 and 4.0. See the "Delimiters" subsection
	 * in section 2 of the 2.1 specs.
	 */
	@Test
	public void newlines_in_property_values() throws Exception {
		Tests tests = new Tests();
		tests.add(VCardVersion.V2_1, "PROP;ENCODING=quoted-printable:one=0D=0Atwo\r\n");
		tests.add(VCardVersion.V3_0, "PROP:one\\ntwo\r\n");
		tests.add(VCardVersion.V4_0, "PROP:one\\ntwo\r\n");

		for (Object test[] : tests) {
			VCardVersion version = (VCardVersion) test[0];
			String expected = (String) test[1];

			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);

			writer.writeProperty("PROP", "one\r\ntwo");

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}

	/**
	 * Property values that use "quoted-printable" encoding must include a "="
	 * at the end of the line if the next line is folded.
	 */
	@Test
	public void quoted_printable_line() throws Exception {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1, new FoldingScheme(40, " "));

		VCardSubTypes parameters = new VCardSubTypes();
		parameters.setEncoding(EncodingParameter.QUOTED_PRINTABLE);

		writer.writeProperty(null, "PROP", parameters, "quoted-printable \r\nline");
		writer.writeProperty(null, "PROP", parameters, "short");
		writer.close();

		//@formatter:off
		String expected =
		"PROP;ENCODING=quoted-printable:quoted-p=\r\n" +
		" rintable =0D=0Aline\r\n" +
		"PROP;ENCODING=quoted-printable:short\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	private class ProblemsListenerImpl implements ProblemsListener {
		private int onParameterValueChanged = 0;

		public final void onParameterValueChanged(String propertyName, String parameterName, String originalValue, String modifiedValue) {
			onParameterValueChanged++;
		}
	}

	private class Tests implements Iterable<Object[]> {
		private List<Object[]> tests = new ArrayList<Object[]>();

		public Tests add(Object... test) {
			tests.add(test);
			return this;
		}

		public Iterator<Object[]> iterator() {
			return tests.iterator();
		}
	}
}
