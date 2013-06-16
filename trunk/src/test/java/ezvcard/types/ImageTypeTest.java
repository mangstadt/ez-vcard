package ezvcard.types;

import static ezvcard.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezvcard.parameters.ImageTypeParameter;
import ezvcard.util.HtmlUtils;

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
public class ImageTypeTest {
	@Test
	public void buildTypeObject_predefined_type() {
		ImageType image = new ImageType("IMAGE");
		assertTrue(ImageTypeParameter.JPEG == image.buildTypeObj("jpeg"));
	}

	@Test
	public void buildTypeObject_predefined_type_case_insensitive() {
		ImageType image = new ImageType("IMAGE");
		assertTrue(ImageTypeParameter.JPEG == image.buildTypeObj("Jpeg"));
	}

	@Test
	public void buildTypeObject_unknown_type() {
		ImageType image = new ImageType("IMAGE");
		ImageTypeParameter foo = image.buildTypeObj("foo");
		assertEquals("foo", foo.getValue());
		assertEquals("image/foo", foo.getMediaType());
	}

	@Test
	public void buildMediaTypeObject_predefined_type() {
		ImageType image = new ImageType("IMAGE");
		assertTrue(ImageTypeParameter.JPEG == image.buildMediaTypeObj("image/jpeg"));
	}

	@Test
	public void buildMediaTypeObject_predefined_type_case_insensitive() {
		ImageType image = new ImageType("IMAGE");
		assertTrue(ImageTypeParameter.JPEG == image.buildMediaTypeObj("Image/Jpeg"));
	}

	@Test
	public void buildMediaTypeObject_unknown_type() {
		ImageType image = new ImageType("IMAGE");
		ImageTypeParameter foo = image.buildMediaTypeObj("image/foo");
		assertEquals("foo", foo.getValue());
		assertEquals("image/foo", foo.getMediaType());
	}

	@Test
	public void buildMediaTypeObject_unknown_type_nothing_after_slash() {
		ImageType image = new ImageType("IMAGE");
		ImageTypeParameter foo = image.buildMediaTypeObj("image/");
		assertEquals("", foo.getValue());
		assertEquals("image/", foo.getMediaType());
	}

	@Test
	public void buildMediaTypeObject_unknown_type_no_slash() {
		ImageType image = new ImageType("IMAGE");
		ImageTypeParameter foo = image.buildMediaTypeObj("image");
		assertEquals("", foo.getValue());
		assertEquals("image", foo.getMediaType());
	}

	@Test
	public void unmarshalHtml() {
		List<String> warnings = new ArrayList<String>();
		ImageType image = new ImageType("IMAGE");
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<img src=\"image.jpg\" />");

		image.unmarshalHtml(element, warnings);

		assertEquals("image.jpg", image.getUrl());
		assertWarnings(0, warnings);
	}
}
