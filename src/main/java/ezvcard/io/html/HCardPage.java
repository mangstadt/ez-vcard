package ezvcard.io.html;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.parameter.ImageType;
import ezvcard.property.Photo;
import ezvcard.util.DataUri;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

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
 * <p>
 * Writes {@link VCard} objects to a templated HTML page (hCard format).
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard1 = ...
 * VCard vcard2 = ...
 * 
 * HCardPage page = new HCardPage();
 * page.add(vcard1);
 * page.add(vcard2);
 * 
 * File file = new File("hcard.html");
 * page.write(file);
 * </pre>
 * @author Michael Angstadt
 * @see <a
 * href="http://microformats.org/wiki/hcard">http://microformats.org/wiki/hcard</a>
 */
public class HCardPage {
	private static final Template template;
	static {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		cfg.setClassForTemplateLoading(HCardPage.class, "");
		cfg.setWhitespaceStripping(true);
		try {
			template = cfg.getTemplate("hcard-template.html");
		} catch (IOException e) {
			//should never be thrown because it's always on the classpath
			throw new RuntimeException(e);
		}
	}

	private final List<VCard> vcards = new ArrayList<VCard>();

	/**
	 * Adds a vCard to the HTML page.
	 * @param vcard the vCard to add
	 */
	public void add(VCard vcard) {
		vcards.add(vcard);
	}

	/**
	 * Writes the HTML document to a string.
	 * @return the HTML document
	 */
	public String write() {
		StringWriter sw = new StringWriter();
		try {
			write(sw);
		} catch (IOException e) {
			//should never thrown because we're writing to a string
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

	/**
	 * Writes the HTML document to an output stream.
	 * @param out the output stream
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(OutputStream out) throws IOException {
		write(new OutputStreamWriter(out));
	}

	/**
	 * Writes the HTML document to a file.
	 * @param file the file
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file) throws IOException {
		FileWriter writer = new FileWriter(file);
		try {
			write(writer);
		} finally {
			writer.close();
		}
	}

	/**
	 * Writes the HTML document to a writer.
	 * @param writer the writer
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void write(Writer writer) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("vcards", vcards);
		map.put("utils", new TemplateUtils());
		map.put("translucentBg", readImage("translucent-bg.png", ImageType.PNG));
		map.put("noProfile", readImage("no-profile.png", ImageType.PNG));
		map.put("ezVCardVersion", Ezvcard.VERSION);
		map.put("ezVCardUrl", Ezvcard.URL);
		map.put("scribeIndex", new ScribeIndex());
		try {
			template.process(map, writer);
		} catch (TemplateException e) {
			//this should never be thrown because we're always using the same template (it is hard-coded and cannot be changed by the user)
			throw new RuntimeException(e);
		}
		writer.flush();
	}

	/**
	 * Reads an image from the classpath.
	 * @param name the file name, relative to this class
	 * @param mediaType the media type of the image
	 * @return the image
	 * @throws IOException
	 */
	private Photo readImage(String name, ImageType mediaType) throws IOException {
		return new Photo(getClass().getResourceAsStream(name), mediaType);
	}

	/**
	 * Utility functions for the freemarker template.
	 */
	public static class TemplateUtils {
		private final Pattern newlineRegex = Pattern.compile("\\r\\n|\\r|\\n");

		public String base64(String contentType, byte[] data) {
			return new DataUri(contentType, data).toString();
		}

		public String lineBreaks(String value) {
			return newlineRegex.matcher(value).replaceAll("<br />");
		}
	}
}
