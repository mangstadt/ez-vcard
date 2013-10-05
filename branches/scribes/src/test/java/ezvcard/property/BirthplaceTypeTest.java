package ezvcard.property;

import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCardVersion;

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
public class BirthplaceTypeTest {
	private final String text = "Philadelphia, PA";
	private final String uri = "geo:39.970806,-75.174809";

	@Test
	public void validate() {
		Birthplace empty = new Birthplace();
		assertValidate(empty).versions(VCardVersion.V2_1).run(2);
		assertValidate(empty).versions(VCardVersion.V3_0).run(2);
		assertValidate(empty).versions(VCardVersion.V4_0).run(1);

		Birthplace withText = new Birthplace();
		withText.setText(text);
		assertValidate(withText).versions(VCardVersion.V2_1).run(1);
		assertValidate(withText).versions(VCardVersion.V3_0).run(1);
		assertValidate(withText).versions(VCardVersion.V4_0).run(0);

		Birthplace withUri = new Birthplace();
		withUri.setUri(uri);
		assertValidate(withUri).versions(VCardVersion.V2_1).run(1);
		assertValidate(withUri).versions(VCardVersion.V3_0).run(1);
		assertValidate(withUri).versions(VCardVersion.V4_0).run(0);
	}

	@Test
	public void setUri() {
		Birthplace property = new Birthplace();

		assertNull(property.getUri());

		property.setText(text);
		property.setUri(uri);

		assertEquals(uri, property.getUri());
		assertNull(property.getText());
	}

	@Test
	public void setText() {
		Birthplace property = new Birthplace();

		assertNull(property.getText());

		property.setUri(uri);
		property.setText(text);

		assertEquals(text, property.getText());
		assertNull(property.getUri());
	}
}
