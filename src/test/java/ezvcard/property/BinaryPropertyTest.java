package ezvcard.property;

import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertEqualsMethod;
import static ezvcard.property.PropertySensei.assertNothingIsEqual;
import static ezvcard.property.PropertySensei.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import ezvcard.parameter.ImageType;

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
public class BinaryPropertyTest {
	@Test
	public void constructors() throws Exception {
		BinaryPropertyImpl property = new BinaryPropertyImpl();
		assertNull(property.getContentType());
		assertNull(property.getUrl());
		assertNull(property.getData());

		property = new BinaryPropertyImpl(new byte[0], ImageType.JPEG);
		assertEquals(ImageType.JPEG, property.getContentType());
		assertNull(property.getUrl());
		assertEquals(0, property.getData().length);

		property = new BinaryPropertyImpl("one", ImageType.PNG);
		assertEquals(ImageType.PNG, property.getContentType());
		assertEquals("one", property.getUrl());
		assertNull(property.getData());

		File file = new File("pom.xml");
		property = new BinaryPropertyImpl(file, ImageType.JPEG);
		assertEquals(ImageType.JPEG, property.getContentType());
		assertNull(property.getUrl());
		assertEquals(file.length(), property.getData().length);

		InputStream in = new ByteArrayInputStream("data".getBytes());
		property = new BinaryPropertyImpl(in, ImageType.JPEG);
		assertEquals(ImageType.JPEG, property.getContentType());
		assertNull(property.getUrl());
		assertEquals("data", new String(property.getData()));
	}

	@Test
	public void set_value() {
		BinaryPropertyImpl property = new BinaryPropertyImpl();

		property.setUrl("one", ImageType.PNG);
		assertEquals(ImageType.PNG, property.getContentType());
		assertEquals("one", property.getUrl());
		assertNull(property.getData());

		property.setData("data".getBytes(), ImageType.JPEG);
		assertEquals(ImageType.JPEG, property.getContentType());
		assertNull(property.getUrl());
		assertEquals("data", new String(property.getData()));

		property.setUrl("one", ImageType.PNG);
		assertEquals(ImageType.PNG, property.getContentType());
		assertEquals("one", property.getUrl());
		assertNull(property.getData());
	}

	@Test
	public void validate() {
		BinaryPropertyImpl empty = new BinaryPropertyImpl();
		assertValidate(empty).run(8);

		BinaryPropertyImpl withUrl = new BinaryPropertyImpl();
		withUrl.setUrl("http://example.com/image.jpg", ImageType.JPEG);
		assertValidate(withUrl).run();

		BinaryPropertyImpl withData = new BinaryPropertyImpl();
		withData.setData("data".getBytes(), ImageType.JPEG);
		assertValidate(withData).run();
	}

	@Test
	public void toStringValues() {
		BinaryPropertyImpl property = new BinaryPropertyImpl();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		BinaryPropertyImpl original = new BinaryPropertyImpl();
		assertCopy(original);

		original = new BinaryPropertyImpl("one", ImageType.PNG);
		assertCopy(original);

		original = new BinaryPropertyImpl("data".getBytes(), ImageType.JPEG);
		assertCopy(original).notSame("getData");
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new BinaryPropertyImpl(),
			new BinaryPropertyImpl("value", ImageType.PNG),
			new BinaryPropertyImpl("value2", ImageType.PNG),
			new BinaryPropertyImpl("value", ImageType.JPEG),
			new BinaryPropertyImpl("value", null),
			new BinaryPropertyImpl("data".getBytes(), ImageType.PNG),
			new BinaryPropertyImpl("data2".getBytes(), ImageType.PNG),
			new BinaryPropertyImpl("data".getBytes(), ImageType.JPEG),
			new BinaryPropertyImpl("data".getBytes(), null)
		);

		assertEqualsMethod(BinaryPropertyImpl.class)
		.constructor().test()
		.constructor("value", ImageType.PNG).test()
		.constructor(new Class<?>[]{String.class, ImageType.class}, "value", null).test()
		.constructor("data".getBytes(), ImageType.PNG).test()
		.constructor(new Class<?>[]{byte[].class, ImageType.class}, "data".getBytes(), null).test();
		//@formatter:on
	}

	public static class BinaryPropertyImpl extends BinaryProperty<ImageType> {
		public BinaryPropertyImpl() {
			super();
		}

		public BinaryPropertyImpl(String url, ImageType type) {
			super(url, type);
		}

		public BinaryPropertyImpl(byte[] data, ImageType type) {
			super(data, type);
		}

		public BinaryPropertyImpl(InputStream in, ImageType type) throws IOException {
			super(in, type);
		}

		public BinaryPropertyImpl(File file, ImageType type) throws IOException {
			super(file, type);
		}

		public BinaryPropertyImpl(BinaryPropertyImpl original) {
			super(original);
		}
	}
}
